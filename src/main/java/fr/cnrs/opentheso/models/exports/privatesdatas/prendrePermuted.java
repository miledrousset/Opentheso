package fr.cnrs.opentheso.models.exports.privatesdatas;

import lombok.Data;


@Data
public class prendrePermuted {

    private Integer ord;
    private String id_concept;
    private String id_group;
    private String id_theso;
    private String original_value;
    private boolean  ispreferredterm;
    private String id_lang;
    private String lexicalValue;

}
