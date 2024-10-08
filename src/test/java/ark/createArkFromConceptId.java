/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ark;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import java.util.ArrayList;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class createArkFromConceptId {

    public createArkFromConceptId() {
    }
    
    
    
    @Test
    public void generateArkFromConceptId() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();   
        
        String idTheso = "th414";
        String naan = "66666";
        boolean overwrite = true;
        int count = 0; 
        
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> conceptIds = conceptHelper.getAllIdConceptOfThesaurus(ds, idTheso);
        
        for (String conceptId : conceptIds) {
            if(!overwrite) {
                if(!conceptHelper.isHaveIdArk(ds, idTheso, conceptId)) {
                    conceptHelper.updateArkIdOfConcept(ds, conceptId, idTheso, naan + "/" + conceptId);
                    count++;
                }
            } else {
                conceptHelper.updateArkIdOfConcept(ds, conceptId, idTheso, naan + "/" + conceptId);
                count++;
            }
        }
        System.out.println("Concepts changés = " + count);
    }
}
