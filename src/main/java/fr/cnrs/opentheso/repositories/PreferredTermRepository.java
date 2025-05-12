package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.PreferredTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface PreferredTermRepository extends JpaRepository<PreferredTerm, Integer> {

    @Modifying
    @Transactional
    void deleteByIdThesaurusAndIdTerm(String idThesaurus, String idTerm);

    Optional<PreferredTerm> findByIdThesaurusAndIdConcept(String idThesaurus, String idConcept);

}
