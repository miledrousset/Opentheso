package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
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
@IdClass(ConceptDocTermId.class)
@Table(name = "concept_dcterms")
public class ConceptDcTerm implements Serializable {

    @Id
    @Column(name = "id_concept", nullable = false)
    private String idConcept;

    @Id
    @Column(name = "id_thesaurus", nullable = false)
    private String idThesaurus;

    @Id
    private String name;

    @Id
    private String value;

    private String language;

    @Column(name = "data_type")
    private String dataType;

}
