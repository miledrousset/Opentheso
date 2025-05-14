package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.Alignement;
import fr.cnrs.opentheso.entites.AlignementType;
import fr.cnrs.opentheso.models.NodeAlignmentProjection;
import fr.cnrs.opentheso.models.NodeIdValueProjection;
import fr.cnrs.opentheso.models.alignment.AlignementElement;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.alignment.NodeAlignmentSmall;
import fr.cnrs.opentheso.models.alignment.NodeAlignmentType;
import fr.cnrs.opentheso.models.alignment.NodeSelectedAlignment;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.repositories.AlignementRepository;
import fr.cnrs.opentheso.repositories.AlignementSourceRepository;
import fr.cnrs.opentheso.repositories.AlignementTypeRepository;
import fr.cnrs.opentheso.utils.StringUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class AlignmentService {

    private final AlignementRepository alignementRepository;
    private final AlignementTypeRepository alignementTypeRepository;
    private final AlignementSourceRepository alignementSourceRepository;


    public List<NodeAlignment> getAllAlignmentOfConcept(String idConcept, String idThesaurus) {

        log.info("Rechercher de tous les alignements du concept {} (thésaurus id {})", idConcept, idThesaurus);
        List<NodeAlignmentProjection> alignements = alignementRepository.findAllAlignmentsByConceptAndThesaurus(idConcept, idThesaurus);

        if (CollectionUtils.isEmpty(alignements)) {
            log.info("Aucun alignement n'est trouvé pour le concept {} (thésaurus id {})", idConcept, idThesaurus);
            return new ArrayList<>();
        }

        log.info("{} alignements trouvé pour le concept {} (thésaurus id {})", alignements.size(), idConcept, idThesaurus);
        return alignements.stream().map(element ->
            NodeAlignment.builder()
                    .id_alignement(element.getId())
                    .created(element.getCreated())
                    .modified(element.getModified())
                    .id_author(element.getAuthor())
                    .thesaurus_target(element.getThesaurus_target())
                    .concept_target(element.getConcept_target())
                    .uri_target(element.getUri_target())
                    .alignement_id_type(element.getAlignement_id_type())
                    .internal_id_concept(element.getInternal_id_concept())
                    .internal_id_thesaurus(element.getInternal_id_thesaurus())
                    .alignmentLabelType(element.getLabel())
                    .alignmentLabelSkosType(element.getLabel_skos())
                    .alignementLocalValide(element.getUrl_available())
                    .id_source(element.getId_alignement_source())
                    .build()
        ).toList();
    }

    public boolean addNewAlignment(NodeAlignment nodeAlignment) {

        return addNewAlignment(nodeAlignment.getId_author(), nodeAlignment.getConcept_target(),
                nodeAlignment.getThesaurus_target(), nodeAlignment.getUri_target(), nodeAlignment.getAlignement_id_type(),
                nodeAlignment.getInternal_id_concept(), nodeAlignment.getInternal_id_thesaurus(), nodeAlignment.getId_source());
    }

    public boolean addNewAlignment(int author, String conceptTarget, String thesaurusTarget, String uriTarget,
                                   int idTypeAlignment, String idConcept, String idThesaurus, int id_alignement_source) {

        thesaurusTarget = StringUtils.convertString(thesaurusTarget);

        if (alignementRepository.existsByConceptThesaurusTypeAndUri(idThesaurus, idConcept, idTypeAlignment, uriTarget)) {
            log.info("L'alignement existe déjà dans la base de données, cas de mise à jour !");
            var alignement = AlignementElement.builder()
                    .idAlignment(idTypeAlignment)
                    .alignement_id_type(idTypeAlignment)
                    .conceptTarget(conceptTarget)
                    .thesaurus_target(thesaurusTarget)
                    .targetUri(uriTarget)
                    .build();
            updateAlignement(alignement, idConcept, idThesaurus);
        } else {
            log.info("Ajout d'un nouveau alignement !");

            log.info("Rechercher du type d'alignement");
            var alignementType = alignementTypeRepository.findById(idTypeAlignment);
            if (alignementType.isEmpty()) {
                log.error("Aucun type alignement n'est trouvé avec l'id {}", idTypeAlignment);
                return false;
            }

            log.info("Rechercher de la source d'alignement");
            var alignementSource = alignementSourceRepository.findById(id_alignement_source);
            if (alignementSource.isEmpty()) {
                log.error("Aucune source d'alignement n'est trouvé avec l'id {}", id_alignement_source);
                return false;
            }

            log.info("Formatage des données target");
            conceptTarget = fr.cnrs.opentheso.utils.StringUtils.convertString(conceptTarget);
            uriTarget = fr.cnrs.opentheso.utils.StringUtils.convertString(uriTarget);

            log.info("Enregistrement dans la base de données");
            alignementRepository.save(Alignement.builder()
                    .author(author)
                    .conceptTarget(conceptTarget)
                    .thesaurusTarget(thesaurusTarget)
                    .uriTarget(uriTarget)
                    .alignementType(alignementType.get())
                    .internalIdConcept(idConcept)
                    .internalIdThesaurus(idThesaurus)
                    .alignementSource(alignementSource.get())
                    .build());
        }

        return true;
    }

    public boolean updateAlignement(AlignementElement alignementElement, String idThesaurus, String idConcept) {

        var alignement = alignementRepository.findByInternalIdThesaurusAndInternalIdConceptAndId(idThesaurus, idConcept,
                alignementElement.getIdAlignment());
        if (alignement.isEmpty()) {
            log.error("Aucun alignement n'est trouvé avec l'id {}", alignementElement.getIdAlignment());
            return false;
        }

        var alignementType = alignementTypeRepository.findById(alignementElement.getAlignement_id_type());
        if (alignementType.isEmpty()) {
            log.error("Aucun type alignement n'est trouvé avec l'id {}", alignementElement.getAlignement_id_type());
            return false;
        }

        log.info("Début de la mise à jour de l'alignement {} dans la base de donnée", alignementElement.getIdAlignment());

        var uriTarget = fr.cnrs.opentheso.utils.StringUtils.convertString(alignementElement.getTargetUri());
        var conceptTarget = fr.cnrs.opentheso.utils.StringUtils.convertString(alignementElement.getConceptTarget());

        alignement.get().setConceptTarget(conceptTarget);
        alignement.get().setThesaurusTarget(alignementElement.getThesaurus_target());
        alignement.get().setUriTarget(uriTarget);
        alignement.get().setAlignementType(alignementType.get());
        alignement.get().setModified(new Date());
        alignementRepository.save(alignement.get());

        log.info("Fin de la mise à jour de l'alignement {} dans la base de donnée", alignementElement.getIdAlignment());
        return true;
    }

    public boolean deleteAlignment(int idAlignment, String idThesaurus) {
        try {
            return alignementRepository.deleteByIdAndThesaurus(idAlignment, idThesaurus) > 0;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l’alignement " + idAlignment, e);
            return false;
        }
    }

    public boolean deleteAlignmentByUri(String uri, String idConcept, String idThesaurus) {
        try {
            return alignementRepository.deleteByUriAndConceptAndThesaurus(uri, idConcept, idThesaurus) > 0;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l’alignement avec URI : " + uri, e);
            return false;
        }
    }

    public void deleteAlignmentOfConceptByType(String idConcept, String idThesaurus, int typeId) {
        try {
            alignementRepository.deleteByConceptThesaurusAndType(idConcept, idThesaurus, typeId);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression des alignements du concept : " + idConcept, e);
        }
    }

    public boolean deleteAlignmentOfConcept(String idConcept, String idThesaurus) {
        try {
            return alignementRepository.deleteByConceptAndThesaurus(idConcept, idThesaurus) > 0;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression des alignements du concept : " + idConcept, e);
            return false;
        }
    }

    public List<NodeAlignmentType> searchAllAlignementTypes() {
        log.info("Recherche de la liste de tous les types d'alignements");
        var alignementTypes = alignementTypeRepository.findAll();

        if (CollectionUtils.isEmpty(alignementTypes)) {
            log.info("{} types d'alignements trouvés !", alignementTypes.size());
            return alignementTypes.stream()
                    .map(element -> NodeAlignmentType.builder()
                            .id(element.getId())
                            .isocode(element.getIsoCode())
                            .label(element.getLabel())
                            .labelSkos(element.getLabelSkos())
                            .build())
                    .collect(Collectors.toList());
        } else {
            log.warn("Aucun type d'alignement trouvé !");
            return List.of();
        }
    }

    public HashMap<String, String> getAlignmentTypes() {

        log.info("Rechercher des types d'alignements disponibles");
        var alignementTypes = alignementTypeRepository.findAll();

        if (CollectionUtils.isEmpty(alignementTypes)) {
            log.info("Aucun type d'alignement n'est trouvé !");
            return new HashMap<>();
        }

        log.info("{} types d'alignement trouvés !", alignementTypes.size());
        HashMap<String, String> result = new HashMap<>();
        for(AlignementType alignementType : alignementTypes) {
            result.put(String.valueOf(alignementType.getId()), alignementType.getLabelSkos());
        }
        return result;
    }

    public List<NodeAlignmentSmall> getAllAlignmentsOfConcept(String idConcept, String idThesaurus) {

        log.info("Recherche de tous les alignements d'un concept {}", idConcept);
        var alignements = alignementRepository.findAllAlignmentsByConceptAndThesaurus(idConcept, idThesaurus);

        if (CollectionUtils.isEmpty(alignements)) {
            log.info("Aucun alignement n'est trouvé pour le concept id {} and thésaurus {}", idConcept, idThesaurus);
            return List.of();
        }

        log.info("{} alignements trouvés pour le concept id {}", alignements.size(), idConcept);
        return alignements.stream()
                .map(element -> NodeAlignmentSmall.builder()
                        .uri_target(element.getUri_target())
                        .alignement_id_type(element.getAlignement_id_type())
                        .build())
                .toList();
    }

    public boolean updateAlignmentUrlStatut(int idAlignment, boolean newStatut, String idConcept, String idThesaurus) {

        log.info("Mise à jour du status de l'URL de l'alignement id {}, id Concept {} et id thesaurus {}", idAlignment, idConcept, idThesaurus);
        var alignement = alignementRepository.findByInternalIdThesaurusAndInternalIdConceptAndId(idThesaurus, idConcept, idAlignment);

        if(alignement.isEmpty()) {
            log.error("L'alignement {} n'existe pas !", idAlignment);
            return false;
        }

        log.info("Mise à jour de l'alignement dans la base");
        alignement.get().setUrlAvailable(newStatut);
        alignementRepository.save(alignement.get());
        return true;
    }

    public List<NodeSelectedAlignment> getSelectedAlignementOfThisThesaurus(String idThesaurus) {

        log.info("Rechercher des alignements d'un thesaurus {}", idThesaurus);
        var alignements = alignementRepository.findSelectedAlignmentsByThesaurus(idThesaurus);

        if (CollectionUtils.isEmpty(alignements)) {
            log.info("Aucun alignement n'est trouvé pour le thésaurus {}", idThesaurus);
            return List.of();
        }

        log.info("{} alignements sont trouvés pour le thésaurus {}", alignements.size(), idThesaurus);
        return alignements.stream()
                .map(element -> NodeSelectedAlignment.builder()
                        .idAlignmentSource(element.getId_alignement_source())
                        .sourceLabel(element.getSource())
                        .sourceDescription(element.getDescription())
                        .isSelected(true)
                        .build())
                .toList();
    }

    public List<NodeIdValue> getLinkedConceptsWithOntome(String idThesaurus, String cidocClass) {

        log.info("Rechercher des relations vers les concepts dans le thésaurus id {} avec Ontome", idThesaurus);
        var alignements = alignementRepository.findLinkedConceptsWithOntome(idThesaurus, cidocClass);
        return formatAlignements(alignements, idThesaurus);
    }

    public List<NodeIdValue> getAllLinkedConceptsWithOntome(String idThesaurus) {

        log.info("Rechercher des relations vers les concepts dans le thésaurus id {} avec Ontome", idThesaurus);
        var alignements = alignementRepository.findAllLinkedConceptsWithOntome(idThesaurus);
        return formatAlignements(alignements, idThesaurus);
    }

    private List<NodeIdValue> formatAlignements(List<NodeIdValueProjection> alignements, String idThesaurus) {

        if (CollectionUtils.isEmpty(alignements)) {
            log.info("Aucun alignement n'est trouvé pour le thésaurus {}", idThesaurus);
            return List.of();
        }

        log.info("{} alignements sont trouvés pour le thésaurus {}", alignements.size(), idThesaurus);
        return alignements.stream()
                .map(element -> NodeIdValue.builder()
                        .id(element.getInternal_id_concept())
                        .value(element.getUri_target())
                        .build())
                .toList();
    }

}
