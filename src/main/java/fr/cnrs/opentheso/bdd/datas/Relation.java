package fr.cnrs.opentheso.bdd.datas;

import java.sql.Date;

public class Relation {

    private String id_concept1;
    private String id_relation;
    private String id_concept2;
    private String id_thesaurus;
    private Date modified;
    private String idUser;
    private String action;

    public Relation() {

    }

    /**
     * @param id_terme1
     * @param id_relation
     * @param id_terme2
     * @param id_thesaurus      *
     */
    public Relation(String id_terme1,
            String id_relation, String id_terme2,
            String id_thesaurus) {
        this.id_concept1 = id_terme1;
        this.id_relation = id_relation;
        this.id_concept2 = id_terme2;
        this.id_thesaurus = id_thesaurus;
    }

    public String getId_concept1() {
        return id_concept1;
    }

    public void setId_concept1(String idTerme1) {
        id_concept1 = idTerme1;
    }

    public String getId_relation() {
        return id_relation;
    }

    public void setId_relation(String idRelation) {
        id_relation = idRelation;
    }

    public String getId_concept2() {
        return id_concept2;
    }

    public void setId_concept2(String idTerme2) {
        id_concept2 = idTerme2;
    }

    public String getId_thesaurus() {
        return id_thesaurus;
    }

    public void setId_thesaurus(String idThesaurus) {
        id_thesaurus = idThesaurus;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
