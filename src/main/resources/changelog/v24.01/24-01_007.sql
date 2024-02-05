CREATE INDEX IF NOT EXISTS note_identifier_idx
    ON public.note USING btree
    (identifier COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS preferred_term_id_term_idx
    ON public.preferred_term USING btree
    (id_term COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS concept_id_ark_idx
    ON public.concept USING btree
    (id_ark COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS concept_group_id_ark_idx
    ON public.concept_group USING btree
    (id_ark COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS concept_group_label_lexicalvalue_idx
    ON public.concept_group_label USING btree
    (lexicalvalue COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

--

DROP PROCEDURE IF EXISTS public.opentheso_normalize_notes();

CREATE OR REPLACE PROCEDURE public.opentheso_normalize_notes()
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	notes_rec record;
	identifier1 character varying;
BEGIN

	FOR notes_rec IN (SELECT id, id_thesaurus, id_concept, id_term from note)
    LOOP
		IF notes_rec.id_concept is NULL THEN
			identifier1 = opentheso_get_idconcept_from_idterm(notes_rec.id_thesaurus, notes_rec.id_term);
		ELSE
			identifier1 = notes_rec.id_concept;
		END IF;
		update note set identifier = identifier1 where id = notes_rec.id;
	END LOOP;
END;
$BODY$;
call opentheso_normalize_notes();
DROP PROCEDURE IF EXISTS public.opentheso_normalize_notes();