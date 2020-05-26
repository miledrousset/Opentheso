--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6beta4
-- Dumped by pg_dump version 9.6beta4

-- Started on 2017-01-06 10:05:53

SET role = opentheso;


SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 1 (class 3079 OID 12387)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2684 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- TOC entry 548 (class 1247 OID 114915)
-- Name: alignement_format; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE alignement_format AS ENUM (
    'skos',
    'json',
    'xml'
);


--
-- TOC entry 551 (class 1247 OID 114922)
-- Name: alignement_type_rqt; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE alignement_type_rqt AS ENUM (
    'SPARQL',
    'REST'
);


--
-- TOC entry 633 (class 1247 OID 114928)
-- Name: auth_method; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE auth_method AS ENUM (
    'DB',
    'LDAP',
    'FILE',
    'test'
);


--
-- TOC entry 267 (class 1255 OID 115571)
-- Name: unaccent_string(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION unaccent_string(text) RETURNS text
    LANGUAGE plpgsql
    AS $_$
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
$_$;


--
-- TOC entry 185 (class 1259 OID 114938)
-- Name: alignement_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE alignement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 186 (class 1259 OID 114940)
-- Name: alignement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE alignement (
    id integer DEFAULT nextval('alignement_id_seq'::regclass) NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    author integer,
    concept_target character varying,
    thesaurus_target character varying,
    uri_target character varying,
    alignement_id_type integer NOT NULL,
    internal_id_thesaurus character varying NOT NULL,
    internal_id_concept character varying,
    id_alignement_source integer
);


--
-- TOC entry 187 (class 1259 OID 114949)
-- Name: alignement_source__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE alignement_source__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 188 (class 1259 OID 114951)
-- Name: alignement_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE alignement_source (
    source character varying,
    requete character varying,
    type_rqt alignement_type_rqt NOT NULL,
    alignement_format alignement_format NOT NULL,
    id integer DEFAULT nextval('alignement_source__id_seq'::regclass) NOT NULL,
    id_user integer,
    description character varying,
    gps boolean DEFAULT false
);


--
-- TOC entry 189 (class 1259 OID 114958)
-- Name: alignement_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE alignement_type (
    id integer NOT NULL,
    label text NOT NULL,
    isocode text NOT NULL,
    label_skos character varying
);


--
-- TOC entry 190 (class 1259 OID 114964)
-- Name: compound_equivalence; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE compound_equivalence (
    id_split_nonpreferredterm text NOT NULL,
    id_preferredterm text NOT NULL
);


--
-- TOC entry 191 (class 1259 OID 114970)
-- Name: concept__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE concept__id_seq
    START WITH 43
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 192 (class 1259 OID 114972)
-- Name: concept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_ark character varying,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    status character varying,
    notation character varying DEFAULT ''::character varying,
    top_concept boolean,
    id_group character varying NOT NULL,
    id integer DEFAULT nextval('concept__id_seq'::regclass),
    gps boolean DEFAULT false
);


--
-- TOC entry 193 (class 1259 OID 114982)
-- Name: concept_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE concept_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 194 (class 1259 OID 114984)
-- Name: concept_candidat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_candidat (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    status character varying DEFAULT 'a'::character varying,
    id integer DEFAULT nextval('concept_candidat__id_seq'::regclass),
    admin_message character varying,
    admin_id integer
);


--
-- TOC entry 195 (class 1259 OID 114994)
-- Name: concept_fusion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_fusion (
    id_concept1 character varying NOT NULL,
    id_concept2 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL
);


--
-- TOC entry 196 (class 1259 OID 115001)
-- Name: concept_group__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE concept_group__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 197 (class 1259 OID 115003)
-- Name: concept_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_group (
    idgroup text NOT NULL,
    id_ark text NOT NULL,
    idthesaurus text NOT NULL,
    idtypecode text DEFAULT 'MT'::text NOT NULL,
    notation text,
    id integer DEFAULT nextval('concept_group__id_seq'::regclass) NOT NULL
);


--
-- TOC entry 198 (class 1259 OID 115011)
-- Name: concept_group_concept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_group_concept (
    idgroup text NOT NULL,
    idthesaurus text NOT NULL,
    idconcept text NOT NULL
);


--
-- TOC entry 199 (class 1259 OID 115017)
-- Name: concept_group_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE concept_group_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 200 (class 1259 OID 115019)
-- Name: concept_group_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_group_historique (
    idgroup text NOT NULL,
    id_ark text NOT NULL,
    idthesaurus text NOT NULL,
    idtypecode text NOT NULL,
    idparentgroup text,
    notation text,
    idconcept text,
    id integer DEFAULT nextval('concept_group_historique__id_seq'::regclass) NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL
);


--
-- TOC entry 201 (class 1259 OID 115027)
-- Name: concept_group_label_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE concept_group_label_id_seq
    START WITH 60
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 202 (class 1259 OID 115029)
-- Name: concept_group_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_group_label (
    id integer DEFAULT nextval('concept_group_label_id_seq'::regclass) NOT NULL,
    lexicalvalue text NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    lang character varying(5) NOT NULL,
    idthesaurus text NOT NULL,
    idgroup text NOT NULL
);


--
-- TOC entry 203 (class 1259 OID 115038)
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE concept_group_label_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 204 (class 1259 OID 115040)
-- Name: concept_group_label_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_group_label_historique (
    id integer DEFAULT nextval('concept_group_label_historique__id_seq'::regclass) NOT NULL,
    lexicalvalue text NOT NULL,
    modified timestamp(6) without time zone DEFAULT now() NOT NULL,
    lang character varying(5) NOT NULL,
    idthesaurus text NOT NULL,
    idgroup text NOT NULL,
    id_user integer NOT NULL
);


--
-- TOC entry 205 (class 1259 OID 115048)
-- Name: concept_group_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_group_type (
    code text NOT NULL,
    label text NOT NULL,
    skoslabel text
);


--
-- TOC entry 206 (class 1259 OID 115054)
-- Name: concept_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE concept_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 207 (class 1259 OID 115056)
-- Name: concept_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_historique (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_ark character varying,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    status character varying,
    notation character varying DEFAULT ''::character varying,
    top_concept boolean,
    id_group character varying NOT NULL,
    id integer DEFAULT nextval('concept_historique__id_seq'::regclass),
    id_user integer NOT NULL
);


--
-- TOC entry 208 (class 1259 OID 115065)
-- Name: concept_orphan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_orphan (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 209 (class 1259 OID 115071)
-- Name: concept_term_candidat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE concept_term_candidat (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 210 (class 1259 OID 115077)
-- Name: custom_concept_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE custom_concept_attribute (
    "idConcept" character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 211 (class 1259 OID 115083)
-- Name: custom_term_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE custom_term_attribute (
    identifier character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 212 (class 1259 OID 115089)
-- Name: facet_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE facet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 252 (class 1259 OID 115481)
-- Name: gps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE gps (
    id_concept character varying NOT NULL,
    id_theso character varying,
    latitude double precision,
    longitude double precision
);


--
-- TOC entry 213 (class 1259 OID 115091)
-- Name: hierarchical_relationship; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE hierarchical_relationship (
    id_concept1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    role character varying NOT NULL,
    id_concept2 character varying NOT NULL
);


--
-- TOC entry 214 (class 1259 OID 115097)
-- Name: hierarchical_relationship_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE hierarchical_relationship_historique (
    id_concept1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    role character varying NOT NULL,
    id_concept2 character varying NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL,
    action character varying NOT NULL
);


--
-- TOC entry 215 (class 1259 OID 115104)
-- Name: images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE images (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    image_name character varying NOT NULL,
    image_copyright character varying NOT NULL,
    id_user integer
);


--
-- TOC entry 251 (class 1259 OID 115475)
-- Name: info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE info (
    version_opentheso character varying,
    version_bdd character varying
);


--
-- TOC entry 216 (class 1259 OID 115110)
-- Name: infodb__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE infodb__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 217 (class 1259 OID 115112)
-- Name: languages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE languages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 218 (class 1259 OID 115114)
-- Name: languages_iso639; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE languages_iso639 (
    iso639_1 character(3),
    iso639_2 character varying,
    english_name character varying,
    french_name character varying,
    id integer DEFAULT nextval('languages_id_seq'::regclass) NOT NULL
);


--
-- TOC entry 219 (class 1259 OID 115121)
-- Name: node_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE node_label (
    facet_id integer NOT NULL,
    id_thesaurus character varying NOT NULL,
    lexical_value character varying,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    lang character varying NOT NULL
);


--
-- TOC entry 220 (class 1259 OID 115129)
-- Name: non_preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE non_preferred_term (
    id_term character varying NOT NULL,
    lexical_value character varying NOT NULL,
    lang character varying NOT NULL,
    id_thesaurus text NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    source character varying,
    status character varying,
    hiden boolean DEFAULT false NOT NULL
);


--
-- TOC entry 221 (class 1259 OID 115138)
-- Name: non_preferred_term_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE non_preferred_term_historique (
    id_term character varying NOT NULL,
    lexical_value character varying NOT NULL,
    lang character varying NOT NULL,
    id_thesaurus text NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    source character varying,
    status character varying,
    hiden boolean DEFAULT false NOT NULL,
    id_user integer NOT NULL,
    action character varying NOT NULL
);


--
-- TOC entry 222 (class 1259 OID 115146)
-- Name: note__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE note__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 223 (class 1259 OID 115148)
-- Name: note; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE note (
    id integer DEFAULT nextval('note__id_seq'::regclass) NOT NULL,
    notetypecode text NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_term character varying,
    id_concept character varying,
    lang character varying NOT NULL,
    lexicalvalue character varying NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 224 (class 1259 OID 115157)
-- Name: note_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE note_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 225 (class 1259 OID 115159)
-- Name: note_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE note_historique (
    id integer DEFAULT nextval('note_historique__id_seq'::regclass) NOT NULL,
    notetypecode text NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_term character varying,
    id_concept character varying,
    lang character varying NOT NULL,
    lexicalvalue character varying NOT NULL,
    modified timestamp(6) without time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL
);


--
-- TOC entry 226 (class 1259 OID 115167)
-- Name: note_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE note_type (
    code text NOT NULL,
    isterm boolean NOT NULL,
    isconcept boolean NOT NULL,
    CONSTRAINT chk_not_false_values CHECK ((NOT ((isterm = false) AND (isconcept = false))))
);


--
-- TOC entry 227 (class 1259 OID 115174)
-- Name: permuted; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE permuted (
    ord integer NOT NULL,
    id_concept character varying NOT NULL,
    id_group character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_lang character varying NOT NULL,
    lexical_value character varying NOT NULL,
    ispreferredterm boolean NOT NULL,
    original_value character varying
);


--
-- TOC entry 228 (class 1259 OID 115180)
-- Name: pref__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE pref__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 229 (class 1259 OID 115182)
-- Name: preferences; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE preferences (
    id_pref integer DEFAULT nextval('pref__id_seq'::regclass) NOT NULL,
    id_thesaurus character varying NOT NULL,
    source_lang character varying(3),
    nb_alert_cdt integer,
    alert_cdt boolean,
    gps_id_source integer,
    gps_integrertraduction boolean DEFAULT true,
    gps_reemplacertraduction boolean DEFAULT true,
    gps_alignementautomatique boolean DEFAULT true
);


--
-- TOC entry 230 (class 1259 OID 115189)
-- Name: preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE preferred_term (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 231 (class 1259 OID 115195)
-- Name: proposition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE proposition (
    id_concept character varying NOT NULL,
    id_user integer NOT NULL,
    id_thesaurus character varying NOT NULL,
    note text,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    concept_parent character varying,
    id_group character varying
);


--
-- TOC entry 232 (class 1259 OID 115203)
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE roles (
    id integer NOT NULL,
    name character varying,
    description character varying
);


--
-- TOC entry 233 (class 1259 OID 115209)
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 2685 (class 0 OID 0)
-- Dependencies: 233
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE role_id_seq OWNED BY roles.id;


--
-- TOC entry 234 (class 1259 OID 115211)
-- Name: split_non_preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE split_non_preferred_term (
);


--
-- TOC entry 235 (class 1259 OID 115214)
-- Name: term__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE term__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 236 (class 1259 OID 115216)
-- Name: term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE term (
    id_term character varying NOT NULL,
    lexical_value character varying NOT NULL,
    lang character varying NOT NULL,
    id_thesaurus text NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    source character varying,
    status character varying DEFAULT 'D'::character varying,
    id integer DEFAULT nextval('term__id_seq'::regclass) NOT NULL,
    contributor integer,
    creator integer
);


--
-- TOC entry 237 (class 1259 OID 115226)
-- Name: term_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE term_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 238 (class 1259 OID 115228)
-- Name: term_candidat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE term_candidat (
    id_term character varying NOT NULL,
    lexical_value character varying NOT NULL,
    lang character varying NOT NULL,
    id_thesaurus text NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    contributor integer NOT NULL,
    id integer DEFAULT nextval('term_candidat__id_seq'::regclass) NOT NULL
);


--
-- TOC entry 239 (class 1259 OID 115237)
-- Name: term_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE term_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 240 (class 1259 OID 115239)
-- Name: term_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE term_historique (
    id_term character varying NOT NULL,
    lexical_value character varying NOT NULL,
    lang character varying NOT NULL,
    id_thesaurus text NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    source character varying,
    status character varying DEFAULT 'D'::character varying,
    id integer DEFAULT nextval('term_historique__id_seq'::regclass) NOT NULL,
    id_user integer NOT NULL
);


--
-- TOC entry 241 (class 1259 OID 115248)
-- Name: thesaurus_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE thesaurus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 242 (class 1259 OID 115250)
-- Name: thesaurus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE thesaurus (
    id_thesaurus character varying NOT NULL,
    id_ark character varying NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    id integer DEFAULT nextval('thesaurus_id_seq'::regclass) NOT NULL
);


--
-- TOC entry 254 (class 1259 OID 115522)
-- Name: thesaurus_alignement_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE thesaurus_alignement_source (
    id_thesaurus character varying NOT NULL,
    id_alignement_source integer NOT NULL
);


--
-- TOC entry 243 (class 1259 OID 115259)
-- Name: thesaurus_array; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE thesaurus_array (
    facet_id integer DEFAULT nextval('facet_id_seq'::regclass) NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_concept_parent character varying NOT NULL,
    ordered boolean DEFAULT false NOT NULL,
    notation character varying
);


--
-- TOC entry 244 (class 1259 OID 115267)
-- Name: thesaurus_array_concept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE thesaurus_array_concept (
    thesaurusarrayid integer NOT NULL,
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    arrayorder integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 245 (class 1259 OID 115274)
-- Name: thesaurus_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE thesaurus_label (
    id_thesaurus character varying NOT NULL,
    contributor character varying,
    coverage character varying,
    creator character varying,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    description character varying,
    format character varying,
    lang character varying NOT NULL,
    publisher character varying,
    relation character varying,
    rights character varying,
    source character varying,
    subject character varying,
    title character varying NOT NULL,
    type character varying
);


--
-- TOC entry 246 (class 1259 OID 115282)
-- Name: user__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE user__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 247 (class 1259 OID 115284)
-- Name: user_role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE user_role (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_group character varying
);


--
-- TOC entry 248 (class 1259 OID 115290)
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE users (
    id_user integer DEFAULT nextval('user__id_seq'::regclass) NOT NULL,
    username character varying NOT NULL,
    password character varying NOT NULL,
    active boolean DEFAULT true NOT NULL,
    mail character varying NOT NULL,
    passtomodify boolean DEFAULT false
);


--
-- TOC entry 249 (class 1259 OID 115299)
-- Name: users2; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE users2 (
    id_user integer DEFAULT nextval('user__id_seq'::regclass) NOT NULL,
    login character varying NOT NULL,
    fullname character varying,
    password character varying,
    active boolean DEFAULT true NOT NULL,
    mail character varying,
    authentication auth_method DEFAULT 'DB'::auth_method
);


--
-- TOC entry 253 (class 1259 OID 115512)
-- Name: users_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE users_historique (
    id_user integer NOT NULL,
    username character varying,
    created timestamp(6) with time zone DEFAULT now() NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    delete timestamp(6) with time zone
);


--
-- TOC entry 250 (class 1259 OID 115308)
-- Name: version_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE version_history (
    "idVersionhistory" character varying NOT NULL,
    "idThesaurus" character varying NOT NULL,
    date date,
    "versionNote" character varying,
    "currentVersion" boolean,
    "thisVersion" boolean NOT NULL
);


--
-- TOC entry 2339 (class 2604 OID 115570)
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY roles ALTER COLUMN id SET DEFAULT nextval('role_id_seq'::regclass);


--
-- TOC entry 2609 (class 0 OID 114940)
-- Dependencies: 186
-- Data for Name: alignement; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2686 (class 0 OID 0)
-- Dependencies: 185
-- Name: alignement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('alignement_id_seq', 1, false);


--
-- TOC entry 2611 (class 0 OID 114951)
-- Dependencies: 188
-- Data for Name: alignement_source; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps) VALUES ('wikipedia', 'https://$$lang$$.wikipedia.org/w/api.php?action=query&list=search&srwhat=text&format=xml&srsearch=$$value$$&srnamespace=0"', 'REST', 'xml', 1, NULL, NULL, false);
INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps) VALUES ('Pactols', 'http://pactols.frantiq.fr/opentheso/webresources/rest/skos/concept/value=$$value$$&lang=$$lang$$&th=TH_1', 'REST', 'skos', 2, NULL, NULL, false);
INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps) VALUES ('bnf', 'PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
SELECT ?instrument ?prop ?value where {
  <http://data.bnf.fr/ark:/12148/cb119367821> skos:narrower+ ?instrument.
  ?instrument ?prop ?value.
  FILTER( (regex(?prop,skos:prefLabel) || regex(?prop,skos:altLabel))  && regex(?value, $$value$$,"i") ) 
    filter(lang(?value) =$$lang$$)
} LIMIT 20', 'SPARQL', 'skos', 3, NULL, NULL, false);
INSERT INTO alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps) VALUES ('Geoname', 'http://api.geonames.org/search?q=##value##&maxRows=10&style=FULL&lang=##lang##&username=demo', 'REST', 'xml', 4, NULL, 'test de geonames', true);


--
-- TOC entry 2687 (class 0 OID 0)
-- Dependencies: 187
-- Name: alignement_source__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('alignement_source__id_seq', 3, true);


--
-- TOC entry 2612 (class 0 OID 114958)
-- Dependencies: 189
-- Data for Name: alignement_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (1, 'Equivalence exacte', '=EQ', 'exactMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (2, 'Equivalence inexacte', '~EQ', 'closeMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (3, 'Equivalence générique', 'EQB', 'broadMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (4, 'Equivalence associative', 'EQR', 'relatedMatch');
INSERT INTO alignement_type (id, label, isocode, label_skos) VALUES (5, 'Equivalence spécifique', 'EQS', 'narrowMatch');


--
-- TOC entry 2613 (class 0 OID 114964)
-- Dependencies: 190
-- Data for Name: compound_equivalence; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2615 (class 0 OID 114972)
-- Dependencies: 192
-- Data for Name: concept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2688 (class 0 OID 0)
-- Dependencies: 191
-- Name: concept__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('concept__id_seq', 1, false);


--
-- TOC entry 2617 (class 0 OID 114984)
-- Dependencies: 194
-- Data for Name: concept_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2689 (class 0 OID 0)
-- Dependencies: 193
-- Name: concept_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('concept_candidat__id_seq', 1, false);


--
-- TOC entry 2618 (class 0 OID 114994)
-- Dependencies: 195
-- Data for Name: concept_fusion; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2620 (class 0 OID 115003)
-- Dependencies: 197
-- Data for Name: concept_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2690 (class 0 OID 0)
-- Dependencies: 196
-- Name: concept_group__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('concept_group__id_seq', 1, false);


--
-- TOC entry 2621 (class 0 OID 115011)
-- Dependencies: 198
-- Data for Name: concept_group_concept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2623 (class 0 OID 115019)
-- Dependencies: 200
-- Data for Name: concept_group_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2691 (class 0 OID 0)
-- Dependencies: 199
-- Name: concept_group_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('concept_group_historique__id_seq', 1, false);


--
-- TOC entry 2625 (class 0 OID 115029)
-- Dependencies: 202
-- Data for Name: concept_group_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2627 (class 0 OID 115040)
-- Dependencies: 204
-- Data for Name: concept_group_label_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2692 (class 0 OID 0)
-- Dependencies: 203
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('concept_group_label_historique__id_seq', 1, false);


--
-- TOC entry 2693 (class 0 OID 0)
-- Dependencies: 201
-- Name: concept_group_label_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('concept_group_label_id_seq', 1, false);


--
-- TOC entry 2628 (class 0 OID 115048)
-- Dependencies: 205
-- Data for Name: concept_group_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO concept_group_type (code, label, skoslabel) VALUES ('MT', 'Microthesaurus', 'MicroThesaurus');
INSERT INTO concept_group_type (code, label, skoslabel) VALUES ('G', 'Group', 'ConceptGroup');
INSERT INTO concept_group_type (code, label, skoslabel) VALUES ('C', 'Collection', 'Collection');


--
-- TOC entry 2630 (class 0 OID 115056)
-- Dependencies: 207
-- Data for Name: concept_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2694 (class 0 OID 0)
-- Dependencies: 206
-- Name: concept_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('concept_historique__id_seq', 1, false);


--
-- TOC entry 2631 (class 0 OID 115065)
-- Dependencies: 208
-- Data for Name: concept_orphan; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2632 (class 0 OID 115071)
-- Dependencies: 209
-- Data for Name: concept_term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2633 (class 0 OID 115077)
-- Dependencies: 210
-- Data for Name: custom_concept_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2634 (class 0 OID 115083)
-- Dependencies: 211
-- Data for Name: custom_term_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2695 (class 0 OID 0)
-- Dependencies: 212
-- Name: facet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('facet_id_seq', 1, false);


--
-- TOC entry 2675 (class 0 OID 115481)
-- Dependencies: 252
-- Data for Name: gps; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2636 (class 0 OID 115091)
-- Dependencies: 213
-- Data for Name: hierarchical_relationship; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2637 (class 0 OID 115097)
-- Dependencies: 214
-- Data for Name: hierarchical_relationship_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2638 (class 0 OID 115104)
-- Dependencies: 215
-- Data for Name: images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2674 (class 0 OID 115475)
-- Dependencies: 251
-- Data for Name: info; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO info (version_opentheso, version_bdd) VALUES ('0.0.0', '4.2.1');


--
-- TOC entry 2696 (class 0 OID 0)
-- Dependencies: 216
-- Name: infodb__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('infodb__id_seq', 80, true);


--
-- TOC entry 2697 (class 0 OID 0)
-- Dependencies: 217
-- Name: languages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('languages_id_seq', 506, false);


--
-- TOC entry 2641 (class 0 OID 115114)
-- Dependencies: 218
-- Data for Name: languages_iso639; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('aa ', 'aar', 'Afar', 'afar', 1);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ab ', 'abk', 'Abkhazian', 'abkhaze', 2);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ace', 'Achinese', 'aceh', 3);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ach', 'Acoli', 'acoli', 4);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ada', 'Adangme', 'adangme', 5);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ady', 'Adyghe; Adygei', 'adyghé', 6);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'afa', 'Afro-Asiatic languages', 'afro-asiatiques, langues', 7);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'afh', 'Afrihili', 'afrihili', 8);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('af ', 'afr', 'Afrikaans', 'afrikaans', 9);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ain', 'Ainu', 'aïnou', 10);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ak ', 'aka', 'Akan', 'akan', 11);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'akk', 'Akkadian', 'akkadien', 12);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sq ', 'alb (B)
sqi (T)', 'Albanian', 'albanais', 13);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ale', 'Aleut', 'aléoute', 14);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'alg', 'Algonquian languages', 'algonquines, langues', 15);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'alt', 'Southern Altai', 'altai du Sud', 16);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('am ', 'amh', 'Amharic', 'amharique', 17);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ang', 'English, Old (ca.450-1100)', 'anglo-saxon (ca.450-1100)', 18);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'anp', 'Angika', 'angika', 19);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'apa', 'Apache languages', 'apaches, langues', 20);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ar ', 'ara', 'Arabic', 'arabe', 21);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'arc', 'Official Aramaic (700-300 BCE); Imperial Aramaic (700-300 BCE)', 'araméen d''empire (700-300 BCE)', 22);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('an ', 'arg', 'Aragonese', 'aragonais', 23);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hy ', 'arm (B)
hye (T)', 'Armenian', 'arménien', 24);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'arn', 'Mapudungun; Mapuche', 'mapudungun; mapuche; mapuce', 25);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'arp', 'Arapaho', 'arapaho', 26);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'art', 'Artificial languages', 'artificielles, langues', 27);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'arw', 'Arawak', 'arawak', 28);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('as ', 'asm', 'Assamese', 'assamais', 29);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ast', 'Asturian; Bable; Leonese; Asturleonese', 'asturien; bable; léonais; asturoléonais', 30);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ath', 'Athapascan languages', 'athapascanes, langues', 31);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'aus', 'Australian languages', 'australiennes, langues', 32);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('av ', 'ava', 'Avaric', 'avar', 33);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ae ', 'ave', 'Avestan', 'avestique', 34);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'awa', 'Awadhi', 'awadhi', 35);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ay ', 'aym', 'Aymara', 'aymara', 36);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('az ', 'aze', 'Azerbaijani', 'azéri', 37);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bad', 'Banda languages', 'banda, langues', 38);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bai', 'Bamileke languages', 'bamiléké, langues', 39);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ba ', 'bak', 'Bashkir', 'bachkir', 40);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bal', 'Baluchi', 'baloutchi', 41);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bm ', 'bam', 'Bambara', 'bambara', 42);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ban', 'Balinese', 'balinais', 43);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('eu ', 'baq (B)
eus (T)', 'Basque', 'basque', 44);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bas', 'Basa', 'basa', 45);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bat', 'Baltic languages', 'baltes, langues', 46);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bej', 'Beja; Bedawiyet', 'bedja', 47);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('be ', 'bel', 'Belarusian', 'biélorusse', 48);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bem', 'Bemba', 'bemba', 49);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bn ', 'ben', 'Bengali', 'bengali', 50);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ber', 'Berber languages', 'berbères, langues', 51);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bho', 'Bhojpuri', 'bhojpuri', 52);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bh ', 'bih', 'Bihari languages', 'langues biharis', 53);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bik', 'Bikol', 'bikol', 54);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bin', 'Bini; Edo', 'bini; edo', 55);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bi ', 'bis', 'Bislama', 'bichlamar', 56);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bla', 'Siksika', 'blackfoot', 57);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bnt', 'Bantu languages', 'bantou, langues', 58);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bo ', 'tib (B)
bod (T)', 'Tibetan', 'tibétain', 59);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bs ', 'bos', 'Bosnian', 'bosniaque', 60);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bra', 'Braj', 'braj', 61);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('br ', 'bre', 'Breton', 'breton', 62);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'btk', 'Batak languages', 'batak, langues', 63);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bua', 'Buriat', 'bouriate', 64);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'bug', 'Buginese', 'bugi', 65);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bg ', 'bul', 'Bulgarian', 'bulgare', 66);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('my ', 'bur (B)
mya (T)', 'Burmese', 'birman', 67);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'byn', 'Blin; Bilin', 'blin; bilen', 68);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cad', 'Caddo', 'caddo', 69);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cai', 'Central American Indian languages', 'amérindiennes de l''Amérique centrale, langues', 70);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'car', 'Galibi Carib', 'karib; galibi; carib', 71);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ca ', 'cat', 'Catalan; Valencian', 'catalan; valencien', 72);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cau', 'Caucasian languages', 'caucasiennes, langues', 73);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ceb', 'Cebuano', 'cebuano', 74);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cel', 'Celtic languages', 'celtiques, langues; celtes, langues', 75);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cs ', 'cze (B)
ces (T)', 'Czech', 'tchèque', 76);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ch ', 'cha', 'Chamorro', 'chamorro', 77);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'chb', 'Chibcha', 'chibcha', 78);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ce ', 'che', 'Chechen', 'tchétchène', 79);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'chg', 'Chagatai', 'djaghataï', 80);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('zh ', 'chi (B)
zho (T)', 'Chinese', 'chinois', 81);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'chk', 'Chuukese', 'chuuk', 82);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'chm', 'Mari', 'mari', 83);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'chn', 'Chinook jargon', 'chinook, jargon', 84);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cho', 'Choctaw', 'choctaw', 85);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'chp', 'Chipewyan; Dene Suline', 'chipewyan', 86);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'chr', 'Cherokee', 'cherokee', 87);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cv ', 'chv', 'Chuvash', 'tchouvache', 88);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'chy', 'Cheyenne', 'cheyenne', 89);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cmc', 'Chamic languages', 'chames, langues', 90);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cop', 'Coptic', 'copte', 91);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kw ', 'cor', 'Cornish', 'cornique', 92);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('co ', 'cos', 'Corsican', 'corse', 93);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cpe', 'Creoles and pidgins, English based', 'créoles et pidgins basés sur l''anglais', 94);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cpf', 'Creoles and pidgins, French-based', 'créoles et pidgins basés sur le français', 95);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cpp', 'Creoles and pidgins, Portuguese-based', 'créoles et pidgins basés sur le portugais', 96);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cr ', 'cre', 'Cree', 'cree', 97);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'crh', 'Crimean Tatar; Crimean Turkish', 'tatar de Crimé', 98);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'crp', 'Creoles and pidgins', 'créoles et pidgins', 99);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'csb', 'Kashubian', 'kachoube', 100);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'cus', 'Cushitic languages', 'couchitiques, langues', 101);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cy ', 'wel (B)
cym (T)', 'Welsh', 'gallois', 102);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cs ', 'cze (B)
ces (T)', 'Czech', 'tchèque', 103);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'dak', 'Dakota', 'dakota', 104);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('da ', 'dan', 'Danish', 'danois', 105);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'dar', 'Dargwa', 'dargwa', 106);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'day', 'Land Dayak languages', 'dayak, langues', 107);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'del', 'Delaware', 'delaware', 108);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'den', 'Slave (Athapascan)', 'esclave (athapascan)', 109);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('de ', 'ger (B)
deu (T)', 'German', 'allemand', 110);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'dgr', 'Dogrib', 'dogrib', 111);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'din', 'Dinka', 'dinka', 112);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('dv ', 'div', 'Divehi; Dhivehi; Maldivian', 'maldivien', 113);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'doi', 'Dogri', 'dogri', 114);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'dra', 'Dravidian languages', 'dravidiennes, langues', 115);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'dsb', 'Lower Sorbian', 'bas-sorabe', 116);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'dua', 'Duala', 'douala', 117);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'dum', 'Dutch, Middle (ca.1050-1350)', 'néerlandais moyen (ca. 1050-1350)', 118);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nl ', 'dut (B)
nld (T)', 'Dutch; Flemish', 'néerlandais; flamand', 119);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'dyu', 'Dyula', 'dioula', 120);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('dz ', 'dzo', 'Dzongkha', 'dzongkha', 121);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'efi', 'Efik', 'efik', 122);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'egy', 'Egyptian (Ancient)', 'égyptien', 123);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'eka', 'Ekajuk', 'ekajuk', 124);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('el ', 'gre (B)
ell (T)', 'Greek, Modern (1453-)', 'grec moderne (après 1453)', 125);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'elx', 'Elamite', 'élamite', 126);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('en ', 'eng', 'English', 'anglais', 127);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'enm', 'English, Middle (1100-1500)', 'anglais moyen (1100-1500)', 128);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('eo ', 'epo', 'Esperanto', 'espéranto', 129);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('et ', 'est', 'Estonian', 'estonien', 130);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('eu ', 'baq (B)
eus (T)', 'Basque', 'basque', 131);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ee ', 'ewe', 'Ewe', 'éwé', 132);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ewo', 'Ewondo', 'éwondo', 133);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'fan', 'Fang', 'fang', 134);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fo ', 'fao', 'Faroese', 'féroïen', 135);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fa ', 'per (B)
fas (T)', 'Persian', 'persan', 136);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'fat', 'Fanti', 'fanti', 137);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fj ', 'fij', 'Fijian', 'fidjien', 138);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'fil', 'Filipino; Pilipino', 'filipino; pilipino', 139);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fi ', 'fin', 'Finnish', 'finnois', 140);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'fiu', 'Finno-Ugrian languages', 'finno-ougriennes, langues', 141);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'fon', 'Fon', 'fon', 142);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fr ', 'fre (B)
fra (T)', 'French', 'français', 143);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fr ', 'fre (B)
fra (T)', 'French', 'français', 144);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'frm', 'French, Middle (ca.1400-1600)', 'français moyen (1400-1600)', 145);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'fro', 'French, Old (842-ca.1400)', 'français ancien (842-ca.1400)', 146);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'frr', 'Northern Frisian', 'frison septentrional', 147);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'frs', 'Eastern Frisian', 'frison oriental', 148);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fy ', 'fry', 'Western Frisian', 'frison occidental', 149);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ff ', 'ful', 'Fulah', 'peul', 150);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'fur', 'Friulian', 'frioulan', 151);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gaa', 'Ga', 'ga', 152);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gay', 'Gayo', 'gayo', 153);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gba', 'Gbaya', 'gbaya', 154);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gem', 'Germanic languages', 'germaniques, langues', 155);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ka ', 'geo (B)
kat (T)', 'Georgian', 'géorgien', 156);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('de ', 'ger (B)
deu (T)', 'German', 'allemand', 157);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gez', 'Geez', 'guèze', 158);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gil', 'Gilbertese', 'kiribati', 159);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gd ', 'gla', 'Gaelic; Scottish Gaelic', 'gaélique; gaélique écossais', 160);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ga ', 'gle', 'Irish', 'irlandais', 161);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gl ', 'glg', 'Galician', 'galicien', 162);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gv ', 'glv', 'Manx', 'manx; mannois', 163);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gmh', 'German, Middle High (ca.1050-1500)', 'allemand, moyen haut (ca. 1050-1500)', 164);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'goh', 'German, Old High (ca.750-1050)', 'allemand, vieux haut (ca. 750-1050)', 165);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gon', 'Gondi', 'gond', 166);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gor', 'Gorontalo', 'gorontalo', 167);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'got', 'Gothic', 'gothique', 168);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'grb', 'Grebo', 'grebo', 169);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'grc', 'Greek, Ancient (to 1453)', 'grec ancien (jusqu''à 1453)', 170);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('el ', 'gre (B)
ell (T)', 'Greek, Modern (1453-)', 'grec moderne (après 1453)', 171);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gn ', 'grn', 'Guarani', 'guarani', 172);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gsw', 'Swiss German; Alemannic; Alsatian', 'suisse alémanique; alémanique; alsacien', 173);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gu ', 'guj', 'Gujarati', 'goudjrati', 174);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'gwi', 'Gwich''in', 'gwich''in', 175);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'hai', 'Haida', 'haida', 176);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ht ', 'hat', 'Haitian; Haitian Creole', 'haïtien; créole haïtien', 177);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ha ', 'hau', 'Hausa', 'haoussa', 178);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'haw', 'Hawaiian', 'hawaïen', 179);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('he ', 'heb', 'Hebrew', 'hébreu', 180);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hz ', 'her', 'Herero', 'herero', 181);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'hil', 'Hiligaynon', 'hiligaynon', 182);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'him', 'Himachali languages; Western Pahari languages', 'langues himachalis; langues paharis occidentales', 183);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hi ', 'hin', 'Hindi', 'hindi', 184);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'hit', 'Hittite', 'hittite', 185);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'hmn', 'Hmong; Mong', 'hmong', 186);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ho ', 'hmo', 'Hiri Motu', 'hiri motu', 187);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hr ', 'hrv', 'Croatian', 'croate', 188);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'hsb', 'Upper Sorbian', 'haut-sorabe', 189);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hu ', 'hun', 'Hungarian', 'hongrois', 190);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'hup', 'Hupa', 'hupa', 191);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hy ', 'arm (B)
hye (T)', 'Armenian', 'arménien', 192);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'iba', 'Iban', 'iban', 193);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ig ', 'ibo', 'Igbo', 'igbo', 194);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('is ', 'ice (B)
isl (T)', 'Icelandic', 'islandais', 195);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('io ', 'ido', 'Ido', 'ido', 196);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ii ', 'iii', 'Sichuan Yi; Nuosu', 'yi de Sichuan', 197);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ijo', 'Ijo languages', 'ijo, langues', 198);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('iu ', 'iku', 'Inuktitut', 'inuktitut', 199);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ie ', 'ile', 'Interlingue; Occidental', 'interlingue', 200);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ilo', 'Iloko', 'ilocano', 201);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'inc', 'Indic languages', 'indo-aryennes, langues', 202);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('id ', 'ind', 'Indonesian', 'indonésien', 203);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ine', 'Indo-European languages', 'indo-européennes, langues', 204);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'inh', 'Ingush', 'ingouche', 205);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ik ', 'ipk', 'Inupiaq', 'inupiaq', 206);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ira', 'Iranian languages', 'iraniennes, langues', 207);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'iro', 'Iroquoian languages', 'iroquoises, langues', 208);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('is ', 'ice (B)
isl (T)', 'Icelandic', 'islandais', 209);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('it ', 'ita', 'Italian', 'italien', 210);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('jv ', 'jav', 'Javanese', 'javanais', 211);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'jbo', 'Lojban', 'lojban', 212);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ja ', 'jpn', 'Japanese', 'japonais', 213);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'jpr', 'Judeo-Persian', 'judéo-persan', 214);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'jrb', 'Judeo-Arabic', 'judéo-arabe', 215);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kaa', 'Kara-Kalpak', 'karakalpak', 216);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kab', 'Kabyle', 'kabyle', 217);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kac', 'Kachin; Jingpho', 'kachin; jingpho', 218);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kl ', 'kal', 'Kalaallisut; Greenlandic', 'groenlandais', 219);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kam', 'Kamba', 'kamba', 220);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kn ', 'kan', 'Kannada', 'kannada', 221);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kar', 'Karen languages', 'karen, langues', 222);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ks ', 'kas', 'Kashmiri', 'kashmiri', 223);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ka ', 'geo (B)
kat (T)', 'Georgian', 'géorgien', 224);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kr ', 'kau', 'Kanuri', 'kanouri', 225);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kaw', 'Kawi', 'kawi', 226);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kk ', 'kaz', 'Kazakh', 'kazakh', 227);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kbd', 'Kabardian', 'kabardien', 228);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kha', 'Khasi', 'khasi', 229);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'khi', 'Khoisan languages', 'khoïsan, langues', 230);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('km ', 'khm', 'Central Khmer', 'khmer central', 231);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kho', 'Khotanese; Sakan', 'khotanais; sakan', 232);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ki ', 'kik', 'Kikuyu; Gikuyu', 'kikuyu', 233);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('rw ', 'kin', 'Kinyarwanda', 'rwanda', 234);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ky ', 'kir', 'Kirghiz; Kyrgyz', 'kirghiz', 235);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kmb', 'Kimbundu', 'kimbundu', 236);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kok', 'Konkani', 'konkani', 237);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kv ', 'kom', 'Komi', 'kom', 238);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kg ', 'kon', 'Kongo', 'kongo', 239);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ko ', 'kor', 'Korean', 'coréen', 240);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kos', 'Kosraean', 'kosrae', 241);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kpe', 'Kpelle', 'kpellé', 242);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'krc', 'Karachay-Balkar', 'karatchai balkar', 243);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'krl', 'Karelian', 'carélien', 244);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kro', 'Kru languages', 'krou, langues', 245);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kru', 'Kurukh', 'kurukh', 246);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kj ', 'kua', 'Kuanyama; Kwanyama', 'kuanyama; kwanyama', 247);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kum', 'Kumyk', 'koumyk', 248);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ku ', 'kur', 'Kurdish', 'kurde', 249);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'kut', 'Kutenai', 'kutenai', 250);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'lad', 'Ladino', 'judéo-espagnol', 251);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'lah', 'Lahnda', 'lahnda', 252);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'lam', 'Lamba', 'lamba', 253);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lo ', 'lao', 'Lao', 'lao', 254);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('la ', 'lat', 'Latin', 'latin', 255);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lv ', 'lav', 'Latvian', 'letton', 256);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'lez', 'Lezghian', 'lezghien', 257);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('li ', 'lim', 'Limburgan; Limburger; Limburgish', 'limbourgeois', 258);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ln ', 'lin', 'Lingala', 'lingala', 259);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lt ', 'lit', 'Lithuanian', 'lituanien', 260);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'lol', 'Mongo', 'mongo', 261);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'loz', 'Lozi', 'lozi', 262);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lb ', 'ltz', 'Luxembourgish; Letzeburgesch', 'luxembourgeois', 263);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'lua', 'Luba-Lulua', 'luba-lulua', 264);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lu ', 'lub', 'Luba-Katanga', 'luba-katanga', 265);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lg ', 'lug', 'Ganda', 'ganda', 266);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'lui', 'Luiseno', 'luiseno', 267);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'lun', 'Lunda', 'lunda', 268);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'luo', 'Luo (Kenya and Tanzania)', 'luo (Kenya et Tanzanie)', 269);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'lus', 'Lushai', 'lushai', 270);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mk ', 'mac (B)
mkd (T)', 'Macedonian', 'macédonien', 271);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mad', 'Madurese', 'madourais', 272);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mag', 'Magahi', 'magahi', 273);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mh ', 'mah', 'Marshallese', 'marshall', 274);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mai', 'Maithili', 'maithili', 275);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mak', 'Makasar', 'makassar', 276);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ml ', 'mal', 'Malayalam', 'malayalam', 277);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'man', 'Mandingo', 'mandingue', 278);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mi ', 'mao (B)
mri (T)', 'Maori', 'maori', 279);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'map', 'Austronesian languages', 'austronésiennes, langues', 280);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mr ', 'mar', 'Marathi', 'marathe', 281);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mas', 'Masai', 'massaï', 282);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ms ', 'may (B)
msa (T)', 'Malay', 'malais', 283);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mdf', 'Moksha', 'moksa', 284);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mdr', 'Mandar', 'mandar', 285);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'men', 'Mende', 'mendé', 286);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mga', 'Irish, Middle (900-1200)', 'irlandais moyen (900-1200)', 287);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mic', 'Mi''kmaq; Micmac', 'mi''kmaq; micmac', 288);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'min', 'Minangkabau', 'minangkabau', 289);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mis', 'Uncoded languages', 'langues non codées', 290);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mk ', 'mac (B)
mkd (T)', 'Macedonian', 'macédonien', 291);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mkh', 'Mon-Khmer languages', 'môn-khmer, langues', 292);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mg ', 'mlg', 'Malagasy', 'malgache', 293);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mt ', 'mlt', 'Maltese', 'maltais', 294);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mnc', 'Manchu', 'mandchou', 295);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mni', 'Manipuri', 'manipuri', 296);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mno', 'Manobo languages', 'manobo, langues', 297);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'moh', 'Mohawk', 'mohawk', 298);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mn ', 'mon', 'Mongolian', 'mongol', 299);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mos', 'Mossi', 'moré', 300);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mi ', 'mao (B)
mri (T)', 'Maori', 'maori', 301);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ms ', 'may (B)
msa (T)', 'Malay', 'malais', 302);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mul', 'Multiple languages', 'multilingue', 303);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mun', 'Munda languages', 'mounda, langues', 304);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mus', 'Creek', 'muskogee', 305);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mwl', 'Mirandese', 'mirandais', 306);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'mwr', 'Marwari', 'marvari', 307);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('my ', 'bur (B)
mya (T)', 'Burmese', 'birman', 308);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'myn', 'Mayan languages', 'maya, langues', 309);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'myv', 'Erzya', 'erza', 310);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nah', 'Nahuatl languages', 'nahuatl, langues', 311);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nai', 'North American Indian languages', 'nord-amérindiennes, langues', 312);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nap', 'Neapolitan', 'napolitain', 313);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('na ', 'nau', 'Nauru', 'nauruan', 314);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nv ', 'nav', 'Navajo; Navaho', 'navaho', 315);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nr ', 'nbl', 'Ndebele, South; South Ndebele', 'ndébélé du Sud', 316);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nd ', 'nde', 'Ndebele, North; North Ndebele', 'ndébélé du Nord', 317);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ng ', 'ndo', 'Ndonga', 'ndonga', 318);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nds', 'Low German; Low Saxon; German, Low; Saxon, Low', 'bas allemand; bas saxon; allemand, bas; saxon, bas', 319);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ne ', 'nep', 'Nepali', 'népalais', 320);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'new', 'Nepal Bhasa; Newari', 'nepal bhasa; newari', 321);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nia', 'Nias', 'nias', 322);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nic', 'Niger-Kordofanian languages', 'nigéro-kordofaniennes, langues', 323);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'niu', 'Niuean', 'niué', 324);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nl ', 'dut (B)
nld (T)', 'Dutch; Flemish', 'néerlandais; flamand', 325);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nb ', 'nob', 'Bokmål, Norwegian; Norwegian Bokmål', 'norvégien bokmål', 326);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nog', 'Nogai', 'nogaï; nogay', 327);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'non', 'Norse, Old', 'norrois, vieux', 328);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('no ', 'nor', 'Norwegian', 'norvégien', 329);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nqo', 'N''Ko', 'n''ko', 330);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nso', 'Pedi; Sepedi; Northern Sotho', 'pedi; sepedi; sotho du Nord', 331);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nub', 'Nubian languages', 'nubiennes, langues', 332);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nwc', 'Classical Newari; Old Newari; Classical Nepal Bhasa', 'newari classique', 333);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ny ', 'nya', 'Chichewa; Chewa; Nyanja', 'chichewa; chewa; nyanja', 334);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nym', 'Nyamwezi', 'nyamwezi', 335);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nyn', 'Nyankole', 'nyankolé', 336);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nyo', 'Nyoro', 'nyoro', 337);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'nzi', 'Nzima', 'nzema', 338);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('oc ', 'oci', 'Occitan (post 1500)', 'occitan (après 1500)', 339);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('oj ', 'oji', 'Ojibwa', 'ojibwa', 340);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('or ', 'ori', 'Oriya', 'oriya', 341);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('om ', 'orm', 'Oromo', 'galla', 342);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'osa', 'Osage', 'osage', 343);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('os ', 'oss', 'Ossetian; Ossetic', 'ossète', 344);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ota', 'Turkish, Ottoman (1500-1928)', 'turc ottoman (1500-1928)', 345);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'oto', 'Otomian languages', 'otomi, langues', 346);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'paa', 'Papuan languages', 'papoues, langues', 347);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'pag', 'Pangasinan', 'pangasinan', 348);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'pal', 'Pahlavi', 'pahlavi', 349);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'pam', 'Pampanga; Kapampangan', 'pampangan', 350);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pa ', 'pan', 'Panjabi; Punjabi', 'pendjabi', 351);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'pap', 'Papiamento', 'papiamento', 352);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'pau', 'Palauan', 'palau', 353);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'peo', 'Persian, Old (ca.600-400 B.C.)', 'perse, vieux (ca. 600-400 av. J.-C.)', 354);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fa ', 'per (B)
fas (T)', 'Persian', 'persan', 355);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'phi', 'Philippine languages', 'philippines, langues', 356);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'phn', 'Phoenician', 'phénicien', 357);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pi ', 'pli', 'Pali', 'pali', 358);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pl ', 'pol', 'Polish', 'polonais', 359);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'pon', 'Pohnpeian', 'pohnpei', 360);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pt ', 'por', 'Portuguese', 'portugais', 361);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'pra', 'Prakrit languages', 'prâkrit, langues', 362);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'pro', 'Provençal, Old (to 1500);Occitan, Old (to 1500)', 'provençal ancien (jusqu''à 1500); occitan ancien (jusqu''à 1500)', 363);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ps ', 'pus', 'Pushto; Pashto', 'pachto', 364);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'qaa-qtz', 'Reserved for local use', 'réservée à l''usage local', 365);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('qu ', 'que', 'Quechua', 'quechua', 366);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'raj', 'Rajasthani', 'rajasthani', 367);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'rap', 'Rapanui', 'rapanui', 368);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'rar', 'Rarotongan; Cook Islands Maori', 'rarotonga; maori des îles Cook', 369);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'roa', 'Romance languages', 'romanes, langues', 370);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('rm ', 'roh', 'Romansh', 'romanche', 371);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'rom', 'Romany', 'tsigane', 372);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ro ', 'rum (B)
ron (T)', 'Romanian; Moldavian; Moldovan', 'roumain; moldave', 373);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ro ', 'rum (B)
ron (T)', 'Romanian; Moldavian; Moldovan', 'roumain; moldave', 374);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('rn ', 'run', 'Rundi', 'rundi', 375);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'rup', 'Aromanian; Arumanian; Macedo-Romanian', 'aroumain; macédo-roumain', 376);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ru ', 'rus', 'Russian', 'russe', 377);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sad', 'Sandawe', 'sandawe', 378);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sg ', 'sag', 'Sango', 'sango', 379);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sah', 'Yakut', 'iakoute', 380);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sai', 'South American Indian languages', 'sud-amérindiennes, langues', 381);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sal', 'Salishan languages', 'salishennes, langues', 382);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sam', 'Samaritan Aramaic', 'samaritain', 383);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sa ', 'san', 'Sanskrit', 'sanskrit', 384);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sas', 'Sasak', 'sasak', 385);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sat', 'Santali', 'santal', 386);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'scn', 'Sicilian', 'sicilien', 387);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sco', 'Scots', 'écossais', 388);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sel', 'Selkup', 'selkoupe', 389);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sem', 'Semitic languages', 'sémitiques, langues', 390);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sga', 'Irish, Old (to 900)', 'irlandais ancien (jusqu''à 900)', 391);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sgn', 'Sign Languages', 'langues des signes', 392);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'shn', 'Shan', 'chan', 393);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sid', 'Sidamo', 'sidamo', 394);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('si ', 'sin', 'Sinhala; Sinhalese', 'singhalais', 395);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sio', 'Siouan languages', 'sioux, langues', 396);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sit', 'Sino-Tibetan languages', 'sino-tibétaines, langues', 397);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sla', 'Slavic languages', 'slaves, langues', 398);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sk ', 'slo (B)
slk (T)', 'Slovak', 'slovaque', 399);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sk ', 'slo (B)
slk (T)', 'Slovak', 'slovaque', 400);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sl ', 'slv', 'Slovenian', 'slovène', 401);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sma', 'Southern Sami', 'sami du Sud', 402);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('se ', 'sme', 'Northern Sami', 'sami du Nord', 403);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'smi', 'Sami languages', 'sames, langues', 404);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'smj', 'Lule Sami', 'sami de Lule', 405);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'smn', 'Inari Sami', 'sami d''Inari', 406);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sm ', 'smo', 'Samoan', 'samoan', 407);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sms', 'Skolt Sami', 'sami skolt', 408);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sn ', 'sna', 'Shona', 'shona', 409);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sd ', 'snd', 'Sindhi', 'sindhi', 410);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'snk', 'Soninke', 'soninké', 411);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sog', 'Sogdian', 'sogdien', 412);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('so ', 'som', 'Somali', 'somali', 413);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'son', 'Songhai languages', 'songhai, langues', 414);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('st ', 'sot', 'Sotho, Southern', 'sotho du Sud', 415);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('es ', 'spa', 'Spanish; Castilian', 'espagnol; castillan', 416);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sq ', 'alb (B)
sqi (T)', 'Albanian', 'albanais', 417);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sc ', 'srd', 'Sardinian', 'sarde', 418);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'srn', 'Sranan Tongo', 'sranan tongo', 419);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sr ', 'srp', 'Serbian', 'serbe', 420);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'srr', 'Serer', 'sérère', 421);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ssa', 'Nilo-Saharan languages', 'nilo-sahariennes, langues', 422);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ss ', 'ssw', 'Swati', 'swati', 423);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'suk', 'Sukuma', 'sukuma', 424);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('su ', 'sun', 'Sundanese', 'soundanais', 425);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sus', 'Susu', 'soussou', 426);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'sux', 'Sumerian', 'sumérien', 427);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sw ', 'swa', 'Swahili', 'swahili', 428);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sv ', 'swe', 'Swedish', 'suédois', 429);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'syc', 'Classical Syriac', 'syriaque classique', 430);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'syr', 'Syriac', 'syriaque', 431);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ty ', 'tah', 'Tahitian', 'tahitien', 432);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tai', 'Tai languages', 'tai, langues', 433);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ta ', 'tam', 'Tamil', 'tamoul', 434);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tt ', 'tat', 'Tatar', 'tatar', 435);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('te ', 'tel', 'Telugu', 'télougou', 436);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tem', 'Timne', 'temne', 437);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ter', 'Tereno', 'tereno', 438);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tet', 'Tetum', 'tetum', 439);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tg ', 'tgk', 'Tajik', 'tadjik', 440);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tl ', 'tgl', 'Tagalog', 'tagalog', 441);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('th ', 'tha', 'Thai', 'thaï', 442);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bo ', 'tib (B)
bod (T)', 'Tibetan', 'tibétain', 443);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tig', 'Tigre', 'tigré', 444);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ti ', 'tir', 'Tigrinya', 'tigrigna', 445);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tiv', 'Tiv', 'tiv', 446);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tkl', 'Tokelau', 'tokelau', 447);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tlh', 'Klingon; tlhIngan-Hol', 'klingon', 448);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tli', 'Tlingit', 'tlingit', 449);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tmh', 'Tamashek', 'tamacheq', 450);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tog', 'Tonga (Nyasa)', 'tonga (Nyasa)', 451);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('to ', 'ton', 'Tonga (Tonga Islands)', 'tongan (Îles Tonga)', 452);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tpi', 'Tok Pisin', 'tok pisin', 453);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tsi', 'Tsimshian', 'tsimshian', 454);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tn ', 'tsn', 'Tswana', 'tswana', 455);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ts ', 'tso', 'Tsonga', 'tsonga', 456);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tk ', 'tuk', 'Turkmen', 'turkmène', 457);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tum', 'Tumbuka', 'tumbuka', 458);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tup', 'Tupi languages', 'tupi, langues', 459);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tr ', 'tur', 'Turkish', 'turc', 460);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tut', 'Altaic languages', 'altaïques, langues', 461);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tvl', 'Tuvalu', 'tuvalu', 462);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tw ', 'twi', 'Twi', 'twi', 463);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'tyv', 'Tuvinian', 'touva', 464);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'udm', 'Udmurt', 'oudmourte', 465);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'uga', 'Ugaritic', 'ougaritique', 466);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ug ', 'uig', 'Uighur; Uyghur', 'ouïgour', 467);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('uk ', 'ukr', 'Ukrainian', 'ukrainien', 468);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'umb', 'Umbundu', 'umbundu', 469);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'und', 'Undetermined', 'indéterminée', 470);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ur ', 'urd', 'Urdu', 'ourdou', 471);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('uz ', 'uzb', 'Uzbek', 'ouszbek', 472);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'vai', 'Vai', 'vaï', 473);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ve ', 'ven', 'Venda', 'venda', 474);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('vi ', 'vie', 'Vietnamese', 'vietnamien', 475);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('vo ', 'vol', 'Volapük', 'volapük', 476);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'vot', 'Votic', 'vote', 477);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'wak', 'Wakashan languages', 'wakashanes, langues', 478);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'wal', 'Wolaitta; Wolaytta', 'wolaitta; wolaytta', 479);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'war', 'Waray', 'waray', 480);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'was', 'Washo', 'washo', 481);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cy ', 'wel (B)
cym (T)', 'Welsh', 'gallois', 482);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'wen', 'Sorbian languages', 'sorabes, langues', 483);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('wa ', 'wln', 'Walloon', 'wallon', 484);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('wo ', 'wol', 'Wolof', 'wolof', 485);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'xal', 'Kalmyk; Oirat', 'kalmouk; oïrat', 486);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('xh ', 'xho', 'Xhosa', 'xhosa', 487);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'yao', 'Yao', 'yao', 488);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'yap', 'Yapese', 'yapois', 489);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('yi ', 'yid', 'Yiddish', 'yiddish', 490);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('yo ', 'yor', 'Yoruba', 'yoruba', 491);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'ypk', 'Yupik languages', 'yupik, langues', 492);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'zap', 'Zapotec', 'zapotèque', 493);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'zbl', 'Blissymbols; Blissymbolics; Bliss', 'symboles Bliss; Bliss', 494);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'zen', 'Zenaga', 'zenaga', 495);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'zgh', 'Standard Moroccan Tamazight', 'amazighe standard marocain', 496);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('za ', 'zha', 'Zhuang; Chuang', 'zhuang; chuang', 497);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('zh ', 'chi (B)
zho (T)', 'Chinese', 'chinois', 498);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'znd', 'Zande languages', 'zandé, langues', 499);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('zu ', 'zul', 'Zulu', 'zoulou', 500);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'zun', 'Zuni', 'zuni', 501);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'z', 'No linguistic content; Not applicable', 'pas de contenu linguistique; non applicable', 502);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES (NULL, 'zza', 'Zaza; Dimili; Dimli; Kirdki; Kirmanjki; Zazaki', 'zaza; dimili; dimli; kirdki; kirmanjki; zazaki', 503);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cu ', 'chu', 'Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic', 'vieux slave; vieux bulgare', 504);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ia ', 'ina', 'Interlingua (International Auxiliary Language Association)', 'interlingua', 505);
INSERT INTO languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nn ', 'nno', 'Norwegian Nynorsk; Nynorsk, Norwegian', 'norvégien nynorsk', 506);


--
-- TOC entry 2642 (class 0 OID 115121)
-- Dependencies: 219
-- Data for Name: node_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2643 (class 0 OID 115129)
-- Dependencies: 220
-- Data for Name: non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2644 (class 0 OID 115138)
-- Dependencies: 221
-- Data for Name: non_preferred_term_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2646 (class 0 OID 115148)
-- Dependencies: 223
-- Data for Name: note; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2698 (class 0 OID 0)
-- Dependencies: 222
-- Name: note__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('note__id_seq', 1, false);


--
-- TOC entry 2648 (class 0 OID 115159)
-- Dependencies: 225
-- Data for Name: note_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2699 (class 0 OID 0)
-- Dependencies: 224
-- Name: note_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('note_historique__id_seq', 1, false);


--
-- TOC entry 2649 (class 0 OID 115167)
-- Dependencies: 226
-- Data for Name: note_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO note_type (code, isterm, isconcept) VALUES ('customNote', false, true);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('definition', true, false);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('editorialNote', true, false);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('historyNote', true, true);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('scopeNote', false, true);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('note', false, true);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('example', true, false);
INSERT INTO note_type (code, isterm, isconcept) VALUES ('changeNote', true, false);


--
-- TOC entry 2650 (class 0 OID 115174)
-- Dependencies: 227
-- Data for Name: permuted; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2700 (class 0 OID 0)
-- Dependencies: 228
-- Name: pref__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('pref__id_seq', 1, false);


--
-- TOC entry 2652 (class 0 OID 115182)
-- Dependencies: 229
-- Data for Name: preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2653 (class 0 OID 115189)
-- Dependencies: 230
-- Data for Name: preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2654 (class 0 OID 115195)
-- Dependencies: 231
-- Data for Name: proposition; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2701 (class 0 OID 0)
-- Dependencies: 233
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('role_id_seq', 1080, true);


--
-- TOC entry 2655 (class 0 OID 115203)
-- Dependencies: 232
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO roles (id, name, description) VALUES (1, 'superAdmin', 'Super Administrateur pour tout gérer tout thésaurus et tout utilisateur');
INSERT INTO roles (id, name, description) VALUES (2, 'admin', 'administrateur par thésaurus ou plus');
INSERT INTO roles (id, name, description) VALUES (3, 'user', 'utilisateur par thésaurus ou plus');
INSERT INTO roles (id, name, description) VALUES (4, 'traducteur', 'traducteur par thésaurus ou plus');
INSERT INTO roles (id, name, description) VALUES (5, 'images', 'gestion des images par thésaurus ou plus');


--
-- TOC entry 2657 (class 0 OID 115211)
-- Dependencies: 234
-- Data for Name: split_non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2659 (class 0 OID 115216)
-- Dependencies: 236
-- Data for Name: term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2702 (class 0 OID 0)
-- Dependencies: 235
-- Name: term__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('term__id_seq', 1, false);


--
-- TOC entry 2661 (class 0 OID 115228)
-- Dependencies: 238
-- Data for Name: term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2703 (class 0 OID 0)
-- Dependencies: 237
-- Name: term_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('term_candidat__id_seq', 1, false);


--
-- TOC entry 2663 (class 0 OID 115239)
-- Dependencies: 240
-- Data for Name: term_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2704 (class 0 OID 0)
-- Dependencies: 239
-- Name: term_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('term_historique__id_seq', 1, false);


--
-- TOC entry 2665 (class 0 OID 115250)
-- Dependencies: 242
-- Data for Name: thesaurus; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2677 (class 0 OID 115522)
-- Dependencies: 254
-- Data for Name: thesaurus_alignement_source; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2666 (class 0 OID 115259)
-- Dependencies: 243
-- Data for Name: thesaurus_array; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2667 (class 0 OID 115267)
-- Dependencies: 244
-- Data for Name: thesaurus_array_concept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2705 (class 0 OID 0)
-- Dependencies: 241
-- Name: thesaurus_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('thesaurus_id_seq', 1, false);


--
-- TOC entry 2668 (class 0 OID 115274)
-- Dependencies: 245
-- Data for Name: thesaurus_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2706 (class 0 OID 0)
-- Dependencies: 246
-- Name: user__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('user__id_seq', 2, false);


--
-- TOC entry 2670 (class 0 OID 115284)
-- Dependencies: 247
-- Data for Name: user_role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO user_role (id_user, id_role, id_thesaurus, id_group) VALUES (1, 1, '', '');


--
-- TOC entry 2671 (class 0 OID 115290)
-- Dependencies: 248
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO users (id_user, username, password, active, mail, passtomodify) VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', true, 'admin@domaine.fr', false);


--
-- TOC entry 2672 (class 0 OID 115299)
-- Dependencies: 249
-- Data for Name: users2; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO users2 (id_user, login, fullname, password, active, mail, authentication) VALUES (8, 'miled', 'Miled Rousset', '', true, 'mie@mile', 'DB');
INSERT INTO users2 (id_user, login, fullname, password, active, mail, authentication) VALUES (9, 'toto', 'toeot', '', true, 'mil@mf', 'LDAP');


--
-- TOC entry 2676 (class 0 OID 115512)
-- Dependencies: 253
-- Data for Name: users_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO users_historique (id_user, username, created, modified, delete) VALUES (1, 'admin', '2017-01-06 09:56:51.167024+01', '2017-01-06 09:56:51.167024+01', NULL);


--
-- TOC entry 2673 (class 0 OID 115308)
-- Dependencies: 250
-- Data for Name: version_history; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 2484 (class 2606 OID 115316)
-- Name: version_history VersionHistory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY version_history
    ADD CONSTRAINT "VersionHistory_pkey" PRIMARY KEY ("idVersionhistory");


--
-- TOC entry 2369 (class 2606 OID 115320)
-- Name: alignement_source alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY alignement_source
    ADD CONSTRAINT alignement_source_pkey PRIMARY KEY (id);


--
-- TOC entry 2371 (class 2606 OID 115531)
-- Name: alignement_source alignement_source_source_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY alignement_source
    ADD CONSTRAINT alignement_source_source_key UNIQUE (source);


--
-- TOC entry 2367 (class 2606 OID 115542)
-- Name: alignement alignement_uri_target_internal_id_thesaurus_internal_id_con_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY alignement
    ADD CONSTRAINT alignement_uri_target_internal_id_thesaurus_internal_id_con_key UNIQUE (uri_target, internal_id_thesaurus, internal_id_concept);


--
-- TOC entry 2373 (class 2606 OID 115322)
-- Name: alignement_type alignment_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY alignement_type
    ADD CONSTRAINT alignment_type_pkey PRIMARY KEY (id);


--
-- TOC entry 2375 (class 2606 OID 115324)
-- Name: compound_equivalence compound_equivalence_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY compound_equivalence
    ADD CONSTRAINT compound_equivalence_pkey PRIMARY KEY (id_split_nonpreferredterm, id_preferredterm);


--
-- TOC entry 2379 (class 2606 OID 115326)
-- Name: concept_candidat concept_candidat_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_candidat
    ADD CONSTRAINT concept_candidat_id_key UNIQUE (id);


--
-- TOC entry 2381 (class 2606 OID 115328)
-- Name: concept_candidat concept_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_candidat
    ADD CONSTRAINT concept_candidat_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 2401 (class 2606 OID 115330)
-- Name: concept_historique concept_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_historique
    ADD CONSTRAINT concept_copy_pkey PRIMARY KEY (id_concept, id_thesaurus, id_group, id_user, modified);


--
-- TOC entry 2383 (class 2606 OID 115332)
-- Name: concept_fusion concept_fusion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_fusion
    ADD CONSTRAINT concept_fusion_pkey PRIMARY KEY (id_concept1, id_concept2, id_thesaurus);


--
-- TOC entry 2387 (class 2606 OID 115334)
-- Name: concept_group_concept concept_group_concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_group_concept
    ADD CONSTRAINT concept_group_concept_pkey PRIMARY KEY (idgroup, idthesaurus, idconcept);


--
-- TOC entry 2389 (class 2606 OID 115336)
-- Name: concept_group_historique concept_group_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_group_historique
    ADD CONSTRAINT concept_group_copy_pkey PRIMARY KEY (idgroup, idthesaurus, modified, id_user);


--
-- TOC entry 2395 (class 2606 OID 115338)
-- Name: concept_group_label_historique concept_group_label_copy_idgrouplabel_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_group_label_historique
    ADD CONSTRAINT concept_group_label_copy_idgrouplabel_key UNIQUE (id);


--
-- TOC entry 2397 (class 2606 OID 115340)
-- Name: concept_group_label_historique concept_group_label_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_group_label_historique
    ADD CONSTRAINT concept_group_label_copy_pkey PRIMARY KEY (lang, idthesaurus, lexicalvalue, modified, id_user);


--
-- TOC entry 2391 (class 2606 OID 115342)
-- Name: concept_group_label concept_group_label_idgrouplabel_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_group_label
    ADD CONSTRAINT concept_group_label_idgrouplabel_key UNIQUE (id);


--
-- TOC entry 2393 (class 2606 OID 115344)
-- Name: concept_group_label concept_group_label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_group_label
    ADD CONSTRAINT concept_group_label_pkey PRIMARY KEY (lang, idthesaurus, lexicalvalue);


--
-- TOC entry 2385 (class 2606 OID 115346)
-- Name: concept_group concept_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_group
    ADD CONSTRAINT concept_group_pkey PRIMARY KEY (idgroup, idthesaurus);


--
-- TOC entry 2399 (class 2606 OID 115348)
-- Name: concept_group_type concept_group_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_group_type
    ADD CONSTRAINT concept_group_type_pkey PRIMARY KEY (code, label);


--
-- TOC entry 2403 (class 2606 OID 115350)
-- Name: concept_orphan concept_orphan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_orphan
    ADD CONSTRAINT concept_orphan_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 2377 (class 2606 OID 115352)
-- Name: concept concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept
    ADD CONSTRAINT concept_pkey PRIMARY KEY (id_concept, id_thesaurus, id_group);


--
-- TOC entry 2405 (class 2606 OID 115354)
-- Name: concept_term_candidat concept_term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY concept_term_candidat
    ADD CONSTRAINT concept_term_candidat_pkey PRIMARY KEY (id_concept, id_term, id_thesaurus);


--
-- TOC entry 2407 (class 2606 OID 115356)
-- Name: custom_concept_attribute custom_concept_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY custom_concept_attribute
    ADD CONSTRAINT custom_concept_attribute_pkey PRIMARY KEY ("idConcept");


--
-- TOC entry 2409 (class 2606 OID 115358)
-- Name: custom_term_attribute custom_term_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY custom_term_attribute
    ADD CONSTRAINT custom_term_attribute_pkey PRIMARY KEY (identifier);


--
-- TOC entry 2486 (class 2606 OID 115488)
-- Name: gps gps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY gps
    ADD CONSTRAINT gps_pkey PRIMARY KEY (id_concept);


--
-- TOC entry 2413 (class 2606 OID 115360)
-- Name: hierarchical_relationship_historique hierarchical_relationship_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY hierarchical_relationship_historique
    ADD CONSTRAINT hierarchical_relationship_copy_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2, modified, id_user);


--
-- TOC entry 2411 (class 2606 OID 115362)
-- Name: hierarchical_relationship hierarchical_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY hierarchical_relationship
    ADD CONSTRAINT hierarchical_relationship_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2);


--
-- TOC entry 2415 (class 2606 OID 115364)
-- Name: images images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY images
    ADD CONSTRAINT images_pkey PRIMARY KEY (id_concept, id_thesaurus, image_name);


--
-- TOC entry 2417 (class 2606 OID 115366)
-- Name: languages_iso639 languages_iso639_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY languages_iso639
    ADD CONSTRAINT languages_iso639_pkey PRIMARY KEY (id);


--
-- TOC entry 2419 (class 2606 OID 115368)
-- Name: node_label node_label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY node_label
    ADD CONSTRAINT node_label_pkey PRIMARY KEY (facet_id, id_thesaurus, lang);


--
-- TOC entry 2421 (class 2606 OID 115370)
-- Name: non_preferred_term non_prefered_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY non_preferred_term
    ADD CONSTRAINT non_prefered_term_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 2423 (class 2606 OID 115372)
-- Name: non_preferred_term_historique non_preferred_term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY non_preferred_term_historique
    ADD CONSTRAINT non_preferred_term_copy_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, modified, id_user);


--
-- TOC entry 2431 (class 2606 OID 115374)
-- Name: note_historique note_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY note_historique
    ADD CONSTRAINT note_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 2425 (class 2606 OID 115472)
-- Name: note note_notetypecode_id_thesaurus_id_concept_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY note
    ADD CONSTRAINT note_notetypecode_id_thesaurus_id_concept_lang_key UNIQUE (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue);


--
-- TOC entry 2427 (class 2606 OID 115474)
-- Name: note note_notetypecode_id_thesaurus_id_term_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY note
    ADD CONSTRAINT note_notetypecode_id_thesaurus_id_term_lang_key UNIQUE (notetypecode, id_thesaurus, id_term, lang, lexicalvalue);


--
-- TOC entry 2429 (class 2606 OID 115380)
-- Name: note note_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY note
    ADD CONSTRAINT note_pkey PRIMARY KEY (id);


--
-- TOC entry 2436 (class 2606 OID 115382)
-- Name: permuted permuted_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY permuted
    ADD CONSTRAINT permuted_pkey PRIMARY KEY (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm);


--
-- TOC entry 2433 (class 2606 OID 115384)
-- Name: note_type pk_note_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY note_type
    ADD CONSTRAINT pk_note_type PRIMARY KEY (code);


--
-- TOC entry 2438 (class 2606 OID 115386)
-- Name: preferences preferences_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY preferences
    ADD CONSTRAINT preferences_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 2440 (class 2606 OID 115388)
-- Name: preferences preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY preferences
    ADD CONSTRAINT preferences_pkey PRIMARY KEY (id_pref);


--
-- TOC entry 2442 (class 2606 OID 115390)
-- Name: preferred_term preferred_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY preferred_term
    ADD CONSTRAINT preferred_term_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 2444 (class 2606 OID 115392)
-- Name: proposition proposition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY proposition
    ADD CONSTRAINT proposition_pkey PRIMARY KEY (id_concept, id_user, id_thesaurus);


--
-- TOC entry 2446 (class 2606 OID 115394)
-- Name: roles role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY roles
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- TOC entry 2455 (class 2606 OID 115396)
-- Name: term_candidat term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY term_candidat
    ADD CONSTRAINT term_candidat_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, contributor);


--
-- TOC entry 2458 (class 2606 OID 115398)
-- Name: term_historique term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY term_historique
    ADD CONSTRAINT term_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 2449 (class 2606 OID 115400)
-- Name: term term_id_term_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY term
    ADD CONSTRAINT term_id_term_key UNIQUE (id_term, lang, id_thesaurus);


--
-- TOC entry 2451 (class 2606 OID 115402)
-- Name: term term_id_term_lexical_value_lang_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY term
    ADD CONSTRAINT term_id_term_lexical_value_lang_id_thesaurus_key UNIQUE (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 2453 (class 2606 OID 115404)
-- Name: term term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY term
    ADD CONSTRAINT term_pkey PRIMARY KEY (id);


--
-- TOC entry 2490 (class 2606 OID 115529)
-- Name: thesaurus_alignement_source thesaurus_alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY thesaurus_alignement_source
    ADD CONSTRAINT thesaurus_alignement_source_pkey PRIMARY KEY (id_thesaurus, id_alignement_source);


--
-- TOC entry 2464 (class 2606 OID 115406)
-- Name: thesaurus_array_concept thesaurus_array_concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY thesaurus_array_concept
    ADD CONSTRAINT thesaurus_array_concept_pkey PRIMARY KEY (thesaurusarrayid, id_concept, id_thesaurus);


--
-- TOC entry 2462 (class 2606 OID 115408)
-- Name: thesaurus_array thesaurus_array_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY thesaurus_array
    ADD CONSTRAINT thesaurus_array_pkey PRIMARY KEY (facet_id, id_thesaurus, id_concept_parent);


--
-- TOC entry 2466 (class 2606 OID 115410)
-- Name: thesaurus_label thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY thesaurus_label
    ADD CONSTRAINT thesaurus_pkey PRIMARY KEY (id_thesaurus, lang, title);


--
-- TOC entry 2460 (class 2606 OID 115412)
-- Name: thesaurus thesaurus_pkey1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY thesaurus
    ADD CONSTRAINT thesaurus_pkey1 PRIMARY KEY (id_thesaurus, id_ark);


--
-- TOC entry 2468 (class 2606 OID 115414)
-- Name: thesaurus_label unique_thesau_lang; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY thesaurus_label
    ADD CONSTRAINT unique_thesau_lang UNIQUE (id_thesaurus, lang);


--
-- TOC entry 2472 (class 2606 OID 115416)
-- Name: users user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT user_pkey PRIMARY KEY (id_user);


--
-- TOC entry 2470 (class 2606 OID 115418)
-- Name: user_role user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY user_role
    ADD CONSTRAINT user_role_pkey PRIMARY KEY (id_user, id_role, id_thesaurus);


--
-- TOC entry 2488 (class 2606 OID 115521)
-- Name: users_historique users_historique_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users_historique
    ADD CONSTRAINT users_historique_pkey PRIMARY KEY (id_user);


--
-- TOC entry 2478 (class 2606 OID 115420)
-- Name: users2 users_login_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users2
    ADD CONSTRAINT users_login_key UNIQUE (login);


--
-- TOC entry 2480 (class 2606 OID 115422)
-- Name: users2 users_mail_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users2
    ADD CONSTRAINT users_mail_key UNIQUE (mail);


--
-- TOC entry 2474 (class 2606 OID 115424)
-- Name: users users_mail_key1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_mail_key1 UNIQUE (mail);


--
-- TOC entry 2482 (class 2606 OID 115426)
-- Name: users2 users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users2
    ADD CONSTRAINT users_pkey PRIMARY KEY (id_user);


--
-- TOC entry 2476 (class 2606 OID 115428)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 2447 (class 1259 OID 115429)
-- Name: index_lexical_value; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value ON term USING btree (lexical_value);


--
-- TOC entry 2456 (class 1259 OID 115430)
-- Name: index_lexical_value_copy; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value_copy ON term_historique USING btree (lexical_value);


--
-- TOC entry 2434 (class 1259 OID 115431)
-- Name: permuted_lexical_value_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX permuted_lexical_value_idx ON permuted USING btree (lexical_value);


-- Completed on 2017-01-06 10:05:53

--
-- PostgreSQL database dump complete
--

