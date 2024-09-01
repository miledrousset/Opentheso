package fr.cnrs.opentheso.models.jsonld;

import lombok.Data;
import java.util.ArrayList;


@Data
public class JsonConcept {

    private String id; // id = "../concept#13370"

    private String nameSpace; // nameSpace = "http://www.w3.org/2004/02/skos/core#Concept"

    private ArrayList<NodeElement> nodeElement;

    private ArrayList<NodeResource> nodeAttribute;

    private ArrayList<NodeResource> nodeResource;

}
