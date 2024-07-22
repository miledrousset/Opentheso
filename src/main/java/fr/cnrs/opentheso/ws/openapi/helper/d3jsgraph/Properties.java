package fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph;

import java.util.List;
import lombok.Data;
/**
 *
 * @author miledrousset
 */
/**
 * Class pour regrouper les datas pour un noeud
 */
@Data
public class Properties {

    private String propertiesLabel; // skos__prefLabel
    private List<String> prefLabels; //["noeud 2@fr","node 2@en"]
   
    private String uri; // http://example.com/th1.301
}
