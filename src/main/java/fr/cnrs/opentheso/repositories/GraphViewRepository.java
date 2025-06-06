package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.GraphView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface GraphViewRepository extends JpaRepository<GraphView, Integer> {

    List<GraphView> findAllByIdUser(Integer idUser);

    @Query(value = """
        SELECT gv.id, gv.name, gv.description, gvcb.top_concept_thesaurus_id, gvcb.top_concept_id
        FROM graph_view gv
        LEFT JOIN graph_view_exported_concept_branch gvcb ON gv.id = gvcb.graph_view_id
        WHERE gv.id = :id
    """, nativeQuery = true)
    List<Object[]> findViewWithConceptsById(@Param("id") Integer id);

}
