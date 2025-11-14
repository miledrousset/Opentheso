DROP FUNCTION IF EXISTS public.opentheso_get_concept_son(character varying, character varying, boolean);

CREATE OR REPLACE FUNCTION public.opentheso_get_concept_son(
	id_theso character varying,
	id_bt character varying,
	private boolean)
    RETURNS TABLE(notation character varying, status character varying, idconcept2 character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
BEGIN
    IF private THEN RETURN QUERY
        SELECT concept.notation, concept.status, hr.id_concept2
        FROM hierarchical_relationship hr JOIN concept ON concept.id_thesaurus = hr.id_thesaurus AND concept.id_concept = hr.id_concept2
                                          LEFT JOIN concept_group_concept cgc ON concept.id_concept = cgc.idconcept
                                          LEFT JOIN concept_group cg ON cgc.idgroup = cg.idgroup
        WHERE hr.id_thesaurus = id_theso
          AND hr.id_concept1 = id_bt
          AND hr.role LIKE 'NT%'
          AND concept.status != 'CA'
          AND id_concept2 NOT IN (SELECT id_concept
                                  FROM concept_facet, thesaurus_array
                                  WHERE concept_facet.id_facet = thesaurus_array.id_facet
                                  AND concept_facet.id_thesaurus = thesaurus_array.id_thesaurus
                                  AND thesaurus_array.id_concept_parent = id_bt
                                  AND thesaurus_array.id_thesaurus = id_theso)
        GROUP BY concept.notation, concept.status, hr.id_concept2, concept.id_ark, concept.id_handle, concept.id_doi
        HAVING BOOL_OR(cg.private IS NULL OR cg.private = false)
        ORDER BY concept.notation ASC;
    ELSE RETURN QUERY
        SELECT concept.notation, concept.status, hierarchical_relationship.id_concept2
        FROM hierarchical_relationship, concept
        WHERE concept.id_thesaurus = hierarchical_relationship.id_thesaurus
          AND concept.id_concept = hierarchical_relationship.id_concept2
          AND hierarchical_relationship.id_thesaurus = id_theso
          AND id_concept1 = id_bt
          AND role LIKE 'NT%'
          AND concept.status != 'CA'
          AND id_concept2 NOT IN (SELECT id_concept
                                  FROM concept_facet, thesaurus_array
                                  WHERE concept_facet.id_facet = thesaurus_array.id_facet
                                  AND concept_facet.id_thesaurus = thesaurus_array.id_thesaurus
                                  AND thesaurus_array.id_concept_parent = id_bt
                                  AND thesaurus_array.id_thesaurus = id_theso)
        ORDER BY concept.notation ASC;
    END IF;
END;
$BODY$;