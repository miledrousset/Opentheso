package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ConceptGroupLabel;
import fr.cnrs.opentheso.entites.Thesaurus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ConceptGroupLabelRepository extends JpaRepository<ConceptGroupLabel, Integer> {

    List<ConceptGroupLabel> findAllByThesaurusAndLang(Thesaurus thesaurus, String lang);

}
