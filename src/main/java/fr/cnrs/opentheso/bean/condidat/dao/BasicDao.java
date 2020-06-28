package fr.cnrs.opentheso.bean.condidat.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class BasicDao {

    protected final Log LOG = LogFactory.getLog(BasicDao.class);

    protected final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    protected ResultSet resultSet;
    protected Statement stmt;
    
    
    protected int executInsertRequest(Statement stmt, String sqlRequest) throws SQLException {
        return stmt.executeUpdate(sqlRequest);
    }


    public boolean executDeleteRequest(Statement stmt, String sqlRequest) throws SQLException {
             return stmt.execute(sqlRequest);
    }
    
    
}
