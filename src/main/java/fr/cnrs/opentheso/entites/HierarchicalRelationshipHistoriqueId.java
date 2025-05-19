package fr.cnrs.opentheso.entites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchicalRelationshipHistoriqueId {

	private String idConcept1;
	private String idThesaurus;
	private String role;
	private String idConcept2;
	private Date modified;
	private String idUser;

}
