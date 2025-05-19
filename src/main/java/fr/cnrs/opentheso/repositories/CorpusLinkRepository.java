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

    List<CorpusLink> findAllByIdThesoAndActive(String idThesaurus, Boolean active);

    List<CorpusLink> findAllByIdThesoOrderBySortAsc(String idThesaurus);

    Optional<CorpusLink> findByIdThesoAndCorpusName(String idThesaurus, String corpusName);

    @Modifying
    @Transactional
    void deleteCorpusLinkByIdThesoAndCorpusName(String idThesaurus, String corpusName);

    @Modifying
    void deleteAllByIdTheso(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE CorpusLink t SET t.idTheso = :newIdThesaurus WHERE t.idTheso = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

}
