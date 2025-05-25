package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class ProjectService {

    private final UserRoleGroupRepository userRoleGroupRepository;
    private final UserGroupLabelRepository userGroupLabelRepository;
    private final UserGroupThesaurusRepository userGroupThesaurusRepository;
    private final ThesaurusRepository thesaurusRepository;


    public List<UserGroupLabel> getAllProjects() {

        log.info("Recherche de tous les projets existant");
        var projectList = userGroupLabelRepository.findAll();
        projectList.sort(Comparator.comparing(UserGroupLabel::getLabel, String.CASE_INSENSITIVE_ORDER));
        return projectList;
    }

    public void deleteProject(int idGroup) {

        var userGroupLabel = userGroupLabelRepository.findById(idGroup);
        if (userGroupLabel.isEmpty()) {
            log.error("Aucun userGroupLabel n'existe pas avec id {}", idGroup);
            return;
        }

        userRoleGroupRepository.deleteByGroup(userGroupLabel.get());
        userGroupThesaurusRepository.deleteByIdGroup(userGroupLabel.get().getId());
        userGroupLabelRepository.deleteById(userGroupLabel.get().getId());
    }

    public List<NodeUserGroupThesaurus> getAllThesaurusProjects(String idLang) {

        log.info("Recherche de tous les projets thésaurus existant");
        List<NodeUserGroupThesaurus> allThesaurusProject = new ArrayList<>();
        allThesaurusProject.addAll(userGroupThesaurusRepository.getAllGroupTheso(idLang));
        allThesaurusProject.addAll(thesaurusRepository.getAllThesaurusWithoutGroup(idLang));
        return allThesaurusProject;
    }

    public UserGroupLabel getUserGroupLabelByLabel(String projectName) {

        log.info("Recherche du userGroupLabelByLabel {}", projectName);
        var userGroupLabel = userGroupLabelRepository.findByLabelLike(projectName);
        if (userGroupLabel.isEmpty()) {
            log.error("Aucun userGroupLabel n'existe avec le label {}", projectName);
            return null;
        }
        return userGroupLabel.get();
    }

    public List<UserGroupLabel> getProjectByUser(int idUser, int idRole) {

        log.info("Recherche des projets de l'utilisateur id {}", idUser);
        return userGroupLabelRepository.findProjectsByRole(idUser, idRole);
    }

    public UserGroupLabel saveNewProject(String projectName) {

        log.info("Enregistrement d'un nouveau projet avec le nom {}", projectName);
        return userGroupLabelRepository.save(UserGroupLabel.builder().label(projectName).build());
    }

    public UserGroupLabel saveNewProject(UserGroupLabel userGroupLabel) {

        log.info("Enregistrement d'un nouveau projet avec le nom {}", userGroupLabel.getLabel());
        return userGroupLabelRepository.save(userGroupLabel);
    }
}
