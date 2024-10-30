package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidat_status", uniqueConstraints = {@UniqueConstraint(columnNames = {"id_concept"})})
public class CandidatStatus implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_concept", referencedColumnName = "id_concept", nullable = false)
    private Concept concept;

    @ManyToOne
    @JoinColumn(name = "id_status", referencedColumnName = "id_status", nullable = false)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_user_admin", referencedColumnName = "id_user", nullable = false)
    private User userAdmin;

    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date date = new Date();

    @Column(name = "message")
    private String message;
}


