package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
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

import java.util.Date;
import java.util.List;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "thesaurus")
@EntityListeners(AuditingEntityListener.class)
public class Thesaurus {

    @Id
    @Column(name = "id_thesaurus", nullable = false)
    private String idThesaurus;

    @Column(name = "id_ark", nullable = false)
    private String idArk;

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @LastModifiedDate
    @Column(name = "modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "private", nullable = false)
    private Boolean isPrivate = false;

    @OneToMany(mappedBy = "thesaurus")
    private List<Concept> concepts;

    @OneToMany(mappedBy = "thesaurus")
    private List<ConceptFacet> conceptFacets;

    @OneToMany(mappedBy = "thesaurus")
    private List<ConceptCandidat> candidats;

    @OneToMany(mappedBy = "thesaurus")
    private List<ConceptGroup> conceptGroups;

    @OneToMany(mappedBy = "thesaurus")
    private List<ConceptGroupLabel> conceptGroupLabels;

}
