DROP PROCEDURE IF EXISTS public.opentheso_add_facet(character varying, integer, character varying, character varying, text, text, text);

CREATE OR REPLACE PROCEDURE public.opentheso_add_facet(
	IN id_facet character varying,
	IN id_user integer,
	IN id_thesaurus character varying,
	IN id_conceotparent character varying,
	IN labels text,
	IN membres text,
	IN notes text)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
seperateur constant varchar := '##';
    sous_seperateur constant varchar := '@@';

    label_rec record;
    membres_rec record;
    array_string   text[];
    isFirst boolean;
BEGIN
    isFirst = false;
FOR label_rec IN SELECT unnest(string_to_array(labels, seperateur)) AS label_value
                     LOOP
SELECT string_to_array(label_rec.label_value, sous_seperateur) INTO array_string;

if (isFirst = false) then
                isFirst = true;
INSERT INTO node_label(id_facet, id_thesaurus, lexical_value, created, modified, lang)
VALUES (id_facet, id_thesaurus, array_string[1], CURRENT_DATE, CURRENT_DATE, array_string[2]);

INSERT INTO thesaurus_array(id_thesaurus, id_concept_parent, id_facet) VALUES (id_thesaurus, id_conceotParent, id_facet) ON CONFLICT DO NOTHING;
ELSE
                Insert into node_label (id_facet, id_thesaurus, lexical_value, lang) values (id_facet, id_thesaurus, array_string[1], array_string[2]);
END IF;
END LOOP;

FOR membres_rec IN SELECT unnest(string_to_array(membres, seperateur)) AS membre_value
                       LOOP
                       INSERT INTO concept_facet(id_facet, id_thesaurus, id_concept) VALUES (id_facet, id_thesaurus, membres_rec.membre_value) ON CONFLICT DO NOTHING;
END LOOP;
    IF (notes IS NOT NULL AND notes != 'null') THEN
        -- 'value@typeCode@lang@id_term'
        CALL opentheso_add_notes(id_facet, id_thesaurus, id_user, notes);
END IF;
END;
$BODY$;