package fr.cnrs.opentheso.services.candidats;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.repositories.AlignmentHelper;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.ImagesHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.models.candidats.NodeCandidateOld;
import fr.cnrs.opentheso.models.candidats.NodeProposition;
import fr.cnrs.opentheso.models.candidats.NodeTraductionCandidat;
import fr.cnrs.opentheso.repositories.candidats.CandidatDao;
import fr.cnrs.opentheso.repositories.candidats.DomaineDao;
import fr.cnrs.opentheso.repositories.candidats.TermeDao;
import fr.cnrs.opentheso.repositories.candidats.MessageCandidatHelper;
import fr.cnrs.opentheso.repositories.candidats.NoteDao;
import fr.cnrs.opentheso.repositories.candidats.RelationDao;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.models.candidats.TraductionDto;
import fr.cnrs.opentheso.models.candidats.enumeration.VoteType;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import java.sql.Connection;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;


@Slf4j
@Named(value = "candidatService")
@SessionScoped
public class CandidatService implements Serializable {

    @Autowired
    private ImagesHelper imagesHelper;

    @Autowired
    private CandidatDao candidatDao;

    @Autowired
    private DomaineDao domaineDao;

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private TermeDao termeDao;

    @Autowired
    private AlignmentHelper alignmentHelper;

    @Autowired
    private RelationDao relationDao;

    @Autowired
    private MessageCandidatHelper messageCandidatHelper;


    /**
     * Permet de récupérer la liste des candidats qui sont en attente
     */
    public List<CandidatDto> getCandidatsByStatus(Connect connect, String idThesaurus, String lang, int etat) {
        List<CandidatDto> candidatList = new ArrayList<>();
        try (HikariDataSource connection = connect.getPoolConnexion()) {
            candidatList = candidatDao.getCandidatsByStatus(connection, idThesaurus, lang, etat);
            candidatList.forEach(candidatDto -> {
                candidatDto.setNbrParticipant(candidatDao.searchParticipantCount(connection, candidatDto.getIdConcepte(), idThesaurus));
                candidatDto.setNbrDemande(candidatDao.searchDemandeCount(connection, candidatDto.getIdConcepte(), idThesaurus));
                candidatDto.setNbrVote(candidatDao.searchVoteCount(connection, candidatDto.getIdConcepte(), idThesaurus, VoteType.CANDIDAT.getLabel()));
                candidatDto.setNbrNoteVote(candidatDao.searchVoteCount(connection, candidatDto.getIdConcepte(), idThesaurus, VoteType.NOTE.getLabel()));
                candidatDto.setAlignments(alignmentHelper.getAllAlignmentOfConcept(connection, candidatDto.getIdConcepte(), idThesaurus));
            });
            connection.close();
        } catch (SQLException sqle) {
            log.error(sqle.toString());
        }
        return candidatList;
    }    
    
    /**
     * Permet de chercher des candidats
     */
    public List<CandidatDto> searchCandidats(Connect connect, String value,
            String idThesaurus, String lang, int etat, String statut) {
        List<CandidatDto> temps = new ArrayList<>();
        HikariDataSource ds = connect.getPoolConnexion();
        try {
            temps = candidatDao.searchCandidatsByValue(ds, value, idThesaurus, lang, etat, statut);
            temps.forEach(candidatDto -> {
                try {
                    candidatDto.setStatut(candidatDao.searchCondidatStatus(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrParticipant(candidatDao.searchParticipantCount(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrDemande(candidatDao.searchDemandeCount(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrVote(candidatDao.searchVoteCount(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus(), VoteType.CANDIDAT.getLabel()));
                    candidatDto.setNbrNoteVote(candidatDao.searchVoteCount(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus(), VoteType.NOTE.getLabel()));
                } catch (SQLException ex) {
                    Logger.getLogger(CandidatService.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            ds.close();
        } catch (SQLException sqle) {
            log.error(sqle.toString());
            System.err.println("Error >>> " + sqle);
            if (!ds.isClosed()) {
                ds.close();
            }
        }
        return temps;
    }

    public String saveNewCondidat(Connect connect, Concept concept) throws SQLException {

        Connection conn = null;
        String idNewCondidat = null;
        try {
            conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            idNewCondidat = conceptHelper.addConceptInTable(conn, concept, concept.getIdUser());
            if (idNewCondidat == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            try {
                Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex1) {
            }
        }            

        candidatDao.setStatutForCandidat(connect.getPoolConnexion(), 1, idNewCondidat, concept.getIdThesaurus(),
                concept.getIdUser() + "");
        return idNewCondidat;
    }

    public String saveNewTerm(Connect connect, Term term, String idConcept, int idUser) throws SQLException {

        Connection conn = null;
        String idTerm = null;
        try {
            conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            idTerm = termHelper.addTerm(connect.getPoolConnexion().getConnection(), term,
                idConcept, idUser);
            if (idTerm == null) {
                conn.rollback();
                conn.close();
                return null;
            }   
            conn.commit();
            conn.close();            
        } catch (SQLException ex) {
            try {
                log.error(ex.toString());
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex1) {
            }
        }  
        return idTerm;
    }

    public void updateIntitule(Connect connect, String intitule, String idThesaurus, String lang, String idTerm) throws SQLException {
        termeDao.updateIntitule(connect.getPoolConnexion(),
                intitule, idTerm, idThesaurus, lang);
    }

    public void updateDetailsCondidat(Connect connect, CandidatDto candidatSelected, int idUser) throws SQLException {

        //update domaine
        domaineDao.deleteAllDomaine(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());
        
        for (NodeIdValue collection : candidatSelected.getCollections()) {
            domaineDao.addNewDomaine(
                    connect, collection.getId(),
                    candidatSelected.getIdThesaurus(),
                    candidatSelected.getIdConcepte());
        }

        // gestion des relations

        relationDao.deleteAllRelations(connect, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), idUser);

        //update terme générique
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesGenerique())) {
            candidatSelected.getTermesGenerique().stream().forEach(nodeBT -> {
                relationDao.addRelationBT(connect,
                        candidatSelected.getIdConcepte(),
                        nodeBT.getId(),
                        candidatSelected.getIdThesaurus());
            });
        }

        //update terme associés
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
        
        if(!candidatSelected.getEmployePourList().isEmpty()) {
            candidatSelected.getEmployePourList().stream().forEach(employe -> {
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

    public void getCandidatDetails(Connect connect, CandidatDto candidatSelected, String idThesaurus) {

        try (HikariDataSource connection = connect.getPoolConnexion()){
            candidatSelected.setIdTerm(termHelper.getIdTermOfConcept(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));
            
            candidatSelected.setCollections(domaineDao.getDomaineCandidatByConceptAndThesaurusAndLang(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), candidatSelected.getLang()));

            candidatSelected.setTermesGenerique(relationDao.getCandidatRelationsBT(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            candidatSelected.setTermesAssocies(relationDao.getCandidatRelationsRT(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            candidatSelected.setEmployePourList(termeDao.getEmployePour(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));
             
            candidatSelected.setNodeNotes(new NoteDao().getNotesCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus()));

            candidatSelected.getNodeNotes().forEach(note -> {
                try {
                    note.setVoted(getVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        candidatSelected.getUserId(), note.getIdNote()+"", VoteType.NOTE));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            candidatSelected.setTraductions(termHelper.getTraductionsOfConcept(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getLang()).stream().map(
                    term -> new TraductionDto(term.getLang(),
                            term.getLexicalValue(), term.getCodePays())).collect(Collectors.toList()));

            candidatSelected.setMessages(messageCandidatHelper.getAllMessagesByCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getUserId()));

            candidatSelected.setVoted(candidatDao.getVote(connection, candidatSelected.getUserId(),
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), null, 
                    VoteType.CANDIDAT.getLabel()));

            candidatSelected.setAlignments(alignmentHelper.getAllAlignmentOfConcept(connect.getPoolConnexion(),
                    candidatSelected.getIdConcepte(), idThesaurus));

            candidatSelected.setImages(imagesHelper.getExternalImages(connect.getPoolConnexion(),
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));

            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }


///////// ajouté par Miled
    public void addVote(Connect connect, String idThesaurus, String idConcept, int idUser,
            String idNote, VoteType voteType) throws SQLException {
        candidatDao.addVote(connect.getPoolConnexion(), idThesaurus, idConcept, idUser, idNote,
                voteType.getLabel());
    }

    public boolean getVote(Connect connect, String idThesaurus, String idConcept, int idUser, String idNote,
            VoteType voteType) throws SQLException {
        return candidatDao.getVote(connect.getPoolConnexion(), idUser, idConcept, idThesaurus,
                idNote, voteType.getLabel());
    }

    public void removeVote(Connect connect, String idThesaurus, String idConcept, int idUser, String idNote,
            VoteType voteType) throws SQLException {
        candidatDao.removeVote(connect.getPoolConnexion(), idThesaurus, idConcept, idUser, idNote,
                voteType.getLabel());
    }
    
    public boolean insertCandidate(Connect connect, CandidatDto candidatDto, String adminMessage, int idUser) {
        return candidatDao.insertCandidate(connect.getPoolConnexion(), candidatDto, adminMessage, idUser);
    }
    
    public boolean rejectCandidate(Connect connect, CandidatDto candidatDto, String adminMessage, int idUser) {
        return candidatDao.rejectCandidate(connect.getPoolConnexion(), candidatDto, adminMessage, idUser);
    }     
    
    /**
     * permet de récupérer les anciens candidats saisies dans l'ancien module uniquement les candidats qui étatient en attente
     */    
    public String getOldCandidates(Connect connect, String idTheso, int idUser, NodePreference nodePreference) {

        StringBuilder messages = new StringBuilder();
        
        //// récupération des anciens candidats
        ArrayList<NodeCandidateOld> nodeCandidateOlds;
        try {
            nodeCandidateOlds = candidatDao.getCandidatesIdFromOldModule(connect.getPoolConnexion(), idTheso);
        } catch (SQLException e) {
            messages.append("Erreur : ").append(e.getMessage());
            return messages.toString();
        }
        if(nodeCandidateOlds == null || nodeCandidateOlds.isEmpty()) {
            return "Pas d'anciens candidats à récupérer";
        }
        
        for (NodeCandidateOld nodeCandidateOld : nodeCandidateOlds) {
            try {
                nodeCandidateOld.setNodeTraductions(
                    candidatDao.getCandidatesTraductionsFromOldModule(
                            connect.getPoolConnexion(), nodeCandidateOld.getIdCandidate(), idTheso));
                nodeCandidateOld.setNodePropositions(
                    candidatDao.getCandidatesMessagesFromOldModule(
                            connect.getPoolConnexion(), nodeCandidateOld.getIdCandidate(), idTheso));
            } catch (SQLException e) {
                messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                System.out.println(messages.toString());
            }
        }
        
        //// ajout des anciens candidats au nouveau module
        boolean exist = false;
        boolean first = true;
        Concept concept = new Concept();
        String idNewConcept = null;
        String idNewTerm = null;
        Term terme = new Term();
        
        for (NodeCandidateOld nodeCandidateOld : nodeCandidateOlds) {
            // ajout du candidat s'il n'existe pas dans le thésaurus en vérifiant langue par langue
            for (NodeTraductionCandidat nodeTraduction : nodeCandidateOld.getNodeTraductions()) {
                // en cas d'un nouveau candidat, verification dans les prefLabels
                if (termHelper.isPrefLabelExist(connect.getPoolConnexion(),
                        nodeTraduction.getTitle().trim(),
                        idTheso,
                        nodeTraduction.getIdLang())) {
                    messages.append("Candidat existe : ").append(nodeTraduction.getTitle());
                    System.out.println("Candidat existe : " + nodeTraduction.getTitle());
                    exist = true;
                    break;
                }
            }
            // si le candidat n'existe pas dans toutes les langues, on l'ajoute
            if(!exist) {
                concept.setIdConcept(null);
                concept.setIdThesaurus(idTheso);
                concept.setTopConcept(false);
                concept.setIdUser(idUser);
               // concept.setUserName(currentUser.getUsername());
                concept.setStatus("CA");

                conceptHelper.setNodePreference(nodePreference);

                try {
                    idNewConcept = saveNewCondidat(connect, concept);
                } catch (SQLException e) {
                    messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                    System.out.println(messages.toString());
                }
                if (idNewConcept == null) {
                    messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                    System.out.println(messages.toString());                    
                    continue;
                }

                for (NodeTraductionCandidat nodeTraduction : nodeCandidateOld.getNodeTraductions()) {
                    if(first) {
                        terme.setIdThesaurus(idTheso);
                        terme.setLang(nodeTraduction.getIdLang());
                        terme.setContributor(idUser);
                        terme.setLexicalValue(nodeTraduction.getTitle().trim());
                        terme.setSource("candidat");
                        terme.setStatus("D");
                        try {
                            idNewTerm = saveNewTerm(connect, terme, idNewConcept, idUser);                            
                        } catch (SQLException e) {
                            messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                            System.out.println(messages.toString());                            
                            continue;                            
                        }
                        first = false;
                    } else {
                        // ajout des traductions
                        if(!termHelper.addTraduction(connect.getPoolConnexion(),
                                nodeTraduction.getTitle(),
                                idNewTerm,
                                nodeTraduction.getIdLang(),
                                "candidat",
                                "D",
                                idTheso, idUser)) {
                            messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                            System.out.println(messages.toString());                            
                        }
                    }
                }
                first = true;
                // ajout des messages  
                for (NodeProposition nodeProposition : nodeCandidateOld.getNodePropositions()) {
                    messageCandidatHelper.addNewMessage(connect.getPoolConnexion(),
                            nodeProposition.getNote(),
                            nodeProposition.getIdUser(),
                            idNewConcept,
                            idTheso);
                }
                
                idNewConcept = null;
                idNewTerm = null;                
            }
            exist = false;
        }

        return "Import réussi\n" + messages;
    }
}
