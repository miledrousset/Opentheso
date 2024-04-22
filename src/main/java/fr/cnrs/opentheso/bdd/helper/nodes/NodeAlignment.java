package fr.cnrs.opentheso.bdd.helper.nodes;

import fr.cnrs.opentheso.core.alignment.SelectedResource;
import lombok.Data;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miled.rousset
 */
@Data
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

    private String definitionLocal, labelLocal, uriTargetLocal, alignementTypeLocal, conceptOrigin;

    private List<SelectedResource> selectedTraductionsList, selectedDefinitionsList, selectedImagesList;
    private boolean alignementLocalValide;

}
