package fr.cnrs.opentheso.core.exports.pdf;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;


@Named(value = "exportBean")
@SessionScoped
public class ExportBean implements Serializable {

    @Inject
    private LanguageBean languageBean;
    
    @Inject
    private Connect connect;

    private String formatFile;
    private String idTheso;
    private int exportFormat;

    private String singleLanguageCodeSelected;
    private String singleLanguageCodeSelected2;

    private List<NodeLangTheso> languagesOfTheso;
    private List<NodeLangTheso> selectedLanguages;
    
    private List<NodeGroup> groupList;
    private List<NodeGroup> selectedGroups;

    private NodePreference nodePreference;
    private boolean exportUriArk = false;
    private boolean exportUriHandle = false;
    

    public void init(NodeIdValue nodeIdValueOfTheso, String format) {

        this.formatFile = format;
        this.idTheso = nodeIdValueOfTheso.getId();

        languagesOfTheso = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(connect.getPoolConnexion(), nodeIdValueOfTheso.getId());

        groupList = new GroupHelper().getListConceptGroup(connect.getPoolConnexion(), nodeIdValueOfTheso.getId(), languageBean.getIdLangue());

        selectedLanguages = new ArrayList<>();
        languagesOfTheso.forEach((nodeLang) -> {
            selectedLanguages.add(nodeLang);
        });

        selectedGroups = new ArrayList<>();
        groupList.forEach((nodeGroup) -> {
            selectedGroups.add(nodeGroup);
        });

        nodePreference = new PreferencesHelper().getThesaurusPreferences(connect.getPoolConnexion(), nodeIdValueOfTheso.getId());

        exportUriArk = false;
        exportUriHandle = false;     
                
    }
    
    public String getExportButtonLabel() {
        if (isPdfExport()) {
            return languageBean.getMsg("edit.exportpdf");
        } else {
            return languageBean.getMsg("edit.exportcsv");
        }
    }

    public List<NodeLangTheso> getSelectedLanguages() {
        return selectedLanguages;
    }

    public void setSelectedLanguages(List<NodeLangTheso> selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }

    public List<NodeLangTheso> getLanguagesOfTheso() {
        return languagesOfTheso;
    }

    public List<String> getCodesLanguagesOfTheso() {

        List<String> codes = new ArrayList<>();
        if (languagesOfTheso == null) {
            return null;
        }

        languagesOfTheso.forEach((nodeLang) -> {
            codes.add(nodeLang.getCode());
        });

        return codes;
    }

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    public Connect getConnect() {
        return connect;
    }

    public void setConnect(Connect connect) {
        this.connect = connect;
    }    

    public void setLanguagesOfTheso(List<NodeLangTheso> languagesOfTheso) {
        this.languagesOfTheso = languagesOfTheso;
    }

    public List<NodeGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<NodeGroup> groupList) {
        this.groupList = groupList;
    }

    public List<NodeGroup> getSelectedGroups() {
        return selectedGroups;
    }

    public void setSelectedGroups(List<NodeGroup> selectedGroups) {
        this.selectedGroups = selectedGroups;
    }

    public int getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(int exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getSingleLanguageCodeSelected() {
        return singleLanguageCodeSelected;
    }

    public void setSingleLanguageCodeSelected(String sigleLanguageCodeSelected) {
        this.singleLanguageCodeSelected = sigleLanguageCodeSelected;
    }

    public String getSingleLanguageCodeSelected2() {
        return singleLanguageCodeSelected2;
    }

    public void setSingleLanguageCodeSelected2(String singleLanguageCodeSelected2) {
        this.singleLanguageCodeSelected2 = singleLanguageCodeSelected2;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public boolean isExportUriArk() {
        return exportUriArk;
    }

    public void setExportUriArk(boolean exportUriArk) {
        this.exportUriArk = exportUriArk;
    }

    public boolean isExportUriHandle() {
        return exportUriHandle;
    }

    public void setExportUriHandle(boolean exportUriHandle) {
        this.exportUriHandle = exportUriHandle;
    }

    public String getIdTheso() {
        return idTheso;
    }

    public void setIdTheso(String idTheso) {
        this.idTheso = idTheso;
    }

    public boolean isPdfExport() {
        return "PDF".equals(formatFile);
    }

    public boolean isCsvExport() {
        return "CSV".equals(formatFile);
    }
    
    public String getFormat() {
        return formatFile;
    } 
    
}