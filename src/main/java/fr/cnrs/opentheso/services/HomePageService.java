package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.entites.HomePage;
import fr.cnrs.opentheso.entites.Info;
import fr.cnrs.opentheso.repositories.HomePageRepository;
import fr.cnrs.opentheso.repositories.InfoRepository;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class HomePageService {

    @Value("${settings.workLanguage:fr}")
    private String workLanguage;

    private final LanguageBean languageBean;
    private final InfoRepository infoRepository;
    private final HomePageRepository homePageRepository;


    public boolean setHomePage(String htmlText, String idLang) {
        var homePage = homePageRepository.findByLang(idLang);
        if (homePage.isPresent() && StringUtils.isNotEmpty(homePage.get().getHtmlCode())) {
            return homePageRepository.updateHtmlCodeByLang(htmlText, idLang) > 0;
        } else {
            var result = homePageRepository.save(HomePage.builder()
                    .htmlCode(htmlText)
                    .lang(idLang)
                    .build());
            return ObjectUtils.isNotEmpty(result);
        }
    }

    public String getLanguage() {

        var lang = languageBean.getIdLangue().toLowerCase();
        return StringUtils.isEmpty(lang) ? workLanguage : lang;
    }

    public String getHomePage() {

        var homePage = homePageRepository.findByLang(getLanguage());
        return homePage.isPresent() ? homePage.get().getHtmlCode() : "";
    }

    public void setCodeGoogleAnalytics(String codeGoogleAnalytics) {
        var informations = infoRepository.findAll();
        if (CollectionUtils.isNotEmpty(informations)) {
            informations.get(0).setGoogleanalytics(codeGoogleAnalytics);
            infoRepository.save(informations.get(0));
        } else {
            infoRepository.save(Info.builder().versionOpentheso("0.0.0").versionBdd("xyz").googleanalytics(codeGoogleAnalytics).build());
        }
    }

    public String getCodeGoogleAnalytics() {
        var informations = infoRepository.findAll();
        return informations.isEmpty() ? "" : informations.get(0).getGoogleanalytics();
    }

}
