package fr.cnrs.opentheso.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.io.Serializable;
import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidat_status", uniqueConstraints = {@UniqueConstraint(columnNames = {"id_concept"})})
public class CandidatStatus implements Serializable {

    @Id
    private String idConcept;

    @ManyToOne
    @JoinColumn(name = "id_status", referencedColumnName = "id_status", nullable = false)
    private Status status;

    private String idThesaurus;

    private Integer idUser;

    private Integer idUserAdmin;

    @Temporal(TemporalType.DATE)
    private Date date = new Date();

    private String message;
}


