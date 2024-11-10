/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package concept;


import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import org.junit.jupiter.api.Test;

/**
 *
 * @author miledrousset
 */
public class MoveConceptToTheso {

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {
        String idConceptToMove = "197419";//andiore / flute
        String idThesoFrom = "th201";
        String idThesoTarget = "th267";
        
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.moveConceptToAnotherTheso(idConceptToMove, idThesoFrom, idThesoTarget);
    }
}
