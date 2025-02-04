package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;

import java.io.IOException;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "deleteThesoBean")
@SessionScoped
public class DeleteThesoBean implements Serializable {

    private final SelectedTheso selectedTheso;
    private final RoleOnThesoBean roleOnThesoBean;
    private final ConceptHelper conceptHelper;
    private final PreferencesHelper preferencesHelper;
    private final ThesaurusHelper thesaurusHelper;
    private final UserGroupThesaurusRepository userGroupThesaurusRepository;
    
    private String idThesoToDelete, valueOfThesoToDelelete, currentIdTheso;
    private boolean isDeleteOn, deletePerennialIdentifiers;


    public DeleteThesoBean(SelectedTheso selectedTheso,
                           RoleOnThesoBean roleOnThesoBean,
                           ConceptHelper conceptHelper,
                           PreferencesHelper preferencesHelper,
                           ThesaurusHelper thesaurusHelper,
                           UserGroupThesaurusRepository userGroupThesaurusRepository) {

        this.selectedTheso = selectedTheso;
        this.roleOnThesoBean = roleOnThesoBean;
        this.conceptHelper = conceptHelper;
        this.preferencesHelper = preferencesHelper;
        this.thesaurusHelper = thesaurusHelper;
        this.userGroupThesaurusRepository = userGroupThesaurusRepository;
    }
    
    public void init() {
        idThesoToDelete = null;
        valueOfThesoToDelelete = null;
        isDeleteOn = false;     
        currentIdTheso = null;
        deletePerennialIdentifiers = false;
    }
    
    public void confirmDelete(NodeIdValue nodeTheso, String cucurrentIdTheso) throws IOException {
        this.idThesoToDelete = nodeTheso.getId();
        this.valueOfThesoToDelelete = nodeTheso.getValue();
        isDeleteOn = true;
        deletePerennialIdentifiers = false;
        // récupération de l'idTheso en cours
        this.currentIdTheso = cucurrentIdTheso;
    }
    
    /**
     * Permet de supprimer un thésaurus 
     */
    public void deleteTheso(CurrentUser currentUser) throws IOException {
        if(idThesoToDelete == null) return;

        var nodePreference = preferencesHelper.getThesaurusPreferences(idThesoToDelete);
        if(nodePreference != null) {
            // suppression des Identifiants Handle
            conceptHelper.setNodePreference(nodePreference);
            if(deletePerennialIdentifiers) {
                conceptHelper.deleteAllIdHandle(idThesoToDelete);
            }
        }
        
        // supression des droits
        userGroupThesaurusRepository.deleteByIdThesaurus(idThesoToDelete);
        
        // suppression complète du thésaurus
        if(!thesaurusHelper.deleteThesaurus(idThesoToDelete)){
            var msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur pendant la suppression !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }
        // vérification si le thésaurus supprimé est en cours de consultation, alors il faut nettoyer l'écran
        if(idThesoToDelete.equalsIgnoreCase(currentIdTheso)) {
            selectedTheso.setSelectedIdTheso("");
            selectedTheso.setSelectedLang(null);
            selectedTheso.setSelectedTheso();
            selectedTheso.setProjectIdSelected("-1");
            selectedTheso.setSelectedProject();
        }
        
        var msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Thesaurus supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("containerIndex");

        init();
        roleOnThesoBean.showListTheso(currentUser, selectedTheso);
        PrimeFaces.current().ajax().update("messageIndex");
    }
    
    public void setThesaurusBeforRemove(String idThesoToDelete, String valueOfThesoToDelelete) {
        this.idThesoToDelete = idThesoToDelete;
        this.valueOfThesoToDelelete = valueOfThesoToDelelete;
    }
}
