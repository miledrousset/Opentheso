package fr.cnrs.opentheso.bean.condidat;

import com.zaxxer.hikari.HikariDataSource;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CandidatDao implements Serializable {
    
    private final Log log = LogFactory.getLog(CandidatDao.class);
    
    
    public void getAllCandidats(HikariDataSource connexion) {

        Statement stmt;
        ResultSet resultSet;

        try {
            try {
                stmt = connexion.getConnection().createStatement();
                try {
                    stmt.executeQuery("SELECT nomPreTer.id_term, nomPreTer.lexical_value "
                            + "FROM non_preferred_term nomPreTer, preferred_term preTer "
                            + "WHERE nomPreTer.id_term = preTer.id_term "
                            + "ORDER BY nomPreTer.lexical_value ASC");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        System.out.println("id_term >> " + resultSet.getString("id_term"));
                        System.out.println("lexical_value >> " + resultSet.getString("lexical_value"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                connexion.getConnection().close();
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " );
        }

    }
    
}