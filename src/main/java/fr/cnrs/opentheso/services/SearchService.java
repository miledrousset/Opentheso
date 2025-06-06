package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.models.ConceptIdOnly;
import fr.cnrs.opentheso.models.NodeAutoCompletionProjection;
import fr.cnrs.opentheso.models.concept.NodeAutoCompletion;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.terms.NodeElement;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.repositories.SearchRepository;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Data
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    private final NonPreferredTermService nonPreferredTermService;
    private final GroupService groupService;
    private final NoteService noteService;
    private final TermService termService;

    public List<NodeIdValue> searchAutoCompletionForRelationIdValue(String value, String idLang, String idTheso) {

        var processedValue = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(
                fr.cnrs.opentheso.utils.StringUtils.convertString(value));

        List<NodeIdValue> results = new ArrayList<>();
        // Preferred terms
        var preferred = searchRepository.searchPreferredLabels(processedValue, idLang, idTheso);
        for (Object[] row : preferred) {
            results.add(NodeIdValue.builder()
                    .id((String) row[0])
                    .value((String) row[1])
                    .build());
        }

        // Non-preferred terms
        var nonPreferred = searchRepository.searchAltLabels(processedValue, idLang, idTheso);
        for (Object[] row : nonPreferred) {
            results.add(NodeIdValue.builder()
                    .id((String) row[0])
                    .value((String) row[1])
                    .build());
        }

        return results;
    }

    public List<NodeSearchMini> searchAutoCompletionForRelation(String value, String idLang, String idThesaurus, boolean includeDeprecated) {

        // Nettoyage de la requête utilisateur
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        // Initialisation de la liste des résultats
        List<NodeSearchMini> results = new ArrayList<>();

        // Appel à la méthode nativeQuery pour les termes préférés
        List<Object[]> preferred = includeDeprecated
                ? searchRepository.searchPreferredLabels(value, idLang, idThesaurus)
                : searchRepository.searchPreferredLabelsWithoutDeprecated(value, idLang, idThesaurus);

        for (Object[] row : preferred) {
            NodeSearchMini mini = new NodeSearchMini();
            mini.setIdConcept((String) row[0]);
            mini.setPrefLabel((String) row[1]);
            mini.setAltLabel(false);
            results.add(mini);
        }

        // Appel à la méthode nativeQuery pour les termes non-préférés (synonymes)
        List<Object[]> alt = includeDeprecated
                ? searchRepository.searchAltLabels(value, idLang, idThesaurus)
                : searchRepository.searchAltLabelsWithoutDeprecated(value, idLang, idThesaurus);

        for (Object[] row : alt) {
            NodeSearchMini mini = new NodeSearchMini();
            mini.setIdConcept((String) row[0]);
            mini.setAltLabelValue(((String) row[1]).split(" ->")[0]);
            mini.setPrefLabel(((String) row[1]).split(" ->")[1]);
            mini.setAltLabel(true);
            results.add(mini);
        }

        return results;
    }

    public List<NodeSearchMini> searchAutoCompletionForCustomRelation(String value, String idLang, String idTheso) {
        String cleanedValue = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(
                fr.cnrs.opentheso.utils.StringUtils.convertString(value));

        List<NodeSearchMini> result = new ArrayList<>();

        List<Object[]> preferredResults = searchRepository.searchPreferredCustomRelations(cleanedValue, idLang, idTheso);
        for (Object[] row : preferredResults) {
            result.add(NodeSearchMini.builder()
                    .idConcept((String) row[0])
                    .prefLabel((String) row[1])
                    .conceptType((String) row[2])
                    .altLabel(false)
                    .build());
        }

        List<Object[]> altResults = searchRepository.searchAltCustomRelations(cleanedValue, idLang, idTheso);
        for (Object[] row : altResults) {
            result.add(NodeSearchMini.builder()
                    .idConcept((String) row[0])
                    .altLabelValue((String) row[1])
                    .prefLabel(row[1].toString().split("->")[1].trim()) // récupération du PT
                    .conceptType((String) row[2])
                    .altLabel(true)
                    .build());
        }

        return result;
    }

    public List<String> searchAutoCompletionWSForWidgetMatchExact(String value, String idLang, String[] idGroups, String idTheso) {
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(
                fr.cnrs.opentheso.utils.StringUtils.convertString(value.trim()));
        var groupList = Optional.ofNullable(idGroups)
                .map(Arrays::asList)
                .orElse(null);
        return searchRepository.searchConceptIdsByExactMatch(value, idLang, groupList, idTheso);
    }

    public List<fr.cnrs.opentheso.models.candidats.NodeConceptSearch> searchConceptWSV2(String value, String idLang, String idGroup, String idTheso) {
        List<fr.cnrs.opentheso.models.candidats.NodeConceptSearch> results = new ArrayList<>();

        List<String> groups = Optional.ofNullable(groupService.getAllGroupDescending(idGroup, idTheso))
                .orElseGet(ArrayList::new)
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        boolean isArabic = "ar".equals(idLang);
        String processed = formatValue(value);
        String tsQuery = isArabic ? processed.replace(" ", " & ") : null;
        String likeQuery = isArabic ? null : "%" + processed.replace(" ", "%") + "%";

        List<Object[]> rawResults = searchRepository.searchConcepts(
                idTheso, idLang, tsQuery, likeQuery, idLang, groups.isEmpty() ? null : groups);

        for (Object[] row : rawResults) {
            String idConcept = (String) row[2];

            List<NodeElement> synonymes = Stream.of("fr", "ar")
                    .flatMap(lang -> Optional.ofNullable(nonPreferredTermService.getNonPreferredTerms(idConcept, idTheso, lang))
                            .stream().flatMap(List::stream).map(this::toElement))
                    .collect(Collectors.toList());

            List<NodeElement> notes = Stream.of("fr", "ar")
                    .map(lang -> noteService.getNodeNote(idConcept, idTheso, lang, "note"))
                    .filter(Objects::nonNull)
                    .map(this::toElement)
                    .collect(Collectors.toList());

            List<NodeElement> definitions = Stream.of("fr", "ar")
                    .map(lang -> noteService.getNodeNote(idConcept, idTheso, lang, "definition"))
                    .filter(Objects::nonNull)
                    .map(this::toElement)
                    .collect(Collectors.toList());

            List<NodeElement> terms = Stream.of("fr", "ar")
                    .flatMap(lang -> Optional.ofNullable(termService.getTraductionsOfConcept(idConcept, idTheso, lang))
                            .stream().flatMap(List::stream).map(this::toElement))
                    .collect(Collectors.toList());

            List<NodeElement> collections = groupService.getListGroupOfConcept(idTheso, idConcept, idLang)
                    .stream().map(this::toElement).collect(Collectors.toList());

            results.add(fr.cnrs.opentheso.models.candidats.NodeConceptSearch.builder()
                    .idConcept(idConcept)
                    .idTerm((String) row[4])
                    .terms(terms)
                    .status(getStatusLabel((String) row[3]))
                    .collections(collections)
                    .synonymes(synonymes)
                    .notes(notes)
                    .definitions(definitions)
                    .build());
        }
        return results;
    }

    private String formatValue(String valueToSearch) {
        return fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(
                fr.cnrs.opentheso.utils.StringUtils.convertString(valueToSearch));
    }

    private NodeElement toElement(NodeTermTraduction nodeTermTraduction) {
        return NodeElement.builder()
                .lang(nodeTermTraduction.getLang())
                .value(nodeTermTraduction.getLexicalValue())
                .build();
    }

    private NodeElement toElement(NodeEM nodeEM) {
        return NodeElement.builder()
                .lang(nodeEM.getLang())
                .value(nodeEM.getLexicalValue())
                .build();
    }

    private NodeElement toElement(NodeNote note) {
        return NodeElement.builder()
                .id(String.valueOf(note.getIdNote()))
                .lang(note.getLang())
                .value(note.getLexicalValue())
                .build();
    }

    private NodeElement toElement(NodeGroup nodeGroup) {
        return NodeElement.builder()
                .id(nodeGroup.getConceptGroup().getIdGroup())
                .lang(nodeGroup.getIdLang())
                .value(nodeGroup.getLexicalValue())
                .build();
    }

    private String getStatusLabel(String status) {
        return StringUtils.isEmpty(status) ? "CO" : status;
    }

    public List<NodeSearchMini> searchFullTextElastic(String value, String idLang, String idThesaurus, boolean isPrivate) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }

        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);

        boolean langSensitive = idLang != null && !idLang.isEmpty();

        List<NodeSearchMini> results = new ArrayList<>();

        List<NodeSearchMini> preferredResults = searchRepository.searchPreferredTermsFullText(
                value, idLang, idThesaurus, isPrivate, langSensitive
        );

        for (NodeSearchMini node : preferredResults) {
            node.setConcept(true);
            if (value.trim().equalsIgnoreCase(node.getPrefLabel())) {
                results.add(0, node);
            } else {
                results.add(node);
            }
        }

        List<NodeSearchMini> altResults = searchRepository.searchAltTermsFullText(
                value, idLang, idThesaurus, isPrivate, langSensitive
        );

        for (NodeSearchMini node : altResults) {
            node.setAltLabel(true);
            if (value.trim().equalsIgnoreCase(node.getAltLabelValue())) {
                if (results.isEmpty()) {
                    results.add(0, node);
                } else if (results.get(0).getPrefLabel().equalsIgnoreCase(value.trim())) {
                    results.add(1, node);
                } else {
                    results.add(0, node);
                }
            } else {
                results.add(node);
            }
        }

        return results;
    }

    public List<NodeSearchMini> searchFullTextElastic(String value, String idLang, String idThesaurus) {
        List<NodeSearchMini> results = new ArrayList<>();

        // Préférés
        var preferredTerms = searchRepository.searchPreferredTermsLike2(value, idLang, idThesaurus);
        for (Object[] row : preferredTerms) {
            var node = new NodeSearchMini();
            node.setIdConcept((String) row[0]);
            node.setPrefLabel((String) row[1]);
            node.setIdTerm((String) row[2]);
            node.setConcept(true);
            if ("DEP".equalsIgnoreCase((String) row[3])) node.setDeprecated(true);
            results.add(node);
        }

        // Synonymes
        var synonyms = searchRepository.searchNonPreferredTermsLike(value, idLang, idThesaurus);
        for (Object[] row : synonyms) {
            var node = new NodeSearchMini();
            node.setIdConcept((String) row[0]);
            node.setIdTerm((String) row[1]);
            node.setAltLabelValue((String) row[2]);
            node.setPrefLabel((String) row[3]);
            node.setAltLabel(true);
            if ("DEP".equalsIgnoreCase((String) row[4])) node.setDeprecated(true);
            results.add(node);
        }

        return results;
    }

    public List<NodeSearchMini> searchStartWith(String value, String idLang, String idTheso, boolean isPrivate) {

        List<NodeSearchMini> results = new ArrayList<>();

        String normalizedValue = fr.cnrs.opentheso.utils.StringUtils.convertString(value);

        List<Object[]> preferredTerms;
        List<Object[]> altTerms;

        if (isPrivate) {
            preferredTerms = searchRepository.searchStartWithPreferredPublic(normalizedValue, idLang, idTheso);
            altTerms = searchRepository.searchStartWithSynonymsPublic(normalizedValue, idLang, idTheso);
        } else {
            preferredTerms = searchRepository.searchStartWithPreferred(normalizedValue, idLang, idTheso);
            altTerms = searchRepository.searchStartWithSynonyms(normalizedValue, idLang, idTheso);
        }

        for (Object[] row : preferredTerms) {
            NodeSearchMini nsm = new NodeSearchMini();
            nsm.setIdConcept((String) row[0]);
            nsm.setPrefLabel((String) row[1]);
            nsm.setIdTerm((String) row[2]);
            nsm.setConcept(true);
            nsm.setDeprecated("DEP".equalsIgnoreCase((String) row[3]));

            if (value.trim().equalsIgnoreCase(nsm.getPrefLabel())) {
                results.add(0, nsm);
            } else {
                results.add(nsm);
            }
        }

        for (Object[] row : altTerms) {
            NodeSearchMini nsm = new NodeSearchMini();
            nsm.setIdConcept((String) row[0]);
            nsm.setIdTerm((String) row[1]);
            nsm.setAltLabelValue((String) row[2]);
            nsm.setPrefLabel((String) row[3]);
            nsm.setAltLabel(true);
            nsm.setDeprecated("DEP".equalsIgnoreCase((String) row[4]));

            if (value.trim().equalsIgnoreCase(nsm.getAltLabelValue())) {
                results.add(0, nsm);
            } else {
                results.add(nsm);
            }
        }

        results.addAll(searchCollections(idTheso, value, idLang));
        results.addAll(searchFacets(idTheso, value, idLang));

        return results;
    }

    private List<NodeSearchMini> searchFacets(String idThesaurus, String value, String idLang) {
        List<Object[]> results = searchRepository.searchFacetsByPrefix(value, idLang, idThesaurus);

        List<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        for (Object[] row : results) {
            NodeSearchMini nodeSearchMini = new NodeSearchMini();
            nodeSearchMini.setIdConcept((String) row[0]);
            nodeSearchMini.setIdTerm("");
            nodeSearchMini.setAltLabelValue("");
            nodeSearchMini.setPrefLabel((String) row[1]);
            nodeSearchMini.setFacet(true);
            nodeSearchMinis.add(nodeSearchMini);
        }

        return nodeSearchMinis;
    }

    public List<NodeSearchMini> searchCollections(String idThesaurus, String value, String idLang) {

        List<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        List<Object[]> rows = searchRepository.searchCollectionsByPrefix(value, idLang, idThesaurus);
        for (Object[] row : rows) {
            NodeSearchMini node = new NodeSearchMini();
            node.setIdConcept((String) row[0]);
            node.setIdTerm("");
            node.setAltLabelValue("");
            node.setPrefLabel((String) row[1]);
            node.setGroup(true);
            nodeSearchMinis.add(node);
        }

        return nodeSearchMinis;
    }

    public List<NodeSearchMini> searchFullText(String value, String idLang, String idThesaurus, int limit) {

        List<NodeSearchMini> results = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        // Requête sur termes préférés
        List<Object[]> preferred = searchRepository.searchPreferredTermsLike2(value, idLang, idThesaurus);
        for (Object[] row : preferred) {
            NodeSearchMini node = new NodeSearchMini();
            node.setIdConcept((String) row[0]);
            node.setPrefLabel((String) row[1]);
            node.setIdTerm((String) row[2]);
            node.setAltLabel(false);
            if (value.trim().equalsIgnoreCase((String) row[1])) {
                results.add(0, node);
            } else {
                results.add(node);
            }
        }

        // Requête sur synonymes
        List<Object[]> alt = searchRepository.searchNonPreferredTermsLike(value, idLang, idThesaurus);
        for (Object[] row : alt) {
            NodeSearchMini node = new NodeSearchMini();
            node.setIdConcept((String) row[0]);
            node.setIdTerm((String) row[1]);
            node.setAltLabelValue((String) row[2]);  // npt
            node.setPrefLabel((String) row[3]);      // pt
            node.setAltLabel(true);
            if (value.trim().equalsIgnoreCase((String) row[2]) && !results.isEmpty()) {
                if (results.get(0).getPrefLabel().equalsIgnoreCase(value.trim())) {
                    results.add(1, node);
                } else {
                    results.add(0, node);
                }
            } else {
                results.add(node);
            }
        }

        return results.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Permet de chercher les termes exacts avec des règles précises pour trouver
     * par exemple : or, Ur, d'Ur ...
     */
    public List<NodeSearchMini> searchExactMatch(String value, String idLang, String idTheso, boolean isPrivate) {
        List<NodeSearchMini> results = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);

        List<Object[]> preferredResults = isPrivate ?
                searchRepository.searchExactPreferredTermsPrivate(idTheso, idLang, value) :
                searchRepository.searchExactPreferredTermsPublic(idTheso, idLang, value);

        for (Object[] row : preferredResults) {
            NodeSearchMini node = new NodeSearchMini();
            node.setIdConcept((String) row[0]);
            node.setPrefLabel((String) row[1]);
            node.setIdTerm((String) row[2]);
            node.setConcept(true);
            if ("DEP".equalsIgnoreCase((String) row[3])) node.setDeprecated(true);
            if (value.trim().equalsIgnoreCase((String) row[1])) {
                results.add(0, node);
            } else {
                results.add(node);
            }
        }

        List<Object[]> altResults = isPrivate ?
                searchRepository.searchExactAltTermsPrivate(idTheso, idLang, value) :
                searchRepository.searchExactAltTermsPublic(idTheso, idLang, value);

        for (Object[] row : altResults) {
            NodeSearchMini node = new NodeSearchMini();
            node.setIdConcept((String) row[0]);
            node.setIdTerm((String) row[1]);
            node.setAltLabelValue((String) row[2]);
            node.setPrefLabel((String) row[3]);
            node.setAltLabel(true);
            if ("DEP".equalsIgnoreCase((String) row[4])) node.setDeprecated(true);
            if (value.trim().equalsIgnoreCase((String) row[2])) {
                results.add(0, node);
            } else {
                results.add(node);
            }
        }

        results.addAll(searchCollections(idTheso, value, idLang));
        results.addAll(searchFacets(idTheso, value, idLang));
        return results;
    }

    public List<NodeSearchMini> searchExactTermForAutocompletion(String value, String lang, String thesaurusId) {
        List<NodeSearchMini> results = new ArrayList<>();
        String cleanedValue = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        cleanedValue = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(cleanedValue);

        List<Object[]> preferredTerms = searchRepository.searchPreferredTermsExact(cleanedValue, lang.isEmpty() ? null : lang, thesaurusId);
        for (Object[] row : preferredTerms) {
            NodeSearchMini node = new NodeSearchMini();
            node.setIdConcept((String) row[0]);
            node.setPrefLabel((String) row[1]);
            node.setIdTerm((String) row[2]);
            node.setConcept(true);
            node.setAltLabel(false);
            results.add(node);
        }

        List<Object[]> altTerms = searchRepository.searchSynonymsExact(cleanedValue, lang.isEmpty() ? null : lang, thesaurusId);
        for (Object[] row : altTerms) {
            NodeSearchMini node = new NodeSearchMini();
            node.setIdConcept((String) row[0]);
            node.setIdTerm((String) row[1]);
            node.setAltLabelValue((String) row[2]);
            node.setPrefLabel((String) row[3]);
            node.setAltLabel(true);
            results.add(node);
        }

        return results;
    }

    public List<String> searchIdConceptFromNotes(String value, String idLang, String idThesaurus) {
        String processed = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        processed = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(processed);

        String[] words = processed.trim().split("\\s+");
        if (words.length == 0) return List.of();

        StringBuilder fragments = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) fragments.append(" AND ");
            fragments.append("f_unaccent(lower(n.lexicalvalue)) LIKE '%").append(words[i]).append("%'");
        }

        List<String> idsFromTerms = searchRepository.searchConceptIdsFromTermNotes(idThesaurus, idLang, fragments.toString());
        List<String> idsFromConcepts = searchRepository.searchConceptIdsFromConceptNotes(idThesaurus, idLang, fragments.toString());

        Set<String> result = new LinkedHashSet<>();
        result.addAll(idsFromTerms);
        result.addAll(idsFromConcepts);

        return new ArrayList<>(result);
    }

    public List<String> searchAutoCompletionWSForWidget(String value, String idLang, String[] idGroups, String idTheso) {
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value.trim());

        List<String> idGroupList = (idGroups == null || idGroups.length == 0)
                ? Collections.emptyList()
                : Arrays.asList(idGroups);

        boolean hasGroups = !idGroupList.isEmpty();

        // 1. Recherche dans les termes préférés
        List<String> conceptIds = searchRepository.searchPreferredConceptsForAutoCompletion(
                value, idLang, idTheso, idGroupList, hasGroups);

        // 2. Si aucun résultat, chercher dans les synonymes
        if (conceptIds.isEmpty()) {
            conceptIds = searchRepository.searchAltConceptsForAutoCompletion(
                    value, idLang, idTheso, idGroupList, hasGroups);
        }

        return conceptIds;
    }

    public List<String> searchConceptDuplicated(String idTheso, String idLang) {
        Set<String> duplicates = new HashSet<>();

        duplicates.addAll(searchRepository.findDuplicatePreferredTerms(idTheso, idLang));
        duplicates.addAll(searchRepository.findPreferredAndAltLabelDuplicates(idTheso, idLang));
        duplicates.addAll(searchRepository.findDuplicateAltLabels(idTheso, idLang));

        return new ArrayList<>(duplicates);
    }

    public List<NodeAutoCompletion> searchAutoCompletionWS(String value, String idLang, String[] idGroupsArray,
                                                           String idTheso, boolean withNotes) {
        List<NodeAutoCompletion> results = new ArrayList<>();
        String[] words = value.trim().toLowerCase().split(" ");

        // Conditions dynamiques SQL LIKE avec unaccent()
        String condition = Arrays.stream(words)
                .map(w -> "f_unaccent(lower(t.lexical_value)) LIKE '%" + w + "%'")
                .collect(Collectors.joining(" AND "));

        List<String> idGroups = idGroupsArray != null ? Arrays.asList(idGroupsArray) : null;

        List<NodeAutoCompletionProjection> preferred = searchRepository.searchPreferredTerms(idTheso, idLang, idGroups, condition);

        for (NodeAutoCompletionProjection p : preferred) {
            NodeAutoCompletion node = new NodeAutoCompletion();
            node.setIdConcept(p.getIdConcept());
            node.setIdArk(p.getIdArk());
            node.setIdHandle(p.getIdHandle());
            node.setPrefLabel(p.getLexicalValue());
            node.setAltLabel(false);
            if (withNotes) {
                var notes = noteService.getNoteByConceptAndThesaurusAndLangAndType(p.getIdConcept(), idTheso, p.getLang(), "definition");
                node.setDefinition(notes.stream().map(NodeNote::getLexicalValue).toList().toString());
            }
            results.add(node);
        }

        // Synonyms (if needed)
        condition = Arrays.stream(words)
                .map(w -> "f_unaccent(lower(npt.lexical_value)) LIKE '%" + w + "%'")
                .collect(Collectors.joining(" AND "));

        List<NodeAutoCompletionProjection> synonyms = searchRepository.searchSynonymTerms(idTheso, idLang, idGroups, condition);

        for (NodeAutoCompletionProjection p : synonyms) {
            NodeAutoCompletion node = new NodeAutoCompletion();
            node.setIdConcept(p.getIdConcept());
            node.setIdArk(p.getIdArk());
            node.setIdHandle(p.getIdHandle());
            node.setPrefLabel(p.getLexicalValue());
            node.setAltLabel(true);
            if (withNotes) {
                var notes = noteService.getNoteByConceptAndThesaurusAndLangAndType(p.getIdConcept(), idTheso, p.getLang(), "definition");
                node.setDefinition(notes.stream().map(NodeNote::getLexicalValue).toList().toString());
            }
            results.add(node);
        }

        return results;
    }

    public List<NodeSearchMini> searchByAllId(String identifier, String idLang, String idThesaurus, boolean isPrivate) {

        if (StringUtils.isEmpty(identifier)) {
            return Collections.emptyList();
        }

        // Appel au repository en fonction de la visibilité
        var results = isPrivate
                ? searchRepository.searchConceptByAllIdPrivate(identifier, idLang, idThesaurus)
                : searchRepository.searchConceptByAllIdPublic(identifier, idLang, idThesaurus);

        // Recherche des collections
        var collections = searchRepository.searchCollectionsById(identifier, idLang, idThesaurus);

        // Recherche des facettes
        var facets = searchRepository.searchFacetsById(identifier, idLang, idThesaurus);

        // Fusion des résultats
        List<NodeSearchMini> allResults = new ArrayList<>();
        allResults.addAll(results);
        allResults.addAll(collections);
        allResults.addAll(facets);
        return allResults;
    }

    public List<String> searchConceptWithMultiGroup(String idTheso) {
        return searchRepository.searchConceptWithMultiGroup(idTheso);
    }

    public List<String> searchNotationId(String value, String idThesaurus) {
        if (StringUtils.isEmpty(value)) return Collections.emptyList();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        return searchRepository.searchNotationId(value, idThesaurus);
    }

    public List<String> searchForIds(String value, String idTheso) {
        if (StringUtils.isEmpty(value)) return Collections.emptyList();
        return searchRepository.searchForIds(value.trim(), idTheso);
    }

    public List<String> searchConceptWithoutGroup(String idTheso) {
        if (StringUtils.isEmpty(idTheso)) return Collections.emptyList();
        return searchRepository.searchConceptWithoutGroup(idTheso);
    }

    public List<String> searchAllPolyHierarchy(String idTheso) {
        if (StringUtils.isEmpty(idTheso)) return Collections.emptyList();
        return searchRepository.searchAllPolyHierarchy(idTheso);
    }

    public List<String> searchAllDeprecatedConcepts(String idTheso) {
        if (StringUtils.isEmpty(idTheso)) return Collections.emptyList();
        return searchRepository.searchAllDeprecatedConcepts(idTheso);
    }

    public boolean isConceptHaveRTandBT(String idConcept, String idTheso) {
        if (StringUtils.isEmpty(idConcept) || StringUtils.isEmpty(idTheso)) return false;
        return searchRepository.isConceptHaveRTandBT(idConcept, idTheso);
    }

    public List<String> searchFullTextElasticId(String value, String idLang, String idThesaurus) {
        if (StringUtils.isEmpty(value)) return Collections.emptyList();

        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);

        Set<String> conceptIds = new LinkedHashSet<>();

        var preferred = searchRepository.searchPreferredTermsFullTextId(value, idLang, idThesaurus);
        for (ConceptIdOnly item : preferred) {
            conceptIds.add(item.getIdConcept());
        }

        var alternates = searchRepository.searchAltTermsFullTextId(value, idLang, idThesaurus);
        for (ConceptIdOnly item : alternates) {
            conceptIds.add(item.getIdConcept());
        }

        return new ArrayList<>(conceptIds);
    }

    public List<NodeIdValue> searchTermForIndex(String value, String lang, String theso, boolean permuted, boolean synonym) {
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        List<NodeIdValue> results = new ArrayList<>();

        if (synonym) {
            results.addAll(searchRepository.searchSynonymsPrefix(value, lang, theso));
            if (permuted) {
                results.addAll(searchRepository.searchSynonymsPermuted(value, lang, theso));
            }
        } else {
            results.addAll(searchRepository.searchTermsPrefix(value, lang, theso));
            if (permuted) {
                results.addAll(searchRepository.searchTermsPermuted(value, lang, theso));
            }
        }

        return results;
    }

    public List<String> searchAutoCompletionWSForWidgetMatchExactForOneLabel(String value, String idLang, String[] idGroups, String idTheso) {

        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }

        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value.trim());
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value.trim());

        List<String> conceptIds;

        // Recherche dans les termes préférés (avec groupes si présents)
        if (idGroups != null && idGroups.length > 0) {
            conceptIds = searchRepository.searchPreferredTermInGroupsAsId(value, idLang, idTheso, Arrays.asList(idGroups));
        } else {
            conceptIds = searchRepository.searchPreferredTermAsId(value, idLang, idTheso);
        }

        // Si aucun résultat, recherche dans les synonymes (altLabels)
        if (conceptIds.isEmpty()) {
            if (idGroups != null && idGroups.length > 0) {
                conceptIds = searchRepository.searchAltTermInGroupsAsId(value, idLang, idTheso, Arrays.asList(idGroups));
            } else {
                conceptIds = searchRepository.searchAltTermAsId(value, idLang, idTheso);
            }
        }

        return conceptIds;
    }

}
