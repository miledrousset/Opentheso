package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.HierarchicalRelationshipHistorique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface HierarchicalRelationshipHistoriqueRepository extends JpaRepository<HierarchicalRelationshipHistorique, Integer> {

    @Modifying
    @Transactional
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE HierarchicalRelationshipHistorique t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

    @Modifying
    @Query(value = "UPDATE hierarchical_relationship_historique SET id_thesaurus = :target WHERE id_concept1 = :concept AND id_thesaurus = :from", nativeQuery = true)
    void updateThesaurusByConcept1(@Param("concept") String concept, @Param("from") String from, @Param("target") String target);

    @Modifying
    @Query(value = "UPDATE hierarchical_relationship_historique SET id_thesaurus = :target WHERE id_concept2 = :concept AND id_thesaurus = :from", nativeQuery = true)
    void updateThesaurusByConcept2(@Param("concept") String concept, @Param("from") String from, @Param("target") String target);
}
