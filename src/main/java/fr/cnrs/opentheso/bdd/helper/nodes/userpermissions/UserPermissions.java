
package fr.cnrs.opentheso.bdd.helper.nodes.userpermissions;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 *
 * @author miledrousset
 */
@Data
public class UserPermissions {

    // infos à initialiser après la sélection d'un projet ou thésaurus
    private String selectedTheso;
    private String selectedThesoName;
    private int projectOfselectedTheso;
    private String projectOfselectedThesoName;
    
    
    // le role sur ce thésaurus en cas d'utilisateur authentifié 
    private int role;
    private String roleName;
    
    
    // pour le projet sélectionné
    private int selectedProject;
    private String selectedProjectName;
    
    // liste des thésaurus du projet avec les rôles
    List<NodeThesoRole> listThesoRoleOfSelectedProject;    
    
    

    
    
    // infos pour l'utilisateur sélectionné après une authentification 
    
    // liste des projets de l'utilisateur
    private Map<String, String> projectlist;
    
    // Liste des projets de l'utilisateur, avec la liste des thesaurus/role par projet
    List<NodeProjectThesoRole> nodeProjectThesoRoles;
   
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
