/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package deepl;

import com.deepl.api.Language;
import com.deepl.api.LanguageType;
import com.deepl.api.TextResult;
import com.deepl.api.TextTranslationOptions;
import com.deepl.api.Translator;
import com.deepl.api.Usage;
import java.time.LocalDate;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class Translate {
    
    public Translate() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    

    public void Translate() throws Exception {
        Translator translator;
        String authKey = "";  // Replace with your key
        translator = new Translator(authKey);
        TextResult result =
                translator.translateText("Cette branche concerne les secteurs d’activité professionnelle ou potentiellement professionnelle socialement et/ou légalement reconnus sur la base de critères d’appartenance, d'efficacité des pratiques employées, de l’adoption de méthodes communes et de la capacité de transfert de connaissances et d’expertise à ce secteur professionnel.", "fr", "en-GB");
        System.out.println(result.getText()); // "Bonjour, le monde !"
        result = translator.translateText("Activité d'achat et de revente de biens et de services.", "fr", "en-GB");
        System.out.println(result.getText()); // "Bonjour, le monde !"      
        result = translator.translateText("voiture", "fr", "en-GB");
        System.out.println(result.getText()); // "Bonjour, le monde !"      
        
        
        
    
        /// liste des langues acceptées pour la tradcution 
        List<Language> sourcelang =  translator.getSourceLanguages();
        for (Language language : sourcelang) {
            System.out.println("les langues acceptées en source :" + language.getCode() + " (" + language.getName() + ")");
        }
        List<Language> targetLang =  translator.getSourceLanguages();
        for (Language language : targetLang) {
            System.out.println("les langues acceptées en target :" + language.getCode() + " (" + language.getName() + ")");
        }        
        
        Usage usage = translator.getUsage();

    }
    
    @Test
    public void getDate(){
                LocalDate currentDate = LocalDate.now();
                System.out.println("deepl.Translate.getDate()" + currentDate);
    }
}
