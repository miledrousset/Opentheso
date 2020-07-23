package fr.cnrs.opentheso.bean.condidat;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bean.condidat.dao.CandidatDao;
import fr.cnrs.opentheso.bean.condidat.dao.DomaineDao;
import fr.cnrs.opentheso.bean.condidat.dao.TermeDao;
import fr.cnrs.opentheso.bean.condidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.condidat.dao.NoteDao;
import fr.cnrs.opentheso.bean.condidat.dao.RelationDao;
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
import java.util.Arrays;
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
                    candidatDto.setNbrVote(condidatDao.searchVoteCount(connection, candidatDto.getIdConcepte(),
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

    public String getCandidatID(Connect connect) {
        int id = 0;
        HikariDataSource connection = connect.getPoolConnexion();
        try {
            id = new CandidatDao().getMaxCandidatId(connection);
        } catch (Exception e) {
            LOG.error(e);
        }
        connection.close();
        return (id + 1) + "";
    }

    public String saveNewCondidat(Connect connect, Concept concept, ConceptHelper conceptHelper)
            throws SQLException {
        HikariDataSource connection = connect.getPoolConnexion();
        String idNewCondidat = conceptHelper.addConceptInTable(connect.getPoolConnexion().getConnection(), concept, concept.getIdUser());
        if (idNewCondidat == null) {
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
            return null;
        }

        // faux, il ne faut pas créer un Synonyme en même temps que le prefLabel
    /*    new TermeDao().saveIntitule(connect, candidatSelected.getNomPref(), candidatSelected.getIdThesaurus(),
                candidatSelected.getLang(), candidatSelected.getIdConcepte(), idTerm);*/

        connection.close();

        return idTerm;
    }

    public void updateIntitule(Connect connect, String intitule, String idThesaurus,
            String lang, String idTerm) throws SQLException {
        new TermeDao().updateIntitule(connect.getPoolConnexion(),
                intitule, idTerm, idThesaurus, lang);
    }

    public void updateDetailsCondidat(Connect connect, CandidatDto candidatSelected, CandidatDto initialCandidat,
            List<CandidatDto> allTerms, List<DomaineDto> allDomaines, int idUser)
            throws SQLException {

        //update domaine
        if (initialCandidat == null || (StringUtils.isEmpty(initialCandidat.getDomaine())
                && !StringUtils.isEmpty(candidatSelected.getDomaine()))) {
            new DomaineDao().addNewDomaine(connect, getDomaineId(allDomaines, candidatSelected.getDomaine()),
                    candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());

        } else if (!candidatSelected.getDomaine().equals(initialCandidat.getDomaine()) && !StringUtils.isEmpty(initialCandidat.getDomaine())) {
            new DomaineDao().updateDomaine(connect, getDomaineId(allDomaines, initialCandidat.getDomaine()),
                    getDomaineId(allDomaines, candidatSelected.getDomaine()),
                    candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());
        }

        // gestion des relations
        RelationDao relationDao = new RelationDao();
        TermeDao termeDao = new TermeDao();

        relationDao.deleteAllRelations(connect, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), idUser);

        //update terme générique
        /*     termeDao.deleteAllT(connect, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), TermEnum.TERME_GENERIQUE.getLabel());*/
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesGenerique())) {
            candidatSelected.getTermesGenerique().stream().forEach(nodeBT -> {
                relationDao.addRelationBT(connect,
                        candidatSelected.getIdConcepte(),
                        nodeBT.getId(),
                        candidatSelected.getIdThesaurus());
            });
        }

        //update terme associés
        /*      termeDao.deleteAllTermesByConcepteAndRole(connect, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), TermEnum.TERME_ASSOCIE.getLabel());*/
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesAssocies())) {
            candidatSelected.getTermesAssocies().stream().forEach(nodeRT -> {
                relationDao.addRelationRT(connect,
                        candidatSelected.getIdConcepte(),
                        nodeRT.getId(),
                        candidatSelected.getIdThesaurus());
            });
        }

        // Employé pour
        termeDao.deleteEMByIdTermAndLang(connect.getPoolConnexion(), candidatSelected.getIdTerm(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang());
        
        if(!candidatSelected.getEmployePour().isEmpty()) {
            List<String> listEmployePour = new ArrayList(Arrays.asList(candidatSelected.getEmployePour().split(",")));
            if (!CollectionUtils.isEmpty(listEmployePour)) {
                listEmployePour.stream().forEach(employe -> {
                    try {
                        termeDao.addNewEmployePour(connect,
                                employe, candidatSelected.getIdThesaurus(),
                                candidatSelected.getLang(),
                                candidatSelected.getIdTerm());
                    } catch (SQLException ex) {
                        Logger.getLogger(CandidatService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }            
        }


        //update défénition
        saveNote(connect, candidatSelected.getDefenitions(), candidatSelected, NoteEnum.DEFINITION.getName());

        //update note
        saveNote(connect, candidatSelected.getNoteApplication(), candidatSelected, NoteEnum.NOTE.getName());

        //update traduction
//        saveTraduction(connect, candidatSelected);

    }

    // déprécié par Miled , ca se passe dans TraductionService
    // il faut faire l'aciton de création en temps réel, ceci évite à lutilisateur 
    //d'oublier d'enregistrer et surtout de controler en temps réel l'existance des traductions dans le thésaurus 
/*    private void saveTraduction(Connect connect, CandidatDto candidatSelected) throws SQLException {
        HikariDataSource connection = connect.getPoolConnexion();

        TermeDao termeDao = new TermeDao();

    //    String idTerm = termeDao.getIdTermeByCandidatAndThesaurus(connection, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());

        termeDao.deleteTermsByIdTerm(connection, candidatSelected.getIdTerm(), candidatSelected.getLang());

        for (TraductionDto traduction : candidatSelected.getTraductions()) {
            Term term = new Term();
            term.setStatus("D");
            term.setSource("Candidat");
            term.setLang(traduction.getLangue().trim().toLowerCase());
            term.setLexical_value(traduction.getTraduction());
            term.setId_thesaurus(candidatSelected.getIdThesaurus());
            term.setContributor(candidatSelected.getUserId());
            term.setIdUser(candidatSelected.getUserId() + "");
            term.setId_term(candidatSelected.getIdTerm());

            termeDao.addNewTerme(connection, term);
        }

        connection.close();
    }*/

    /// déprécié par Miled, on ne peut pas récupérer un Id d'après un label,
    // le label peut être en doublon
    /*
    private String getIdCancepteFromLabel(List<CandidatDto> termes, String label) {
        for (CandidatDto candidat : termes) {
            if (candidat.getNomPref().equals(label)) {
                return candidat.getIdConcepte();
            }
        }
        return null;
    }*/

    private String getDomaineId(List<DomaineDto> domaines, String label) {
        for (DomaineDto domaineDto : domaines) {
            if (domaineDto.getName().equals(label)) {
                return domaineDto.getId();
            }
        }
        return "";
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
            CandidatDao candidatDao = new CandidatDao();
            RelationDao relationDao = new RelationDao();

            candidatSelected.setDomaine(new DomaineDao().getDomaineCandidatByConceptAndThesaurusAndLang(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), candidatSelected.getLang()));

            candidatSelected.setTermesGenerique(relationDao.getCandidatRelationsBT(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            candidatSelected.setTermesAssocies(relationDao.getCandidatRelationsRT(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            candidatSelected.setEmployePour(termeDao.getEmployePour(connection,
                    candidatSelected.getIdTerm(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));
             
            candidatSelected.setDefenitions(noteDao.getNotesCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), NoteEnum.DEFINITION.getName(), candidatSelected.getLang()));

            candidatSelected.setNoteApplication(noteDao.getNoteCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), NoteEnum.NOTE.getName(), candidatSelected.getLang()));

            candidatSelected.setTraductions(new TermHelper().getTraductionsOfConcept(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getLang()).stream().map(
                    term -> new TraductionDto(term.getLang(),
                            term.getLexicalValue())).collect(Collectors.toList()));

            candidatSelected.setMessages(messageDao.getAllMessagesByCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getUserId()));

            candidatSelected.setVoted(candidatDao.getVote(connection,
                    candidatSelected.getUserId(),
                    candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus()));

            connection.close();
            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
            if (!connection.isClosed()) {
                connection.close();
            }
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
        HikariDataSource connection = connect.getPoolConnexion();
        NoteDao noteDao = new NoteDao();
        noteDao.deleteAllNoteByConceptAndThesaurusAndType(connection, noteType, candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus());
        newNoteValue.forEach(note -> {
            noteDao.saveNote(connection, noteType, note, candidatSelected.getIdTerm(), candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getLang());
        });
        connection.close();
    }

///////// ajouté par Miled
    public void addVote(Connect connect,
            String idThesaurus, String idConcept, int idUser) throws SQLException {
        new CandidatDao().addVote(connect.getPoolConnexion(), idThesaurus, idConcept, idUser);
    }

    public boolean getVote(Connect connect,
            String idThesaurus, String idConcept, int idUser) throws SQLException {
        return new CandidatDao().getVote(connect.getPoolConnexion(),
                idUser, idConcept, idThesaurus);
    }

    public void removeVote(Connect connect,
            String idThesaurus, String idConcept, int idUser) throws SQLException {
        new CandidatDao().removeVote(connect.getPoolConnexion(), idThesaurus, idConcept, idUser);
    }

}
