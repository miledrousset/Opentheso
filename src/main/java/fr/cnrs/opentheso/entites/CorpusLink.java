package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CorpusLinkId.class)
@Table(name = "corpus_link")
public class CorpusLink {

    @Id
    private String idTheso;

    @Id
    private String corpusName;

    private String uriCount;

    private String uriLink;

    private boolean active;

    private boolean onlyUriLink;

    private Integer sort;

    @Column(name = "omeka_s")
    private boolean omekaS;
}
