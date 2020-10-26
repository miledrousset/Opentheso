package fr.cnrs.opentheso.ws;
/*
import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import java.util.ArrayList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


@Path("/graph")
public class GraphGroup {


    @GET
    @Path("/firstElement")
    @Produces("application/json")
    public String getFirstElement() {
        
        if (Tree.treeNodeDataSelect == null) {
            return "";
        }
        
        return "{\"name\": \"" + Tree.treeNodeDataSelect.getName() + "\", \"id\": \""
                + Tree.treeNodeDataSelect.getNodeId() + "\", \"lang\": \"" + Tree.idLangSelected + "\" ,"
                + "\"idTheso\": \"" + Tree.idThesoSelected + "\"}";
    }    

    
    @GET
    @Path("{idTheso}/{idConcept}/{lang}")
    @Produces("application/json")
    public String getConceptSons(@PathParam("idTheso") String idTheso, 
            @PathParam("idConcept") String idConcept, 
            @PathParam("lang") String lang) {
       
        HikariDataSource connect = new ConnexionRest().getConnexion();
        ConceptHelper conceptHelper = new ConceptHelper();
        String idConceptFound = conceptHelper.getConceptIdFromPrefLabel(connect, idConcept, idTheso, lang);
        ArrayList<NodeConceptTree> childs = new ConceptHelper().getListConcepts(connect, idConceptFound, idTheso, lang, false);
        
        if (childs == null || childs.isEmpty()) {
            return "[]";
        }
        
        String str = "[" + getConceptDetails(childs.get(0));
        
        for (int i = 1; i < childs.size(); i++) {
            str += ',' + getConceptDetails(childs.get(i));
        }
        
        return str + "]";
    }
    
    private String getConceptDetails(NodeConceptTree nodeConceptTree) {
        return "{\"id\": \"" + nodeConceptTree.getIdConcept() 
                + "\", \"name\": \"" + nodeConceptTree.getTitle()
                + "\", \"isHaveChildren\": \"" + nodeConceptTree.isHaveChildren() + "\"}";
    }
    
}
*/