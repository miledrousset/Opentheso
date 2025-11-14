package fr.cnrs.opentheso.repositories;

import java.util.List;
import fr.cnrs.opentheso.entites.ExternalResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface ExternalResourcesRepository extends JpaRepository<ExternalResource, Integer> {

    List<ExternalResource> findAllByIdConceptAndIdThesaurus(String idConcept, String idTheso);

    @Modifying
    @Transactional
    @Query("UPDATE ExternalResource e SET e.externalUri = :newExternalUri, e.idUser = :idUser, e.description = :description " +
            "WHERE e.idConcept = :idConcept AND e.idThesaurus = :idThesaurus AND e.externalUri = :oldExternalUri")
    int updateExternalResource(@Param("newExternalUri") String newExternalUri,
                         @Param("idUser") String idUser,
                         @Param("description") String description,
                         @Param("idConcept") String idConcept,
                         @Param("idThesaurus") String idThesaurus,
                         @Param("oldExternalUri") String oldExternalUri);

    @Modifying
    @Transactional
    @Query("DELETE FROM ExternalResource e WHERE e.idThesaurus = :idThesaurus AND e.idConcept  = :idConcept AND e.externalUri  = :externalUri")
    int deleteExternalResource(@Param("idThesaurus") String idThesaurus,
                               @Param("idConcept") String idConcept,
                               @Param("externalUri") String externalUri);
}
