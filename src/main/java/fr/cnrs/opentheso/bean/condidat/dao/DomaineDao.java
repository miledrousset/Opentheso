package fr.cnrs.opentheso.bean.condidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.condidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DomaineDao extends BasicDao {
    
    public List<DomaineDto> getAllDomaines(Connect connect, String idThesaurus) {
        List<DomaineDto> domaines = new ArrayList<>();
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();

            String request = "SELECT idgroup, lexicalvalue FROM concept_group_label";
            if (!StringUtils.isEmpty(idThesaurus))
                request += " where idthesaurus = '" + idThesaurus+"'";

            stmt.executeQuery(request);
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                DomaineDto domaineDto = new DomaineDto();
                domaineDto.setId(resultSet.getInt("idgroup"));
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

    public void addNewDomaine(Connect connect, int idgroup, String idthesaurus, String idconcept) throws SQLException {

        stmt = connect.getPoolConnexion().getConnection().createStatement();

        executInsertRequest(stmt,"INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES ('"
                +idgroup+"', '"+idthesaurus+"', '"+idconcept+"');");

        stmt.close();
    }

    public void updateDomaine(Connect connect, int oldIdgroup, int newIdgroup, String idthesaurus, String idconcept) throws SQLException {

        stmt = connect.getPoolConnexion().getConnection().createStatement();

        if (newIdgroup > 0) {
            executInsertRequest(stmt,"INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES ('"
                    +newIdgroup+"', '"+idthesaurus+"', '"+idconcept+"')");
        } else {
            executDeleteRequest(stmt,"DELETE FROM concept_group_concept WHERE idconcept = '"+idconcept+"' AND idgroup = '"
                    +oldIdgroup+"' AND idthesaurus = '"+idthesaurus+"'");
        }

        stmt.close();
    }

    public int getDomaineCandidat(HikariDataSource hikariDataSource, String idconcept, String idThesaurus) {
        int domaine = 0;
        try {
            openDataBase(hikariDataSource);
            stmt.executeQuery(new StringBuffer("SELECT idgroup FROM concept_group_concept WHERE idthesaurus = '")
                    .append(idThesaurus).append("' AND idconcept = '").append(idconcept).append("'").toString());
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                domaine = resultSet.getInt("idgroup");
            }
            closeDataBase();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return domaine;
    }
    
}
