package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.ThesaurusAlignementSource;
import fr.cnrs.opentheso.models.AlignementSourceProjection;
import fr.cnrs.opentheso.models.alignment.AlignementSource;
import fr.cnrs.opentheso.repositories.AlignementSourceRepository;
import fr.cnrs.opentheso.repositories.ThesaurusAlignementSourceRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class AlignmentSourceService {

    private final AlignementSourceRepository alignementSourceRepository;
    private final ThesaurusAlignementSourceRepository thesaurusAlignementSourceRepository;


    public List<AlignementSource> getAllAlignementSources() {

        log.info("Recherche de tous les sources d'alignement disponible");
        var alignmentSources = alignementSourceRepository.findAll();

        if (CollectionUtils.isEmpty(alignmentSources)) {
            log.info("Aucune source d'alignement n'est trouvée dans la base");
            return new ArrayList<>();
        } else {
            log.info("{} sources d'alignement trouvée", alignmentSources.size());
            return alignmentSources.stream().map(element ->
                    AlignementSource.builder()
                            .id(element.getId())
                            .source(element.getSource())
                            .requete(element.getRequete())
                            .typeRequete(element.getTypeRqt())
                            .alignement_format(element.getAlignementFormat())
                            .description(element.getDescription())
                            .source_filter(element.getSourceFilter())
                            .isGps(element.getGps())
                            .build()
            ).toList();
        }
    }

    public List<AlignementSource> getAlignementSources(String idThesaurus) {

        log.info("Recherche des sources d'alignement rattachées au thésaurus {}", idThesaurus);
        List<AlignementSourceProjection> projections = alignementSourceRepository.findAllByThesaurus(idThesaurus);

        if (CollectionUtils.isNotEmpty(projections)) {
            log.info("{} sources d'alignement trouvée pour le thésaurus {}", projections.size(), idThesaurus);
            return projections.stream().map(element ->
                AlignementSource.builder()
                        .id(element.getId())
                        .source(element.getSource())
                        .requete(element.getRequete())
                        .typeRequete(element.getTypeRequete())
                        .alignement_format(element.getAlignement_format())
                        .description(element.getDescription())
                        .source_filter(element.getSource_filter())
                        .isGps(element.getGps())
                        .build()
                ).toList();
        } else {
            log.info("Aucune source d'alignement n'est trouvée pour le thésaurus {}", idThesaurus);
            return List.of();
        }
    }

    public void addSourceAlignementToThesaurus(String idThesaurus, int idAlignement) {

        log.info("Ajout de la source d'alignement {} au thésaurus {}", idAlignement, idThesaurus);
        thesaurusAlignementSourceRepository.save(ThesaurusAlignementSource.builder()
                .idAlignementSource(idAlignement)
                .idThesaurus(idThesaurus)
                .build());

    }

    public AlignementSource getAlignementSourceById(int idSourceAlignement) {

        log.info("Rechercher de la source d'alignement {}", idSourceAlignement);
        var sourceAlignement = alignementSourceRepository.findById(idSourceAlignement);
        if (sourceAlignement.isEmpty()) {
            log.error("Aucune source d'alignement n'est trouvée avec l'id {}", idSourceAlignement);
            return null;
        }

        log.info("Formatage de la source d'alignement trouvée");
        return AlignementSource.builder()
                .id(sourceAlignement.get().getId())
                .source(sourceAlignement.get().getSource())
                .requete(sourceAlignement.get().getRequete())
                .typeRequete(sourceAlignement.get().getTypeRqt())
                .alignement_format(sourceAlignement.get().getAlignementFormat())
                .description(sourceAlignement.get().getDescription())
                .source_filter(sourceAlignement.get().getSourceFilter())
                .isGps(sourceAlignement.get().getGps())
                .build();
    }

    @Transactional
    public boolean addNewAlignmentSource(AlignementSource alignement, String idThesaurus, int idUser) {

        log.info("Enregistrement de la source d'alignement dans la base de données");
        var alignementSourceSaved = alignementSourceRepository.save(fr.cnrs.opentheso.entites.AlignementSource.builder()
                .source(alignement.getSource())
                .requete(alignement.getRequete())
                .typeRqt(alignement.getTypeRequete())
                .alignementFormat(alignement.getAlignement_format())
                .description(alignement.getDescription())
                .idUser(idUser)
                .gps(false)
                .sourceFilter("")
                .build());

        if (StringUtils.isNotEmpty(idThesaurus)) {
            addSourceAlignementToThesaurus(idThesaurus, alignementSourceSaved.getId());
        }

        return true;
    }

    public boolean updateAlignmentSource(fr.cnrs.opentheso.models.alignment.AlignementSource alignementSource) {

        log.info("Mise à jour de la source d'alignement {}", alignementSource.getId());
        var alignementSourceFound = alignementSourceRepository.findById(alignementSource.getId());
        if (alignementSourceFound.isPresent()) {
            log.info("Source d'alignement trouvée dans la base de données");
            alignementSourceFound.get().setSource(alignementSource.getSource());
            alignementSourceFound.get().setRequete(alignementSource.getRequete());
            alignementSourceFound.get().setDescription(alignementSource.getDescription());
            alignementSourceRepository.save(alignementSourceFound.get());
            return true;
        } else {
            log.error("Aucune source d'alignement n'est trouvé avec l'id {}", alignementSource.getId());
            return false;
        }
    }

    public boolean deleteAlignmentSource(int id) {
        try {
            return alignementSourceRepository.deleteByIdAlignementSource(id) > 0;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la source d’alignement avec id : " + id, e);
            return false;
        }
    }

}
