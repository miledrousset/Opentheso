package fr.cnrs.opentheso.bdd.helper.dao;

import lombok.Data;

/**
 *
 * @author miledrousset
 */

@Data
public class ConceptLabel implements Comparable<ConceptLabel>{
    private String idTerm;
    private int id;
    private String label;    
    private String idLang;
    private String codeFlag;
 
    
    @Override
    public int compareTo(ConceptLabel other) {
        // Trier par le champ 'label'
        return this.label.compareTo(other.label);
    } 
    
    
}
