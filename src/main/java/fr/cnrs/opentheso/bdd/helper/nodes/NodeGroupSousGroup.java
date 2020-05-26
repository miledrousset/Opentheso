/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;

import java.util.ArrayList;

/**
 *
 * @author miled.rousset
 */
public class NodeGroupSousGroup {

    ArrayList<NodeGroupIdLabel> hierarchyOfGroup;

    public NodeGroupSousGroup() {
        hierarchyOfGroup = new ArrayList<>();
    }

    public ArrayList<NodeGroupIdLabel> getHierarchyOfGroup() {
        return hierarchyOfGroup;
    }

    public void setHierarchyOfGroup(ArrayList<NodeGroupIdLabel> hierarchyOfGroup) {
        this.hierarchyOfGroup = hierarchyOfGroup;
    }

   
}
