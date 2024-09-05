
package fr.cnrs.opentheso.bdd.helper;

import com.deepl.api.DeepLException;
import com.deepl.api.Language;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import com.deepl.api.Usage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author miledrousset
 */
public class DeeplHelper {

    private Translator translator;
    
    public DeeplHelper(String authKey) {
        translator = new Translator(authKey);        
    }
    
    /**
     * permet de traduire un texte d'une langue vers une autre
     * @param value
     * @param fromLang
     * @param toLang
     * @return 
     */
    public String translate(String value, String fromLang, String toLang){
        try {
            if(translator == null) return null;
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
     * @return 
     */
    public List<Language> getSourceLanguages(){
        try {
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
     * @return 
     */
    public List<Language> getTargetLanguages(){
        try {
            List<Language> targetLangs =  translator.getTargetLanguages();
            return targetLangs;
            
        } catch (DeepLException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }    
    
    /**
     * permet de connaitre le total de caractères consommés 
     * @return 
     */
    public Usage getUsage(){
        try {
            Usage usage = translator.getUsage();
            return usage;
            
        } catch (DeepLException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DeeplHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }     
    
}
