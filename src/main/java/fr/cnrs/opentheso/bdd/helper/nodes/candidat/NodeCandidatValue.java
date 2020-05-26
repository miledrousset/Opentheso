package fr.cnrs.opentheso.bdd.helper.nodes.candidat;

import java.io.Serializable;

public class NodeCandidatValue implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private String idConcept;
    private String value;
    private int nbProp;
    
    // etat : v=validé, a=attente,r=refusé,i=inserré 
    private String etat;

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getNbProp() {
        return nbProp;
    }

    public void setNbProp(int nbProp) {
        this.nbProp = nbProp;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
}
