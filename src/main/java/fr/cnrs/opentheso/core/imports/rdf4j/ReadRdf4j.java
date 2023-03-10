package fr.cnrs.opentheso.core.imports.rdf4j;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import fr.cnrs.opentheso.bdd.tools.FileUtilities;
import fr.cnrs.opentheso.core.exports.rdf4j.WriteRdf4j;
import fr.cnrs.opentheso.skosapi.FoafImage;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import org.apache.commons.collections.CollectionUtils;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe qui permet le chargement et la lecture de fichier RDF
 */
public class ReadRdf4j {

    private Logger logger = LoggerFactory.getLogger(ReadRdf4j.class);

    private String message = "";
    private String workLanguage;

    private SKOSXmlDocument sKOSXmlDocument;

    /**
     * Chargement des données rdf d'après un InputStream
     *
     * @param is
     * @param type
     * @param isCandidatImport
     * @param workLanguage
     * @throws IOException
     */
    public ReadRdf4j(InputStream is, int type, boolean isCandidatImport, String workLanguage) throws IOException {

        this.workLanguage = workLanguage;
        sKOSXmlDocument = new SKOSXmlDocument();

        switch (type) {
            case 0:
                readModel(Rio.parse(is, "", RDFFormat.RDFXML), isCandidatImport);
                break;
            case 1:
                readModel(Rio.parse(is, "", RDFFormat.JSONLD), isCandidatImport);
                break;
            case 2:
                readModel(Rio.parse(is, "", RDFFormat.TURTLE), isCandidatImport);
                break;
            default:
                readModel(Rio.parse(is, "", RDFFormat.RDFJSON), isCandidatImport);

        }
    }

    /**
     * structure qui contiens des informations pour la lecture de fichier RDF
     */
    private class ReadStruct {

        Value value;
        IRI property;
        Literal literal;
        SKOSResource resource;
    }

    /**
     * Permet de lire un fichier RDF précédament charger avec la fonction
     * laodModel() les données sont stoqué dans la variable thesaurus
     */
    private void readModel(Model model, boolean isCandidatImport) {

        int resourceType;
        String currentObject = null;
        ReadStruct readStruct = new ReadStruct();
        ArrayList<String> nonReco = new ArrayList<>();

        for (Statement statement : model) {

            resourceType = SKOSProperty.Concept;
            readStruct.value = statement.getObject();
            readStruct.property = statement.getPredicate();

            if (currentObject == null) {
                currentObject = statement.getSubject().stringValue();
            } else if (!currentObject.equalsIgnoreCase(statement.getSubject().stringValue())) {
                if (readStruct.value.stringValue().contains("foaf/0.1/Image")) {
                    resourceType = SKOSProperty.FoafImage;
                }
                readStruct.resource = new SKOSResource();
                currentObject = statement.getSubject().stringValue();
            }

            // pour exclure les balises de type (shema.org) sinon, ca produit une erreur quand on a un tableau
            if (!readStruct.property.getNamespace().contains("schema.org")
                    && !readStruct.property.getNamespace().contains("skos-xl")) {

                if (readStruct.value instanceof Literal) {
                    readStruct.literal = (Literal) readStruct.value;
                }

                //Concept or ConceptScheme or Collection or ConceptGroup
                if (readStruct.property.getLocalName().equals("type")) {
                    boolean validProperty = false;
                    int prop = -1;
                    if (readStruct.value.toString().toUpperCase().contains("ConceptScheme".toUpperCase())) {
                        prop = SKOSProperty.ConceptScheme;
                        sKOSXmlDocument.setTitle(statement.getSubject().stringValue());
                        validProperty = true;
                    } else if (readStruct.value.toString().toUpperCase().contains("ConceptGroup".toUpperCase())) {
                        prop = SKOSProperty.ConceptGroup;
                        validProperty = true;
                    } else if (readStruct.value.toString().toUpperCase().contains("ThesaurusArray".toUpperCase())) {
                        prop = SKOSProperty.FACET;
                        validProperty = true;
                    } else if (readStruct.value.toString().toUpperCase().contains("Theme".toUpperCase())) {
                        prop = SKOSProperty.Theme;
                        validProperty = true;
                    } else if (readStruct.value.toString().toUpperCase().contains("MicroThesaurus".toUpperCase())) {
                        prop = SKOSProperty.MicroThesaurus;
                        validProperty = true;
                    } else if (readStruct.value.toString().toUpperCase().contains("Collection".toUpperCase())) {
                        prop = SKOSProperty.Collection;
                        validProperty = true;
                    } else if (readStruct.value.toString().toUpperCase().contains("Concept".toUpperCase())) {
                        prop = SKOSProperty.Concept;
                        validProperty = true;
                    } else if (readStruct.value.toString().toUpperCase().contains("IMAGE".toUpperCase())) {
                        prop = SKOSProperty.FoafImage;
                        validProperty = true;
                    }
                    if (validProperty) {
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
                                //   foafImageResource.setUri(uri);
                                sKOSXmlDocument.addFoafImage(readStruct.resource);
                                break;
                            default:
                                logger.info("Propriétée non reconnue");
                                break;
                        }
                    }
                } else if (readStruct.property.getLocalName().equals("note") && isCandidatImport) {
                    //Labelling Properties
                    if (null == readStruct.literal.getLanguage().get()) {
                        String lang = readStruct.literal.getLanguage().get();
                        readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.note);
                    } else {
                        switch (readStruct.literal.getLanguage().get()) {
                            case WriteRdf4j.STATUS_TAG:
                                readStruct.resource.setSkosStatus(readStruct.literal.getLabel().split(WriteRdf4j.DELIMINATE));
                                break;
                            case WriteRdf4j.VOTE_TAG:
                                readStruct.resource.addVote(readStruct.literal.getLabel().split(WriteRdf4j.DELIMINATE));
                                break;
                            case WriteRdf4j.DISCUSSION_TAG:
                                readStruct.resource.addMessage(readStruct.literal.getLabel().split(WriteRdf4j.DELIMINATE));
                                break;
                            default:
                                String lang = readStruct.literal.getLanguage().get();
                                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.note);
                                break;
                        }
                    }
                } else if (readLabellingProperties(readStruct)) {
                    if (resourceType == SKOSProperty.FoafImage) {
                        readFoafImageIdentifier(readStruct);
                        readFoafImageName(readStruct);
                        readFoafImageRight(readStruct);
                    } else {
                        if (readStruct.resource == null) {
                            readStruct.resource = new SKOSResource();
                        }
                        //Dates
                        if (readDate(readStruct)) {
                            if (readRelationships(readStruct)) {
                                if (readDocumentation(readStruct)) {
                                    if (readCreator(readStruct)) {
                                        if (readGPSCoordinates(readStruct)) {
                                            if (readNotation(readStruct)) {
                                                if (readIdentifier(readStruct)) {
                                                    if (readMatch(readStruct)) {
                                                        if (readImage(readStruct)) {
                                                            if (readReplaces(readStruct)) { // pour le concepts dépréciés
                                                                if (readConceptStatus(readStruct)) { // pour le reconnaitre le status du concept
                                                                    if (readRights(readStruct)) {
                                                                        if (readTitle(readStruct)) {
                                                                            if (readDcSource(readStruct)) {
                                                                                //debug
                                                                                if (!nonReco.contains(readStruct.property.getLocalName())) {
                                                                                    if (!readStruct.property.getLocalName().contains("superOrdinate")
                                                                                            && !readStruct.property.getLocalName().contains("subordinateArray")
                                                                                            && !readStruct.property.getLocalName().contains("description")) {
                                                                                        nonReco.add(readStruct.property.getLocalName());
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(nonReco)) {
            message = message + " Not readed RDF tag \n " + nonReco.toString();
        }
    }

    private void readFoafImageIdentifier(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("identifier")) {
            readStruct.resource.setIdentifier(readStruct.literal.getLabel());
        }
    }

    private void readFoafImageName(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("title")) {
            readStruct.resource.getFoafImage().setImageName(readStruct.literal.getLabel());
        }
    }

    private void readFoafImageRight(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("rights")) {
            readStruct.resource.getFoafImage().setCopyRight(readStruct.literal.getLabel());
        }
    }

    /**
     * lit les balises Rights pour les images
     *
     * @param readStruct
     * @return false si on a lus une balise de Date true sinon
     */
    private boolean readRights(ReadStruct readStruct) {
        if (readStruct.resource.getFoafImage() == null) {
            readStruct.resource.setFoafImage(new FoafImage());
        }
        if (readStruct.property.getLocalName().equals("rights")) {
            readStruct.resource.getFoafImage().setCopyRight(readStruct.literal.getLabel());
            return false;
        }
        return true;
    }

    /**
     * lit les balises Rights pour les images
     *
     * @param readStruct
     * @return false si on a lus une balise de Date true sinon
     */
    private boolean readTitle(ReadStruct readStruct) {
        if (readStruct.resource.getFoafImage() == null) {
            readStruct.resource.setFoafImage(new FoafImage());
        }
        if (readStruct.property.getLocalName().equals("title")) {
            readStruct.resource.getFoafImage().setImageName(readStruct.literal.getLabel());
            return false;
        }
        return true;
    }

    /**
     * lit les balise de Documentation
     *
     * @param readStruct
     * @return false si on a lus une balise de Documentation true sinon
     */
    private boolean readDocumentation(ReadStruct readStruct) {
        String lang = workLanguage;
        if (readStruct.literal == null) {
            return true;
        }
        // si aucune langue n'est précisée, on applique la langue par défaut
        if (readStruct.literal.getLanguage().isPresent()) {
            lang = readStruct.literal.getLanguage().get();
        }

        switch (readStruct.property.getLocalName()) {
            case "definition":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.definition);
                return false;
            case "scopeNote":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.scopeNote);
                return false;
            case "example":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.example);
                return false;
            case "historyNote":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.historyNote);
                return false;
            case "editorialNote":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.editorialNote);
                return false;
            case "changeNote":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.changeNote);
                return false;
            case "note":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.note);
                return false;
            default:
                return true;
        }

    }

    /**
     * lit les balise de Relationships
     *
     * @param readStruct
     * @return false si on a lus une balise de Relationships true sinon
     */
    private boolean readReplaces(ReadStruct readStruct) {
        switch (readStruct.property.getLocalName()) {
            case "replaces":
                readStruct.resource.addReplaces(readStruct.value.toString(), SKOSProperty.replaces);
                return false;
            case "isReplacedBy":
                readStruct.resource.addReplaces(readStruct.value.toString(), SKOSProperty.isReplacedBy);
                return false;
            default:
                return true;
        }
    }

    /**
     * lit les balise de Relationships
     *
     * @param readStruct
     * @return false si on a lus une balise de Relationships true sinon
     */
    private boolean readConceptStatus(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("deprecated")) {
            readStruct.resource.setStatus(SKOSProperty.deprecated);
            return false;
        } else {
            return true;
        }
    }

    /**
     * lit les balise de Relationships
     *
     * @param readStruct
     * @return false si on a lus une balise de Relationships true sinon
     */
    private boolean readRelationships(ReadStruct readStruct) {

        switch (readStruct.property.getLocalName()) {
            case "broader":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.broader);
                return false;
            case "broaderGeneric":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.broaderGeneric);
                return false;
            case "broaderInstantial":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.broaderInstantial);
                return false;
            case "broaderPartitive":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.broaderPartitive);
                return false;
            case "narrower":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.narrower);
                return false;
            case "narrowerGeneric":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.narrowerGeneric);
                return false;
            case "narrowerInstantial":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.narrowerInstantial);
                return false;
            case "narrowerPartitive":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.narrowerPartitive);
                return false;
            case "related":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.related);
                return false;
            case "relatedHasPart":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.relatedHasPart);
                return false;
            case "relatedPartOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.relatedPartOf);
                return false;
            case "hasTopConcept":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.hasTopConcept);
                return false;
            case "inScheme":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.inScheme);
                return false;
            case "member":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.member);
                return false;
            case "topConceptOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.topConceptOf);
                return false;
            case "microThesaurusOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.microThesaurusOf);
                return false;
            case "subGroup":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.subGroup);
                return false;
            case "superGroup":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.superGroup);
                return false;
            case "hasMainConcept":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.hasMainConcept);
                return false;
            case "memberOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.memberOf);
                return false;
            case "mainConceptOf":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.mainConceptOf);
                return false;
            case "superOrdinate":
                readStruct.resource.addRelation("", readStruct.value.toString(), SKOSProperty.superOrdinate);
                return false;
            default:
                return true;
        }
    }

    /**
     * lit les balise de Labelling
     *
     * @param readStruct
     * @return false si on a lus une balise de Labelling true sinon
     */
    private boolean readLabellingProperties(ReadStruct readStruct) {
        if (readStruct.literal == null) {
            return true;
        }
        if (readStruct.resource == null) {
            return true;
        }

        // si aucune langue n'est précisée, on applique la langue par défaut        
        String lang = workLanguage;

        if (readStruct.literal.getLanguage().isPresent()) {
            lang = readStruct.literal.getLanguage().get();
        }

        ///// récupération des informations sur le thésaurus DC-Terms
        if (SKOSProperty.ConceptScheme == readStruct.resource.getProperty()) {
            // remplir un tableau de dublin-core pour les métas-données 
            switch (readStruct.property.getLocalName()) {
                case "title":
                    readStruct.resource.getThesaurus().setTitle(readStruct.literal.getLabel());
                    readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.prefLabel);
                    return false;
                case "creator":
                    readStruct.resource.getThesaurus().setCreator(readStruct.literal.getLabel());
                    return false;
                case "contributor":
                    readStruct.resource.getThesaurus().setContributor(readStruct.literal.getLabel());
                    return false;
                case "publisher":
                    readStruct.resource.getThesaurus().setPublisher(readStruct.literal.getLabel());
                    return false;
                case "description":
                    readStruct.resource.getThesaurus().setDescription(readStruct.literal.getLabel());
                    return false;
                case "type":
                    readStruct.resource.getThesaurus().setType(readStruct.literal.getLabel());
                    return false;
                case "rights":
                    readStruct.resource.getThesaurus().setRights(readStruct.literal.getLabel());
                    return false;
                case "subject":
                    readStruct.resource.getThesaurus().setSubject(readStruct.literal.getLabel());
                    return false;
                case "coverage":
                    readStruct.resource.getThesaurus().setCoverage(readStruct.literal.getLabel());
                    return false;
                case "language":
                    readStruct.resource.getThesaurus().setLanguage(readStruct.literal.getLabel());
                    return false;
                case "relation":
                    readStruct.resource.getThesaurus().setRelation(readStruct.literal.getLabel());
                    return false;
                case "source":
                    readStruct.resource.getThesaurus().setSource(readStruct.literal.getLabel());
                    return false;
                case "created":
                    readStruct.resource.getThesaurus().setCreated(new FileUtilities().getDateFromString(readStruct.literal.getLabel()));
                    return false;
                case "modified":
                    readStruct.resource.getThesaurus().setModified(new FileUtilities().getDateFromString(readStruct.literal.getLabel()));
                    return false;
            }
        }

        switch (readStruct.property.getLocalName()) {
            case "prefLabel":
                readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.prefLabel);
                return false;
            case "altLabel":
                readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.altLabel);
                return false;
            case "hiddenLabel":
                readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.hiddenLabel);
                return false;
            default:
                return true;
        }

    }

    /**
     * lit les balise de Date
     *
     * @param readStruct
     * @return false si on a lus une balise de Date true sinon
     */
    private boolean readDate(ReadStruct readStruct) {

        switch (readStruct.property.getLocalName()) {
            case "created":
                readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.created);
                return false;
            case "modified":
                readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.modified);
                return false;
            case "date":
                readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.date);
                return false;
            default:
                return true;
        }
    }

    /**
     * lit les balise de Creator
     *
     * @param readStruct
     * @return false si on a lus une balise de Creator true sinon
     */
    private boolean readCreator(ReadStruct readStruct) {
        switch (readStruct.property.getLocalName()) {
            case "creator":
                readStruct.resource.addCreator(readStruct.literal.getLabel(), SKOSProperty.creator);
                return false;
            case "contributor":
                readStruct.resource.addCreator(readStruct.literal.getLabel(), SKOSProperty.contributor);
                return false;
            default:
                return true;
        }
    }

    /**
     * lit les balise de GPSCoordinates
     *
     * @param readStruct
     * @return false si on a lus une balise de GPSCoordinates true sinon
     */
    private boolean readGPSCoordinates(ReadStruct readStruct) {
        switch (readStruct.property.getLocalName()) {
            case "lat":
                readStruct.resource.getGPSCoordinates().setLat(readStruct.literal.getLabel());
                return false;
            case "long":
                readStruct.resource.getGPSCoordinates().setLon(readStruct.literal.getLabel());
                return false;
            default:
                return true;
        }
    }

    /**
     * lit les balise de Notation
     *
     * @param readStruct
     * @return false si on a lus une balise de Notation true sinon
     */
    private boolean readNotation(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("notation")) {
            readStruct.resource.addNotation(readStruct.literal.getLabel());
            return false;
        } else {
            return true;
        }
    }

    /**
     * lit les balise de image Foaf
     *
     * @param readStruct
     * @return false si on a lus une balise de Notation true sinon
     */
    private boolean readImage(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equalsIgnoreCase("Image")) {
            NodeImage nodeImage = new NodeImage();
            nodeImage.setImageName("");
            nodeImage.setCopyRight("");
            nodeImage.setUri(readStruct.value.stringValue());
            readStruct.resource.addNodeImage(nodeImage);//ImageUri(readStruct.value.stringValue());

            return false;
        } else {
            return true;
        }
    }

    /**
     * lit les balise de Match
     *
     * @param readStruct
     * @return false si on a lus une balise de Match true sinon
     */
    private boolean readMatch(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("exactMatch")) {
            readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.exactMatch);
            return false;
        }
        if (readStruct.property.getLocalName().equals("closeMatch")) {
            readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.closeMatch);
            return false;
        }
        if (readStruct.property.getLocalName().equals("broadMatch")) {
            readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.broadMatch);
            return false;
        }
        if (readStruct.property.getLocalName().equals("relatedMatch")) {
            readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.relatedMatch);
            return false;
        }
        if (readStruct.property.getLocalName().equals("narrowMatch")) {
            readStruct.resource.addMatch(readStruct.value.toString(), SKOSProperty.narrowMatch);
            return false;
        } else {
            return true;
        }
    }

    /**
     * lit les balise de Identifier
     *
     * @param readStruct
     * @return false si on a lus une balise de Identifier true sinon
     */
    private boolean readIdentifier(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("identifier")) {
            readStruct.resource.setIdentifier(readStruct.literal.getLabel());
            addEquivalenceIdArk(readStruct);
            return false;
        } else {
            return true;
        }
    }

    /**
     * lit les balise de Identifier
     *
     * @param readStruct
     * @return false si on a lus une balise de Identifier true sinon
     */
    private boolean readDcSource(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("source")) {
            readStruct.resource.addDcRelations(readStruct.value.stringValue());
            return false;
        } else {
            return true;
        }
    }

    private void addEquivalenceIdArk(ReadStruct readStruct) {
        sKOSXmlDocument.getEquivalenceUriArkHandle().put(readStruct.resource.getUri(), readStruct.literal.getLabel());
    }

    /**
     *
     * @return l'objet qui contiens les données lus par readModel()
     */
    public SKOSXmlDocument getsKOSXmlDocument() {
        return sKOSXmlDocument;
    }

    public String getMessage() {
        return message;
    }

}
