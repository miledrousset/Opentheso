package fr.cnrs.opentheso.repositories.candidats;

import fr.cnrs.opentheso.repositories.UserHelper;
import fr.cnrs.opentheso.models.candidats.NodeCandidateOld;
import fr.cnrs.opentheso.models.candidats.NodeTraductionCandidat;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.utils.StringUtils;

import lombok.extern.slf4j.Slf4j;
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


@Slf4j
@Service
public class CandidatDao {

    @Autowired
    private DataSource dataSource;

    
    /**
     * permet de récupérer la liste des candidats qui sont en attente
     */
    public List<CandidatDto> getCandidatsByStatus(String idThesaurus, String lang, int etat) {

        List<CandidatDto> candidatDtos = new ArrayList<>();

        getAllIdOfCandidate(idThesaurus, etat, candidatDtos);

        for (CandidatDto candidatDto : candidatDtos) {
            candidatDto.setNomPref(getLexicalValueOfConcept(candidatDto.getIdConcepte(), idThesaurus, lang));
        }
        return candidatDtos;
    }

    private String getLexicalValueOfConcept(String idConcept, String idThesaurus, String idLang) {

        String lexicalValue = "";
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lexical_value from term, preferred_term where"
                        + " preferred_term.id_term = term.id_term AND"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and term.id_thesaurus = '" + idThesaurus + "'"
                        + " and preferred_term.id_concept = '" + idConcept + "'"
                        + " and term.lang = '" + idLang + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        lexicalValue = resultSet.getString("lexical_value");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting LexicalValue of Concept : " + idConcept, sqle);
        }
        return lexicalValue.trim();
    }

    
    private void getAllIdOfCandidate(String idTheso, int etat, List<CandidatDto> candidatDtos) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("SELECT concept.id_concept, concept.created,concept.modified," +
                        " candidat_status.id_user, candidat_status.id_user_admin, candidat_status.message" +
                        " FROM concept, candidat_status" +
                        " WHERE concept.id_concept = candidat_status.id_concept" +
                        " AND concept.id_thesaurus = candidat_status.id_thesaurus" +
                        " AND candidat_status.id_status = " + etat +
                        " AND concept.id_thesaurus = '" + idTheso + "'" +
                        " order by created DESC");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while(resultSet.next()) {
                        CandidatDto candidatDto = new CandidatDto();
                        candidatDto.setIdConcepte(resultSet.getString("id_concept"));
                        candidatDto.setCreationDate(resultSet.getDate("created"));
                        candidatDto.setInsertionDate(resultSet.getDate("modified"));                        
                        candidatDto.setStatut("" + etat);
                        candidatDto.setCreatedById(resultSet.getInt("id_user"));
                        candidatDto.setCreatedByIdAdmin(resultSet.getInt("id_user_admin"));
                        candidatDto.setIdThesaurus(idTheso);
                        candidatDto.setAdminMessage(resultSet.getString("message"));
                        candidatDtos.add(candidatDto);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting LexicalValue of Concept : " + sqle);
        }
        candidatDtos.forEach(candidatDto -> {
            candidatDto.setCreatedBy(getNameUser(candidatDto.getCreatedById()));
            candidatDto.setCreatedByAdmin(getNameUser(candidatDto.getCreatedByIdAdmin()));
        });
    }

    private String getNameUser(int userId) {
        String name = "";

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT username from users WHERE id_user =" + userId);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        name = resultSet.getString("username");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return name;
    }
    
    /**
     * permet de récupérer la liste des candidats qui sont en attente
     * @param value
     * @param idThesaurus
     * @param lang
     * @param etat
     * @param statut
     * @return
     * @throws SQLException 
     */
    public List<CandidatDto> searchCandidatsByValue(String value, String idThesaurus, String lang, int etat, String statut) {

        List<CandidatDto> temps = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery(createRequestSearchValue(lang, value, idThesaurus, etat, statut));

                try (ResultSet resultSet = stmt.getResultSet()) {
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
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return temps;
    }
    
    private String createRequestSearchValue(String lang, String value, String idThesaurus, int etat, String statut) {
        
        value = StringUtils.convertString(value);
        value = StringUtils.unaccentLowerString(value);
        
        StringBuffer request = new StringBuffer("SELECT DISTINCT term.lang, term.id_term,")
                .append(" term.lexical_Value, con.id_concept,")
                .append(" con.id_thesaurus, con.created,")
                .append(" users.username, term.contributor")

                .append(" FROM preferred_term preTer, concept con, term, users, candidat_status")
                .append(" WHERE con.id_concept = candidat_status.id_concept")
                .append(" AND con.id_thesaurus = candidat_status.id_thesaurus")
                .append(" AND con.id_concept = preTer.id_concept")
                .append(" AND term.id_term = preTer.id_term")
                .append(" AND con.id_thesaurus = preTer.id_thesaurus")
                .append(" AND preTer.id_thesaurus = term.id_thesaurus");

        if ("CA".equals(statut)) {
            request.append(" AND con.status = 'CA'");
        } else {
            request.append(" AND con.status <> 'CA'");
        }

        request.append(" AND users.id_user = term.contributor")
                .append(" AND candidat_status.id_status = ").append(etat)
                .append(" AND term.lang = '").append(lang).append("' ")
                .append(" AND con.id_thesaurus = '").append(idThesaurus).append("'")
                .append(" AND f_unaccent(lower(term.lexical_Value)) like '%").append(value).append("%'")
                .append(" ORDER BY term.lexical_Value ASC");

        return request.toString();


    }
    
    public ArrayList<NodeCandidateOld> getCandidatesIdFromOldModule (String idTheso) throws SQLException{
       
        ArrayList<NodeCandidateOld> nodeCandidateOlds = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select concept_candidat.id_concept, concept_candidat.status from concept_candidat where concept_candidat.id_thesaurus = '" + idTheso +"' and concept_candidat.status = 'a'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeCandidateOld nodeCandidateOld = new NodeCandidateOld();
                        nodeCandidateOld.setIdCandidate(resultSet.getString("id_concept"));
                        nodeCandidateOld.setStatus(resultSet.getString("status"));
                        nodeCandidateOlds.add(nodeCandidateOld);
                    }
                }
            }
        }

        return nodeCandidateOlds;
    }
    
    public ArrayList<NodeTraductionCandidat> getCandidatesTraductionsFromOldModule(String idOldCandidat, String idTheso) {
       
        ArrayList<NodeTraductionCandidat> nodeTraductionCandidats = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select term_candidat.lexicalValue, term_candidat.lang"
                        + " from concept_term_candidat, term_candidat"
                        + " where concept_term_candidat.id_term = term_candidat.id_term"
                        + " and concept_term_candidat.id_thesaurus = term_candidat.id_thesaurus"
                        + " and term_candidat.id_thesaurus = '" + idTheso + "'"
                        + " and concept_term_candidat.id_concept = '" + idOldCandidat + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeTraductionCandidat nodeTraductionCandidat  = new NodeTraductionCandidat();
                        nodeTraductionCandidat.setIdLang(resultSet.getString("lang"));
                        nodeTraductionCandidat.setTitle(resultSet.getString("lexical_value"));
                        nodeTraductionCandidats.add(nodeTraductionCandidat);
                    }
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        return nodeTraductionCandidats;
    }
}
