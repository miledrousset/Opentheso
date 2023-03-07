/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.rdf4j;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnrs.opentheso.skosapi.*;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfResource;
/**
 *
 * @author Quincy
 */
public class WriteRdf4j {

    public final static String DELIMINATE = "##";
    public final static String STATUS_TAG = "status";
    public final static String DISCUSSION_TAG = "message";
    public final static String VOTE_TAG = "vote";

    private Model model;
    private ModelBuilder builder;
    private SKOSXmlDocument xmlDocument;
    private ValueFactory vf;

    /**
     *
     * @param xmlDocument
     */
    public WriteRdf4j(SKOSXmlDocument xmlDocument) {
        this.xmlDocument = xmlDocument;
        vf = SimpleValueFactory.getInstance();
        loadModel();
        writeModel();
        model = builder.build();
    }

    public void closeCache() {
        model.clear();
        model = null;
        vf = null;
        builder = null;
    //    System.gc();
    }

    private void loadModel() {
        builder = new ModelBuilder();
        builder.setNamespace("skos", "http://www.w3.org/2004/02/skos/core#");
        builder.setNamespace("dc", "http://purl.org/dc/elements/1.1/");
        builder.setNamespace("dcterms", "http://purl.org/dc/terms/");
        builder.setNamespace("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        builder.setNamespace("iso-thes", "http://purl.org/iso25964/skos-thes#");
        builder.setNamespace("opentheso", "http://purl.org/umu/uneskos#");
        builder.setNamespace("foaf", "http://xmlns.com/foaf/0.1/");
        builder.setNamespace("owl", "http://www.w3.org/2002/07/owl#");
    }

    private void writeModel() {
        writeConceptScheme();
        writeGroup();
        writeFacet();
        writeConcept();
    }

    private void writeConcept() {
    //    int i = 0;
        for (SKOSResource sResource : xmlDocument.getConceptList()) {
            builder.subject(vf.createIRI(sResource.getUri()));
            builder.add(RDF.TYPE, SKOS.CONCEPT);
            writeLabel(sResource);
            writeRelation(sResource);
            writeMatch(sResource);
            writeNotation(sResource);
            writeDate(sResource);
            writeIdentifier(sResource);
            writePath(sResource);
            writeCreator(sResource);
            writeDocumentation(sResource);
            writeGPS(sResource);
            writeExternalResources(sResource);            

            writeStatusCandidat(sResource);
            writeDiscussions(sResource);
            writeVotes(sResource);

            writeStatus(sResource);
            writeReplaces(sResource);
            
            // écriture d'un objet FOF pour les images
        //    writeImageUri(sResource);            
            writeNodeImage(sResource);
        }
    }

    private void writeNodeImage(SKOSResource resource) {
        for (NodeImage nodeImage : resource.getNodeImage()) {
            if(StringUtils.isEmpty(nodeImage.getUri())) continue;
            
            builder.subject(vf.createIRI(nodeImage.getUri().replaceAll(" ", "%20")));
            builder.add(RDF.TYPE, FOAF.IMAGE);
            builder.add(DCTERMS.IDENTIFIER, resource.getSdc().getIdentifier());
            if(!StringUtils.isEmpty(nodeImage.getImageName()))
                builder.add(DCTERMS.TITLE, nodeImage.getImageName());
            if(!StringUtils.isEmpty(nodeImage.getCopyRight()))
                builder.add(DCTERMS.RIGHTS, nodeImage.getCopyRight());
        }
    }    
    /*
    private void writeImageUri(SKOSResource resource) {
        if (resource.getNodeImage() != null ) {
            if (!resource.getNodeImage().isEmpty()) {
                for (NodeImage nodeImage : resource.getNodeImage()) {
                    try {
                        nodeImage.setUri(nodeImage.getUri().replaceAll(" ", "%20"));
                        IRI uri = vf.createIRI(nodeImage.getUri());
                        builder.add(FOAF.IMAGE, uri);
                    } catch (Exception e) {
                        return;
                    }
                }
            }
        }
    }*/

    private void writeReplaces(SKOSResource resource) {
        for (SKOSReplaces replace : resource.getsKOSReplaces()) {
            IRI uri = vf.createIRI(replace.getTargetUri());
            int prop = replace.getProperty();
            switch (prop) {
                case SKOSProperty.isReplacedBy:
                    builder.add(DCTERMS.IS_REPLACED_BY, uri);
                    break;
                case SKOSProperty.replaces:
                    builder.add(DCTERMS.REPLACES, uri);
                    break;
            }
        }
    }

    private void writeStatus(SKOSResource resource) {
        int prop = resource.getStatus();
        switch (prop) {
            case SKOSProperty.deprecated:
                builder.add(OWL.DEPRECATED, true);
                break;
            default:
                break;
        }
    }

    private void writeFacet() {
        for (SKOSResource facet : xmlDocument.getFacetList()) {
            builder.subject(vf.createIRI(facet.getUri()));
            builder.add(RDF.TYPE, vf.createIRI("http://purl.org/iso25964/skos-thes#ThesaurusArray"));//vf.createIRI(facet.getUri()));//RDF.TYPE, SKOS.COLLECTION);
            writeRelation(facet);
            writeLabel(facet);
            writeDate(facet);
        }
    }

    private void writeGroup() {
        for (SKOSResource group : xmlDocument.getGroupList()) {
            builder.subject(vf.createIRI(group.getUri()));
            builder.add(RDF.TYPE, SKOS.COLLECTION);
            writeLabel(group);
            writeRelation(group);
            writeMatch(group);
            writeNotation(group);
            writeDate(group);
            writeIdentifier(group);
            writeCreator(group);
            writeDocumentation(group);
            writeGPS(group);

        }
    }

    private void writeConceptScheme() {
        if (xmlDocument.getConceptScheme() == null) {
            return;
        }
        SKOSResource conceptScheme = xmlDocument.getConceptScheme();
        builder.subject(vf.createIRI(conceptScheme.getUri()));//createURI(conceptScheme.getUri()));

        builder.add(RDF.TYPE, SKOS.CONCEPT_SCHEME);


        writeLabel(conceptScheme);
        writeRelation(conceptScheme);
        writeMatch(conceptScheme);
        writeNotation(conceptScheme);
        writeDate(conceptScheme);
        writeIdentifier(conceptScheme);
        writeCreator(conceptScheme);
        writeDocumentation(conceptScheme);
        writeGPS(conceptScheme);
        writeDcTerms(conceptScheme);
    }

    private void writeGPS(SKOSResource resource) {
        SKOSGPSCoordinates gps = resource.getGPSCoordinates();
        Double lat = null;
        Double lon = null;
        if(gps.getLat() == null || gps.getLon() == null) return;
        try {
            lat = Double.valueOf(gps.getLat());
            lon = Double.valueOf(gps.getLon());
        } catch (NumberFormatException e) {
        }
        if (lat != null && lon != null) {
            builder.add("geo:lat", lat);
            builder.add("geo:long", lon);
        }
    }
    
    private void writeExternalResources(SKOSResource resource) {
        ArrayList<String> externalResources = resource.getExternalResources();
        for (String externalResource : externalResources) {
            builder.add(DCTERMS.SOURCE, externalResource);
        }
    }    

    private void writeDocumentation(SKOSResource resource) {
        int prop;
        for (SKOSDocumentation doc : resource.getDocumentationsList()) {
            prop = doc.getProperty();
            Literal literal = vf.createLiteral(doc.getText(), doc.getLanguage());
            switch (prop) {
                case SKOSProperty.definition:
                    builder.add(SKOS.DEFINITION, literal);
                    break;
                case SKOSProperty.scopeNote:
                    builder.add(SKOS.SCOPE_NOTE, literal);
                    break;
                case SKOSProperty.example:
                    builder.add(SKOS.EXAMPLE, literal);
                    break;
                case SKOSProperty.historyNote:
                    builder.add(SKOS.HISTORY_NOTE, literal);
                    break;
                case SKOSProperty.editorialNote:
                    builder.add(SKOS.EDITORIAL_NOTE, literal);
                    break;
                case SKOSProperty.changeNote:
                    builder.add(SKOS.CHANGE_NOTE, literal);
                    break;
                case SKOSProperty.note:
                    builder.add(SKOS.NOTE, literal);
                    break;
                default:
                    break;
            }
        }
    }

    private void writeCreator(SKOSResource resource) {
        int prop;
        for (SKOSCreator creator : resource.getCreatorList()) {
            if (creator == null) {
                return;
            }
            if (creator.getCreator() == null) {
                return;
            }
            if (creator.getCreator().isEmpty()) {
                return;
            }

            prop = creator.getProperty();
            if (prop == SKOSProperty.creator) {
                builder.add(DCTERMS.CREATOR, creator.getCreator());
            } else if (prop == SKOSProperty.contributor) {
                builder.add(DCTERMS.CONTRIBUTOR, creator.getCreator());
            }
        }
    }

    private void writeDate(SKOSResource resource) {
        int prop;
        Literal literal;

        for (SKOSDate date : resource.getDateList()) {
            literal = vf.createLiteral(date.getDate(), XMLSchema.DATE);
            prop = date.getProperty();
            switch (prop) {
                case SKOSProperty.created:
                    builder.add(DCTERMS.CREATED, literal);
                    break;
                case SKOSProperty.modified:
                    builder.add(DCTERMS.MODIFIED, literal);
                    break;
                case SKOSProperty.date:
                    builder.add(DCTERMS.DATE, literal);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * Pour écrire le chemin complet / autopostage
     *
     * @param resource
     */
    private void writePath(SKOSResource resource) {
        if (resource.getPaths() != null) {
            if (!resource.getPaths().isEmpty()) {
                for (String path1 : resource.getPaths()) {
                    builder.add(DCTERMS.DESCRIPTION, path1.trim());
                }
            }
        }
    }

    private void writeIdentifier(SKOSResource resource) {
        if (resource.getSdc() != null) {
            builder.add(DCTERMS.IDENTIFIER, resource.getSdc().getIdentifier());
        }
    }

    private void writeNotation(SKOSResource resource) {
        for (SKOSNotation notation : resource.getNotationList()) {
            if (notation == null) {
                return;
            }
            if (notation.getNotation() == null) {
                return;
            }
            if (notation.getNotation().isEmpty()) {
                return;
            }

            builder.add(SKOS.NOTATION, notation.getNotation());
        }
    }

    private void writeDiscussions(SKOSResource resource) {
        for (SKOSDiscussion discussion : resource.getMessages()) {
            builder.add(SKOS.NOTE, vf.createLiteral(discussion.getDate() + DELIMINATE + discussion.getIdUser() + DELIMINATE + discussion.getMsg(), DISCUSSION_TAG));
        }
    }

    private void writeVotes(SKOSResource resource) {
        for (SKOSVote vote : resource.getVotes()) {
            builder.add(SKOS.NOTE, vf.createLiteral(vote.getIdThesaurus() + DELIMINATE + vote.getIdConcept() + DELIMINATE + vote.getValueNote()
                    + DELIMINATE + vote.getIdUser() + DELIMINATE + vote.getTypeVote(), VOTE_TAG));
        }
    }

    private void writeLabel(SKOSResource resource) {
        int prop;
        for (SKOSLabel label : resource.getLabelsList()) {
            prop = label.getProperty();
            Literal literal = vf.createLiteral(label.getLabel(), label.getLanguage());
            switch (prop) {
                case SKOSProperty.prefLabel:
                    builder.add(SKOS.PREF_LABEL, literal);
                    break;
                case SKOSProperty.altLabel:
                    builder.add(SKOS.ALT_LABEL, literal);
                    break;
                case SKOSProperty.hiddenLabel:
                    builder.add(SKOS.HIDDEN_LABEL, literal);
                    break;
                default:
                    break;
            }
        }
    }

    private void writeDcTerms(SKOSResource resource){
        if(resource.getThesaurus().getTitle()!= null && !resource.getThesaurus().getTitle().isEmpty())
            builder.add(DCTERMS.TITLE, resource.getThesaurus().getTitle());

        if(resource.getThesaurus().getCreator()!= null && !resource.getThesaurus().getCreator().isEmpty())
            builder.add(DCTERMS.CREATOR, resource.getThesaurus().getCreator());

        if(resource.getThesaurus().getContributor() != null && !resource.getThesaurus().getContributor().isEmpty())
            builder.add(DCTERMS.CONTRIBUTOR, resource.getThesaurus().getContributor());

        if(resource.getThesaurus().getPublisher()!= null && !resource.getThesaurus().getPublisher().isEmpty())
            builder.add(DCTERMS.PUBLISHER, resource.getThesaurus().getPublisher());

        if(resource.getThesaurus().getDescription()!= null && !resource.getThesaurus().getDescription().isEmpty())
            builder.add(DCTERMS.DESCRIPTION, resource.getThesaurus().getDescription());

        if(resource.getThesaurus().getType()!= null && !resource.getThesaurus().getType().isEmpty())
            builder.add(DCTERMS.TYPE, resource.getThesaurus().getType());

        if(resource.getThesaurus().getRights()!= null && !resource.getThesaurus().getRights().isEmpty())
            builder.add(DCTERMS.RIGHTS, resource.getThesaurus().getRights());

        if(resource.getThesaurus().getSubject()!= null && !resource.getThesaurus().getSubject().isEmpty())
            builder.add(DCTERMS.SUBJECT, resource.getThesaurus().getSubject());

        if(resource.getThesaurus().getCoverage()!= null && !resource.getThesaurus().getCoverage().isEmpty())
            builder.add(DCTERMS.COVERAGE, resource.getThesaurus().getCoverage());

        if(resource.getThesaurus().getLanguage()!= null && !resource.getThesaurus().getLanguage().isEmpty())
            builder.add(DCTERMS.LANGUAGE, resource.getThesaurus().getLanguage());

        if(resource.getThesaurus().getRelation()!= null && !resource.getThesaurus().getRelation().isEmpty())
            builder.add(DCTERMS.RELATION, resource.getThesaurus().getRelation());

        if(resource.getThesaurus().getSource()!= null && !resource.getThesaurus().getSource().isEmpty())
            builder.add(DCTERMS.SOURCE, resource.getThesaurus().getSource());
    }

    private void writeMatch(SKOSResource resource) {
        int prop;
        IRI uri;
        for (SKOSMatch match : resource.getMatchList()) {
            prop = match.getProperty();
            try {
                uri = vf.createIRI(match.getValue());
                switch (prop) {
                    case SKOSProperty.exactMatch:
                        builder.add(SKOS.EXACT_MATCH, uri);
                        break;
                    case SKOSProperty.closeMatch:
                        builder.add(SKOS.CLOSE_MATCH, uri);
                        break;
                    case SKOSProperty.broadMatch:
                        builder.add(SKOS.BROAD_MATCH, uri);
                        break;
                    case SKOSProperty.relatedMatch:
                        builder.add(SKOS.RELATED_MATCH, uri);
                        break;
                    case SKOSProperty.narrowMatch:
                        builder.add(SKOS.NARROW_MATCH, uri);
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                Logger.getLogger(WriteRdf4j.class.getName()).log(Level.SEVERE,
                        "Error URI Alignement : " + resource.getUri(), ex);
            }
        }
    }

    private void writeStatusCandidat(SKOSResource resource) {
        if (resource.getSkosStatus() != null && !StringUtils.isEmpty(resource.getSkosStatus().getIdStatus())) {
            builder.add(SKOS.NOTE, vf.createLiteral(resource.getSkosStatus().getIdStatus() + DELIMINATE + resource.getSkosStatus().getMessage()
                    + DELIMINATE + resource.getSkosStatus().getIdUser() + DELIMINATE + resource.getSkosStatus().getDate(), STATUS_TAG));
        }
    }

    private void writeRelation(SKOSResource resource) {
        int prop;

        for (SKOSRelation relation : resource.getRelationsList()) {

            IRI uri = vf.createIRI(relation.getTargetUri());
            prop = relation.getProperty();
            switch (prop) {
                case SKOSProperty.member:
                    builder.add(SKOS.MEMBER, uri);
                    break;
                case SKOSProperty.broader:
                    builder.add(SKOS.BROADER, uri);
                    break;
                case SKOSProperty.broaderGeneric:
                    builder.add("iso-thes:broaderGeneric", uri);
                    break;
                case SKOSProperty.broaderInstantial:
                    builder.add("iso-thes:broaderInstantial", uri);
                    break;
                case SKOSProperty.broaderPartitive:
                    builder.add("iso-thes:broaderPartitive", uri);
                    break;
                case SKOSProperty.narrower:
                    builder.add(SKOS.NARROWER, uri);
                    break;
                case SKOSProperty.narrowerGeneric:
                    builder.add("iso-thes:narrowerGeneric", uri);
                    break;
                case SKOSProperty.narrowerInstantial:
                    builder.add("iso-thes:narrowerInstantial", uri);
                    break;
                case SKOSProperty.narrowerPartitive:
                    builder.add("iso-thes:narrowerPartitive", uri);
                    break;
                case SKOSProperty.related:
                    builder.add(SKOS.RELATED, uri);
                    break;
                case SKOSProperty.relatedHasPart:
                    builder.add("iso-thes:relatedHasPart", uri);
                    break;
                case SKOSProperty.relatedPartOf:
                    builder.add("iso-thes:relatedPartOf", uri);
                    break;
                case SKOSProperty.hasTopConcept:
                    builder.add(SKOS.HAS_TOP_CONCEPT, uri);
                    break;
                case SKOSProperty.inScheme:
                    builder.add(SKOS.IN_SCHEME, uri);
                    break;
                case SKOSProperty.topConceptOf:
                    builder.add(SKOS.TOP_CONCEPT_OF, uri);
                    break;
                case SKOSProperty.subGroup:
                    builder.add("iso-thes:subGroup", uri);
                    break;
                case SKOSProperty.microThesaurusOf:
                    builder.add("iso-thes:microThesaurusOf", uri);
                    break;
                case SKOSProperty.superGroup:
                    builder.add("iso-thes:superGroup", uri);
                    break;

                /// unesco properties for Groups or Collections    
                case SKOSProperty.memberOf:
                    builder.add("opentheso:memberOf", uri);
                    break;

/*                case SKOSProperty.TOP_FACET:
                    builder.add("opentheso:topFacet", uri);
                    break;
                case SKOSProperty.FACET:
                    builder.add("opentheso:facet", uri);
                    break;*/
                case SKOSProperty.superOrdinate:
                    builder.add("iso-thes:superOrdinate", uri);
                    break;
                case SKOSProperty.subordinateArray:
                    builder.add("iso-thes:subordinateArray", uri);
                    break;                       
            }

        }

    }

    //********
    public Model getModel() {
        return model;
    }

    public SKOSXmlDocument getsKOSXmlDocument() {
        return xmlDocument;
    }

    public void setsKOSXmlDocument(SKOSXmlDocument xmlDocument) {
        this.xmlDocument = xmlDocument;
    }

}