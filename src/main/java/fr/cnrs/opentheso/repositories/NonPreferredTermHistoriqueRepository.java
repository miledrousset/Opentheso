package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.NonPreferredTermHistorique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface NonPreferredTermHistoriqueRepository extends JpaRepository<NonPreferredTermHistorique, Integer> {

    @Modifying
    void deleteAllByIdThesaurus(String thesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE NonPreferredTermHistorique t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

}
