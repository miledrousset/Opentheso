package fr.cnrs.opentheso.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "proposition")
@IdClass(PropositionId.class)
public class Proposition {

    @Id
    private Integer idUser;

    @Id
    private String idConcept;

    @Id
    private String idThesaurus;

    private String note;
    private String conceptParent;
    private String idGroup;
    private Date created;
    private Date modified;
}
