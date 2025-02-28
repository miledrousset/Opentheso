package fr.cnrs.opentheso.models.users;

import lombok.Data;
import java.io.Serializable;
import java.util.List;


@Data
public class NodeUserComplet extends NodeUser implements Serializable {

    private List<String> thesorusList;

}
