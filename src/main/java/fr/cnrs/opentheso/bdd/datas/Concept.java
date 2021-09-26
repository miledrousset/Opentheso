package fr.cnrs.opentheso.bdd.datas;

import java.util.Date;

public class Concept {

    private String idConcept;
    private String idThesaurus;
    private String idArk = "";
    private String idHandle = "";
    private String idDoi = "";    
    private Date created;
    private Date modified;
    private String status;
    private String notation;
    private boolean topConcept;
    private String idGroup;
    private String userName;
    private int idUser;
    private String lang;
    private boolean isDeprecated;
    
    private int creator;
    private int contributor;
    private String creatorName;
    private String contributorName;    
    

    public Concept(String idConcept, String status, String notation, String idThesaurus, String idGroup,
            boolean topConcept) {
        super();
        this.idConcept = idConcept;
        this.status = status;
        this.notation = notation;
        this.idThesaurus = idThesaurus;
        this.idGroup = idGroup;
        this.topConcept = topConcept;
        this.notation = "";
    }

    public Concept() {
        super();
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public boolean isTopConcept() {
        return topConcept;
    }

    public void setTopConcept(boolean topConcept) {
        this.topConcept = topConcept;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getIdArk() {
        return idArk;
    }

    public void setIdArk(String idArk) {
        this.idArk = idArk;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdHandle() {
        return idHandle;
    }

    public void setIdHandle(String idHandle) {
        this.idHandle = idHandle;
    }

    public String getIdDoi() {
        return idDoi;
    }

    public void setIdDoi(String idDoi) {
        this.idDoi = idDoi;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isIsDeprecated() {
        return isDeprecated;
    }

    public void setIsDeprecated(boolean isDeprecated) {
        this.isDeprecated = isDeprecated;
    }

    public int getCreator() {
        return creator;
    }

    public void setCreator(int creator) {
        this.creator = creator;
    }

    public int getContributor() {
        return contributor;
    }

    public void setContributor(int contributor) {
        this.contributor = contributor;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getContributorName() {
        return contributorName;
    }

    public void setContributorName(String contributorName) {
        this.contributorName = contributorName;
    }
    
    
}
