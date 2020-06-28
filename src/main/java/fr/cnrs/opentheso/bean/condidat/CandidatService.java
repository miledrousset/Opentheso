package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bean.condidat.dao.CandidatDao;
import fr.cnrs.opentheso.bean.condidat.dao.DomaineDao;
import fr.cnrs.opentheso.bean.condidat.dao.TermeDao;
import fr.cnrs.opentheso.bean.condidat.dao.LangageDao;
import fr.cnrs.opentheso.bean.condidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.condidat.dao.NoteDao;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.condidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Named(value = "candidatService")
@SessionScoped
public class CandidatService implements Serializable {

    private final Log LOG = LogFactory.getLog(CandidatService.class);

    private Statement stmt;

    public List<CandidatDto> getAllCandidats(Connect connect, String idThesaurus, String lang) {
        List<CandidatDto> temps = new ArrayList<>();
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            CandidatDao condidatDao = new CandidatDao();
            temps = condidatDao.searchAllCondidats(stmt, idThesaurus, lang);
            for (CandidatDto candidatDto : temps) {
                candidatDto.setStatut(condidatDao.searchCondidatStatus(stmt,
                        candidatDto.getIdConcepte(), candidatDto.getIdThesaurus()));
                candidatDto.setNbrParticipant(condidatDao.searchParticipantCount(stmt,
                        candidatDto.getIdConcepte(), candidatDto.getIdThesaurus()));
                candidatDto.setNbrDemande(condidatDao.searchDemandeCount(stmt,
                        candidatDto.getIdConcepte(), candidatDto.getIdThesaurus()));
            }
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException sqle) {
            LOG.error(sqle);
            System.err.println("Error >>> " + sqle);
        }
        return temps;
    }

    public String saveNewCondidat(Connect connect, Concept concept, Term term,
            CandidatDto candidatSelected, ConceptHelper conceptHelper) throws SQLException {

        String idNewCondidat = conceptHelper.addConceptInTable(connect.getPoolConnexion().getConnection(), concept, concept.getIdUser());
        if (idNewCondidat == null) {
            connect.getPoolConnexion().getConnection().rollback();
            connect.getPoolConnexion().close();
            return null;
        }

        new CandidatDao().setStatutForCandidat(connect, 0, idNewCondidat,
                concept.getIdThesaurus(), concept.getIdUser() + "");

        String idTerm = new TermHelper().addTerm(connect.getPoolConnexion().getConnection(),
                term, idNewCondidat, concept.getIdUser());
        if (idTerm == null) {
            connect.getPoolConnexion().getConnection().rollback();
            connect.getPoolConnexion().close();
            return null;
        }

        new TermeDao().saveIntitule(connect, candidatSelected.getNomPref(),
                concept.getIdThesaurus(), concept.getLang(), idNewCondidat,
                concept.getIdUser() + "", idTerm);

        return idNewCondidat;
    }

    public void updateIntitule(Connect connect, String intitule, String idThesaurus,
            String lang, String idConcept, String idTerm) throws SQLException {

        new TermeDao().updateIntitule(connect, intitule, idThesaurus, lang, idConcept, idTerm);
    }

    public void updateDetailsCondidat(Connect connect, CandidatDto candidatSelected, CandidatDto initialCandidat)
            throws SQLException {

        //update domaine
        if (initialCandidat.getDomaine_id() == 0 && candidatSelected.getDomaine_id() > 0) {
            new DomaineDao().addNewDomaine(connect, candidatSelected.getDomaine_id(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getIdConcepte());
        } else if (candidatSelected.getDomaine_id() != initialCandidat.getDomaine_id()
                && initialCandidat.getDomaine_id() != 0) {
            new DomaineDao().updateDomaine(connect, initialCandidat.getDomaine_id(), candidatSelected.getDomaine_id(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());

        }

        //update terme générique
        if (initialCandidat.getTermeGenerique_id() == null && candidatSelected.getTermeGenerique_id() != null) {
            new TermeDao().saveNewTerme(connect, candidatSelected.getIdConcepte(), candidatSelected.getTermeGenerique_id(),
                    candidatSelected.getIdThesaurus(), "TG");
        } else if (!candidatSelected.getTermeGenerique_id().equals(initialCandidat.getDomaine_id())) {
            if (candidatSelected.getTermeGenerique_id() == null) {
                new TermeDao().deleteExistingTerme(connect, candidatSelected.getIdConcepte(), candidatSelected.getTermeGenerique_id(),
                        candidatSelected.getIdThesaurus(), "TG");
            } else {
                new TermeDao().updateTerme(connect, candidatSelected.getIdConcepte(), candidatSelected.getTermeGenerique_id(),
                        candidatSelected.getIdThesaurus(), "TG");
            }
        }
        
        //update terme associés
        TermeDao termeDao = new TermeDao();
        termeDao.deleteAllTermesByConcepteAndRole(connect, candidatSelected.getIdConcepte(), 
                    candidatSelected.getIdThesaurus(), "TS");
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesAssocies())) {
            //insert new associted termes-
            candidatSelected.getTermesAssocies().stream().forEach(terme -> {
                termeDao.saveNewTerme(connect, candidatSelected.getIdConcepte(), terme, 
                        candidatSelected.getIdThesaurus(), "TS");
            });
        }

        //update défénition
        saveNote(connect, initialCandidat == null ? "" : initialCandidat.getDefenition(), candidatSelected.getDefenition(),
                candidatSelected,"definition");

        //update note
        saveNote(connect, initialCandidat == null ? "" : initialCandidat.getNoteApplication(), candidatSelected.getNoteApplication(),
                candidatSelected,"note");

        //update traduction
    }

    public List<DomaineDto> getDomainesList(Connect connect, String idThesaurus) {
        return new DomaineDao().getAllDomaines(connect, idThesaurus);
    }

    public void getCandidatDetails(Connect connect, CandidatDto candidatSelected, String username) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();

            candidatSelected.setDomaine_id(new DomaineDao().getDomaineCandidat(stmt, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus()));
            candidatSelected.setTermeGenerique_id(new TermeDao().searchTGByConceptAndThesaurus(connect,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));
            candidatSelected.setTermesAssocies(new TermeDao().searchTSByConceptAndThesaurus(connect,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));
            candidatSelected.setDefenition(new NoteDao().getNoteCandidat(stmt, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), "definition"));
            candidatSelected.setNoteApplication(new NoteDao().getNoteCandidat(stmt, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), "note"));
            candidatSelected.setTraductions(new LangageDao().getLangagesCandidat(stmt, candidatSelected.getIdThesaurus()));
            candidatSelected.setMessages(new MessageDao().getMessagesCandidat(stmt, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), username));
            candidatSelected.setParticipants(new MessageDao().getParticipantsByCandidat(connect,candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus()));

            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    public List<MessageDto> getAllMessagesCandidat(Connect connect, String idconcept,
            String idThesaurus, String username) throws SQLException {
        return new MessageDao().getAllMessagesCandidat(connect, idconcept, idThesaurus, username);
    }

    public void addNewMessage(Connect connect, String msg, String idUser, String idConcept,
            String idThesaurus) throws SQLException {
        new MessageDao().addNewMessage(connect, msg, idUser, idConcept, idThesaurus);
    }

    public void saveNote(Connect connect, String initialNoteValue, String newNoteValue, CandidatDto candidatSelected,
                         String noteType) throws SQLException {

        stmt = connect.getPoolConnexion().getConnection().createStatement();
        NoteDao noteDao = new NoteDao();
        noteDao.deleteNote(connect, noteType, newNoteValue, candidatSelected.getIdTerm(), candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus());

        if (!StringUtils.isEmpty(newNoteValue) && StringUtils.isEmpty(initialNoteValue)) {
            noteDao.SaveNote(connect, noteType, newNoteValue, candidatSelected.getIdTerm(), candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getLang());
        } else if (!StringUtils.isEmpty(newNoteValue) && !newNoteValue.equals(initialNoteValue)) {
            noteDao.updateNote(connect, noteType, newNoteValue, candidatSelected.getIdTerm(), candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getLang());
        }
        stmt.close();
    }

}
