
package fr.cnrs.opentheso.bdd.helper.nodes;
import lombok.Data;

@Data
/**
 *
 * @author miledrousset
 */
public class NodeCompareTheso {
    private String idConcept;
    private String idArk;
    private String originalPrefLabel, prefLabel, altLabel;
    private String definition;
  
}
