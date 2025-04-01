package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.ProjectDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface ProjectDescriptionRepository extends JpaRepository<ProjectDescription, Integer> {

    Optional<ProjectDescription> findByIdGroupAndLang(String idGroup, String lang);

}
