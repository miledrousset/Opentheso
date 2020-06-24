package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dao.CondidatDao;
import fr.cnrs.opentheso.bean.condidat.dao.DomaineDao;
import fr.cnrs.opentheso.bean.condidat.dao.IntituleDao;
import fr.cnrs.opentheso.bean.condidat.dao.LangageDao;
import fr.cnrs.opentheso.bean.condidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.condidat.dao.NoteDao;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Named(value = "candidatService")
@SessionScoped
public class CandidatService implements Serializable {

    private final Log LOG = LogFactory.getLog(CandidatService.class);

    private Statement stmt;

    public List<CandidatDto> getAllCandidats(Connect connect) {
        List<CandidatDto> temps = new ArrayList<>();
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            CondidatDao condidatDao = new CondidatDao();
            temps = condidatDao.searchAllCondidats(stmt);
            for (CandidatDto candidatDto : temps) {
                candidatDto.setStatut(condidatDao.searchCondidatStatus(stmt,
                        candidatDto.getIdConcepte(), candidatDto.getIdThesaurus()));
                candidatDto.setNbrParticipant(condidatDao.searchParticipantCount(stmt,
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

    public void saveIntitule(Connect connect, String intitule, String idThesaurus, String lang, String idConcept) {
        new IntituleDao().saveIntitule(connect, stmt, intitule, idThesaurus,
                lang, idConcept);
    }

    public void updateIntitule(Connect connect, String intitule, String idThesaurus, String lang, String idConcept) {
        new IntituleDao().updateIntitule(connect, stmt, intitule, idThesaurus, lang, idConcept);
    }

    public void getCandidatDetails(Connect connect, CandidatDto candidatSelected, String username) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            candidatSelected.setDomaine(new DomaineDao().getDomaineCandidat(connect, stmt, candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));
            candidatSelected.setMessages(new MessageDao().getMessagesCandidat(stmt, candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), username));
            candidatSelected.setDefenition(new NoteDao().getNoteCandidat(stmt, candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), "definition"));
            candidatSelected.setNoteApplication(new NoteDao().getNoteCandidat(stmt, candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), "note"));
            candidatSelected.setTraductions(new LangageDao().getLangagesCandidat(stmt, candidatSelected.getIdThesaurus()));
            stmt.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    public List<MessageDto> getAllMessagesCandidat(Connect connect, String idconcept, String idThesaurus, String username) {
        return new MessageDao().getAllMessagesCandidat(connect, stmt, idconcept,
                idThesaurus, username);
    }

    public void addNewMessage(Connect connect, String msg, String idUser, String idConcept, String idThesaurus) {
        new MessageDao().addNewMessage(connect, stmt, msg, idUser, idConcept,
                idThesaurus);
    }
    
    public void saveNote(Connect connect, String initialNoteValue, String newNoteValue, 
            CandidatDto candidatSelected, String lang, String noteType) {
        
        if (!StringUtils.isEmpty(newNoteValue) && StringUtils.isEmpty(initialNoteValue)) {
            new NoteDao().SaveNote(connect, stmt, noteType, newNoteValue, 
                    candidatSelected.getIdTerm(), candidatSelected.getIdConcepte(), lang, candidatSelected.getIdThesaurus());
        } else if (!StringUtils.isEmpty(newNoteValue) && !newNoteValue.equals(initialNoteValue)) {
            new NoteDao().updateNote(connect, stmt, noteType, newNoteValue, 
                    candidatSelected.getIdTerm(), candidatSelected.getIdConcepte(), lang, candidatSelected.getIdThesaurus());
        } 
    }

}
