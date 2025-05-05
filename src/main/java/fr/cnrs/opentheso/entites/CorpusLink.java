package fr.cnrs.opentheso.entites;

import jakarta.persistence.*;
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
