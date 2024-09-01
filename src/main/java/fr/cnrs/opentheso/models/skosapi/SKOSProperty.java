package fr.cnrs.opentheso.models.skosapi;

/**
 *
 * @author Miled Rousset
 *
 */
public interface SKOSProperty {

    //Semantic Relationships
    int BROADER = 0;
    int NARROWER = 1;
    int RELATED = 2;
    int HAS_TOP_CONCEPT = 3;
    int INSCHEME = 4;
    int MEMBER = 5;
    int TOP_CONCEPT_OF = 6;
    int MICROTHESAURUS_OF = 7;
    int SUBGROUP = 8;
    int SUPERGROUP = 9;
    int HAS_MAIN_CONCEPT = 10;
    int MEMBER_OF = 11;
    int MAIN_CONCEPT_OF = 12;
    int BROADER_GENERIC =13;
    int BROADER_INSTANTIAL=14;
    int BROADER_PARTITIVE=15;
    int NARROWER_GENERIC=16;
    int NARROWER_INSTANTIAL=17;
    int NARROWER_PARTITIVE=18;
    int RELATED_HAS_PART=19;

    int RELATED_PART_OF=-1;
    
    int SUPER_ORDINATE = 100;
    int SUB_ORDINATE_ARRAY = 101;
    int TOP_FACET = 102;
    int FACET = 103;


    //Symbolic Labelling
    int PREF_SYMBOL = 20;
    int ALT_SYMBOL = 21;

    //Documentation Properties
    int DEFINITION = 30;
    int SCOPE_NOTE = 31;
    int EXAMPLE = 32;
    int HISTORY_NOTE = 33;
    int EDITORIAL_NOTE = 34;
    int CHANGE_NOTE = 35;
    int NOTE = 36;

    //Labelling Properties
    int PREF_LABEL = 40;
    int ALT_LABEL = 41;
    int HIDDEN_LABEL=42;

    //Dates
    int CREATED = 50;
    int MODIFIED = 51;
    int DATE = 52;
    
    //Identifier
    int IDENTIFIER = 55;

    //Creation
    int CREATOR = 60;
    int CONTRIBUTOR = 61;

    //Match
    int EXACT_MATCH = 70;
    int CLOSE_MATCH = 71;
    int BROAD_MATCH = 72;
    int RELATED_MATCH = 73;
    int NARROWER_MATCH = 74;

    //Ressource
    int FOAF_IMAGE = 79;
    int CONCEPT = 80;
    int CONCEPT_SCHEME = 81;
    int COLLECTION = 82;
    int CONCEPT_GROUP = 83;
    int MICROTHESAURUS = 84;
    int THEME = 85;

    int MESSAGE = 86;
    
    int CANDIDATE = 90;
    int DEPRECATED = 91;

    int IS_REPLACED_BY = 95;
    int REPLACES = 96;

}
