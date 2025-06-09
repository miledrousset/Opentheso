package fr.cnrs.opentheso.bean.facet;

import fr.cnrs.opentheso.models.facets.NodeFacet;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.FacetService;
import fr.cnrs.opentheso.services.NoteService;
import fr.cnrs.opentheso.services.SearchService;
import fr.cnrs.opentheso.services.TermService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;


@Data
@Named(value = "editFacet")
@SessionScoped
public class EditFacet implements Serializable {

    private final Tree tree;
    private final TermService termService;
    private final NoteService noteService;
    private final SearchService searchService;
    private final RightBodySetting rightBodySetting;
    private final ConceptView conceptBean;
    private final IndexSetting indexSetting;
    private final ConceptView conceptView;
    private final SelectedTheso selectedTheso;
    private final CurrentUser currentUser;
    private final FacetService facetService;
    private final ConceptService conceptService;

    private List<NodeLangTheso> nodeLangs, nodeLangsFiltered;
    private List<NodeIdValue> conceptList;
    private List<NodeFacet> facetTraductions;

    private NodeConcept concepParent;
    private NodeFacet facetSelected;
    private String newFacetName, conceptParentTerme, selectedLang, traductionValue, definition1;
    private NodeIdValue conceptSelected, termeParentAssocie, facetSelectedAutocomplete;
    private NodeNote note, scopeNote, changeNote, definition, editorialNote, example, historyNote;



    public void reset() {
        if(nodeLangs != null){
            nodeLangs.clear();
        }
        if(nodeLangsFiltered != null){
            nodeLangsFiltered.clear();
        }
        if(conceptList != null){
            conceptList.clear();
        }
        if(facetTraductions != null){
            facetTraductions.clear();
        }
        if(concepParent != null)
            concepParent.clear();

        newFacetName = null;
        conceptParentTerme = null;
        selectedLang = null;
        traductionValue = null;
        termeParentAssocie = null;
        facetSelected = null;
        conceptSelected = null;
        facetSelectedAutocomplete = null;
        definition = null;
    }

    public void initNewFacet(){
        newFacetName = "";
        definition = null;
    }

    public void initEditFacet(String facetId, String idTheso, String idLang) {

        facetSelected = facetService.getFacet(facetId, idTheso, idLang);
        if(facetSelected == null || facetSelected.getIdFacet() == null) return;
        concepParent = conceptService.getConceptOldVersion(facetSelected.getIdConceptParent(), idTheso, idLang, 21, 0);

        conceptParentTerme = concepParent.getTerm().getLexicalValue();

        facetTraductions = facetService.getAllTraductionsFacet(facetId, idTheso, idLang);
        setAllNotes(noteService.getListNotes(facetId, idTheso, idLang));
        conceptList = facetService.getAllMembersOfFacetSorted(facetSelected.getIdFacet(), selectedTheso.getCurrentLang(),
                selectedTheso.getCurrentIdTheso());
        newFacetName = facetSelected.getLexicalValue();
    }

    public void finishAddingNewConcept() {

        if (facetSelected != null) {
            conceptList = facetService.getAllMembersOfFacetSorted(facetSelected.getIdFacet(), selectedTheso.getCurrentLang(),
                    selectedTheso.getCurrentIdTheso());

            PrimeFaces.current().ajax().update("containerIndex");
            PrimeFaces.current().executeScript("PF('addNTFacette').hide();");
        }
    }

    private void setAllNotes(List<NodeNote> nodeNotes) {
        clearNotes();
        for (NodeNote nodeNote : nodeNotes) {
            switch (nodeNote.getNoteTypeCode()) {
                case "note":
                    note = nodeNote;
                    break;
                case "scopeNote":
                    scopeNote = nodeNote;
                    break;
                case "changeNote":
                    changeNote = nodeNote;
                    break;
                case "definition":
                    definition = nodeNote;
                    break;
                case "editorialNote":
                    editorialNote = nodeNote;
                    break;
                case "example":
                    example = nodeNote;
                    break;
                case "historyNote":
                    historyNote = nodeNote;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + nodeNote.getNoteTypeCode());
            }
        }
    }

    private void clearNotes() {
        note = null;
        scopeNote = null;
        changeNote =  null;
        definition = null;
        editorialNote = null;
        example = null;
        historyNote = null;
    }

    public String getNoteSource(String noteSource) {
        if (StringUtils.isEmpty(noteSource))
            return "";
        else
            return " (" + noteSource + ")";
    }    

    public void supprimerFacette() {

        facetService.deleteFacet(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso());

        tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.expandTreeToPath(facetSelected.getIdConceptParent(), selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.setIdConcept(facetSelected.getIdConceptParent());

        indexSetting.setIsFacetSelected(false);
        indexSetting.setIsValueSelected(true);

        rightBodySetting.setShowConceptToOn();
        conceptBean.getConceptForTree(selectedTheso.getCurrentIdTheso(), facetSelected.getIdConceptParent(),
                selectedTheso.getSelectedLang(), currentUser);
        rightBodySetting.setIndex("0");
        MessageUtils.showInformationMessage("Facette supprimée avec succès !");
    }

    public void deleteTraduction(NodeFacet nodeFacet) {

        facetService.deleteTraductionFacet(nodeFacet.getIdFacet(), nodeFacet.getIdThesaurus(), nodeFacet.getLang());
        initDataAfterAction();
        MessageUtils.showInformationMessage("Traduction supprimée avec succès !");
    }

    public void updateTraduction(NodeFacet nodeFacet) {

        if (StringUtils.isEmpty(nodeFacet.getLexicalValue())) {
            MessageUtils.showErrorMessage("Vous devez saisir une traduction !");
            return;
        }

        facetService.updateFacetTraduction(nodeFacet.getIdFacet(), nodeFacet.getIdThesaurus(), nodeFacet.getLang(), nodeFacet.getLexicalValue());

        initEditFacet(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formRightTab:facetView");
        }

        initDataAfterAction();
        MessageUtils.showInformationMessage("Traduction modifiée avec succès !");

    }

    public void addNewTraduction() {

        if (StringUtils.isEmpty(traductionValue)) {
            MessageUtils.showErrorMessage("Vous devez saisir une traduction !");
            return;
        }

        facetService.addFacetTraduction(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), traductionValue, selectedLang);

        initDataAfterAction();
        setLangListForTraduction();
        MessageUtils.showInformationMessage("Traduction ajoutée avec succès !");
        traductionValue = "";
    }

    public void initDataAfterAction() {
        initEditFacet(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formRightTab:facetView");
        }
    }

    public void setLangListForTraduction() {

        nodeLangsFiltered = new ArrayList<>();

        var facetLists = facetService.getAllTraductionsFacet(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), facetSelected.getLang());

        nodeLangs = selectedTheso.getNodeLangs();
        nodeLangsFiltered.addAll(nodeLangs);

        // les langues à ignorer
        ArrayList<String> langsToRemove = new ArrayList<>();
        langsToRemove.add(facetSelected.getLang());
        for (NodeFacet nodeFacet : facetLists) {
            langsToRemove.add(nodeFacet.getLang());
        }

        for (NodeLangTheso nodeLang : nodeLangs) {
            if(langsToRemove.contains(nodeLang.getCode())) {
                nodeLangsFiltered.remove(nodeLang);
            }
        }
        if(nodeLangsFiltered.isEmpty()) {
            MessageUtils.showWarnMessage("La Facette est déjà traduite dans toutes les autres langues du thésaurus !!!");
            return;
        }

        PrimeFaces.current().executeScript("PF('addFacetTraduction').show();");
    }

    public void addMemberToFacet() {

        if(conceptSelected == null || conceptSelected.getId() == null || conceptSelected.getId().isEmpty()){
            MessageUtils.showErrorMessage("Aucun concept n'est sélectionné !!!");
            return;
        }

        facetService.addConceptToFacet(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), conceptSelected.getId());
        var label = termService.getLexicalValueOfConcept(conceptSelected.getId(), selectedTheso.getCurrentIdTheso(),
                selectedTheso.getSelectedLang());
        var data = new TreeNodeData(conceptSelected.getId(), label, "", false, false,
                true, false, "term");
        data.setIdFacetParent(facetSelected.getIdFacet());

        if(conceptService.haveChildren(selectedTheso.getCurrentIdTheso(), conceptSelected.getId())) {
            tree.getDataService().addNodeWithChild("concept", data, tree.getSelectedNode());
        } else {
            tree.getDataService().addNodeWithoutChild("file", data, tree.getSelectedNode());
        }

        initDataAfterAction();

        tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.expandTreeToPath2(facetSelected.getIdConceptParent(), selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang(), facetSelected.getIdFacet());

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formLeftTab:tabTree:tree");
        }

        MessageUtils.showInformationMessage("Facette mise à jour avec succès !");
    }

    /**
     * permet d'ajouter un concept à la facette en passant par l'autocomplétion
     */
    public void addConceptToFacet() {

        if(facetSelectedAutocomplete == null || facetSelectedAutocomplete.getId() == null 
                || facetSelectedAutocomplete.getId().isEmpty()){
            MessageUtils.showErrorMessage("Aucune facette sélectionnée !");
            return;
        }

        facetService.addConceptToFacet(facetSelectedAutocomplete.getId(), selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept());
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept()
                .getConcept().getIdConcept(), conceptBean.getSelectedLang(), currentUser);
        MessageUtils.showInformationMessage("Concept ajouté à la facette avec succès !");
    }

    
    /**
     * permet de supprimer l'appartenance du concept à la facette
     * @param idConceptToRemove
     * @param fromIdFacet 
     */
    public void removeConceptFromFacet(String idConceptToRemove, String fromIdFacet){

        if(StringUtils.isEmpty(idConceptToRemove)){
            MessageUtils.showErrorMessage("Aucun concept sélectionné !!!");
            return;
        }

        if(StringUtils.isEmpty(fromIdFacet)){
            MessageUtils.showErrorMessage("Aucune Facette sélectionnée !!!");
            return;
        }

        facetService.deleteConceptFromFacet(fromIdFacet, idConceptToRemove, selectedTheso.getCurrentIdTheso());
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), idConceptToRemove, conceptBean.getSelectedLang(), currentUser);
    }
    
    /**
     * permet de supprimer un membre de la facette
     * @param idConceptToRemove
     */
    public void removeMemberFromFacet(String idConceptToRemove) {

        if(idConceptToRemove == null || idConceptToRemove.isEmpty()){
            MessageUtils.showErrorMessage("Aucun concept sélectionné !!!");
            return;
        }

        facetService.deleteConceptFromFacet(facetSelected.getIdFacet(), idConceptToRemove, selectedTheso.getCurrentIdTheso());

        initDataAfterAction();

        tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.expandTreeToPath2(facetSelected.getIdConceptParent(), selectedTheso.getCurrentIdTheso(),
                selectedTheso.getSelectedLang(), facetSelected.getIdFacet());

        PrimeFaces.current().ajax().update("formLeftTab:tabTree:tree");
        MessageUtils.showInformationMessage("Facette mise à jour avec succès !");
    }


    public void modifierConceptParent() {

        if (termeParentAssocie == null) {
            MessageUtils.showErrorMessage("Vous devez choisir le concepts parent !");
            return;
        }

        facetService.updateFacetParent(termeParentAssocie.getId(), facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso());

        facetSelected.setIdConceptParent(termeParentAssocie.getId());

        MessageUtils.showInformationMessage("Concept parent modifié avec succès !");

        tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.expandTreeToPath2(facetSelected.getIdConceptParent(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getSelectedLang(),
                facetSelected.getIdFacet()+"");

        concepParent = conceptService.getConceptOldVersion(termeParentAssocie.getId(), selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang(), 21, 0);

        conceptParentTerme = concepParent.getTerm().getLexicalValue();

        PrimeFaces.current().ajax().update("containerIndex");
        PrimeFaces.current().executeScript("PF('addConceptParentToFacet').hide();");

    }

    public void addNewFacet() {
        if(!checkLabelFacet()){
            return;
        }

        var idFacet = facetService.addNewFacet(null, selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), newFacetName, selectedTheso.getCurrentLang());
        if(idFacet == null) {
            MessageUtils.showErrorMessage("Erreur pendant la création de la Facette !");
            return;
        }

        MessageUtils.showInformationMessage("Facette enregistrée avec succès !");
        
        tree.addNewFacet(tree.getSelectedNode(), newFacetName, idFacet+"");

        // ajout de la définition s'il elle est renseignée
        if(StringUtils.isNotEmpty(definition1)) {
            noteService.addNote(idFacet, selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso(),
                    definition1, "definition", "",  currentUser.getNodeUser().getIdUser());            
        }

        PrimeFaces.current().ajax().update("formLeftTab:tabTree:tree");
        PrimeFaces.current().ajax().update("formRightTab:facetView");

        newFacetName = "";
        definition1 = "";
    }

    public void updateLabelFacet() {

        if(!checkLabelFacet()) return;

        if(facetService.isTraductionExistOfFacet(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang())){
            if(!facetService.updateFacetTraduction(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang(), newFacetName)) {
                MessageUtils.showErrorMessage("Erreur interne BDD!");
                return;
            }
        } else {
            facetService.addFacetTraduction(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), newFacetName, selectedTheso.getCurrentLang());
        }

        MessageUtils.showInformationMessage("Facette modifiée avec succès !");
        ((TreeNodeData) tree.getSelectedNode().getData()).setName(newFacetName);

        facetSelected = facetService.getFacet(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());

        newFacetName = "";

        PrimeFaces.current().ajax().update("formRightTab:facetView");
        PrimeFaces.current().ajax().update("formLeftTab:tabTree:tree");
        PrimeFaces.current().executeScript("PF('renameFacet').hide();");
    }

    private boolean checkLabelFacet() {
        if (StringUtils.isEmpty(newFacetName)) {
            MessageUtils.showErrorMessage("Vous devez saisir un nom à la nouvelle facette !");
            return false;
        }

        if (facetService.checkExistenceFacetByNameAndLangAndThesaurus(newFacetName, selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso())) {
            MessageUtils.showErrorMessage("Le nom de la facette '" + newFacetName + "' existe déjà !");
            return false;
        }
        return true;
    }

    public List<NodeIdValue> searchConcept(String value) {

        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            return searchService.searchAutoCompletionForRelationIdValue(value,
                    selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
        }
        return List.of();
    }

    public List<NodeIdValue> searchFacet(String value) {

        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            return facetService.searchFacet(value, selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
        }
        return List.of();
    }
}
