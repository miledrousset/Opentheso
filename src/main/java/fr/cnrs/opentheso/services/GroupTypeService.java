package fr.cnrs.opentheso.services;

import fr.cnrs.opentheso.entites.ConceptGroupType;
import fr.cnrs.opentheso.repositories.ConceptGroupRepository;
import fr.cnrs.opentheso.repositories.ConceptGroupTypeRepository;

import jakarta.faces.model.SelectItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import java.util.List;


@Slf4j
@Service
@AllArgsConstructor
public class GroupTypeService {

    private final ConceptGroupRepository conceptGroupRepository;
    private final ConceptGroupTypeRepository conceptGroupTypeRepository;


    public boolean updateTypeGroup(String type, String idThesaurus, String idGroup) {

        log.info("Mise à jour du type de group id {}", idGroup);

        var conceptGroup = conceptGroupRepository.findByIdGroupAndIdThesaurus(idGroup.toLowerCase(), idThesaurus);
        if (conceptGroup.isEmpty()) {
            log.error("Aucun concept group n'est trouvé avec l'id Group {}", idGroup);
            return false;
        }

        conceptGroup.get().setIdTypeCode(type);
        conceptGroupRepository.save(conceptGroup.get());
        log.info("Fin de la mise à jour du type de group id {}", idGroup);
        return true;
    }

    public List<SelectItem> getAllGroupType() {

        log.info("Recherche de tous les groups disponible");
        var groupTypes = conceptGroupTypeRepository.findAll();
        if(CollectionUtils.isEmpty(groupTypes)) {
            log.warn("Aucun type de group n'est disponible");
            return List.of();
        }

        log.info("{} types de group trouvé", groupTypes.size());
        return groupTypes.stream().map(element -> {
            var item = new SelectItem();
            item.setLabel(element.getLabel());
            item.setValue(element.getCode());
            return item;
        }).toList();
    }

    public ConceptGroupType getGroupType(String codeType) {

        log.info("Chercher le group type à partir du code {}", codeType);
        var groupType = conceptGroupTypeRepository.findByCode(codeType);
        if (groupType.isEmpty()) {
            log.error("Aucun type de group n'est trouvé à partir du type {}", codeType);
            return null;
        }

        return groupType.get();
    }
}
