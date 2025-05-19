package fr.cnrs.opentheso.models.thesaurus;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cette classe permet de regrouper le thésaurus avec ses traductions
 *
 * @author miled.rousset
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeThesaurus {

    private String idThesaurus;
    private String idArk;
    private List<Thesaurus> listThesaurusTraduction = new ArrayList<>();

}
