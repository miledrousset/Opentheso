DELETE FROM concept_group_concept
WHERE ctid IN (
    SELECT ctid
    FROM (
             SELECT ctid,
                    ROW_NUMBER() OVER(PARTITION BY LOWER(idgroup), idthesaurus, idconcept ORDER BY idgroup) AS row_num
             FROM concept_group_concept
         ) AS duplicates
    WHERE duplicates.row_num > 1
);