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
import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(HierarchicalRelationshipHistoriqueId.class)
@Table(name = "hierarchical_relationship_historique")
public class HierarchicalRelationshipHistorique {

	@Id
	private String idConcept1;

	@Id
	private String idThesaurus;

	@Id
	private String role;

	@Id
	private String idConcept2;

	@Id
	private Date modified;

	@Id
	private Integer idUser;

	private String action;

}
