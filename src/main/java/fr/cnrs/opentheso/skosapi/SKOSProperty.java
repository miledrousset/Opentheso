package fr.cnrs.opentheso.skosapi;

/**
 *
 * @author Miled Rousset
 *
 */
public interface SKOSProperty {

    //Semantic Relationships
    public static final int BROADER = 0;
    public static final int NARROWER = 1;
    public static final int RELATED = 2;
    public static final int HAS_TOP_CONCEPT = 3;
    public static final int INSCHEME = 4;
    public static final int MEMBER = 5;
    public static final int TOP_CONCEPT_OF = 6;
    public static final int MICROTHESAURUS_OF = 7;
    public static final int SUBGROUP = 8;
    public static final int SUPERGROUP = 9;
    public static final int HAS_MAIN_CONCEPT = 10;
    public static final int MEMBER_OF = 11;
    public static final int MAIN_CONCEPT_OF = 12;
    public static final int BROADER_GENERIC =13;
    public static final int BROADER_INSTANTIAL=14;
    public static final int BROADER_PARTITIVE=15;
    public static final int NARROWER_GENERIC=16;
    public static final int NARROWER_INSTANTIAL=17;
    public static final int NARROWER_PARTITIVE=18;
    public static final int RELATED_HAS_PART=19;

    public static final int RELATED_PART_OF=-1;
    
    public static final int SUPER_ORDINATE = 100;
    public static final int SUB_ORDINATE_ARRAY = 101;      
    public static final int TOP_FACET = 102;
    public static final int FACET = 103;    
    

    //Symbolic Labelling
    public static final int PREF_SYMBOL = 20;
    public static final int ALT_SYMBOL = 21;

    //Documentation Properties
    public static final int DEFINITION = 30;
    public static final int SCOPE_NOTE = 31;
    public static final int EXAMPLE = 32;
    public static final int HISTORY_NOTE = 33;
    public static final int EDITORIAL_NOTE = 34;
    public static final int CHANGE_NOTE = 35;
    public static final int NOTE = 36;

    //Labelling Properties
    public static final int PREF_LABEL = 40;
    public static final int ALT_LABEL = 41;
    public static final int HIDDEN_LABEL=42;

    //Dates
    public static final int CREATED = 50;
    public static final int MODIFIED = 51;
    public static final int DATE = 52;
    
    //Identifier
    public static final int IDENTIFIER = 55;

    //Creation
    public static final int CREATOR = 60;
    public static final int CONTRIBUTOR = 61;

    //Match
    public static final int EXACT_MATCH = 70;
    public static final int CLOSE_MATCH = 71;
    public static final int BROAD_MATCH = 72;
    public static final int RELATED_MATCH = 73;
    public static final int NARROWER_MATCH = 74;

    //Ressource
    public static final int FOAF_IMAGE = 79;
    public static final int CONCEPT = 80;
    public static final int CONCEPT_SCHEME = 81;
    public static final int COLLECTION = 82;
    public static final int CONCEPT_GROUP = 83;
    public static final int MICROTHESAURUS = 84;
    public static final int THEME = 85;

    public static final int MESSAGE = 86;
    
    public static final int CANDIDATE = 90;
    public static final int DEPRECATED = 91;

    public static final int IS_REPLACED_BY = 95;
    public static final int REPLACES = 96;

        

    
}
