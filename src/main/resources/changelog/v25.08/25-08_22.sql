DROP FUNCTION IF EXISTS public.opentheso_get_nt(character varying, character varying, integer, integer, boolean);

CREATE OR REPLACE FUNCTION public.opentheso_get_nt(
    id_theso character varying,
    id_con character varying,
    offset_ integer,
    step integer,
    isprivate boolean)
RETURNS TABLE(
    relationship_id_concept character varying,
    relationship_role character varying,
    relationship_id_ark character varying,
    relationship_id_handle character varying,
    relationship_id_doi character varying
)
LANGUAGE plpgsql
AS $BODY$
DECLARE
sql_query text;
BEGIN

    IF isprivate THEN
        sql_query := '
            SELECT hr.id_concept2, hr.role, con.id_ark, con.id_handle, con.id_doi
            FROM hierarchical_relationship hr
            JOIN concept con ON con.id_thesaurus = hr.id_thesaurus
                             AND con.id_concept = hr.id_concept2
            WHERE hr.id_thesaurus = $1
              AND hr.id_concept1 = $2
              AND hr.role LIKE ''NT%''
              AND con.status != ''CA''
              AND hr.id_concept2 NOT IN (
                    SELECT cf.id_concept
                    FROM concept_facet cf
                    JOIN thesaurus_array ta ON cf.id_facet = ta.id_facet
                                            AND cf.id_thesaurus = ta.id_thesaurus
                    WHERE ta.id_concept_parent = $2
                      AND ta.id_thesaurus = $1
              )
            ORDER BY con.notation ASC';
ELSE
        sql_query := '
            SELECT hr.id_concept2, hr.role, con.id_ark, con.id_handle, con.id_doi
            FROM hierarchical_relationship hr
            JOIN concept con ON con.id_thesaurus = hr.id_thesaurus
                             AND con.id_concept = hr.id_concept2
            LEFT JOIN concept_group_concept cgc ON con.id_concept = cgc.idconcept
            LEFT JOIN concept_group cg ON cgc.idgroup = cg.idgroup
            WHERE hr.id_thesaurus = $1
              AND hr.id_concept1 = $2
              AND hr.role LIKE ''NT%''
              AND con.status != ''CA''
            GROUP BY con.notation, con.status, hr.id_concept2, con.id_ark, con.id_handle, con.id_doi, hr.role
            HAVING BOOL_OR(cg.private IS NULL OR cg.private = false)
            ORDER BY con.notation ASC';
END IF;

    -- Ajout pagination
    IF step > 0 THEN
        sql_query := sql_query || format(' OFFSET %s FETCH NEXT %s ROWS ONLY', offset_, step);
END IF;

    -- Exécution
RETURN QUERY EXECUTE sql_query USING id_theso, id_con;

END;
$BODY$;

ALTER FUNCTION public.opentheso_get_nt(character varying, character varying, integer, integer, boolean)
    OWNER TO opentheso;
