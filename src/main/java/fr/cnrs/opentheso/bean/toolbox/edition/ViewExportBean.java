/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.importexport.ExportFileBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "viewExportBean")
@SessionScoped
public class ViewExportBean implements Serializable {

    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private Connect connect;
    @Inject private ExportFileBean downloadBean; 

    private ArrayList<NodeLangTheso> languagesOfTheso;
    private ArrayList<NodeGroup> groupList;
    private NodePreference nodePreference;

    private List<NodeLangTheso> selectedLanguages;
    private List<NodeGroup> selectedGroups;

    private boolean exportUriArk = false;
    private boolean exportUriHandle = false;
    private NodeIdValue nodeIdValueOfTheso;

    private ArrayList<String> exportFormat;
    private String selectedExportFormat;

    public ViewExportBean() {
    }

    public void init(NodeIdValue nodeIdValueOfTheso) {
        this.nodeIdValueOfTheso = nodeIdValueOfTheso;

        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(), nodeIdValueOfTheso.getId());

        String idLang = selectedTheso.getCurrentLang();
        if (idLang == null || idLang.isEmpty()) {
            idLang = connect.getWorkLanguage();
        }

        GroupHelper groupHelper = new GroupHelper();
        groupList = groupHelper.getListConceptGroup(
                connect.getPoolConnexion(),
                nodeIdValueOfTheso.getId(),
                idLang);

        selectedLanguages = new ArrayList<>();
        for (NodeLangTheso nodeLang : languagesOfTheso) {
            selectedLanguages.add(nodeLang);
        }

        selectedGroups = new ArrayList<>();
        for (NodeGroup nodeGroup : groupList) {
            selectedGroups.add(nodeGroup);
        }

        PreferencesHelper preferencesHelper = new PreferencesHelper();
        nodePreference = preferencesHelper.getThesaurusPreferences(
                connect.getPoolConnexion(),
                nodeIdValueOfTheso.getId());
        exportUriArk = false;
        exportUriHandle = false;

        exportFormat = new ArrayList<>();
        exportFormat.add("skos");
        exportFormat.add("json");
        exportFormat.add("jsonLd");
        exportFormat.add("turtle");
        selectedExportFormat = "skos";

        downloadBean.init();
    }


    public ArrayList<NodeLangTheso> getLanguagesOfTheso() {
        return languagesOfTheso;
    }

    public void setLanguagesOfTheso(ArrayList<NodeLangTheso> languagesOfTheso) {
        this.languagesOfTheso = languagesOfTheso;
    }

    public ArrayList<NodeGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(ArrayList<NodeGroup> groupList) {
        this.groupList = groupList;
    }

    public boolean isExportUriHandle() {
        return exportUriHandle;
    }

    public void setExportUriHandle(boolean exportUriHandle) {
        this.exportUriHandle = exportUriHandle;
    }

    public NodeIdValue getNodeIdValueOfTheso() {
        return nodeIdValueOfTheso;
    }

    public void setNodeIdValueOfTheso(NodeIdValue nodeIdValueOfTheso) {
        this.nodeIdValueOfTheso = nodeIdValueOfTheso;
    }

    public List<NodeLangTheso> getSelectedLanguages() {
        return selectedLanguages;
    }

    public void setSelectedLanguages(List<NodeLangTheso> selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }

    public List<NodeGroup> getSelectedGroups() {
        return selectedGroups;
    }

    public void setSelectedGroups(List<NodeGroup> selectedGroups) {
        this.selectedGroups = selectedGroups;
    }

    public boolean isExportUriArk() {
        return exportUriArk;
    }

    public void setExportUriArk(boolean exportUriArk) {
        this.exportUriArk = exportUriArk;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public ArrayList<String> getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(ArrayList<String> exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getSelectedExportFormat() {
        return selectedExportFormat;
    }

    public void setSelectedExportFormat(String selectedExportFormat) {
        this.selectedExportFormat = selectedExportFormat;
    }



}
