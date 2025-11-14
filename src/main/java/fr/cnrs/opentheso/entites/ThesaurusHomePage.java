package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "thesohomepage")
public class ThesaurusHomePage {

    @Id
    @Column(name = "idtheso")
    private String idTheso;

    private String lang;

    @Column(name = "htmlcode")
    private String htmlCode;

}
