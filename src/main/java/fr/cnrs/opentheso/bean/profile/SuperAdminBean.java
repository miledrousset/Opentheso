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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


@Data
@Named(value = "superAdminBean")
@SessionScoped
public class SuperAdminBean implements Serializable {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    @Autowired
    private UserRoleGroupRepository userRoleGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ThesaurusRepository thesaurusRepository;

    @Autowired
    private UserGroupThesaurusRepository userGroupThesaurusRepository;

    @Autowired
    private UserGroupLabelRepository2 userGroupLabelRepository;
    
    @Autowired
    private CurrentUser currentUser;
    
    @Autowired
    private SelectedTheso selectedTheso;

    private List<NodeUserGroupUser> nodeUserGroupUsers; // liste des utilisateurs + projets + roles
    private List<UserGroupLabel> allProjects;
    private List<NodeUserGroupThesaurus> allThesoProject;


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
