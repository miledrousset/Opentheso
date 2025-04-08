package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.models.users.NodeUserGroupUser;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;


@Data
@SessionScoped
@NoArgsConstructor
@Named(value = "superAdminBean")
public class SuperAdminBean implements Serializable {

    private UserRoleGroupRepository userRoleGroupRepository;
    private UserRepository userRepository;
    private ThesaurusRepository thesaurusRepository;
    private UserGroupThesaurusRepository userGroupThesaurusRepository;
    private UserGroupLabelRepository userGroupLabelRepository;
    private CurrentUser currentUser;
    private SelectedTheso selectedTheso;

    private List<NodeUserGroupUser> nodeUserGroupUsers; // liste des utilisateurs + projets + roles
    private List<UserGroupLabel> allProjects;
    private List<NodeUserGroupThesaurus> allThesoProject;
    private String workLanguage;


    @Inject
    public SuperAdminBean(@Value("${settings.workLanguage:fr}") String workLanguage,
                          UserRoleGroupRepository userRoleGroupRepository,
                          UserRepository userRepository,
                          ThesaurusRepository thesaurusRepository,
                          UserGroupThesaurusRepository userGroupThesaurusRepository,
                          UserGroupLabelRepository userGroupLabelRepository,
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
        allProjects.sort(Comparator.comparing(UserGroupLabel::getLabel, String.CASE_INSENSITIVE_ORDER));

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
