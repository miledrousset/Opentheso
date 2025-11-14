package fr.cnrs.opentheso.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "nt_type")
public class NtType {

    @Id
    private int id;

    private String relation;

    private String descriptionFr;

    private String descriptionEn;
}
