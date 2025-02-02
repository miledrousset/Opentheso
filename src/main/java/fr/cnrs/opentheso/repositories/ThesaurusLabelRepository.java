package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ThesaurusLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ThesaurusLabelRepository extends JpaRepository<ThesaurusLabel, String> {

    Optional<ThesaurusLabel> findByIdThesaurusAndLang(String idThesaurus, String lang);

}
