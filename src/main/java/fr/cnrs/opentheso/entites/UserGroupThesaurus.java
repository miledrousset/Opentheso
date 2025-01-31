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
@Table(name = "user_group_thesaurus")
@IdClass(UserGroupThesaurusId.class)
public class UserGroupThesaurus {

    @Id
    @Column(name = "id_group")
    private int idGroup;

    @Id
    @Column(name = "id_thesaurus")
    private String idThesaurus;

}
