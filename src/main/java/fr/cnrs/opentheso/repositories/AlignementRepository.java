package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Alignement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AlignementRepository extends JpaRepository<Alignement, Integer> {

    @Query(value = """
        SELECT * 
        FROM alignement 
        WHERE internal_id_thesaurus = :idThesaurus 
        AND internal_id_concept NOT IN (
            SELECT idconcept 
            FROM concept_group_concept 
            WHERE idthesaurus = :idThesaurus
        )
        """, nativeQuery = true)
    List<Alignement> findAlignementsNotInConceptGroup(@Param("idThesaurus") String idThesaurus);


    @Query(value = """
        SELECT a.*
        FROM concept_group_concept cgc, alignement a
        WHERE cgc.idconcept = a.internal_id_concept
        AND cgc.idthesaurus = a.internal_id_thesaurus
        AND LOWER(cgc.idgroup) = LOWER(:idGroup)
        AND cgc.idthesaurus = :idThesaurus
    """, nativeQuery = true)
    List<Alignement> findAlignementsByGroupAndThesaurus(@Param("idGroup") String idGroup, @Param("idThesaurus") String idThesaurus);

}
