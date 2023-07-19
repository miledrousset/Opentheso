/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author miledrousset
 */
public class DcElmentHelper {
    
    
    public ArrayList<DcElement> getDcElement(HikariDataSource ds, String idTheso, String idConcept){
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
            Logger.getLogger(DcElmentHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dcElements;
    }
    
    public boolean addDcElement(HikariDataSource ds, DcElement dcElement, String idConcept, String idTheso) {
        try(Connection conn = ds.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeUpdate("insert into concept_dcterms "
                    + " (id_concept, id_thesaurus, name, value, language) "
                    + " values (" 
                    + "'" + idConcept + "'"
                    + "'" + idTheso + "'"
                    + "'" + dcElement.getName() + "'"
                    + "'" + dcElement.getValue() + "'"
                    + (dcElement.getLanguage() != null ?  "'" + dcElement.getLanguage() + "'" : null)      
                    + ")"
                );
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DcElmentHelper.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return false;
    }
}
