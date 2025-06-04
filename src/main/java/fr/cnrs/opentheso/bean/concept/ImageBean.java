package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.ImageService;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.utils.MessageUtils;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.io.Serializable;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@Named(value = "imageBean")
@RequiredArgsConstructor
public class ImageBean implements Serializable {

    private final ConceptView conceptBean;
    private final SelectedTheso selectedTheso;
    private final CurrentUser currentUser;
    private final ConceptDcTermRepository conceptDcTermRepository;
    private final ImageService imageService;
    private final ConceptService conceptService;

    private String uri, copyright, name, creator;
    private List<NodeImage> nodeImages, nodeImagesForEdit;


    public void reset() {
        nodeImages = conceptBean.getNodeConcept().getNodeimages();
        uri = null;
        copyright = null;
        name = null;
        creator = null;

        nodeImagesForEdit = imageService.getAllExternalImages(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept());
    }

    public void addNewImage(int idUser) {
        
        if(StringUtils.isEmpty(uri)) {
            MessageUtils.showErrorMessage("Aucune URI insérée !");
            return;
        }

        imageService.addExternalImage(conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(),
                name, copyright, uri, creator, idUser);
        
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

        MessageUtils.showInformationMessage("Image ajoutée avec succès");
        reset();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void updateImage(NodeImage nodeImage, int idUser) {
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            MessageUtils.showWarnMessage("Aucune image sélectionnée !");
            return;
        }

        imageService.updateExternalImage(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), nodeImage);
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        MessageUtils.showInformationMessage("L'URI du l'image est modifiée avec succès");
        reset();
    }       


    public void deleteImage(NodeImage nodeImage, int idUser) {
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            MessageUtils.showWarnMessage("Aucune sélection !");
            return;
        }

        imageService.deleteImages(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                nodeImage.getUri());
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        MessageUtils.showInformationMessage("Image supprimée avec succès");
        reset();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

}
