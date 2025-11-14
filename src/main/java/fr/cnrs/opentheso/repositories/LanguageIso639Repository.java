package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.LanguageIso639;
import fr.cnrs.opentheso.models.LanguageOfThesaurusProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface LanguageIso639Repository extends JpaRepository<LanguageIso639, Integer> {

    @Query("""
        SELECT l.iso6391 as iso6391, l.iso6392 as iso6392, l.englishName as englishName, l.frenchName as frenchName, l.codePays as codePays
        FROM LanguageIso639 l JOIN ThesaurusLabel tl ON l.iso6391 = tl.lang
        WHERE tl.idThesaurus = :idThesaurus
        GROUP BY l.iso6391, l.iso6392, l.englishName, l.frenchName, l.codePays
    """)
    List<LanguageOfThesaurusProjection> findLanguagesByThesaurusId(@Param("idThesaurus") String idThesaurus);

}
