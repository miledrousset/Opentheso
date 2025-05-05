package fr.cnrs.opentheso.bean.setting;

import fr.cnrs.opentheso.entites.CorpusLink;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.models.nodes.NodeCorpus;
import fr.cnrs.opentheso.repositories.CorpusLinkRepository;

import java.io.Serializable;
import java.util.List;

import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;
import jakarta.enterprise.context.SessionScoped;

import lombok.Data;
import org.primefaces.PrimeFaces;



@Data
@Named(value = "corpusBean")
@SessionScoped
public class CorpusBean implements Serializable {

    private final SelectedTheso selectedTheso;
    private final CorpusLinkRepository corpusLinkRepository;

    private String oldName;
    private NodeCorpus nodeCorpusForEdit;
    private List<NodeCorpus> nodeCorpuses;


    public CorpusBean(SelectedTheso selectedTheso, CorpusLinkRepository corpusLinkRepository) {

        this.selectedTheso = selectedTheso;
        this.corpusLinkRepository = corpusLinkRepository;
    }

    public void init() {

        var corpusList = corpusLinkRepository.findAllByIdThesoOrderBySortAsc(selectedTheso.getCurrentIdTheso());
        if (corpusList.isEmpty()) {
            nodeCorpuses = List.of();
        } else {
            nodeCorpuses = corpusList.stream()
                    .map(element -> NodeCorpus.builder()
                            .corpusName(element.getCorpusName())
                            .active(element.isActive())
                            .omekaS(element.isOmekaS())
                            .isOnlyUriLink(element.isOnlyUriLink())
                            .uriLink(element.getUriLink())
                            .uriCount(element.getUriCount())
                            .build())
                    .toList();
        }
        nodeCorpusForEdit = new NodeCorpus();
        oldName = "";
    }

    public void setState(){
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("newCorpusForm:uriCount");
        }
    }
    
    public void updateCorpus() {

        if (nodeCorpusForEdit == null) {
            return;
        }

        if (nodeCorpusForEdit.getCorpusName().isEmpty()) {
            showMessage(FacesMessage.SEVERITY_INFO, "Le nom du corpus est obligatoire !");
            return;
        }

        //Vérification de l'URI
        if (nodeCorpusForEdit.getUriLink().isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "L'URI du lien est obligatoire !");
            return;
        }

        //Vérification de l'URI du comptage
        if(!nodeCorpusForEdit.isOnlyUriLink()){
            if(nodeCorpusForEdit.getUriCount().isEmpty()) {
                showMessage(FacesMessage.SEVERITY_ERROR, "L'URI pour le comptage est obligatoire !");
                return;
            }
        }

        var corpusFind = corpusLinkRepository.findByIdThesoAndCorpusName(selectedTheso.getCurrentIdTheso(),
                nodeCorpusForEdit.getCorpusName());
        if (corpusFind.isPresent() && !nodeCorpusForEdit.getCorpusName().equalsIgnoreCase(oldName)) {
            showMessage(FacesMessage.SEVERITY_INFO, "Ce corpus existe déjà, changez de nom !");
            return;
        }

        var corpusToUpdate = corpusLinkRepository.findByIdThesoAndCorpusName(selectedTheso.getCurrentIdTheso(), oldName);
        if (corpusToUpdate.isPresent()) {
            corpusToUpdate.get().setCorpusName(nodeCorpusForEdit.getCorpusName());
            corpusToUpdate.get().setUriLink(nodeCorpusForEdit.getUriLink());
            corpusToUpdate.get().setUriCount(nodeCorpusForEdit.getUriCount());
            corpusToUpdate.get().setOnlyUriLink(nodeCorpusForEdit.isOnlyUriLink());
            corpusToUpdate.get().setOmekaS(nodeCorpusForEdit.isOmekaS());
            corpusToUpdate.get().setActive(nodeCorpusForEdit.isActive());
            corpusLinkRepository.save(corpusToUpdate.get());

            showMessage(FacesMessage.SEVERITY_INFO, "Corpus modifié avec succès");
            PrimeFaces.current().executeScript("PF('editCorpus').hide();");

            init();

            PrimeFaces.current().ajax().update("messageIndex");
            PrimeFaces.current().ajax().update("containerIndex");
        } else {
            showMessage(FacesMessage.SEVERITY_INFO, "Erreur de modification de corpus !");
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }

    public void addNewCorpus() {

        PrimeFaces pf = PrimeFaces.current();
        if (nodeCorpusForEdit == null) {
            return;
        }

        if (nodeCorpusForEdit.getCorpusName().isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le nom du corpus est obligatoire !");
            return;
        }

        //Vérification de l'URI
        if (nodeCorpusForEdit.getUriLink().isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "L'URI du lien est obligatoire !");
            return;
        }

        //Vérification de l'URI du comptage
        if(!nodeCorpusForEdit.isOnlyUriLink() && nodeCorpusForEdit.getUriCount().isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "L'URI pour le comptage est obligatoire !");
            return;
        }

        var corpusFind = corpusLinkRepository.findByIdThesoAndCorpusName(selectedTheso.getCurrentIdTheso(),
                nodeCorpusForEdit.getCorpusName());
        if (corpusFind.isPresent()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Ce corpus existe déjà, changez de nom !");
            return;
        }

        corpusLinkRepository.save(CorpusLink.builder()
                .idTheso(selectedTheso.getCurrentIdTheso())
                .corpusName(nodeCorpusForEdit.getCorpusName())
                .active(nodeCorpusForEdit.isActive())
                .omekaS(nodeCorpusForEdit.isOmekaS())
                .onlyUriLink(nodeCorpusForEdit.isOnlyUriLink())
                .uriLink(nodeCorpusForEdit.getUriLink())
                .uriCount(nodeCorpusForEdit.getUriCount())
                .build());

        showMessage(FacesMessage.SEVERITY_INFO, "Corpus crée avec succès");
        PrimeFaces.current().executeScript("PF('newCorpus').hide();");
        init();

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
    }

    public void deleteCorpus() {

        if (nodeCorpusForEdit == null) {
            return;
        }

        corpusLinkRepository.deleteCorpusLinkByIdThesoAndCorpusName(selectedTheso.getCurrentIdTheso(), nodeCorpusForEdit.getCorpusName());

        showMessage(FacesMessage.SEVERITY_INFO, "Corpus suprimé avec succès");
        PrimeFaces.current().executeScript("PF('newCorpus').hide();");
        
        init();
        PrimeFaces.current().ajax().update("containerIndex");
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

    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesMessage msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }
}
