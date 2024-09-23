package fr.cnrs.opentheso.bean.menu.connect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.Serializable;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Data
@Service
public class Connect implements Serializable{

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    @Value("${spring.datasource.dataSourceClassName}")
    private String dataSourceClassName;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.minIdle}")
    private int minIdle;

    @Value("${spring.datasource.maxPoolSize}")
    private int maxPoolSize;

    @Value("${spring.datasource.idleTimeout}")
    private int idleTimeout;

    @Value("${spring.datasource.connectionTimeout}")
    private int connectionTimeout;

    @Value("${settings.timeout:10}")
    private int timeoutMin;


    private HikariDataSource poolConnexion;
    private String localUri;


    //Retourne la version actuelle d'Opentheso d'après le WAR
    public String getOpenthesoVersionFromWar() {
        return FacesContext.getCurrentInstance().getExternalContext().getInitParameterMap().get("version");
    }

    private void openConnexionPool() {
        try {
            Properties props = new Properties();
            props.setProperty("dataSourceClassName", dataSourceClassName);
            props.setProperty("dataSource.user", username);
            props.setProperty("dataSource.password", password);

            // Regex pour capturer le serveur, le port et le nom de la base de données
            Matcher matcher = Pattern.compile("jdbc:postgresql://([^:]+):(\\d+)/(.+)").matcher(databaseUrl);
            matcher.matches();
            props.setProperty("dataSource.databaseName", matcher.group(3));
            props.setProperty("dataSource.serverName", matcher.group(1));
            props.setProperty("dataSource.portNumber", matcher.group(2));

            HikariConfig config = new HikariConfig(props);
            config.setMinimumIdle(minIdle);
            config.setMaximumPoolSize(maxPoolSize);
            config.setIdleTimeout(idleTimeout);
            config.setConnectionTimeout(connectionTimeout);
            config.setAutoCommit(true);

            poolConnexion = new HikariDataSource(config);
        } catch (Exception ex) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, ex.getClass().getName(), ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public String status(){
        if(poolConnexion != null)
            return "OK! Connected";
        else
            return "!! Failed !!";
    }

    public HikariDataSource getPoolConnexion() {
        if(ObjectUtils.isEmpty(poolConnexion) || poolConnexion.isClosed()) {
            openConnexionPool();
        }
        return poolConnexion;
    }

    public int getTimeout() {
        return timeoutMin * 60 * 1000;
    }

    public boolean isConnected(){
        return poolConnexion != null;
    }

}
