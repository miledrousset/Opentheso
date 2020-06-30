package fr.cnrs.opentheso.bean.condidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CandidatDao extends BasicDao {

    public List<CandidatDto> searchAllCondidats(HikariDataSource hikariDataSource, String idThesaurus,
            String lang) throws SQLException {

        List<CandidatDto> temps = new ArrayList<>();

        openDataBase(hikariDataSource);

        stmt.executeQuery(new StringBuffer("SELECT nomPreTer.lang, nomPreTer.id_term, nomPreTer.lexical_value,")
                .append("con.id_concept, con.id_thesaurus, con.created, users.username, term.contributor ")
                .append("FROM non_preferred_term nomPreTer, preferred_term preTer, concept con, term, users ")
                .append("WHERE nomPreTer.id_term = preTer.id_term ")
                .append("AND con.id_concept = preTer.id_concept ")
                .append("AND term.id_term = nomPreTer.id_term ")
                .append("AND users.id_user = term.contributor ")
                .append("AND nomPreTer.lang = '").append(lang).append("' ")
                .append("AND con.id_thesaurus = '").append(idThesaurus).append("'")
                .append("ORDER BY nomPreTer.lexical_value ASC").toString());

        resultSet = stmt.getResultSet();

        while (resultSet.next()) {
            CandidatDto candidatDto = new CandidatDto();
            candidatDto.setIdTerm(resultSet.getString("id_term"));
            candidatDto.setNomPref(resultSet.getString("lexical_value"));
            candidatDto.setIdConcepte(resultSet.getString("id_concept"));
            candidatDto.setIdThesaurus(resultSet.getString("id_thesaurus"));
            candidatDto.setCreationDate(resultSet.getDate("created"));
            candidatDto.setUser(resultSet.getString("username"));
            candidatDto.setUserId(resultSet.getInt("contributor"));
            temps.add(candidatDto);
        }

        closeDataBase();
        return temps;
    }

    public String searchCondidatStatus(HikariDataSource hikariDataSource, String idCouncepte,
            String idThesaurus) throws SQLException {
        String status = null;
        openDataBase(hikariDataSource);
        stmt.executeQuery(new StringBuffer("SELECT sta.value FROM candidat_status can_sta, status sta ")
                .append("WHERE can_sta.id_status = sta.id_status AND can_sta.id_concept = ")
                .append(idCouncepte).append(" AND can_sta.id_thesaurus = '")
                .append(idThesaurus).append("'").toString());
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            status = resultSet.getString("value");
        }
        closeDataBase();
        return status;
    }

    public int searchParticipantCount(HikariDataSource hikariDataSource, String idCouncepte, String idThesaurus) throws SQLException {
        int nbrParticipant = 0;
        openDataBase(hikariDataSource);
        stmt.executeQuery(new StringBuffer("SELECT count(*) FROM candidat_messages WHERE id_concept = ")
                .append(idCouncepte).append(" AND id_thesaurus = '")
                .append(idThesaurus).append("'").toString());
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            nbrParticipant = resultSet.getInt("count");
        }
        closeDataBase();
        return nbrParticipant;
    }

    public int searchDemandeCount(HikariDataSource hikariDataSource, String idCouncepte, String idThesaurus) throws SQLException {
        int nbrDemande = 0;
        openDataBase(hikariDataSource);
        stmt.executeQuery(new StringBuffer("SELECT count(*) FROM proposition WHERE id_concept = '")
                .append(idCouncepte).append("' AND id_thesaurus = '")
                .append(idThesaurus).append("'").toString());
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            nbrDemande = resultSet.getInt("count");
        }

        closeDataBase();
        return nbrDemande;
    }

    public void setStatutForCandidat(HikariDataSource hikariDataSource, int status, String idConcepte,
            String idThesaurus, String idUser) throws SQLException {

        openDataBase(hikariDataSource);
        executInsertRequest(stmt,
                "INSERT INTO candidat_status(id_concept, id_status, date, id_user, id_thesaurus) "
                + "VALUES (" + idConcepte + ", " + status + ", now(), " + idUser + ", '" + idThesaurus + "')");
        closeDataBase();
    }
}
