package fr.cnrs.opentheso.core.imports.rdf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import fr.cnrs.opentheso.bdd.tools.FileUtilities;
import fr.cnrs.opentheso.core.exports.rdf4j.WriteRdf4j;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

/**
 * Classe qui permet le chargement et la lecture de fichier RDF
 */
public class ReadRdf4j {

    private String message = "";
    private String workLanguage;

    private SKOSXmlDocument sKOSXmlDocument;

    //Chargement des données rdf d'après un InputStream
    public ReadRdf4j(InputStream is, int type, boolean isCandidateImport, String workLanguage) throws IOException {

        this.workLanguage = workLanguage;
        this.sKOSXmlDocument = new SKOSXmlDocument();

        switch (type) {
            case 0:
                readModel(Rio.parse(is, "", RDFFormat.RDFXML), isCandidateImport);
                break;
            case 1:
                readModel(Rio.parse(is, "", RDFFormat.JSONLD), isCandidateImport);
                break;
            case 2:
                readModel(Rio.parse(is, "", RDFFormat.TURTLE), isCandidateImport);
                break;
            default:
                readModel(Rio.parse(is, "", RDFFormat.RDFJSON), isCandidateImport);

        }
    }

    /**
     * Permet de lire un fichier RDF précédament charger avec la fonction
     * laodModel() les données sont stoqué dans la variable thesaurus
     */
    private void readModel(Model model, boolean isCandidatImport) {

        String currentObject = null;
        ReadStruct readStruct = new ReadStruct();
        ArrayList<String> nonReco = new ArrayList<>();
        ConceptData conceptData = new ConceptData();

        for (Statement statement : model) {
            readStruct.value = statement.getObject();
            readStruct.property = statement.getPredicate();

            if (currentObject == null) {
                currentObject = statement.getSubject().stringValue();
            } else if (!currentObject.equalsIgnoreCase(statement.getSubject().stringValue())) {
                if (readStruct.value.stringValue().contains("foaf/0.1/Image")) {
                    setFoatImageDatas(readStruct);
                    continue;
                }
                readStruct.resource = new SKOSResource();
                currentObject = statement.getSubject().stringValue();
            }

            // pour exclure les balises de type (shema.org) sinon, ca produit une erreur quand on a un tableau
            if (!readStruct.property.getNamespace().contains("schema.org")
                    && !readStruct.property.getNamespace().contains("skos-xl")) {

                if (readStruct.value instanceof Literal) {
                    readStruct.literal = (Literal) readStruct.value;
        
                    String lang = workLanguage;
                    if (ObjectUtils.isNotEmpty(readStruct.literal) && readStruct.literal.getLanguage().isPresent()) {
                        lang = readStruct.literal.getLanguage().get();
                    }

                    if (SKOSProperty.CONCEPT_SCHEME == readStruct.resource.getProperty()) {
                        setThesaurusData(readStruct, lang);
                    } else {
                        setLabels(readStruct, lang);
                        readDocumentation(readStruct, lang);
                    }
                    continue;
                }

                if (readStruct.resource == null) {
                    readStruct.resource = new SKOSResource();
                }

                if (isCandidatImport && readStruct.property.getLocalName().equals("note")) {
                    readCandidateData(readStruct);
                    continue;
                }

                conceptData.addConceptData(readStruct, statement, nonReco, sKOSXmlDocument);
            }
        }

        if (CollectionUtils.isNotEmpty(nonReco)) {
            message = message + " Not readed RDF tag \n " + nonReco.toString();
        }
    }

    private void setFoatImageDatas(ReadStruct readStruct) {

        if (readStruct.property.getLocalName().equals("identifier")) {
            readStruct.resource.setIdentifier(readStruct.literal.getLabel());
        }

        if (readStruct.property.getLocalName().equals("title")) {
            readStruct.resource.getFoafImage().setImageName(readStruct.literal.getLabel());
        }

        if (readStruct.property.getLocalName().equals("rights")) {
            readStruct.resource.getFoafImage().setCopyRight(readStruct.literal.getLabel());
        }
    }

    private void readCandidateData(ReadStruct readStruct) {
        if (null == readStruct.literal.getLanguage().get()) {
            String lang = readStruct.literal.getLanguage().get();
            readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.NOTE);
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
                    readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.NOTE);
                    break;
            }
        }
    }

    /**
     * lit les balise de Documentation
     *
     * @param readStruct
     * @return false si on a lus une balise de Documentation true sinon
     */
    private boolean readDocumentation(ReadStruct readStruct, String lang) {
        switch (readStruct.property.getLocalName()) {
            case "definition":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.DEFINITION);
                return false;
            case "scopeNote":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.SCOPE_NOTE);
                return false;
            case "example":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.EXAMPLE);
                return false;
            case "historyNote":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.HISTORY_NOTE);
                return false;
            case "editorialNote":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.EDITORIAL_NOTE);
                return false;
            case "changeNote":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.CHANGE_NOTE);
                return false;
            case "note":
                readStruct.resource.addDocumentation(readStruct.literal.getLabel(), lang, SKOSProperty.NOTE);
                return false;
            default:
                return true;
        }

    }

    private boolean setLabels(ReadStruct readStruct, String lang) {
        switch (readStruct.property.getLocalName()) {
            case "prefLabel":
                readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.PREF_LABEL);
                return true;
            case "altLabel":
                readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.ALT_LABEL);
                return true;
            case "hiddenLabel":
                readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.HIDDEN_LABEL);
                return true;
            default:
                return false;
        }
    }

    private boolean setThesaurusData(ReadStruct readStruct, String lang) {
        switch (readStruct.property.getLocalName()) {
            case "title":
                readStruct.resource.getThesaurus().setTitle(readStruct.literal.getLabel());
                readStruct.resource.addLabel(readStruct.literal.getLabel(), lang, SKOSProperty.PREF_LABEL);
                return true;
            case "creator":
                readStruct.resource.getThesaurus().setCreator(readStruct.literal.getLabel());
                return true;
            case "contributor":
                readStruct.resource.getThesaurus().setContributor(readStruct.literal.getLabel());
                return true;
            case "publisher":
                readStruct.resource.getThesaurus().setPublisher(readStruct.literal.getLabel());
                return true;
            case "description":
                readStruct.resource.getThesaurus().setDescription(readStruct.literal.getLabel());
                return true;
            case "type":
                readStruct.resource.getThesaurus().setType(readStruct.literal.getLabel());
                return true;
            case "rights":
                readStruct.resource.getThesaurus().setRights(readStruct.literal.getLabel());
                return true;
            case "subject":
                readStruct.resource.getThesaurus().setSubject(readStruct.literal.getLabel());
                return true;
            case "coverage":
                readStruct.resource.getThesaurus().setCoverage(readStruct.literal.getLabel());
                return true;
            case "language":
                readStruct.resource.getThesaurus().setLanguage(readStruct.literal.getLabel());
                return true;
            case "relation":
                readStruct.resource.getThesaurus().setRelation(readStruct.literal.getLabel());
                return true;
            case "source":
                readStruct.resource.getThesaurus().setSource(readStruct.literal.getLabel());
                return true;
            case "created":
                readStruct.resource.getThesaurus().setCreated(new FileUtilities().getDateFromString(readStruct.literal.getLabel()));
                return true;
            case "modified":
                readStruct.resource.getThesaurus().setModified(new FileUtilities().getDateFromString(readStruct.literal.getLabel()));
                return true;
        }
        return false;
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
