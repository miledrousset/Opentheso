/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author miledrousset
 */
public class NodeCorpus {
    private String corpusName;
    private String uriCount;
    private String uriLink;
    private boolean isOnlyUriLink;
    
    private boolean active;
    
    
    private int count;
            
    public NodeCorpus() {
        count = -1;
    }

    public String getCorpusName() {
        return corpusName;
    }

    public void setCorpusName(String corpusName) {
        this.corpusName = corpusName;
    }

    public String getUriCount() {
        return uriCount;
    }

    public void setUriCount(String uriCount) {
        this.uriCount = uriCount;
    }

    public String getUriLink() {
        return uriLink;
    }

    public void setUriLink(String uriLink) {
        this.uriLink = uriLink;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isIsOnlyUriLink() {
        return isOnlyUriLink;
    }

    public void setIsOnlyUriLink(boolean isOnlyUriLink) {
        this.isOnlyUriLink = isOnlyUriLink;
    }

   
    
}
