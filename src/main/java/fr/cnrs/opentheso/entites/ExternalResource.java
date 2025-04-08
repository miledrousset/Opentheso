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
@IdClass(ExternalResourceId.class)
@Table(name = "external_resources")
public class ExternalResource {

    @Id
    @Column(name = "id_concept")
    private String idConcept;

    @Id
    @Column(name = "id_thesaurus")
    private String idThesaurus;

    @Id
    @Column(name = "external_uri")
    private String externalUri;

    @Column(name = "id_user")
    private Integer idUser;

    private String description;
}
