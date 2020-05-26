/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes.thesaurus;

import java.util.ArrayList;

/**
 *
 * @author miled.rousset
 */
public class NodeThesaurusMini {

    private String idThesaurus;
    private ArrayList<NodeThesaurusTraduction> nodeTraduction = new ArrayList<NodeThesaurusTraduction>();

    public NodeThesaurusMini() {
        super();
    }

    public NodeThesaurusMini(String idThesaurus,
            ArrayList<NodeThesaurusTraduction> nodeTraduction) {
        super();
        this.idThesaurus = idThesaurus;
        this.nodeTraduction = nodeTraduction;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public ArrayList<NodeThesaurusTraduction> getNodeTraduction() {
        return nodeTraduction;
    }

    public void setNodeTraduction(ArrayList<NodeThesaurusTraduction> nodeTraduction) {
        this.nodeTraduction = nodeTraduction;
    }

}
