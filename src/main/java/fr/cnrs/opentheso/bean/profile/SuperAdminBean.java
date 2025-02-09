package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository2;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.models.users.NodeUserGroupUser;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;


@Data
@Named(value = "superAdminBean")
@SessionScoped
public class SuperAdminBean implements Serializable {

    private final UserRoleGroupRepository userRoleGroupRepository;
    private final UserRepository userRepository;
    private final ThesaurusRepository thesaurusRepository;
    private final UserGroupThesaurusRepository userGroupThesaurusRepository;
    private final UserGroupLabelRepository2 userGroupLabelRepository;
    private final CurrentUser currentUser;
    private final SelectedTheso selectedTheso;

    private List<NodeUserGroupUser> nodeUserGroupUsers; // liste des utilisateurs + projets + roles
    private List<UserGroupLabel> allProjects;
    private List<NodeUserGroupThesaurus> allThesoProject;
    private String workLanguage;

    public SuperAdminBean(@Value("${settings.workLanguage:fr}") String workLanguage,
                          UserRoleGroupRepository userRoleGroupRepository,
                          UserRepository userRepository,
                          ThesaurusRepository thesaurusRepository,
                          UserGroupThesaurusRepository userGroupThesaurusRepository,
                          UserGroupLabelRepository2 userGroupLabelRepository,
                          CurrentUser currentUser,
                          SelectedTheso selectedTheso) {

        this.workLanguage = workLanguage;
        this.userRoleGroupRepository = userRoleGroupRepository;
        this.userRepository = userRepository;
        this.thesaurusRepository = thesaurusRepository;
        this.userGroupThesaurusRepository = userGroupThesaurusRepository;
        this.userGroupLabelRepository = userGroupLabelRepository;
        this.currentUser = currentUser;
        this.selectedTheso = selectedTheso;
    }

    public void init() {

        allProjects = userGroupLabelRepository.findAll();

        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeUserGroupUsers = userRoleGroupRepository.getAllGroupUser();
            nodeUserGroupUsers.addAll(userRepository.getAllGroupUserWithoutGroup());
            nodeUserGroupUsers.addAll(userRepository.getAllUsersSuperadmin());
        }

        var idLang = StringUtils.isEmpty(selectedTheso.getCurrentLang())
                ? workLanguage
                : selectedTheso.getCurrentLang();

        allThesoProject = new ArrayList<>();
        allThesoProject.addAll(userGroupThesaurusRepository.getAllGroupTheso(idLang));
        allThesoProject.addAll(thesaurusRepository.getAllThesaurusWithoutGroup(idLang));
    }
}
