package fr.cnrs.opentheso.ws.api;

import java.util.List;
import lombok.Data;

/**
 * Class pour regrouper les datas pour un noeud
 */
@Data
public class NodeDatas {

    private String name;
    private String type;
    private String url;
    private List<String> definition;
    private List<String> synonym;
    private List<NodeDatas> childrens;
}
