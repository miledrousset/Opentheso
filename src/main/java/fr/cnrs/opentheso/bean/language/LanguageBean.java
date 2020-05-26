package fr.cnrs.opentheso.bean.language;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named (value="langueBean")
@SessionScoped
public class LanguageBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject private Connect connect;
    
    private String currentBundle;
    private String idLangue;

    private ResourceBundle getBundleLangue(String l) {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundleLangue = context.getApplication().getResourceBundle(context, l);
        return bundleLangue;
    }

    /**
     * Constructeur
     */
    public LanguageBean() {
        //    FacesContext context = FacesContext.getCurrentInstance();
    }

    @PostConstruct
    public void InitLanguageBean() {
        currentBundle = "langue_" + connect.getWorkLanguage();
        idLangue = connect.getWorkLanguage().toUpperCase();
    }
  
    public void changeLangue(String l) {
        currentBundle = "langue_" + l;
        idLangue = l.toUpperCase();
    }    

    public String getMsg(String msg) {
        return getBundleLangue(currentBundle).getString(msg);
    }

    public String getIdLangue() {
        return idLangue;
    }

    public void setIdLangue(String idLangue) {
        this.idLangue = idLangue;
    }

    public String getCurrentBundle() {
        return currentBundle;
    }

    public void setCurrentBundle(String currentBundle) {
        this.currentBundle = currentBundle;
    }

}
