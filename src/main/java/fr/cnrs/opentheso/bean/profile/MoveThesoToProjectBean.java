package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.entites.UserGroupThesaurus;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.users.NodeUserGroup;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.PrimeFaces;
import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named(value = "moveThesoToProjectBean")
public class MoveThesoToProjectBean implements Serializable {

    private final MyProjectBean myProjectBean;
    private final CurrentUser currentUser;
    private final SuperAdminBean superAdminBean;
    private final UserGroupLabelRepository userGroupLabelRepository;
    private final UserRoleGroupRepository userRoleGroupRepository;
    private final UserGroupThesaurusRepository userGroupThesaurusRepository;
    
    private NodeIdValue selectedThesoToMove;
    private NodeUserGroup newProject;


    public void setTheso(NodeIdValue selectedThesoToMove) {
        this.selectedThesoToMove = selectedThesoToMove;
        newProject = null; 
    }   
    
    public void setThesoSuperAdmin(String idTheso, String thesoName) {
        selectedThesoToMove = new NodeIdValue();
        selectedThesoToMove.setId(idTheso);
        selectedThesoToMove.setValue(thesoName);
        newProject = null; 
    }       
    
    public List<NodeUserGroup> autoCompleteProject(String projectName) {
        if(currentUser.getNodeUser().isSuperAdmin()) {
            return userGroupLabelRepository.findAll().stream()
                    .map(group -> NodeUserGroup.builder().idGroup(group.getId()).groupName(group.getLabel()).build())
                    .toList();
        } else {
            return userRoleGroupRepository.findGroupByUserAndProject(currentUser.getNodeUser().getIdUser(), projectName);
        }
    }        

    public void moveThesoToProject(boolean isSuerpAdmin){

        if(ObjectUtils.isEmpty(newProject)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucun projet sélectioné !!!");
            return;              
        }

        userGroupThesaurusRepository.deleteByIdThesaurus(selectedThesoToMove.getId());
        userGroupThesaurusRepository.save(UserGroupThesaurus.builder()
                .idGroup(newProject.getIdGroup())
                .idThesaurus(selectedThesoToMove.getId())
                .build());

        showMessage(FacesMessage.SEVERITY_INFO, "Projet déplacé avec succès !!!");
        if (isSuerpAdmin) {
            superAdminBean.init();
        } else {
            myProjectBean.init();
        }
        PrimeFaces.current().ajax().update("containerIndex:contenu");
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        var msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }
    
}
