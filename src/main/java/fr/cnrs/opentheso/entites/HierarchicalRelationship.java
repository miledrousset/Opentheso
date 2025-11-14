package fr.cnrs.opentheso.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(HierarchicalRelationshipId.class)
@Table(name = "hierarchical_relationship")
public class HierarchicalRelationship {

	@Id
	private String idConcept1;

	@Id
	private String idConcept2;

	@Id
	private String idThesaurus;

	@Id
	private String role;

}
