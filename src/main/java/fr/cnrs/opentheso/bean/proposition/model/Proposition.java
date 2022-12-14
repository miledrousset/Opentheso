package fr.cnrs.opentheso.bean.proposition.model;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
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
    private List<SynonymPropBean> synonymsPropV2;
    private List<TraductionPropBean> traductionsProp;

    private List<NotePropBean> notes, scopeNotes, historyNotes, examples,
            editorialNotes, definitions, changeNotes;

    
    public Proposition() {
        synonymsProp = new ArrayList<>();
        synonymsPropV2 = new ArrayList<>();
        traductionsProp = new ArrayList<>();
        notes = new ArrayList<>();
        scopeNotes = new ArrayList<>();
        historyNotes = new ArrayList<>();
        examples = new ArrayList<>();
        editorialNotes = new ArrayList<>();
        definitions = new ArrayList<>();
        changeNotes = new ArrayList<>();
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

    public List<NotePropBean> getScopeNotes() {
        return scopeNotes;
    }

    public void setScopeNotes(List<NotePropBean> scopeNotes) {
        this.scopeNotes = scopeNotes;
    }

    public List<NotePropBean> getHistoryNotes() {
        return historyNotes;
    }

    public void setHistoryNotes(List<NotePropBean> historyNotes) {
        this.historyNotes = historyNotes;
    }

    public List<NotePropBean> getExamples() {
        return examples;
    }

    public void setExamples(List<NotePropBean> examples) {
        this.examples = examples;
    }

    public List<NotePropBean> getEditorialNotes() {
        return editorialNotes;
    }

    public void setEditorialNotes(List<NotePropBean> editorialNotes) {
        this.editorialNotes = editorialNotes;
    }

    public List<NotePropBean> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<NotePropBean> definitions) {
        this.definitions = definitions;
    }

    public List<NotePropBean> getChangeNotes() {
        return changeNotes;
    }

    public void setChangeNotes(List<NotePropBean> changeNotes) {
        this.changeNotes = changeNotes;
    }

    public List<NotePropBean> getNotes() {
        return notes;
    }

    public void setNotes(List<NotePropBean> notes) {
        this.notes = notes;
    }
    
    public List<SynonymPropBean> getSynonymsPropV2() {
        return synonymsPropV2;
    }

    public void setSynonymsPropV2(List<SynonymPropBean> synonymsPropV2) {
        this.synonymsPropV2 = synonymsPropV2;
    }
}
