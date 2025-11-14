package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "concept_group_label_historique")
@EntityListeners(AuditingEntityListener.class)
public class ConceptGroupLabelHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lexicalvalue")
    private String lexicalValue;

    @LastModifiedDate
    @Column(name = "modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    private String lang;

    @Column(name = "idthesaurus")
    private String idThesaurus;

    @Column(name = "idgroup")
    private String idGroup;

    private Integer idUser;
}
