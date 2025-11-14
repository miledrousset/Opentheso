package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "note")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "notetypecode")
    private String noteTypeCode;

    private String idThesaurus;

    private Integer idUser;

    private String idTerm;

    private String idConcept;

    private String lang;

    @Column(name = "lexicalvalue")
    private String lexicalValue;

    private Date created;

    private Date modified;

    @Column(name = "notesource")
    private String noteSource;

    private String identifier;

}
