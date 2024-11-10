package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Slf4j
@Service
public class HistoryHelper {

    @Autowired
    private DataSource dataSource;

    
    public ArrayList<HistoryValue> getLabelHistory(String idTerm, String idTheso){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<HistoryValue> historyValues = new ArrayList <>();

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lexical_value, lang, action, modified, username"
                            + " from term_historique, users where"
                            + " users.id_user = term_historique.id_user and"
                            + " id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idTheso + "'"
                            ;
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        HistoryValue historyValue = new HistoryValue();
                        historyValue.setValue(resultSet.getString("lexical_value"));
                        historyValue.setLang(resultSet.getString("lang"));
                        historyValue.setAction(resultSet.getString("action"));
                        historyValue.setDate(resultSet.getDate("modified"));
                        historyValue.setUser(resultSet.getString("username"));                        
                        historyValues.add(historyValue);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting history of Term : " + idTerm, sqle);
        }
        return historyValues;
    }
    
    public ArrayList<HistoryValue> getSynonymHistory(
            String idTerm, String idTheso){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<HistoryValue> historyValues = new ArrayList <>();

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lexical_value, lang, action, modified, username"
                            + " from non_preferred_term_historique, users where"
                            + " users.id_user = non_preferred_term_historique.id_user and"
                            + " id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idTheso + "'"
                            ;
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        HistoryValue historyValue = new HistoryValue();
                        historyValue.setValue(resultSet.getString("lexical_value"));
                        historyValue.setLang(resultSet.getString("lang"));
                        historyValue.setAction(resultSet.getString("action"));
                        historyValue.setDate(resultSet.getDate("modified"));
                        historyValue.setUser(resultSet.getString("username"));                        
                        historyValues.add(historyValue);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting history of NonPreferredTerm : " + idTerm, sqle);
        }
        return historyValues;
    }    
    
     public ArrayList<HistoryValue> getRelationsHistory(
            String idConcept, String idTheso){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<HistoryValue> historyValues = new ArrayList <>();

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_concept1, role, id_concept2, action, modified, username"
                            + " from hierarchical_relationship_historique, users where"
                            + " users.id_user = hierarchical_relationship_historique.id_user and"
                            + " id_concept1 = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idTheso + "'"
                            ;
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        HistoryValue historyValue = new HistoryValue();
                        historyValue.setId_concept1(resultSet.getString("id_concept1"));
                        historyValue.setRole(resultSet.getString("role"));
                        historyValue.setId_concept2(resultSet.getString("id_concept2"));                        
                        historyValue.setAction(resultSet.getString("action"));
                        historyValue.setDate(resultSet.getDate("modified"));
                        historyValue.setUser(resultSet.getString("username"));                        
                        historyValues.add(historyValue);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting history of Relations : " + idConcept, sqle);
        }
        return historyValues;
    }  
    
     public ArrayList<HistoryValue> getNotesHistory(
            String idConcept, String idTerm, String idTheso){
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<HistoryValue> historyValues = new ArrayList <>();

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lexicalvalue, notetypecode, lang, action_performed, modified, username"
                            + " from note_historique, users where"
                            + " users.id_user = note_historique.id_user and"
                            + " id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idTheso + "'";
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        HistoryValue historyValue = new HistoryValue();
                        historyValue.setValue(resultSet.getString("lexicalvalue"));
                        historyValue.setNoteType(resultSet.getString("notetypecode"));                        
                        historyValue.setLang(resultSet.getString("lang"));
                        historyValue.setAction(resultSet.getString("action_performed"));
                        historyValue.setDate(resultSet.getDate("modified"));
                        historyValue.setUser(resultSet.getString("username"));                        
                        historyValues.add(historyValue);
                    }
                    query = "select lexicalvalue, notetypecode, lang, action_performed, modified, username"
                            + " from note_historique, users where"
                            + " users.id_user = note_historique.id_user and"
                            + " id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idTheso + "'";
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        HistoryValue historyValue = new HistoryValue();
                        historyValue.setValue(resultSet.getString("lexicalvalue"));
                        historyValue.setNoteType(resultSet.getString("notetypecode"));                        
                        historyValue.setLang(resultSet.getString("lang"));
                        historyValue.setAction(resultSet.getString("action_performed"));
                        historyValue.setDate(resultSet.getDate("modified"));
                        historyValue.setUser(resultSet.getString("username"));                        
                        historyValues.add(historyValue);
                    }                    
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting history of notes : " + idConcept, sqle);
        }
        return historyValues;
    }
    
    /**
     * Classe pour les attribus du tableau historique
     */
    @Data
    public class HistoryValue {

        //// pour l'historique des relations
        private String id_concept1;
        private String role;
        private String id_concept2;
        
        //// pour les notes
        private String noteType;
        private String value;
        private String lang;
        private String action;
        private Date date;
        private String user;
    }
    
    
}
