package fr.cnrs.opentheso.bean.candidat.dto;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CandidatDto implements Serializable {

    private String idTerm;
    private String idConcepte;
    private String idThesaurus;
    private String lang;
    private String adminMessage;
    
    private String createdBy;
    private int createdById;
    
    private String user;
    private int userId;

    private String domaine;
    private String nomPref;
    private Date creationDate;
    private String statut;
    private int nbrDemande;
    private int nbrParticipant;
    private boolean voted;
    private boolean noteVoted;
    private int nbrVote;
    private int nbrNoteVote;

    private String noteApplication;
    private List<String> defenitions;
    private List<String> participants;
    private List<TraductionDto> traductions;
    private List<CorpusDto> corpus;
    private List<MessageDto> messages;
    
    
    // ajoutés par Miled 
    private ArrayList<NodeIdValue> collections;
    private List<NodeNote> nodeNotes;    
    
    //    private List<String> termesGenerique;
    private ArrayList<NodeIdValue> termesGenerique;  
    //private List<String> termesAssocies;    
    private ArrayList<NodeIdValue> termesAssocies;   
    
    //private List<String> employePour;    
    private String employePour;    
    
    
    public CandidatDto() {
        corpus = new ArrayList<>();
        messages = new ArrayList<>();
        traductions = new ArrayList<>();
        defenitions = new ArrayList<>();
        termesGenerique = new ArrayList<>();
        termesAssocies = new ArrayList<>();
        employePour = "";
        participants = new ArrayList<>();
        collections = new ArrayList<>();
        adminMessage = "";
    }

    public CandidatDto(String nomPref) {
        idConcepte = null;
        this.nomPref = nomPref;
    }

    public CandidatDto(CandidatDto source) {
        this.idTerm = source.getIdTerm();
        this.idConcepte = source.getIdConcepte();
        this.idThesaurus = source.getIdThesaurus();
        this.userId = source.getUserId();
        this.user = source.getUser();
        this.nomPref = source.getNomPref();
        this.collections = source.getCollections();
        this.termesGenerique = source.getTermesGenerique();
        this.defenitions = source.getDefenitions();
        this.noteApplication = source.getNoteApplication();
        this.termesAssocies = source.getTermesAssocies();
        this.corpus = source.getCorpus();
        this.messages = source.getMessages();
        this.traductions = source.getTraductions();
        this.adminMessage = source.getAdminMessage();
        this.domaine = source.getDomaine();
    }

    public String getDomaine() {
        return domaine;
    }

    public void setDomaine(String domaine) {
        this.domaine = domaine;
    }

    public ArrayList<NodeIdValue> getCollections() {
        return collections;
    }

    public void setCollections(ArrayList<NodeIdValue> collections) {
        this.collections = collections;
    }


    public List<String> getDefenitions() {
        return defenitions;
    }

    public void setDefenitions(List<String> defenitions) {
        this.defenitions = defenitions;
    }

    public String getNoteApplication() {
        return noteApplication;
    }

    public void setNoteApplication(String noteApplication) {
        this.noteApplication = noteApplication;
    }

    public List<TraductionDto> getTraductions() {
        return traductions;
    }

    public void setTraductions(List<TraductionDto> traductions) {
        this.traductions = traductions;
    }

    public List<CorpusDto> getCorpus() {
        return corpus;
    }

    public void setCorpus(List<CorpusDto> corpus) {
        this.corpus = corpus;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }

    public String getNomPref() {
        return nomPref;
    }

    public void setNomPref(String nomPref) {
        this.nomPref = nomPref;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getNbrDemande() {
        return nbrDemande;
    }

    public void setNbrDemande(int nbrDemande) {
        this.nbrDemande = nbrDemande;
    }

    public int getNbrParticipant() {
        return nbrParticipant;
    }

    public void setNbrParticipant(int nbrParticipant) {
        this.nbrParticipant = nbrParticipant;
    }

    public String getIdTerm() {
        return idTerm;
    }

    public void setIdTerm(String idTerm) {
        this.idTerm = idTerm;
    }

    public String getIdConcepte() {
        return idConcepte;
    }

    public void setIdConcepte(String idConcepte) {
        this.idConcepte = idConcepte;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
///// ajoutés par Miled

    public ArrayList<NodeIdValue> getTermesGenerique() {
        return termesGenerique;
    }

    public void setTermesGenerique(ArrayList<NodeIdValue> termesGenerique) {
        this.termesGenerique = termesGenerique;
    }

    public ArrayList<NodeIdValue> getTermesAssocies() {
        return termesAssocies;
    }

    public void setTermesAssocies(ArrayList<NodeIdValue> termesAssocies) {
        this.termesAssocies = termesAssocies;
    }


    public String getEmployePour() {
        return employePour;
    }

    public void setEmployePour(String employePour) {
        this.employePour = employePour;
    }

    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }

    public int getNbrVote() {
        return nbrVote;
    }

    public void setNbrVote(int nbrVote) {
        this.nbrVote = nbrVote;
    }

    public List<NodeNote> getNodeNotes() {
        return nodeNotes;
    }

    public void setNodeNotes(List<NodeNote> nodeNotes) {
        this.nodeNotes = nodeNotes;
    }

    public boolean isNoteVoted() {
        return noteVoted;
    }

    public void setNoteVoted(boolean noteVoted) {
        this.noteVoted = noteVoted;
    }

    public int getNbrNoteVote() {
        return nbrNoteVote;
    }

    public void setNbrNoteVote(int nbrNoteVote) {
        this.nbrNoteVote = nbrNoteVote;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getCreatedById() {
        return createdById;
    }

    public void setCreatedById(int createdById) {
        this.createdById = createdById;
    }

    public String getAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }
    
}
