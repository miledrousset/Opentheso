/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package notes;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdConceptIdTerm;
import java.util.ArrayList;
import org.junit.Test;

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

/*        NoteHelper noteHelper = new NoteHelper();
        ArrayList<NodeIdConceptIdTerm> nodeIdConceptIdTerms = noteHelper.getAllNotesByType(ds, idThesaurus, "note", true);
        nodeIdConceptIdTerms.forEach(nodeIdConceptIdTerm -> {
            noteHelper.moveConceptNoteToTermNote(ds, nodeIdConceptIdTerm.getIdConcept(), nodeIdConceptIdTerm.getIdTerm(),
                    idThesaurus, "definition", 1);
        });
  */      
    
    }
}
