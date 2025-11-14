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
@IdClass(ThesaurusArrayId.class)
@Table(name = "thesaurus_array")
public class ThesaurusArray {

    @Id
    private String idThesaurus;

    @Id
    private String idConceptParent;

    @Id
    private String idFacet;

    private String notation;

    private boolean ordered;

    private Date created;

    private Date modified;

    private Integer contributor;

}
