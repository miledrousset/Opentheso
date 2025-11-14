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
@IdClass(ConceptTermCandidatId.class)
@Table(name = "concept_term_candidat")
public class ConceptTermCandidat implements Serializable {

    @Id
    private String idConcept;

    @Id
    private String idTerm;

    @Id
    private String idThesaurus;

}
