package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.User;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserRepository;
import fr.cnrs.opentheso.repositories.UserRoleOnlyOnRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ThesaurusService thesaurusService;
    private final UserGroupThesaurusRepository userGroupThesaurusRepository;
    private final UserRoleOnlyOnRepository userRoleOnlyOnRepository;


    public void deleteByThesaurus(String idThesaurus) {

        var thesaurus = thesaurusService.getThesaurusById(idThesaurus);
        if (thesaurus != null) {
            return;
        }

        userRoleOnlyOnRepository.deleteByTheso(thesaurus);
        userGroupThesaurusRepository.deleteByIdThesaurus(idThesaurus);
    }

    public User getById(int id) {

        return userRepository.getById(id);
    }
}
