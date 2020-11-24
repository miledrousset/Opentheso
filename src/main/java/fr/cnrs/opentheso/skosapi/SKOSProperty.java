package fr.cnrs.opentheso.skosapi;

/**
 *
 * @author Miled Rousset
 *
 */
public interface SKOSProperty {

    //Semantic Relationships
    int broader = 0;
    int narrower = 1;
    int related = 2;
    int hasTopConcept = 3;
    int inScheme = 4;
    int member = 5;
    int topConceptOf = 6;
    int microThesaurusOf = 7;
    int subGroup = 8;
    int superGroup = 9;
    int hasMainConcept = 10;
    int memberOf = 11;
    int mainConceptOf = 12;
    int broaderGeneric =13;
    int broaderInstantial=14;
    int broaderPartitive=15;
    int narrowerGeneric=16;
    int narrowerInstantial=17;
    int narrowerPartitive=18;
    int relatedHasPart=19;
    int TOP_FACET = 20;
    int FACET = 21;
    int relatedPartOf=-1;
    
    

    //Symbolic Labelling
    int prefSymbol = 20;
    int altSymbol = 21;

    //Documentation Properties
    int definition = 30;
    int scopeNote = 31;
    int example = 32;
    int historyNote = 33;
    int editorialNote = 34;
    int changeNote = 35;
    int note = 36;

    //Labelling Properties
    int prefLabel = 40;
    int altLabel = 41;
    int hiddenLabel=42;

    //Dates
    int created = 50;
    int modified = 51;
    int date = 52;
    
    //Identifier
    int identifier = 55;

    //Creation
    int creator = 60;
    int contributor = 61;

    //Match
    int exactMatch = 70;
    int closeMatch = 71;
    int broadMatch = 72;
    int relatedMatch = 73;
    int narrowMatch = 74;

    //Ressource
    int Concept = 80;
    int ConceptScheme = 81;
    int Collection = 82;
    int ConceptGroup = 83;
    int MicroThesaurus = 84;
    int Theme = 85;

    int message = 86;

}
