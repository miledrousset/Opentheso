/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.leftbody.viewliste;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author miledrousset
 */
@Named(value = "listIndex")
@SessionScoped
public class ListIndex implements Serializable {

    @Autowired @Lazy
    private Connect connect;
    @Autowired @Lazy
    private SelectedTheso selectedTheso;
    @Autowired @Lazy
    private CurrentUser currentUser;
    @Autowired @Lazy
    private RightBodySetting rightBodySetting;
    @Autowired @Lazy 
    private LeftBodySetting leftBodySetting;
    @Autowired @Lazy
    private ConceptView conceptBean;
    @Autowired @Lazy
    private PropositionBean propositionBean;

    @Autowired
    private SearchHelper searchHelper;

    private String searchValue;
    private NodeIdValue selectedNode;
    private List<NodeIdValue> nodeIdValues;

    private boolean withAltLabel;
    private boolean permuted;

    @PreDestroy
    public void destroy() {
        reset();
    }

    public void reset() {
        searchValue = null;
        selectedNode = null;
        if (nodeIdValues != null) {
            nodeIdValues.clear();
            nodeIdValues = null;
        }
    }

    public void onChange() {
        String idTheso = selectedTheso.getCurrentIdTheso();
        String idLang = selectedTheso.getCurrentLang();
        if (idTheso == null || idTheso.isEmpty()) {
            return;
        }
        if (idLang == null || idLang.isEmpty()) {
            return;
        }
        if (searchValue == null || searchValue.isEmpty()) {
            return;
        }

        nodeIdValues = searchHelper.searchTermForIndex(
                connect.getPoolConnexion(),
                searchValue,
                idLang, idTheso,
                permuted, withAltLabel);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formList");
        }
    }

    public void onRowSelect(SelectEvent event) {
        rightBodySetting.setShowConceptToOn();
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                ((NodeIdValue) event.getObject()).getId(), selectedTheso.getCurrentLang(), currentUser);

        rightBodySetting.setIndex("0");
        propositionBean.setRubriqueVisible(false);
        leftBodySetting.setIndex("1");
        if (PrimeFaces.current().isAjaxRequest()) {
                    PrimeFaces.current().executeScript("srollToSelected();");
        }
        
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public NodeIdValue getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(NodeIdValue selectedNode) {
        this.selectedNode = selectedNode;
    }

    public List<NodeIdValue> getNodeIdValues() {
        return nodeIdValues;
    }

    public void setNodeIdValues(List<NodeIdValue> nodeIdValues) {
        this.nodeIdValues = nodeIdValues;
    }

    public boolean isWithAltLabel() {
        return withAltLabel;
    }

    public void setWithAltLabel(boolean withAltLabel) {
        this.withAltLabel = withAltLabel;
    }

    public boolean isPermuted() {
        return permuted;
    }

    public void setPermuted(boolean permuted) {
        this.permuted = permuted;
    }

}
