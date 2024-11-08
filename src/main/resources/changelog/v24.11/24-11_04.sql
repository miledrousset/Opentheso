DROP FUNCTION IF EXISTS public.opentheso_get_groups(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_groups(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(group_id text, group_id_ark text, group_id_handle character varying, group_id_doi character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
return query
SELECT concept_group.idgroup, concept_group.id_ark, concept_group.id_handle, concept_group.id_doi
FROM concept_group_concept, concept_group
WHERE
  LOWER(concept_group.idgroup) = LOWER(concept_group_concept.idgroup)
  AND concept_group.idthesaurus = concept_group_concept.idthesaurus
  AND concept_group_concept.idthesaurus = id_theso
  AND concept_group_concept.idconcept = id_con;

end;
$BODY$;