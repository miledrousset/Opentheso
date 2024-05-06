package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeSelectedAlignment;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.core.alignment.AlignementSource;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@Named(value = "setAlignmentSourceBean")
public class SetAlignmentSourceBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private ConceptView conceptView;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private AlignmentBean alignmentBean;

    private ArrayList<AlignementSource> allAlignementSources;
    private List<NodeSelectedAlignment> selectedAlignments = new ArrayList<>();
    private ArrayList<NodeSelectedAlignment> selectedAlignmentsOfTheso;
    private NodeSelectedAlignment selectedSource;

    private List<NodeSelectedAlignment> nodeSelectedAlignmentsAll, sourcesSelected;

    //// pour l'ajout d'une nouvelle source
    private String sourceName;
    private String sourceUri;
    private String sourceIdTheso;
    private String description;

    private AlignementSource selectedAlignementSource;
    private AlignementSource alignementSourceToUpdate;

    private String sourceSelectedName;
    

    public void init() {
        initSourcesList();
        alignmentBean.setViewSetting(true);
        alignmentBean.setViewAddNewSource(false);
        alignementSourceToUpdate = null;
    }
    
    public void initForManage(){
        alignmentBean.initForManageAlignment();
        initSourcesList();
    }
    
    public void initForUpdate(int id){
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        alignementSourceToUpdate= alignmentHelper.getThisAlignementSource(connect.getPoolConnexion(), id);
    }

    public void initAlignementAutomatique(String alignementMode) {

        initSourcesList();

        sourcesSelected = nodeSelectedAlignmentsAll.stream()
                .filter(source -> source.isSelected())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(sourcesSelected)) {
            showMessage(FacesMessage.SEVERITY_WARN, "Veuillez sélectionner une source !");
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
                showMessage(FacesMessage.SEVERITY_WARN, "Veuillez sélectionner une source !");
            }
            return;
        }

        sourcesSelected.forEach(source -> source.setSelected(false));
        PrimeFaces.current().executeScript("PF('selectSourceManagement').show();");
    }

    public void updateSelectedSource(NodeSelectedAlignment selectedAlignment) throws SQLException {

        if (selectedAlignment.isSelected()) {
            new AlignmentHelper().addSourceAlignementToTheso(connect.getPoolConnexion(),
                    selectedTheso.getCurrentIdTheso(), selectedAlignment.getIdAlignmentSource());
        } else {
            new AlignmentHelper().deleteSourceAlignementFromTheso(connect.getPoolConnexion().getConnection(),
                    selectedTheso.getCurrentIdTheso(), selectedAlignment.getIdAlignmentSource());
        }
        showMessage(FacesMessage.SEVERITY_INFO, "Source mise à jour !");
    }

    public void startAlignementAutomatique() {
        if (!ObjectUtils.isEmpty(selectedSource)) {
            var sourceFound = alignmentBean.getAlignementSources().stream()
                    .filter(source -> source.getId() == selectedSource.getIdAlignmentSource())
                    .findFirst();
            sourceSelectedName = sourceFound.get().getSource();
            alignmentBean.searchAlignementsForAllConcepts(sourceFound.get());
            selectedSource = null;
            PrimeFaces.current().executeScript("PF('selectSourceManagement').hide();");
        } else {
            showMessage(FacesMessage.SEVERITY_WARN, "Veuillez sélectionner une source !");
        }
    }

    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesMessage msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public void initSourcesList() {
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        // toutes les sources d'alignements
        allAlignementSources = alignmentHelper.getAlignementSourceSAdmin(connect.getPoolConnexion());
        nodeSelectedAlignmentsAll = new ArrayList<>();

        // la liste des sources séléctionnées pour le thésaurus en cours
        selectedAlignmentsOfTheso = alignmentHelper.getSelectedAlignementOfThisTheso(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

        // intégrer les éléments dans un vecteur global pour la modifiation
        for (AlignementSource allAlignementSource : allAlignementSources) {
            NodeSelectedAlignment nodeSelectedAlignment = new NodeSelectedAlignment();
            nodeSelectedAlignment.setIdAlignmentSource(allAlignementSource.getId());
            nodeSelectedAlignment.setSourceLabel(allAlignementSource.getSource());
            nodeSelectedAlignment.setSourceDescription(allAlignementSource.getDescription());
            nodeSelectedAlignment.setSelected(false);
            nodeSelectedAlignmentsAll.add(nodeSelectedAlignment);
        }

        for (NodeSelectedAlignment selectedAlignmentOfTheso : selectedAlignmentsOfTheso) {
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
        sourceIdTheso = null;
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

        if (!new AlignmentHelper().deleteAlignmentSource(connect.getPoolConnexion(), alignment.getIdAlignmentSource())) {
            showMessage(FacesMessage.SEVERITY_ERROR,"Erreur pendant la suppression de la source !");
            return;
        }

        showMessage(FacesMessage.SEVERITY_ERROR,"Suppression réussie");
        initForManage();
    }              
            
            
    public void updateAlignmentSource() {
        if (alignementSourceToUpdate == null) {
            return;
        }

        if (!new AlignmentHelper().updateAlignmentSource(connect.getPoolConnexion(), alignementSourceToUpdate)) {
            showMessage(FacesMessage.SEVERITY_ERROR,"Erreur pendant la mise à jour de la source !");
            return;
        }

        showMessage(FacesMessage.SEVERITY_INFO,"Source mise à jour avec succès");

        initForManage();
    }    
    
    /**
     * permet d'ajouter une source d'alignement et affecter cette source au thésaurus en cours.
     */
    public void addAlignmentSource(int idUser) {

        if (sourceName == null || sourceName.isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR,"Le nom de la source est obligatoire !");
            return;
        }
        if (sourceUri == null || sourceUri.isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR,"L'URL est obligatoire !");
            return;
        }
        if (sourceIdTheso == null || sourceIdTheso.isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "L'Id. du thésaurus est obligatoire !");
            return;
        }

        if (sourceUri.lastIndexOf("/") + 1 == sourceUri.length()) {
            sourceUri = sourceUri.substring(0, sourceUri.length() - 1);
        }


        if(!testNewSourceAlignment(sourceUri)) {
            showMessage(FacesMessage.SEVERITY_ERROR,"Uri du serveur non valide !");
            return;
        }
        
        AlignementSource alignementSource = new AlignementSource();
        alignementSource.setAlignement_format("skos");
        alignementSource.setDescription(description);
        alignementSource.setRequete(sourceUri + "/api/search?q=##value##&lang=##lang##&theso=" + sourceIdTheso);
        alignementSource.setSource(sourceName);
        alignementSource.setTypeRequete("REST");

        if (!new AlignmentHelper().addNewAlignmentSource(connect.getPoolConnexion(), alignementSource, idUser, selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_ERROR,"Erreur côté base de données !");
            return;
        }

        showMessage(FacesMessage.SEVERITY_INFO,"Source ajoutée avec succès !");

        initForManage();
        alignmentBean.setViewAddNewSource(false);
        alignmentBean.setViewSetting(false);
    }
    
    private boolean testNewSourceAlignment(String uri){
        uri = uri + "/api/ping";
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {  
                return true;
            }      
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void cancel() {
        alignmentBean.setViewSetting(false);
        alignmentBean.setViewAddNewSource(false);
    }

    public void clearValues() {
        sourceName = "";
        sourceUri = "";
        sourceIdTheso = "";
        description = "";
    }

}
