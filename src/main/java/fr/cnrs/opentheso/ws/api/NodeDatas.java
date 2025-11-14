package fr.cnrs.opentheso.ws.api;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class pour regrouper les datas pour un noeud
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeDatas {

    private String name;
    private String type;
    private String url;
    private List<String> definition;
    private List<String> synonym;
    private List<NodeDatas> childrens;
    private List<String> image;
 }
