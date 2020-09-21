/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.leftbody;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */


public class DataService {
     
    public TreeNode createRoot() {
        TreeNode root = new DefaultTreeNode(new TreeNodeData("1", "root","1", false, false, false, false, "root"), null);
        return root;
    }
   
    public void addNodeWithChild(String typeDocument, TreeNodeData data, TreeNode parentNode){
        if(parentNode == null) return;
        TreeNode document;
        if(typeDocument == null || typeDocument.isEmpty())
            document = new DefaultTreeNode(data, parentNode);
        else
            document = new DefaultTreeNode(typeDocument, data, parentNode);
        
        new DefaultTreeNode("DUMMY", document);
    }
    
    public void addNodeWithoutChild(String typeDocument, TreeNodeData data, TreeNode parentNode){
        if(parentNode == null) return;
        TreeNode document;
        if(typeDocument == null || typeDocument.isEmpty())
            document = new DefaultTreeNode(data, parentNode);
        else
            document = new DefaultTreeNode(typeDocument, data, parentNode);
    }    
}
