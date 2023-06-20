/*
{
    "tree": {
        "nodeName": "Root",
        "type": "type1",
        "url": "http://TEXT",
        "definition": "definition",
        "synonym": [ 
                {"value": "SYN_1"}, 
                {"value": "SYN_2"}, 
                {"value": "SYN_3"}],        
        "children": [{
                "nodeName": "TopTerm1",
                "type": "type2",
                "url": "http://TopTerm1",
                "definition": "definition",
                "synonym": [],
                "children": [{
                        "nodeName": "concept1",
                        "type": "type3",
                        "url": "http://concept1",
                        "definition": "definition",
                        "synonym": [],                        
                        "children": []
                    }, {
                        "nodeName": "concept2",
                        "type": "type3",
                        "url": "http://concept2",
                        "definition": "definition",
                        "synonym": [],                      
                        "children": []
                    }
                ]
            },
            {
                "nodeName": "Topterm2",
                "type": "type3",
                "url": "http://Topterm2",
                "definition": "definition",
                "synonym": [],
                "children": []
            }            
        ]
    }
}
 */
package fr.cnrs.opentheso.ws.api;


/**
 *
 * @author miledrousset
 */
public class NodeJsonD3js {

    private String root;
    
    private NodeDatas nodeDatas;
    
    public NodeJsonD3js() {
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public NodeDatas getNodeDatas() {
        return nodeDatas;
    }

    public void setNodeDatas(NodeDatas nodeDatas) {
        this.nodeDatas = nodeDatas;
    }
    
}
