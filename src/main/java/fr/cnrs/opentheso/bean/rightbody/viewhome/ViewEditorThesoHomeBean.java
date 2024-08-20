package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.HtmlPageHelper;
import fr.cnrs.opentheso.bdd.helper.StatisticHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@SessionScoped
@Named(value = "viewEditorThesoHomeBean")
public class ViewEditorThesoHomeBean implements Serializable {

    @Autowired @Lazy
    private Connect connect;

    private boolean isViewPlainText;
    private boolean isInEditing;

    private String text;
    private String colorOfHtmlButton;
    private String colorOfTextButton;    


    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        text = null;
        colorOfHtmlButton = null;
        colorOfTextButton = null;        
    }     

    public void reset(){
        isInEditing = false;
        isViewPlainText = false;
        text = null;
    }
    
    public void initText(String idLanguage, String idThesaurus) {
        if(idLanguage == null || idLanguage.isEmpty()) {
            idLanguage = connect.getWorkLanguage();
        } 
        HtmlPageHelper copyrightHelper = new HtmlPageHelper();
        text = copyrightHelper.getThesoHomePage(connect.getPoolConnexion(), idThesaurus, idLanguage);
        isInEditing = true;
        isViewPlainText = false;
        colorOfHtmlButton = "#F49F66;";
        colorOfTextButton = "#8C8C8C;";        
    }

    public String getThesoHomePage(String idLanguage, String idThesaurus){

        if(idLanguage == null || idLanguage.isEmpty()) {
            idLanguage = connect.getWorkLanguage();
        }         
        HtmlPageHelper copyrightHelper = new HtmlPageHelper();
        String homePage = copyrightHelper.getThesoHomePage(
                connect.getPoolConnexion(),
                idThesaurus,
                idLanguage);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("containerIndex:meta:metadataTheso");
            PrimeFaces.current().ajax().update("containerIndex:thesoHomeData");
        }        

        return homePage;
    }
    
    public List<DcElement> meta(String idThesaurus){

        List<DcElement> dcElements = new DcElementHelper().getDcElementOfThesaurus(connect.getPoolConnexion(), idThesaurus);
        if(dcElements == null || dcElements.isEmpty())
            dcElements = new ArrayList<>();           
        return dcElements;
    }
    /**
     * permet d'ajouter un copyright, s'il n'existe pas, on le créé,sinon, on applique une mise à jour 
     */
    public void updateThesoHomePage(String idLanguage, String idThesaurus) {
        FacesMessage msg;
        if(idLanguage == null || idLanguage.isEmpty()) {
            idLanguage = connect.getWorkLanguage();
        }         
        HtmlPageHelper htmlPageHelper = new HtmlPageHelper();
        if (!htmlPageHelper.setThesoHomePage(
                connect.getPoolConnexion(),
                text,
                idThesaurus,
                idLanguage)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'ajout a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;               
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "texte ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        isInEditing = false;
        isViewPlainText = false;
    }

    public String getTotalConceptOfTheso(String idThesaurus){
        StatisticHelper statisticHelper = new StatisticHelper();
        int count = statisticHelper.getNbCpt(connect.getPoolConnexion(), idThesaurus);
        return "" + count;
    }

    public String getLastModifiedDate(String idThesaurus){
        ConceptHelper conceptHelper = new ConceptHelper();
        Date date = conceptHelper.getLastModification(connect.getPoolConnexion(), idThesaurus);
        if(date != null)
            return date.toString();
        return "";
    }

    public String getProjectName(String idThesaurus){
        UserHelper userHelper = new UserHelper();
        int idProject = userHelper.getGroupOfThisTheso(connect.getPoolConnexion(), idThesaurus);
        if(idProject != -1) {
            return userHelper.getGroupName(connect.getPoolConnexion(), idProject);
        } else
            return "";
    }

    public ArrayList<NodeIdValue> getLastModifiedConcepts(String idThesaurus, String idLanguage){
        return new ConceptHelper().getLastModifiedConcept(connect.getPoolConnexion(), idThesaurus, idLanguage);
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

    public boolean isViewPlainText() {
        return isViewPlainText;
    }

    public void setIsViewPlainText(boolean isViewPlainText) {
        this.isViewPlainText = isViewPlainText;
    }

    public boolean isInEditing() {
        return isInEditing;
    }

    public void setIsInEditing(boolean isInEditing) {
        this.isInEditing = isInEditing;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
    
}
