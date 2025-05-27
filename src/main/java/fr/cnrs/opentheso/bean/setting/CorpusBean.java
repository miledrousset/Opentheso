package fr.cnrs.opentheso.bean.setting;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.models.nodes.NodeCorpus;
import fr.cnrs.opentheso.services.CorpusService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.primefaces.PrimeFaces;



@Data
@Named(value = "corpusBean")
@SessionScoped
@RequiredArgsConstructor
public class CorpusBean implements Serializable {

    private final SelectedTheso selectedTheso;
    private final CorpusService corpusService;

    private String oldName;
    private NodeCorpus nodeCorpusForEdit;
    private List<NodeCorpus> corpusList;


    public void init() {

        var idThesaurus = selectedTheso.getCurrentIdTheso();
        corpusList = corpusService.getAllCorpusByThesaurus(idThesaurus);
        nodeCorpusForEdit = new NodeCorpus();
        oldName = "";
    }
    
    public void updateCorpus() {

        if (checkCorpusDatas()) {
            var corpusFind = corpusService.getCorpusByNameAndThesaurus(selectedTheso.getCurrentIdTheso(),
                    nodeCorpusForEdit.getCorpusName());
            if (corpusFind != null && !nodeCorpusForEdit.getCorpusName().equalsIgnoreCase(oldName)) {
                MessageUtils.showWarnMessage("Ce corpus existe déjà, changez de nom !");
                return;
            }

            if (corpusService.updateCorpusLink(selectedTheso.getCurrentIdTheso(), nodeCorpusForEdit, oldName)) {
                PrimeFaces.current().executeScript("PF('editCorpus').hide();");
                updateScreen();
                MessageUtils.showInformationMessage("Corpus modifié avec succès");
            } else {
                MessageUtils.showErrorMessage("Erreur de modification de corpus !");
            }
        }
    }

    public void addNewCorpus() {

        if (checkCorpusDatas()) {
            if (corpusService.saveCorpusLink(selectedTheso.getCurrentIdTheso(), nodeCorpusForEdit)) {
                PrimeFaces.current().executeScript("PF('newCorpus').hide();");
                updateScreen();
                MessageUtils.showInformationMessage("Corpus créé avec succès");
            } else {
                MessageUtils.showWarnMessage("Ce corpus existe déjà, changez de nom !");
            }
        }
    }

    public void deleteCorpus() {

        if (nodeCorpusForEdit == null) {
            return;
        }

        corpusService.deleteCorpusLinkByThesaurusAndName(selectedTheso.getCurrentIdTheso(), nodeCorpusForEdit.getCorpusName());
        updateScreen();
        MessageUtils.showInformationMessage("Corpus supprimé avec succès");
    }

    public void setCorpusForEdit(NodeCorpus nodeCorpus) {

        oldName = nodeCorpus.getCorpusName();
        nodeCorpusForEdit = NodeCorpus.builder()
                .active(nodeCorpus.isActive())
                .corpusName(nodeCorpus.getCorpusName())
                .isOnlyUriLink(nodeCorpus.isOnlyUriLink())
                .uriLink(nodeCorpus.getUriLink())
                .uriCount(nodeCorpus.getUriCount())
                .omekaS(nodeCorpus.isOmekaS())
                .build();
    }

    public void setCorpusForNew() {
        nodeCorpusForEdit = new NodeCorpus();
    }

    private void updateScreen() {
        init();
        PrimeFaces.current().ajax().update("containerIndex");
    }

    private boolean checkCorpusDatas() {
        if (nodeCorpusForEdit == null) {
            return false;
        }

        if (nodeCorpusForEdit.getCorpusName().isEmpty()) {
            MessageUtils.showWarnMessage("Le nom du corpus est obligatoire !");
            return false;
        }

        //Vérification de l'URI
        if (nodeCorpusForEdit.getUriLink().isEmpty()) {
            MessageUtils.showWarnMessage("L'URI du lien est obligatoire !");
            return false;
        }

        //Vérification de l'URI du comptage
        if(!nodeCorpusForEdit.isOnlyUriLink() && nodeCorpusForEdit.getUriCount().isEmpty()) {
            MessageUtils.showWarnMessage("L'URI pour le comptage est obligatoire !");
            return false;
        }

        return true;
    }
}
