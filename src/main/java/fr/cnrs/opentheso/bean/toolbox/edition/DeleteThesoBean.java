package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;

import java.io.IOException;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "deleteThesoBean")
@SessionScoped
public class DeleteThesoBean implements Serializable {
    
    @Autowired @Lazy 
    private SelectedTheso selectedTheso;
    
    @Autowired @Lazy
    private RoleOnThesoBean roleOnThesoBean;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private UserHelper userHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;
    
    private String idThesoToDelete;
    private String valueOfThesoToDelelete;
    private boolean isDeleteOn;
    
    private boolean deletePerennialIdentifiers;
            
    private String currentIdTheso;
    /**
     * Creates a new instance of DeleteThesoBean
     */
    public DeleteThesoBean() {
        idThesoToDelete = null;
        valueOfThesoToDelelete = null;
        isDeleteOn = false;
        currentIdTheso = null;
        deletePerennialIdentifiers = false;
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
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(idThesoToDelete);
        if(nodePreference != null) {
            // suppression des Identifiants Handle
            conceptHelper.setNodePreference(nodePreference);
            if(deletePerennialIdentifiers) {
                conceptHelper.deleteAllIdHandle(idThesoToDelete);
            }
        }
        FacesMessage msg;
        
        // supression des droits
        if(!userHelper.deleteThesoFromGroup(idThesoToDelete)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur pendant la suppression !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        // suppression complète du thésaurus
        if(!thesaurusHelper.deleteThesaurus(idThesoToDelete)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur pendant la suppression !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
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
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Thesaurus supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init();
        roleOnThesoBean.showListTheso(currentUser);
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }    
    }
    
    public void setThesaurusBeforRemove(String idThesoToDelete, String valueOfThesoToDelelete) {
        this.idThesoToDelete = idThesoToDelete;
        this.valueOfThesoToDelelete = valueOfThesoToDelelete;
    }

    public boolean isIsDeleteOn() {
        return isDeleteOn;
    }

    public void setIsDeleteOn(boolean isDeleteOn) {
        this.isDeleteOn = isDeleteOn;
    }

    public String getIdThesoToDelete() {
        return idThesoToDelete;
    }

    public void setIdThesoToDelete(String idThesoToDelete) {
        this.idThesoToDelete = idThesoToDelete;
    }

    public String getValueOfThesoToDelelete() {
        return valueOfThesoToDelelete;
    }

    public void setValueOfThesoToDelelete(String valueOfThesoToDelelete) {
        this.valueOfThesoToDelelete = valueOfThesoToDelelete;
    }

    public boolean isDeletePerennialIdentifiers() {
        return deletePerennialIdentifiers;
    }

    public void setDeletePerennialIdentifiers(boolean deletePerennialIdentifiers) {
        this.deletePerennialIdentifiers = deletePerennialIdentifiers;
    }


    
    
    
}
