package fr.cnrs.opentheso.models.userpermissions;

import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import java.io.Serializable;
import lombok.Data;
import java.util.List;


@Data
public class UserPermissions implements Serializable{

    // chargenement de tous les projets (non connecté) ou après connection
    private List<UserGroupLabel> listProjects;
    // chargenement de tous les thésaurus (non connecté)ou après sélection d'un projet
    private List <NodeIdValue> listThesos;
    
    // infos à initialiser après la sélection d'un projet ou thésaurus
    private String selectedTheso;
    private String selectedThesoName;
    private int projectOfselectedTheso;
    private String projectOfselectedThesoName;
    private String preferredLangOfSelectedTheso;
    private List<NodeLangTheso> listLangsOfSelectedTheso;
    
    // le role sur ce thésaurus en cas d'utilisateur authentifié 
    private int role;
    private String roleName;

    // le projet sélectionné
    private int selectedProject;
    private String selectedProjectName;
    
    
    // infos pour l'utilisateur sélectionné après une authentification 
    // Liste des projets de l'utilisateur, avec la liste des thesaurus/role par projet
    List<NodeProjectThesoRole> nodeProjectsWithThesosRoles;

    public UserPermissions() {
        selectedProject = -1;
    }
    public boolean isSuperAdmin(){
        return role == 1;   
    }
    public boolean isAdmin(){
        return role == 2; 
    }    
    public boolean isManager(){
        return role == 3; 
    }
    public boolean isContributor(){
        return role == 4; 
    }     
    public boolean isNoRole(){
        return role == -1; 
    }
    
}
