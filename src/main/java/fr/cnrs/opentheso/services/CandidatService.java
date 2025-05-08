package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.Note;
import fr.cnrs.opentheso.entites.CandidatMessages;
import fr.cnrs.opentheso.entites.CandidatStatus;
import fr.cnrs.opentheso.entites.CandidatVote;
import fr.cnrs.opentheso.entites.NonPreferredTerm;
import fr.cnrs.opentheso.entites.PreferredTerm;
import fr.cnrs.opentheso.entites.ConceptGroupConcept;
import fr.cnrs.opentheso.models.candidats.CandidatDto;
import fr.cnrs.opentheso.models.candidats.TraductionDto;
import fr.cnrs.opentheso.models.candidats.MessageDto;
import fr.cnrs.opentheso.models.candidats.NodeCandidateOld;
import fr.cnrs.opentheso.models.candidats.NodeTraductionCandidat;
import fr.cnrs.opentheso.models.candidats.NodeProposition;
import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.repositories.*;
import fr.cnrs.opentheso.repositories.candidats.CandidatDao;
import fr.cnrs.opentheso.repositories.candidats.DomaineDao;
import fr.cnrs.opentheso.repositories.candidats.TermeDao;
import fr.cnrs.opentheso.repositories.candidats.NoteDao;
import fr.cnrs.opentheso.repositories.candidats.RelationDao;
import fr.cnrs.opentheso.models.candidats.enumeration.VoteType;
import fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost.Candidate;
import fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost.Element;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@AllArgsConstructor
public class CandidatService {

    private final NoteDao noteDao;
    private final CandidatDao candidatDao;
    private final DomaineDao domaineDao;
    private final TermHelper termHelper;
    private final ConceptHelper conceptHelper;
    private final TermeDao termeDao;
    private final AlignmentHelper alignmentHelper;
    private final RelationDao relationDao;

    private final ImageService imageService;
    private final StatusRepository statusRepository;
    private final ConceptRepository conceptRepository;
    private final PropositionRepository propositionRepository;
    private final CandidatVoteRepository candidatVoteRepository;
    private final CandidatStatusRepository candidatStatusRepository;
    private final CandidatMessageRepository candidatMessageRepository;
    private final ThesaurusRepository thesaurusRepository;
    private final NonPreferredTermRepository nonPreferredTermRepository;
    private final PreferredTermRepository preferredTermRepository;
    private final ConceptGroupConceptRepository conceptGroupConceptRepository;
    private final TermRepository termRepository;
    private final NoteRepository noteRepository;


    /**
     * Permet de récupérer la liste des candidats qui sont en attente
     */
    public List<CandidatDto> getCandidatsByStatus(String idThesaurus, String lang, int etat) {
        List<CandidatDto> candidatList = candidatDao.getCandidatsByStatus(idThesaurus, lang, etat);
        candidatList.forEach(candidatDto -> {
            var candidatMessages = candidatMessageRepository.findMessagesByConceptAndThesaurus(candidatDto.getIdConcepte(), idThesaurus);
            candidatDto.setNbrParticipant(CollectionUtils.isEmpty(candidatMessages) ? 0 : candidatMessages.size());

            var proposition = propositionRepository.findAllByIdConceptAndIdThesaurusOrderByCreated(candidatDto.getIdConcepte(), idThesaurus);
            candidatDto.setNbrDemande(CollectionUtils.isEmpty(proposition) ? 0 : proposition.size());

            var candidatVotes = candidatVoteRepository.findAllByIdConceptAndIdThesaurusAndTypeVote(candidatDto.getIdConcepte(), idThesaurus, VoteType.CANDIDAT.getLabel());
            candidatDto.setNbrVote(CollectionUtils.isEmpty(candidatVotes) ? 0 : candidatVotes.size());

            var noteVotes = candidatVoteRepository.findAllByIdConceptAndIdThesaurusAndTypeVote(candidatDto.getIdConcepte(), idThesaurus, VoteType.NOTE.getLabel());
            candidatDto.setNbrNoteVote(CollectionUtils.isEmpty(noteVotes) ? 0 : noteVotes.size());

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

            var candidatStatus = candidatStatusRepository.findAllByIdConceptAndIdThesaurus(candidatDto.getIdConcepte(), candidatDto.getIdThesaurus());
            candidatStatus.ifPresent(status -> candidatDto.setStatut(status.getStatus().getValue()));

            var candidatMessages = candidatMessageRepository.findMessagesByConceptAndThesaurus(candidatDto.getIdConcepte(), idThesaurus);
            candidatDto.setNbrParticipant(CollectionUtils.isEmpty(candidatMessages) ? 0 : candidatMessages.size());

            var proposition = propositionRepository.findAllByIdConceptAndIdThesaurusOrderByCreated(candidatDto.getIdConcepte(), idThesaurus);
            candidatDto.setNbrDemande(CollectionUtils.isEmpty(proposition) ? 0 : proposition.size());

            var candidatVotes = candidatVoteRepository.findAllByIdConceptAndIdThesaurusAndTypeVote(candidatDto.getIdConcepte(), idThesaurus, VoteType.CANDIDAT.getLabel());
            candidatDto.setNbrVote(CollectionUtils.isEmpty(candidatVotes) ? 0 : candidatVotes.size());

            var noteVotes = candidatVoteRepository.findAllByIdConceptAndIdThesaurusAndTypeVote(candidatDto.getIdConcepte(), idThesaurus, VoteType.NOTE.getLabel());
            candidatDto.setNbrNoteVote(CollectionUtils.isEmpty(noteVotes) ? 0 : noteVotes.size());
        });
        return temps;
    }

    public String saveNewCondidat(Concept concept) throws SQLException {

        var idNewCondidat = conceptHelper.addConceptInTable(concept, concept.getIdUser());
        var status = statusRepository.findById(1);
        candidatStatusRepository.save(CandidatStatus.builder()
                        .idConcept(idNewCondidat)
                        .idThesaurus(concept.getIdThesaurus())
                        .idUser(concept.getIdUser())
                        .date(new Date())
                        .status(status.orElse(null))
                .build());
        return idNewCondidat;
    }

    public boolean updateCandidatStatus(String idConcept, String idThesaurus, int statusId) {
        var candidatStatus = candidatStatusRepository.findAllByIdConceptAndIdThesaurus(idConcept, idThesaurus);
        if (candidatStatus.isPresent()) {
            var newStatus = statusRepository.findById(statusId);
            if (newStatus.isPresent()) {
                candidatStatus.get().setStatus(newStatus.get());
                candidatStatusRepository.save(candidatStatus.get());
                return true;
            }
        }
        return false;
    }

    public String saveNewTerm(Term term, String idConcept, int idUser) throws SQLException {

        return termHelper.addTerm(term, idConcept, idUser);
    }

    public void updateIntitule(String intitule, String idThesaurus, String lang, String idTerm) {
        termeDao.updateIntitule(intitule, idTerm, idThesaurus, lang);
    }

    public void updateDetailsCondidat(CandidatDto candidatSelected) {

        //update domaine
        domaineDao.deleteAllDomaine(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());
        
        for (NodeIdValue collection : candidatSelected.getCollections()) {
            domaineDao.addNewDomaine(collection.getId(), candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());
        }

        // gestion des relations
        relationDao.deleteAllRelations(candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus());

        //update terme générique
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesGenerique())) {
            candidatSelected.getTermesGenerique().forEach(nodeBT -> relationDao.addRelationBT(candidatSelected.getIdConcepte(), nodeBT.getId(), candidatSelected.getIdThesaurus()));
        }

        //update terme associés
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesAssocies())) {
            candidatSelected.getTermesAssocies().forEach(nodeRT -> relationDao.addRelationRT(candidatSelected.getIdConcepte(), nodeRT.getId(), candidatSelected.getIdThesaurus()));
        }

        // Employé pour
        termeDao.deleteEMByIdTermAndLang(candidatSelected.getIdTerm(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang());
        
        if(!candidatSelected.getEmployePourList().isEmpty()) {
            candidatSelected.getEmployePourList().forEach(employe -> termeDao.addNewEmployePour(employe, candidatSelected.getIdThesaurus(), candidatSelected.getLang(),
                    candidatSelected.getIdTerm()));
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

        candidatSelected.getNodeNotes().forEach(note -> note.setVoted(getVote(candidatSelected.getIdThesaurus(),
                candidatSelected.getIdConcepte(), candidatSelected.getUserId(), note.getIdNote()+"", VoteType.NOTE)));

        candidatSelected.setTraductions(termHelper.getTraductionsOfConcept(candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang()).stream().map(
                term -> new TraductionDto(term.getLang(),
                        term.getLexicalValue(), term.getCodePays())).collect(Collectors.toList()));

        var candidatMessages = candidatMessageRepository.findMessagesByConceptAndThesaurus(
                candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus());
        if (CollectionUtils.isNotEmpty(candidatMessages)) {
            candidatSelected.setMessages(candidatMessages.stream().map(element ->
                            MessageDto.builder()
                                    .msg(element.getValue())
                                    .nom(element.getUsername())
                                    .idUser(element.getIdUser())
                                    .mine(candidatSelected.getUserId() == element.getIdUser())
                                    .date(element.getDate())
                                    .build())
                    .toList());
        } else {
            candidatSelected.setMessages(List.of());
        }

        var votes = candidatVoteRepository.findAllByIdConceptAndIdThesaurusAndIdUserAndTypeVote(candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), candidatSelected.getUserId(), VoteType.CANDIDAT.getLabel());
        candidatSelected.setVoted(CollectionUtils.isNotEmpty(votes));

        candidatSelected.setAlignments(alignmentHelper.getAllAlignmentOfConcept(candidatSelected.getIdConcepte(), idThesaurus));
        candidatSelected.setImages(imageService.getAllExternalImages(candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte()));
    }


///////// ajouté par Miled
    public void addVote(String idThesaurus, String idConcept, int idUser, String idNote, VoteType voteType) {

        candidatVoteRepository.save(CandidatVote.builder()
                .idConcept(idConcept)
                .idThesaurus(idThesaurus)
                .idUser(idUser)
                .idNote(idNote)
                .typeVote(voteType.getLabel())
                .build());
    }

    public boolean getVote(String idThesaurus, String idConcept, int idUser, String idNote, VoteType voteType) {

        return CollectionUtils.isNotEmpty(candidatVoteRepository.findAllByIdConceptAndIdThesaurusAndIdUserAndIdNoteAndTypeVote(idConcept,
                idThesaurus, idUser, idNote, voteType.getLabel()));
    }

    public void removeVote(String idThesaurus, String idConcept, int idUser, String idNote, VoteType voteType) throws SQLException {

        candidatVoteRepository.deleteAllByIdUserAndIdConceptAndIdThesaurusAndTypeVoteAndIdNote(idUser, idConcept,
                idThesaurus, voteType.getLabel(), idNote);
    }
    
    public boolean insertCandidate(CandidatDto candidatDto, String adminMessage, int idUser) {

        var candidatStatus = candidatStatusRepository.findByIdConcept(candidatDto.getIdConcepte());
        if (candidatStatus.isPresent()) {
            candidatStatus.get().setStatus(statusRepository.findById(2).orElse(null));
            candidatStatus.get().setMessage(adminMessage);
            candidatStatus.get().setIdUserAdmin(idUser);
            candidatStatusRepository.save(candidatStatus.get());

            conceptRepository.setStatus("D", candidatDto.getIdConcepte(), candidatDto.getIdThesaurus());
            conceptRepository.setTopConceptTag(candidatDto.getTermesGenerique().isEmpty(), candidatDto.getIdConcepte(), candidatDto.getIdThesaurus());

            return true;
        }
        return false;
    }
    
    public boolean rejectCandidate(CandidatDto candidatDto, String adminMessage, int idUser) {
        var candidatStatus = candidatStatusRepository.findByIdConcept(candidatDto.getIdConcepte());
        if (candidatStatus.isPresent()) {
            candidatStatus.get().setStatus(statusRepository.findById(3).orElse(null));
            candidatStatus.get().setMessage(adminMessage);
            candidatStatus.get().setIdUserAdmin(idUser);
            candidatStatusRepository.save(candidatStatus.get());
            return true;
        }
        return false;
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

            var proposition = propositionRepository.findAllByIdConceptAndIdThesaurusOrderByCreated(nodeCandidateOld.getIdCandidate(), idTheso);
            if (CollectionUtils.isNotEmpty(proposition)) {
                nodeCandidateOld.setNodePropositions(proposition.stream()
                        .map(element-> NodeProposition.builder()
                                .note(element.getNote())
                                .idUser(element.getIdUser())
                                .build())
                        .toList());
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
                if (termHelper.isPrefLabelExist(
                        nodeTraduction.getTitle().trim(),
                        idTheso,
                        nodeTraduction.getIdLang())) {
                    messages.append("Candidat existe : ").append(nodeTraduction.getTitle());
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
                concept.setStatus("CA");

                conceptHelper.setNodePreference(nodePreference);

                try {
                    idNewConcept = saveNewCondidat(concept);
                } catch (SQLException e) {
                    messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                }
                if (idNewConcept == null) {
                    messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
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
                            continue;                            
                        }
                        first = false;
                    } else {
                        // ajout des traductions
                        if(!termHelper.addTraduction(nodeTraduction.getTitle(), idNewTerm, nodeTraduction.getIdLang(),
                                "candidat", "D", idTheso, idUser)) {
                            messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                        }
                    }
                }
                first = true;
                // ajout des messages  
                for (NodeProposition nodeProposition : nodeCandidateOld.getNodePropositions()) {
                    candidatMessageRepository.save(CandidatMessages.builder()
                            .value(nodeProposition.getNote())
                            .idUser(nodeProposition.getIdUser())
                            .idThesaurus(idTheso)
                            .idConcept(idNewConcept)
                            .date(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()))
                            .build());
                }
                
                idNewConcept = null;
                idNewTerm = null;                
            }
            exist = false;
        }

        return "Import réussi\n" + messages;
    }


    //Sauvegarde un candidat en base à partir d'un JSON
    public boolean saveCandidat(Candidate candidate, int userId) {

        var idTerm = String.valueOf(conceptRepository.getNextConceptNumericId());
        var idConcept = String.valueOf(conceptRepository.getNextConceptNumericId());

        if (CollectionUtils.isNotEmpty(candidate.getTerme())) {
            for (Element term : candidate.getTerme()) {
                termRepository.save(fr.cnrs.opentheso.entites.Term.builder()
                        .idTerm(idTerm)
                        .lexicalValue(term.getValue())
                        .lang(term.getLang())
                        .idThesaurus(candidate.getThesoId())
                        .created(new Date())
                        .status("D")
                        .source("candidat")
                        .creator(userId)
                        .build());
            }
        }

        log.error("Création du nouveau concept dans la base");
        var thesaurus = thesaurusRepository.findById(candidate.getThesoId());
        conceptRepository.save(fr.cnrs.opentheso.entites.Concept.builder()
                .idConcept(idConcept)
                .created(new Date())
                .status("CA")
                .conceptType("concept")
                .creator(userId)
                .topConcept(false)
                .thesaurus(thesaurus.get())
                .build());

        conceptGroupConceptRepository.save(ConceptGroupConcept.builder()
                .idGroup(candidate.getCollectionId())
                .idThesaurus(candidate.getThesoId())
                .idConcept(idConcept)
                .build());

        if (CollectionUtils.isNotEmpty(candidate.getDefinition())) {
            for (Element definition : candidate.getDefinition()) {
                insertNoteInCandidat("definition", candidate.getThesoId(), idTerm, definition.getLang(),
                        definition.getValue(), userId, candidate.getSource(), idConcept);
            }
        }

        if (CollectionUtils.isNotEmpty(candidate.getNote())) {
            for (Element note : candidate.getNote()) {
                insertNoteInCandidat("note", candidate.getThesoId(), idTerm, note.getLang(),
                        note.getValue(), userId, candidate.getSource(), idConcept);
            }
        }

        if (StringUtils.isNotEmpty(candidate.getComment())) {
            candidatMessageRepository.save(CandidatMessages.builder()
                    .value(candidate.getComment())
                    .idUser(userId)
                    .date(new SimpleDateFormat("yyyy-MM-dd mm:HH").format(new Date()))
                    .idConcept(idConcept)
                    .idThesaurus(candidate.getThesoId())
                    .build());
        }

        if (CollectionUtils.isNotEmpty(candidate.getSynonymes())) {
            for (Element synonyme : candidate.getSynonymes()) {
                nonPreferredTermRepository.save(NonPreferredTerm.builder()
                        .lexicalValue(synonyme.getValue())
                        .lang(synonyme.getLang())
                        .idThesaurus(candidate.getThesoId())
                        .idTerm(idTerm)
                        .build());
            }
        }

        if (StringUtils.isNotEmpty(candidate.getConceptGenericId())) {
            relationDao.addRelationBT(idConcept, candidate.getConceptGenericId(), candidate.getThesoId());
        }

        preferredTermRepository.save(PreferredTerm.builder()
                .idConcept(idConcept)
                .idTerm(idTerm)
                .idThesaurus(candidate.getThesoId())
                .build());

        var cadiadatStatus = statusRepository.findById(1);
        cadiadatStatus.ifPresent(status -> candidatStatusRepository.save(CandidatStatus.builder()
                .idConcept(idConcept)
                .status(status)
                .date(new Date(System.currentTimeMillis()))
                .idUser(userId)
                .idThesaurus(candidate.getThesoId())
                .build()));

        return true;
    }

    private void insertNoteInCandidat(String type, String thesoId, String idTerm, String lang, String description,
                                      int userId, String source, String idConcept) {

        noteRepository.save(Note.builder()
                .notetypecode(type)
                .idThesaurus(thesoId)
                .idTerm(idTerm)
                .lang(lang)
                .lexicalvalue(description)
                .created(new Date())
                .modified(new Date())
                .idUser(userId)
                .notesource(source)
                .idConcept(idConcept)
                .build());
    }
}
