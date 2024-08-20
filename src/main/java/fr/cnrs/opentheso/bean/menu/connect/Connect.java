package fr.cnrs.opentheso.bean.menu.connect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.notification.NewVersionService;
import java.io.Serializable;
import java.util.Properties;
import java.util.ResourceBundle;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;


import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.inject.Named;


@Named (value = "connect")
@ApplicationScoped
public class Connect implements Serializable{
    @Autowired private NewVersionService newVersionService;
    private final int DEFAULT_TIMEOUT_IN_MIN = 10;
   
   
    private HikariDataSource poolConnexion = null;
    private String workLanguage = "fr";
    private String defaultThesaurusId;
    private String localUri;
    
    
    @PostConstruct
    public void initPref() {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
        workLanguage = bundlePref.getString("workLanguage");
        defaultThesaurusId = bundlePref.getString("defaultThesaurusId");
        newVersionService.isNewVersionExist();
    }    

    
    /**
     * retourne la version actuelle d'Opentheso d'après le WAR
     * @return 
     */
    public String getOpenthesoVersionFromWar() {
        return FacesContext.getCurrentInstance().getExternalContext().getInitParameterMap().get("version");
    }
    
    /**
     * Pour détecter les agents d'indexation
     *
     * @return
     */
    public String getBrowserName() {
        if(FacesContext.getCurrentInstance() != null){
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            String userAgent = externalContext.getRequestHeaderMap().get("User-Agent");

            // Yahoo
            if (userAgent.toLowerCase().contains("Slurp")) {
                return "agent";
            }
            //Bing
            if (userAgent.toLowerCase().contains("bingbot")) {
                return "agent";
            }

            if (userAgent.toLowerCase().contains("msnbot")) {
                return "agent";
            }
            //Google
            if (userAgent.toLowerCase().contains("googlebot")) {
                return "agent";
            }
        }
        return "notagent";
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

    public String status(){
        if(poolConnexion != null) return "OK! Connected";
        else
            return "!! Failed !!";
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

    public int getTimeout() {
        int minNbr;
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ResourceBundle bundlePref = context.getApplication().getResourceBundle(context, "pref");
            minNbr = Integer.parseInt(bundlePref.getString("timeout_nbr_minute"));
        } catch (Exception e) {
            minNbr = DEFAULT_TIMEOUT_IN_MIN;
        }
        return (minNbr * 60 * 1000);
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }
   
}
