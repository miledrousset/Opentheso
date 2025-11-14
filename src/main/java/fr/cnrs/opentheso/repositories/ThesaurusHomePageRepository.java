package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ThesaurusHomePage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface ThesaurusHomePageRepository extends JpaRepository<ThesaurusHomePage, Integer> {

    Optional<ThesaurusHomePage> findByIdThesoAndLang(String idTheso, String lang);

    @Modifying
    @Transactional
    void deleteAllByIdTheso(String idTheso);

    @Modifying
    @Transactional
    @Query("UPDATE ThesaurusHomePage t SET t.idTheso = :newIdThesaurus WHERE t.idTheso = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

}
