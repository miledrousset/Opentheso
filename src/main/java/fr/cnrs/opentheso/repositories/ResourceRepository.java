package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.NarrowerResultProjection;
import fr.cnrs.opentheso.entites.Concept;
import fr.cnrs.opentheso.models.FacetProjection;
import fr.cnrs.opentheso.models.FullConceptProjection;
import fr.cnrs.opentheso.models.NarrowerTreeProjection;
import fr.cnrs.opentheso.models.NodeConceptGraphProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ResourceRepository extends JpaRepository<Concept, Integer> {

    @Query(value = """
        SELECT idconcept2 FROM opentheso_get_narrowers_ignorefacet(:idThesaurus, :idBT)
    """, nativeQuery = true)
    List<String> findNarrowersIgnoreFacet(@Param("idThesaurus") String idThesaurus, @Param("idBT") String idBT);

    @Query(value = """
        SELECT * FROM opentheso_get_list_topterm_forgraph(:idThesaurus, :idLang)
        AS x(idconcept character varying, local_uri text, status character varying, label varchar, altlabel varchar, definition text, havechildren boolean, image text)
    """, nativeQuery = true)
    List<NodeConceptGraphProjection> getTopConceptsForGraph(@Param("idThesaurus") String idThesaurus, @Param("idLang") String idLang);

    @Query(value = """
        SELECT * FROM opentheso_get_list_narrower_forgraph(:idTheso, :idConceptBT, :idLang)
        AS x(idconcept2 character varying, local_uri text, status character varying,
             label varchar, altlabel varchar, definition text, havechildren boolean, image text)
    """, nativeQuery = true)
    List<NodeConceptGraphProjection> getNarrowersForGraph(@Param("idTheso") String idTheso, @Param("idConceptBT") String idConceptBT,
                                                          @Param("idLang") String idLang);

    @Query(value = """
        SELECT * FROM opentheso_get_list_narrower_fortree(:idTheso, :idBT, :idLang, :isPrivate)
        AS x(idconcept2 character varying, notation character varying, status character varying, label varchar, havechildren boolean)
    """, nativeQuery = true)
    List<NarrowerTreeProjection> getNarrowersForTree(@Param("idTheso") String idTheso, @Param("idBT") String idBT,
                                                     @Param("idLang") String idLang, @Param("isPrivate") boolean isPrivate);

    @Query(value = """
        SELECT * FROM opentheso_get_facets_of_concept(:idTheso, :idBT, :idLang)
        AS x(id_facet character varying, libelle character varying, have_members boolean)
    """, nativeQuery = true)
    List<FacetProjection> getFacetsOfConcept(@Param("idTheso") String idTheso, @Param("idBT") String idBT, @Param("idLang") String idLang);

    @Query(value = """
        SELECT * FROM opentheso_get_next_nt(:idTheso, :idConcept, :idLang, :offset, :step)
    """, nativeQuery = true)
    List<NarrowerResultProjection> getNextNT(@Param("idTheso") String idTheso, @Param("idConcept") String idConcept,
                                             @Param("idLang") String idLang, @Param("offset") int offset, @Param("step") int step);

    @Query(value = """
        SELECT * FROM opentheso_get_concept(:idTheso, :idConcept, :idLang, :offset, :step)
        AS x(uri text, resourcetype varchar, localuri text, identifier varchar, permalinkid varchar,
             preflabel varchar, altlabel varchar, hidenlabel varchar, preflabel_trad varchar,
             altlabel_trad varchar, hiddenlabel_trad varchar, definition text, example text,
             editorialnote text, changenote text, scopenote text, note text, historynote text,
             notation varchar, narrower text, broader text, related text, exactmatch text,
             closematch text, broadmatch text, relatedmatch text, narrowmatch text,
             gpsdata text, membre text, created date, modified date, images text,
             creator text, contributor text, replaces text, replaced_by text,
             facets text, externalresources text, concepttype text)
    """, nativeQuery = true)
    Optional<FullConceptProjection> getFullConcept(@Param("idTheso") String idTheso, @Param("idConcept") String idConcept,
                                                   @Param("idLang") String idLang, @Param("offset") int offset, @Param("step") int step);
}
