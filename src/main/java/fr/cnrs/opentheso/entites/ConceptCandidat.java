package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.util.Date;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ConceptCandidatId.class)
@Table(name = "concept_candidat")
@EntityListeners(AuditingEntityListener.class)
public class ConceptCandidat implements Serializable {

    @Id
    private String idConcept;

    @Id
    private String idThesaurus;

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @LastModifiedDate
    @Column(name = "modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "status", nullable = false)
    private String status = "a";

    @Column(name = "admin_message")
    private String adminMessage;

    @Column(name = "admin_id")
    private Integer adminId;

}
