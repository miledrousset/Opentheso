package fr.cnrs.opentheso.models.thesaurus;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Cette classe permet de regrouper le thésaurus avec ses traductions
 *
 * @author miled.rousset
 */
@Data
public class NodeThesaurus {

    private String idThesaurus;
    private String idArk;
    private List<Thesaurus> listThesaurusTraduction = new ArrayList<>();

}
