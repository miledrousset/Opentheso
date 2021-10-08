package fr.cnrs.opentheso.bean.facet;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeFacet;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import org.primefaces.PrimeFaces;


@Named(value = "editFacet")
@SessionScoped
public class EditFacet implements Serializable {

    @Inject private Connect connect;
    @Inject private RightBodySetting rightBodySetting;
    @Inject private ConceptView conceptBean;
    @Inject private IndexSetting indexSetting;
    @Inject private ConceptView conceptView;
    @Inject private Tree tree;
    @Inject private SelectedTheso selectedTheso;

    private ArrayList<NodeLangTheso> nodeLangs, nodeLangsFiltered;
    private ArrayList<NodeIdValue> conceptList;
    private List<NodeFacet> facetTraductions; 
    
    private String newFacetName, conceptParentTerme, selectedLang, traductionValue;
    private NodeIdValue termeParentAssocie;
    private NodeConcept concepParent;
    private NodeFacet facetSelected;
    private NodeIdValue conceptSelected;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeLangs != null){
            nodeLangs.clear();
            nodeLangs = null;
        }
        if(nodeLangsFiltered != null){
            nodeLangsFiltered.clear();
            nodeLangsFiltered = null;
        }      
        if(conceptList != null){
            conceptList.clear();
            conceptList = null;
        }        
        if(facetTraductions != null){
            facetTraductions.clear();
            facetTraductions = null;
        }
        if(concepParent != null) {
            concepParent.clear();
            concepParent = null;
        }
        
        newFacetName = null;
        conceptParentTerme = null;
        selectedLang = null;
        traductionValue = null;
        termeParentAssocie = null;
        facetSelected = null;
        conceptSelected = null;
    }       

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
    }

    public void initEditFacet(String facetId, String idTheso, String idLang) {
        
        FacetHelper facetHelper = new FacetHelper();
        
        facetSelected = facetHelper.getThisFacet(connect.getPoolConnexion(),
                facetId, idTheso, idLang);
        if(facetSelected == null || facetSelected.getIdFacet() == null) return;
        concepParent = new ConceptHelper().getConcept(connect.getPoolConnexion(),
                facetSelected.getIdConceptParent(),
                idTheso,
                idLang);
        
        conceptParentTerme = concepParent.getTerm().getLexical_value();
        
        facetTraductions = facetHelper.getAllTraductionsFacet(connect.getPoolConnexion(), facetId, 
                idTheso, idLang);
        
        setListConceptsAssocie();

        newFacetName = facetSelected.getLexicalValue();
        
    }
    
    public void initNewFacet(){
        newFacetName = "";
    }
    
    public void supprimerFacette() {
        new FacetHelper().deleteFacet(connect.getPoolConnexion(), 
                facetSelected.getIdFacet(), 
                selectedTheso.getCurrentIdTheso());
        
        tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.expandTreeToPath(facetSelected.getIdConceptParent(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getSelectedLang());
        
        tree.setIdConcept(facetSelected.getIdConceptParent());
        indexSetting.setIsFacetSelected(false);
        indexSetting.setIsValueSelected(true);
        
        rightBodySetting.setShowConceptToOn();
        conceptBean.getConceptForTree(selectedTheso.getCurrentIdTheso(), 
                facetSelected.getIdConceptParent(), 
                selectedTheso.getSelectedLang());
        rightBodySetting.setIndex("0");

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formRightTab");
            pf.ajax().update("formLeftTab:tabTree:tree");
        }
    }
    
    private void setListConceptsAssocie() {
        
        FacetHelper facetHelper = new FacetHelper();
        ConceptHelper termHelper = new ConceptHelper();
        
        List<String> concepts = facetHelper.getAllMembersOfFacet(
                connect.getPoolConnexion(), 
                facetSelected.getIdFacet(), 
                selectedTheso.getCurrentIdTheso());    
        
        conceptList = new ArrayList<>();
        for (String idConcept : concepts) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idConcept);
            nodeIdValue.setValue(termHelper.getLexicalValueOfConcept(connect.getPoolConnexion(),
                    idConcept, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang()));
            conceptList.add(nodeIdValue);
        }
    }
    
    public void deleteTraduction(NodeFacet nodeFacet) {
        
        new FacetHelper().deleteTraductionFacet(connect.getPoolConnexion(), 
                nodeFacet.getIdFacet(), 
                nodeFacet.getIdThesaurus(), 
                nodeFacet.getLang());
        
        initDataAfterAction();
        showMessage(FacesMessage.SEVERITY_INFO, "Traduction suprimée avec sucée !");
        
    }
    
    public void updateTraduction(NodeFacet nodeFacet) {
        
        if (StringUtils.isEmpty(nodeFacet.getLexicalValue())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez saisir une traduction !");
            return;
        }
        
        new FacetHelper().updateFacetTraduction(connect.getPoolConnexion(), 
                nodeFacet.getIdFacet(),
                nodeFacet.getIdThesaurus(),
                nodeFacet.getLang(),
                nodeFacet.getLexicalValue());

        initEditFacet(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:formRightTab:facetView");
        }
        
        initDataAfterAction();
        showMessage(FacesMessage.SEVERITY_INFO, "Traduction modifiée avec sucée !");
        
    } 
    
    public void addNewTraduction() {
        
        if (StringUtils.isEmpty(traductionValue)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez saisir une traduction !");
            return;
        }

        new FacetHelper().addFacetTraduction(connect.getPoolConnexion(),
                facetSelected.getIdFacet(), 
                selectedTheso.getCurrentIdTheso(), 
                traductionValue,
                selectedLang);
        
        initDataAfterAction();
        PrimeFaces.current().executeScript("PF('addFacetTraduction').hide();");
        showMessage(FacesMessage.SEVERITY_INFO, "Traduction ajoutée avec sucée !");

        traductionValue = "";
    }

    private void initDataAfterAction() {
        initEditFacet(facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:formRightTab:facetView");
        }
    }
    
    public void setLangListForTraduction() {
        nodeLangsFiltered = new ArrayList<>();

        FacetHelper facetHelper = new FacetHelper();

        List<NodeFacet> facetLists = facetHelper.getAllTraductionsFacet(
                connect.getPoolConnexion(),
                facetSelected.getIdFacet(),
                selectedTheso.getCurrentIdTheso(),
                facetSelected.getLang());
        
        nodeLangs = selectedTheso.getNodeLangs();
        nodeLangs.forEach((nodeLang) -> {
            nodeLangsFiltered.add(nodeLang);
        });
       
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
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " La Facette est déjà traduite dans toutes les autres langues du thésaurus !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        PrimeFaces.current().executeScript("PF('addFacetTraduction').show();");
    }
    
    /**
     * permet d'ajouter un concept existant à la facette
     */
    public void addMemberToFacet() {
        FacetHelper facetHelper = new FacetHelper();
        ConceptHelper conceptHelper = new ConceptHelper();

        FacesMessage msg;
        
        if(conceptSelected == null || conceptSelected.getId() == null || conceptSelected.getId().isEmpty()){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " pas de concept sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }        
        
        if(!facetHelper.addConceptToFacet(connect.getPoolConnexion(),
                facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), conceptSelected.getId())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " L'ajout a échoué !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        String label = conceptHelper.getLexicalValueOfConcept(connect.getPoolConnexion(),
                conceptSelected.getId(), selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        TreeNodeData data = new TreeNodeData(conceptSelected.getId(), label, "", false,
                false, true, false, "term");
        data.setIdFacetParent(facetSelected.getIdFacet());
        if(conceptHelper.haveChildren(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), conceptSelected.getId())) {
            tree.getDataService().addNodeWithChild("concept", data, tree.getSelectedNode());
        } else {
            tree.getDataService().addNodeWithoutChild("file", data, tree.getSelectedNode());
        }

        initDataAfterAction();

        tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.expandTreeToPath2(facetSelected.getIdConceptParent(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getSelectedLang(),
                facetSelected.getIdFacet());

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formLeftTab:tabTree:tree");
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Facet mise à jour avec sucée !");
    }
    
    /**
     * permet de supprimer un membre de la facette
     * @param idConceptToRemove
     */
    public void removeMemberFromFacet(String idConceptToRemove) {
        FacetHelper facetHelper = new FacetHelper();
  //      ConceptHelper conceptHelper = new ConceptHelper();

        FacesMessage msg;
        
        if(idConceptToRemove == null || idConceptToRemove.isEmpty()){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " pas de concept sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        if(!facetHelper.deleteConceptFromFacet(connect.getPoolConnexion(),
                facetSelected.getIdFacet(), 
                idConceptToRemove,
                selectedTheso.getCurrentIdTheso())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " La suppression a échoué !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        /// suppression du concept de la facette
     /*   TreeNode parent = tree.getSelectedNode();
        if (parent != null) {
            List <TreeNode> childrens = parent.getChildren();
            for (TreeNode children : childrens) {
                if(((TreeNodeData)children).getNodeId().equalsIgnoreCase(idConceptToRemove)) {
                     childrens.remove(children);
                     break;
                }
            }
        }*/

        initDataAfterAction();

        tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.expandTreeToPath2(facetSelected.getIdConceptParent(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getSelectedLang(),
                facetSelected.getIdFacet());

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formLeftTab:tabTree:tree");
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Facet mise à jour avec sucée !");
    }    
    
    
    public void modifierConceptParent() {
        
        if (termeParentAssocie == null) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez choisir le concepts parent !");
            return;
        }

        new FacetHelper().updateFacetParent(connect.getPoolConnexion(),
                termeParentAssocie.getId(),
                facetSelected.getIdFacet(),
                selectedTheso.getCurrentIdTheso());

        facetSelected.setIdConceptParent(termeParentAssocie.getId());
        
        showMessage(FacesMessage.SEVERITY_INFO, "Concept parent modifié avec sucée !");

        tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.expandTreeToPath2(facetSelected.getIdConceptParent(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getSelectedLang(),
                facetSelected.getIdFacet()+"");

        concepParent = new ConceptHelper().getConcept(connect.getPoolConnexion(),
                termeParentAssocie.getId(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang());

        conceptParentTerme = concepParent.getTerm().getLexical_value();

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:formRightTab:facetView");
            pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");
        }
        PrimeFaces.current().executeScript("PF('addConceptParentToFacet').hide();");
        
    }

    public void addNewFacet() {
        FacetHelper facetHelper = new FacetHelper();
        if(!checkLabelFacet(facetHelper)){
            return;            
        }

        String idFacet = facetHelper.addNewFacet(connect.getPoolConnexion(),
                null,
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                newFacetName, selectedTheso.getCurrentLang(),
                null);
        
        if(idFacet == null) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la création de la Facette !");
            return;
        }
        
        showMessage(FacesMessage.SEVERITY_INFO, "Facette enregistrée avec sucée !");

        tree.addNewFacet(tree.getSelectedNode(), newFacetName, idFacet+"");

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formLeftTab");
            pf.ajax().update("containerIndex:formRightTab");
        }
        PrimeFaces.current().executeScript("PF('addFacet').hide();");
        newFacetName = "";
    }

    public void updateLabelFacet() {

        FacetHelper facetHelper = new FacetHelper();
        if(!checkLabelFacet(facetHelper)) return;

        if(facetHelper.isTraductionExistOfFacet(connect.getPoolConnexion(),
                facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang())){
            if(!facetHelper.updateLabelFacet(connect.getPoolConnexion(), newFacetName,
                                facetSelected.getIdFacet(),
                                selectedTheso.getCurrentIdTheso(),
                                selectedTheso.getCurrentLang())) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Erreur interne BDD!");
                return;
            }
        } else 
            if(!facetHelper.addFacetTraduction(connect.getPoolConnexion(), 
                facetSelected.getIdFacet(),
                selectedTheso.getCurrentIdTheso(),
                newFacetName,
                selectedTheso.getCurrentLang())){
                showMessage(FacesMessage.SEVERITY_ERROR, "Erreur interne BDD!");
                return;                
            }

        showMessage(FacesMessage.SEVERITY_INFO, "Facette modifiée avec sucée !");

        ((TreeNodeData) tree.getSelectedNode().getData()).setName(newFacetName);
        
        facetSelected = facetHelper.getThisFacet(connect.getPoolConnexion(),
                facetSelected.getIdFacet(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang());

        newFacetName = "";

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("containerIndex:formRightTab:facetView");
        pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");

    }

    private boolean checkLabelFacet(FacetHelper facetHelper) {
        if (StringUtils.isEmpty(newFacetName)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez saisir un nom à la nouvelle facette !");
            return false;
        }

        if (facetHelper.checkExistanceFacetByNameAndLangAndThesau(connect.getPoolConnexion(),
                newFacetName, selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Le nom de la facette '" + newFacetName + "' existe déjà !");
            return false;
        }
        return true;
    }

    public ArrayList<NodeIdValue> searchConcept(String value) {
        ArrayList<NodeIdValue> liste = new ArrayList<>();
        SearchHelper searchHelper = new SearchHelper();
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            liste = searchHelper.searchAutoCompletionForRelationIdValue(connect.getPoolConnexion(), value,
                    selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
        }
        return liste;
    }

    private void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }

    public NodeFacet getFacetSelected() {
        return facetSelected;
    }

    public void setFacetSelected(NodeFacet facetSelected) {
        this.facetSelected = facetSelected;
    }

    public String getNewFacetName() {
        return newFacetName;
    }

    public void setNewFacetName(String newFacetName) {
        this.newFacetName = newFacetName;
    }

    public NodeConcept getConcepParent() {
        return concepParent;
    }

    public void setConcepParent(NodeConcept concepParent) {
        this.concepParent = concepParent;
    }

    public String getConceptParentTerme() {
        return conceptParentTerme;
    }

    public void setConceptParentTerme(String conceptParentTerme) {
        this.conceptParentTerme = conceptParentTerme;
    }

    public NodeIdValue getTermeParentAssocie() {
        return termeParentAssocie;
    }

    public void setTermeParentAssocie(NodeIdValue termeParentAssocie) {
        this.termeParentAssocie = termeParentAssocie;
    }

    public ArrayList<NodeLangTheso> getNodeLangsFiltered() {
        return nodeLangsFiltered;
    }

    public void setNodeLangsFiltered(ArrayList<NodeLangTheso> nodeLangsFiltered) {
        this.nodeLangsFiltered = nodeLangsFiltered;
    }
    
    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public String getTraductionValue() {
        return traductionValue;
    }

    public void setTraductionValue(String traductionValue) {
        this.traductionValue = traductionValue;
    }

    public List<NodeFacet> getFacetTraductions() {
        return facetTraductions;
    }

    public void setFacetTraductions(List<NodeFacet> facetTraductions) {
        this.facetTraductions = facetTraductions;
    }

    public ArrayList<NodeIdValue> getConceptList() {
        return conceptList;
    }

    public void setConceptList(ArrayList<NodeIdValue> conceptList) {
        this.conceptList = conceptList;
    }

    public NodeIdValue getConceptSelected() {
        return conceptSelected;
    }

    public void setConceptSelected(NodeIdValue conceptSelected) {
        this.conceptSelected = conceptSelected;
    }
    
    
    
}
