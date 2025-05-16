package fr.cnrs.opentheso.models.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeGroupTraductions {

    private String title;
    private String idLang;
    private Date created;
    private Date modified;

}
