/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.models.exports.tabulate;

import java.util.ArrayList;
import java.util.Date;

/**
 * Cette classe permet de stocker les données d'un thésaurus provenant d'un fichier tabulé
 * 
 * @author miled.rousset
 */
public class TabulateDocument {

    private String id;
    private String idArk;
    private String idHandle;
    private String uri;
    
    // type du concept (MT = domaine ou microthésaurus, DE = descripteur)
    private String type;
    
    private ArrayList<Label> prefLabels;
    private ArrayList<Label> altLabels;
    
    
    private ArrayList<String> inScheme;
    private ArrayList<String> broader;
    private ArrayList<String> narrower;
    private ArrayList<String> related;

   
    private ArrayList<Alignment> alignments;    
    
    private ArrayList<Note> note;    
    private ArrayList<Note> definition;
    private ArrayList<Note> scopeNote;
    private ArrayList<Note> historyNote;
    private ArrayList<Note> editorialNote;

    private Date created;
    private Date modified;
    
    
    public TabulateDocument() {
        prefLabels = new ArrayList<>();
        altLabels = new ArrayList<>();


        inScheme = new ArrayList<>();
        broader = new ArrayList<>();
        narrower = new ArrayList<>();
        related = new ArrayList<>();


        alignments = new ArrayList<>();    

        note = new ArrayList<>();
        definition = new ArrayList<>();
        scopeNote = new ArrayList<>();
        historyNote = new ArrayList<>();
        editorialNote = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdArk() {
        return idArk;
    }

    public void setIdArk(String idArk) {
        this.idArk = idArk;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<Label> getPrefLabels() {
        return prefLabels;
    }

    public void setPrefLabels(ArrayList<Label> prefLabels) {
        this.prefLabels = prefLabels;
    }
    
    public void addPrefLabel(String label, String lang) {
        Label label_lang = new Label();
        label_lang.setLabel(label);
        label_lang.setLang(lang);
        
        this.prefLabels.add(label_lang);
    }

    public ArrayList<Label> getAltLabels() {
        return altLabels;
    }

    public void setAltLabels(ArrayList<Label> altLabels) {
        this.altLabels = altLabels;
    }

    public void addAltLabel(String label, String lang) {
        Label label_lang = new Label();
        label_lang.setLabel(label);
        label_lang.setLang(lang);
        
        this.altLabels.add(label_lang);
    }    
    
    public ArrayList<String> getInScheme() {
        return inScheme;
    }

    public void setInScheme(ArrayList<String> inScheme) {
        this.inScheme = inScheme;
    }
    
    public void addInScheme(String inScheme) {
        this.inScheme.add(inScheme);
    }

    public ArrayList<String> getBroader() {
        return broader;
    }

    public void setBroader(ArrayList<String> broader) {
        this.broader = broader;
    }
    
    public void addBroader(String broader) {
        this.broader.add(broader);
    }    

    public ArrayList<String> getNarrower() {
        return narrower;
    }

    public void setNarrower(ArrayList<String> narrower) {
        this.narrower = narrower;
    }
    
    public void addNarrower(String narrower) {
        this.narrower.add(narrower);
    }    

    public ArrayList<String> getRelated() {
        return related;
    }

    public void setRelated(ArrayList<String> related) {
        this.related = related;
    }

    public void addRelated(String related) {
        this.related.add(related);
    }    
    
    public ArrayList<Alignment> getAlignments() {
        return alignments;
    }

    public void setAlignments(ArrayList<Alignment> alignments) {
        this.alignments = alignments;
    }
    
    public void addAlignments(String type, String uri) {
        Alignment alignment = new Alignment();
        alignment.setType(type);
        alignment.setUri(uri);
        
        this.alignments.add(alignment);
    }    

    public ArrayList<Note> getDefinition() {
        return definition;
    }

    public void setDefinition(ArrayList<Note> definition) {
        this.definition = definition;
    }
    
    public void addDefinition(String text, String lang) {
        Note note = new Note();
        note.setNote(text);
        note.setLang(lang);
        
        this.definition.add(note);
    }    

    public ArrayList<Note> getNote() {
        return note;
    }

    public void setNote(ArrayList<Note> note) {
        this.note = note;
    }
    
    public void addNote(String text, String lang) {
        Note note = new Note();
        note.setNote(text);
        note.setLang(lang);
        
        this.note.add(note);
    }     
    
    

    public ArrayList<Note> getScopeNote() {
        return scopeNote;
    }

    public void setScopeNote(ArrayList<Note> scopeNote) {
        this.scopeNote = scopeNote;
    }

    public void addScopeNote(String text, String lang) {
        Note note = new Note();
        note.setNote(text);
        note.setLang(lang);
        
        this.scopeNote.add(note);
    }      
    
    public ArrayList<Note> getHistoryNote() {
        return historyNote;
    }

    public void setHistoryNote(ArrayList<Note> historyNote) {
        this.historyNote = historyNote;
    }

    public void addHistoryNote(String text, String lang) {
        Note note = new Note();
        note.setNote(text);
        note.setLang(lang);
        
        this.historyNote.add(note);
    } 
    
    public ArrayList<Note> getEditorialNote() {
        return editorialNote;
    }

    public void setEditorialNote(ArrayList<Note> editorialNote) {
        this.editorialNote = editorialNote;
    }
    
    public void addEditorialNote(String text, String lang) {
        Note note = new Note();
        note.setNote(text);
        note.setLang(lang);
        
        this.editorialNote.add(note);
    }     

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getIdHandle() {
        return idHandle;
    }

    public void setIdHandle(String idHandle) {
        this.idHandle = idHandle;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
