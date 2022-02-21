/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author antonio.perez
 */
public class AlignementSource {
    private String source;
    
    // pour définir le mode de filtrage ex : Opentheso, Wikidata, Gemet ....
    private String source_filter = "";    
        
    private String requete;
    private String typeRequete;
    private String alignement_format;
    private  int id;
    private String description;
    
    
    // deprecated
    private boolean isGps;

    public AlignementSource() {
    }
    public void init_alignementSource()
    {
        source="";
        source_filter = "";
        requete="";
        alignement_format="";
        typeRequete="";
        description="";
        isGps = false;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRequete() {
        return requete;
    }

    public void setRequete(String requete) {
        this.requete = requete;
    }

    public String getTypeRequete() {
        return typeRequete;
    }

    public void setTypeRequete(String typeRequete) {
        this.typeRequete = typeRequete;
    }

    public String getAlignement_format() {
        return alignement_format;
    }

    public void setAlignement_format(String alignement_format) {
        this.alignement_format = alignement_format;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource_filter() {
        if(StringUtils.isEmpty(source_filter)) return "";
        return source_filter;
    }

    public void setSource_filter(String source_filter) {
        this.source_filter = source_filter;
    }

    public boolean isIsGps() {
        return isGps;
    }

    public void setIsGps(boolean isGps) {
        this.isGps = isGps;
    }


  
}
