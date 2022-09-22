

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

-- Préférences : ajout de l'option activation ou non le dernier onglet de l'arbre avec collection
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


-- Préférences : ajout de l'option pour activer l'affichage des noms pour les personnes qui ont changé les concepts
--
create or replace function update_table_preferences_displayUserName() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='display_user_name') THEN
        execute 'ALTER TABLE preferences ADD COLUMN display_user_name boolean DEFAULT false;';
END IF;
end
$$language plpgsql;

-- Préférences : ajout de l'option pour activer le module de propositions d'amélioration
--
create or replace function update_table_preferences_suggestion() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='suggestion') THEN
        execute 'ALTER TABLE preferences ADD COLUMN suggestion boolean DEFAULT false;';
END IF;
end
$$language plpgsql;


-- Module proposition --
CREATE TABLE IF NOT EXISTS proposition_modification
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    id_concept character varying COLLATE pg_catalog."default" NOT NULL,
    id_theso character varying COLLATE pg_catalog."default" NOT NULL,
    status character varying COLLATE pg_catalog."default" NOT NULL,
    nom character varying COLLATE pg_catalog."default" NOT NULL,
    email character varying COLLATE pg_catalog."default" NOT NULL,
    commentaire character varying COLLATE pg_catalog."default",
    approuve_par character varying COLLATE pg_catalog."default",
    approuve_date timestamp with time zone,
    lang character varying COLLATE pg_catalog."default",
    date character varying COLLATE pg_catalog."default",
    CONSTRAINT proposition_modification_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS proposition_modification_detail
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    id_proposition integer NOT NULL,
    categorie character varying COLLATE pg_catalog."default" NOT NULL,
    value character varying COLLATE pg_catalog."default" NOT NULL,
    action character varying COLLATE pg_catalog."default",
    lang character varying COLLATE pg_catalog."default",
    old_value character varying COLLATE pg_catalog."default",
    hiden boolean,
    status character varying COLLATE pg_catalog."default",
    id_term character varying COLLATE pg_catalog."default",
    CONSTRAINT proposition_modification_detail_pkey PRIMARY KEY (id)
);
-- Fin Module proposition --

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
SELECT update_table_preferences_displayUserName();
SELECT update_table_preferences_suggestion();



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
SELECT delete_fonction('update_table_preferences_displayUserName', '');
SELECT delete_fonction('update_table_preferences_suggestion', '');



-- auto_suppression de nettoyage
SELECT delete_fonction ('delete_fonction','TEXT','TEXT');
select delete_fonction1('delete_fonction','TEXT','TEXT');
SELECT delete_fonction1 ('delete_fonction1','TEXT','TEXT');


-- languages_iso639 TABLE
DROP TABLE languages_iso639;


CREATE TABLE public.languages_iso639 (
    iso639_1 character varying,
    iso639_2 character varying,
    english_name character varying,
    french_name character varying,
    id integer DEFAULT nextval('public.languages_id_seq'::regclass) NOT NULL,
    code_pays character varying
);


INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('cr', 'cre', 'Cree', 'cree', 32, NULL);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ie', 'ile', 'Interlingue; Occidental', 'interlingue', 71, NULL);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('zh', 'chi (B)
zho (T)', 'Chinese', 'chinois', 28, 'cn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('cs', 'cze (B)
ces (T)', 'Czech', 'tchèque', 34, 'cz');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('da', 'dan', 'Danish', 'danois', 35, 'dk');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sq', 'alb (B)
sqi (T)', 'Albanian', 'albanais', 6, 'al');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ar', 'ara', 'Arabic', 'arabe', 8, 'lb');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('be', 'bel', 'Belarusian', 'biélorusse', 18, 'by');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('bs', 'bos', 'Bosnian', 'bosniaque', 22, 'ba');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('bg', 'bul', 'Bulgarian', 'bulgare', 24, 'bg');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ko', 'kor', 'Korean', 'coréen', 88, 'kr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('lv', 'lav', 'Latvian', 'letton', 93, 'lv');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('fa', 'per (B)
fas (T)', 'Persian', 'persan', 126, 'ir');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ga', 'gle', 'Irish', 'irlandais', 52, 'ie');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('hi', 'hin', 'Hindi', 'hindi', 61, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('no', 'nor', 'Norwegian', 'norvégien', 118, 'no');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('cy', 'wel (B)
cym (T)', 'Welsh', 'gallois', 33, 'gb-wls');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('dv', 'div', 'Divehi; Dhivehi; Maldivian', 'maldivien', 37, 'mv');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('dz', 'dzo', 'Dzongkha', 'dzongkha', 38, 'bt');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ak', 'aka', 'Akan', 'akan', 5, 'gh');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('av', 'ava', 'Avaric', 'avar', 11, 'ge');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ae', 'ave', 'Avestan', 'avestique', 12, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ay', 'aym', 'Aymara', 'aymara', 13, 'bo');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('az', 'aze', 'Azerbaijani', 'azéri', 14, 'az');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ba', 'bak', 'Bashkir', 'bachkir', 15, 'ru');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('bm', 'bam', 'Bambara', 'bambara', 16, 'ml');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('nl', 'dut (B)
nld (T)', 'Dutch; Flemish', 'néerlandais; flamand', 116, 'nl');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('he', 'heb', 'Hebrew', 'hébreu', 59, 'il');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('aa', 'aar', 'Afar', 'afar', 2, 'et');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ab', 'abk', 'Abkhazian', 'abkhaze', 3, 'ge');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('af', 'afr', 'Afrikaans', 'afrikaans', 4, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('am', 'amh', 'Amharic', 'amharique', 7, 'et');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('an', 'arg', 'Aragonese', 'aragonais', 9, 'es');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('as', 'asm', 'Assamese', 'assamais', 10, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('eu', 'baq (B)
eus (T)', 'Basque', 'basque', 17, 'es');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('bn', 'ben', 'Bengali', 'bengali', 19, 'bd');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('lo', 'lao', 'Lao', 'lao', 91, 'la');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('eo', 'epo', 'Esperanto', 'espéranto', 41, 'ca');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ee', 'ewe', 'Ewe', 'éwé', 43, 'gh');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('fo', 'fao', 'Faroese', 'féroïen', 44, 'dk');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('fi', 'fin', 'Finnish', 'finnois', 46, 'fi');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('fy', 'fry', 'Western Frisian', 'frison occidental', 48, 'nl');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ff', 'ful', 'Fulah', 'peul', 49, 'sn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ka', 'geo (B)
kat (T)', 'Georgian', 'géorgien', 50, 'ge');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('kn', 'kan', 'Kannada', 'kannada', 78, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ks', 'kas', 'Kashmiri', 'kashmiri', 79, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('kr', 'kau', 'Kanuri', 'kanouri', 80, 'ne');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('kk', 'kaz', 'Kazakh', 'kazakh', 81, 'kz');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('km', 'khm', 'Central Khmer', 'khmer central', 82, 'kh');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ki', 'kik', 'Kikuyu; Gikuyu', 'kikuyu', 83, 'cf');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('kv', 'kom', 'Komi', 'kom', 86, 'cm');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('kg', 'kon', 'Kongo', 'kongo', 87, 'cd');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('kj', 'kua', 'Kuanyama; Kwanyama', 'kuanyama; kwanyama', 89, 'ao');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ku', 'kur', 'Kurdish', 'kurde', 90, 'tr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('hz', 'her', 'Herero', 'herero', 60, 'ao');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('gl', 'glg', 'Galician', 'galicien', 53, 'es');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('gv', 'glv', 'Manx', 'manx; mannois', 54, 'im');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('gn', 'grn', 'Guarani', 'guarani', 55, 'py');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('gu', 'guj', 'Gujarati', 'goudjrati', 56, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ht', 'hat', 'Haitian; Haitian Creole', 'haïtien; créole haïtien', 57, 'ht');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ha', 'hau', 'Hausa', 'haoussa', 58, 'gh');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ho', 'hmo', 'Hiri Motu', 'hiri motu', 62, 'pg');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('hr', 'hrv', 'Croatian', 'croate', 63, 'hr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('hu', 'hun', 'Hungarian', 'hongrois', 64, 'hu');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('hy', 'arm (B)
hye (T)', 'Armenian', 'arménien', 65, 'am');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ig', 'ibo', 'Igbo', 'igbo', 66, 'ng');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('io', 'ido', 'Ido', 'ido', 68, 'pg');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('jv', 'jav', 'Javanese', 'javanais', 75, 'fr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('os', 'oss', 'Ossetian; Ossetic', 'ossète', 124, 'ru');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('pi', 'pli', 'Pali', 'pali', 127, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ky', 'kir', 'Kirghiz; Kyrgyz', 'kirghiz', 85, 'kg');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('mk', 'mac (B)
mkd (T)', 'Macedonian', 'macédonien', 100, 'mk');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('mh', 'mah', 'Marshallese', 'marshall', 101, 'mh');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ml', 'mal', 'Malayalam', 'malayalam', 102, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ms', 'may (B)
msa (T)', 'Malay', 'malais', 104, 'my');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('mg', 'mlg', 'Malagasy', 'malgache', 105, 'mg');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('mi', 'mao (B)
mri (T)', 'Maori', 'maori', 108, 'nz');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('my', 'bur (B)
mya (T)', 'Burmese', 'birman', 109, 'mm');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('nv', 'nav', 'Navajo; Navaho', 'navaho', 111, 'mx');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('nb', 'nob', 'Bokmål, Norwegian; Norwegian Bokmål', 'norvégien bokmål', 117, 'no');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ny', 'nya', 'Chichewa; Chewa; Nyanja', 'chichewa; chewa; nyanja', 119, 'mw');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('pa', 'pan', 'Panjabi; Punjabi', 'pendjabi', 125, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('lu', 'lub', 'Luba-Katanga', 'luba-katanga', 98, 'cg');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('lg', 'lug', 'Ganda', 'ganda', 99, 'ug');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ik', 'ipk', 'Inupiaq', 'inupiaq', 73, 'us');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('nr', 'nbl', 'Ndebele, South; South Ndebele', 'ndébélé du Sud', 112, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('oj', 'oji', 'Ojibwa', 'ojibwa', 121, 'ca');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('or', 'ori', 'Oriya', 'oriya', 122, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('na', 'nau', 'Nauru', 'nauruan', 110, 'nr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ln', 'lin', 'Lingala', 'lingala', 95, 'cd');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('li', 'lim', 'Limburgan; Limburger; Limburgish', 'limbourgeois', 94, 'nl');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('mr', 'mar', 'Marathi', 'marathe', 103, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('kl', 'kal', 'Kalaallisut; Greenlandic', 'groenlandais', 77, 'dk');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('iu', 'iku', 'Inuktitut', 'inuktitut', 70, 'ca');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('nd', 'nde', 'Ndebele, North; North Ndebele', 'ndébélé du Nord', 113, 'zw');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ng', 'ndo', 'Ndonga', 'ndonga', 114, 'na');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('om', 'orm', 'Oromo', 'galla', 123, 'ke');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ii', 'iii', 'Sichuan Yi; Nuosu', 'yi de Sichuan', 69, 'cn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('oc', 'oci', 'Occitan (post 1500)', 'occitan (après 1500)', 120, 'fn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('gd', 'gla', 'Gaelic; Scottish Gaelic', 'gaélique; gaélique écossais', 51, 'ie');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('en', 'eng', 'English', 'anglais', 40, 'gb');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('iso', 'iso', 'norme ISO 233-2 (1993)', 'norme ISO 233-2 (1993)', 187, NULL);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ala', 'ala', 'ALA-LC Romanization Table (American Library Association-Library of Congress)', 'ALA-LC)', 188, NULL);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('de', 'ger (B)
deu (T)', 'German', 'allemand', 36, 'de');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('fr', 'fre (B)
fra (T)', 'French', 'français', 47, 'fr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('mt', 'mlt', 'Maltese', 'maltais', 106, 'mt');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('mn', 'mon', 'Mongolian', 'mongol', 107, 'mn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ne', 'nep', 'Nepali', 'népalais', 115, 'np');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('et', 'est', 'Estonian', 'estonien', 42, 'est');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('fj', 'fij', 'Fijian', 'fidjien', 45, 'fj');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('el', 'gre (B)
ell (T)', 'Greek, Modern (1453-)', 'grec moderne (après 1453)', 39, 'gr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('is', 'ice (B)
isl (T)', 'Icelandic', 'islandais', 67, 'is');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('id', 'ind', 'Indonesian', 'indonésien', 72, 'id');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('it', 'ita', 'Italian', 'italien', 74, 'it');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ja', 'jpn', 'Japanese', 'japonais', 76, 'jp');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('rw', 'kin', 'Kinyarwanda', 'rwanda', 84, 'rw');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('pt', 'por', 'Portuguese', 'portugais', 129, 'pt');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ru', 'rus', 'Russian', 'russe', 135, 'ru');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sk', 'slo (B)
slk (T)', 'Slovak', 'slovaque', 139, 'sk');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('qu', 'que', 'Quechua', 'quechua', 131, 'pe');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('rm', 'roh', 'Romansh', 'romanche', 132, 'ro');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ro', 'rum (B)
ron (T)', 'Romanian; Moldavian; Moldovan', 'roumain; moldave', 133, 'ro');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sl', 'slv', 'Slovenian', 'slovène', 140, 'si');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('so', 'som', 'Somali', 'somali', 145, 'so');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sv', 'swe', 'Swedish', 'suédois', 153, 'se');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('th', 'tha', 'Thai', 'thaï', 160, 'th');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('mul', 'mul', 'multiple langages', 'multiple langages', 189, NULL);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('to', 'ton', 'Tonga (Tonga Islands)', 'tongan (Îles Tonga)', 163, 'to');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('uk', 'ukr', 'Ukrainian', 'ukrainien', 170, 'ua');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('vi', 'vie', 'Vietnamese', 'vietnamien', 174, 'vn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('nn', 'nno', 'Norwegian Nynorsk; Nynorsk, Norwegian', 'norvégien nynorsk', 185, 'no');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('gr', 'grc', 'Greek, Ancient (to 1453)', 'grec ancien (jusqu''à 1453)', 186, 'gr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('fro', 'fro', 'Old French (842—ca. 1400)', 'ancien français (842-environ 1400)', 190, 'fr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('zh-Hans', 'zh-Hans', 'chinese (simplified)', 'chinois (simplifié)', 191, 'cn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('zh-Hant', 'zh-Hant', 'chinese (traditional)', 'chinois (traditionnel)', 192, 'cn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('zh-Latn-pinyin', 'zh-Latn-pinyin', 'chinese (pinyin)', 'chinois (pinyin)', 193, 'cn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('pl', 'pol', 'Polish', 'polonais', 128, 'pl');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ps', 'pus', 'Pushto; Pashto', 'pachto', 130, 'af');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('rn', 'run', 'Rundi', 'rundi', 134, 'BI');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sg', 'sag', 'Sango', 'sango', 136, 'cf');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sa', 'san', 'Sanskrit', 'sanskrit', 137, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('si', 'sin', 'Sinhala; Sinhalese', 'singhalais', 138, 'lk');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('se', 'sme', 'Northern Sami', 'sami du Nord', 141, 'no');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sm', 'smo', 'Samoan', 'samoan', 142, 'pf');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sn', 'sna', 'Shona', 'shona', 143, 'zw');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sd', 'snd', 'Sindhi', 'sindhi', 144, 'pk');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('es', 'spa', 'Spanish; Castilian', 'espagnol; castillan', 147, 'es');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sc', 'srd', 'Sardinian', 'sarde', 148, 'it');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sr', 'srp', 'Serbian', 'serbe', 149, 'rs');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ss', 'ssw', 'Swati', 'swati', 150, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('su', 'sun', 'Sundanese', 'soundanais', 151, 'pf');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('sw', 'swa', 'Swahili', 'swahili', 152, 'tz');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ty', 'tah', 'Tahitian', 'tahitien', 154, 'pf');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ta', 'tam', 'Tamil', 'tamoul', 155, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('tt', 'tat', 'Tatar', 'tatar', 156, 'ru');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('tr', 'tur', 'Turkish', 'turc', 167, 'tr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ur', 'urd', 'Urdu', 'ourdou', 171, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('uz', 'uzb', 'Uzbek', 'ouszbek', 172, 'uz');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ve', 'ven', 'Venda', 'venda', 173, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('vo', 'vol', 'Volapük', 'volapük', 175, 'de');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('wa', 'wln', 'Walloon', 'wallon', 176, 'be');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('wo', 'wol', 'Wolof', 'wolof', 177, 'sn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('zu', 'zul', 'Zulu', 'zoulou', 182, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('te', 'tel', 'Telugu', 'télougou', 157, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('tg', 'tgk', 'Tajik', 'tadjik', 158, 'tj');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('tl', 'tgl', 'Tagalog', 'tagalog', 159, 'ph');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ti', 'tir', 'Tigrinya', 'tigrigna', 162, 'et');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('tn', 'tsn', 'Tswana', 'tswana', 164, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ts', 'tso', 'Tsonga', 'tsonga', 165, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('tk', 'tuk', 'Turkmen', 'turkmène', 166, 'tm');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('tw', 'twi', 'Twi', 'twi', 168, 'gh');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ug', 'uig', 'Uighur; Uyghur', 'ouïgour', 169, 'tr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('xh', 'xho', 'Xhosa', 'xhosa', 178, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('yi', 'yid', 'Yiddish', 'yiddish', 179, 'in');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('yo', 'yor', 'Yoruba', 'yoruba', 180, 'cg');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('bh', 'bih', 'Bihari languages', 'langues biharis', 20, 'np');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('bi', 'bis', 'Bislama', 'bichlamar', 21, 'vu');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('br', 'bre', 'Breton', 'breton', 23, 'fr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ca', 'cat', 'Catalan; Valencian', 'catalan; valencien', 25, 'es');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ch', 'cha', 'Chamorro', 'chamorro', 26, 'us');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ce', 'che', 'Chechen', 'tchétchène', 27, 'ru');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('cv', 'chv', 'Chuvash', 'tchouvache', 29, 'tr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('kw', 'cor', 'Cornish', 'cornique', 30, 'gb');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('co', 'cos', 'Corsican', 'corse', 31, 'fr');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('la', 'lat', 'Latin', 'latin', 92, 'va');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('lt', 'lit', 'Lithuanian', 'lituanien', 96, 'lt');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('lb', 'ltz', 'Luxembourgish; Letzeburgesch', 'luxembourgeois', 97, 'lu');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('bo', 'tib (B)
bod (T)', 'Tibetan', 'tibétain', 161, 'cn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('za', 'zha', 'Zhuang; Chuang', 'zhuang; chuang', 181, 'cn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('bo-x-ewts', 'bo-x-ewts', 'tibetan (ewts)', 'tibétain (ewts)', 194, 'np');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('cu', 'chu', 'Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic', 'vieux slave; vieux bulgare', 183, 'bg');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('st', 'sot', 'Sotho, Southern', 'sotho du Sud', 146, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ia', 'ina', 'Interlingua (International Auxiliary Language Association)', 'interlingua', 184, NULL);


ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_iso639_1_key UNIQUE (iso639_1);

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_pkey PRIMARY KEY (id);
