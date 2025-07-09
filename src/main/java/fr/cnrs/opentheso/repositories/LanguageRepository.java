package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.LanguageIso639;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface LanguageRepository extends JpaRepository<LanguageIso639, Integer> {

    @Query(value = """
        SELECT lang.* FROM languages_iso639 lang ORDER BY lang.iso639_1
    """, nativeQuery = true)
    List<LanguageIso639> findAllOrderByCodePays();

    Optional<LanguageIso639> findByIso6391(String iso6391);

    @Query(value = """
        SELECT lang.* 
        FROM languages_iso639 lang
        JOIN project_description pro ON lang.iso639_1 = pro.lang
        WHERE pro.id_group = :idProject
        """, nativeQuery = true)
    List<LanguageIso639> findLanguagesByProject(@Param("idProject") String idProject);

}
