package fr.cnrs.opentheso.bean.facet;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
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

    @Inject
    private Connect connect;
    
    @Inject
    private RightBodySetting rightBodySetting;
    
    @Inject
    private ConceptView conceptBean;
    
    @Inject
    private IndexSetting indexSetting;

    @Inject
    private ConceptView conceptView;

    @Inject
    private Tree tree;

    @Inject
    private SelectedTheso selectedTheso;

    private ArrayList<NodeLangTheso> nodeLangs, nodeLangsFiltered;
    private ArrayList<NodeIdValue> conceptList;
    private NodeIdValue termeParentAssocie;
    private List<NodeFacet> facetTraductions; 
    private String newFacetName, conceptParentTerme, selectedLang, traductionValue;
    private NodeConcept concepParent;
    private NodeFacet facetSelected;
    

    public void initEditFacet(int facetId, String idTheso, String idLang) {
        
        FacetHelper facetHelper = new FacetHelper();
        
        facetSelected = facetHelper.getThisFacet(connect.getPoolConnexion(),
                facetId, idTheso, idLang);
        
        concepParent = new ConceptHelper().getConcept(connect.getPoolConnexion(),
                facetSelected.getIdConceptParent(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang());
        
        conceptParentTerme = concepParent.getTerm().getLexical_value();
        
        facetTraductions = facetHelper.getAllTraductionsFacet(connect.getPoolConnexion(), facetId, 
                idTheso, idLang);
        
        setListConceptsAssocie();

        newFacetName = facetSelected.getLexicalValue();
        
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
        
        List<String> concepts = facetHelper.getConceptAssocietedToFacette(connect.getPoolConnexion(), 
                facetSelected.getLexicalValue(), 
                selectedTheso.getCurrentIdTheso(), 
                selectedTheso.getCurrentLang(), 
                facetSelected.getIdConceptParent());
        
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
            pf.ajax().update("formRightTab:facetView");
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
            pf.ajax().update("formRightTab:facetView");
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
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " La Facette est déjà traduit dans toutes les langues du thésaurus !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        PrimeFaces.current().executeScript("PF('addFacetTraduction').show();");
    }
    
    public void modifierConceptAssocie() {

        TermHelper termHelper = new TermHelper();
        FacetHelper facetHelper = new FacetHelper();
        
        facetHelper.deleteAllConceptAssocietedToFacet(connect.getPoolConnexion(), 
                facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso());
        
        tree.getSelectedNode().clearParent();
        
        for (NodeIdValue concept : conceptList) {
            
            facetHelper.addConceptToFacet(connect.getPoolConnexion(), facetSelected.getIdFacet(), selectedTheso.getCurrentIdTheso(),
                    concept.getId());

            Term term = termHelper.getThisTerm(connect.getPoolConnexion(), concept.getId(), selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang());
           
            TreeNodeData data = new TreeNodeData(concept.getId(), term.getLexical_value(), "", false,
                    false, true, false, "term");

            tree.getDataService().addNodeWithoutChild("file", data, tree.getSelectedNode());
        }

        initDataAfterAction();

        tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
        tree.expandTreeToPath2(facetSelected.getIdConceptParent(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getSelectedLang(),
                facetSelected.getIdFacet()+"");

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
            pf.ajax().update("formRightTab:facetView");
            pf.ajax().update("formLeftTab:tabTree:tree");
        }
        PrimeFaces.current().executeScript("PF('addConceptParentToFacet').hide();");
        
    }

    public void addNewFacet() {

        FacetHelper facetHelper = new FacetHelper();
        if(!checkLabelFacet(facetHelper)){
            return;            
        }

        int idFacet = facetHelper.addNewFacet(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), newFacetName, selectedTheso.getCurrentLang(),
                null);
        if(idFacet == -1) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la création de la Facette !");
            return;
        }
        showMessage(FacesMessage.SEVERITY_INFO, "Facette enregistrée avec sucée !");

        tree.addNewFacet(tree.getSelectedNode(), newFacetName, idFacet+"");

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formLeftTab:tabTree:tree");
            pf.ajax().update("formRightTab:facetView");
        }
        PrimeFaces.current().executeScript("PF('addFacet').hide();");
        newFacetName = "";
    }

    public void updateLabelFacet() {

        FacetHelper facetHelper = new FacetHelper();
        checkLabelFacet(facetHelper);

        facetHelper.updateLabelFacet(connect.getPoolConnexion(), newFacetName,
                facetSelected.getIdFacet(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang());

        showMessage(FacesMessage.SEVERITY_INFO, "Facette modifiée avec sucée !");

        ((TreeNodeData) tree.getSelectedNode().getData()).setName(newFacetName);
        
        facetSelected = facetHelper.getThisFacet(connect.getPoolConnexion(),
                facetSelected.getIdFacet(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang());

        newFacetName = "";

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("formRightTab:facetView");
        pf.ajax().update("formLeftTab:tabTree:tree");
        PrimeFaces.current().executeScript("PF('renameFacet').hide();");

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
    
}
