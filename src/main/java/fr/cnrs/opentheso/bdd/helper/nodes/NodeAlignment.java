/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;

import java.sql.Date;
import java.util.ArrayList;

/**
 *
 * @author miled.rousset
 */
public class NodeAlignment {

    private int id_alignement;
    private int id_source;
    private Date created;
    private Date modified;
    private int id_author;
    private String concept_target;
    private String concept_target_alt;
    private String thesaurus_target;
    private String uri_target;
    private int alignement_id_type;
    private String internal_id_thesaurus;
    private String internal_id_concept; 
    private String def_target;
    private String img_target;
    private boolean save;
    
    private String alignmentLabelType;
    private String alignmentLabelSkosType;    

// coordonnées GPS
    private String concept_target_bold;
    private String countryName;
    private String name;
    private String idUrl;
    private Double lat;
    private Double lng;
    private String toponymName;
    private String adminName1;
    private String adminName2;
    
    private ArrayList<String> traduction;
    private ArrayList<NodeLangTheso> alltraductions;
    
    private String uri_target_url;
    
    public NodeAlignment() {
        concept_target_alt = "";
    }

    public int getId_alignement() {
        return id_alignement;
    }

    public void setId_alignement(int id_alignement) {
        this.id_alignement = id_alignement;
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

    public int getId_author() {
        return id_author;
    }

    public void setId_author(int id_author) {
        this.id_author = id_author;
    }

    public String getConcept_target() {      
        return concept_target;
        
    }

    public void setConcept_target(String concept_target) {
        this.concept_target = concept_target;
    }

    public String getConcept_target_alt() {
        return concept_target_alt;
    }

    public void setConcept_target_alt(String concept_target_alt) {
        this.concept_target_alt = concept_target_alt;
    }
    
    public String getThesaurus_target() {
        return thesaurus_target;
    }

    public void setThesaurus_target(String thesaurus_target) {
        this.thesaurus_target = thesaurus_target;
    }

    public String getUri_target() {
        return uri_target;
    }

    public void setUri_target(String uri_target) {
        this.uri_target = uri_target;
    }

    public int getAlignement_id_type() {
        return alignement_id_type;
    }

    public void setAlignement_id_type(int alignement_id_type) {
        this.alignement_id_type = alignement_id_type;
    }

    public String getInternal_id_thesaurus() {
        return internal_id_thesaurus;
    }

    public void setInternal_id_thesaurus(String internal_id_thesaurus) {
        this.internal_id_thesaurus = internal_id_thesaurus;
    }

    public String getInternal_id_concept() {
        return internal_id_concept;
    }

    public void setInternal_id_concept(String internal_id_concept) {
        this.internal_id_concept = internal_id_concept;
    }

    public String getDef_target() {
        if(def_target== null)
            def_target = "";
        else
            def_target = "<i>"+def_target+"</i>";
        return def_target;
    }

    public void setDef_target(String def_target) {
        this.def_target = def_target;
    }

    public String getImg_target() {
        return img_target;
    }

    public void setImg_target(String img_target) {
        this.img_target = img_target;
    }

    public boolean isSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdUrl() {
        return idUrl;
    }

    public void setIdUrl(String idUrl) {
        this.idUrl = idUrl;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getAdminName1() {
        return adminName1;
    }

    public void setAdminName1(String adminName1) {
        this.adminName1 = adminName1;
    }

    public String getAdminName2() {
        return adminName2;
    }

    public void setAdminName2(String adminName2) {
        this.adminName2 = adminName2;
    }
    
    public ArrayList<String> getTraduction() {
        return traduction;
    }

    public void setTraduction(ArrayList<String> traduction) {
        this.traduction = traduction;
    }

    public String getToponymName() {
        return toponymName;
    }

    public void setToponymName(String toponymName) {
        this.toponymName = toponymName;
    }

    public ArrayList<NodeLangTheso> getAlltraductions() {
        return alltraductions;
    }

    public void setAlltraductions(ArrayList<NodeLangTheso> alltraductions) {
        this.alltraductions = alltraductions;
    }

    public String getUri_target_url() {
        uri_target_url = "<a href=\"" + uri_target + 
                "\" target=\"_blank\"><font color=\"green\"> url</font> </a>";
        return uri_target_url;
    }

    public void setUri_target_url(String uri_target_url) {
        this.uri_target_url = uri_target_url;
    }

    public String getConcept_target_bold() {
        concept_target_bold = "<b>"+concept_target+"</b>";
        return concept_target_bold;
    }

    public void setConcept_target_bold(String concept_target_bold) {
        this.concept_target_bold = concept_target_bold;
    }

    public int getId_source() {
        return id_source;
    }

    public void setId_source(int id_source) {
        this.id_source = id_source;
    }

    public String getAlignmentLabelType() {
        return alignmentLabelType;
    }

    public void setAlignmentLabelType(String alignmentLabelType) {
        this.alignmentLabelType = alignmentLabelType;
    }

    public String getAlignmentLabelSkosType() {
        return alignmentLabelSkosType;
    }

    public void setAlignmentLabelSkosType(String alignmentLabelSkosType) {
        this.alignmentLabelSkosType = alignmentLabelSkosType;
    }

}
