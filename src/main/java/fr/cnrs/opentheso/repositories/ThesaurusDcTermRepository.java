package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ThesaurusDcTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface ThesaurusDcTermRepository extends JpaRepository<ThesaurusDcTerm, Integer> {

    List<ThesaurusDcTerm> findAllByIdThesaurus(String idThesaurus);


    @Modifying
    @Transactional
    @Query("DELETE FROM ThesaurusDcTerm tdt WHERE tdt.idThesaurus = :idThesaurus AND tdt.id = :id")
    void deleteDcElementThesaurus(@Param("id") Integer id, @Param("idThesaurus") String idThesaurus);

}
