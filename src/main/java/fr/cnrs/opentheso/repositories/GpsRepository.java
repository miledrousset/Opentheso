package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Gps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


public interface GpsRepository extends JpaRepository<Gps, Integer> {

    List<Gps> findByIdConceptAndIdThesoOrderByPosition(String idConcept, String idTheso);

    @Modifying
    @Transactional
    @Query("DELETE FROM Gps g WHERE g.idConcept = :idConcept AND g.idTheso = :idTheso")
    void deleteByIdConceptAndIdTheso(@Param("idConcept") String idConcept, @Param("idTheso") String idTheso);

    @Modifying
    @Transactional
    @Query("DELETE FROM Gps g WHERE g.id = :id")
    void deleteById(@Param("id") Integer id);

    @Modifying
    @Transactional
    void deleteByIdTheso(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE Gps g SET g.latitude = :lat, g.longitude = :lon WHERE g.idConcept = :idConcept AND g.idTheso = :idTheso")
    int updateCoordinates(@Param("idConcept") String idConcept, @Param("idTheso") String idTheso,
                          @Param("lat") double latitude, @Param("lon") double longitude);

    @Modifying
    @Transactional
    @Query("UPDATE Gps t SET t.idTheso = :newIdThesaurus WHERE t.idTheso = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);
}
