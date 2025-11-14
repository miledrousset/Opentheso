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

import java.io.Serializable;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ConceptFacetId.class)
@Table(name = "concept_facet")
public class ConceptFacet implements Serializable {

    @Id
    private String idFacet;

    @Id
    private String idConcept;

    @Id
    private String idThesaurus;

}
