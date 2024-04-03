package fr.cnrs.opentheso.bean.leftbody;

import java.io.Serializable;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */


public class DataService implements Serializable{
     
    public TreeNode createRoot() {
        return new DefaultTreeNode(new TreeNodeData("1", "root","1", false, false, false, false, "root"), null);
    }
   
    public void addNodeWithChild(String typeDocument, TreeNodeData data, TreeNode parentNode){
        if(parentNode == null) return;
        if(typeDocument == null || typeDocument.isEmpty())
            parentNode = new DefaultTreeNode(data, parentNode);
        else
            parentNode = new DefaultTreeNode(typeDocument, data, parentNode);
        
        new DefaultTreeNode("DUMMY", parentNode);
    }
    
    public void addNodeWithoutChild(String typeDocument, TreeNodeData data, TreeNode parentNode){
        if(parentNode == null) return;
        if(typeDocument == null || typeDocument.isEmpty())
            new DefaultTreeNode(data, parentNode);
        else
            new DefaultTreeNode(typeDocument, data, parentNode);
    }    
}
