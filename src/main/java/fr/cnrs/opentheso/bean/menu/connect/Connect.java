package fr.cnrs.opentheso.bean.menu.connect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;
//import java.util.logging.Level;
import javax.annotation.PostConstruct;

import javax.faces.application.FacesMessage;


import javax.faces.context.FacesContext;
import javax.inject.Named;


@Named (value = "connect")
@javax.enterprise.context.ApplicationScoped
public class Connect {
   
   
    private HikariDataSource poolConnexion = null;
    private String workLanguage = "fr";
    private String defaultThesaurusId;

    
    
    @PostConstruct
    public void initPref() {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
        workLanguage = bundlePref.getString("workLanguage");
        defaultThesaurusId = bundlePref.getString("defaultThesaurusId");
        
    }    
    
    private ResourceBundle getBundlePool(){
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            ResourceBundle bundlePool = context.getApplication().getResourceBundle(context, "conHikari");
            return bundlePool;
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }
 
    public Connect() {
//        if(poolConnexion != null)
        openConnexionPool();
    }

    private void openConnexionPool() {
        ResourceBundle resourceBundle = getBundlePool();
        if(resourceBundle == null){
            // retour pour installation
            return;
        }

        Properties props = new Properties();
        props.setProperty("dataSourceClassName", resourceBundle.getString("dataSourceClassName"));
        props.setProperty("dataSource.user", resourceBundle.getString("dataSource.user"));
        props.setProperty("dataSource.password", resourceBundle.getString("dataSource.password"));
        props.setProperty("dataSource.databaseName", resourceBundle.getString("dataSource.databaseName"));
        
        props.setProperty("dataSource.serverName", resourceBundle.getString("dataSource.serverName"));
        props.setProperty("dataSource.portNumber", resourceBundle.getString("dataSource.serverPort"));        
        
        HikariConfig config = new HikariConfig(props);

        config.setMinimumIdle(Integer.parseInt(resourceBundle.getString("minimumIdle")));
        config.setMaximumPoolSize(Integer.parseInt(resourceBundle.getString("setMaximumPoolSize")));
        config.setIdleTimeout(Integer.parseInt(resourceBundle.getString("idleTimeout")));
        config.setConnectionTimeout(Integer.parseInt(resourceBundle.getString("connectionTimeout")));        
        config.setAutoCommit(true);

        try {
            poolConnexion = new HikariDataSource(config);
        } catch (Exception ex) {
        //    logger.error(ex.toString());
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL, ex.getClass().getName(), ex.getMessage()); 
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
     }
    
    public boolean isConnected(){
        return poolConnexion != null;
    }

    public void closeConnexion() {
        if(poolConnexion != null)
            poolConnexion.close();
    }

    public String getWorkLanguage() {
        return workLanguage;
    }

    public String getDefaultThesaurusId() {
        return defaultThesaurusId;
    }
    
    public HikariDataSource getPoolConnexion() {
        if(poolConnexion == null) return null;
        if (poolConnexion.isClosed()) {
            openConnexionPool();
        }
        return poolConnexion;
    }    
   
}
