package fr.cnrs.opentheso.core.imports.rdf4j.nouvelle;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;


public class ConceptReader {

    public void readGeneric(SKOSXmlDocument sKOSXmlDocument, SKOSResource skosConcept, IRI predicate, Literal literal, String lang) {

        switch (predicate.getLocalName()) {
            case "prefLabel":
                skosConcept.addLabel(literal.getLabel(), lang, SKOSProperty.prefLabel);
                break;
            case "altLabel":
                skosConcept.addLabel(literal.getLabel(), lang, SKOSProperty.altLabel);
                break;
            case "hiddenLabel":
                skosConcept.addLabel(literal.getLabel(), lang, SKOSProperty.hiddenLabel);
                break;

            case "definition":
                skosConcept.addDocumentation(literal.getLabel(), lang, SKOSProperty.definition);
                break;
            case "scopeNote":
                skosConcept.addDocumentation(literal.getLabel(), lang, SKOSProperty.scopeNote);
                break;
            case "example":
                skosConcept.addDocumentation(literal.getLabel(), lang, SKOSProperty.example);
                break;
            case "historyNote":
                skosConcept.addDocumentation(literal.getLabel(), lang, SKOSProperty.historyNote);
                break;
            case "editorialNote":
                skosConcept.addDocumentation(literal.getLabel(), lang, SKOSProperty.editorialNote);
                break;
            case "changeNote":
                skosConcept.addDocumentation(literal.getLabel(), lang, SKOSProperty.changeNote);
                break;
            case "note":
                skosConcept.addDocumentation(literal.getLabel(), lang, SKOSProperty.note);
                break;
            //DcSource
            case "source":
                skosConcept.addDcRelations(literal.getLabel());
                break;

            //Image
            case "Image":
                NodeImage nodeImage = new NodeImage();
                nodeImage.setImageName("");
                nodeImage.setCopyRight("");
                nodeImage.setUri(literal.getLabel());
                skosConcept.addNodeImage(nodeImage);
                break;

            //Replaces
            case "replaces":
                skosConcept.addReplaces(literal.getLabel(), SKOSProperty.replaces);
                break;
            case "isReplacedBy":
                skosConcept.addReplaces(literal.getLabel(), SKOSProperty.isReplacedBy);
                break;

            //deprecated
            case "deprecated":
                skosConcept.setStatus(SKOSProperty.deprecated);
                break;


            //GPSCoordinates
            case "lat":
                if (skosConcept.getGpsCoordinates().stream()
                        .filter(element -> element.getLat() == null)
                        .findFirst()
                        .isPresent()) {
                    for (SKOSGPSCoordinates element : skosConcept.getGpsCoordinates()) {
                        if (element.getLat() == null) {
                            element.setLat(literal.getLabel());
                            break;
                        }
                    }
                } else {
                    SKOSGPSCoordinates element = new SKOSGPSCoordinates();
                    element.setLat(literal.getLabel());
                    skosConcept.getGpsCoordinates().add(element);
                }
                break;

            case "long":
                if (skosConcept.getGpsCoordinates().stream()
                        .filter(element -> element.getLon() == null)
                        .findFirst()
                        .isPresent()) {
                    for (SKOSGPSCoordinates element : skosConcept.getGpsCoordinates()) {
                        if (element.getLon() == null) {
                            element.setLon(literal.getLabel());
                            break;
                        }
                    }
                } else {
                    SKOSGPSCoordinates element = new SKOSGPSCoordinates();
                    element.setLon(literal.getLabel());
                    skosConcept.getGpsCoordinates().add(element);
                }
                break;

            //identifier
            case "identifier":
                skosConcept.setIdentifier(literal.getLabel());
                sKOSXmlDocument.getEquivalenceUriArkHandle().put(skosConcept.getUri(), literal.getLabel());
                break;
                
            case "created":
                skosConcept.addDate(literal.getLabel(), SKOSProperty.created);
                break;
            case "modified":
                skosConcept.addDate(literal.getLabel(), SKOSProperty.modified);
                break;   
                
            case "creator":
                skosConcept.addAgent(literal.getLabel(), SKOSProperty.creator);
                break;
            case "contributor":
                skosConcept.addAgent(literal.getLabel(), SKOSProperty.contributor);
                break;                 
        }
    }


    public void readConcept(SKOSXmlDocument sKOSXmlDocument, SKOSResource skosConcept, IRI predicate, Value value, Literal literal) {

        switch (predicate.getLocalName()) {
            //readDate    
            case "created":
                skosConcept.addDate(literal.getLabel(), SKOSProperty.created);
                break;
            case "modified":
                skosConcept.addDate(literal.getLabel(), SKOSProperty.modified);
                break;
            case "date":
                skosConcept.addDate(literal.getLabel(), SKOSProperty.date);
                break;

            //readRelationships    
            case "broader":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.broader);
                break;
            case "broaderGeneric":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.broaderGeneric);
                break;
            case "broaderInstantial":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.broaderInstantial);
                break;
            case "broaderPartitive":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.broaderPartitive);
                break;
            case "narrower":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.narrower);
                break;
            case "narrowerGeneric":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.narrowerGeneric);
                break;
            case "narrowerInstantial":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.narrowerInstantial);
                break;
            case "narrowerPartitive":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.narrowerPartitive);
                break;
            case "related":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.related);
                break;
            case "relatedHasPart":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.relatedHasPart);
                break;
            case "relatedPartOf":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.relatedPartOf);
                break;
            case "hasTopConcept":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.hasTopConcept);
                break;
            case "inScheme":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.inScheme);
                break;
            case "member":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.member);
                break;
            case "topConceptOf":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.topConceptOf);
                break;
            case "microThesaurusOf":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.microThesaurusOf);
                break;
            case "subGroup":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.subGroup);
                break;
            case "superGroup":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.superGroup);
                break;
            case "hasMainConcept":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.hasMainConcept);
                break;
            case "memberOf":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.memberOf);
                break;
            case "mainConceptOf":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.mainConceptOf);
                break;
            case "superOrdinate":
                skosConcept.addRelation("", value.stringValue(), SKOSProperty.superOrdinate);
                break;

            //Creator
            case "creator":
                // Attention au Null 
                if(literal != null)
                    skosConcept.addAgent(literal.getLabel(), SKOSProperty.creator);
                break;
            case "contributor":
                if(literal != null)
                    skosConcept.addAgent(literal.getLabel(), SKOSProperty.contributor);
                break;

            //Notation
            case "notation":
                skosConcept.addNotation(literal.getLabel());
                break;

            //identifier
            case "identifier":
                skosConcept.setIdentifier(literal.getLabel());
                sKOSXmlDocument.getEquivalenceUriArkHandle().put(skosConcept.getUri(), literal.getLabel());
                break;

            //Match
            case "exactMatch":
                skosConcept.addMatch(value.stringValue(), SKOSProperty.exactMatch);
                break;
            case "closeMatch":
                skosConcept.addMatch(value.stringValue(), SKOSProperty.closeMatch);
                break;
            case "broadMatch":
                skosConcept.addMatch(value.stringValue(), SKOSProperty.broadMatch);
                break;
            case "relatedMatch":
                skosConcept.addMatch(value.stringValue(), SKOSProperty.relatedMatch);
                break;
            case "narrowMatch":
                skosConcept.addMatch(value.stringValue(), SKOSProperty.narrowMatch);
                break;

            //Image
            case "Image":
                NodeImage nodeImage = new NodeImage();
                nodeImage.setImageName("");
                nodeImage.setCopyRight("");
                nodeImage.setUri(value.stringValue());
                skosConcept.addNodeImage(nodeImage);
                break;

            //Replaces
            case "replaces":
                skosConcept.addReplaces(value.stringValue(), SKOSProperty.replaces);
                break;
            case "isReplacedBy":
                skosConcept.addReplaces(value.stringValue(), SKOSProperty.isReplacedBy);
                break;

            //deprecated
            case "deprecated":
                skosConcept.setStatus(SKOSProperty.deprecated);
                break;
        }
        if(StringUtils.contains(predicate.getNamespace(), "purl.org/dc/terms")){
            if(predicate.isResource()) {
                skosConcept.getThesaurus().addDcElement(new DcElement(predicate.getLocalName(), value.stringValue(), null, DCMIResource.TYPE_RESOURCE));
            }
        }
    }
}
