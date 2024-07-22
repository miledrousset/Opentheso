package fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph;

import lombok.Data;
/**
 *
 * @author miledrousset
 */
/**
 * Class pour regrouper les datas pour un noeud
 */
@Data
public class Relationship {
    private String relation; //skos__narrower
    private String start; //id of node
    private String end; // id of node
}
