package fr.cnrs.opentheso.bean.proposition.dao;

import fr.cnrs.opentheso.bean.proposition.model.PropositionStatusEnum;
import java.io.Serializable;


public class PropositionDao implements Serializable {
    
    private Integer id;
    
    private String idTheso;
    
    private String thesoName;
    
    private String idConcept;
    
    private String lang;
    
    private String nomConcept;
    
    private String nom;
    
    private String email;
    
    private String commentaire;
    
    private String status;
    
    private String datePublication;
    
    private String dateUpdate;
    
    private String userAction;
    
    private String codeDrapeau;
    
    private String adminComment;
    
    public boolean isEnvoyer() {
        return PropositionStatusEnum.ENVOYER.name().equals(status);
    }
    
    public boolean isLu() {
        return PropositionStatusEnum.LU.name().equals(status);
    }
    
    public boolean isApprouver() {
        return PropositionStatusEnum.APPROUVER.name().equals(status);
    }
    
    public boolean isRefuser() {
        return PropositionStatusEnum.REFUSER.name().equals(status);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdTheso() {
        return idTheso;
    }

    public void setIdTheso(String idTheso) {
        this.idTheso = idTheso;
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(String datePublication) {
        this.datePublication = datePublication;
    }

    public String getDateUpdate() {
        return dateUpdate;
    }

    public void setDateUpdate(String dateUpdate) {
        this.dateUpdate = dateUpdate;
    }

    public String getUserAction() {
        return userAction;
    }

    public void setUserAction(String userAction) {
        this.userAction = userAction;
    }

    public String getNomConcept() {
        return nomConcept;
    }

    public void setNomConcept(String nomConcept) {
        this.nomConcept = nomConcept;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCodeDrapeau() {
        return codeDrapeau;
    }

    public void setCodeDrapeau(String codeDrapeau) {
        this.codeDrapeau = codeDrapeau;
    }

    public String getThesoName() {
        return thesoName;
    }

    public void setThesoName(String thesoName) {
        this.thesoName = thesoName;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
    
}
