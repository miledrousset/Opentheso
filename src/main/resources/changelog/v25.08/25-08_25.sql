DROP FUNCTION IF EXISTS public.opentheso_get_nt(character varying, character varying, integer, integer, boolean);

CREATE OR REPLACE FUNCTION public.opentheso_get_nt(
    id_theso character varying,
    id_con character varying,
    offset_ integer,
    step integer,
    isprivate boolean)
RETURNS TABLE(
    relationship_lexical_value character varying,
    relationship_id_concept character varying,
    relationship_role character varying,
    relationship_id_ark character varying,
    relationship_id_handle character varying,
    relationship_id_doi character varying,
    relationship_sort_key integer
)
LANGUAGE plpgsql
AS $BODY$
DECLARE
sql_query text;
BEGIN

    IF isprivate THEN
        sql_query := '
            SELECT DISTINCT term.lexical_value, hr.id_concept2, hr.role, con.id_ark, con.id_handle, con.id_doi,
                CASE
                    WHEN term.lexical_value ~ ''^[0-9]+$'' THEN term.lexical_value::int
                ELSE NULL
                END AS sort_key
            FROM hierarchical_relationship hr
                JOIN concept con ON con.id_thesaurus = hr.id_thesaurus AND con.id_concept = hr.id_concept2
                JOIN preferred_term pt ON pt.id_concept = con.id_concept
                JOIN term ON pt.id_term = term.id_term AND pt.id_thesaurus = term.id_thesaurus
            WHERE hr.id_thesaurus = $1
                AND hr.id_concept1 = $2
                AND hr.role LIKE ''NT%''
                AND con.status != ''CA''
                AND hr.id_concept2 NOT IN (SELECT cf.id_concept FROM concept_facet cf
                JOIN thesaurus_array ta ON cf.id_facet = ta.id_facet AND cf.id_thesaurus = ta.id_thesaurus WHERE ta.id_concept_parent = $2 AND ta.id_thesaurus = $1)
            ORDER BY sort_key, term.lexical_value ASC';

    ELSE
        sql_query := '
            SELECT DISTINCT term.lexical_value, hr.id_concept2, hr.role, con.id_ark, con.id_handle, con.id_doi,
                CASE WHEN term.lexical_value ~ ''^[0-9]+$'' THEN term.lexical_value::int
                ELSE NULL
                END AS sort_key
            FROM hierarchical_relationship hr
                JOIN concept con ON con.id_thesaurus = hr.id_thesaurus AND con.id_concept = hr.id_concept2
                JOIN preferred_term pt ON pt.id_concept = con.id_concept
                JOIN term ON pt.id_term = term.id_term AND pt.id_thesaurus = term.id_thesaurus
                LEFT JOIN concept_group_concept cgc ON cgc.idconcept = con.id_concept AND cgc.idthesaurus = con.id_thesaurus
                LEFT JOIN concept_group cg ON cg.idgroup = cgc.idgroup
            WHERE hr.id_thesaurus = $1
              AND hr.id_concept1 = $2
              AND hr.role LIKE ''NT%''
              AND con.status != ''CA''
              AND (cg.private IS NULL OR cg.private = false)
              AND hr.id_concept2 NOT IN (
                    SELECT cf.id_concept
                    FROM concept_facet cf
                    JOIN thesaurus_array ta
                      ON cf.id_facet = ta.id_facet
                     AND cf.id_thesaurus = ta.id_thesaurus
                    WHERE ta.id_concept_parent = $2
                      AND ta.id_thesaurus = $1)
            ORDER BY sort_key, term.lexical_value ASC';
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
