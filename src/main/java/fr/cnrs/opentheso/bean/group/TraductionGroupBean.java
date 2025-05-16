package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.models.group.NodeGroupTraductions;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.services.GroupService;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "traductionGroupBean")
@SessionScoped
public class TraductionGroupBean implements Serializable {

    
    @Autowired @Lazy
    private GroupView groupView;
    @Autowired @Lazy
    private SelectedTheso selectedTheso;

    private String selectedLang;
    private ArrayList<NodeLangTheso> nodeLangs;
    private ArrayList<NodeLangTheso> nodeLangsFiltered; // uniquement les langues non traduits
    private List<NodeGroupTraductions> nodeGroupTraductionses;
    private String traductionValue;
    @Autowired
    private GroupService groupService;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        selectedLang = null;
        traductionValue = null;
        if (nodeLangs != null) {
            nodeLangs.clear();
            nodeLangs = null;
        }
        if (nodeLangsFiltered != null) {
            nodeLangsFiltered.clear();
            nodeLangsFiltered = null;
        }
        if (nodeGroupTraductionses != null) {
            nodeGroupTraductionses.clear();
            nodeGroupTraductionses = null;
        }
    }
    public void reset() {
        nodeLangs = selectedTheso.getNodeLangs();
        nodeLangsFiltered = new ArrayList<>();
        nodeGroupTraductionses = groupView.getNodeGroupTraductions();

        selectedLang = null;
        traductionValue = "";
    }

    public void setLangWithNoTraduction() {
        nodeLangs.forEach((nodeLang) -> {
            nodeLangsFiltered.add(nodeLang);
        });

        // les langues à ignorer
        ArrayList<String> langsToRemove = new ArrayList<>();
        langsToRemove.add(groupView.getNodeGroup().getIdLang());
        for (NodeGroupTraductions nodeGroupTraductions : groupView.getNodeGroupTraductions()) {
            langsToRemove.add(nodeGroupTraductions.getIdLang());
        }
        for (NodeLangTheso nodeLang : nodeLangs) {
            if (langsToRemove.contains(nodeLang.getCode())) {
                nodeLangsFiltered.remove(nodeLang);
            }
        }
        if (nodeLangsFiltered.isEmpty()) {
            infoNoTraductionToAdd();
        }
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Groupe !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void infoNoTraductionToAdd() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " Le Groupe est traduit déjà dans toutes les langues du thésaurus !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter une nouvelle traduction au groupe
     *
     * @param idUser
     */
    public void addNewTraduction(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        if (traductionValue == null || traductionValue.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Une valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Pas de langue choisie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (groupService.isDomainExist(traductionValue, selectedLang, selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Ce nom existe déjà dans cette langue !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        groupService.addGroupTraduction(groupView.getNodeGroup().getConceptGroup().getIdGroup(), selectedTheso.getCurrentIdTheso(),
                selectedLang, traductionValue);
        groupService.updateModifiedDate(groupView.getNodeGroup().getConceptGroup().getIdGroup(), selectedTheso.getCurrentIdTheso());

        groupView.getGroup(selectedTheso.getCurrentIdTheso(),
                groupView.getNodeGroup().getConceptGroup().getIdGroup(),
                groupView.getNodeGroup().getIdLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " traduction ajoutée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        reset();
        setLangWithNoTraduction();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    /**
     * permet de modifier une traduction au groupe
     *
     * @param nodeGroupTraductions
     * @param idUser
     */
    public void updateTraduction(NodeGroupTraductions nodeGroupTraductions, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        if (nodeGroupTraductions == null || nodeGroupTraductions.getTitle().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " veuillez saisir une valeur !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (groupService.isDomainExist(nodeGroupTraductions.getTitle(), nodeGroupTraductions.getIdLang(), selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Ce nom existe déjà dans cette langue !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (groupService.renameGroup(nodeGroupTraductions.getTitle(), nodeGroupTraductions.getIdLang(),
                groupView.getNodeGroup().getConceptGroup().getIdGroup(), selectedTheso.getCurrentIdTheso(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur d'ajout de traduction !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(),
                groupView.getNodeGroup().getConceptGroup().getIdGroup(),
                groupView.getNodeGroup().getIdLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " traduction modifiée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    /**
     * permet de supprimer une traduction au groupe
     *
     * @param nodeGroupTraductions
     * @param idUser
     */
    public void deleteTraduction(NodeGroupTraductions nodeGroupTraductions, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        if (nodeGroupTraductions == null || nodeGroupTraductions.getIdLang().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de sélection de tradcution !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (!groupService.deleteGroupTraduction(groupView.getNodeGroup().getConceptGroup().getIdGroup(),
                selectedTheso.getCurrentIdTheso(), nodeGroupTraductions.getIdLang())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(),
                groupView.getNodeGroup().getConceptGroup().getIdGroup(),
                groupView.getNodeGroup().getIdLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " traduction supprimée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        reset();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }
}
