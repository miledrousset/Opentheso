package fr.cnrs.opentheso.core.imports.rdf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import fr.cnrs.opentheso.bdd.tools.FileUtilities;
import fr.cnrs.opentheso.core.exports.rdf4j.WriteRdf4j;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;

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
 *
 * @author Quincy
 *
 * Classe qui permet le chargement et la lecture de fichier RDF
 */
public class ReadRdf4j {

    private Model model;
    private SKOSXmlDocument sKOSXmlDocument;
    private String message = "";
    private String workLanguage;
    
    Logger logger = LoggerFactory.getLogger(ReadRdf4j.class);


    /**
     * Chargement des données rdf d'après un InputStream
     */
    public ReadRdf4j(InputStream is, int type, boolean isCandidatImport, String workLanguage) throws IOException {
        model = null;
        this.workLanguage = workLanguage;
        sKOSXmlDocument = new SKOSXmlDocument();
        switch (type) {
            case 0:
                model = Rio.parse(is, "", RDFFormat.RDFXML);
                break;
            case 1:
                model = Rio.parse(is, "", RDFFormat.JSONLD);
                break;
            case 2:
                model = Rio.parse(is, "", RDFFormat.TURTLE);
                break;
            case 3:
                model = Rio.parse(is, "", RDFFormat.RDFJSON);
                break;                
        }
        readModel(isCandidatImport);
    }

    public void clean() {
        model.clear();
        model = null;
        sKOSXmlDocument = null;
        message = workLanguage = null;
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
    private void readModel(boolean isCandidatImport) {

        ReadStruct readStruct = new ReadStruct();
        readStruct.resource = null;
        boolean validProperty;
        //pour le debug : 
        ArrayList<String> nonReco = new ArrayList<>();

        for (Statement st : model) {

            readStruct.value = st.getObject();
            readStruct.property = st.getPredicate();

            // pour exclure les balises de type (shema.org) sinon, ca produit une erreur quand on a un tableau
            if(!readStruct.property.getNamespace().contains("schema.org") && !readStruct.property.getNamespace().contains("skos-xl")) {
                if (readStruct.value instanceof Literal) {
                    readStruct.literal = (Literal) readStruct.value;
                }
                //Concept or ConceptScheme or Collection or ConceptGroup
                if (readStruct.property.getLocalName().equals("type")) {
                    validProperty = false;
                    int prop = -1;
                    String type = readStruct.value.toString();

                    type = type.toUpperCase();
                    if (type.contains("ConceptScheme".toUpperCase())) {
                        prop = SKOSProperty.ConceptScheme;
                        sKOSXmlDocument.setTitle(st.getSubject().stringValue());
                        validProperty = true;
                    } else if (type.contains("ConceptGroup".toUpperCase())) {
                        prop = SKOSProperty.ConceptGroup;
                        validProperty = true;

                    } else if (type.contains("Theme".toUpperCase())) {
                        prop = SKOSProperty.Theme;
                        validProperty = true;
                    } else if (type.contains("MicroThesaurus".toUpperCase())) {
                        prop = SKOSProperty.MicroThesaurus;
                        validProperty = true;
                    } else if (type.contains("Collection".toUpperCase())) {
                        if (st.getSubject().toString().contains("Facet".toUpperCase())) {
                            prop = SKOSProperty.FACET;
                        } else {
                            prop = SKOSProperty.Collection;
                        }
                        validProperty = true;
                    } else if (type.contains("Concept".toUpperCase())) {
                        prop = SKOSProperty.Concept;
                        validProperty = true;
                    }
                    if(validProperty) {
                        String uri = st.getSubject().stringValue();
                        readStruct.resource = new SKOSResource(uri, prop);
                        if (prop == SKOSProperty.ConceptScheme) {
                            sKOSXmlDocument.setConceptScheme(readStruct.resource);
                        } else if (prop == SKOSProperty.FACET) {
                            sKOSXmlDocument.addFacet(readStruct.resource);
                        } else if (prop == SKOSProperty.ConceptGroup || prop == SKOSProperty.Collection || prop == SKOSProperty.Theme || prop == SKOSProperty.MicroThesaurus) {
                            sKOSXmlDocument.addGroup(readStruct.resource);
                        } else if (prop == SKOSProperty.Concept) {
                            sKOSXmlDocument.addconcept(readStruct.resource);
                        }else {
                                logger.info("This is how you configure Java Logging with SLF4J");
                        }
                    }

                } //Labelling Properties
                else if (readStruct.property.getLocalName().equals("note") && isCandidatImport) {
                    if (WriteRdf4j.STATUS_TAG.equals(readStruct.literal.getLanguage().get())) {
                        readStruct.resource.setSkosStatus(readStruct.literal.getLabel().split(WriteRdf4j.DELIMINATE));
                    } else if (WriteRdf4j.VOTE_TAG.equals(readStruct.literal.getLanguage().get())) {
                        readStruct.resource.addVote(readStruct.literal.getLabel().split(WriteRdf4j.DELIMINATE));
                    } else if (WriteRdf4j.DISCUSSION_TAG.equals(readStruct.literal.getLanguage().get())) {
                        readStruct.resource.addMessage(readStruct.literal.getLabel().split(WriteRdf4j.DELIMINATE));
                    } else {
                        String lang = readStruct.literal.getLanguage().get();
                        readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.note);
                    }

                }
                else if (readLabellingProperties(readStruct)) {
                    if(readStruct.resource == null)
                        readStruct.resource = new SKOSResource();
                    //Dates
                  /* désactivé par Miled (pose problèe avec les notations  
                    if (!isCandidatImport) {
                        readNote(readStruct);
                    }*/
                    if (readDate(readStruct)) {
                        //Semantic Relationships
                        if (readRelationships(readStruct)) {
                            //Documentation Properties
                            if (readDocumentation(readStruct)) {
                                if (readCreator(readStruct)) {
                                    if (readGPSCoordinates(readStruct)) {
                                        if (readNotation(readStruct)) {
                                            if (readIdentifier(readStruct)) {
                                                if (readMatch(readStruct)) {
                                                    if (readImage(readStruct)) {
                                                        if (readReplaces(readStruct)) { // pour le concepts dépréciés
                                                            if (readConceptStatus(readStruct)) { // pour le reconnaitre le status du concept
                                                                //debug
                                                                if (!nonReco.contains(readStruct.property.getLocalName())) {
                                                                    //System.out.println("non reconue : " + readStruct.property.getLocalName());
                                                                    if(!readStruct.property.getLocalName().contains("superOrdinate") 
                                                                            && !readStruct.property.getLocalName().contains("subordinateArray")
                                                                            && !readStruct.property.getLocalName().contains("description"))
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
        if (!nonReco.isEmpty()) {
            message = message + " Not readed RDF tag \n " +  nonReco.toString();
        }
    }

    /**
     * lit les balise de Documentation
     *
     * @param readStruct
     * @return false si on a lus une balise de Documentation true sinon
     */
    private boolean readDocumentation(ReadStruct readStruct) {
        String lang = workLanguage; 
        if(readStruct.literal == null) return true;
        // si aucune langue n'est précisée, on applique la langue par défaut
        if(readStruct.literal.getLanguage().isPresent()) {
            lang = readStruct.literal.getLanguage().get();
        }        
        
        if (readStruct.property.getLocalName().equals("definition")) {
            readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.definition);
            return false;
        } else if (readStruct.property.getLocalName().equals("scopeNote")) {
            readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.scopeNote);
            return false;
        } else if (readStruct.property.getLocalName().equals("example")) {
            readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.example);
            return false;
        } else if (readStruct.property.getLocalName().equals("historyNote")) {
            readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.historyNote);
            return false;
        } else if (readStruct.property.getLocalName().equals("editorialNote")) {
            readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.editorialNote);
            return false;
        } else if (readStruct.property.getLocalName().equals("changeNote")) {
            readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.changeNote);
            return false;
        } else if (readStruct.property.getLocalName().equals("note")) {
            readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.note);
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
    private boolean readReplaces(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("replaces")) {
            readStruct.resource.addReplaces(readStruct.value.toString(), SKOSProperty.replaces);
            return false;
        } else if (readStruct.property.getLocalName().equals("isReplacedBy")) {
            readStruct.resource.addReplaces(readStruct.value.toString(), SKOSProperty.isReplacedBy);
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

        if (readStruct.property.getLocalName().equals("broader")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.broader);
            return false;
        } else if (readStruct.property.getLocalName().equals("broaderGeneric")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.broaderGeneric);
            return false;
        } else if (readStruct.property.getLocalName().equals("broaderInstantial")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.broaderInstantial);
            return false;
        } else if (readStruct.property.getLocalName().equals("broaderPartitive")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.broaderPartitive);
            return false;
        } else if (readStruct.property.getLocalName().equals("narrower")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.narrower);
            return false;
        } else if (readStruct.property.getLocalName().equals("narrowerGeneric")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.narrowerGeneric);
            return false;
        } else if (readStruct.property.getLocalName().equals("narrowerInstantial")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.narrowerInstantial);
            return false;
        } else if (readStruct.property.getLocalName().equals("narrowerPartitive")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.narrowerPartitive);
            return false;
        } else if (readStruct.property.getLocalName().equals("related")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.related);
            return false;
        } else if (readStruct.property.getLocalName().equals("relatedHasPart")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.relatedHasPart);
            return false;
        } else if (readStruct.property.getLocalName().equals("relatedPartOf")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.relatedPartOf);
            return false;
        } else if (readStruct.property.getLocalName().equals("hasTopConcept")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.hasTopConcept);
            return false;
        } else if (readStruct.property.getLocalName().equals("inScheme")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.inScheme);
            return false;
        } else if (readStruct.property.getLocalName().equals("member")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.member);
            return false;
        } else if (readStruct.property.getLocalName().equals("topConceptOf")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.topConceptOf);
            return false;
        } else if (readStruct.property.getLocalName().equals("microThesaurusOf")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.microThesaurusOf);
            return false;
        } else if (readStruct.property.getLocalName().equals("subGroup")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.subGroup);
            return false;
        } else if (readStruct.property.getLocalName().equals("superGroup")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.superGroup);
            return false;
        } else if (readStruct.property.getLocalName().equals("hasMainConcept")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.hasMainConcept);
            return false;
        } else if (readStruct.property.getLocalName().equals("memberOf")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.memberOf);
            return false;
        } else if (readStruct.property.getLocalName().equals("mainConceptOf")) {
            readStruct.resource.addRelation(readStruct.value.toString(), SKOSProperty.mainConceptOf);
            return false;
        } else {
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
        if(readStruct.literal == null) return true;
        if(readStruct.resource == null) return true;
        
        // si aucune langue n'est précisée, on applique la langue par défaut        
        String lang = workLanguage; 
       
        if(readStruct.literal.getLanguage().isPresent()) {
            lang = readStruct.literal.getLanguage().get();
        }
            
        ///// récupération des informations sur le thésaurus DC-Terms
        if(SKOSProperty.ConceptScheme == readStruct.resource.getProperty()){
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
            
        if (readStruct.property.getLocalName().equals("prefLabel")) {
            readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.prefLabel);
            return false;
        } else if (readStruct.property.getLocalName().equals("altLabel")) {
            readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.altLabel);
            return false;
        } else if (readStruct.property.getLocalName().equals("hiddenLabel")) {
            readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.hiddenLabel);
            return false;
        } else {
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

        if (readStruct.property.getLocalName().equals("created")) {
            readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.created);
            return false;
        } else if (readStruct.property.getLocalName().equals("modified")) {
            readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.modified);
            return false;
        } else if (readStruct.property.getLocalName().equals("date")) {
            readStruct.resource.addDate(readStruct.literal.getLabel(), SKOSProperty.date);
            return false;
        } else {
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
        if (readStruct.property.getLocalName().equals("creator")) {
            readStruct.resource.addCreator(readStruct.literal.getLabel(), SKOSProperty.creator);
            return false;
        } else if (readStruct.property.getLocalName().equals("contributor")) {
            readStruct.resource.addCreator(readStruct.literal.getLabel(), SKOSProperty.contributor);
            return false;
        } else {
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
        if (readStruct.property.getLocalName().equals("lat")) {
            readStruct.resource.getGPSCoordinates().setLat(readStruct.literal.getLabel());
            return false;
        } else if (readStruct.property.getLocalName().equals("long")) {
            readStruct.resource.getGPSCoordinates().setLon(readStruct.literal.getLabel());
            return false;
        } else {
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


    private boolean readNote(ReadStruct readStruct) {
        if (readStruct.property.getLocalName().equals("note")) {
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
            readStruct.resource.addImageUri(readStruct.value.stringValue());
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
    //        System.out.println("id = " +  readStruct.literal.getLabel());
            return false;
        } else {
            return true;
        }
    }

    private void addEquivalenceIdArk(ReadStruct readStruct){
        sKOSXmlDocument.getEquivalenceUriArkHandle().put(readStruct.resource.getUri(),readStruct.literal.getLabel());
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
