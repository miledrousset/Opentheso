package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.entites.HomePage;
import fr.cnrs.opentheso.repositories.HomePageRepository;
import jakarta.inject.Inject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class HomePageService {

    private String workLanguage;
    private LanguageBean languageBean;
    private HomePageRepository homePageRepository;


    @Inject
    public HomePageService(@Value("${settings.workLanguage:fr}") String workLanguage,
                           LanguageBean languageBean,
                           HomePageRepository homePageRepository) {

        this.workLanguage = workLanguage;
        this.languageBean = languageBean;
        this.homePageRepository = homePageRepository;
    }

    public boolean setHomePage(String htmlText, String idLang) {

        var homePage = homePageRepository.findByLang(idLang);
        var homePageValue = fr.cnrs.opentheso.utils.StringUtils.convertString(htmlText);

        if (homePage.isPresent() && StringUtils.isNotEmpty(homePage.get().getHtmlCode())) {
            return homePageRepository.updateHtmlCodeByLang(homePageValue, idLang) > 0;
        } else {
            var result = homePageRepository.save(HomePage.builder()
                    .htmlCode(homePageValue)
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

}
