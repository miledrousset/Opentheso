package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.UserRoleGroup;
import fr.cnrs.opentheso.entites.UserRoleOnlyOn;
import fr.cnrs.opentheso.repositories.RoleRepository;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.UserRoleOnlyOnRepository;
import fr.cnrs.opentheso.repositories.UserRoleGroupRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@AllArgsConstructor
public class UserRoleGroupService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ThesaurusRepository thesaurusRepository;
    private final UserRoleGroupRepository userRoleGroupRepository;
    private final UserRoleOnlyOnRepository userRoleOnlyOnRepository;
    private final UserGroupLabelRepository userGroupLabelRepository;


    @Transactional
    public void addUserRoleOnGroup(Integer userId, Integer roleId, Integer groupId) {

        var user = userRepository.findById(userId).get();
        var role = roleRepository.findById(roleId).get();
        var group = userGroupLabelRepository.findById(groupId).get();

        var isSuperAdmin = role.getId() == 1;
        if (isSuperAdmin) {
            userRoleGroupRepository.deleteByUserAndGroup(user, group);
        } else {
            var userRoleGroup = UserRoleGroup.builder().user(user).role(role).group(group).build();
            userRoleGroupRepository.save(userRoleGroup);
        }

        user.setIsSuperAdmin(isSuperAdmin);
        userRepository.save(user);
    }

    @Transactional
    public void addUserRoleOnTheso(int userId, int roleId, int groupId, List<String> idThesos) {

        var user = userRepository.findById(userId).get();
        var role = roleRepository.findById(roleId).get();
        var group = userGroupLabelRepository.findById(groupId).get();

        userRoleGroupRepository.deleteByUserAndGroup(user, group);

        for (String idTheso : idThesos) {
            var thesaurus = thesaurusRepository.findById(idTheso);
            var userRoleGroupOn = UserRoleOnlyOn.builder()
                    .user(user)
                    .role(role)
                    .group(group)
                    .thesaurus(thesaurus.get())
                    .build();
            userRoleOnlyOnRepository.save(userRoleGroupOn);
        }
    }

    @Transactional
    public void updateUserRoleOnGroup(int userId, int roleId, int groupId) {

        var user = userRepository.findById(userId).get();
        var group = userGroupLabelRepository.findById(groupId).get();

        if (roleId == 1) {
            userRoleGroupRepository.deleteByUserAndGroup(user, group);
        } else {
            var role = roleRepository.findById(roleId).get();
            userRoleGroupRepository.updateUserRole(user, group, role);
        }

        var isSuperAdmin = (roleId == 1);
        user.setIsSuperAdmin(isSuperAdmin);
        userRepository.save(user);
    }

    public void deleteRoleByIdGroup(int groupId) {
        var group = userGroupLabelRepository.findById(groupId);
        if (group.isPresent()) {
            userRoleGroupRepository.deleteByGroup(group.get());
        }
    }
}
