package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.Thesaurus;
import fr.cnrs.opentheso.entites.UserGroupThesaurus;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.repositories.ThesaurusLabelRepository;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserGroupThesaurusRepository;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class ThesaurusService {

    private final PreferenceService preferenceService;
    private final ThesaurusRepository thesaurusRepository;
    private final ThesaurusLabelRepository thesaurusLabelRepository;
    private final UserGroupThesaurusRepository userGroupThesaurusRepository;


    public Thesaurus getThesaurusById(String idThesaurus) {
        return thesaurusRepository.findById(idThesaurus).get();
    }

    public List<NodeIdValue> getThesaurusOfProject(int idGroup, String idLang) {

        var thesaurusList = userGroupThesaurusRepository.findAllByIdGroup(idGroup);

        List<NodeIdValue> nodeIdValues = new ArrayList<>();

        for (UserGroupThesaurus thesaurus : thesaurusList) {

            var idLangTemp = preferenceService.getWorkLanguageOfThesaurus(thesaurus.getIdThesaurus());
            if (StringUtils.isEmpty(idLangTemp)) {
                idLangTemp = idLang;
            }

            var thesaurusLabel = thesaurusLabelRepository.findByIdThesaurusAndLang(thesaurus.getIdThesaurus(), idLangTemp);

            var thesaurusDetails = thesaurusRepository.findById(thesaurus.getIdThesaurus());

            nodeIdValues.add(NodeIdValue.builder()
                    .id(thesaurus.getIdThesaurus())
                    .value(thesaurusLabel.get().getTitle())
                    .status(thesaurusDetails.get().getIsPrivate())
                    .build());
        }
        return nodeIdValues;
    }
}
