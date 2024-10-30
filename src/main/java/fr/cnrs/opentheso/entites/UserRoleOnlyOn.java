package fr.cnrs.opentheso.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "user_role_only_on")
public class UserRoleOnlyOn {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_user", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_role", insertable = false, updatable = false)
    private Roles role;

    @ManyToOne
    @JoinColumn(name = "id_theso", insertable = false, updatable = false)
    private Thesaurus thesaurus;

    @ManyToOne
    @JoinColumn(name = "id_group", insertable = false, updatable = false)
    private UserGroupLabel group;

}
