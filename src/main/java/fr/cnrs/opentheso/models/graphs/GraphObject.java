
package fr.cnrs.opentheso.models.graphs;

import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miledrousset
 */
@Data
public class GraphObject implements Serializable {

    private int id;
    private String name;
    private String description;
    private List<ImmutablePair<String, String>> exportedData;

    public GraphObject(int id, String name, String description, List<ImmutablePair<String, String>>  exported_data) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.exportedData = new ArrayList<>(exported_data);
    }

    public GraphObject(String name, String description, List<ImmutablePair<String, String>>  exported_data) {
        this.name = name;
        this.description = description;
        this.exportedData = new ArrayList<>(exported_data);
    }

    public GraphObject(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.exportedData = new ArrayList<>();
    }
}

