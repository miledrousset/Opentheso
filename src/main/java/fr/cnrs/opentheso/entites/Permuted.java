package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PermutedId.class)
@Table(name = "permuted")
public class Permuted {

    @Id
    private int ord;

    @Id
    private String idConcept;

    @Id
    private String idGroup;

    @Id
    private String idThesaurus;

    @Id
    private String idLang;

    @Id
    private String lexicalValue;

    @Id
    @Column(name = "ispreferredterm")
    private boolean isPreferredTerm;

    private String originalValue;
}
