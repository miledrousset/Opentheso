package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.CorpusLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface CorpusLinkRepository extends JpaRepository<CorpusLink, Integer> {

    List<CorpusLink> findAllByIdThesaurusAndActive(String idThesaurus, Boolean active);

    List<CorpusLink> findAllByIdThesaurusOrderBySortAsc(String idThesaurus);

    Optional<CorpusLink> findByIdThesaurusAndCorpusName(String idThesaurus, String corpusName);

    @Modifying
    @Transactional
    void deleteCorpusLinkByIdThesaurusAndCorpusName(String idThesaurus, String corpusName);

    @Modifying
    void deleteAllByIdThesaurus(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE CorpusLink t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE CorpusLink t SET t.corpusName = :corpusName WHERE t.idThesaurus = :idThesaurus AND t.corpusName = :oldCorpusName")
    void updateCorpusName(@Param("corpusName") String corpusName, @Param("oldCorpusName") String oldCorpusName, @Param("idThesaurus") String idThesaurus);

}
