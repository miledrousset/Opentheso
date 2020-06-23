package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.condidat.dto.TraductionDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Named(value = "candidatService")
@SessionScoped
public class CandidatService implements Serializable {

    private final Log LOG = LogFactory.getLog(CandidatService.class);
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Statement stmt;
    private ResultSet resultSet;

    public List<CandidatDto> getAllCandidats(Connect connect) {

        List<CandidatDto> temps = new ArrayList<>();

        try {
            try {
                stmt = connect.getPoolConnexion().getConnection().createStatement();
                try {
                    stmt.executeQuery("SELECT nomPreTer.id_term, nomPreTer.lexical_value, con.id_concept, con.id_thesaurus, con.created "
                            + "FROM non_preferred_term nomPreTer, preferred_term preTer, concept con "
                            + "WHERE nomPreTer.id_term = preTer.id_term "
                            + "AND con.id_concept = preTer.id_concept "
                            + "ORDER BY nomPreTer.lexical_value ASC");
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        CandidatDto candidatDto = new CandidatDto();
                        candidatDto.setIdTerm(resultSet.getInt("id_term"));
                        candidatDto.setNomPref(resultSet.getString("lexical_value"));
                        candidatDto.setIdConcepte(resultSet.getInt("id_concept"));
                        candidatDto.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        candidatDto.setCreationDate(resultSet.getDate("created"));
                        temps.add(candidatDto);
                    }

                    for (CandidatDto candidatDto : temps) {
                        stmt.executeQuery("SELECT sta.value "
                                + "FROM candidat_status can_sta, status sta "
                                + "WHERE can_sta.id_status = sta.id_status "
                                + "AND can_sta.id_concept = " + candidatDto.getIdConcepte()
                                + " AND can_sta.id_thesaurus = '" + candidatDto.getIdThesaurus() + "'");
                        resultSet = stmt.getResultSet();
                        while (resultSet.next()) {
                            candidatDto.setStatut(resultSet.getString("value"));
                        }

                        stmt.executeQuery("SELECT count(*) FROM candidat_messages WHERE id_concept = "
                                + candidatDto.getIdConcepte()
                                + " AND id_thesaurus = '" + candidatDto.getIdThesaurus() + "'");
                        resultSet = stmt.getResultSet();
                        while (resultSet.next()) {
                            candidatDto.setNbrParticipant(resultSet.getInt("count"));
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                try {
                    connect.getPoolConnexion().getConnection().close();
                } catch (Exception e) {
                    LOG.error(e);
                }
            }
        } catch (SQLException sqle) {
            System.err.println("Error >>> " + sqle);
        }

        return temps;
    }
    
    
    
    public void enregistrerCandidat(Connect connect, CandidatDto candidatSelected, String IdThesaurus) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();

            /*stmt.executeUpdate("INSERT INTO candidat_messages(value, id_user, date, id_concept, id_thesaurus) "
                    + "VALUES ('" + msg + "', " + idUser + ", '" + sdf.format(new Date())
                    + "', " + idConcept + ", '" + idThesaurus + "')");*/
            
            resultSet.close();
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    public void getCandidatDetails(Connect connect, CandidatDto candidatSelected, String username) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();

            candidatSelected.setDomaine(getDomaineCandidat(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));
            candidatSelected.setMessages(getMessagesCandidat(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), username));
            candidatSelected.setDefenition(getNoteCandidat(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), "definition"));
            candidatSelected.setNoteApplication(getNoteCandidat(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), "note"));
            candidatSelected.setTraductions(getLangagesCandidat(candidatSelected.getIdThesaurus()));

            resultSet.close();
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    private List<TraductionDto> getLangagesCandidat(String idThesaurus) {
        List<TraductionDto> Traductions = new ArrayList<>();
        try {
            stmt.executeQuery("SELECT lang, lexical_value FROM term WHERE id_thesaurus = '" + idThesaurus + "'");
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                TraductionDto traductionDto = new TraductionDto();
                traductionDto.setLangue(resultSet.getString("lang"));
                traductionDto.setTraduction(resultSet.getString("lexical_value"));
                Traductions.add(traductionDto);
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        return Traductions;
    }

    private String getNoteCandidat(int idconcept, String idThesaurus, String noteType) {
        String definition = null;
        try {
            stmt.executeQuery("SELECT lexicalvalue FROM note "
                    + "WHERE notetypecode = '" + noteType + "' "
                    + "AND id_concept = '" + idconcept + "' AND "
                    + "id_thesaurus = '" + idThesaurus + "'");
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                definition = resultSet.getString("lexicalvalue");
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        return definition;
    }

    private String getDomaineCandidat(int idconcept, String idThesaurus) {
        String domaine = null;
        try {
            stmt.executeQuery("SELECT groupLabel.lexicalvalue "
                    + "FROM concept_group_label groupLabel, concept_group_concept con "
                    + "WHERE groupLabel.idgroup = con.idgroup "
                    + "AND con.idconcept = '" + idconcept + "' "
                    + "AND con.idthesaurus = '" + idThesaurus + "'");
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                domaine = resultSet.getString("lexicalvalue");
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        return domaine;
    }

    private List<MessageDto> getMessagesCandidat(int idconcept, String idThesaurus, String username) throws SQLException {
        List<MessageDto> messages = new ArrayList<>();
        try {
            stmt.executeQuery("SELECT users.username, cand.value, cand.date "
                    + "FROM candidat_messages cand, users "
                    + "WHERE id_concept = " + idconcept + " "
                    + "AND id_thesaurus = '" + idThesaurus + "' "
                    + "AND cand.id_user = users.id_user");
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                MessageDto messageDto = new MessageDto();
                messageDto.setMsg(resultSet.getString("value"));
                messageDto.setNom(resultSet.getString("username"));
                messageDto.setDate(resultSet.getString("date"));
                if (username.equals(resultSet.getString("username"))) {
                    messageDto.setMine(true);
                }
                messages.add(messageDto);
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        return messages;
    }

    public List<MessageDto> getAllMessagesCandidat(Connect connect, int idconcept, String idThesaurus, String username) {
        List<MessageDto> messages = new ArrayList<>();
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            messages = getMessagesCandidat(idconcept, idThesaurus, username);
            resultSet.close();
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
        }
        return messages;
    }

    public void addNewMessage(Connect connect, String msg, int idUser, int idConcept, String idThesaurus) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeUpdate("INSERT INTO candidat_messages(value, id_user, date, id_concept, id_thesaurus) "
                    + "VALUES ('" + msg + "', " + idUser + ", '" + sdf.format(new Date())
                    + "', " + idConcept + ", '" + idThesaurus + "')");
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

}
