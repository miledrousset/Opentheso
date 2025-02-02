package fr.cnrs.opentheso.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserRoleGroupId.class)
@Table(name = "user_role_group")
public class UserRoleGroup {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_user", insertable = false, updatable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_role", insertable = false, updatable = false)
    private Roles role;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_group", insertable = false, updatable = false)
    private UserGroupLabel group;

}
