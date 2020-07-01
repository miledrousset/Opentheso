package fr.cnrs.opentheso.bean.condidat;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bean.condidat.dao.CandidatDao;
import fr.cnrs.opentheso.bean.condidat.dao.DomaineDao;
import fr.cnrs.opentheso.bean.condidat.dao.TermeDao;
import fr.cnrs.opentheso.bean.condidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.condidat.dao.NoteDao;
import fr.cnrs.opentheso.bean.condidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.condidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.condidat.dto.TraductionDto;
import fr.cnrs.opentheso.bean.condidat.enumeration.LanguageEnum;
import fr.cnrs.opentheso.bean.condidat.enumeration.NoteEnum;
import fr.cnrs.opentheso.bean.condidat.enumeration.TermEnum;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Named(value = "candidatService")
@SessionScoped
public class CandidatService implements Serializable {

    private final Log LOG = LogFactory.getLog(CandidatService.class);
    

    public List<CandidatDto> getAllCandidats(Connect connect, String idThesaurus, String lang) {
        List<CandidatDto> temps = new ArrayList<>();
        HikariDataSource connection = connect.getPoolConnexion();
        try {
            CandidatDao condidatDao = new CandidatDao();
            temps = condidatDao.searchAllCondidats(connection, idThesaurus, lang);
            temps.forEach(candidatDto -> {
                try {
                    candidatDto.setStatut(condidatDao.searchCondidatStatus(connection, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrParticipant(condidatDao.searchParticipantCount(connection, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrDemande(condidatDao.searchDemandeCount(connection, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                } catch (SQLException ex) {
                    Logger.getLogger(CandidatService.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            connection.close();
        } catch (SQLException sqle) {
            LOG.error(sqle);
            System.err.println("Error >>> " + sqle);
            if (!connection.isClosed()) {
                connection.close();
            }
        }
        return temps;
    }

    public String getCandidatID(Connect connect) throws SQLException {
        HikariDataSource connection = connect.getPoolConnexion();
        int id = new CandidatDao().getMaxCandidatId(connection) + 1;
        connection.close();
        return id+"";
    }
    
    public String saveNewCondidat(Connect connect, Concept concept, ConceptHelper conceptHelper) 
            throws SQLException {
        HikariDataSource connection = connect.getPoolConnexion();
        String idNewCondidat = conceptHelper.addConceptInTable(connect.getPoolConnexion().getConnection(), concept, concept.getIdUser());
        if (idNewCondidat == null) {
            connect.getPoolConnexion().getConnection().rollback();
            connect.getPoolConnexion().close();
            return null;
        }

        new CandidatDao().setStatutForCandidat(connection, 1, idNewCondidat, concept.getIdThesaurus(),
                concept.getIdUser() + "");

        connection.close();
        return idNewCondidat;
    }

    public String saveNewTerm(Connect connect, Term term, CandidatDto candidatSelected) throws SQLException {
        
        HikariDataSource connection = connect.getPoolConnexion();
        
        String idTerm = new TermHelper().addTerm(connect.getPoolConnexion().getConnection(), term,
                candidatSelected.getIdConcepte(), candidatSelected.getUserId());

        if (idTerm == null) {
            connect.getPoolConnexion().getConnection().rollback();
            connect.getPoolConnexion().close();
            return null;
        }

        new TermeDao().saveIntitule(connect, candidatSelected.getNomPref(), candidatSelected.getIdThesaurus(), 
                candidatSelected.getLang(), candidatSelected.getIdConcepte(), idTerm);
        
        connection.close();

        return idTerm;
    }

    public void updateIntitule(Connect connect, String intitule, String idThesaurus,
            String lang, String idConcept, String idTerm) {
        new TermeDao().updateIntitule(connect, intitule, idThesaurus, lang, idConcept, idTerm);
    }

    public void updateDetailsCondidat(Connect connect, CandidatDto candidatSelected, CandidatDto initialCandidat,
                                      List<CandidatDto> allTerms, List<DomaineDto> allDomaines)
            throws SQLException {

        //update domaine
        if (initialCandidat == null || (StringUtils.isEmpty(initialCandidat.getDomaine())
                && !StringUtils.isEmpty(candidatSelected.getDomaine()))) {
            new DomaineDao().addNewDomaine(connect, getDomaineId(allDomaines,candidatSelected.getDomaine()),
                    candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());
            
        } else if (!candidatSelected.getDomaine().equals(initialCandidat.getDomaine()) && !StringUtils.isEmpty(initialCandidat.getDomaine())) {
            new DomaineDao().updateDomaine(connect, getDomaineId(allDomaines, initialCandidat.getDomaine()),
                    getDomaineId(allDomaines, candidatSelected.getDomaine()),
                    candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());
        }

        TermeDao termeDao = new TermeDao();
        //update terme générique
        termeDao.deleteAllTermesByConcepteAndRole(connect, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), TermEnum.TERME_GENERIQUE.getLabel());
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesGenerique())) {
            candidatSelected.getTermesGenerique().stream().forEach(terme -> {
                termeDao.saveNewTerme(connect, candidatSelected.getIdConcepte(), getIdCancepteFromLabel(allTerms, terme),
                        candidatSelected.getIdThesaurus(), TermEnum.TERME_GENERIQUE.getLabel());
            });
        }

        //update terme associés
        termeDao.deleteAllTermesByConcepteAndRole(connect, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), TermEnum.TERME_ASSOCIE.getLabel());
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesAssocies())) {
            candidatSelected.getTermesAssocies().stream().forEach(terme -> {
                termeDao.saveNewTerme(connect, candidatSelected.getIdConcepte(), getIdCancepteFromLabel(allTerms, terme),
                        candidatSelected.getIdThesaurus(), TermEnum.TERME_ASSOCIE.getLabel());
            });
        }

        // Employé pour
        termeDao.deleteAllTermesByConcepteAndRole(connect, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), TermEnum.EMPLOYE.getLabel());
        if (!CollectionUtils.isEmpty(candidatSelected.getEmployePour())) {
            candidatSelected.getEmployePour().stream().forEach(terme -> {
                termeDao.saveNewTerme(connect, candidatSelected.getIdConcepte(), getIdCancepteFromLabel(allTerms, terme),
                        candidatSelected.getIdThesaurus(), TermEnum.EMPLOYE.getLabel());
            });
        }

        //update défénition
        saveNote(connect, candidatSelected.getDefenitions(), candidatSelected, NoteEnum.DEFINITION.getName());

        //update note
        saveNote(connect, candidatSelected.getNoteApplication(), candidatSelected, NoteEnum.NOTE.getName());

    }

    private String getIdCancepteFromLabel(List<CandidatDto> termes, String label) {
        for (CandidatDto candidat : termes) {
            if (candidat.getNomPref().equals(label)) {
                return candidat.getIdConcepte();
            }
        }
        return null;
    }

    private int getDomaineId(List<DomaineDto> domaines, String label) {
        for (DomaineDto domaineDto : domaines) {
            if (domaineDto.getName().equals(label)) {
                return domaineDto.getId();
            }
        }
        return 0;
    }

    public List<DomaineDto> getDomainesList(Connect connect, String idThesaurus, String lang) {
        return new DomaineDao().getAllDomaines(connect, idThesaurus, lang);
    }

    public void getCandidatDetails(Connect connect, CandidatDto candidatSelected) {
        HikariDataSource connection = connect.getPoolConnexion();
        try {
            NoteDao noteDao = new NoteDao();
            TermeDao termeDao = new TermeDao();
            MessageDao messageDao = new MessageDao();
            
            candidatSelected.setDomaine(new DomaineDao().getDomaineCandidatByConceptAndThesaurusAndLang(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), candidatSelected.getLang()));
            
            candidatSelected.setTermesGenerique(termeDao.searchTermeByConceptAndThesaurusAndRoleAndLang(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), TermEnum.TERME_GENERIQUE.getLabel(),
                    candidatSelected.getLang()));
            
            candidatSelected.setTermesAssocies(termeDao.searchTermeByConceptAndThesaurusAndRoleAndLang(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), TermEnum.TERME_ASSOCIE.getLabel(),
                    candidatSelected.getLang()));

            candidatSelected.setEmployePour(termeDao.searchTermeByConceptAndThesaurusAndRoleAndLang(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), TermEnum.EMPLOYE.getLabel(),
                    candidatSelected.getLang()));

            candidatSelected.setDefenitions(noteDao.getNotesCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), NoteEnum.DEFINITION.getName(), candidatSelected.getLang()));
            
            candidatSelected.setNoteApplication(noteDao.getNoteCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), NoteEnum.NOTE.getName(), candidatSelected.getLang()));

            candidatSelected.setTraductions(new TermHelper().getTraductionsOfConcept(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getLang()).stream().map(
                            term -> new TraductionDto(LanguageEnum.valueOf(term.getLang()).getLanguage(),
                            term.getLexicalValue())).collect(Collectors.toList()));
            
            candidatSelected.setMessages(messageDao.getAllMessagesByCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getUserId()));

            candidatSelected.setParticipants(messageDao.getParticipantsByCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus()));

            connection.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
            if(!connection.isClosed()) connection.close();
        }
    }

    public void saveNote(Connect connect, String newNoteValue, CandidatDto candidatSelected, String noteType) {
        
        HikariDataSource connection = connect.getPoolConnexion();
        NoteDao noteDao = new NoteDao();
        noteDao.deleteNote(connection, noteType, newNoteValue, candidatSelected.getIdTerm(), candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus());
        noteDao.saveNote(connection, noteType, newNoteValue, candidatSelected.getIdTerm(), candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang());
        connection.close();
    }

    public void saveNote(Connect connect, List<String> newNoteValue, CandidatDto candidatSelected, String noteType) {
        newNoteValue.forEach(note -> {
            saveNote(connect, note, candidatSelected, NoteEnum.DEFINITION.getName());
        });
    }

}
