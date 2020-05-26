
--  !!!!!!! Attention !!!!!!!!!
--
-- pour le passage des anciennes versions vers la 4.0.8, vous allez perdre les utilisateurs
-- à cause de la nouvelle gestion avancée des droits 
-- L'utilisateur par defaut redevient (admin / admin)
-- 
-- Je suis en train de mettre en place un export et import complet pour passer les versions sans aucune perte de données.
--
--  !!!!!!! Attention !!!!!!!!! 



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
ALTER TABLE alignement_type
  OWNER TO opentheso;

INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (1, 'Equivalence exacte', '=EQ', 'exactMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (2, 'Equivalence inexacte', '~EQ', 'closeMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (3, 'Equivalence générique', 'EQB', 'broadMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (4, 'Equivalence associative', 'EQR', 'relatedMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (5, 'Equivalence spécifique', 'EQS', 'narrowMatch');



-- Mise à jour de la Function: unaccent_string(text)

DROP FUNCTION unaccent_string(text);

CREATE OR REPLACE FUNCTION unaccent_string(text)
  RETURNS text AS
$BODY$
DECLARE
input_string text := $1;
BEGIN

input_string := translate(input_string, 'âãäåāăąÁÂÃÄÅĀĂĄ', 'aaaaaaaaaaaaaaa');
input_string := translate(input_string, 'èééêëēĕėęěĒĔĖĘĚ', 'eeeeeeeeeeeeeee');
input_string := translate(input_string, 'ìíîïìĩīĭÌÍÎÏÌĨĪĬ', 'iiiiiiiiiiiiiiii');
input_string := translate(input_string, 'óôõöōŏőÒÓÔÕÖŌŎŐ', 'ooooooooooooooo');
input_string := translate(input_string, 'ùúûüũūŭůÙÚÛÜŨŪŬŮ', 'uuuuuuuuuuuuuuuu');
input_string := translate(input_string, '-_/()', '     ');

return input_string;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION unaccent_string(text)
  OWNER TO opentheso;



--mise à jour de la table note_type
DROP TABLE note_type;

CREATE TABLE note_type
(
  code text NOT NULL,
  isterm boolean NOT NULL,
  isconcept boolean NOT NULL,
  CONSTRAINT pk_note_type PRIMARY KEY (code),
  CONSTRAINT chk_not_false_values CHECK (NOT (isterm = false AND isconcept = false))
)
WITH (
  OIDS=FALSE
);
ALTER TABLE note_type
  OWNER TO opentheso;

INSERT INTO note_type (code, isterm, isconcept) VALUES ('customNote', false, true);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('definition', true, false);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('editorialNote', true, false);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('historyNote', true, true);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('scopeNote', false, true);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('note', false, true);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('example', true, false);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('changeNote', true, false);


--mise à jour de la table term 
ALTER TABLE term ADD COLUMN contributor character varying;
ALTER TABLE term ADD COLUMN creator character varying;


-- Table: preferences

CREATE SEQUENCE pref__id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE pref__id_seq
  OWNER TO opentheso;


DROP TABLE preferences;

CREATE TABLE preferences
(
  id_pref integer NOT NULL DEFAULT nextval('pref__id_seq'::regclass),
  id_thesaurus character varying NOT NULL,
  source_lang character varying(3),
  nb_alert_cdt integer,
  alert_cdt boolean,
  CONSTRAINT preferences_pkey PRIMARY KEY (id_pref),
  CONSTRAINT preferences_id_thesaurus_key UNIQUE (id_thesaurus)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE preferences
  OWNER TO opentheso;


-- INSERT INTO preferences (id_pref, source_lang, nb_alert_cdt, alert_cdt, id_thesaurus) VALUES (1, 'fr', 5, false, '1');


--
-- TOC entry 210 (class 1259 OID 56478)
-- Name: roles; Type: TABLE; Schema: public; Owner: opentheso; Tablespace: 
--
DROP TABLE roles;

CREATE TABLE roles (
    id integer NOT NULL,
    name character varying,
    description character varying
);


ALTER TABLE roles OWNER TO opentheso;

--
-- TOC entry 211 (class 1259 OID 56484)
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE role_id_seq OWNER TO opentheso;

--
-- TOC entry 2465 (class 0 OID 0)
-- Dependencies: 211
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: opentheso
--

ALTER SEQUENCE role_id_seq OWNED BY roles.id;


--
-- TOC entry 2347 (class 2604 OID 56571)
-- Name: id; Type: DEFAULT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY roles ALTER COLUMN id SET DEFAULT nextval('role_id_seq'::regclass);


--
-- TOC entry 2466 (class 0 OID 0)
-- Dependencies: 211
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('role_id_seq', 1, false);


--
-- TOC entry 2459 (class 0 OID 56478)
-- Dependencies: 210
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO roles (id, name, description) VALUES (1, 'superAdmin', 'Super Administrateur pour tout gérer tout thésaurus et tout utilisateur');
INSERT INTO roles (id, name, description) VALUES (2, 'admin', 'administrateur par thésaurus ou plus');
INSERT INTO roles (id, name, description) VALUES (3, 'user', 'utilisateur par thésaurus ou plus');
INSERT INTO roles (id, name, description) VALUES (4, 'traducteur', 'traducteur par thésaurus ou plus');
INSERT INTO roles (id, name, description) VALUES (5, 'images', 'gestion des images par thésaurus ou plus');
ALTER TABLE ONLY roles
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);



-- contraintes d'unicité sur la table note 
ALTER TABLE ONLY note
  ADD CONSTRAINT note_notetypecode_id_thesaurus_id_concept_lang_key UNIQUE (notetypecode, id_thesaurus, id_concept, lang);
ALTER TABLE ONLY note
  ADD CONSTRAINT note_notetypecode_id_thesaurus_id_term_lang_key UNIQUE (notetypecode, id_thesaurus, id_term, lang);



-- Sequence: user__id_seq

-- DROP SEQUENCE user__id_seq;

CREATE SEQUENCE user__id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE user__id_seq
  OWNER TO opentheso;



-- Table: users

DROP TABLE users;

CREATE TABLE users
(
  id_user integer NOT NULL DEFAULT nextval('user__id_seq'::regclass),
  username character varying NOT NULL,
  password character varying NOT NULL,
  active boolean NOT NULL,
  mail character varying,
  CONSTRAINT user_pkey PRIMARY KEY (id_user),
  CONSTRAINT user_username_key UNIQUE (username)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE users
  OWNER TO opentheso;


-- Table: user_role

-- DROP TABLE user_role;

CREATE TABLE user_role
(
  id_user integer NOT NULL,
  id_role integer NOT NULL,
  id_thesaurus character varying NOT NULL,
  id_group character varying,
  CONSTRAINT user_role_pkey PRIMARY KEY (id_user, id_role, id_thesaurus)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE user_role
  OWNER TO opentheso;

INSERT INTO users (id_user, username, password, active, mail) VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', true, 'admin@domaine.fr');

INSERT INTO user_role (id_user, id_role, id_thesaurus, id_group) VALUES (1, 1, '', '');

UPDATE concept SET notation = '' WHERE notation ilike 'null';


ALTER TABLE concept_group ALTER COLUMN idtypecode SET DEFAULT 'MT';
UPDATE concept_group SET idtypecode = 'MT' WHERE idtypecode ilike 'null';
ALTER TABLE concept_group ALTER COLUMN idtypecode SET NOT NULL;
UPDATE concept_group SET notation = '' WHERE notation ilike 'null';