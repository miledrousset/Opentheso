package fr.cnrs.opentheso.ws.graphql;

import fr.cnrs.opentheso.models.concept.*;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class NodeFullConceptService {
    private final static String SEPARATEUR = "##";
    private final static String SUB_SEPARATEUR = "@@";
    private final DataSource dataSource;

    public NodeFullConceptService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public NodeFullConcept getFullConcept(String idTheso, String idConcept, String idLang, int offset, int step) {
        NodeFullConcept nodeFullConcept = null;

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from opentheso_get_concept('" + idTheso + "', '" + idConcept + "', '" + idLang + "'," + offset + "," + step + ")"
                        + " as x(URI text, resourceType varchar, localUri text, identifier varchar, permalinkId varchar,"
                        + "prefLabel varchar, altLabel varchar, hidenlabel varchar,"
                        + "prefLabel_trad varchar, altLabel_trad varchar, hiddenLabel_trad varchar, definition text, example text, editorialNote text, changeNote text,"
                        + "scopeNote text, note text, historyNote text, notation varchar, narrower text, broader text, related text, exactMatch text, "
                        + "closeMatch text, broadMatch text, relatedMatch text, narrowMatch text, gpsData text,"
                        + " membre text, created date, modified date, images text, creator text, contributor text,"
                        + "replaces text, replaced_by text, facets text, externalResources text, conceptType text);"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeFullConcept = new NodeFullConcept();

                        nodeFullConcept.setUri(resultSet.getString("URI"));

                        // Type de ressource
                        nodeFullConcept.setResourceType(SKOSProperty.CONCEPT);

                        // Type de concept
                        nodeFullConcept.setConceptType(resultSet.getString("conceptType"));

                        // Statut du concept
                        nodeFullConcept.setResourceStatus(SKOSProperty.CONCEPT);
                        if ("DEP".equalsIgnoreCase(resultSet.getString("resourceType"))) {
                            nodeFullConcept.setResourceStatus(SKOSProperty.DEPRECATED);
                        }
                        if ("CA".equalsIgnoreCase(resultSet.getString("resourceType"))) {
                            nodeFullConcept.setResourceStatus(SKOSProperty.CANDIDATE);
                        }

                        // Identifiants
                        nodeFullConcept.setIdentifier(resultSet.getString("identifier"));
                        nodeFullConcept.setPermanentId(resultSet.getString("permalinkId"));
                        nodeFullConcept.setNotation(resultSet.getString("notation"));

                        // Dates
                        nodeFullConcept.setCreated(resultSet.getString("created"));
                        nodeFullConcept.setModified(resultSet.getString("modified"));

                        // Utilisateurs
                        nodeFullConcept.setCreatorName(resultSet.getString("creator"));
                        nodeFullConcept.setContributorName(getContributors(resultSet.getString("contributor")));

                        // Labels
                        nodeFullConcept.setPrefLabel(getLabel(resultSet.getString("prefLabel"), idLang));
                        nodeFullConcept.setAltLabels(getLabels(resultSet.getString("altLabel"), idLang));
                        nodeFullConcept.setHiddenLabels(getLabels(resultSet.getString("hidenlabel"), idLang));
                        nodeFullConcept.setPrefLabelsTraduction(getLabelsTraduction(resultSet.getString("prefLabel_trad")));
                        nodeFullConcept.setAltLabelTraduction(getLabelsTraduction(resultSet.getString("altLabel_trad")));
                        nodeFullConcept.setHiddenLabelTraduction(getLabelsTraduction(resultSet.getString("hiddenLabel_trad")));

                        // Notes
                        nodeFullConcept.setDefinitions(getNotes(resultSet.getString("definition")));
                        nodeFullConcept.setExamples(getNotes(resultSet.getString("example")));
                        nodeFullConcept.setEditorialNotes(getNotes(resultSet.getString("editorialNote")));
                        nodeFullConcept.setChangeNotes(getNotes(resultSet.getString("changeNote")));
                        nodeFullConcept.setScopeNotes(getNotes(resultSet.getString("scopeNote")));
                        nodeFullConcept.setNotes(getNotes(resultSet.getString("note")));
                        nodeFullConcept.setHistoryNotes(getNotes(resultSet.getString("historyNote")));

                        // Relations
                        nodeFullConcept.setBroaders(getRelations(resultSet.getString("broader")));
                        nodeFullConcept.setNarrowers(getRelations(resultSet.getString("narrower")));
                        nodeFullConcept.setRelateds(getRelations(resultSet.getString("related")));

                        // Alignements
                        nodeFullConcept.setExactMatchs(getFromIdLabel(resultSet.getString("exactMatch")));
                        nodeFullConcept.setCloseMatchs(getFromIdLabel(resultSet.getString("closeMatch")));
                        nodeFullConcept.setBroadMatchs(getFromIdLabel(resultSet.getString("broadMatch")));
                        nodeFullConcept.setRelatedMatchs(getFromIdLabel(resultSet.getString("relatedMatch")));
                        nodeFullConcept.setNarrowMatchs(getFromIdLabel(resultSet.getString("narrowMatch")));

                        // Ressources externes
                        nodeFullConcept.setExternalResources(getFromIdLabel(resultSet.getString("externalResources")));

                        // GPS
                        nodeFullConcept.setGps(getGps(resultSet.getString("gpsData")));

                        // Membres
                        nodeFullConcept.setMembres(getFromIdLabel(resultSet.getString("membre")));

                        // Images
                        nodeFullConcept.setImages(getImages(resultSet.getString("images")));

                        // Replacements
                        nodeFullConcept.setReplaces(getFromIdLabel(resultSet.getString("replaces")));
                        nodeFullConcept.setReplacedBy(getFromIdLabel(resultSet.getString("replaced_by")));

                        // Facettes
                        nodeFullConcept.setFacets(getFromIdLabel(resultSet.getString("facets")));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(NodeFullConceptService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeFullConcept;
    }

    private List<String> getExternalResources(String textBrut) {
        List<String> resources = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    resources.add(tab);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(resources)) {
            return resources;
        }
        return null;
    }

    private List<String> getContributors(String textBrut) {
        List<String> contributors = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    contributors.add(tab);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(contributors)) {
            return contributors;
        }
        return null;
    }

    private List<ConceptImage> getImages(String textBrut) {
        List<ConceptImage> conceptImages = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptImage conceptImage = new ConceptImage();
                    if (StringUtils.isEmpty(element[0]) || "null".equalsIgnoreCase(element[0])) {
                        conceptImage.setImageName("");
                    } else {
                        conceptImage.setImageName(element[0]);
                    }
                    conceptImage.setCopyRight(element[1]);
                    conceptImage.setUri(element[2]);
                    if(element.length > 3)
                        conceptImage.setCreator(element[3]);
                    conceptImages.add(conceptImage);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptImages)) {
            return conceptImages;
        }
        return null;
    }

    private List<ConceptIdLabel> getFromIdLabel(String textBrut) {
        List<ConceptIdLabel> membres = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptIdLabel membre = new ConceptIdLabel();
                    membre.setUri(element[0]);
                    membre.setIdentifier(element[1]);
                    if(element.length > 2) {
                        if (StringUtils.isNotEmpty(element[2]) && !element[2].equalsIgnoreCase("null")) {
                            membre.setLabel(element[2]);
                        } else {
                            membre.setLabel("");
                        }
                    } else {
                        membre.setLabel("");
                    }
                    membres.add(membre);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(membres)) {
            Collections.sort(membres);
            return membres;
        }
        return null;
    }

    private List<ResourceGPS> getGps(String textBrut) {
        List<ResourceGPS> resourceGPSs = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ResourceGPS resourceGPS = new ResourceGPS();
                    resourceGPS.setLatitude(Double.valueOf(element[0]));
                    resourceGPS.setLongitude(Double.valueOf(element[1]));
                    resourceGPS.setPosition(Integer.parseInt(element[2]));
                    resourceGPSs.add(resourceGPS);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(resourceGPSs)) {
            return resourceGPSs;
        }
        return null;
    }

    private List<String> getAlignments(String textBrut) {
        List<String> uris = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    uris.add(tab);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(uris)) {
            return uris;
        }
        return null;
    }

    private List<ConceptRelation> getRelations(String textBrut) {
        List<ConceptRelation> conceptRelations = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptRelation conceptRelation = new ConceptRelation();
                    conceptRelation.setUri(element[0]);
                    conceptRelation.setRole(element[1]);
                    conceptRelation.setIdConcept(element[2]);
                    if(element.length > 3) {
                        conceptRelation.setLabel(element[3]);
                    } else {
                        conceptRelation.setLabel("");
                    }
                    conceptRelations.add(conceptRelation);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptRelations)) {
            Collections.sort(conceptRelations);
            return conceptRelations;
        }
        return null;
    }

    private List<ConceptNote> getNotes(String textBrut) {
        List<ConceptNote> conceptNotes = new ArrayList<>();


        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptNote conceptNote = new ConceptNote();
                    conceptNote.setIdNote(Integer.parseInt(element[0]));
                    conceptNote.setLabel(fr.cnrs.opentheso.utils.StringUtils.normalizeStringForXml(element[1]));
                    conceptNote.setIdLang(element[2]);
                    if(element.length > 3)
                        conceptNote.setNoteSource(element[3]);
                    conceptNotes.add(conceptNote);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptNotes)) {
            return conceptNotes;
        }
        return null;
    }

    private ConceptLabel getLabel(String labelBrut, String idLang) {
        ConceptLabel conceptLabel = null;

        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    conceptLabel = new ConceptLabel();
                    conceptLabel.setIdLang(idLang);
                    conceptLabel.setLabel(element[0]);
                    conceptLabel.setIdTerm(element[1]);
                    conceptLabel.setId(Integer.parseInt(element[2]));
                } catch (Exception e) {
                }
            }
        }
        return conceptLabel;
    }

    private List<String> getLabelsWithoutSub(String labelBrut) {
        List<String> labels = new ArrayList<>();

        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    labels.add(tab);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(labels)) {
            return labels;
        }
        return null;
    }

    private List<ConceptLabel> getLabels(String labelBrut, String idLang) {
        List<ConceptLabel> conceptLabels = new ArrayList<>();

        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptLabel conceptLabel = new ConceptLabel();
                    conceptLabel.setIdLang(idLang);
                    conceptLabel.setLabel(element[0]);
                    conceptLabel.setIdTerm(element[1]);
                    if(element.length > 2)
                        conceptLabel.setId(Integer.parseInt(element[2]));

                    conceptLabels.add(conceptLabel);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptLabels)) {
            Collections.sort(conceptLabels);
            return conceptLabels;
        }
        return null;
    }

    private List<ConceptLabel> getLabelsTraduction(String labelBrut) {
        List<ConceptLabel> conceptLabels = new ArrayList<>();

        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptLabel conceptLabel = new ConceptLabel();
                    conceptLabel.setIdTerm(element[0]);
                    conceptLabel.setLabel(element[1]);
                    conceptLabel.setIdLang(element[2]);
                    conceptLabel.setId(Integer.parseInt(element[3]));
                    if(element.length > 4 )
                        conceptLabel.setCodeFlag(element[4]);
                    conceptLabels.add(conceptLabel);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptLabels)) {
            Collections.sort(conceptLabels);
            return conceptLabels;
        }
        return null;
    }
}
