package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.models.languages.Languages_iso639;
import fr.cnrs.opentheso.repositories.LanguageHelper;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import lombok.Data;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

@Data
@Named(value = "flagBean")
@SessionScoped
public class FlagBean implements Serializable {

    @Autowired
    private LanguageHelper languageHelper;

    private List<Languages_iso639> allLangs;

    public FlagBean() {
    }

    public void init() {
        // toutes les langues Iso
        allLangs = languageHelper.getAllLanguages();
    }

    public void updateLang(Languages_iso639 language) {
        languageHelper.updateLanguage(language);
        init();
        PrimeFaces.current().executeScript("window.location.reload();");
    }

    
}
