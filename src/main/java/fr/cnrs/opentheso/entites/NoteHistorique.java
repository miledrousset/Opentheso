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
import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "note_historique")
public class NoteHistorique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String notetypecode;

    private String idThesaurus;

    private String idTerm;

    private String idConcept;

    private String lang;

    private String lexicalvalue;

    private Date modified;

    private Integer idUser;

    private String actionPerformed;

}
