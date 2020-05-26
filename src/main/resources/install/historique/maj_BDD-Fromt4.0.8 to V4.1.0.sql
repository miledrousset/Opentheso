
--  !!!!!!! Attention !!!!!!!!!
-- par Miled Rousset 
-- 
-- La base de données change pour : 
-- modification de la gestion des Groupes et sous groupes / Collections et sous collections.
--
--  !!!!!!! Attention !!!!!!!!! 


-- Table: public.concept_group_concept

DROP TABLE public.concept_group_concept;

CREATE TABLE public.concept_group_concept
(
  idgroup text NOT NULL,
  idthesaurus text NOT NULL,
  idconcept text NOT NULL,
  CONSTRAINT concept_group_concept_pkey PRIMARY KEY (idgroup, idthesaurus, idconcept)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.concept_group_concept
  OWNER TO opentheso;


-- Suppression de la colonne idconcept de la table concept_group
alter table public.concept_group drop column idconcept;

alter table public.concept_group drop column idparentgroup;

-- Ajout du contributeur et du créateur à la table Term

ALTER TABLE term ALTER COLUMN creator TYPE integer USING (creator::integer);
ALTER TABLE term ALTER COLUMN contributor TYPE integer USING (creator::integer);


/*

MAJ utilise par Zorro


--#ALTER TABLE public.concept_group_concept
OWNER to opentheso;



UPDATE term SET contributor = 1 WHERE contributor ilike 'null';
UPDATE term SET contributor = 1 WHERE contributor ilike '';
UPDATE term SET creator = 1 WHERE creator ilike 'null';
UPDATE term SET creator = 1 WHERE creator ilike '';

ALTER TABLE term ALTER COLUMN creator TYPE integer USING (creator::integer);
ALTER TABLE term ALTER COLUMN contributor TYPE integer USING (creator::integer);
ALTER TABLE users ADD motpasstemp character varying(100);
*/


-- ajouté par Miled

/* modification de la clé primaire de la table candidat */
ALTER TABLE ONLY concept_candidat
    drop CONSTRAINT concept_candidat_pkey;
ALTER TABLE ONLY concept_candidat
    ADD CONSTRAINT concept_candidat_pkey PRIMARY KEY (id_concept, id_thesaurus);


DROP SEQUENCE definition_note__id_seq;
DROP SEQUENCE editorial_note__id_seq;
DROP SEQUENCE history_note__id_seq;


/* normalisation des identifiants à séquences pour préparation aux mises à jour automatiques
*/
ALTER TABLE alignement RENAME COLUMN id_alignement TO id;
ALTER TABLE concept_group_label RENAME COLUMN idgrouplabel TO id;
ALTER TABLE concept_group_label_historique RENAME COLUMN idgrouplabel TO id;
ALTER TABLE note RENAME COLUMN id_note TO id;
ALTER TABLE note_historique RENAME COLUMN id_note TO id;
ALTER TABLE preferences RENAME COLUMN id_pref TO id;
ALTER TABLE thesaurus_array RENAME COLUMN identifier TO facet_id;
