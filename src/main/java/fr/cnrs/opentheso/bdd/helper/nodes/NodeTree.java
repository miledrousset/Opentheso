package fr.cnrs.opentheso.bdd.helper.nodes;

import java.util.ArrayList;
import java.util.List;


public class NodeTree {

    private String idConcept;
    private String preferredTerm;
    private String idParent;
    private List<NodeTree> childrens = new ArrayList<NodeTree>();

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getPreferredTerm() {
        return preferredTerm;
    }

    public void setPreferredTerm(String preferredTerm) {
        this.preferredTerm = preferredTerm;
    }

    public List<NodeTree> getChildrens() {
        return childrens;
    }

    public void setChildrens(List<NodeTree> childrens) {
        this.childrens = childrens;
    }

    public String getIdParent() {
        return idParent;
    }

    public void setIdParent(String idParent) {
        this.idParent = idParent;
    }
    
}
