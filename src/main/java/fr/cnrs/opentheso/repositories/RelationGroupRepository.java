package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.entites.RelationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface RelationGroupRepository extends JpaRepository<RelationGroup, Integer> {

    List<RelationGroup> findByIdThesaurusAndIdGroup1AndRelation(String idThesaurus, String idGroup1, String idRelation);

    Optional<RelationGroup> findByIdThesaurusAndIdGroup2AndRelation(String idThesaurus, String idGroup2, String idRelation);

    @Modifying
    @Transactional
    void deleteByIdGroup1AndIdGroup2AndIdThesaurus(String idGroup1, String idGroup2, String idThesaurus);

    @Modifying
    @Transactional
    void deleteByIdThesaurusAndIdGroup2(String idThesaurus, String idGroup2);

    @Modifying
    @Transactional
    void deleteByIdThesaurus(String idThesaurus);

    @Query(value = """
        SELECT cg.idgroup 
        FROM relation_group rg
            JOIN concept_group cg ON LOWER(cg.idgroup) = LOWER(rg.id_group2) AND cg.idthesaurus = rg.id_thesaurus
        WHERE rg.id_thesaurus = :idThesaurus
        AND LOWER(rg.id_group1) = LOWER(:idGroupParent)
        AND rg.relation = 'sub'
        ORDER BY cg.notation ASC
    """, nativeQuery = true)
    List<String> findChildGroupIds(@Param("idThesaurus") String idThesaurus, @Param("idGroupParent") String idGroupParent);

    @Query(value = """
        SELECT cg.idgroup, cg.id_ark, cg.id_handle, cg.id_doi
        FROM relation_group rg
            JOIN concept_group cg ON LOWER(cg.idgroup) = LOWER(rg.id_group2) AND cg.idthesaurus = rg.id_thesaurus
        WHERE rg.id_thesaurus = :idThesaurus
        AND LOWER(rg.id_group1) = LOWER(:idGroupParent)
        AND rg.relation = 'sub'
        ORDER BY rg.id_group2 ASC
    """, nativeQuery = true)
    List<Object[]> findChildGroupDetails(@Param("idThesaurus") String idThesaurus, @Param("idGroupParent") String idGroupParent);

    @Modifying
    @Transactional
    @Query("UPDATE RelationGroup t SET t.idThesaurus = :newIdThesaurus WHERE t.idThesaurus = :oldIdThesaurus")
    void updateThesaurusId(@Param("newIdThesaurus") String newIdThesaurus, @Param("oldIdThesaurus") String oldIdThesaurus);


}
