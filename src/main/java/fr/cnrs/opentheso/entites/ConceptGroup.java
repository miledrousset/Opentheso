package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
import java.util.List;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "concept_group")
@EntityListeners(AuditingEntityListener.class)
public class ConceptGroup implements Serializable {

    @Id
    @Column(name = "idgroup", nullable = false)
    private String idGroup;

    @Column(name = "id_ark", nullable = false)
    private String idArk;

    @Column(name = "idtypecode", nullable = false)
    private String idTypeCode = "MT";

    @Column(name = "notation")
    private String notation;

    @Column(name = "numerotation")
    private Integer numerotation;

    @Column(name = "id_handle", nullable = false)
    private String idHandle = "";

    @Column(name = "id_doi", nullable = false)
    private String idDoi = "";

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @LastModifiedDate
    @Column(name = "modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "private", nullable = false)
    private boolean isPrivate;

    @ManyToOne
    @JoinColumn(name = "idthesaurus", referencedColumnName = "id_thesaurus", nullable = false)
    private Thesaurus thesaurus;

    @OneToMany(mappedBy = "conceptGroup")
    private List<ConceptGroupLabel> conceptGroupLabels;

}
