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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
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
    
    
    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (allAlignementSources != null) {
            allAlignementSources.clear();
            allAlignementSources = null;
        }
        if (selectedAlignmentsOfTheso != null) {
            selectedAlignmentsOfTheso.clear();
            selectedAlignmentsOfTheso = null;
        }
        if (nodeSelectedAlignmentsAll != null) {
            nodeSelectedAlignmentsAll.clear();
            nodeSelectedAlignmentsAll = null;
        }
        sourceName = null;
        sourceUri = null;
        sourceIdTheso = null;
        description = null;
    }

    public SetAlignmentSourceBean() {
    }
    
    public void clearValues(){
        sourceName = "";
        sourceUri = "";
        sourceIdTheso = "";
        description = "";        
    }
    

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
    
    
    public void initAlignementAutomatique() {

        initSourcesList();

        sourcesSelected = nodeSelectedAlignmentsAll.stream()
                .filter(source -> source.isIsSelected())
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(sourcesSelected)) {
            showMessage(FacesMessage.SEVERITY_WARN, "Veuillez sélectionner une source !");
            return;
        }

        if (sourcesSelected.size() == 1) {
            var sourceFound = alignmentBean.getAlignementSources().stream()
                    .filter(source -> source.getId() == sourcesSelected.get(0).getIdAlignmnetSource())
                    .findFirst();
            if (sourceFound.isPresent()) {
                alignmentBean.searchAlignementsForAllConcepts(sourceFound.get());
            } else {
                showMessage(FacesMessage.SEVERITY_WARN, "Veuillez sélectionner une source !");
            }
            return;
        }

        sourcesSelected.forEach(source -> source.setIsSelected(false));
        PrimeFaces.current().executeScript("PF('selectSourceManagement').show();");
    }


    public void startAlignementAutomatique() {
        if (!ObjectUtils.isEmpty(selectedSource)) {
            var sourceFound = alignmentBean.getAlignementSources().stream()
                    .filter(source -> source.getId() == selectedSource.getIdAlignmnetSource())
                    .findFirst();
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

    private void initSourcesList() {
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        // toutes les sources d'alignements
        allAlignementSources = alignmentHelper.getAlignementSourceSAdmin(connect.getPoolConnexion());
        nodeSelectedAlignmentsAll = new ArrayList<>();

        // la liste des sources séléctionnées pour le thésaurus en cours
        selectedAlignmentsOfTheso = alignmentHelper.getSelectedAlignementOfThisTheso(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

        // intégrer les éléments dans un vecteur global pour la modifiation
        for (AlignementSource allAlignementSource : allAlignementSources) {
            NodeSelectedAlignment nodeSelectedAlignment = new NodeSelectedAlignment();
            nodeSelectedAlignment.setIdAlignmnetSource(allAlignementSource.getId());
            nodeSelectedAlignment.setSourceLabel(allAlignementSource.getSource());
            nodeSelectedAlignment.setSourceDescription(allAlignementSource.getDescription());
            nodeSelectedAlignment.setIsSelected(false);
            nodeSelectedAlignmentsAll.add(nodeSelectedAlignment);
        }

        for (NodeSelectedAlignment selectedAlignmentOfTheso : selectedAlignmentsOfTheso) {
            for (NodeSelectedAlignment nodeSelectedAlignment : nodeSelectedAlignmentsAll) {
                if (nodeSelectedAlignment.getIdAlignmnetSource() == selectedAlignmentOfTheso.getIdAlignmnetSource()) {
                    nodeSelectedAlignment.setIsSelected(true);
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
        alignmentBean.setViewSetting(false);
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    
    public void deleteAlignmentSource() {
        if (alignementSourceToUpdate == null) {
            return;
        }

        AlignmentHelper alignmentHelper = new AlignmentHelper();

        FacesMessage msg;

        if (!alignmentHelper.deleteAlignmentSource(
                connect.getPoolConnexion(),
                alignementSourceToUpdate.getId())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la suppression de la source !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Suppression réussie");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        initForManage();
    }              
            
            
    public void updateAlignmentSource() {
        if (alignementSourceToUpdate == null) {
            return;
        }

        AlignmentHelper alignmentHelper = new AlignmentHelper();

        FacesMessage msg;

        if (!alignmentHelper.updateAlignmentSource(
                connect.getPoolConnexion(),
                alignementSourceToUpdate)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la mise à jour de la source !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Source mise à jour avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        initForManage();
    }    
    
    /**
     * permet d'ajouter une source d'alignement et affecter cette source au
     * thésaurus en cours.
     *
     * @param idUser
     */
    public void addAlignmentSource(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (sourceName == null || sourceName.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Le nom de la source est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
          //      pf.ajax().update("containerIndex:formRightTab:viewTabConcept:addAlignmentForm:panelAddNewSource");
                pf.ajax().update("messageIndex");
            }
            return;
        }
        if (sourceUri == null || sourceUri.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'URL est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
          //      pf.ajax().update("containerIndex:formRightTab:viewTabConcept:addAlignmentForm:panelAddNewSource");
                pf.ajax().update("messageIndex");
            }
            return;
        }
        if (sourceIdTheso == null || sourceIdTheso.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'Id. du thésaurus est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
        //        pf.ajax().update("containerIndex:formRightTab:viewTabConcept:addAlignmentForm:panelAddNewSource");
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (sourceUri.lastIndexOf("/") + 1 == sourceUri.length()) {
            sourceUri = sourceUri.substring(0, sourceUri.length() - 1);
        }
        if(!testNewSourceAlignment(sourceUri)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Uri du serveur non valide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);    
            return;
        }
        
        String requete = sourceUri + "/api/search?q=##value##&lang=##lang##&theso=" + sourceIdTheso;
        
        AlignementSource alignementSource = new AlignementSource();
        alignementSource.setAlignement_format("skos");
        alignementSource.setDescription(description);
        alignementSource.setRequete(requete);
        alignementSource.setSource(sourceName);
        alignementSource.setTypeRequete("REST");

        AlignmentHelper alignementHelper = new AlignmentHelper();

        if (!alignementHelper.addNewAlignmentSource(
                connect.getPoolConnexion(),
                alignementSource,
                idUser, selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur côté base de données !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            alignementHelper.getMessage();
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", " Source ajoutée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        initForManage();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }
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

}
