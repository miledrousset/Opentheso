package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Preferences;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface PreferencesRepository extends JpaRepository<Preferences, Integer> {

    Optional<Preferences> findByIdThesaurus(String idThesaurus);

}
