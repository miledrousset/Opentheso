/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.cnrs.opentheso.bdd.helper.nodes.search;

import java.util.ArrayList;

/**
 *
 * @author miled.rousset
 */
public class NodeSearchFull {
    private String idConcept;
    private String prefLabel;
    private ArrayList<String> altLabel;    
    private boolean isAltLabel;
    private ArrayList<String> BT;    
    private ArrayList<String> NT;
    private ArrayList<String> RT;

    private ArrayList<String> groups;
    private ArrayList<String> langs;    
    
    private String internalUri;

    public NodeSearchFull() {
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

    public ArrayList<String> getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(ArrayList<String> altLabel) {
        this.altLabel = altLabel;
    }

    public boolean isIsAltLabel() {
        return isAltLabel;
    }

    public void setIsAltLabel(boolean isAltLabel) {
        this.isAltLabel = isAltLabel;
    }

    public ArrayList<String> getBT() {
        return BT;
    }

    public void setBT(ArrayList<String> BT) {
        this.BT = BT;
    }

    public ArrayList<String> getNT() {
        return NT;
    }

    public void setNT(ArrayList<String> NT) {
        this.NT = NT;
    }

    public ArrayList<String> getRT() {
        return RT;
    }

    public void setRT(ArrayList<String> RT) {
        this.RT = RT;
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }

    public ArrayList<String> getLangs() {
        return langs;
    }

    public void setLangs(ArrayList<String> langs) {
        this.langs = langs;
    }

    public String getInternalUri() {
        return internalUri;
    }

    public void setInternalUri(String internalUri) {
        this.internalUri = internalUri;
    }


}
