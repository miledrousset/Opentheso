package fr.cnrs.opentheso.models.languages;

import lombok.Data;


@Data
public class Languages_iso639 {

    private String id_iso639_1;
    private String id_iso639_2;
    private String english_name;
    private String french_name;
    private String codePays;

}
