package fr.cnrs.opentheso.bean.proposition.model;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import java.io.Serializable;
import java.util.List;


public class Proposition implements Serializable {
    
    private String conceptID;
    
    private String nomConceptProp;
    private Term nomConcept;
    
    private List<SynonymPropBean> synonymsProp;


    public Term getNomConcept() {
        return nomConcept;
    }

    public void setNomConcept(Term nomConcept) {
        this.nomConcept = nomConcept;
    }

    public List<SynonymPropBean> getSynonymsProp() {
        return synonymsProp;
    }

    public void setSynonymsProp(List<SynonymPropBean> synonymsProp) {
        this.synonymsProp = synonymsProp;
    }

    public String getNomConceptProp() {
        return nomConceptProp;
    }

    public void setNomConceptProp(String nomConceptProp) {
        this.nomConceptProp = nomConceptProp;
    }

    public String getConceptID() {
        return conceptID;
    }

    public void setConceptID(String conceptID) {
        this.conceptID = conceptID;
    }
    
}
