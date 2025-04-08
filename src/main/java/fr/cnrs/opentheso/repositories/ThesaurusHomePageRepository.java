package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ThesaurusHomePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ThesaurusHomePageRepository extends JpaRepository<ThesaurusHomePage, Integer> {

    Optional<ThesaurusHomePage> findByIdThesoAndLang(String idTheso, String lang);

}
