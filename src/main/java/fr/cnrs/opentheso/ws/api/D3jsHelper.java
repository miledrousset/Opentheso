package fr.cnrs.opentheso.ws.api;


import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.repositories.DaoResourceHelper;
import fr.cnrs.opentheso.models.concept.NodeConceptGraph;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class D3jsHelper {

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;

    @Autowired
    private DaoResourceHelper daoResourceHelper;


    private int count = 0;
    private NodePreference nodePreference;

    public String findDatasForGraph__(String idConcept, String idTheso, String idLang) {

        if(StringUtils.isEmpty(idTheso)) {
            return null;
        }

        if(StringUtils.isEmpty(idLang)) {
            return null;
        }
        nodePreference = preferencesHelper.getThesaurusPreferences(idTheso);
        if (nodePreference == null) {
            return null;
        }
        
        List<NodeConceptGraph> nodeConceptGraphs_childs;
        
        // cas où on affiche tout le thésaurus
        if(StringUtils.isEmpty(idConcept)){
            nodeConceptGraphs_childs = daoResourceHelper.getConceptsTTForGraph(idTheso, idLang);
        } else {
            nodeConceptGraphs_childs = daoResourceHelper.getConceptsNTForGraph(idTheso, idConcept, idLang);
        }
        
        if(nodeConceptGraphs_childs == null || nodeConceptGraphs_childataSource.isEmpty())
            return null;

        /// limitation des frères à 2000
        if(nodeConceptGraphs_childataSource.size() > 2000) {
            nodeConceptGraphs_childs = nodeConceptGraphs_childataSource.subList(0, 2001);
        }
        
        NodeJsonD3js nodeJsonD3js = new NodeJsonD3js();
        nodeJsonD3js.setNodeDatas(getRootNode(idTheso, idLang, idConcept, nodeConceptGraphs_childs));            

        return getJsonFromNodeJsonD3js(nodeJsonD3js);
    }

    private NodeDatas getRootNode(String idTheso, String idLang,
            String idTopConcept,  List<NodeConceptGraph> nodeConceptGraphs_childs){
        
        NodeDatas nodeDatas;
        if(StringUtils.isEmpty(idTopConcept)){
            nodeDatas = getTopNodeDatasForTheso(idTheso, idLang);
        } else {
            nodeDatas = getTopNodeDatas(idTopConcept, idTheso, idLang);
        }

        //Children
        List<NodeDatas> childrens = new ArrayList<>();

        // pour limiter les noeuds à 3000, sinon, c'est invisible sur le graphe
        count = 0;

        // boucle récursive pour récupérer les fils
        for (NodeConceptGraph nodeConceptGraph : nodeConceptGraphs_childs) {
            childrens.add(getNode(nodeConceptGraph, idTheso, idLang));
        }
        nodeDatas.setChildrens(childrens);
        return nodeDatas;
    }

    private NodeDatas getNode(NodeConceptGraph nodeConceptGraph, String idTheso, String idLang){
        NodeDatas nodeDatas = getNodeDatas(nodeConceptGraph);
        count++;
        //Children
        List<NodeDatas> childrens = new ArrayList<>();
        if(count < 3000) {
            List<NodeConceptGraph> nodeConceptGraphs_childs = daoResourceHelper.getConceptsNTForGraph(idTheso, nodeConceptGraph.getIdConcept(), idLang);
            
            if(nodeConceptGraphs_childs != null && !nodeConceptGraphs_childataSource.isEmpty()) {            
                /// limitation des frères à 2000
                if(nodeConceptGraphs_childataSource.size() > 2000) {
                    nodeConceptGraphs_childs = nodeConceptGraphs_childataSource.subList(0, 2001);
                }            
                for (NodeConceptGraph nodeConceptGraph1 : nodeConceptGraphs_childs) {
                    childrens.add(getNode(nodeConceptGraph1, idTheso, idLang));
                    count++;
                    nodeDatas.setChildrens(childrens);
                }
            }
        }
        return nodeDatas;
    }

    private String getJsonFromNodeJsonD3js(NodeJsonD3js nodeJsonD3js) {

        JsonObjectBuilder nodeRoot = Json.createObjectBuilder();
        nodeRoot.add("name", nodeJsonD3js.getNodeDatas().getName());
        nodeRoot.add("type", "type1");
        nodeRoot.add("url", nodeJsonD3js.getNodeDatas().getUrl());
        if(nodeJsonD3js.getNodeDatas().getDefinition() == null){
            nodeRoot.add("definition", "");
        } else {
            nodeRoot.add("definition", nodeJsonD3js.getNodeDatas().getDefinition().toString());
        }

        if(nodeJsonD3js.getNodeDatas().getSynonym() == null){
            nodeRoot.add("synonym", "");
        } else {
            JsonArrayBuilder jsonArrayBuilderSynonyms = Json.createArrayBuilder();
            for (String synonym : nodeJsonD3js.getNodeDatas().getSynonym()) {
                jsonArrayBuilderSynonyms.add(synonym);
            }
            nodeRoot.add("synonym", jsonArrayBuilderSynonyms.build());            
        }
        
        JsonArrayBuilder jsonArrayBuilderChilds = Json.createArrayBuilder();

        for (NodeDatas nodeData : nodeJsonD3js.getNodeDatas().getChildrens()) {
            jsonArrayBuilderChildataSource.add(getChild(nodeData).build());
        }

        nodeRoot.add("children", jsonArrayBuilderChildataSource.build());

        return nodeRoot.build().toString();
    }


    private JsonObjectBuilder getChild(NodeDatas nodeData) {

        JsonObjectBuilder nodeChild = Json.createObjectBuilder();
        nodeChild.add("name", nodeData.getName());
        nodeChild.add("type", nodeData.getType());
        nodeChild.add("url", nodeData.getUrl());

        // altLabels
        JsonArrayBuilder jsonArrayBuilderSynonyms = Json.createArrayBuilder();
        if(nodeData.getSynonym() == null){
            nodeChild.add("synonym", jsonArrayBuilderSynonyms.build());
        } else {
            for (String synonym : nodeData.getSynonym()) {
                jsonArrayBuilderSynonyms.add(synonym);
            }
            nodeChild.add("synonym", jsonArrayBuilderSynonyms.build());
        }
      
        // définition
        JsonArrayBuilder jsonArrayBuilderDefinition = Json.createArrayBuilder();
        if(nodeData.getDefinition() == null){
            nodeChild.add("definition", jsonArrayBuilderSynonyms.build());
        } else {
            for (String definition : nodeData.getDefinition()) {
                jsonArrayBuilderDefinition.add(definition);
            }
            nodeChild.add("definition", jsonArrayBuilderDefinition.build());              
        }
        
        
        JsonArrayBuilder jsonArrayBuilderChilds = Json.createArrayBuilder();
        if(nodeData.getChildrens() != null && !nodeData.getChildrens().isEmpty()){
            for (NodeDatas nodeDataChild : nodeData.getChildrens()) {
                jsonArrayBuilderChildataSource.add(getChild(nodeDataChild).build());
            }
        }
        nodeChild.add("children", jsonArrayBuilderChildataSource.build());

        return nodeChild;
    }


    private NodeDatas getNodeDatas(NodeConceptGraph nodeConceptGraph){
        NodeDatas nodeDatas = new NodeDatas();
        
        if(StringUtils.isEmpty(nodeConceptGraph.getPrefLabel())) {
            nodeDatas.setName("(" + nodeConceptGraph.getIdConcept() + ")");
        } else 
            nodeDatas.setName(nodeConceptGraph.getPrefLabel());

        nodeDatas.setUrl(nodeConceptGraph.getUri());
        nodeDatas.setDefinition(nodeConceptGraph.getDefinitions());
        nodeDatas.setSynonym(nodeConceptGraph.getAltLabels());
        
        if(nodeConceptGraph.isHaveChildren()){
            nodeDatas.setType("type2");
        } else
            nodeDatas.setType("type3");
        return nodeDatas;
    }    


    private NodeDatas getTopNodeDatas(String idConcept, String idTheso, String idLang){

        conceptHelper.setNodePreference(nodePreference);

        NodeDatas nodeDatas = conceptHelper.getConceptForGraph(idConcept, idTheso, idLang);
        if(conceptHelper.haveChildren(idTheso, idConcept)) {
            nodeDatas.setType("type2");
        } else
            nodeDatas.setType("type3");
        return nodeDatas;
    }
    
    private NodeDatas getTopNodeDatasForTheso(String idTheso, String idLang){

        String title = thesaurusHelper.getTitleOfThesaurus(idTheso, idLang);
        NodeDatas nodeDatas = new NodeDatas();
        
        nodeDatas.setType("type2");
        nodeDatas.setName(title);
        nodeDatas.setUrl(getUri(idTheso));
        nodeDatas.setDefinition(null);
        return nodeDatas;
    }    

    private String getUri(String idTheso) {
        if (idTheso == null) {
            return "";
        }
        return nodePreference.getCheminSite() + "?idt=" + idTheso;
    }    
}
