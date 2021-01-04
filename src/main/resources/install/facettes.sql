
CREATE TABLE public.concept_facette
(
    id_facette integer NOT NULL,
    id_thesaurus text COLLATE pg_catalog."default" NOT NULL,
    id_concept text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT concept_facettes_pkey PRIMARY KEY (id_facette, id_thesaurus, id_concept)
);

DROP TABLE IF EXISTS thesaurus_array;

CREATE TABLE thesaurus_array
(
    id_thesaurus character varying COLLATE pg_catalog."default" NOT NULL,
    id_concept_parent character varying COLLATE pg_catalog."default" NOT NULL,
    ordered boolean NOT NULL DEFAULT false,
    notation character varying COLLATE pg_catalog."default",
    id_facet integer NOT NULL DEFAULT nextval('thesaurus_array_facet_id_seq'::regclass)
)