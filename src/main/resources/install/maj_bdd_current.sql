

--  !!!!!!! Attention !!!!!!!!!
--
-- à appliquer uniquement pour le passage des anciennes versions vers la 20.07
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



-- requête pour detecter les relations en boucle qui sont interdites
-- select * from hierarchical_relationship where id_concept1 = id_concept2 and id_thesaurus = 'TH_1' and role = 'BT';
-- select * from hierarchical_relationship where id_concept1 = id_concept2 and id_thesaurus = 'TH_1' and role = 'NT';
-- select * from hierarchical_relationship where id_concept1 = id_concept2 and id_thesaurus = 'TH_1' and role = 'RT';




-- permet de retrouver les concepts qui n'ont pas de realtion associative RT
--
-- select id_concept from concept 
-- where 
-- concept.id_thesaurus = 'th1'
-- and 
-- concept.id_concept NOT IN (
--	select id_concept1 from hierarchical_relationship 
-- 	where 
--	hierarchical_relationship.id_thesaurus = 'th1'
-- 	and
--	hierarchical_relationship.role = 'RT')



-- requête pour récupérer les candidat d'un thésaurus
--select term_candidat.lexical_value, concept_candidat.status, concept_candidat.id_thesaurus
--from concept_candidat, concept_term_candidat, term_candidat
--where
--concept_candidat.id_concept = concept_term_candidat.id_concept
--and
--concept_candidat.id_thesaurus = concept_term_candidat.id_thesaurus
--and
--concept_term_candidat.id_term = term_candidat.id_term
--and
--concept_term_candidat.id_thesaurus = term_candidat.id_thesaurus
--and
--term_candidat.id_thesaurus = '166' 







-- fonction pour détecter les concepts qui sont à la fois TopConcept et spécifique (ce qui est interdit)
-- select id_concept from concept, hierarchical_relationship 
-- where 
-- concept.id_concept = hierarchical_relationship.id_concept1
-- and
-- concept.id_thesaurus = hierarchical_relationship.id_thesaurus
-- and hierarchical_relationship.role = 'BT'
-- and concept.top_concept = true
-- and concept.id_thesaurus = 'TH_1'



-- !!! Important !!!
-- pour créer ces extensions, il faut avoir des privilèves SuperAdmin sur Postgres
--
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE OR REPLACE FUNCTION f_unaccent(text)
  RETURNS text AS
$func$
SELECT public.unaccent('public.unaccent', $1)
$func$  LANGUAGE sql IMMUTABLE;
--
--- fin des fonctions à appliquer en premier et à part







DROP TABLE public.note_type;
CREATE TABLE public.note_type
(
  code text NOT NULL,
  isterm boolean NOT NULL,
  isconcept boolean NOT NULL,
  label_fr character varying,
  label_en character varying,
  CONSTRAINT pk_note_type PRIMARY KEY (code),
  CONSTRAINT chk_not_false_values CHECK (NOT (isterm = false AND isconcept = false))
)
WITH (
  OIDS=FALSE
);

INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('note', false, true, 'Note', 'Note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('historyNote', true, true, 'Note historique', 'History note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('scopeNote', false, true, 'Note de portée', 'Scope note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('example', true, false, 'Exemple', 'Example');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('editorialNote', true, false, 'Note éditoriale', 'Editorial note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('definition', true, false, 'Définition', 'Definition');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('changeNote', true, false, 'Note de changement', 'Change note');


--alter table alignement drop constraint alignement_internal_id_concept_internal_id_thesaurus_id_ali_key;
--alter table alignement drop constraint alignement_internal_id_concept_internal_id_thesaurus_id_alignem;
--alter TABLE ONLY alignement add CONSTRAINT alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key
-- UNIQUE (internal_id_concept, internal_id_thesaurus, uri_target);

--select internal_id_concept, internal_id_thesaurus, uri_target, count(*) from alignement 
-- group by internal_id_concept, internal_id_thesaurus, uri_target having count(*)>1


-- pour récupérer les concepts qui ont plusieurs domaines 
-- select idconcept from concept_group_concept where idthesaurus = '1' 
-- group by idconcept having count(idconcept) >1;

-- pour trouver les branches polyhiérarchiques 
-- select id_concept1 from hierarchical_relationship where role = 'BT' 
--  group by id_concept1 having count(id_concept1) > 2;

-- 
-- ajout des nouvelles fonctions
--

-- avertissement, il faut faire attention, si le script ne passe pas,
-- c'est peut être que les contraintes que nous avons ajouté montrent des doublons interdits
-- il faut alors les identifier et les supprimer 
-- voici une requête d'exemple pour repérer les doublons 

--
-- 
-- select notetypecode, id_thesaurus, id_term,lexicalvalue, lang, count(*) from note 
-- where id_term !='' group by notetypecode, lexicalvalue, id_thesaurus, id_term, lang having count(*)>1;

-- select notetypecode, id_thesaurus, id_concept,lexicalvalue, lang, count(*) from note 
-- where id_concept !='' group by notetypecode, lexicalvalue, id_thesaurus, id_concept, lang having count(*)>1;


-- pour supprimer les relations vers des concepts qui n'existent pas (cas de thésaurus conçus à la main et mal controlés 
-- SELECT DISTINCT 
--  hierarchical_relationship.id_concept2
--FROM 
--  public.hierarchical_relationship, 
--  public.concept
--WHERE 
--  hierarchical_relationship.id_concept2 NOT IN (select concept.id_concept from concept where id_thesaurus = '167')
--  and hierarchical_relationship.id_thesaurus = '167';


-- pour remettre tous les concepts dans un group en particulier 
-- insert into concept_group_concept (idgroup,idthesaurus,idconcept) 
-- select 'G2854', id_thesaurus, id_concept from concept where id_thesaurus = '43' 





-- nouvelles tables pour le nouveau module de candidats 
-- Table: candidat_messages
CREATE TABLE IF NOT EXISTS public.candidat_messages
(
    id_message SERIAL PRIMARY KEY,
    value text NOT NULL,
    id_user integer,
    id_concept character varying,
    id_thesaurus character varying,
    date text
)
WITH (
  OIDS=FALSE
);


-- Table: candidat_status
CREATE TABLE IF NOT EXISTS public.candidat_status
(
    id_concept character varying NOT NULL,
    id_status integer,
    date date NOT NULL DEFAULT now(),
    id_user integer,
    id_thesaurus character varying,
    message text,
    id_user_admin integer,
    CONSTRAINT candidat_status_id_concept_id_thesaurus_key UNIQUE (id_concept, id_thesaurus)
)
WITH (
  OIDS=FALSE
);


-- Table: candidat_vote
CREATE TABLE IF NOT EXISTS public.candidat_vote
(
    id_vote SERIAL PRIMARY KEY,
    id_user integer,
    id_concept character varying,
    id_thesaurus character varying,
    type_vote character varying(30),
    id_note character varying(30)
)
WITH (
  OIDS=FALSE
);

-- Table: status
DROP TABLE if exists public.status;
CREATE TABLE IF NOT EXISTS status
(
    id_status SERIAL PRIMARY KEY,
    value text
)
WITH (
  OIDS=FALSE
);
INSERT INTO status (id_status, value) VALUES (1, 'En attente'), (2, 'Inséré'), (3, 'Rejeté');














--- rechargement et optimisation des langues iso_latin1
DROP TABLE languages_iso639;

CREATE TABLE languages_iso639
(
  iso639_1 character(3),
  iso639_2 character varying,
  english_name character varying,
  french_name character varying,
  id integer NOT NULL DEFAULT nextval('languages_id_seq'::regclass),
  CONSTRAINT languages_iso639_pkey PRIMARY KEY (id),
  CONSTRAINT languages_iso639_iso639_1_key UNIQUE (iso639_1)
)
WITH (
  OIDS=FALSE
);




INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('aa ', 'aar', 'Afar', 'afar', 2);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ab ', 'abk', 'Abkhazian', 'abkhaze', 3);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('af ', 'afr', 'Afrikaans', 'afrikaans', 4);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ak ', 'aka', 'Akan', 'akan', 5);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sq ', 'alb (B)
sqi (T)', 'Albanian', 'albanais', 6);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('am ', 'amh', 'Amharic', 'amharique', 7);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ar ', 'ara', 'Arabic', 'arabe', 8);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('an ', 'arg', 'Aragonese', 'aragonais', 9);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('as ', 'asm', 'Assamese', 'assamais', 10);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('av ', 'ava', 'Avaric', 'avar', 11);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ae ', 'ave', 'Avestan', 'avestique', 12);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ay ', 'aym', 'Aymara', 'aymara', 13);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('az ', 'aze', 'Azerbaijani', 'azéri', 14);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ba ', 'bak', 'Bashkir', 'bachkir', 15);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bm ', 'bam', 'Bambara', 'bambara', 16);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('eu ', 'baq (B)
eus (T)', 'Basque', 'basque', 17);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('be ', 'bel', 'Belarusian', 'biélorusse', 18);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bn ', 'ben', 'Bengali', 'bengali', 19);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bh ', 'bih', 'Bihari languages', 'langues biharis', 20);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bi ', 'bis', 'Bislama', 'bichlamar', 21);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bs ', 'bos', 'Bosnian', 'bosniaque', 22);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('br ', 'bre', 'Breton', 'breton', 23);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bg ', 'bul', 'Bulgarian', 'bulgare', 24);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ca ', 'cat', 'Catalan; Valencian', 'catalan; valencien', 25);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ch ', 'cha', 'Chamorro', 'chamorro', 26);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ce ', 'che', 'Chechen', 'tchétchène', 27);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('zh ', 'chi (B)
zho (T)', 'Chinese', 'chinois', 28);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cv ', 'chv', 'Chuvash', 'tchouvache', 29);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kw ', 'cor', 'Cornish', 'cornique', 30);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('co ', 'cos', 'Corsican', 'corse', 31);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cr ', 'cre', 'Cree', 'cree', 32);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cy ', 'wel (B)
cym (T)', 'Welsh', 'gallois', 33);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cs ', 'cze (B)
ces (T)', 'Czech', 'tchèque', 34);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('da ', 'dan', 'Danish', 'danois', 35);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('de ', 'ger (B)
deu (T)', 'German', 'allemand', 36);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('dv ', 'div', 'Divehi; Dhivehi; Maldivian', 'maldivien', 37);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('dz ', 'dzo', 'Dzongkha', 'dzongkha', 38);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('el ', 'gre (B)
ell (T)', 'Greek, Modern (1453-)', 'grec moderne (après 1453)', 39);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('en ', 'eng', 'English', 'anglais', 40);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('eo ', 'epo', 'Esperanto', 'espéranto', 41);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('et ', 'est', 'Estonian', 'estonien', 42);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ee ', 'ewe', 'Ewe', 'éwé', 43);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fo ', 'fao', 'Faroese', 'féroïen', 44);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fj ', 'fij', 'Fijian', 'fidjien', 45);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fi ', 'fin', 'Finnish', 'finnois', 46);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fr ', 'fre (B)
fra (T)', 'French', 'français', 47);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fy ', 'fry', 'Western Frisian', 'frison occidental', 48);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ff ', 'ful', 'Fulah', 'peul', 49);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ka ', 'geo (B)
kat (T)', 'Georgian', 'géorgien', 50);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gd ', 'gla', 'Gaelic; Scottish Gaelic', 'gaélique; gaélique écossais', 51);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ga ', 'gle', 'Irish', 'irlandais', 52);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gl ', 'glg', 'Galician', 'galicien', 53);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gv ', 'glv', 'Manx', 'manx; mannois', 54);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gn ', 'grn', 'Guarani', 'guarani', 55);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gu ', 'guj', 'Gujarati', 'goudjrati', 56);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ht ', 'hat', 'Haitian; Haitian Creole', 'haïtien; créole haïtien', 57);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ha ', 'hau', 'Hausa', 'haoussa', 58);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('he ', 'heb', 'Hebrew', 'hébreu', 59);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hz ', 'her', 'Herero', 'herero', 60);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hi ', 'hin', 'Hindi', 'hindi', 61);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ho ', 'hmo', 'Hiri Motu', 'hiri motu', 62);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hr ', 'hrv', 'Croatian', 'croate', 63);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hu ', 'hun', 'Hungarian', 'hongrois', 64);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hy ', 'arm (B)
hye (T)', 'Armenian', 'arménien', 65);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ig ', 'ibo', 'Igbo', 'igbo', 66);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('is ', 'ice (B)
isl (T)', 'Icelandic', 'islandais', 67);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('io ', 'ido', 'Ido', 'ido', 68);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ii ', 'iii', 'Sichuan Yi; Nuosu', 'yi de Sichuan', 69);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('iu ', 'iku', 'Inuktitut', 'inuktitut', 70);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ie ', 'ile', 'Interlingue; Occidental', 'interlingue', 71);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('id ', 'ind', 'Indonesian', 'indonésien', 72);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ik ', 'ipk', 'Inupiaq', 'inupiaq', 73);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('it ', 'ita', 'Italian', 'italien', 74);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('jv ', 'jav', 'Javanese', 'javanais', 75);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ja ', 'jpn', 'Japanese', 'japonais', 76);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kl ', 'kal', 'Kalaallisut; Greenlandic', 'groenlandais', 77);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kn ', 'kan', 'Kannada', 'kannada', 78);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ks ', 'kas', 'Kashmiri', 'kashmiri', 79);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kr ', 'kau', 'Kanuri', 'kanouri', 80);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kk ', 'kaz', 'Kazakh', 'kazakh', 81);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('km ', 'khm', 'Central Khmer', 'khmer central', 82);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ki ', 'kik', 'Kikuyu; Gikuyu', 'kikuyu', 83);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('rw ', 'kin', 'Kinyarwanda', 'rwanda', 84);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ky ', 'kir', 'Kirghiz; Kyrgyz', 'kirghiz', 85);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kv ', 'kom', 'Komi', 'kom', 86);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kg ', 'kon', 'Kongo', 'kongo', 87);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ko ', 'kor', 'Korean', 'coréen', 88);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kj ', 'kua', 'Kuanyama; Kwanyama', 'kuanyama; kwanyama', 89);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ku ', 'kur', 'Kurdish', 'kurde', 90);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lo ', 'lao', 'Lao', 'lao', 91);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('la ', 'lat', 'Latin', 'latin', 92);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lv ', 'lav', 'Latvian', 'letton', 93);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('li ', 'lim', 'Limburgan; Limburger; Limburgish', 'limbourgeois', 94);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ln ', 'lin', 'Lingala', 'lingala', 95);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lt ', 'lit', 'Lithuanian', 'lituanien', 96);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lb ', 'ltz', 'Luxembourgish; Letzeburgesch', 'luxembourgeois', 97);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lu ', 'lub', 'Luba-Katanga', 'luba-katanga', 98);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lg ', 'lug', 'Ganda', 'ganda', 99);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mk ', 'mac (B)
mkd (T)', 'Macedonian', 'macédonien', 100);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mh ', 'mah', 'Marshallese', 'marshall', 101);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ml ', 'mal', 'Malayalam', 'malayalam', 102);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mr ', 'mar', 'Marathi', 'marathe', 103);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ms ', 'may (B)
msa (T)', 'Malay', 'malais', 104);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mg ', 'mlg', 'Malagasy', 'malgache', 105);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mt ', 'mlt', 'Maltese', 'maltais', 106);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mn ', 'mon', 'Mongolian', 'mongol', 107);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mi ', 'mao (B)
mri (T)', 'Maori', 'maori', 108);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('my ', 'bur (B)
mya (T)', 'Burmese', 'birman', 109);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('na ', 'nau', 'Nauru', 'nauruan', 110);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nv ', 'nav', 'Navajo; Navaho', 'navaho', 111);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nr ', 'nbl', 'Ndebele, South; South Ndebele', 'ndébélé du Sud', 112);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nd ', 'nde', 'Ndebele, North; North Ndebele', 'ndébélé du Nord', 113);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ng ', 'ndo', 'Ndonga', 'ndonga', 114);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ne ', 'nep', 'Nepali', 'népalais', 115);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nl ', 'dut (B)
nld (T)', 'Dutch; Flemish', 'néerlandais; flamand', 116);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nb ', 'nob', 'Bokmål, Norwegian; Norwegian Bokmål', 'norvégien bokmål', 117);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('no ', 'nor', 'Norwegian', 'norvégien', 118);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ny ', 'nya', 'Chichewa; Chewa; Nyanja', 'chichewa; chewa; nyanja', 119);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('oc ', 'oci', 'Occitan (post 1500)', 'occitan (après 1500)', 120);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('oj ', 'oji', 'Ojibwa', 'ojibwa', 121);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('or ', 'ori', 'Oriya', 'oriya', 122);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('om ', 'orm', 'Oromo', 'galla', 123);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('os ', 'oss', 'Ossetian; Ossetic', 'ossète', 124);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pa ', 'pan', 'Panjabi; Punjabi', 'pendjabi', 125);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fa ', 'per (B)
fas (T)', 'Persian', 'persan', 126);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pi ', 'pli', 'Pali', 'pali', 127);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pl ', 'pol', 'Polish', 'polonais', 128);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pt ', 'por', 'Portuguese', 'portugais', 129);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ps ', 'pus', 'Pushto; Pashto', 'pachto', 130);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('qu ', 'que', 'Quechua', 'quechua', 131);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('rm ', 'roh', 'Romansh', 'romanche', 132);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ro ', 'rum (B)
ron (T)', 'Romanian; Moldavian; Moldovan', 'roumain; moldave', 133);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('rn ', 'run', 'Rundi', 'rundi', 134);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ru ', 'rus', 'Russian', 'russe', 135);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sg ', 'sag', 'Sango', 'sango', 136);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sa ', 'san', 'Sanskrit', 'sanskrit', 137);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('si ', 'sin', 'Sinhala; Sinhalese', 'singhalais', 138);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sk ', 'slo (B)
slk (T)', 'Slovak', 'slovaque', 139);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sl ', 'slv', 'Slovenian', 'slovène', 140);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('se ', 'sme', 'Northern Sami', 'sami du Nord', 141);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sm ', 'smo', 'Samoan', 'samoan', 142);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sn ', 'sna', 'Shona', 'shona', 143);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sd ', 'snd', 'Sindhi', 'sindhi', 144);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('so ', 'som', 'Somali', 'somali', 145);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('st ', 'sot', 'Sotho, Southern', 'sotho du Sud', 146);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('es ', 'spa', 'Spanish; Castilian', 'espagnol; castillan', 147);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sc ', 'srd', 'Sardinian', 'sarde', 148);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sr ', 'srp', 'Serbian', 'serbe', 149);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ss ', 'ssw', 'Swati', 'swati', 150);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('su ', 'sun', 'Sundanese', 'soundanais', 151);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sw ', 'swa', 'Swahili', 'swahili', 152);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sv ', 'swe', 'Swedish', 'suédois', 153);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ty ', 'tah', 'Tahitian', 'tahitien', 154);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ta ', 'tam', 'Tamil', 'tamoul', 155);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tt ', 'tat', 'Tatar', 'tatar', 156);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('te ', 'tel', 'Telugu', 'télougou', 157);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tg ', 'tgk', 'Tajik', 'tadjik', 158);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tl ', 'tgl', 'Tagalog', 'tagalog', 159);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('th ', 'tha', 'Thai', 'thaï', 160);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bo ', 'tib (B)
bod (T)', 'Tibetan', 'tibétain', 161);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ti ', 'tir', 'Tigrinya', 'tigrigna', 162);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('to ', 'ton', 'Tonga (Tonga Islands)', 'tongan (Îles Tonga)', 163);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tn ', 'tsn', 'Tswana', 'tswana', 164);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ts ', 'tso', 'Tsonga', 'tsonga', 165);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tk ', 'tuk', 'Turkmen', 'turkmène', 166);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tr ', 'tur', 'Turkish', 'turc', 167);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tw ', 'twi', 'Twi', 'twi', 168);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ug ', 'uig', 'Uighur; Uyghur', 'ouïgour', 169);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('uk ', 'ukr', 'Ukrainian', 'ukrainien', 170);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ur ', 'urd', 'Urdu', 'ourdou', 171);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('uz ', 'uzb', 'Uzbek', 'ouszbek', 172);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ve ', 'ven', 'Venda', 'venda', 173);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('vi ', 'vie', 'Vietnamese', 'vietnamien', 174);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('vo ', 'vol', 'Volapük', 'volapük', 175);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('wa ', 'wln', 'Walloon', 'wallon', 176);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('wo ', 'wol', 'Wolof', 'wolof', 177);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('xh ', 'xho', 'Xhosa', 'xhosa', 178);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('yi ', 'yid', 'Yiddish', 'yiddish', 179);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('yo ', 'yor', 'Yoruba', 'yoruba', 180);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('za ', 'zha', 'Zhuang; Chuang', 'zhuang; chuang', 181);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('zu ', 'zul', 'Zulu', 'zoulou', 182);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cu ', 'chu', 'Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic', 'vieux slave; vieux bulgare', 183);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ia ', 'ina', 'Interlingua (International Auxiliary Language Association)', 'interlingua', 184);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nn ', 'nno', 'Norwegian Nynorsk; Nynorsk, Norwegian', 'norvégien nynorsk', 185);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gr ', 'grc', 'Greek, Ancient (to 1453)', 'grec ancien (jusqu''à 1453)', 186);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('iso', 'iso', 'norme ISO 233-2 (1993)', 'norme ISO 233-2 (1993)', 187);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ala', 'ala', 'ALA-LC Romanization Table (American Library Association-Library of Congress)', 'ALA-LC)', 188);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mul', 'mul', 'multiple langages', 'multiple langages', 189);

SELECT pg_catalog.setval('languages_id_seq', 189, false);



--
--permet de changer tous les identifiants à id
--
CREATE OR REPLACE FUNCTION updateID_table() RETURNS VOID AS $$
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'id_alignement' AND TABLE_NAME = 'alignement') THEN
	Execute
		'ALTER TABLE alignement RENAME COLUMN id_alignement TO id;
		ALTER TABLE concept_group_label RENAME COLUMN idgrouplabel TO id;
		ALTER TABLE concept_group_label_historique RENAME COLUMN idgrouplabel TO id;
		ALTER TABLE note RENAME COLUMN id_note TO id;
		ALTER TABLE note_historique RENAME COLUMN id_note TO id;
		ALTER TABLE preferences RENAME COLUMN id_pref TO id;
		ALTER TABLE thesaurus_array RENAME COLUMN identifier TO facet_id;
		ALTER TABLE node_label RENAME COLUMN id TO facet_id;
		';
    END IF;
END;
$$ LANGUAGE plpgsql; 

--
--
--

CREATE OR REPLACE FUNCTION updateIDusers() RETURNS VOID AS $$
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'id' AND TABLE_NAME = 'users') THEN
	Execute
	'ALTER TABLE users RENAME COLUMN id TO id_user';
	end if;
end;
$$ LANGUAGE plpgsql;

--
--Permet de créer la table gps
--

-- DROP FUNCTION public.table_gps();

CREATE OR REPLACE FUNCTION public.table_gps() RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'gps') THEN

        execute 
		'CREATE TABLE gps (
		id_concept character varying,
		id_theso character varying,
		latitude float,
		longitude float,
		CONSTRAINT gps_pkey PRIMARY KEY (id_concept, id_theso)
		);'
	;

    END IF;
END;

$$LANGUAGE plpgsql VOLATILE;


--
--Permet de creé la fonction si elle n'existe pas (alignement_format)
--  
CREATE OR REPLACE FUNCTION addtype_Alignement_format() RETURNS VOID AS $$
BEGIN
    IF not exists(SELECT e.enumlabel from pg_type t, pg_enum e
		 where t.oid = e.enumtypid and t.typname = 'alignement_format') then 
		 execute 				
			'CREATE TYPE public.alignement_format AS ENUM
			   (''skos'',
			    ''json'',
			    ''xml'');';
			  END IF;

END;
$$ LANGUAGE plpgsql; 

--
--Permet de creé la fonction si elle n'existe pas (alignement_type_rqt)
--

  CREATE OR REPLACE FUNCTION addtype_Alignement_type_rqt() RETURNS VOID AS $$
BEGIN
    IF not exists(SELECT e.enumlabel from pg_type t, pg_enum e
		 where t.oid = e.enumtypid and t.typname = 'alignement_type_rqt') then 
		 execute 				
			'CREATE TYPE public.alignement_type_rqt AS ENUM
			   (''SPARQL'',
			    ''REST'');';
			  END IF;

END;
$$ LANGUAGE plpgsql;

--  
--Permet de creé la fonction si elle n'existe pas (auth_method)
--

CREATE OR REPLACE FUNCTION addtype_auth_method() RETURNS VOID AS $$
BEGIN
    IF not exists(SELECT e.enumlabel from pg_type t, pg_enum e
		 where t.oid = e.enumtypid and t.typname = 'auth_method') then 
		 execute 				
			'CREATE TYPE public.auth_method AS ENUM
			   (''DB'',
			    ''LDAP'',
			    ''FILE'',
			    ''test'');';
			  END IF;

END;
$$ LANGUAGE plpgsql;


--
--Permet de creé la fonction si elle n'existe pas (relations_nt)
--  

CREATE OR REPLACE FUNCTION addtype_relations_nt() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'nt_type') THEN
		 execute 				
			'CREATE TABLE public.nt_type (
                                id integer NOT NULL,
                                relation character varying,
                                description_fr character varying,
                                description_en character varying
                            );
                        ALTER TABLE ONLY public.nt_type ADD CONSTRAINT nt_type_pkey PRIMARY KEY (id);
                        ALTER TABLE ONLY public.nt_type ADD CONSTRAINT nt_type_relation_key UNIQUE (relation);
                        INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (1, ''NT'', ''Term spécifique'', ''Narrower term'');
                        INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (2, ''NTG'', ''Term spécifique (generic)'', ''Narrower term (generic)'');
                        INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (3, ''NTP'', ''Term spécifique (partitive)'', ''Narrower term (partitive)'');
                        INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (4, ''NTI'', ''Term spécifique (instantial)'', ''Narrower term (instantial)'');';
    END IF;

END;
$$ LANGUAGE plpgsql; 

--
--Permet de creé la fonction si elle n'existe pas (relations_bt)
--  
CREATE OR REPLACE FUNCTION addtype_relations_bt() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'bt_type') THEN
		 execute 				
			'CREATE TABLE public.bt_type (
                                id integer NOT NULL,
                                relation character varying,
                                description_fr character varying,
                                description_en character varying
                            );
                        ALTER TABLE ONLY public.bt_type ADD CONSTRAINT bt_type_pkey PRIMARY KEY (id);
                        ALTER TABLE ONLY public.bt_type ADD CONSTRAINT bt_type_relation_key UNIQUE (relation);
                        INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (1, ''BT'', ''Terme générique'', ''Broader term'');
                        INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (2, ''BTG'', ''Terme générique (generic)'', ''Broader term (generic)'');
                        INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (3, ''BTP'', ''Terme générique (partitive)'', ''Broader term (partitive)'');
                        INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (4, ''BTI'', ''Terme générique (instance)'', ''Broader term (instance)'');';
    END IF;

END;
$$ LANGUAGE plpgsql; 

  
--
--Permet de creér la table alignement_source si elle n'existe pas
--

CREATE OR REPLACE FUNCTION create_table_aligenementSources() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'alignement_source') THEN

        execute 
		'
CREATE TABLE alignement_source
(
  source character varying,
  requete character varying,
  type_rqt alignement_type_rqt NOT NULL,
  alignement_format alignement_format NOT NULL,
  id integer NOT NULL DEFAULT nextval(''alignement_source__id_seq''::regclass),
  id_user integer,
  description character varying,
  gps boolean DEFAULT false,
  CONSTRAINT alignement_source_pkey PRIMARY KEY (id),
  CONSTRAINT alignement_source_source_key UNIQUE (source)
);

                INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps) VALUES 
                    (''GeoNames'', ''http://api.geonames.org/search?q=##value##&maxRows=10&style=FULL&lang=##lang##&username=opentheso'', ''REST'', ''xml'', 1, ''test de geonames'', true);
                INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps) VALUES
                    (''Pactols'', ''https://pactols.frantiq.fr/opentheso/webresources/rest/skos/concept/value=##value##&lang=##lang##&th=TH_1'', ''REST'', ''skos'', 1, ''test PACTOLS'', false);
                INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps) VALUES (''bnf'', ''PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX xml: <http://www.w3.org/XML/1998/namespace>
                SELECT ?instrument ?prop ?value where {
                  <http://data.bnf.fr/ark:/12148/cb119367821> skos:narrower+ ?instrument.
                  ?instrument ?prop ?value.
                  FILTER( (regex(?prop,skos:prefLabel) || regex(?prop,skos:altLabel))  && regex(?value, ##value##,"i") ) 
                    filter(lang(?value) =##lang##)
                } LIMIT 20'', ''SPARQL'', ''skos'', 1, ''Musique BNF'', false);
                INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps) VALUES (''Gemet'',
                    ''http://www.eionet.europa.eu/gemet/getConceptsMatchingKeyword?keyword=##value##&search_mode=3&thesaurus_uri=http://www.eionet.europa.eu/gemet/concept/&language=##lang##'',
                    ''REST'', ''json'', 1, ''source Gemet'', false);
                INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps) VALUES (''Wikidata'', ''SELECT ?item ?itemLabel ?itemDescription WHERE {
                            ?item rdfs:label "##value##"@##lang##.
                            SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],##lang##". }
                }'', ''SPARQL'', ''json'', 3, 9, ''alignement avec le thésaurus de Wikidata'', false);
';

    END IF;
END;
$$ LANGUAGE plpgsql;


--
--Permet de adjuter une nouvelle constraint a la table users
--

create or replace function adjuteconstraintuser() returns void as $$
begin
	if not exists (SELECT * from information_schema.table_constraints where table_name = 'users' and constraint_type = 'UNIQUE'
	and constraint_name ='users_mail_key1') then 
	execute
	'ALTER TABLE ONLY users
	  ADD CONSTRAINT users_mail_key1 UNIQUE 
	  (mail);';
  end if;
  end;
  $$LANGUAGE plpgsql;

--
--Permet de créer la table d'info
--

CREATE OR REPLACE FUNCTION create_table_info() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'info') THEN
        execute 
		'CREATE TABLE info (
		    version_Opentheso character varying,
		    version_Bdd character varying
		);';
    END IF;
END;
$$ LANGUAGE plpgsql;




CREATE OR REPLACE FUNCTION info_donnes() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT * FROM info ) THEN

        execute 
		'insert into info  values (''0.0.0'', ''xyz'');';
    END IF;
END;
$$ LANGUAGE plpgsql;


--
-- permet de supprimer une table 
-- avec condition si elle existe ou pas

CREATE OR REPLACE FUNCTION delete_table(TEXT) RETURNS VOID AS $$
DECLARE
 tableName ALIAS FOR $1;

BEGIN
    IF EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = tableName) THEN

        execute 'drop table ' || tableName ;

    END IF;
END;
$$ LANGUAGE plpgsql;

-- permet d'ajouter une colonne dans une table 
-- exp :  select updatecolumn_table('term','contributor','integer')
-- (table, colonne, type) 

CREATE OR REPLACE FUNCTION updateColumn_table(TEXT, TEXT, TEXT) RETURNS VOID AS $$
DECLARE
 tableName1 ALIAS FOR $1;
 columnName1 ALIAS for $2;
 typeVar ALIAS FOR $3;

BEGIN
    IF not EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = columnName1 AND TABLE_NAME = tableName1) THEN
	Execute
	'Alter TABLE '||tableName1||' ADD COLUMN ' || columnName1|| ' ' || typeVar||'  default false;';
    END IF;
END;
$$ LANGUAGE plpgsql; 

-- Table: public.user_role
-- DROP TABLE public.user_role;
-- permet de créer la table user_role qui n'existait pas avant la version 4.8
-- deprecated 

--CREATE OR REPLACE FUNCTION createUser_role() RETURNS VOID AS $$
--DECLARE 

--Begin
--	IF NOT EXISTs (SELECT * from information_schema.tables  where table_name = 'user_role') then
--		execute 'CREATE TABLE user_role 
--		(
--		  id_user integer NOT NULL,
--		  id_role integer NOT NULL,
--		  id_thesaurus character varying  DEFAULT ''''::character varying,
--		  id_group character varying,
--		  CONSTRAINT user_role_pkey PRIMARY KEY (id_user, id_role, id_thesaurus))
--		WITH (
--		  OIDS=FALSE
--		);
--
--		insert into user_role (id_user, id_role)
--		 SELECT id, id_role from users;
--
--
--		Alter Table users drop id_role;';
--
--		END IF;
--END;
--$$ LANGUAGE plpgsql;

--
-- permet d'ajouter la table user_historique
--

CREATE OR REPLACE FUNCTION create_table_users_historique() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'users_historique') THEN

        execute 
		'CREATE TABLE users_historique (
		    id_user integer NOT NULL,
		    username character varying,
		    created timestamp(6) with time zone NOT NULL DEFAULT now(),
		    modified timestamp(6) with time zone NOT NULL DEFAULT now(),
                    delete timestamp(6) with time zone,
			CONSTRAINT users_historique_pkey PRIMARY KEY (id_user))
		WITH (
		  OIDS=FALSE
		);
                INSERT INTO users_historique (id_user, username)
                SELECT id_user, username from users;

                ';

    END IF;
END;
$$ LANGUAGE plpgsql;

--
--permet d'ajouter la table thesaurus_alignement_source
--

CREATE OR REPLACE FUNCTION create_table_thesaurus_alignement_source() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'thesaurus_alignement_source') THEN

        execute 
		'CREATE TABLE public.thesaurus_alignement_source
		(
		  id_thesaurus character varying NOT NULL,
		  id_alignement_source integer NOT NULL,
		  CONSTRAINT thesaurus_alignement_source_pkey PRIMARY KEY (id_thesaurus, id_alignement_source)
		)
		WITH (
		  OIDS=FALSE
		);';

    END IF;
END;
$$ LANGUAGE plpgsql;

--
--permet de changer la column de alignemet source
--

CREATE OR REPLACE FUNCTION updateColumn_alignement_source() RETURNS VOID AS $$
BEGIN
    IF  EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'id_thesaurus' AND TABLE_NAME = 'alignement_source') THEN
	Execute
	'Alter TABLE alignement_source DROP COLUMN  id_thesaurus;
	 Alter TABLE alignement_source ADD COLUMN id_user integer;';
    END IF;
END;
$$ LANGUAGE plpgsql; 

--
--Permet d'ajouter la column description dans alignement source
--

CREATE OR REPLACE FUNCTION ajoutercolumn_alignement_source() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'description' AND TABLE_NAME = 'alignement_source') THEN
	Execute
	'
	 Alter TABLE alignement_source ADD COLUMN description character varying;';
    END IF;
END;
$$ LANGUAGE plpgsql; 

--
--
--
CREATE OR REPLACE FUNCTION ajoutercolumn_alignement() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'id_alignement_source' AND TABLE_NAME = 'alignement') THEN
	Execute
	'
	 Alter TABLE alignement ADD COLUMN id_alignement_source integer;
	 ALTER TABLE ONLY alignement ADD CONSTRAINT alignement_uri_target_internal_id_thesaurus_internal_id_con_key 
	 UNIQUE (uri_target, internal_id_thesaurus, internal_id_concept);

';
    END IF;
END;
$$ LANGUAGE plpgsql; 


--permet d'ajouter une sequence si elle n'existe pas

CREATE OR REPLACE FUNCTION ajouter_sequence(TEXT) RETURNS VOID AS $$
DECLARE
 nom_sequence ALIAS FOR $1;

BEGIN
    IF NOT EXISTS (SELECT * FROM information_schema.sequences WHERE sequence_name = nom_sequence) THEN

        execute 'CREATE sequence ' || nom_sequence || '
              INCREMENT 1
		  MINVALUE 1
		  MAXVALUE 9223372036854775807
		  START 1
		  CACHE 1;';

    END IF;
END;
$$ LANGUAGE plpgsql;


-- permet de supprimer une sequence si elle existe

CREATE OR REPLACE FUNCTION delete_sequence(TEXT) RETURNS VOID AS $$
DECLARE
 nom_sequence ALIAS FOR $1;

BEGIN
    IF EXISTS (SELECT * FROM information_schema.sequences WHERE sequence_name = nom_sequence) THEN
        execute 'Drop sequence ' || nom_sequence;
    END IF;
END;
$$ LANGUAGE plpgsql; 


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
--
--ajoute des colonnes creator et contributor
--
--drop function updateColumnTerm(TEXT);
CREATE OR REPLACE FUNCTION updateColumnTerm(TEXT) RETURNS VOID AS $$
DECLARE
tableName1 ALIAS for $1;
BEGIN
    IF not exists(SELECT *FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'creator' AND TABLE_NAME = tableName1) THEN

	EXECUTE
		'ALTER TABLE term ADD COLUMN contributor integer;
		ALTER TABLE term ADD COLUMN creator integer;';
     ELSE
     EXECUTE
		'
		ALTER TABLE term ALTER COLUMN creator TYPE integer USING (creator::integer);
		ALTER TABLE term ALTER COLUMN contributor TYPE integer USING (creator::integer);';
		END IF;
END;
$$ LANGUAGE plpgsql;

--
--permet de suprimer une colonne 
--select('nom_table','colonne')
--
CREATE OR REPLACE FUNCTION delete_column(TEXT, TEXT) RETURNS VOID AS $$
DECLARE
 nom_table ALIAS FOR $1;
 nom_column ALIAS for $2;

BEGIN
    IF EXISTS (SELECT *FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = nom_column AND TABLE_NAME = nom_table) THEN
     EXECUTE 
	'alter table '|| nom_table || ' drop column '||nom_column||';';
    END IF;
END;
$$ LANGUAGE plpgsql;


--
-- Permet de changer  les sequences de les tables-- term_historique
-- 

CREATE OR REPLACE FUNCTION updatesequencesTH() RETURNS VOID AS $$
declare 
id int;
BEGIN
	IF EXISTS (SELECT * FROM term where term.id is not null) THEN

	SELECT max(term.id) from term into id ; 
	id= id+2;
	Execute
		'ALTER TABLE ONLY term_historique ALTER COLUMN id SET DEFAULT nextval(''term_historique__id_seq''::regclass);
		ALTER SEQUENCE term_historique__id_seq RESTART WITH '|| id||';';
		else
		Execute
		'ALTER TABLE ONLY term_historique ALTER COLUMN id SET DEFAULT nextval(''term_historique__id_seq''::regclass);
		ALTER SEQUENCE term_historique__id_seq RESTART WITH 1;';
		END IF;
END;
$$ LANGUAGE plpgsql;

--
-- Permet de changer  les sequences de les tables-- users
-- 

CREATE OR REPLACE FUNCTION updatesequences_user() RETURNS VOID AS $$
declare 
id int;
BEGIN
	IF EXISTS (SELECT * FROM users where id_user is not null) THEN

	SELECT max(users.id_user) from users into id ; 
	id= id+2;
	Execute
		'
		ALTER SEQUENCE user__id_seq RESTART WITH '|| id||';';
	else
	Execute
		'
		ALTER SEQUENCE user__id_seq RESTART WITH 1;';
		END IF;
END;
$$ LANGUAGE plpgsql;

--
-- Permet de changer  les sequences de les tables-- concept_historique
-- 

CREATE OR REPLACE FUNCTION updatesequencesCH() RETURNS VOID AS $$
declare 
id int;
BEGIN
		IF EXISTS (SELECT * FROM concept where concept.id is not null) THEN
	SELECT max(concept.id) from concept into id ; 
	id= id+2;
	Execute
		'ALTER TABLE ONLY concept_historique ALTER COLUMN id SET DEFAULT nextval(''concept_historique__id_seq''::regclass);
		ALTER SEQUENCE concept_historique__id_seq RESTART WITH '|| id||';';
	ELSE 
	execute	
	'ALTER TABLE ONLY concept_historique ALTER COLUMN id SET DEFAULT nextval(''concept_historique__id_seq''::regclass);
		ALTER SEQUENCE concept_historique__id_seq RESTART WITH 1;';
	END IF;
END;
$$ LANGUAGE plpgsql;

--
-- Permet de changer  les sequences de les tables-- concept_group_label_historique
-- 

CREATE OR REPLACE FUNCTION updatesequencesCGLH() RETURNS VOID AS $$
declare 
id int;
BEGIN
	IF EXISTS (SELECT * FROM concept_group_label where concept_group_label.id is not null) THEN
	SELECT max(concept_group_label.id) from concept_group_label into id ; 
	id= id+2;
	Execute
		'ALTER TABLE ONLY concept_group_label_historique ALTER COLUMN id SET DEFAULT nextval(''concept_group_label_historique__id_seq''::regclass);
		ALTER SEQUENCE concept_group_label_historique__id_seq RESTART WITH '|| id||';';
		else 
		Execute
		'ALTER TABLE ONLY concept_group_label_historique ALTER COLUMN id SET DEFAULT nextval(''concept_group_label_historique__id_seq''::regclass);
		ALTER SEQUENCE concept_group_label_historique__id_seq RESTART WITH 1;';
		END IF;
END;
$$ LANGUAGE plpgsql;

--
-- Permet de changer  les sequences de les tables-- concept_group_historique
-- 

CREATE OR REPLACE FUNCTION updatesequencesCGH() RETURNS VOID AS $$
declare 
id int;
BEGIN
	IF EXISTS (SELECT * FROM concept_group where concept_group.id is not null) THEN
	SELECT max(concept_group.id) from concept_group into id ; 
	id= id+2;
	Execute
		'ALTER TABLE ONLY concept_group_historique ALTER COLUMN id SET DEFAULT nextval(''concept_group_historique__id_seq''::regclass);
		ALTER SEQUENCE concept_group_historique__id_seq RESTART WITH '|| id||';';
		else
		Execute
		'ALTER TABLE ONLY concept_group_historique ALTER COLUMN id SET DEFAULT nextval(''concept_group_historique__id_seq''::regclass);
		ALTER SEQUENCE concept_group_historique__id_seq RESTART WITH 1;';
		END IF;
END;
$$ LANGUAGE plpgsql;

--
-- Permet de changer  les sequences de les tables-- note_historique
-- 

CREATE OR REPLACE FUNCTION updatesequencesNH() RETURNS VOID AS $$
declare 
id int;
BEGIN
	IF EXISTS (SELECT * FROM note where note.id is not null) THEN
	SELECT max(note.id) from note into id ; 
	id= id+2;
	Execute
		'ALTER TABLE ONLY note_historique ALTER COLUMN id SET DEFAULT nextval(''note_historique__id_seq''::regclass);
		ALTER SEQUENCE note_historique__id_seq RESTART WITH '|| id||';';
	
	else
	Execute
		'ALTER TABLE ONLY note_historique ALTER COLUMN id SET DEFAULT nextval(''note_historique__id_seq''::regclass);
		ALTER SEQUENCE note_historique__id_seq RESTART WITH 1;';
		END IF;
END;
$$ LANGUAGE plpgsql;

--
-- Permet de changer  les sequences de la table-- note_historique
-- 

CREATE OR REPLACE FUNCTION updatesequencesalignement_source() RETURNS VOID AS $$
declare 
id int;
BEGIN
	IF EXISTS (SELECT * FROM alignement_source where alignement_source.id is not null) THEN
	SELECT max(alignement_source.id) from alignement_source into id ; 
	id= id+2;
	Execute
		'
		ALTER SEQUENCE alignement_source__id_seq RESTART WITH '|| id||';';
	
	else
	Execute
		'
		ALTER SEQUENCE alignement_source__id_seq RESTART WITH 1;';
		END IF;
END;
$$ LANGUAGE plpgsql;
-- 
-- fin des fonctions
-- 
create or replace function majnote() returns void as $$
begin
	if not exists (SELECT * from information_schema.table_constraints where table_name = 'note' and constraint_type = 'UNIQUE') then 
	execute 
	'ALTER TABLE ONLY note
	  ADD CONSTRAINT note_notetypecode_id_thesaurus_id_concept_lang_key UNIQUE (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue);
	ALTER TABLE ONLY note
	ADD CONSTRAINT note_notetypecode_id_thesaurus_id_term_lang_key UNIQUE (notetypecode, id_thesaurus, id_term, lang, lexicalvalue);';
	else 
	execute
	'alter table note drop constraint note_notetypecode_id_thesaurus_id_concept_lang_key;
	alter table note drop constraint note_notetypecode_id_thesaurus_id_term_lang_key;
	ALTER TABLE ONLY note
	  ADD CONSTRAINT note_notetypecode_id_thesaurus_id_concept_lang_key UNIQUE (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue);
	ALTER TABLE ONLY note
	ADD CONSTRAINT note_notetypecode_id_thesaurus_id_term_lang_key UNIQUE (notetypecode, id_thesaurus, id_term, lang, lexicalvalue);';
  end if;
  end;
  $$LANGUAGE plpgsql;
  
--
--Permet d'ajouter la column 'gps' dans la table 'concept'
-- Valeur par default: false
--

CREATE OR REPLACE FUNCTION ajouter_column_concept() RETURNS VOID AS $$
BEGIN
    IF not EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'gps' AND TABLE_NAME = 'concept') THEN
	Execute
	'Alter TABLE concept ADD COLUMN gps boolean default false;';
    END IF;
END;
$$ LANGUAGE plpgsql;

--
-- Permet d'ajouter la constraint unique sûr la source dans alignement_source
--
create or replace function adjouteconstraint_alignement_source() returns void as $$
begin
	if not exists (SELECT * from information_schema.table_constraints where table_name = 'alignement_source' and constraint_type = 'UNIQUE'
	and constraint_name ='alignement_source_source_key') then 
	execute
	'ALTER TABLE ONLY alignement_source
	  ADD CONSTRAINT alignement_source_source_key UNIQUE 
	  (source);';
  end if;
  end;
  $$LANGUAGE plpgsql;



--////////////////////////////////à partir de la Version 4.2.0/////////////////////////////
--
--Permet d'effacer la constrainte dans la table alignement
--
create or replace function drop_constraint_alignement_source() returns void as $$
begin
	if exists (SELECT * from information_schema.table_constraints where table_name = 'alignement' and constraint_type = 'UNIQUE'
	and constraint_name ='alignement_concept_target_thesaurus_target_alignement_id_ty_key') then 
	execute
	'ALTER TABLE ONLY alignement
	  DROP CONSTRAINT alignement_concept_target_thesaurus_target_alignement_id_ty_key ;';
  end if;
  end;
  $$LANGUAGE plpgsql;


--
--Permet de crée la table de note_type si n'exists pas
-- et de faire ses includes
--
CREATE OR REPLACE FUNCTION create_table_note_type()
  RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'note_type') THEN
        execute 
		'CREATE TABLE note_type
		(
		  code text NOT NULL,
		  isterm boolean NOT NULL,
		  isconcept boolean NOT NULL,
		  CONSTRAINT pk_note_type PRIMARY KEY (code),
		  CONSTRAINT chk_not_false_values CHECK (NOT (isterm = false AND isconcept = false))
		);
		INSERT INTO note_type (code, isterm, isconcept) VALUES (''customNote'', false, true);
		INSERT INTO note_type (code, isterm, isconcept) VALUES (''definition'', true, false);
		INSERT INTO note_type (code, isterm, isconcept) VALUES (''editorialNote'', true, false);
		INSERT INTO note_type (code, isterm, isconcept) VALUES (''historyNote'', true, true);
		INSERT INTO note_type (code, isterm, isconcept) VALUES (''scopeNote'', false, true);
		INSERT INTO note_type (code, isterm, isconcept) VALUES (''note'', false, true);
		INSERT INTO note_type (code, isterm, isconcept) VALUES (''example'', true, false);
		INSERT INTO note_type (code, isterm, isconcept) VALUES (''changeNote'', true, false);';

    END IF;
END;
$$  LANGUAGE plpgsql;

--
--Permet de crée la table de alignement_type si n'exists pas
-- et de faire ses includes
--

CREATE OR REPLACE FUNCTION create_table_alignement_type()
  RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'alignement_type') THEN
        execute 
		'CREATE TABLE alignement_type
			(
			  id integer NOT NULL,
			  label text NOT NULL,
			  isocode text NOT NULL,
			  label_skos character varying,
			  CONSTRAINT alignment_type_pkey PRIMARY KEY (id)
			);
			INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (1, ''Equivalence exacte'', ''=EQ'', ''exactMatch'');
			INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (2, ''Equivalence inexacte'', ''~EQ'', ''closeMatch'');
			INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (3, ''Equivalence générique'', ''EQB'', ''broadMatch'');
			INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (4, ''Equivalence associative'', ''EQR'', ''relatedMatch'');
			INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (5, ''Equivalence spécifique'', ''EQS'', ''narrowMatch'');';

    END IF;
END;
$$  LANGUAGE plpgsql;

--
--Permet d'ajouter la column gps a la table alignement_source 
--

CREATE OR REPLACE FUNCTION ajoutercolumngps_alignement_source() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'gps' AND TABLE_NAME = 'alignement_source') THEN
	Execute
	'
	 Alter TABLE alignement_source ADD COLUMN gps boolean  default false;';
    END IF;
END;
$$ LANGUAGE plpgsql; 

--
--Permet de mettre à jour la table preferences
--
CREATE OR REPLACE FUNCTION ajoutercolumn_preferences() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'url_counter_bdd' AND TABLE_NAME = 'preferences') THEN
        begin
            DROP TABLE preferences;
            CREATE TABLE preferences
            (
              id_pref integer NOT NULL DEFAULT nextval('pref__id_seq'::regclass),
              id_thesaurus character varying NOT NULL,
              source_lang character varying(2) DEFAULT 'fr'::character varying,
              nb_alert_cdt integer DEFAULT 10,
              alert_cdt boolean DEFAULT false,
              identifier_type integer DEFAULT 2,
              use_ark boolean DEFAULT false,
              server_ark character varying DEFAULT 'http://ark.mondomaine.fr/ark:/'::character varying,
              path_image character varying DEFAULT '/var/www/images/'::character varying,
              dossier_resize character varying DEFAULT 'resize'::character varying,
              bdd_active boolean DEFAULT false,
              bdd_use_id boolean DEFAULT false,
              url_bdd character varying DEFAULT 'http://www.mondomaine.fr/concept/##value##'::character varying,
              url_counter_bdd character varying DEFAULT 'http://mondomaine.fr/concept/##conceptId##/total'::character varying,
              z3950actif boolean DEFAULT false,
              collection_adresse character varying DEFAULT 'KOHA/biblios'::character varying,
              notice_url character varying DEFAULT 'http://catalogue.mondomaine.fr/cgi-bin/koha/opac-search.pl?type=opac&op=do_search&q=an=terme'::character varying,
              url_encode character varying(10) DEFAULT 'UTF-8'::character varying,
              path_notice1 character varying DEFAULT '/var/www/notices/repositories.xml'::character varying,
              path_notice2 character varying DEFAULT '/var/www/notices/SchemaMappings.xml'::character varying,
              chemin_site character varying DEFAULT 'http://mondomaine.fr/'::character varying,
              webservices boolean DEFAULT true,
              CONSTRAINT preferences_pkey PRIMARY KEY (id_pref),
              CONSTRAINT preferences_id_thesaurus_key UNIQUE (id_thesaurus)
            )
            WITH (
              OIDS=FALSE
            );                                                       
        end;
    END IF;
END;
$$ LANGUAGE plpgsql; 



--
--Permet de changer l'ancienne constrainte par une nouvelle
--
create or replace function changeconstraintalignement() returns void as $$
begin
	if exists (SELECT * from information_schema.table_constraints where table_name = 'alignement' and constraint_type = 'UNIQUE'
	and constraint_name ='alignement_uri_target_internal_id_thesaurus_internal_id_con_key') then 
	execute
	'
            alter table alignement
            drop constraint alignement_uri_target_internal_id_thesaurus_internal_id_con_key;
        alter table alignement 
            add constraint  alignement_internal_id_concept_internal_id_thesaurus_id_alignement_source_key unique 
            (internal_id_concept, internal_id_thesaurus, id_alignement_source, alignement_id_type)
        ';
        end if;
        if exists (SELECT * from information_schema.table_constraints where table_name = 'alignement' and constraint_type = 'UNIQUE'
	and constraint_name ='alignement_internal_id_concept_internal_id_thesaurus_id_alignem') then 
	execute
	'
            alter table alignement
            drop constraint alignement_internal_id_concept_internal_id_thesaurus_id_alignem;
        ';
        end if;
        if exists (SELECT * from information_schema.table_constraints where table_name = 'alignement' and constraint_type = 'UNIQUE'
	and constraint_name ='alignement_internal_id_concept_internal_id_thesaurus_id_alignement_source_key') then 
	execute
	'
            alter table alignement
            drop constraint alignement_internal_id_concept_internal_id_thesaurus_id_alignement_source_key;
        ';
        end if;

        if not exists (SELECT * from information_schema.table_constraints where table_name = 'alignement' and constraint_type = 'UNIQUE'
	and constraint_name ='alignement_internal_id_concept_internal_id_thesaurus_id_alignement_source_key2') then 
	execute
	'
            alter table alignement
            add constraint  alignement_internal_id_concept_internal_id_thesaurus_id_alignement_source_key2 unique 
            (internal_id_concept, internal_id_thesaurus, id_alignement_source, alignement_id_type, uri_target)
        ';
        end if;
  end;
  $$LANGUAGE plpgsql;

--
--Ajoute la PRIMARY KEY dans alignement si n'exists pas
--
create or replace function add_primary_keyalignement() returns void as $$
begin
	if not exists (SELECT * from information_schema.table_constraints where table_name = 'alignement' and constraint_type = 'PRIMARY KEY'
	and constraint_name ='alignement_pkey') then 
	execute
	'
            ALTER TABLE ONLY alignement
			ADD CONSTRAINT alignement_pkey PRIMARY KEY (id);
';
  end if;
  end;
  $$LANGUAGE plpgsql;

create or replace function add_primary_keyalignement_source() returns void as $$
begin
	if not exists (SELECT * from information_schema.table_constraints where table_name = 'alignement_source' and constraint_type = 'PRIMARY KEY'
	and constraint_name ='alignement_source_pkey') then 
	execute
	'
            ALTER TABLE ONLY alignement_source
			ADD CONSTRAINT alignement_source_pkey PRIMARY KEY (id);
';
  end if;
  end;
  $$LANGUAGE plpgsql;



--- déprécié depuis la version 4.7 (inutile)

--
-- permet de changer les id des alignements s'ils sont à 0;
--
create or replace function id_alignements()
returns void as $$
declare
	p_data text[]:='{}';
	nom text;
	cherchesource text;
	source text;
	source_recuperated text;
	positio1 integer;
	positio2 integer;
	total integer;
	cherche integer;
	id integer:=0;
	begin 
		for nom in 
		select uri_target from alignement where id_alignement_source = 0
		loop
			 select position ('//' in nom) into positio1;
			 select substring (nom from positio1+2) into source_recuperated;
			 select position ('/' in source_recuperated) into positio2;
			 total := positio2 - positio1;
			 select substring (source_recuperated from 0 for positio2) into source_recuperated;
			for cherchesource in 
			select alignement_source.source from alignement_source
			loop
				if(source_recuperated = cherchesource) then
					select alignement_source.id from alignement_source where alignement_source.source = cherchesource into id;
				execute
					'update alignement set  id_alignement_source = '||id||' where uri_target = '||quote_literal(nom)||' and id_alignement_source = 0;';
				end if;
			end loop;
			if (id=0) then	
				execute
				'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format) VALUES ('''||source_recuperated||''', ''null'', ''REST'', ''xml'');';
				SELECT max(alignement_source.id) from alignement_source  into id;
				execute
				'update alignement set  id_alignement_source = '||id||' where uri_target = '||quote_literal(nom)||';';
				
			end if;
			id:=0;	
		end loop;
	end;
	$$language plpgsql;



--
--Pour effacer les contraintes de la BDD Huma-Num
--
create or replace function delete_constraint_term_changer_concept_historique() returns void as $$
begin
	if exists (SELECT * from information_schema.table_constraints where table_name = 'term_historique' and constraint_type = 'PRIMARY KEY'
	and constraint_name ='term_copy_pkey') then 
	execute
	'
            ALTER TABLE ONLY term_historique
			DROP CONSTRAINT term_copy_pkey ;
            ALTER TABLE ONLY term_historique
			ADD CONSTRAINT term_copy_pkey PRIMARY KEY (id, modified, id_user);
        ';
        end if;
	if exists (SELECT * from information_schema.table_constraints where table_name = 'term_historique' and constraint_type = 'UNIQUE'
	and constraint_name ='term_copy_id_term_lang_id_thesaurus_key') then 
	execute
	'
            ALTER TABLE ONLY term_historique
			DROP CONSTRAINT term_copy_id_term_lang_id_thesaurus_key ;
        ';
        end if;
	if exists (SELECT * from information_schema.table_constraints where table_name = 'term_historique' and constraint_type = 'UNIQUE'
	and constraint_name ='term_copy_id_term_lexical_value_lang_id_thesaurus_key') then 
	execute
	'
            ALTER TABLE ONLY term_historique
			DROP CONSTRAINT term_copy_id_term_lexical_value_lang_id_thesaurus_key ;
        ';
        end if;
	if exists (SELECT * from information_schema.table_constraints where table_name = 'concept_historique' and constraint_type = 'PRIMARY KEY'
	and constraint_name ='concept_copy_pkey') then 
	execute
	'
            ALTER TABLE ONLY concept_historique
			DROP CONSTRAINT concept_copy_pkey ;
            ALTER TABLE ONLY concept_historique
			ADD CONSTRAINT concept_copy_pkey PRIMARY KEY (id_concept, id_thesaurus, id_group, id_user, modified);
        ';
        end if;
  end;
  $$LANGUAGE plpgsql;

--
-- Permet de crée la table alignement_preferences
--
CREATE OR REPLACE FUNCTION alignement_preferences() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'alignement_preferences') THEN

        execute 
		'Create table alignement_preferences (
                    id integer DEFAULT nextval(''alignement_preferences_id_seq''::regclass)NOT NULL,
                    id_thesaurus character varying NOT NULL,
                    id_user integer NOT NULL,
                    id_concept_depart character varying,
                    id_concept_tratees character varying,
                    id_alignement_source integer,
                    CONSTRAINT alignement_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_concept_depart, id_alignement_source)
                     );';
    END IF;
END;
$$ LANGUAGE plpgsql;

--
--
--
CREATE OR REPLACE FUNCTION gps_preferences() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'gps_preferences') THEN

        execute 
		'Create table gps_preferences (
                    id integer DEFAULT nextval(''gps_preferences_id_seq''::regclass)NOT NULL,
                    id_thesaurus character varying NOT NULL,
                    id_user integer NOT NULL,
                    gps_integrertraduction boolean DEFAULT true,
                    gps_reemplacertraduction boolean DEFAULT true,
                    gps_alignementautomatique boolean DEFAULT true,
                    id_alignement_source integer,
                    CONSTRAINT gps_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_alignement_source)
                     );';
    END IF;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION delete_colonne_preferences() RETURNS VOID AS $$
BEGIN
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'gps_integrertraduction' AND TABLE_NAME = 'preferences') THEN
	Execute
	'
	 Alter TABLE preferences drop COLUMN gps_integrertraduction;
        ';
    END IF;
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'gps_reemplacertraduction' AND TABLE_NAME = 'preferences') THEN
	Execute
	'
	 Alter TABLE preferences drop COLUMN gps_reemplacertraduction;
        ';
    END IF;
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'gps_alignementautomatique' AND TABLE_NAME = 'preferences') THEN
	Execute
	'
	 Alter TABLE preferences drop COLUMN gps_alignementautomatique;
        ';
    END IF;
    IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
     WHERE COLUMN_NAME = 'gps_id_source' AND TABLE_NAME = 'preferences') THEN
	Execute
	'
	 Alter TABLE preferences drop COLUMN gps_id_source;
        ';
    END IF;
END;
$$ LANGUAGE plpgsql; 

--
--
--
create or replace function primary_key_info() returns void as 
$$
begin	
if not exists (SELECT * from information_schema.table_constraints where table_name = 'info' and constraint_type = 'PRIMARY KEY'
	and constraint_name ='info_pkey') then 
	execute
	'
            ALTER TABLE ONLY info
			ADD CONSTRAINT info_pkey PRIMARY KEY (version_opentheso, version_bdd);
        ';
        end if;
  end;
  $$LANGUAGE plpgsql;
















--
--
-- mise à jour de la table Concept pour intégrer la gestion des collections
--
--

-- ajout de la table (relation_group)

CREATE OR REPLACE FUNCTION relation_group() RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'relation_group') THEN

        execute 
		'CREATE TABLE relation_group
                ( id_group1 character varying NOT NULL,
                  id_thesaurus character varying NOT NULL,
                  relation character varying NOT NULL,
                  id_group2 character varying NOT NULL,
                  CONSTRAINT pk_relation_group PRIMARY KEY (id_group1, id_thesaurus, relation, id_group2)
                )'
	;

    END IF;
END;

$$LANGUAGE plpgsql VOLATILE;


--
--permet d'ajouter la table thesaurus_alignement_source
--

CREATE OR REPLACE FUNCTION create_table_concept_group_concept() RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'concept_group_concept') THEN

        execute 
		'CREATE TABLE concept_group_concept
                (
                  idgroup text NOT NULL,
                  idthesaurus text NOT NULL,
                  idconcept text NOT NULL,
                  CONSTRAINT concept_group_concept_pkey PRIMARY KEY (idgroup, idthesaurus, idconcept)
                )
                WITH (
                  OIDS=FALSE
                );';

    END IF;
END;
$$ LANGUAGE plpgsql;


--
-- fonction de transfert des données de la table Concept à la table (concept_gourp_concept) 
-- #MR
--
    create or replace function update_table_concept()
    returns void as $$
    declare
	line RECORD;

	begin 
		if exists (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'concept' and COLUMN_NAME = 'id_group') then
                    for line in select id_group, id_thesaurus, id_concept  from concept
			loop
                            begin
				INSERT INTO concept_group_concept (idgroup, idthesaurus, idconcept) VALUES (line.id_group, line.id_thesaurus,line.id_concept);
			    EXCEPTION WHEN unique_violation THEN continue;
			    end;
			end loop;
                    execute
                    '  CREATE TABLE concepttemp
			(
			  id_concept character varying NOT NULL,
			  id_thesaurus character varying NOT NULL,
			  id_ark character varying,
			  created timestamp with time zone NOT NULL DEFAULT now(),
			  modified timestamp with time zone NOT NULL DEFAULT now(),
			  status character varying,
			  notation character varying DEFAULT ''''::character varying,
			  top_concept boolean,
			  id integer DEFAULT nextval(''concept__id_seq''::regclass) NOT NULL,
			  gps boolean DEFAULT false,
			  CONSTRAINT concept_theso_pkey PRIMARY KEY (id_concept, id_thesaurus))
		  	WITH (OIDS=FALSE);
                    ';
                    
                    for line in select id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps from concept
                        loop
                                IF line.id_ark is null THEN line.id_ark = '' RETURN;
                                END IF;
                                IF line.status is null THEN line.status = '' RETURN;
                                END IF;									
                                IF line.notation is null THEN line.notation = '' RETURN;
                                END IF;										
                                IF line.top_concept is null THEN line.top_concept = false RETURN;
                                END IF;										
                            begin
                                INSERT INTO concepttemp (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps)
                                     VALUES (line.id_concept,line.id_thesaurus,line.id_ark,line.created, line.modified,
                                     line.status,line.notation,line.top_concept,line.id,line.gps);
                             EXCEPTION WHEN unique_violation THEN continue;
                             end;
                        end loop;
                    begin
                        drop TABLE concept;
                        ALTER TABLE concepttemp RENAME TO concept;
                        UPDATE concept_group SET idtypecode = 'MT';
                    end;
		end if;
	end;
	$$language plpgsql;


--
-- fonction pour mettre à jour la table GPS (ajouter la contrainte de clé unique si elle n'est pas juste)
-- #MR
--
create or replace function update_table_gps() returns void as $$
begin 
    if exists (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'gps') then
        begin
            ALTER TABLE gps DROP CONSTRAINT gps_pkey;
            ALTER TABLE ONLY gps
                ADD CONSTRAINT gps_pkey PRIMARY KEY (id_concept, id_theso);
        end;
    end if;
end;
$$language plpgsql;


--fonction pour créer une table copyright pour gérer les copyrights
--
--

create or replace function create_table_copyright() returns void as $$
begin
     IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'copyright') THEN

        execute 
		'CREATE TABLE copyright
                ( id_thesaurus character varying NOT NULL,
                  copyright character varying,
                  CONSTRAINT copyright_pkey PRIMARY KEY (id_thesaurus)
                )';
        

    END IF;
end;
$$language plpgsql;

--fonction pour insérer une valeur booleénne  dans la table thesaurus
--
--
create or replace function alter_table_thesaurus_private() returns void as $$
begin
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_name='thesaurus' AND column_name='private' ) THEN
        execute 'ALTER TABLE thesaurus ADD COLUMN private boolean DEFAULT false;';
    END IF;
end;
$$language plpgsql;




--fonction pour créer une table routine_mail pour l'utilisation 
-- des routines d'envoi de mail
--
create or replace function create_table_routine_mail() returns void as $$
begin
    IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name='routine_mail') THEN
        execute 'CREATE TABLE routine_mail 
                    ( id_thesaurus character varying PRIMARY KEY,
                      alert_cdt boolean DEFAULT true,
                      debut_env_cdt_propos DATE NOT NULL,
                      debut_env_cdt_valid DATE NOT NULL,
                      period_env_cdt_propos integer NOT NULL,
                      period_env_cdt_valid integer NOT NULL
                        )';
    END IF;
end
$$language plpgsql;

--mise à jour de la table préférence
--
create or replace function update_table_preferences_alert() returns void as $$
begin
    IF EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='alert_cdt') THEN
        execute 'ALTER TABLE preferences DROP COLUMN "alert_cdt";
                 ALTER TABLE preferences DROP COLUMN "nb_alert_cdt"';
    END IF;
end
$$language plpgsql;

--mise à jour de la table préférence pour Handle
--
create or replace function update_table_preferences_handle() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='use_handle') THEN
        execute 'ALTER TABLE preferences ADD COLUMN id_naan character varying DEFAULT ''66666''::character varying NOT NULL;
                ALTER TABLE preferences ADD COLUMN prefix_ark character varying DEFAULT ''crt''::character varying NOT NULL;
                ALTER TABLE preferences ADD COLUMN user_ark character varying;
                ALTER TABLE preferences ADD COLUMN pass_ark character varying;
                ALTER TABLE preferences ADD COLUMN use_handle boolean DEFAULT false;
                ALTER TABLE preferences ADD COLUMN user_handle character varying;
                ALTER TABLE preferences ADD COLUMN pass_handle character varying;
                ALTER TABLE preferences ADD COLUMN path_key_handle character varying DEFAULT ''/certificat/key.p12''::character varying;
                ALTER TABLE preferences ADD COLUMN path_cert_handle character varying DEFAULT ''/certificat/cacerts2''::character varying;
                ALTER TABLE preferences ADD COLUMN url_api_handle character varying DEFAULT ''https://handle-server.mondomaine.fr:8001/api/handles/''::character varying NOT NULL;
                ALTER TABLE preferences ADD COLUMN prefix_handle character varying DEFAULT ''66.666.66666''::character varying NOT NULL;
                ALTER TABLE preferences ADD COLUMN private_prefix_handle character varying DEFAULT ''crt''::character varying NOT NULL;';
    END IF;
end
$$language plpgsql;

create or replace function update_table_concept_handle() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='concept' AND column_name='id_handle') THEN
        execute 'ALTER TABLE concept ADD COLUMN id_handle character varying DEFAULT ''''::character varying;
                 ALTER TABLE concept ALTER COLUMN id_ark SET DEFAULT ''''::character varying';
    END IF;
end
$$language plpgsql;


---création de la table preferences_sparql
---
---
create or replace function create_table_preferences_sparql() returns void as $$
begin
    IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name='preferences_sparql' ) THEN
        execute 'CREATE TABLE public.preferences_sparql
                    (
                        adresse_serveur character varying,
                        mot_de_passe character varying,
                        nom_d_utilisateur character varying,
                        graph character varying,
                        synchronisation boolean NOT NULL DEFAULT false,
                        thesaurus character varying NOT NULL,
                        heure time without time zone,
                        CONSTRAINT preferences_sparql_pkey PRIMARY KEY (thesaurus)
                      )';
    END IF;
end
$$language plpgsql;




-----update de la table concept-group
-----ajout d'une colonne pour  numérotation
create or replace function update_table_concept_group() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='concept_group' AND column_name='numerotation') THEN
        execute 'ALTER TABLE public.concept_group ADD COLUMN numerotation integer';
    END IF;
end
$$language plpgsql;
----------------------------------------------------------------------------
-- mises à jour 
-- 
--
-- mise a jour de la table users (ajout de la colonne alertmail)
--
create or replace function update_table_users_alert() returns void as $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='users' AND column_name='alertmail') THEN
        execute 'ALTER TABLE users ADD COLUMN alertmail boolean DEFAULT false';
    END IF;
end
$$language plpgsql;

----------------------------------------------------------------------------
-- mises à jour
--
-- mise a jour de la table alignement (ajout d'une nouvelle contrainte)
--
create or replace function addconstraintto_alignement() returns void as $$
begin
    IF EXISTS(SELECT * FROM information_schema.constraint_table_usage where constraint_name ='alignement_internal_id_concept_internal_id_thesaurus_id_alignem') THEN
        execute 'ALTER TABLE alignement DROP CONSTRAINT alignement_internal_id_concept_internal_id_thesaurus_id_alignem;';
    END IF;
    IF EXISTS(SELECT * FROM information_schema.constraint_table_usage where constraint_name ='alignement_internal_id_concept_internal_id_thesaurus_id_ali_key') THEN
        execute 'ALTER TABLE alignement DROP CONSTRAINT alignement_internal_id_concept_internal_id_thesaurus_id_ali_key;';
    END IF;
    
   
    IF NOT EXISTS(SELECT * FROM information_schema.constraint_table_usage where constraint_name ='alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key') THEN
        execute 'alter TABLE ONLY alignement add CONSTRAINT alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key
	UNIQUE (internal_id_concept, internal_id_thesaurus, uri_target);';
    END IF;
end
$$language plpgsql;



----------------------------------------------------------------------------
-- mises à jour
--
-- mise a jour des indexes pour augmenter les performances
--
CREATE OR REPLACE FUNCTION table_hompage() RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'homepage') THEN

        execute 
	    'CREATE TABLE homepage (
            htmlcode character varying,
            lang character varying,
            CONSTRAINT homepage_lang_key UNIQUE (lang)
            );';
    END IF;
END;

$$LANGUAGE plpgsql VOLATILE;

----------------------------------------------------------------------------
-- mises à jour
--
-- mise a jour des indexes pour augmenter les performances
--
CREATE OR REPLACE FUNCTION table_thesohompage() RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'thesohomepage') THEN

        execute 
	    'CREATE TABLE thesohomepage (
            htmlcode character varying,
            lang character varying,
            idtheso character varying,
            CONSTRAINT thesohomepage_idtheso_lang_key UNIQUE (idtheso, lang)
            );';
    END IF;
END;

$$LANGUAGE plpgsql VOLATILE;









----------------------------------------------------------------------------
-- mises à jour
--
-- mise a jour des indexes pour augmenter les performances
--







CREATE INDEX IF NOT EXISTS  term_lexical_value_unaccent ON term USING GIN (f_unaccent(lower(lexical_value)) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS  term_lexical_value_npt_unaccent ON non_preferred_term USING GIN (f_unaccent(lower(lexical_value)) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS  note_lexical_value_unaccent ON note USING GIN (f_unaccent(lower(lexicalvalue)) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS  concept_notation_unaccent ON concept USING GIN (f_unaccent(lower(notation)) gin_trgm_ops);












-- création ou mise à jour des séquences
SELECT create_table_preferences_sparql();

SELECT create_table_routine_mail();
SELECT alter_table_thesaurus_private();
SELECT create_table_copyright();
SELECT majnote();
SELECT create_table_info();
SELECT info_donnes();
SELECT table_gps();
SELECT update_table_gps();
SELECT ajouter_column_concept();
SELECT ajouter_sequence('alignement_source__id_seq');
SELECT ajouter_sequence('concept_group_historique__id_seq');
SELECT ajouter_sequence('concept_group_label_historique__id_seq');
SELECT ajouter_sequence('concept_historique__id_seq');
SELECT ajouter_sequence('note_historique__id_seq');
SELECT ajouter_sequence('pref__id_seq');
SELECT ajouter_sequence('role_id_seq');
SELECT ajouter_sequence('term_historique__id_seq');
SELECT ajouter_sequence('alignement_preferences_id_seq');
SELECT ajouter_sequence('gps_preferences_id_seq');
--SELECT createUser_role();
SELECT updateColumnTerm('term');
SELECT updateColumn_table('users','passtomodify','boolean');
SELECT updateID_table();
SELECT updateIDusers();
SELECT updatesequencesNH();
SELECT updatesequencesCGH();
SELECT updatesequencesCGLH();
SELECT updatesequencesCH();
SELECT updatesequencesTH();
SELECT adjuteconstraintuser();
SELECT create_table_users_historique();
SELECT add_primary_keyalignement_source();
SELECT alignement_preferences();
SELECT gps_preferences();
SELECT table_hompage();
SELECT table_thesohompage();


SELECT primary_key_info();


SELECT create_table_thesaurus_alignement_source();
-- Creation de les types pour alignement_source

SELECT addtype_Alignement_format();

-- Type: public.alignement_type_rqt


SELECT addtype_Alignement_type_rqt();

-- Type: public.auth_method

SELECT add_primary_keyalignement();


SELECT addtype_relations_nt();
SELECT addtype_relations_bt();
SELECT addtype_auth_method();
SELECT create_table_aligenementSources();
SELECT updateColumn_alignement_source();
SELECT ajouterColumn_alignement_source();
SELECT adjouteconstraint_alignement_source();
SELECT ajoutercolumngps_alignement_source();

-- pas toucher
SELECT updatesequencesalignement_source();
SELECT ajoutercolumn_alignement();


UPDATE alignement SET id_alignement_source = 0  WHERE id_alignement_source  is null;
--SELECT id_alignements();
SELECT addconstraintto_alignement();
--SELECT changeconstraintalignement();

SELECT delete_constraint_term_changer_concept_historique();

--SELECT drop_constraint_alignement_source();
SELECT create_table_alignement_type();
SELECT ajoutercolumn_preferences();

SELECT delete_colonne_preferences();
SELECT update_table_preferences_alert();

SELECT create_table_note_type();

-- delete sequences
SELECT delete_sequence('user_username_seq');
SELECT delete_sequence('editorial_note__id_seq');
SELECT delete_sequence('definition_note__id_seq');
SELECT delete_sequence('history_note__id_seq');


ALTER TABLE ONLY roles ALTER COLUMN id SET DEFAULT nextval('role_id_seq'::regclass);

SELECT create_table_concept_group_concept();
SELECT update_table_concept();
SELECT relation_group();
SELECT update_table_concept_group();

--
-- création et mise à jour des tables
--
delete from roles;
INSERT INTO roles (id, name, description) VALUES (1, 'superAdmin', 'Super Administrateur pour tout gérer tout thésaurus et tout utilisateur');
INSERT INTO roles (id, name, description) VALUES (2, 'admin', 'administrateur pour un domaine ou parc de thésaurus');
INSERT INTO roles (id, name, description) VALUES (3, 'manager', 'gestionnaire de thésaurus, pas de création de thésaurus');
INSERT INTO roles (id, name, description) VALUES (4, 'contributor', 'traducteur, notes, candidats, images');

   
--mise a jour de la table concept_group
SELECT delete_column('concept_group','idparentgroup');
SELECT delete_column('concept_group','idconcept');


SELECT update_table_preferences_handle();
SELECT update_table_concept_handle();

SELECT update_table_users_alert();


/*
-- Mise à jour de la table de types d'alignement
DROP TABLE alignement_type;

CREATE TABLE alignement_type
(
  id integer NOT NULL,
  label text NOT NULL,
  isocode text NOT NULL,
  label_skos character varying,
  CONSTRAINT alignment_type_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);


INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (1, 'Equivalence exacte', '=EQ', 'exactMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (2, 'Equivalence inexacte', '~EQ', 'closeMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (3, 'Equivalence générique', 'EQB', 'broadMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (4, 'Equivalence associative', 'EQR', 'relatedMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (5, 'Equivalence spécifique', 'EQS', 'narrowMatch');
*/



-- Mise à jour de la table concept_group_type 
DELETE FROM concept_group_type;
INSERT INTO concept_group_type (code, label, skoslabel) VALUES ('MT', 'Microthesaurus', 'MicroThesaurus');
INSERT INTO concept_group_type (code, label, skoslabel) VALUES ('G', 'Group', 'ConceptGroup');
INSERT INTO concept_group_type (code, label, skoslabel) VALUES ('C', 'Collection', 'Collection');
INSERT INTO concept_group_type (code, label, skoslabel) VALUES ('T', 'Theme', 'Theme');


-- Mise à jour de la Function: unaccent_string(text)
DROP FUNCTION unaccent_string(text);

CREATE OR REPLACE FUNCTION unaccent_string(text)
  RETURNS text AS
$BODY$
DECLARE
input_string text := $1;
BEGIN

input_string := translate(input_string, 'âãäåāăąÁÂÃÄÅĀĂĄ', 'aaaaaaaaaaaaaaa');
input_string := translate(input_string, 'èééêëēĕėęěĒĔĖĘĚÉ', 'eeeeeeeeeeeeeeee');
input_string := translate(input_string, 'ìíîïìĩīĭÌÍÎÏÌĨĪĬ', 'iiiiiiiiiiiiiiii');
input_string := translate(input_string, 'óôõöōŏőÒÓÔÕÖŌŎŐ', 'ooooooooooooooo');
input_string := translate(input_string, 'ùúûüũūŭůÙÚÛÜŨŪŬŮ', 'uuuuuuuuuuuuuuuu');
input_string := translate(input_string, '-_/()', '     ');

return input_string;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
--Changer id_alignement_source a 0 si est null












--
-- à partir de la version 4.3.8
-- #MR

-- fonction pour supprimer une contrainte inutile dans la table concept_group_label_historique (concept_group_label_copy_idgrouplabel_key)
create or replace function update_table_iconcept_group_label_historique() returns void as $$
    begin 
        execute 'Alter TABLE ONLY concept_group_label_historique drop CONSTRAINT IF EXISTS concept_group_label_copy_idgrouplabel_key;';
    end;
$$language plpgsql;




-- fonction pour supprimer une contrainte dans la table user_role_group (user_role_pkey)
-- puis ajouter la nouvelle contrainte
create or replace function update_table_user_role_group_constraint() returns void as $$
    begin 
        execute 'Alter TABLE ONLY user_role_group drop CONSTRAINT IF EXISTS user_role_pkey;';
        execute 'Alter TABLE ONLY user_role_group drop CONSTRAINT IF EXISTS user_group_pkey;';
        execute 'Alter TABLE ONLY user_role_group add CONSTRAINT user_group_pkey UNIQUE(id_user, id_group);';
    end;
$$language plpgsql;




--fonction pour insérrer la colonne id_handle à la table concept_group
create or replace function alter_table_concept_group_addcolumn_id_handle() returns void as $$
begin
    IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_name='concept_group' AND column_name='id_handle' ) THEN
        execute 'ALTER TABLE concept_group ADD COLUMN id_handle character varying DEFAULT ''''::character varying;';
    END IF;
end;
$$language plpgsql;

---création de la table user_role_group
create or replace function create_table_user_role_group() returns void as $$
begin
    IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name='user_role_group' ) THEN
        execute 'CREATE TABLE user_role_group
                    (
                      id_user integer NOT NULL,
                      id_role integer NOT NULL,
                      id_group integer NOT NULL,
                      CONSTRAINT user_role_pkey PRIMARY KEY (id_user, id_role, id_group)
                    )';
    END IF;
end
$$language plpgsql;

---création de la table user_role_only_on
create or replace function create_table_user_role_only_on() returns void as $$
begin
    IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name='user_role_only_on' ) THEN
        execute 'CREATE TABLE user_role_only_on
                    (
                      id_user integer NOT NULL,
                      id_role integer NOT NULL,
                      id_theso character varying NOT NULL,
                      id_theso_domain character varying NOT NULL DEFAULT ''all''::character varying,
                      CONSTRAINT user_role_only_on_pkey PRIMARY KEY (id_user, id_role, id_theso)
                    )';
    END IF;
end
$$language plpgsql;

---création de la table user_group_thesaurus
create or replace function user_group_thesaurus() returns void as $$
begin
    IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name='user_group_thesaurus' ) THEN
        execute 'CREATE TABLE user_group_thesaurus
                    (
                      id_group integer NOT NULL,
                      id_thesaurus character varying NOT NULL,
                      CONSTRAINT user_group_thesaurus_pkey PRIMARY KEY (id_group, id_thesaurus),
                      CONSTRAINT user_group_thesaurus_id_thesaurus_key UNIQUE (id_thesaurus)
                    )';
    END IF;
end
$$language plpgsql;

---création de la table user_group_label
create or replace function user_group_label() returns void as $$
begin
    IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name='user_group_label' ) THEN
        execute 'CREATE TABLE public.user_group_label
                    (
                      id_group integer NOT NULL DEFAULT nextval(''user_group_label__id_seq''::regclass),
                      label_group character varying,
                      CONSTRAINT "user_group-label_pkey" PRIMARY KEY (id_group)
                    )';
    END IF;
end
$$language plpgsql;


----------------------------------------------------------------------------
-- récupération des roles pour adaptation à la nouvelle utilisation des roles
-- ajout de la colonne issuperadmin à la table users
create or replace function update_table_users() returns void as $$
    declare
	line RECORD;
	begin 
            if exists (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'user_role') then
                execute 'ALTER TABLE users ADD COLUMN issuperadmin boolean DEFAULT false;';

                for line in select id_user, id_role from user_role
                    loop
                        begin
                            update users SET issuperadmin = 'true' WHERE line.id_user = users.id_user and line.id_role = 1;
                        EXCEPTION WHEN unique_violation THEN continue;
                        end;
                    end loop;
                execute 'DROP TABLE user_role;';
            end if;
	end;
$$language plpgsql;


----------------------------------------------------------------------------
-- mise à jour de la table préférences pour le lien permanent vers le thésaurus DNS
-- ajout de la colonne preferredname 

create or replace function update_table_preferences_preferredname() returns void as $$
    declare
	line RECORD;
	begin 
            IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_name='preferences' AND column_name='preferredname' ) THEN
                execute 'Alter TABLE preferences ADD COLUMN preferredname character varying;
                        Alter TABLE ONLY preferences add CONSTRAINT preferences_preferredname_key UNIQUE (preferredname);';
            end if;
	end;
$$language plpgsql;


----------------------------------------------------------------------------
-- mise à jour de la table images pour la gestion des URI externes 

create or replace function update_table_imagesuri() returns void as $$
    declare
	line RECORD;
	begin 
            IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_name='images' AND column_name='external_uri' ) THEN
                execute 'Alter TABLE images ADD COLUMN external_uri character varying NOT NULL DEFAULT ''''::character varying;
                        Alter TABLE ONLY images drop CONSTRAINT images_pkey;';

            end if;
	end;
$$language plpgsql;


----------------------------------------------------------------------------
-- mise à jour de la table alignement_source pour ajouter la colonne filter (définir le mode de filtrage ex : Opentheso, Wikidata, Gemet ....)

create or replace function update_table_alignement_source_filter() returns void as $$
    begin 
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_name='alignement_source' AND column_name='source_filter' ) THEN
            execute 'Alter TABLE alignement_source ADD COLUMN source_filter character varying NOT NULL DEFAULT ''Opentheso''::character varying;';

        end if;
    end;
$$language plpgsql;





----------------------------------------------------------------------------
-- mise à jour de la table alignement source pour la gestion des nouvelles sources IdRef et Wikidata 

create or replace function update_table_alignement_source() returns void as $$
    declare
	begin 
            execute 'delete from alignement_source where source = ''idRefSujets'';
                     delete from alignement_source where source = ''idRefAuteurs'';
                     delete from alignement_source where source = ''idRefPersonnes'';
                     delete from alignement_source where source = ''IdRefLieux'';
                     delete from alignement_source where source = ''IdRefTitreUniforme'';
                     delete from alignement_source where source = ''wikidata'';
                     delete from alignement_source where source = ''IdRefSujets'';
                     delete from alignement_source where source = ''IdRefAuteurs'';
                     delete from alignement_source where source = ''IdRefPersonnes'';
                     delete from alignement_source where source = ''Wikidata'';
                     delete from alignement_source where source = ''Getty_AAT'';
                     delete from alignement_source where source = ''GeoNames'';
                     delete from alignement_source where source = ''Pactols'';
                     delete from alignement_source where source = ''Gemet'';
                     delete from alignement_source where source = ''Agrovoc'';';

	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'IdRefSujets') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES
                    (''IdRefSujets'', ''https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:(##value##)%20AND%20recordtype_z:r'',
                     ''REST'', ''json'', 1, ''alignement avec les Sujets de IdRef ABES Rameaux'', false, ''IdRefSujets'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'IdRefAuteurs') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES
                    (''IdRefAuteurs'', ''https://www.idref.fr/Sru/Solr?wt=json&q=nom_t:(##nom##)%20AND%20prenom_t:(##prenom##)%20AND%20recordtype_z:a&fl=ppn_z,affcourt_z,prenom_s,nom_s&start=0&rows=30&version=2.2'',
                     ''REST'', ''json'', 1, ''alignement avec les Auteurs de IdRef ABES'', false, ''IdRefAuteurs'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'IdRefPersonnes') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES
                    (''IdRefPersonnes'', ''https://www.idref.fr/Sru/Solr?wt=json&q=persname_t:(##value##)&fl=ppn_z,affcourt_z,prenom_s,nom_s&start=0&rows=30&version=2.2'',
                     ''REST'', ''json'', 1, ''alignement avec les Noms de personnes de IdRef ABES'', false, ''IdRefPersonnes'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'IdRefTitreUniforme') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES
                    (''IdRefTitreUniforme'', ''https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=uniformtitle_t:(##value##)%20AND%20recordtype_z:f'',
                     ''REST'', ''json'', 1, ''alignement avec les titres uniformes de IdRef ABES'', false, ''IdRefTitreUniforme'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'IdRefPersonnes') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES
                    (''IdRefLieux'', ''https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=geogname_t:(##value##)%20AND%20recordtype_z:c'',
                     ''REST'', ''json'', 1, ''alignement avec les Lieux de IdRef ABES'', false, ''IdRefLieux'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'Wikidata') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES
                    (''Wikidata'', ''SELECT ?item ?itemLabel ?itemDescription WHERE {
                    ?item rdfs:label "##value##"@##lang##.
                    SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],##lang##". }
                    }'', ''SPARQL'', ''json'', 1, ''alignement avec le thésaurus de wikidata'', false, ''Wikidata'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'Getty_AAT') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES
                    (''Getty_AAT'', ''http://vocabsservices.getty.edu/AATService.asmx/AATGetTermMatch?term=##value##&logop=and&notes='',
                     ''REST'', ''xml'', 1, ''alignement avec le thésaurus du Getty AAT'', false, ''Getty_AAT'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'GeoNames') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES 
                    (''GeoNames'', ''http://api.geonames.org/search?q=##value##&maxRows=10&style=FULL&lang=##lang##&username=opentheso'',
                     ''REST'', ''xml'', 1, ''Alignement avec GeoNames'', true, ''GeoNames'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'Pactols') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES 
                    (''Pactols'', ''https://pactols.frantiq.fr/opentheso/api/search?q=##value##&lang=##lang##&theso=TH_1'',
                     ''REST'', ''skos'', 1, ''Alignement avec PACTOLS'', false, ''Opentheso'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'Gemet') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES 
                    (''Gemet'', ''https://www.eionet.europa.eu/gemet/getConceptsMatchingKeyword?keyword=##value##&search_mode=3&thesaurus_uri=http://www.eionet.europa.eu/gemet/concept/&language=##lang##'',
                     ''REST'', ''json'', 1, ''Alignement avec le thésaurus Gemet'', false, ''Gemet'');';
            end if;
	    IF NOT EXISTS (SELECT source FROM alignement_source WHERE source = 'Agrovoc') THEN
		execute 'INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id_user, description, gps, source_filter) VALUES 
                    (''Agrovoc'', ''http://agrovoc.uniroma2.it/agrovoc/rest/v1/search/?query=##value##&lang=##lang##'',
                     ''REST'', ''json'', 1, ''Alignement avec le thésaurus Agrovoc'', false, ''Agrovoc'');';
            end if;
	end;
$$language plpgsql;


----------------------------------------------------------------------------
-- mise à jour de la table note_historique / ajout de la colonne action_performed 
create or replace function update_table_note_historique() returns void as $$
    begin 
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_name='note_historique' AND column_name='action_performed' ) THEN
            execute 'Alter TABLE note_historique ADD COLUMN action_performed character varying DEFAULT ''''::character varying;';
        end if;
    end;
$$language plpgsql;

----------------------------------------------------------------------------
-- mise à jour de la table term_historique / ajout de la colonne action 
create or replace function update_table_term_historique() returns void as $$
    begin 
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_name='term_historique' AND column_name='action' ) THEN
            execute 'Alter TABLE term_historique ADD COLUMN action character varying DEFAULT ''''::character varying;';
        end if;
    end;
$$language plpgsql;


----------------------------------------------------------------------------
-- mise à jour de la table note / ajout de la colonne id_user 
create or replace function update_table_note_user() returns void as $$
    begin 
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_name='note' AND column_name='id_user' ) THEN
            execute 'Alter TABLE note ADD COLUMN id_user integer;';
        end if;
    end;
$$language plpgsql;


create or replace function create_table_external_images() returns void as $$
    begin 
        IF NOT EXISTS (SELECT * FROM information_schema.tables WHERE table_name='external_images') THEN
            execute 'CREATE TABLE external_images
                    (
                      id_concept character varying NOT NULL,
                      id_thesaurus character varying NOT NULL,
                      image_name character varying NOT NULL,
                      image_copyright character varying NOT NULL,
                      id_user integer,
                      external_uri character varying NOT NULL DEFAULT ''''::character varying,
                      CONSTRAINT external_images_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri)
                    );';

        end if;
    end;
$$language plpgsql;


----------------------------------------------------------------------------
-- mise à jour de la table preferences pour ajouter les colonnes original_uri, original_uri_is_ark, original_uri_is_handle

create or replace function update_table_preferences_original_uri() returns void as $$
    begin 
        IF NOT EXISTS (SELECT * FROM information_schema.columns WHERE table_name='preferences' AND column_name='original_uri' ) THEN
            execute 'Alter TABLE preferences ADD COLUMN original_uri character varying COLLATE pg_catalog."default";
                     Alter TABLE preferences ADD COLUMN original_uri_is_ark boolean DEFAULT false;
                     Alter TABLE preferences ADD COLUMN original_uri_is_handle boolean DEFAULT false;
                     Alter TABLE preferences ADD COLUMN uri_ark character varying DEFAULT ''https://ark.mom.fr/ark:/''::character varying;
                     Alter TABLE preferences ADD COLUMN generate_handle boolean DEFAULT true;
                     Alter TABLE preferences ADD COLUMN auto_expand_tree boolean DEFAULT true;';
        end if;
    end;
$$language plpgsql;


----------------------------------------------------------------------------
-- Création de la table corpus pour le lien avec les ressources externes
CREATE OR REPLACE FUNCTION create_table_corpus()
  RETURNS void AS $$
BEGIN
    IF NOT EXISTS (SELECT table_name FROM information_schema.tables WHERE table_name = 'corpus_link') THEN
        execute 
                'CREATE TABLE public.corpus_link
                (
                    id_theso character varying NOT NULL,
                    corpus_name character varying NOT NULL DEFAULT ''''::character varying,
                    uri_count character varying DEFAULT ''''::character varying,
                    uri_link character varying NOT NULL DEFAULT ''''::character varying,
                    active boolean DEFAULT false,
                    CONSTRAINT corpus_link_pkey PRIMARY KEY (id_theso, corpus_name)
                );';

    END IF;
END;
$$  LANGUAGE plpgsql;





-- execution des fonctions 
SELECT update_table_iconcept_group_label_historique();
SELECT alter_table_concept_group_addcolumn_id_handle();
SELECT update_table_users();
SELECT create_table_user_role_group();
SELECT create_table_user_role_only_on();
SELECT user_group_thesaurus();
SELECT ajouter_sequence('user_group_label__id_seq');
SELECT user_group_label();
SELECT update_table_preferences_preferredname();
SELECT update_table_imagesuri();
SELECT create_table_external_images();
SELECT update_table_alignement_source_filter();
SELECT update_table_alignement_source();
SELECT update_table_note_historique();
SELECT update_table_term_historique();
SELECT update_table_preferences_original_uri();
select update_table_user_role_group_constraint();
select create_table_corpus();
select update_table_note_user();


-- suppression des fonctions 
select delete_fonction('alter_table_concept_group_addcolumn_id_handle','');
select delete_fonction('update_table_users','');
select delete_fonction('create_table_user_role_group','');
select delete_fonction('create_table_user_role_only_on','');
select delete_fonction('user_group_thesaurus','');
select delete_fonction('user_group_label','');
select delete_fonction('update_table_preferences_preferredname','');
select delete_fonction('update_table_imagesuri', '');
select delete_fonction('create_table_external_images','');
select delete_fonction('update_table_iconcept_group_label_historique','');
select delete_fonction('update_table_note_historique','');
select delete_fonction('update_table_term_historique','');

select delete_fonction('table_hompage','');
select delete_fonction('table_thesohompage','');
select delete_fonction('update_table_preferences_original_uri','');
select delete_fonction('update_table_user_role_group_constraint','');
select delete_fonction('create_table_corpus','');

select delete_fonction('update_table_note_user', '');






--
--Delete toutes les function
--
select delete_fonction ('update_table_concept_group','');
select delete_fonction ('create_table_preferences_sparql','');
SELECT delete_fonction ('create_table_routine_mail','');
SELECT delete_fonction ('alter_table_thesaurus_private','');
SELECT delete_fonction ('create_table_copyright','');
SELECT delete_fonction ('create_table_info','');
SELECT delete_fonction ('majnote', '');
SELECT delete_fonction ('table_gps','');
SELECT delete_fonction ('update_table_gps','');
SELECT delete_fonction ('info_donnes','');
SELECT delete_fonction ('ajouter_column_concept','');
SELECT delete_fonction ('updateColumn_alignement_source','');
SELECT delete_fonction ('create_table_thesaurus_alignement_source','');
SELECT delete_fonction ('updatesequencesch','');
SELECT delete_fonction ('updatesequencesth','');
SELECT delete_fonction ('updatesequencesnh','');
SELECT delete_fonction ('updatesequencescgh','');
SELECT delete_fonction ('updatesequencescglh','');
SELECT delete_fonction ('updatesequences_user','');
SELECT delete_fonction ('updateidusers','');
SELECT delete_fonction ('adjuteconstraintuser','');
SELECT delete_fonction ('updateid_table','');
SELECT delete_fonction ('create_table_users_historique','');
SELECT delete_fonction ('updatecolumn_alignement_source','');
SELECT delete_fonction ('updatecolumnterm','TEXT');
SELECT delete_fonction ('delete_table','TEXT');
SELECT delete_fonction ('delete_sequence','TEXT');
--SELECT delete_fonction ('createuser_role','');
SELECT delete_fonction ('create_table_aligenementsources','');
SELECT delete_fonction ('ajouter_sequence','TEXT');
SELECT delete_fonction ('addtype_relations_nt','');
SELECT delete_fonction ('addtype_relations_bt','');
SELECT delete_fonction ('addtype_auth_method','');
SELECT delete_fonction ('addtype_alignement_type_rqt','');
SELECT delete_fonction ('addtype_alignement_format','');
SELECT delete_fonction1 ('updatecolumn_table','TEXT','TEXT');
SELECT delete_fonction ('delete_column','TEXT','TEXT');
SELECT delete_fonction ('ajoutercolumn_preferences','');
SELECT delete_fonction ('ajoutercolumngps_alignement_source','');
SELECT delete_fonction ('ajoutercolumn_alignement','');
SELECT delete_fonction ('create_table_alignement_type','');
SELECT delete_fonction ('create_table_note_type','');
SELECT delete_fonction ('adjouteconstraint_alignement_source','');
SELECT delete_fonction ('drop_constraint_alignement_source','');
SELECT delete_fonction ('changeconstraintalignement','');
SELECT delete_fonction ('add_primary_keyalignement','');
SELECT delete_fonction ('id_alignements','');
SELECT delete_fonction ('addconstraintto_alignement', '');
SELECT delete_fonction ('add_primary_keyalignement_source','');
SELECT delete_fonction ('updatesequencesalignement_source','');
SELECT delete_fonction ('delete_constraint_term_changer_concept_historique','');
SELECT delete_fonction ('alignement_preferences','');
SELECT delete_fonction ('gps_preferences','');
SELECT delete_fonction ('delete_colonne_preferences','');
SELECT delete_fonction ('update_table_preferences_alert','');
SELECT delete_fonction ('primary_key_info','');
SELECT delete_fonction('update_table_concept','');
SELECT delete_fonction('relation_group','');
SELECT delete_fonction('create_table_concept_group_concept','');
SELECT delete_fonction('update_table_preferences_handle','');
SELECT delete_fonction('update_table_concept_handle','');
SELECT delete_fonction('update_table_users_alert','');
SELECT delete_fonction('update_table_alignement_source','');
SELECT delete_fonction('update_table_alignement_source_filter','');

--
--

--Ne pas toucher les prochaines fonctions
SELECT delete_fonction ('ajoutercolumn_alignement_source','');
SELECT delete_fonction ('delete_fonction','TEXT','TEXT');
select delete_fonction1('delete_fonction','TEXT','TEXT');
SELECT delete_fonction1 ('delete_fonction1','TEXT','TEXT');

