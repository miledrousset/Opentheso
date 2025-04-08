package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ImageExterne;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ImagesRepository extends JpaRepository<ImageExterne, Integer> {

    List<ImageExterne> findAllByIdConceptAndIdThesaurus(String idConcept, String idThesaurus);

    void deleteAllByIdThesaurusAndIdConcept(String idThesaurus, String idConcept);

    void deleteByIdThesaurusAndIdConceptAndExternalUri(String idThesaurus, String idConcept, String uri);

}
