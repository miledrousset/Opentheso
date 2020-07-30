package fr.cnrs.opentheso.bean.condidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.condidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DomaineDao extends BasicDao {
    
    public List<DomaineDto> getAllDomaines(Connect connect, String idThesaurus, String lang) {
        List<DomaineDto> domaines = new ArrayList<>();
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeQuery("SELECT idgroup, lexicalvalue FROM concept_group_label where idthesaurus = '"
                    + idThesaurus+"' AND lang = '"+lang+"'");
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                DomaineDto domaineDto = new DomaineDto();
                domaineDto.setId(resultSet.getString("idgroup"));
                domaineDto.setName(resultSet.getString("lexicalvalue"));
                domaines.add(domaineDto);
            }
            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return domaines;
    }

    public void addNewDomaine(Connect connect, String idgroup, String idthesaurus, String idconcept) throws SQLException {

        stmt = connect.getPoolConnexion().getConnection().createStatement();

        executInsertRequest(stmt,"INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES ('"
                +idgroup+"', '"+idthesaurus+"', '"+idconcept+"');");

        stmt.close();
    }

    public void updateDomaine(Connect connect, String oldIdgroup, String newIdgroup, String idthesaurus, String idconcept) throws SQLException {

        stmt = connect.getPoolConnexion().getConnection().createStatement();

        if (newIdgroup != null) {
            executInsertRequest(stmt,"INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES ('"
                    +newIdgroup+"', '"+idthesaurus+"', '"+idconcept+"')");
        } else {
            executDeleteRequest(stmt,"DELETE FROM concept_group_concept WHERE idconcept = '"+idconcept+"' AND idgroup = '"
                    +oldIdgroup+"' AND idthesaurus = '"+idthesaurus+"'");
        }

        stmt.close();
    }

    public String getDomaineCandidatByConceptAndThesaurusAndLang(HikariDataSource hikariDataSource, String idconcept,
                                                              String idThesaurus, String lang) {
        String domaine = null;
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT lab.lexicalvalue, lab.lang " +
                    "FROM concept_group_concept con, concept_group_label lab " +
                    "WHERE con.idgroup = lab.idgroup " +
                    "AND con.idthesaurus = '"+idThesaurus+"' " +
                    "AND idconcept = '"+idconcept+"' " +
                    "AND lab.lang = '"+lang+"'").toString());
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                domaine = resultSet.getString("lexicalvalue");
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return domaine;
    }
    
}
