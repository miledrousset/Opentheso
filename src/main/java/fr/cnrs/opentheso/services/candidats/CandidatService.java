package fr.cnrs.opentheso.services.candidats;

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

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
    private NoteDao noteDao;

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
    public List<CandidatDto> getCandidatsByStatus(String idThesaurus, String lang, int etat) {
        List<CandidatDto> candidatList = candidatDao.getCandidatsByStatus(idThesaurus, lang, etat);
        candidatList.forEach(candidatDto -> {
            candidatDto.setNbrParticipant(candidatDao.searchParticipantCount(candidatDto.getIdConcepte(), idThesaurus));
            candidatDto.setNbrDemande(candidatDao.searchDemandeCount(candidatDto.getIdConcepte(), idThesaurus));
            candidatDto.setNbrVote(candidatDao.searchVoteCount(candidatDto.getIdConcepte(), idThesaurus, VoteType.CANDIDAT.getLabel()));
            candidatDto.setNbrNoteVote(candidatDao.searchVoteCount(candidatDto.getIdConcepte(), idThesaurus, VoteType.NOTE.getLabel()));
            candidatDto.setAlignments(alignmentHelper.getAllAlignmentOfConcept(candidatDto.getIdConcepte(), idThesaurus));
        });
        return candidatList;
    }    
    
    /**
     * Permet de chercher des candidats
     */
    public List<CandidatDto> searchCandidats(String value, String idThesaurus, String lang, int etat, String statut) {

        List<CandidatDto> temps = candidatDao.searchCandidatsByValue(value, idThesaurus, lang, etat, statut);
        temps.forEach(candidatDto -> {
            candidatDto.setStatut(candidatDao.searchCondidatStatus(candidatDto.getIdConcepte(), candidatDto.getIdThesaurus()));
            candidatDto.setNbrParticipant(candidatDao.searchParticipantCount(candidatDto.getIdConcepte(), candidatDto.getIdThesaurus()));
            candidatDto.setNbrDemande(candidatDao.searchDemandeCount(candidatDto.getIdConcepte(), candidatDto.getIdThesaurus()));
            candidatDto.setNbrVote(candidatDao.searchVoteCount(candidatDto.getIdConcepte(), candidatDto.getIdThesaurus(), VoteType.CANDIDAT.getLabel()));
            candidatDto.setNbrNoteVote(candidatDao.searchVoteCount(candidatDto.getIdConcepte(), candidatDto.getIdThesaurus(), VoteType.NOTE.getLabel()));
        });
        return temps;
    }

    public String saveNewCondidat(Concept concept) throws SQLException {

        var idNewCondidat = conceptHelper.addConceptInTable(concept, concept.getIdUser());
        candidatDao.setStatutForCandidat(1, idNewCondidat, concept.getIdThesaurus(),
                concept.getIdUser() + "");
        return idNewCondidat;
    }

    public String saveNewTerm(Term term, String idConcept, int idUser) throws SQLException {

        return termHelper.addTerm(term, idConcept, idUser);
    }

    public void updateIntitule(String intitule, String idThesaurus, String lang, String idTerm) {
        termeDao.updateIntitule(intitule, idTerm, idThesaurus, lang);
    }

    public void updateDetailsCondidat(CandidatDto candidatSelected, int idUser) {

        //update domaine
        domaineDao.deleteAllDomaine(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());
        
        for (NodeIdValue collection : candidatSelected.getCollections()) {
            domaineDao.addNewDomaine(collection.getId(), candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());
        }

        // gestion des relations
        relationDao.deleteAllRelations(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus());

        //update terme générique
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesGenerique())) {
            candidatSelected.getTermesGenerique().stream().forEach(nodeBT -> {
                relationDao.addRelationBT(candidatSelected.getIdConcepte(), nodeBT.getId(), candidatSelected.getIdThesaurus());
            });
        }

        //update terme associés
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesAssocies())) {
            candidatSelected.getTermesAssocies().stream().forEach(nodeRT -> {
                relationDao.addRelationRT(candidatSelected.getIdConcepte(), nodeRT.getId(), candidatSelected.getIdThesaurus());
            });
        }

        // Employé pour
        termeDao.deleteEMByIdTermAndLang(candidatSelected.getIdTerm(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang());
        
        if(!candidatSelected.getEmployePourList().isEmpty()) {
            candidatSelected.getEmployePourList().stream().forEach(employe -> {
                termeDao.addNewEmployePour(employe, candidatSelected.getIdThesaurus(), candidatSelected.getLang(),
                        candidatSelected.getIdTerm());
            });
        }
    }

    public void getCandidatDetails(CandidatDto candidatSelected, String idThesaurus) {

        candidatSelected.setIdTerm(termHelper.getIdTermOfConcept(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));

        candidatSelected.setCollections(domaineDao.getDomaineCandidatByConceptAndThesaurusAndLang(candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang()));

        candidatSelected.setTermesGenerique(relationDao.getCandidatRelationsBT(candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang()));

        candidatSelected.setTermesAssocies(relationDao.getCandidatRelationsRT(candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang()));

        candidatSelected.setEmployePourList(termeDao.getEmployePour(candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang()));

        candidatSelected.setNodeNotes(noteDao.getNotesCandidat(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));

        candidatSelected.getNodeNotes().forEach(note -> {
            try {
                note.setVoted(getVote(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        candidatSelected.getUserId(), note.getIdNote()+"", VoteType.NOTE));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        candidatSelected.setTraductions(termHelper.getTraductionsOfConcept(candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang()).stream().map(
                term -> new TraductionDto(term.getLang(),
                        term.getLexicalValue(), term.getCodePays())).collect(Collectors.toList()));

        candidatSelected.setMessages(messageCandidatHelper.getAllMessagesByCandidat(candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), candidatSelected.getUserId()));

        candidatSelected.setVoted(candidatDao.getVote(candidatSelected.getUserId(),
                candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), null,
                VoteType.CANDIDAT.getLabel()));

        candidatSelected.setAlignments(alignmentHelper.getAllAlignmentOfConcept(
                candidatSelected.getIdConcepte(), idThesaurus));

        candidatSelected.setImages(imagesHelper.getExternalImages(
                candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));
    }


///////// ajouté par Miled
    public void addVote(String idThesaurus, String idConcept, int idUser,
            String idNote, VoteType voteType) throws SQLException {
        candidatDao.addVote(idThesaurus, idConcept, idUser, idNote,
                voteType.getLabel());
    }

    public boolean getVote(String idThesaurus, String idConcept, int idUser, String idNote,
            VoteType voteType) throws SQLException {
        return candidatDao.getVote(idUser, idConcept, idThesaurus,
                idNote, voteType.getLabel());
    }

    public void removeVote(String idThesaurus, String idConcept, int idUser, String idNote,
            VoteType voteType) throws SQLException {
        candidatDao.removeVote(idThesaurus, idConcept, idUser, idNote,
                voteType.getLabel());
    }
    
    public boolean insertCandidate(CandidatDto candidatDto, String adminMessage, int idUser) {
        return candidatDao.insertCandidate(candidatDto, adminMessage, idUser);
    }
    
    public boolean rejectCandidate(CandidatDto candidatDto, String adminMessage, int idUser) {
        return candidatDao.rejectCandidate(candidatDto, adminMessage, idUser);
    }     
    
    /**
     * permet de récupérer les anciens candidats saisies dans l'ancien module uniquement les candidats qui étatient en attente
     */    
    public String getOldCandidates(String idTheso, int idUser, NodePreference nodePreference) {

        StringBuilder messages = new StringBuilder();
        
        //// récupération des anciens candidats
        ArrayList<NodeCandidateOld> nodeCandidateOlds;
        try {
            nodeCandidateOlds = candidatDao.getCandidatesIdFromOldModule(idTheso);
        } catch (SQLException e) {
            messages.append("Erreur : ").append(e.getMessage());
            return messages.toString();
        }
        if(nodeCandidateOlds == null || nodeCandidateOlds.isEmpty()) {
            return "Pas d'anciens candidats à récupérer";
        }
        
        for (NodeCandidateOld nodeCandidateOld : nodeCandidateOlds) {
            nodeCandidateOld.setNodeTraductions(candidatDao.getCandidatesTraductionsFromOldModule(nodeCandidateOld.getIdCandidate(), idTheso));
            nodeCandidateOld.setNodePropositions(candidatDao.getCandidatesMessagesFromOldModule(nodeCandidateOld.getIdCandidate(), idTheso));
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
                if (termHelper.isPrefLabelExist(
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
                    idNewConcept = saveNewCondidat(concept);
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
                            idNewTerm = saveNewTerm(terme, idNewConcept, idUser);                            
                        } catch (SQLException e) {
                            messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                            System.out.println(messages.toString());                            
                            continue;                            
                        }
                        first = false;
                    } else {
                        // ajout des traductions
                        if(!termHelper.addTraduction(
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
                    messageCandidatHelper.addNewMessage(
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
