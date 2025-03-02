package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.ExternalResourcesHelper;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;


@Data
@Named(value = "externalResources")
@SessionScoped
public class ExternalResources implements Serializable {

    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private CurrentUser currentUser;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private ExternalResourcesHelper externalResourcesHelper;

    private String uri;
    private String description;
    
    private List<NodeImage> nodeImages;
    private List<NodeImage> nodeImagesForEdit;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeImages != null){
            nodeImages.clear();
            nodeImages = null;
        }
        if(nodeImagesForEdit != null){
            nodeImagesForEdit.clear();
            nodeImagesForEdit = null;
        }
        uri = null;
        description = null;
    }      
    
    public ExternalResources() {
    }

    public void reset() {
        nodeImages = conceptBean.getNodeConcept().getNodeExternalResources();
        uri = null;
        description = null;

        prepareImageForEdit();
    }
   
    public void prepareImageForEdit(){
        nodeImagesForEdit = new ArrayList<>();
        for (NodeImage nodeImage : nodeImages) {
            nodeImage.setOldUri(nodeImage.getUri());
            nodeImagesForEdit.add(nodeImage);
        }
    }    
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour ressources !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
    /**
     * permet d'ajouter un 
     * @param idUser 
     */
    public void addNewExternalResource(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(uri == null || uri.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        if(!StringUtils.urlValidator(uri)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'URL n'est pas valide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        if(!externalResourcesHelper.addExternalResource(
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                description,
                uri)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant l'ajout de la ressource !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor

        dcElementHelper.addDcElementConcept(
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void updateExternalResource(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        if(!StringUtils.urlValidator(nodeImage.getUri())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'URL n'est pas valide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        if(!externalResourcesHelper.setExternalResourceUri(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), nodeImage.getOldUri(), nodeImage.getUri(),
                nodeImage.getImageName(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la modification de l'URI !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser); 
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////         
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "URL modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }       
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void deleteExternalResource(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if(!externalResourcesHelper.deleteExternalResource(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), nodeImage.getUri())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la suppression de la ressource !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);  
        ///// insert DcTermsData to add contributor
        dcElementHelper.addDcElementConcept(
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////         
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }
}
