package fr.cnrs.opentheso.core.imports.rdf4j.nouvelle;

import fr.cnrs.opentheso.bdd.tools.FileUtilities;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;


public class GenericReader {

    public void setThesaurusData(SKOSResource skosConcept, IRI predicate, Literal literal, String lang) {
        switch (predicate.getLocalName()) {
            case "title":
                skosConcept.getThesaurus().setTitle(literal.getLabel());
                skosConcept.addLabel(literal.getLabel(), lang, SKOSProperty.prefLabel);
                break;
            case "creator":
                skosConcept.getThesaurus().setCreator(literal.getLabel());
                break;
            case "contributor":
                skosConcept.getThesaurus().setContributor(literal.getLabel());
                break;
            case "publisher":
                skosConcept.getThesaurus().setPublisher(literal.getLabel());
                break;
            case "description":
                skosConcept.getThesaurus().setDescription(literal.getLabel());
                break;
            case "type":
                skosConcept.getThesaurus().setType(literal.getLabel());
                break;
            case "rights":
                skosConcept.getThesaurus().setRights(literal.getLabel());
                break;
            case "subject":
                skosConcept.getThesaurus().setSubject(literal.getLabel());
                break;
            case "coverage":
                skosConcept.getThesaurus().setCoverage(literal.getLabel());
                break;
            case "language":
                skosConcept.getThesaurus().setLanguage(literal.getLabel());
                break;
            case "relation":
                skosConcept.getThesaurus().setRelation(literal.getLabel());
                break;
            case "source":
                skosConcept.getThesaurus().setSource(literal.getLabel());
                break;
            case "created":
                skosConcept.getThesaurus().setCreated(new FileUtilities().getDateFromString(literal.getLabel()));
                break;
            case "modified":
                skosConcept.getThesaurus().setModified(new FileUtilities().getDateFromString(literal.getLabel()));
                break;
        }
    }

    public void setFoatImageDatas(SKOSResource skosResource, Literal literal, IRI predicate) {

        if (predicate.getLocalName().equals("identifier")) {
            skosResource.setIdentifier(literal.getLabel());
        }

        if (predicate.getLocalName().equals("title")) {
            skosResource.getFoafImage().setImageName(literal.getLabel());
        }

        if (predicate.getLocalName().equals("rights")) {
            skosResource.getFoafImage().setCopyRight(literal.getLabel());
        }
    }

}
