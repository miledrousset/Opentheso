package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.AlignementSource;
import fr.cnrs.opentheso.models.AlignementSourceProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


public interface AlignementSourceRepository extends JpaRepository<AlignementSource, Integer> {

    @Transactional
    @Modifying
    @Query("DELETE FROM AlignementSource a WHERE a.id = :id")
    int deleteByIdAlignementSource(int id);

    @Query(value = """
        SELECT a.gps, a.source, a.requete, a.type_rqt AS typeRequete,a.alignement_format, a.id, a.description, a.source_filter
        FROM alignement_source a
        JOIN thesaurus_alignement_source tas ON tas.id_alignement_source = a.id
        WHERE tas.id_thesaurus = :idTheso
        ORDER BY a.source ASC
    """, nativeQuery = true)
    List<AlignementSourceProjection> findAllByThesaurus(@Param("idTheso") String idTheso);

}
