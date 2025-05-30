package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Permuted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface PermutedRepository extends JpaRepository<Permuted, Integer> {

    @Modifying
    @Transactional
    void deleteAllByIdThesaurus(String thesaurusId);

    @Modifying
    @Transactional
    @Query("UPDATE Permuted t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);
}
