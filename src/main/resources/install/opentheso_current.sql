--
-- PostgreSQL database dump
--

-- Dumped from database version 11.5
-- Dumped by pg_dump version 13.0

-- Started on 2021-02-03 11:24:19 CET


SET role = opentheso;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 3 (class 3079 OID 16395)
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--


--
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-- !!!!!!!!!!!!!!!!! Important !!!!!!!!!!!!!!!!!!!
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-- pour créer ces extensions, il faut avoir des privilèves Superuser sur Postgres
--
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;
COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';

CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;
COMMENT ON EXTENSION unaccent IS 'text search dictionary that removes accents';
--
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-- !!!!!!!!!!!!!!!!! Fin !!!!!!!!!!!!!!!!!!!
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!



--
-- TOC entry 724 (class 1247 OID 16480)
-- Name: alignement_format; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.alignement_format AS ENUM (
    'skos',
    'json',
    'xml'
);


--
-- TOC entry 727 (class 1247 OID 16488)
-- Name: alignement_type_rqt; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.alignement_type_rqt AS ENUM (
    'SPARQL',
    'REST'
);


--
-- TOC entry 730 (class 1247 OID 16494)
-- Name: auth_method; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.auth_method AS ENUM (
    'DB',
    'LDAP',
    'FILE',
    'test'
);


--
-- TOC entry 339 (class 1255 OID 16503)
-- Name: f_unaccent(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.f_unaccent(text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
SELECT public.unaccent('public.unaccent', $1)
$_$;


--
-- TOC entry 340 (class 1255 OID 91767)
-- Name: unaccent_string(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.unaccent_string(text) RETURNS text
    LANGUAGE plpgsql
    AS $_$
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
$_$;


--
-- TOC entry 198 (class 1259 OID 16505)
-- Name: alignement_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alignement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

--
-- TOC entry 199 (class 1259 OID 16507)
-- Name: alignement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alignement (
    id integer DEFAULT nextval('public.alignement_id_seq'::regclass) NOT NULL,
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
-- TOC entry 200 (class 1259 OID 16516)
-- Name: alignement_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alignement_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 201 (class 1259 OID 16518)
-- Name: alignement_preferences; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alignement_preferences (
    id integer DEFAULT nextval('public.alignement_preferences_id_seq'::regclass) NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_user integer NOT NULL,
    id_concept_depart character varying NOT NULL,
    id_concept_tratees character varying,
    id_alignement_source integer NOT NULL
);


--
-- TOC entry 202 (class 1259 OID 16525)
-- Name: alignement_source__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alignement_source__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 203 (class 1259 OID 16527)
-- Name: alignement_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alignement_source (
    source character varying,
    requete character varying,
    type_rqt public.alignement_type_rqt NOT NULL,
    alignement_format public.alignement_format NOT NULL,
    id integer DEFAULT nextval('public.alignement_source__id_seq'::regclass) NOT NULL,
    id_user integer,
    description character varying,
    gps boolean DEFAULT false,
    source_filter character varying DEFAULT 'Opentheso'::character varying NOT NULL
);


--
-- TOC entry 204 (class 1259 OID 16535)
-- Name: alignement_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alignement_type (
    id integer NOT NULL,
    label text NOT NULL,
    isocode text NOT NULL,
    label_skos character varying
);


--
-- TOC entry 205 (class 1259 OID 16541)
-- Name: bt_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bt_type (
    id integer NOT NULL,
    relation character varying,
    description_fr character varying,
    description_en character varying
);


--
-- TOC entry 280 (class 1259 OID 69545)
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidat_messages_id_message_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 281 (class 1259 OID 69547)
-- Name: candidat_messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidat_messages (
    id_message integer DEFAULT nextval('public.candidat_messages_id_message_seq'::regclass) NOT NULL,
    value text NOT NULL,
    id_user integer,
    id_concept integer,
    id_thesaurus character varying,
    date text
);


--
-- TOC entry 282 (class 1259 OID 90025)
-- Name: candidat_status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidat_status (
    id_concept character varying NOT NULL,
    id_status integer,
    date date DEFAULT now() NOT NULL,
    id_user integer,
    id_thesaurus character varying,
    message text,
    id_user_admin integer
);


--
-- TOC entry 284 (class 1259 OID 90528)
-- Name: candidat_vote; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidat_vote (
    id_vote integer NOT NULL,
    id_user integer,
    id_concept character varying,
    id_thesaurus character varying,
    type_vote character varying(30),
    id_note character varying(30)
);


--
-- TOC entry 283 (class 1259 OID 90526)
-- Name: candidat_vote_id_vote_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidat_vote_id_vote_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3981 (class 0 OID 0)
-- Dependencies: 283
-- Name: candidat_vote_id_vote_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidat_vote_id_vote_seq OWNED BY public.candidat_vote.id_vote;


--
-- TOC entry 206 (class 1259 OID 16547)
-- Name: compound_equivalence; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.compound_equivalence (
    id_split_nonpreferredterm text NOT NULL,
    id_preferredterm text NOT NULL
);


--
-- TOC entry 207 (class 1259 OID 16553)
-- Name: concept__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept__id_seq
    START WITH 43
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 208 (class 1259 OID 16555)
-- Name: concept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_ark character varying DEFAULT ''::character varying,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    status character varying,
    notation character varying DEFAULT ''::character varying,
    top_concept boolean,
    id integer DEFAULT nextval('public.concept__id_seq'::regclass),
    gps boolean DEFAULT false,
    id_handle character varying DEFAULT ''::character varying,
    id_doi character varying DEFAULT ''::character varying
);


--
-- TOC entry 209 (class 1259 OID 16568)
-- Name: concept_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 210 (class 1259 OID 16570)
-- Name: concept_candidat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_candidat (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    status character varying DEFAULT 'a'::character varying,
    id integer DEFAULT nextval('public.concept_candidat__id_seq'::regclass),
    admin_message character varying,
    admin_id integer
);


--
-- TOC entry 292 (class 1259 OID 115737)
-- Name: concept_facet; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_facet (
    id_facet character varying NOT NULL,
    id_thesaurus text NOT NULL,
    id_concept text NOT NULL
);


--
-- TOC entry 212 (class 1259 OID 16587)
-- Name: concept_group__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 213 (class 1259 OID 16589)
-- Name: concept_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group (
    idgroup text NOT NULL,
    id_ark text NOT NULL,
    idthesaurus text NOT NULL,
    idtypecode text DEFAULT 'MT'::text NOT NULL,
    notation text,
    id integer DEFAULT nextval('public.concept_group__id_seq'::regclass) NOT NULL,
    numerotation integer,
    id_handle character varying DEFAULT ''::character varying,
    id_doi character varying DEFAULT ''::character varying
);


--
-- TOC entry 214 (class 1259 OID 16598)
-- Name: concept_group_concept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group_concept (
    idgroup text NOT NULL,
    idthesaurus text NOT NULL,
    idconcept text NOT NULL
);


--
-- TOC entry 215 (class 1259 OID 16604)
-- Name: concept_group_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 216 (class 1259 OID 16606)
-- Name: concept_group_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group_historique (
    idgroup text NOT NULL,
    id_ark text NOT NULL,
    idthesaurus text NOT NULL,
    idtypecode text NOT NULL,
    idparentgroup text,
    notation text,
    idconcept text,
    id integer DEFAULT nextval('public.concept_group_historique__id_seq'::regclass) NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL
);


--
-- TOC entry 217 (class 1259 OID 16614)
-- Name: concept_group_label_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group_label_id_seq
    START WITH 60
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 218 (class 1259 OID 16616)
-- Name: concept_group_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group_label (
    id integer DEFAULT nextval('public.concept_group_label_id_seq'::regclass) NOT NULL,
    lexicalvalue text NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    lang character varying(5) NOT NULL,
    idthesaurus text NOT NULL,
    idgroup text NOT NULL
);


--
-- TOC entry 219 (class 1259 OID 16625)
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group_label_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 220 (class 1259 OID 16627)
-- Name: concept_group_label_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group_label_historique (
    id integer DEFAULT nextval('public.concept_group_label_historique__id_seq'::regclass) NOT NULL,
    lexicalvalue text NOT NULL,
    modified timestamp(6) without time zone DEFAULT now() NOT NULL,
    lang character varying(5) NOT NULL,
    idthesaurus text NOT NULL,
    idgroup text NOT NULL,
    id_user integer NOT NULL
);


--
-- TOC entry 221 (class 1259 OID 16635)
-- Name: concept_group_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group_type (
    code text NOT NULL,
    label text NOT NULL,
    skoslabel text
);


--
-- TOC entry 222 (class 1259 OID 16641)
-- Name: concept_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 223 (class 1259 OID 16643)
-- Name: concept_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_historique (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_ark character varying,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    status character varying,
    notation character varying DEFAULT ''::character varying,
    top_concept boolean,
    id_group character varying NOT NULL,
    id integer DEFAULT nextval('public.concept_historique__id_seq'::regclass),
    id_user integer NOT NULL
);


--
-- TOC entry 211 (class 1259 OID 16580)
-- Name: concept_replacedby; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_replacedby (
    id_concept1 character varying NOT NULL,
    id_concept2 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL
);


--
-- TOC entry 224 (class 1259 OID 16658)
-- Name: concept_term_candidat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_term_candidat (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 225 (class 1259 OID 16664)
-- Name: copyright; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.copyright (
    id_thesaurus character varying NOT NULL,
    copyright character varying
);


--
-- TOC entry 278 (class 1259 OID 69360)
-- Name: corpus_link; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.corpus_link (
    id_theso character varying NOT NULL,
    corpus_name character varying NOT NULL,
    uri_count character varying,
    uri_link character varying NOT NULL,
    active boolean DEFAULT false
);


--
-- TOC entry 226 (class 1259 OID 16670)
-- Name: custom_concept_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custom_concept_attribute (
    "idConcept" character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 227 (class 1259 OID 16676)
-- Name: custom_term_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custom_term_attribute (
    identifier character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 228 (class 1259 OID 16682)
-- Name: external_images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.external_images (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    image_name character varying NOT NULL,
    image_copyright character varying NOT NULL,
    id_user integer,
    external_uri character varying DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 229 (class 1259 OID 16689)
-- Name: facet_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.facet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 230 (class 1259 OID 16691)
-- Name: gps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gps (
    id_concept character varying NOT NULL,
    id_theso character varying NOT NULL,
    latitude double precision,
    longitude double precision
);


--
-- TOC entry 231 (class 1259 OID 16697)
-- Name: gps_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gps_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 232 (class 1259 OID 16699)
-- Name: gps_preferences; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gps_preferences (
    id integer DEFAULT nextval('public.gps_preferences_id_seq'::regclass) NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_user integer NOT NULL,
    gps_integrertraduction boolean DEFAULT true,
    gps_reemplacertraduction boolean DEFAULT true,
    gps_alignementautomatique boolean DEFAULT true,
    id_alignement_source integer NOT NULL
);


--
-- TOC entry 233 (class 1259 OID 16709)
-- Name: hierarchical_relationship; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hierarchical_relationship (
    id_concept1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    role character varying NOT NULL,
    id_concept2 character varying NOT NULL
);


--
-- TOC entry 234 (class 1259 OID 16715)
-- Name: hierarchical_relationship_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hierarchical_relationship_historique (
    id_concept1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    role character varying NOT NULL,
    id_concept2 character varying NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL,
    action character varying NOT NULL
);


--
-- TOC entry 276 (class 1259 OID 40590)
-- Name: homepage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.homepage (
    htmlcode character varying,
    lang character varying
);


--
-- TOC entry 235 (class 1259 OID 16722)
-- Name: images; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.images (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    image_name character varying NOT NULL,
    image_copyright character varying NOT NULL,
    id_user integer,
    external_uri character varying DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 236 (class 1259 OID 16729)
-- Name: info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.info (
    version_opentheso character varying NOT NULL,
    version_bdd character varying NOT NULL,
    googleanalytics character varying
);


--
-- TOC entry 237 (class 1259 OID 16735)
-- Name: languages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.languages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 288 (class 1259 OID 91656)
-- Name: languages_iso639; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.languages_iso639 (
    iso639_1 character(3),
    iso639_2 character varying,
    english_name character varying,
    french_name character varying,
    id integer DEFAULT nextval('public.languages_id_seq'::regclass) NOT NULL
);


--
-- TOC entry 290 (class 1259 OID 115654)
-- Name: thesaurus_array_facet_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.thesaurus_array_facet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 289 (class 1259 OID 115617)
-- Name: node_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.node_label (
    id_thesaurus character varying NOT NULL,
    lexical_value character varying,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    lang character varying NOT NULL,
    id integer DEFAULT nextval('public.thesaurus_array_facet_id_seq'::regclass) NOT NULL,
    id_facet character varying NOT NULL
);


--
-- TOC entry 238 (class 1259 OID 16752)
-- Name: non_preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.non_preferred_term (
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
-- TOC entry 239 (class 1259 OID 16761)
-- Name: non_preferred_term_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.non_preferred_term_historique (
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
-- TOC entry 240 (class 1259 OID 16769)
-- Name: note__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 241 (class 1259 OID 16771)
-- Name: note; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.note (
    id integer DEFAULT nextval('public.note__id_seq'::regclass) NOT NULL,
    notetypecode text NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_term character varying,
    id_concept character varying,
    lang character varying NOT NULL,
    lexicalvalue character varying NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    id_user integer
);


--
-- TOC entry 242 (class 1259 OID 16780)
-- Name: note_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 243 (class 1259 OID 16782)
-- Name: note_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.note_historique (
    id integer DEFAULT nextval('public.note_historique__id_seq'::regclass) NOT NULL,
    notetypecode text NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_term character varying,
    id_concept character varying,
    lang character varying NOT NULL,
    lexicalvalue character varying NOT NULL,
    modified timestamp(6) without time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL,
    action_performed character varying
);


--
-- TOC entry 285 (class 1259 OID 91636)
-- Name: note_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.note_type (
    code text NOT NULL,
    isterm boolean NOT NULL,
    isconcept boolean NOT NULL,
    label_fr character varying,
    label_en character varying,
    CONSTRAINT chk_not_false_values CHECK ((NOT ((isterm = false) AND (isconcept = false))))
);


--
-- TOC entry 244 (class 1259 OID 16797)
-- Name: nt_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.nt_type (
    id integer NOT NULL,
    relation character varying,
    description_fr character varying,
    description_en character varying
);


--
-- TOC entry 245 (class 1259 OID 16803)
-- Name: permuted; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.permuted (
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
-- TOC entry 246 (class 1259 OID 16809)
-- Name: pref__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pref__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 247 (class 1259 OID 16811)
-- Name: preferences; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.preferences (
    id_pref integer DEFAULT nextval('public.pref__id_seq'::regclass) NOT NULL,
    id_thesaurus character varying NOT NULL,
    source_lang character varying(2) DEFAULT 'fr'::character varying,
    identifier_type integer DEFAULT 2,
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
    use_ark boolean DEFAULT false,
    server_ark character varying DEFAULT 'http://ark.mondomaine.fr/ark:/'::character varying,
    id_naan character varying DEFAULT '66666'::character varying NOT NULL,
    prefix_ark character varying DEFAULT 'crt'::character varying NOT NULL,
    user_ark character varying,
    pass_ark character varying,
    use_handle boolean DEFAULT false,
    user_handle character varying,
    pass_handle character varying,
    path_key_handle character varying DEFAULT '/certificat/key.p12'::character varying,
    path_cert_handle character varying DEFAULT '/certificat/cacerts2'::character varying,
    url_api_handle character varying DEFAULT 'https://handle-server.mondomaine.fr:8001/api/handles/'::character varying NOT NULL,
    prefix_handle character varying DEFAULT '66.666.66666'::character varying NOT NULL,
    private_prefix_handle character varying DEFAULT 'crt'::character varying NOT NULL,
    preferredname character varying,
    original_uri character varying,
    original_uri_is_ark boolean DEFAULT false,
    original_uri_is_handle boolean DEFAULT false,
    uri_ark character varying DEFAULT 'https://ark.mom.fr/ark:/'::character varying,
    generate_handle boolean DEFAULT true,
    auto_expand_tree boolean DEFAULT true,
    sort_by_notation boolean DEFAULT false,
    tree_cache boolean DEFAULT false,
    original_uri_is_doi boolean DEFAULT false
);


--
-- TOC entry 248 (class 1259 OID 16844)
-- Name: preferences_sparql; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.preferences_sparql (
    adresse_serveur character varying,
    mot_de_passe character varying,
    nom_d_utilisateur character varying,
    graph character varying,
    synchronisation boolean DEFAULT false NOT NULL,
    thesaurus character varying NOT NULL,
    heure time without time zone
);


--
-- TOC entry 249 (class 1259 OID 16851)
-- Name: preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.preferred_term (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 250 (class 1259 OID 16857)
-- Name: proposition; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.proposition (
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
-- TOC entry 251 (class 1259 OID 16865)
-- Name: relation_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.relation_group (
    id_group1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    relation character varying NOT NULL,
    id_group2 character varying NOT NULL
);


--
-- TOC entry 252 (class 1259 OID 16871)
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying,
    description character varying
);


--
-- TOC entry 253 (class 1259 OID 16877)
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3982 (class 0 OID 0)
-- Dependencies: 253
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.role_id_seq OWNED BY public.roles.id;


--
-- TOC entry 254 (class 1259 OID 16879)
-- Name: routine_mail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.routine_mail (
    id_thesaurus character varying NOT NULL,
    alert_cdt boolean DEFAULT true,
    debut_env_cdt_propos date NOT NULL,
    debut_env_cdt_valid date NOT NULL,
    period_env_cdt_propos integer NOT NULL,
    period_env_cdt_valid integer NOT NULL
);


--
-- TOC entry 255 (class 1259 OID 16886)
-- Name: split_non_preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.split_non_preferred_term (
);


--
-- TOC entry 287 (class 1259 OID 91647)
-- Name: status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.status (
    id_status integer NOT NULL,
    value text
);


--
-- TOC entry 279 (class 1259 OID 69543)
-- Name: status_id_status_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.status_id_status_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 286 (class 1259 OID 91645)
-- Name: status_id_status_seq1; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.status_id_status_seq1
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3983 (class 0 OID 0)
-- Dependencies: 286
-- Name: status_id_status_seq1; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.status_id_status_seq1 OWNED BY public.status.id_status;


--
-- TOC entry 256 (class 1259 OID 16889)
-- Name: term__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 257 (class 1259 OID 16891)
-- Name: term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.term (
    id_term character varying NOT NULL,
    lexical_value character varying NOT NULL,
    lang character varying NOT NULL,
    id_thesaurus text NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    source character varying,
    status character varying DEFAULT 'D'::character varying,
    id integer DEFAULT nextval('public.term__id_seq'::regclass) NOT NULL,
    contributor integer,
    creator integer
);


--
-- TOC entry 258 (class 1259 OID 16901)
-- Name: term_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 259 (class 1259 OID 16903)
-- Name: term_candidat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.term_candidat (
    id_term character varying NOT NULL,
    lexical_value character varying NOT NULL,
    lang character varying NOT NULL,
    id_thesaurus text NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    contributor integer NOT NULL,
    id integer DEFAULT nextval('public.term_candidat__id_seq'::regclass) NOT NULL
);


--
-- TOC entry 260 (class 1259 OID 16912)
-- Name: term_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 261 (class 1259 OID 16914)
-- Name: term_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.term_historique (
    id_term character varying NOT NULL,
    lexical_value character varying NOT NULL,
    lang character varying NOT NULL,
    id_thesaurus text NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    source character varying,
    status character varying DEFAULT 'D'::character varying,
    id integer DEFAULT nextval('public.term_historique__id_seq'::regclass) NOT NULL,
    id_user integer NOT NULL,
    action character varying
);


--
-- TOC entry 262 (class 1259 OID 16923)
-- Name: thesaurus_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.thesaurus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 263 (class 1259 OID 16925)
-- Name: thesaurus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus (
    id_thesaurus character varying NOT NULL,
    id_ark character varying NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    id integer DEFAULT nextval('public.thesaurus_id_seq'::regclass) NOT NULL,
    private boolean DEFAULT false
);


--
-- TOC entry 264 (class 1259 OID 16935)
-- Name: thesaurus_alignement_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus_alignement_source (
    id_thesaurus character varying NOT NULL,
    id_alignement_source integer NOT NULL
);


--
-- TOC entry 291 (class 1259 OID 115718)
-- Name: thesaurus_array; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus_array (
    id_thesaurus character varying NOT NULL,
    id_concept_parent character varying NOT NULL,
    ordered boolean DEFAULT false NOT NULL,
    notation character varying,
    id_facet character varying NOT NULL
);


--
-- TOC entry 265 (class 1259 OID 16956)
-- Name: thesaurus_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus_label (
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
-- TOC entry 277 (class 1259 OID 40596)
-- Name: thesohomepage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesohomepage (
    htmlcode character varying,
    lang character varying,
    idtheso character varying
);


--
-- TOC entry 266 (class 1259 OID 16964)
-- Name: user__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 267 (class 1259 OID 16966)
-- Name: user_group_label__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_group_label__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 268 (class 1259 OID 16968)
-- Name: user_group_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_group_label (
    id_group integer DEFAULT nextval('public.user_group_label__id_seq'::regclass) NOT NULL,
    label_group character varying
);


--
-- TOC entry 269 (class 1259 OID 16975)
-- Name: user_group_thesaurus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_group_thesaurus (
    id_group integer NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 270 (class 1259 OID 16981)
-- Name: user_role_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role_group (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_group integer NOT NULL
);


--
-- TOC entry 271 (class 1259 OID 16984)
-- Name: user_role_only_on; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role_only_on (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_theso character varying NOT NULL,
    id_theso_domain character varying DEFAULT 'all'::character varying NOT NULL
);


--
-- TOC entry 272 (class 1259 OID 16991)
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id_user integer DEFAULT nextval('public.user__id_seq'::regclass) NOT NULL,
    username character varying NOT NULL,
    password character varying NOT NULL,
    active boolean DEFAULT true NOT NULL,
    mail character varying NOT NULL,
    passtomodify boolean DEFAULT false,
    alertmail boolean DEFAULT false,
    issuperadmin boolean DEFAULT false
);


--
-- TOC entry 273 (class 1259 OID 17002)
-- Name: users2; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users2 (
    id_user integer DEFAULT nextval('public.user__id_seq'::regclass) NOT NULL,
    login character varying NOT NULL,
    fullname character varying,
    password character varying,
    active boolean DEFAULT true NOT NULL,
    mail character varying,
    authentication public.auth_method DEFAULT 'DB'::public.auth_method
);


--
-- TOC entry 274 (class 1259 OID 17011)
-- Name: users_historique; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users_historique (
    id_user integer NOT NULL,
    username character varying,
    created timestamp(6) with time zone DEFAULT now() NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    delete timestamp(6) with time zone
);


--
-- TOC entry 275 (class 1259 OID 17019)
-- Name: version_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.version_history (
    "idVersionhistory" character varying NOT NULL,
    "idThesaurus" character varying NOT NULL,
    date date,
    "versionNote" character varying,
    "currentVersion" boolean,
    "thisVersion" boolean NOT NULL
);


--
-- TOC entry 3573 (class 2604 OID 90531)
-- Name: candidat_vote id_vote; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_vote ALTER COLUMN id_vote SET DEFAULT nextval('public.candidat_vote_id_vote_seq'::regclass);


--
-- TOC entry 3540 (class 2604 OID 91766)
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);


--
-- TOC entry 3575 (class 2604 OID 91650)
-- Name: status id_status; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status ALTER COLUMN id_status SET DEFAULT nextval('public.status_id_status_seq1'::regclass);


--
-- TOC entry 3880 (class 0 OID 16507)
-- Dependencies: 199
-- Data for Name: alignement; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3882 (class 0 OID 16518)
-- Dependencies: 201
-- Data for Name: alignement_preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3884 (class 0 OID 16527)
-- Dependencies: 203
-- Data for Name: alignement_source; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefSujets', 'https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:(##value##)%20AND%20recordtype_z:r', 'REST', 'json', 184, 1, 'alignement avec les Sujets de IdRef ABES Rameaux', false, 'IdRefSujets');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefAuteurs', 'https://www.idref.fr/Sru/Solr?wt=json&q=nom_t:(##nom##)%20AND%20prenom_t:(##prenom##)%20AND%20recordtype_z:a&fl=ppn_z,affcourt_z,prenom_s,nom_s&start=0&rows=30&version=2.2', 'REST', 'json', 185, 1, 'alignement avec les Auteurs de IdRef ABES', false, 'IdRefAuteurs');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('local_culture', 'http://localhost:8082/opentheso2/api/search?q=##value##&lang=##lang##&theso=th2', 'REST', 'skos', 39, 1, 'Opentheso', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('local_sarah', 'http://localhost:8082/opentheso2/api/search?q=##value##&lang=##lang##&theso=th1', 'REST', 'skos', 41, 1, 'Opentheso', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefPersonnes', 'https://www.idref.fr/Sru/Solr?wt=json&q=persname_t:(##value##)&fl=ppn_z,affcourt_z,prenom_s,nom_s&start=0&rows=30&version=2.2', 'REST', 'json', 186, 1, 'alignement avec les Noms de personnes de IdRef ABES', false, 'IdRefPersonnes');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefTitreUniforme', 'https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=uniformtitle_t:(##value##)%20AND%20recordtype_z:f', 'REST', 'json', 187, 1, 'alignement avec les titres uniformes de IdRef ABES', false, 'IdRefTitreUniforme');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Wikidata', 'SELECT ?item ?itemLabel ?itemDescription WHERE {
                    ?item rdfs:label "##value##"@##lang##.
                    SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],##lang##". }
                    }', 'SPARQL', 'json', 188, 1, 'alignement avec le thésaurus de wikidata', false, 'Wikidata');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Getty_AAT', 'http://vocabsservices.getty.edu/AATService.asmx/AATGetTermMatch?term=##value##&logop=and&notes=', 'REST', 'xml', 189, 1, 'alignement avec le thésaurus du Getty AAT', false, 'Getty_AAT');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('local_th2', 'http://localhost:8082/opentheso2/api/search?q=##value##&lang=##lang##&theso=th2', 'REST', 'skos', 127, 1, 'pour tester', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('GeoNames', 'http://api.geonames.org/search?q=##value##&maxRows=10&style=FULL&lang=##lang##&username=opentheso', 'REST', 'xml', 190, 1, 'Alignement avec GeoNames', true, 'GeoNames');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Pactols', 'https://pactols.frantiq.fr/opentheso/api/search?q=##value##&lang=##lang##&theso=TH_1', 'REST', 'skos', 191, 1, 'Alignement avec PACTOLS', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Gemet', 'https://www.eionet.europa.eu/gemet/getConceptsMatchingKeyword?keyword=##value##&search_mode=3&thesaurus_uri=http://www.eionet.europa.eu/gemet/concept/&language=##lang##', 'REST', 'json', 192, 1, 'Alignement avec le thésaurus Gemet', false, 'Gemet');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Agrovoc', 'http://agrovoc.uniroma2.it/agrovoc/rest/v1/search/?query=##value##&lang=##lang##', 'REST', 'json', 193, 1, 'Alignement avec le thésaurus Agrovoc', false, 'Agrovoc');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('bnf_instrumentMusique', 'PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
SELECT ?instrument ?prop ?value where {
  <http://data.bnf.fr/ark:/12148/cb119367821> skos:narrower+ ?instrument.
  ?instrument ?prop ?value.
  FILTER( (regex(?prop,skos:prefLabel) || regex(?prop,skos:altLabel))  && regex(?value, ##value##,"i") ) 
    filter(lang(?value) =##lang##)
} LIMIT 20', 'SPARQL', 'skos', 5, 1, '', false, 'Opentheso');


--
-- TOC entry 3885 (class 0 OID 16535)
-- Dependencies: 204
-- Data for Name: alignement_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (1, 'Equivalence exacte', '=EQ', 'exactMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (2, 'Equivalence inexacte', '~EQ', 'closeMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (3, 'Equivalence générique', 'EQB', 'broadMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (4, 'Equivalence associative', 'EQR', 'relatedMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (5, 'Equivalence spécifique', 'EQS', 'narrowMatch');


--
-- TOC entry 3886 (class 0 OID 16541)
-- Dependencies: 205
-- Data for Name: bt_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (1, 'BT', 'Terme générique', 'Broader term');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (2, 'BTG', 'Terme générique (generic)', 'Broader term (generic)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (3, 'BTP', 'Terme générique (partitive)', 'Broader term (partitive)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (4, 'BTI', 'Terme générique (instance)', 'Broader term (instance)');


--
-- TOC entry 3962 (class 0 OID 69547)
-- Dependencies: 281
-- Data for Name: candidat_messages; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3963 (class 0 OID 90025)
-- Dependencies: 282
-- Data for Name: candidat_status; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3965 (class 0 OID 90528)
-- Dependencies: 284
-- Data for Name: candidat_vote; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3887 (class 0 OID 16547)
-- Dependencies: 206
-- Data for Name: compound_equivalence; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3889 (class 0 OID 16555)
-- Dependencies: 208
-- Data for Name: concept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3891 (class 0 OID 16570)
-- Dependencies: 210
-- Data for Name: concept_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3973 (class 0 OID 115737)
-- Dependencies: 292
-- Data for Name: concept_facet; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3894 (class 0 OID 16589)
-- Dependencies: 213
-- Data for Name: concept_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3895 (class 0 OID 16598)
-- Dependencies: 214
-- Data for Name: concept_group_concept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3897 (class 0 OID 16606)
-- Dependencies: 216
-- Data for Name: concept_group_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3899 (class 0 OID 16616)
-- Dependencies: 218
-- Data for Name: concept_group_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3901 (class 0 OID 16627)
-- Dependencies: 220
-- Data for Name: concept_group_label_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3902 (class 0 OID 16635)
-- Dependencies: 221
-- Data for Name: concept_group_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('MT', 'Microthesaurus', 'MicroThesaurus');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('G', 'Group', 'ConceptGroup');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('C', 'Collection', 'Collection');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('T', 'Theme', 'Theme');


--
-- TOC entry 3904 (class 0 OID 16643)
-- Dependencies: 223
-- Data for Name: concept_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3892 (class 0 OID 16580)
-- Dependencies: 211
-- Data for Name: concept_replacedby; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_replacedby (id_concept1, id_concept2, id_thesaurus, modified, id_user) VALUES ('64282', '64270', 'th105', '2021-01-20 10:08:18.223509+01', 5);
INSERT INTO public.concept_replacedby (id_concept1, id_concept2, id_thesaurus, modified, id_user) VALUES ('25', '19', 'th1', '2021-01-21 15:19:24.969325+01', 1);
INSERT INTO public.concept_replacedby (id_concept1, id_concept2, id_thesaurus, modified, id_user) VALUES ('27', '19', 'th1', '2021-01-21 15:19:24.973384+01', 1);
INSERT INTO public.concept_replacedby (id_concept1, id_concept2, id_thesaurus, modified, id_user) VALUES ('25', '19', 'th15', '2021-02-02 09:34:40.61152+01', 1);
INSERT INTO public.concept_replacedby (id_concept1, id_concept2, id_thesaurus, modified, id_user) VALUES ('27', '19', 'th15', '2021-02-02 09:34:40.615404+01', 1);


--
-- TOC entry 3905 (class 0 OID 16658)
-- Dependencies: 224
-- Data for Name: concept_term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3906 (class 0 OID 16664)
-- Dependencies: 225
-- Data for Name: copyright; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3959 (class 0 OID 69360)
-- Dependencies: 278
-- Data for Name: corpus_link; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.corpus_link (id_theso, corpus_name, uri_count, uri_link, active) VALUES ('th9', 'Frantiq', 'https://pro.frantiq.fr/es/koha_frantiq_biblios/_count?q=koha-auth-number:##id##', 'https://catalogue.frantiq.fr/cgi-bin/koha/opac-search.pl?q=an:##id##', true);
INSERT INTO public.corpus_link (id_theso, corpus_name, uri_count, uri_link, active) VALUES ('th9', 'Frantiq2', 'https://pro.frantiq.fr/es/koha_frantiq_biblios/_count?q=koha-auth-number:##id##', 'https://catalogue.frantiq.fr/cgi-bin/koha/opac-search.pl?q=an:##id##', false);
INSERT INTO public.corpus_link (id_theso, corpus_name, uri_count, uri_link, active) VALUES ('th5', 'dd', 'https://www.archeogrid.fr/concept/##id##/total ', 'https://www.archeogrid.fr/concept/##value##', false);
INSERT INTO public.corpus_link (id_theso, corpus_name, uri_count, uri_link, active) VALUES ('th1', 'Frantiq', 'https://pro.frantiq.fr/es/koha_frantiq_biblios/_count?q=koha-auth-number:##id##', 'https://catalogue.frantiq.fr/cgi-bin/koha/opac-search.pl?q=an:##id##', true);


--
-- TOC entry 3907 (class 0 OID 16670)
-- Dependencies: 226
-- Data for Name: custom_concept_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3908 (class 0 OID 16676)
-- Dependencies: 227
-- Data for Name: custom_term_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3909 (class 0 OID 16682)
-- Dependencies: 228
-- Data for Name: external_images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3911 (class 0 OID 16691)
-- Dependencies: 230
-- Data for Name: gps; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3913 (class 0 OID 16699)
-- Dependencies: 232
-- Data for Name: gps_preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3914 (class 0 OID 16709)
-- Dependencies: 233
-- Data for Name: hierarchical_relationship; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3915 (class 0 OID 16715)
-- Dependencies: 234
-- Data for Name: hierarchical_relationship_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3957 (class 0 OID 40590)
-- Dependencies: 276
-- Data for Name: homepage; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.homepage (htmlcode, lang) VALUES ('<p>Help and tutorials : <a href="https://opentheso.hypotheses.org" rel="noopener noreferrer" target="_blank" style="color: blue;">https://opentheso.hypotheses.org</a></p><p><strong style="color: rgb(230, 0, 0);">!!!!! To get started, select a thesaurus in the upper right !!!!!</strong></p><p>Opentheso is distributed under a free French law license compatible with the license <a href="http://www.gnu.org/copyleft/gpl.html" rel="noopener noreferrer" target="_blank" style="color: blue;">GNU GPL</a></p><p>It is a multilingual thesaurus manager, developed by the Technological Platform <a href="https://www.mom.fr/plateformes-technologiques/web-semantique-et-thesauri" rel="noopener noreferrer" target="_blank" style="color: blue;">WST</a> (Semantic Web &amp; Thesauri) located at <a href="https://www.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">MOM</a></p><p>in partnership with the <a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">GDS-FRANTIQ</a></p><p>&nbsp;</p><p><span style="color: black;">Designer : Brann Etienne</span><strong style="color: black;"> (</strong><a href="http://ithaqstudio.com/" rel="noopener noreferrer" target="_blank" style="color: rgb(149, 79, 114);"><strong>ithaqstudio.com</strong></a><strong style="color: black;">) </strong></p><p>Design integrator : Miled Rousset</p><p>&nbsp;</p><p>The development of Opentheso is supported in part by the consortium <a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank" style="color: blue;">MASA </a>(Memory of Archaeologists and Archaeological Sites) of the <a href="http://www.huma-num.fr/" rel="noopener noreferrer" target="_blank" style="color: blue;">TGIR Huma-Num.</a></p><p>Project Manager : <strong>Miled Rousset</strong></p><p>Development : <strong>Miled Rousset, Firas Gabsi, Emmanuelle Perrin, Prudham Jean-Marc, Quincy Mbape Eyoke, Antonio Perez, Carole Bonfré</strong></p><p>Partnership, testing and expertise : <strong>The teams of the network </strong><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;"><strong>Frantiq</strong></a> and in particular the group <a href="https://www.frantiq.fr/frantiq/organisation/groupes-de-travail-et-projets/pactols-opentheso/" rel="noopener noreferrer" target="_blank" style="color: blue;">PACTOLS</a>.</p><p>The development was carried out with the following technologies :</p><ul><li>PostgreSQL for the database</li><li>Java for the API and business module</li><li>JSF2 and PrimeFaces for the graphic part</li></ul><p>&nbsp;</p><p><strong>Opentheso</strong> is based on the project <a href="http://ark.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Arkéo</a> of the MOM to generate ark type identifiers <a href="http://fr.wikipedia.org/wiki/Archival_Resource_Key" rel="noopener noreferrer" target="_blank" style="color: blue;">ARK</a></p><p>Partners :</p><ul><li><a href="http://www.cnrs.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">CNRS</a></li><li><a href="http://www.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">MOM</a></li><li><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Frantiq</a></li><li><a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank" style="color: blue;">MASA</a></li><li><a href="http://www.huma-num.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Huma-Num</a></li></ul>', 'en');
INSERT INTO public.homepage (htmlcode, lang) VALUES ('<p>Aide et tutoriels : <a href="https://opentheso.hypotheses.org" rel="noopener noreferrer" target="_blank" style="color: blue;">https://opentheso.hypotheses.org</a></p><p><strong style="color: rgb(230, 0, 0);">!!!!! Pour commencer, sélectionnez un thésaurus en haut à droite !!!!!</strong></p><p>Opentheso est distribué en licence libre de droit français compatible avec la licence <a href="http://www.gnu.org/copyleft/gpl.html" rel="noopener noreferrer" target="_blank" style="color: blue;">GNU GPL</a></p><p>C''est un gestionnaire de thesaurus multilingue, développé par la plateforme Technologique <a href="https://www.mom.fr/plateformes-technologiques/web-semantique-et-thesauri" rel="noopener noreferrer" target="_blank" style="color: blue;">WST</a> (Web Sémantique &amp; Thesauri) située à la <a href="https://www.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">MOM</a></p><p>en partenariat avec le <a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">GDS-FRANTIQ</a></p><p>&nbsp;</p><p><span style="color: black;">Designer : Brann Etienne</span><strong style="color: black;"> (</strong><a href="http://ithaqstudio.com/" rel="noopener noreferrer" target="_blank" style="color: rgb(149, 79, 114);"><strong>ithaqstudio.com</strong></a><strong style="color: black;">) </strong></p><p>Intégrateur du design : Miled Rousset</p><p>&nbsp;</p><p>Le développement d''Opentheso est soutenu en partie par le Consortium <a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank" style="color: blue;">MASA </a>(Mémoire des archéologues et des Sites Archéologiques) de la <a href="http://www.huma-num.fr/" rel="noopener noreferrer" target="_blank" style="color: blue;">TGIR Huma-Num.</a></p><p>Chef de Projet : <strong>Miled Rousset</strong></p><p>Développement : <strong>Miled Rousset, Firas Gabsi, Emmanuelle Perrin, Prudham Jean-Marc, Quincy Mbape Eyoke, Antonio Perez, Carole Bonfré</strong></p><p>Partenariat, test et expertise : <strong>Les équipes du réseau </strong><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;"><strong>Frantiq</strong></a> et en particulier le groupe <a href="https://www.frantiq.fr/frantiq/organisation/groupes-de-travail-et-projets/pactols-opentheso/" rel="noopener noreferrer" target="_blank" style="color: blue;">PACTOLS</a>.</p><p>Le développement a été réalisé avec les technologies suivantes :</p><ul><li>PostgreSQL pour la base des données</li><li>Java pour le module API et module métier</li><li>JSF2 et PrimeFaces pour la partie graphique</li></ul><p>&nbsp;</p><p><strong>Opentheso</strong> s''appuie sur le projet <a href="http://ark.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Arkéo</a> de la MOM pour générer des identifiants type <a href="http://fr.wikipedia.org/wiki/Archival_Resource_Key" rel="noopener noreferrer" target="_blank" style="color: blue;">ARK</a></p><p>Partenaires :</p><ul><li><a href="http://www.cnrs.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">CNRS</a></li><li><a href="http://www.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">MOM</a></li><li><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Frantiq</a></li><li><a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank" style="color: blue;">MASA</a></li><li><a href="http://www.huma-num.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Huma-Num</a></li></ul>', 'fr');


--
-- TOC entry 3916 (class 0 OID 16722)
-- Dependencies: 235
-- Data for Name: images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3917 (class 0 OID 16729)
-- Dependencies: 236
-- Data for Name: info; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3969 (class 0 OID 91656)
-- Dependencies: 288
-- Data for Name: languages_iso639; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('aa ', 'aar', 'Afar', 'afar', 2);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ab ', 'abk', 'Abkhazian', 'abkhaze', 3);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('af ', 'afr', 'Afrikaans', 'afrikaans', 4);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ak ', 'aka', 'Akan', 'akan', 5);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sq ', 'alb (B)
sqi (T)', 'Albanian', 'albanais', 6);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('am ', 'amh', 'Amharic', 'amharique', 7);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ar ', 'ara', 'Arabic', 'arabe', 8);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('an ', 'arg', 'Aragonese', 'aragonais', 9);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('as ', 'asm', 'Assamese', 'assamais', 10);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('av ', 'ava', 'Avaric', 'avar', 11);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ae ', 'ave', 'Avestan', 'avestique', 12);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ay ', 'aym', 'Aymara', 'aymara', 13);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('az ', 'aze', 'Azerbaijani', 'azéri', 14);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ba ', 'bak', 'Bashkir', 'bachkir', 15);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bm ', 'bam', 'Bambara', 'bambara', 16);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('eu ', 'baq (B)
eus (T)', 'Basque', 'basque', 17);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('be ', 'bel', 'Belarusian', 'biélorusse', 18);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bn ', 'ben', 'Bengali', 'bengali', 19);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bh ', 'bih', 'Bihari languages', 'langues biharis', 20);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bi ', 'bis', 'Bislama', 'bichlamar', 21);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bs ', 'bos', 'Bosnian', 'bosniaque', 22);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('br ', 'bre', 'Breton', 'breton', 23);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bg ', 'bul', 'Bulgarian', 'bulgare', 24);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ca ', 'cat', 'Catalan; Valencian', 'catalan; valencien', 25);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ch ', 'cha', 'Chamorro', 'chamorro', 26);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ce ', 'che', 'Chechen', 'tchétchène', 27);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('zh ', 'chi (B)
zho (T)', 'Chinese', 'chinois', 28);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cv ', 'chv', 'Chuvash', 'tchouvache', 29);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kw ', 'cor', 'Cornish', 'cornique', 30);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('co ', 'cos', 'Corsican', 'corse', 31);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cr ', 'cre', 'Cree', 'cree', 32);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cy ', 'wel (B)
cym (T)', 'Welsh', 'gallois', 33);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cs ', 'cze (B)
ces (T)', 'Czech', 'tchèque', 34);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('da ', 'dan', 'Danish', 'danois', 35);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('de ', 'ger (B)
deu (T)', 'German', 'allemand', 36);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('dv ', 'div', 'Divehi; Dhivehi; Maldivian', 'maldivien', 37);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('dz ', 'dzo', 'Dzongkha', 'dzongkha', 38);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('el ', 'gre (B)
ell (T)', 'Greek, Modern (1453-)', 'grec moderne (après 1453)', 39);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('en ', 'eng', 'English', 'anglais', 40);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('eo ', 'epo', 'Esperanto', 'espéranto', 41);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('et ', 'est', 'Estonian', 'estonien', 42);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ee ', 'ewe', 'Ewe', 'éwé', 43);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fo ', 'fao', 'Faroese', 'féroïen', 44);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fj ', 'fij', 'Fijian', 'fidjien', 45);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fi ', 'fin', 'Finnish', 'finnois', 46);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fr ', 'fre (B)
fra (T)', 'French', 'français', 47);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fy ', 'fry', 'Western Frisian', 'frison occidental', 48);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ff ', 'ful', 'Fulah', 'peul', 49);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ka ', 'geo (B)
kat (T)', 'Georgian', 'géorgien', 50);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gd ', 'gla', 'Gaelic; Scottish Gaelic', 'gaélique; gaélique écossais', 51);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ga ', 'gle', 'Irish', 'irlandais', 52);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gl ', 'glg', 'Galician', 'galicien', 53);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gv ', 'glv', 'Manx', 'manx; mannois', 54);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gn ', 'grn', 'Guarani', 'guarani', 55);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gu ', 'guj', 'Gujarati', 'goudjrati', 56);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ht ', 'hat', 'Haitian; Haitian Creole', 'haïtien; créole haïtien', 57);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ha ', 'hau', 'Hausa', 'haoussa', 58);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('he ', 'heb', 'Hebrew', 'hébreu', 59);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hz ', 'her', 'Herero', 'herero', 60);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hi ', 'hin', 'Hindi', 'hindi', 61);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ho ', 'hmo', 'Hiri Motu', 'hiri motu', 62);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hr ', 'hrv', 'Croatian', 'croate', 63);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hu ', 'hun', 'Hungarian', 'hongrois', 64);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('hy ', 'arm (B)
hye (T)', 'Armenian', 'arménien', 65);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ig ', 'ibo', 'Igbo', 'igbo', 66);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('is ', 'ice (B)
isl (T)', 'Icelandic', 'islandais', 67);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('io ', 'ido', 'Ido', 'ido', 68);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ii ', 'iii', 'Sichuan Yi; Nuosu', 'yi de Sichuan', 69);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('iu ', 'iku', 'Inuktitut', 'inuktitut', 70);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ie ', 'ile', 'Interlingue; Occidental', 'interlingue', 71);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('id ', 'ind', 'Indonesian', 'indonésien', 72);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ik ', 'ipk', 'Inupiaq', 'inupiaq', 73);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('it ', 'ita', 'Italian', 'italien', 74);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('jv ', 'jav', 'Javanese', 'javanais', 75);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ja ', 'jpn', 'Japanese', 'japonais', 76);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kl ', 'kal', 'Kalaallisut; Greenlandic', 'groenlandais', 77);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kn ', 'kan', 'Kannada', 'kannada', 78);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ks ', 'kas', 'Kashmiri', 'kashmiri', 79);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kr ', 'kau', 'Kanuri', 'kanouri', 80);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kk ', 'kaz', 'Kazakh', 'kazakh', 81);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('km ', 'khm', 'Central Khmer', 'khmer central', 82);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ki ', 'kik', 'Kikuyu; Gikuyu', 'kikuyu', 83);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('rw ', 'kin', 'Kinyarwanda', 'rwanda', 84);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ky ', 'kir', 'Kirghiz; Kyrgyz', 'kirghiz', 85);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kv ', 'kom', 'Komi', 'kom', 86);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kg ', 'kon', 'Kongo', 'kongo', 87);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ko ', 'kor', 'Korean', 'coréen', 88);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('kj ', 'kua', 'Kuanyama; Kwanyama', 'kuanyama; kwanyama', 89);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ku ', 'kur', 'Kurdish', 'kurde', 90);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lo ', 'lao', 'Lao', 'lao', 91);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('la ', 'lat', 'Latin', 'latin', 92);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lv ', 'lav', 'Latvian', 'letton', 93);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('li ', 'lim', 'Limburgan; Limburger; Limburgish', 'limbourgeois', 94);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ln ', 'lin', 'Lingala', 'lingala', 95);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lt ', 'lit', 'Lithuanian', 'lituanien', 96);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lb ', 'ltz', 'Luxembourgish; Letzeburgesch', 'luxembourgeois', 97);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lu ', 'lub', 'Luba-Katanga', 'luba-katanga', 98);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('lg ', 'lug', 'Ganda', 'ganda', 99);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mk ', 'mac (B)
mkd (T)', 'Macedonian', 'macédonien', 100);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mh ', 'mah', 'Marshallese', 'marshall', 101);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ml ', 'mal', 'Malayalam', 'malayalam', 102);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mr ', 'mar', 'Marathi', 'marathe', 103);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ms ', 'may (B)
msa (T)', 'Malay', 'malais', 104);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mg ', 'mlg', 'Malagasy', 'malgache', 105);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mt ', 'mlt', 'Maltese', 'maltais', 106);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mn ', 'mon', 'Mongolian', 'mongol', 107);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mi ', 'mao (B)
mri (T)', 'Maori', 'maori', 108);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('my ', 'bur (B)
mya (T)', 'Burmese', 'birman', 109);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('na ', 'nau', 'Nauru', 'nauruan', 110);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nv ', 'nav', 'Navajo; Navaho', 'navaho', 111);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nr ', 'nbl', 'Ndebele, South; South Ndebele', 'ndébélé du Sud', 112);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nd ', 'nde', 'Ndebele, North; North Ndebele', 'ndébélé du Nord', 113);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ng ', 'ndo', 'Ndonga', 'ndonga', 114);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ne ', 'nep', 'Nepali', 'népalais', 115);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nl ', 'dut (B)
nld (T)', 'Dutch; Flemish', 'néerlandais; flamand', 116);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nb ', 'nob', 'Bokmål, Norwegian; Norwegian Bokmål', 'norvégien bokmål', 117);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('no ', 'nor', 'Norwegian', 'norvégien', 118);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ny ', 'nya', 'Chichewa; Chewa; Nyanja', 'chichewa; chewa; nyanja', 119);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('oc ', 'oci', 'Occitan (post 1500)', 'occitan (après 1500)', 120);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('oj ', 'oji', 'Ojibwa', 'ojibwa', 121);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('or ', 'ori', 'Oriya', 'oriya', 122);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('om ', 'orm', 'Oromo', 'galla', 123);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('os ', 'oss', 'Ossetian; Ossetic', 'ossète', 124);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pa ', 'pan', 'Panjabi; Punjabi', 'pendjabi', 125);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('fa ', 'per (B)
fas (T)', 'Persian', 'persan', 126);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pi ', 'pli', 'Pali', 'pali', 127);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pl ', 'pol', 'Polish', 'polonais', 128);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('pt ', 'por', 'Portuguese', 'portugais', 129);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ps ', 'pus', 'Pushto; Pashto', 'pachto', 130);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('qu ', 'que', 'Quechua', 'quechua', 131);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('rm ', 'roh', 'Romansh', 'romanche', 132);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ro ', 'rum (B)
ron (T)', 'Romanian; Moldavian; Moldovan', 'roumain; moldave', 133);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('rn ', 'run', 'Rundi', 'rundi', 134);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ru ', 'rus', 'Russian', 'russe', 135);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sg ', 'sag', 'Sango', 'sango', 136);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sa ', 'san', 'Sanskrit', 'sanskrit', 137);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('si ', 'sin', 'Sinhala; Sinhalese', 'singhalais', 138);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sk ', 'slo (B)
slk (T)', 'Slovak', 'slovaque', 139);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sl ', 'slv', 'Slovenian', 'slovène', 140);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('se ', 'sme', 'Northern Sami', 'sami du Nord', 141);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sm ', 'smo', 'Samoan', 'samoan', 142);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sn ', 'sna', 'Shona', 'shona', 143);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sd ', 'snd', 'Sindhi', 'sindhi', 144);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('so ', 'som', 'Somali', 'somali', 145);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('st ', 'sot', 'Sotho, Southern', 'sotho du Sud', 146);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('es ', 'spa', 'Spanish; Castilian', 'espagnol; castillan', 147);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sc ', 'srd', 'Sardinian', 'sarde', 148);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sr ', 'srp', 'Serbian', 'serbe', 149);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ss ', 'ssw', 'Swati', 'swati', 150);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('su ', 'sun', 'Sundanese', 'soundanais', 151);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sw ', 'swa', 'Swahili', 'swahili', 152);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('sv ', 'swe', 'Swedish', 'suédois', 153);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ty ', 'tah', 'Tahitian', 'tahitien', 154);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ta ', 'tam', 'Tamil', 'tamoul', 155);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tt ', 'tat', 'Tatar', 'tatar', 156);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('te ', 'tel', 'Telugu', 'télougou', 157);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tg ', 'tgk', 'Tajik', 'tadjik', 158);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tl ', 'tgl', 'Tagalog', 'tagalog', 159);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('th ', 'tha', 'Thai', 'thaï', 160);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('bo ', 'tib (B)
bod (T)', 'Tibetan', 'tibétain', 161);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ti ', 'tir', 'Tigrinya', 'tigrigna', 162);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('to ', 'ton', 'Tonga (Tonga Islands)', 'tongan (Îles Tonga)', 163);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tn ', 'tsn', 'Tswana', 'tswana', 164);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ts ', 'tso', 'Tsonga', 'tsonga', 165);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tk ', 'tuk', 'Turkmen', 'turkmène', 166);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tr ', 'tur', 'Turkish', 'turc', 167);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('tw ', 'twi', 'Twi', 'twi', 168);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ug ', 'uig', 'Uighur; Uyghur', 'ouïgour', 169);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('uk ', 'ukr', 'Ukrainian', 'ukrainien', 170);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ur ', 'urd', 'Urdu', 'ourdou', 171);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('uz ', 'uzb', 'Uzbek', 'ouszbek', 172);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ve ', 'ven', 'Venda', 'venda', 173);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('vi ', 'vie', 'Vietnamese', 'vietnamien', 174);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('vo ', 'vol', 'Volapük', 'volapük', 175);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('wa ', 'wln', 'Walloon', 'wallon', 176);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('wo ', 'wol', 'Wolof', 'wolof', 177);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('xh ', 'xho', 'Xhosa', 'xhosa', 178);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('yi ', 'yid', 'Yiddish', 'yiddish', 179);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('yo ', 'yor', 'Yoruba', 'yoruba', 180);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('za ', 'zha', 'Zhuang; Chuang', 'zhuang; chuang', 181);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('zu ', 'zul', 'Zulu', 'zoulou', 182);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('cu ', 'chu', 'Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic', 'vieux slave; vieux bulgare', 183);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ia ', 'ina', 'Interlingua (International Auxiliary Language Association)', 'interlingua', 184);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('nn ', 'nno', 'Norwegian Nynorsk; Nynorsk, Norwegian', 'norvégien nynorsk', 185);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('gr ', 'grc', 'Greek, Ancient (to 1453)', 'grec ancien (jusqu''à 1453)', 186);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('iso', 'iso', 'norme ISO 233-2 (1993)', 'norme ISO 233-2 (1993)', 187);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('ala', 'ala', 'ALA-LC Romanization Table (American Library Association-Library of Congress)', 'ALA-LC)', 188);
INSERT INTO public.languages_iso639 (iso639_1, iso639_2, english_name, french_name, id) VALUES ('mul', 'mul', 'multiple langages', 'multiple langages', 189);


--
-- TOC entry 3970 (class 0 OID 115617)
-- Dependencies: 289
-- Data for Name: node_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3919 (class 0 OID 16752)
-- Dependencies: 238
-- Data for Name: non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3920 (class 0 OID 16761)
-- Dependencies: 239
-- Data for Name: non_preferred_term_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3922 (class 0 OID 16771)
-- Dependencies: 241
-- Data for Name: note; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3924 (class 0 OID 16782)
-- Dependencies: 243
-- Data for Name: note_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3966 (class 0 OID 91636)
-- Dependencies: 285
-- Data for Name: note_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('note', false, true, 'Note', 'Note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('historyNote', true, true, 'Note historique', 'History note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('scopeNote', false, true, 'Note de portée', 'Scope note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('example', true, false, 'Exemple', 'Example');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('editorialNote', true, false, 'Note éditoriale', 'Editorial note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('definition', true, false, 'Définition', 'Definition');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('changeNote', true, false, 'Note de changement', 'Change note');


--
-- TOC entry 3925 (class 0 OID 16797)
-- Dependencies: 244
-- Data for Name: nt_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (1, 'NT', 'Term spécifique', 'Narrower term');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (2, 'NTG', 'Term spécifique (generic)', 'Narrower term (generic)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (3, 'NTP', 'Term spécifique (partitive)', 'Narrower term (partitive)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (4, 'NTI', 'Term spécifique (instantial)', 'Narrower term (instantial)');


--
-- TOC entry 3926 (class 0 OID 16803)
-- Dependencies: 245
-- Data for Name: permuted; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3928 (class 0 OID 16811)
-- Dependencies: 247
-- Data for Name: preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3929 (class 0 OID 16844)
-- Dependencies: 248
-- Data for Name: preferences_sparql; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3930 (class 0 OID 16851)
-- Dependencies: 249
-- Data for Name: preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3931 (class 0 OID 16857)
-- Dependencies: 250
-- Data for Name: proposition; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3932 (class 0 OID 16865)
-- Dependencies: 251
-- Data for Name: relation_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3933 (class 0 OID 16871)
-- Dependencies: 252
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.roles (id, name, description) VALUES (1, 'superAdmin', 'Super Administrateur pour tout gérer tout thésaurus et tout utilisateur');
INSERT INTO public.roles (id, name, description) VALUES (2, 'admin', 'administrateur pour un domaine ou parc de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (3, 'manager', 'gestionnaire de thésaurus, pas de création de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (4, 'contributor', 'traducteur, notes, candidats, images');


--
-- TOC entry 3935 (class 0 OID 16879)
-- Dependencies: 254
-- Data for Name: routine_mail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3936 (class 0 OID 16886)
-- Dependencies: 255
-- Data for Name: split_non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3968 (class 0 OID 91647)
-- Dependencies: 287
-- Data for Name: status; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.status (id_status, value) VALUES (1, 'En attente');
INSERT INTO public.status (id_status, value) VALUES (2, 'Inséré');
INSERT INTO public.status (id_status, value) VALUES (3, 'Rejeté');


--
-- TOC entry 3938 (class 0 OID 16891)
-- Dependencies: 257
-- Data for Name: term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3940 (class 0 OID 16903)
-- Dependencies: 259
-- Data for Name: term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3942 (class 0 OID 16914)
-- Dependencies: 261
-- Data for Name: term_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3944 (class 0 OID 16925)
-- Dependencies: 263
-- Data for Name: thesaurus; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3945 (class 0 OID 16935)
-- Dependencies: 264
-- Data for Name: thesaurus_alignement_source; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3972 (class 0 OID 115718)
-- Dependencies: 291
-- Data for Name: thesaurus_array; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3946 (class 0 OID 16956)
-- Dependencies: 265
-- Data for Name: thesaurus_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3958 (class 0 OID 40596)
-- Dependencies: 277
-- Data for Name: thesohomepage; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<table class=MsoTableGrid border=0 cellspacing=0 cellpadding=0

 style=''border-collapse:collapse;border:none''>

 <tr>

 <td width=94 style=''width:70.65pt;padding:0cm 5.4pt 0cm 5.4pt''>

 <p class=MsoNormal><img width=50 height=65 id="Image 1"

 src="Opentheso.fld/image001.png"

 alt="Une image contenant dessin&#10;&#10;Description générée automatiquement"></p>

 </td>

 <td width=510 style=''width:382.15pt;padding:0cm 5.4pt 0cm 5.4pt''>

 <p class=MsoNormal><b><span style=''font-size:18.0pt;font-family:"Times New Roman",serif''>Opentheso</span></b></p>

 <p class=MsoNormal><span style=''font-size:10.0pt;font-family:"Times New Roman",serif''>Copyright

 ©CNRS</span></p>

 </td>

 </tr>

</table>', 'fr', 'th10');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p><strong>Espace de travail PACTOLS v2</strong></p><p><br></p><ul><li><strong>Les concepts </strong>présentés ici sont issus de la version publique du thésaurus PACTOLS auxquels ont été ajoutés un certain nombre de concepts suggérés (candidats) et des termes demandés par les spécialistes de certains domaines avec lesquels nous travaillons étroitement.</li></ul><p><br></p><ul><li><strong>L''organisation</strong> des concepts telle qu''elle s''affiche aujourd''hui est provisoire. Les dossiers sont susceptibles de changer de nom et d''emplacement dans l''arbre. Les collections vont aussi évoluer, au fur et à mesure que le travail de réorganisation avancera.</li></ul><p><br></p><ul><li><strong>Les identifiants pérennes de ces concepts</strong> ne doivent pas être utilisés pour le moment, car ils renverront une erreur de direction. Ils pointent en effet sur la version publique du thésaurus, toujours disponible à l''adresse : <a href="https://pactols.frantiq.fr/opentheso" rel="noopener noreferrer" target="_blank">https://pactols.frantiq.fr/opentheso</a></li></ul><p><br></p><p><br></p><p><strong class="ql-size-large">Une nouvelle version du thésaurus PACTOLS est prévue pour la fin de l''année 2021.</strong></p><p><br></p><p><br></p><p><br></p><p><br></p><p><br></p>', 'fr', 'th5');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>tets de. ll ùmdqdqsd</p><p><br></p><p>fq</p><p>sf</p><p>sf</p><p> qs</p><p>fd</p><p><br></p><p><br></p><p><br></p><p>f qs</p><p>f qsfqsdfqsdf qsf</p><p><br></p>', 'fr', 'th11');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>Mon thésaurus est diffusé en libre sous licence GPL ....</p><p>dqsd qsd qsdqs</p><p>dqs</p>', 'fr', 'th1');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>texte pour New Th47</p>', 'fr', 'th47');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>texte pour Theso_th54</p>', 'fr', 'th54');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>Unesco thésaurus FR</p>', 'fr', 'th44');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>Unesco thesaurus EN</p>', 'en', 'th44');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>à propos de Essai 1 </p>', 'fr', 'th55');


--
-- TOC entry 3949 (class 0 OID 16968)
-- Dependencies: 268
-- Data for Name: user_group_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3950 (class 0 OID 16975)
-- Dependencies: 269
-- Data for Name: user_group_thesaurus; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3951 (class 0 OID 16981)
-- Dependencies: 270
-- Data for Name: user_role_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3952 (class 0 OID 16984)
-- Dependencies: 271
-- Data for Name: user_role_only_on; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3953 (class 0 OID 16991)
-- Dependencies: 272
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.users (id_user, username, password, active, mail, passtomodify, alertmail, issuperadmin) VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', true, 'admin@domaine.fr', false, false, true);


--
-- TOC entry 3954 (class 0 OID 17002)
-- Dependencies: 273
-- Data for Name: users2; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3955 (class 0 OID 17011)
-- Dependencies: 274
-- Data for Name: users_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3956 (class 0 OID 17019)
-- Dependencies: 275
-- Data for Name: version_history; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3984 (class 0 OID 0)
-- Dependencies: 198
-- Name: alignement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_id_seq', 1, false);


--
-- TOC entry 3985 (class 0 OID 0)
-- Dependencies: 200
-- Name: alignement_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_preferences_id_seq', 1, false);


--
-- TOC entry 3986 (class 0 OID 0)
-- Dependencies: 202
-- Name: alignement_source__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_source__id_seq', 193, true);


--
-- TOC entry 3987 (class 0 OID 0)
-- Dependencies: 280
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.candidat_messages_id_message_seq', 13, true);


--
-- TOC entry 3988 (class 0 OID 0)
-- Dependencies: 283
-- Name: candidat_vote_id_vote_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.candidat_vote_id_vote_seq', 19, true);


--
-- TOC entry 3989 (class 0 OID 0)
-- Dependencies: 207
-- Name: concept__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept__id_seq', 1, false);


--
-- TOC entry 3990 (class 0 OID 0)
-- Dependencies: 209
-- Name: concept_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_candidat__id_seq', 1, false);


--
-- TOC entry 3991 (class 0 OID 0)
-- Dependencies: 212
-- Name: concept_group__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group__id_seq', 1, false);


--
-- TOC entry 3992 (class 0 OID 0)
-- Dependencies: 215
-- Name: concept_group_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_historique__id_seq', 1, false);


--
-- TOC entry 3993 (class 0 OID 0)
-- Dependencies: 219
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_label_historique__id_seq', 1, false);


--
-- TOC entry 3994 (class 0 OID 0)
-- Dependencies: 217
-- Name: concept_group_label_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_label_id_seq', 1, false);


--
-- TOC entry 3995 (class 0 OID 0)
-- Dependencies: 222
-- Name: concept_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_historique__id_seq', 1, false);


--
-- TOC entry 3996 (class 0 OID 0)
-- Dependencies: 229
-- Name: facet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.facet_id_seq', 1, false);


--
-- TOC entry 3997 (class 0 OID 0)
-- Dependencies: 231
-- Name: gps_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gps_preferences_id_seq', 1, false);


--
-- TOC entry 3998 (class 0 OID 0)
-- Dependencies: 237
-- Name: languages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.languages_id_seq', 189, false);


--
-- TOC entry 3999 (class 0 OID 0)
-- Dependencies: 240
-- Name: note__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.note__id_seq', 1, false);


--
-- TOC entry 4000 (class 0 OID 0)
-- Dependencies: 242
-- Name: note_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.note_historique__id_seq', 1, false);


--
-- TOC entry 4001 (class 0 OID 0)
-- Dependencies: 246
-- Name: pref__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pref__id_seq', 1, false);


--
-- TOC entry 4002 (class 0 OID 0)
-- Dependencies: 253
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.role_id_seq', 6, true);


--
-- TOC entry 4003 (class 0 OID 0)
-- Dependencies: 279
-- Name: status_id_status_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.status_id_status_seq', 1, false);


--
-- TOC entry 4004 (class 0 OID 0)
-- Dependencies: 286
-- Name: status_id_status_seq1; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.status_id_status_seq1', 1, false);


--
-- TOC entry 4005 (class 0 OID 0)
-- Dependencies: 256
-- Name: term__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term__id_seq', 1, false);


--
-- TOC entry 4006 (class 0 OID 0)
-- Dependencies: 258
-- Name: term_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term_candidat__id_seq', 1, false);


--
-- TOC entry 4007 (class 0 OID 0)
-- Dependencies: 260
-- Name: term_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term_historique__id_seq', 1, false);


--
-- TOC entry 4008 (class 0 OID 0)
-- Dependencies: 290
-- Name: thesaurus_array_facet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.thesaurus_array_facet_id_seq', 1, false);


--
-- TOC entry 4009 (class 0 OID 0)
-- Dependencies: 262
-- Name: thesaurus_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.thesaurus_id_seq', 1, false);


--
-- TOC entry 4010 (class 0 OID 0)
-- Dependencies: 266
-- Name: user__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user__id_seq', 2, false);


--
-- TOC entry 4011 (class 0 OID 0)
-- Dependencies: 267
-- Name: user_group_label__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user_group_label__id_seq', 1, false);


--
-- TOC entry 3733 (class 2606 OID 17027)
-- Name: version_history VersionHistory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.version_history
    ADD CONSTRAINT "VersionHistory_pkey" PRIMARY KEY ("idVersionhistory");


--
-- TOC entry 3582 (class 2606 OID 17029)
-- Name: alignement alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement
    ADD CONSTRAINT alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key UNIQUE (internal_id_concept, internal_id_thesaurus, uri_target);


--
-- TOC entry 3584 (class 2606 OID 17031)
-- Name: alignement alignement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement
    ADD CONSTRAINT alignement_pkey PRIMARY KEY (id);


--
-- TOC entry 3586 (class 2606 OID 17033)
-- Name: alignement_preferences alignement_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_preferences
    ADD CONSTRAINT alignement_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_concept_depart, id_alignement_source);


--
-- TOC entry 3588 (class 2606 OID 17035)
-- Name: alignement_source alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_source
    ADD CONSTRAINT alignement_source_pkey PRIMARY KEY (id);


--
-- TOC entry 3590 (class 2606 OID 17037)
-- Name: alignement_source alignement_source_source_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_source
    ADD CONSTRAINT alignement_source_source_key UNIQUE (source);


--
-- TOC entry 3592 (class 2606 OID 17039)
-- Name: alignement_type alignment_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_type
    ADD CONSTRAINT alignment_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3594 (class 2606 OID 17041)
-- Name: bt_type bt_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bt_type
    ADD CONSTRAINT bt_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3596 (class 2606 OID 17043)
-- Name: bt_type bt_type_relation_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bt_type
    ADD CONSTRAINT bt_type_relation_key UNIQUE (relation);


--
-- TOC entry 3741 (class 2606 OID 69555)
-- Name: candidat_messages candidat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_messages
    ADD CONSTRAINT candidat_messages_pkey PRIMARY KEY (id_message);


--
-- TOC entry 3743 (class 2606 OID 90033)
-- Name: candidat_status candidat_status_id_concept_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_status
    ADD CONSTRAINT candidat_status_id_concept_id_thesaurus_key UNIQUE (id_concept, id_thesaurus);


--
-- TOC entry 3745 (class 2606 OID 90536)
-- Name: candidat_vote candidat_vote_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_vote
    ADD CONSTRAINT candidat_vote_pkey PRIMARY KEY (id_vote);


--
-- TOC entry 3598 (class 2606 OID 17045)
-- Name: compound_equivalence compound_equivalence_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compound_equivalence
    ADD CONSTRAINT compound_equivalence_pkey PRIMARY KEY (id_split_nonpreferredterm, id_preferredterm);


--
-- TOC entry 3603 (class 2606 OID 17047)
-- Name: concept_candidat concept_candidat_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_candidat
    ADD CONSTRAINT concept_candidat_id_key UNIQUE (id);


--
-- TOC entry 3605 (class 2606 OID 17049)
-- Name: concept_candidat concept_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_candidat
    ADD CONSTRAINT concept_candidat_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3623 (class 2606 OID 91765)
-- Name: concept_historique concept_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_historique
    ADD CONSTRAINT concept_copy_pkey PRIMARY KEY (id_concept, id_thesaurus, id_group, id_user, modified);


--
-- TOC entry 3757 (class 2606 OID 115744)
-- Name: concept_facet concept_facettes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_facet
    ADD CONSTRAINT concept_facettes_pkey PRIMARY KEY (id_facet, id_thesaurus, id_concept);


--
-- TOC entry 3607 (class 2606 OID 17053)
-- Name: concept_replacedby concept_fusion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_replacedby
    ADD CONSTRAINT concept_fusion_pkey PRIMARY KEY (id_concept1, id_concept2, id_thesaurus);


--
-- TOC entry 3611 (class 2606 OID 17055)
-- Name: concept_group_concept concept_group_concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_concept
    ADD CONSTRAINT concept_group_concept_pkey PRIMARY KEY (idgroup, idthesaurus, idconcept);


--
-- TOC entry 3613 (class 2606 OID 17057)
-- Name: concept_group_historique concept_group_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_historique
    ADD CONSTRAINT concept_group_copy_pkey PRIMARY KEY (idgroup, idthesaurus, modified, id_user);


--
-- TOC entry 3619 (class 2606 OID 17059)
-- Name: concept_group_label_historique concept_group_label_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_label_historique
    ADD CONSTRAINT concept_group_label_copy_pkey PRIMARY KEY (lang, idthesaurus, lexicalvalue, modified, id_user);


--
-- TOC entry 3615 (class 2606 OID 17061)
-- Name: concept_group_label concept_group_label_idgrouplabel_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_label
    ADD CONSTRAINT concept_group_label_idgrouplabel_key UNIQUE (id);


--
-- TOC entry 3617 (class 2606 OID 17063)
-- Name: concept_group_label concept_group_label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_label
    ADD CONSTRAINT concept_group_label_pkey PRIMARY KEY (lang, idthesaurus, lexicalvalue);


--
-- TOC entry 3609 (class 2606 OID 17065)
-- Name: concept_group concept_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group
    ADD CONSTRAINT concept_group_pkey PRIMARY KEY (idgroup, idthesaurus);


--
-- TOC entry 3621 (class 2606 OID 17067)
-- Name: concept_group_type concept_group_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_type
    ADD CONSTRAINT concept_group_type_pkey PRIMARY KEY (code, label);


--
-- TOC entry 3601 (class 2606 OID 17071)
-- Name: concept concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept
    ADD CONSTRAINT concept_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3625 (class 2606 OID 17073)
-- Name: concept_term_candidat concept_term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_term_candidat
    ADD CONSTRAINT concept_term_candidat_pkey PRIMARY KEY (id_concept, id_term, id_thesaurus);


--
-- TOC entry 3627 (class 2606 OID 17075)
-- Name: copyright copyright_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.copyright
    ADD CONSTRAINT copyright_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 3739 (class 2606 OID 69367)
-- Name: corpus_link corpus_link_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.corpus_link
    ADD CONSTRAINT corpus_link_pkey PRIMARY KEY (id_theso, corpus_name);


--
-- TOC entry 3629 (class 2606 OID 17077)
-- Name: custom_concept_attribute custom_concept_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_concept_attribute
    ADD CONSTRAINT custom_concept_attribute_pkey PRIMARY KEY ("idConcept");


--
-- TOC entry 3631 (class 2606 OID 17079)
-- Name: custom_term_attribute custom_term_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_term_attribute
    ADD CONSTRAINT custom_term_attribute_pkey PRIMARY KEY (identifier);


--
-- TOC entry 3633 (class 2606 OID 17081)
-- Name: external_images external_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_images
    ADD CONSTRAINT external_images_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 3635 (class 2606 OID 91738)
-- Name: gps gps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gps
    ADD CONSTRAINT gps_pkey PRIMARY KEY (id_concept, id_theso);


--
-- TOC entry 3637 (class 2606 OID 17085)
-- Name: gps_preferences gps_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gps_preferences
    ADD CONSTRAINT gps_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_alignement_source);


--
-- TOC entry 3641 (class 2606 OID 17087)
-- Name: hierarchical_relationship_historique hierarchical_relationship_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchical_relationship_historique
    ADD CONSTRAINT hierarchical_relationship_copy_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2, modified, id_user);


--
-- TOC entry 3639 (class 2606 OID 17089)
-- Name: hierarchical_relationship hierarchical_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchical_relationship
    ADD CONSTRAINT hierarchical_relationship_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2);


--
-- TOC entry 3735 (class 2606 OID 40603)
-- Name: homepage homepage_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.homepage
    ADD CONSTRAINT homepage_lang_key UNIQUE (lang);


--
-- TOC entry 3643 (class 2606 OID 17091)
-- Name: images images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.images
    ADD CONSTRAINT images_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 3645 (class 2606 OID 17093)
-- Name: info info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.info
    ADD CONSTRAINT info_pkey PRIMARY KEY (version_opentheso, version_bdd);


--
-- TOC entry 3751 (class 2606 OID 91666)
-- Name: languages_iso639 languages_iso639_iso639_1_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_iso639_1_key UNIQUE (iso639_1);


--
-- TOC entry 3753 (class 2606 OID 91664)
-- Name: languages_iso639 languages_iso639_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_pkey PRIMARY KEY (id);


--
-- TOC entry 3648 (class 2606 OID 17101)
-- Name: non_preferred_term non_prefered_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.non_preferred_term
    ADD CONSTRAINT non_prefered_term_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 3651 (class 2606 OID 17103)
-- Name: non_preferred_term_historique non_preferred_term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.non_preferred_term_historique
    ADD CONSTRAINT non_preferred_term_copy_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, modified, id_user);


--
-- TOC entry 3660 (class 2606 OID 17105)
-- Name: note_historique note_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_historique
    ADD CONSTRAINT note_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 3654 (class 2606 OID 91734)
-- Name: note note_notetypecode_id_thesaurus_id_concept_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_notetypecode_id_thesaurus_id_concept_lang_key UNIQUE (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue);


--
-- TOC entry 3656 (class 2606 OID 91736)
-- Name: note note_notetypecode_id_thesaurus_id_term_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_notetypecode_id_thesaurus_id_term_lang_key UNIQUE (notetypecode, id_thesaurus, id_term, lang, lexicalvalue);


--
-- TOC entry 3658 (class 2606 OID 17111)
-- Name: note note_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_pkey PRIMARY KEY (id);


--
-- TOC entry 3662 (class 2606 OID 17113)
-- Name: nt_type nt_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3664 (class 2606 OID 17115)
-- Name: nt_type nt_type_relation_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_relation_key UNIQUE (relation);


--
-- TOC entry 3667 (class 2606 OID 17117)
-- Name: permuted permuted_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permuted
    ADD CONSTRAINT permuted_pkey PRIMARY KEY (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm);


--
-- TOC entry 3747 (class 2606 OID 91644)
-- Name: note_type pk_note_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_type
    ADD CONSTRAINT pk_note_type PRIMARY KEY (code);


--
-- TOC entry 3681 (class 2606 OID 17121)
-- Name: relation_group pk_relation_group; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.relation_group
    ADD CONSTRAINT pk_relation_group PRIMARY KEY (id_group1, id_thesaurus, relation, id_group2);


--
-- TOC entry 3669 (class 2606 OID 17123)
-- Name: preferences preferences_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 3671 (class 2606 OID 17125)
-- Name: preferences preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_pkey PRIMARY KEY (id_pref);


--
-- TOC entry 3673 (class 2606 OID 17127)
-- Name: preferences preferences_preferredname_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_preferredname_key UNIQUE (preferredname);


--
-- TOC entry 3675 (class 2606 OID 17129)
-- Name: preferences_sparql preferences_sparql_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences_sparql
    ADD CONSTRAINT preferences_sparql_pkey PRIMARY KEY (thesaurus);


--
-- TOC entry 3677 (class 2606 OID 17131)
-- Name: preferred_term preferred_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferred_term
    ADD CONSTRAINT preferred_term_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3679 (class 2606 OID 17133)
-- Name: proposition proposition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposition
    ADD CONSTRAINT proposition_pkey PRIMARY KEY (id_concept, id_user, id_thesaurus);


--
-- TOC entry 3683 (class 2606 OID 17135)
-- Name: roles role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- TOC entry 3685 (class 2606 OID 17137)
-- Name: routine_mail routine_mail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routine_mail
    ADD CONSTRAINT routine_mail_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 3749 (class 2606 OID 91655)
-- Name: status status_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_pkey PRIMARY KEY (id_status);


--
-- TOC entry 3696 (class 2606 OID 17139)
-- Name: term_candidat term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term_candidat
    ADD CONSTRAINT term_candidat_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, contributor);


--
-- TOC entry 3699 (class 2606 OID 91763)
-- Name: term_historique term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term_historique
    ADD CONSTRAINT term_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 3688 (class 2606 OID 17143)
-- Name: term term_id_term_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_key UNIQUE (id_term, lang, id_thesaurus);


--
-- TOC entry 3690 (class 2606 OID 17145)
-- Name: term term_id_term_lexical_value_lang_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_lexical_value_lang_id_thesaurus_key UNIQUE (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 3693 (class 2606 OID 17147)
-- Name: term term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_pkey PRIMARY KEY (id);


--
-- TOC entry 3703 (class 2606 OID 17149)
-- Name: thesaurus_alignement_source thesaurus_alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_alignement_source
    ADD CONSTRAINT thesaurus_alignement_source_pkey PRIMARY KEY (id_thesaurus, id_alignement_source);


--
-- TOC entry 3755 (class 2606 OID 115726)
-- Name: thesaurus_array thesaurus_array_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_array
    ADD CONSTRAINT thesaurus_array_pkey PRIMARY KEY (id_facet, id_thesaurus, id_concept_parent);


--
-- TOC entry 3705 (class 2606 OID 17155)
-- Name: thesaurus_label thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT thesaurus_pkey PRIMARY KEY (id_thesaurus, lang, title);


--
-- TOC entry 3701 (class 2606 OID 17157)
-- Name: thesaurus thesaurus_pkey1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus
    ADD CONSTRAINT thesaurus_pkey1 PRIMARY KEY (id_thesaurus, id_ark);


--
-- TOC entry 3737 (class 2606 OID 40605)
-- Name: thesohomepage thesohomepage_idtheso_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesohomepage
    ADD CONSTRAINT thesohomepage_idtheso_lang_key UNIQUE (idtheso, lang);


--
-- TOC entry 3707 (class 2606 OID 17159)
-- Name: thesaurus_label unique_thesau_lang; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT unique_thesau_lang UNIQUE (id_thesaurus, lang);


--
-- TOC entry 3709 (class 2606 OID 17161)
-- Name: user_group_label user_group-label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_label
    ADD CONSTRAINT "user_group-label_pkey" PRIMARY KEY (id_group);


--
-- TOC entry 3715 (class 2606 OID 91787)
-- Name: user_role_group user_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_group
    ADD CONSTRAINT user_group_pkey UNIQUE (id_user, id_group);


--
-- TOC entry 3711 (class 2606 OID 17163)
-- Name: user_group_thesaurus user_group_thesaurus_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 3713 (class 2606 OID 17165)
-- Name: user_group_thesaurus user_group_thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_pkey PRIMARY KEY (id_group, id_thesaurus);


--
-- TOC entry 3719 (class 2606 OID 17167)
-- Name: users user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT user_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3717 (class 2606 OID 17169)
-- Name: user_role_only_on user_role_only_on_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_only_on
    ADD CONSTRAINT user_role_only_on_pkey PRIMARY KEY (id_user, id_role, id_theso);


--
-- TOC entry 3731 (class 2606 OID 17173)
-- Name: users_historique users_historique_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users_historique
    ADD CONSTRAINT users_historique_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3725 (class 2606 OID 17175)
-- Name: users2 users_login_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_login_key UNIQUE (login);


--
-- TOC entry 3727 (class 2606 OID 17177)
-- Name: users2 users_mail_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_mail_key UNIQUE (mail);


--
-- TOC entry 3721 (class 2606 OID 17179)
-- Name: users users_mail_key1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_mail_key1 UNIQUE (mail);


--
-- TOC entry 3729 (class 2606 OID 17181)
-- Name: users2 users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3723 (class 2606 OID 17183)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 3599 (class 1259 OID 17184)
-- Name: concept_notation_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX concept_notation_unaccent ON public.concept USING gin (public.f_unaccent(lower((notation)::text)) public.gin_trgm_ops);


--
-- TOC entry 3686 (class 1259 OID 17185)
-- Name: index_lexical_value; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value ON public.term USING btree (lexical_value);


--
-- TOC entry 3697 (class 1259 OID 17186)
-- Name: index_lexical_value_copy; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value_copy ON public.term_historique USING btree (lexical_value);


--
-- TOC entry 3646 (class 1259 OID 17187)
-- Name: index_lexical_value_npt; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value_npt ON public.non_preferred_term USING btree (lexical_value);


--
-- TOC entry 3652 (class 1259 OID 17188)
-- Name: note_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX note_lexical_value_unaccent ON public.note USING gin (public.f_unaccent(lower((lexicalvalue)::text)) public.gin_trgm_ops);


--
-- TOC entry 3665 (class 1259 OID 17189)
-- Name: permuted_lexical_value_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX permuted_lexical_value_idx ON public.permuted USING btree (lexical_value);


--
-- TOC entry 3649 (class 1259 OID 17190)
-- Name: term_lexical_value_npt_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX term_lexical_value_npt_unaccent ON public.non_preferred_term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 3691 (class 1259 OID 17191)
-- Name: term_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX term_lexical_value_unaccent ON public.term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 3694 (class 1259 OID 17192)
-- Name: terms_values_gin; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX terms_values_gin ON public.term USING gin (lexical_value public.gin_trgm_ops);


-- Completed on 2021-02-03 11:24:19 CET

--
-- PostgreSQL database dump complete
--

