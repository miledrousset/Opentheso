package fr.cnrs.opentheso.bean.setting;

import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "preferenceBean")
@SessionScoped
public class PreferenceBean implements Serializable {

    @Autowired
    private RoleOnThesoBean roleOnThesoBean;

    @Autowired
    private SelectedTheso selectedTheso;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    private String uriType;
    private NodePreference nodePreference;
    private ArrayList<NodeLangTheso> languagesOfTheso;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (languagesOfTheso != null) {
            languagesOfTheso.clear();
            languagesOfTheso = null;
        }
        nodePreference = null;
        uriType = null;
    }

    /**
     * Creates a new instance of preferenceBean
     */
    public PreferenceBean() {
    }

    public void init() {
        if (selectedTheso.getCurrentIdTheso() == null) {
            return;
        }
        nodePreference = roleOnThesoBean.getNodePreference();
        // les langues du thésaurus
        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(selectedTheso.getCurrentIdTheso(), nodePreference.getSourceLang());

        uriType = "uri";
        if (nodePreference.isOriginalUriIsHandle()) {
            uriType = "handle";
        }
        if (nodePreference.isOriginalUriIsArk()) {
            uriType = "ark";
        }
        if (nodePreference.isOriginalUriIsDoi()) {
            uriType = "doi";
        }

    }

    public void updateSelectedServer(String selectedServer){
       
        switch (selectedServer) {
            case "ark":
                nodePreference.setUseArkLocal(false);
                nodePreference.setUseHandle(false);
                preferencesHelper.setUseArk(selectedTheso.getCurrentIdTheso(), nodePreference.isUseArk());                
                break;
            case "arklocal":
                nodePreference.setUseArk(false);
                nodePreference.setUseHandle(false);  
                preferencesHelper.setUseArkLocal(selectedTheso.getCurrentIdTheso(), nodePreference.isUseArkLocal());                 
                break;
            case "handle":
                nodePreference.setUseArk(false);
                nodePreference.setUseArkLocal(false);      
                preferencesHelper.setUseHandle(selectedTheso.getCurrentIdTheso(), nodePreference.isUseHandle());                 
                break;                
            default:
                break;
        }
    }
    
    public String getGoogleAnalytics() {
        return preferencesHelper.getCodeGoogleAnalytics();
    }

    public void savePreference() {
        setUriType();
        PrimeFaces pf = PrimeFaces.current();

        FacesMessage msg;
        
        if (!preferencesHelper.updateAllPreferenceUser(
                nodePreference, selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur d'enregistrement des préférences !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Préférences enregistrées avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init();

        PrimeFaces.current().executeScript("PF('waitDialog').hide();");

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    private void setUriType() {
        if (uriType == null) {
            return;
        }
        nodePreference.setOriginalUriIsArk(false);
        nodePreference.setOriginalUriIsHandle(false);
        if (uriType.equalsIgnoreCase("ark")) {
            nodePreference.setOriginalUriIsArk(true);
        }
        if (uriType.equalsIgnoreCase("handle")) {
            nodePreference.setOriginalUriIsHandle(true);
        }
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public ArrayList<NodeLangTheso> getLanguagesOfTheso() {
        return languagesOfTheso;
    }

    public void setLanguagesOfTheso(ArrayList<NodeLangTheso> languagesOfTheso) {
        this.languagesOfTheso = languagesOfTheso;
    }

    public String getUriType() {
        return uriType;
    }

    public void setUriType(String uriType) {
        this.uriType = uriType;
    }

}
