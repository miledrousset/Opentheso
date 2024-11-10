/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package notes;


import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import org.junit.jupiter.api.Test;

/**
 *
 * @author miledrousset
 */
public class MoveNotes {
    
    public MoveNotes() {
    }
   
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void moveNoteToDefinition() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();

        
        String idThesaurus = "24";
    
    }
}
