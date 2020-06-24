package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DomaineDao {

    private final Log LOG = LogFactory.getLog(DomaineDao.class);

    public String getDomaineCandidat(Connect connect, Statement stmt, String idconcept, String idThesaurus) {
        String domaine = null;
        try {
            stmt.executeQuery("SELECT groupLabel.lexicalvalue "
                    + "FROM concept_group_label groupLabel, concept_group_concept con "
                    + "WHERE groupLabel.idgroup = con.idgroup "
                    + "AND con.idconcept = '" + idconcept + "' "
                    + "AND con.idthesaurus = '" + idThesaurus + "'");
            ResultSet resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                domaine = resultSet.getString("lexicalvalue");
            }
            resultSet.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return domaine;
    }
    
}
