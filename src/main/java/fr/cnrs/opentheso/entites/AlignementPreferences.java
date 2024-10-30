package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@Table(name = "alignement_preferences")
@IdClass(AlignementPreferencesId.class)
public class AlignementPreferences {

    @Id
    @Column(name = "id_thesaurus", nullable = false)
    private String idThesaurus;

    @Id
    @Column(name = "id_user", nullable = false)
    private Integer idUser;

    @Id
    @Column(name = "id_concept_depart", nullable = false)
    private String idConceptDepart;

    @Id
    @Column(name = "id_alignement_source", nullable = false)
    private Integer idAlignementSource;

    @Column(name = "id_concept_tratees")
    private String idConceptTratees;
}
