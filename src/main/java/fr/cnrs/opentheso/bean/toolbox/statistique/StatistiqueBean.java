package fr.cnrs.opentheso.bean.toolbox.statistique;

import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.candidats.DomaineDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.services.exports.csv.StatistiquesRapportCSV;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.inject.Named;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;



@Data
@Named(value = "statistiqueBean")
@SessionScoped
@NoArgsConstructor
public class StatistiqueBean implements Serializable {
    
    private SelectedTheso selectedTheso;
    private LanguageBean languageBean;
    private GroupHelper groupHelper;
    private ThesaurusHelper thesaurusHelper;
    private ConceptHelper conceptHelper;
    private StatistiqueService statistiqueService;

    private boolean genericTypeVisible, conceptTypeVisible;
    private String selectedStatistiqueTypeCode, selectedCollection, nbrResultat, selectedLanguage;
    private int nbrCanceptByThes, nbrCandidateByThes, nbrDeprecatedByThes;
    private Date dateDebut, dateFin, derniereModification;
    private ConceptStatisticData canceptStatistiqueSelected;

    private List<GenericStatistiqueData> genericStatistiques;
    private List<ConceptStatisticData> canceptStatistiques;
    private ArrayList<NodeLangTheso> languagesOfTheso;
    private ArrayList<DomaineDto> groupList;

    private List<String> colors = new ArrayList<>(List.of("rgb(255, 99, 132)","rgb(54, 162, 235)",
            "rgb(75, 192, 192)","rgb(158, 14, 64)",
            "rgb(136, 66, 29)", "rgb(240, 195, 0)", "rgb(63, 34, 4)", "rgb(29, 96, 198)",
            "rgb(121, 248, 248)", "rgb(0, 204, 203)", "rgb(23, 101, 125)", "rgb(102, 0, 255)",
            "rgb(0, 255, 0)", "rgb(135, 233, 144)", "rgb(9, 106, 9)", "rgb(112, 141, 35)",
            "rgb(255, 205, 86)"));


    @Inject
    public StatistiqueBean(SelectedTheso selectedTheso,
                           LanguageBean languageBean,
                           GroupHelper groupHelper,
                           ThesaurusHelper thesaurusHelper,
                           ConceptHelper conceptHelper,
                           StatistiqueService statistiqueService) {

        this.selectedTheso = selectedTheso;
        this.languageBean = languageBean;
        this.groupHelper = groupHelper;
        this.thesaurusHelper = thesaurusHelper;
        this.conceptHelper = conceptHelper;
        this.statistiqueService = statistiqueService;
    }
    
    public void init() {

        genericTypeVisible = false;
        conceptTypeVisible = false;

        genericStatistiques = new ArrayList<>();
        canceptStatistiques = new ArrayList<>();

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous devez choisir un Thesorus avant !");
            return;
        }

        selectedLanguage = selectedTheso.getCurrentLang();
        initChamps();
    }

    public void clearFilter(){
        dateDebut = null;
        dateFin = null;
        selectedCollection = "";
        canceptStatistiques = new ArrayList<>();
        genericStatistiques = new ArrayList<>();
    }

    public DonutChartModel createChartModel(int model) {

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();

        int pos = 0;
        for (GenericStatistiqueData genericStatistiqueData : genericStatistiques) {
            switch(model) {
                case 1:
                    values.add(genericStatistiqueData.getConceptsNbr());
                    break;
                case 2:
                    values.add(genericStatistiqueData.getSynonymesNbr());
                    break;
                case 3:
                    values.add(genericStatistiqueData.getTermesNonTraduitsNbr());
                    break;
                case 4:
                    values.add(genericStatistiqueData.getNotesNbr());
                    break;
            }
            labels.add(genericStatistiqueData.getCollection());
            bgColors.add(colors.get(pos));
            pos ++;
            if (pos == colors.size()) pos = 0;
        }

        var dataSet = new DonutChartDataSet();
        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);

        var data = new ChartData();
        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        var donutModel = new DonutChartModel();
        donutModel.setData(data);
        return donutModel;
    }
    
    private void initChamps() {

        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(selectedTheso.getSelectedIdTheso(), languageBean.getIdLangue());
        groupList = groupHelper.getAllGroupsByThesaurusAndLang(selectedTheso.getSelectedIdTheso(), languageBean.getIdLangue());
    }

    public void onSelectStatType() {
        
        genericStatistiques = new ArrayList<>();
        canceptStatistiques = new ArrayList<>();

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous devez choisir un Thesorus avant !");
            return;
        }

        if (CollectionUtils.isEmpty(languagesOfTheso)) {
            initChamps();
        }

        genericTypeVisible = false;
        conceptTypeVisible = false;
    }

    public List<String> searchDomaineName(String enteredValue) {

        if ("%".equals(enteredValue)) {
            return groupList.stream()
                    .map(group -> group.getName())
                    .collect(Collectors.toList());
        } else {
            return groupList.stream()
                    .filter((s) -> (s.getName() != null && s.getName().toLowerCase().startsWith(enteredValue.toLowerCase())))
                    .map(element -> element.getName())
                    .toList();
        }
    }

    private String searchGroupIdFromLabel(String label) {

        if (CollectionUtils.isNotEmpty(groupList)) {
            var groupLabel = groupList.stream().filter(element -> element.getName().equals(label)).findFirst();
            return groupLabel.isPresent() ? groupLabel.get().getId() : "";
        }
        return "";
    }

    public String formatLanguage(String langLabel) {
        return langLabel.substring(0, 1).toUpperCase() + langLabel.substring(1, langLabel.length());
    }

    public void onSelectLanguageType() {

        onSelectStatType();
        clearFilter();

        if ("0".equals(selectedStatistiqueTypeCode)) {

            genericStatistiques = statistiqueService.searchAllCollectionsByThesaurus(selectedTheso.getCurrentIdTheso(), selectedLanguage);

            nbrCanceptByThes = statistiqueService.countValidConceptsByThesaurus(selectedTheso.getCurrentIdTheso());

            var conceptsList = statistiqueService.findAllByThesaurusIdThesaurusAndStatus(selectedTheso.getCurrentIdTheso(), "CA");
            nbrCandidateByThes = CollectionUtils.isNotEmpty(conceptsList) ? conceptsList.size() : 0;

            var deprecatedConceptsList = statistiqueService.findAllByThesaurusIdThesaurusAndStatus(selectedTheso.getCurrentIdTheso(), "DEP");
            nbrDeprecatedByThes = CollectionUtils.isNotEmpty(deprecatedConceptsList) ? deprecatedConceptsList.size() : 0;

            derniereModification = conceptHelper.getLastModification(selectedTheso.getCurrentIdTheso());

            genericTypeVisible = true;
            conceptTypeVisible = false;
        } else {
            genericTypeVisible = false;
            conceptTypeVisible = true;
        }

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
    }
    
    public boolean isExportVisible() {
        return !CollectionUtils.isEmpty(genericStatistiques) || !CollectionUtils.isEmpty(canceptStatistiques);
    }

    public void getStatisticByConcept() {
        canceptStatistiques = statistiqueService.searchAllConceptsByThesaurus(this,
                selectedTheso.getCurrentIdTheso(),
                selectedLanguage, dateDebut, dateFin,
                searchGroupIdFromLabel(selectedCollection), nbrResultat);
    }

    public StreamedContent exportStatiqituque() {

        var statistiquesRapportCSV = new StatistiquesRapportCSV();
        if (genericTypeVisible) {
            statistiquesRapportCSV.createGenericStatitistiquesRapport(genericStatistiques);
        } else {
            statistiquesRapportCSV.createConceptsStatitistiquesRapport(canceptStatistiques);
        }

        return DefaultStreamedContent.builder()
                .contentType("text/csv")
                .name(selectedTheso.getThesoName() + ".csv")
                .stream(() -> new ByteArrayInputStream(statistiquesRapportCSV.getOutput().toByteArray()))
                .build();

    }
    
    public void setConceptSelected(ConceptStatisticData canceptStatistiqueSelected) {
        this.canceptStatistiqueSelected = canceptStatistiqueSelected;
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }
}
