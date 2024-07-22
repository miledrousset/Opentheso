package fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph;

import java.util.List;
import java.util.Objects;
import lombok.Data;
/**
 *
 * @author miledrousset
 */
/**
 * Class pour regrouper les datas pour un noeud
 */
@Data
public class Node {

    private String id; //id du concept combiné avec id theso "th1.300"
    private List<String> labels; //"skos__Concept", "Resource"
    private Properties properties; 
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }    
    @Override
    public String toString() {
        return "Node{id='" + id + "', label='" + labels.toString() + "'}";
    }    
}
