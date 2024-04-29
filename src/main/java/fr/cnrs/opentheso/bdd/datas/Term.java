package fr.cnrs.opentheso.bdd.datas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Term {

    private String id_term;
    private String id_concept;
    private String lexical_value;
    private String lang;
    private String id_thesaurus;
    private Date created;
    private Date modified;
    private String source;
    private String status;
    private boolean prefered;
    private boolean hidden;
    private String idUser;
    private String codePays;    
    private int contributor;
    private int creator;

}
