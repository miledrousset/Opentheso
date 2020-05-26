/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.tabulate;

import java.util.ArrayList;


/**
 *
 * @author miled.rousset
 */
public class FieldsSkos {
    
    public static final String id = "id";
    public static final String IdArk = "idArk";
    public static final String type = "type";
    public static final String prefLabel = "prefLabel";
    public static final String altLabel = "altLabel";
    public static final String inScheme = "inScheme";
    public static final String broader = "broader";
    public static final String narrower = "narrower";
    public static final String related = "related";
    public static final String alignment = "alignment";
    public static final String note = "note";    
    public static final String definition = "definition";
    public static final String scopeNote = "scopeNote";
    public static final String historyNote = "historyNote";
    public static final String editorialNote = "editorialNote";
    public static final String createdDate = "createdDate";
    public static final String modifiedDate = "modifiedDate";
    
    
    
    
    public static final int id_ = 0;
    public static final int IdArk_ = 1;
    public static final int type_ = 2;
    public static final int prefLabel_ = 3;
    public static final int altLabel_ = 4;
    public static final int inScheme_ = 5;
    public static final int broader_ = 6;
    public static final int narrower_ = 7;
    public static final int related_ = 8;
    public static final int alignment_ = 9;
    public static final int note_ = 16;   
    public static final int definition_ = 10;
    public static final int scopeNote_ = 11;
    public static final int historyNote_ = 12;
    public static final int editorialNote_ = 13;
    public static final int createdDate_ = 14;
    public static final int modifiedDate_ = 15;

    public FieldsSkos() {
    }
    
    public ArrayList<String> getFields(){
        ArrayList<String> fields = new ArrayList<>();
        
        fields.add(FieldsSkos.id);
        fields.add(FieldsSkos.IdArk);
        fields.add(FieldsSkos.type);
        fields.add(FieldsSkos.prefLabel);
        fields.add(FieldsSkos.altLabel);
        fields.add(FieldsSkos.inScheme);
        fields.add(FieldsSkos.broader);
        fields.add(FieldsSkos.narrower);
        fields.add(FieldsSkos.related);
        fields.add(FieldsSkos.alignment);
        fields.add(FieldsSkos.note);        
        fields.add(FieldsSkos.definition);
        fields.add(FieldsSkos.scopeNote);
        fields.add(FieldsSkos.historyNote);
        fields.add(FieldsSkos.editorialNote);
        fields.add(FieldsSkos.createdDate);
        fields.add(FieldsSkos.modifiedDate);
        
        return fields;
    }
}
