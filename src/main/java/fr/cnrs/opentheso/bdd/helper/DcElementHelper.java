/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author miledrousset
 */
public class DcElementHelper {
    
    
    public ArrayList<DcElement> getDcElementOfConcept(HikariDataSource ds, String idTheso, String idConcept){
        ArrayList<DcElement> dcElements = null;
        try(Connection conn = ds.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeQuery("select name, value, language from concept_dcterms "
                    + " where id_concept = '" + idConcept + "'"
                    + " and id_thesaurus = '" + idTheso + "'");
                try(ResultSet resultSet = stmt.getResultSet()){
                    dcElements = new ArrayList<>();
                    while (resultSet.next()) {
                        DcElement dcElement = new DcElement();
                        dcElement.setName(resultSet.getString("name"));
                        dcElement.setValue(resultSet.getString("value"));
                        dcElement.setLanguage(resultSet.getString("language"));
                        dcElements.add(dcElement);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DcElementHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dcElements;
    }
    
    public boolean addDcElementConcept(HikariDataSource ds, DcElement dcElement, String idConcept, String idTheso) {
        dcElement.setValue(new StringPlus().convertString(dcElement.getValue()));
        try(Connection conn = ds.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeUpdate("insert into concept_dcterms "
                    + " (id_concept, id_thesaurus, name, value, language) "
                    + " values (" 
                    + "'" + idConcept + "',"
                    + "'" + idTheso + "',"
                    + "'" + dcElement.getName() + "',"
                    + "'" + dcElement.getValue() + "',"
                    + (dcElement.getLanguage() != null ?  "'" + dcElement.getLanguage() + "'" : null)      
                    + ") ON CONFLICT DO NOTHING"
                );
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DcElementHelper.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return false;
    }
    
    public int addDcElementThesaurus(HikariDataSource ds, DcElement dcElement, String idTheso) {
        dcElement.setValue(new StringPlus().convertString(dcElement.getValue()));
        try(Connection conn = ds.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeQuery("insert into thesaurus_dcterms "
                    + " (id_thesaurus, name, value, language) "
                    + " values (" 
                    + "'" + idTheso + "',"
                    + "'" + dcElement.getName() + "',"
                    + "'" + dcElement.getValue() + "',"
                    + (dcElement.getLanguage() != null ?  "'" + dcElement.getLanguage() + "'" : null)      
                    + ") ON CONFLICT DO NOTHING RETURNING id"
                );
                try(ResultSet resultSet = stmt.getResultSet()){
                    if (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                }                
            }
        } catch (SQLException ex) {
            Logger.getLogger(DcElementHelper.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return -1;
    }    
    
    public boolean updateDcElementThesaurus(HikariDataSource ds, DcElement dcElement, String idTheso) {
        dcElement.setValue(new StringPlus().convertString(dcElement.getValue()));
        try(Connection conn = ds.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeUpdate("update thesaurus_dcterms "
                    + " set "
                    + " name = '" + dcElement.getName() + "'"
                    + ", value = '" + dcElement.getValue() + "'"
                    + ", language= " + (dcElement.getLanguage() != null ?  "'" + dcElement.getLanguage() + "'" : null)  
                    + " where " 
                    + " id= " + dcElement.getId()
                    + " and id_thesaurus = '" + idTheso + "'"
                );
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DcElementHelper.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return false;
    }
    
    public boolean deleteDcElementThesaurus(HikariDataSource ds, DcElement dcElement, String idTheso) {
        try(Connection conn = ds.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeUpdate("delete from thesaurus_dcterms "
                    + " where "
                    + " id_thesaurus = '" + idTheso + "'"
                    + " and id = " + dcElement.getId()
                );
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DcElementHelper.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return false;
    }    
    
    public List<DcElement> getDcElementOfThesaurus(HikariDataSource ds, String idTheso){
        List<DcElement> dcElements = null;
        try(Connection conn = ds.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeQuery("select id, name, value, language from thesaurus_dcterms "
                    + " where "
                    + " id_thesaurus = '" + idTheso + "'");
                try(ResultSet resultSet = stmt.getResultSet()){
                    dcElements = new ArrayList<>();
                    while (resultSet.next()) {
                        DcElement dcElement = new DcElement();
                        dcElement.setId(resultSet.getInt("id"));
                        dcElement.setName(resultSet.getString("name"));
                        dcElement.setValue(resultSet.getString("value"));
                        dcElement.setLanguage(resultSet.getString("language"));
                        dcElements.add(dcElement);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DcElementHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dcElements;
    }    
    
    
}
