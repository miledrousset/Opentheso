package fr.cnrs.opentheso.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "non_preferred_term_historique")
public class SynonymHistorique {


    private String idTerm;
    @Id
    private String lexicalValue;
    private String lang;
    private String idThesaurus;
    private int idUser;
    private LocalDateTime modified;
    private String source;
    private String status;
    private String action;
    private boolean hiden;
}
