package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class CondidatDao extends BasicDao {
    

    public List<CandidatDto> searchAllCondidats(Statement stmt, String idThesaurus) throws SQLException {

        List<CandidatDto> temps = new ArrayList<>();

        String request = "SELECT nomPreTer.id_term, nomPreTer.lexical_value, con.id_concept, con.id_thesaurus, con.created "
                + "FROM non_preferred_term nomPreTer, preferred_term preTer, concept con "
                + "WHERE nomPreTer.id_term = preTer.id_term "
                + "AND con.id_concept = preTer.id_concept ";

        if (!StringUtils.isEmpty(idThesaurus)) {
            request +=  "AND con.id_thesaurus = '"+idThesaurus+"'";
        }

        stmt.executeQuery(request + "ORDER BY nomPreTer.lexical_value ASC");

        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            CandidatDto candidatDto = new CandidatDto();
            candidatDto.setIdTerm(resultSet.getString("id_term"));
            candidatDto.setNomPref(resultSet.getString("lexical_value"));
            candidatDto.setIdConcepte(resultSet.getString("id_concept"));
            candidatDto.setIdThesaurus(resultSet.getString("id_thesaurus"));
            candidatDto.setCreationDate(resultSet.getDate("created"));
            temps.add(candidatDto);
        }
        resultSet.close();
        return temps;
    }

    public String searchCondidatStatus(Statement stmt, String idCouncepte, String idThesaurus) throws SQLException {
        String status = null;
        stmt.executeQuery("SELECT sta.value "
                + "FROM candidat_status can_sta, status sta "
                + "WHERE can_sta.id_status = sta.id_status "
                + "AND can_sta.id_concept = " + idCouncepte
                + " AND can_sta.id_thesaurus = '" + idThesaurus + "'");
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            status = resultSet.getString("value");
        }
        resultSet.close();
        return status;
    }
    
    public int searchParticipantCount(Statement stmt, String idCouncepte, String idThesaurus) throws SQLException {
        int nbrParticipant = 0;
        stmt.executeQuery("SELECT count(*) FROM candidat_messages WHERE id_concept = "
                + idCouncepte + " AND id_thesaurus = '" + idThesaurus + "'");
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            nbrParticipant = resultSet.getInt("count");
        }
        resultSet.close();
        return nbrParticipant;
    }
    
    public int searchDemandeCount(Statement stmt, String idCouncepte, String idThesaurus) throws SQLException {
        int nbrDemande = 0;
        stmt.executeQuery("SELECT count(*) FROM proposition WHERE id_concept = '"+idCouncepte
                +"' AND id_thesaurus = '"+idThesaurus+"';");
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            nbrDemande = resultSet.getInt("count");
        }
        resultSet.close();
        return nbrDemande;
    }

    public void setStatutForCandidat(Connect connect, int status, String idConcepte,
            String idThesaurus, String idUser) throws SQLException {
        
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        executInsertRequest(stmt,
                "INSERT INTO candidat_status(id_concept, id_status, date, id_user, id_thesaurus) " +
                        "VALUES ("+idConcepte+", "+status+", now(), "+idUser+", '"+idThesaurus+"')");
        stmt.close();
    }
}
