package fr.cnrs.opentheso.bean.proposition.dao;

import java.io.Serializable;


public class PropositionDetailDao implements Serializable {
    
    private Integer id;
    
    private Integer idProposition;

    private String categorie;

    private String value;
    
    private String oldValue;

    private String action;
    
    private String lang;
    
    private String idTerm;
    
    private String status;
    
    private boolean hiden;
    

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdProposition() {
        return idProposition;
    }

    public void setIdProposition(Integer idProposition) {
        this.idProposition = idProposition;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public boolean isHiden() {
        return hiden;
    }

    public void setHiden(boolean hiden) {
        this.hiden = hiden;
    }

    public String getIdTerm() {
        return idTerm;
    }

    public void setIdTerm(String idTerm) {
        this.idTerm = idTerm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
}
