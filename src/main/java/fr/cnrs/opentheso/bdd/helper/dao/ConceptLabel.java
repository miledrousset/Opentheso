package fr.cnrs.opentheso.bdd.helper.dao;

import lombok.Data;

/**
 *
 * @author miledrousset
 */

@Data
public class ConceptLabel {
    private String idTerm;
    private int id;
    private String label;    
    private String idLang;
    private String codeFlag;
   
}
