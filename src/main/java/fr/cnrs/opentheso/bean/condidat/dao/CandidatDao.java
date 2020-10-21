package fr.cnrs.opentheso.bean.condidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeCandidateOld;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeProposition;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeTraductionCandidat;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.condidat.dto.VoteDto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CandidatDao extends BasicDao {

    public List<CandidatDto> searchAllCondidats(HikariDataSource hikariDataSource, String idThesaurus,
            String lang) throws SQLException {

        List<CandidatDto> temps = new ArrayList<>();

        openDataBase(hikariDataSource);
        
        /* erreur, il ne faut pas récupérer les termes de la table (non preferredTerm)
        stmt.executeQuery(new StringBuffer("SELECT DISTINCT nomPreTer.lang, nomPreTer.id_term, nomPreTer.lexical_value,")
                .append(" con.id_concept, con.id_thesaurus, con.created, users.username, term.contributor ")
                .append(" FROM non_preferred_term nomPreTer, preferred_term preTer, concept con, term, users ")
                .append(" WHERE nomPreTer.id_term = preTer.id_term ")
                .append(" AND con.id_concept = preTer.id_concept ")
                .append(" AND term.id_term = nomPreTer.id_term ")
                .append(" AND con.status = 'CA' ")
                .append(" AND users.id_user = term.contributor ")
                .append(" AND nomPreTer.lang = '").append(lang).append("' ")
                .append(" AND con.id_thesaurus = '").append(idThesaurus).append("'")
                .append(" ORDER BY nomPreTer.lexical_value ASC").toString()); */
        
        stmt.executeQuery(new StringBuffer("SELECT term.lang, term.id_term,")
                .append(" term.lexical_value, con.id_concept,")
                .append(" con.id_thesaurus, con.created,")
                .append(" users.username, term.contributor")              

                .append(" FROM preferred_term preTer, concept con, term, users")
                .append(" WHERE") 
                .append(" con.id_concept = preTer.id_concept") 
                .append(" AND term.id_term = preTer.id_term") 
                .append(" AND con.id_thesaurus = preTer.id_thesaurus")                 
                .append(" AND preTer.id_thesaurus = term.id_thesaurus")  
                .append(" AND con.status = 'CA'")  
                .append(" AND users.id_user = term.contributor")  
                  
                .append(" AND term.lang = '").append(lang).append("' ")
                .append(" AND con.id_thesaurus = '").append(idThesaurus).append("'")
                .append(" ORDER BY term.lexical_value ASC").toString());        

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
    
    /**
     * permet de récupérer la liste des candidats qui sont en attente
     * @param hikariDataSource
     * @param idThesaurus
     * @param lang
     * @param etat
     * @param statut
     * @return
     * @throws SQLException 
     */
    public List<CandidatDto> searchCandidatsByStatus(HikariDataSource hikariDataSource, String idThesaurus,
            String lang, int etat, String statut) throws SQLException {

        List<CandidatDto> temps = new ArrayList<>();

        openDataBase(hikariDataSource);
        
        stmt.executeQuery(createRequest(lang, idThesaurus, etat, statut));

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
    
   /**
     * permet de récupérer la liste des candidats qui sont en attente
     * @param hikariDataSource
     * @param value
     * @param idThesaurus
     * @param lang
     * @param etat
     * @param statut
     * @return
     * @throws SQLException 
     */
    public List<CandidatDto> searchCandidatsByValue(HikariDataSource hikariDataSource,
            String value, String idThesaurus,
            String lang, int etat, String statut) throws SQLException {

        List<CandidatDto> temps = new ArrayList<>();

        openDataBase(hikariDataSource);
        
        stmt.executeQuery(createRequestSearchValue(lang, value, idThesaurus, etat, statut));

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

    private String createRequest(String lang, String idThesaurus, int etat, String statut) {

        StringBuffer request = new StringBuffer("SELECT DISTINCT term.lang, term.id_term,")
                .append(" term.lexical_value, con.id_concept,")
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
                .append(" ORDER BY term.lexical_value ASC");

        return request.toString();

    }    
    
    private String createRequestSearchValue(String lang, String value, String idThesaurus, int etat, String statut) {
        StringPlus stringPlus = new StringPlus();
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);
        
        StringBuffer request = new StringBuffer("SELECT DISTINCT term.lang, term.id_term,")
                .append(" term.lexical_value, con.id_concept,")
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
                .append(" AND f_unaccent(lower(term.lexical_value)) like '%").append(value).append("%'")
                .append(" ORDER BY term.lexical_value ASC");

        return request.toString();

    }

    public String searchCondidatStatus(HikariDataSource hikariDataSource, String idCouncepte,
            String idThesaurus) throws SQLException {
        String status = null;
        openDataBase(hikariDataSource);
        stmt.executeQuery(new StringBuffer("SELECT sta.value FROM candidat_status can_sta, status sta ")
                .append("WHERE can_sta.id_status = sta.id_status AND can_sta.id_concept = '")
                .append(idCouncepte).append("' AND can_sta.id_thesaurus = '")
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
        stmt.executeQuery(new StringBuffer("SELECT count(*) FROM candidat_messages WHERE id_concept = '")
                .append(idCouncepte).append("' AND id_thesaurus = '")
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
                                     String idThesaurus, String idUser, String date) {
        try{
            openDataBase(hikariDataSource);
            executInsertRequest(stmt,
                    "INSERT INTO candidat_status(id_concept, id_status, date, id_user, id_thesaurus) "
                            + "VALUES ('" + idConcepte + "', " + status + ", '"+date+"', " + idUser + ", '" + idThesaurus + "')");
            closeDataBase();
        } catch (Exception e) {

        }
    }

    public void setStatutForCandidat(HikariDataSource hikariDataSource, int status, String idConcepte,
            String idThesaurus, String idUser) {

        setStatutForCandidat(hikariDataSource, status, idConcepte, idThesaurus, idUser, sdf.format(new Date()));
    }

    public int getMaxCandidatId(HikariDataSource hikariDataSource) throws SQLException {
        int nbrDemande = 0;
        openDataBase(hikariDataSource);
        stmt.executeQuery("SELECT COALESCE(max(id_concept), '0') max_id from concept where status = 'CA'");
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            nbrDemande = resultSet.getInt("max_id");
        }
        closeDataBase();
        return nbrDemande;
    }
    
/////// ajouté par Miled
    
    public int searchVoteCount(HikariDataSource hikariDataSource, String idCouncepte, String idThesaurus,
            String typeVote) throws SQLException {
        int nbrDemande = 0;
        openDataBase(hikariDataSource);
        stmt.executeQuery(new StringBuffer("SELECT count(*) FROM candidat_vote WHERE id_concept = '")
                .append(idCouncepte).append("' AND id_thesaurus = '").append(idThesaurus)
                .append("' AND type_vote = '").append(typeVote).append("'").toString());
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            nbrDemande = resultSet.getInt("count");
        }

        closeDataBase();
        return nbrDemande;
    }    
    
    public void addVote(HikariDataSource hikariDataSource, String idThesaurus, String idConcept,
            int idUser, String idNote, String typeVote) {
        try {
            openDataBase(hikariDataSource);
            executInsertRequest(stmt,
                    "INSERT INTO candidat_vote(id_user, id_concept, id_thesaurus, id_note, type_vote) "
                            + "VALUES (" + idUser + ",'" + idConcept + "','" + idThesaurus + "', '"+idNote+"', '" + typeVote + "')");
            closeDataBase();
        } catch (Exception e) {

        }
    }
    
    public boolean removeVote(HikariDataSource hikariDataSource, String idThesaurus, String idConcept, 
            int idUser, String idNote, String typeVote) throws SQLException {
        openDataBase(hikariDataSource);
        String requet = "delete from candidat_vote where id_user = " + idUser 
                + " and id_concept = '" + idConcept + "'"
                + " and id_thesaurus = '" + idThesaurus + "'"
                + " and type_vote = '" + typeVote + "'";
        
        if (idNote != null) {
            requet += " and id_note = '" + idNote + "'";
        }
        
        executInsertRequest(stmt, requet);
        closeDataBase();
        return true;
    }    
    
    public boolean getVote(HikariDataSource hikariDataSource, int userId, String idConcept,
                    String idTheso, String idNote, String typeVote) throws SQLException {
        boolean voted = false;
        openDataBase(hikariDataSource);
        String requet = "select id_concept from candidat_vote where" +
                    " id_user = " + userId +
                    " and id_concept = '" + idConcept + "'" +
                    " and id_thesaurus = '" + idTheso + "'"+
                    " and type_vote = '" + typeVote + "' ";
        if (idNote != null){
            requet += " and id_note = '" + idNote + "' ";
        }
        stmt.executeQuery(requet);
        
        resultSet = stmt.getResultSet();
        if(resultSet.next()) {
            voted = resultSet.getRow() != 0;
        }
        closeDataBase();
        return voted;
    }



    public List<VoteDto> getAllVotesByCandidat(HikariDataSource hikariDataSource, String idConcept, String idTheso) {
        List<VoteDto> votes = new ArrayList<>();
        try {
            openDataBase(hikariDataSource);
            String requet = "SELECT id_user, id_concept, id_thesaurus, type_vote, id_note FROM candidat_vote " +
                    " WHERE id_concept = '"+idConcept+"' AND id_thesaurus = '"+idTheso+"'";
            stmt.executeQuery(requet);
            resultSet = stmt.getResultSet();
            while(resultSet.next()) {
                VoteDto voteDto = new VoteDto();
                voteDto.setIdUser(resultSet.getInt("id_user"));
                voteDto.setIdConcept(resultSet.getString("id_concept"));
                voteDto.setIdThesaurus(resultSet.getString("id_thesaurus"));
                voteDto.setTypeVote(resultSet.getString("type_vote"));
                voteDto.setIdNote(resultSet.getString("id_note"));
                votes.add(voteDto);
            }
            closeDataBase();
        } catch (Exception e) {

        }

        return votes;
    }

    public boolean insertCandidate(HikariDataSource hikariDataSource,
            CandidatDto candidatDto, String adminMessage, int idUser) {
        try {
            openDataBase(hikariDataSource);
            connection.setAutoCommit(false);

            if(!updateCandidateStatus(candidatDto, adminMessage, 2, idUser)){ // 2 = insérée
                connection.rollback();
                closeDataBase();
                return false;
            }

            if(!changeCandidateToConcept(candidatDto)){
                connection.rollback();
                closeDataBase();
                return false;
            }
            if(candidatDto.getTermesGenerique().isEmpty()) {
                if(!setTopTermToConcept(candidatDto)){
                    connection.rollback();
                    closeDataBase();
                    return false;
                }
            }
            connection.commit();
            closeDataBase();
            return true;            
        } catch (SQLException e) {
            try {
                connection.rollback();
                closeDataBase();
            } catch (SQLException ex) {
            }
        }
        return false;
    }     
    
    public boolean rejectCandidate(HikariDataSource hikariDataSource,
            CandidatDto candidatDto, String adminMessage, int idUser) {

        try {
            openDataBase(hikariDataSource);
            connection.setAutoCommit(false);

            if(!updateCandidateStatus(candidatDto, adminMessage, 3, idUser)){ // 3 = rejeté
                connection.rollback();
                closeDataBase();
                return false;
            }
            connection.commit();
            closeDataBase();
            return true;            
        } catch (SQLException e) {
            try {
                connection.rollback();
                closeDataBase();
            } catch (SQLException ex) {
            }
        }
        return false;
    }      
    
////////// import des candidats 
    
    public ArrayList<NodeCandidateOld> getCandidatesIdFromOldModule (
            HikariDataSource hikariDataSource, String idTheso) throws SQLException{
       
        ArrayList<NodeCandidateOld> nodeCandidateOlds = new ArrayList<>();
        
        openDataBase(hikariDataSource);
        stmt.executeQuery("select concept_candidat.id_concept, concept_candidat.status"
                            + " from concept_candidat"
                            + " where"
                            + " concept_candidat.id_thesaurus = '" + idTheso +"'"
                            + " and concept_candidat.status = 'a'");
        
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            NodeCandidateOld nodeCandidateOld = new NodeCandidateOld();
            nodeCandidateOld.setIdCandidate(resultSet.getString("id_concept"));
            nodeCandidateOld.setStatus(resultSet.getString("status"));
            nodeCandidateOlds.add(nodeCandidateOld);
        }
        closeDataBase();
        return nodeCandidateOlds;
    }
    
    public ArrayList<NodeTraductionCandidat> getCandidatesTraductionsFromOldModule(
            HikariDataSource hikariDataSource, String idOldCandidat, String idTheso) throws SQLException{
       
        ArrayList<NodeTraductionCandidat> nodeTraductionCandidats = new ArrayList<>();
         
        openDataBase(hikariDataSource);
        stmt.executeQuery("select term_candidat.lexical_value, term_candidat.lang"
                                + " from concept_term_candidat, term_candidat"
                                + " where"
                                + " concept_term_candidat.id_term = term_candidat.id_term"
                                + " and"
                                + " concept_term_candidat.id_thesaurus = term_candidat.id_thesaurus"
                                + " and"
                                + " term_candidat.id_thesaurus = '" + idTheso + "'"
                                + " and"
                                + " concept_term_candidat.id_concept = '" + idOldCandidat + "'");
        
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            NodeTraductionCandidat nodeTraductionCandidat  = new NodeTraductionCandidat();
            nodeTraductionCandidat.setIdLang(resultSet.getString("lang"));
            nodeTraductionCandidat.setTitle(resultSet.getString("lexical_value"));
            nodeTraductionCandidats.add(nodeTraductionCandidat);
        }
        closeDataBase();        
        return nodeTraductionCandidats;
    }
    
    public ArrayList<NodeProposition> getCandidatesMessagesFromOldModule(
            HikariDataSource hikariDataSource, String idOldCandidat, String idTheso) throws SQLException{
       
        ArrayList<NodeProposition> nodePropositions = new ArrayList<>();
         
        openDataBase(hikariDataSource);
        stmt.executeQuery("select proposition.note, proposition.id_user "
                + " from proposition where"
                + " proposition.id_concept = '" + idOldCandidat + "'"
                + " and proposition.id_thesaurus = '" + idTheso + "'"
                + " ORDER BY created ASC");
        
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            NodeProposition nodeProposition  = new NodeProposition();
            nodeProposition.setNote(resultSet.getString("note"));
            nodeProposition.setId_user(resultSet.getInt("id_user"));
            nodePropositions.add(nodeProposition);
        }
        closeDataBase();        
        return nodePropositions;
    }     
////////// fin import des candidats 
    
    
    
    private boolean updateCandidateStatus(CandidatDto candidatDto,
            String adminMessage, int status, int idUser) throws SQLException{
        StringPlus stringPlus = new StringPlus();
        adminMessage = stringPlus.convertString(adminMessage);
        
        executInsertRequest(stmt,
                "update candidat_status set id_status = " + status +
                ", message = '" + adminMessage + "', id_user_admin = " + idUser +
                " where id_concept = '" + candidatDto.getIdConcepte() + "'" +
                " and id_thesaurus = '" + candidatDto.getIdThesaurus() + "'");
        return true;
    }
    
    private boolean changeCandidateToConcept(CandidatDto candidatDto) throws SQLException{
        executInsertRequest(stmt,
                "update concept set status = 'D'" +
                " where id_concept = '" + candidatDto.getIdConcepte() + "'" +
                " and id_thesaurus = '" + candidatDto.getIdThesaurus() + "'");
        return true;
    }
    
    private boolean setTopTermToConcept(CandidatDto candidatDto) throws SQLException{        
        executInsertRequest(stmt,
                "update concept set top_concept = true" +
                " where id_concept = '" + candidatDto.getIdConcepte() + "'" +
                " and id_thesaurus = '" + candidatDto.getIdThesaurus() + "'");
        return true;
    }
            
}
