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
@Table(name = "concept_type")
@IdClass(ConceptTypeId.class)
public class ConceptType implements Serializable {

    @Id
    private String code;

    @Id
    private String idTheso;

    private String labelFr;

    private String labelEn;

    private boolean reciprocal;

}
