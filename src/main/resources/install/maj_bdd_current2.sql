

--  !!!!!!! Attention !!!!!!!!!
--
-- à appliquer ce script uniquement sur la nouvelle version à partir de 20.07
-- il faut appliquer ce script à votre BDD actuelle,
-- il faut faire une sauvegarde avant toute opération
-- pour une nouvelle installation, il faut utiliser le script (opentheso_current.sql) en créant votre BDD avant 

--  !!!!!!! Attention !!!!!!!!! 

-- version=20.07
-- date : 29/07/2020
--
-- n'oubliez pas de définir le role suivant votre installation 
--


--- fonctions à appliquer en premier et à part depuis la version 4.4.1
SET ROLE = opentheso;
SET schema 'public';
----------------------------------------------------------------------------
-- ne pas modifier, ces sont les fonctions de base
----------------------------------------------------------------------------
-- permet d'effacer une fonction
CREATE OR REPLACE FUNCTION delete_fonction(TEXT, TEXT) RETURNS VOID AS $$
DECLARE
 nom_fonction ALIAS FOR $1;
 type_function ALIAS for $2;

BEGIN
    IF EXISTS (SELECT proargtypes FROM pg_proc  WHERE proname = nom_fonction) THEN
        execute 'Drop function ' || nom_fonction||'('||type_function||')';
    END IF;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION delete_fonction(TEXT, TEXT, TEXT) RETURNS VOID AS $$
DECLARE
 nom_fonction ALIAS FOR $1;
 type_function ALIAS for $2;
 type_function2 ALIAS for $3;
BEGIN
    IF EXISTS (SELECT proargtypes FROM pg_proc  WHERE proname = nom_fonction) THEN
        execute 'Drop function ' || nom_fonction||'('||type_function||','||type_function2||')';
    END IF;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION delete_fonction1(TEXT, TEXT, TEXT) RETURNS VOID AS $$
DECLARE
 nom_fonction ALIAS FOR $1;
 type_function ALIAS for $2;
 type_function2 ALIAS for $3;
BEGIN
    IF EXISTS (SELECT proargtypes FROM pg_proc  WHERE proname = nom_fonction) THEN
        execute 'Drop function ' || nom_fonction||'('||type_function||','||type_function2||','||type_function2||')';
    END IF;
END;
$$ LANGUAGE plpgsql;

----------------------------------------------------------------------------
-- fin des fonctions de base
----------------------------------------------------------------------------



--
-- Tables pour la gestion des facettes
--

CREATE TABLE IF NOT EXISTS concept_facet
(
    id_facet character varying NOT NULL,
    id_thesaurus text COLLATE pg_catalog."default" NOT NULL,
    id_concept text COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT concept_facettes_pkey PRIMARY KEY (id_facet, id_thesaurus, id_concept)
);


create or replace function delete_table_thesaurus_array() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='thesaurus_array' AND column_name='id_facet') THEN
        execute 'DROP TABLE thesaurus_array;
                CREATE sequence IF NOT EXISTS thesaurus_array_facet_id_seq
                INCREMENT 1
                    MINVALUE 1
                    MAXVALUE 9223372036854775807
                    START 1
                    CACHE 1;
                CREATE TABLE thesaurus_array
                (
                    id_thesaurus character varying COLLATE pg_catalog."default" NOT NULL,
                    id_concept_parent character varying COLLATE pg_catalog."default" NOT NULL,
                    ordered boolean NOT NULL DEFAULT false,
                    notation character varying COLLATE pg_catalog."default",
                    id_facet character varying NOT NULL,
                    CONSTRAINT thesaurus_array_pkey PRIMARY KEY (id_facet, id_thesaurus, id_concept_parent)
                );
                ALTER TABLE node_label drop COLUMN facet_id;
                ALTER TABLE node_label ADD COLUMN id integer NOT NULL DEFAULT nextval(''thesaurus_array_facet_id_seq''::regclass);
                ALTER TABLE node_label ADD COLUMN id_facet character varying DEFAULT ''''::character varying';
    END IF;
end
$$language plpgsql;




--
-- mise a jour de la table candidat_vote (ajout de la colonne type_vote)
--
create or replace function update_table_candidat_vote() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='candidat_vote' AND column_name='type_vote') THEN
        execute 'ALTER TABLE candidat_vote ADD COLUMN  type_vote varchar(30);
                ALTER TABLE candidat_vote ADD COLUMN id_note varchar(30);';
    END IF;
end
$$language plpgsql;

--
-- mise a jour de la table candidat_status(ajout de la colonne id_user_admin)
--
create or replace function update_table_candidat_status() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='candidat_status' AND column_name='id_user_admin') THEN
        execute 'ALTER TABLE candidat_status ADD COLUMN  id_user_admin integer;';
    END IF;
end
$$language plpgsql;


--
-- mise a jour de la table info (suppression des contraintes)
--
create or replace function update_table_info_constraint() returns void as $$
begin
    if exists (SELECT * from information_schema.table_constraints where table_name = 'info' 
	and constraint_name ='info_pkey') then 
	execute
	'ALTER TABLE ONLY info
	  drop CONSTRAINT info_pkey';
    END IF;
end
$$language plpgsql;

--
-- mise a jour de la table info (ajout de la colonne googleanalytics)
--
create or replace function update_table_info() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='info' AND column_name='googleanalytics') THEN
        execute 'ALTER TABLE info ADD COLUMN  googleanalytics character varying;
                 delete from info;
                 INSERT INTO public.info (version_opentheso, version_bdd, googleanalytics) VALUES ('''', '''', '''');';
    END IF;
end
$$language plpgsql;




--
-- mise a jour de la table corpus_link (ajout de la colonne active)
--
create or replace function update_table_corpus_link() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='corpus_link' AND column_name='active') THEN
        execute 'ALTER TABLE corpus_link ADD COLUMN  active boolean DEFAULT false;';
    END IF;
end
$$language plpgsql;

-- Table: concept_orphan à supprimer, elle n'est plus utile
DROP TABLE if exists public.concept_orphan;

-- 
DROP TABLE if exists public.thesaurus_array_concept;


-- Renommer la table concept_fusion pour gérer les concepts dépréciés 
ALTER TABLE if exists concept_fusion RENAME TO concept_replacedby;


-- Préférences : ajout de l'option mise en cache de l'arbre et sort_by_notation
--
create or replace function update_table_preferences_original_uri_doi() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='original_uri_is_doi') THEN
        execute 'ALTER TABLE preferences ADD COLUMN original_uri_is_doi boolean DEFAULT false;';
    END IF;
end
$$language plpgsql;

create or replace function update_table_preferences_tree_cache() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='tree_cache') THEN
        execute 'ALTER TABLE preferences ADD COLUMN tree_cache boolean DEFAULT false;';
    END IF;
end
$$language plpgsql;

create or replace function update_table_preferences_sortbynotation() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='sort_by_notation') THEN
        execute 'ALTER TABLE preferences ADD COLUMN sort_by_notation boolean DEFAULT false';
    END IF;
end
$$language plpgsql;


-- Ajout du champ DOI à la table Concept
--
create or replace function update_table_concept_doi() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='concept' AND column_name='id_doi') THEN
        execute 'ALTER TABLE concept ADD COLUMN id_doi character varying DEFAULT ''''::character varying;';
    END IF;
end
$$language plpgsql;

-- Ajout du champ DOI à la table concept_group
--
create or replace function update_table_concept_group_doi() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='concept_group' AND column_name='id_doi') THEN
        execute 'ALTER TABLE concept_group ADD COLUMN id_doi character varying DEFAULT ''''::character varying;';
    END IF;
end
$$language plpgsql;


-- Ajout d'une nouvelle langue à la liste ISO 
--
create or replace function update_table_languages() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM languages_iso639 where iso639_1='fro') THEN
        execute 'INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (''fro'', ''fro'', ''Old French (842—ca. 1400)'', ''ancien français (842-environ 1400)'', 190);';
    END IF;
end
$$language plpgsql;


-- Ajout d'une nouvelle source à la table sources d'alignements 
--
create or replace function update_table_alignement_source() returns void as $$
begin
    IF EXISTS(SELECT *  FROM alignement_source where source='Wikidata') THEN
        execute 'DELETE FROM public.alignement_source where source = ''Wikidata'';';
    END IF;
    IF NOT EXISTS(SELECT *  FROM alignement_source where source='Wikidata_sparql') THEN
        execute 'INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter)
                 VALUES (''Wikidata_sparql'', ''SELECT ?item ?itemLabel ?itemDescription WHERE {
                            ?item rdfs:label "##value##"@##lang##.
                            SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],##lang##". }
                }'', ''SPARQL'', ''json'', 1, ''alignement avec le vocabulaire Wikidata SPARQL'', false, ''Wikidata_sparql'');';
    END IF;
    IF NOT EXISTS(SELECT *  FROM alignement_source where source='Wikidata_rest') THEN
        execute 'INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) 
                VALUES (''Wikidata_rest'',
                ''https://www.wikidata.org/w/api.php?action=wbsearchentities&language=##lang##&search=##value##&format=json&limit=10'',
                ''REST'', ''json'', 1, ''alignement avec le vocabulaire Wikidata REST'', false, ''Wikidata_rest'');';
    END IF;
end
$$language plpgsql;


--
-- mise a jour de la note pour accepter le texte de gros volume
--
create or replace function update_table_note_constraint() returns void as $$
begin
    if exists (SELECT * from information_schema.table_constraints where table_name = 'note' 
	and constraint_name ='note_notetypecode_id_thesaurus_id_concept_lang_key') then 
	execute
	'ALTER TABLE ONLY note drop CONSTRAINT note_notetypecode_id_thesaurus_id_concept_lang_key;
         ALTER TABLE ONLY note drop CONSTRAINT note_notetypecode_id_thesaurus_id_term_lang_key;';
    END IF;
end
$$language plpgsql;


-- Modification de la table Corpus pour gérer l'option URI uniquement et le tri
--
create or replace function update_table_corpus_link() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='corpus_link' AND column_name='only_uri_link') THEN
        execute 'ALTER TABLE corpus_link ADD COLUMN only_uri_link boolean DEFAULT false;
                 ALTER TABLE corpus_link ADD COLUMN sort integer;';
    END IF;
end
$$language plpgsql;

-- Modification de la table preferences pour enlever la limite de taille des langues
--
ALTER TABLE preferences ALTER COLUMN source_lang Type character varying;
ALTER TABLE languages_iso639 ALTER COLUMN iso639_1 Type character varying;
ALTER TABLE concept_group_label ALTER COLUMN lang Type character varying;
ALTER TABLE preferences ALTER COLUMN generate_handle SET DEFAULT false;


-- Modification de la table des langues iso630, ajout des nouvelles langues
--
create or replace function update_table_languages() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM languages_iso639 where iso639_1 = 'zh-Hans') THEN
        execute 'INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name) VALUES (''zh-Hans'', ''zh-Hans'', ''chinese (simplified)'', ''chinois (simplifié)'');';
    END IF;
    IF NOT EXISTS(SELECT *  FROM languages_iso639 where iso639_1 = 'zh-Hant') THEN
        execute 'INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name) VALUES (''zh-Hant'', ''zh-Hant'', ''chinese (traditional)'', ''chinois (traditionnel)'');';
    END IF;
    IF NOT EXISTS(SELECT *  FROM languages_iso639 where iso639_1 = 'zh-Latn-pinyin') THEN
        execute 'INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name) VALUES (''zh-Latn-pinyin'', ''zh-Latn-pinyin'', ''chinese (pinyin)'', ''chinois (pinyin)'');';
    END IF;
    IF NOT EXISTS(SELECT *  FROM languages_iso639 where iso639_1 = 'bo-x-ewts') THEN
        execute 'INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name) VALUES (''bo-x-ewts'', ''bo-x-ewts'', ''tibetan (ewts)'', ''tibétain (ewts)'');';
    END IF;
end
$$language plpgsql;


-- mise à jour des types de notes --
delete from public.note_type;
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('note', false, true, 'Note', 'Note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('historyNote', true, true, 'Note historique', 'History note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('scopeNote', false, true, 'Note d''application', 'Scope note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('example', true, false, 'Exemple', 'Example');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('editorialNote', true, false, 'Note éditoriale', 'Editorial note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('definition', true, false, 'Définition', 'Definition');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('changeNote', true, false, 'Note de changement', 'Change note');


-- Modification de la table Concept, ajout des colonnes creator et contributor
--
create or replace function update_table_concept_role() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='concept' AND column_name='creator') THEN
        execute 'ALTER TABLE concept ADD COLUMN creator integer DEFAULT -1;
                 ALTER TABLE concept ADD COLUMN contributor integer DEFAULT -1;';
    END IF;
end
$$language plpgsql;


-- Préférences : ajout des paramètres pour la génération de l'identifiant ARK en local
--
create or replace function update_table_preferences_ark_local() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='use_ark_local') THEN
        execute 'ALTER TABLE preferences ADD COLUMN use_ark_local boolean DEFAULT false;
                 ALTER TABLE preferences ADD COLUMN naan_ark_local character varying DEFAULT ''''::character varying;
                 ALTER TABLE preferences ADD COLUMN prefix_ark_local character varying DEFAULT ''''::character varying;
                 ALTER TABLE preferences ADD COLUMN sizeid_ark_local integer DEFAULT 10;
                 ';
    END IF;
end
$$language plpgsql;

-- pour la gestion des ressources externes
--
CREATE TABLE IF NOT EXISTS external_resources
(
    id_concept character varying COLLATE pg_catalog."default" NOT NULL,
    id_thesaurus character varying COLLATE pg_catalog."default" NOT NULL,
    description character varying COLLATE pg_catalog."default",
    id_user integer,
    external_uri character varying COLLATE pg_catalog."default" NOT NULL DEFAULT ''::character varying,
    CONSTRAINT external_resources_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri)
);

-- Préférences : ajout de l'option activation ou non du fil d'ariane
--
create or replace function update_table_preferences_breadcrumb() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='breadcrumb') THEN
        execute 'ALTER TABLE preferences ADD COLUMN breadcrumb boolean DEFAULT true;';
    END IF;
end
$$language plpgsql;

-- Préférences : ajout de l'option activation ou non du fil d'ariane
--
create or replace function update_table_preferences_useConceptTree() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='useconcepttree') THEN
        execute 'ALTER TABLE preferences ADD COLUMN useconcepttree boolean DEFAULT false;';
    END IF;
end
$$language plpgsql;


-- fonction pour le tri naturel
CREATE OR REPLACE FUNCTION naturalsort(
	text)
    RETURNS bytea
    LANGUAGE 'sql'
    COST 100
    IMMUTABLE STRICT PARALLEL UNSAFE
AS $BODY$
    select string_agg(convert_to(coalesce(r[2], length(length(r[1])::text) || length(r[1])::text || r[1]), 'SQL_ASCII'),'\x00')
    from regexp_matches($1, '0*([0-9]+)|([^0-9]+)', 'g') r;
$BODY$;


-- table pour typer les concepts 
CREATE TABLE IF NOT EXISTS concept_type
(
    code text COLLATE pg_catalog."default" NOT NULL,
    label_fr text COLLATE pg_catalog."default" NOT NULL,
    label_en text COLLATE pg_catalog."default",
    CONSTRAINT concept_type_pkey PRIMARY KEY (code)
);
INSERT INTO concept_type (code, label_fr, label_en) SELECT 'concept', 'concept', 'concept' WHERE NOT EXISTS (SELECT code FROM concept_type WHERE code = 'concept');
INSERT INTO concept_type (code, label_fr, label_en) SELECT 'people', 'personne', 'people' WHERE NOT EXISTS (SELECT code FROM concept_type WHERE code = 'people');
INSERT INTO concept_type (code, label_fr, label_en) SELECT 'period', 'période', 'period' WHERE NOT EXISTS (SELECT code FROM concept_type WHERE code = 'period');
INSERT INTO concept_type (code, label_fr, label_en) SELECT 'place', 'lieu', 'place' WHERE NOT EXISTS (SELECT code FROM concept_type WHERE code = 'place');


-- Modification de la table Concept pour gérer le type de concept (Personne, Lieu, période ...)
--
create or replace function update_table_concept_type() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='concept' AND column_name='concept_type') THEN
        execute 'ALTER TABLE concept ADD COLUMN concept_type text DEFAULT ''concept''::text;';
    END IF;
end
$$language plpgsql;

----------------------------------------------------------------------------
-- exécution des fonctions
----------------------------------------------------------------------------
SELECT update_table_preferences_original_uri_doi();
SELECT update_table_preferences_sortbynotation();
SELECT update_table_preferences_tree_cache();
SELECT update_table_candidat_vote();
SELECT update_table_candidat_status();
SELECT update_table_info_constraint();
SELECT update_table_info();
SELECT update_table_corpus_link();
SELECT delete_table_thesaurus_array();
SELECT update_table_concept_doi();
SELECT update_table_concept_group_doi();
SELECT update_table_languages();
SELECT update_table_alignement_source();
SELECT update_table_note_constraint();
SELECT update_table_corpus_link();
SELECT update_table_languages();
SELECT update_table_concept_role();
SELECT update_table_preferences_ark_local();
SELECT update_table_preferences_breadcrumb();
SELECT update_table_concept_type();
SELECT update_table_preferences_useConceptTree();

----------------------------------------------------------------------------
-- suppression des fonctions
----------------------------------------------------------------------------
SELECT delete_fonction('update_table_preferences_original_uri_doi','');
SELECT delete_fonction('update_table_preferences_sortbynotation','');
SELECT delete_fonction('update_table_preferences_tree_cache','');
SELECT delete_fonction('update_table_candidat_vote','');
SELECT delete_fonction('update_table_candidat_status','');
SELECT delete_fonction('update_table_info_constraint','');
SELECT delete_fonction('update_table_info','');
SELECT delete_fonction('update_table_corpus_link','');
SELECT delete_fonction('delete_table_thesaurus_array','');
SELECT delete_fonction('update_table_concept_doi','');
SELECT delete_fonction('update_table_concept_group_doi','');
SELECT delete_fonction('update_table_languages','');
SELECT delete_fonction('update_table_alignement_source','');
SELECT delete_fonction('update_table_note_constraint','');
SELECT delete_fonction('update_table_corpus_link','');
SELECT delete_fonction('update_table_languages','');
SELECT delete_fonction('update_table_concept_role','');
SELECT delete_fonction('update_table_preferences_ark_local','');
SELECT delete_fonction('update_table_preferences_breadcrumb','');
SELECT delete_fonction('update_table_concept_type','');
SELECT delete_fonction('update_table_preferences_useConceptTree', '');

-- auto_suppression de nettoyage
SELECT delete_fonction ('delete_fonction','TEXT','TEXT');
select delete_fonction1('delete_fonction','TEXT','TEXT');
SELECT delete_fonction1 ('delete_fonction1','TEXT','TEXT');

