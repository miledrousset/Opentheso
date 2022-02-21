/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package contributor;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import java.util.ArrayList;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class CorrectRolesOnConceptTest {
    
    public CorrectRolesOnConceptTest() {
    }
    

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Ignore
    @Test
    public void switchRolesFromTermToConcept() {
        
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();   
        
        String idTheso = "th402";
        String lang = "fr";
        
        ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();
        
        String idTerm;
        int idCreator;
        int idContributor;        
        ArrayList<String> allConcepts = conceptHelper.getAllIdConceptOfThesaurus(ds, idTheso);
        
        for (String idConcept : allConcepts) {
            if(!conceptHelper.isHaveCreator(ds, idTheso, idConcept)) {
                idTerm = termHelper.getIdTermOfConcept(ds, idConcept, idTheso);
                if(idTerm != null) {
                    idCreator = termHelper.getCreator(ds, idTheso, idTerm, lang);
                    if(idCreator != -1)
                        conceptHelper.setCreator(ds, idTheso, idConcept, idCreator);
                }
            }
            if(!conceptHelper.isHaveContributor(ds, idTheso, idConcept)) {
                idTerm = termHelper.getIdTermOfConcept(ds, idConcept, idTheso);
                if(idTerm != null) {
                    idContributor = termHelper.getContributor(ds, idTheso, idTerm, lang);
                    if(idContributor != -1)
                        conceptHelper.setContributor(ds, idTheso, idConcept, idContributor);
                }
            }
        }
        
    }
}
