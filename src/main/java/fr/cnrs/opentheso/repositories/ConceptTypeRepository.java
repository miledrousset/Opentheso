package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface ConceptTypeRepository extends JpaRepository<ConceptType, Integer> {

    @Modifying
    @Transactional
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    @Transactional
    void deleteByCodeAndIdThesaurus(String code, String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE ConceptType t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

    Optional<ConceptType> findByCodeAndIdThesaurusIn(String code, List<String> idThesaurus);

    Optional<ConceptType> findByCodeAndIdThesaurus(String code, String idThesaurus);

    List<ConceptType> findAllByIdThesaurusIn(List<String> idThesaurus);

}
