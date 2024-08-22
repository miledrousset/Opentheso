/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package notes;


import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;


/**
 *
 * @author miledrousset
 */
public class RemoveHtmlBalises {
    
    public RemoveHtmlBalises() {
    }
    

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {
        String htmlContent = "<p>Ceci est un <strong>exemple</strong> de texte avec <a href='#'>des balises HTML</a>.</p>";
        String text = Jsoup.parse(htmlContent).text();
        System.out.println(text);
    }
}
