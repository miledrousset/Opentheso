package fr.cnrs.opentheso.services;

import com.deepl.api.Language;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;


@Data
@Slf4j
@Service
public class DeeplService {
    
    /**
     * permet de traduire un texte d'une langue vers une autre
     */
    public String translate(String authKey, String value, String fromLang, String toLang){
        try {
            TextResult result = new Translator(authKey).translateText(value, fromLang, toLang);
            return result.getText().trim();
        } catch (Exception ex) {
            log.error(ex.toString());
            return null;
        }
    }

    /**
     * permet de connaitre les langues disponibles en entrée
     */
    public List<Language> getSourceLanguages(String authKey){
        try {
            return new Translator(authKey).getSourceLanguages();
        } catch (Exception ex) {
            log.error(ex.toString());
            return null;
        }
    }
    
    /**
     * permet de connaitre les langues disponibles en cible
     */
    public List<Language> getTargetLanguages(String authKey){
        try {
            return new Translator(authKey).getTargetLanguages();
        } catch (Exception ex) {
            log.error(ex.toString());
            return null;
        }
    }
}
