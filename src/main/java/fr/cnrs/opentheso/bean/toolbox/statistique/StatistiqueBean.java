package fr.cnrs.opentheso.bean.toolbox.statistique;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.condidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.core.exports.csv.StatistiquesRapportCSV;
import fr.cnrs.opentheso.core.exports.rdf4j.ExportRdf4jHelperNew;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@Named(value = "statistiqueBean")
@SessionScoped
public class StatistiqueBean implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private LanguageBean languageBean;

    private boolean selectLanguageVisible, genericTypeVisible, conceptTypeVisible;
    private String selectedStatistiqueTypeCode, selectedCollection, nbrResultat;
    private int nbrCanceptByThes;
    private Date dateDebut, dateFin, derniereModification;
    private NodeLangTheso selectedLanguage;
    private ConceptStatisticData canceptStatistiqueSelected;

    private List<GenericStatistiqueData> genericStatistiques;
    private List<ConceptStatisticData> conceptStatistic;
    private ArrayList<NodeLangTheso> languagesOfTheso;
    private ArrayList<DomaineDto> groupList;

    public void inti() {

        selectLanguageVisible = false;
        genericTypeVisible = false;
        conceptTypeVisible = false;

        genericStatistiques = new ArrayList<>();
        conceptStatistic = new ArrayList<>();

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous devez choisir un Thesorus avant !");
            return;
        }

        initChamps();
    }

    public void clearFilter(){
        dateDebut = null;
        dateFin = null;
        selectedCollection = "";
        conceptStatistic = new ArrayList<>();
        genericStatistiques = new ArrayList<>();
    }
    
    private void initChamps() {
        languagesOfTheso = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(), selectedTheso.getSelectedIdTheso());

        groupList = new GroupHelper().getAllGroupsByThesaurusAndLang(connect, selectedTheso.getSelectedIdTheso(),
                languageBean.getIdLangue());
    }

    public void onSelectStatType() {
        
        genericStatistiques = new ArrayList<>();
        conceptStatistic = new ArrayList<>();

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous devez choisir un Thesorus avant !");
            return;
        }

        if (CollectionUtils.isEmpty(languagesOfTheso)) {
            initChamps();
        }

        selectLanguageVisible = true;
        genericTypeVisible = false;
        conceptTypeVisible = false;
    }

    public List<String> searchDomaineName(String enteredValue) {

        if ("%".equals(enteredValue)) {
            return groupList.stream().map(group -> group.getName()).collect(Collectors.toList());
        }

        List<String> matches = new ArrayList<>();

        groupList.stream().filter((s) -> (s.getName() != null && s.getName().toLowerCase().startsWith(enteredValue.toLowerCase()))).forEachOrdered((s) -> {
            matches.add(s.getName());
        });

        return matches;
    }

    private String searchGroupIdFromLabel(String label) {
        String groupId = "";
        for (DomaineDto group : groupList) {
            if (group.getName().equals(label)) {
                groupId = group.getId();
                break;
            }
        }
        return groupId;
    }

    public String formatLanguage(String langLabel) {
        return langLabel.substring(0, 1).toUpperCase() + langLabel.substring(1, langLabel.length());
    }

    public void onSelectLanguageType() throws SQLException {
        clearFilter();
        selectLanguageVisible = true;

        if ("0".equals(selectedStatistiqueTypeCode)) {

            ConceptHelper conceptHelper = new ConceptHelper();

            genericStatistiques = new StatistiqueService().searchAllCollectionsByThesaurus(connect, selectedTheso.getCurrentIdTheso(), selectedLanguage.getCode());

            nbrCanceptByThes = conceptHelper.getNbrOfCanceptByThes(connect.getPoolConnexion().getConnection(), selectedTheso.getCurrentIdTheso());

            derniereModification = conceptHelper.getLastModifcation(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

            genericTypeVisible = true;
            conceptTypeVisible = false;

            PrimeFaces.current().executeScript("PF('bui').hide();");
        } else {

            genericTypeVisible = false;
            conceptTypeVisible = true;
        }
    }
    
    public boolean isExportVisible() {
        return !CollectionUtils.isEmpty(genericStatistiques) || !CollectionUtils.isEmpty(conceptStatistic);
    }

    public void getStatisticByConcept() {
        conceptStatistic = new StatistiqueService().searchAllConceptsByThesaurus(this, 
                connect, selectedTheso.getCurrentIdTheso(),
                selectedLanguage.getCode(), dateDebut, dateFin, 
                searchGroupIdFromLabel(selectedCollection), nbrResultat);

        PrimeFaces.current().executeScript("PF('bui').hide();");
    }

    public StreamedContent exportStatiqituque() {

        StatistiquesRapportCSV statistiquesRapportCSV = new StatistiquesRapportCSV();
        if (genericTypeVisible) {
            statistiquesRapportCSV.createGenericStatitistiquesRapport(genericStatistiques);
        } else {
            statistiquesRapportCSV.createConceptsStatitistiquesRapport(conceptStatistic);
        }

        return DefaultStreamedContent.builder()
                .contentType("text/csv")
                .name(selectedTheso.getThesoName() + ".csv")
                .stream(() -> new ByteArrayInputStream(statistiquesRapportCSV.getOutput().toByteArray()))
                .build();

    }
    
    private ExportRdf4jHelperNew getThesorusDatas(String idTheso) {

        NodePreference nodePreference = new PreferencesHelper().getThesaurusPreferences(connect.getPoolConnexion(), idTheso);

        if (nodePreference == null) {
            return null;
        }

        ExportRdf4jHelperNew resources = new ExportRdf4jHelperNew();
        resources.setInfos(nodePreference, "dd-mm-yyyy", false, false);
        resources.exportTheso(connect.getPoolConnexion(), idTheso, nodePreference);
        ArrayList<String> allConcepts = new ConceptHelper().getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso);
        for (String idConcept : allConcepts) {
            resources.exportConcept(connect.getPoolConnexion(), idTheso, idConcept, false);
        }

        return resources;
    }
    
    public void setConceptSelected(ConceptStatisticData canceptStatistiqueSelected) {
        this.canceptStatistiqueSelected = canceptStatistiqueSelected;
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }

    public boolean showExportButton() {
        return genericTypeVisible || conceptTypeVisible;
    }

    public Date getDerniereModification() {
        return derniereModification;
    }

    public int getNbrCanceptByThes() {
        return nbrCanceptByThes;
    }

    public String getSelectedStatistiqueTypeCode() {
        return selectedStatistiqueTypeCode;
    }

    public void setSelectedStatistiqueTypeCode(String selectedStatistiqueTypeCode) {
        this.selectedStatistiqueTypeCode = selectedStatistiqueTypeCode;
    }

    public ArrayList<NodeLangTheso> getLanguagesOfTheso() {
        return languagesOfTheso;
    }

    public void setLanguagesOfTheso(ArrayList<NodeLangTheso> languagesOfTheso) {
        this.languagesOfTheso = languagesOfTheso;
    }

    public NodeLangTheso getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(NodeLangTheso selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public boolean isSelectLanguageVisible() {
        return selectLanguageVisible;
    }

    public boolean isGenericTypeVisible() {
        return genericTypeVisible;
    }

    public boolean isConceptTypeVisible() {
        return conceptTypeVisible;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public List<GenericStatistiqueData> getGenericStatistiques() {
        return genericStatistiques;
    }

    public void setGenericStatistiques(List<GenericStatistiqueData> genericStatistiques) {
        this.genericStatistiques = genericStatistiques;
    }

    public List<ConceptStatisticData> getCanceptStatistiques() {
        return conceptStatistic;
    }

    public void setCanceptStatistiques(List<ConceptStatisticData> canceptStatistiques) {
        this.conceptStatistic = canceptStatistiques;
    }

    public String getSelectedCollection() {
        return selectedCollection;
    }

    public void setSelectedCollection(String selectedCollection) {
        this.selectedCollection = selectedCollection;
    }

    public SelectedTheso getSelectedTheso() {
        return selectedTheso;
    }

    public String getNbrResultat() {
        return nbrResultat;
    }

    public void setNbrResultat(String nbrResultat) {
        this.nbrResultat = nbrResultat;
    }

    public ConceptStatisticData getCanceptStatistiqueSelected() {
        return canceptStatistiqueSelected;
    }

}