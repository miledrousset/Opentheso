package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Thesaurus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ThesaurusRepository extends JpaRepository<Thesaurus, String> {

    Optional<Thesaurus> getThesaurusByIdArk(String idArk);

}
