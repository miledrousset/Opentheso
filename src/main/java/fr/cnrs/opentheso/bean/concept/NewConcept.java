package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.facets.NodeFacet;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.relations.NodeTypeRelation;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.FacetHelper;
import fr.cnrs.opentheso.repositories.NonPreferredTermRepository;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.TermRepository;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Named;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.List;


@Data
@Slf4j
@SessionScoped
@RequiredArgsConstructor
@Named("newConcept")
public class NewConcept implements Serializable {

    private final RoleOnThesoBean roleOnThesoBean;
    private final ConceptView conceptBean;
    private final SelectedTheso selectedTheso;
    private final CurrentUser currentUser;
    private final Tree tree;
    private final FacetHelper facetHelper;
    private final RelationsHelper relationsHelper;
    private final GroupService groupService;
    private final ConceptHelper conceptHelper;
    private final SearchHelper searchHelper;
    private final TermRepository termRepository;
    private final NonPreferredTermRepository nonPreferredTermRepository;

    private boolean isCreated, duplicate, isConceptUnderFacet;
    private String prefLabel, notation, idNewConcept, source, relationType, idGroup, idBTfacet, idFacet;
    private List<NodeTypeRelation> typesRelationsNT;
    private List<NodeGroup> nodeGroups;
    private List<NodeSearchMini> nodeSearchMinis;

    public void reset() {
        isCreated = false;
        duplicate = false;
        prefLabel = null;
        idNewConcept = null;
        notation = null;
        isConceptUnderFacet = false;

        if (conceptBean.getNodeConcept() != null &&
                CollectionUtils.isNotEmpty(conceptBean.getNodeConcept().getNodeConceptGroup())) {
            idGroup = conceptBean.getNodeConcept().getNodeConceptGroup().get(0).getConceptGroup().getIdGroup();
        }

        typesRelationsNT = relationsHelper.getTypesRelationsNT();
        nodeGroups = groupService.getListConceptGroup(selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
    }

    public void resetForFacet(NodeFacet nodeFacet) {
        reset();
        isConceptUnderFacet = true;
        idBTfacet = nodeFacet.getIdConceptParent();
        idFacet = nodeFacet.getIdFacet();
    }

    public void infos() {
        MessageUtils.showMessage(FacesMessage.SEVERITY_WARN, "Information", "Rédiger une aide ici pour Add Concept !");
    }

    public void infosTopConcept() {
        MessageUtils.showMessage(FacesMessage.SEVERITY_WARN, "Information", "Rédiger une aide ici pour Add Top Concept!");
    }

    public void addTopConcept(String idLang, String status, String idTheso, int idUser) {
        isCreated = false;
        duplicate = false;

        if (!validateFields(idTheso, idLang)) return;

        if (roleOnThesoBean.getNodePreference() == null) {
            MessageUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Le thésaurus n'a pas de préférences !");
            return;
        }

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        Concept concept = buildConcept(idTheso, status);
        Term term = buildTerm(idTheso, idLang, status);

        idNewConcept = conceptHelper.addConcept(null, null, concept, term, idUser);
        if (idNewConcept == null) {
            MessageUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", conceptHelper.getMessage());
            return;
        }

        conceptBean.getConcept(idTheso, idNewConcept, idLang, currentUser);
        isCreated = true;

        MessageUtils.showMessage(FacesMessage.SEVERITY_INFO, "Information", "Le top concept a bien été ajouté");
        tree.addNewChild(tree.getRoot(), idNewConcept, idTheso, idLang, notation);
        tree.expandTreeToPath(idNewConcept, idTheso, idLang);
        init();

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:idAddTopConcept");
    }

    private boolean validateFields(String idTheso, String idLang) {
        if (StringUtils.isBlank(prefLabel)) {
            MessageUtils.showMessage(FacesMessage.SEVERITY_WARN, "Attention!", "Le label est obligatoire !");
            return false;
        }
        String label = prefLabel.trim();
        if (termRepository.existsPrefLabel(label, idLang, idTheso)) {
            duplicate = true;
            MessageUtils.showMessage(FacesMessage.SEVERITY_WARN, "Attention!", "Un prefLabel existe déjà avec ce nom !");
            return false;
        }
        if (nonPreferredTermRepository.isAltLabelExist(label, idTheso, idLang)) {
            duplicate = true;
            MessageUtils.showMessage(FacesMessage.SEVERITY_WARN, "Attention!", "Un synonyme existe déjà avec ce nom !");
            return false;
        }
        if (StringUtils.isNotBlank(notation) && conceptHelper.isNotationExist(idTheso, notation.trim())) {
            MessageUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Attention!", "Notation existe déjà, veuillez choisir une autre !");
            return false;
        }
        if (StringUtils.isNotBlank(idNewConcept) && conceptHelper.isIdExiste(idNewConcept, idTheso)) {
            MessageUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Attention!", "Identifiant déjà attribué !");
            return false;
        }
        return true;
    }

    private Concept buildConcept(String idTheso, String status) {
        return Concept.builder()
                .idGroup(idGroup)
                .idThesaurus(idTheso)
                .idConcept(StringUtils.defaultIfBlank(idNewConcept, null))
                .status(status)
                .notation(notation)
                .topConcept(false)
                .build();
    }

    private Term buildTerm(String idTheso, String idLang, String status) {
        return Term.builder()
                .idThesaurus(idTheso)
                .lang(idLang)
                .lexicalValue(prefLabel.trim())
                .source(StringUtils.defaultString(source))
                .status(status)
                .build();
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
}
