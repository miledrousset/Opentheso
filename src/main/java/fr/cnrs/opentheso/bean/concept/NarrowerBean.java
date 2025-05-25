package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.models.concept.ConceptRelation;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.models.terms.NodeNT;
import fr.cnrs.opentheso.models.relations.NodeTypeRelation;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.RelationService;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.primefaces.PrimeFaces;


@Data
@Named(value = "narrowerBean")
@SessionScoped
public class NarrowerBean implements Serializable {

    @Autowired
    @Lazy
    private ConceptView conceptBean;
    @Autowired
    @Lazy
    private SelectedTheso selectedTheso;
    @Autowired
    @Lazy
    private Tree tree;
    @Autowired
    @Lazy
    private CurrentUser currentUser;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private ConceptDcTermRepository conceptDcTermRepository;

    private NodeSearchMini searchSelected;
    private List<NodeNT> nodeNTs;
    private List<NodeTypeRelation> typesRelationsNT;
    private String selectedRelationRole;
    private boolean applyToBranch;
    @Autowired
    private RelationService relationService;

    public void clear() {
        if (nodeNTs != null) {
            nodeNTs.clear();
            nodeNTs = null;
        }
        if (typesRelationsNT != null) {
            typesRelationsNT.clear();
            typesRelationsNT = null;
        }
        searchSelected = null;
    }

    public void reset() {
        nodeNTs = conceptBean.getNodeConcept().getNodeNT();
        searchSelected = null;
        applyToBranch = false;
    }

    public void initForChangeRelations() {
        typesRelationsNT = relationsHelper.getTypesRelationsNT();
        nodeNTs = conceptBean.getNodeConcept().getNodeNT();
        applyToBranch = false;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Related !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de retourner la liste des concepts possibles
     * pour ajouter une relation NT
     * (en ignorant les relations interdites)
     * on ignore les concepts de type TT
     * on ignore les concepts de type RT
     *
     * @param value
     * @return
     */
    public List<NodeSearchMini> getAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null) {
            liste = searchHelper.searchAutoCompletionForRelation(value, conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(), true);
        }
        return liste;
    }

    /**
     * permet d'ajouter un
     *
     * @param idUser
     */
    public void addNewNarrowerLink(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        /// vérifier la cohérence de la relation
        if (isAddRelationNTValid(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), searchSelected.getIdConcept())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (!relationsHelper.addRelationNT(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), searchSelected.getIdConcept(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La création a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // on vérifie si le concept qui a été ajouté était TopTerme, alors on le rend plus TopTerm pour éviter les boucles à l'infini
        if (conceptService.isTopConcept(searchSelected.getIdConcept(), selectedTheso.getCurrentIdTheso())) {
            if (!conceptHelper.setNotTopConcept(searchSelected.getIdConcept(), selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !",
                        " erreur en enlevant le concept du TopConcept, veuillez utiliser les outils de coorection de cohérence !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        if (pf.isAjaxRequest()) {
            //    pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }

        tree.initAndExpandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getSelectedLang());
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("formLeftTab:tabTree:tree");
        }

        PrimeFaces.current().executeScript("srollToSelected();");
        PrimeFaces.current().executeScript("PF('addNarrowerLink').hide();");
        reset();
    }

    /**
     * permet de supprimer une relation NT au concept
     *
     * @param nodeNT
     * @param idUser
     */
    public void deleteNarrowerLink(ConceptRelation nodeNT, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (nodeNT == null || nodeNT.getIdConcept() == null || nodeNT.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (!relationsHelper.deleteRelationNT(
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeNT.getIdConcept(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        // on vérifie si le concept qui a été enlevé n'a plus de BT, on le rend TopTerme
        if (!relationService.isConceptHaveRelationBT(nodeNT.getIdConcept(), selectedTheso.getCurrentIdTheso())) {
            if (!conceptHelper.setTopConcept(nodeNT.getIdConcept(), selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !",
                        " erreur en passant le concept et TopConcept, veuillez utiliser les outils de coorection de cohérence !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", " Relation supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:listNarrowerLink");
        }

        tree.initAndExpandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getSelectedLang());
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("formLeftTab:tabTree:tree");
        }

        PrimeFaces.current().executeScript("srollToSelected();");
        PrimeFaces.current().executeScript("PF('deleteNarrowerLink').hide();");
    }


    public void applyRelationToBranch(String idConceptParent, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        String inverseRelation = "BT";
        switch (selectedRelationRole) {
            case "NT":
                inverseRelation = "BT";
                break;
            case "NTG":
                inverseRelation = "BTG";
                break;
            case "NTP":
                inverseRelation = "BTP";
                break;
            case "NTI":
                inverseRelation = "BTI";
                break;
        }
        applyRelationToBranch__(idConceptParent, selectedRelationRole, inverseRelation, idUser);
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", " Relation modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        initForChangeRelations();
    }
    private void applyRelationToBranch__(String idConceptParent, String relation, String inverseRelation, int idUser) {
        List<String> conceptsFils = conceptService.getListChildrenOfConcept(idConceptParent, selectedTheso.getCurrentIdTheso());
        for (String idConcept : conceptsFils) {
            relationsHelper.updateRelationNT(idConceptParent,
                    idConcept, selectedTheso.getCurrentIdTheso(), relation, inverseRelation, idUser);
            applyRelationToBranch__(idConcept, relation, inverseRelation, idUser);
        }
    }

    public void updateRelation(ConceptRelation nodeNT, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        String inverseRelation = "BT";
        switch (nodeNT.getRole()) {
            case "NT":
                inverseRelation = "BT";
                break;
            case "NTG":
                inverseRelation = "BTG";
                break;
            case "NTP":
                inverseRelation = "BTP";
                break;
            case "NTI":
                inverseRelation = "BTI";
                break;
        }

        if (!relationsHelper.updateRelationNT(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                nodeNT.getIdConcept(), selectedTheso.getCurrentIdTheso(), nodeNT.getRole(), inverseRelation, idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !",
                    " erreur modifiant la relation pour le concept !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", " Relation modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        initForChangeRelations();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:changeRelationForm");
        }
    }

    private boolean isAddRelationNTValid(String idTheso, String idConcept, String idConceptToAdd) {

        return idConcept.equalsIgnoreCase(idConceptToAdd)
                || relationsHelper.isConceptHaveRelationRT(idConcept, idConceptToAdd, idTheso)
                || relationsHelper.isConceptHaveRelationNTorBT(idConcept, idConceptToAdd, idTheso)
                || relationsHelper.isConceptHaveBrother(idConcept, idConceptToAdd, idTheso);
    }
}

