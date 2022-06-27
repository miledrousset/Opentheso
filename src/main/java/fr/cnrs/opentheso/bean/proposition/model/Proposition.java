package fr.cnrs.opentheso.bean.proposition.model;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.bean.proposition.TraductionPropBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Proposition implements Serializable {
    
    private String conceptID;
    
    private boolean updateNomConcept;
    private String nomConceptProp;
    private Term nomConcept;
    
    private List<SynonymPropBean> synonymsProp;
    
    private List<TraductionPropBean> traductionsProp;
    
    
    public Proposition() {
        synonymsProp = new ArrayList<>();
        traductionsProp = new ArrayList<>();
    }


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

    public boolean isUpdateNomConcept() {
        return updateNomConcept;
    }

    public void setUpdateNomConcept(boolean updateNomConcept) {
        this.updateNomConcept = updateNomConcept;
    }

    public List<TraductionPropBean> getTraductionsProp() {
        return traductionsProp;
    }

    public void setTraductionsProp(List<TraductionPropBean> traductionsProp) {
        this.traductionsProp = traductionsProp;
    }
    
}
