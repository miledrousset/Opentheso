package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.models.users.NodeUserGroupThesaurus;
import fr.cnrs.opentheso.models.users.NodeUserGroupUser;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.services.ProjectService;
import fr.cnrs.opentheso.services.UserService;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;


@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "superAdminBean")
public class SuperAdminBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private final CurrentUser currentUser;
    private final UserService userService;
    private final SelectedTheso selectedTheso;
    private final ProjectService projectService;

    private List<NodeUserGroupUser> nodeUserGroupUsers;
    private List<UserGroupLabel> allProjects;
    private List<NodeUserGroupThesaurus> allThesaurusProject;


    public void init() {

        allProjects = projectService.getAllProjects();

        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeUserGroupUsers = userService.getAllUserGroup();
        }

        var idLang = StringUtils.isEmpty(selectedTheso.getCurrentLang())
                ? workLanguage
                : selectedTheso.getCurrentLang();

        allThesaurusProject = projectService.getAllThesaurusProjects(idLang);
    }
}
