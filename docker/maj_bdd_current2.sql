

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


-- auto_suppression de nettoyage
SELECT delete_fonction ('delete_fonction','TEXT','TEXT');
select delete_fonction1('delete_fonction','TEXT','TEXT');
SELECT delete_fonction1 ('delete_fonction1','TEXT','TEXT');



INSERT INTO users (id_user, username, password, active, mail, passtomodify,alertmail, issuperadmin) VALUES
(6, 'firas', '21232f297a57a5a743894a0e4a801fc3', true, 'firas@domaine.fr', false, false, true);
