package fr.cnrs.opentheso.models.group;

import lombok.Data;
import java.sql.Date;


@Data
public class NodeGroupTraductions {

    private String title;
    private String idLang;
    private Date created;
    private Date modified;

}
