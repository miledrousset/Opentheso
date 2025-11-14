package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.GraphViewExportedConceptBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface GraphViewExportedConceptBranchRepository extends JpaRepository<GraphViewExportedConceptBranch, Integer> {

    @Modifying
    @Transactional
    void deleteAllByGraphViewId(Integer graphViewId);

    @Modifying
    @Transactional
    void deleteAllByTopConceptThesaurusId(String idThesaurus);

    @Modifying
    @Transactional
    void deleteAllByGraphViewIdAndTopConceptThesaurusId(Integer graphView, String topConceptThesaurusId);

    @Modifying
    @Transactional
    void deleteAllByGraphViewIdAndTopConceptIdAndTopConceptThesaurusId(Integer graphView, String topConceptId, String topConceptThesaurusId);

    @Modifying
    @Transactional
    @Query("UPDATE GraphViewExportedConceptBranch t SET t.topConceptThesaurusId = :newIdThesaurus WHERE t.topConceptThesaurusId = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

    List<GraphViewExportedConceptBranch> findAllByGraphViewId(int graphView);
    
    Optional<GraphViewExportedConceptBranch> findByGraphViewIdAndTopConceptIdNullAndTopConceptThesaurusId(Integer graphView, String topConceptId);

    Optional<GraphViewExportedConceptBranch> findByGraphViewIdAndTopConceptIdAndTopConceptThesaurusId(Integer graphViewId, String topConceptId, String topConceptThesaurusId);
}
