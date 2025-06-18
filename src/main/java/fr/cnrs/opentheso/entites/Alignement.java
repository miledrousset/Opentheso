package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alignement", uniqueConstraints = {@UniqueConstraint(columnNames = {"internal_id_concept", "internal_id_thesaurus", "uri_target"})})
@EntityListeners(AuditingEntityListener.class)
public class Alignement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @LastModifiedDate
    @Column(name = "modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "author")
    private Integer author;

    @Column(name = "concept_target")
    private String conceptTarget;

    @Column(name = "thesaurus_target")
    private String thesaurusTarget;

    @Column(name = "uri_target")
    private String uriTarget;

    @Column(name = "internal_id_thesaurus", nullable = false)
    private String internalIdThesaurus;

    @Column(name = "internal_id_concept")
    private String internalIdConcept;

    @Column(name = "url_available", nullable = false)
    private Boolean urlAvailable = true;

    @ManyToOne
    @JoinColumn(name = "alignement_id_type", nullable = false)
    private AlignementType alignementType;

    @ManyToOne
    @JoinColumn(name = "id_alignement_source")
    @NotFound(action = NotFoundAction.IGNORE)
    private AlignementSource alignementSource;
}
