/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.setting;

import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import org.primefaces.PrimeFaces;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "preferenceBean")
@SessionScoped
public class PreferenceBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private SelectedTheso selectedTheso;
    
    private NodePreference nodePreference; 
    private ArrayList<NodeLangTheso> languagesOfTheso;
    
    private String uriType;
           
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(languagesOfTheso!= null){
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
    
    public void init(){
        if(selectedTheso.getCurrentIdTheso() == null) return;
        nodePreference = roleOnThesoBean.getNodePreference();
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        // les langues du thésaurus
        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());    
        
        uriType = "uri";
        if(nodePreference.isOriginalUriIsHandle())
            uriType = "handle";
        if(nodePreference.isOriginalUriIsArk())
            uriType = "ark";
        if(nodePreference.isOriginalUriIsDoi())
            uriType = "doi";        
        
    }
    
    public String getGoogleAnalytics() {
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        return preferencesHelper.getCodeGoogleAnalytics(
                connect.getPoolConnexion());
    }

    public void savePreference() {
        setUriType();
        
        FacesMessage msg;
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        if(!preferencesHelper.updateAllPreferenceUser(
                connect.getPoolConnexion(),
                nodePreference,
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur d'enregistrement des préférences !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Préférences enregistrées avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init();

        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
        PrimeFaces.current().ajax().update("preferenceForm");
    }
    
    private void setUriType(){
        if(uriType == null) return;
        nodePreference.setOriginalUriIsArk(false);
        nodePreference.setOriginalUriIsHandle(false);
        if(uriType.equalsIgnoreCase("ark")) nodePreference.setOriginalUriIsArk(true);
        if(uriType.equalsIgnoreCase("handle")) nodePreference.setOriginalUriIsHandle(true);
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
