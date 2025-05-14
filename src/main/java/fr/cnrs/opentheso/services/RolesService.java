package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.models.users.NodeUserRole;
import fr.cnrs.opentheso.repositories.ThesaurusLabelRepository;
import fr.cnrs.opentheso.repositories.UserRoleOnlyOnRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
@AllArgsConstructor
public class RolesService {

    private final PreferenceService preferenceService;
    private final ThesaurusLabelRepository thesaurusLabelRepository;
    private final UserRoleOnlyOnRepository userRoleOnlyOnRepository;


    public List<NodeUserRole> getListRoleByThesoLimited(int idGroup, int idUser) {

        var roles = userRoleOnlyOnRepository.getListRoleByThesoLimited(idGroup, idUser);

        roles.forEach(role -> {
            //getTitleOfThesaurus(
            var idLang = preferenceService.getWorkLanguageOfThesaurus(role.getIdTheso());
            var thesaurusTitle = thesaurusLabelRepository.findByIdThesaurusAndLang(role.getIdTheso(), idLang);
            role.setThesoName(thesaurusTitle.get().getTitle());
        });

        return roles;
    }
}
