package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ImageExterne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
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

    @Modifying
    @Transactional
    void deleteByIdThesaurus(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE ImageExterne t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

    @Procedure(procedureName = "opentheso_add_external_images")
    void addExternalImages(@Param("id_theso") String idTheso, @Param("identifier") String identifier,
                           @Param("id_user") int idUser, @Param("images_str") String imagesStr);

}
