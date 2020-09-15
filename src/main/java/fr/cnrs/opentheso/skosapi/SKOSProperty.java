package fr.cnrs.opentheso.skosapi;

/**
 *
 * @author Miled Rousset
 *
 */
public interface SKOSProperty {

    //Semantic Relationships
    public static final int broader = 0;
    public static final int narrower = 1;
    public static final int related = 2;
    public static final int hasTopConcept = 3;
    public static final int inScheme = 4;
    public static final int member = 5;
    public static final int topConceptOf = 6;
    public static final int microThesaurusOf = 7;
    public static final int subGroup = 8;
    public static final int superGroup = 9;
    public static final int hasMainConcept = 10;
    public static final int memberOf = 11;
    public static final int mainConceptOf = 12;
    public static final int broaderGeneric =13;
    public static final int broaderInstantial=14;
    public static final int broaderPartitive=15;
    public static final int narrowerGeneric=16;
    public static final int narrowerInstantial=17;
    public static final int narrowerPartitive=18;
    public static final int relatedHasPart=19;
    public static final int relatedPartOf=-1;
    
    

    //Symbolic Labelling
    public static final int prefSymbol = 20;
    public static final int altSymbol = 21;

    //Documentation Properties
    public static final int definition = 30;
    public static final int scopeNote = 31;
    public static final int example = 32;
    public static final int historyNote = 33;
    public static final int editorialNote = 34;
    public static final int changeNote = 35;
    public static final int note = 36;

    //Labelling Properties
    public static final int prefLabel = 40;
    public static final int altLabel = 41;
    public static final int hiddenLabel=42;

    //Dates
    public static final int created = 50;
    public static final int modified = 51;
    public static final int date = 52;
    
    //Identifier
    public static final int identifier = 55;

    //Creation
    public static final int creator = 60;
    public static final int contributor = 61;

    //Match
    public static final int exactMatch = 70;
    public static final int closeMatch = 71;
    public static final int broadMatch = 72;
    public static final int relatedMatch = 73;
    public static final int narrowMatch = 74;

    //Ressource
    public static final int Concept = 80;
    public static final int ConceptScheme = 81;
    public static final int Collection = 82;
    public static final int ConceptGroup = 83;
    public static final int MicroThesaurus = 84;
    public static final int Theme = 85;

    public static final int message = 86;
    
 
    

}
