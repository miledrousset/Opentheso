package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.LanguageIso639;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface LanguageRepository extends JpaRepository<LanguageIso639, Integer> {


    Optional<LanguageIso639> findByIso6391(String iso6391);

}
