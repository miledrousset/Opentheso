package fr.cnrs.opentheso.entites;

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
@IdClass(RelationGroupId.class)
@Table(name = "relation_group")
public class RelationGroup {

    @Id
    private String idGroup1;

    @Id
    private String idThesaurus;

    @Id
    private String relation;

    @Id
    private String idGroup2;
}
