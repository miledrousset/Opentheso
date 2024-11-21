package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "alignement_source")
public class AlignementSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "source", unique = true)
    private String source;

    @Column(name = "requete")
    private String requete;

    @Column(name = "type_rqt", nullable = false)
    private String typeRqt;

    @Column(name = "alignement_format", nullable = false)
    private String alignementFormat;

    @Column(name = "id_user")
    private Integer idUser;

    @Column(name = "description")
    private String description;

    @Column(name = "gps", nullable = false)
    private Boolean gps = false;

    @Column(name = "source_filter", nullable = false)
    private String sourceFilter = "Opentheso";
}
