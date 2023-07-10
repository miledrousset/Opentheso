package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "thesaurus")
public class Thesaurus {

    @Id
    private int id;

    @Column(name = "id_thesaurus")
    private String thesaurusId;

    @Column(name = "id_ark")
    private String arkId;

    @Column(name = "private")
    private boolean privateTheso;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "modified")
    private LocalDateTime modified;
}
