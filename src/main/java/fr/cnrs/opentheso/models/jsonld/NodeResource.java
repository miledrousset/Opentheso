package fr.cnrs.opentheso.models.jsonld;

import lombok.Data;
import java.util.ArrayList;


@Data
public class NodeResource {
    
    private String nameSpace;
    private ArrayList<Attribute> attributes;
    
}
