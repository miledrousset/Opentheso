package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import fr.cnrs.opentheso.models.concept.NodeAutoCompletion;
import fr.cnrs.opentheso.models.candidats.NodeConceptSearch;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.terms.NodeElement;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.search.NodeSearch;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SearchHelper {

    @Autowired
    private TermHelper termHelper;

    @Autowired
    private NoteHelper noteHelper;

    @Autowired
    private GroupHelper groupHelper;

    /**
     * Permet de chercher les terms avec précision pour limiter le bruit avec filtre par langue et ou par groupe
     */
    public ArrayList<NodeAutoCompletion> searchAutoCompletionWS(HikariDataSource ds, String value, String idLang,
                                                                String[] idGroups, String idTheso, boolean withNotes) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        

        ArrayList<NodeAutoCompletion> nodeAutoCompletions = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        String multiValuesPT = "";
        String multiValuesNPT = "";
        String values[] = value.trim().split(" ");

        // filter by lang, c'est très important 
        if (idLang != null && !idLang.isEmpty()) {
            multiValuesPT += " and term.lang = '" + idLang + "'";
            multiValuesNPT += " and non_preferred_term.lang = '" + idLang + "'";
        }

        // filter by group, c'est très important 
        if (idGroups != null && idGroups.length != 0) {
            String groupSearch = "";
            for (String idGroup : idGroups) {
                if (groupSearch.isEmpty()) {
                    groupSearch = "'" + idGroup.toLowerCase() + "'";
                } else {
                    groupSearch = groupSearch + ",'" + idGroup.toLowerCase() + "'";
                }
            }
            multiValuesPT += " and LOWER(concept_group_concept.idgroup) in (" + groupSearch + ")";
            multiValuesNPT += " and LOWER(concept_group_concept.idgroup) in (" + groupSearch + ")";
        }

        for (String value1 : values) {
            multiValuesPT
                    += " and ("
                    + " f_unaccent(lower(term.lexical_value)) like '" + value1 + "%'"
                    + " or"
                    + " f_unaccent(lower(term.lexical_value)) like '% " + value1 + "%'"
                    + " or"
                    + " f_unaccent(lower(term.lexical_value)) like '%''" + value1 + "%'"
                    + ")";
            multiValuesNPT
                    += " and ("
                    + " f_unaccent(lower(non_preferred_term.lexical_value)) like '" + value1 + "%'"
                    + " or"
                    + " f_unaccent(lower(non_preferred_term.lexical_value)) like '% " + value1 + "%'"
                    + " or"
                    + " f_unaccent(lower(non_preferred_term.lexical_value)) like '%''" + value1 + "%'"
                    + ")";
        }

        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if (idGroups != null && idGroups.length != 0) {
                        query = "select term.lexical_value, term.lang, concept.id_concept, concept.id_ark, concept.id_handle "
                                + " from concept, concept_group_concept, preferred_term, term "
                                + " where"
                                + " concept.id_concept = concept_group_concept.idconcept"
                                + " and"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " and"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus "
                                + " and"
                                + " preferred_term.id_term = term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = term.id_thesaurus "
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'"
                                + multiValuesPT
                                + " order by term.lexical_value ASC limit 100";
                    } else {
                        query = "select term.lexical_value, term.lang,"
                                + " concept.id_concept, concept.id_ark, concept.id_handle"
                                + " from term, preferred_term, concept where"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and preferred_term.id_term = term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = term.id_thesaurus"
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'"
                                + multiValuesPT
                                + " order by term.lexical_value ASC limit 100";
                    }
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setIdArk(resultSet.getString("id_ark"));
                        nodeAutoCompletion.setIdHandle(resultSet.getString("id_handle"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setAltLabel(false);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeAutoCompletions.add(0, nodeAutoCompletion);
                        } else {
                            nodeAutoCompletions.add(nodeAutoCompletion);
                        }
                        if (withNotes) {
                            if (noteHelper != null) {
                                nodeAutoCompletion.setDefinition(noteHelper.getDefinition(ds, nodeAutoCompletion.getIdConcept(), idTheso, resultSet.getString("lang")).toString());
                            }
                        }
                    }

                    /**
                     * recherche de Synonymes
                     */
                    if (idGroups != null && idGroups.length != 0) {
                        query = "select non_preferred_term.lexical_value, non_preferred_term.lang, concept.id_concept, concept.id_ark, concept.id_handle "
                                + " from concept, concept_group_concept, preferred_term, non_preferred_term"
                                + " where"
                                + " concept.id_concept = concept_group_concept.idconcept"
                                + " and"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " and"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and"
                                + " preferred_term.id_term = non_preferred_term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                                + multiValuesNPT
                                + " order by non_preferred_term.lexical_value ASC limit 100";
                    } else {
                        query = "select non_preferred_term.lexical_value, non_preferred_term.lang, "
                                + " concept.id_concept, concept.id_ark, concept.id_handle"
                                + " from non_preferred_term, preferred_term, concept"
                                + " where"
                                + " preferred_term.id_term = non_preferred_term.id_term "
                                + " and preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                + " and preferred_term.id_concept = concept.id_concept"
                                + " AND preferred_term.id_thesaurus = concept.id_thesaurus"
                                + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                                + multiValuesNPT
                                + " order by non_preferred_term.lexical_value ASC limit 100";
                    }

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setIdArk(resultSet.getString("id_ark"));
                        nodeAutoCompletion.setIdHandle(resultSet.getString("id_handle"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setAltLabel(true);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeAutoCompletions.add(0, nodeAutoCompletion);
                        } else {
                            nodeAutoCompletions.add(nodeAutoCompletion);
                        }
                        if (withNotes) {
                            if (noteHelper != null) {
                                nodeAutoCompletion.setDefinition(noteHelper.getDefinition(ds, nodeAutoCompletion.getIdConcept(), idTheso, resultSet.getString("lang")).toString());
                            }
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeAutoCompletions;
    }


    public List<NodeConceptSearch> searchConceptWSV2(HikariDataSource ds, String value, String idLang, String idGroup, String idTheso) {

        var results = new ArrayList<NodeConceptSearch>();

        try (var conn = ds.getConnection()){
            try (var stmt = conn.createStatement()) {

                var groups = generateGroupCondition(ds, idTheso, idGroup);
                var resultSet = stmt.executeQuery(getSqlSearch(value, idTheso, idLang, groups));
                while (resultSet.next()) {
                    var idConcept = resultSet.getString("id_concept");

                    var synonymes = new ArrayList<NodeElement>();
                    synonymes.addAll(Optional.ofNullable(termHelper.getNonPreferredTerms(ds, idConcept, idTheso, "fr"))
                            .map(terms -> terms.stream().map(this::toElement).collect(Collectors.toList()))
                            .orElse(Collections.emptyList()));
                    synonymes.addAll(Optional.ofNullable(termHelper.getNonPreferredTerms(ds, idConcept, idTheso, "ar"))
                            .map(terms -> terms.stream().map(this::toElement).collect(Collectors.toList()))
                            .orElse(Collections.emptyList()));

                    var notes = new ArrayList<NodeElement>();
                    var noteFr = noteHelper.getNodeNote(ds, idConcept, idTheso, "fr", "note");
                    if (ObjectUtils.isNotEmpty(noteFr)) notes.add(toElement(noteFr));
                    var noteAr = noteHelper.getNodeNote(ds, idConcept, idTheso, "ar", "note");
                    if (ObjectUtils.isNotEmpty(noteAr)) notes.add(toElement(noteAr));

                    var definitions = new ArrayList<NodeElement>();
                    var definitionFr = noteHelper.getNodeNote(ds, idConcept, idTheso, "fr", "definition");
                    if (ObjectUtils.isNotEmpty(definitionFr)) definitions.add(toElement(definitionFr));
                    var definitionAr = noteHelper.getNodeNote(ds, idConcept, idTheso, "ar", "definition");
                    if (ObjectUtils.isNotEmpty(definitionAr)) definitions.add(toElement(definitionAr));

                    var terms = new ArrayList<NodeElement>();
                    terms.addAll(Optional.ofNullable(termHelper.getTraductionsOfConcept(ds, idConcept, idTheso, "fr"))
                            .map(element -> element.stream().map(this::toElement).collect(Collectors.toList()))
                            .orElse(Collections.emptyList()));
                    terms.addAll(Optional.ofNullable(termHelper.getTraductionsOfConcept(ds, idConcept, idTheso, "ar"))
                            .map(element -> element.stream().map(this::toElement).collect(Collectors.toList()))
                            .orElse(Collections.emptyList()));


                    var nodeConceptSearch = NodeConceptSearch.builder()
                            .idConcept(idConcept)
                            .idTerm(resultSet.getString("id_term"))
                            .terms(terms)
                            .status(getStatusLabel(resultSet.getString("status")))
                            .collections(groupHelper.getListGroupOfConcept(ds, idTheso, idConcept, idLang).stream()
                                    .map(this::toElement)
                                    .collect(Collectors.toList()))
                            .synonymes(synonymes)
                            .notes(notes)
                            .definitions(definitions)
                            .build();

                    results.add(nodeConceptSearch);
                }
            }
            return results;
        } catch (Exception ex) {
            return results;
        }
    }

    private String generateGroupCondition(HikariDataSource ds, String idTheso, String idGroup) {
        String groups = null;
        if (!StringUtils.isEmpty(idGroup)) {
            var groupList = groupHelper.getAllGroupDescending(ds, idGroup, idTheso);
            if (CollectionUtils.isEmpty(groupList)) groupList = new ArrayList<>();
            groups = String.join(",", groupList.stream()
                    .map(element -> String.format("'%s'", element.toLowerCase()))
                    .collect(Collectors.toList()));
        }
        return groups;
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
                .id(note.getIdNote() + "")
                .lang(note.getLang())
                .value(note.getLexicalValue())
                .build();
    }

    private NodeElement toElement(NodeGroup nodeGroup) {
        return NodeElement.builder()
                .id(nodeGroup.getConceptGroup().getIdgroup())
                .lang(nodeGroup.getIdLang())
                .value(nodeGroup.getLexicalValue())
                .build();
    }

    private String getStatusLabel(String status) {
        return StringUtils.isEmpty(status) ? "CO" : status;
    }

    private String getSqlSearch(String valueToSearch, String idTheso, String idLang, String idGroups) {

        var subQuerry = "";

        var values = List.of(formatValue(valueToSearch).trim().split(" "));
        for (String value1 : values) {
            if ("ar".equals(idLang)) {
                subQuerry += "AND to_tsvector('arabic', term.lexical_value) @@ to_tsquery('arabic', '"+ Normalizer.normalize(value1, Normalizer.Form.NFKC)+"') ";
            } else {
                subQuerry += "AND f_unaccent(lower(term.lexical_value)) like '%" + value1 + "%' ";
            }
        }

        return "SELECT DISTINCT term.lexical_value, term.lang, concept.id_concept, concept.status, term.id_term "
                + "FROM concept, concept_group_concept, preferred_term, term "
                + "WHERE concept.id_concept = concept_group_concept.idconcept "
                + "AND concept.id_thesaurus = concept_group_concept.idthesaurus "
                + "AND concept.id_concept = preferred_term.id_concept "
                + "AND concept.id_thesaurus = preferred_term.id_thesaurus "
                + "AND preferred_term.id_term = term.id_term "
                + "AND preferred_term.id_thesaurus = term.id_thesaurus "
                + "AND term.id_thesaurus = '" + idTheso + "' "
                + subQuerry
                + "AND term.lang = '" + idLang + "' "
                + (StringUtils.isEmpty(idGroups) ? "" : "AND LOWER(concept_group_concept.idgroup) IN (" + idGroups + ") ")
                + "ORDER BY term.lexical_value ASC limit 100";
    }

    private String formatValue(String valueToSearch) {
        valueToSearch = fr.cnrs.opentheso.utils.StringUtils.convertString(valueToSearch);
        return fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(valueToSearch);
    }

    /**
     * Permet de chercher les terms avec précision pour limiter le bruit avec
     * filtre par langue et ou par groupe Adapter pour le widget de connexion
     * avec l'affichage d'un arbre
     */
    public ArrayList<String> searchAutoCompletionWSForWidget(HikariDataSource ds,
            String value, String idLang, String[] idGroups, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        

        ArrayList<String> nodeIds = new ArrayList<>();
        value = value.trim();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);
        
        String limit = " limit 100";
        if(StringUtils.isEmpty(value)){
            limit = " limit 500";
        }
        
        String multiValuesPT = "";
        String multiValuesNPT = "";
        String values[] = value.split(" ");

        // filter by lang, c'est très important 
        if (idLang != null && !idLang.isEmpty()) {
            multiValuesPT += " and term.lang = '" + idLang + "'";
            multiValuesNPT += " and non_preferred_term.lang = '" + idLang + "'";
        }

        // filter by group, c'est très important 
        if (idGroups != null && idGroups.length != 0) {
            String groupSearch = "";
            for (String idGroup : idGroups) {
                if (groupSearch.isEmpty()) {
                    groupSearch = "'" + idGroup.toLowerCase() + "'";
                } else {
                    groupSearch = groupSearch + ",'" + idGroup.toLowerCase() + "'";
                }
            }
            multiValuesPT += " and LOWER(concept_group_concept.idgroup) in (" + groupSearch + ")";
            multiValuesNPT += " and LOWER(concept_group_concept.idgroup) in (" + groupSearch + ")";
        }

        for (String value1 : values) {
            multiValuesPT
                    += " and ("
                    + " unaccent(lower(term.lexical_value)) like unaccent(lower('" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(term.lexical_value)) like unaccent(lower('% " + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(term.lexical_value)) like unaccent(lower('% " + value1 + "-%'))"
                    + " or"
                    + " unaccent(lower(term.lexical_value)) like unaccent(lower('%-" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(term.lexical_value)) like unaccent(lower('%(" + value1 + "%')) "
                    + " or"
                    + " unaccent(lower(term.lexical_value)) like unaccent(lower('%\\_" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(term.lexical_value)) like unaccent(lower('%''" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(term.lexical_value)) like unaccent(lower('%ʿ" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(term.lexical_value)) like unaccent(lower('%[" + value1 + "%')) "
                    + ")";
            multiValuesNPT
                    += " and ("
                    + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('% " + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('% " + value1 + "-%'))"
                    + " or"
                    + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%-" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%(" + value1 + "%')) "
                    + " or"
                    + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%\\_" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%''" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%ʿ" + value1 + "%'))"
                    + " or"
                    + " unaccent(lower(lexical_value)) like unaccent(lower('%[" + value1 + "%')) "
                    + ")";
        }

        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if (idGroups != null && idGroups.length != 0) {
                        query = "select concept.id_concept, term.lexical_value "
                                + " from concept, concept_group_concept, preferred_term, term "
                                + " where"
                                + " concept.id_concept = concept_group_concept.idconcept"
                                + " and"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " and"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus "
                                + " and"
                                + " preferred_term.id_term = term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = term.id_thesaurus "
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'"
                                + " and concept.status not in ('CA', 'DEP') "
                                + multiValuesPT
                                + " order by "
                                + " CASE unaccent(lower(lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + limit;
                    } else {
                        query = "select term.lexical_value, "
                                + " concept.id_concept"
                                + " from term, preferred_term, concept where"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and preferred_term.id_term = term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = term.id_thesaurus"
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'"
                                + " and concept.status not in ('CA', 'DEP') "
                                + multiValuesPT
                                + " order by "
                                + " CASE unaccent(lower(lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + limit;
                    }
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        if (!nodeIds.contains(resultSet.getString("id_concept"))) {
                            nodeIds.add(resultSet.getString("id_concept"));
                        }
                    }

                    /**
                     * recherche de Synonymes
                     */
                    if (idGroups != null && idGroups.length != 0) {
                        query = "select concept.id_concept, non_preferred_term.lexical_value "
                                + " from concept, concept_group_concept, preferred_term, non_preferred_term"
                                + " where"
                                + " concept.id_concept = concept_group_concept.idconcept"
                                + " and"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " and"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and"
                                + " preferred_term.id_term = non_preferred_term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                                + multiValuesNPT
                                + " order by "
                                + " CASE unaccent(lower(non_preferred_term.lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + limit ;
                    } else {
                        query = "select concept.id_concept, non_preferred_term.lexical_value"
                                + " from non_preferred_term, preferred_term, concept"
                                + " where"
                                + " preferred_term.id_term = non_preferred_term.id_term "
                                + " and preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                + " and preferred_term.id_concept = concept.id_concept"
                                + " AND preferred_term.id_thesaurus = concept.id_thesaurus"
                                + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                                + multiValuesNPT
                                + " order by "
                                + " CASE unaccent(lower(non_preferred_term.lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + limit;
                    }
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        if (!nodeIds.contains(resultSet.getString("id_concept"))) {
                            nodeIds.add(resultSet.getString("id_concept"));
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIds;
    }

    /**
     * Permet de chercher les terms avec précision pour limiter le bruit avec
     * filtre par langue et ou par groupe Adapter pour le widget de connexion
     * avec l'affichage d'un arbre
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idGroups
     * @param idTheso
     * @return
     */
    public ArrayList<String> searchAutoCompletionWSForWidgetMatchExact(HikariDataSource ds,
            String value, String idLang, String[] idGroups, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        

        ArrayList<String> nodeIds = new ArrayList<>();
        value = value.trim();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        String multiValuesPT = "";
        String multiValuesNPT = "";

        // filter by lang, c'est très important 
        if (idLang != null && !idLang.isEmpty()) {
            multiValuesPT += " and term.lang = '" + idLang + "'";
            multiValuesNPT += " and non_preferred_term.lang = '" + idLang + "'";
        }

        // filter by group, c'est très important 
        if (idGroups != null && idGroups.length != 0) {
            String groupSearch = "";
            for (String idGroup : idGroups) {
                if (groupSearch.isEmpty()) {
                    groupSearch = "'" + idGroup.toLowerCase() + "'";
                } else {
                    groupSearch = groupSearch + ",'" + idGroup.toLowerCase() + "'";
                }
            }
            multiValuesPT += " and LOWER(concept_group_concept.idgroup) in (" + groupSearch + ")";
            multiValuesNPT += " and LOWERconcept_group_concept.idgroup) in (" + groupSearch + ")";
        }

        multiValuesPT += " and ("
                + " unaccent(lower(term.lexical_value)) like unaccent(lower('" + value + "'))"
                + ")";
        multiValuesNPT += " and ("
                + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('" + value + "'))"
                + ")";

        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if (idGroups != null && idGroups.length != 0) {
                        query = "select concept.id_concept, term.lexical_value "
                                + " from concept, concept_group_concept, preferred_term, term "
                                + " where"
                                + " concept.id_concept = concept_group_concept.idconcept"
                                + " and"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " and"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus "
                                + " and"
                                + " preferred_term.id_term = term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = term.id_thesaurus "
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'"
                                + " and concept.status not in ('CA', 'DEP') "
                                + multiValuesPT
                                + " order by "
                                + " CASE unaccent(lower(lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + " limit 100";

                        //by unaccent(lower(lexical_value)) ASC limit 100";
                    } else {
                        query = "select term.lexical_value, "
                                + " concept.id_concept"
                                + " from term, preferred_term, concept where"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and preferred_term.id_term = term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = term.id_thesaurus"
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'"
                                + " and concept.status not in ('CA', 'DEP') "
                                + multiValuesPT
                                + " order by "
                                + " CASE unaccent(lower(lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + " limit 100";

                        //+ " order by unaccent(lower(lexical_value)) ASC limit 100";
                    }
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        if (!nodeIds.contains(resultSet.getString("id_concept"))) {
                            nodeIds.add(resultSet.getString("id_concept"));
                        }
                    }

                    /**
                     * recherche de Synonymes
                     */
                    if (idGroups != null && idGroups.length != 0) {
                        query = "select concept.id_concept, non_preferred_term.lexical_value "
                                + " from concept, concept_group_concept, preferred_term, non_preferred_term"
                                + " where"
                                + " concept.id_concept = concept_group_concept.idconcept"
                                + " and"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " and"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and"
                                + " preferred_term.id_term = non_preferred_term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                                + multiValuesNPT
                                + " order by "
                                + " CASE unaccent(lower(non_preferred_term.lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + " limit 100";

                        // + " order by unaccent(lower(non_preferred_term.lexical_value)) ASC limit 100";
                    } else {
                        query = "select concept.id_concept, non_preferred_term.lexical_value"
                                + " from non_preferred_term, preferred_term, concept"
                                + " where"
                                + " preferred_term.id_term = non_preferred_term.id_term "
                                + " and preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                + " and preferred_term.id_concept = concept.id_concept"
                                + " AND preferred_term.id_thesaurus = concept.id_thesaurus"
                                + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                                + multiValuesNPT
                                + " order by "
                                + " CASE unaccent(lower(non_preferred_term.lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + " limit 100";

                        //+ " order by unaccent(lower(non_preferred_term.lexical_value)) ASC limit 100";
                    }
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        if (!nodeIds.contains(resultSet.getString("id_concept"))) {
                            nodeIds.add(resultSet.getString("id_concept"));
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIds;
    }

    /**
     * Permet de chercher les terms avec précision pour limiter le bruit avec
     * filtre par langue et ou par groupe, si prefLabel existe, on le retourne
     * le résultat sans chercher sur altLabel sinon, on cherche alors dans les
     * altLabels
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idGroups
     * @param idTheso
     * @return
     */
    public ArrayList<String> searchAutoCompletionWSForWidgetMatchExactForOneLabel(HikariDataSource ds,
            String value, String idLang, String[] idGroups, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        

        ArrayList<String> nodeIds = new ArrayList<>();
        value = value.trim();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        String multiValuesPT = "";
        String multiValuesNPT = "";

        // filter by lang, c'est très important 
        if (idLang != null && !idLang.isEmpty()) {
            multiValuesPT += " and term.lang = '" + idLang + "'";
            multiValuesNPT += " and non_preferred_term.lang = '" + idLang + "'";
        }

        // filter by group, c'est très important 
        if (idGroups != null && idGroups.length != 0) {
            String groupSearch = "";
            for (String idGroup : idGroups) {
                if (groupSearch.isEmpty()) {
                    groupSearch = "'" + idGroup + "'";
                } else {
                    groupSearch = groupSearch + ",'" + idGroup + "'";
                }
            }
            multiValuesPT += " and concept_group_concept.idgroup in (" + groupSearch + ")";
            multiValuesNPT += " and concept_group_concept.idgroup in (" + groupSearch + ")";
        }

        multiValuesPT += " and ("
                + " unaccent(lower(term.lexical_value)) like unaccent(lower('" + value + "'))"
                + ")";
        multiValuesNPT += " and ("
                + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('" + value + "'))"
                + ")";

        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if (idGroups != null && idGroups.length != 0) {
                        query = "select concept.id_concept, term.lexical_value "
                                + " from concept, concept_group_concept, preferred_term, term "
                                + " where"
                                + " concept.id_concept = concept_group_concept.idconcept"
                                + " and"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " and"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus "
                                + " and"
                                + " preferred_term.id_term = term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = term.id_thesaurus "
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'"
                                + " and concept.status != 'CA' "
                                + multiValuesPT
                                + " order by "
                                + " CASE unaccent(lower(lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + " limit 100";

                        //by unaccent(lower(lexical_value)) ASC limit 100";
                    } else {
                        query = "select term.lexical_value, "
                                + " concept.id_concept"
                                + " from term, preferred_term, concept where"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and preferred_term.id_term = term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = term.id_thesaurus"
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'"
                                + " and concept.status != 'CA' "
                                + multiValuesPT
                                + " order by "
                                + " CASE unaccent(lower(lexical_value)) "
                                + " WHEN '" + value + "' THEN 1"
                                + " END, lexical_value"
                                + " limit 100";

                        //+ " order by unaccent(lower(lexical_value)) ASC limit 100";
                    }
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        if (!nodeIds.contains(resultSet.getString("id_concept"))) {
                            nodeIds.add(resultSet.getString("id_concept"));
                        }
                    }
                    // si on ne trouve pas de résultat dans les prefLabels, on cherche alors dans les altLabels
                    if (nodeIds.isEmpty()) {
                        /**
                         * recherche de Synonymes
                         */
                        if (idGroups != null && idGroups.length != 0) {
                            query = "select concept.id_concept, non_preferred_term.lexical_value "
                                    + " from concept, concept_group_concept, preferred_term, non_preferred_term"
                                    + " where"
                                    + " concept.id_concept = concept_group_concept.idconcept"
                                    + " and"
                                    + " concept.id_thesaurus = concept_group_concept.idthesaurus"
                                    + " and"
                                    + " concept.id_concept = preferred_term.id_concept"
                                    + " and"
                                    + " concept.id_thesaurus = preferred_term.id_thesaurus"
                                    + " and"
                                    + " preferred_term.id_term = non_preferred_term.id_term"
                                    + " and"
                                    + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                    + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                                    + multiValuesNPT
                                    + " order by "
                                    + " CASE unaccent(lower(non_preferred_term.lexical_value)) "
                                    + " WHEN '" + value + "' THEN 1"
                                    + " END, lexical_value"
                                    + " limit 100";

                            // + " order by unaccent(lower(non_preferred_term.lexical_value)) ASC limit 100";
                        } else {
                            query = "select concept.id_concept, non_preferred_term.lexical_value"
                                    + " from non_preferred_term, preferred_term, concept"
                                    + " where"
                                    + " preferred_term.id_term = non_preferred_term.id_term "
                                    + " and preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                    + " and preferred_term.id_concept = concept.id_concept"
                                    + " AND preferred_term.id_thesaurus = concept.id_thesaurus"
                                    + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                                    + multiValuesNPT
                                    + " order by "
                                    + " CASE unaccent(lower(non_preferred_term.lexical_value)) "
                                    + " WHEN '" + value + "' THEN 1"
                                    + " END, lexical_value"
                                    + " limit 100";

                            //+ " order by unaccent(lower(non_preferred_term.lexical_value)) ASC limit 100";
                        }
                        resultSet = stmt.executeQuery(query);
                        while (resultSet.next()) {
                            if (!nodeIds.contains(resultSet.getString("id_concept"))) {
                                nodeIds.add(resultSet.getString("id_concept"));
                            }
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIds;
    }

    /**
     * En cours d'optimisation (non utilisée encore) Cette fonction permet de
     * faire une recherche par valeur sur les termes Préférés et les synonymes
     * (la recherche porte sur les termes contenus dans une chaine) en utilisant
     * la méthode PostgreSQL Trigram Index, le résultat est proche d'une
     * recherche avec ElasticSearch
     *
     * Elle retourne la liste des termes + identifiants
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param limit
     * @return
     */
    public ArrayList<NodeSearchMini> searchFullText(HikariDataSource ds,
            String value, String idLang, String idThesaurus, int limit) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        

        ArrayList<NodeSearchMini> nodeSearchMinis = null;
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        String preparedValuePT = " and f_unaccent(lower(term.lexical_value)) like '%" + value + "%'";
        String preparedValueNPT = " and f_unaccent(lower(non_preferred_term.lexical_value)) like '%" + value + "%'";

        String query;
        String lang;
        String langSynonyme;

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    nodeSearchMinis = new ArrayList<>();
                    query = "SELECT preferred_term.id_concept, term.lexical_value "
                            + " FROM term, preferred_term WHERE "
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + preparedValuePT
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + " order by term.lexical_value limit " + limit;

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setAltLabel(false);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT preferred_term.id_concept,"
                            + " non_preferred_term.lexical_value as npt,"
                            + " term.lexical_value as pt"
                            + " FROM"
                            + " non_preferred_term, term, preferred_term WHERE"
                            + "  preferred_term.id_term = term.id_term AND"
                            + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                            + "   preferred_term.id_term = non_preferred_term.id_term AND"
                            + "   term.lang = non_preferred_term.lang AND"
                            + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                            + preparedValueNPT
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            + " order by non_preferred_term.lexical_value limit " + limit;

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setAltLabelValue(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setAltLabel(true);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("npt")) && !nodeSearchMinis.isEmpty()) {
                            if (nodeSearchMinis.get(0).getPrefLabel().equalsIgnoreCase(value.trim())) {
                                nodeSearchMinis.add(1, nodeSearchMini);
                            } else {
                                nodeSearchMinis.add(0, nodeSearchMini);
                            }
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchMinis;
    }

    /**
     * Permet de chercher les terms par identifiant (idConcept, idArk, idHandle, notation)
     *
     * @param ds
     * @param identifier
     * @param idLang
     * @param idTheso
     * @return
     */
    public ArrayList<NodeSearchMini> searchByAllId(HikariDataSource ds,
            String identifier, String idLang, String idTheso) {

        ArrayList<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        if(StringUtils.isEmpty(identifier)) return nodeSearchMinis;
        identifier = identifier.trim();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select preferred_term.id_concept, term.lexical_value, term.id_term, concept.status " +
                            " from term, preferred_term, concept" +
                            " where " +
                            " concept.id_concept = preferred_term.id_concept " +
                            " and concept.id_thesaurus = preferred_term.id_thesaurus " +
                            " and preferred_term.id_term = term.id_term " +
                            " and preferred_term.id_thesaurus = term.id_thesaurus " +
                            " and concept.id_thesaurus = '" + idTheso + "' " +
                            " and term.lang = '" + idLang + "' " +
                            " and concept.status != 'CA' " +
                            " and (	" +
                            "	concept.id_concept = '" + identifier + "'" +
                            "	or	" +
                            "	concept.id_ark = '" + identifier + "'" +
                            "	or " +
                            "	concept.id_handle = '" + identifier + "'" +
                            "	or concept.notation = '" + identifier + "'" +
                            " ) " +
                            " order by unaccent(lower(lexical_value))");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setConcept(true);
                        if (resultSet.getString("status").equalsIgnoreCase("DEP")) {
                            nodeSearchMini.setDeprecated(true);
                        }
                        nodeSearchMinis.add(nodeSearchMini);
                    }
                }
            }
            //// rechercher les collections
            nodeSearchMinis = searchCollectionsById(conn, idTheso, identifier, idLang, nodeSearchMinis);

            /// rechercher les Facettes
            nodeSearchMinis = searchFacetsById(conn, idTheso, identifier, idLang, nodeSearchMinis);
        } catch (SQLException sqle) {
            log.error("Error while search By Id : " + identifier, sqle);
        }
        return nodeSearchMinis;
    }    
    private ArrayList<NodeSearchMini> searchCollectionsById(Connection conn,
            String idTheso, String identifier, String idLang,
            ArrayList<NodeSearchMini> nodeSearchMinis) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select idGroup, lexicalvalue from concept_group_label " +
                        " where" +
                        " idthesaurus = '" + idTheso + "'" +
                        " and " +
                        " lower(idGroup) = lower('" + identifier + "')" +
                        " and lang = '" + idLang + "'");

            try (ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    NodeSearchMini nodeSearchMini = new NodeSearchMini();
                    nodeSearchMini.setIdConcept(resultSet.getString("idGroup"));
                    nodeSearchMini.setIdTerm("");
                    nodeSearchMini.setAltLabelValue("");
                    nodeSearchMini.setPrefLabel(resultSet.getString("lexicalvalue"));
                    nodeSearchMini.setGroup(true);
                    nodeSearchMinis.add(nodeSearchMini);
                }
            }
        }
        return nodeSearchMinis;
    }    
    
    private ArrayList<NodeSearchMini> searchFacetsById(Connection conn,
            String idTheso, String identifier, String idLang,
            ArrayList<NodeSearchMini> nodeSearchMinis) throws SQLException {
        /// rechercher les Facettes
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_facet, lexical_value from node_label " +
                        " where " +
                        " id_thesaurus = '" + idTheso + "'" +
                        " and " +
                        " id_facet = '" + identifier + "'" +
                        " and lang = '" + idLang + "'");

            try (ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    NodeSearchMini nodeSearchMini = new NodeSearchMini();
                    nodeSearchMini.setIdConcept(resultSet.getString("id_facet"));
                    nodeSearchMini.setIdTerm("");
                    nodeSearchMini.setAltLabelValue("");
                    nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                    nodeSearchMini.setFacet(true);
                    nodeSearchMinis.add(nodeSearchMini);
                }
            }
        }
        return nodeSearchMinis;
    }

    
    /**
     * Permet de chercher les terms exacts avec des règles précises pour trouver
     * par exemple : or, Ur, d'Ur ...
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idTheso
     * @return
     */
    public ArrayList<NodeSearchMini> searchExactMatch(HikariDataSource ds,
            String value, String idLang, String idTheso) {
        

        ArrayList<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        String lang;
        if (idLang == null) {
            lang = "";
        } else {
            lang = " and term.lang = '" + idLang + "'";
        }

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select preferred_term.id_concept, term.lexical_value, term.id_term, concept.status from term, preferred_term, concept where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and"
                        + " term.id_thesaurus = '" + idTheso + "'"
                        + lang
                        + " and concept.status != 'CA'"
                        + " and ("
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('" + value + "'))"
                        + "	or"
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('" + value + " %'))"
                        + "	or"
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('% " + value + "'))"
                        + "	or"
                        // pour les tirets pour trouver victor exp: saint-victor
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('" + value + "-%'))"
                        + "	or"
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('%-" + value + "-%'))"
                        + "	or"
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('%-" + value + "'))"
                        + "	or"
                        // pour les sous_tirets pour trouver victor exp: saint_victor
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('" + value + "\\_%'))"
                        + "	or"
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('%\\_" + value + "\\_%'))"
                        + "	or"
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('%\\_" + value + "'))"
                        + "	or"
                        + "	unaccent(lower(lexical_value)) like unaccent(lower('%''" + value + "'))"
                        // pour les parenthèses pour trouver monstre exp: (monstre)                                
                        + "     or"
                        + "     unaccent(lower(lexical_value)) like unaccent(lower('%(" + value + ")%'))"
                        + "     or"
                        + "     unaccent(lower(lexical_value)) like unaccent(lower('" + value + "(%'))"
                        + "	)"
                        + " order by lexical_value");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));

                        nodeSearchMini.setConcept(true);
                        if (resultSet.getString("status").equalsIgnoreCase("DEP")) {
                            nodeSearchMini.setDeprecated(true);
                        }

                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                }
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select preferred_term.id_concept, term.id_term,"
                        + " non_preferred_term.lexical_value as npt, term.lexical_value as pt, concept.status"
                        + " from non_preferred_term, term, preferred_term, concept where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = non_preferred_term.id_term"
                        + " and"
                        + " term.lang = non_preferred_term.lang"
                        + " and"
                        + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                        + " and"
                        + " term.id_thesaurus = '" + idTheso + "'"
                        + lang
                        + " and concept.status != 'CA'"
                        + " and ("
                        + "	unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('" + value + "'))"
                        + "	or"
                        + "	unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('" + value + " %'))"
                        + "	or"
                        + "	unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('% " + value + "'))"
                        + "	or"
                        + "	unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('" + value + "-%'))"
                        + "	or"
                        + "	unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%-" + value + "'))"
                        + "	or"
                        // pour les sous_tirets pour trouver victor exp: saint_victor
                        + "	unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('" + value + "\\_%'))"
                        + "	or"
                        + "	unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%\\_" + value + "\\_%'))"
                        + "	or"
                        + "	unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%\\_" + value + "'))"
                        + "	or"
                        + "	unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%''" + value + "'))"
                        + "     or"
                        + "     unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%(" + value + ")%'))"
                        + "	)"
                        + "order by non_preferred_term.lexical_value");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setAltLabelValue(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));

                        nodeSearchMini.setAltLabel(true);
                        if (resultSet.getString("status").equalsIgnoreCase("DEP")) {
                            nodeSearchMini.setDeprecated(true);
                        }

                        if (value.trim().equalsIgnoreCase(resultSet.getString("npt"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                }
            }
            //// rechercher les collections
            nodeSearchMinis = searchCollections(conn, idTheso, value, idLang, nodeSearchMinis);

            /// rechercher les Facettes
            nodeSearchMinis = searchFacets(conn, idTheso, value, idLang, nodeSearchMinis);
        } catch (SQLException sqle) {
            log.error("Error while search excat of value  : " + value, sqle);
        }
        return nodeSearchMinis;
    }

    /**
     * Permet de chercher les terms qui commencent par un mot
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idTheso
     * @return
     */
    public ArrayList<NodeSearchMini> searchStartWith(HikariDataSource ds,
            String value, String idLang, String idTheso) {
        

        ArrayList<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        String lang;
        if (idLang == null) {
            lang = "";
        } else {
            lang = " and term.lang = '" + idLang + "'";
        }

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select preferred_term.id_concept, term.lexical_value, term.id_term, concept.status from term, preferred_term, concept where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and"
                        + " term.id_thesaurus = '" + idTheso + "'"
                        + lang
                        + " and concept.status != 'CA'"
                        + " and ("
                        + " unaccent(lower(lexical_value)) like unaccent(lower('" + value + "%'))"
                        + " or"
                        + " unaccent(lower(lexical_value)) like unaccent(lower('% " + value + "%'))"
                        + " or"
                        + " unaccent(lower(lexical_value)) like unaccent(lower('% " + value + "-%'))"
                        + " or"
                        + " unaccent(lower(lexical_value)) like unaccent(lower('%-" + value + "%'))"
                        + " or"
                        + " unaccent(lower(lexical_value)) like unaccent(lower('%(" + value + "%')) "
                        + " or"
                        + " unaccent(lower(lexical_value)) like unaccent(lower('%\\_" + value + "%'))"
                        + " or"
                        + " unaccent(lower(lexical_value)) like unaccent(lower('%''" + value + "%'))"
                        + " or"
                        + " unaccent(lower(lexical_value)) like unaccent(lower('%ʿ" + value + "%'))"
                        + " or"
                        + " unaccent(lower(lexical_value)) like unaccent(lower('%[" + value + "%')) "
                        + "	)"
                        + " order by"
                        + " CASE "
                        + " WHEN unaccent(lower(lexical_value)) ilike '" + value + "' THEN 1"
                        + " WHEN unaccent(lower(lexical_value)) ilike '" + value + " %' THEN 2"
                        + " END, unaccent(lower(lexical_value))"
                        + " limit 50");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setConcept(true);

                        if (resultSet.getString("status").equalsIgnoreCase("DEP")) {
                            nodeSearchMini.setDeprecated(true);
                        }

                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                }
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select preferred_term.id_concept, term.id_term,"
                        + " non_preferred_term.lexical_value as npt, term.lexical_value as pt, concept.status"
                        + " from non_preferred_term, term, preferred_term, concept where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = non_preferred_term.id_term"
                        + " and"
                        + " term.lang = non_preferred_term.lang"
                        + " and"
                        + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                        + " and"
                        + " term.id_thesaurus = '" + idTheso + "'"
                        + lang
                        + " and concept.status != 'CA'"
                        + " and ("
                        + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('" + value + "%'))"
                        + " or"
                        + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('% " + value + "%'))"
                        + " or"
                        + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('% " + value + "-%'))"
                        + " or"
                        + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%-" + value + "%'))"
                        + " or"
                        + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%(" + value + "%')) "
                        + " or"
                        + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%\\_" + value + "%'))"
                        + " or"
                        + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%''" + value + "%'))"
                        + " or"
                        + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%ʿ" + value + "%'))"
                        + " or"
                        + " unaccent(lower(non_preferred_term.lexical_value)) like unaccent(lower('%[" + value + "%')) "
                        + "	)"
                        + "order by non_preferred_term.lexical_value limit 50");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setAltLabelValue(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setAltLabel(true);
                        if (resultSet.getString("status").equalsIgnoreCase("DEP")) {
                            nodeSearchMini.setDeprecated(true);
                        }

                        if (value.trim().equalsIgnoreCase(resultSet.getString("npt"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                }
            }
            //// rechercher les collections
            nodeSearchMinis = searchCollections(conn, idTheso, value, idLang, nodeSearchMinis);

            /// rechercher les Facettes
            nodeSearchMinis = searchFacets(conn, idTheso, value, idLang, nodeSearchMinis);
        } catch (SQLException sqle) {
            log.error("Error while search excat of value  : " + value, sqle);
        }
        return nodeSearchMinis;
    }

    /**
     * Permet de chercher les concepts qui ont un status déprécié
     *
     * @param ds
     * @param idTheso
     * @return #MR
     */
    public ArrayList<String> searchAllDeprecatedConcepts(HikariDataSource ds, String idTheso) {
        ArrayList<String> idConcepts = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where status = 'DEP'"
                        + " and id_thesaurus = '" + idTheso + "' limit 200");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("id_concept"));
                    }
                }
            }

        } catch (SQLException sqle) {
            log.error("Error while getting deprecateed concept of theso : " + idTheso, sqle);
        }
        return idConcepts;
    }

    /**
     * Permet de chercher les concepts qui ont une poly-hiérérachie
     *
     * @param ds
     * @param idTheso
     * @return #MR
     */
    public ArrayList<String> searchAllPolyierarchy(HikariDataSource ds, String idTheso) {
        ArrayList<String> idConcepts = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1 from hierarchical_relationship where role = 'BT'"
                        + " and id_thesaurus = '" + idTheso + "'"
                        + " group by id_concept1 having count(id_concept1) > 1 limit 200;");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("id_concept1"));
                    }
                }
            }

        } catch (SQLException sqle) {
            log.error("Error while getting PolyHierarchie of theso : " + idTheso, sqle);
        }
        return idConcepts;
    }

    /**
     * Permet de chercher les concepts qui ont plusieurs Groupes
     *
     * @param ds
     * @param idTheso
     * @return #MR
     */
    public ArrayList<String> searchConceptWithMultiGroup(HikariDataSource ds, String idTheso) {
        ArrayList<String> idConcepts = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idconcept from concept_group_concept where idthesaurus = '" + idTheso + "'"
                        + " group by idconcept having count(idconcept) > 1 limit 200");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("idconcept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Multi Group of theso : " + idTheso, sqle);
        }
        return idConcepts;
    }

    /**
     * Permet de chercher les concepts qui n'ont pas de Groupes
     *
     * @param ds
     * @param idTheso
     * @return #MR
     */
    public ArrayList<String> searchConceptWithoutGroup(HikariDataSource ds, String idTheso) {
        ArrayList<String> idConcepts = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_concept from concept where concept.id_thesaurus = '" + idTheso + "'"
                        + " and concept.status != 'CA' "
                        + " and concept.id_concept not in ("
                        + " select concept_group_concept.idconcept from concept_group_concept where idthesaurus = '" + idTheso + "') limit 200");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Concept without Group of theso : " + idTheso, sqle);
        }
        return idConcepts;
    }

    /**
     * Permet de chercher les libellés qui sont en doublons
     *
     * @param ds
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public ArrayList<String> searchConceptDuplicated(HikariDataSource ds, String idTheso, String idLang) {
        ArrayList<String> conceptLabels = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lower(lexical_value) from term "
                        + " where"
                        + " id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " lang = '" + idLang + "'"
                        + " group by lower(lexical_value) having count(*) > 1 ");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        if(!conceptLabels.contains(resultSet.getString("lower")))
                            conceptLabels.add(resultSet.getString("lower"));
                    }
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lower(term.lexical_value) from term, non_preferred_term"
                        + " where"
                        + " term.id_thesaurus = non_preferred_term.id_thesaurus"
                        + " and"
                        + " term.lang = non_preferred_term.lang"
                        + " and"
                        + " lower(term.lexical_value) = lower(non_preferred_term.lexical_value)"
                        + " and"
                        + " term.id_thesaurus = '" + idTheso + "'"
                        + " and term.lang = '" + idLang + "'"
                        );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        if(!conceptLabels.contains(resultSet.getString("lower")))
                            conceptLabels.add(resultSet.getString("lower"));
                    }
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lower(lexical_value) from non_preferred_term "
                        + " where"
                        + " id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " lang = '" + idLang + "'"
                        + " group by lower(lexical_value) having count(*) > 1 ");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        if(!conceptLabels.contains(resultSet.getString("lower")))                        
                            conceptLabels.add(resultSet.getString("lower"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting dupplicated labels of theso : " + idTheso, sqle);
        }
        return conceptLabels;
    }

    /**
     * Permet de chercher les concepts qui ont RT et BT ou NT à la fois, ce qui
     * est interdit
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @return #MR
     */
    public boolean isConceptHaveRTandBT(HikariDataSource ds, String idConcept, String idTheso) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1"
                        + " from hierarchical_relationship"
                        + " where"
                        + " id_thesaurus = '" + idTheso + "'"
                        + " and id_concept1 = '" + idConcept + "'"
                        + " and (role = 'NT' or role = 'BT')"
                        + " and id_concept2 in "
                        + " (select id_concept2 "
                        + " from hierarchical_relationship"
                        + " where"
                        + " id_thesaurus = '" + idTheso + "'"
                        + " and id_concept1 = '" + idConcept + "'"
                        + " and role = 'RT')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting relationn RT and BT of theso : " + idTheso, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes exactes en
     * ignorant les accents et la casse)
     *
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idTheso
     * @return
     */
    public ArrayList<NodeSearchMini> searchExactTermForAutocompletion(HikariDataSource ds,
            String value, String idLang, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        

        ArrayList<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        String query;
        String lang;
        String langSynonyme;
        String multivaluesTerm = "";
        String multivaluesSynonyme = "";
        multivaluesTerm
                += " and f_unaccent(lower(term.lexical_value)) like"
                + " '" + value + "'";
        multivaluesSynonyme
                += " and f_unaccent(lower(non_preferred_term.lexical_value)) like"
                + " '" + value + "'";

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    
                    query = "SELECT preferred_term.id_concept, term.lexical_value, "
                            + " preferred_term.id_term"
                            + " FROM term, preferred_term WHERE "
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + multivaluesTerm
                            + " and term.id_thesaurus = '" + idTheso + "'"
                            + lang
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setAltLabel(false);
                        nodeSearchMini.setConcept(true);
                        nodeSearchMinis.add(nodeSearchMini);
                    }

                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT preferred_term.id_concept, term.id_term, "
                            + " non_preferred_term.lexical_value as npt,"
                            + " term.lexical_value as pt"
                            + " FROM"
                            + " non_preferred_term, term, preferred_term WHERE"
                            + "  preferred_term.id_term = term.id_term AND"
                            + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                            + "   preferred_term.id_term = non_preferred_term.id_term AND"
                            + "   term.lang = non_preferred_term.lang AND"
                            + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                            + multivaluesSynonyme
                            + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                            + langSynonyme
                            + " order by non_preferred_term.lexical_value ASC LIMIT 200";
                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setAltLabelValue(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setAltLabel(true);
                        nodeSearchMinis.add(nodeSearchMini);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchMinis;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * mais en ajoutant uniquement les mots commencant par.Ca sert à afficher
     * les index par ordre alphabétique
     *
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param permuted
     * @param synonym
     * @return
     */
    public ArrayList<NodeIdValue> searchTermForIndex(HikariDataSource ds,
            String value, String idLang, String idThesaurus,
            boolean permuted, boolean synonym) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        String query;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    // recherche avec les synonymes
                    if (synonym) {
                        query = "SELECT preferred_term.id_concept, non_preferred_term.lexical_value"
                                + " FROM non_preferred_term, preferred_term, concept WHERE "
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and"
                                + " preferred_term.id_term = non_preferred_term.id_term AND"
                                + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                + " and"
                                + " f_unaccent(lower(non_preferred_term.lexical_value)) like '" + value + "%'"
                                + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                                + " and non_preferred_term.lang ='" + idLang + "'"
                                + " and concept.status != 'CA'"
                                + " order by lexical_value ASC LIMIT 200";

                        resultSet = stmt.executeQuery(query);
                        while (resultSet.next()) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(resultSet.getString("id_concept"));
                            nodeIdValue.setValue(resultSet.getString("lexical_value"));
                            nodeIdValues.add(nodeIdValue);
                        }
                        if (permuted) {
                            query = "SELECT preferred_term.id_concept, non_preferred_term.lexical_value"
                                    + " FROM non_preferred_term, preferred_term, concept WHERE "
                                    + " concept.id_concept = preferred_term.id_concept"
                                    + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                    + " and"
                                    + " preferred_term.id_term = non_preferred_term.id_term AND"
                                    + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                    + " and"
                                    + " f_unaccent(lower(non_preferred_term.lexical_value)) like '% " + value + "%'"
                                    + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                                    + " and non_preferred_term.lang ='" + idLang + "'"
                                    + " and concept.status != 'CA'"
                                    + " order by lexical_value ASC LIMIT 200";

                            resultSet = stmt.executeQuery(query);
                            while (resultSet.next()) {
                                NodeIdValue nodeIdValue = new NodeIdValue();
                                nodeIdValue.setId(resultSet.getString("id_concept"));
                                nodeIdValue.setValue(resultSet.getString("lexical_value"));
                                nodeIdValues.add(nodeIdValue);
                            }
                        }
                    } else {
                        // sans les synonymes
                        query = "SELECT preferred_term.id_concept, term.lexical_value"
                                + " FROM term, preferred_term, concept WHERE "
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and"
                                + " preferred_term.id_term = term.id_term AND"
                                + " preferred_term.id_thesaurus = term.id_thesaurus"
                                + " and"
                                + " f_unaccent(lower(term.lexical_value)) like '" + value + "%'"
                                + " and term.id_thesaurus = '" + idThesaurus + "'"
                                + " and term.lang ='" + idLang + "'"
                                + " and concept.status != 'CA'"
                                + " order by lexical_value ASC LIMIT 200";

                        resultSet = stmt.executeQuery(query);
                        while (resultSet.next()) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(resultSet.getString("id_concept"));
                            nodeIdValue.setValue(resultSet.getString("lexical_value"));
                            nodeIdValues.add(nodeIdValue);
                        }
                        if (permuted) {
                            query = "SELECT preferred_term.id_concept, term.lexical_value"
                                    + " FROM term, preferred_term, concept WHERE "
                                    + " concept.id_concept = preferred_term.id_concept"
                                    + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                    + " and"
                                    + " preferred_term.id_term = term.id_term AND"
                                    + " preferred_term.id_thesaurus = term.id_thesaurus"
                                    + " and"
                                    + " f_unaccent(lower(term.lexical_value)) like '% " + value + "%'"
                                    + " and term.id_thesaurus = '" + idThesaurus + "'"
                                    + " and term.lang ='" + idLang + "'"
                                    + " and concept.status != 'CA'"
                                    + " order by lexical_value ASC LIMIT 200";

                            resultSet = stmt.executeQuery(query);
                            while (resultSet.next()) {
                                NodeIdValue nodeIdValue = new NodeIdValue();
                                nodeIdValue.setId(resultSet.getString("id_concept"));
                                nodeIdValue.setValue(resultSet.getString("lexical_value"));
                                nodeIdValues.add(nodeIdValue);
                            }
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdValues;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les notes et
     * retourner les Ids des concepts
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return #MR
     */
    public ArrayList<String> searchIdConceptFromNotes(HikariDataSource ds,
            String value, String idLang, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        
        ArrayList<String> tabIdConcepts = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        String multiValues = "";
        String values[] = value.trim().split(" ");
        for (String value1 : values) {
            multiValues += " and (f_unaccent(lower(note.lexicalvalue)) like '%" + value1 + "%')";
        }
        String lang;
        if (idLang == null || idLang.isEmpty()) {
            lang = "";
        } else {
            lang = " and note.lang ='" + idLang + "'";
        }

        String query;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    // notes de type terme
                    query = "SELECT distinct preferred_term.id_concept"
                            + " FROM note, preferred_term, concept WHERE"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_term = note.id_term"
                            + " and"
                            + " preferred_term.id_thesaurus = note.id_thesaurus"
                            + multiValues
                            + //" and" +
                            //" f_unaccent(lower(note.lexicalvalue)) like '%" + value + "%'" +
                            " and note.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + " and concept.status != 'CA' limit 50";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        tabIdConcepts.add(resultSet.getString("id_concept"));
                    }
                    // notes des concepts
                    query = "SELECT distinct preferred_term.id_concept"
                            + " FROM note, preferred_term, concept WHERE"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_concept = note.id_concept"
                            + " and"
                            + " preferred_term.id_thesaurus = note.id_thesaurus"
                            + //" and" +
                            //" f_unaccent(lower(note.lexicalvalue)) % '" + value + "'" +
                            multiValues
                            + " and note.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + " and concept.status != 'CA' limit 50";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        tabIdConcepts.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tabIdConcepts;
    }

    /**
     * Cette fonction permet de faire une recherche sur les identifiants
     * (idConcept, idHandle, IdArk)
     *
     * @param ds
     * @param value
     * @param idTheso
     * @return #MR
     */
    public ArrayList<String> searchForIds(HikariDataSource ds, String value, String idTheso) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> idConcepts = null;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT concept.id_concept"
                            + " FROM concept WHERE "
                            + " concept.id_thesaurus = '" + idTheso + "'"
                            + " and status != 'CA'"
                            + " and ("
                            + " lower(concept.id_concept) = lower('" + value + "')"
                            + " or lower(concept.id_ark) = lower('" + value + "')"
                            + " or lower(concept.id_handle) = lower('" + value + "')"
                            + " or lower(concept.notation) = lower('" + value + "')"
                            + ")";

                    resultSet = stmt.executeQuery(query);
                    idConcepts = new ArrayList<>();
                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idConcepts;
    }

    /**
     * Cette fonction permet de faire une recherche par valeur sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) en utilisant la méthode PostgreSQL Trigram Index, le
     * résultat est proche d'une recherche avec ElasticSearch
     *
     * Elle retourne la liste des termes + identifiants
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return #MR
     */
    public ArrayList<NodeSearchMini> searchFullTextElastic(HikariDataSource ds, String value, String idLang, String idThesaurus) {
        if (value == null) {
            return null;
        }
        

        ArrayList<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);

        String preparedValuePT = " and unaccent(lower(term.lexical_value)) % (unaccent(lower('" + value + "')))";
        String preparedValueNPT = " and unaccent(lower(non_preferred_term.lexical_value)) % (unaccent(lower('" + value + "')))";

        String lang;
        String langSynonyme;
        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang == null || idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT preferred_term.id_concept, term.lexical_value, term.id_term, concept.status "
                        + " FROM term, preferred_term, concept "
                        + " WHERE "
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and preferred_term.id_term = term.id_term AND"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + preparedValuePT
                        + " and term.id_thesaurus = '" + idThesaurus + "'"
                        + lang
                        + " and concept.status != 'CA'"
                        + " order by term.lexical_value <-> '" + value + "' limit 50");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setConcept(true);
                        if (resultSet.getString("status").equalsIgnoreCase("DEP")) {
                            nodeSearchMini.setDeprecated(true);
                        }

                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT preferred_term.id_concept, term.id_term, "
                        + " non_preferred_term.lexical_value as npt,"
                        + " term.lexical_value as pt, concept.status"
                        + " FROM"
                        + " non_preferred_term, term, preferred_term, concept"
                        + " WHERE"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + "  preferred_term.id_term = term.id_term AND"
                        + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                        + "   preferred_term.id_term = non_preferred_term.id_term AND"
                        + "   term.lang = non_preferred_term.lang AND"
                        + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                        + preparedValueNPT
                        + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                        + langSynonyme
                        + " and concept.status != 'CA'"
                        + " order by non_preferred_term.lexical_value <-> '" + value + "' limit 50");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setAltLabelValue(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));

                        nodeSearchMini.setAltLabel(true);
                        if (resultSet.getString("status").equalsIgnoreCase("DEP")) {
                            nodeSearchMini.setDeprecated(true);
                        }

                        if (value.trim().equalsIgnoreCase(resultSet.getString("npt"))) {
                            if (nodeSearchMinis.isEmpty()) {
                                nodeSearchMinis.add(0, nodeSearchMini);
                            } else {
                                if (nodeSearchMinis.get(0).getPrefLabel().equalsIgnoreCase(value.trim())) {
                                    nodeSearchMinis.add(1, nodeSearchMini);
                                } else {
                                    nodeSearchMinis.add(0, nodeSearchMini);
                                }
                            }
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                }
            }
            //// rechercher les collections
            nodeSearchMinis = searchCollections(conn, idThesaurus, value, idLang, nodeSearchMinis);

            /// rechercher les Facettes
            nodeSearchMinis = searchFacets(conn, idThesaurus, value, idLang, nodeSearchMinis);

        } catch (SQLException sqle) {
            log.error("Error searchFullTextElastic of theso : " + idThesaurus, sqle);
        }
        return nodeSearchMinis;
    }

    /**
     * Cette fonction permet de faire une recherche par valeur sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) en utilisant la méthode PostgreSQL Trigram Index, le
     * résultat est proche d'une recherche avec ElasticSearch
     *
     * Elle retourne la liste des termes + identifiants
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return #MR
     */
    public ArrayList<String> searchFullTextElasticId(HikariDataSource ds,
            String value, String idLang, String idThesaurus) {
        if (value == null) {
            return null;
        }
        

        ArrayList<String> listIds = new ArrayList<>();
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);

        String preparedValuePT = " and unaccent(lower(term.lexical_value)) % (unaccent(lower('" + value + "')))";
        String preparedValueNPT = " and unaccent(lower(non_preferred_term.lexical_value)) % (unaccent(lower('" + value + "')))";

        String lang;
        String langSynonyme;
        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang == null || idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT preferred_term.id_concept, term.lexical_value, term.id_term, concept.status "
                        + " FROM term, preferred_term, concept "
                        + " WHERE "
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and preferred_term.id_term = term.id_term AND"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + preparedValuePT
                        + " and term.id_thesaurus = '" + idThesaurus + "'"
                        + lang
                        + " and concept.status != 'CA'"
                        + " order by term.lexical_value <-> '" + value + "' limit 50");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        if (!listIds.contains(resultSet.getString("id_concept"))) {
                            listIds.add(resultSet.getString("id_concept"));
                        }
                    }
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT preferred_term.id_concept, term.id_term, "
                        + " non_preferred_term.lexical_value as npt,"
                        + " term.lexical_value as pt, concept.status"
                        + " FROM"
                        + " non_preferred_term, term, preferred_term, concept"
                        + " WHERE"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + "  preferred_term.id_term = term.id_term AND"
                        + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                        + "   preferred_term.id_term = non_preferred_term.id_term AND"
                        + "   term.lang = non_preferred_term.lang AND"
                        + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                        + preparedValueNPT
                        + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                        + langSynonyme
                        + " and concept.status != 'CA'"
                        + " order by non_preferred_term.lexical_value <-> '" + value + "' limit 50");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        if (!listIds.contains(resultSet.getString("id_concept"))) {
                            listIds.add(resultSet.getString("id_concept"));
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error searchFullTextElastic of theso : " + idThesaurus, sqle);
        }
        return listIds;
    }

    private ArrayList<NodeSearchMini> searchCollections(Connection conn,
            String idTheso, String value, String idLang,
            ArrayList<NodeSearchMini> nodeSearchMinis) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select idGroup, lexicalvalue from concept_group_label"
                    + " where idthesaurus = '" + idTheso + "'"
                    + " and ("
                    + " unaccent(lower(lexicalvalue)) like unaccent(lower('" + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexicalvalue)) like unaccent(lower('% " + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexicalvalue)) like unaccent(lower('% " + value + "-%'))"
                    + " or"
                    + " unaccent(lower(lexicalvalue)) like unaccent(lower('%-" + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexicalvalue)) like unaccent(lower('%(" + value + "%')) "
                    + " or"
                    + " unaccent(lower(lexicalvalue)) like unaccent(lower('%\\_" + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexicalvalue)) like unaccent(lower('%''" + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexicalvalue)) like unaccent(lower('%ʿ" + value + "%'))"
                    + "	)"
                    + " and lang = '" + idLang + "'");

            try (ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    NodeSearchMini nodeSearchMini = new NodeSearchMini();
                    nodeSearchMini.setIdConcept(resultSet.getString("idGroup"));
                    nodeSearchMini.setIdTerm("");
                    nodeSearchMini.setAltLabelValue("");
                    nodeSearchMini.setPrefLabel(resultSet.getString("lexicalvalue"));
                    nodeSearchMini.setGroup(true);
                    nodeSearchMinis.add(nodeSearchMini);
                }
            }
        }
        return nodeSearchMinis;
    }

    private ArrayList<NodeSearchMini> searchFacets(Connection conn,
            String idTheso, String value, String idLang,
            ArrayList<NodeSearchMini> nodeSearchMinis) throws SQLException {
        /// rechercher les Facettes
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_facet, lexical_value from node_label"
                    + " where id_thesaurus = '" + idTheso + "'"
                    + " and ("
                    + " unaccent(lower(lexical_value)) like unaccent(lower('" + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexical_value)) like unaccent(lower('% " + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexical_value)) like unaccent(lower('% " + value + "-%'))"
                    + " or"
                    + " unaccent(lower(lexical_value)) like unaccent(lower('%-" + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexical_value)) like unaccent(lower('%(" + value + "%')) "
                    + " or"
                    + " unaccent(lower(lexical_value)) like unaccent(lower('%\\_" + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexical_value)) like unaccent(lower('%''" + value + "%'))"
                    + " or"
                    + " unaccent(lower(lexical_value)) like unaccent(lower('%ʿ" + value + "%'))"
                    + "	)"
                    + " and lang = '" + idLang + "'");

            try (ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    NodeSearchMini nodeSearchMini = new NodeSearchMini();
                    nodeSearchMini.setIdConcept(resultSet.getString("id_facet"));
                    nodeSearchMini.setIdTerm("");
                    nodeSearchMini.setAltLabelValue("");
                    nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                    nodeSearchMini.setFacet(true);
                    nodeSearchMinis.add(nodeSearchMini);
                }
            }
        }
        return nodeSearchMinis;
    }

    /**
     * Cette fonction permet de récupérer une liste de termes pour
     * l'autocomplétion pour créer des relations entre les concepts
     *
     * @param ds
     * @param value
     * @param idTheso
     * @param idLang
     * @param includeDeprecated
     * @return #MR
     */
    public List<NodeSearchMini> searchAutoCompletionForRelation(
            HikariDataSource ds,
            String value,
            String idLang,
            String idTheso,
            boolean includeDeprecated) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        

        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        String status;
        if(includeDeprecated) {
            status = " and concept.status != 'CA'";
            
        } else {
            status = " and concept.status not in ('CA', 'DEP')";
        }
        
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + " term.lexical_value,"
                            + " preferred_term.id_concept"
                            + " FROM"
                            + " concept, preferred_term, term"
                            + " WHERE"
                            + " preferred_term.id_concept = concept.id_concept "
                            + " AND  preferred_term.id_thesaurus = concept.id_thesaurus "
                            + " AND  term.id_term = preferred_term.id_term "
                            + " AND  term.id_thesaurus = preferred_term.id_thesaurus "
                            + " AND  concept.status != 'hidden' "
                            + " AND term.lang = '" + idLang + "'"
                            + status
                            + " AND term.id_thesaurus = '" + idTheso + "'"
                            + " AND f_unaccent(lower(term.lexical_value)) LIKE '%" + value + "%' order by term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();

                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setAltLabel(false);
                        nodeSearchMinis.add(nodeSearchMini);
                    }
                    query = "SELECT "
                            + "  non_preferred_term.lexical_value as npt,"
                            + "  term.lexical_value as pt,"
                            + "  preferred_term.id_concept"
                            + " FROM "
                            + "  concept, "
                            + "  preferred_term, "
                            + "  non_preferred_term, "
                            + "  term "
                            + " WHERE "
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  non_preferred_term.id_term = preferred_term.id_term AND"
                            + "  non_preferred_term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.id_term = preferred_term.id_term AND"
                            + "  term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.lang = non_preferred_term.lang AND"
                            + "  concept.status != 'hidden' AND"
                            + "  non_preferred_term.id_thesaurus = '" + idTheso + "' AND"
                            + "  non_preferred_term.lang = '" + idLang + "'"
                            + status
                            + " AND"
                            + " f_unaccent(lower(non_preferred_term.lexical_value)) LIKE '%" + value + "%'"
                            + " order by non_preferred_term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();

                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setAltLabelValue(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setAltLabel(true);
                        nodeSearchMinis.add(nodeSearchMini);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of autocompletion of Text : " + value, sqle);
        }

        return nodeSearchMinis;
    }    
    
    /**
     * Cette fonction permet de récupérer une liste de termes pour
     * l'autocomplétion pour créer des relations entre les concepts
     *
     * @param ds
     * @param value
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public List<NodeSearchMini> searchAutoCompletionForCustomRelation(
            HikariDataSource ds,
            String value,
            String idLang,
            String idTheso) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        

        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + " term.lexical_value,"
                            + " preferred_term.id_concept, concept.concept_type"
                            + " FROM"
                            + " concept, preferred_term, term"
                            + " WHERE"
                            + " preferred_term.id_concept = concept.id_concept "
                            + " AND  preferred_term.id_thesaurus = concept.id_thesaurus "
                            + " AND  term.id_term = preferred_term.id_term "
                            + " AND  term.id_thesaurus = preferred_term.id_thesaurus "
                            + " AND  concept.status != 'hidden' "
                            + " AND term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"
                            + " and (concept.concept_type != 'concept' and concept.concept_type != '')"
                            + " AND term.id_thesaurus = '" + idTheso + "'"
                            + " AND f_unaccent(lower(term.lexical_value)) LIKE '%" + value + "%' order by term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();

                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setConceptType(resultSet.getString("concept_type"));
                        nodeSearchMini.setAltLabel(false);
                        nodeSearchMinis.add(nodeSearchMini);
                    }
                    query = "SELECT "
                            + "  non_preferred_term.lexical_value as npt,"
                            + "  term.lexical_value as pt,"
                            + "  preferred_term.id_concept,"
                            + "  concept.concept_type"
                            + " FROM "
                            + "  concept, "
                            + "  preferred_term, "
                            + "  non_preferred_term, "
                            + "  term "
                            + " WHERE "
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  non_preferred_term.id_term = preferred_term.id_term AND"
                            + "  non_preferred_term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.id_term = preferred_term.id_term AND"
                            + "  term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.lang = non_preferred_term.lang AND"
                            + "  concept.status != 'hidden' AND"
                            + "  non_preferred_term.id_thesaurus = '" + idTheso + "' AND"
                            + "  non_preferred_term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"
                            + " and (concept.concept_type != 'concept' and concept.concept_type != '')"
                            + " AND"
                            + " f_unaccent(lower(non_preferred_term.lexical_value)) LIKE '%" + value + "%'"
                            + " order by non_preferred_term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();

                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setAltLabelValue(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setConceptType(resultSet.getString("concept_type"));                        
                        nodeSearchMini.setAltLabel(true);
                        nodeSearchMinis.add(nodeSearchMini);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of autocompletion of Text : " + value, sqle);
        }

        return nodeSearchMinis;
    }

    /**
     * Cette fonction permet de récupérer une liste de termes pour
     * l'autocomplétion pour créer des relations entre les concepts
     *
     * @param ds
     * @param value
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public ArrayList<NodeIdValue> searchAutoCompletionForRelationIdValue(
            HikariDataSource ds,
            String value,
            String idLang,
            String idTheso) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        

        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + " term.lexical_value,"
                            + " preferred_term.id_concept"
                            + " FROM"
                            + " concept, preferred_term, term"
                            + " WHERE"
                            + " preferred_term.id_concept = concept.id_concept "
                            + " AND  preferred_term.id_thesaurus = concept.id_thesaurus "
                            + " AND  term.id_term = preferred_term.id_term "
                            + " AND  term.id_thesaurus = preferred_term.id_thesaurus "
                            + " AND  concept.status != 'hidden' "
                            + " AND term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"
                            + " AND term.id_thesaurus = '" + idTheso + "'"
                            + " AND f_unaccent(lower(term.lexical_value)) LIKE '%" + value + "%' order by term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();

                        nodeIdValue.setId(resultSet.getString("id_concept"));
                        nodeIdValue.setValue(resultSet.getString("lexical_value"));
                        nodeIdValues.add(nodeIdValue);
                    }
                    query = "SELECT "
                            + "  non_preferred_term.lexical_value as npt,"
                            + "  term.lexical_value as pt,"
                            + "  preferred_term.id_concept"
                            + " FROM "
                            + "  concept, "
                            + "  preferred_term, "
                            + "  non_preferred_term, "
                            + "  term "
                            + " WHERE "
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  non_preferred_term.id_term = preferred_term.id_term AND"
                            + "  non_preferred_term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.id_term = preferred_term.id_term AND"
                            + "  term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.lang = non_preferred_term.lang AND"
                            + "  concept.status != 'hidden' AND"
                            + "  non_preferred_term.id_thesaurus = '" + idTheso + "' AND"
                            + "  non_preferred_term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"
                            + " AND"
                            + " f_unaccent(lower(non_preferred_term.lexical_value)) LIKE '%" + value + "%'"
                            + " order by non_preferred_term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();

                        nodeIdValue.setId(resultSet.getString("id_concept"));
                        nodeIdValue.setValue(resultSet.getString("npt") + " ->" + resultSet.getString("pt"));
                        nodeIdValues.add(nodeIdValue);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of autocompletion of Text : " + value, sqle);
        }

        return nodeIdValues;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) exp : la recherche de "ceramiqu four" trouve la chaine
     * (four à céramique)
     */
    public ArrayList<NodeSearch> searchTermNew(HikariDataSource ds, String value, String idLang, String idThesaurus, String idGroup, boolean withNote) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        

        ArrayList<NodeSearch> nodeSearchList = null;
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        value = fr.cnrs.opentheso.utils.StringUtils.unaccentLowerString(value);

        String values[] = value.trim().split(" ");

        String query;
        String lang;
        String langSynonyme;
        String langNote;
        String group;
        String multivaluesTerm = "";
        String multivaluesSynonyme = "";
        String multivaluesNote = "";
        String notation = "";

        for (String value1 : values) {
            multivaluesTerm
                    += " and f_unaccent(lower(term.lexical_value)) like"
                    + " '%" + value1 + "%'";
            multivaluesSynonyme
                    += " and f_unaccent(lower(non_preferred_term.lexical_value)) like"
                    + " '%" + value1 + "%'";
            multivaluesNote
                    += " and f_unaccent(lower(note.lexicalvalue)) like"
                    + " '%" + value1 + "%'";
            notation
                    = " and f_unaccent(lower(concept.notation)) like "
                    + " '%" + value + "%'";
        }

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
            langNote = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
            langNote = " and note.lang ='" + idLang + "'";
        }

        // cas du choix d'un group
        if (idGroup.isEmpty()) {
            group = "";
        } else {
            group = " and idgroup = '" + idGroup + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT term.lexical_value, preferred_term.id_concept,"
                            + " preferred_term.id_term, term.lang, term.id_thesaurus,"
                            + " idgroup, concept.top_concept"
                            + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                            + "concept_group_concept.idthesaurus  = term.id_thesaurus AND "
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + multivaluesTerm
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(resultSet.getString("lang"));
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexicalValue().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }

                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT non_preferred_term.id_term, non_preferred_term.lang,"
                            + " non_preferred_term.lexical_value, "
                            + " idgroup, preferred_term.id_concept,"
                            + " concept.top_concept"
                            + " FROM non_preferred_term, preferred_term,concept_group_concept, concept WHERE "
                            + "  concept.id_concept = concept_group_concept.idconcept AND"
                            + "  concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + "  preferred_term.id_term = non_preferred_term.id_term AND"
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus "
                            + multivaluesSynonyme
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexicalValue(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(resultSet.getString("lang"));
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(false);

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexicalValue().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }

                    /**
                     * recherche aussi dans les notes
                     */
                    if (withNote) {
                        query = "SELECT \n"
                                + "  note.lang, \n"
                                + "  note.lexicalvalue, \n"
                                + " note.id_term,"
                                + "  concept.top_concept, \n"
                                + "  concept.id_concept, \n"
                                + "  concept_group_concept.idgroup\n"
                                + " FROM \n"
                                + "  public.note, \n"
                                + "  public.preferred_term, \n"
                                + "  public.concept, \n"
                                + "  public.concept_group_concept\n"
                                + " WHERE \n"
                                + "  preferred_term.id_term = note.id_term AND\n"
                                + "  preferred_term.id_thesaurus = note.id_thesaurus AND\n"
                                + "  concept.id_concept = preferred_term.id_concept AND\n"
                                + "  concept.id_thesaurus = preferred_term.id_thesaurus AND\n"
                                + "  concept_group_concept.idconcept = concept.id_concept AND\n"
                                + "  concept_group_concept.idthesaurus = concept.id_thesaurus AND\n"
                                + "  note.id_thesaurus = '" + idThesaurus + "' "
                                + multivaluesNote
                                + langNote
                                + group
                                + " ORDER BY\n"
                                + "  note.lexicalvalue ASC;";

                        resultSet = stmt.executeQuery(query);

                        while (resultSet.next()) {
                            NodeSearch nodeSearch = new NodeSearch();
                            nodeSearch.setLexicalValue(resultSet.getString("lexicalvalue"));
                            nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                            nodeSearch.setIdTerm(resultSet.getString("id_term"));
                            nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                            nodeSearch.setIdLang(resultSet.getString("lang"));
                            nodeSearch.setIdThesaurus(idThesaurus);
                            nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                            nodeSearch.setPreferredLabel(true);

                            nodeSearchList.add(nodeSearch);
                        }
                    }
                    /**
                     * recherche aussi dans les notations
                     */
                    if (withNote) {
                        query = "SELECT concept.id_concept, concept.id_thesaurus,"
                                + " concept.top_concept, idgroup,"
                                + " concept.notation "
                                + " FROM concept JOIN concept_group_concept ON concept.id_concept = concept_group_concept.idconcept "
                                + "AND concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " WHERE"
                                + " concept.id_thesaurus = '" + idThesaurus + "'"
                                + notation
                                + group
                                + " order by notation ASC LIMIT 200";

                        resultSet = stmt.executeQuery(query);

                        while (resultSet.next()) {
                            NodeSearch nodeSearch = new NodeSearch();
                            nodeSearch.setLexicalValue(resultSet.getString("notation"));
                            nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                            nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                            nodeSearch.setIdLang(idLang);
                            nodeSearch.setIdThesaurus(idThesaurus);
                            nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                            nodeSearch.setPreferredLabel(true);

                            nodeSearchList.add(nodeSearch);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par notation comme c'est une
     * valeur unique, on peut cherche sur tout le thésaurus sans filter par
     * groupe
     *
     * @param ds
     * @param value
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> searchNotationId(HikariDataSource ds,
            String value, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> ListIdConcept = null;
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        String query;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "select id_concept from concept where notation ilike '" + value + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";

                    resultSet = stmt.executeQuery(query);
                    ListIdConcept = new ArrayList();
                    while (resultSet.next()) {
                        ListIdConcept.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ListIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la valeur d'origine du concept
     * réorganisé dans le bon ordre pour construire un tableau de 3 colonnes : 1
     * - le premier est le contenu avant la valeur recherchée 2 - le deuxième
     * contient le mot recherché 3 - le troisième contient le reste de la valeur
     * exp : saint clair du rhone si on cherche (clair) 1 = saint 2 = clair 3 =
     * du rhone
     *
     * @param ds
     * @param idThesaurus
     * @param idConcept
     * @param idLang
     * @param isPreferredTerm
     * @return ArrayList de String (les valeurs dans l'ordre)
     */
    public ArrayList<String> getThisConceptPermute(HikariDataSource ds,
            String idThesaurus, String idConcept, String idLang, boolean isPreferredTerm) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabValues = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT ord, lexical_value"
                            + " FROM permuted WHERE"
                            + " id_lang = '" + idLang + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'"
                            + " and ispreferredterm = " + isPreferredTerm
                            + " order by ord ASC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    tabValues = new ArrayList<>();
                    while (resultSet.next()) {
                        tabValues.add(resultSet.getString("lexical_value"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting the value Permute of Concept : " + idConcept, sqle);
        }
        return tabValues;
    }
}
