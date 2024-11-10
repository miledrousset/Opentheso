package fr.cnrs.opentheso.bean.setting;

import fr.cnrs.opentheso.repositories.CorpusHelper;
import fr.cnrs.opentheso.models.nodes.NodeCorpus;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "corpusBean")
@SessionScoped
public class CorpusBean implements Serializable {

    @Autowired
    private Connect connect;

    @Autowired
    private SelectedTheso selectedTheso;

    @Autowired
    private CorpusHelper corpusHelper;

    private ArrayList<NodeCorpus> nodeCorpuses;

    private String oldName;
    private NodeCorpus nodeCorpusForEdit;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeCorpuses != null) {
            nodeCorpuses.clear();
            nodeCorpuses = null;
        }
        oldName = null;
        nodeCorpusForEdit = null;
    }

    public CorpusBean() {
    }

    public void init() {
        nodeCorpuses = corpusHelper.getAllCorpus(selectedTheso.getCurrentIdTheso());
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
        if(!nodeCorpusForEdit.isOnlyUriLink() && nodeCorpusForEdit.getUriCount().isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "L'URI pour le comptage est obligatoire !");
            return;
        }
        if (nodeCorpusForEdit.isOnlyUriLink() ) {
            showMessage(FacesMessage.SEVERITY_ERROR, "L'URI de comptage n'est pas valide !");
            return;
        }

        if (corpusHelper.isCorpusExist(selectedTheso.getCurrentIdTheso(),
                nodeCorpusForEdit.getCorpusName()) && !nodeCorpusForEdit.getCorpusName().equalsIgnoreCase(oldName)) {
            showMessage(FacesMessage.SEVERITY_INFO, "Ce corpus existe déjà, changez de nom !");
            return;
        }

        if (!corpusHelper.updateCorpus(selectedTheso.getCurrentIdTheso(), oldName, nodeCorpusForEdit)) {
            showMessage(FacesMessage.SEVERITY_INFO, "Erreur de modification de corpus !");
            return;
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Corpus modifié avec succès");
        PrimeFaces.current().executeScript("PF('editCorpus').hide();");
        init();

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
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
    /*    if (!UrlUtils.isAPIAvailable(nodeCorpusForEdit.getUriLink())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "L'URI n'est pas valide !");
            return;
        }*/

        //Vérification de l'URI du comptage
        if(!nodeCorpusForEdit.isOnlyUriLink() && nodeCorpusForEdit.getUriCount().isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "L'URI pour le comptage est obligatoire !");
            return;
        }
        if (nodeCorpusForEdit.isOnlyUriLink() ) {
            showMessage(FacesMessage.SEVERITY_ERROR, "L'URI de comptage n'est pas valide !");
            return;
        }

        if (corpusHelper.isCorpusExist(selectedTheso.getCurrentIdTheso(), nodeCorpusForEdit.getCorpusName())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Ce corpus existe déjà, changez de nom !");
            return;
        }

        if (!corpusHelper.addNewCorpus(selectedTheso.getCurrentIdTheso(), nodeCorpusForEdit)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de création du corpus !");
            return;
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Corpus crée avec succès");
        PrimeFaces.current().executeScript("PF('newCorpus').hide();");
        init();

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
    }

    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesMessage msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public void deleteCorpus() {

        if (nodeCorpusForEdit == null) {
            return;
        }

        if (!corpusHelper.deleteCorpus(selectedTheso.getCurrentIdTheso(), nodeCorpusForEdit.getCorpusName())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de suppression de corpus !");
            return;
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Corpus suprimé avec succès");
        PrimeFaces.current().executeScript("PF('newCorpus').hide();");
        
        init();
        PrimeFaces.current().ajax().update("containerIndex");
    }

    
    public void setCorpusForEdit(NodeCorpus nodeCorpus) {
        nodeCorpusForEdit = new NodeCorpus();
        
        nodeCorpusForEdit.setActive(nodeCorpus.isActive());
        nodeCorpusForEdit.setCorpusName(nodeCorpus.getCorpusName());
        nodeCorpusForEdit.setOnlyUriLink(nodeCorpus.isOnlyUriLink());
        nodeCorpusForEdit.setUriLink(nodeCorpus.getUriLink());
        nodeCorpusForEdit.setUriCount(nodeCorpus.getUriCount());

        oldName = nodeCorpus.getCorpusName();
    }

}
