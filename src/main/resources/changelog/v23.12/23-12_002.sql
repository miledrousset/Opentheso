CREATE INDEX IF NOT EXISTS note_id_concept_idx
    ON public.note USING btree
    (id_concept COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (deduplicate_items=True)
    TABLESPACE pg_default;
-- Index: note_id_term_idx

-- DROP INDEX IF EXISTS public.note_id_term_idx;

CREATE INDEX IF NOT EXISTS note_id_term_idx
    ON public.note USING btree
    (id_term COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (deduplicate_items=True)
    TABLESPACE pg_default;
-- Index: note_id_thesaurus_idx

-- DROP INDEX IF EXISTS public.note_id_thesaurus_idx;

CREATE INDEX IF NOT EXISTS note_id_thesaurus_idx
    ON public.note USING btree
    (id_thesaurus COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (deduplicate_items=True)
    TABLESPACE pg_default;
-- Index: note_lang_idx

-- DROP INDEX IF EXISTS public.note_lang_idx;

CREATE INDEX IF NOT EXISTS note_lang_idx
    ON public.note USING btree
    (lang COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (deduplicate_items=True)
    TABLESPACE pg_default;


-- Index: note_notetypecode_idx

-- DROP INDEX IF EXISTS public.note_notetypecode_idx;

CREATE INDEX IF NOT EXISTS note_notetypecode_idx
    ON public.note USING btree
    (notetypecode COLLATE pg_catalog."default" bpchar_pattern_ops ASC NULLS LAST)
    WITH (deduplicate_items=True)
    TABLESPACE pg_default;