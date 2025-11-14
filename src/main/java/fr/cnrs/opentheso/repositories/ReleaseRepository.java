package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.Release;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface ReleaseRepository extends JpaRepository<Release, Integer> {

    Optional<Release> findByVersion(String version);

}
