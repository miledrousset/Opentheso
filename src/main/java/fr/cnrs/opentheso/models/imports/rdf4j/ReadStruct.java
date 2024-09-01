package fr.cnrs.opentheso.models.imports.rdf4j;

import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;


//structure qui contiens des informations pour la lecture de fichier RDF
public class ReadStruct {

    Value value;
    IRI property;
    Literal literal;
    SKOSResource resource;
    
}
