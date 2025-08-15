package fr.cnrs.opentheso.mappers;

import fr.cnrs.opentheso.entites.ConceptGroup;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.concept.Concept;
import fr.cnrs.opentheso.models.concept.ConceptIdLabel;
import fr.cnrs.opentheso.models.concept.ConceptImage;
import fr.cnrs.opentheso.models.concept.ConceptLabel;
import fr.cnrs.opentheso.models.concept.ConceptNote;
import fr.cnrs.opentheso.models.concept.ConceptRelation;
import fr.cnrs.opentheso.models.concept.NodeConcept;
import fr.cnrs.opentheso.models.concept.NodeFullConcept;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.terms.NodeNT;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import fr.cnrs.opentheso.models.terms.Term;
import fr.cnrs.opentheso.services.PreferenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@AllArgsConstructor
public class NodeFullConceptMapper {

    private final PreferenceService preferenceService;


    public NodeConcept getConceptFromNodeFullConcept(NodeFullConcept nodeFullConcept, String idThesaurus, String idLang) {
        NodeConcept nodeConcept = new NodeConcept();
        // récupération des BT
        nodeConcept.setNodeBT(getBTFromNFC(nodeFullConcept.getBroaders()));
        //récupération du Concept
        nodeConcept.setConcept(getConceptFromNFC(nodeFullConcept, idThesaurus));
        //récupération du Terme
        nodeConcept.setTerm(getTermFromNFC(nodeFullConcept, idThesaurus));
        //récupération des termes spécifiques
        nodeConcept.setNodeNT(getNTFromNFC(nodeFullConcept.getNarrowers()));
        //récupération des termes associés
        nodeConcept.setNodeRT(getRTFromNFC(nodeFullConcept.getRelateds()));
        //récupération des Non Prefered Term
        nodeConcept.setNodeEM(getEMFromNFC(nodeFullConcept));
        //récupération des notes
        nodeConcept.setNodeNotes(getNotesFromNFC(nodeFullConcept));
        //récupération des collections
        nodeConcept.setNodeConceptGroup(getGroupFromNFC(nodeFullConcept, idThesaurus, idLang));
        // récupération des alignements
        nodeConcept.setNodeAlignments(getAlignmentsFromNFC(nodeFullConcept, idThesaurus));
        // récupération des images
        nodeConcept.setNodeimages(getImagesFromNFC(nodeFullConcept, idThesaurus));
        //gestion des ressources externes
        nodeConcept.setNodeExternalResources(getExternalResourcesFromNFC(nodeFullConcept, idThesaurus));
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

    private List<NodeImage> getExternalResourcesFromNFC(NodeFullConcept nodeFullConcept, String idThesaurus){
        List<ConceptIdLabel> resources = nodeFullConcept.getExternalResources();
        if(resources == null) return Collections.emptyList();
        return resources.stream()
                .map(resource -> {
                    NodeImage node = new NodeImage();
                    node.setIdConcept(nodeFullConcept.getIdentifier());
                    node.setIdThesaurus(idThesaurus);
                    node.setUri(resource.getUri());
                    node.setImageName(resource.getLabel());
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeImage> getImagesFromNFC(NodeFullConcept nodeFullConcept, String idThesaurus){
        List<ConceptImage> conceptImages = nodeFullConcept.getImages();
        if(conceptImages == null) return Collections.emptyList();
        return conceptImages.stream()
                .map(image -> {
                    NodeImage node = new NodeImage();
                    node.setId(image.getId());
                    node.setIdConcept(nodeFullConcept.getIdentifier());
                    node.setIdThesaurus(idThesaurus);
                    node.setImageName(image.getImageName());
                    node.setCreator(image.getCreator());
                    node.setCopyRight(image.getCopyRight());
                    node.setUri(image.getUri());
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeAlignment> getAlignmentsFromNFC(NodeFullConcept nodeFullConcept, String idThesaurus) {
        List<ConceptIdLabel> exactMatchs = nodeFullConcept.getExactMatchs();
        List<ConceptIdLabel> broadMatchs = nodeFullConcept.getBroadMatchs();
        List<ConceptIdLabel> narrowMatchs = nodeFullConcept.getNarrowMatchs();
        List<ConceptIdLabel> relatedMatchs = nodeFullConcept.getRelatedMatchs();
        List<ConceptIdLabel> closeMatchs = nodeFullConcept.getCloseMatchs();
        return Stream.of(
                        createAlignmentNodes(exactMatchs, idThesaurus, nodeFullConcept.getIdentifier(), 1, "exactMatch"),
                        createAlignmentNodes(broadMatchs, idThesaurus, nodeFullConcept.getIdentifier(), 3, "broadMatch"),
                        createAlignmentNodes(narrowMatchs, idThesaurus, nodeFullConcept.getIdentifier(), 5, "narrowMatch"),
                        createAlignmentNodes(relatedMatchs, idThesaurus, nodeFullConcept.getIdentifier(), 4, "relatedMatch"),
                        createAlignmentNodes(closeMatchs, idThesaurus, nodeFullConcept.getIdentifier(), 2, "closeMatch")
                )
                .flatMap(Collection::stream) // Aplatir la liste des listes
                .collect(Collectors.toList());
    }

    private List<NodeAlignment> createAlignmentNodes(List<ConceptIdLabel> matches, String idThesaurus,
                                                     String idConcept, int alignmentType, String alignmentLabelType) {
        if(matches == null) return Collections.emptyList();
        return matches.stream()
                .map(conceptLabel -> {
                    NodeAlignment node = new NodeAlignment();
                    node.setUri_target(conceptLabel.getUri());
                    node.setInternal_id_thesaurus(idThesaurus);
                    node.setInternal_id_concept(idConcept);
                    node.setAlignement_id_type(alignmentType);
                    node.setAlignmentLabelType(alignmentLabelType);
                    return node;
                })
                .collect(Collectors.toList());
    }

    private List<NodeGroup> getGroupFromNFC(NodeFullConcept nodeFullConcept, String idThesaurus, String idLang) {
        List <ConceptIdLabel> conceptIdLabels = nodeFullConcept.getMembres();
        if(conceptIdLabels == null) return Collections.emptyList();
        return conceptIdLabels.stream()
                .map(collection -> {
                    NodeGroup node = new NodeGroup();
                    node.setLexicalValue(collection.getLabel());
                    node.setIdLang(idLang);
                    var conceptGroup = new ConceptGroup();
                    conceptGroup.setIdGroup(collection.getIdentifier());
                    conceptGroup.setIdThesaurus(idThesaurus);
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

        return Stream.of(safeStream(conceptNotes, "note", nodeFullConcept.getIdentifier()),
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

    private Concept getConceptFromNFC(NodeFullConcept nodeFullConcept, String idThesaurus) {

        Concept concept = new Concept();
        concept.setIdConcept(nodeFullConcept.getIdentifier());
        concept.setIdThesaurus(idThesaurus);

        var preferences = preferenceService.getThesaurusPreferences(idThesaurus);
        if (preferences != null && preferences.isUseArk()) {
            concept.setIdArk(nodeFullConcept.getPermanentId());
        }

        if (preferences != null && preferences.isUseHandle()) {
            concept.setIdHandle(nodeFullConcept.getPermanentId());
        } else if (preferences == null || !preferences.isUseArk()) {
            concept.setIdArk(nodeFullConcept.getPermanentId());
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            if (nodeFullConcept.getCreated() != null && !nodeFullConcept.getCreated().isEmpty()) {
                concept.setCreated(formatter.parse(nodeFullConcept.getCreated()));
            }

            if (nodeFullConcept.getModified() != null  && !nodeFullConcept.getModified().isEmpty()) {
                concept.setModified(formatter.parse(nodeFullConcept.getModified()));
            }
        } catch (Exception e) {
            log.error("Erreur pendant la conversation des dates du concept");
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
        concept.setTopConcept(nodeFullConcept.getBroaders() == null || nodeFullConcept.getBroaders().isEmpty());
        concept.setCreatorName(nodeFullConcept.getCreatorName());
        if(nodeFullConcept.getContributorName() != null)
            concept.setContributorName(nodeFullConcept.getContributorName().toString());
        return concept;
    }

    private Term getTermFromNFC(NodeFullConcept nodeFullConcept, String idThesaurus) {
        if(nodeFullConcept.getPrefLabel() == null) return null;

        Term term = new Term();
        term.setIdTerm(nodeFullConcept.getPrefLabel().getIdTerm());
        term.setLexicalValue(nodeFullConcept.getPrefLabel().getLabel());
        term.setLang(nodeFullConcept.getPrefLabel().getIdLang());
        term.setIdThesaurus(idThesaurus);
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            if (nodeFullConcept.getCreated() != null) {
                term.setCreated(formatter.parse(nodeFullConcept.getCreated()));
            }

            if (nodeFullConcept.getModified() != null) {
                term.setModified(formatter.parse(nodeFullConcept.getModified()));
            }
        } catch (Exception e) {
            log.error("Erreur pendant la conversation des dates du term");
        }
        return term;
    }
}
