package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface ConceptGroupRepository extends JpaRepository<ConceptGroup, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE ConceptGroup cg SET cg.isPrivate = :isPrivate WHERE cg.idGroup = :idGroup AND cg.thesaurus.idThesaurus = :idThesaurus")
    void updateVisibility(@Param("idGroup") String idGroup, @Param("idThesaurus") String idThesaurus, @Param("isPrivate") boolean isPrivate);

}
