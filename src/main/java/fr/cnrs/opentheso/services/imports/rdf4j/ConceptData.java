package fr.cnrs.opentheso.services.imports.rdf4j;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.models.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;
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
                readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.CREATED);
                break;
            case "modified":
                readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.MODIFIED);
                break;
            case "date":
                readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.DATE);
                break;

            //readRelationships    
            case "broader":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.BROADER);
                break;
            case "broaderGeneric":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.BROADER_GENERIC);
                break;
            case "broaderInstantial":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.BROADER_INSTANTIAL);
                break;
            case "broaderPartitive":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.BROADER_PARTITIVE);
                break;
            case "narrower":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.NARROWER);
                break;
            case "narrowerGeneric":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.NARROWER_GENERIC);
                break;
            case "narrowerInstantial":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.NARROWER_INSTANTIAL);
                break;
            case "narrowerPartitive":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.NARROWER_PARTITIVE);
                break;
            case "related":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.RELATED);
                break;
            case "relatedHasPart":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.RELATED_HAS_PART);
                break;
            case "relatedPartOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.RELATED_PART_OF);
                break;
            case "hasTopConcept":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.HAS_TOP_CONCEPT);
                break;
            case "inScheme":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.INSCHEME);
                break;
            case "member":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.MEMBER);
                break;
            case "topConceptOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.TOP_CONCEPT_OF);
                break;
            case "microThesaurusOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.MICROTHESAURUS_OF);
                break;
            case "subGroup":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.SUBGROUP);
                break;
            case "superGroup":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.SUPERGROUP);
                break;
            case "hasMainConcept":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.HAS_MAIN_CONCEPT);
                break;
            case "memberOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.MEMBER_OF);
                break;
            case "mainConceptOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.MAIN_CONCEPT_OF);
                break;
            case "superOrdinate":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.SUPER_ORDINATE);
                break;

            //Creator
            case "creator":
                readStruct.resource.addAgent(readStruct.literal.getLabel(), SKOSProperty.CREATOR);
                break;
            case "contributor":
                readStruct.resource.addAgent(readStruct.literal.getLabel(), SKOSProperty.CONTRIBUTOR);
                break;

            //GPSCoordinates
            case "lat":
                if (readStruct.resource.getGpsCoordinates().stream().anyMatch(element -> element.getLat() == null)) {
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
                if (readStruct.resource.getGpsCoordinates().stream().anyMatch(element -> element.getLon() == null)) {
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
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.EXACT_MATCH);
                break;
            case "closeMatch":
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.CLOSE_MATCH);
                break;
            case "broadMatch":
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.BROAD_MATCH);
                break;
            case "relatedMatch":
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.RELATED_MATCH);
                break;
            case "narrowMatch":
                readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.NARROWER_MATCH);
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
                readStruct.resource.addReplaces(readStruct.value.toString(), SKOSProperty.REPLACES);
                break;
            case "isReplacedBy":
                readStruct.resource.addReplaces(readStruct.value.toString(), SKOSProperty.IS_REPLACED_BY);
                break;

            //deprecated    
            case "deprecated":
                readStruct.resource.setStatus(SKOSProperty.DEPRECATED);
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
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.CONCEPT_SCHEME);
        } else if (readStruct.value.toString().toUpperCase().contains("CONCEPTGROUP")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.CONCEPT_GROUP);
        } else if (readStruct.value.toString().toUpperCase().contains("THESAURUSARRAY")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.FACET);
        } else if (readStruct.value.toString().toUpperCase().contains("THEME")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.THEME);
        } else if (readStruct.value.toString().toUpperCase().contains("MICROTHESAURUS")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.MICROTHESAURUS);
        } else if (readStruct.value.toString().toUpperCase().contains("COLLECTION")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.COLLECTION);
        } else if (readStruct.value.toString().toUpperCase().contains("CONCEPT")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.CONCEPT);
        } else if (readStruct.value.toString().toUpperCase().contains("IMAGE")) {
            addStructData(readStruct, statement, sKOSXmlDocument, SKOSProperty.FOAF_IMAGE);
        }
    }
    

    private void addStructData(ReadStruct readStruct, Statement statement, SKOSXmlDocument sKOSXmlDocument, int prop) {
        if (readStruct.resource == null) {
            readStruct.resource = new SKOSResource();
        }
        readStruct.resource.setProperty(prop);
        readStruct.resource.setUri(statement.getSubject().stringValue());

        switch (prop) {
            case SKOSProperty.CONCEPT_SCHEME:
                sKOSXmlDocument.setConceptScheme(readStruct.resource);
                break;
            case SKOSProperty.FACET:
                sKOSXmlDocument.addFacet(readStruct.resource);
                break;
            case SKOSProperty.CONCEPT_GROUP:
            case SKOSProperty.COLLECTION:
            case SKOSProperty.THEME:
            case SKOSProperty.MICROTHESAURUS:
                sKOSXmlDocument.addGroup(readStruct.resource);
                break;
            case SKOSProperty.CONCEPT:
                sKOSXmlDocument.addconcept(readStruct.resource);
                break;
            case SKOSProperty.FOAF_IMAGE:
                sKOSXmlDocument.addFoafImage(readStruct.resource);
                break;
        }
    }

}
