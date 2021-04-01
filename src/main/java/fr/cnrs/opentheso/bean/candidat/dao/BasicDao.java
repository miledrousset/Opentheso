package fr.cnrs.opentheso.bean.candidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
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
    protected Connection connection;
    
    
    protected int executInsertRequest(Statement stmt, String sqlRequest) throws SQLException {
        return stmt.executeUpdate(sqlRequest);
    }


    public boolean executDeleteRequest(Statement stmt, String sqlRequest) throws SQLException {
             return stmt.execute(sqlRequest);
    }
    
    protected void openDataBase(HikariDataSource hikariDataSource) throws SQLException {
        connection = hikariDataSource.getConnection();
        stmt = connection.createStatement();
    }
    
    protected void closeDataBase() throws SQLException {
        if (resultSet != null && resultSet.isClosed()) resultSet.close();
        if (stmt != null && !stmt.isClosed()) stmt.close();
        if (connection != null && !connection.isClosed()) connection.close();
    }
    
}
