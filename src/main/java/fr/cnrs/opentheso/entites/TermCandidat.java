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
@Table(name = "term_candidat")
@IdClass(TermCandidatId.class)
public class TermCandidat {

    private Integer id;

    @Id
    private String idTerm;

    @Id
    private String lexicalValue;

    @Id
    private String lang;

    @Id
    private String idThesaurus;

    private Date created;

    private Date modified;

    private Integer contributor;

}
