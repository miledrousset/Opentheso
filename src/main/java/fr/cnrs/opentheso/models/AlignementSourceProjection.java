package fr.cnrs.opentheso.models;


public interface AlignementSourceProjection {

    String getSource();
    String getRequete();
    String getTypeRequete();
    String getAlignement_format();
    int getId();
    String getDescription();
    String getSource_filter();
    boolean getGps();

}
