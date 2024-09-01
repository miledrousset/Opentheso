package fr.cnrs.opentheso.bdd.datas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Relation {

    private String id_concept1;
    private String id_relation;
    private String id_concept2;
    private String id_thesaurus;
    private Date modified;
    private String idUser;
    private String action;


    public Relation(String id_terme1,
            String id_relation, String id_terme2,
            String id_thesaurus) {
        this.id_concept1 = id_terme1;
        this.id_relation = id_relation;
        this.id_concept2 = id_terme2;
        this.id_thesaurus = id_thesaurus;
    }
}
