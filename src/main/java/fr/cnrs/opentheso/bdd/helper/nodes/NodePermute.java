package fr.cnrs.opentheso.bdd.helper.nodes;

import java.io.Serializable;

public class NodePermute implements Serializable {
    private String firstColumn;
    private String searchedValue;
    private String lastColumn;
    private String idThesaurus;
    private String idConcept;
    private String idGroup;
    private String idLang;
    private int indexOfValue;
    private boolean isPreferredTerm = false;

    public NodePermute() {
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getIdLang() {
        return idLang;
    }

    public void setIdLang(String idLang) {
        this.idLang = idLang;
    }

    public String getFirstColumn() {
        return firstColumn;
    }

    public void setFirstColumn(String firstColumn) {
        this.firstColumn = firstColumn;
    }

    public String getSearchedValue() {
        return searchedValue;
    }

    public void setSearchedValue(String searchedValue) {
        this.searchedValue = searchedValue;
    }

    public String getLastColumn() {
        return lastColumn;
    }

    public void setLastColumn(String lastColumn) {
        this.lastColumn = lastColumn;
    }

    public int getIndexOfValue() {
        return indexOfValue;
    }

    public void setIndexOfValue(int indexOfValue) {
        this.indexOfValue = indexOfValue;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public boolean isIsPreferredTerm() {
        return isPreferredTerm;
    }

    public void setIsPreferredTerm(boolean isPreferredTerm) {
        this.isPreferredTerm = isPreferredTerm;
    }

    
    
}
