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
             SELECT id_concept2, role, id_ark, id_handle, id_doi
             FROM hierarchical_relationship, concept
             WHERE hierarchical_relationship.id_concept2 = concept.id_concept
             AND hierarchical_relationship.id_thesaurus = concept.id_thesaurus
             AND hierarchical_relationship.id_thesaurus = $1
             AND hierarchical_relationship.id_concept1 = $2
             AND concept.status != ''CA''
             AND role LIKE ''NT%''';
    ELSE
        sql_query := '
            SELECT DISTINCT hr.id_concept2, hr.role, c.id_ark, c.id_handle, c.id_doi
            FROM hierarchical_relationship hr
                JOIN concept c ON hr.id_concept2 = c.id_concept AND hr.id_thesaurus = c.id_thesaurus
                LEFT JOIN concept_group_concept cgc ON c.id_concept = cgc.idconcept AND c.id_thesaurus = cgc.idthesaurus
                LEFT JOIN concept_group cg ON cgc.idgroup = cg.idgroup AND cgc.idthesaurus = cg.idthesaurus
            WHERE hr.id_thesaurus = $1
              AND hr.id_concept1 = $2
              AND c.status != ''CA''
              AND hr.role LIKE ''NT%''
              AND (cg.private = false OR cg.private IS NULL)';

    END IF;

    -- Ajout pagination
    IF step > 0 THEN
        sql_query := sql_query || format(' LIMIT %s OFFSET %s', step, offset_);
    END IF;

    -- Exécution
RETURN QUERY EXECUTE sql_query USING id_theso, id_con;

END;
$BODY$;

ALTER FUNCTION public.opentheso_get_nt(character varying, character varying, integer, integer, boolean)
    OWNER TO opentheso;
