package fr.cnrs.opentheso.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "concept_facet")
public class ConceptFacet implements Serializable {

    @Id
    @Column(name = "id_facet", nullable = false)
    private String idFacet;

    @ManyToOne
    @JoinColumn(name = "id_thesaurus", referencedColumnName = "id_thesaurus", nullable = false)
    private Thesaurus thesaurus;

    @OneToOne
    @JoinColumn(name = "id_concept", referencedColumnName = "id_concept", nullable = false, unique = true)
    private Concept concept;
}
