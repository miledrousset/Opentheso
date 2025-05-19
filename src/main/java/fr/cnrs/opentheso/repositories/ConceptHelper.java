package fr.cnrs.opentheso.repositories;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnrs.opentheso.entites.ConceptGroup;
import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.entites.Preferences;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.candidats.MessageDto;
import fr.cnrs.opentheso.models.candidats.VoteDto;
import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.concept.ConceptIdLabel;
import fr.cnrs.opentheso.models.concept.ConceptImage;
import fr.cnrs.opentheso.models.concept.ConceptLabel;
import fr.cnrs.opentheso.models.concept.ConceptNote;
import fr.cnrs.opentheso.models.concept.ConceptRelation;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.concept.NodeConceptExport;
import fr.cnrs.opentheso.models.concept.NodeConceptTree;
import fr.cnrs.opentheso.models.concept.NodeConceptType;
import fr.cnrs.opentheso.models.concept.NodeFullConcept;
import fr.cnrs.opentheso.models.concept.NodeMetaData;
import fr.cnrs.opentheso.models.concept.NodeUri;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.models.nodes.NodeGps;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.nodes.NodeTree;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;
import fr.cnrs.opentheso.models.relations.NodeDeprecated;
import fr.cnrs.opentheso.models.relations.NodeHieraRelation;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.terms.NodeNT;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.models.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.bean.importexport.outils.HTMLLinkElement;
import fr.cnrs.opentheso.bean.importexport.outils.HtmlLinkExtraction;
import fr.cnrs.opentheso.services.AlignmentService;
import fr.cnrs.opentheso.services.DeprecateService;
import fr.cnrs.opentheso.services.GpsService;
import fr.cnrs.opentheso.services.GroupService;
import fr.cnrs.opentheso.services.ImageService;
import fr.cnrs.opentheso.services.NonPreferredTermService;
import fr.cnrs.opentheso.services.PreferenceService;
import fr.cnrs.opentheso.services.RelationGroupService;
import fr.cnrs.opentheso.services.ResourceService;
import fr.cnrs.opentheso.services.TermService;
import fr.cnrs.opentheso.services.ThesaurusService;
import fr.cnrs.opentheso.utils.NoIdCheckDigit;
import fr.cnrs.opentheso.ws.api.NodeDatas;
import fr.cnrs.opentheso.ws.ark.ArkHelper2;
import fr.cnrs.opentheso.ws.handle.HandleHelper;
import fr.cnrs.opentheso.ws.handlestandard.HandleService;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import javax.sql.DataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Slf4j
@Service
public class ConceptHelper implements Serializable {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ImageService imageService;

    @Autowired
    private RelationsHelper relationsHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HandleHelper handleHelper;

    @Autowired
    private NoteHelper noteHelper;

    @Autowired
    private DeprecateService deprecateHelper;

    @Autowired
    private ConceptDcTermRepository conceptDcTermRepository;

    @Autowired
    private ExternalResourcesRepository externalResourcesRepository;

    @Autowired
    private GpsService gpsService;

    @Autowired
    private FacetHelper facetHelper;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private CandidatMessageRepository candidatMessageRepository;

    @Autowired
    private CandidatVoteRepository candidatVoteRepository;

    @Autowired
    private NonPreferredTermRepository nonPreferredTermRepository;

    //identifierType  1=numericId ; 2=alphaNumericId
    private Preferences nodePreference;
    private String message = "";
    @Autowired
    private HandleService handleService;
    @Autowired
    private PreferenceService preferenceService;
    @Autowired
    private TermService termService;
    @Autowired
    private NonPreferredTermService nonPreferredTermService;

    @Autowired
    private AlignmentService alignmentService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private RelationGroupService relationGroupService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ThesaurusService thesaurusService;


    /**
     * permet de changer les valeurs d'un type de concept dans la table
     * ConceptType
     */
    public boolean applyChangeForConceptType(String idThesaurus, NodeConceptType nodeConceptType) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept_type set "
                        + " label_fr = '" + nodeConceptType.getLabelFr() + "',"
                        + " label_en = '" + nodeConceptType.getLabelEn() + "',"
                        + " reciprocal = " + nodeConceptType.isReciprocal() + ","
                        + " id_theso = '" + idThesaurus + "'"
                        + " WHERE code ='" + nodeConceptType.getCode() + "'"
                        + " AND id_theso ='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating type of concept : " + nodeConceptType.getCode(), sqle);
        }
        return false;
    }

    /**
     * permet de déplacer le concept vers un autre thésaurus
     */
    public boolean moveConceptToAnotherTheso(String idConceptToMove, String idThesoFrom, String idThesoTarget) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        " update concept set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update thesaurus_array set id_thesaurus = '" + idThesoTarget + "' where id_concept_parent = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update thesaurus_array set id_thesaurus = '" + idThesoTarget + "' where id_concept_parent = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update concept_historique set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update term set id_thesaurus = '" + idThesoTarget + "' from preferred_term where term.id_term = preferred_term.id_term"
                                + " and term.id_thesaurus = preferred_term.id_thesaurus and preferred_term.id_concept = '" + idConceptToMove + "' and term.id_thesaurus = '" + idThesoFrom + "';"
                                + " update non_preferred_term set id_thesaurus = '" + idThesoTarget + "' from preferred_term where non_preferred_term.id_term = preferred_term.id_term"
                                + " and non_preferred_term.id_thesaurus = preferred_term.id_thesaurus and preferred_term.id_concept = '" + idConceptToMove + "' and non_preferred_term.id_thesaurus = '" + idThesoFrom + "';"
                                + " update note set id_thesaurus = '" + idThesoTarget + "' from preferred_term where note.id_term = preferred_term.id_term"
                                + " and note.id_thesaurus = preferred_term.id_thesaurus and preferred_term.id_concept = '" + idConceptToMove + "' and note.id_thesaurus = '" + idThesoFrom + "';"
                                + " update note set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update note_historique set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update preferred_term set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update concept_candidat set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update candidat_status set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update candidat_messages set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update candidat_vote set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update concept_group_concept set idthesaurus = '" + idThesoTarget + "' where idconcept = '" + idConceptToMove + "' and idthesaurus = '" + idThesoFrom + "';"
                                + " update hierarchical_relationship set id_thesaurus = '" + idThesoTarget + "' where id_concept1 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update hierarchical_relationship set id_thesaurus = '" + idThesoTarget + "' where id_concept2 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update hierarchical_relationship_historique set id_thesaurus = '" + idThesoTarget + "' where id_concept1 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update hierarchical_relationship_historique set id_thesaurus = '" + idThesoTarget + "' where id_concept2 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update concept_term_candidat set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update alignement set internal_id_thesaurus = '" + idThesoTarget + "' where internal_id_concept = '" + idConceptToMove + "' and internal_id_thesaurus = '" + idThesoFrom + "';"
                                + " update proposition set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update concept_replacedby set id_thesaurus = '" + idThesoTarget + "' where id_concept1 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update concept_replacedby set id_thesaurus = '" + idThesoTarget + "' where id_concept2 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update gps set id_theso = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_theso = '" + idThesoFrom + "';"
                                + " update concept_facet set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update external_resources set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update external_images set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update proposition set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                                + " update concept_dcterms set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                );
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while moving concept : " + idThesoTarget, sqle);
        }
        return false;
    }

    /**
     * permet de supprimer un type de concept
     */
    public boolean deleteConceptType(String idThesaurus, NodeConceptType nodeConceptType) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from concept_type where "
                        + " code = '" + nodeConceptType.getCode() + "'"
                        + " AND id_theso ='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting type of concept : " + nodeConceptType.getCode(), sqle);
        }
        return false;
    }

    /**
     * Permet d'ajouter un nouveau type de concept
     *
     * @param idThesaurus
     * @param nodeConceptType
     * @return
     */
    public boolean addNewConceptType(
                                     String idThesaurus, NodeConceptType nodeConceptType) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("insert into concept_type (code, label_fr, label_en, reciprocal, id_theso) values ("
                        + "'" + nodeConceptType.getCode() + "',"
                        + "'" + nodeConceptType.getLabelFr() + "',"
                        + "'" + nodeConceptType.getLabelEn() + "',"
                        + nodeConceptType.isReciprocal() + ","
                        + "'" + idThesaurus + "'"
                        + ")");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding type of concept : " + nodeConceptType.getCode(), sqle);
        }
        return false;
    }

    public boolean isConceptTypeExist(String idTheso, NodeConceptType nodeConceptType) {
        boolean existe = false;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select code from concept_type where " + "code = '" + nodeConceptType.getCode() + "'"
                        + " and id_theso = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if concept_type exist : " + nodeConceptType.getCode(), sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de changer le type du concept
     *
     * @param idConcept
     * @param idThesaurus
     * @param type
     * @param idUser
     * @return
     */
    public boolean setConceptType(
                                  String idThesaurus,
                                  String idConcept,
                                  String type,
                                  int idUser) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set concept_type = '" + type + "'"
                        + " WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating type of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Permet de retourner la liste de types de concepts date de type 2021-02-01
     *
     * @param idTheso
     * @return
     */
    public ArrayList<NodeConceptType> getAllTypesOfConcept(String idTheso) {
        ArrayList<NodeConceptType> nodeConceptTypes = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from concept_type where id_theso in ('" + idTheso + "', 'all')"
                        + " order by "
                        + " CASE unaccent(lower(code))"
                        + " WHEN 'concept' THEN 1"
                        + " END");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeConceptType nodeConceptType = new NodeConceptType();
                        nodeConceptType.setCode(resultSet.getString("code"));
                        nodeConceptType.setLabelFr(resultSet.getString("label_fr"));
                        nodeConceptType.setLabelEn(resultSet.getString("label_en"));
                        nodeConceptType.setReciprocal(resultSet.getBoolean("reciprocal"));
                        if ("all".equalsIgnoreCase(resultSet.getString("id_theso"))) {
                            nodeConceptType.setPermanent(true);
                        } else {
                            nodeConceptType.setPermanent(false);
                        }

                        nodeConceptTypes.add(nodeConceptType);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All types of concepts ", sqle);
        }
        return nodeConceptTypes;
    }

    /**
     * Permet de retourner la liste des concepts à partir d'une date donnée date
     * de type 2021-02-01
     *
     * @param idTheso
     * @param date
     * @return
     */
    public ArrayList<String> getIdConceptFromDate(String idTheso, String date) {
        ArrayList<String> ids = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept"
                        + " where "
                        + " concept.id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " concept.status != 'CA'"
                        + " and"
                        + " concept.modified BETWEEN '" + date + "' and now();");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        ids.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting concepts from date ", sqle);
        }
        return ids;
    }

    /**
     * permet de récupérer les concepts dépréciés
     *
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public ArrayList<NodeDeprecated> getAllDeprecatedConceptOfThesaurus(String idTheso, String idLang) {
        ArrayList<NodeDeprecated> nodeDeprecateds = new ArrayList<>();
        ArrayList<NodeIdValue> replacesValues;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_concept, concept.modified, users.username, term.lexical_value"
                        + " from term, concept, preferred_term, users"
                        + " where  "
                        + " concept.contributor = users.id_user"
                        + " and"
                        + " concept.id_concept = preferred_term.id_concept "
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus "
                        + " and preferred_term.id_term = term.id_term "
                        + " and preferred_term.id_thesaurus = term.id_thesaurus "
                        + " and concept.id_thesaurus = '" + idTheso + "'"
                        + " and term.lang = '" + idLang + "'"
                        + " and concept.status = 'DEP'"
                        + " order by unaccent(lower(lexical_value))");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeDeprecated nodeDeprecated = new NodeDeprecated();
                        nodeDeprecated.setDeprecatedId(resultSet.getString("id_concept"));
                        nodeDeprecated.setDeprecatedLabel(resultSet.getString("lexical_value"));
                        nodeDeprecated.setModified(resultSet.getDate("modified"));
                        nodeDeprecated.setUserName(resultSet.getString("username"));
                        nodeDeprecateds.add(nodeDeprecated);
                    }
                }
                for (NodeDeprecated nodeDeprecated : nodeDeprecateds) {
                    replacesValues = deprecateHelper.getAllReplacedBy(idTheso, nodeDeprecated.getDeprecatedId(), idLang, this);
                    boolean first = true;
                    for (NodeIdValue replacesValue : replacesValues) {
                        if (first) {
                            nodeDeprecated.setReplacedByLabel(replacesValue.getValue());
                            nodeDeprecated.setReplacedById(replacesValue.getId());
                        } else {
                            nodeDeprecated.setReplacedByLabel(nodeDeprecated.getDeprecatedLabel() + "##" + replacesValue.getValue());
                            nodeDeprecated.setReplacedById(nodeDeprecated.getDeprecatedId() + "##" + replacesValue.getId());
                        }
                        first = false;
                    }
                }
                return nodeDeprecateds;
            }
        } catch (SQLException sqle) {
            log.error("Error while getting deprecated values : " + idTheso, sqle);
        }
        return null;
    }

    /**
     * permet de retourner un noeud de données optimisées pour l'affichage du
     * graphe D3Js
     *
     * @param idConcept
     * @param idTheso
     * @param idLang
     * @return
     */
    public NodeDatas getConceptForGraph(String idConcept, String idTheso, String idLang) {
        NodeDatas nodeDatas = new NodeDatas();
        String label = termService.getLexicalValueOfConcept(idConcept, idTheso, idLang);
        if (label == null || label.isEmpty()) {
            nodeDatas.setName("(" + idConcept + ")");
        } else {
            nodeDatas.setName(label);
        }
        nodeDatas.setUrl(getUri(idConcept, idTheso));
        nodeDatas.setDefinition(noteHelper.getDefinition(idConcept, idTheso, idLang));
        nodeDatas.setSynonym(nonPreferredTermRepository.findAltLabelsByConceptAndThesaurusAndLang(idConcept, idTheso, idLang));
        return nodeDatas;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept en s'adaptant au
     * format défini pour le thésaurus
     *
     * @return
     */
    private String getUri(String idConcept, String idTheso) {
        if (idConcept == null || idTheso == null) {
            return "";
        }
        return nodePreference.getCheminSite() + "?idc=" + idConcept + "&idt=" + idTheso;
    }

    /**
     * Cette fonction permet de récupérer la liste des concepts suivant l'id du
     * Concept-Père et le thésaurus sous forme de classe NodeConceptTree (sans
     * les relations) elle fait le tri alphabétique ou par notation
     *
     * @param idConcept
     * @param idThesaurus
     * @param isSortByNotation
     * @param idLang
     * @return
     */
    public ArrayList<NodeConceptTree> getListConcepts(String idConcept, String idThesaurus, String idLang,
                                                      boolean isSortByNotation) {

        ResultSet resultSet = null;
        ArrayList<NodeConceptTree> nodeConceptTree = null;
        String query;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    if (isSortByNotation) {
                        /// Notation Sort 
                        query = "SELECT concept.notation, hierarchical_relationship.id_concept2"
                                + " FROM concept, hierarchical_relationship"
                                + " WHERE "
                                + " concept.id_concept = hierarchical_relationship.id_concept2 AND"
                                + " concept.id_thesaurus = hierarchical_relationship.id_thesaurus AND"
                                + " hierarchical_relationship.id_thesaurus = '" + idThesaurus + "' AND"
                                + " hierarchical_relationship.id_concept1 = '" + idConcept + "' AND"
                                + " hierarchical_relationship.role ILIKE 'NT%'"
                                + " and concept.status != 'CA'"
                                + " ORDER BY"
                                + " concept.notation ASC limit 2000";
                    } else {
                        // alphabétique Sort
                        query = "select id_concept2 from hierarchical_relationship, concept"
                                + " where concept.id_thesaurus = hierarchical_relationship.id_thesaurus"
                                + " and concept.id_concept = hierarchical_relationship.id_concept2"
                                + " and hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                                + " and id_concept1 = '" + idConcept + "'"
                                + " and role LIKE 'NT%'"
                                + " and concept.status != 'CA' limit 2000";
                    }

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeConceptTree = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeConceptTree nodeConceptTree1 = new NodeConceptTree();
                            nodeConceptTree1.setIdConcept(resultSet.getString("id_concept2"));
                            if (isSortByNotation) {
                                nodeConceptTree1.setNotation(resultSet.getString("notation"));
                            }

                            nodeConceptTree1.setIdThesaurus(idThesaurus);
                            nodeConceptTree1.setIdLang(idLang);
                            nodeConceptTree1.setTerm(true);
                            nodeConceptTree.add(nodeConceptTree1);
                        }
                    }
                    if (nodeConceptTree != null) {
                        for (NodeConceptTree nodeConceptTree1 : nodeConceptTree) {
                            query = "SELECT term.lexical_value, concept.status"
                                    + " FROM concept, preferred_term, term"
                                    + " WHERE concept.id_concept = preferred_term.id_concept AND"
                                    + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                                    + " preferred_term.id_term = term.id_term AND"
                                    + " preferred_term.id_thesaurus = term.id_thesaurus AND"
                                    + " concept.id_concept = '" + nodeConceptTree1.getIdConcept() + "' AND"
                                    + " term.lang = '" + idLang + "' AND"
                                    + " term.id_thesaurus = '" + idThesaurus + "';";

                            stmt.executeQuery(query);
                            resultSet = stmt.getResultSet();
                            if (resultSet != null) {
                                resultSet.next();
                                if (resultSet.getRow() == 0) {
                                    nodeConceptTree1.setTitle("");
                                    nodeConceptTree1.setStatusConcept("");
                                } else {
                                    nodeConceptTree1.setTitle(resultSet.getString("lexical_value"));
                                    if (resultSet.getString("status") == null) {
                                        nodeConceptTree1.setStatusConcept("");
                                    } else {
                                        nodeConceptTree1.setStatusConcept(resultSet.getString("status"));
                                    }
                                }
                                nodeConceptTree1.setHaveChildren(
                                        haveChildren(idThesaurus, nodeConceptTree1.getIdConcept())
                                );
                            }
                        }
                    }
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting ListConcept of Concept : " + idConcept, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(nodeConceptTree);
        }
        return nodeConceptTree;
    }

    /**
     * Cettte fonction permet de retourner la liste des TopConcept avec IdArk et
     * handle
     *
     * @param idTheso
     * @return
     */
    public ArrayList<NodeUri> getAllTopConcepts(String idTheso) {

        ArrayList<NodeUri> NodeUris = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_concept, id_ark, id_handle, id_doi FROM concept"
                        + " WHERE id_thesaurus = '" + idTheso + "'"
                        + " AND top_concept = true and status !='CA'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUri nodeUri = new NodeUri();
                        if ((resultSet.getString("id_ark") == null) || (resultSet.getString("id_ark").trim().isEmpty())) {
                            nodeUri.setIdArk("");
                        } else {
                            nodeUri.setIdArk(resultSet.getString("id_ark"));
                        }
                        if ((resultSet.getString("id_handle") == null) || (resultSet.getString("id_handle").trim().isEmpty())) {
                            nodeUri.setIdHandle("");
                        } else {
                            nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        }
                        if ((resultSet.getString("id_doi") == null) || (resultSet.getString("id_doi").trim().isEmpty())) {
                            nodeUri.setIdDoi("");
                        } else {
                            nodeUri.setIdDoi(resultSet.getString("id_doi"));
                        }
                        nodeUri.setIdConcept(resultSet.getString("id_concept"));
                        NodeUris.add(nodeUri);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of TT of theso : " + idTheso, sqle);
        }
        return NodeUris;
    }

    /**
     * permet de récupérer les tops concepts par langue, cette focntion ne prend
     * pas en compte quand le concept n'existe pas dans la langue demandée
     *
     * @param idTheso
     * @param idLang
     * @return
     */
    public List<NodeTree> getTopConceptsWithTermByTheso(String idTheso, String idLang) {

        List<NodeTree> nodes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT distinct(concept.id_concept), term.lexical_value "
                        + " FROM concept, term, preferred_term "
                        + " WHERE concept.id_concept = preferred_term.id_concept"
                        + " AND concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " AND preferred_term.id_thesaurus = term.id_thesaurus"
                        + " AND preferred_term.id_term = term.id_term"
                        + " AND concept.id_thesaurus = '" + idTheso + "' "
                        + " AND concept.top_concept = true "
                        + " AND concept.status != 'CA'"
                        + " AND term.lang = '" + idLang + "'"
                        + " order by term.lexical_value");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeTree nodeTree = new NodeTree();
                        nodeTree.setIdConcept(resultSet.getString("id_concept"));
                        nodeTree.setPreferredTerm(resultSet.getString("lexical_value"));
                        nodes.add(nodeTree);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of TT of theso : " + idTheso, sqle);
        }
        return nodes;
    }

    public List<NodeTree> getListChildrenOfConceptWithTerm(String idConcept, String idLang, String idThesaurus) {
        List<NodeTree> nodes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT distinct(hierarchical_relationship.id_concept2), term.lexical_value "
                        + "FROM hierarchical_relationship, term, preferred_term "
                        + "WHERE hierarchical_relationship.id_concept2 = preferred_term.id_concept "
                        + "AND hierarchical_relationship.id_thesaurus = preferred_term.id_thesaurus "
                        + "AND preferred_term.id_term = term.id_term "
                        + "AND preferred_term.id_thesaurus = term.id_thesaurus "
                        + "AND hierarchical_relationship.id_thesaurus = '" + idThesaurus + "' "
                        + "AND hierarchical_relationship.id_concept1 = '" + idConcept + "' "
                        + "AND hierarchical_relationship.role LIKE 'NT%' "
                        + "AND term.lang = '" + idLang + "' "
                        + "ORDER BY term.lexical_value");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeTree nodeTree = new NodeTree();
                        nodeTree.setIdConcept(resultSet.getString("id_concept2"));
                        nodeTree.setPreferredTerm(resultSet.getString("lexical_value"));
                        nodes.add(nodeTree);
                    }
                } catch (SQLException sqle) {
                    log.error("Error while getting Liste of TT of theso : " + idThesaurus, sqle);
                }
            } catch (SQLException sqle) {
                log.error("Error while getting Liste of TT of theso : " + idThesaurus, sqle);
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of TT of theso : " + idThesaurus, sqle);
        }
        return nodes;
    }

    /**
     * Cette fonction permet de déplacer une Branche
     */
    public boolean moveBranchFromConceptToConcept(String idConcept, ArrayList<String> idOldBTsToDelete,
                                                  String idNewConceptBT, String idThesaurus, int idUser) {

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            if (idOldBTsToDelete.size() < 2 && !idOldBTsToDelete.isEmpty()) {
                if (idOldBTsToDelete.get(0).equalsIgnoreCase(idNewConceptBT)) {
                    return true;
                }
            }

            for (String idOldBT : idOldBTsToDelete) {
                if (!relationsHelper.deleteRelationBT(conn, idConcept, idThesaurus, idOldBT, idUser)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
            }

            if (!relationsHelper.addRelationBT(conn, idConcept, idThesaurus, idNewConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Cette fonction permet de déplacer un concept/Branche de la racine vers un
     * concept dans le thésaurus
     */
    public boolean moveBranchFromRootToConcept(String idConcept, String idNewConceptBT,
                                               String idThesaurus, int idUser) {

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            if (!relationsHelper.addRelationBT(conn, idConcept, idThesaurus, idNewConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }

            conn.commit();
            conn.close();

            return setNotTopConcept(idConcept, idThesaurus);
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    // Cette fonction permet de déplacer une Branche vers la racine, elle devient topterme
    public boolean moveBranchFromConceptToRoot(String idConcept, String idOldConceptBT,
                                               String idThesaurus, int idUser) {

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            if (!relationsHelper.deleteRelationBT(conn, idConcept, idThesaurus, idOldConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            return setTopConcept(idConcept, idThesaurus);

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    // Cette fonction permet de mettre à jour la notation pour un concept
    public boolean updateNotation(String idConcept, String idTheso, String notation) {

        boolean status = false;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set notation ='" + notation + "'"
                        + " WHERE id_concept ='" + idConcept + "' AND id_thesaurus='" + idTheso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    //Cette fonction permet de récupérer la liste des Ids of Topconcepts d'un thésaurus
    public ArrayList<String> getAllTopTermOfThesaurus(String idThesaurus) {

        ArrayList<String> listIdOfTopConcept = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and top_concept = true and status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdOfTopConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All Ids of TopConcept : " + idThesaurus, sqle);
        }
        return listIdOfTopConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Topconcepts suivant l'id
     * du thésaurus sous forme de classe NodeConceptTree (sans les relations) La
     * liste est triée
     *
     * @param idThesaurus
     * @param idLang
     * @param isSortByNotation
     * @return
     */
    public List<NodeConceptTree> getListOfTopConcepts(String idThesaurus, String idLang, boolean isSortByNotation, boolean isPrivate) {

        List<NodeConceptTree> nodeConceptTree = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query;
                if (!isPrivate) {
                    query = "SELECT concept.notation,concept.status, concept.id_concept "
                            + "FROM concept "
                            + "WHERE concept.top_concept = true "
                            + "AND concept.status != 'CA' "
                            + "AND concept.id_thesaurus = '" + idThesaurus + "';";
                } else {
                    query = "SELECT concept.notation,concept.status, concept.id_concept "
                            + "FROM concept "
                            + "LEFT JOIN concept_group_concept cgc ON concept.id_concept = cgc.idconcept "
                            + "LEFT JOIN concept_group cg ON cgc.idgroup = cg.idgroup "
                            + "WHERE concept.top_concept = true "
                            + "AND concept.status != 'CA' "
                            + "AND concept.id_thesaurus = '" + idThesaurus + "' "
                            + "GROUP BY concept.notation,concept.status, concept.id_concept "
                            + "HAVING BOOL_OR(cg.private IS NULL OR cg.private = false);";
                }

                stmt.executeQuery(query);

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeConceptTree nodeConceptTree1 = new NodeConceptTree();
                        nodeConceptTree1.setIdConcept(resultSet.getString("id_concept"));
                        if (isSortByNotation) {
                            nodeConceptTree1.setNotation(resultSet.getString("notation"));
                        }
                        nodeConceptTree1.setStatusConcept(resultSet.getString("status"));
                        nodeConceptTree1.setIdThesaurus(idThesaurus);
                        nodeConceptTree1.setIdLang(idLang);
                        nodeConceptTree1.setTopTerm(true);
                        nodeConceptTree.add(nodeConceptTree1);
                    }
                    for (NodeConceptTree nodeConceptTree1 : nodeConceptTree) {

                        stmt.executeQuery("SELECT term.lexical_value"
                                + " FROM preferred_term, term"
                                + " WHERE preferred_term.id_term = term.id_term "
                                + " AND preferred_term.id_thesaurus = term.id_thesaurus "
                                + " AND term.lang = '" + idLang + "' "
                                + " AND preferred_term.id_concept = '" + nodeConceptTree1.getIdConcept() + "' "
                                + " AND term.id_thesaurus = '" + idThesaurus + "'");

                        try (ResultSet resultSet2 = stmt.getResultSet()) {
                            resultSet2.next();
                            if (resultSet2.getRow() == 0) {
                                nodeConceptTree1.setTitle("(" + nodeConceptTree1.getIdConcept() + ")");
                            } else {
                                nodeConceptTree1.setTitle(resultSet2.getString("lexical_value"));
                            }
                            nodeConceptTree1.setHaveChildren(
                                    haveChildren(idThesaurus, nodeConceptTree1.getIdConcept())
                            );
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting TopConcept sorted of theso  : " + idThesaurus, sqle);
        }

        if (CollectionUtils.isEmpty(nodeConceptTree)) return nodeConceptTree;

        if (!isSortByNotation) {
            Collections.sort(nodeConceptTree);
        }
        return nodeConceptTree;
    }

    /**
     * permet de retourner la liste des concepts pour un group donné
     */
    public ArrayList<NodeUri> getListConceptsOfGroup(String idThesaurus, String idGroup) {

        String query = "SELECT DISTINCT concept.id_concept,"
                + " concept.id_ark, concept.id_handle, concept.id_doi"
                + " FROM concept, concept_group_concept"
                + " WHERE"
                + " concept.id_concept = concept_group_concept.idconcept AND"
                + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                + " concept.status != 'CA' AND "
                + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')";

        return getConceptDetails(query, idThesaurus);
    }

    private ArrayList<NodeUri> getConceptDetails(String query, String idThesaurus) {

        ArrayList<NodeUri> nodeUris = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery(query);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUri nodeUri = new NodeUri();
                        nodeUri.setIdConcept(resultSet.getString("id_concept"));
                        nodeUri.setIdArk(resultSet.getString("id_ark"));
                        nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        nodeUri.setIdDoi(resultSet.getString("id_doi"));
                        nodeUris.add(nodeUri);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return nodeUris;
    }

    /**
     * Permet de retourner la date de la dernière modification sur un thésaurus
     *
     * @param idTheso
     * @return
     */
    public Date getLastModification(String idTheso) {

        Date date = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select modified from concept where id_thesaurus = '"
                        + idTheso + "' and status != 'CA' and modified IS NOT NULL order by modified DESC limit 1 ");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        date = resultSet.getDate("modified");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return date;
    }

    /**
     * Permet de retourner la date de la dernière modification sur un thésaurus
     *
     * @param idTheso
     * @param idLang
     * @return
     */
    public ArrayList<NodeIdValue> getLastModifiedConcept(String idTheso, String idLang) {

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_concept, term.lexical_value from concept, preferred_term, term"
                        + " where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and"
                        + " concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and"
                        + " concept.id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " term.lang = '" + idLang + "'"
                        + " and concept.status != 'CA' and concept.modified IS not null  order by concept.modified DESC, term.lexical_value limit 10");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id_concept"));
                        nodeIdValue.setValue(resultSet.getString("lexical_value"));
                        nodeIdValues.add(nodeIdValue);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdValues;
    }

    public ArrayList<NodeIdValue> getIdsAndValuesOfConcepts2(List<String> idsToGet, String idLang, String idTheso) {

        ArrayList<NodeIdValue> idsAndValues = new ArrayList<>();
        String label;
        for (String idConcept : idsToGet) {
            label = termService.getLexicalValueOfConcept(idConcept, idTheso, idLang);
            if (label != null) {
                if (!label.isEmpty()) {
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(idConcept);
                    nodeIdValue.setValue(label);
                    idsAndValues.add(nodeIdValue);
                }
            }
        }
        return idsAndValues;
    }

    /**
     * Cette fonction permet de retrouver tous tes identifiants d'une branche en
     * partant du concept en paramètre
     *
     * @param idConceptDeTete
     * @param idTheso
     * @return
     */
    public ArrayList<String> getIdsOfBranch(String idConceptDeTete, String idTheso) {
        ArrayList<String> lisIds = new ArrayList<>();
        lisIds = getIdsOfBranch__(idConceptDeTete, idTheso, lisIds);
        return lisIds;
    }

    private ArrayList<String> getIdsOfBranch__(String idConceptDeTete, String idTheso, ArrayList<String> lisIds) {

        if (lisIds.contains(idConceptDeTete)) {
            return lisIds;
        }

        lisIds.add(idConceptDeTete);

        ArrayList<String> listIdsOfConceptChildren = getListChildrenOfConcept(idConceptDeTete, idTheso);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranch__(listIdsOfConceptChildren1, idTheso, lisIds);
        }
        return lisIds;
    }

    /**
     * Cette fonction permet de retrouver tous tes identifiants d'une branche en
     * partant du concept en paramètre, elle évite les boucles à l'infini
     *
     * @param idConceptDeTete
     * @param idTheso
     * @return
     */
    public ArrayList<String> getIdsOfBranchWithoutLoop(String idConceptDeTete, String idTheso) {
        ArrayList<String> lisIds = new ArrayList<>();
        lisIds = getIdsOfBranchWithoutLoop__(idConceptDeTete, idTheso, lisIds);
        return lisIds;
    }

    private ArrayList<String> getIdsOfBranchWithoutLoop__(String idConceptDeTete, String idTheso, ArrayList<String> lisIds) {

        if (lisIds.contains(idConceptDeTete)) {
            return lisIds;
        }

        lisIds.add(idConceptDeTete);

        ArrayList<String> listIdsOfConceptChildren = getListChildrenOfConcept(idConceptDeTete, idTheso);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranchWithoutLoop__(listIdsOfConceptChildren1, idTheso, lisIds);
        }
        return lisIds;
    }

    /**
     * Cette fonction permet de retrouver tous tes identifiants d'une branche en
     * partant du concept en paramètre avec limit pour le nombre de résultat
     *
     * @param idConceptDeTete
     * @param idTheso
     * @param limit
     * @return
     */
    public ArrayList<String> getIdsOfBranchLimited(String idConceptDeTete, String idTheso, int limit) {
        ArrayList<String> lisIds = new ArrayList<>();
        lisIds = getIdsOfBranchLimited__(idConceptDeTete, idTheso, lisIds, limit);
        return lisIds;
    }

    private ArrayList<String> getIdsOfBranchLimited__(String idConceptDeTete, String idTheso, ArrayList<String> lisIds, int limit) {

        if (lisIds.size() > limit) {
            return lisIds;
        }
        lisIds.add(idConceptDeTete);

        ArrayList<String> listIdsOfConceptChildren = getListChildrenOfConcept(idConceptDeTete, idTheso);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranchLimited__(listIdsOfConceptChildren1, idTheso, lisIds, limit);
        }
        return lisIds;
    }


    /**
     * Cette fonction regenère un identifiant Ark pour un concept donné
     *
     * @param idTheso
     * @return
     */
    public String generateArkIdForTheso(String idTheso) {
        if(nodePreference == null)
            nodePreference = preferenceService.getThesaurusPreferences(idTheso);

        NodeThesaurus nodeThesaurus = thesaurusService.getNodeThesaurus(idTheso);
        if (nodePreference.isUseArk()) {
            ArkHelper2 arkHelper2 = new ArkHelper2(nodePreference);
            if (!arkHelper2.login()) {
                message = "Erreur de connexion !!";
                return null;
            }
            NodeMetaData nodeMetaData;
            String privateUri;
            if (nodePreference == null) {
                message = ("Erreur: Veuillez paramétrer les préférences pour ce thésaurus !!");
                return null;
            }
            if (!nodePreference.isUseArk()) {
                message = "Erreur: Veuillez activer Ark dans les préférences !!";
                return null;
            }
            nodeMetaData = initNodeMetaData();
            if (nodeMetaData == null) {
                message = "Erreur: pas de méta-données";
                return null;
            }
            nodeMetaData.setTitle(nodeThesaurus.getIdThesaurus());
            nodeMetaData.setSource(nodePreference.getPreferredName());
            nodeMetaData.setCreator("");
            privateUri = "?idt=" + idTheso;
            if (StringUtils.isEmpty(nodeThesaurus.getIdArk())) {
                if (!arkHelper2.addArk(privateUri, nodeMetaData)) {
                    message = arkHelper2.getMessage();
                    message = arkHelper2.getMessage() + "  idTheso = " + nodeThesaurus.getIdThesaurus();
                    log.info("La création Ark a échoué ici : " + nodeThesaurus.getIdThesaurus());
                    return null;
                }
                if (thesaurusService.updateIdArkOfThesaurus(idTheso, arkHelper2.getIdArk())) {
                    return null;
                }
                return nodeThesaurus.getIdArk();
            }
            return arkHelper2.getIdArk();
        }
        if (nodePreference.isUseArkLocal()) {
            String idArk = nodeThesaurus.getIdArk();
            if (StringUtils.isEmpty(idArk)) {
                idArk = getNewId(nodePreference.getSizeIdArkLocal(), nodePreference.isUppercaseForArk(), true);
                idArk = nodePreference.getNaanArkLocal() + "/" + nodePreference.getPrefixArkLocal() + idArk;
                if (thesaurusService.updateIdArkOfThesaurus(idTheso, idArk)) {
                    return null;
                }
            }
            return idArk;
        }
        return null;
    }

    /**
     * Pour préparer les données pour la création d'un idArk
     */
    private NodeMetaData initNodeMetaData() {
        NodeMetaData nodeMetaData = new NodeMetaData();
        nodeMetaData.setDcElementsList(new ArrayList<>());
        return nodeMetaData;
    }

    /**
     * @param idTheso
     * @param idConcept
     * @return
     */
    public boolean isHaveIdArk(String idTheso, String idConcept) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_ark from concept where id_concept = '" + idConcept + "'"
                        + " and id_thesaurus = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        String idArk = resultSet.getString("id_ark");
                        if (idArk == null || idArk.isEmpty()) {
                            return false;
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * @param idTheso
     * @param idConcept
     * @return
     */
    public boolean isHaveNotation(String idTheso, String idConcept) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select notation from concept where id_concept = '" + idConcept + "'"
                        + " and id_thesaurus = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        String notation = resultSet.getString("notation");
                        if (notation == null || notation.isEmpty()) {
                            return false;
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept existe ou non
     */
    public boolean isNotationExist(String idThesaurus, String notation) {

        boolean existe = false;

        if (notation.isEmpty()) {
            return false;
        }

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and notation ilike '" + notation.trim() + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Notation exist : " + notation, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept a un createur
     *
     * @param idThesaurus
     * @param idConcept
     * @return
     */
    public boolean isHaveCreator(String idThesaurus, String idConcept) {
        boolean existe = false;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select creator from concept where id_thesaurus = '" + idThesaurus + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if ((resultSet.getInt("creator") != -1) && (resultSet.getInt("creator") != 0)) {
                            existe = true;
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if creator exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept a un contributeur
     *
     * @param idThesaurus
     * @param idConcept
     * @return
     */
    public boolean isHaveContributor(String idThesaurus, String idConcept) {
        boolean existe = false;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select contributor from concept where id_thesaurus = '" + idThesaurus + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if ((resultSet.getInt("contributor") != -1) && (resultSet.getInt("contributor") != 0)) {
                            existe = true;
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if contributor exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Permet de supprimer tous les identifiants Handle de la table Concept et
     * de la plateforme (handle.net) via l'API REST pour un thésaurus donné
     * suite à une suppression d'un thésaurus
     *
     * @param idThesaurus
     * @return
     */
    public boolean deleteAllIdHandle(String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        ArrayList<String> tabIdHandle = getAllIdHandleOfThesaurus(idThesaurus);

        if (nodePreference.isUseHandleWithCertificat()) {
            if (!handleHelper.deleteAllIdHandle(tabIdHandle, nodePreference)) {
                message = handleHelper.getMessage();
                return false;
            }
            message = handleHelper.getMessage();
            return true;
        } else {
           // HandleService hs = HandleService.getInstance();
            handleService.applyNodePreference(nodePreference);
            handleService.connectHandle();
            for (String idHandle : tabIdHandle) {
                try {
                    handleService.deleteHandle(idHandle);
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
            return true;
        }
    }

    /**
     * Cette fonction permet d'insérrer un Concept dans la table Concept avec un
     * idConcept existant (Import) avec Rollback
     *
     * @param concept
     * @return
     */
    public boolean insertConceptInTable(Concept concept) {

        String created;
        String modified;
        if (concept.getCreated() == null) {
            created = null;
        } else {
            created = "'" + concept.getCreated() + "'";
        }
        if (concept.getModified() == null) {
            modified = null;
        } else {
            modified = "'" + concept.getModified() + "'";
        }
        if (concept.getStatus() == null) {
            concept.setStatus("D");
        }
        if (concept.getIdArk() == null) {
            concept.setIdArk("");
        }
        if (concept.getIdHandle() == null) {
            concept.setIdHandle("");
        }
        if (concept.getNotation() == null) {
            concept.setNotation("");
        }

        concept.setCreator(userRepository.findAllByUsername(concept.getCreatorName()).get().getId());
        concept.setContributor(userRepository.findAllByUsername(concept.getContributorName()).get().getId());

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into concept "
                        + "(id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id_handle, id_doi, creator, contributor)"
                        + " values ("
                        + "'" + concept.getIdConcept() + "'"
                        + ",'" + concept.getIdThesaurus() + "'"
                        + ",'" + concept.getIdArk() + "'"
                        + "," + created
                        + "," + modified
                        + ",'" + concept.getStatus() + "'"
                        + ",'" + concept.getNotation() + "'"
                        + "," + concept.isTopConcept()
                        + ",'" + concept.getIdHandle() + "'"
                        + ",'" + concept.getIdDoi() + "'"
                        + "," + concept.getCreator()
                        + "," + concept.getContributor()
                        + ")");
                return true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept : " + concept.getIdConcept(), sqle);
            } else {
                return true;
            }
        }
        return false;
    }

    private String getNewId(int length, boolean isUpperCase, boolean isUseNoidCheck) {
        String chars = "0123456789bcdfghjklmnpqrstvwxz";
        StringBuilder pass = new StringBuilder();
        String idArk;
        for (int x = 0; x < length; x++) {
            int i = (int) Math.floor(Math.random() * (chars.length() - 1));
            pass.append(chars.charAt(i));
        }
        idArk = pass.toString();
        if (isUseNoidCheck) {
            NoIdCheckDigit noIdCheckDigit = new NoIdCheckDigit();
            String checkCode = noIdCheckDigit.getControlCharacter(idArk);
            idArk = idArk + "-" + checkCode;
        }

        if (isUpperCase)
            return idArk.toUpperCase();
        else
            return idArk;
    }

    /**
     * Cette fonction permet de récupérer un Concept par son id et son thésaurus
     * sous forme de classe Concept (sans les relations) ni le Terme
     *
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public Concept getThisConcept(String idConcept, String idThesaurus) {

        Concept concept = null;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from concept where id_thesaurus = '" + idThesaurus + "' and id_concept = '" + idConcept + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        concept = new Concept();
                        concept.setIdConcept(idConcept);
                        concept.setIdThesaurus(idThesaurus);
                        concept.setIdArk(resultSet.getString("id_ark"));
                        concept.setIdHandle(resultSet.getString("id_handle"));
                        concept.setIdDoi(resultSet.getString("id_doi"));
                        concept.setCreated(resultSet.getDate("created"));
                        concept.setModified(resultSet.getDate("modified"));
                        concept.setStatus(resultSet.getString("status"));
                        concept.setNotation(resultSet.getString("notation"));
                        concept.setTopConcept(resultSet.getBoolean("top_concept"));
                        concept.setCreator(resultSet.getInt("creator"));
                        concept.setContributor(resultSet.getInt("contributor"));
                        concept.setIdGroup("");//resultSet.getString("idgroup"));
                        concept.setConceptType(resultSet.getString("concept_type").toLowerCase());
                    }
                }

                if (concept != null) {
                    var contributor = userRepository.findById(concept.getContributor());
                    concept.setContributorName(contributor.isPresent() ? contributor.get().getUsername() : "");

                    var creator = userRepository.findById(concept.getCreator());
                    concept.setCreatorName(creator.isPresent() ? creator.get().getUsername() : "");
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Concept : " + idConcept, sqle);
        }
        return concept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     */
    public ArrayList<String> getAllIdConceptOfThesaurus(String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and concept.status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id Handle d'un thésaurus
     */
    public ArrayList<String> getAllIdHandleOfThesaurus(String idThesaurus) {

        ArrayList<String> tabId = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_handle from concept where id_thesaurus = '" + idThesaurus + "'"
                        + " and (id_handle != null or id_handle != '')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        if (resultSet.getString("id_handle") != null) {
                            if (!resultSet.getString("id_handle").isEmpty()) {
                                tabId.add(resultSet.getString("id_handle"));
                            }
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdHandle of Thesaurus : " + idThesaurus, sqle);
        }
        return tabId;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * qui n'ont pas d'identifiants Ark
     *
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> getAllIdConceptOfThesaurusWithoutArk(String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and (id_ark = '' or id_ark = null) and status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus without Ark : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * qui n'ont pas d'identifiants Handle
     */
    public ArrayList<String> getAllIdConceptOfThesaurusWithoutHandle(String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and (id_handle = '' or id_handle = null)");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus without Handle : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * en filtrant par plusieurs domaines/Groupes
     *
     * @param idThesaurus
     * @param idGroups
     * @return
     */
    public ArrayList<String> getAllIdConceptOfThesaurusByMultiGroup(String idThesaurus, String[] idGroups) {

        ArrayList<String> tabIdConcept = new ArrayList<>();
        String multiValuesGroup = "";
        // filter by group
        if (idGroups != null && idGroups.length != 0) {
            String groupSearch = "";
            for (String idGroup : idGroups) {
                if (groupSearch.isEmpty()) {
                    groupSearch = "lower('" + idGroup + "')";
                } else {
                    groupSearch = groupSearch + ",lower('" + idGroup + "')";
                }
            }
            multiValuesGroup = " and lower(concept_group_concept.idgroup) in (" + groupSearch + ")";
        }

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT concept.id_concept "
                        + " FROM concept, concept_group_concept "
                        + " WHERE "
                        + " concept.id_concept = concept_group_concept.idconcept"
                        + " AND"
                        + " concept.id_thesaurus = concept_group_concept.idthesaurus "
                        + " AND"
                        + " concept.id_thesaurus = '" + idThesaurus + "' "
                        + " AND"
                        + " concept.status != 'CA' "
                        + multiValuesGroup);

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by multiGroups : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * en filtrant par Domaine/Group
     */
    public ArrayList<String> getAllIdConceptOfThesaurusByGroup(String idThesaurus, String idGroup) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT concept.id_concept "
                        + " FROM concept, concept_group_concept "
                        + " WHERE "
                        + " concept.id_concept = concept_group_concept.idconcept"
                        + " AND"
                        + " concept.id_thesaurus = concept_group_concept.idthesaurus "
                        + " AND"
                        + " concept.id_thesaurus = '" + idThesaurus + "' "
                        + " AND"
                        + " concept.status != 'CA' "
                        + " AND"
                        + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    public ArrayList<String> getAllIdConceptOfThesaurus(Connection conn, String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_concept from concept where id_thesaurus = '" + idThesaurus + "'");
            try (ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    tabIdConcept.add(resultSet.getString("id_concept"));
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }


    /**
     * Cette fonction permet de récupérer la notation sinon renvoie une chaine
     * vide
     *
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public String getNotationOfConcept(String idConcept, String idThesaurus) {

        String notation = "";
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select notation from concept where id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        notation = resultSet.getString("notation") == null ? "" : resultSet.getString("notation").trim();
                        //notation = resultSet.getString("notation").trim();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting notation of Concept : " + idConcept, sqle);
        }
        return notation;
    }

    /**
     * Cette fonction permet de récupérer la notation sinon renvoie une chaine
     * vide
     *
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public String getTypeOfConcept(String idConcept, String idThesaurus) {

        String conceptType = "";
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept_type from concept where id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        conceptType = resultSet.getString("concept_type") == null ? "" : resultSet.getString("concept_type").trim();
                        //notation = resultSet.getString("notation").trim();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting notation of Concept : " + idConcept, sqle);
        }
        return conceptType;
    }

    /**
     * Cette fonction permet de récupérer les identifiants d'un concept idArk,
     * idHandle, idConcept sous forme de nodeUri
     *
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public NodeUri getNodeUriOfConcept(String idConcept, String idThesaurus) {

        NodeUri nodeUri = new NodeUri();
        nodeUri.setIdConcept(idConcept);

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_ark, id_handle from concept where id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeUri.setIdArk(resultSet.getString("id_ark"));
                        nodeUri.setIdHandle(resultSet.getString("id_handle"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting nodeUri of Concept : " + idConcept, sqle);
        }
        return nodeUri;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après
     * l'idArk
     *
     * @param arkId
     * @param idTheso
     * @return
     */
    public String getIdConceptFromArkId(String arkId, String idTheso) {
        String idConcept = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where"
                        + " id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " REPLACE(concept.id_ark, '-', '') ilike REPLACE('" + arkId + "', '-', '')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept").trim();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcept by idArk : " + arkId, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après
     * l'idArk
     */
    public String getIdConceptFromHandleId(String handleId) {
        String idConcept = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_handle ilike '" + handleId + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcept by idArk : " + handleId, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après l'id
     * du concept !!!! ATTENTION !!!! l'id du concept peut se trouver dans
     * plusieurs thésaurus différents donc on ne retourne que le premier.
     */
    public String getIdThesaurusFromIdConcept(String idConcept) {
        String idThesaurus = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_thesaurus from concept where id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("id_thesaurus");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idThesaurus by idConcept: " + idConcept, sqle);
        }
        return idThesaurus;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du thésaurus d'après
     * l'idArk
     */
    public String getIdThesaurusFromArkId(String arkId) {
        String idThesaurus = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_thesaurus from concept where " +
                        " REPLACE(concept.id_ark, '-', '') = REPLACE('" + arkId + "', '-', '')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("id_thesaurus");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idThesaurus by idArk : " + arkId, sqle);
        }
        return idThesaurus;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du thésaurus d'après
     * l'idHandle
     */
    public String getIdThesaurusFromHandleId(String handleId) {

        String idThesaurus = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_thesaurus from concept where id_handle = '" + handleId + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("id_thesaurus");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idThesaurus by idArk : " + handleId, sqle);
        }
        return idThesaurus;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Group d'un Concept
     */
    public String getGroupIdOfConcept(String idConcept, String idThesaurus) {

        String idGroup = null;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idgroup from concept_group_concept where idthesaurus = '"
                        + idThesaurus + "' and idconcept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        if (resultSet.next()) {
                            idGroup = resultSet.getString("idgroup");
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Id of group of Concept : " + idConcept, sqle);
        }
        return idGroup;
    }

    /**
     * Cette fonction permet de rendre un Concept de type Topconcept
     */
    public boolean setNotTopConcept(String idConcept, String idThesaurus) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set top_concept = false WHERE id_concept ='" + idConcept
                        + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de rendre un Concept de type Topconcept
     *
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public boolean setTopConcept(String idConcept, String idThesaurus) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set top_concept = true WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de mettre à jour le createur
     *
     * @param idThesaurus
     * @param idConcept
     * @param idCreator
     * @return
     */
    public boolean setCreator(String idThesaurus, String idConcept, int idCreator) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set creator = " + idCreator
                        + " WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating creator of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de mettre à jour le contributeur
     *
     * @param idThesaurus
     * @param idConcept
     * @param idContributor
     * @return
     */
    public boolean setContributor(String idThesaurus, String idConcept, int idContributor) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set contributor = " + idContributor
                        + " WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating contributor of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de savoir si le Concept est un TopConcept
     */
    public boolean isTopConcept(String idConcept, String idThesaurus, String idGroup) {
        boolean existe = false;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select top_concept from concept where id_concept = '"
                        + idConcept + "' and id_thesaurus = '" + idThesaurus
                        + "' and id_group = '" + idGroup + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getBoolean("top_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Asking if TopConcept : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Concept est un TopConcept sans
     * définir le group (pour permettre de nettoyer les orphelins)
     */
    public boolean isTopConcept(String idConcept, String idThesaurus) {
        boolean existe = false;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select top_concept from concept where id_concept = '" + idConcept
                        + "' and id_thesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getBoolean("top_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Asking if TopConcept : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de récupérer les Ids des concepts suivant l'id du
     * Concept-Père et le thésaurus sous forme de classe tableau pas de tri
     *
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> getListChildrenOfConcept(String idConcept, String idThesaurus) {
        ArrayList<String> listIdsOfConcept = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept2 from hierarchical_relationship where id_thesaurus = '"
                        + idThesaurus + "' and id_concept1 = '" + idConcept + "' and role LIKE 'NT%'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdsOfConcept.add(resultSet.getString("id_concept2"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List of Id of Concept : " + idConcept, sqle);
        }
        return listIdsOfConcept;
    }

    /**
     * Cette fonction permet de récupérer les IdArk des concepts suivant l'idArk
     * du Concept-Père et le thésaurus
     *
     * @param idArk
     * @return
     */
    public ArrayList<String> getListChildrenOfConceptByArk(String idArk) {
        ArrayList<String> listIdsArks = new ArrayList<>();

        String idTheso = getIdThesaurusFromArkId(idArk);
        String idConcept = getIdConceptFromArkId(idArk, idTheso);

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_ark  from hierarchical_relationship, concept"
                        + " where"
                        + " concept.id_concept = hierarchical_relationship.id_concept2"
                        + " and"
                        + " concept.id_thesaurus = hierarchical_relationship.id_thesaurus"
                        + " and"
                        + " hierarchical_relationship.id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " hierarchical_relationship.id_concept1 = '" + idConcept + "'"
                        + " and"
                        + " hierarchical_relationship.role LIKE 'NT%'"
                        + " and"
                        + " concept.status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdsArks.add(resultSet.getString("id_ark"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List of Id of Concept : " + idConcept, sqle);
        }
        return listIdsArks;
    }

    private List<NodeHieraRelation> getRelations(List<NodeHieraRelation> nodeHieraRelations,
                                                 List<String> relations) {

        List<NodeHieraRelation> nodeHieraRelations1 = new ArrayList<>();
        for (NodeHieraRelation nodeHieraRelation : nodeHieraRelations) {
            if (relations.contains(nodeHieraRelation.getRole())) {
                nodeHieraRelations1.add(nodeHieraRelation);
            }
            ;
        }
        return nodeHieraRelations1;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue On récupère aussi les
     * IdArk si Ark est actif
     *
     * @param idConcept
     * @param idThesaurus
     * @param isCandidatExport
     * @return
     */
    public NodeConceptExport getConceptForExport(String idConcept, String idThesaurus, boolean isCandidatExport) {

        NodeConceptExport nodeConceptExport = new NodeConceptExport();

        String htmlTagsRegEx = "<[^>]*>";

        // les relations BT, NT, RT
        ArrayList<NodeHieraRelation> nodeListRelations = relationsHelper.getAllRelationsOfConcept(idConcept, idThesaurus);
        nodeConceptExport.setNodeListOfBT(getRelations(nodeListRelations, nodeConceptExport.getRelationsBT()));
        nodeConceptExport.setNodeListOfNT(getRelations(nodeListRelations, nodeConceptExport.getRelationsNT()));
        nodeConceptExport.setNodeListIdsOfRT(getRelations(nodeListRelations, nodeConceptExport.getRelationsRT()));

        //récupération du Concept        
        Concept concept = getThisConcept(idConcept, idThesaurus);
        if (concept == null) {
            return null;
        }
        nodeConceptExport.setConcept(concept);

        //récupération les aligenemnts 
        nodeConceptExport.setNodeAlignmentsList(alignmentService.getAllAlignmentsOfConcept(idConcept, idThesaurus));

        //récupération des traductions        
        nodeConceptExport.setNodeTermTraductions(termRepository.findAllTraductionsOfConcept(idConcept, idThesaurus));

        //récupération des Non Prefered Term        
        nodeConceptExport.setNodeEM(nonPreferredTermService.getAllNonPreferredTerms(idConcept, idThesaurus));

        //récupération des Groupes ou domaines 
        nodeConceptExport.setNodeListIdsOfConceptGroup(groupService.getListGroupOfConceptArk(idThesaurus, idConcept));

        ArrayList<NodeNote> nodeNotes = noteHelper.getListNotesAllLang(idConcept, idThesaurus);
        if (isCandidatExport) {
            for (NodeNote note : nodeNotes) {
                String str = formatLinkTag(note.getLexicalValue());
                note.setLexicalValue(str.replaceAll(htmlTagsRegEx, ""));
            }
        }
        nodeConceptExport.setNodeNotes(nodeNotes);

        //récupération des coordonnées GPS
        List<Gps> nodeGps = gpsService.findByIdConceptAndIdThesoOrderByPosition(idConcept, idThesaurus);
        if (CollectionUtils.isNotEmpty(nodeGps)) {
            nodeConceptExport.setNodeGps(nodeGps.stream().map(element -> NodeGps.builder()
                            .position(element.getPosition())
                            .longitude(element.getLongitude())
                            .latitude(element.getLatitude())
                            .build())
                    .toList());
        }

        List<NodeImage> nodeImages = imageService.getAllExternalImages(idConcept, idThesaurus);
        if (nodeImages != null) {
            nodeConceptExport.setNodeImages(nodeImages);
        }

        if (isCandidatExport) {

            var messages = candidatMessageRepository.findMessagesByConceptAndThesaurus(idConcept, idThesaurus);
            if (CollectionUtils.isNotEmpty(messages)) {
                nodeConceptExport.setMessages(messages.stream().map(element -> MessageDto.builder()
                                .msg(element.getValue())
                                .date(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(element.getDate()))
                                .idUser(element.getIdUser())
                                .build())
                        .toList());
            }

            var votes = candidatVoteRepository.findAllByIdConceptAndIdThesaurus(idConcept, idThesaurus);
            if (CollectionUtils.isNotEmpty(votes)) {
                nodeConceptExport.setVotes(votes.stream().map(element -> VoteDto.builder()
                                .idConcept(element.getIdConcept())
                                .idThesaurus(element.getIdThesaurus())
                                .idUser(element.getIdUser())
                                .idNote(element.getIdNote())
                                .typeVote(element.getTypeVote())
                                .build())
                        .toList());
            }
        }

        // pour les facettes
        List<String> idFacettes = facetHelper.getAllIdFacetsOfConcept(idConcept, idThesaurus);
        if (!idFacettes.isEmpty()) {
            nodeConceptExport.setListFacetsOfConcept(idFacettes);
        }

        /// pour les concepts dépréciés
        nodeConceptExport.setReplacedBy(deprecateHelper.getAllReplacedByWithArk(idThesaurus, idConcept));
        nodeConceptExport.setReplaces(deprecateHelper.getAllReplacesWithArk(idThesaurus, idConcept));

        return nodeConceptExport;
    }

    public static String formatLinkTag(String initialStr) {
        Pattern MY_PATTERN = Pattern.compile("<a(.*?)a>");
        Matcher m = MY_PATTERN.matcher(initialStr);
        while (m.find()) {
            String link = "<a" + m.group(1) + "a>";
            ArrayList<HTMLLinkElement> result = new HtmlLinkExtraction().extractHTMLLinks(link);
            if (CollectionUtils.isNotEmpty(result)) {
                initialStr = initialStr.replace(link, result.get(0).getLinkElement()
                        + " (" + result.get(0).getLinkAddress() + ")");
            }
        }
        return initialStr;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue ##MR ajout de limit NT
     * qui permet de définir la taille maxi des NT à récupérer, si = -1, pas de
     * limit offset 42 fetch next 21 rows only
     *
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param step
     * @param offset
     * @return
     */
    public NodeFullConcept getConcept2(String idConcept, String idThesaurus, String idLang, int offset, int step) {
        return resourceService.getFullConcept(idThesaurus, idConcept, idLang, offset, step);
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue ##MR ajout de limit NT
     * qui permet de définir la taille maxi des NT à récupérer, si = -1, pas de
     * limit offset 42 fetch next 21 rows only
     */
    public NodeConcept getConcept(String idConcept, String idThesaurus, String idLang, int step, int offset) {
        NodeConcept nodeConcept = new NodeConcept();

        // récupération des BT
        ArrayList<NodeBT> nodeListBT = relationsHelper.getListBT(idConcept, idThesaurus, idLang);
        nodeConcept.setNodeBT(nodeListBT);

        //récupération du Concept
        Concept concept = getThisConcept(idConcept, idThesaurus);
        if (concept == null) {
            return null;
        }
        if ("dep".equalsIgnoreCase(concept.getStatus())) {
            concept.setDeprecated(true);
        }
        nodeConcept.setConcept(concept);

        //récupération du Terme
        Term term = termService.getThisTerm(idConcept, idThesaurus, idLang);
        nodeConcept.setTerm(term);

        //récupération des termes spécifiques
        nodeConcept.setNodeNT(relationsHelper.getListNT(idConcept, idThesaurus, idLang, step, offset));

        //récupération des termes associés
        nodeConcept.setNodeRT(relationsHelper.getListRT(idConcept, idThesaurus, idLang));

        //récupération des Non Prefered Term
        nodeConcept.setNodeEM(nonPreferredTermService.getNonPreferredTerms(idConcept, idThesaurus, idLang));

        //récupération des traductions
        nodeConcept.setNodeTermTraductions(termService.getTraductionsOfConcept(idConcept, idThesaurus, idLang));

        //récupération des notes du term
        nodeConcept.setNodeNotes(noteHelper.getListNotes(idConcept, idThesaurus, idLang));

        nodeConcept.setNodeConceptGroup(groupService.getListGroupOfConcept(idThesaurus, idConcept, idLang));

        nodeConcept.setNodeAlignments(alignmentService.getAllAlignmentOfConcept(idConcept, idThesaurus));

        nodeConcept.setNodeimages(imageService.getAllExternalImages(idConcept, idThesaurus));

        //gestion des ressources externes
        var externalResources = externalResourcesRepository.findAllByIdConceptAndIdThesaurus(idConcept, idThesaurus);
        if (CollectionUtils.isNotEmpty(externalResources)) {
            nodeConcept.setNodeExternalResources(externalResources.stream()
                    .map(element -> NodeImage.builder()
                            .idConcept(element.getIdConcept())
                            .idThesaurus(element.getIdThesaurus())
                            .imageName(element.getDescription())
                            .uri(element.getExternalUri())
                            .build())
                    .toList());
        }

        // concepts qui remplacent un concept déprécié
        nodeConcept.setReplacedBy(deprecateHelper.getAllReplacedBy(idThesaurus, idConcept, idLang, this));
        // les concepts dépécés que ce concept remplace
        nodeConcept.setReplaces(deprecateHelper.getAllReplaces(idThesaurus, idConcept, idLang, this));

        /// récupération des Méta-données DC_terms
        var conceptDcTerms = conceptDcTermRepository.findAllByIdThesaurusAndIdConcept(idThesaurus, idConcept);
        if (CollectionUtils.isNotEmpty(conceptDcTerms)) {
            nodeConcept.setDcElements(conceptDcTerms.stream()
                    .map(element -> {
                        DcElement dcElement = new DcElement();
                        dcElement.setName(element.getName());
                        dcElement.setValue(element.getValue());
                        dcElement.setLanguage(element.getLanguage());
                        dcElement.setType(element.getDataType());
                        return dcElement;
                    })
                    .toList());
        }



        return nodeConcept;
    }

    public String getConceptIdFromPrefLabel(String prefLabel, String idThesaurus, String lang) {

        String idConcept = null;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String str = prefLabel.replaceAll("\'", "%");
                stmt.executeQuery("SELECT DISTINCT(preferred_term.id_concept) FROM preferred_term, term "
                        + "WHERE term.id_thesaurus = '" + idThesaurus + "' AND term.id_term = preferred_term.id_term "
                        + "AND term.lexical_value like '%" + str + "%' AND lang = '" + lang + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcept of idTerm : " + idConcept, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de savoir si un concept a des fils ou non suivant
     * l'id du Concept et l'id du thésaurus sous forme de classe Concept (sans
     * les relations)
     *
     * @param idThesaurus
     * @param idConcept
     * @return
     */
    public boolean haveChildren(String idThesaurus, String idConcept) {

        boolean children = false;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select count(id_concept2)  from hierarchical_relationship, concept where "
                        + " hierarchical_relationship.id_concept2 = concept.id_concept and"
                        + " hierarchical_relationship.id_thesaurus = concept.id_thesaurus"
                        + " and hierarchical_relationship.id_thesaurus='" + idThesaurus + "'"
                        + " and id_concept1='" + idConcept + "' and role LIKE 'NT%' and concept.status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) != 0) {
                            children = true;
                        }
                    }
                }
                // s'il n'a pas d'enfant, on regarde s'il n'a pas de facette également
                if (!children) {
                    stmt.executeQuery("SELECT count(id_facet) FROM thesaurus_array"
                            + " WHERE "
                            + " id_concept_parent = '" + idConcept + "'"
                            + " and "
                            + " id_thesaurus = '" + idThesaurus + "'");
                    try (ResultSet resultSet = stmt.getResultSet()) {
                        if (resultSet.next()) {
                            if (resultSet.getInt(1) != 0) {
                                children = true;
                            }
                        }
                    }

                }
            }
        } catch (SQLException sqle) {
            log.error("Error while testing if haveChildren of Concept : " + idConcept, sqle);
        }
        return children;
    }

    /**
     * Focntion récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête en incluant les Groupes on
     * peut rencontrer plusieurs têtes en remontant, alors on construit à chaque
     * fois un chemin complet.
     *
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<ArrayList<String>> getInvertPathOfConcept(String idConcept, String idThesaurus,
                                                                ArrayList<String> firstPath,
                                                                ArrayList<String> path,
                                                                ArrayList<ArrayList<String>> tabId) {

        ArrayList<String> resultat = relationsHelper.getListIdBT(idConcept, idThesaurus);
        if (resultat.size() > 1) {
            for (String path1 : path) {
                firstPath.add(path1);
            }
        }
        if (resultat.isEmpty()) {

            String group;

            do {
                group = getGroupIdOfConcept(idConcept, idThesaurus);
                if (group == null) {
                    group = relationGroupService.getIdFather(idConcept, idThesaurus);
                }

                path.add(group);
                idConcept = group;
            } while (relationGroupService.getIdFather(group, idThesaurus) != null);

            ArrayList<String> pathTemp = new ArrayList<>();
            for (String path2 : firstPath) {
                pathTemp.add(path2);
            }
            for (String path1 : path) {
                if (pathTemp.indexOf(path1) == -1) {
                    pathTemp.add(path1);
                }
            }
            tabId.add(pathTemp);
            path.clear();
        }

        for (String resultat1 : resultat) {
            path.add(resultat1);
            getInvertPathOfConcept(resultat1, idThesaurus, firstPath, path, tabId);
        }

        return tabId;

    }

    /**
     * Focntion récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     */
    private ArrayList<ArrayList<String>> getInvertPathOfConceptWithoutGroup(String idConcept, String idThesaurus,
                                                                            ArrayList<String> firstPath,
                                                                            ArrayList<String> path,
                                                                            ArrayList<ArrayList<String>> tabId) {

        ArrayList<String> resultat = relationsHelper.getListIdBT(idConcept, idThesaurus);
        if (resultat.size() > 1) {
            for (String path1 : path) {
                firstPath.add(path1);
            }
        }
        if (resultat.isEmpty()) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String path2 : firstPath) {
                pathTemp.add(path2);
            }
            for (String path1 : path) {
                if (pathTemp.indexOf(path1) == -1) {
                    pathTemp.add(path1);
                }
            }
            tabId.add(pathTemp);
            path.clear();
        }

        for (String resultat1 : resultat) {
            path.add(resultat1);
            getInvertPathOfConceptWithoutGroup(resultat1, idThesaurus, firstPath, path, tabId);
        }

        return tabId;
    }

    public ArrayList<ArrayList<String>> getPathOfConceptWithoutGroup(String idConcept, String idThesaurus, ArrayList<String> path, ArrayList<ArrayList<String>> tabId) {

        ArrayList<String> firstPath = new ArrayList<>();

        ArrayList<ArrayList<String>> tabIdInvert = getInvertPathOfConceptWithoutGroup(idConcept,
                idThesaurus, firstPath, path, tabId);
        for (int i = 0; i < tabIdInvert.size(); i++) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (int j = tabIdInvert.get(i).size(); j > 0; j--) {
                pathTemp.add(tabIdInvert.get(i).get(j - 1));
            }
            tabIdInvert.remove(i);
            tabIdInvert.add(i, pathTemp);
        }
        return tabIdInvert;
    }

    /**
     * Cette fonction permet de mettre à jour la notation pour un concept
     *
     * @param conn
     * @param idConcept
     * @param idTheso
     * @param notation
     * @return
     */
    public boolean updateNotation(Connection conn, String idConcept, String idTheso, String notation) {
        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE concept set notation ='" + notation + "' WHERE id_concept ='"
                    + idConcept + "' AND id_thesaurus='" + idTheso + "'");
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Change l'id d'un concept dans la table concept
     */
    public void setIdConcept(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE concept SET id_concept = '" + newIdConcept + "' WHERE id_concept = '" + idConcept + "' AND id_thesaurus = '" + idTheso + "' ");
        }
    }

    public Preferences getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(Preferences nodePreference) {
        this.nodePreference = nodePreference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NodeConcept getConceptFromNodeFullConcept(NodeFullConcept nodeFullConcept, String idTheso, String idLang) {
        NodeConcept nodeConcept = new NodeConcept();
        // récupération des BT
        nodeConcept.setNodeBT(getBTFromNFC(nodeFullConcept.getBroaders()));
        //récupération du Concept
        nodeConcept.setConcept(getConceptFromNFC(nodeFullConcept, idTheso));
        //récupération du Terme
        nodeConcept.setTerm(getTermFromNFC(nodeFullConcept, idTheso));
        //récupération des termes spécifiques
        nodeConcept.setNodeNT(getNTFromNFC(nodeFullConcept.getNarrowers()));
        //récupération des termes associés
        nodeConcept.setNodeRT(getRTFromNFC(nodeFullConcept.getRelateds()));
        //récupération des Non Prefered Term
        nodeConcept.setNodeEM(getEMFromNFC(nodeFullConcept));
        //récupération des notes
        nodeConcept.setNodeNotes(getNotesFromNFC(nodeFullConcept));
        //récupération des collections
        nodeConcept.setNodeConceptGroup(getGroupFromNFC(nodeFullConcept, idTheso, idLang));
        // récupération des alignements
        nodeConcept.setNodeAlignments(getAlignmentsFromNFC(nodeFullConcept, idTheso));
        // récupération des images
        nodeConcept.setNodeimages(getImagesFromNFC(nodeFullConcept, idTheso));
        //gestion des ressources externes
        nodeConcept.setNodeExternalResources(getExternalResourcesFromNFC(nodeFullConcept, idTheso));
        //récupération des traductions
        nodeConcept.setNodeTermTraductions(getTraductionsFromNFC(nodeFullConcept.getPrefLabelsTraduction()));
        // concepts qui remplacent un concept déprécié
        nodeConcept.setReplaces(getReplacesFromNFC(nodeFullConcept.getReplaces()));
        // les concepts dépécés que ce concept remplace
        nodeConcept.setReplacedBy(getReplacesFromNFC(nodeFullConcept.getReplacedBy()));

        return nodeConcept;
    }

    private List<NodeIdValue> getReplacesFromNFC(List<ConceptIdLabel> conceptIdLabels) {
        if(conceptIdLabels == null) return Collections.emptyList();
        return conceptIdLabels.stream()
                .map(resource -> {
                    NodeIdValue node = new NodeIdValue();
                    node.setId(resource.getIdentifier());
                    node.setValue(resource.getLabel());
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeTermTraduction> getTraductionsFromNFC(List<ConceptLabel> conceptLabels){
        if(conceptLabels == null) return Collections.emptyList();
        return conceptLabels.stream()
                .map(resource -> {
                    NodeTermTraduction node = new NodeTermTraduction();
                    node.setLang(resource.getIdLang());
                    node.setLexicalValue(resource.getLabel());
                    node.setCodePays(resource.getCodeFlag());
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeImage> getExternalResourcesFromNFC(NodeFullConcept nodeFullConcept, String idTheso){
        List<ConceptIdLabel> resources = nodeFullConcept.getExternalResources();
        if(resources == null) return Collections.emptyList();
        return resources.stream()
                .map(resource -> {
                    NodeImage node = new NodeImage();
                    node.setIdConcept(nodeFullConcept.getIdentifier());
                    node.setIdThesaurus(idTheso);
                    node.setUri(resource.getUri());
                    node.setImageName(resource.getLabel());
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeImage> getImagesFromNFC(NodeFullConcept nodeFullConcept, String idTheso){
        List<ConceptImage> conceptImages = nodeFullConcept.getImages();
        if(conceptImages == null) return Collections.emptyList();
        return conceptImages.stream()
                .map(image -> {
                    NodeImage node = new NodeImage();
                    node.setId(image.getId());
                    node.setIdConcept(nodeFullConcept.getIdentifier());
                    node.setIdThesaurus(idTheso);
                    node.setImageName(image.getImageName());
                    node.setCreator(image.getCreator());
                    node.setCopyRight(image.getCopyRight());
                    node.setUri(image.getUri());
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeAlignment> getAlignmentsFromNFC(NodeFullConcept nodeFullConcept, String idTheso) {
        List<ConceptIdLabel> exactMatchs = nodeFullConcept.getExactMatchs();
        List<ConceptIdLabel> broadMatchs = nodeFullConcept.getBroadMatchs();
        List<ConceptIdLabel> narrowMatchs = nodeFullConcept.getNarrowMatchs();
        List<ConceptIdLabel> relatedMatchs = nodeFullConcept.getRelatedMatchs();
        List<ConceptIdLabel> closeMatchs = nodeFullConcept.getCloseMatchs();
        return Stream.of(
                        createAlignmentNodes(exactMatchs, idTheso, nodeFullConcept.getIdentifier(), 1, "exactMatch"),
                        createAlignmentNodes(broadMatchs, idTheso, nodeFullConcept.getIdentifier(), 3, "broadMatch"),
                        createAlignmentNodes(narrowMatchs, idTheso, nodeFullConcept.getIdentifier(), 5, "narrowMatch"),
                        createAlignmentNodes(relatedMatchs, idTheso, nodeFullConcept.getIdentifier(), 4, "relatedMatch"),
                        createAlignmentNodes(closeMatchs, idTheso, nodeFullConcept.getIdentifier(), 2, "closeMatch")
                )
                .flatMap(Collection::stream) // Aplatir la liste des listes
                .collect(Collectors.toList());
    }

    private List<NodeAlignment> createAlignmentNodes(List<ConceptIdLabel> matches, String idTheso, String idConcept, int alignmentType, String alignmentLabelType) {
        if(matches == null) return Collections.emptyList();
        return matches.stream()
                .map(conceptLabel -> {
                    NodeAlignment node = new NodeAlignment();
                    node.setUri_target(conceptLabel.getUri());
                    node.setInternal_id_thesaurus(idTheso);
                    node.setInternal_id_concept(idConcept);
                    node.setAlignement_id_type(alignmentType);
                    node.setAlignmentLabelType(alignmentLabelType);
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeGroup> getGroupFromNFC(NodeFullConcept nodeFullConcept, String idTheso, String idLang) {
        List <ConceptIdLabel> conceptIdLabels = nodeFullConcept.getMembres();
        if(conceptIdLabels == null) return Collections.emptyList();
        return conceptIdLabels.stream()
                .map(collection -> {
                    NodeGroup node = new NodeGroup();
                    node.setLexicalValue(collection.getLabel());
                    node.setIdLang(idLang);
                    var conceptGroup = new ConceptGroup();
                    conceptGroup.setIdGroup(collection.getIdentifier());
                    conceptGroup.setIdThesaurus(idTheso);
                    //conceptGroup.setIdARk(collection.get);
                    conceptGroup.setIdTypeCode("C");

                    node.setConceptGroup(conceptGroup);
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeNote> getNotesFromNFC(NodeFullConcept nodeFullConcept) {
        List<ConceptNote> conceptNotes = nodeFullConcept.getNotes();
        List<ConceptNote> conceptHistoryNotes = nodeFullConcept.getHistoryNotes();
        List<ConceptNote> conceptChangeNotes = nodeFullConcept.getChangeNotes();
        List<ConceptNote> conceptScopeNotes = nodeFullConcept.getScopeNotes();
        List<ConceptNote> conceptEditorialNotes = nodeFullConcept.getEditorialNotes();
        List<ConceptNote> conceptDefinitions = nodeFullConcept.getDefinitions();
        List<ConceptNote> conceptExamples = nodeFullConcept.getExamples();

        return Stream.of(
                        safeStream(conceptNotes, "note", nodeFullConcept.getIdentifier()),
                        safeStream(conceptHistoryNotes, "historyNote", nodeFullConcept.getIdentifier()),
                        safeStream(conceptChangeNotes, "changeNote", nodeFullConcept.getIdentifier()),
                        safeStream(conceptScopeNotes, "scopeNote", nodeFullConcept.getIdentifier()),
                        safeStream(conceptEditorialNotes, "editorialNote", nodeFullConcept.getIdentifier()),
                        safeStream(conceptDefinitions, "definition", nodeFullConcept.getIdentifier()),
                        safeStream(conceptExamples, "example", nodeFullConcept.getIdentifier())
                ).flatMap(stream -> stream) // Combine tous les flux en un seul
                .collect(Collectors.toList());
    }

    private Stream<NodeNote> safeStream(List<ConceptNote> conceptNotes, String noteType, String conceptId) {
        return conceptNotes == null ? Stream.empty() // Retourne un flux vide si la liste est null
                : conceptNotes.stream()
                .filter(Objects::nonNull) // Filtre les objets nulls
                .map(conceptNote -> createNodeNote(conceptNote, noteType, conceptId));
    }

    private NodeNote createNodeNote(ConceptNote conceptNote, String noteType, String idConcept) {
        NodeNote node = new NodeNote();
        node.setIdNote(conceptNote.getIdNote());
        node.setNoteTypeCode(noteType);
        node.setNoteSource(conceptNote.getNoteSource());
        node.setIdConcept(idConcept);
        node.setLang(conceptNote.getIdLang());
        node.setLexicalValue(conceptNote.getLabel());
        return node;
    }

    private List<NodeEM> getEMFromNFC(NodeFullConcept nodeFullConcept) {
        List<ConceptLabel> conceptLabels = nodeFullConcept.getAltLabels();
        List<ConceptLabel> conceptLabelsHidden = nodeFullConcept.getHiddenLabels();
        return Stream.concat(
                        getNOdeEM(conceptLabels),
                        getNOdeEMHidden(conceptLabelsHidden)
                )
                .collect(Collectors.toList());
    }

    private Stream<NodeEM> getNOdeEM(List<ConceptLabel> conceptLabels){
        if(conceptLabels == null) return Stream.empty();
        return conceptLabels.stream().map(conceptLabel -> {
            NodeEM node = new NodeEM();
            node.setLexicalValue(conceptLabel.getLabel());
            node.setStatus("USE");
            node.setHiden(false); // non caché pour `conceptLabels`
            node.setLang(conceptLabel.getIdLang());
            return node;
        });
    }

    private Stream<NodeEM> getNOdeEMHidden(List<ConceptLabel> conceptLabels){
        if(conceptLabels == null) return Stream.empty();
        return conceptLabels.stream().map(conceptLabel -> {
            NodeEM node = new NodeEM();
            node.setLexicalValue(conceptLabel.getLabel());
            node.setStatus("Hidden");
            node.setHiden(true); // non caché pour `conceptLabels`
            node.setLang(conceptLabel.getIdLang());
            return node;
        });
    }

    private List<NodeRT> getRTFromNFC(List<ConceptRelation> conceptRelations) {
        if(conceptRelations == null) return Collections.emptyList();
        return conceptRelations.stream()
                .map(conceptRelation -> {
                    NodeRT node = new NodeRT();
                    node.setTitle(conceptRelation.getLabel());
                    node.setIdConcept(conceptRelation.getIdConcept());
                    node.setStatus(conceptRelation.getStatus());
                    node.setRole(conceptRelation.getRole());
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeNT> getNTFromNFC(List<ConceptRelation> conceptRelations) {
        if(conceptRelations == null) return Collections.emptyList();
        return conceptRelations.stream()
                .map(conceptRelation -> {
                    NodeNT node = new NodeNT();
                    node.setTitle(conceptRelation.getLabel());
                    node.setIdConcept(conceptRelation.getIdConcept());
                    node.setStatus(conceptRelation.getStatus());
                    node.setRole(conceptRelation.getRole());
                    return node;
                })
                .collect(Collectors.toList());
    }

    private ArrayList<NodeBT> getBTFromNFC(List<ConceptRelation> conceptRelations) {
        if(conceptRelations == null) return new ArrayList<>();
        return conceptRelations.stream()
                .map(conceptRelation -> {
                    NodeBT node = new NodeBT();
                    node.setTitle(conceptRelation.getLabel());
                    node.setIdConcept(conceptRelation.getIdConcept());
                    node.setStatus(conceptRelation.getStatus());
                    node.setRole(conceptRelation.getRole());
                    node.setSelected(false);
                    return node;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Concept getConceptFromNFC(NodeFullConcept nodeFullConcept, String idTheso) {
        Concept concept = new Concept();
        concept.setIdConcept(nodeFullConcept.getIdentifier());
        concept.setIdThesaurus(idTheso);
        if (nodePreference != null && nodePreference.isUseArk()) {
            concept.setIdArk(nodeFullConcept.getPermanentId());
        }
        if (nodePreference != null && nodePreference.isUseHandle()) {
            concept.setIdHandle(nodeFullConcept.getPermanentId());
        } else if (nodePreference == null || (!nodePreference.isUseArk() && !nodePreference.isUseHandle())) {
            concept.setIdArk(nodeFullConcept.getPermanentId());
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            concept.setCreated(formatter.parse(nodeFullConcept.getCreated()));
        } catch (Exception e) {
        }
        try {
            concept.setModified(formatter.parse(nodeFullConcept.getModified()));
        } catch (Exception e) {
        }
        switch (nodeFullConcept.getResourceStatus()) {
            case SKOSProperty.CONCEPT:
                concept.setStatus("D");
                concept.setConceptType("concept");
                break;
            case SKOSProperty.DEPRECATED:
                concept.setStatus("DEP");
                concept.setDeprecated(true);
                break;
            case SKOSProperty.CANDIDATE:
                concept.setStatus("CA");
                break;
        }
        concept.setNotation(nodeFullConcept.getNotation());
        if(nodeFullConcept.getBroaders() == null || nodeFullConcept.getBroaders().isEmpty()) {
            concept.setTopConcept(true);
        } else {
            concept.setTopConcept(false);
        }
        concept.setCreatorName(nodeFullConcept.getCreatorName());
        if(nodeFullConcept.getContributorName() != null)
            concept.setContributorName(nodeFullConcept.getContributorName().toString());
        return concept;
    }

    private Term getTermFromNFC(NodeFullConcept nodeFullConcept, String idTheso) {
        if(nodeFullConcept.getPrefLabel() == null) return null;
        Term term = new Term();

        term.setIdTerm(nodeFullConcept.getPrefLabel().getIdTerm());
        term.setLexicalValue(nodeFullConcept.getPrefLabel().getLabel());
        term.setLang(nodeFullConcept.getPrefLabel().getIdLang());
        term.setIdThesaurus(idTheso);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            term.setCreated(formatter.parse(nodeFullConcept.getCreated()));
        } catch (Exception e) {
        }
        try {
            term.setModified(formatter.parse(nodeFullConcept.getModified()));
        } catch (Exception e) {
        }
        return term;
    }
}
