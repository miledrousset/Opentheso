package fr.cnrs.opentheso.bean.toolbox.statistique;


public class GenericStatistiqueData {
    
    private String idCollection;
    private String collection;
    private int conceptsNbr;
    private int synonymesNbr;
    private int termesNonTraduitsNbr;
    private int notesNbr;
    private int wikidataAlignNbr;
    private int totalAlignment;    

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public int getConceptsNbr() {
        return conceptsNbr;
    }

    public void setConceptsNbr(int conceptsNbr) {
        this.conceptsNbr = conceptsNbr;
    }

    public int getSynonymesNbr() {
        return synonymesNbr;
    }

    public void setSynonymesNbr(int synonymesNbr) {
        this.synonymesNbr = synonymesNbr;
    }

    public int getTermesNonTraduitsNbr() {
        return termesNonTraduitsNbr;
    } 

    public void setTermesNonTraduitsNbr(int termesNonTraduitsNbr) {
        this.termesNonTraduitsNbr = termesNonTraduitsNbr;
    }

    public int getNotesNbr() {
        return notesNbr;
    }

    public void setNotesNbr(int notesNbr) {
        this.notesNbr = notesNbr;
    }

    public String getIdCollection() {
        return idCollection;
    }

    public void setIdCollection(String idCollection) {
        this.idCollection = idCollection;
    }

    public int getWikidataAlignNbr() {
        return wikidataAlignNbr;
    }

    public void setWikidataAlignNbr(int wikidataAlignNbr) {
        this.wikidataAlignNbr = wikidataAlignNbr;
    }

    public int getTotalAlignment() {
        return totalAlignment;
    }

    public void setTotalAlignment(int totalAlignment) {
        this.totalAlignment = totalAlignment;
    }

}