package fr.cnrs.opentheso.models.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeUserGroup {

    private int idGroup;
    private String groupName;

}
