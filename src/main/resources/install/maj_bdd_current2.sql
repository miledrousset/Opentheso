

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
                VALUES (''Ontome'',
                ''https://www.wikidata.org/w/api.php?action=wbsearchentities&language=##lang##&search=##value##&format=json&limit=10'',
                ''REST'', ''json'', 1, ''alignement avec le vocabulaire Wikidata REST'', false, ''Wikidata_rest'');';
    END IF;
    IF NOT EXISTS(SELECT *  FROM alignement_source where source='Ontome') THEN
        execute 'INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter)
                VALUES (''Ontome'',
                ''https://ontome.net/api/classes-type-descendants/label/##value##/json'',
                ''REST'', ''json'', 1, ''OntoME is a LARHRA application, developed and maintained by the Digital history research team'', false, ''Ontome'');';
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

-- enlever la contrainte des dates not null
ALTER TABLE concept ALTER COLUMN created DROP DEFAULT;
ALTER TABLE concept ALTER COLUMN modified DROP DEFAULT;
ALTER TABLE concept ALTER COLUMN created drop not null;
ALTER TABLE concept ALTER COLUMN modified drop not null;



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
INSERT INTO concept_type (code, label_fr, label_en) SELECT 'qualifier', 'qualificatif', 'qualifier' WHERE NOT EXISTS (SELECT code FROM concept_type WHERE code = 'qualifier');
INSERT INTO concept_type (code, label_fr, label_en) SELECT 'attribute', 'attribut', 'attribute' WHERE NOT EXISTS (SELECT code FROM concept_type WHERE code = 'attribute');
INSERT INTO concept_type (code, label_fr, label_en) SELECT 'attitude', 'attitude', 'attitude' WHERE NOT EXISTS (SELECT code FROM concept_type WHERE code = 'attitude');

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


-- Modification de la table note pour ajouter la source de la note
--
create or replace function update_table_note_source() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='note' AND column_name='notesource') THEN
        execute 'ALTER TABLE note ADD COLUMN notesource character varying COLLATE pg_catalog."default";';
    END IF;
end
$$language plpgsql;

--
-- mise a jour de la table GPS pour permettre de gérer la polyline
--
create or replace function update_table_gps_constraint() returns void as $$
begin
    if exists (SELECT * from information_schema.table_constraints where table_name = 'gps'
	and constraint_name ='gps_pkey') then
	execute
	'ALTER TABLE ONLY gps drop CONSTRAINT gps_pkey;
         Alter TABLE ONLY gps add CONSTRAINT gps_pkey2 PRIMARY KEY (id_concept, id_theso, latitude, longitude);';
    END IF;
end
$$language plpgsql;

-- proposition : ajout de la colonne commentaire de l'administrateur
--
create or replace function update_table_proposition_modification() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='proposition_modification' AND column_name='admin_comment') THEN
        execute 'ALTER TABLE proposition_modification ADD COLUMN admin_comment character varying COLLATE pg_catalog."default";';
END IF;
end
$$language plpgsql;


-- Préférences : ajout des options pour qualifier, attribute et attitude
--
create or replace function update_table_preferences_custom_relation() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='use_custom_relation') THEN
        execute 'ALTER TABLE preferences ADD COLUMN use_custom_relation boolean DEFAULT false;';
END IF;
end
$$language plpgsql;

-- Mise à jour de la table Concept_type 
--
create or replace function update_table_concept_type2() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='concept_type' AND column_name='reciprocal') THEN
        execute 'ALTER TABLE concept_type ADD COLUMN reciprocal boolean DEFAULT false;
                 ALTER TABLE concept_type ADD COLUMN id_theso character varying COLLATE pg_catalog."default" NOT NULL DEFAULT ''all''::character varying;';
    END IF;
end
$$language plpgsql;



-- Préférences : ajout une option pour choisir si l'id ARK est majuscule ou non 
--
create or replace function update_table_preferences_uppercase_ark() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='uppercase_for_ark') THEN
        execute 'ALTER TABLE preferences ADD COLUMN uppercase_for_ark boolean DEFAULT false;';
END IF;
end
$$language plpgsql;

-- Collections : ajout des dates (created et modified )
--
create or replace function update_table_group() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='concept_group' AND column_name='created') THEN
        execute 'ALTER TABLE concept_group ADD COLUMN created timestamp without time zone;
                 ALTER TABLE concept_group ADD COLUMN modified timestamp without time zone;
                ';
END IF;
end
$$language plpgsql;


CREATE TABLE IF NOT EXISTS public.concept_dcterms
(
    id_concept character varying COLLATE pg_catalog."default" NOT NULL,
    id_thesaurus character varying COLLATE pg_catalog."default" NOT NULL,
    name character varying COLLATE pg_catalog."default" NOT NULL,
    value character varying COLLATE pg_catalog."default" NOT NULL,
    language character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT concept_dc_terms_pkey PRIMARY KEY (id_concept, id_thesaurus, name, value, language)
);
CREATE TABLE IF NOT EXISTS public.thesaurus_dcterms
(
    id_thesaurus character varying COLLATE pg_catalog."default" NOT NULL,
    name character varying COLLATE pg_catalog."default" NOT NULL,
    value character varying COLLATE pg_catalog."default" NOT NULL,
    language character varying COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT thesaurus_dcterms_pkey PRIMARY KEY (id_thesaurus, name, value, language)
);

-- Users : suppression de la table inutile User2
--
create or replace function delete_table_user2() returns void as $$
begin
    IF EXISTS(SELECT *  FROM information_schema.columns where table_name='users2') THEN
        execute 'Drop TABLE users2;';
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
SELECT update_table_alignement_source();
SELECT update_table_note_constraint();
SELECT update_table_corpus_link();
SELECT update_table_concept_role();
SELECT update_table_preferences_ark_local();
SELECT update_table_preferences_breadcrumb();
SELECT update_table_concept_type();
SELECT update_table_preferences_useConceptTree();
SELECT update_table_preferences_displayUserName();
SELECT update_table_preferences_suggestion();
SELECT update_table_note_source();
SELECT update_table_gps_constraint();
SELECT update_table_proposition_modification();
SELECT update_table_preferences_custom_relation();
SELECT update_table_concept_type2();
SELECT update_table_preferences_uppercase_ark();
SELECT update_table_group();
SELECT delete_table_user2();


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
SELECT delete_fonction('update_table_alignement_source','');
SELECT delete_fonction('update_table_note_constraint','');
SELECT delete_fonction('update_table_corpus_link','');
SELECT delete_fonction('update_table_concept_role','');
SELECT delete_fonction('update_table_preferences_ark_local','');
SELECT delete_fonction('update_table_preferences_breadcrumb','');
SELECT delete_fonction('update_table_concept_type','');
SELECT delete_fonction('update_table_preferences_useConceptTree', '');
SELECT delete_fonction('update_table_preferences_displayUserName', '');
SELECT delete_fonction('update_table_preferences_suggestion', '');
SELECT delete_fonction('update_table_note_source', '');
SELECT delete_fonction('update_table_gps_constraint', '');
SELECT delete_fonction('update_table_proposition_modification', '');
SELECT delete_fonction('update_table_preferences_custom_relation', '');
SELECT delete_fonction('update_table_concept_type2', '');
SELECT delete_fonction('update_table_preferences_uppercase_ark', '');
SELECT delete_fonction('update_table_group', '');
SELECT delete_fonction('delete_table_user2', '');


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
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ar', 'ara', 'Arabic', 'arabe', 8, 'dad');
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
bod (T)', 'Tibetan', 'tibétain', 161, 'tibet');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('za', 'zha', 'Zhuang; Chuang', 'zhuang; chuang', 181, 'cn');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('bo-x-ewts', 'bo-x-ewts', 'tibetan (ewts)', 'tibétain (ewts)', 194, 'tibet');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('cu', 'chu', 'Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic', 'vieux slave; vieux bulgare', 183, 'bg');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('st', 'sot', 'Sotho, Southern', 'sotho du Sud', 146, 'za');
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('ia', 'ina', 'Interlingua (International Auxiliary Language Association)', 'interlingua', 184, NULL);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id, code_pays) VALUES ('metadata', 'Metadata', 'métadonnées', 'Metadata', 2000, NULL);

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_iso639_1_key UNIQUE (iso639_1);

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_pkey PRIMARY KEY (id);




-- FUNCTION: public.opentheso_get_alter_term(character varying, character varying, boolean)

-- DROP FUNCTION public.opentheso_get_alter_term(character varying, character varying, boolean);

CREATE OR REPLACE FUNCTION public.opentheso_get_alter_term(
	id_theso character varying,
	id_con character varying,
	ishiden boolean)
    RETURNS TABLE(alter_term_lexical_value character varying, alter_term_source character varying, alter_term_status character varying, alter_term_lang character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT non_preferred_term.lexical_value, non_preferred_term.source, non_preferred_term.status, non_preferred_term.lang
		FROM non_preferred_term, preferred_term
		WHERE preferred_term.id_term = non_preferred_term.id_term
		AND preferred_term.id_thesaurus = non_preferred_term.id_thesaurus
		AND preferred_term.id_concept = id_con
		AND non_preferred_term.id_thesaurus = id_theso
		AND non_preferred_term.hiden = isHiden;
end;
$BODY$;


-- FUNCTION: public.opentheso_get_gps(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_gps(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_gps(
	id_thesorus character varying,
	id_con character varying)
    RETURNS TABLE(gps_latitude double precision, gps_longitude double precision)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT latitude, longitude
		FROM gps
		WHERE id_theso = id_thesorus
		AND id_concept = id_con;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_groups(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_groups(character varying, character varying);

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
		WHERE concept_group.idgroup = concept_group_concept.idgroup
		AND concept_group.idthesaurus = concept_group_concept.idthesaurus
		AND concept_group_concept.idthesaurus = id_theso
		AND concept_group_concept.idconcept = id_con;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_images(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_images(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_images(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(name character varying, copyright character varying, url character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT image_name, image_copyright, external_uri
		FROM external_images
		WHERE id_thesaurus = id_theso
		AND id_concept = id_con;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_note_concept(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_note_concept(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_note_concept(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(note_id integer, note_notetypecode text, note_lexicalvalue character varying, note_created timestamp without time zone, note_modified timestamp without time zone, note_lang character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT note.id, note.notetypecode, note.lexicalvalue, note.created, note.modified, note.lang
		FROM note, note_type
		WHERE note.notetypecode = note_type.code
		AND note_type.isconcept = true
		AND note.id_thesaurus = id_theso
		AND note.id_concept = id_con;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_note_term(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_note_term(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_note_term(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(note_id integer, note_notetypecode text, note_lexicalvalue character varying, note_created timestamp without time zone, note_modified timestamp without time zone, note_lang character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT note.id, note.notetypecode, note.lexicalvalue, note.created, note.modified, note.lang
		FROM note
		WHERE id_thesaurus = id_theso
		AND note.id_term IN (SELECT id_term
							 FROM preferred_term
							 WHERE id_thesaurus = id_theso
							 AND id_concept = id_con);
end;
$BODY$;


-- FUNCTION: public.opentheso_get_relations(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_relations(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_relations(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(relationship_id_concept character varying, relationship_role character varying, relationship_id_ark character varying, relationship_id_handle character varying, relationship_id_doi character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
                select id_concept2, role, id_ark, id_handle, id_doi
                from hierarchical_relationship, concept
                where  hierarchical_relationship.id_concept2 = concept.id_concept
                and hierarchical_relationship.id_thesaurus = concept.id_thesaurus
                and hierarchical_relationship.id_thesaurus = id_theso
                and hierarchical_relationship.id_concept1 = id_con
                and concept.status != 'CA';
end;
$BODY$;


-- FUNCTION: public.opentheso_get_traductions(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_traductions(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_traductions(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(term_id character varying, term_lexical_value character varying, term_lang character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT term.id_term, term.lexical_value, term.lang
		FROM term, preferred_term
		WHERE term.id_term = preferred_term.id_term
		AND term.id_thesaurus = preferred_term.id_thesaurus
		AND term.id_thesaurus = id_theso
		AND preferred_term.id_concept = id_con
		ORDER BY term.lexical_value;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_uri(boolean, character varying, character varying, boolean, character varying, boolean, character varying, character varying, character varying, character varying)

-- DROP FUNCTION public.opentheso_get_uri(boolean, character varying, character varying, boolean, character varying, boolean, character varying, character varying, character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_uri(
	original_uri_is_ark boolean,
	id_ark character varying,
	original_uri character varying,
	original_uri_is_handle boolean,
	id_handle character varying,
	original_uri_is_doi boolean,
	id_doi character varying,
	id_concept character varying,
	id_theso character varying,
	path character varying)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
BEGIN
	IF (original_uri_is_ark = true AND id_ark IS NOT NULL AND id_ark != '') THEN
		return original_uri || '/' || id_ark;
	ELSIF (original_uri_is_ark = true AND (id_ark IS NULL or id_ark = '')) THEN
		return path || '/?idc=' || id_concept || '&idt=' || id_theso;	
	ELSIF (original_uri_is_handle = true AND id_handle IS NOT NULL AND id_handle != '') THEN
		return 'https://hdl.handle.net/' || id_handle;
	ELSIF (original_uri_is_doi = true AND id_doi IS NOT NULL AND id_doi != '') THEN
		return 'https://doi.org/' || id_doi;
	ELSIF (original_uri IS NOT NULL AND original_uri != '') THEN
		return original_uri || '/?idc=' || id_concept || '&idt=' || id_theso;
	ELSE
		return path || '/?idc=' || id_concept || '&idt=' || id_theso;
	end if;
end;
$BODY$;


-- FUNCTION: public.opentheso_get_alignements(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_alignements(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_alignements(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(alig_uri_target character varying, alig_id_type integer)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT uri_target, alignement_id_type
		FROM alignement
		where internal_id_concept = id_con
		and internal_id_thesaurus = id_theso;
end;
$BODY$;


CREATE OR REPLACE FUNCTION opentheso_get_facettes (id_theso VARCHAR, path VARCHAR)
	RETURNS SETOF RECORD
	LANGUAGE plpgsql
AS $$
DECLARE
	rec record;
	facet_rec record;
	theso_rec record;
	concept_rec record;

	uri_membre VARCHAR;
	id_ark VARCHAR;
	id_handle VARCHAR;
	uri_value VARCHAR;
BEGIN

	SELECT * INTO theso_rec FROM preferences where id_thesaurus = id_theso;

	FOR facet_rec IN SELECT node_label.*, thesaurus_array.id_concept_parent
			   	 FROM node_label, thesaurus_array
			   	 WHERE node_label.id_thesaurus = thesaurus_array.id_thesaurus
			   	 AND node_label.id_facet = thesaurus_array.id_facet
			   	 AND node_label.id_thesaurus = id_theso
    LOOP

		SELECT * INTO concept_rec FROM concept WHERE id_thesaurus = id_theso AND id_concept = facet_rec.id_concept_parent;

		uri_value = opentheso_get_uri(theso_rec.original_uri_is_ark, concept_rec.id_ark, theso_rec.original_uri, theso_rec.original_uri_is_handle,
					 concept_rec.id_handle, theso_rec.original_uri_is_doi, concept_rec.id_doi, facet_rec.id_concept_parent, id_theso, path);

		SELECT facet_rec.id_facet, facet_rec.lexical_value, facet_rec.created, facet_rec.modified, facet_rec.lang,
				facet_rec.id_concept_parent, uri_value INTO rec;

  		RETURN NEXT rec;
    END LOOP;
END;
$$;

CREATE OR REPLACE FUNCTION opentheso_get_concepts_by_group (id_theso VARCHAR, path VARCHAR, id_group VARCHAR)
	RETURNS SETOF RECORD
	LANGUAGE plpgsql
AS $$
DECLARE

	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';

	rec record;
	con record;
	theso_rec record;
	traduction_rec record;
	altLab_rec record;
	altLab_hiden_rec record;
	geo_rec record;
	group_rec record;
	note_concept_rec record;
	note_term_rec record;
	relation_rec record;
	alignement_rec record;
	img_rec record;
	vote_record record;
	message_record record;
	cadidat_record record;
	replace_rec record;
	replacedBy_rec record;
	facet_rec record;
    externalResource_rec record;

	tmp text;
	uri text;
	local_URI text;
	prefLab VARCHAR;
	altLab VARCHAR;
	altLab_hiden VARCHAR;
	membre text;
	definition text;
	secopeNote text;
	note text;
	historyNote text;
	example text;
	changeNote text;
	editorialNote text;
	narrower text;
	broader text;
	related text;
	exactMatch text;
	closeMatch text;
	broadMatch text;
	relatedMatch text;
	narrowMatch text;
	img text;
	creator text;
	contributor text;
	replacedBy text;
	replaces text;
	facets text;
    externalResources text;
    gpsData text;
        
BEGIN

	SELECT * INTO theso_rec FROM preferences where id_thesaurus = id_theso;


	FOR con IN SELECT concept.* FROM concept, concept_group_concept WHERE concept.id_concept = concept_group_concept.idconcept
								AND concept.id_thesaurus = concept_group_concept.idthesaurus AND concept.id_thesaurus = id_theso
								AND concept_group_concept.idgroup = id_group AND concept.status != 'CA'
    LOOP
		-- URI
		uri = opentheso_get_uri(theso_rec.original_uri_is_ark, con.id_ark, theso_rec.original_uri, theso_rec.original_uri_is_handle,
					 con.id_handle, theso_rec.original_uri_is_doi, con.id_doi, con.id_concept, id_theso, path);

		-- LocalUri
		local_URI = path || '/?idc=' || con.id_concept || '&idt=' || id_theso;
		prefLab = '';

		-- PrefLab
		FOR traduction_rec IN SELECT * FROM opentheso_get_traductions(id_theso, con.id_concept)
		LOOP
			prefLab = prefLab || traduction_rec.term_lexical_value || sous_seperateur || traduction_rec.term_lang || seperateur;
		END LOOP;

		-- altLab
		altLab = '';
		FOR altLab_rec IN SELECT * FROM opentheso_get_alter_term(id_theso, con.id_concept, false)
		LOOP
			altLab = altLab || altLab_rec.alter_term_lexical_value || sous_seperateur || altLab_rec.alter_term_lang || seperateur;
		END LOOP;

		-- altLab hiden
		altLab_hiden = '';
		FOR altLab_hiden_rec IN SELECT * FROM opentheso_get_alter_term(id_theso, con.id_concept, true)
		LOOP
			altLab_hiden = altLab_hiden || altLab_hiden_rec.alter_term_lexical_value || sous_seperateur || altLab_hiden_rec.alter_term_lang || seperateur;
		END LOOP;

		-- Notes
		note = '';
		example = '';
		changeNote = '';
		secopeNote = '';
		definition = '';
		historyNote = '';
		editorialNote = '';
		FOR note_concept_rec IN SELECT * FROM opentheso_get_note_concept(id_theso, con.id_concept)
		LOOP
			IF (note_concept_rec.note_notetypecode = 'note') THEN
				note = note || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'example') THEN
				example = example || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			END IF;
		END LOOP;

		FOR note_term_rec IN SELECT * FROM opentheso_get_note_term(id_theso, con.id_concept)
		LOOP
			IF (note_term_rec.note_notetypecode = 'note') THEN
				note = note || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'example') THEN
				example = example || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			END IF;
		END LOOP;

		-- Narrower
		narrower = '';
		broader = '';
		related = '';
		FOR relation_rec IN SELECT * FROM opentheso_get_relations(id_theso, con.id_concept)
		LOOP
			tmp = opentheso_get_uri(theso_rec.original_uri_is_ark, relation_rec.relationship_id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, relation_rec.relationship_id_handle, theso_rec.original_uri_is_doi,
					relation_rec.relationship_id_doi, relation_rec.relationship_id_concept, id_theso, path)
					|| sous_seperateur || relation_rec.relationship_role || sous_seperateur || relation_rec.relationship_id_concept || sous_seperateur ;

			IF (relation_rec.relationship_role = 'NT' OR relation_rec.relationship_role = 'NTP' OR relation_rec.relationship_role = 'NTI'
					OR relation_rec.relationship_role = 'NTG') THEN
				narrower = narrower || tmp || seperateur;
			ELSIF (relation_rec.relationship_role = 'BT' OR relation_rec.relationship_role = 'BTP' OR relation_rec.relationship_role = 'BTI'
					OR relation_rec.relationship_role = 'BTG') THEN
				broader = broader || tmp || seperateur;
			ELSIF (relation_rec.relationship_role = 'RT' OR relation_rec.relationship_role = 'RHP' OR relation_rec.relationship_role = 'RPO') THEN
				related = related || tmp || seperateur;
			END IF;
		END LOOP;

		-- Alignement
		exactMatch = '';
		closeMatch = '';
		broadMatch = '';
		relatedMatch = '';
		narrowMatch = '';
		FOR alignement_rec IN SELECT * FROM opentheso_get_alignements(id_theso, con.id_concept)
		LOOP
			if (alignement_rec.alig_id_type = 1) THEN
				exactMatch = exactMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 2) THEN
				closeMatch = closeMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 3) THEN
				broadMatch = broadMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 4) THEN
				relatedMatch = relatedMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 5) THEN
				narrowMatch = narrowMatch || alignement_rec.alig_uri_target || seperateur;
			END IF;
		END LOOP;

		-- geo:alt && geo:long
		gpsData = '';
        FOR geo_rec IN SELECT * FROM opentheso_get_gps(id_theso, con.id_concept)
        LOOP
            gpsData = gpsData || geo_rec.gps_latitude || sous_seperateur || geo_rec.gps_longitude || seperateur;
        END LOOP;

		-- membre
		membre = '';
		FOR group_rec IN SELECT * FROM opentheso_get_groups(id_theso, con.id_concept)
		LOOP
			IF (theso_rec.original_uri_is_ark = true AND group_rec.group_id_ark IS NOT NULL  AND group_rec.group_id_ark != '') THEN
				membre = membre || theso_rec.original_uri || '/' || group_rec.group_id_ark || seperateur;
			ELSIF (theso_rec.original_uri_is_ark = true AND (group_rec.group_id_ark IS NULL OR group_rec.group_id_ark = '')) THEN
				membre = membre || path || '/?idg=' || group_rec.group_idgroup || '&idt=' || id_theso || seperateur;
			ELSIF (group_rec.group_id_handle IS NOT NULL) THEN
				membre = membre || 'https://hdl.handle.net/' || group_rec.group_id_handle || seperateur;
			ELSIF (theso_rec.original_uri IS NOT NULL) THEN
				membre = membre || theso_rec.original_uri || '/?idg=' || group_rec.group_idgroup || '&idt=' || id_theso || seperateur;
			ELSE
				membre = membre || path || '/?idg=' || group_rec.group_idgroup || '&idt=' || id_theso || seperateur;
			END IF;
		END LOOP;

		-- Images
		img = '';
		FOR img_rec IN SELECT * FROM opentheso_get_images(id_theso, con.id_concept)
		LOOP
                    img = img || img_rec.name || sous_seperateur || img_rec.copyright || sous_seperateur || img_rec.url || seperateur;
		END LOOP;

		SELECT username INTO creator FROM users WHERE id_user = con.creator;
		SELECT username INTO contributor FROM users WHERE id_user = con.contributor;

		replaces = '';
		FOR replace_rec IN SELECT id_concept1, id_ark, id_handle, id_doi
				FROM concept_replacedby, concept
				WHERE concept.id_concept = concept_replacedby.id_concept1
				AND concept.id_thesaurus = concept_replacedby.id_thesaurus
				AND concept_replacedby.id_concept1 = con.id_concept
				AND concept_replacedby.id_thesaurus = id_theso
		LOOP
			replaces = replaces || opentheso_get_uri(theso_rec.original_uri_is_ark, replace_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replace_rec.id_handle, theso_rec.original_uri_is_doi,
					replace_rec.id_doi, replace_rec.id_concept1, id_theso, path) || seperateur;
		END LOOP;

		replacedBy = '';
		FOR replacedBy_rec IN SELECT id_concept2, id_ark, id_handle, id_doi
				FROM concept_replacedby, concept
				WHERE concept.id_concept = concept_replacedby.id_concept2
				AND concept.id_thesaurus = concept_replacedby.id_thesaurus
				AND concept_replacedby.id_concept2 = con.id_concept
				AND concept_replacedby.id_thesaurus = id_theso
		LOOP
			replacedBy = replacedBy || opentheso_get_uri(theso_rec.original_uri_is_ark, replacedBy_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replacedBy_rec.id_handle, theso_rec.original_uri_is_doi,
					replacedBy_rec.id_doi, replacedBy_rec.id_concept2, id_theso, path) || seperateur;
		END LOOP;

		facets = '';
		FOR facet_rec IN SELECT thesaurus_array.id_facet
						 FROM thesaurus_array
						 WHERE thesaurus_array.id_thesaurus = id_theso
						 AND thesaurus_array.id_concept_parent = con.id_concept
		LOOP
			facets = facets || facet_rec.id_facet || seperateur;
		END LOOP;

		externalResources = '';
		FOR externalResource_rec IN SELECT external_resources.external_uri
						 FROM external_resources
						 WHERE external_resources.id_thesaurus = id_theso
						 AND external_resources.id_concept = con.id_concept
		LOOP
			externalResources = externalResources || externalResource_rec.external_uri || seperateur;
		END LOOP;

		SELECT 	uri, con.status, local_URI, con.id_concept, con.id_ark, prefLab, altLab, altLab_hiden, definition, example,
				editorialNote, changeNote, secopeNote, note, historyNote, con.notation, narrower, broader, related, exactMatch, closeMatch,
				broadMatch, relatedMatch, narrowMatch, gpsData, membre, con.created, con.modified, img,
				creator, contributor, replaces, replacedBy, facets, externalResources INTO rec;

  		RETURN NEXT rec;
    END LOOP;
END;
$$;


CREATE OR REPLACE FUNCTION opentheso_get_concepts (id_theso VARCHAR, path VARCHAR)
	RETURNS SETOF RECORD
	LANGUAGE plpgsql
AS $$
DECLARE

	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';

	rec record;
	con record;
	theso_rec record;
	traduction_rec record;
	altLab_rec record;
	altLab_hiden_rec record;
	geo_rec record;
	group_rec record;
	note_concept_rec record;
	note_term_rec record;
	relation_rec record;
	alignement_rec record;
	img_rec record;
	vote_record record;
	message_record record;
	cadidat_record record;
	replace_rec record;
	replacedBy_rec record;
	facet_rec record;
    externalResource_rec record;

	tmp text;
	uri text;
	local_URI text;
	prefLab VARCHAR;
	altLab VARCHAR;
	altLab_hiden VARCHAR;
	membre text;
	definition text;
	secopeNote text;
	note text;
	historyNote text;
	example text;
	changeNote text;
	editorialNote text;
	narrower text;
	broader text;
	related text;
	exactMatch text;
	closeMatch text;
	broadMatch text;
	relatedMatch text;
	narrowMatch text;
	img text;
	creator text;
	contributor text;
	replaces text;
	replacedBy text;
	facets text;
    externalResources text;
    gpsData text;
BEGIN

	SELECT * INTO theso_rec FROM preferences where id_thesaurus = id_theso;


	FOR con IN SELECT * FROM concept WHERE id_thesaurus = id_theso AND status != 'CA'
    LOOP
		-- URI
		uri = opentheso_get_uri(theso_rec.original_uri_is_ark, con.id_ark, theso_rec.original_uri, theso_rec.original_uri_is_handle,
					 con.id_handle, theso_rec.original_uri_is_doi, con.id_doi, con.id_concept, id_theso, path);

		-- LocalUri
		local_URI = path || '/?idc=' || con.id_concept || '&idt=' || id_theso;
		
		-- PrefLab
                prefLab = '';
		FOR traduction_rec IN SELECT * FROM opentheso_get_traductions(id_theso, con.id_concept)
		LOOP
			prefLab = prefLab || traduction_rec.term_lexical_value || sous_seperateur || traduction_rec.term_lang || seperateur;
		END LOOP;

		-- altLab
		altLab = '';
		FOR altLab_rec IN SELECT * FROM opentheso_get_alter_term(id_theso, con.id_concept, false)
		LOOP
			altLab = altLab || altLab_rec.alter_term_lexical_value || sous_seperateur || altLab_rec.alter_term_lang || seperateur;
		END LOOP;

		-- altLab hiden
		altLab_hiden = '';
		FOR altLab_hiden_rec IN SELECT * FROM opentheso_get_alter_term(id_theso, con.id_concept, true)
		LOOP
			altLab_hiden = altLab_hiden || altLab_hiden_rec.alter_term_lexical_value || sous_seperateur || altLab_hiden_rec.alter_term_lang || seperateur;
		END LOOP;

		-- Notes
		note = '';
		example = '';
		changeNote = '';
		secopeNote = '';
		definition = '';
		historyNote = '';
		editorialNote = '';
		FOR note_concept_rec IN SELECT * FROM opentheso_get_note_concept(id_theso, con.id_concept)
		LOOP
			IF (note_concept_rec.note_notetypecode = 'note') THEN
				note = note || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'example') THEN
				example = example || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			END IF;
		END LOOP;

		FOR note_term_rec IN SELECT * FROM opentheso_get_note_term(id_theso, con.id_concept)
		LOOP
			IF (note_term_rec.note_notetypecode = 'note') THEN
				note = note || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'example') THEN
				example = example || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			END IF;
		END LOOP;

		-- Narrower
		narrower = '';
		broader = '';
		related = '';
		FOR relation_rec IN SELECT * FROM opentheso_get_relations(id_theso, con.id_concept)
		LOOP
			tmp = opentheso_get_uri(theso_rec.original_uri_is_ark, relation_rec.relationship_id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, relation_rec.relationship_id_handle, theso_rec.original_uri_is_doi,
					relation_rec.relationship_id_doi, relation_rec.relationship_id_concept, id_theso, path)
					|| sous_seperateur || relation_rec.relationship_role || sous_seperateur || relation_rec.relationship_id_concept || sous_seperateur ;

			IF (relation_rec.relationship_role = 'NT' OR relation_rec.relationship_role = 'NTP' OR relation_rec.relationship_role = 'NTI'
					OR relation_rec.relationship_role = 'NTG') THEN
				narrower = narrower || tmp || seperateur;
			ELSIF (relation_rec.relationship_role = 'BT' OR relation_rec.relationship_role = 'BTP' OR relation_rec.relationship_role = 'BTI'
					OR relation_rec.relationship_role = 'BTG') THEN
				broader = broader || tmp || seperateur;
			ELSIF (relation_rec.relationship_role = 'RT' OR relation_rec.relationship_role = 'RHP' OR relation_rec.relationship_role = 'RPO') THEN
				related = related || tmp || seperateur;
			END IF;
		END LOOP;

		-- Alignement
		exactMatch = '';
		closeMatch = '';
		broadMatch = '';
		relatedMatch = '';
		narrowMatch = '';
		FOR alignement_rec IN SELECT * FROM opentheso_get_alignements(id_theso, con.id_concept)
		LOOP
			if (alignement_rec.alig_id_type = 1) THEN
				exactMatch = exactMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 2) THEN
				closeMatch = closeMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 3) THEN
				broadMatch = broadMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 4) THEN
				relatedMatch = relatedMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 5) THEN
				narrowMatch = narrowMatch || alignement_rec.alig_uri_target || seperateur;
			END IF;
		END LOOP;

		-- geo:alt && geo:long
		gpsData = '';
        FOR geo_rec IN SELECT * FROM opentheso_get_gps(id_theso, con.id_concept)
        LOOP
            gpsData = gpsData || geo_rec.gps_latitude || sous_seperateur || geo_rec.gps_longitude || seperateur;
        END LOOP;

		-- membre
		membre = '';
		FOR group_rec IN SELECT * FROM opentheso_get_groups(id_theso, con.id_concept)
		LOOP
			IF (theso_rec.original_uri_is_ark = true AND group_rec.group_id_ark IS NOT NULL  AND group_rec.group_id_ark != '') THEN
				membre = membre || theso_rec.original_uri || '/' || group_rec.group_id_ark || seperateur;
			ELSIF (theso_rec.original_uri_is_ark = true AND (group_rec.group_id_ark IS NULL OR group_rec.group_id_ark = '')) THEN
				membre = membre || path || '/?idg=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
			ELSIF (group_rec.group_id_handle IS NOT NULL AND group_rec.group_id_handle != '') THEN
				membre = membre || 'https://hdl.handle.net/' || group_rec.group_id_handle || seperateur;
			ELSIF (theso_rec.original_uri IS NOT NULL AND theso_rec.original_uri != '') THEN
				membre = membre || theso_rec.original_uri || '/?idg=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
			ELSE
				membre = membre || path || '/?idc=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
			END IF;
		END LOOP;

		-- Images
		img = '';
		FOR img_rec IN SELECT * FROM opentheso_get_images(id_theso, con.id_concept)
		LOOP
			img = img || img_rec.name || sous_seperateur || img_rec.copyright || sous_seperateur || img_rec.url || seperateur;
		END LOOP;

		SELECT username INTO creator FROM users WHERE id_user = con.creator;
		SELECT username INTO contributor FROM users WHERE id_user = con.contributor;

		replaces = '';
		FOR replace_rec IN SELECT id_concept1, id_ark, id_handle, id_doi
				FROM concept_replacedby, concept
				WHERE concept.id_concept = concept_replacedby.id_concept1
				AND concept.id_thesaurus = concept_replacedby.id_thesaurus
				AND concept_replacedby.id_concept2 = con.id_concept
				AND concept_replacedby.id_thesaurus = id_theso
		LOOP
			replaces = replaces || opentheso_get_uri(theso_rec.original_uri_is_ark, replace_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replace_rec.id_handle, theso_rec.original_uri_is_doi,
					replace_rec.id_doi, replace_rec.id_concept1, id_theso, path) || seperateur;
		END LOOP;

		replacedBy = '';
		FOR replacedBy_rec IN SELECT id_concept2, id_ark, id_handle, id_doi
				FROM concept_replacedby, concept
				WHERE concept.id_concept = concept_replacedby.id_concept2
				AND concept.id_thesaurus = concept_replacedby.id_thesaurus
				AND concept_replacedby.id_concept1 = con.id_concept
				AND concept_replacedby.id_thesaurus = id_theso
		LOOP
			replacedBy = replacedBy || opentheso_get_uri(theso_rec.original_uri_is_ark, replacedBy_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replacedBy_rec.id_handle, theso_rec.original_uri_is_doi,
					replacedBy_rec.id_doi, replacedBy_rec.id_concept2, id_theso, path) || seperateur;
		END LOOP;

		facets = '';
		FOR facet_rec IN SELECT thesaurus_array.id_facet
						 FROM thesaurus_array
						 WHERE thesaurus_array.id_thesaurus = id_theso
						 AND thesaurus_array.id_concept_parent = con.id_concept
		LOOP
			facets = facets || facet_rec.id_facet || seperateur;
		END LOOP;

		externalResources = '';
		FOR externalResource_rec IN SELECT external_resources.external_uri
						 FROM external_resources
						 WHERE external_resources.id_thesaurus = id_theso
						 AND external_resources.id_concept = con.id_concept
		LOOP
                    externalResources = externalResources || externalResource_rec.external_uri || seperateur;
		END LOOP;

		SELECT 	uri, con.status, local_URI, con.id_concept, con.id_ark, prefLab, altLab, altLab_hiden, definition, example,
				editorialNote, changeNote, secopeNote, note, historyNote, con.notation, narrower, broader, related, exactMatch, closeMatch,
				broadMatch, relatedMatch, narrowMatch, gpsData, membre, con.created, con.modified,
				img, creator, contributor, replaces, replacedBy, facets, externalResources INTO rec;

  		RETURN NEXT rec;
    END LOOP;
END;
$$;

-- Procédures

CREATE OR REPLACE procedure opentheso_add_terms(
	id_term character varying,
	id_thesaurus character varying,
	id_concept character varying,
	id_user int,
	terms text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	term_rec record;
	array_string   text[];
BEGIN
	--label.getLabel() + SOUS_SEPERATEUR + label.getLanguage()
	FOR term_rec IN SELECT unnest(string_to_array(terms, seperateur)) AS term_value
    LOOP
		SELECT string_to_array(term_rec.term_value, sous_seperateur) INTO array_string;
            
      	Insert into term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, contributor) 
			values (id_term, array_string[1], array_string[2], id_thesaurus, CURRENT_DATE, CURRENT_DATE, '', '', id_user) ;
	END LOOP;
	
	-- Insert link term
	Insert into preferred_term (id_concept, id_term, id_thesaurus) values (id_concept, id_term, id_thesaurus) ;
END;
$BODY$;


CREATE OR REPLACE procedure opentheso_add_concept_dcterms(
	id_concept character varying,
	id_thesaurus character varying,
	dcterms text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	dcterms_rec record;
	array_string   text[];
BEGIN
	--label.getLabel() + SOUS_SEPERATEUR + label.getLanguage()
	FOR dcterms_rec IN SELECT unnest(string_to_array(dcterms, seperateur)) AS term_value
    LOOP
	SELECT string_to_array(dcterms_rec.term_value, sous_seperateur) INTO array_string;
            
      	Insert into concept_dcterms (id_concept, id_thesaurus, name, value, language) 
			values (id_concept, id_thesaurus, array_string[1], array_string[2], array_string[3]) ON CONFLICT DO NOTHING;
    END LOOP;
END;
$BODY$;




CREATE OR REPLACE procedure opentheso_add_hierarchical_relations(
	id_thesaurus character varying,
	relations text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	
	relations_rec record;
	array_string   text[];
BEGIN

	FOR relations_rec IN SELECT unnest(string_to_array(relations, seperateur)) AS relation_value
    LOOP
		SELECT string_to_array(relations_rec.relation_value, sous_seperateur) INTO array_string;
		
		INSERT INTO hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) 
			VALUES (array_string[1], id_thesaurus, array_string[2], array_string[3]) ON CONFLICT DO NOTHING; 
	END LOOP;
END;
$BODY$;


CREATE OR REPLACE procedure opentheso_add_custom_relations(
	id_thesaurus character varying,
	relations text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	
	relations_rec record;
	array_string   text[];
BEGIN

	FOR relations_rec IN SELECT unnest(string_to_array(relations, seperateur)) AS relation_value
    LOOP
		SELECT string_to_array(relations_rec.relation_value, sous_seperateur) INTO array_string;
		
		INSERT INTO hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) 
			VALUES (array_string[1], id_thesaurus, array_string[2], array_string[3]) ON CONFLICT DO NOTHING; 
	END LOOP;
END;
$BODY$;


CREATE OR REPLACE procedure opentheso_add_notes(
	id_concept character varying,
	id_thesaurus character varying,
	id_user int,
	notes text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	
	notes_rec record;
	array_string   text[];
BEGIN

	FOR notes_rec IN SELECT unnest(string_to_array(notes, seperateur)) AS note_value
    LOOP
		SELECT string_to_array(notes_rec.note_value, sous_seperateur) INTO array_string;
		
		if (array_string[2] = 'customnote' OR array_string[2] = 'scopeNote' OR array_string[2] = 'note')  THEN
			insert into note (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue, id_user) 
				values (array_string[2], id_thesaurus, id_concept, array_string[3], array_string[1], id_user);
		END IF;
		
		if (array_string[2] = 'definition' OR array_string[2] = 'historyNote' OR array_string[2] = 'editorialNote'
		   		OR array_string[2] = 'changeNote' OR array_string[2] = 'example')  THEN
			Insert into note (notetypecode, id_thesaurus, id_term, lang, lexicalvalue, id_user) 
				values (array_string[2], id_thesaurus, array_string[4], array_string[3], array_string[1], id_user);
		END IF;	
		
		if (id_user != '-1') THEN
			Insert into note_historique (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue, action_performed, id_user)
				values (array_string[2], id_thesaurus, id_concept, array_string[3], array_string[1], 'add', id_user);
		END IF;
	END LOOP;
END;
$BODY$;



CREATE OR REPLACE procedure opentheso_add_non_preferred_term(id_thesaurus character varying,
			id_user int, 
			non_pref_terms text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	
	non_pref_rec record;
	array_string   text[];
BEGIN

	FOR non_pref_rec IN SELECT unnest(string_to_array(non_pref_terms, seperateur)) AS non_pref_value
    LOOP
		SELECT string_to_array(non_pref_rec.non_pref_value, sous_seperateur) INTO array_string;
		-- 'id_term@lexical_value@lang@id_thesaurus@source@status@hiden'
		Insert into non_preferred_term (id_term, lexical_value, lang, id_thesaurus, source, status, hiden)
			values (array_string[1], array_string[2], array_string[3], array_string[4], array_string[5], array_string[6], CAST(array_string[7] AS BOOLEAN)) ON CONFLICT DO NOTHING;
			
		Insert into non_preferred_term_historique (id_term, lexical_value, lang, id_thesaurus, source, status, id_user, action)
			values (array_string[1], array_string[2], array_string[3], id_thesaurus, array_string[4], array_string[5], id_user, 'ADD') ON CONFLICT DO NOTHING;	
	END LOOP;
END;
$BODY$;



CREATE OR REPLACE procedure opentheso_add_external_images(
	id_thesaurus character varying,
	id_concept character varying,
	id_user int,
	images text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    seperateur constant varchar := '##';
    sous_seperateur constant varchar := '@@';
    images_rec record;
    array_string text[];
BEGIN

    FOR images_rec IN SELECT unnest(string_to_array(images, seperateur)) AS image_value
        LOOP
            SELECT string_to_array(images_rec.image_value, sous_seperateur) INTO array_string;
            Insert into external_images (id_concept, id_thesaurus, id_user, image_name, image_copyright, external_uri) 
            values (id_concept, id_thesaurus, id_user, array_string[1], array_string[2], array_string[3]);	
        END LOOP;
END;
$BODY$;


CREATE OR REPLACE procedure opentheso_add_gps(
    id_concept character varying,
    id_thesaurus character varying,
    gpsList text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    seperateur constant varchar := '##';
    sous_seperateur constant varchar := '@@';

    gps_rec record;
    array_string   text[];
BEGIN
    FOR gps_rec IN SELECT unnest(string_to_array(gpsList, seperateur)) AS gps_value
        LOOP
            SELECT string_to_array(gps_rec.gps_value, sous_seperateur) INTO array_string;
            IF array_string[1] IS NOT NULL THEN
                insert into gps(id_concept, id_theso, latitude, longitude)
                values (id_concept, id_thesaurus, CAST (array_string[1] AS double precision), CAST (array_string[2] AS double precision));
            END IF;
        END LOOP;
END;
$BODY$;




CREATE OR REPLACE procedure opentheso_add_alignements(alignements text) 
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	
	alignements_rec record;
	array_string text[];
BEGIN

    FOR alignements_rec IN SELECT unnest(string_to_array(alignements, seperateur)) AS alignement_value
        LOOP
		SELECT string_to_array(alignements_rec.alignement_value, sous_seperateur) INTO array_string;
		Insert into alignement (author, concept_target, thesaurus_target, uri_target, alignement_id_type, internal_id_thesaurus, internal_id_concept) 
			values (CAST(array_string[1] AS int), array_string[2], array_string[3], array_string[4], CAST(array_string[5] AS int), array_string[6], array_string[7]) ON CONFLICT DO NOTHING;
	END LOOP;
END;
$BODY$;



CREATE OR REPLACE procedure opentheso_add_new_concept(
    id_thesaurus character varying,
    id_con character varying,
    id_user int,
    conceptStatus character varying,
    conceptType text,
    notationConcept character varying,
    id_ark character varying,
    isTopConcept Boolean,
    id_handle character varying,
    id_doi character varying,
    prefterms text,
    relation_hiarchique text,
    custom_relation text,
    notes text,
    non_pref_terms text,
    alignements text,
    images text,
    idsConceptsReplaceBy text,
    isGpsPresent Boolean,
    gps text,
    created Date,
    modified Date,
    concept_dcterms text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    id_new_concet character varying;
    seperateur constant varchar := '##';
    concept_Rep_rec record;
BEGIN

    Insert into concept (id_concept, id_thesaurus, id_ark, created, modified, status, concept_type, notation, top_concept, id_handle, id_doi, creator, contributor, gps)
    values (id_con, id_thesaurus, id_ark, created, modified, conceptStatus, conceptType, notationConcept, isTopConcept, id_handle, id_doi, id_user, id_user, isGpsPresent) ;

    SELECT concept.id_concept INTO id_new_concet FROM concept WHERE concept.id_concept = id_con;

    IF (id_new_concet IS NOT NULL) THEN

        IF (prefterms IS NOT NULL AND prefterms != 'null') THEN
            -- 'lexical_value@lang@source@status@createed@modified'
            CALL opentheso_add_terms(id_new_concet, id_thesaurus, id_new_concet, id_user, prefterms);
        END IF;

        IF (relation_hiarchique IS NOT NULL AND relation_hiarchique != 'null') THEN
            -- 'id_concept1@role@id_concept2'
            CALL opentheso_add_hierarchical_relations(id_thesaurus, relation_hiarchique);
        END IF;

        IF (custom_relation IS NOT NULL AND custom_relation != 'null') THEN
            -- 'id_concept1@role@id_concept2'
            CALL opentheso_add_custom_relations(id_thesaurus, custom_relation);
        END IF;

        IF (concept_dcterms IS NOT NULL AND concept_dcterms != 'null') THEN
            -- 'creator@@miled@@fr##contributor@@zozo@@fr'
            CALL opentheso_add_concept_dcterms(id_new_concet, id_thesaurus, concept_dcterms);
        END IF;

        IF (notes IS NOT NULL AND notes != 'null') THEN
            -- 'value@typeCode@lang@id_term'
            CALL opentheso_add_notes(id_new_concet, id_thesaurus, id_user, notes);
        END IF;

        IF (non_pref_terms IS NOT NULL AND non_pref_terms != 'null') THEN
            -- 'id_term@lexical_value@lang@id_thesaurus@source@status@hiden'
            CALL opentheso_add_non_preferred_term(id_thesaurus, id_user, non_pref_terms);
        END IF;

        IF (images IS NOT NULL AND images != 'null') THEN
            -- 'url1##url2'
            CALL opentheso_add_external_images(id_thesaurus, id_new_concet, id_user, images);
        END IF;

        IF (alignements IS NOT NULL AND alignements != 'null') THEN
            -- 'author@concept_target@thesaurus_target@uri_target@alignement_id_type@internal_id_thesaurus@internal_id_concept'
            CALL opentheso_add_alignements(alignements);
        END IF;

        IF (idsConceptsReplaceBy IS NOT NULL AND idsConceptsReplaceBy != 'null') THEN
            FOR concept_Rep_rec IN SELECT unnest(string_to_array(idsConceptsReplaceBy, seperateur)) AS idConceptReplaceBy
                                                  LOOP
                                                  Insert into concept_replacedby (id_concept1, id_concept2, id_thesaurus, id_user)
                                   values(id_new_concet, concept_Rep_rec.idConceptReplaceBy, id_thesaurus, id_user);
            END LOOP;
        END IF;

        IF (gps IS NOT NULL AND gps != 'null') THEN
            CALL opentheso_add_gps(id_new_concet, id_thesaurus, gps);
        END IF;
    END IF;
END;
$BODY$;
