package fr.cnrs.opentheso.models;

public interface LanguageOfThesaurusProjection {

    String getIso6391();
    String getIso6392();
    String getEnglishName();
    String getFrenchName();
    String getCodePays(); // peut être null selon ta table
}
