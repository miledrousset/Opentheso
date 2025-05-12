package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.entites.ConceptDcTerm;
import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.concept.DCMIResource;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.ConceptHelper;
import fr.cnrs.opentheso.repositories.FacetHelper;
import fr.cnrs.opentheso.utils.MessageUtils;

import jakarta.faces.application.FacesMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ConceptService {

    private final FacetHelper facetHelper;
    private final ConceptHelper conceptHelper;
    private final ConceptDcTermRepository conceptDcTermRepository;

    private final Tree tree;
    private final ConceptView conceptBean;
    private final SelectedTheso selectedTheso;
    private final RoleOnThesoBean roleOnThesoBean;


    public boolean addNewConcept(String idThesaurus, String idNewConcept, String idGroup, String idLang, String prefLabel,
                                 String status, String source, String idFacet, String idConceptParent, String notation,
                                 String idBTfacet, String relationType, int idUser, boolean isConceptUnderFacet,
                                 CurrentUser currentUser) {

        log.info("Début de l'ajout du nouveau concept");
        if (roleOnThesoBean.getNodePreference() == null) {
            MessageUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Le thésaurus n'a pas de préférences !");
            return false;
        }

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        if (StringUtils.isNotEmpty(idNewConcept)) {
            if (conceptHelper.isIdExiste(idNewConcept, idThesaurus)) {
               MessageUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Attention !", "Identifiant déjà attribué, veuillez choisir un autre ou laisser vide !!");
               return false;
            }
        } else {
            idNewConcept = null;
        }

        // si le group est à null ou vide, on créé le concept sans l'ajouter à aucun groupe
        // c'est dans ConceptHelper que ca se passe.
        var concept = Concept.builder()
                .idGroup(idGroup)
                .idThesaurus(idThesaurus)
                .idConcept(idNewConcept)
                .topConcept(false)
                .status(status)
                .notation(notation)
                .build();

        var terme = Term.builder()
                .idThesaurus(idThesaurus)
                .lang(idLang)
                .status(status)
                .lexicalValue(prefLabel.trim())
                .source(StringUtils.isEmpty(source) ? "" : source)
                .build();


        log.info("Vérification : le nouveau concept est sous une Facette (le BT est celui du parent de la Facette)");
        if (isConceptUnderFacet) {
            log.info("Le concept est sous une Facette");
            idConceptParent = idBTfacet;
        }

        log.info("Enregistrement du concept dans la base");
        idNewConcept = conceptHelper.addConcept(idConceptParent, relationType, concept, terme, idUser);

        if (idNewConcept == null) {
            MessageUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", conceptHelper.getMessage());
            return false;
        }

        log.info("Enregistrement du trace de l'action");
        conceptDcTermRepository.save(ConceptDcTerm.builder()
                .name(DCMIResource.CREATOR)
                .value(currentUser.getNodeUser().getName())
                .idConcept(idNewConcept)
                .idThesaurus(idThesaurus)
                .build());

        if (isConceptUnderFacet) {
            if (!facetHelper.addConceptToFacet(idFacet, idThesaurus, idNewConcept)) {
                MessageUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Le concept n'a pas été ajouté à la facette");
                return false;
            }

            var data = new TreeNodeData(idNewConcept, prefLabel, "", false, false, true, false, "term");
            data.setIdFacetParent(idFacet);

            tree.getDataService().addNodeWithoutChild("file", data, tree.getSelectedNode());
            tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
            tree.expandTreeToPath2(idBTfacet, selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang(), idFacet);

            PrimeFaces.current().ajax().update("containerIndex");
            MessageUtils.showMessage(FacesMessage.SEVERITY_INFO, "", "Concept ajouté avec succès !");
            return true;
        }

        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(idConceptParent)) {
                tree.expandTreeToPath(idConceptParent, idThesaurus, idLang);
            }

            // cas où l'arbre est déjà déplié ou c'est un concept sans fils
            /// attention, cette condition permet d'éviter une erreur dans l'arbre si :
            // un concept est sélectionné dans l'arbre mais non déployé, puis, on ajoute un TS, alors ca produit une erreur
            if (tree.getSelectedNode().getChildCount() == 0) {
                tree.getSelectedNode().setType("concept");
            }
            if (tree.getSelectedNode().isExpanded() || tree.getSelectedNode().getChildCount() == 0) {
                PrimeFaces.current().executeScript("srollToSelected()");
                tree.addNewChild(tree.getSelectedNode(), idNewConcept, idThesaurus, idLang, notation);
                tree.getSelectedNode().setExpanded(true);
            }
        }

        conceptBean.getConcept(idThesaurus, idConceptParent, idLang, currentUser);

        MessageUtils.showMessage(FacesMessage.SEVERITY_INFO, "Information", "Le concept a bien été ajouté");

        if (!conceptHelper.getMessage().isEmpty()) {
            MessageUtils.showMessage(FacesMessage.SEVERITY_WARN, "", conceptHelper.getMessage());
        }

        log.info("Fin de l'ajout du nouveau concept");
        return true;
    }
}
