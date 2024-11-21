package fr.cnrs.opentheso.entites;

import jakarta.persistence.*;
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
@Table(name = "concept")
@EntityListeners(AuditingEntityListener.class)
public class Concept implements Serializable {

    @Id
    @Column(name = "id_concept", nullable = false)
    private String idConcept;

    @ManyToOne
    @JoinColumn(name = "id_thesaurus", referencedColumnName = "id_thesaurus", nullable = false)
    private Thesaurus thesaurus;

    @Column(name = "id_ark", nullable = false)
    private String idArk = "";

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @LastModifiedDate
    @Column(name = "modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "status")
    private String status;

    @Column(name = "notation", nullable = false)
    private String notation = "";

    @Column(name = "top_concept")
    private Boolean topConcept;

    @Column(name = "gps", nullable = false)
    private Boolean gps = false;

    @Column(name = "id_handle", nullable = false)
    private String idHandle = "";

    @Column(name = "id_doi", nullable = false)
    private String idDoi = "";

    @Column(name = "creator", nullable = false)
    private Integer creator = -1;

    @Column(name = "contributor", nullable = false)
    private Integer contributor = -1;

    @Column(name = "concept_type", nullable = false)
    private String conceptType = "concept";

    @OneToMany(mappedBy = "concept")
    private List<CandidatMessages> candidatMessages;

    @OneToMany(mappedBy = "concept")
    private List<CandidatStatus> candidatStatuses;

    @OneToMany(mappedBy = "concept")
    private List<CandidatVote> candidatVotes;

    @OneToOne(mappedBy = "concept")
    private ConceptFacet conceptFacet;
}
