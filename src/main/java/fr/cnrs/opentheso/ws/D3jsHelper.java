/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author miledrousset
 */
public class D3jsHelper {
    
    
    /**
     * Permet de retourner une branche au format Json pour le graphe D3js
     * 
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idLang
     * @return  
     */
    public String findDatasForGraph(HikariDataSource ds,
            String idConcept, String idTheso, String idLang) {

        String datas = findDatasForGraph__(ds,
                 idConcept, idTheso, idLang);
        if(datas == null) return null;
        return datas;
    }    
    
    
    /**
     * recherche par valeur
     * @param ds
     * @param value
     * @param idTheso
     * @param lang
     * @return 
     */
    private String findDatasForGraph__(
            HikariDataSource ds,
            String idConcept, String idTheso,
            String idLang) {

        if(idTheso == null || idTheso.isEmpty()) {
            return null;
        }
        if(idConcept == null || idConcept.isEmpty()) {
            return null;
        }
        if(idLang == null || idLang.isEmpty()) {
            return null;
        }        
        NodePreference nodePreference = new PreferencesHelper().getThesaurusPreferences(ds, idTheso);
        if (nodePreference == null) {
            return null;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        
    //    ArrayList<String> listIds = conceptHelper.getIdsOfBranch(ds, idConcept, idTheso);
        
        ArrayList<String> listChilds = conceptHelper.getListChildrenOfConcept(ds, idConcept, idTheso);
        
        if(listChilds == null || listChilds.isEmpty())
            return null;
        
        NodeJsonD3js nodeJsonD3js = new NodeJsonD3js();        
        nodeJsonD3js.setRoot("tree");
        nodeJsonD3js.setNodeDatas(getRootNode(
                ds,
                idTheso, idLang,
                idConcept, listChilds));

        return getJsonFromNodeJsonD3js(nodeJsonD3js); 
    }       

    private NodeDatas getRootNode(HikariDataSource ds,
            String idTheso, String idLang,
            String idTopConcept, ArrayList<String> listIds){
        
        NodeDatas nodeDatas = getNodeDatas(ds, idTopConcept, idTheso, idLang);
        
        //Children        
        ArrayList<NodeDatas> childrens = new ArrayList<>();
        
        // boucle récursive pour récupérer les fils
        for (String idConcept : listIds) {
            childrens.add(getNode(ds, idConcept, idTheso, idLang));
        }
        nodeDatas.setChildren(childrens);
        return nodeDatas;
    }
    
    private NodeDatas getNode(HikariDataSource ds,
            String idConcept, String idTheso, String idLang){
        NodeDatas nodeDatas = getNodeDatas(ds, idConcept, idTheso, idLang);
        
        //Children        
        ArrayList<NodeDatas> childrens = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> listChilds = conceptHelper.getListChildrenOfConcept(ds, idConcept, idTheso);
        if(listChilds != null && !listChilds.isEmpty()) {
            for (String child : listChilds) {
                childrens.add(getNode(ds, child, idTheso, idLang));
                nodeDatas.setChildren(childrens);
            }
        }
        return nodeDatas;
    }     
/////////////////////////////////
///////// OK jusqu'ici //////////
/////////////////////////////////    
    
    
    
    
    private String getJsonFromNodeJsonD3js(NodeJsonD3js nodeJsonD3js) {
        String datas;
        JsonObjectBuilder root = Json.createObjectBuilder();
 
            JsonObjectBuilder nodeRoot = Json.createObjectBuilder();
            nodeRoot.add("nodeName", nodeJsonD3js.getNodeDatas().getNodeName());
            nodeRoot.add("type", "type1");
            nodeRoot.add("url", nodeJsonD3js.getNodeDatas().getUrl());
            nodeRoot.add("definition", nodeJsonD3js.getNodeDatas().getDefinition());
            
            
            JsonArrayBuilder jsonArrayBuilderSynonyms = Json.createArrayBuilder();
            for (String synonym : nodeJsonD3js.getNodeDatas().getSynonym()) {
                jsonArrayBuilderSynonyms.add(synonym);
            }
            nodeRoot.add("synonym", jsonArrayBuilderSynonyms.build());
        
            JsonArrayBuilder jsonArrayBuilderChilds = Json.createArrayBuilder();
            
            for (NodeDatas nodeData : nodeJsonD3js.getNodeDatas().getChildrens()) {
                jsonArrayBuilderChilds.add(getChild(nodeData).build()); 
            }
            nodeRoot.add("children", jsonArrayBuilderChilds.build());
            
        root.add("tree", nodeRoot.build());        

        
        datas = root.build().toString();        
        return datas;
    }     
    
    
    private JsonObjectBuilder getChild(NodeDatas nodeData) {
     //   JsonArrayBuilder child = Json.createArrayBuilder();
 
            JsonObjectBuilder nodeChild = Json.createObjectBuilder();
            nodeChild.add("nodeName", nodeData.getNodeName());
            nodeChild.add("type", nodeData.getType());
            nodeChild.add("url", nodeData.getUrl());
            nodeChild.add("definition", nodeData.getDefinition());
        
            JsonArrayBuilder jsonArrayBuilderSynonyms = Json.createArrayBuilder();
            for (String synonym : nodeData.getSynonym()) {
                jsonArrayBuilderSynonyms.add(synonym);
            }
            nodeChild.add("synonym", jsonArrayBuilderSynonyms.build());
            
            JsonArrayBuilder jsonArrayBuilderChilds = Json.createArrayBuilder();            
            if(nodeData.getChildrens() != null && !nodeData.getChildrens().isEmpty()){
                for (NodeDatas nodeDataChild : nodeData.getChildrens()) {
                    jsonArrayBuilderChilds.add(getChild(nodeDataChild).build()); 
                }
            }
            nodeChild.add("children", jsonArrayBuilderChilds.build());
//            if(nodeData.getChildrens() != null && !nodeData.getChildrens().isEmpty()){
//                nodeChild.addNull("children");
//            } else {
//                nodeChild.addNull("children");
//            }
                
      //  child.add(nodeChild.build());
 
        return nodeChild;
    }      
    
    
    
    
    private NodeDatas getNodeDatas(HikariDataSource ds,
            String idConcept, String idTheso, String idLang){
        ConceptHelper conceptHelper = new ConceptHelper();
        String label = conceptHelper.getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
        NodeDatas nodeDatas = new NodeDatas();
        nodeDatas.setNodeName(label);
        nodeDatas.setUrl("https://" + label);
        nodeDatas.setDefinition("def " + label);
        if(conceptHelper.haveChildren(ds, idTheso, idConcept)) {
            nodeDatas.setType("type2");
        } else
            nodeDatas.setType("type3");
        ArrayList<String> synonyms =  new ArrayList<>();
        synonyms.add(0, "Syno1");
        synonyms.add(1, "Syno2");
        synonyms.add(2,"Syno3");
        
        nodeDatas.setSynonym(synonyms);
        return nodeDatas;
    }    
    
}
