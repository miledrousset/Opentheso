package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.HomePage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


public interface HomePageRepository extends JpaRepository<HomePage, Integer> {

    Optional<HomePage> findByLang(String lang);

    @Modifying
    @Transactional
    @Query("UPDATE HomePage hp set hp.htmlCode = :htmlCode where hp.lang = :idLang")
    int updateHtmlCodeByLang(@Param("htmlCode") String htmlCode, @Param("idLang") String idLang);

}
