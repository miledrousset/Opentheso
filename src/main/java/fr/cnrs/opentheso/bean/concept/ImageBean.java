package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.ImageService;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.Serializable;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "imageBean")
@SessionScoped
public class ImageBean implements Serializable {

    
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private CurrentUser currentUser;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private ConceptDcTermRepository conceptDcTermRepository;

    @Autowired
    private ImageService imageService;

    private String uri;
    private String copyright;
    private String name;
    private String creator;
    
    private List<NodeImage> nodeImages;
    private List<NodeImage> nodeImagesForEdit;


    public void reset() {
        nodeImages = conceptBean.getNodeConcept().getNodeimages();
        uri = null;
        copyright = null;
        name = null;
        creator = null;

        nodeImagesForEdit = imageService.getAllExternalImages(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept());
    }
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour images !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
    /**
     * permet d'ajouter une image
     */
    public void addNewImage(int idUser) {
        
        if(StringUtils.isEmpty(uri)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucune URI insérée !");
            return;
        }

        imageService.addExternalImage(conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(),
                name, copyright, uri, creator, idUser);
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        showMessage(FacesMessage.SEVERITY_INFO, "Image ajoutée avec succès");
        reset();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesMessage msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void updateImage(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        imageService.updateExternalImage(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), nodeImage);
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image_URI modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();
    }       
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void deleteImage(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        imageService.deleteImages(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                nodeImage.getUri());
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptHelper.updateDateOfConcept(
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }

}
