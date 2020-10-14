/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.rightbody.viewgroup;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGroupType;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "groupView")
@SessionScoped
public class GroupView implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private IndexSetting indexSetting;     
    @Inject
    private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Inject
    private ViewEditorHomeBean viewEditorHomeBean;       

    private NodeGroup nodeGroup;
    private ArrayList<NodeGroupTraductions> nodeGroupTraductions;
    private NodeGroupType nodeGroupType;
    
    private int count;

    /**
     * Creates a new instance of ConceptBean
     */
    public GroupView() {
    }

    public void init() {
        /*  if(isUriRequest) {
            isUriRequest = false;
            return;
        }*/
        count = 0;
        nodeGroup = null;
        nodeGroupType = null;
        nodeGroupTraductions = null;
    }

    /**
     * récuparation des informations pour le concept sélectionné
     *
     * @param idTheso
     * @param idGroup
     * @param idLang
     */
    public void getGroup(String idTheso, String idGroup, String idLang) {
        GroupHelper groupHelper = new GroupHelper();
        nodeGroup = groupHelper.getThisConceptGroup(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        
        nodeGroupTraductions = groupHelper.getGroupTraduction(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        nodeGroupType = groupHelper.getGroupType(
                connect.getPoolConnexion(), nodeGroup.getConceptGroup().getIdtypecode());
        
        ConceptHelper conceptHelper = new ConceptHelper();
        count = conceptHelper.getCountOfConceptsOfGroup(connect.getPoolConnexion(), idTheso, idGroup);
        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();
    }

    public NodeGroup getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(NodeGroup nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<NodeGroupTraductions> getNodeGroupTraductions() {
        return nodeGroupTraductions;
    }

    public void setNodeGroupTraductions(ArrayList<NodeGroupTraductions> nodeGroupTraductions) {
        this.nodeGroupTraductions = nodeGroupTraductions;
    }

    public NodeGroupType getNodeGroupType() {
        return nodeGroupType;
    }

    public void setNodeGroupType(NodeGroupType nodeGroupType) {
        this.nodeGroupType = nodeGroupType;
    }
   
}
