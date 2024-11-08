/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package querysql;


import connexion.ConnexionTest;
import fr.cnrs.opentheso.models.concept.Concept;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/**
 *
 * @author miledrousset
 */
public class testConnectDeconnect {
    
    public testConnectDeconnect() {
    }
    

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testGet() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();    
        Concept concept;
        for (int i = 0; i < 1000000; i++) {
            concept = getThisConceptTest("293300", "th19");            
        }
    }
    private Concept getThisConceptTest(String idConcept, String idThesaurus) {
        Concept concept = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from concept where id_thesaurus = '" + idThesaurus + "'"
                        + " and id_concept = '" + idConcept + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        concept = new Concept();
                        concept.setIdConcept(idConcept);
                        concept.setIdThesaurus(idThesaurus);
                        concept.setIdArk(resultSet.getString("id_ark"));
                        concept.setIdHandle(resultSet.getString("id_handle"));
                        concept.setIdDoi(resultSet.getString("id_doi"));
                        concept.setCreated(resultSet.getDate("created"));
                        concept.setModified(resultSet.getDate("modified"));
                        concept.setStatus(resultSet.getString("status"));
                        concept.setNotation(resultSet.getString("notation"));
                        concept.setTopConcept(resultSet.getBoolean("top_concept"));
                        concept.setIdGroup("");//resultSet.getString("idgroup"));
                    }
                }
            }
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
        return concept;
    }
    
    @Test
    public void testIsert() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();    
        Concept concept = new Concept();
        
        concept.setIdConcept("266607");
        concept.setIdThesaurus("th19");
        concept.setIdArk("");
        concept.setIdHandle("");
        concept.setStatus("D");
        concept.setNotation("");
        concept.setTopConcept(true);
        concept.setIdDoi("");
        concept.setCreated(new java.util.Date());
        concept.setModified(new java.util.Date());
        
        for (int i = 0; i < 10000; i++) {
            insert(concept);
        }
        
    }   
    private void insert(Concept concept) {
        
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into concept "
                            + "(id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id_handle, id_doi)"
                            + " values ("
                            + "'" + concept.getIdConcept() + "'"
                            + ",'" + concept.getIdThesaurus() + "'"
                            + ",'" + concept.getIdArk() + "'"
                            + ",'" + concept.getCreated() + "'"
                            + ",'" + concept.getModified() + "'"
                            + ",'" + concept.getStatus() + "'"
                            + ",'" + concept.getNotation() + "'"
                            + "," + concept.isTopConcept()
                            + ",'" + concept.getIdHandle() + "'"
                            + ",'" + concept.getIdDoi() + "'"
                            + ")");
            }
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
    }    
    
    @Test
    public void testIsertWithAutocommitFalse() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();    
        Concept concept = new Concept();
        
        concept.setIdConcept("266607");
        concept.setIdThesaurus("th19");
        concept.setIdArk("");
        concept.setIdHandle("");
        concept.setStatus("D");
        concept.setNotation("");
        concept.setTopConcept(true);
        concept.setIdDoi("");
        concept.setCreated(new java.util.Date());
        concept.setModified(new java.util.Date());
        
        for (int i = 0; i < 10000; i++) {
            insertWithAutocommitFalse(concept);
        }
        
    }   
    private void insertWithAutocommitFalse(Concept concept) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            if(step1(concept)){
                conn.rollback();
//                conn.close();
            }
            if(step2(concept)) {
                conn.rollback();
//                conn.close();
            } else
                conn.commit();
//            conn.close();
        } catch (SQLException sqle) {
            System.err.println(sqle);
        }
    }       
    private boolean step1(Concept concept) {
        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into concept "
                        + "(id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id_handle, id_doi)"
                        + " values ("
                        + "'" + concept.getIdConcept() + "'"
                        + ",'" + concept.getIdThesaurus() + "'"
                        + ",'" + concept.getIdArk() + "'"
                        + ",'" + concept.getCreated() + "'"
                        + ",'" + concept.getModified() + "'"
                        + ",'" + concept.getStatus() + "'"
                        + ",'" + concept.getNotation() + "'"
                        + "," + concept.isTopConcept()
                        + ",'" + concept.getIdHandle() + "'"
                        + ",'" + concept.getIdDoi() + "'"
                        + ")");
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(testConnectDeconnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }  
    private boolean step2(Concept concept) {
        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into concept "
                        + "(id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id_handle, id_doi)"
                        + " values ("
                        + "'" + concept.getIdConcept() + "'"
                        + ",'" + concept.getIdThesaurus() + "'"
                        + ",'" + concept.getIdArk() + "'"
                        + ",'" + concept.getCreated() + "'"
                        + ",'" + concept.getModified() + "'"
                        + ",'" + concept.getStatus() + "'"
                        + ",'" + concept.getNotation() + "'"
                        + "," + concept.isTopConcept()
                        + ",'" + concept.getIdHandle() + "'"
                        + ",'" + concept.getIdDoi() + "'"
                        + ")");
            status = true;
        } catch (SQLException ex) {
            Logger.getLogger(testConnectDeconnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }    
   
}
