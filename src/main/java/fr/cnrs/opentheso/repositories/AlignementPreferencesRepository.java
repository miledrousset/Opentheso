package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.AlignementPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface AlignementPreferencesRepository extends JpaRepository<AlignementPreferences, Integer> {

    @Modifying
    @Transactional
    void deleteByIdThesaurus(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE AlignementPreferences t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);
}
