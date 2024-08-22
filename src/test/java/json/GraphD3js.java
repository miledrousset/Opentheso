/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json;

import fr.cnrs.opentheso.ws.api.NodeDatas;
import fr.cnrs.opentheso.ws.api.NodeJsonD3js;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/**
 *
 * @author miledrousset
 */
public class GraphD3js {
    
    public GraphD3js() {
    }

    @Test
    public void addDatas()
    {
        NodeJsonD3js nodeJsonD3js = new NodeJsonD3js();
        
        // écriture du Root
        nodeJsonD3js.setRoot("tree");

        // les premières infos du root

        nodeJsonD3js.setNodeDatas(getRootNode());
        
    }
    
    private NodeDatas getRootNode(){
        NodeDatas nodeDatas = new NodeDatas();
        nodeDatas.setName("Root");
        nodeDatas.setType("type1");
        nodeDatas.setUrl("http://root");
  //      nodeDatas.setDefinition("definition");
        
        //Synonymes
        ArrayList<String> nodeIdValues = new ArrayList<>();
        nodeIdValues.add("Syn_1");
        nodeIdValues.add("Syn_2");        

        nodeDatas.setSynonym(nodeIdValues);
        
        //Children        
        ArrayList<NodeDatas> childrens = new ArrayList<>();
        
        // boucle récursive pour récupérer les fils
        for (int i = 0; i < 3; i++) {
            childrens.add(getNode(i));
        }
        nodeDatas.setChildrens(childrens);

        return nodeDatas;
    }
    
    private NodeDatas getNode(int i){
        NodeDatas nodeDatas = new NodeDatas();
        nodeDatas.setName("fils" + i);
        nodeDatas.setType("type2");
        nodeDatas.setUrl("http://fils" +i);
   //     nodeDatas.setDefinition("definition");
        
        ArrayList<String> nodeIdValues = new ArrayList<>();
        nodeIdValues.add("Syn_1");
        nodeIdValues.add("Syn_2");      
        nodeDatas.setSynonym(nodeIdValues);
        
        //Children        
        ArrayList<NodeDatas> childrens = new ArrayList<>();
        if(i==0) {
            childrens.add(getNode(3 + i++));
            nodeDatas.setChildrens(childrens);
        }
        return nodeDatas;
    }    
    
}
