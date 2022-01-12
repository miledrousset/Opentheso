package fr.cnrs.opentheso.bean.alignment;

import java.util.List;

public class ResultatAlignement {

    private String title;
    private String url;
    private List<String> broarder;
    private List<String> narrowers;
    private String related;
    private String note;
    private String hierarchy;
    private List<String> terms;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getBroarder() {
        return broarder;
    }

    public void setBroarder(List<String> broarder) {
        this.broarder = broarder;
    }

    public List<String> getNarrowers() {
        return narrowers;
    }

    public void setNarrowers(List<String> narrowers) {
        this.narrowers = narrowers;
    }

    public String getRelated() {
        return related;
    }

    public void setRelated(String related) {
        this.related = related;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(String hierarchy) {
        this.hierarchy = hierarchy;
    }

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }
    
    
}
