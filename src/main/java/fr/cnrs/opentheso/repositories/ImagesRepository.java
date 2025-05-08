package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ImageExterne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface ImagesRepository extends JpaRepository<ImageExterne, Integer> {

    List<ImageExterne> findAllByIdConceptAndIdThesaurus(String idConcept, String idThesaurus);

    @Modifying
    @Transactional
    void deleteAllByIdThesaurusAndIdConcept(String idThesaurus, String idConcept);

    @Modifying
    @Transactional
    void deleteByIdThesaurusAndIdConceptAndExternalUri(String idThesaurus, String idConcept, String uri);

}
