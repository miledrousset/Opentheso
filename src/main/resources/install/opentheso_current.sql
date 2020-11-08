--
-- PostgreSQL database dump
--

-- Dumped from database version 11.5
-- Dumped by pg_dump version 12.2

-- Started on 2020-10-16 14:55:16 CEST


SET role = opentheso;


-- testé sous Mac osx, il faut adapter les SET si windows
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
-- TOC entry 284 (class 1259 OID 69545)
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidat_messages_id_message_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 285 (class 1259 OID 69547)
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
-- TOC entry 286 (class 1259 OID 90025)
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
-- TOC entry 288 (class 1259 OID 90528)
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
-- TOC entry 287 (class 1259 OID 90526)
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
-- TOC entry 3985 (class 0 OID 0)
-- Dependencies: 287
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
    id_handle character varying DEFAULT ''::character varying
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
-- TOC entry 211 (class 1259 OID 16580)
-- Name: concept_fusion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_fusion (
    id_concept1 character varying NOT NULL,
    id_concept2 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL
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
    id_handle character varying DEFAULT ''::character varying
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
-- TOC entry 224 (class 1259 OID 16652)
-- Name: concept_orphan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_orphan (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 225 (class 1259 OID 16658)
-- Name: concept_term_candidat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_term_candidat (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 226 (class 1259 OID 16664)
-- Name: copyright; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.copyright (
    id_thesaurus character varying NOT NULL,
    copyright character varying
);


--
-- TOC entry 282 (class 1259 OID 69360)
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
-- TOC entry 227 (class 1259 OID 16670)
-- Name: custom_concept_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custom_concept_attribute (
    "idConcept" character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 228 (class 1259 OID 16676)
-- Name: custom_term_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custom_term_attribute (
    identifier character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 229 (class 1259 OID 16682)
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
-- TOC entry 230 (class 1259 OID 16689)
-- Name: facet_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.facet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 231 (class 1259 OID 16691)
-- Name: gps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gps (
    id_concept character varying NOT NULL,
    id_theso character varying NOT NULL,
    latitude double precision,
    longitude double precision
);


--
-- TOC entry 232 (class 1259 OID 16697)
-- Name: gps_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gps_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 233 (class 1259 OID 16699)
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
-- TOC entry 234 (class 1259 OID 16709)
-- Name: hierarchical_relationship; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hierarchical_relationship (
    id_concept1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    role character varying NOT NULL,
    id_concept2 character varying NOT NULL
);


--
-- TOC entry 235 (class 1259 OID 16715)
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
-- TOC entry 280 (class 1259 OID 40590)
-- Name: homepage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.homepage (
    htmlcode character varying,
    lang character varying
);


--
-- TOC entry 236 (class 1259 OID 16722)
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
-- TOC entry 237 (class 1259 OID 16729)
-- Name: info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.info (
    version_opentheso character varying NOT NULL,
    version_bdd character varying NOT NULL
);


--
-- TOC entry 238 (class 1259 OID 16735)
-- Name: languages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.languages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 292 (class 1259 OID 91656)
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
-- TOC entry 239 (class 1259 OID 16744)
-- Name: node_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.node_label (
    facet_id integer NOT NULL,
    id_thesaurus character varying NOT NULL,
    lexical_value character varying,
    created timestamp with time zone DEFAULT now() NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    lang character varying NOT NULL
);


--
-- TOC entry 240 (class 1259 OID 16752)
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
-- TOC entry 241 (class 1259 OID 16761)
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
-- TOC entry 242 (class 1259 OID 16769)
-- Name: note__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 243 (class 1259 OID 16771)
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
-- TOC entry 244 (class 1259 OID 16780)
-- Name: note_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 245 (class 1259 OID 16782)
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
-- TOC entry 289 (class 1259 OID 91636)
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
-- TOC entry 246 (class 1259 OID 16797)
-- Name: nt_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.nt_type (
    id integer NOT NULL,
    relation character varying,
    description_fr character varying,
    description_en character varying
);


--
-- TOC entry 247 (class 1259 OID 16803)
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
-- TOC entry 248 (class 1259 OID 16809)
-- Name: pref__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pref__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 249 (class 1259 OID 16811)
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
    sort_by_notation boolean DEFAULT false
);


--
-- TOC entry 250 (class 1259 OID 16844)
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
-- TOC entry 251 (class 1259 OID 16851)
-- Name: preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.preferred_term (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 252 (class 1259 OID 16857)
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
-- TOC entry 253 (class 1259 OID 16865)
-- Name: relation_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.relation_group (
    id_group1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    relation character varying NOT NULL,
    id_group2 character varying NOT NULL
);


--
-- TOC entry 254 (class 1259 OID 16871)
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying,
    description character varying
);


--
-- TOC entry 255 (class 1259 OID 16877)
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3986 (class 0 OID 0)
-- Dependencies: 255
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.role_id_seq OWNED BY public.roles.id;


--
-- TOC entry 256 (class 1259 OID 16879)
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
-- TOC entry 257 (class 1259 OID 16886)
-- Name: split_non_preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.split_non_preferred_term (
);


--
-- TOC entry 291 (class 1259 OID 91647)
-- Name: status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.status (
    id_status integer NOT NULL,
    value text
);


--
-- TOC entry 283 (class 1259 OID 69543)
-- Name: status_id_status_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.status_id_status_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 290 (class 1259 OID 91645)
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
-- TOC entry 3987 (class 0 OID 0)
-- Dependencies: 290
-- Name: status_id_status_seq1; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.status_id_status_seq1 OWNED BY public.status.id_status;


--
-- TOC entry 258 (class 1259 OID 16889)
-- Name: term__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 259 (class 1259 OID 16891)
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
-- TOC entry 260 (class 1259 OID 16901)
-- Name: term_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 261 (class 1259 OID 16903)
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
-- TOC entry 262 (class 1259 OID 16912)
-- Name: term_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 263 (class 1259 OID 16914)
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
-- TOC entry 264 (class 1259 OID 16923)
-- Name: thesaurus_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.thesaurus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 265 (class 1259 OID 16925)
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
-- TOC entry 266 (class 1259 OID 16935)
-- Name: thesaurus_alignement_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus_alignement_source (
    id_thesaurus character varying NOT NULL,
    id_alignement_source integer NOT NULL
);


--
-- TOC entry 267 (class 1259 OID 16941)
-- Name: thesaurus_array; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus_array (
    facet_id integer DEFAULT nextval('public.facet_id_seq'::regclass) NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_concept_parent character varying NOT NULL,
    ordered boolean DEFAULT false NOT NULL,
    notation character varying
);


--
-- TOC entry 268 (class 1259 OID 16949)
-- Name: thesaurus_array_concept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus_array_concept (
    thesaurusarrayid integer NOT NULL,
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    arrayorder integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 269 (class 1259 OID 16956)
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
-- TOC entry 281 (class 1259 OID 40596)
-- Name: thesohomepage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesohomepage (
    htmlcode character varying,
    lang character varying,
    idtheso character varying
);


--
-- TOC entry 270 (class 1259 OID 16964)
-- Name: user__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 271 (class 1259 OID 16966)
-- Name: user_group_label__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_group_label__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 272 (class 1259 OID 16968)
-- Name: user_group_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_group_label (
    id_group integer DEFAULT nextval('public.user_group_label__id_seq'::regclass) NOT NULL,
    label_group character varying
);


--
-- TOC entry 273 (class 1259 OID 16975)
-- Name: user_group_thesaurus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_group_thesaurus (
    id_group integer NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 274 (class 1259 OID 16981)
-- Name: user_role_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role_group (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_group integer NOT NULL
);


--
-- TOC entry 275 (class 1259 OID 16984)
-- Name: user_role_only_on; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role_only_on (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_theso character varying NOT NULL,
    id_theso_domain character varying DEFAULT 'all'::character varying NOT NULL
);


--
-- TOC entry 276 (class 1259 OID 16991)
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
-- TOC entry 277 (class 1259 OID 17002)
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
-- TOC entry 278 (class 1259 OID 17011)
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
-- TOC entry 279 (class 1259 OID 17019)
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
-- TOC entry 3577 (class 2604 OID 90531)
-- Name: candidat_vote id_vote; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_vote ALTER COLUMN id_vote SET DEFAULT nextval('public.candidat_vote_id_vote_seq'::regclass);


--
-- TOC entry 3541 (class 2604 OID 91766)
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);


--
-- TOC entry 3579 (class 2604 OID 91650)
-- Name: status id_status; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status ALTER COLUMN id_status SET DEFAULT nextval('public.status_id_status_seq1'::regclass);


--
-- TOC entry 3884 (class 0 OID 16507)
-- Dependencies: 199
-- Data for Name: alignement; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.alignement (id, created, modified, author, concept_target, thesaurus_target, uri_target, alignement_id_type, internal_id_thesaurus, internal_id_concept, id_alignement_source) VALUES (18, '2020-10-09 08:21:39.535769', '2020-10-09 08:21:39.535769', 1, '', '', 'http://www.wikidata.org/entity/Q456', 1, 'th1', '47612', NULL);
INSERT INTO public.alignement (id, created, modified, author, concept_target, thesaurus_target, uri_target, alignement_id_type, internal_id_thesaurus, internal_id_concept, id_alignement_source) VALUES (19, '2020-10-09 08:21:39.546365', '2020-10-09 08:21:39.546365', 1, '', '', 'https://www.geonames.org/2996944', 1, 'th1', '47612', NULL);
INSERT INTO public.alignement (id, created, modified, author, concept_target, thesaurus_target, uri_target, alignement_id_type, internal_id_thesaurus, internal_id_concept, id_alignement_source) VALUES (20, '2020-10-09 08:21:39.608692', '2020-10-09 08:21:39.608692', 1, '', '', 'http://www.wikidata.org/entity/Q4989906', 1, 'th1', '31', NULL);


--
-- TOC entry 3886 (class 0 OID 16518)
-- Dependencies: 201
-- Data for Name: alignement_preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3888 (class 0 OID 16527)
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
-- TOC entry 3889 (class 0 OID 16535)
-- Dependencies: 204
-- Data for Name: alignement_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (1, 'Equivalence exacte', '=EQ', 'exactMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (2, 'Equivalence inexacte', '~EQ', 'closeMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (3, 'Equivalence générique', 'EQB', 'broadMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (4, 'Equivalence associative', 'EQR', 'relatedMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (5, 'Equivalence spécifique', 'EQS', 'narrowMatch');


--
-- TOC entry 3890 (class 0 OID 16541)
-- Dependencies: 205
-- Data for Name: bt_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (1, 'BT', 'Terme générique', 'Broader term');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (2, 'BTG', 'Terme générique (generic)', 'Broader term (generic)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (3, 'BTP', 'Terme générique (partitive)', 'Broader term (partitive)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (4, 'BTI', 'Terme générique (instance)', 'Broader term (instance)');


--
-- TOC entry 3970 (class 0 OID 69547)
-- Dependencies: 285
-- Data for Name: candidat_messages; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.candidat_messages (id_message, value, id_user, id_concept, id_thesaurus, date) VALUES (1, 'un concept utile pour moi', 1, 36, 'th1', '2020-10-08 14:26');
INSERT INTO public.candidat_messages (id_message, value, id_user, id_concept, id_thesaurus, date) VALUES (2, 'mon concept', 1, 51, 'th1', '2020-10-08 15:37');
INSERT INTO public.candidat_messages (id_message, value, id_user, id_concept, id_thesaurus, date) VALUES (3, 'test', 1, 74, 'th1', '2020-10-15 13:10');
INSERT INTO public.candidat_messages (id_message, value, id_user, id_concept, id_thesaurus, date) VALUES (4, 'test_autre', 1, 74, 'th1', '2020-10-15 13:10');
INSERT INTO public.candidat_messages (id_message, value, id_user, id_concept, id_thesaurus, date) VALUES (5, 'test2', 1, 74, 'th1', '2020-10-15 13:30');


--
-- TOC entry 3971 (class 0 OID 90025)
-- Dependencies: 286
-- Data for Name: candidat_status; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.candidat_status (id_concept, id_status, date, id_user, id_thesaurus, message, id_user_admin) VALUES ('36', 2, '2020-10-08', 1, 'th1', 'ok pour intégration', 1);
INSERT INTO public.candidat_status (id_concept, id_status, date, id_user, id_thesaurus, message, id_user_admin) VALUES ('51', 2, '2020-10-08', 1, 'th1', 'ok', 1);
INSERT INTO public.candidat_status (id_concept, id_status, date, id_user, id_thesaurus, message, id_user_admin) VALUES ('74', 1, '2020-10-15', 1, 'th1', NULL, NULL);


--
-- TOC entry 3973 (class 0 OID 90528)
-- Dependencies: 288
-- Data for Name: candidat_vote; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.candidat_vote (id_vote, id_user, id_concept, id_thesaurus, type_vote, id_note) VALUES (3, 1, '36', 'th1', 'CA', 'null');
INSERT INTO public.candidat_vote (id_vote, id_user, id_concept, id_thesaurus, type_vote, id_note) VALUES (4, 1, '36', 'th1', 'NT', '28');
INSERT INTO public.candidat_vote (id_vote, id_user, id_concept, id_thesaurus, type_vote, id_note) VALUES (5, 1, '51', 'th1', 'CA', 'null');
INSERT INTO public.candidat_vote (id_vote, id_user, id_concept, id_thesaurus, type_vote, id_note) VALUES (6, 1, '51', 'th1', 'NT', '54');


--
-- TOC entry 3891 (class 0 OID 16547)
-- Dependencies: 206
-- Data for Name: compound_equivalence; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3893 (class 0 OID 16555)
-- Dependencies: 208
-- Data for Name: concept; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('34', 'th1', '', '2020-10-08 00:00:00+02', '2020-10-08 00:00:00+02', 'D', '', true, 52, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('47612', 'th1', '', '2020-09-24 00:00:00+02', '2020-10-08 00:00:00+02', 'D', 'note', false, 53, true, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('47609', 'th1', '', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', 'D', 'C2', false, 55, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('47608', 'th1', '', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', 'D', 'C1', false, 56, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('47611', 'th1', '', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', 'D', '', false, 60, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('47613', 'th1', '', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', 'D', '', false, 61, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('32', 'th1', '', '2020-10-08 00:00:00+02', '2020-10-08 00:00:00+02', 'D', '', false, 63, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('33', 'th1', '', '2020-10-08 00:00:00+02', '2020-10-08 00:00:00+02', 'D', '', false, 64, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('30', 'th1', '', '2020-10-08 00:00:00+02', '2020-10-11 00:00:00+02', 'D', '', true, 62, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('47606', 'th1', '', '2020-09-24 00:00:00+02', '2020-10-13 00:00:00+02', 'D', 'T1', true, 57, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('47607', 'th1', '', '2020-09-24 00:00:00+02', '2020-10-13 00:00:00+02', 'D', '', true, 59, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('66', 'th1', '', '2020-10-14 16:48:03.857999+02', '2020-10-14 16:48:03.857999+02', 'D', '', false, 66, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('67', 'th1', '', '2020-10-14 16:48:07.935253+02', '2020-10-14 16:48:07.935253+02', 'D', '', false, 67, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('68', 'th1', '', '2020-10-14 16:48:46.29588+02', '2020-10-14 16:48:46.29588+02', 'D', '', false, 68, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('31', 'th1', '', '2020-10-08 00:00:00+02', '2020-10-08 00:00:00+02', 'D', 'M1', false, 54, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('65', 'th1', '', '2020-10-14 10:33:51.778996+02', '2020-10-15 00:00:00+02', 'D', 'BO1', false, 65, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('47610', 'th1', '', '2020-09-24 00:00:00+02', '2020-10-15 00:00:00+02', 'D', 'C11', false, 58, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('69', 'th1', '', '2020-10-15 09:05:19.696467+02', '2020-10-15 09:05:19.696467+02', 'D', '', false, 69, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('70', 'th1', '', '2020-10-15 09:07:38.970353+02', '2020-10-15 09:07:38.970353+02', 'D', '', false, 70, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('71', 'th1', '', '2020-10-15 09:22:49.837451+02', '2020-10-15 09:22:49.837451+02', 'D', '', false, 71, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('73', 'th1', '', '2020-10-15 09:55:29.897473+02', '2020-10-15 09:55:29.897473+02', 'D', '', false, 73, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('74', 'th1', '', '2020-10-15 13:09:21.28884+02', '2020-10-15 13:09:21.28884+02', 'CA', '', false, 74, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('75', 'th1', '', '2020-10-15 15:37:46.052005+02', '2020-10-15 15:37:46.052005+02', 'D', '', false, 75, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('76', 'th1', '', '2020-10-15 16:06:40.695307+02', '2020-10-15 16:06:40.695307+02', 'D', '', false, 76, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('72', 'th1', '', '2020-10-15 09:23:06.016477+02', '2020-10-15 00:00:00+02', 'D', '', false, 72, false, '');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle) VALUES ('77', 'th1', '', '2020-10-16 09:38:58.688215+02', '2020-10-16 09:38:58.688215+02', 'D', '', false, 77, false, '');


--
-- TOC entry 3895 (class 0 OID 16570)
-- Dependencies: 210
-- Data for Name: concept_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3896 (class 0 OID 16580)
-- Dependencies: 211
-- Data for Name: concept_fusion; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3898 (class 0 OID 16589)
-- Dependencies: 213
-- Data for Name: concept_group; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group (idgroup, id_ark, idthesaurus, idtypecode, notation, id, numerotation, id_handle) VALUES ('G11', '', 'th1', 'C', '', 12, NULL, '');
INSERT INTO public.concept_group (idgroup, id_ark, idthesaurus, idtypecode, notation, id, numerotation, id_handle) VALUES ('G19', '', 'th1', 'C', '', 20, NULL, '');
INSERT INTO public.concept_group (idgroup, id_ark, idthesaurus, idtypecode, notation, id, numerotation, id_handle) VALUES ('G41', '', 'th1', 'C', '', 42, NULL, '');
INSERT INTO public.concept_group (idgroup, id_ark, idthesaurus, idtypecode, notation, id, numerotation, id_handle) VALUES ('G45', '', 'th1', 'C', '', 46, NULL, '');


--
-- TOC entry 3899 (class 0 OID 16598)
-- Dependencies: 214
-- Data for Name: concept_group_concept; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('G19', 'th1', '47607');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('G19', 'th1', '47610');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('G19', 'th1', '47611');


--
-- TOC entry 3901 (class 0 OID 16606)
-- Dependencies: 216
-- Data for Name: concept_group_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3903 (class 0 OID 16616)
-- Dependencies: 218
-- Data for Name: concept_group_label; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_label (id, lexicalvalue, created, modified, lang, idthesaurus, idgroup) VALUES (8, 'sous_coll', '2020-10-10 00:00:00', '2020-10-10 00:00:00', 'fr', 'th1', 'G11');
INSERT INTO public.concept_group_label (id, lexicalvalue, created, modified, lang, idthesaurus, idgroup) VALUES (12, 'testCol1', '2020-10-11 00:00:00', '2020-10-11 00:00:00', 'fr', 'th1', 'G19');
INSERT INTO public.concept_group_label (id, lexicalvalue, created, modified, lang, idthesaurus, idgroup) VALUES (23, 'coll2', '2020-10-14 00:00:00', '2020-10-14 00:00:00', 'fr', 'th1', 'G41');
INSERT INTO public.concept_group_label (id, lexicalvalue, created, modified, lang, idthesaurus, idgroup) VALUES (25, 'sous_coll11', '2020-10-14 00:00:00', '2020-10-14 00:00:00', 'fr', 'th1', 'G45');


--
-- TOC entry 3905 (class 0 OID 16627)
-- Dependencies: 220
-- Data for Name: concept_group_label_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (6, 'collection1', '2020-10-09 08:21:39.688072', 'fr', 'th1', 'G392', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (7, 'coll2', '2020-10-10 05:27:46.433433', 'fr', 'th1', 'G9', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (8, 'sous_coll', '2020-10-10 05:41:02.996869', 'fr', 'th1', 'G11', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (9, 'coll2', '2020-10-11 15:02:26.491241', 'fr', 'th1', 'G13', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (10, 'coll2', '2020-10-11 15:07:54.473037', 'fr', 'th1', 'G15', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (11, 'coll2', '2020-10-11 15:27:09.426023', 'fr', 'th1', 'G17', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (12, 'testCol1', '2020-10-11 15:43:36.228648', 'fr', 'th1', 'G19', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (13, 'Sous_testCol1', '2020-10-11 15:44:12.472632', 'fr', 'th1', 'G21', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (14, 'sous_testCol1', '2020-10-13 07:33:17.779424', 'fr', 'th1', 'G23', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (15, 'coll2', '2020-10-13 07:33:49.349201', 'fr', 'th1', 'G25', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (16, 'coll3', '2020-10-14 07:35:29.147377', 'fr', 'th1', 'G27', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (17, 'coll4', '2020-10-14 07:44:21.122308', 'fr', 'th1', 'G29', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (18, 'coll5', '2020-10-14 07:47:18.255913', 'fr', 'th1', 'G31', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (19, 'coll6', '2020-10-14 08:05:16.732508', 'fr', 'th1', 'G33', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (20, 'coll7', '2020-10-14 08:08:44.202003', 'fr', 'th1', 'G35', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (21, 'coll8', '2020-10-14 08:09:45.800258', 'fr', 'th1', 'G37', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (22, 'coll2', '2020-10-14 12:06:15.575579', 'fr', 'th1', 'G39', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (23, 'coll2', '2020-10-14 12:06:42.50262', 'fr', 'th1', 'G41', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (24, 'sous_coll2', '2020-10-14 12:06:55.773351', 'fr', 'th1', 'G43', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (25, 'sous_coll1', '2020-10-14 13:14:51.74073', 'fr', 'th1', 'G45', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (26, 'sous_coll11', '2020-10-14 16:46:18.540186', 'fr', 'th1', 'G45', 1);


--
-- TOC entry 3906 (class 0 OID 16635)
-- Dependencies: 221
-- Data for Name: concept_group_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('MT', 'Microthesaurus', 'MicroThesaurus');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('G', 'Group', 'ConceptGroup');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('C', 'Collection', 'Collection');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('T', 'Theme', 'Theme');


--
-- TOC entry 3908 (class 0 OID 16643)
-- Dependencies: 223
-- Data for Name: concept_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('65', 'th1', '', '2020-10-14 10:33:51.778996+02', 'D', '', false, '', 23, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('66', 'th1', '', '2020-10-14 16:48:03.857999+02', 'D', '', false, '', 24, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('67', 'th1', '', '2020-10-14 16:48:07.935253+02', 'D', '', false, '', 25, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('68', 'th1', '', '2020-10-14 16:48:46.29588+02', 'D', '', false, '', 26, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('69', 'th1', '', '2020-10-15 09:05:19.696467+02', 'D', '', false, '', 27, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('70', 'th1', '', '2020-10-15 09:07:38.970353+02', 'D', '', false, '', 28, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('71', 'th1', '', '2020-10-15 09:22:49.837451+02', 'D', '', false, '', 29, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('72', 'th1', '', '2020-10-15 09:23:06.016477+02', 'D', '', false, '', 30, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('73', 'th1', '', '2020-10-15 09:55:29.897473+02', 'D', '', false, '', 31, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('74', 'th1', '', '2020-10-15 13:09:21.295334+02', 'CA', '', false, 'null', 32, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('75', 'th1', '', '2020-10-15 15:37:46.052005+02', 'D', '', false, '', 33, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('76', 'th1', '', '2020-10-15 16:06:40.695307+02', 'D', '', false, '', 34, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('77', 'th1', '', '2020-10-16 09:38:58.688215+02', 'D', '', false, '', 35, 1);


--
-- TOC entry 3909 (class 0 OID 16652)
-- Dependencies: 224
-- Data for Name: concept_orphan; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3910 (class 0 OID 16658)
-- Dependencies: 225
-- Data for Name: concept_term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3911 (class 0 OID 16664)
-- Dependencies: 226
-- Data for Name: copyright; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3967 (class 0 OID 69360)
-- Dependencies: 282
-- Data for Name: corpus_link; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.corpus_link (id_theso, corpus_name, uri_count, uri_link, active) VALUES ('th9', 'Frantiq', 'https://pro.frantiq.fr/es/koha_frantiq_biblios/_count?q=koha-auth-number:##id##', 'https://catalogue.frantiq.fr/cgi-bin/koha/opac-search.pl?q=an:##id##', true);
INSERT INTO public.corpus_link (id_theso, corpus_name, uri_count, uri_link, active) VALUES ('th9', 'Frantiq2', 'https://pro.frantiq.fr/es/koha_frantiq_biblios/_count?q=koha-auth-number:##id##', 'https://catalogue.frantiq.fr/cgi-bin/koha/opac-search.pl?q=an:##id##', false);


--
-- TOC entry 3912 (class 0 OID 16670)
-- Dependencies: 227
-- Data for Name: custom_concept_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3913 (class 0 OID 16676)
-- Dependencies: 228
-- Data for Name: custom_term_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3914 (class 0 OID 16682)
-- Dependencies: 229
-- Data for Name: external_images; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('16', 'th1', '', 'Wikidata', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/Paris - Eiffelturm und Marsfeld2.jpg');
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('47612', 'th1', '', '', 1, 'https://commons.wikimedia.org/wiki/Special:FilePath/01. Panorama de Lyon pris depuis le toit de la Basilique de Fourvière.jpg');
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('13', 'th1', '', 'Wikidata', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/Torun Kopernika 21 pietro strop (2).jpg');
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('13', 'th1', '', 'Wikidata', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/Bending.svg');
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('13', 'th1', '', 'Wikidata', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/Balken.jpg');
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('18', 'th1', 'statues', 'bnf', 1, 'https://gallica.bnf.fr/ark:/12148/btv1b531928307/f1.highres');
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('20', 'th1', '', 'Wikidata', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/Wikipedia Monument 2.JPG');
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('35', 'th1', '', 'Wikidata', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/Paris - Eiffelturm und Marsfeld2.jpg');
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('32', 'th1', '', 'Wikidata', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/MG-Paris-Aphrodite of Milos.jpg');
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri) VALUES ('50', 'th1', '', 'Wikidata', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/Paris - Eiffelturm und Marsfeld2.jpg');


--
-- TOC entry 3916 (class 0 OID 16691)
-- Dependencies: 231
-- Data for Name: gps; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3918 (class 0 OID 16699)
-- Dependencies: 233
-- Data for Name: gps_preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3919 (class 0 OID 16709)
-- Dependencies: 234
-- Data for Name: hierarchical_relationship; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47612', 'th1', 'RT', '47613');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47613', 'th1', 'RT', '47612');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47612', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '47612');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('31', 'th1', 'NT', '33');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('33', 'th1', 'BT', '31');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('31', 'th1', 'BT', '30');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('30', 'th1', 'NT', '31');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47609', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '47609');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47608', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '47608');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '47613');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47613', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47610', 'th1', 'BT', '47607');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47607', 'th1', 'NT', '47610');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47607', 'th1', 'NT', '47611');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47611', 'th1', 'BT', '47607');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('30', 'th1', 'NT', '32');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('32', 'th1', 'BT', '30');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '65');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('65', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '66');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('66', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '67');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('67', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '68');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('68', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '69');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('69', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '70');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('70', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '71');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('71', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '72');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('72', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '73');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('73', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '75');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('75', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '76');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('76', 'th1', 'BT', '47606');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('47606', 'th1', 'NT', '77');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('77', 'th1', 'BT', '47606');


--
-- TOC entry 3920 (class 0 OID 16715)
-- Dependencies: 235
-- Data for Name: hierarchical_relationship_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '65', '2020-10-14 10:33:51.778996+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('65', 'th1', 'BT', '47606', '2020-10-14 10:33:51.778996+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '66', '2020-10-14 16:48:03.857999+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('66', 'th1', 'BT', '47606', '2020-10-14 16:48:03.857999+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '67', '2020-10-14 16:48:07.935253+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('67', 'th1', 'BT', '47606', '2020-10-14 16:48:07.935253+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '68', '2020-10-14 16:48:46.29588+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('68', 'th1', 'BT', '47606', '2020-10-14 16:48:46.29588+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '69', '2020-10-15 09:05:19.696467+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('69', 'th1', 'BT', '47606', '2020-10-15 09:05:19.696467+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '70', '2020-10-15 09:07:38.970353+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('70', 'th1', 'BT', '47606', '2020-10-15 09:07:38.970353+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '71', '2020-10-15 09:22:49.837451+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('71', 'th1', 'BT', '47606', '2020-10-15 09:22:49.837451+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '72', '2020-10-15 09:23:06.016477+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('72', 'th1', 'BT', '47606', '2020-10-15 09:23:06.016477+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '73', '2020-10-15 09:55:29.897473+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('73', 'th1', 'BT', '47606', '2020-10-15 09:55:29.897473+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '75', '2020-10-15 15:37:46.052005+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('75', 'th1', 'BT', '47606', '2020-10-15 15:37:46.052005+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '76', '2020-10-15 16:06:40.695307+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('76', 'th1', 'BT', '47606', '2020-10-15 16:06:40.695307+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('47606', 'th1', 'NT', '77', '2020-10-16 09:38:58.688215+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('77', 'th1', 'BT', '47606', '2020-10-16 09:38:58.688215+02', 1, 'ADD');


--
-- TOC entry 3965 (class 0 OID 40590)
-- Dependencies: 280
-- Data for Name: homepage; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.homepage (htmlcode, lang) VALUES ('<h1><img src="data:image/png;base64,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"> Opentheso</h1><h3>Copyright ©CNRS</h3><p>english</p><p>Opentheso est distribué sous licence <a href="http://www.cecill.info/licences.fr.html" target="_blank">CeCILL_C</a>, Licence libre de droit français compatible avec la licence <a href="http://www.gnu.org/copyleft/gpl.html" target="_blank">GNU GPL</a></p><p>C''est un gestionnaire de thesaurus multilingue, développé par la plateforme Technologique <a href="https://www.mom.fr/plateformes-technologiques/web-semantique-et-thesauri" target="_blank">WST</a> (Web Sémantique &amp; Thesauri) située à la <a href="https://www.mom.fr" target="_blank">MOM</a></p><p>en partenariat avec le <a href="http://www.frantiq.fr" target="_blank">GDS-FRANTIQ</a></p><p><br></p><p>Le développement des versions 3 et 4 a bénéficié d’une participation financière du Consortium <a href="http://masa.hypotheses.org/" target="_blank">MASA</a>(Mémoire des archéologues et des Sites Archéologiques) de la <a href="http://www.huma-num.fr/" target="_blank">TGIR Huma-Num</a>, ce financement a permis de produire une version FullWeb qui respecte la nouvelle norme des thésaurus ISO 25964.</p><p>Chef de Projet : <strong>Miled Rousset</strong></p><p>Développement : <strong>Miled Rousset</strong></p><p>Contributeurs : <strong>Prudham Jean-Marc, Quincy Mbape Eyoke, Antonio Perez, Carole Bonfré</strong></p><p>Partenariat, test et expertise : <strong>Les équipes du réseau </strong><a href="http://www.frantiq.fr" target="_blank"><strong>Frantiq</strong></a></p><p><br></p><p>Le développement a été réalisé avec les technologies suivantes :</p><ul><li>PostgreSQL pour la base des données</li><li>Java pour le module API et module métier</li><li>JSF2 et PrimeFaces pour la partie graphique</li></ul><p><br></p><p><strong>Opentheso</strong> s''appuie sur le projet <a href="http://ark.mom.fr" target="_blank">Arkéo</a> de la MOM pour générer des identifiants type <a href="http://fr.wikipedia.org/wiki/Archival_Resource_Key" target="_blank">ARK</a></p><p>Modules complémentaires :</p><ul><li><a href="https://github.com/brettwooldridge/HikariCP" target="_blank"><strong>Hikari</strong></a></li><li><a href="http://rdf4j.org/" target="_blank"><strong>RDF4J</strong></a></li><li>Kj-jzkit</li><li>...</li></ul><p>Partenaires :</p><ul><li><a href="http://www.cnrs.fr" target="_blank">CNRS</a></li><li><a href="http://www.mom.fr" target="_blank">MOM</a></li><li><a href="http://www.frantiq.fr" target="_blank">Frantiq</a></li><li><a href="http://www.mae.u-paris10.fr" target="_blank">MAE</a></li><li><a href="http://masa.hypotheses.org/" target="_blank">MASA</a></li><li><a href="http://www.huma-num.fr" target="_blank">Huma-Num</a></li></ul><p><br></p>', 'en');
INSERT INTO public.homepage (htmlcode, lang) VALUES ('<h1><img src="data:image/png;base64,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"></h1><h1>Opentheso</h1><p>Copyright ©CNRS</p><p><br></p><p><span class="ql-size-large">Aide et tutoriels : </span><a href="https://opentheso.hypotheses.org" rel="noopener noreferrer" target="_blank" class="ql-size-large">https://opentheso.hypotheses.org</a></p><p><strong style="color: rgb(230, 0, 0);">!!!!! Pour commencer, sélectionnez un thésaurus en haut à droite !!!!!</strong></p><p>Opentheso est distribué en licence libre de droit français compatible avec la licence <a href="http://www.gnu.org/copyleft/gpl.html" rel="noopener noreferrer" target="_blank">GNU GPL</a></p><p>C''est un gestionnaire de thesaurus multilingue, développé par la plateforme Technologique <a href="https://www.mom.fr/plateformes-technologiques/web-semantique-et-thesauri" rel="noopener noreferrer" target="_blank">WST</a> (Web Sémantique &amp; Thesauri) située à la <a href="https://www.mom.fr" rel="noopener noreferrer" target="_blank">MOM</a></p><p>en partenariat avec le <a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank">GDS-FRANTIQ</a></p><p><br></p><p><span style="color: rgb(0, 0, 0);">Designer : Brann Etienne</span><strong style="color: rgb(0, 0, 0);"> (</strong><a href="http://ithaqstudio.com/" rel="noopener noreferrer" target="_blank" style="color: rgb(149, 79, 114);"><strong>ithaqstudio.com</strong></a><strong style="color: rgb(0, 0, 0);">) </strong></p><p>Intégrateur du design : Miled Rousset</p><p>Version actuelle 20.06 (Beta) pour test ...</p><p><br></p><p>Le développement d''Opentheso est soutenu en partie par le Consortium <a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank">MASA </a>(Mémoire des archéologues et des Sites Archéologiques) de la <a href="http://www.huma-num.fr/" rel="noopener noreferrer" target="_blank">TGIR Huma-Num.</a></p><p>Chef de Projet : <strong>Miled Rousset</strong></p><p>Développement : <strong>Miled Rousset</strong></p><p>Contributeurs : <strong>Prudham Jean-Marc, Quincy Mbape Eyoke, Antonio Perez, Carole Bonfré</strong></p><p>Partenariat, test et expertise : <strong>Les équipes du réseau </strong><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank"><strong>Frantiq</strong></a> et en particulier le groupe <a href="https://www.frantiq.fr/frantiq/organisation/groupes-de-travail-et-projets/pactols-opentheso/" rel="noopener noreferrer" target="_blank">PACTOLS</a>.</p><p>Le développement a été réalisé avec les technologies suivantes :</p><ul><li>PostgreSQL pour la base des données</li><li>Java pour le module API et module métier</li><li>JSF2 et PrimeFaces pour la partie graphique</li></ul><p><br></p><p><strong>Opentheso</strong> s''appuie sur le projet <a href="http://ark.mom.fr" rel="noopener noreferrer" target="_blank">Arkéo</a> de la MOM pour générer des identifiants type <a href="http://fr.wikipedia.org/wiki/Archival_Resource_Key" rel="noopener noreferrer" target="_blank">ARK</a></p><p>Modules complémentaires :</p><ul><li><a href="https://github.com/brettwooldridge/HikariCP" rel="noopener noreferrer" target="_blank"><strong>Hikari</strong></a></li><li><a href="http://rdf4j.org/" rel="noopener noreferrer" target="_blank"><strong>RDF4J</strong></a></li><li>...</li></ul><p>Partenaires :</p><ul><li><a href="http://www.cnrs.fr" rel="noopener noreferrer" target="_blank">CNRS</a></li><li><a href="http://www.mom.fr" rel="noopener noreferrer" target="_blank">MOM</a></li><li><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank">Frantiq</a></li><li><a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank">MASA</a></li><li><a href="http://www.huma-num.fr" rel="noopener noreferrer" target="_blank">Huma-Num</a></li></ul><p><br></p><p><br></p><p><br></p><h1><br></h1><h1><br></h1><h1><br></h1>', 'fr');


--
-- TOC entry 3921 (class 0 OID 16722)
-- Dependencies: 236
-- Data for Name: images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3922 (class 0 OID 16729)
-- Dependencies: 237
-- Data for Name: info; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3977 (class 0 OID 91656)
-- Dependencies: 292
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
-- TOC entry 3924 (class 0 OID 16744)
-- Dependencies: 239
-- Data for Name: node_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3925 (class 0 OID 16752)
-- Dependencies: 240
-- Data for Name: non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.non_preferred_term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, hiden) VALUES ('47612', 'bonne bouffe', 'fr', 'th1', '2020-10-09 08:21:39.547507+02', '2020-10-09 08:21:39.547507+02', '1', 'Hidden', true);
INSERT INTO public.non_preferred_term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, hiden) VALUES ('47612', 'ville de la gastronomie', 'fr', 'th1', '2020-10-09 08:21:39.569259+02', '2020-10-09 08:21:39.569259+02', '1', 'USE', false);
INSERT INTO public.non_preferred_term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, hiden) VALUES ('33', 'patrimoine architectural', 'fr', 'th1', '2020-10-09 08:21:39.645816+02', '2020-10-09 08:21:39.645816+02', '1', 'USE', false);


--
-- TOC entry 3926 (class 0 OID 16761)
-- Dependencies: 241
-- Data for Name: non_preferred_term_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.non_preferred_term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, hiden, id_user, action) VALUES ('47612', 'bonne bouffe', 'fr', 'th1', '2020-10-09 08:21:39.547507+02', '1', 'Hidden', false, 1, 'ADD');
INSERT INTO public.non_preferred_term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, hiden, id_user, action) VALUES ('47612', 'ville de la gastronomie', 'fr', 'th1', '2020-10-09 08:21:39.569259+02', '1', 'USE', false, 1, 'ADD');
INSERT INTO public.non_preferred_term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, hiden, id_user, action) VALUES ('33', 'patrimoine architectural', 'fr', 'th1', '2020-10-09 08:21:39.645816+02', '1', 'USE', false, 1, 'ADD');


--
-- TOC entry 3928 (class 0 OID 16771)
-- Dependencies: 243
-- Data for Name: note; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user) VALUES (55, 'note', 'th1', NULL, '47612', 'fr', 'note', '2020-10-09 08:21:39.508261', '2020-10-09 08:21:39.508261', 1);
INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user) VALUES (56, 'historyNote', 'th1', NULL, '47612', 'fr', 'note historique', '2020-10-09 08:21:39.529187', '2020-10-09 08:21:39.529187', 1);
INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user) VALUES (57, 'definition', 'th1', '47612', NULL, 'en', 'commune in the metropolis of Lyon, France (Wikidata)', '2020-10-09 08:21:39.53062', '2020-10-09 08:21:39.53062', 1);
INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user) VALUES (58, 'definition', 'th1', '47612', NULL, 'en', 'def_en', '2020-10-09 08:21:39.531942', '2020-10-09 08:21:39.531942', 1);
INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user) VALUES (59, 'definition', 'th1', '47612', NULL, 'fr', 'commune française de la métropole de Lyon (chef-lieu) (Wikidata)', '2020-10-09 08:21:39.533351', '2020-10-09 08:21:39.533351', 1);
INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user) VALUES (60, 'definition', 'th1', '47612', NULL, 'fr', 'definition', '2020-10-09 08:21:39.534558', '2020-10-09 08:21:39.534558', 1);


--
-- TOC entry 3930 (class 0 OID 16782)
-- Dependencies: 245
-- Data for Name: note_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (58, 'note', 'th1', NULL, '47612', 'fr', 'note', '2020-10-09 08:21:39.522312', 1, 'add');
INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (59, 'historyNote', 'th1', NULL, '47612', 'fr', 'note historique', '2020-10-09 08:21:39.529968', 1, 'add');
INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (60, 'definition', 'th1', '47612', NULL, 'en', 'commune in the metropolis of Lyon, France (Wikidata)', '2020-10-09 08:21:39.531335', 1, 'add');
INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (61, 'definition', 'th1', '47612', NULL, 'en', 'def_en', '2020-10-09 08:21:39.532668', 1, 'add');
INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (62, 'definition', 'th1', '47612', NULL, 'fr', 'commune française de la métropole de Lyon (chef-lieu) (Wikidata)', '2020-10-09 08:21:39.534108', 1, 'add');
INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (63, 'definition', 'th1', '47612', NULL, 'fr', 'definition', '2020-10-09 08:21:39.535134', 1, 'add');


--
-- TOC entry 3974 (class 0 OID 91636)
-- Dependencies: 289
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
-- TOC entry 3931 (class 0 OID 16797)
-- Dependencies: 246
-- Data for Name: nt_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (1, 'NT', 'Term spécifique', 'Narrower term');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (2, 'NTG', 'Term spécifique (generic)', 'Narrower term (generic)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (3, 'NTP', 'Term spécifique (partitive)', 'Narrower term (partitive)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (4, 'NTI', 'Term spécifique (instantial)', 'Narrower term (instantial)');


--
-- TOC entry 3932 (class 0 OID 16803)
-- Dependencies: 247
-- Data for Name: permuted; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '34', 'null', 'th1', 'fr', 'Lieux', true, 'Lieux');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '47612', 'null', 'th1', 'en', 'Lyon', true, 'Lyon');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '47612', 'null', 'th1', 'fr', 'Lyon', true, 'Lyon');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '31', 'null', 'th1', 'fr', 'monument', true, 'monument');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '47609', 'null', 'th1', 'fr', 'concept2', true, 'concept2');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '47608', 'null', 'th1', 'fr', 'concept1', true, 'concept1');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '47606', 'null', 'th1', 'fr', 'topTerm1', true, 'topTerm1');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '47610', 'null', 'th1', 'fr', 'concept11', true, 'concept11');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '47607', 'null', 'th1', 'fr', 'topTerm2', true, 'topTerm2');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '47611', 'null', 'th1', 'fr', 'concept22', true, 'concept22');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '47613', 'null', 'th1', 'fr', 'Gastronomie', true, 'Gastronomie');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '30', 'null', 'th1', 'fr', 'Architecture', true, 'Architecture');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '32', 'null', 'th1', 'fr', 'statue', true, 'statue');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (1, '33', 'null', 'th1', 'fr', 'monuments', true, 'monuments historiques');
INSERT INTO public.permuted (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm, original_value) VALUES (2, '33', 'null', 'th1', 'fr', 'historiques', true, 'monuments historiques');


--
-- TOC entry 3934 (class 0 OID 16811)
-- Dependencies: 249
-- Data for Name: preferences; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.preferences (id_pref, id_thesaurus, source_lang, identifier_type, path_image, dossier_resize, bdd_active, bdd_use_id, url_bdd, url_counter_bdd, z3950actif, collection_adresse, notice_url, url_encode, path_notice1, path_notice2, chemin_site, webservices, use_ark, server_ark, id_naan, prefix_ark, user_ark, pass_ark, use_handle, user_handle, pass_handle, path_key_handle, path_cert_handle, url_api_handle, prefix_handle, private_prefix_handle, preferredname, original_uri, original_uri_is_ark, original_uri_is_handle, uri_ark, generate_handle, auto_expand_tree, sort_by_notation) VALUES (4, 'th1', 'fr', 2, '/var/www/images/', 'resize', false, false, 'http://www.mondomaine.fr/concept/##value##', 'http://mondomaine.fr/concept/##conceptId##/total', false, 'KOHA/biblios', 'http://catalogue.mondomaine.fr/cgi-bin/koha/opac-search.pl?type=opac&op=do_search&q=an=terme', 'UTF-8', '/var/www/notices/repositories.xml', '/var/www/notices/SchemaMappings.xml', 'http://localhost:8082/opentheso2/', true, false, 'http://ark.mondomaine.fr/ark:/', '66666', 'crt', 'null', 'null', false, 'null', 'null', '/certificat/key.p12', '/certificat/cacerts2', 'https://handle-server.mondomaine.fr:8001/api/handles/', '66.666.66666', 'crt', 'th1', 'http://localhost:8082/opentheso2', false, false, 'https://ark.mom.fr/ark:/', true, true, false);


--
-- TOC entry 3935 (class 0 OID 16844)
-- Dependencies: 250
-- Data for Name: preferences_sparql; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3936 (class 0 OID 16851)
-- Dependencies: 251
-- Data for Name: preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('34', '34', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('47612', '47612', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('31', '31', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('47609', '47609', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('47608', '47608', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('47606', '47606', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('47610', '47610', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('47607', '47607', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('47611', '47611', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('47613', '47613', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('30', '30', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('32', '32', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('33', '33', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('65', '84', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('66', '86', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('67', '87', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('68', '88', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('69', '89', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('70', '90', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('71', '91', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('72', '92', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('73', '93', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('74', '94', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('75', '95', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('76', '96', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('77', '97', 'th1');


--
-- TOC entry 3937 (class 0 OID 16857)
-- Dependencies: 252
-- Data for Name: proposition; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3938 (class 0 OID 16865)
-- Dependencies: 253
-- Data for Name: relation_group; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.relation_group (id_group1, id_thesaurus, relation, id_group2) VALUES ('G392', 'th1', 'sub', 'G11');
INSERT INTO public.relation_group (id_group1, id_thesaurus, relation, id_group2) VALUES ('G19', 'th1', 'sub', 'G45');


--
-- TOC entry 3939 (class 0 OID 16871)
-- Dependencies: 254
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.roles (id, name, description) VALUES (1, 'superAdmin', 'Super Administrateur pour tout gérer tout thésaurus et tout utilisateur');
INSERT INTO public.roles (id, name, description) VALUES (2, 'admin', 'administrateur pour un domaine ou parc de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (3, 'manager', 'gestionnaire de thésaurus, pas de création de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (4, 'contributor', 'traducteur, notes, candidats, images');


--
-- TOC entry 3941 (class 0 OID 16879)
-- Dependencies: 256
-- Data for Name: routine_mail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3942 (class 0 OID 16886)
-- Dependencies: 257
-- Data for Name: split_non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3976 (class 0 OID 91647)
-- Dependencies: 291
-- Data for Name: status; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.status (id_status, value) VALUES (1, 'En attente');
INSERT INTO public.status (id_status, value) VALUES (2, 'Inséré');
INSERT INTO public.status (id_status, value) VALUES (3, 'Rejeté');


--
-- TOC entry 3944 (class 0 OID 16891)
-- Dependencies: 259
-- Data for Name: term; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('34', 'Lieux', 'fr', 'th1', '2020-10-08 00:00:00+02', '2020-10-08 00:00:00+02', '', '', 70, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('47612', 'Lyon', 'en', 'th1', '2020-09-24 00:00:00+02', '2020-10-08 00:00:00+02', '', '', 71, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('47612', 'Lyon', 'fr', 'th1', '2020-09-24 00:00:00+02', '2020-10-08 00:00:00+02', '', '', 72, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('31', 'monument', 'fr', 'th1', '2020-10-08 00:00:00+02', '2020-10-08 00:00:00+02', '', '', 73, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('47609', 'concept2', 'fr', 'th1', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', '', '', 74, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('47608', 'concept1', 'fr', 'th1', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', '', '', 75, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('47606', 'topTerm1', 'fr', 'th1', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', '', '', 76, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('47607', 'topTerm2', 'fr', 'th1', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', '', '', 78, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('47611', 'concept22', 'fr', 'th1', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', '', '', 79, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('47613', 'Gastronomie', 'fr', 'th1', '2020-09-24 00:00:00+02', '2020-09-24 00:00:00+02', '', '', 80, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('30', 'Architecture', 'fr', 'th1', '2020-10-08 00:00:00+02', '2020-10-08 00:00:00+02', '', '', 81, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('32', 'statue', 'fr', 'th1', '2020-10-08 00:00:00+02', '2020-10-08 00:00:00+02', '', '', 82, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('33', 'monuments historiques', 'fr', 'th1', '2020-10-08 00:00:00+02', '2020-10-08 00:00:00+02', '', '', 83, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('84', 'Buddhism', 'en', 'th1', '2020-10-14 10:34:21.748097+02', '2020-10-14 00:00:00+02', '', '', 85, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('86', 'or', 'fr', 'th1', '2020-10-14 16:48:03.857999+02', '2020-10-14 16:48:03.857999+02', '', 'D', 86, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('87', 'ur', 'fr', 'th1', '2020-10-14 16:48:07.935253+02', '2020-10-14 16:48:07.935253+02', '', 'D', 87, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('88', 'ori', 'fr', 'th1', '2020-10-14 16:48:46.29588+02', '2020-10-14 16:48:46.29588+02', '', 'D', 88, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('84', 'Bouddhismes', 'fr', 'th1', '2020-10-14 10:33:51.778996+02', '2020-10-15 00:00:00+02', '', 'D', 84, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('47610', 'concept12', 'fr', 'th1', '2020-09-24 00:00:00+02', '2020-10-15 00:00:00+02', '', '', 77, 1, NULL);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('89', 'val -or', 'fr', 'th1', '2020-10-15 09:05:19.696467+02', '2020-10-15 09:05:19.696467+02', '', 'D', 89, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('90', 'visa -orgue', 'fr', 'th1', '2020-10-15 09:07:38.970353+02', '2020-10-15 09:07:38.970353+02', '', 'D', 90, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('91', 'saint-vigor-Mal', 'fr', 'th1', '2020-10-15 09:22:49.837451+02', '2020-10-15 09:22:49.837451+02', '', 'D', 91, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('93', 'victor_malte', 'fr', 'th1', '2020-10-15 09:55:29.897473+02', '2020-10-15 09:55:29.897473+02', '', 'D', 93, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('94', 'testCA', 'fr', 'th1', '2020-10-15 13:09:21.322287+02', '2020-10-15 13:09:21.322287+02', 'candidat', 'D', 94, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('95', 'zmalte', 'fr', 'th1', '2020-10-15 15:37:46.052005+02', '2020-10-15 15:37:46.052005+02', '', 'D', 95, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('96', 'victori''malte', 'fr', 'th1', '2020-10-15 16:06:40.695307+02', '2020-10-15 16:06:40.695307+02', '', 'D', 96, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('92', 'victor-Malte', 'fr', 'th1', '2020-10-15 09:23:06.016477+02', '2020-10-15 00:00:00+02', '', 'D', 92, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('97', 'de l''archive', 'fr', 'th1', '2020-10-16 09:38:58.688215+02', '2020-10-16 09:38:58.688215+02', '', 'D', 97, 1, 1);


--
-- TOC entry 3946 (class 0 OID 16903)
-- Dependencies: 261
-- Data for Name: term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3948 (class 0 OID 16914)
-- Dependencies: 263
-- Data for Name: term_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('84', 'bouddhisme', 'fr', 'th1', '2020-10-14 10:33:51.778996+02', '', 'D', 45, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('84', 'buddhism', 'en', 'th1', '2020-10-14 10:34:21.748097+02', '', 'D', 46, 1, 'New');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('84', 'Bouddhisme', 'fr', 'th1', '2020-10-14 10:43:57.328335+02', '', 'D', 47, 1, 'Rename');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('84', 'Buddhism', 'en', 'th1', '2020-10-14 10:44:08.250417+02', '', 'D', 48, 1, 'Rename');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('86', 'or', 'fr', 'th1', '2020-10-14 16:48:03.857999+02', '', 'D', 49, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('87', 'ur', 'fr', 'th1', '2020-10-14 16:48:07.935253+02', '', 'D', 50, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('88', 'ori', 'fr', 'th1', '2020-10-14 16:48:46.29588+02', '', 'D', 51, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('84', 'Bouddhismes', 'fr', 'th1', '2020-10-15 07:42:37.379254+02', '', 'D', 52, 1, 'Rename');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('47610', 'concept12', 'fr', 'th1', '2020-10-15 07:46:01.7657+02', '', 'D', 53, 1, 'Rename');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('89', 'val -or', 'fr', 'th1', '2020-10-15 09:05:19.696467+02', '', 'D', 54, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('90', 'visa -orgue', 'fr', 'th1', '2020-10-15 09:07:38.970353+02', '', 'D', 55, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('91', 'saint-vigor-Mal', 'fr', 'th1', '2020-10-15 09:22:49.837451+02', '', 'D', 56, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('92', 'victor-Mal', 'fr', 'th1', '2020-10-15 09:23:06.016477+02', '', 'D', 57, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('93', 'victor_malte', 'fr', 'th1', '2020-10-15 09:55:29.897473+02', '', 'D', 58, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('94', 'testCA', 'fr', 'th1', '2020-10-15 13:09:21.329302+02', 'candidat', 'D', 59, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('95', 'zmalte', 'fr', 'th1', '2020-10-15 15:37:46.052005+02', '', 'D', 60, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('96', 'victori''malte', 'fr', 'th1', '2020-10-15 16:06:40.695307+02', '', 'D', 61, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('92', 'victor-Malte', 'fr', 'th1', '2020-10-15 16:09:23.161133+02', '', 'D', 62, 1, 'Rename');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('97', 'de l''archive', 'fr', 'th1', '2020-10-16 09:38:58.688215+02', '', 'D', 63, 1, NULL);


--
-- TOC entry 3950 (class 0 OID 16925)
-- Dependencies: 265
-- Data for Name: thesaurus; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesaurus (id_thesaurus, id_ark, created, modified, id, private) VALUES ('th1', '', '2020-10-09 00:00:00', '2020-10-09 00:00:00', 4, false);


--
-- TOC entry 3951 (class 0 OID 16935)
-- Dependencies: 266
-- Data for Name: thesaurus_alignement_source; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesaurus_alignement_source (id_thesaurus, id_alignement_source) VALUES ('th1', 188);


--
-- TOC entry 3952 (class 0 OID 16941)
-- Dependencies: 267
-- Data for Name: thesaurus_array; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3953 (class 0 OID 16949)
-- Dependencies: 268
-- Data for Name: thesaurus_array_concept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3954 (class 0 OID 16956)
-- Dependencies: 269
-- Data for Name: thesaurus_label; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', 'admin', '', 'admin', '2020-10-09 00:00:00', '2020-10-09 00:00:00', '', '', 'fr', '', '', '', '', '', 'theso_test', '');
INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', 'admin', '', 'admin', '2020-10-09 00:00:00', '2020-10-09 00:00:00', '', '', 'en', '', '', '', '', '', 'teso_test_en', '');
INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', 'admin', '', 'admin', '2020-10-09 00:00:00', '2020-10-09 00:00:00', '', '', 'es', '', '', '', '', '', 'teso_test_es', '');


--
-- TOC entry 3966 (class 0 OID 40596)
-- Dependencies: 281
-- Data for Name: thesohomepage; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>texte pour New Th47</p>', 'fr', 'th47');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>texte pour Theso_th54</p>', 'fr', 'th54');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>Unesco thésaurus FR</p>', 'fr', 'th44');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>Unesco thesaurus EN</p>', 'en', 'th44');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>à propos de Essai 1 </p>', 'fr', 'th55');


--
-- TOC entry 3957 (class 0 OID 16968)
-- Dependencies: 272
-- Data for Name: user_group_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3958 (class 0 OID 16975)
-- Dependencies: 273
-- Data for Name: user_group_thesaurus; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3959 (class 0 OID 16981)
-- Dependencies: 274
-- Data for Name: user_role_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3960 (class 0 OID 16984)
-- Dependencies: 275
-- Data for Name: user_role_only_on; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3961 (class 0 OID 16991)
-- Dependencies: 276
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.users (id_user, username, password, active, mail, passtomodify, alertmail, issuperadmin) VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', true, 'admin@domaine.fr', false, false, true);


--
-- TOC entry 3962 (class 0 OID 17002)
-- Dependencies: 277
-- Data for Name: users2; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3963 (class 0 OID 17011)
-- Dependencies: 278
-- Data for Name: users_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3964 (class 0 OID 17019)
-- Dependencies: 279
-- Data for Name: version_history; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3988 (class 0 OID 0)
-- Dependencies: 198
-- Name: alignement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_id_seq', 20, true);


--
-- TOC entry 3989 (class 0 OID 0)
-- Dependencies: 200
-- Name: alignement_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_preferences_id_seq', 1, false);


--
-- TOC entry 3990 (class 0 OID 0)
-- Dependencies: 202
-- Name: alignement_source__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_source__id_seq', 193, true);


--
-- TOC entry 3991 (class 0 OID 0)
-- Dependencies: 284
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.candidat_messages_id_message_seq', 5, true);


--
-- TOC entry 3992 (class 0 OID 0)
-- Dependencies: 287
-- Name: candidat_vote_id_vote_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.candidat_vote_id_vote_seq', 6, true);


--
-- TOC entry 3993 (class 0 OID 0)
-- Dependencies: 207
-- Name: concept__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept__id_seq', 77, true);


--
-- TOC entry 3994 (class 0 OID 0)
-- Dependencies: 209
-- Name: concept_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_candidat__id_seq', 1, false);


--
-- TOC entry 3995 (class 0 OID 0)
-- Dependencies: 212
-- Name: concept_group__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group__id_seq', 46, true);


--
-- TOC entry 3996 (class 0 OID 0)
-- Dependencies: 215
-- Name: concept_group_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_historique__id_seq', 1, false);


--
-- TOC entry 3997 (class 0 OID 0)
-- Dependencies: 219
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_label_historique__id_seq', 26, true);


--
-- TOC entry 3998 (class 0 OID 0)
-- Dependencies: 217
-- Name: concept_group_label_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_label_id_seq', 25, true);


--
-- TOC entry 3999 (class 0 OID 0)
-- Dependencies: 222
-- Name: concept_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_historique__id_seq', 35, true);


--
-- TOC entry 4000 (class 0 OID 0)
-- Dependencies: 230
-- Name: facet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.facet_id_seq', 1, false);


--
-- TOC entry 4001 (class 0 OID 0)
-- Dependencies: 232
-- Name: gps_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gps_preferences_id_seq', 1, false);


--
-- TOC entry 4002 (class 0 OID 0)
-- Dependencies: 238
-- Name: languages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.languages_id_seq', 189, false);


--
-- TOC entry 4003 (class 0 OID 0)
-- Dependencies: 242
-- Name: note__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.note__id_seq', 60, true);


--
-- TOC entry 4004 (class 0 OID 0)
-- Dependencies: 244
-- Name: note_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.note_historique__id_seq', 63, true);


--
-- TOC entry 4005 (class 0 OID 0)
-- Dependencies: 248
-- Name: pref__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pref__id_seq', 4, true);


--
-- TOC entry 4006 (class 0 OID 0)
-- Dependencies: 255
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.role_id_seq', 6, true);


--
-- TOC entry 4007 (class 0 OID 0)
-- Dependencies: 283
-- Name: status_id_status_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.status_id_status_seq', 1, false);


--
-- TOC entry 4008 (class 0 OID 0)
-- Dependencies: 290
-- Name: status_id_status_seq1; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.status_id_status_seq1', 1, false);


--
-- TOC entry 4009 (class 0 OID 0)
-- Dependencies: 258
-- Name: term__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term__id_seq', 97, true);


--
-- TOC entry 4010 (class 0 OID 0)
-- Dependencies: 260
-- Name: term_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term_candidat__id_seq', 1, false);


--
-- TOC entry 4011 (class 0 OID 0)
-- Dependencies: 262
-- Name: term_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term_historique__id_seq', 63, true);


--
-- TOC entry 4012 (class 0 OID 0)
-- Dependencies: 264
-- Name: thesaurus_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.thesaurus_id_seq', 4, true);


--
-- TOC entry 4013 (class 0 OID 0)
-- Dependencies: 270
-- Name: user__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user__id_seq', 2, false);


--
-- TOC entry 4014 (class 0 OID 0)
-- Dependencies: 271
-- Name: user_group_label__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user_group_label__id_seq', 1, false);


--
-- TOC entry 3741 (class 2606 OID 17027)
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
-- TOC entry 3749 (class 2606 OID 69555)
-- Name: candidat_messages candidat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_messages
    ADD CONSTRAINT candidat_messages_pkey PRIMARY KEY (id_message);


--
-- TOC entry 3751 (class 2606 OID 90033)
-- Name: candidat_status candidat_status_id_concept_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_status
    ADD CONSTRAINT candidat_status_id_concept_id_thesaurus_key UNIQUE (id_concept, id_thesaurus);


--
-- TOC entry 3753 (class 2606 OID 90536)
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
-- TOC entry 3607 (class 2606 OID 17053)
-- Name: concept_fusion concept_fusion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_fusion
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
-- TOC entry 3625 (class 2606 OID 17069)
-- Name: concept_orphan concept_orphan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_orphan
    ADD CONSTRAINT concept_orphan_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3601 (class 2606 OID 17071)
-- Name: concept concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept
    ADD CONSTRAINT concept_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3627 (class 2606 OID 17073)
-- Name: concept_term_candidat concept_term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_term_candidat
    ADD CONSTRAINT concept_term_candidat_pkey PRIMARY KEY (id_concept, id_term, id_thesaurus);


--
-- TOC entry 3629 (class 2606 OID 17075)
-- Name: copyright copyright_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.copyright
    ADD CONSTRAINT copyright_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 3747 (class 2606 OID 69367)
-- Name: corpus_link corpus_link_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.corpus_link
    ADD CONSTRAINT corpus_link_pkey PRIMARY KEY (id_theso, corpus_name);


--
-- TOC entry 3631 (class 2606 OID 17077)
-- Name: custom_concept_attribute custom_concept_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_concept_attribute
    ADD CONSTRAINT custom_concept_attribute_pkey PRIMARY KEY ("idConcept");


--
-- TOC entry 3633 (class 2606 OID 17079)
-- Name: custom_term_attribute custom_term_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_term_attribute
    ADD CONSTRAINT custom_term_attribute_pkey PRIMARY KEY (identifier);


--
-- TOC entry 3635 (class 2606 OID 17081)
-- Name: external_images external_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_images
    ADD CONSTRAINT external_images_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 3637 (class 2606 OID 91738)
-- Name: gps gps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gps
    ADD CONSTRAINT gps_pkey PRIMARY KEY (id_concept, id_theso);


--
-- TOC entry 3639 (class 2606 OID 17085)
-- Name: gps_preferences gps_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gps_preferences
    ADD CONSTRAINT gps_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_alignement_source);


--
-- TOC entry 3643 (class 2606 OID 17087)
-- Name: hierarchical_relationship_historique hierarchical_relationship_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchical_relationship_historique
    ADD CONSTRAINT hierarchical_relationship_copy_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2, modified, id_user);


--
-- TOC entry 3641 (class 2606 OID 17089)
-- Name: hierarchical_relationship hierarchical_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchical_relationship
    ADD CONSTRAINT hierarchical_relationship_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2);


--
-- TOC entry 3743 (class 2606 OID 40603)
-- Name: homepage homepage_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.homepage
    ADD CONSTRAINT homepage_lang_key UNIQUE (lang);


--
-- TOC entry 3645 (class 2606 OID 17091)
-- Name: images images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.images
    ADD CONSTRAINT images_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 3647 (class 2606 OID 17093)
-- Name: info info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.info
    ADD CONSTRAINT info_pkey PRIMARY KEY (version_opentheso, version_bdd);


--
-- TOC entry 3759 (class 2606 OID 91666)
-- Name: languages_iso639 languages_iso639_iso639_1_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_iso639_1_key UNIQUE (iso639_1);


--
-- TOC entry 3761 (class 2606 OID 91664)
-- Name: languages_iso639 languages_iso639_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_pkey PRIMARY KEY (id);


--
-- TOC entry 3649 (class 2606 OID 17099)
-- Name: node_label node_label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.node_label
    ADD CONSTRAINT node_label_pkey PRIMARY KEY (facet_id, id_thesaurus, lang);


--
-- TOC entry 3652 (class 2606 OID 17101)
-- Name: non_preferred_term non_prefered_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.non_preferred_term
    ADD CONSTRAINT non_prefered_term_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 3655 (class 2606 OID 17103)
-- Name: non_preferred_term_historique non_preferred_term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.non_preferred_term_historique
    ADD CONSTRAINT non_preferred_term_copy_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, modified, id_user);


--
-- TOC entry 3664 (class 2606 OID 17105)
-- Name: note_historique note_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_historique
    ADD CONSTRAINT note_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 3658 (class 2606 OID 91734)
-- Name: note note_notetypecode_id_thesaurus_id_concept_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_notetypecode_id_thesaurus_id_concept_lang_key UNIQUE (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue);


--
-- TOC entry 3660 (class 2606 OID 91736)
-- Name: note note_notetypecode_id_thesaurus_id_term_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_notetypecode_id_thesaurus_id_term_lang_key UNIQUE (notetypecode, id_thesaurus, id_term, lang, lexicalvalue);


--
-- TOC entry 3662 (class 2606 OID 17111)
-- Name: note note_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_pkey PRIMARY KEY (id);


--
-- TOC entry 3666 (class 2606 OID 17113)
-- Name: nt_type nt_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3668 (class 2606 OID 17115)
-- Name: nt_type nt_type_relation_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_relation_key UNIQUE (relation);


--
-- TOC entry 3671 (class 2606 OID 17117)
-- Name: permuted permuted_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permuted
    ADD CONSTRAINT permuted_pkey PRIMARY KEY (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm);


--
-- TOC entry 3755 (class 2606 OID 91644)
-- Name: note_type pk_note_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_type
    ADD CONSTRAINT pk_note_type PRIMARY KEY (code);


--
-- TOC entry 3685 (class 2606 OID 17121)
-- Name: relation_group pk_relation_group; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.relation_group
    ADD CONSTRAINT pk_relation_group PRIMARY KEY (id_group1, id_thesaurus, relation, id_group2);


--
-- TOC entry 3673 (class 2606 OID 17123)
-- Name: preferences preferences_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 3675 (class 2606 OID 17125)
-- Name: preferences preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_pkey PRIMARY KEY (id_pref);


--
-- TOC entry 3677 (class 2606 OID 17127)
-- Name: preferences preferences_preferredname_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_preferredname_key UNIQUE (preferredname);


--
-- TOC entry 3679 (class 2606 OID 17129)
-- Name: preferences_sparql preferences_sparql_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences_sparql
    ADD CONSTRAINT preferences_sparql_pkey PRIMARY KEY (thesaurus);


--
-- TOC entry 3681 (class 2606 OID 17131)
-- Name: preferred_term preferred_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferred_term
    ADD CONSTRAINT preferred_term_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3683 (class 2606 OID 17133)
-- Name: proposition proposition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposition
    ADD CONSTRAINT proposition_pkey PRIMARY KEY (id_concept, id_user, id_thesaurus);


--
-- TOC entry 3687 (class 2606 OID 17135)
-- Name: roles role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- TOC entry 3689 (class 2606 OID 17137)
-- Name: routine_mail routine_mail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routine_mail
    ADD CONSTRAINT routine_mail_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 3757 (class 2606 OID 91655)
-- Name: status status_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_pkey PRIMARY KEY (id_status);


--
-- TOC entry 3700 (class 2606 OID 17139)
-- Name: term_candidat term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term_candidat
    ADD CONSTRAINT term_candidat_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, contributor);


--
-- TOC entry 3703 (class 2606 OID 91763)
-- Name: term_historique term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term_historique
    ADD CONSTRAINT term_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 3692 (class 2606 OID 17143)
-- Name: term term_id_term_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_key UNIQUE (id_term, lang, id_thesaurus);


--
-- TOC entry 3694 (class 2606 OID 17145)
-- Name: term term_id_term_lexical_value_lang_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_lexical_value_lang_id_thesaurus_key UNIQUE (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 3697 (class 2606 OID 17147)
-- Name: term term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_pkey PRIMARY KEY (id);


--
-- TOC entry 3707 (class 2606 OID 17149)
-- Name: thesaurus_alignement_source thesaurus_alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_alignement_source
    ADD CONSTRAINT thesaurus_alignement_source_pkey PRIMARY KEY (id_thesaurus, id_alignement_source);


--
-- TOC entry 3711 (class 2606 OID 17151)
-- Name: thesaurus_array_concept thesaurus_array_concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_array_concept
    ADD CONSTRAINT thesaurus_array_concept_pkey PRIMARY KEY (thesaurusarrayid, id_concept, id_thesaurus);


--
-- TOC entry 3709 (class 2606 OID 17153)
-- Name: thesaurus_array thesaurus_array_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_array
    ADD CONSTRAINT thesaurus_array_pkey PRIMARY KEY (facet_id, id_thesaurus, id_concept_parent);


--
-- TOC entry 3713 (class 2606 OID 17155)
-- Name: thesaurus_label thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT thesaurus_pkey PRIMARY KEY (id_thesaurus, lang, title);


--
-- TOC entry 3705 (class 2606 OID 17157)
-- Name: thesaurus thesaurus_pkey1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus
    ADD CONSTRAINT thesaurus_pkey1 PRIMARY KEY (id_thesaurus, id_ark);


--
-- TOC entry 3745 (class 2606 OID 40605)
-- Name: thesohomepage thesohomepage_idtheso_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesohomepage
    ADD CONSTRAINT thesohomepage_idtheso_lang_key UNIQUE (idtheso, lang);


--
-- TOC entry 3715 (class 2606 OID 17159)
-- Name: thesaurus_label unique_thesau_lang; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT unique_thesau_lang UNIQUE (id_thesaurus, lang);


--
-- TOC entry 3717 (class 2606 OID 17161)
-- Name: user_group_label user_group-label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_label
    ADD CONSTRAINT "user_group-label_pkey" PRIMARY KEY (id_group);


--
-- TOC entry 3723 (class 2606 OID 91787)
-- Name: user_role_group user_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_group
    ADD CONSTRAINT user_group_pkey UNIQUE (id_user, id_group);


--
-- TOC entry 3719 (class 2606 OID 17163)
-- Name: user_group_thesaurus user_group_thesaurus_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 3721 (class 2606 OID 17165)
-- Name: user_group_thesaurus user_group_thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_pkey PRIMARY KEY (id_group, id_thesaurus);


--
-- TOC entry 3727 (class 2606 OID 17167)
-- Name: users user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT user_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3725 (class 2606 OID 17169)
-- Name: user_role_only_on user_role_only_on_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_only_on
    ADD CONSTRAINT user_role_only_on_pkey PRIMARY KEY (id_user, id_role, id_theso);


--
-- TOC entry 3739 (class 2606 OID 17173)
-- Name: users_historique users_historique_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users_historique
    ADD CONSTRAINT users_historique_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3733 (class 2606 OID 17175)
-- Name: users2 users_login_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_login_key UNIQUE (login);


--
-- TOC entry 3735 (class 2606 OID 17177)
-- Name: users2 users_mail_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_mail_key UNIQUE (mail);


--
-- TOC entry 3729 (class 2606 OID 17179)
-- Name: users users_mail_key1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_mail_key1 UNIQUE (mail);


--
-- TOC entry 3737 (class 2606 OID 17181)
-- Name: users2 users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3731 (class 2606 OID 17183)
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
-- TOC entry 3690 (class 1259 OID 17185)
-- Name: index_lexical_value; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value ON public.term USING btree (lexical_value);


--
-- TOC entry 3701 (class 1259 OID 17186)
-- Name: index_lexical_value_copy; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value_copy ON public.term_historique USING btree (lexical_value);


--
-- TOC entry 3650 (class 1259 OID 17187)
-- Name: index_lexical_value_npt; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value_npt ON public.non_preferred_term USING btree (lexical_value);


--
-- TOC entry 3656 (class 1259 OID 17188)
-- Name: note_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX note_lexical_value_unaccent ON public.note USING gin (public.f_unaccent(lower((lexicalvalue)::text)) public.gin_trgm_ops);


--
-- TOC entry 3669 (class 1259 OID 17189)
-- Name: permuted_lexical_value_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX permuted_lexical_value_idx ON public.permuted USING btree (lexical_value);


--
-- TOC entry 3653 (class 1259 OID 17190)
-- Name: term_lexical_value_npt_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX term_lexical_value_npt_unaccent ON public.non_preferred_term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 3695 (class 1259 OID 17191)
-- Name: term_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX term_lexical_value_unaccent ON public.term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 3698 (class 1259 OID 17192)
-- Name: terms_values_gin; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX terms_values_gin ON public.term USING gin (lexical_value public.gin_trgm_ops);


-- Completed on 2020-10-16 14:55:16 CEST

--
-- PostgreSQL database dump complete
--
