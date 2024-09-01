package fr.cnrs.opentheso.bdd.datas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class HierarchicalRelationship {

	private String idConcept1;
	private String idConcept2;
	private String idThesaurus;
	private String role;

}
