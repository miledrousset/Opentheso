package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OrphanHelper {
    
    private final Log log = LogFactory.getLog(ThesaurusHelper.class);
    
    public OrphanHelper() {
        
    }
    
    public ArrayList<String> getListOrphanId(HikariDataSource ds, String idThesaurus) {
        ArrayList<String> listIdConcept = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        
        
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_concept FROM concept_orphan WHERE"
                            + " id_thesaurus = '" + idThesaurus + "'";

                    resultSet = stmt.executeQuery(query);
                    while(resultSet.next()){
                        listIdConcept.add(resultSet.getString("id_concept"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        }
        catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of Orphan concept of thesaurus : " + idThesaurus, sqle);
        }
        
        return listIdConcept;
    }
    
    /**
     * Permet d'ajouter à concept à la liste des orphelins
     * @param ds
     * @param idConcept 
     * @param idThesaurus 
     * @return  true or false
     */
    public boolean addNewOrphan(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Statement stmt;
        boolean status = false;
        try {
            Connection conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into concept_orphan "
                            + "(id_concept, id_thesaurus)"
                            + " values ("
                            + "'" + idConcept + "'"
                            + ",'" + idThesaurus + "')";

                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Orphan concept : " + idConcept, sqle);
            } else
                status = true;
        }
        return status;
    }

    
    /**
     * 
     * @param conn 
     * @param idConcept 
     * @param idThesaurus 
     * @return  true or false
     */
    public boolean addNewOrphan(Connection conn,
            String idConcept, String idThesaurus) {

        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into concept_orphan "
                            + "(id_concept, id_thesaurus)"
                            + " values ("
                            + "'" + idConcept + "'"
                            + ",'" + idThesaurus + "')";

                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
           //     conn.close();
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Orphan concept : " + idConcept, sqle);
                return false;
            }
            return true;
            // Log exception

        }
        return status;
    }
    
    /**
     * Cette fonction permet de savoir si un orphelin existe ou non
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return 
     */
    public boolean isOrphanExist(HikariDataSource ds,
            String idConcept, String idThesaurus){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;
        
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id_concept FROM concept_orphan WHERE"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        if (resultSet.getRow() == 0) {
                            existe = false;
                        } else {
                            existe = true;
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        }
        catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of Orphan concept of thesaurus : " + idThesaurus, sqle);
        }
        
        return existe;        
    }    
    
    /**
     * 
     * @param conn
     * @param idConcept 
     * @param idThesaurus 
     * @return  true or false
     */
    public boolean deleteOrphan(Connection conn,
            String idConcept, String idThesaurus) {

        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_orphan "
                            + " where id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
      //          conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting orphan concept : " + idConcept, sqle);
        }
        return status;
    }    
    
    
    /**
     * Cette fonction permet de supprimer une branche du thésaurus (type Orphelins)
     * Il faut controler si un des concepts fait pas partie d'autres branches (plusieurs BT), 
     * alors, on ne supprime que la branche descendante. 
     * @param conn
     * @param idConcept 
     * @param idThesaurus 
     * @param idUser 
     * @return  true or false
     */
    public boolean deleteOrphanBranch2(Connection conn,
            String idConcept, String idThesaurus, int idUser) {

        Statement stmt;
        boolean status = false;
        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_orphan "
                            + " where id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
      //          conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting orphan concept : " + idConcept, sqle);
        }
        return status;
    }
    
    /**
     * Cette fonction permet de savoir si le Concept est un orphelin ou non
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return boolean
     */
    public boolean isOrphan(HikariDataSource ds,
            String idConcept, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false; 

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept from concept_orphan where "
                            + " id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    resultSet.next();
                    if (resultSet.getRow() == 0) {
                        existe = false;
                    } else {
                        existe = true;
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while Asking if Concept Is Orphan or not : " + idConcept, sqle);
        }
        return existe;
    }  
    
}
