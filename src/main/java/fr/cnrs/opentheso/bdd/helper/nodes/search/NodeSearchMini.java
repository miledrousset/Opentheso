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
public class NodeSearchMini {
    private String idConcept;
    private String idTerm;
    private String prefLabel;
    private String altLabel;
    private boolean isConcept;
    private boolean isAltLabel;
    private boolean isGroup;
    private boolean isFacet;
    private boolean isDeprecated;
    
    private String conceptType;

    
    public NodeSearchMini() {
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(String altLabel) {
        this.altLabel = altLabel;
    }

    public boolean isIsAltLabel() {
        return isAltLabel;
    }

    public void setIsAltLabel(boolean isAltLabel) {
        this.isAltLabel = isAltLabel;
    }

    public boolean isIsGroup() {
        return isGroup;
    }

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public String getIdTerm() {
        return idTerm;
    }

    public void setIdTerm(String idTerm) {
        this.idTerm = idTerm;
    }

    public boolean isIsFacet() {
        return isFacet;
    }

    public void setIsFacet(boolean isFacet) {
        this.isFacet = isFacet;
    }

    public boolean isIsConcept() {
        return isConcept;
    }

    public void setIsConcept(boolean isConcept) {
        this.isConcept = isConcept;
    }

    public boolean isIsDeprecated() {
        return isDeprecated;
    }

    public void setIsDeprecated(boolean isDeprecated) {
        this.isDeprecated = isDeprecated;
    }

    public String getConceptType() {
        return conceptType;
    }

    public void setConceptType(String conceptType) {
        this.conceptType = conceptType;
    }



}
