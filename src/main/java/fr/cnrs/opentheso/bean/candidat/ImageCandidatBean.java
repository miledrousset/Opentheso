package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.bean.concept.ImageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.services.ImageService;
import fr.cnrs.opentheso.utils.MessageUtils;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import java.io.Serializable;


@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "imageCandidatBean")
public class ImageCandidatBean implements Serializable {

    private final ImageBean imageBean;
    private final CandidatBean candidatBean;
    private final ImageService imageService;
    private final SelectedTheso selectedTheso;


    public void addNewImage(int idUser) {

        if (StringUtils.isEmpty(imageBean.getUri())) {
            MessageUtils.showErrorMessage("Aucune URI insérée !");
            return;
        }

        imageService.addExternalImage(candidatBean.getCandidatSelected().getIdConcepte(),
                selectedTheso.getCurrentIdTheso(), imageBean.getName(),
                imageBean.getCopyright(), imageBean.getUri(), imageBean.getCreator(), idUser);

        candidatBean.getCandidatSelected().setImages(imageService.getAllExternalImages(
                candidatBean.getCandidatSelected().getIdThesaurus(),
                candidatBean.getCandidatSelected().getIdConcepte()));

        MessageUtils.showInformationMessage("Image ajoutée avec succès");
        initImageDialog();
        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public void deleteImage(String imageUri) {

        imageService.deleteImages(selectedTheso.getSelectedIdTheso(), candidatBean.getCandidatSelected().getIdConcepte(), imageUri);
        candidatBean.getCandidatSelected().setImages(imageService.getAllExternalImages(candidatBean.getCandidatSelected().getIdThesaurus(),
                candidatBean.getCandidatSelected().getIdConcepte()));
        MessageUtils.showInformationMessage("Image supprimée avec succès");
    }

    public void initImageDialog() {
        imageBean.setUri(null);
        imageBean.setCopyright(null);
        imageBean.setName(null);
        imageBean.setCreator(null);
    }
}
