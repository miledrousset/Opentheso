package fr.cnrs.opentheso.entites;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;



@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ConceptGroupConceptId.class)
@Table(name = "concept_group_concept")
public class ConceptGroupConcept {

    @Id
    @Column(name = "idgroup")
    private String idGroup;

    @Id
    @Column(name = "idthesaurus")
    private String idThesaurus;

    @Id
    @Column(name = "idconcept")
    private String idConcept;
}
