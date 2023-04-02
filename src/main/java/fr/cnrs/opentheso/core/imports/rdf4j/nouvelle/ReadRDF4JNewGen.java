package fr.cnrs.opentheso.core.imports.rdf4j.nouvelle;

import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;

import org.apache.commons.lang3.ObjectUtils;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

import java.io.IOException;
import java.io.InputStream;


public class ReadRDF4JNewGen {

    private final static String RESOURCE_TAG = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    private final static String CONCEPT_SCHEME_TAG = "http://www.w3.org/2004/02/skos/core#ConceptScheme";
    private final static String COLLECTION_TAG = "http://www.w3.org/2004/02/skos/core#Collection";
    private final static String FACET_TAG = "http://purl.org/iso25964/skos-thes#ThesaurusArray";
    private final static String CONCEPT_TAG = "http://www.w3.org/2004/02/skos/core#Concept";
    private final static String IMAGE_TAG = "http://xmlns.com/foaf/0.1/Image";


    public SKOSXmlDocument readRdfFlux(InputStream inputStream, RDFFormat rdfFormat, String defaultLang) throws IOException {

        SKOSXmlDocument skosXmlDocument = new SKOSXmlDocument();
        RDFParser parser = Rio.createParser(rdfFormat);
        parser.setRDFHandler(new AbstractRDFHandler() {

            private Resource subject;
            private IRI predicate;
            private Value value;
            private boolean isConceptScheme;
            private boolean isCollection;
            private boolean isFacet;
            private boolean isConcept;
            private boolean isImage;
            private SKOSResource skosResource;
            private ThesaurusManager fileManager = new ThesaurusManager();
            private ConceptReader conceptReader = new ConceptReader();
            private GenericReader genericReader = new GenericReader();

            @Override
            public void handleStatement(Statement st) throws RDFHandlerException {

                predicate = st.getPredicate();
                value = st.getObject();
                subject = st.getSubject();

                System.out.println("-------------------------------");
                System.out.println("-- Predicate : " + predicate.stringValue());
                System.out.println("-- Value : " + st.getObject().stringValue());
                System.out.println("-- Subject : " + subject.stringValue());
                System.out.println("-------------------------------");

                if (st.getObject().stringValue().equals("Wikidata_sparql")) {
                    System.out.println("-------------------------------");
                }

                if (RESOURCE_TAG.equalsIgnoreCase(predicate.stringValue())) {
                    addNewResources();

                    if (!isConceptScheme && !isFacet && !isConcept && !isCollection && !isImage) {
                        skosResource = new SKOSResource();
                        skosResource.setUri(subject.stringValue());
                        if (CONCEPT_SCHEME_TAG.equals(value.stringValue())) {
                            isConceptScheme = true;
                        } else if (COLLECTION_TAG.equals(value.stringValue())) {
                            isCollection = true;
                        } else if (FACET_TAG.equals(value.stringValue())) {
                            isFacet = true;
                        } else if (CONCEPT_TAG.equals(value.stringValue())) {
                            isConcept = true;
                        } else if (IMAGE_TAG.equals(value.stringValue())) {
                            isImage = true;
                        }
                    }
                }

                String lang = defaultLang;
                Literal literal = null;
                if (value instanceof Literal) {
                    literal = (Literal) value;
                    if (ObjectUtils.isNotEmpty(literal) && literal.getLanguage().isPresent()) {
                        lang = literal.getLanguage().get();
                    }

                    if (isConceptScheme) {
                        genericReader.setThesaurusData(skosResource, predicate, literal, lang);
                    } else if (isImage) {
                        genericReader.setFoatImageDatas(skosResource, literal, predicate);
                    } else {
                        conceptReader.readGeneric(skosResource, predicate, literal, lang);
                    }
                } else {
                    conceptReader.readConcept(skosXmlDocument, skosResource, predicate, value, literal);
                }
            }

            @Override
            public void endRDF() throws RDFHandlerException {
                addNewResources();
            }

            private void addNewResources() {
                if (isConceptScheme) {
                    isConceptScheme = false;
                    skosXmlDocument.setTitle(subject.stringValue());
                    fileManager.addStructData(skosResource, subject, skosXmlDocument, SKOSProperty.ConceptScheme);
                } else if (isFacet) {
                    isFacet = false;
                    fileManager.addStructData(skosResource, subject, skosXmlDocument, SKOSProperty.FACET);
                } else if (isConcept) {
                    isConcept = false;
                    fileManager.addStructData(skosResource, subject, skosXmlDocument, SKOSProperty.Concept);
                } else  if (isCollection) {
                    isCollection = false;
                    fileManager.addStructData(skosResource, subject, skosXmlDocument, SKOSProperty.Collection);
                } else  if (isImage) {
                    isImage = false;
                    fileManager.addStructData(skosResource, subject, skosXmlDocument, SKOSProperty.FoafImage);
                }
            }
        });

        parser.parse(inputStream, "");

        return skosXmlDocument;
    }
}
