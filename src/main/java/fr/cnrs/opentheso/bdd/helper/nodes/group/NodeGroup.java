package fr.cnrs.opentheso.bdd.helper.nodes.group;

import java.io.Serializable;
import java.sql.Date;
import fr.cnrs.opentheso.bdd.datas.ConceptGroup;

public class NodeGroup implements Serializable, Comparable {
    
    private static final long serialVersionUID = 1L;

    private ConceptGroup conceptGroup;
    private String lexicalValue;
    private String idLang;
    private Date created;
    private Date modified;
    private String idUser;
    private int orde;
    private String original_value;
    private boolean  ispreferredterm;
    private String notation;
    private boolean isHaveChildren = false;

    public NodeGroup() {
        conceptGroup = new ConceptGroup();
    }

    public ConceptGroup getConceptGroup() {
        return conceptGroup;
    }

    public void setConceptGroup(ConceptGroup conceptGroup) {
        this.conceptGroup = conceptGroup;
    }

    public String getLexicalValue() {
        return lexicalValue;
    }

    public void setLexicalValue(String lexicalValue) {
        this.lexicalValue = lexicalValue;
    }

    public String getIdLang() {
        return idLang;
    }

    public void setIdLang(String idLang) {
        this.idLang = idLang;
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

    @Override
    public int compareTo(Object o) {
        return this.lexicalValue.compareTo(((NodeGroup)o).lexicalValue);
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public int getOrde() {
        return orde;
    }

    public void setOrde(int orde) {
        this.orde = orde;
    }

    public String getOriginal_value() {
        return original_value;
    }

    public void setOriginal_value(String original_value) {
        this.original_value = original_value;
    }

    public boolean isIspreferredterm() {
        return ispreferredterm;
    }

    public void setIspreferredterm(boolean ispreferredterm) {
        this.ispreferredterm = ispreferredterm;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public boolean isIsHaveChildren() {
        return isHaveChildren;
    }

    public void setIsHaveChildren(boolean isHaveChildren) {
        this.isHaveChildren = isHaveChildren;
    }


}
