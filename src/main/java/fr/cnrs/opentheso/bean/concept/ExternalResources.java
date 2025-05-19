package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.entites.ExternalResource;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.ExternalResourcesRepository;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.PrimeFaces;


@Data
@Named(value = "externalResources")
@SessionScoped
@RequiredArgsConstructor
public class ExternalResources implements Serializable {

    private final ConceptView conceptBean;
    private final SelectedTheso selectedTheso;
    private final CurrentUser currentUser;
    private final ConceptDcTermRepository conceptDcTermRepository;
    private final ConceptHelper conceptHelper;
    private final ExternalResourcesRepository externalResourcesRepository;
    private final ConceptService conceptService;

    private String uri, description;
    private List<NodeImage> nodeImages, nodeImagesForEdit;


    public void reset() {
        uri = null;
        description = null;
        nodeImages = conceptBean.getNodeConcept().getNodeExternalResources();
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

    public void addNewExternalResource(int idUser) {
        
        if(uri == null || uri.isEmpty()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Pas de sélection !");
            return;
        }
        
        if(!StringUtils.urlValidator(uri)) {
            showMessage(FacesMessage.SEVERITY_WARN, "L'URL n'est pas valide !");
            return;            
        }

        externalResourcesRepository.save(ExternalResource.builder()
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .description(description)
                .externalUri(uri)
                .build());
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        showMessage(FacesMessage.SEVERITY_INFO, "Image ajoutée avec succès");
        reset();

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void updateExternalResource(NodeImage nodeImage, int idUser) {
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Pas de sélection !");
            return;
        }
        
        if(!StringUtils.urlValidator(nodeImage.getUri())) {
            showMessage(FacesMessage.SEVERITY_WARN, "L'URL n'est pas valide !");
            return;            
        }

        externalResourcesRepository.updateExternalResource(nodeImage.getUri(), idUser+"", nodeImage.getImageName(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(),
                nodeImage.getOldUri());

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        showMessage(FacesMessage.SEVERITY_INFO, "URL modifiée avec succès");
        reset();

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }       

    public void deleteExternalResource(NodeImage nodeImage, int idUser) {
        
        if(ObjectUtils.isEmpty(nodeImage) || nodeImage.getUri().isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Pas de sélection !");
            return;
        }

        externalResourcesRepository.deleteExternalResource(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), nodeImage.getUri());
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        showMessage(FacesMessage.SEVERITY_INFO, "Image supprimée avec succès");
        reset();

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    private void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }
}
