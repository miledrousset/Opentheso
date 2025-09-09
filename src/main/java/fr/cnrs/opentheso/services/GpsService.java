package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.models.nodes.NodeGps;
import fr.cnrs.opentheso.repositories.ConceptRepository;
import fr.cnrs.opentheso.repositories.GpsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class GpsService {

    private final GpsRepository gpsRepository;
    private final ConceptRepository conceptRepository;


    public boolean insertCoordinates(String idConcept, String idThesaurus, double latitude, double longitude) {

        var result = gpsRepository.findByIdConceptAndIdThesoOrderByPosition(idConcept, idThesaurus);
        if (CollectionUtils.isNotEmpty(result)) {
            return gpsRepository.updateCoordinates(idConcept, idThesaurus, latitude, longitude) > 0;
        } else {
            var gpsSaved = gpsRepository.save(Gps.builder().idTheso(idThesaurus)
                    .idConcept(idConcept)
                    .longitude(longitude)
                    .latitude(latitude)
                    .build());

            if (ObjectUtils.isEmpty(gpsSaved)) {
                return false;
            }

            return conceptRepository.setGpsTag(true, idConcept, idThesaurus) > 0;
        }
    }

    public void saveNewGps(Gps gps) {
        gpsRepository.save(gps);
    }

    public void saveNewGps(String idConcept, String idThesaurus, List<NodeGps> nodeGpses) {

        nodeGpses.forEach(element -> saveNewGps(Gps.builder()
                .idTheso(idThesaurus)
                .idConcept(idConcept)
                .latitude(element.getLatitude())
                .longitude(element.getLongitude())
                .position(element.getPosition())
                .build()));
    }

    public List<Gps> findByIdConceptAndIdThesoOrderByPosition(String idConcept, String idThesaurus) {
        return gpsRepository.findByIdConceptAndIdThesoOrderByPosition(idConcept, idThesaurus);
    }

    public void deleteGpsByConceptIdAndThesaurusId(String conceptId, String thesaurusId) {
        gpsRepository.deleteByIdConceptAndIdTheso(conceptId, thesaurusId);
    }

    public void deleteGps(Gps gps) {
        gpsRepository.deleteById(gps.getId());
    }

    public void deleteGpsByThesaurus(String idThesaurus) {

        log.debug("Suppression de tous les GPS présents dans le thésaurus id {}", idThesaurus);
        gpsRepository.deleteByIdTheso(idThesaurus);
    }

    public void updateThesaurusId(String oldThesaurusId, String newThesaurusId) {

        log.debug("Mise à jour du thésaurus id pour les GPS dont l'id du thésaurus est {}", oldThesaurusId);
        gpsRepository.updateThesaurusId(newThesaurusId, oldThesaurusId);
    }

}
