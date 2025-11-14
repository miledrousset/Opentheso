package fr.cnrs.opentheso.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(NonPreferredTermHistoriqueId.class)
@Table(name = "non_preferred_term_historique")
public class NonPreferredTermHistorique {

    @Id
    private String idTerm;

    @Id
    private String lexicalValue;

    @Id
    private String lang;

    @Id
    private String idThesaurus;

    @Id
    private Date modified;

    private String source;

    private String status;

    private boolean hiden;

    private Integer idUser;

    private String action;
}
