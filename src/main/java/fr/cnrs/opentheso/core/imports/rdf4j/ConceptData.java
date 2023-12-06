package fr.cnrs.opentheso.core.imports.rdf4j;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import java.util.ArrayList;
import org.eclipse.rdf4j.model.Statement;


//structure qui contiens des informations pour la lecture de fichier RDF
public class ConceptData {

    
    public void addConceptData(ReadStruct readStruct, Statement statement, ArrayList<String> nonReco, SKOSXmlDocument sKOSXmlDocument) {
        
        switch (readStruct.property.getLocalName()) {
            case "type":
                addDiversDatas(readStruct, statement, sKOSXmlDocument);
                break;

            //readDate    
            case "created":
                readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.created);
                break;
            case "modified":
                readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.modified);
                break;
            case "date":
                readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.date);
                break;

            //readRelationships    
            case "broader":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.BROADER);
                break;
            case "broaderGeneric":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.broaderGeneric);
                break;
            case "broaderInstantial":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.broaderInstantial);
                break;
            case "broaderPartitive":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.broaderPartitive);
                break;
            case "narrower":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.NARROWER);
                break;
            case "narrowerGeneric":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.narrowerGeneric);
                break;
            case "narrowerInstantial":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.narrowerInstantial);
                break;
            case "narrowerPartitive":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.narrowerPartitive);
                break;
            case "related":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.RELATED);
                break;
            case "relatedHasPart":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.relatedHasPart);
                break;
            case "relatedPartOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.relatedPartOf);
                break;
            case "hasTopConcept":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.HASTOPCONCEPT);
                break;
            case "inScheme":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.INSCHEME);
                break;
            case "member":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.MEMBER);
                break;
            case "topConceptOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.topConceptOf);
                break;
            case "microThesaurusOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.microThesaurusOf);
                break;
            case "subGroup":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.subGroup);
                break;
            case "superGroup":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.superGroup);
                break;
            case "hasMainConcept":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.hasMainConcept);
                break;
            case "memberOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.memberOf);
                break;
            case "mainConceptOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.mainConceptOf);
                break;
            case "superOrdinate":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.superOrdinate);
                break;

            //Creator
            case "creator":
                readStruct.resource.addAgent(readStruct.literal.getLabel(), SKOSProperty.creator);
                break;
            case "contributor":
                readStruct.resource.addAgent(readStruct.literal.getLabel(), SKOSProperty.contributor);
                break;

            //GPSCoordinates
            case "lat":
                if (readStruct.resource.getGpsCoordinates().stream()
                        .filter(element -> element.getLat() == null)
                        .findFirst()
                        .isPresent()) {
                    for (SKOSGPSCoordinates element : readStruct.resource.getGpsCoordinates()) {
                        if (element.getLat() == null) {
                            element.setLat(readStruct.literal.getLabel());
                            break;
                        }
                    }
                } else {
                    SKOSGPSCoordinates element = new SKOSGPSCoordinates();
                    element.setLat(readStruct.literal.getLabel());
                    readStruct.resource.getGpsCoordinates().add(element);
                }
                break;
            case "long":
                if (readStruct.resource.getGpsCoordinates().stream()
                        .filter(element -> element.getLon() == null)
                        .findFirst()
                        .isPresent()) {
                    for (SKOSGPSCoordinates element : readStruct.resource.getGpsCoordinates()) {
                        if (element.getLon() == null) {
                            element.setLon(readStruct.literal.getLabel());
                            break;
                        }
                    }
                } else {
                    SKOSGPSCoordinates element = new SKOSGPSCoordinates();
                    element.setLon(readStruct.literal.getLabel());
                    readStruct.resource.getGpsCoordinates().add(element);
                }
                break;
            //Notation
            case "notation":
                readStruct.resource.addNotation(readStruct.literal.getLabel());
                break;

            //identifier
            case "identifier":
                readStruct.resource.setIdentifier(readStruct.literal.getLabel());
                sKOSXmlDocument.getEquivalenceUriArkHandle().put(readStruct.resource.getUri(), readStruct.literal.getLabel());
                break;

            //Match 
            case "exactMatch":
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.exactMatch);
                break;
            case "closeMatch":
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.closeMatch);
                break;
            case "broadMatch":
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.broadMatch);
                break;
            case "relatedMatch":
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.relatedMatch);
                break;
            case "narrowMatch":
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.narrowMatch);
                break;

            //Image
            case "Image":
                NodeImage nodeImage = new NodeImage();
                nodeImage.setImageName("");
                nodeImage.setCopyRight("");
                nodeImage.setUri(readStruct.value.stringValue());
                readStruct.resource.addNodeImage(nodeImage);
                break;

            //Replaces
            case "replaces":
                readStruct.resource.addReplaces(readStruct.value.toString(), SKOSProperty.replaces);
                break;
            case "isReplacedBy":
                readStruct.resource.addReplaces(readStruct.value.toString(), SKOSProperty.isReplacedBy);
                break;

            //deprecated    
            case "deprecated":
                readStruct.resource.setStatus(SKOSProperty.deprecated);
                break;

            //DcSource
            case "source":
                readStruct.resource.addDcRelations(readStruct.value.stringValue());
                break;

            default:
                if (!nonReco.contains(readStruct.property.getLocalName())
                        && !readStruct.property.getLocalName().contains("superOrdinate")
                        && !readStruct.property.getLocalName().contains("subordinateArray")
                        && !readStruct.property.getLocalName().contains("description")) {
                    nonReco.add(readStruct.property.getLocalName());
                }
        }
    }
    

    private void addDiversDatas(ReadStruct readStruct, Statement statement, SKOSXmlDocument sKOSXmlDocument) {
        
        if (readStruct.value.toString().toUpperCase().contains("CONCEPTSCHEME")) {
            sKOSXmlDocument.setTitle(statement.getSubject().stringValue());
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.ConceptScheme);
        } else if (readStruct.value.toString().toUpperCase().contains("CONCEPTGROUP")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.ConceptGroup);
        } else if (readStruct.value.toString().toUpperCase().contains("THESAURUSARRAY")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.FACET);
        } else if (readStruct.value.toString().toUpperCase().contains("THEME")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.Theme);
        } else if (readStruct.value.toString().toUpperCase().contains("MICROTHESAURUS")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.MicroThesaurus);
        } else if (readStruct.value.toString().toUpperCase().contains("COLLECTION")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.Collection);
        } else if (readStruct.value.toString().toUpperCase().contains("CONCEPT")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.Concept);
        } else if (readStruct.value.toString().toUpperCase().contains("IMAGE")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.FoafImage);
        }
    }
    

    private void addStructData(ReadStruct readStruct, Statement statement, SKOSXmlDocument sKOSXmlDocument, int prop) {
        if (readStruct.resource == null) {
            readStruct.resource = new SKOSResource();
        }
        if (readStruct.resource != null) {
            readStruct.resource.setProperty(prop);
            readStruct.resource.setUri(statement.getSubject().stringValue());
        }

        switch (prop) {
            case SKOSProperty.ConceptScheme:
                sKOSXmlDocument.setConceptScheme(readStruct.resource);
                break;
            case SKOSProperty.FACET:
                sKOSXmlDocument.addFacet(readStruct.resource);
                break;
            case SKOSProperty.ConceptGroup:
            case SKOSProperty.Collection:
            case SKOSProperty.Theme:
            case SKOSProperty.MicroThesaurus:
                sKOSXmlDocument.addGroup(readStruct.resource);
                break;
            case SKOSProperty.Concept:
                sKOSXmlDocument.addconcept(readStruct.resource);
                break;
            case SKOSProperty.FoafImage:
                sKOSXmlDocument.addFoafImage(readStruct.resource);
                break;
        }
    }

}
