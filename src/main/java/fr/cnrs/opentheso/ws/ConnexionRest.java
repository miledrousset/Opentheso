/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServlet;
/**
 *
 * @author miled.rousset
 */
public class ConnexionRest extends HttpServlet{

    public ConnexionRest() {
    }

    public HikariDataSource getConnexion() {

        Properties properties = new Properties();
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("hikari.properties");
            if (inputStream != null) {
                properties.load(inputStream);
                return openConnexionPool(properties);
            }
        } catch (IOException ex) {
            System.err.println(ex.toString());
        }
        return null;
    }       

    private HikariDataSource openConnexionPool(Properties properties) {
        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("minimumIdle")));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("setMaximumPoolSize")));
        config.setAutoCommit(true);
        config.setIdleTimeout(Integer.parseInt(properties.getProperty("idleTimeout")));
        config.setConnectionTimeout(Integer.parseInt(properties.getProperty("connectionTimeout")));
        config.setConnectionTestQuery(properties.getProperty("connectionTestQuery"));
        config.setDataSourceClassName(properties.getProperty("dataSourceClassName"));

        config.addDataSourceProperty("user", properties.getProperty("dataSource.user"));
        config.addDataSourceProperty("password", properties.getProperty("dataSource.password"));
        config.addDataSourceProperty("databaseName", properties.getProperty("dataSource.databaseName"));
        config.addDataSourceProperty("serverName", properties.getProperty("dataSource.serverName"));
        config.addDataSourceProperty("portNumber", properties.getProperty("dataSource.serverPort"));

        HikariDataSource poolConnexion1 = new HikariDataSource(config);
        try {
            Connection conn = poolConnexion1.getConnection();

            if (conn == null) {
                return null;
            }
            conn.close();

        } catch (SQLException ex) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, ex.getClass().getName(), ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            poolConnexion1.close();
            return null;
        }
        return poolConnexion1;
    }
  
    
}
