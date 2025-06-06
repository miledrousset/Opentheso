/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.leftbody.viewliste;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.SearchService;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;


@Data
@SessionScoped
@Named(value = "listIndex")
@RequiredArgsConstructor
public class ListIndex implements Serializable {

    private final SearchService searchService;
    private final SelectedTheso selectedTheso;
    private final CurrentUser currentUser;
    private final RightBodySetting rightBodySetting;
    private final LeftBodySetting leftBodySetting;
    private final ConceptView conceptBean;
    private final PropositionBean propositionBean;

    private String searchValue;
    private NodeIdValue selectedNode;
    private List<NodeIdValue> nodeIdValues;

    private boolean withAltLabel, permuted;


    public void onChange() {

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            return;
        }

        if (StringUtils.isEmpty(selectedTheso.getCurrentLang())) {
            return;
        }

        if (StringUtils.isEmpty(searchValue)) {
            return;
        }

        nodeIdValues = searchService.searchTermForIndex(searchValue, selectedTheso.getCurrentLang(),
                selectedTheso.getCurrentIdTheso(), permuted, withAltLabel);
        PrimeFaces.current().ajax().update("formList");
    }

    public void reset() {
        searchValue = null;
        selectedNode = null;
        if (nodeIdValues != null) {
            nodeIdValues.clear();
            nodeIdValues = null;
        }
    }

    public void onRowSelect(SelectEvent event) {

        rightBodySetting.setShowConceptToOn();
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                ((NodeIdValue) event.getObject()).getId(), selectedTheso.getCurrentLang(), currentUser);
        rightBodySetting.setIndex("0");
        propositionBean.setRubriqueVisible(false);
        leftBodySetting.setIndex("1");
        PrimeFaces.current().executeScript("srollToSelected();");
    }
}
