package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.models.alignment.NodeSelectedAlignment;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.models.alignment.AlignementSource;
import fr.cnrs.opentheso.services.AlignmentService;
import fr.cnrs.opentheso.services.AlignmentSourceService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.PrimeFaces;

import static java.net.HttpURLConnection.HTTP_OK;


@Slf4j
@Data
@SessionScoped
@RequiredArgsConstructor
@Named(value = "setAlignmentSourceBean")
public class SetAlignmentSourceBean implements Serializable {

    private final ConceptView conceptView;
    private final SelectedTheso selectedTheso;
    private final AlignmentBean alignmentBean;
    private final AlignmentService alignmentService;
    private final AlignmentSourceService alignmentSourceService;

    private String sourceName, sourceUri, sourceIdThesaurus, description, sourceSelectedName;
    private AlignementSource selectedAlignementSource, alignementSourceToUpdate;
    private List<AlignementSource> allAlignementSources;
    private NodeSelectedAlignment selectedSource;
    private List<NodeSelectedAlignment> selectedAlignments, selectedAlignmentsOfThesaurus, nodeSelectedAlignmentsAll, sourcesSelected;
    

    public void init() {
        initSourcesList();
        alignmentBean.setViewSetting(true);
        alignmentBean.setViewAddNewSource(false);
        alignementSourceToUpdate = null;
        selectedAlignments = new ArrayList<>();
    }
    
    public void initForManage(){
        alignmentBean.initForManageAlignment();
        initSourcesList();
    }
    
    public void initForUpdate(NodeSelectedAlignment nodeSelectedAlignment){
        alignementSourceToUpdate= alignmentSourceService.getAlignementSourceById(nodeSelectedAlignment.getIdAlignmentSource());
    }

    public void initAlignementAutomatique(String alignementMode) {

        initSourcesList();

        sourcesSelected = nodeSelectedAlignmentsAll.stream().filter(NodeSelectedAlignment::isSelected).toList();
        if (CollectionUtils.isEmpty(sourcesSelected)) {
            MessageUtils.showWarnMessage("Veuillez sélectionner une source !");
            return;
        }

        alignmentBean.setMode(alignementMode);

        if (sourcesSelected.size() == 1) {
            var sourceFound = alignmentBean.getAlignementSources().stream()
                    .filter(source -> source.getId() == sourcesSelected.get(0).getIdAlignmentSource())
                    .findFirst();
            if (sourceFound.isPresent()) {
                sourceSelectedName = sourceFound.get().getSource();
                alignmentBean.searchAlignementsForAllConcepts(sourceFound.get());
            } else {
                MessageUtils.showWarnMessage("Veuillez sélectionner une source !");
            }
            return;
        }

        sourcesSelected.forEach(source -> source.setSelected(false));
        PrimeFaces.current().executeScript("PF('selectSourceManagement').show();");
    }

    public void updateSelectedSource(NodeSelectedAlignment selectedAlignment) throws SQLException {

        if (selectedAlignment.isSelected()) {
            alignmentSourceService.addSourceAlignementToThesaurus(selectedTheso.getCurrentIdTheso(), selectedAlignment.getIdAlignmentSource());
        } else {
            alignmentSourceService.deleteAlignmentSource(selectedAlignment.getIdAlignmentSource());
        }
        MessageUtils.showInformationMessage("Source mise à jour !");
    }

    public void startAlignementAutomatique() {
        if (!ObjectUtils.isEmpty(selectedSource)) {
            alignmentBean.setAlignementSources(alignmentSourceService.getAlignementSources(selectedTheso.getCurrentIdTheso()));
            var sourceFound = alignmentBean.getAlignementSources().stream()
                    .filter(source -> source.getId() == selectedSource.getIdAlignmentSource())
                    .findFirst();
            if (sourceFound.isPresent()) {
                sourceSelectedName = sourceFound.get().getSource();
                alignmentBean.initAlignmentType();
                alignmentBean.searchAlignementsForAllConcepts(sourceFound.get());
                selectedSource = null;
                PrimeFaces.current().executeScript("PF('selectSourceManagement').hide();");
            }
        } else {
            MessageUtils.showWarnMessage("Veuillez sélectionner une source !");
        }
    }

    public void initSourcesList() {

        // toutes les sources d'alignements
        allAlignementSources = alignmentSourceService.getAllAlignementSources();
        nodeSelectedAlignmentsAll = new ArrayList<>();

        // la liste des sources sélectionnées pour le thésaurus en cours
        selectedAlignmentsOfThesaurus = alignmentService.getSelectedAlignementOfThisThesaurus(selectedTheso.getCurrentIdTheso());

        // intégrer les éléments dans un vecteur global pour la modifiation
        for (AlignementSource allAlignementSource : allAlignementSources) {
            NodeSelectedAlignment nodeSelectedAlignment = new NodeSelectedAlignment();
            nodeSelectedAlignment.setIdAlignmentSource(allAlignementSource.getId());
            nodeSelectedAlignment.setSourceLabel(allAlignementSource.getSource());
            nodeSelectedAlignment.setSourceDescription(allAlignementSource.getDescription());
            nodeSelectedAlignment.setSelected(false);
            nodeSelectedAlignmentsAll.add(nodeSelectedAlignment);
        }

        for (NodeSelectedAlignment selectedAlignmentOfTheso : selectedAlignmentsOfThesaurus) {
            for (NodeSelectedAlignment nodeSelectedAlignment : nodeSelectedAlignmentsAll) {
                if (nodeSelectedAlignment.getIdAlignmentSource() == selectedAlignmentOfTheso.getIdAlignmentSource()) {
                    nodeSelectedAlignment.setSelected(true);
                }
            }
        }
    }

    public void initAddSource() {

        sourceName = null;
        sourceUri = null;
        sourceIdThesaurus = null;
        description = null;

        alignmentBean.setViewAddNewSource(true);
        alignmentBean.setComparaisonVisible(false);
        alignmentBean.setViewSetting(false);
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    
    public void deleteAlignmentSource(NodeSelectedAlignment alignment) {
        if (alignment == null) {
            return;
        }

        if (!alignmentSourceService.deleteAlignmentSource(alignment.getIdAlignmentSource())) {
            MessageUtils.showErrorMessage("Erreur pendant la suppression de la source !");
            return;
        }

        MessageUtils.showWarnMessage("Suppression réussie");
        initForManage();
    }              
            
            
    public void updateAlignmentSource() {
        if (alignementSourceToUpdate == null) {
            return;
        }

        if (!alignmentSourceService.updateAlignmentSource(alignementSourceToUpdate)) {
            MessageUtils.showWarnMessage("Erreur pendant la mise à jour de la source !");
            return;
        }

        MessageUtils.showInformationMessage("Source mise à jour avec succès");
        initForManage();
    }    
    
    /**
     * permet d'ajouter une source d'alignement et affecter cette source au thésaurus en cours.
     */
    public void addAlignmentSource(int idUser) throws Exception {

        if (sourceName == null || sourceName.isEmpty()) {
            MessageUtils.showWarnMessage("Le nom de la source est obligatoire !");
            return;
        }
        if (sourceUri == null || sourceUri.isEmpty()) {
            MessageUtils.showWarnMessage("L'URL est obligatoire !");
            return;
        }
        if (sourceIdThesaurus == null || sourceIdThesaurus.isEmpty()) {
            MessageUtils.showWarnMessage( "L'Id. du thésaurus est obligatoire !");
            return;
        }

        if (sourceUri.lastIndexOf("/") + 1 == sourceUri.length()) {
            sourceUri = sourceUri.substring(0, sourceUri.length() - 1);
        }


        if(!testNewSourceAlignment(sourceUri)) {
            MessageUtils.showWarnMessage("Uri du serveur non valide !");
            return;
        }
        
        var alignementSource = new AlignementSource();
        alignementSource.setAlignement_format("skos");
        alignementSource.setDescription(description);
        alignementSource.setRequete(sourceUri + "/api/search?q=##value##&lang=##lang##&theso=" + sourceIdThesaurus);
        alignementSource.setSource(sourceName);
        alignementSource.setTypeRequete("REST");
        if (!alignmentSourceService.addNewAlignmentSource(alignementSource, selectedTheso.getCurrentIdTheso(), idUser)) {
            MessageUtils.showWarnMessage("Erreur côté base de données !");
            return;
        }

        MessageUtils.showInformationMessage("Source ajoutée avec succès !");

        initForManage();
        alignmentBean.setViewAddNewSource(false);
        alignmentBean.setViewSetting(false);
    }
    
    private boolean testNewSourceAlignment(String uri) throws Exception {

        var url = new URL(uri + "/api/ping");
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return HTTP_OK == connection.getResponseCode();
    }

    public void cancel() {
        alignmentBean.setViewSetting(false);
        alignmentBean.setViewAddNewSource(false);
    }

    public void clearValues() {
        sourceName = "";
        sourceUri = "";
        sourceIdThesaurus = "";
        description = "";
    }
}
