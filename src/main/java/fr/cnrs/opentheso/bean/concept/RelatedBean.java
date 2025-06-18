package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.relations.NodeCustomRelation;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.ConceptTypeService;
import fr.cnrs.opentheso.services.RelationService;
import fr.cnrs.opentheso.services.SearchService;
import fr.cnrs.opentheso.services.TermService;
import fr.cnrs.opentheso.utils.MessageUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Getter
@Setter
@Named(value = "relatedBean")
@SessionScoped
@RequiredArgsConstructor
public class RelatedBean implements Serializable {

    private final Tree tree;
    private final ConceptView conceptBean;
    private final SelectedTheso selectedTheso;
    private final CurrentUser currentUser;
    private final TermService termService;
    private final ConceptService conceptService;
    private final ConceptDcTermRepository conceptDcTermRepository;
    private final RelationService relationService;
    private final SearchService searchService;
    private final ConceptTypeService conceptTypeService;

    private NodeSearchMini searchSelected;
    private List<NodeRT> nodeRTs;
    private boolean tagPrefLabel = false;


    public void reset() {
        nodeRTs = conceptBean.getNodeConcept().getNodeRT();
        searchSelected = null;
        tagPrefLabel = false;
    }

    public List<NodeSearchMini> getAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null) {
            liste = searchService.searchAutoCompletionForRelation(value, conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(), true);
        }
        return liste;
    }

    public List<NodeSearchMini> getAutoCompletCustomRelation(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null) {
            liste = searchService.searchAutoCompletionForCustomRelation(value, conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso());
        }
        return liste;
    }

    public void addNewCustomRelationship(int idUser) {

        PrimeFaces pf = PrimeFaces.current();

        if (searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            MessageUtils.showErrorMessage("Aucune relation n'est sélectionné !");
            return;
        }

        var concept = conceptService.getConcept(searchSelected.getIdConcept(), selectedTheso.getCurrentIdTheso());
        if(concept == null || concept.getConceptType() == null){
            MessageUtils.showErrorMessage(" Le type de concept n'est pas reconnu !");
            return;
        }
        
        var nodeConceptType = conceptTypeService.getNodeTypeConcept(concept.getConceptType(), selectedTheso.getCurrentIdTheso());
        relationService.addCustomRelationship(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), searchSelected.getIdConcept(), idUser, concept.getConceptType(),
                nodeConceptType.isReciprocal());

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        MessageUtils.showInformationMessage("Relation ajoutée avec succès");

        reset();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    public void deleteCustomRelationship(NodeCustomRelation nodeCustomRelation, int idUser) {
        if (nodeCustomRelation == null || nodeCustomRelation.getTargetConcept() == null || nodeCustomRelation.getTargetConcept().isEmpty()) {
            MessageUtils.showErrorMessage("Aucune relation n'est sélectionné !");
            return;
        }

        relationService.deleteCustomRelationship(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), nodeCustomRelation.getTargetConcept(), idUser,
                nodeCustomRelation.getRelation(), nodeCustomRelation.isReciprocal());

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        MessageUtils.showInformationMessage("Relation supprimée avec succès");
        reset();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("conceptForm:listConceptSpecAssocies");
        PrimeFaces.current().executeScript("PF('deleteQualifierLink').show();");
    }

    public void addNewRelatedLink() {

        if (searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            MessageUtils.showErrorMessage("Aucune relation n'est sélectionné !");
            return;
        }

        /// vérifier la cohérence de la relation
        if (relationService.isConceptHaveRelationNTorBT(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                searchSelected.getIdConcept(), selectedTheso.getCurrentIdTheso())) {
            MessageUtils.showErrorMessage("Relation non permise !");
            return;
        }

        relationService.addRelationRT(conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(),
                searchSelected.getIdConcept(), currentUser.getNodeUser().getIdUser());

        // mettre à jour le label du concept si l'option TAG est activée
        if (tagPrefLabel) {
            var taggedValue = termService.getLexicalValueOfConcept(searchSelected.getIdConcept(), selectedTheso.getCurrentIdTheso(), conceptBean.getSelectedLang());
            termService.updateTermTraduction(conceptBean.getNodeConcept().getTerm().getLexicalValue() + " (" + taggedValue + ")",
                    conceptBean.getNodeConcept().getTerm().getIdTerm(), conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(), currentUser.getNodeUser().getIdUser());
        }

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        if (tagPrefLabel) {
            if (CollectionUtils.isNotEmpty(tree.getClickselectedNodes())) {
                // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
                if (!((TreeNodeData) tree.getClickselectedNodes().get(0).getData()).getNodeId().equalsIgnoreCase(
                        conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                    tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                            selectedTheso.getCurrentIdTheso(),
                            conceptBean.getSelectedLang());
                }
                ((TreeNodeData) tree.getClickselectedNodes().get(0).getData()).setName(conceptBean.getNodeConcept().getTerm().getLexicalValue());
                PrimeFaces.current().ajax().update("formLeftTab:tabTree:tree");
            }
        }

        MessageUtils.showInformationMessage("Relation ajoutée avec succès");

        reset();
        PrimeFaces.current().ajax().update("containerIndex");
        PrimeFaces.current().executeScript("PF('addRelatedLink').hide();");
    }

    public void deleteRelatedLink(NodeRT nodeRT, int idUser) {

        if (nodeRT == null || nodeRT.getIdConcept() == null || nodeRT.getIdConcept().isEmpty()) {
            MessageUtils.showErrorMessage("Aucune relation sélectionné !");
            return;
        }

        relationService.deleteRelationRT(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), nodeRT.getIdConcept(), idUser);

        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang(), currentUser);

        conceptService.updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CONTRIBUTOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(conceptBean.getNodeConcept().getConcept().getIdConcept())
                .idThesaurus(selectedTheso.getCurrentIdTheso())
                .build());

        MessageUtils.showInformationMessage("Relation supprimée avec succès");
        reset();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("conceptForm:listConceptSpecAssocies");
        PrimeFaces.current().executeScript("PF('deleteRelatedLink').show();");
    }
}
