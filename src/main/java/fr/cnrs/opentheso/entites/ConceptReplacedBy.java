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
@IdClass(ConceptReplacedById.class)
@Table(name = "concept_replacedby")
public class ConceptReplacedBy {

    @Id
    private String idConcept1;

    @Id
    private String idConcept2;

    @Id
    private String idThesaurus;

    private Date modified;

    private Integer idUser;
}

