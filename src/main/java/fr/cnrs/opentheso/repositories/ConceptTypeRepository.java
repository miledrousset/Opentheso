package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface ConceptTypeRepository extends JpaRepository<ConceptType, Integer> {

    void deleteAllByIdTheso(String idTheso);

    @Modifying
    @Transactional
    @Query("UPDATE ConceptType t SET t.idTheso = :newIdThesaurus WHERE t.idTheso = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

}
