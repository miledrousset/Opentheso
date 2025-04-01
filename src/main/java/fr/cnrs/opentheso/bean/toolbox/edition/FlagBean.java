package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.entites.LanguageIso639;
import fr.cnrs.opentheso.repositories.LanguageRepository;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.primefaces.PrimeFaces;
import java.io.Serializable;
import java.util.List;


@Data
@Named(value = "flagBean")
@SessionScoped
@NoArgsConstructor
public class FlagBean implements Serializable {

    private LanguageRepository languageRepository;
    private List<LanguageIso639> allLanguages;


    @Inject
    public FlagBean(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    public void init() {
        allLanguages = languageRepository.findAll();
    }

    public void updateLang(LanguageIso639 language) {
        languageRepository.save(language);
        init();
        PrimeFaces.current().executeScript("window.location.reload();");
    }
    
}
