package fr.cnrs.opentheso.bean.menu.connect;

import java.io.Serializable;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${info.application.version}")
    private String buildVersion;


    //Retourne la version actuelle d'Opentheso d'après le WAR
    public String getOpenthesoVersion() {
        return buildVersion;
    }

    @Bean
    public DataSource openConnexionPool() {
        var dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
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

}
