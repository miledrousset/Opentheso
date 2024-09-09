package fr.cnrs.opentheso.repositories;

import com.deepl.api.DeepLException;
import com.deepl.api.Language;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Data
@Service
public class DeeplHelper {
    
    /**
     * permet de traduire un texte d'une langue vers une autre
     */
    public String translate(String authKey, String value, String fromLang, String toLang){
        try {
            var translator = new Translator(authKey);
            TextResult result = translator.translateText(value, fromLang, toLang);
            return result.getText().trim();
           
        } catch (DeepLException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * permet de connaitre les langues disponibles en entrée
     */
    public List<Language> getSourceLanguages(String authKey){
        try {
            var translator = new Translator(authKey);
            List<Language> sourceLangs =  translator.getSourceLanguages();
            return sourceLangs;
            
        } catch (DeepLException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * permet de connaitre les langues disponibles en cible
     */
    public List<Language> getTargetLanguages(String authKey){
        try {
            var translator = new Translator(authKey);
            List<Language> targetLangs =  translator.getTargetLanguages();
            return targetLangs;
            
        } catch (DeepLException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }    

    
}
