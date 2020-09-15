package fr.cnrs.opentheso.skosapi;

public class SKOSStatus {
    private String idConcept;
    private String idStatus;
    private String idUser;
    private String idThesaurus;
    private String date;
    private String message;

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(String idStatus) {
        this.idStatus = idStatus;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}