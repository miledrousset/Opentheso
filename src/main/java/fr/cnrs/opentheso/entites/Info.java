package fr.cnrs.opentheso.entites;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "info")
public class Info {

    @Id
    private String versionOpentheso;

    private String versionBdd;

    private String googleanalytics;

}
