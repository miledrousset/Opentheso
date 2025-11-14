DROP FUNCTION IF EXISTS opentheso_get_nt(id_theso character varying, id_con character varying, offset_ integer, step integer, isprivate boolean, idlang character varying);

DROP FUNCTION IF EXISTS opentheso_get_nt(id_theso character varying, id_con character varying, offset_ integer, step integer, isprivate boolean);

CREATE OR REPLACE FUNCTION opentheso_get_nt(
    id_theso character varying,
    id_con character varying,
    offset_ integer,
    step integer,
    isprivate boolean,
    idlang character varying)
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
            SELECT DISTINCT term.lexical_value, id_concept2, role, id_ark, id_handle, id_doi,
                CASE
                    WHEN term.lexical_value ~ ''^[0-9]+$'' THEN term.lexical_value::int
                    ELSE NULL
                END AS sort_key
            FROM hierarchical_relationship, concept con, preferred_term pt, term
            WHERE hierarchical_relationship.id_concept2 = con.id_concept
            AND hierarchical_relationship.id_thesaurus = con.id_thesaurus
            AND pt.id_concept = con.id_concept
            AND pt.id_thesaurus = con.id_thesaurus
            AND term.lang = $3
            AND pt.id_term = term.id_term
            AND hierarchical_relationship.id_thesaurus = $1
            AND hierarchical_relationship.id_concept1 = $2
            AND con.status != ''CA''
            AND role LIKE ''NT%''
            ORDER BY sort_key, term.lexical_value ASC';
ELSE
        sql_query := '
            SELECT DISTINCT t.lexical_value, hr.id_concept2, hr.role, c.id_ark, c.id_handle, c.id_doi,
                CASE WHEN t.lexical_value ~ ''^[0-9]+$'' THEN t.lexical_value::int ELSE NULL END AS sort_key
            FROM hierarchical_relationship hr
            JOIN concept c ON hr.id_concept2 = c.id_concept AND hr.id_thesaurus = c.id_thesaurus
            JOIN preferred_term pt ON pt.id_concept = c.id_concept AND pt.id_thesaurus = c.id_thesaurus
            JOIN term t ON t.id_term = pt.id_term AND t.lang = $3
            LEFT JOIN concept_group_concept cgc ON cgc.idconcept = c.id_concept AND cgc.idthesaurus = c.id_thesaurus
            LEFT JOIN concept_group cg ON cg.idgroup = cgc.idgroup
            WHERE hr.id_thesaurus = $1
              AND hr.id_concept1 = $2
              AND c.status != ''CA''
              AND hr.role LIKE ''NT%''
              AND (cg.private IS NULL OR cg.private = false)
            ORDER BY sort_key, t.lexical_value ASC';
END IF;

    -- Ajout pagination
    IF step > 0 THEN
        sql_query := sql_query || format(' OFFSET %s FETCH NEXT %s ROWS ONLY', offset_, step);
END IF;

    -- Exécution
RETURN QUERY EXECUTE sql_query USING id_theso, id_con, idlang;

END;
$BODY$;