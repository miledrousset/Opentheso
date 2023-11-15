package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
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


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gps")
public class Gps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_concept")
    private String idConcept;

    @Column(name = "id_theso")
    private String idTheso;

    private Double latitude;

    private Double longitude;

    private Integer position;

    @Override
    public String toString() {
        return String.format("%f %f", latitude, longitude).replaceAll(",", ".");
    }
}
