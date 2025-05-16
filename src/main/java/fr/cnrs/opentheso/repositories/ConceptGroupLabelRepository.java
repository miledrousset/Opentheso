package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptGroupLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ConceptGroupLabelRepository extends JpaRepository<ConceptGroupLabel, Integer> {

    List<ConceptGroupLabel> findAllByIdThesaurusAndLang(String idThesaurus, String lang);

    List<ConceptGroupLabel> findAllByIdThesaurusAndIdGroup(String idThesaurus, String idGroup);

    List<ConceptGroupLabel> findAllByIdThesaurusAndIdGroupAndLangNot(String idThesaurus, String idGroup, String idLang);

    Optional<ConceptGroupLabel> findAllByIdThesaurusAndIdGroupAndLang(String idThesaurus, String idGroup, String idLang);

    Optional<ConceptGroupLabel> findByLexicalValueLikeAndLangAndIdThesaurus(String lexicalValue, String lang, String idThesaurus);

    @Modifying
    void deleteAllByIdGroupAndIdThesaurusAndLang(String idGroup, String idThesaurus, String idLang);

    @Modifying
    void deleteByIdThesaurusAndIdGroup(String idThesaurus, String idGroup);

    @Query(value = """
        SELECT idgroup, lexicalvalue 
        FROM concept_group_label
        WHERE idthesaurus = :idThesaurus
        AND lang = :idLang
        AND unaccent_string(lexicalvalue) ILIKE unaccent_string(CONCAT('%', :searchText, '%'))
        ORDER BY lexicalvalue ASC
        LIMIT 40
    """, nativeQuery = true)
    List<Object[]> searchGroups(@Param("idThesaurus") String idThesaurus, @Param("idLang") String idLang, @Param("searchText") String searchText);

    @Query(value = """
        SELECT idgroup, lexicalvalue 
        FROM concept_group_label
        WHERE idthesaurus = :idThesaurus
        AND lang = :idLang
        AND unaccent_string(lexicalvalue) ILIKE unaccent_string(CONCAT('%', :searchText, '%'))
        ORDER BY lexicalvalue ASC
        LIMIT 40
    """, nativeQuery = true)
    List<Object[]> getGroupAutoCompletions(@Param("idThesaurus") String idThesaurus, @Param("idLang") String idLang, @Param("searchText") String searchText);
}
