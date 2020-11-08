/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.bdd.helper.HtmlPageHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "viewEditorHomeBean")
@SessionScoped
public class ViewEditorHomeBean implements Serializable {
    @Inject private Connect connect;
    @Inject private ConceptView conceptBean;
    @Inject private LanguageBean languageBean;    
    
    public ViewEditorHomeBean() {
    }
    
    private boolean isViewPlainText = false;
    private String text;
    
    private boolean isInEditing;
    private String colorOfHtmlButton;
    private String colorOfTextButton;    

    private boolean isInEditingHomePage;       
    
    private String codeGoogleAnalitics;
    private boolean isInEditingGoogleAnalytics;    
    
    public void reset(){
        isInEditing = false;
        isViewPlainText = false;
        text = null;
        codeGoogleAnalitics = null;
        isInEditingGoogleAnalytics = false;
        isInEditingHomePage = false;
    }
    
    public void initText() {
        String lang = languageBean.getIdLangue().toLowerCase();
        if(lang == null || lang.isEmpty()) {
            lang = connect.getWorkLanguage();
        } 
        HtmlPageHelper copyrightHelper = new HtmlPageHelper();
        text = copyrightHelper.getHomePage(
                connect.getPoolConnexion(), lang);
        isInEditing = true;
        isViewPlainText = false;
        isInEditingGoogleAnalytics = false;
        isInEditingHomePage = true;
        
        colorOfHtmlButton = "#F49F66;";
        colorOfTextButton = "#8C8C8C;";          
    }
    
    public void initGoogleAnalytics() {
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        codeGoogleAnalitics = preferencesHelper.getCodeGoogleAnalytics(
                connect.getPoolConnexion());
        isInEditing = true;
        isViewPlainText = false;
        isInEditingGoogleAnalytics = true;
        isInEditingHomePage = false;        
    }
    
    public void updateGoogleAnalytics() {
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        
        preferencesHelper.setCodeGoogleAnalytics(
                connect.getPoolConnexion(),codeGoogleAnalitics);
        isInEditing = false;
        isViewPlainText = false;
        isInEditingGoogleAnalytics = false;
        isInEditingHomePage = false;        
    }    
    

    public String getHomePage(String idLang){
        HtmlPageHelper copyrightHelper = new HtmlPageHelper();
        String lang = languageBean.getIdLangue().toLowerCase();
        if(lang == null || lang.isEmpty()) {
            lang = connect.getWorkLanguage();
        }        
        String homePage = copyrightHelper.getHomePage(
                connect.getPoolConnexion(), lang);
        return homePage;
    }
    /**
     * permet d'ajouter un copyright, s'il n'existe pas, on le créé,sinon, on applique une mise à jour 
     */
    public void updateHomePage() {
        FacesMessage msg;
        String lang = languageBean.getIdLangue().toLowerCase();
        if(lang == null || lang.isEmpty()) {
            lang = connect.getWorkLanguage();
        }
        HtmlPageHelper htmlPageHelper = new HtmlPageHelper();
        if (!htmlPageHelper.setHomePage(
                connect.getPoolConnexion(),
                text,
                lang)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'ajout a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;               
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "texte ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        isInEditing = false;
        isViewPlainText = false;
        isInEditingHomePage = false; 
        isInEditingGoogleAnalytics = false;        
    }
    
   
    public void setViewPlainTextTo(boolean status){
        if(status) {
            colorOfHtmlButton = "#8C8C8C;";
            colorOfTextButton = "#F49F66;";
        } else {
            colorOfHtmlButton = "#F49F66;";
            colorOfTextButton = "#8C8C8C;";            
        } 
        isViewPlainText = status;
    }

    public boolean isIsViewPlainText() {
        return isViewPlainText;
    }

    public void setIsViewPlainText(boolean isViewPlainText) {
        this.isViewPlainText = isViewPlainText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isIsInEditing() {
        return isInEditing;
    }

    public void setIsInEditing(boolean isInEditing) {
        this.isInEditing = isInEditing;
    }

    public String getColorOfHtmlButton() {
        return colorOfHtmlButton;
    }

    public void setColorOfHtmlButton(String colorOfHtmlButton) {
        this.colorOfHtmlButton = colorOfHtmlButton;
    }

    public String getColorOfTextButton() {
        return colorOfTextButton;
    }

    public void setColorOfTextButton(String colorOfTextButton) {
        this.colorOfTextButton = colorOfTextButton;
    }

    public boolean isIsInEditingGoogleAnalytics() {
        return isInEditingGoogleAnalytics;
    }

    public void setIsInEditingGoogleAnalytics(boolean isInEditingGoogleAnalytics) {
        this.isInEditingGoogleAnalytics = isInEditingGoogleAnalytics;
    }

    public String getCodeGoogleAnalitics() {
        return codeGoogleAnalitics;
    }

    public void setCodeGoogleAnalitics(String codeGoogleAnalitics) {
        this.codeGoogleAnalitics = codeGoogleAnalitics;
    }

    public boolean isIsInEditingHomePage() {
        return isInEditingHomePage;
    }

    public void setIsInEditingHomePage(boolean isInEditingHomePage) {
        this.isInEditingHomePage = isInEditingHomePage;
    }


    
}
