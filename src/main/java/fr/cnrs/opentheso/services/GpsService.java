package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.models.nodes.NodeGps;
import fr.cnrs.opentheso.repositories.ConceptRepository;
import fr.cnrs.opentheso.repositories.GpsRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import java.util.List;


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

            return conceptRepository.setGpstTag(true, idConcept, idThesaurus) > 0;
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

}
