package fr.cnrs.opentheso.bean.menu.connect;

import java.io.Serializable;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;


@Slf4j
@Data
@Configuration
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

    @Value("${info.application.version}")
    private String buildVersion;


    //Retourne la version actuelle d'Opentheso d'après le WAR
    public String getOpenthesoVersion() {
        return buildVersion;
    }

    @Bean
    public DataSource openConnexionPool() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setMaximumPoolSize(maxPoolSize);
        dataSource.setMinimumIdle(minIdle);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setMaxLifetime(1800000);
        return dataSource;
    }

    public String getLocalUri() {
        var facesContext = FacesContext.getCurrentInstance();
        var request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        var protocol = request.isSecure() ? "https://" : "http://";
        var host = request.getHeader("host");
        var contextPath = request.getContextPath();

        return protocol + host + contextPath + "/";
    }

    public String status(){
        return "OK! Connected";
    }

    public int getTimeout() {
        return timeoutMin * 60 * 1000;
    }

}
