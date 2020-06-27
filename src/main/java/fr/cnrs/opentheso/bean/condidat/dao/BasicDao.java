package fr.cnrs.opentheso.bean.condidat.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class BasicDao {

    protected final Log LOG = LogFactory.getLog(BasicDao.class);
    
    protected ResultSet resultSet;
    protected Statement stmt;
    
    
    protected int executInsertRequest(Statement stmt, String sqlRequest) throws SQLException {
        return stmt.executeUpdate(sqlRequest);
    }


    public boolean executDelete(Statement stmt, String sqlRequest) throws SQLException {
             return stmt.execute(sqlRequest);
    }
    
    
}
