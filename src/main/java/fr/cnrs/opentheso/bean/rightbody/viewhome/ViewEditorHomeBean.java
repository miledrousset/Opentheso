/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.repositories.HtmlPageHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import java.io.Serializable;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@SessionScoped
@Named(value = "viewEditorHomeBean")
public class ViewEditorHomeBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    @Autowired @Lazy
    private LanguageBean languageBean;

    @Autowired
    private HtmlPageHelper htmlPageHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    private boolean isViewPlainText = false;
    private String text;

    private boolean isInEditing;
    private String colorOfHtmlButton;
    private String colorOfTextButton;

    private boolean isInEditingHomePage;

    private String codeGoogleAnalitics;
    private boolean isInEditingGoogleAnalytics;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        text = null;
        colorOfHtmlButton = null;
        colorOfTextButton = null;
        codeGoogleAnalitics = null;
    }

    public void reset() {
        isInEditing = false;
        isViewPlainText = false;
        text = null;
        codeGoogleAnalitics = null;
        isInEditingGoogleAnalytics = false;
        isInEditingHomePage = false;
        //selectedTheso.setOptionThesoSelected("Option1");

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void initText() {
        String lang = languageBean.getIdLangue().toLowerCase();
        if (lang == null || lang.isEmpty()) {
            lang = workLanguage;
        }

        text = htmlPageHelper.getHomePage(lang);
        isInEditing = true;
        isViewPlainText = false;
        isInEditingGoogleAnalytics = false;
        isInEditingHomePage = true;

        colorOfHtmlButton = "#F49F66;";
        colorOfTextButton = "#8C8C8C;";
    }

    public void initGoogleAnalytics() {

        codeGoogleAnalitics = preferencesHelper.getCodeGoogleAnalytics();
        isInEditing = true;
        isViewPlainText = false;
        isInEditingGoogleAnalytics = true;
        isInEditingHomePage = false;

    }

    public void updateGoogleAnalytics() {

        preferencesHelper.setCodeGoogleAnalytics(codeGoogleAnalitics);
        isInEditing = false;
        isViewPlainText = false;
        isInEditingGoogleAnalytics = false;
        isInEditingHomePage = false;
        reset();

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    public String getHomePage(String idLang) {
        String lang = languageBean.getIdLangue().toLowerCase();
        if (lang == null || lang.isEmpty()) {
            lang = workLanguage;
        }
        String homePage = htmlPageHelper.getHomePage(lang);
        return homePage;
    }

    /**
     * permet d'ajouter un copyright, s'il n'existe pas, on le créé,sinon, on
     * applique une mise à jour
     */
    public void updateHomePage() {
        FacesMessage msg;
        String lang = languageBean.getIdLangue().toLowerCase();
        if (lang == null || lang.isEmpty()) {
            lang = workLanguage;
        }
        if (!htmlPageHelper.setHomePage(text, lang)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'ajout a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);

            PrimeFaces pf = PrimeFaces.current();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "texte ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        isInEditing = false;
        isViewPlainText = false;
        isInEditingHomePage = false;
        isInEditingGoogleAnalytics = false;
        reset();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    public void setViewPlainTextTo(boolean status) {
        if (status) {
            colorOfHtmlButton = "#8C8C8C;";
            colorOfTextButton = "#F49F66;";
        } else {
            colorOfHtmlButton = "#F49F66;";
            colorOfTextButton = "#8C8C8C;";
        }
        isViewPlainText = status;

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    public boolean isTextVisible() {
        return !isInEditingGoogleAnalytics && !isInEditingHomePage;
    }

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    public boolean isViewPlainText() {
        return isViewPlainText;
    }

    public void setViewPlainText(boolean viewPlainText) {
        isViewPlainText = viewPlainText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isInEditing() {
        return isInEditing;
    }

    public void setInEditing(boolean inEditing) {
        isInEditing = inEditing;
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

    public boolean isInEditingHomePage() {
        return isInEditingHomePage;
    }

    public void setInEditingHomePage(boolean inEditingHomePage) {
        isInEditingHomePage = inEditingHomePage;
    }

    public String getCodeGoogleAnalitics() {
        return codeGoogleAnalitics;
    }

    public void setCodeGoogleAnalitics(String codeGoogleAnalitics) {
        this.codeGoogleAnalitics = codeGoogleAnalitics;
    }

    public boolean isInEditingGoogleAnalytics() {
        return isInEditingGoogleAnalytics;
    }

    public void setInEditingGoogleAnalytics(boolean inEditingGoogleAnalytics) {
        isInEditingGoogleAnalytics = inEditingGoogleAnalytics;
    }
}
