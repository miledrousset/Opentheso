package fr.cnrs.opentheso.bean.toolbox.atelier;

import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.fusion.FusionService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import jakarta.inject.Named;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


@Getter
@Setter
@SessionScoped
@RequiredArgsConstructor
@Named("atelierThesBean")
public class AtelierThesBean implements Serializable {

    private final AtelierThesService atelierThesService;
    private final FusionService fusionService;

    private NodeIdValue thesaurusSelected;
    private List<String> titles;
    private List<List<String>> values = new ArrayList<>();
    private List<NodeIdValue> nodeListThesaurus;
    private List<ConceptResultNode> result;
    private int spanTable, choiceDelimiter = 0;
    private char delimiterCsv = ',';
    private String selectedColumn, actionSelected;


    @PostConstruct
    public void init() {

        fusionService.setConceptsAjoutes(new ArrayList<>());
        fusionService.setConceptsExists(new ArrayList<>());
        fusionService.setConceptsModifies(new ArrayList<>());

        thesaurusSelected = null;
        actionSelected = null;
        fusionService.setFusionBtnEnable(false);
        fusionService.setFusionDone(false);
        fusionService.setLoadDone(false);

        choiceDelimiter = 0;
        delimiterCsv = ',';
        titles = new ArrayList<>();
        values = new ArrayList<>();
        result = new ArrayList<>();
        nodeListThesaurus = atelierThesService.searchAllThesaurus();
    }

    public void clearAll() {
        if(titles == null)
            titles = new ArrayList<>();
        else
            titles.clear();
        if(values == null)
            values = new ArrayList<>();
        else
            values.clear();
        if(result == null)
            result = new ArrayList<>();
        else
            result.clear();
        nodeListThesaurus = atelierThesService.searchAllThesaurus();
    }

    public void actionChoice() {
        if(choiceDelimiter == 0)
            delimiterCsv = ',';
        if(choiceDelimiter == 1)
            delimiterCsv = ';';
        if(choiceDelimiter == 2)
            delimiterCsv = '\t';
    }

    public void fusionner() {
        fusionService.lancerFussion(thesaurusSelected);
    }

    public void comparer() {
        
        int position = titles.indexOf(selectedColumn);
        result = atelierThesService.comparer(values, position, thesaurusSelected);

        MessageUtils.showInformationMessage(result.size() + " résultat(s) trouvées !");

        PrimeFaces.current().executeScript("PF('bui').hide();");
    }

    public void loadFileCsv(FileUploadEvent event) {
        clearAll();
        List<List<String>> temp = atelierThesService.loadCsvFile(event, delimiterCsv);
        if (!CollectionUtils.isEmpty(temp)) {
            titles = temp.get(0);
            for (int i = 1; i < temp.size(); i++) {
                values.add(temp.get(i));
            }
            spanTable = 12 / titles.size();
            MessageUtils.showInformationMessage(values.size() + " donnée(s) trouvées !");
        } else {
            MessageUtils.showErrorMessage("Aucune donnée trouvées !");
        }

        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
    }

    public StreamedContent exportResultat() {

        ExportResultatCsv exportResultatCsv = new ExportResultatCsv();
        exportResultatCsv.createResultatFileRapport(result);

        return DefaultStreamedContent.builder()
                .contentType("text/csv")
                .name("Résultat.csv")
                .stream(() -> new ByteArrayInputStream(exportResultatCsv.getOutput().toByteArray()))
                .build();

    }

    public String onFlowProcess(FlowEvent event) {
        if ("actions".equals(event.getNewStep())){
            return event.getNewStep();
        }
        if ("actions".equals(event.getOldStep())) {
            if (StringUtils.isEmpty(actionSelected)) {
                MessageUtils.showErrorMessage("Vous devez selectionnez une action !");
                return event.getOldStep();
            } else if ("opt2".equals(actionSelected)) {
                MessageUtils.showInformationMessage("Cette action n'est pas disponible pour le moment ..");
                return event.getOldStep();
            } else {
                return event.getNewStep();
            }
        } else if ("entre".equals(event.getOldStep())) {
            if ("opt1".equals(actionSelected)) {
                if ("actions".equals(event.getNewStep())) {
                    return event.getNewStep();
                } else if (CollectionUtils.isEmpty(values)) {
                    MessageUtils.showErrorMessage("Vous devez ajouter des données d'entrées !");
                    return event.getOldStep();
                } else {
                    return event.getNewStep();
                }
            } else {
                if (!fusionService.isLoadDone()) {
                    MessageUtils.showErrorMessage("Vous devez importer un thesaurus !");
                    return event.getOldStep();
                }
                return event.getNewStep();
            }
        } else if ("thesaurus".equals(event.getOldStep())) {
            if ("entre".equals(event.getNewStep())) {
                return event.getNewStep();
            } else if (thesaurusSelected == null) {
                MessageUtils.showErrorMessage("Vous devez selectionner un thesaurus !");
                return event.getOldStep();
            } else {
                return event.getNewStep();
            }
        } else {
            fusionService.initFusionResult();
            return event.getNewStep();
        }
    }
}
