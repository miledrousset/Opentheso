package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.entites.LanguageIso639;
import fr.cnrs.opentheso.repositories.LanguageRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.primefaces.PrimeFaces;
import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@Named(value = "flagBean")
@SessionScoped
@RequiredArgsConstructor
public class FlagBean implements Serializable {

    private final LanguageRepository languageRepository;

    private List<LanguageIso639> allLanguages;


    public void init() {
        allLanguages = languageRepository.findAll();
    }

    public void updateLang(LanguageIso639 language) {
        languageRepository.save(language);
        init();
        PrimeFaces.current().executeScript("window.location.reload();");
    }
    
}
