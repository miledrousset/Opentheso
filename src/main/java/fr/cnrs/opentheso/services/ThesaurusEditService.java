package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.ThesaurusHomePage;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.repositories.ThesaurusDcTermRepository;
import fr.cnrs.opentheso.repositories.ThesaurusHomePageRepository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ThesaurusEditService {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private final ThesaurusDcTermRepository thesaurusDcTermRepository;
    private final ThesaurusHomePageRepository thesaurusHomePageRepository;


    public String getHtmlPage(String idThesaurus, String idLanguage) {

        idLanguage = StringUtils.isEmpty(idLanguage) ? workLanguage : idLanguage;
        var thesaurusHomePage = thesaurusHomePageRepository.findByIdThesoAndLang(idThesaurus, idLanguage);
        return thesaurusHomePage.isPresent() ? thesaurusHomePage.get().getHtmlCode() : "";
    }

    public ThesaurusHomePage updateThesaurusHomePage(String idThesaurus, String idLanguage, String text) {

        idLanguage = StringUtils.isEmpty(idLanguage) ? workLanguage : idLanguage;

        var thesaurusHomePage = thesaurusHomePageRepository.findByIdThesoAndLang(idThesaurus, idLanguage);
        ThesaurusHomePage thesaurusToSave;
        if (thesaurusHomePage.isEmpty()) {
            thesaurusToSave = ThesaurusHomePage.builder()
                    .idTheso(idThesaurus)
                    .lang(idLanguage)
                    .htmlCode(text)
                    .build();
        } else {
            thesaurusHomePage.get().setHtmlCode(text);
            thesaurusToSave = thesaurusHomePage.get();
        }

        return thesaurusHomePageRepository.save(thesaurusToSave);
    }

    public List<DcElement> getThesaurusMetaDatas(String idThesaurus){

        var dcElements = thesaurusDcTermRepository.findAllByIdThesaurus(idThesaurus);
        if (CollectionUtils.isEmpty(dcElements)) {
            return List.of();
        }

        return dcElements.stream()
                .map(element -> DcElement.builder()
                        .id(element.getId().intValue())
                        .name(element.getName())
                        .value(element.getValue())
                        .language(element.getLanguage())
                        .type(element.getDataType())
                        .build())
                .toList();
    }
}
