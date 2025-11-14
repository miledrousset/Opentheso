package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.services.HomePageService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.Serializable;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.primefaces.PrimeFaces;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "viewEditorHomeBean")
public class ViewEditorHomeBean implements Serializable {

    private final HomePageService homePageService;
    private final SelectedTheso selectedTheso;

    private boolean isViewPlainText, isInEditing, isInEditingHomePage, isInEditingGoogleAnalytics;
    private String text, colorOfHtmlButton, colorOfTextButton, codeGoogleAnalitics;



    public void reset() {
        isInEditing = false;
        isViewPlainText = false;
        text = null;
        codeGoogleAnalitics = null;
        isInEditingGoogleAnalytics = false;
        isInEditingHomePage = false;

        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void initText() {

        text = getHomePage();
        isInEditing = true;
        isViewPlainText = false;
        isInEditingGoogleAnalytics = false;
        isInEditingHomePage = true;

        colorOfHtmlButton = "#F49F66;";
        colorOfTextButton = "#8C8C8C;";
    }

    public void initGoogleAnalytics() {

        codeGoogleAnalitics = homePageService.getCodeGoogleAnalytics();
        isInEditing = true;
        isViewPlainText = false;
        isInEditingGoogleAnalytics = true;
        isInEditingHomePage = false;
    }

    public void updateGoogleAnalytics() {

        homePageService.setCodeGoogleAnalytics(codeGoogleAnalitics);

        isInEditing = false;
        isViewPlainText = false;
        isInEditingGoogleAnalytics = false;
        isInEditingHomePage = false;

        selectedTheso.setOptionThesoSelected("Option1");

        reset();
        PrimeFaces.current().ajax().update("containerIndex");
    }

    public String getHomePage() {
        return homePageService.getHomePage();
    }

    /**
     * permet d'ajouter un copyright, s'il n'existe pas, on le créé,sinon, on
     * applique une mise à jour
     */
    public void updateHomePage() throws IOException {

        var lang = homePageService.getLanguage();

        if (!homePageService.setHomePage(text, lang)) {
            MessageUtils.showErrorMessage("L'ajout a échoué !");
            return;
        }

        MessageUtils.showInformationMessage("Texte ajouté avec succès");

        isInEditing = false;
        isViewPlainText = false;
        isInEditingHomePage = false;
        isInEditingGoogleAnalytics = false;

        selectedTheso.setOptionThesoSelected("Option1");

        reset();

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        String currentUrl = ec.getRequestContextPath() + ((HttpServletRequest) ec.getRequest()).getServletPath();
        ec.redirect(currentUrl);
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
        PrimeFaces.current().ajax().update("containerIndex");
    }

    public boolean isTextVisible() {
        return !isInEditingGoogleAnalytics && !isInEditingHomePage;
    }
}
