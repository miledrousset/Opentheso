package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bean.facet.EditFacet;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.models.facets.NodeFacet;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.relations.NodeTypeRelation;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.NonPreferredTermRepository;
import fr.cnrs.opentheso.repositories.RelationsHelper;
import fr.cnrs.opentheso.repositories.TermRepository;
import fr.cnrs.opentheso.services.ConceptAddService;
import fr.cnrs.opentheso.services.ConceptService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.TermService;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

import java.io.Serializable;
import java.util.List;

@Data
@Slf4j
@SessionScoped
@RequiredArgsConstructor
@Named("addConcept")
public class AddConcept implements Serializable {

    private final ConceptView conceptBean;
    private final SelectedTheso selectedTheso;
    private final EditFacet editFacet;
    private final CurrentUser currentUser;
    private final ConceptHelper conceptHelper;
    private final RelationsHelper relationsHelper;
    private final TermRepository termRepository;
    private final ConceptService conceptService;
    private final GroupService groupService;
    private final NonPreferredTermRepository nonPreferredTermRepository;
    private final ConceptAddService conceptAddService;
    private final TermService termService;

    private boolean isCreated, duplicate, isConceptUnderFacet;
    private String prefLabel, notation, idNewConcept, source, relationType, idGroup, idBTfacet, idFacet;
    private List<NodeTypeRelation> typesRelationsNT;
    private List<NodeGroup> nodeGroups;
    private List<NodeSearchMini> nodeSearchMinis;

    public void addNewConcept(String idConceptParent, String idLang, String status, String idTheso, int idUser) {
        isCreated = false;
        duplicate = false;

        if (!isLabelValid() || !isNotationValid(idTheso)) {
            updateUIOnError();
            return;
        }

        if (termRepository.existsPrefLabel(prefLabel.trim(), idLang, idTheso)) {
            duplicate = true;
            MessageUtils.showWarnMessage("un prefLabel existe déjà avec ce nom !");
            updateUIOnError();
            return;
        }

        if (termService.isAltLabelExist(prefLabel.trim(), idTheso, idLang)) {
            duplicate = true;
            MessageUtils.showWarnMessage("un synonyme existe déjà avec ce nom !");
            updateUIOnError();
            return;
        }

        addNewConceptForced(idConceptParent, idLang, status, idTheso, idUser);
    }

    public void addNewConceptForced(String idConceptParent, String idLang, String status, String idThesaurus, int idUser) {

        isCreated = conceptAddService.addNewConcept(idThesaurus, idNewConcept, idGroup, idLang, prefLabel, status, source,
                idFacet, idConceptParent, notation, idBTfacet, relationType, idUser, isConceptUnderFacet, currentUser);

        duplicate = false;
        idNewConcept = null;
        prefLabel = "";
        notation = "";
    }

    public void resetForFacet(NodeFacet nodeFacet) {
        reset();
        isConceptUnderFacet = true;
        idBTfacet = nodeFacet.getIdConceptParent();
        idFacet = nodeFacet.getIdFacet();
    }

    public void reset() {
        isCreated = false;
        duplicate = false;
        prefLabel = null;
        idNewConcept = null;
        notation = null;
        isConceptUnderFacet = false;

        if (conceptBean.getNodeConcept() != null) {
            conceptBean.getNodeConcept().getNodeConceptGroup().stream()
                    .findFirst()
                    .ifPresent(nodeGroup -> idGroup = nodeGroup.getConceptGroup().getIdGroup());
        }

        typesRelationsNT = relationsHelper.getTypesRelationsNT();
        nodeGroups = groupService.getListConceptGroup(selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
    }

    public void cancel() {
        duplicate = false;
    }

    public void infos() {
        MessageUtils.showInformationMessage("Rédiger une aide ici pour Add Concept !");
    }

    private boolean isLabelValid() {
        if (StringUtils.isBlank(prefLabel)) {
            MessageUtils.showWarnMessage("le label est obligatoire !");
            return false;
        }
        return true;
    }

    private boolean isNotationValid(String idTheso) {
        return StringUtils.isBlank(notation) || !conceptHelper.isNotationExist(idTheso, notation.trim());
    }

    private void updateUIOnError() {
        PrimeFaces.current().ajax().update("containerIndex:addNTMessage");
        PrimeFaces.current().ajax().update("containerIndex:idAddNT");
    }
}