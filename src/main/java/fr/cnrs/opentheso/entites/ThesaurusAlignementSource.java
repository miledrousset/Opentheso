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
@Table(name = "thesaurus_alignement_source")
@AllArgsConstructor@IdClass(ThesaurusAlignementSourceId.class)
public class ThesaurusAlignementSource {

    @Id
    private String idThesaurus;

    @Id
    private Integer idAlignementSource;
}
