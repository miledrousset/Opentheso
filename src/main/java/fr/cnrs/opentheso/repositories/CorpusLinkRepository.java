package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.CorpusLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface CorpusLinkRepository extends JpaRepository<CorpusLink, Integer> {

    List<CorpusLink> findAllByIdThesoAndActive(String idThesaurus, Boolean active);

    List<CorpusLink> findAllByIdThesoOrderBySortAsc(String idThesaurus);

    Optional<CorpusLink> findByIdThesoAndCorpusName(String idThesaurus, String corpusName);

    @Transactional
    void deleteCorpusLinkByIdThesoAndCorpusName(String idThesaurus, String corpusName);

}
