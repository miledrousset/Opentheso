package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.entites.PreferredTerm;
import fr.cnrs.opentheso.entites.Term;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.concept.NodeConceptType;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesaurusBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.NonPreferredTermRepository;
import fr.cnrs.opentheso.repositories.PreferredTermRepository;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.TermRepository;
import fr.cnrs.opentheso.services.ArkService;
import fr.cnrs.opentheso.services.ConceptAddService;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.DeprecateService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.HandleConceptService;
import fr.cnrs.opentheso.services.TermService;
import fr.cnrs.opentheso.services.exports.csv.CsvWriteHelper;
import fr.cnrs.opentheso.utils.MessageUtils;
import fr.cnrs.opentheso.services.HandleService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;


@SessionScoped
@Named(value = "editConcept")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class EditConcept implements Serializable {

    
    @Autowired @Lazy private RoleOnThesaurusBean roleOnThesoBean;
    @Autowired @Lazy private LanguageBean languageBean;
    @Autowired @Lazy private ConceptView conceptView;
    @Autowired @Lazy private SelectedTheso selectedTheso;
    @Autowired @Lazy private Tree tree;
    @Autowired @Lazy private ConceptView conceptBean;
    @Autowired @Lazy private CurrentUser currentUser;
    @Autowired @Lazy private HandleService handleService;

    @Autowired
    private DeprecateService deprecateHelper;

    @Autowired
    private PreferredTermRepository preferredTermRepository;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private ConceptDcTermRepository conceptDcTermRepository;

    @Autowired
    private CsvWriteHelper csvWriteHelper;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private HandleService handleHelper;

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private TermService termService;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private NonPreferredTermRepository nonPreferredTermRepository;

    @Autowired
    private GroupService groupService;

    private String prefLabel;
    private String notation;
    private String source;
    private boolean applyToBranch;
    private boolean reciprocalRelationship;
    
    private boolean isCreated;
    private boolean duplicate;
    private boolean forDelete;

    private boolean inProgress;
    
    private boolean isReplacedByRTrelation;
    
    private List<NodeConceptType> nodeConceptTypes;
    private String selectedConceptType;

    // dépréciation
    private List<NodeIdValue> nodeReplaceBy;
    
    private NodeSearchMini searchSelected;    
    
    private List<NodeIdValue> nodeIdValues;
    
    private NodeConceptType nodeConceptTypeToDelete;
    private NodeConceptType nodeConceptTypeToAdd;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private ConceptAddService conceptAddService;
    @Autowired
    private HandleConceptService handleConceptService;
    @Autowired
    private ArkService arkService;


    public void clear() {
        if (nodeReplaceBy != null) {
            nodeReplaceBy.clear();
            nodeReplaceBy = null;
        }
        inProgress = false;
        prefLabel = null;
        notation = null;
        source = null;
        selectedConceptType = null;
        nodeIdValues = null;
        nodeConceptTypeToDelete = null;
        nodeConceptTypeToAdd = null;
    }

    public EditConcept() {
    }

    public void reset(String label) {
        isCreated = false;
        duplicate = false;
        prefLabel = label;
        notation = "";
        forDelete = false;
        isReplacedByRTrelation = false;
        inProgress = false;
        nodeIdValues = null;
        nodeConceptTypeToDelete = null;
        nodeConceptTypeToAdd = null;

        nodeReplaceBy = conceptView.getNodeConcept().getReplacedBy();
    }
    public void initForConceptType(){

        nodeConceptTypes = conceptHelper.getAllTypesOfConcept(selectedTheso.getCurrentIdTheso());
        selectedConceptType = conceptView.getNodeConcept().getConcept().getConceptType();
        applyToBranch = false;
        reciprocalRelationship = false;
        nodeConceptTypeToDelete = null;
        nodeConceptTypeToAdd = null;

      //  PrimeFaces.current().executeScript("window.location.reload();");
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour modifier Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void removeAllConceptFromCollection(String idGroup){
        groupService.removeAllConceptsFromThisGroup(idGroup, selectedTheso.getCurrentIdTheso());

        selectedTheso.reloadGroups();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Tous les concepts ont été retirés de la collection.");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de changer les informations dans la table du type de concept
     * @param nodeConceptType
     * @param idUser 
     */
    public void applyChangeForConceptType(NodeConceptType nodeConceptType, int idUser){
        if(nodeConceptType == null || StringUtils.isEmpty(nodeConceptType.getCode())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }

        if(!conceptHelper.applyChangeForConceptType(selectedTheso.getCurrentIdTheso(), nodeConceptType)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant le changement !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        initForConceptType();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Changement réussi !");
        FacesContext.getCurrentInstance().addMessage(null, msg);   
    }
    
    /**
     * permet de supprimer un type de concept
     */
    public void deleteCustomRelationship(){
        if(nodeConceptTypeToDelete == null || StringUtils.isEmpty(nodeConceptTypeToDelete.getCode())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;            
        }

        if(!conceptHelper.deleteConceptType(selectedTheso.getCurrentIdTheso(), nodeConceptTypeToDelete)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        initForConceptType();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Suppression réussie !");
        FacesContext.getCurrentInstance().addMessage(null, msg);         
    }
    
    /**
     * permet d'ajouter un nouveau type de concept
     */
    public void addNewConceptType(){
        if(nodeConceptTypeToAdd == null || StringUtils.isEmpty(nodeConceptTypeToAdd.getCode())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;            
        }    
        if(StringUtils.isEmpty( selectedTheso.getCurrentIdTheso())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;              
        }
        
        nodeConceptTypeToAdd.setCode(fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(nodeConceptTypeToAdd.getCode()));
        nodeConceptTypeToAdd.setCode(nodeConceptTypeToAdd.getCode().replaceAll(" ", ""));
        
        if(conceptHelper.isConceptTypeExist(selectedTheso.getCurrentIdTheso(), nodeConceptTypeToAdd)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le type de concept existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        
        if(!conceptHelper.addNewConceptType(selectedTheso.getCurrentIdTheso(), nodeConceptTypeToAdd)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant l'ajout !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        initForConceptType();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Ajout réussi !");
        FacesContext.getCurrentInstance().addMessage(null, msg);         
    }    
    
    /**
     * permet de chnager le type du concept
     * @param idUser 
     */
    public void updateTypeConcept(int idUser) {
        PrimeFaces pf = PrimeFaces.current();

        if (selectedConceptType == null || selectedConceptType.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "aucune relation n'est définie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if(isApplyToBranch()) {
            var allId  = conceptService.getIdsOfBranch(conceptView.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso());

            if( (allId == null) || (allId.isEmpty())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "aucun concept sélectionné !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }                
                return;
            }
            for (String conceptId : allId) {
                if(!conceptHelper.setConceptType(
                        selectedTheso.getCurrentIdTheso(),
                        conceptId,
                        selectedConceptType,
                        idUser)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Une erreur s'est produite !!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    if (pf.isAjaxRequest()) {
                        pf.ajax().update("messageIndex");
                    }
                    return;            
                }             
            }
        } else {
            if(!conceptHelper.setConceptType(
                    selectedTheso.getCurrentIdTheso(),
                    conceptView.getNodeConcept().getConcept().getIdConcept(),
                    selectedConceptType,
                    idUser)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Une erreur s'est produite !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;            
            }            
        }

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le changement a réussi");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }       
    }
    
    
    /**
     * permet d'ajouter un nouveau top concept si le groupe = null, on ajoute un
     * TopConcept sans groupe si l'id du concept est fourni, il faut controler
     * s'il est unique
     *
     * @param idLang
     * @param idTheso
     * @param idUser
     */
    public void updateLabel(
            String idTheso,
            String idLang,
            int idUser) {

        duplicate = false;
        PrimeFaces pf = PrimeFaces.current();

        if (prefLabel == null || prefLabel.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "Le label est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
                PrimeFaces.current().ajax().update("containerIndex:rightTab:idRenameConcept");                
            }
            return;
        }

        // vérification si le term à ajouter existe déjà, s oui, on a l'Id, sinon, on a Null
        var value = fr.cnrs.opentheso.utils.StringUtils.convertString(prefLabel.trim());
        var termFound = termRepository.findByLexicalValueAndLangAndIdThesaurus(value, idLang, idTheso);
        String idTerm = termFound.map(Term::getIdThesaurus).orElse(null);

        if (idTerm != null) {
            var term = termRepository.findByIdTermAndIdThesaurusAndLang(idTerm, idTheso, idLang);
            var label = term.isPresent() ? term.get().getLexicalValue() : "";
            MessageUtils.showWarnMessage("Le label '" + label + "' existe déjà ! voulez-vous continuer ?");
            duplicate = true;
            PrimeFaces.current().ajax().update("containerIndex:renameConceptMessage");
            return;
        }
        if (termService.isAltLabelExist(idTerm, idTheso, idLang)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " un synonyme existe déjà ! voulez-vous continuer ?");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            duplicate = true;
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
                PrimeFaces.current().ajax().update("containerIndex:renameConceptMessage");
                PrimeFaces.current().executeScript("PF('renameConcept').show();");
            }
            return;
        }

        updateForced(idTheso, idLang, idUser);
    }

    public void updateForced(String idTheso, String idLang, int idUser) {

        var preferredTerm = preferredTermRepository.findByIdThesaurusAndIdConcept(idTheso, conceptView.getNodeConcept().getConcept().getIdConcept());
        String idTerm = preferredTerm.map(PreferredTerm::getIdTerm).orElse(null);
        if (idTerm == null) {
            MessageUtils.showErrorMessage("Erreur de cohérence de BDD !!");
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        // on vérifie si la tradcution existe, on la met à jour, sinon, on en ajoute une
        if (termService.isTermExistInLangAndThesaurus(idTerm, idTheso, idLang)) {
            termService.updateTermTraduction(prefLabel, idTerm, idLang, idTheso, idUser);
        } else {
            var term = fr.cnrs.opentheso.models.terms.Term.builder()
                    .lexicalValue(prefLabel)
                    .idTerm(idTerm)
                    .lang(idLang)
                    .idThesaurus(selectedTheso.getCurrentIdTheso())
                    .source("")
                    .status("")
                    .build();
            termService.addTermTraduction(term, idUser);
        }

        conceptService.updateDateOfConcept(idTheso, conceptView.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptView.getConcept(idTheso, conceptView.getNodeConcept().getConcept().getIdConcept(), idLang, currentUser);

        MessageUtils.showInformationMessage("Le concept a bien été modifié");

        PrimeFaces.current().ajax().update("containerIndex::idRenameConcept");
        PrimeFaces.current().executeScript("PF('renameConcept').hide();");

        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptView.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptView.getNodeConcept().getConcept().getIdConcept(), idTheso, idLang);
            }
            ((TreeNodeData) tree.getSelectedNode().getData()).setName(prefLabel);
            PrimeFaces.current().ajax().update("containerIndex:formLeftTab:tabTree:tree");
        }
        reset("");
    }

    public void cancel() {
        duplicate = false;
    }

    public void infosDelete() {
        String message;
        if (conceptView.getNodeConcept().getNodeNT().isEmpty()) { // pas d'enfants
            message = languageBean.getMsg("rightbody.conceptdialog.infoDeleteConcept");// rightbody.conceptdialog.infoDeleteConcept "La suppression du concept est définitive !!";
        } else {
            message = languageBean.getMsg("rightbody.conceptdialog.infoDeleteBranch"); //"La suppression de la branche est définitive !!";
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention !", message);
        FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:deleteConceptForm:currentPrefLabelToDelete", msg);
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }  
        forDelete = true;
    }

    /**
     * permet de supprimer un concept
     *
     * @param idTheso
     * @param idUser
     */
    public void deleteConcept(String idTheso, int idUser) {

        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        if (conceptView.getNodeConcept().getNodeNT().isEmpty()) {
            // suppression du concept
            if (!conceptService.deleteConcept(conceptView.getNodeConcept().getConcept().getIdConcept(), idTheso)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La suppression a échoué !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        } else {
            /// suppression d'une branche
            if(!conceptService.deleteBranchConcept(conceptView.getNodeConcept().getConcept().getIdConcept(), idTheso)){
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La suppression a échoué, vérifier la poly-hiérarchie pour le concept : " 
                        + conceptHelper.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                
            }
        }

        // mise à jour
        PrimeFaces pf = PrimeFaces.current();
        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptView.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptView.getNodeConcept().getConcept().getIdConcept(),
                        idTheso,
                        selectedTheso.getCurrentLang());
            }
            TreeNode parent = tree.getSelectedNode().getParent();
            if (parent != null) {
                parent.getChildren().remove(tree.getSelectedNode());

                if (pf.isAjaxRequest()) {
                    pf.ajax().update("formLeftTab:tabTree:tree");
                }
            }
        }

        if (CollectionUtils.isNotEmpty(conceptView.getNodeConcept().getNodeBT())){// !conceptView.getNodeConcept().getNodeBT().isEmpty()) {
            conceptView.getConcept(idTheso, conceptView.getNodeConcept().getNodeBT().get(0).getIdConcept(),
                    selectedTheso.getCurrentLang(), currentUser);
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a bien été supprimé");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
        PrimeFaces.current().executeScript("PF('deleteConcept').hide();");
        reset("");
    }

///////////////////////////////////////////////////////////////////////////////
//////// gestion des concepts dépérciés    
///////////////////////////////////////////////////////////////////////////////    
    public void deprecateConcept(String idConcept, String idTheso, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (!deprecateHelper.deprecateConcept(idConcept, idTheso, idUser, conceptHelper)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le concept n'a pas été déprécié !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), idConcept, idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(idConcept)
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptView.getConceptForTree(idTheso, idConcept, conceptView.getSelectedLang(), currentUser);
        
        
        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(), idTheso, conceptBean.getSelectedLang());
            }
            ((DefaultTreeNode) tree.getSelectedNode()).setType("deprecated");
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");
            }
        }         
        
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "le concept est maintenant obsolète");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formLeftTab");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }
    
    public void approveConcept(String idConcept, String idTheso, int idUser){
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        if (!deprecateHelper.approveConcept(idConcept, idTheso, idUser, conceptHelper)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le concept n'a pas été approuvé !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (isReplacedByRTrelation) {
            if (conceptView.getNodeConcept().getReplacedBy() != null && !conceptView.getNodeConcept().getReplacedBy().isEmpty()) {
                for (NodeIdValue nodeIdValue : nodeReplaceBy) {
                    relationsHelper.addRelationRT(idConcept, idTheso, nodeIdValue.getId(), idUser);
                }
            }
        }

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), idConcept, idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(idConcept)
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptView.getConceptForTree(idTheso, idConcept, conceptView.getSelectedLang(), currentUser);
        
        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(), idTheso, conceptBean.getSelectedLang());
            }
            if((tree.getSelectedNode()).getChildCount() == 0)
                (tree.getSelectedNode()).setType("file");
            else 
                (tree.getSelectedNode()).setType("concept");
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");
            }
        }          
        
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "le concept n'est plus obsolète maintenant");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void addReplacedBy(String idConceptDeprecated, String idTheso, int idUser) {
        FacesMessage msg;
        
        if ( searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pas de concept sélectionné !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (!deprecateHelper.addReplacedBy(
                idConceptDeprecated, idTheso, searchSelected.getIdConcept(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le concept n'a pas été ajouté !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), idConceptDeprecated, idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(idConceptDeprecated)
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptView.getConceptForTree(idTheso, idConceptDeprecated, conceptView.getSelectedLang(), currentUser);

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("containerIndex:rightTab:idReplaceBy containerIndex:rightTab:idDeprecatedLabel");
        }
    }
    
    public List<NodeSearchMini> getAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null){
            liste = searchHelper.searchAutoCompletionForRelation(value, conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(), false);
        }
        return liste;
    }      

    public void deleteReplacedBy(String idConceptDeprecated, String idTheso, String idConceptReplaceBy, int idUser){
        FacesMessage msg;        
        if(idConceptReplaceBy == null || idConceptReplaceBy.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pas de concept sélectionné !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        if (!deprecateHelper.deleteReplacedBy(
                idConceptDeprecated, idTheso, idConceptReplaceBy)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "la relation n'a pas été enlevée !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), idConceptDeprecated, idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(idConceptDeprecated)
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        conceptView.getConceptForTree(idTheso, idConceptDeprecated, conceptView.getSelectedLang(), currentUser);

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("containerIndex:idDeleteReplaceBy");
        }
        reset("");

    }

    ///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
//////////// générer les identifiants Ark    //////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
    public void infosArk() {
        String message = "Permet de générer un identifiant Ark, si l'identifiant existe, il sera mise à jour !!";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de générer l'identifiant Ark, s'il n'existe pas, il sera créé,
     * sinon, il sera mis à jour.
     */
    public void generateArk() {
        ArrayList<String> idConcepts = new ArrayList<>();
        idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateArkIds(idConcepts);
    }

    /**
     * permet de générer les identifiants Ark pour les concepts qui n'en ont pas
     * si un identifiant n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateArkForConceptWithoutArk() {
        ArrayList<String> idConcepts;
        idConcepts = conceptHelper.getAllIdConceptOfThesaurusWithoutArk(selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateArkIds(idConcepts);
    }
    
    /**
     * permet de générer les identifiants Ark pour cette branche, si un
     * identifiant n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateArkForThisBranch() {
        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        if (conceptView.getNodeConcept() == null) {
            return;
        }
        nodeIdValues = new ArrayList<>();
        var idConcepts = conceptService.getIdsOfBranch(conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        nodeIdValues = generateArkIds(idConcepts);
    }

    /**
     * permet de mettre à jour les URI des identifiants Ark pour cette branche,
     * cette fonction ne fait que la mise à jour de l'URL et ne permet de créer des identifiants ARK
     */
    public void updateUriArkForThisBranch(){
        if(roleOnThesoBean.getNodePreference() == null) return;
        if(conceptView.getNodeConcept() == null) return;

        var idConcepts = conceptService.getIdsOfBranch(conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        updateUriArkIds(conceptHelper, idConcepts);
    }

    /**
     * permet de générer la totalité des identifiants Ark, si un identifiant
     * n'existe pas, il sera créé, sinon, il sera mis à jour.
    */
    public void generateAllArk() {

        var idConcepts = conceptService.getAllIdConceptOfThesaurus(selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        nodeIdValues = new ArrayList<>();
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        nodeIdValues = generateArkIds(idConcepts);
    }
    
    /**
     * permet de générer la totalité des identifiants Ark, si un identifiant
     * n'existe pas, il sera créé, sinon, il sera mis à jour.
    */
    public void generateAllArkFast() {

        var idConcepts = conceptService.getAllIdConceptOfThesaurus(selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        nodeIdValues = new ArrayList<>();
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        nodeIdValues = arkService.generateArkIdFast(selectedTheso.getCurrentIdTheso(), idConcepts, selectedTheso.getCurrentLang());

        FacesMessage msg;
        if(nodeIdValues == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'opération est terminée avec succès !!");
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'opération est terminée, vérifier le fichier de résultat téléchargé !!");
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }        
    }    

    private List<NodeIdValue> generateArkIds(List<String> idConcepts) {
        FacesMessage msg;
        nodeIdValues = conceptAddService.generateArkId(selectedTheso.getCurrentIdTheso(), idConcepts, selectedTheso.getCurrentLang(), null);
        if(nodeIdValues == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'opération est terminée avec succès !!");
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'opération est terminée, vérifier le fichier de résultat téléchargé !!");
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
        return nodeIdValues;
    }


    private void updateUriArkIds(ConceptHelper conceptHelper, List<String> idConcepts){
        FacesMessage msg;
        if(!arkService.updateUriArk(selectedTheso.getCurrentIdTheso(), idConcepts)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La mise à jour des URIs Ark a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La mise à jour des URIs Ark a réussi !!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }


    /**
     * récupération du résultat du traitement
     * @return 
     */
    public StreamedContent getResultOfProcess(){
        if(nodeIdValues == null) return null; 
        /// pour retourner le résultat du traitement
        byte[] datas = csvWriteHelper.writeCsvResultProcess(nodeIdValues, "idConcept", "Résultat");

        try ( ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
            return DefaultStreamedContent.builder()
                    .contentType("text/csv")
                    .name("resultat.csv")
                    .stream(() -> input)
                    .build();
        } catch (IOException ex) {
        }
        return new DefaultStreamedContent();            
    }    


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
//////////// générer les identifiants Handle //////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
    public void infosHandle() {
        String message = "Permet de générer un identifiant Handle, si l'identifiant existe, il sera mise à jour !!";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void infosDeleteHandle() {
        String message = "Permet de supprimer un identifiant Handle, il sera définitivement supprimé !!";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de générer l'identifiant Handle, s'il n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void deleteHandle() {
        FacesMessage msg;
        if (roleOnThesoBean.getNodePreference() == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Pas de préférences pour le thésaurus !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (conceptView.getNodeConcept().getConcept().getIdHandle() == null || conceptView.getNodeConcept().getConcept().getIdHandle().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Pas d'identifiant Handle à supprimer !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        if(roleOnThesoBean.getNodePreference().isUseHandleWithCertificat()) {
            if (!handleHelper.deleteIdHandle(conceptView.getNodeConcept().getConcept().getIdHandle(), roleOnThesoBean.getNodePreference())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", handleHelper.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La suppression de Handle a échoué !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }

            handleConceptService.updateHandleIdOfConcept(conceptView.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), "");
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La suppression de Handle a réussi !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
        } else {
     //       HandleService hs = new HandleService();
            handleService.applyNodePreference(roleOnThesoBean.getNodePreference());
            if(!handleService.connectHandle()){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La suppression de Handle a échoué !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            try {
                handleService.deleteHandle(conceptView.getNodeConcept().getConcept().getIdHandle());
            } catch (Exception ex) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La suppression de Handle a échoué !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            handleConceptService.updateHandleIdOfConcept(conceptView.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), "");
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La suppression de Handle a réussi !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);                
        }
    }

    /**
     * permet de générer l'identifiant Handle, s'il n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateHandle() {
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        ArrayList<String> idConcepts = new ArrayList<>();
        idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        generateHandleIds(conceptHelper, idConcepts);
    }

    /**
     * permet de générer les identifiants Handle pour les concepts qui n'en ont
     * pas si un identifiant n'existe pas, il sera créé, sinon, il sera mis à
     * jour.
     */
    public void generateHandleForConceptWithoutHandle() {
        ArrayList<String> idConcepts = conceptHelper.getAllIdConceptOfThesaurusWithoutHandle(selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateHandleIds(conceptHelper, idConcepts);

        PrimeFaces.current().executeScript("window.location.reload();");
    }

    /**
     * permet de générer les identifiants Handle pour cette branche, si un
     * identifiant n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateHandleForThisBranch() {
        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        if (conceptView.getNodeConcept() == null) {
            return;
        }
        var idConcepts = conceptService.getIdsOfBranch(conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateHandleIds(conceptHelper, idConcepts);

        PrimeFaces.current().executeScript("window.location.reload();");
    }

    /**
     * permet de générer la totalité des identifiants Handle, si un identifiant
     * n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateAllHandle() {

        var idConcepts = conceptService.getAllIdConceptOfThesaurus(selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateHandleIds(conceptHelper, idConcepts);

        PrimeFaces.current().executeScript("window.location.reload();");
    }

    private void generateHandleIds(ConceptHelper conceptHelper, List<String> idConcepts) {
        FacesMessage msg;
        if (!handleConceptService.generateHandleId(idConcepts, selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La génération de Handle a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La génération de Handle a réussi !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }

    public void cancelDelete() {
        forDelete = false;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isIsCreated() {
        return isCreated;
    }

    public void setIsCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public boolean isForDelete() {
        return forDelete;
    }

    public void setForDelete(boolean forDelete) {
        this.forDelete = forDelete;
    }

    public List<NodeIdValue> getNodeReplaceBy() {
        return nodeReplaceBy;
    }

    public void setNodeReplaceBy(List<NodeIdValue> nodeReplaceBy) {
        this.nodeReplaceBy = nodeReplaceBy;
    }

    public boolean isIsReplacedByRTrelation() {
        return isReplacedByRTrelation;
    }

    public void setIsReplacedByRTrelation(boolean isReplacedByRTrelation) {
        this.isReplacedByRTrelation = isReplacedByRTrelation;
    }

    public NodeSearchMini getSearchSelected() {
        return searchSelected;
    }

    public void setSearchSelected(NodeSearchMini searchSelected) {
        this.searchSelected = searchSelected;
    }

    public List<NodeConceptType> getNodeConceptTypes() {
        return nodeConceptTypes;
    }

    public void setNodeConceptTypes(ArrayList<NodeConceptType> nodeConceptTypes) {
        this.nodeConceptTypes = nodeConceptTypes;
    }

    public String getSelectedConceptType() {
        return selectedConceptType;
    }

    public void setSelectedConceptType(String selectedConceptType) {
        this.selectedConceptType = selectedConceptType;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isApplyToBranch() {
        return applyToBranch;
    }

    public void setApplyToBranch(boolean applyToBranch) {
        this.applyToBranch = applyToBranch;
    }

    public boolean isReciprocalRelationship() {
        return reciprocalRelationship;
    }

    public void setReciprocalRelationship(boolean reciprocalRelationship) {
        this.reciprocalRelationship = reciprocalRelationship;
    }

    public NodeConceptType getNodeConceptTypeToDelete() {
        return nodeConceptTypeToDelete;
    }

    public void setNodeConceptTypeToDelete(NodeConceptType nodeConceptTypeToDelete) {
        this.nodeConceptTypeToDelete = nodeConceptTypeToDelete;
    }

    public NodeConceptType getNodeConceptTypeToAdd() {
        return nodeConceptTypeToAdd;
    }

    public void initNodeConceptTypeToAdd(){
        nodeConceptTypeToAdd = new NodeConceptType();
    }
    public void setNodeConceptTypeToAdd(NodeConceptType nodeConceptTypeToAdd) {
        this.nodeConceptTypeToAdd = nodeConceptTypeToAdd;
    }

}
