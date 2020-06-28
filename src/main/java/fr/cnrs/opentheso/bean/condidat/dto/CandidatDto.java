package fr.cnrs.opentheso.bean.condidat.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class CandidatDto implements Serializable {

    private String idTerm;
    private String idConcepte;
    private String idThesaurus;
    private String lang;
    private String user;
    private int userId;

    private String nomPref;
    private Date creationDate;
    private String statut;
    private int nbrDemande;
    private int nbrParticipant;

    private int domaine_id;
    private String termeGenerique_id;
    private String defenition;
    private String noteApplication;

    private List<String> termesAssocies;
    private List<String> employePour;
    private List<String> participants;

    private List<TraductionDto> traductions;
    private List<CorpusDto> corpus;
    private List<MessageDto> messages;

    public CandidatDto() {
        corpus = new ArrayList<>();
        messages = new ArrayList<>();
        traductions = new ArrayList<>();
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
        this.domaine_id = source.getDomaine_id();
        this.termeGenerique_id = source.getTermeGenerique_id();
        this.defenition = source.getDefenition();
        this.noteApplication = source.getNoteApplication();
        this.termesAssocies = source.getTermesAssocies();
        this.corpus = source.getCorpus();
        this.messages = source.getMessages();
        this.traductions = source.getTraductions();
    }

    public int getDomaine_id() {
        return domaine_id;
    }

    public void setDomaine_id(int domaine_id) {
        this.domaine_id = domaine_id;
    }

    public String getTermeGenerique_id() {
        return termeGenerique_id;
    }

    public void setTermeGenerique_id(String termeGenerique_id) {
        this.termeGenerique_id = termeGenerique_id;
    }

    public String getDefenition() {
        return defenition;
    }

    public void setDefenition(String defenition) {
        this.defenition = defenition;
    }

    public String getNoteApplication() {
        return noteApplication;
    }

    public void setNoteApplication(String noteApplication) {
        this.noteApplication = noteApplication;
    }

    public List<String> getTermesAssocies() {
        return termesAssocies;
    }

    public void setTermesAssocies(List<String> termesAssocies) {
        this.termesAssocies = termesAssocies;
    }

    public List<String> getEmployePour() {
        return employePour;
    }

    public void setEmployePour(List<String> employePour) {
        this.employePour = employePour;
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
}
