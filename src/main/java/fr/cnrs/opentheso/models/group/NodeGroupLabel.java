package fr.cnrs.opentheso.models.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeGroupLabel {

    private String idGroup;
    private String idArk;
    private String idHandle;
    private String idDoi;
    private String notation;
    private String idThesaurus;
    private Date created;
    private Date modified;
    private List<NodeGroupTraductions> nodeGroupTraductionses = new ArrayList<>();

}
