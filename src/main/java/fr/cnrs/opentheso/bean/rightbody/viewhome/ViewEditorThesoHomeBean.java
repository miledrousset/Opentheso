package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.HtmlPageHelper;
import fr.cnrs.opentheso.repositories.StatisticHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
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
import org.springframework.beans.factory.annotation.Value;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@SessionScoped
@Named(value = "viewEditorThesoHomeBean")
public class ViewEditorThesoHomeBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private StatisticHelper statisticHelper;

    @Autowired
    private HtmlPageHelper htmlPageHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private UserHelper userHelper;

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
            idLanguage = workLanguage;
        }

        text = htmlPageHelper.getThesoHomePage(idThesaurus, idLanguage);
        isInEditing = true;
        isViewPlainText = false;
        colorOfHtmlButton = "#F49F66;";
        colorOfTextButton = "#8C8C8C;";        
    }

    public String getThesoHomePage(String idLanguage, String idThesaurus){

        if(idLanguage == null || idLanguage.isEmpty()) {
            idLanguage = workLanguage;
        }
        String homePage = htmlPageHelper.getThesoHomePage(
                
                idThesaurus,
                idLanguage);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("containerIndex:meta:metadataTheso");
            PrimeFaces.current().ajax().update("containerIndex:thesoHomeData");
        }        

        return homePage;
    }
    
    public List<DcElement> meta(String idThesaurus){

        List<DcElement> dcElements = dcElementHelper.getDcElementOfThesaurus(idThesaurus);
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
            idLanguage = workLanguage;
        }

        if (!htmlPageHelper.setThesoHomePage(
                
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

        int count = statisticHelper.getNbCpt(idThesaurus);
        return "" + count;
    }

    public String getLastModifiedDate(String idThesaurus){
        Date date = conceptHelper.getLastModification(idThesaurus);
        if(date != null)
            return date.toString();
        return "";
    }

    public String getProjectName(String idThesaurus){
        int idProject = userHelper.getGroupOfThisTheso(idThesaurus);
        if(idProject != -1) {
            return userHelper.getGroupName(idProject);
        } else
            return "";
    }

    public ArrayList<NodeIdValue> getLastModifiedConcepts(String idThesaurus, String idLanguage){
        return conceptHelper.getLastModifiedConcept(idThesaurus, idLanguage);
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
