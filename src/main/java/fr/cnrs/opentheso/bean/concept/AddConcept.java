package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.repositories.GroupHelper;
import fr.cnrs.opentheso.repositories.TermHelper;
import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.DcElementHelper;
import fr.cnrs.opentheso.repositories.FacetHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.models.facets.NodeFacet;
import fr.cnrs.opentheso.models.relations.NodeTypeRelation;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.facet.EditFacet;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;

import org.primefaces.PrimeFaces;


@Data
@Named(value = "addConcept")
@SessionScoped
public class AddConcept implements Serializable {
    
    @Autowired @Lazy
    private Connect connect;
    @Autowired @Lazy
    private RoleOnThesoBean roleOnThesoBean;
    @Autowired @Lazy
    private ConceptView conceptBean;
    @Autowired @Lazy
    private SelectedTheso selectedTheso;
    @Autowired @Lazy
    private Tree tree;
    @Autowired @Lazy
    private EditFacet editFacet;
    @Autowired @Lazy
    private CurrentUser currentUser;

    @Autowired
    private DcElementHelper dcElementHelper;

    @Autowired
    private FacetHelper facetHelper;

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private GroupHelper groupHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private RelationsHelper relationsHelper;

    private String prefLabel;
    private String notation;
    private String idNewConcept; // l'utilisateur peut choisir un identifiant à la création.
    private String source;
    private ArrayList<NodeTypeRelation> typesRelationsNT;
    private String relationType;
    private ArrayList<NodeGroup> nodeGroups;
    private String idGroup; // facultatif    
    private boolean isCreated;
    private boolean duplicate;
    private List<NodeSearchMini> nodeSearchMinis;

    /// partie pour les concepts à créer sous une Facette 
    private String idBTfacet;
    private String idFacet;
    private boolean isConceptUnderFacet;


    /**
     * permet d'ajouter un nouveau concept si le groupe = null, on ajoute un
     * concept sans groupe si l'id du concept est fourni, il faut controler s'il
     * est unique
     */
    public void addNewConcept(
            String idConceptParent,
            String idLang,
            String status, // CA ou D
            String idTheso,
            int idUser) {
        isCreated = false;
        duplicate = false;
        PrimeFaces pf = PrimeFaces.current();

        if (prefLabel == null || prefLabel.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "le label est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if ((notation != null) && (!notation.isEmpty())) {
            if (conceptHelper.isNotationExist(connect.openConnexionPool(), idTheso, notation.trim())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Attention!", "Notation existe déjà, veuillez choisir une autre!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (PrimeFaces.current().isAjaxRequest()) {
                    PrimeFaces.current().ajax().update("containerIndex:addNTMessage");
                    PrimeFaces.current().ajax().update("containerIndex:idAddNT");
                }                
                return;
            }
        }

        // vérification si le term à ajouter existe déjà 
        // verification dans les prefLabels
        if (termHelper.isPrefLabelExist(connect.openConnexionPool(), prefLabel.trim(), idTheso, idLang)) {
            duplicate = true;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "un prefLabel existe déjà avec ce nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (PrimeFaces.current().isAjaxRequest()) {
                PrimeFaces.current().ajax().update("containerIndex:addNTMessage");
                PrimeFaces.current().ajax().update("containerIndex:idAddNT");
            }            
            return;
        }
        // verification dans les altLabels
        if (termHelper.isAltLabelExist(connect.openConnexionPool(),
                prefLabel.trim(),
                idTheso,
                idLang)) {
            duplicate = true;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "un synonyme existe déjà avec ce nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);

            if (PrimeFaces.current().isAjaxRequest()) {
                PrimeFaces.current().ajax().update("containerIndex:addNTMessage");
                PrimeFaces.current().ajax().update("containerIndex:idAddNT");
            }
            return;        
        }
            

        addNewConceptForced(idConceptParent, idLang, status, idTheso, idUser);
    }

    public void resetForFacet(NodeFacet nodeFacet) {
        reset();
        isConceptUnderFacet = true;
        // le concept BT
        idBTfacet = nodeFacet.getIdConceptParent();
        // id de la facette
        idFacet = nodeFacet.getIdFacet();

    }
    
    /**
     * permet d'ajouter un nouveau concept en doublon après validation de
     * l'utilisateur pour créer un doublon
     *
     * @param idConceptParent
     * @param idLang
     * @param status // descripteur=D cancdidat = CA
     * @param idTheso
     * @param idUser
     */
    public void addNewConceptForced(
            String idConceptParent,
            String idLang,
            String status, // CA ou D
            String idTheso,
            int idUser) {
        isCreated = false;
        duplicate = false;
        FacesMessage msg;

        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        if ((idNewConcept != null) && (!idNewConcept.isEmpty())) {
            if (conceptHelper.isIdExiste(connect.openConnexionPool(), idNewConcept, idTheso)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Attention!", "Identifiant déjà attribué, veuillez choisir un autre ou laisser vide !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        } else {
            idNewConcept = null;
        }

        Concept concept = new Concept();

        // si le group est à null ou vide, on créé le concept sans l'ajouter à aucun groupe 
        // c'est dans ConceptHelper que ca se passe.
        concept.setIdGroup(idGroup);

        concept.setIdThesaurus(idTheso);
        concept.setStatus(status);
        concept.setNotation(notation);

        concept.setIdConcept(idNewConcept); // si l'id est null, un nouvel identifiant sera attribué

        Term terme = new Term();
        terme.setIdThesaurus(idTheso);
        terme.setLang(idLang);
        terme.setLexicalValue(prefLabel.trim());
        if (source == null) {
            source = "";
        }
        terme.setSource(source);

        terme.setStatus(status);
        concept.setTopConcept(false);

        // Si le concept est sous une Facette, le BT est celui du parent de la facette 
        if (isConceptUnderFacet) {
            idConceptParent = idBTfacet;
        }

        idNewConcept = conceptHelper.addConcept(
                connect.openConnexionPool(),
                idConceptParent, relationType,
                concept,
                terme,
                idUser);

        if (idNewConcept == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        /////////////////////////
        ///// insert DcTermsData
        DcElement dcElement = new DcElement();
        dcElement.setName(DCMIResource.CREATOR);
        dcElement.setValue(currentUser.getNodeUser().getName());
        dcElement.setLanguage(null);
        dcElementHelper.addDcElementConcept(connect.openConnexionPool(), dcElement, idNewConcept, idTheso);
        ///////////////

        if (isConceptUnderFacet) {

            if (!facetHelper.addConceptToFacet(connect.openConnexionPool(), idFacet, idTheso, idNewConcept)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le concept n'a pas été ajouté à la facette");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            TreeNodeData data = new TreeNodeData(idNewConcept, prefLabel, "", false,
                    false, true, false, "term");
            data.setIdFacetParent(idFacet);
            tree.getDataService().addNodeWithoutChild("file", data, tree.getSelectedNode());

            tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
            tree.expandTreeToPath2(idBTfacet,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getSelectedLang(),
                    idFacet);

            PrimeFaces pf = PrimeFaces.current();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
                pf.ajax().update("containerIndex");
            }
            FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concept ajouté avec succès !");
            FacesContext.getCurrentInstance().addMessage(null, msg2);
            init();
            update();
            return;
        }

        PrimeFaces pf = PrimeFaces.current();
        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(idConceptParent)) {
                tree.expandTreeToPath(idConceptParent, idTheso, idLang);
            }

            // cas où l'arbre est déjà déplié ou c'est un concept sans fils
            /// attention, cette condition permet d'éviter une erreur dans l'arbre si : 
            // un concept est sélectionné dans l'arbre mais non déployé, puis, on ajoute un TS, alors ca produit une erreur
            if (tree.getSelectedNode().getChildCount() == 0) {
                tree.getSelectedNode().setType("concept");
            }
            if (tree.getSelectedNode().isExpanded() || tree.getSelectedNode().getChildCount() == 0) {
                tree.addNewChild(tree.getSelectedNode(), idNewConcept, idTheso, idLang, notation);
                if (pf.isAjaxRequest()) {
                    pf.executeScript("srollToSelected()");
                }
                tree.getSelectedNode().setExpanded(true);
            }
        }

        conceptBean.getConcept(idTheso, idConceptParent, idLang, currentUser);
        isCreated = true;

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a bien été ajouté");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (!conceptHelper.getMessage().isEmpty()) {
            FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_WARN, "", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg2);
        }

        init();
        update();
    }

    private void update(){
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");            
            PrimeFaces.current().ajax().update("containerIndex:addNTMessage");
            PrimeFaces.current().ajax().update("containerIndex:idAddNT");
            PrimeFaces.current().ajax().update("containerIndex:addNTForm");            
        }
    }

    public void reset() {
        isCreated = false;
        duplicate = false;
        prefLabel = null;
        idNewConcept = null;
        notation = null;
        isConceptUnderFacet = false;
        if (conceptBean.getNodeConcept() != null) {
            for (NodeGroup nodeGroup : conceptBean.getNodeConcept().getNodeConceptGroup()) {
                idGroup = nodeGroup.getConceptGroup().getIdgroup();
            }
        }
        
        typesRelationsNT = relationsHelper.getTypesRelationsNT(connect.openConnexionPool());
        nodeGroups = groupHelper.getListConceptGroup(connect.openConnexionPool(),
                selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
    }

    private void init() {

        duplicate = false;
        idNewConcept = null;
        prefLabel = "";
        notation = "";
    }

    public void cancel() {
        duplicate = false;
    }
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Info !", "Rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}
