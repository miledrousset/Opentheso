package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.GraphViewExportedConceptBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface GraphViewExportedConceptBranchRepository extends JpaRepository<GraphViewExportedConceptBranch, Integer> {

    void deleteAllByTopConceptThesaurusId(String idThesaurus);

    @Modifying
    @Transactional
    @Query("UPDATE GraphViewExportedConceptBranch t SET t.topConceptThesaurusId = :newIdThesaurus WHERE t.topConceptThesaurusId = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);

}
