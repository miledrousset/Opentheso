/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.cnrs.opentheso.bdd.helper.nodes.search;

/**
 *
 * @author miled.rousset
 */
public class NodeSearch {
    private String lexical_value;
    private String idConcept;
    private String idTerm;
    private String idGroup;
    private String typeGroup;
    private String groupLabel;
    private String idLang;
    private String idThesaurus;
    private boolean topConcept;
    private boolean preferredLabel;

    public NodeSearch() {
    }

    public String getLexical_value() {
        return lexical_value;
    }

    public void setLexical_value(String lexical_value) {
        this.lexical_value = lexical_value;
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getIdTerm() {
        return idTerm;
    }

    public void setIdTerm(String idTerm) {
        this.idTerm = idTerm;
    }

    public String getIdLang() {
        return idLang;
    }

    public void setIdLang(String idLang) {
        this.idLang = idLang;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public boolean isTopConcept() {
        return topConcept;
    }

    public void setTopConcept(boolean topConcept) {
        this.topConcept = topConcept;
    }

    public boolean isPreferredLabel() {
        return preferredLabel;
    }

    public void setPreferredLabel(boolean preferredLabel) {
        this.preferredLabel = preferredLabel;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public void setGroupLabel(String groupLabel) {
        this.groupLabel = groupLabel;
    }

    public String getTypeGroup() {
        return typeGroup;
    }

    public void setTypeGroup(String typeGroup) {
        this.typeGroup = typeGroup;
    }
    
    

}
