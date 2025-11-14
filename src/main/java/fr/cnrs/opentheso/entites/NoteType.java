package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "note_type")
public class NoteType {

    @Id
    private String code;

    @Column(name = "isterm")
    private boolean isTerm;

    @Column(name = "isconcept")
    private boolean isConcept;

    private String labelFr;

    private String labelEn;
}
