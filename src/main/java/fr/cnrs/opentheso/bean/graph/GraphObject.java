
package fr.cnrs.opentheso.bean.graph;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author miledrousset
 */

public class GraphObject implements Serializable {

    private int id;
    private String name;
    private String description;
    private List<ImmutablePair<String, String>> exported_data;

    public GraphObject(int id, String name, String description, List<ImmutablePair<String, String>>  exported_data) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.exported_data = new ArrayList<>(exported_data);
    }

    public GraphObject(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.exported_data = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ImmutablePair<String, String>>  getExportedData() {
        return exported_data;
    }
}

