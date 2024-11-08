package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.models.nodes.DcElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class DcElementHelper {

    @Autowired
    private DataSource dataSource;


    public ArrayList<DcElement> getDcElementOfConcept(String idTheso, String idConcept){
        ArrayList<DcElement> dcElements = null;
        try(Connection conn = dataSource.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeQuery("select name, value, language, data_type from concept_dcterms "
                    + " where id_concept = '" + idConcept + "'"
                    + " and id_thesaurus = '" + idTheso + "'");
                try(ResultSet resultSet = stmt.getResultSet()){
                    dcElements = new ArrayList<>();
                    while (resultSet.next()) {
                        DcElement dcElement = new DcElement();
                        dcElement.setName(resultSet.getString("name"));
                        dcElement.setValue(resultSet.getString("value"));
                        dcElement.setLanguage(resultSet.getString("language"));
                        dcElement.setType(resultSet.getString("data_type"));
                        dcElements.add(dcElement);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DcElementHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dcElements;
    }
    
    public boolean addDcElementConcept(DcElement dcElement, String idConcept, String idTheso) {
        dcElement.setValue(fr.cnrs.opentheso.utils.StringUtils.convertString(dcElement.getValue()));
        try(Connection conn = dataSource.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeUpdate("insert into concept_dcterms "
                    + " (id_concept, id_thesaurus, name, value, language, data_type) "
                    + " values (" 
                    + "'" + idConcept + "',"
                    + "'" + idTheso + "',"
                    + "'" + dcElement.getName() + "',"
                    + "'" + dcElement.getValue() + "',"
                    + (dcElement.getLanguage() != null ?  "'" + dcElement.getLanguage() + "'" : null)  + "," 
                    + (dcElement.getType()!= null ?  "'" + dcElement.getType() + "'" : null)  
                    + ") ON CONFLICT DO NOTHING"
                );
                return true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DcElementHelper.class.getName()).log(Level.SEVERE, null, ex);
        }        
        return false;
    }
    
    public int addDcElementThesaurus(DcElement dcElement, String idTheso) {
        dcElement.setValue(fr.cnrs.opentheso.utils.StringUtils.convertString(dcElement.getValue()));
        try(Connection conn = dataSource.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeQuery("insert into thesaurus_dcterms "
                    + " (id_thesaurus, name, value, language, data_type) "
                    + " values (" 
                    + "'" + idTheso + "',"
                    + "'" + dcElement.getName() + "',"
                    + "'" + dcElement.getValue() + "',"
                    + (dcElement.getLanguage() != null ?  "'" + dcElement.getLanguage() + "'" : null) + ","
                    + (dcElement.getType()!= null ?  "'" + dcElement.getType()+ "'" : null)                            
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
    
    public boolean updateDcElementThesaurus(DcElement dcElement, String idTheso) {
        dcElement.setValue(fr.cnrs.opentheso.utils.StringUtils.convertString(dcElement.getValue()));
        try(Connection conn = dataSource.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeUpdate("update thesaurus_dcterms "
                    + " set "
                    + " name = '" + dcElement.getName() + "'"
                    + ", value = '" + dcElement.getValue() + "'"
                    + ", language= " + (dcElement.getLanguage() != null ?  "'" + dcElement.getLanguage() + "'" : null) 
                    + ", data_type= " + (dcElement.getType()!= null ?  "'" + dcElement.getType()+ "'" : null) 
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
    
    public boolean deleteDcElementThesaurus(DcElement dcElement, String idTheso) {
        try(Connection conn = dataSource.getConnection()){
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
    
    public List<DcElement> getDcElementOfThesaurus(String idTheso){
        List<DcElement> dcElements = null;
        try(Connection conn = dataSource.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeQuery("select id, name, value, language, data_type from thesaurus_dcterms "
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
                        dcElement.setType(resultSet.getString("data_type"));
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
