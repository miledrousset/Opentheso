package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.Concept;
import fr.cnrs.opentheso.entites.HierarchicalRelationship;
import fr.cnrs.opentheso.models.concept.NodeConceptSearch;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.repositories.ConceptDcTermRepository;
import fr.cnrs.opentheso.repositories.ConceptFacetRepository;
import fr.cnrs.opentheso.repositories.ConceptHistoriqueRepository;
import fr.cnrs.opentheso.repositories.ConceptReplacedByRepository;
import fr.cnrs.opentheso.repositories.ConceptRepository;
import fr.cnrs.opentheso.repositories.ConceptTypeRepository;
import fr.cnrs.opentheso.repositories.CorpusLinkRepository;
import fr.cnrs.opentheso.repositories.PermutedRepository;
import fr.cnrs.opentheso.repositories.PreferredTermRepository;
import fr.cnrs.opentheso.repositories.RelationsHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;



@Slf4j
@Service
@RequiredArgsConstructor
public class ConceptService {

    private final ConceptDcTermRepository conceptDcTermRepository;
    private final ConceptHistoriqueRepository conceptHistoriqueRepository;
    private final ConceptReplacedByRepository conceptReplacedByRepository;
    private final CorpusLinkRepository corpusLinkRepository;
    private final ConceptTypeRepository conceptTypeRepository;
    private final ConceptFacetRepository conceptFacetRepository;
    private final ConceptRepository conceptRepository;
    private final PermutedRepository permutedRepository;
    private final PreferenceService preferenceService;
    private final TermService termService;
    private final RelationsHelper relationsHelper;
    private final NonPreferredTermService nonPreferredTermService;
    private final GroupService groupService;
    private final ResourceService resourceService;
    private final FacetService facetService;
    private final PreferredTermRepository preferredTermRepository;
    private final AlignmentService alignmentService;
    private final HandleConceptService handleConceptService;
    private final RelationService relationService;
    private final NoteService noteService;


    public void updateConcept(Concept concept) {

        log.info("Mise à jour du concept id {}", concept.getId());
        conceptRepository.save(concept);
    }

    public List<Concept> getConceptByThesaurusAndTopConceptAndStatusNotLike(String idThesaurus, boolean isTopConcept, String status) {

        log.info("Recherche des concepts par thésaurus {}, topConcept {} et status {}", idThesaurus, isTopConcept, status);
        return conceptRepository.findAllByIdThesaurusAndTopConceptAndStatusNotLike(idThesaurus, isTopConcept, status);
    }

    public List<String> getAllIdConceptOfThesaurus(String idThesaurus) {

        log.info("Recherche de tous les concepts présent dans le thésaurus id {}", idThesaurus);
        var concepts = conceptRepository.findAllByIdThesaurusAndStatusNot(idThesaurus, "CA");
        if (CollectionUtils.isEmpty(concepts)) {
            log.info("Aucun concept n'est trouvé dans le thésaurus id {}", idThesaurus);
            return List.of();
        }
        log.info("{} concepts trouvés !", concepts.size());
        return concepts.stream().map(Concept::getIdConcept).toList();
    }

    public boolean isTopConcept(String idConcept, String idThesaurus) {

        log.info("Vérifier si le concept id {} est un top concept {}", idConcept, idThesaurus);
        var concept = conceptRepository.findByIdConceptAndIdThesaurus(idConcept, idThesaurus);
        if (concept.isEmpty()) {
            log.error("Aucun concept n'est trouvé avec l'id {}", idConcept);
            return false;
        }

        return concept.get().getTopConcept();
    }

    public Concept getConcept(String idConcept) {

        log.info("Recherche du concept avec l'id {}", idConcept);
        var concept = conceptRepository.findByIdConcept(idConcept);
        if (concept.isEmpty()) {
            log.info("Aucun concept n'est trouvé avec l'id {}", idConcept);
            return null;
        }

        return concept.get();
    }

    public Concept getConcept(String idConcept, String idThesaurus) {

        log.info("Recherche du concept avec l'id {} dans le thésaurus id {}", idConcept, idThesaurus);
        var concept = conceptRepository.findByIdConceptAndIdThesaurus(idConcept, idThesaurus);
        if (concept.isEmpty()) {
            log.info("Aucun concept n'est trouvé avec l'id {}", idConcept);
            return null;
        }

        return concept.get();
    }

    public boolean isIdExiste(String idNewConcept, String idThesaurus) {

        log.info("Vérifier l'existence de l'id concept {} dans le thésaurus id {}", idNewConcept, idThesaurus);
        var concept = conceptRepository.findByIdConceptAndIdThesaurus(idNewConcept, idThesaurus);
        log.info("Le concept id {} existe ? {}", idNewConcept, concept.isPresent());
        return concept.isPresent();
    }

    public boolean isIdExiste(String idConcept) {

        log.info("Vérifier l'existence de l'id concept {}", idConcept);
        var concept = conceptRepository.findByIdConcept(idConcept);
        log.info("Le concept id {} existe ? {}", idConcept, concept.isPresent());
        return concept.isPresent();
    }

    public void cleanConcept() {

        log.info("Nettoyage des concepts");
        conceptRepository.cleanConcept();
    }

    public void setTopConceptTag(boolean status, String idConcept, String idThesaurus) {

        log.info("Mise à jour du Flag 'topConcept' du concept id {}", idConcept);
        conceptRepository.setTopConceptTag(status, idConcept, idThesaurus);
    }

    public void deleteByThesaurus(String idThesaurus) {

        log.info("Suppression de tous les concepts présents dans le thésaurus id {}", idThesaurus);
        permutedRepository.deleteAllByIdThesaurus(idThesaurus);
        conceptReplacedByRepository.deleteAllByIdThesaurus(idThesaurus);
        corpusLinkRepository.deleteAllByIdThesaurus(idThesaurus);
        conceptDcTermRepository.deleteAllByIdThesaurus(idThesaurus);
        conceptHistoriqueRepository.deleteAllByIdThesaurus(idThesaurus);
        conceptRepository.deleteAllByIdThesaurus(idThesaurus);
        conceptTypeRepository.deleteAllByIdTheso(idThesaurus);
        conceptFacetRepository.deleteAllByIdThesaurus(idThesaurus);
    }

    public void updateThesaurusId(String oldThesaurusId, String newThesaurusId) {

        log.info("Modification du thesaurus id dans tous les concepts (du l'id {} vers l'id {})", oldThesaurusId, newThesaurusId);
        permutedRepository.updateThesaurusId(newThesaurusId, oldThesaurusId);
        conceptReplacedByRepository.updateThesaurusId(newThesaurusId, oldThesaurusId);
        corpusLinkRepository.updateThesaurusId(newThesaurusId, oldThesaurusId);
        conceptDcTermRepository.updateThesaurusId(newThesaurusId, oldThesaurusId);
        conceptHistoriqueRepository.updateThesaurusId(newThesaurusId, oldThesaurusId);
        conceptRepository.updateThesaurusId(newThesaurusId, oldThesaurusId);
        conceptTypeRepository.updateThesaurusId(newThesaurusId, oldThesaurusId);
        conceptFacetRepository.updateThesaurusId(newThesaurusId, oldThesaurusId);
    }

    public NodeConceptSearch getConceptForSearch(String idConcept, String idThesaurus, String idLang) {
        log.info("Chargement du concept '{}' dans le thésaurus '{}' pour la langue '{}'", idConcept, idThesaurus, idLang);
        return buildConceptSearch(idConcept, idThesaurus, idLang);
    }

    public NodeConceptSearch getConceptForSearchFromLabel(String label, String idThesaurus, String idLang) {
        log.info("Recherche d'un concept depuis le label '{}' (langue '{}', thésaurus '{}')", label, idLang, idThesaurus);

        String conceptId = getOneIdConceptFromLabel(idThesaurus, label, idLang);

        if (StringUtils.isEmpty(conceptId)) {
            String normalizedLabel = fr.cnrs.opentheso.utils.StringUtils.convertString(label);
            log.info("Aucun concept trouvé via label exact. Tentative via altLabel normalisé : '{}'", normalizedLabel);
            conceptId = conceptRepository.findConceptIdFromAltLabel(idThesaurus, normalizedLabel, idLang).orElse(null);
        }

        if (StringUtils.isEmpty(conceptId)) {
            log.warn("Aucun concept trouvé pour le label '{}'", label);
            return null;
        }

        log.info("Concept trouvé : '{}'", conceptId);
        return buildConceptSearch(conceptId, idThesaurus, idLang);
    }

    private NodeConceptSearch buildConceptSearch(String idConcept, String idThesaurus, String idLang) {
        NodeConceptSearch node = new NodeConceptSearch();
        node.setIdConcept(idConcept);
        node.setIdTheso(idThesaurus);
        node.setCurrentLang(idLang);

        node.setDeprecated(isDeprecated(idConcept, idThesaurus));

        log.debug("Récupération du prefLabel...");
        node.setPrefLabel(termService.getLexicalValueOfConcept(idConcept, idThesaurus, idLang));

        log.debug("Récupération des traductions...");
        node.setNodeTermTraductions(termService.getTraductionsOfConcept(idConcept, idThesaurus, idLang));

        log.debug("Récupération des termes génériques (BT)...");
        node.setNodeBT(relationService.getListBT(idConcept, idThesaurus, idLang));

        log.debug("Récupération des termes spécifiques (NT)...");
        node.setNodeNT(relationsHelper.getListNT(idConcept, idThesaurus, idLang, 21, 0));

        log.debug("Récupération des termes associés (RT)...");
        node.setNodeRT(relationService.getListRT(idConcept, idThesaurus, idLang));

        log.debug("Récupération des termes non préférés (EM)...");
        node.setNodeEM(nonPreferredTermService.getNonPreferredTerms(idConcept, idThesaurus, idLang));

        log.debug("Récupération des groupes de concept...");
        node.setNodeConceptGroup(groupService.getListGroupOfConcept(idThesaurus, idConcept, idLang));

        log.info("Concept chargé avec succès : '{}'", idConcept);
        return node;
    }

    public boolean isDeprecated(String idConcept, String idThesaurus) {

        log.info("Vérifier si le concept id {} est déprécié", idConcept);
        var concept = conceptRepository.findByIdConceptAndIdThesaurus(idConcept, idThesaurus);
        log.info("Le concept id {} est déprécié : {}", idConcept, concept.isPresent() && "dep".equalsIgnoreCase(concept.get().getStatus()));
        return concept.isPresent() && "dep".equalsIgnoreCase(concept.get().getStatus());
    }

    public String getOneIdConceptFromLabel(String idTheso, String label, String idLang) {

        log.info("Recherche d'un idConcept depuis un label exact dans le thésaurus '{}', langue '{}', valeur '{}'", idTheso, idLang, label);
        var normalizedLabel = fr.cnrs.opentheso.utils.StringUtils.convertString(label);
        return conceptRepository.findConceptIdFromLabel(idTheso, normalizedLabel, idLang).orElse(null);
    }

    public boolean deleteConcept(String idConcept, String idThesaurus) {

        log.info("Suppression du Concept id {} avec ses relations et traductions", idConcept);
        var preferredTerm = preferredTermRepository.findByIdThesaurusAndIdConcept(idThesaurus, idConcept);
        if (preferredTerm.isEmpty()) {
            return false;
        }

        termService.deleteTerm(preferredTerm.get().getIdTerm(), idThesaurus);
        relationsHelper.deleteAllRelationOfConcept(idConcept, idThesaurus);
        noteService.deleteNotes(idConcept, idThesaurus);
        alignmentService.deleteAlignmentOfConcept(idConcept, idThesaurus);
        conceptRepository.deleteAllByIdThesaurusAndIdConcept(idThesaurus, idConcept);
        facetService.deleteFacetsByConceptAndThesaurus(idConcept, idThesaurus);
        groupService.deleteAllGroupOfConcept(idConcept, idThesaurus);
        deleteConceptReplacedby(idThesaurus, idConcept);

        var preferences = preferenceService.getThesaurusPreferences(idThesaurus);
        if (preferences != null && preferences.isUseHandle()) {
            var concept = conceptRepository.findByIdConceptAndIdThesaurus(idConcept, idThesaurus);
            if (concept.isPresent() && StringUtils.isNotEmpty(concept.get().getIdHandle())) {
                handleConceptService.deleteIdHandle(idConcept, concept.get().getIdHandle(), idThesaurus);
            }
        }
        return true;
    }

    public boolean deleteBranchConcept(String idConceptTop, String idThesaurus) {

        log.info("Suppression du concept (id {}) avec ses relations et traductions", idConceptTop);
        var idConcepts = getIdsOfBranch2(idThesaurus, idConceptTop);

        // test si les concepts fils ont une poly-hiérarchie, on refuse la suppression (qui peut supprimer plusieurs branches
        for (String idConcept : idConcepts) {
            if (relationsHelper.isConceptHaveManyRelationBT(idConcept, idThesaurus)) {
                log.error("Le concept id {} dispose de plusieurs relation de type BT", idConcept);
                return false;
            }
        }

        // supprimer les concepts
        for (String idConcept : idConcepts) {
            deleteConcept(idConcept, idThesaurus);
        }
        return true;
    }

    public List<String> getIdsOfBranch2(String idThesaurus, String idConceptDeTete) {

        List<String> lisIds = new ArrayList<>();
        return getIdsOfBranch2__(idThesaurus, idConceptDeTete, lisIds);
    }

    private List<String> getIdsOfBranch2__(String idTheso, String idConceptDeTete, List<String> lisIds) {

        // pour éviter les boucles à l'infini
        if (lisIds.contains(idConceptDeTete)) {
            return lisIds;
        }

        lisIds.add(idConceptDeTete);
        List<String> listIdsOfConceptChildren = resourceService.getConceptsTT(idTheso, idConceptDeTete);

        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranch2__(idTheso, listIdsOfConceptChildren1, lisIds);
        }
        return lisIds;
    }

    public List<NodeIdValue> getListConceptsOfGroup(String idThesaurus, String idLang, String idGroup, boolean isSortByNotation) {
        return getConceptsByGroup(idThesaurus, idLang, idGroup, isSortByNotation, false);
    }

    public List<NodeIdValue> getListTopConceptsOfGroup(String idThesaurus, String idLang, String idGroup, boolean isSortByNotation) {
        return getConceptsByGroup(idThesaurus, idLang, idGroup, isSortByNotation, true);
    }

    private List<NodeIdValue> getConceptsByGroup(String idThesaurus, String idLang, String idGroup,
                                                 boolean isSortByNotation, boolean topOnly) {
        String typeLabel = topOnly ? "Top Concepts" : "Concepts";
        log.info("Chargement des {} du groupe '{}' dans le thésaurus '{}' (tri par notation: {})", typeLabel, idGroup, idThesaurus, isSortByNotation);

        List<Object[]> rawConcepts = topOnly
                ? conceptRepository.findTopConceptsByGroup(idThesaurus, idGroup)
                : conceptRepository.findConceptsByGroup(idThesaurus, idGroup);

        List<NodeIdValue> result = new ArrayList<>(rawConcepts.stream().map(row -> {
            String idConcept = (String) row[0];
            String notation = (String) row[1];
            String lexicalValue = termService.getLexicalValueOfConcept(idConcept, idThesaurus, idLang);

            NodeIdValue node = new NodeIdValue();
            node.setId(idConcept);
            node.setNotation(notation);
            node.setValue(StringUtils.isEmpty(lexicalValue) ? "__" + idConcept : lexicalValue);
            return node;
        }).toList());

        if (!isSortByNotation) {
            Collections.sort(result);
        }

        log.info("{} {} récupérés pour le groupe '{}'", result.size(), typeLabel, idGroup);
        return result;
    }

    public void deleteConceptReplacedby(String idThesaurus, String idConcept) {

        log.info("Supprimer le concept id {} de la table concept_replacedby", idConcept);
        conceptReplacedByRepository.deleteAllByIdConcept1AndIdThesaurus(idConcept, idThesaurus);
        conceptReplacedByRepository.deleteAllByIdConcept2AndIdThesaurus(idConcept, idThesaurus);
    }

    public boolean updateDateOfConcept(String idThesaurus, String idConcept, int contributor) {

        log.info("Mise à jour de la date de mise à jour du concept id {}", idConcept);
        var concept = conceptRepository.findByIdConceptAndIdThesaurus(idConcept, idThesaurus);
        if (concept.isEmpty()) {
            log.error("Aucun concept n'est trouvé avec l'id {}", idConcept);
            return false;
        }

        concept.get().setModified(new Date());
        conceptRepository.save(concept.get());
        log.info("Mise à jour de la date de modification du concept id {}", idConcept);
        return true;
    }

    public List<String> getIdsOfBranchWithoutLoop(String idConceptDeTete, String idThesaurus) {
        log.info("Rechercher tous les identifiants d'une branche en partant du concept id {}", idConceptDeTete);
        List<String> lisIds = new ArrayList<>();
        return getIdsOfBranchWithoutLoop__(idConceptDeTete, idThesaurus, lisIds);
    }

    private List<String> getIdsOfBranchWithoutLoop__(String idConceptDeTete, String idThesaurus, List<String> lisIds) {

        if (lisIds.contains(idConceptDeTete)) {
            return lisIds;
        }
        lisIds.add(idConceptDeTete);
        var listIdsOfConceptChildren = getListChildrenOfConcept(idConceptDeTete, idThesaurus);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranchWithoutLoop__(listIdsOfConceptChildren1, idThesaurus, lisIds);
        }
        return lisIds;
    }

    /**
     * Cette fonction permet de récupérer les Ids des concepts suivant l'id du
     * Concept-Père et le thésaurus sous forme de classe tableau pas de tri
     */
    public List<String> getListChildrenOfConcept(String idConcept, String idThesaurus) {

        log.info("Recherche des concept id {} de la table concept_children", idConcept);
        var relations = relationService.getListConceptRelationParRole(idConcept, idThesaurus, "NT");

        if (CollectionUtils.isEmpty(relations)) return List.of();
        return relations.stream().map(HierarchicalRelationship::getIdConcept2).toList();
    }

    /**
     * Cette fonction permet de retrouver tous tes identifiants d'une branche en
     * partant du concept en paramètre avec limit pour le nombre de résultat
     */
    public List<String> getIdsOfBranchLimited(String idConceptDeTete, String idThesaurus, int limit) {
        List<String> lisIds = new ArrayList<>();
        lisIds = getIdsOfBranchLimited__(idConceptDeTete, idThesaurus, lisIds, limit);
        return lisIds;
    }

    private List<String> getIdsOfBranchLimited__(String idConceptDeTete, String idThesaurus, List<String> lisIds, int limit) {

        if (lisIds.size() > limit) {
            return lisIds;
        }
        lisIds.add(idConceptDeTete);

        var listIdsOfConceptChildren = getListChildrenOfConcept(idConceptDeTete, idThesaurus);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranchLimited__(listIdsOfConceptChildren1, idThesaurus, lisIds, limit);
        }
        return lisIds;
    }



    /**
     * Cette fonction permet de retrouver tous tes identifiants d'une branche en partant du concept en paramètre
     */
    public List<String> getIdsOfBranch(String idConceptDeTete, String idTheso) {
        List<String> lisIds = new ArrayList<>();
        lisIds = getIdsOfBranch__(idConceptDeTete, idTheso, lisIds);
        return lisIds;
    }

    private List<String> getIdsOfBranch__(String idConceptDeTete, String idTheso, List<String> lisIds) {

        if (lisIds.contains(idConceptDeTete)) {
            return lisIds;
        }
        lisIds.add(idConceptDeTete);
        var listIdsOfConceptChildren = getListChildrenOfConcept(idConceptDeTete, idTheso);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranch__(listIdsOfConceptChildren1, idTheso, lisIds);
        }
        return lisIds;
    }
}
