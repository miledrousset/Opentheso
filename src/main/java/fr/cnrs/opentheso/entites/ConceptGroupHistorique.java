package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "concept_group_historique")
public class ConceptGroupHistorique implements Serializable {

    @Id
    private Integer id;

    @Column(name = "idgroup")
    private String idGroup;

    private String idArk;

    @Column(name = "idthesaurus")
    private String idThesaurus;

    @Column(name = "idtypecode")
    private String idTypeCode;

    @Column(name = "idparentgroup")
    private String idParentGroup;

    private String notation;

    @Column(name = "idconcept")
    private String idConcept;

    private Date modified;

    private Integer idUser;

}
