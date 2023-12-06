
package plpgsql;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class Export {
    
    public Export() {
    }
    

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void exportThesoV1() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        
        String idTheso = "";
        String baseUrl = "http://opentheso2.mom.fr";

        System.out.println("start at =  " + LocalDateTime.now());
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM opentheso_get_concepts('" + idTheso + "', '" + baseUrl + "')"
                + " as x(URI text, TYPE varchar, LOCAL_URI text, IDENTIFIER varchar, ARK_ID varchar, "
                + " prefLab varchar, altLab varchar, altLab_hiden varchar, definition text, example text, editorialNote text, changeNote text, "
                + " secopeNote text, note text, historyNote text, notation varchar, narrower text, broader text, related text, exactMatch text, "
                + " closeMatch text, broadMatch text, relatedMatch text, narrowMatch text, gpsData text, "
                + " membre text, created timestamp with time zone, modified timestamp with time zone, img text, creator text, contributor text, "
                + " replaces text, replaced_by text, facets text, externalResources text);");
                
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        System.out.println("resultat en " + new Date().getTime());
                    }   
                }
            }
        } catch (SQLException sqle) {
            System.out.println(">> " + sqle.getMessage());
        }
        System.out.println("fin at = " + LocalDateTime.now());
    }
    
    @Test
    public void exportThesoV2() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        
        String idTheso = "th134";
        String idConcept = "1027237";
       
        
/*        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts =  conceptHelper.getAllIdConceptOfThesaurus(ds, idTheso);
*/
  //      System.out.println("total = " + idConcepts.size());

  
        System.out.println("start at =  " + LocalDateTime.now());
        for (int i = 0; i < 1000; i++) {
            try (Connection conn = ds.getConnection()) {
                try (Statement stmt = conn.createStatement()) {

       //             for (String idConcept : idConcepts) {
                        stmt.executeQuery("SELECT * FROM opentheso_get_concept('" + idTheso + "', '" + idConcept + "')"
                            + " as x(URI text, TYPE varchar, LOCAL_URI text, IDENTIFIER varchar, ARK_ID varchar, "
                            + " prefLab varchar, altLab varchar, altLab_hiden varchar, definition text, example text, editorialNote text, changeNote text, "
                            + " secopeNote text, note text, historyNote text, notation varchar, narrower text, broader text, related text, exactMatch text, "
                            + " closeMatch text, broadMatch text, relatedMatch text, narrowMatch text, gpsData text, "
                            + " membre text, created timestamp with time zone, modified timestamp with time zone, img text, creator text, contributor text, "
                            + " replaces text, replaced_by text, facets text, externalResources text);");

                        try ( ResultSet resultSet = stmt.getResultSet()) {
                            while (resultSet.next()) {
                              //  System.out.println(resultSet.getString("identifier"));
                            }   
                        }  
                  
       //             }

                }
            } catch (SQLException sqle) {
                System.out.println(">> " + sqle.getMessage());
            }
        }
        System.out.println("fin at = " + LocalDateTime.now());           
        
        
        System.out.println("start NodeConcept at =  " + LocalDateTime.now());    
        for (int i = 0; i < 1000; i++) {
            new ConceptHelper().getConceptForExport(ds, idConcept, idTheso, false, false);                    
        }


        System.out.println("fin NodeConcept at = " + LocalDateTime.now());         
        
   //     System.out.println("fin at = " + LocalDateTime.now());
    }    
    
    @Test
    public void exportNoteConcept() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        
        String idTheso = "th133";
        
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts =  conceptHelper.getAllIdConceptOfThesaurus(ds, idTheso);
        String resultat;
        System.out.println("total = " + idConcepts.size());
        System.out.println("start at =  " + LocalDateTime.now());
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                
                for (String idConcept : idConcepts) {
                    stmt.executeQuery("SELECT * FROM opentheso_get_note_concept('" + idTheso + "', '" + idConcept + "')");

                    try ( ResultSet resultSet = stmt.getResultSet()) {
                        while (resultSet.next()) {
                            resultat = resultSet.getString("note_lexicalvalue");
                        }   
                    }                    
                }
            }
        } catch (SQLException sqle) {
            System.out.println(">> " + sqle.getMessage());
        }
        System.out.println("fin at = " + LocalDateTime.now());
    }     
    
    @Test
    public void exportTerms() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        
        String idTheso = "th133";
        
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts =  conceptHelper.getAllIdConceptOfThesaurus(ds, idTheso);
        String resultat;
        System.out.println("total = " + idConcepts.size());
        System.out.println("start at =  " + LocalDateTime.now());
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                
                for (String idConcept : idConcepts) {
                    stmt.executeQuery("SELECT * FROM opentheso_get_traductions('" + idTheso + "', '" + idConcept + "')");

                    try ( ResultSet resultSet = stmt.getResultSet()) {
                        while (resultSet.next()) {
                            resultat = resultSet.getString("term_lexical_value");
                        }   
                    }                    
                }
            }
        } catch (SQLException sqle) {
            System.out.println(">> " + sqle.getMessage());
        }
        System.out.println("fin at = " + LocalDateTime.now());
    }      

}

