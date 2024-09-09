
package plpgsql;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DaoResourceHelper;
import fr.cnrs.opentheso.models.concept.NodeConceptGraph;
import fr.cnrs.opentheso.models.concept.NodeFullConcept;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.concept.NodeConceptTree;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 *
 * @author miledrousset
 */
public class Export {
    
    public Export() {
    }
    
    @Test
    public void getChildren() {
        String idTheso = "th42";
        String idConcept = "38559"; 
        String idLang = "fr";
        
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();

        DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
        System.out.println("Commence ");
        List<NodeConceptGraph> listChilds = daoResourceHelper.getConceptsNTForGraph(ds, idTheso, idConcept, idLang);
        
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
            new ConceptHelper().getConceptForExport(ds, idConcept, idTheso, false);
        }


        System.out.println("fin NodeConcept at = " + LocalDateTime.now());         
        
   //     System.out.println("fin at = " + LocalDateTime.now());
    }    
    
    @Test
    public void getFullConcept(){
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        
        String idTheso = "th42";
        String idConcept = "38559";
        String idLang = "fr";
        
        DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
        NodeFullConcept nodeFullConcept = daoResourceHelper.getFullConcept(ds, idTheso, idConcept, idLang, -1, -1);
    }
    
    @Test
    public void getListConceptFils(){
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        
        String idTheso = "th42";
        String idConcept = "38559";
        String idLang = "fr";
        
        DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
        System.out.println("start");
        List<NodeConceptTree> nodeConceptTrees = daoResourceHelper.getConceptsNTForTree(ds, idTheso, idConcept, idLang, false);
        System.out.println("stop");
    }    
    
    
    @Test
    public void getOneConcept() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        
        String idTheso = "th42";
        String idConcept = "38553";
        String idLang = "fr";

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from opentheso_get_concept('" + idTheso + "', '" + idConcept + "', '" + idLang + "')" +
                    "as x(URI text, conceptType varchar, localUri text, identifier varchar, permalinkId varchar," +
                    "prefLabel varchar, altLabel varchar, hidenlabel varchar," + 
                    "prefLabel_trad varchar, altLabel_trad varchar, hiddenLabel_trad varchar, definition text, example text, editorialNote text, changeNote text," +
                    "scopeNote text, note text, historyNote text, notation varchar, narrower text, broader text, related text, exactMatch text, " +
                    "closeMatch text, broadMatch text, relatedMatch text, narrowMatch text, gpsData text," +
                    " membre text, created timestamp with time zone, modified timestamp with time zone, images text, creator text, contributor text," +
                    "replaces text, replaced_by text, facets text, externalResources text);"
                );

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        System.out.println("##############################################");
                        System.out.println("##############################################");                        
                        System.out.println(resultSet.getString("URI"));
                        System.out.println(resultSet.getString("conceptType"));
                        System.out.println(resultSet.getString("localUri"));
                        System.out.println(resultSet.getString("identifier"));
                        System.out.println(resultSet.getString("permalinkId"));
                        
                        System.out.println(resultSet.getString("prefLabel"));
                        System.out.println(resultSet.getString("altLabel"));
                        System.out.println(resultSet.getString("hiddenLabel"));
                        
                        System.out.println(resultSet.getString("definition"));
                        System.out.println(resultSet.getString("example"));
                        System.out.println(resultSet.getString("editorialNote"));
                        System.out.println(resultSet.getString("changeNote"));
                        System.out.println(resultSet.getString("secopeNote"));
                        System.out.println(resultSet.getString("note"));
                        System.out.println(resultSet.getString("historyNote"));
                        
                        
                        System.out.println(resultSet.getString("notation"));
                        System.out.println(resultSet.getString("narrower"));
                        System.out.println(resultSet.getString("broader"));
                        System.out.println(resultSet.getString("related"));
                        
                        
                        System.out.println(resultSet.getString("exactMatch"));
                        System.out.println(resultSet.getString("closeMatch"));
                        System.out.println(resultSet.getString("broadMatch"));
                        System.out.println(resultSet.getString("relatedMatch"));
                        System.out.println(resultSet.getString("narrowMatch"));
                        
                        System.out.println(resultSet.getString("gpsData"));
                        
                        System.out.println(resultSet.getString("membre"));
                        System.out.println(resultSet.getString("created"));
                        System.out.println(resultSet.getString("modified"));
                        
                        System.out.println(resultSet.getString("images"));
                        System.out.println(resultSet.getString("creator"));
                        System.out.println(resultSet.getString("contributor"));
                        
                        System.out.println(resultSet.getString("replaces"));
                        System.out.println(resultSet.getString("replaced_by"));
                        System.out.println(resultSet.getString("facets"));
                        System.out.println(resultSet.getString("externalResources"));
                        System.out.println("##############################################");
                        System.out.println("##############################################");                         
                    }   
                }  
            }
        } catch (SQLException sqle) {
            System.out.println(">> " + sqle.getMessage());
        }
    }     
    
    @Test
    public void addImages() {
        String image = "nom@@copyright@@https://m.media-amazon.com/images/I/71x9PLXMWOL._AC_UF1000,1000_QL80_.jpg@@";
                String[] imageDetail = image.split("@@");
           //     if(imageDetail.length != 4) return;
                
                NodeImage nodeImage = new NodeImage();
                nodeImage.setImageName(imageDetail[0]);
                nodeImage.setCopyRight(imageDetail[1]);
                nodeImage.setUri(imageDetail[2]);
                if(imageDetail.length >= 4)
                    nodeImage.setCreator(imageDetail[3]);
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
                            resultat = resultSet.getString("term_lexicalValue");
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

