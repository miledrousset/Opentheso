--
-- PostgreSQL database dump
--

-- Dumped from database version 11.5
-- Dumped by pg_dump version 12.2

-- Started on 2020-07-09 12:18:30 CEST


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

SET role = opentheso;

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 3 (class 3079 OID 69566)
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--





--
-- TOC entry 721 (class 1247 OID 69651)
-- Name: alignement_format; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.alignement_format AS ENUM (
    'skos',
    'json',
    'xml'
);


--
-- TOC entry 724 (class 1247 OID 69658)
-- Name: alignement_type_rqt; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.alignement_type_rqt AS ENUM (
    'SPARQL',
    'REST'
);


--
-- TOC entry 727 (class 1247 OID 69664)
-- Name: auth_method; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.auth_method AS ENUM (
    'DB',
    'LDAP',
    'FILE',
    'test'
);


--
-- TOC entry 336 (class 1255 OID 69673)
-- Name: f_unaccent(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.f_unaccent(text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
SELECT public.unaccent('public.unaccent', $1)
$_$;


--
-- TOC entry 337 (class 1255 OID 70684)
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
-- TOC entry 198 (class 1259 OID 69675)
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
-- TOC entry 199 (class 1259 OID 69677)
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
-- TOC entry 200 (class 1259 OID 69686)
-- Name: alignement_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alignement_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 201 (class 1259 OID 69688)
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
-- TOC entry 202 (class 1259 OID 69695)
-- Name: alignement_source__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alignement_source__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 203 (class 1259 OID 69697)
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
-- TOC entry 204 (class 1259 OID 69706)
-- Name: alignement_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alignement_type (
    id integer NOT NULL,
    label text NOT NULL,
    isocode text NOT NULL,
    label_skos character varying
);


--
-- TOC entry 205 (class 1259 OID 69712)
-- Name: bt_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bt_type (
    id integer NOT NULL,
    relation character varying,
    description_fr character varying,
    description_en character varying
);


--
-- TOC entry 284 (class 1259 OID 70538)
-- Name: candidat_messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidat_messages (
    id_message integer NOT NULL,
    value text NOT NULL,
    id_user integer,
    id_concept integer,
    id_thesaurus character varying,
    date text
);


--
-- TOC entry 283 (class 1259 OID 70536)
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidat_messages_id_message_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3969 (class 0 OID 0)
-- Dependencies: 283
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidat_messages_id_message_seq OWNED BY public.candidat_messages.id_message;


--
-- TOC entry 285 (class 1259 OID 70547)
-- Name: candidat_status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.candidat_status (
    id_concept integer NOT NULL,
    id_status integer,
    date date,
    id_user integer,
    id_thesaurus character varying,
    message text
);


--
-- TOC entry 206 (class 1259 OID 69718)
-- Name: compound_equivalence; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.compound_equivalence (
    id_split_nonpreferredterm text NOT NULL,
    id_preferredterm text NOT NULL
);


--
-- TOC entry 207 (class 1259 OID 69724)
-- Name: concept__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept__id_seq
    START WITH 43
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 208 (class 1259 OID 69726)
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
-- TOC entry 209 (class 1259 OID 69739)
-- Name: concept_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 210 (class 1259 OID 69741)
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
-- TOC entry 211 (class 1259 OID 69751)
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
-- TOC entry 212 (class 1259 OID 69758)
-- Name: concept_group__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 213 (class 1259 OID 69760)
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
-- TOC entry 214 (class 1259 OID 69769)
-- Name: concept_group_concept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group_concept (
    idgroup text NOT NULL,
    idthesaurus text NOT NULL,
    idconcept text NOT NULL
);


--
-- TOC entry 215 (class 1259 OID 69775)
-- Name: concept_group_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 216 (class 1259 OID 69777)
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
-- TOC entry 217 (class 1259 OID 69785)
-- Name: concept_group_label_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group_label_id_seq
    START WITH 60
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 218 (class 1259 OID 69787)
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
-- TOC entry 219 (class 1259 OID 69796)
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group_label_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 220 (class 1259 OID 69798)
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
-- TOC entry 221 (class 1259 OID 69806)
-- Name: concept_group_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group_type (
    code text NOT NULL,
    label text NOT NULL,
    skoslabel text
);


--
-- TOC entry 222 (class 1259 OID 69812)
-- Name: concept_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 223 (class 1259 OID 69814)
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
-- TOC entry 224 (class 1259 OID 69823)
-- Name: concept_orphan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_orphan (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 225 (class 1259 OID 69829)
-- Name: concept_term_candidat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_term_candidat (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 226 (class 1259 OID 69835)
-- Name: copyright; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.copyright (
    id_thesaurus character varying NOT NULL,
    copyright character varying
);


--
-- TOC entry 282 (class 1259 OID 70522)
-- Name: corpus_link; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.corpus_link (
    id_theso character varying NOT NULL,
    corpus_name character varying DEFAULT ''::character varying NOT NULL,
    uri_count character varying DEFAULT ''::character varying,
    uri_link character varying DEFAULT ''::character varying NOT NULL,
    active boolean DEFAULT false
);


--
-- TOC entry 227 (class 1259 OID 69841)
-- Name: custom_concept_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custom_concept_attribute (
    "idConcept" character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 228 (class 1259 OID 69847)
-- Name: custom_term_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custom_term_attribute (
    identifier character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 229 (class 1259 OID 69853)
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
-- TOC entry 230 (class 1259 OID 69860)
-- Name: facet_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.facet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 231 (class 1259 OID 69862)
-- Name: gps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gps (
    id_concept character varying NOT NULL,
    id_theso character varying NOT NULL,
    latitude double precision,
    longitude double precision
);


--
-- TOC entry 232 (class 1259 OID 69868)
-- Name: gps_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gps_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 233 (class 1259 OID 69870)
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
-- TOC entry 234 (class 1259 OID 69880)
-- Name: hierarchical_relationship; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hierarchical_relationship (
    id_concept1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    role character varying NOT NULL,
    id_concept2 character varying NOT NULL
);


--
-- TOC entry 235 (class 1259 OID 69886)
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
-- TOC entry 236 (class 1259 OID 69893)
-- Name: homepage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.homepage (
    htmlcode character varying,
    lang character varying
);


--
-- TOC entry 237 (class 1259 OID 69899)
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
-- TOC entry 238 (class 1259 OID 69906)
-- Name: info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.info (
    version_opentheso character varying NOT NULL,
    version_bdd character varying NOT NULL
);


--
-- TOC entry 239 (class 1259 OID 69912)
-- Name: languages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.languages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 289 (class 1259 OID 70573)
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
-- TOC entry 240 (class 1259 OID 69921)
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
-- TOC entry 241 (class 1259 OID 69929)
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
-- TOC entry 242 (class 1259 OID 69938)
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
-- TOC entry 243 (class 1259 OID 69946)
-- Name: note__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 244 (class 1259 OID 69948)
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
    modified timestamp without time zone DEFAULT now() NOT NULL
);


--
-- TOC entry 245 (class 1259 OID 69957)
-- Name: note_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 246 (class 1259 OID 69959)
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
-- TOC entry 288 (class 1259 OID 70564)
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
-- TOC entry 247 (class 1259 OID 69974)
-- Name: nt_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.nt_type (
    id integer NOT NULL,
    relation character varying,
    description_fr character varying,
    description_en character varying
);


--
-- TOC entry 248 (class 1259 OID 69980)
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
-- TOC entry 249 (class 1259 OID 69986)
-- Name: pref__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pref__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 250 (class 1259 OID 69988)
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
    auto_expand_tree boolean DEFAULT true
);


--
-- TOC entry 251 (class 1259 OID 70021)
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
-- TOC entry 252 (class 1259 OID 70028)
-- Name: preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.preferred_term (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 253 (class 1259 OID 70034)
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
-- TOC entry 254 (class 1259 OID 70042)
-- Name: relation_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.relation_group (
    id_group1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    relation character varying NOT NULL,
    id_group2 character varying NOT NULL
);


--
-- TOC entry 255 (class 1259 OID 70048)
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying,
    description character varying
);


--
-- TOC entry 256 (class 1259 OID 70054)
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3970 (class 0 OID 0)
-- Dependencies: 256
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.role_id_seq OWNED BY public.roles.id;


--
-- TOC entry 257 (class 1259 OID 70056)
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
-- TOC entry 258 (class 1259 OID 70063)
-- Name: split_non_preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.split_non_preferred_term (
);


--
-- TOC entry 287 (class 1259 OID 70555)
-- Name: status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.status (
    id_status integer NOT NULL,
    value text
);


--
-- TOC entry 286 (class 1259 OID 70553)
-- Name: status_id_status_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.status_id_status_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3971 (class 0 OID 0)
-- Dependencies: 286
-- Name: status_id_status_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.status_id_status_seq OWNED BY public.status.id_status;


--
-- TOC entry 259 (class 1259 OID 70066)
-- Name: term__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 260 (class 1259 OID 70068)
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
-- TOC entry 261 (class 1259 OID 70078)
-- Name: term_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 262 (class 1259 OID 70080)
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
-- TOC entry 263 (class 1259 OID 70089)
-- Name: term_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 264 (class 1259 OID 70091)
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
-- TOC entry 265 (class 1259 OID 70100)
-- Name: thesaurus_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.thesaurus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 266 (class 1259 OID 70102)
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
-- TOC entry 267 (class 1259 OID 70112)
-- Name: thesaurus_alignement_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus_alignement_source (
    id_thesaurus character varying NOT NULL,
    id_alignement_source integer NOT NULL
);


--
-- TOC entry 268 (class 1259 OID 70118)
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
-- TOC entry 269 (class 1259 OID 70126)
-- Name: thesaurus_array_concept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus_array_concept (
    thesaurusarrayid integer NOT NULL,
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    arrayorder integer DEFAULT 0 NOT NULL
);


--
-- TOC entry 270 (class 1259 OID 70133)
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
-- TOC entry 271 (class 1259 OID 70141)
-- Name: thesohomepage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesohomepage (
    htmlcode character varying,
    lang character varying,
    idtheso character varying
);


--
-- TOC entry 272 (class 1259 OID 70147)
-- Name: user__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 273 (class 1259 OID 70149)
-- Name: user_group_label__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_group_label__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 274 (class 1259 OID 70151)
-- Name: user_group_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_group_label (
    id_group integer DEFAULT nextval('public.user_group_label__id_seq'::regclass) NOT NULL,
    label_group character varying
);


--
-- TOC entry 275 (class 1259 OID 70158)
-- Name: user_group_thesaurus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_group_thesaurus (
    id_group integer NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 276 (class 1259 OID 70164)
-- Name: user_role_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role_group (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_group integer NOT NULL
);


--
-- TOC entry 277 (class 1259 OID 70167)
-- Name: user_role_only_on; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role_only_on (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_theso character varying NOT NULL,
    id_theso_domain character varying DEFAULT 'all'::character varying NOT NULL
);


--
-- TOC entry 278 (class 1259 OID 70174)
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
-- TOC entry 279 (class 1259 OID 70185)
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
-- TOC entry 280 (class 1259 OID 70194)
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
-- TOC entry 281 (class 1259 OID 70202)
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
-- TOC entry 3568 (class 2604 OID 70541)
-- Name: candidat_messages id_message; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_messages ALTER COLUMN id_message SET DEFAULT nextval('public.candidat_messages_id_message_seq'::regclass);


--
-- TOC entry 3531 (class 2604 OID 70683)
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);


--
-- TOC entry 3569 (class 2604 OID 70558)
-- Name: status id_status; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status ALTER COLUMN id_status SET DEFAULT nextval('public.status_id_status_seq'::regclass);


--
-- TOC entry 3871 (class 0 OID 69677)
-- Dependencies: 199
-- Data for Name: alignement; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3873 (class 0 OID 69688)
-- Dependencies: 201
-- Data for Name: alignement_preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3875 (class 0 OID 69697)
-- Dependencies: 203
-- Data for Name: alignement_source; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('local', 'http://localhost:8080/opentheso/api/search?q=##value##&lang=##lang##&theso=11', 'REST', 'skos', 39, 1, 'Opentheso', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('local_sarah', 'http://localhost:8080/opentheso/api/search?q=##value##&lang=##lang##&theso=13', 'REST', 'skos', 41, 1, 'Opentheso', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('bnf_instrumentMusique', 'PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX xml: <http://www.w3.org/XML/1998/namespace>
SELECT ?instrument ?prop ?value where {
  <http://data.bnf.fr/ark:/12148/cb119367821> skos:narrower+ ?instrument.
  ?instrument ?prop ?value.
  FILTER( (regex(?prop,skos:prefLabel) || regex(?prop,skos:altLabel))  && regex(?value, ##value##,"i") ) 
    filter(lang(?value) =##lang##)
} LIMIT 20', 'SPARQL', 'skos', 5, 1, '', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefSujets', 'https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:(##value##)%20AND%20recordtype_z:r', 'REST', 'json', 111, 1, 'alignement avec les Sujets de IdRef ABES Rameaux', false, 'IdRefSujets');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefAuteurs', 'https://www.idref.fr/Sru/Solr?wt=json&q=nom_t:(##nom##)%20AND%20prenom_t:(##prenom##)%20AND%20recordtype_z:a&fl=ppn_z,affcourt_z,prenom_s,nom_s&start=0&rows=30&version=2.2', 'REST', 'json', 112, 1, 'alignement avec les Auteurs de IdRef ABES', false, 'IdRefAuteurs');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefPersonnes', 'https://www.idref.fr/Sru/Solr?wt=json&q=persname_t:(##value##)&fl=ppn_z,affcourt_z,prenom_s,nom_s&start=0&rows=30&version=2.2', 'REST', 'json', 113, 1, 'alignement avec les Noms de personnes de IdRef ABES', false, 'IdRefPersonnes');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefTitreUniforme', 'https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=uniformtitle_t:(##value##)%20AND%20recordtype_z:f', 'REST', 'json', 114, 1, 'alignement avec les titres uniformes de IdRef ABES', false, 'IdRefTitreUniforme');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Wikidata', 'SELECT ?item ?itemLabel ?itemDescription WHERE {
                    ?item rdfs:label "##value##"@##lang##.
                    SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],##lang##". }
                    }', 'SPARQL', 'json', 115, 1, 'alignement avec le thésaurus de wikidata', false, 'Wikidata');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Getty_AAT', 'http://vocabsservices.getty.edu/AATService.asmx/AATGetTermMatch?term=##value##&logop=and&notes=', 'REST', 'xml', 116, 1, 'alignement avec le thésaurus du Getty AAT', false, 'Getty_AAT');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('GeoNames', 'http://api.geonames.org/search?q=##value##&maxRows=10&style=FULL&lang=##lang##&username=opentheso', 'REST', 'xml', 117, 1, 'Alignement avec GeoNames', true, 'GeoNames');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Pactols', 'https://pactols.frantiq.fr/opentheso/api/search?q=##value##&lang=##lang##&theso=TH_1', 'REST', 'skos', 118, 1, 'Alignement avec PACTOLS', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Gemet', 'https://www.eionet.europa.eu/gemet/getConceptsMatchingKeyword?keyword=##value##&search_mode=3&thesaurus_uri=http://www.eionet.europa.eu/gemet/concept/&language=##lang##', 'REST', 'json', 119, 1, 'Alignement avec le thésaurus Gemet', false, 'Gemet');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Agrovoc', 'http://agrovoc.uniroma2.it/agrovoc/rest/v1/search/?query=##value##&lang=##lang##', 'REST', 'json', 120, 1, 'Alignement avec le thésaurus Agrovoc', false, 'Agrovoc');


--
-- TOC entry 3876 (class 0 OID 69706)
-- Dependencies: 204
-- Data for Name: alignement_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (1, 'Equivalence exacte', '=EQ', 'exactMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (2, 'Equivalence inexacte', '~EQ', 'closeMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (3, 'Equivalence générique', 'EQB', 'broadMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (4, 'Equivalence associative', 'EQR', 'relatedMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (5, 'Equivalence spécifique', 'EQS', 'narrowMatch');


--
-- TOC entry 3877 (class 0 OID 69712)
-- Dependencies: 205
-- Data for Name: bt_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (1, 'BT', 'Terme générique', 'Broader term');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (2, 'BTG', 'Terme générique (generic)', 'Broader term (generic)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (3, 'BTP', 'Terme générique (partitive)', 'Broader term (partitive)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (4, 'BTI', 'Terme générique (instance)', 'Broader term (instance)');


--
-- TOC entry 3956 (class 0 OID 70538)
-- Dependencies: 284
-- Data for Name: candidat_messages; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3957 (class 0 OID 70547)
-- Dependencies: 285
-- Data for Name: candidat_status; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3878 (class 0 OID 69718)
-- Dependencies: 206
-- Data for Name: compound_equivalence; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3880 (class 0 OID 69726)
-- Dependencies: 208
-- Data for Name: concept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3882 (class 0 OID 69741)
-- Dependencies: 210
-- Data for Name: concept_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3883 (class 0 OID 69751)
-- Dependencies: 211
-- Data for Name: concept_fusion; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3885 (class 0 OID 69760)
-- Dependencies: 213
-- Data for Name: concept_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3886 (class 0 OID 69769)
-- Dependencies: 214
-- Data for Name: concept_group_concept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3888 (class 0 OID 69777)
-- Dependencies: 216
-- Data for Name: concept_group_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3890 (class 0 OID 69787)
-- Dependencies: 218
-- Data for Name: concept_group_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3892 (class 0 OID 69798)
-- Dependencies: 220
-- Data for Name: concept_group_label_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3893 (class 0 OID 69806)
-- Dependencies: 221
-- Data for Name: concept_group_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('MT', 'Microthesaurus', 'MicroThesaurus');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('G', 'Group', 'ConceptGroup');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('C', 'Collection', 'Collection');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('T', 'Theme', 'Theme');


--
-- TOC entry 3895 (class 0 OID 69814)
-- Dependencies: 223
-- Data for Name: concept_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3896 (class 0 OID 69823)
-- Dependencies: 224
-- Data for Name: concept_orphan; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3897 (class 0 OID 69829)
-- Dependencies: 225
-- Data for Name: concept_term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3898 (class 0 OID 69835)
-- Dependencies: 226
-- Data for Name: copyright; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3954 (class 0 OID 70522)
-- Dependencies: 282
-- Data for Name: corpus_link; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3899 (class 0 OID 69841)
-- Dependencies: 227
-- Data for Name: custom_concept_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3900 (class 0 OID 69847)
-- Dependencies: 228
-- Data for Name: custom_term_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3901 (class 0 OID 69853)
-- Dependencies: 229
-- Data for Name: external_images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3903 (class 0 OID 69862)
-- Dependencies: 231
-- Data for Name: gps; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3905 (class 0 OID 69870)
-- Dependencies: 233
-- Data for Name: gps_preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3906 (class 0 OID 69880)
-- Dependencies: 234
-- Data for Name: hierarchical_relationship; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3907 (class 0 OID 69886)
-- Dependencies: 235
-- Data for Name: hierarchical_relationship_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3908 (class 0 OID 69893)
-- Dependencies: 236
-- Data for Name: homepage; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.homepage (htmlcode, lang) VALUES ('<h1><img src="data:image/png;base64,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"> Opentheso</h1><h3>Copyright ©CNRS</h3><p>english</p><p>Opentheso est distribué sous licence <a href="http://www.cecill.info/licences.fr.html" target="_blank">CeCILL_C</a>, Licence libre de droit français compatible avec la licence <a href="http://www.gnu.org/copyleft/gpl.html" target="_blank">GNU GPL</a></p><p>C''est un gestionnaire de thesaurus multilingue, développé par la plateforme Technologique <a href="https://www.mom.fr/plateformes-technologiques/web-semantique-et-thesauri" target="_blank">WST</a> (Web Sémantique &amp; Thesauri) située à la <a href="https://www.mom.fr" target="_blank">MOM</a></p><p>en partenariat avec le <a href="http://www.frantiq.fr" target="_blank">GDS-FRANTIQ</a></p><p><br></p><p>Le développement des versions 3 et 4 a bénéficié d’une participation financière du Consortium <a href="http://masa.hypotheses.org/" target="_blank">MASA</a>(Mémoire des archéologues et des Sites Archéologiques) de la <a href="http://www.huma-num.fr/" target="_blank">TGIR Huma-Num</a>, ce financement a permis de produire une version FullWeb qui respecte la nouvelle norme des thésaurus ISO 25964.</p><p>Chef de Projet : <strong>Miled Rousset</strong></p><p>Développement : <strong>Miled Rousset</strong></p><p>Contributeurs : <strong>Prudham Jean-Marc, Quincy Mbape Eyoke, Antonio Perez, Carole Bonfré</strong></p><p>Partenariat, test et expertise : <strong>Les équipes du réseau </strong><a href="http://www.frantiq.fr" target="_blank"><strong>Frantiq</strong></a></p><p><br></p><p>Le développement a été réalisé avec les technologies suivantes :</p><ul><li>PostgreSQL pour la base des données</li><li>Java pour le module API et module métier</li><li>JSF2 et PrimeFaces pour la partie graphique</li></ul><p><br></p><p><strong>Opentheso</strong> s''appuie sur le projet <a href="http://ark.mom.fr" target="_blank">Arkéo</a> de la MOM pour générer des identifiants type <a href="http://fr.wikipedia.org/wiki/Archival_Resource_Key" target="_blank">ARK</a></p><p>Modules complémentaires :</p><ul><li><a href="https://github.com/brettwooldridge/HikariCP" target="_blank"><strong>Hikari</strong></a></li><li><a href="http://rdf4j.org/" target="_blank"><strong>RDF4J</strong></a></li><li>Kj-jzkit</li><li>...</li></ul><p>Partenaires :</p><ul><li><a href="http://www.cnrs.fr" target="_blank">CNRS</a></li><li><a href="http://www.mom.fr" target="_blank">MOM</a></li><li><a href="http://www.frantiq.fr" target="_blank">Frantiq</a></li><li><a href="http://www.mae.u-paris10.fr" target="_blank">MAE</a></li><li><a href="http://masa.hypotheses.org/" target="_blank">MASA</a></li><li><a href="http://www.huma-num.fr" target="_blank">Huma-Num</a></li></ul><p><br></p>', 'en');
INSERT INTO public.homepage (htmlcode, lang) VALUES ('<h1><img src="data:image/png;base64,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"> Opentheso</h1><h3>Copyright ©CNRS</h3><p><br></p><p>Opentheso est distribué sous licence <a href="http://www.cecill.info/licences.fr.html" rel="noopener noreferrer" target="_blank">CeCILL_C</a>, Licence libre de droit français compatible avec la licence <a href="http://www.gnu.org/copyleft/gpl.html" rel="noopener noreferrer" target="_blank">GNU GPL</a></p><p>C''est un gestionnaire de thesaurus multilingue, développé par la plateforme Technologique <a href="https://www.mom.fr/plateformes-technologiques/web-semantique-et-thesauri" rel="noopener noreferrer" target="_blank">WST</a> (Web Sémantique &amp; Thesauri) située à la <a href="https://www.mom.fr" rel="noopener noreferrer" target="_blank">MOM</a></p><p>en partenariat avec le <a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank">GDS-FRANTIQ</a></p><p><br></p><p>Le développement des versions 3 et 4 a bénéficié d’une participation financière du Consortium <a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank">MASA </a>(Mémoire des archéologues et des Sites Archéologiques) de la <a href="http://www.huma-num.fr/" rel="noopener noreferrer" target="_blank">TGIR Huma-Num</a>, ce financement a permis de produire une version FullWeb qui respecte la nouvelle norme des thésaurus ISO 25964.</p><p>Chef de Projet : <strong>Miled Rousset</strong></p><p>Développement : <strong>Miled Rousset</strong></p><p>Contributeurs : <strong>Prudham Jean-Marc, Quincy Mbape Eyoke, Antonio Perez, Carole Bonfré</strong></p><p>Partenariat, test et expertise : <strong>Les équipes du réseau </strong><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank"><strong>Frantiq</strong></a></p><p><br></p><p>Le développement a été réalisé avec les technologies suivantes :</p><ul><li>PostgreSQL pour la base des données</li><li>Java pour le module API et module métier</li><li>JSF2 et PrimeFaces pour la partie graphique</li></ul><p><br></p><p><strong>Opentheso</strong> s''appuie sur le projet <a href="http://ark.mom.fr" rel="noopener noreferrer" target="_blank">Arkéo</a> de la MOM pour générer des identifiants type <a href="http://fr.wikipedia.org/wiki/Archival_Resource_Key" rel="noopener noreferrer" target="_blank">ARK</a></p><p>Modules complémentaires :</p><ul><li><a href="https://github.com/brettwooldridge/HikariCP" rel="noopener noreferrer" target="_blank"><strong>Hikari</strong></a></li><li><a href="http://rdf4j.org/" rel="noopener noreferrer" target="_blank"><strong>RDF4J</strong></a></li><li>Kj-jzkit</li><li>...</li></ul><p>Partenaires :</p><ul><li><a href="http://www.cnrs.fr" rel="noopener noreferrer" target="_blank">CNRS</a></li><li><a href="http://www.mom.fr" rel="noopener noreferrer" target="_blank">MOM</a></li><li><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank">Frantiq</a></li><li><a href="http://www.mae.u-paris10.fr" rel="noopener noreferrer" target="_blank">MAE</a></li><li><a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank">MASA</a></li><li><a href="http://www.huma-num.fr" rel="noopener noreferrer" target="_blank">Huma-Num</a></li></ul><p><br></p>', 'fr');


--
-- TOC entry 3909 (class 0 OID 69899)
-- Dependencies: 237
-- Data for Name: images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3910 (class 0 OID 69906)
-- Dependencies: 238
-- Data for Name: info; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.info (version_opentheso, version_bdd) VALUES ('0.0.0', 'xyz');


--
-- TOC entry 3961 (class 0 OID 70573)
-- Dependencies: 289
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
-- TOC entry 3912 (class 0 OID 69921)
-- Dependencies: 240
-- Data for Name: node_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3913 (class 0 OID 69929)
-- Dependencies: 241
-- Data for Name: non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3914 (class 0 OID 69938)
-- Dependencies: 242
-- Data for Name: non_preferred_term_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3916 (class 0 OID 69948)
-- Dependencies: 244
-- Data for Name: note; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3918 (class 0 OID 69959)
-- Dependencies: 246
-- Data for Name: note_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3960 (class 0 OID 70564)
-- Dependencies: 288
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
-- TOC entry 3919 (class 0 OID 69974)
-- Dependencies: 247
-- Data for Name: nt_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (1, 'NT', 'Term spécifique', 'Narrower term');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (2, 'NTG', 'Term spécifique (generic)', 'Narrower term (generic)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (3, 'NTP', 'Term spécifique (partitive)', 'Narrower term (partitive)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (4, 'NTI', 'Term spécifique (instantial)', 'Narrower term (instantial)');


--
-- TOC entry 3920 (class 0 OID 69980)
-- Dependencies: 248
-- Data for Name: permuted; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3922 (class 0 OID 69988)
-- Dependencies: 250
-- Data for Name: preferences; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.preferences (id_pref, id_thesaurus, source_lang, identifier_type, path_image, dossier_resize, bdd_active, bdd_use_id, url_bdd, url_counter_bdd, z3950actif, collection_adresse, notice_url, url_encode, path_notice1, path_notice2, chemin_site, webservices, use_ark, server_ark, id_naan, prefix_ark, user_ark, pass_ark, use_handle, user_handle, pass_handle, path_key_handle, path_cert_handle, url_api_handle, prefix_handle, private_prefix_handle, preferredname, original_uri, original_uri_is_ark, original_uri_is_handle, uri_ark, generate_handle, auto_expand_tree) VALUES (1, 'th1', 'fr', 2, '/var/www/images/', 'resize', false, false, 'http://www.mondomaine.fr/concept/##value##', 'http://mondomaine.fr/concept/##conceptId##/total', false, 'KOHA/biblios', 'http://catalogue.mondomaine.fr/cgi-bin/koha/opac-search.pl?type=opac&op=do_search&q=an=terme', 'UTF-8', '/var/www/notices/repositories.xml', '/var/www/notices/SchemaMappings.xml', 'http://mondomaine.fr/', true, false, 'http://ark.mondomaine.fr/ark:/', '66666', 'crt', NULL, NULL, false, NULL, NULL, '/certificat/key.p12', '/certificat/cacerts2', 'https://handle-server.mondomaine.fr:8001/api/handles/', '66.666.66666', 'crt', 'th1', NULL, false, false, 'https://ark.mom.fr/ark:/', true, true);


--
-- TOC entry 3923 (class 0 OID 70021)
-- Dependencies: 251
-- Data for Name: preferences_sparql; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3924 (class 0 OID 70028)
-- Dependencies: 252
-- Data for Name: preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3925 (class 0 OID 70034)
-- Dependencies: 253
-- Data for Name: proposition; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3926 (class 0 OID 70042)
-- Dependencies: 254
-- Data for Name: relation_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3927 (class 0 OID 70048)
-- Dependencies: 255
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.roles (id, name, description) VALUES (1, 'superAdmin', 'Super Administrateur pour tout gérer tout thésaurus et tout utilisateur');
INSERT INTO public.roles (id, name, description) VALUES (2, 'admin', 'administrateur pour un domaine ou parc de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (3, 'manager', 'gestionnaire de thésaurus, pas de création de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (4, 'contributor', 'traducteur, notes, candidats, images');


--
-- TOC entry 3929 (class 0 OID 70056)
-- Dependencies: 257
-- Data for Name: routine_mail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3930 (class 0 OID 70063)
-- Dependencies: 258
-- Data for Name: split_non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3959 (class 0 OID 70555)
-- Dependencies: 287
-- Data for Name: status; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.status (id_status, value) VALUES (1, 'En attente');
INSERT INTO public.status (id_status, value) VALUES (2, 'Inséré');
INSERT INTO public.status (id_status, value) VALUES (3, 'Rejeté');


--
-- TOC entry 3932 (class 0 OID 70068)
-- Dependencies: 260
-- Data for Name: term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3934 (class 0 OID 70080)
-- Dependencies: 262
-- Data for Name: term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3936 (class 0 OID 70091)
-- Dependencies: 264
-- Data for Name: term_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3938 (class 0 OID 70102)
-- Dependencies: 266
-- Data for Name: thesaurus; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesaurus (id_thesaurus, id_ark, created, modified, id, private) VALUES ('th1', '', '2020-07-09 00:00:00', '2020-07-09 00:00:00', 1, false);


--
-- TOC entry 3939 (class 0 OID 70112)
-- Dependencies: 267
-- Data for Name: thesaurus_alignement_source; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3940 (class 0 OID 70118)
-- Dependencies: 268
-- Data for Name: thesaurus_array; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3941 (class 0 OID 70126)
-- Dependencies: 269
-- Data for Name: thesaurus_array_concept; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3942 (class 0 OID 70133)
-- Dependencies: 270
-- Data for Name: thesaurus_label; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', '', '', '', '2020-07-09 00:00:00', '2020-07-09 00:00:00', '', '', 'de', '', '', '', '', '', 'OpenEdition', '');
INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', '', '', '', '2020-07-09 00:00:00', '2020-07-09 00:00:00', '', '', 'it', '', '', '', '', '', 'OpenEdition', '');
INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', '', '', '', '2020-07-09 00:00:00', '2020-07-09 00:00:00', '', '', 'pt', '', '', '', '', '', 'OpenEdition', '');
INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', '', '', '', '2020-07-09 00:00:00', '2020-07-09 00:00:00', '', '', 'en', '', '', '', '', '', 'OpenEdition', '');
INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', '', '', '', '2020-07-09 00:00:00', '2020-07-09 00:00:00', '', '', 'fr', '', '', '', '', '', 'OpenEdition', '');
INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', '', '', '', '2020-07-09 00:00:00', '2020-07-09 00:00:00', '', '', 'es', '', '', '', '', '', 'OpenEdition', '');


--
-- TOC entry 3943 (class 0 OID 70141)
-- Dependencies: 271
-- Data for Name: thesohomepage; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>texte pour New Th47</p>', 'fr', 'th47');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>texte pour Theso_th54</p>', 'fr', 'th54');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>Unesco thésaurus FR</p>', 'fr', 'th44');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>Unesco thesaurus EN</p>', 'en', 'th44');
INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>à propos de Essai 1 </p>', 'fr', 'th55');


--
-- TOC entry 3946 (class 0 OID 70151)
-- Dependencies: 274
-- Data for Name: user_group_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3947 (class 0 OID 70158)
-- Dependencies: 275
-- Data for Name: user_group_thesaurus; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3948 (class 0 OID 70164)
-- Dependencies: 276
-- Data for Name: user_role_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3949 (class 0 OID 70167)
-- Dependencies: 277
-- Data for Name: user_role_only_on; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3950 (class 0 OID 70174)
-- Dependencies: 278
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.users (id_user, username, password, active, mail, passtomodify, alertmail, issuperadmin) VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', true, 'admin@domaine.fr', false, false, true);


--
-- TOC entry 3951 (class 0 OID 70185)
-- Dependencies: 279
-- Data for Name: users2; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3952 (class 0 OID 70194)
-- Dependencies: 280
-- Data for Name: users_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3953 (class 0 OID 70202)
-- Dependencies: 281
-- Data for Name: version_history; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 3972 (class 0 OID 0)
-- Dependencies: 198
-- Name: alignement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_id_seq', 1, false);


--
-- TOC entry 3973 (class 0 OID 0)
-- Dependencies: 200
-- Name: alignement_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_preferences_id_seq', 1, false);


--
-- TOC entry 3974 (class 0 OID 0)
-- Dependencies: 202
-- Name: alignement_source__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_source__id_seq', 120, true);


--
-- TOC entry 3975 (class 0 OID 0)
-- Dependencies: 283
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.candidat_messages_id_message_seq', 1, false);


--
-- TOC entry 3976 (class 0 OID 0)
-- Dependencies: 207
-- Name: concept__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept__id_seq', 1, false);


--
-- TOC entry 3977 (class 0 OID 0)
-- Dependencies: 209
-- Name: concept_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_candidat__id_seq', 1, false);


--
-- TOC entry 3978 (class 0 OID 0)
-- Dependencies: 212
-- Name: concept_group__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group__id_seq', 1, false);


--
-- TOC entry 3979 (class 0 OID 0)
-- Dependencies: 215
-- Name: concept_group_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_historique__id_seq', 1, false);


--
-- TOC entry 3980 (class 0 OID 0)
-- Dependencies: 219
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_label_historique__id_seq', 1, false);


--
-- TOC entry 3981 (class 0 OID 0)
-- Dependencies: 217
-- Name: concept_group_label_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_label_id_seq', 1, false);


--
-- TOC entry 3982 (class 0 OID 0)
-- Dependencies: 222
-- Name: concept_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_historique__id_seq', 1, false);


--
-- TOC entry 3983 (class 0 OID 0)
-- Dependencies: 230
-- Name: facet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.facet_id_seq', 1, false);


--
-- TOC entry 3984 (class 0 OID 0)
-- Dependencies: 232
-- Name: gps_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gps_preferences_id_seq', 1, false);


--
-- TOC entry 3985 (class 0 OID 0)
-- Dependencies: 239
-- Name: languages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.languages_id_seq', 189, false);


--
-- TOC entry 3986 (class 0 OID 0)
-- Dependencies: 243
-- Name: note__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.note__id_seq', 1, false);


--
-- TOC entry 3987 (class 0 OID 0)
-- Dependencies: 245
-- Name: note_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.note_historique__id_seq', 1, false);


--
-- TOC entry 3988 (class 0 OID 0)
-- Dependencies: 249
-- Name: pref__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pref__id_seq', 1, true);


--
-- TOC entry 3989 (class 0 OID 0)
-- Dependencies: 256
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.role_id_seq', 6, true);


--
-- TOC entry 3990 (class 0 OID 0)
-- Dependencies: 286
-- Name: status_id_status_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.status_id_status_seq', 1, false);


--
-- TOC entry 3991 (class 0 OID 0)
-- Dependencies: 259
-- Name: term__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term__id_seq', 1, false);


--
-- TOC entry 3992 (class 0 OID 0)
-- Dependencies: 261
-- Name: term_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term_candidat__id_seq', 1, false);


--
-- TOC entry 3993 (class 0 OID 0)
-- Dependencies: 263
-- Name: term_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term_historique__id_seq', 1, false);


--
-- TOC entry 3994 (class 0 OID 0)
-- Dependencies: 265
-- Name: thesaurus_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.thesaurus_id_seq', 1, true);


--
-- TOC entry 3995 (class 0 OID 0)
-- Dependencies: 272
-- Name: user__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user__id_seq', 2, false);


--
-- TOC entry 3996 (class 0 OID 0)
-- Dependencies: 273
-- Name: user_group_label__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user_group_label__id_seq', 1, false);


--
-- TOC entry 3736 (class 2606 OID 70212)
-- Name: version_history VersionHistory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.version_history
    ADD CONSTRAINT "VersionHistory_pkey" PRIMARY KEY ("idVersionhistory");


--
-- TOC entry 3573 (class 2606 OID 70214)
-- Name: alignement alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement
    ADD CONSTRAINT alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key UNIQUE (internal_id_concept, internal_id_thesaurus, uri_target);


--
-- TOC entry 3575 (class 2606 OID 70216)
-- Name: alignement alignement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement
    ADD CONSTRAINT alignement_pkey PRIMARY KEY (id);


--
-- TOC entry 3577 (class 2606 OID 70218)
-- Name: alignement_preferences alignement_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_preferences
    ADD CONSTRAINT alignement_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_concept_depart, id_alignement_source);


--
-- TOC entry 3579 (class 2606 OID 70220)
-- Name: alignement_source alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_source
    ADD CONSTRAINT alignement_source_pkey PRIMARY KEY (id);


--
-- TOC entry 3581 (class 2606 OID 70222)
-- Name: alignement_source alignement_source_source_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_source
    ADD CONSTRAINT alignement_source_source_key UNIQUE (source);


--
-- TOC entry 3583 (class 2606 OID 70224)
-- Name: alignement_type alignment_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_type
    ADD CONSTRAINT alignment_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3585 (class 2606 OID 70226)
-- Name: bt_type bt_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bt_type
    ADD CONSTRAINT bt_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3587 (class 2606 OID 70228)
-- Name: bt_type bt_type_relation_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bt_type
    ADD CONSTRAINT bt_type_relation_key UNIQUE (relation);


--
-- TOC entry 3740 (class 2606 OID 70546)
-- Name: candidat_messages candidat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_messages
    ADD CONSTRAINT candidat_messages_pkey PRIMARY KEY (id_message);


--
-- TOC entry 3589 (class 2606 OID 70230)
-- Name: compound_equivalence compound_equivalence_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compound_equivalence
    ADD CONSTRAINT compound_equivalence_pkey PRIMARY KEY (id_split_nonpreferredterm, id_preferredterm);


--
-- TOC entry 3594 (class 2606 OID 70232)
-- Name: concept_candidat concept_candidat_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_candidat
    ADD CONSTRAINT concept_candidat_id_key UNIQUE (id);


--
-- TOC entry 3596 (class 2606 OID 70234)
-- Name: concept_candidat concept_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_candidat
    ADD CONSTRAINT concept_candidat_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3614 (class 2606 OID 70682)
-- Name: concept_historique concept_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_historique
    ADD CONSTRAINT concept_copy_pkey PRIMARY KEY (id_concept, id_thesaurus, id_group, id_user, modified);


--
-- TOC entry 3598 (class 2606 OID 70238)
-- Name: concept_fusion concept_fusion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_fusion
    ADD CONSTRAINT concept_fusion_pkey PRIMARY KEY (id_concept1, id_concept2, id_thesaurus);


--
-- TOC entry 3602 (class 2606 OID 70240)
-- Name: concept_group_concept concept_group_concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_concept
    ADD CONSTRAINT concept_group_concept_pkey PRIMARY KEY (idgroup, idthesaurus, idconcept);


--
-- TOC entry 3604 (class 2606 OID 70242)
-- Name: concept_group_historique concept_group_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_historique
    ADD CONSTRAINT concept_group_copy_pkey PRIMARY KEY (idgroup, idthesaurus, modified, id_user);


--
-- TOC entry 3610 (class 2606 OID 70244)
-- Name: concept_group_label_historique concept_group_label_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_label_historique
    ADD CONSTRAINT concept_group_label_copy_pkey PRIMARY KEY (lang, idthesaurus, lexicalvalue, modified, id_user);


--
-- TOC entry 3606 (class 2606 OID 70246)
-- Name: concept_group_label concept_group_label_idgrouplabel_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_label
    ADD CONSTRAINT concept_group_label_idgrouplabel_key UNIQUE (id);


--
-- TOC entry 3608 (class 2606 OID 70248)
-- Name: concept_group_label concept_group_label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_label
    ADD CONSTRAINT concept_group_label_pkey PRIMARY KEY (lang, idthesaurus, lexicalvalue);


--
-- TOC entry 3600 (class 2606 OID 70250)
-- Name: concept_group concept_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group
    ADD CONSTRAINT concept_group_pkey PRIMARY KEY (idgroup, idthesaurus);


--
-- TOC entry 3612 (class 2606 OID 70252)
-- Name: concept_group_type concept_group_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_type
    ADD CONSTRAINT concept_group_type_pkey PRIMARY KEY (code, label);


--
-- TOC entry 3616 (class 2606 OID 70254)
-- Name: concept_orphan concept_orphan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_orphan
    ADD CONSTRAINT concept_orphan_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3592 (class 2606 OID 70256)
-- Name: concept concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept
    ADD CONSTRAINT concept_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3618 (class 2606 OID 70258)
-- Name: concept_term_candidat concept_term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_term_candidat
    ADD CONSTRAINT concept_term_candidat_pkey PRIMARY KEY (id_concept, id_term, id_thesaurus);


--
-- TOC entry 3620 (class 2606 OID 70260)
-- Name: copyright copyright_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.copyright
    ADD CONSTRAINT copyright_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 3738 (class 2606 OID 70533)
-- Name: corpus_link corpus_link_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.corpus_link
    ADD CONSTRAINT corpus_link_pkey PRIMARY KEY (id_theso, corpus_name);


--
-- TOC entry 3622 (class 2606 OID 70262)
-- Name: custom_concept_attribute custom_concept_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_concept_attribute
    ADD CONSTRAINT custom_concept_attribute_pkey PRIMARY KEY ("idConcept");


--
-- TOC entry 3624 (class 2606 OID 70264)
-- Name: custom_term_attribute custom_term_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_term_attribute
    ADD CONSTRAINT custom_term_attribute_pkey PRIMARY KEY (identifier);


--
-- TOC entry 3626 (class 2606 OID 70266)
-- Name: external_images external_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_images
    ADD CONSTRAINT external_images_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 3628 (class 2606 OID 70655)
-- Name: gps gps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gps
    ADD CONSTRAINT gps_pkey PRIMARY KEY (id_concept, id_theso);


--
-- TOC entry 3630 (class 2606 OID 70270)
-- Name: gps_preferences gps_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gps_preferences
    ADD CONSTRAINT gps_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_alignement_source);


--
-- TOC entry 3634 (class 2606 OID 70272)
-- Name: hierarchical_relationship_historique hierarchical_relationship_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchical_relationship_historique
    ADD CONSTRAINT hierarchical_relationship_copy_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2, modified, id_user);


--
-- TOC entry 3632 (class 2606 OID 70274)
-- Name: hierarchical_relationship hierarchical_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchical_relationship
    ADD CONSTRAINT hierarchical_relationship_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2);


--
-- TOC entry 3636 (class 2606 OID 70276)
-- Name: homepage homepage_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.homepage
    ADD CONSTRAINT homepage_lang_key UNIQUE (lang);


--
-- TOC entry 3638 (class 2606 OID 70278)
-- Name: images images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.images
    ADD CONSTRAINT images_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 3640 (class 2606 OID 70280)
-- Name: info info_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.info
    ADD CONSTRAINT info_pkey PRIMARY KEY (version_opentheso, version_bdd);


--
-- TOC entry 3746 (class 2606 OID 70583)
-- Name: languages_iso639 languages_iso639_iso639_1_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_iso639_1_key UNIQUE (iso639_1);


--
-- TOC entry 3748 (class 2606 OID 70581)
-- Name: languages_iso639 languages_iso639_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_pkey PRIMARY KEY (id);


--
-- TOC entry 3642 (class 2606 OID 70286)
-- Name: node_label node_label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.node_label
    ADD CONSTRAINT node_label_pkey PRIMARY KEY (facet_id, id_thesaurus, lang);


--
-- TOC entry 3645 (class 2606 OID 70288)
-- Name: non_preferred_term non_prefered_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.non_preferred_term
    ADD CONSTRAINT non_prefered_term_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 3648 (class 2606 OID 70290)
-- Name: non_preferred_term_historique non_preferred_term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.non_preferred_term_historique
    ADD CONSTRAINT non_preferred_term_copy_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, modified, id_user);


--
-- TOC entry 3657 (class 2606 OID 70292)
-- Name: note_historique note_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_historique
    ADD CONSTRAINT note_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 3651 (class 2606 OID 70651)
-- Name: note note_notetypecode_id_thesaurus_id_concept_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_notetypecode_id_thesaurus_id_concept_lang_key UNIQUE (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue);


--
-- TOC entry 3653 (class 2606 OID 70653)
-- Name: note note_notetypecode_id_thesaurus_id_term_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_notetypecode_id_thesaurus_id_term_lang_key UNIQUE (notetypecode, id_thesaurus, id_term, lang, lexicalvalue);


--
-- TOC entry 3655 (class 2606 OID 70298)
-- Name: note note_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_pkey PRIMARY KEY (id);


--
-- TOC entry 3659 (class 2606 OID 70300)
-- Name: nt_type nt_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3661 (class 2606 OID 70302)
-- Name: nt_type nt_type_relation_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_relation_key UNIQUE (relation);


--
-- TOC entry 3664 (class 2606 OID 70304)
-- Name: permuted permuted_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permuted
    ADD CONSTRAINT permuted_pkey PRIMARY KEY (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm);


--
-- TOC entry 3744 (class 2606 OID 70572)
-- Name: note_type pk_note_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_type
    ADD CONSTRAINT pk_note_type PRIMARY KEY (code);


--
-- TOC entry 3678 (class 2606 OID 70308)
-- Name: relation_group pk_relation_group; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.relation_group
    ADD CONSTRAINT pk_relation_group PRIMARY KEY (id_group1, id_thesaurus, relation, id_group2);


--
-- TOC entry 3666 (class 2606 OID 70310)
-- Name: preferences preferences_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 3668 (class 2606 OID 70312)
-- Name: preferences preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_pkey PRIMARY KEY (id_pref);


--
-- TOC entry 3670 (class 2606 OID 70314)
-- Name: preferences preferences_preferredname_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_preferredname_key UNIQUE (preferredname);


--
-- TOC entry 3672 (class 2606 OID 70316)
-- Name: preferences_sparql preferences_sparql_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences_sparql
    ADD CONSTRAINT preferences_sparql_pkey PRIMARY KEY (thesaurus);


--
-- TOC entry 3674 (class 2606 OID 70318)
-- Name: preferred_term preferred_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferred_term
    ADD CONSTRAINT preferred_term_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 3676 (class 2606 OID 70320)
-- Name: proposition proposition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposition
    ADD CONSTRAINT proposition_pkey PRIMARY KEY (id_concept, id_user, id_thesaurus);


--
-- TOC entry 3680 (class 2606 OID 70322)
-- Name: roles role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- TOC entry 3682 (class 2606 OID 70324)
-- Name: routine_mail routine_mail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routine_mail
    ADD CONSTRAINT routine_mail_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 3742 (class 2606 OID 70563)
-- Name: status status_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_pkey PRIMARY KEY (id_status);


--
-- TOC entry 3693 (class 2606 OID 70326)
-- Name: term_candidat term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term_candidat
    ADD CONSTRAINT term_candidat_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, contributor);


--
-- TOC entry 3696 (class 2606 OID 70680)
-- Name: term_historique term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term_historique
    ADD CONSTRAINT term_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 3685 (class 2606 OID 70330)
-- Name: term term_id_term_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_key UNIQUE (id_term, lang, id_thesaurus);


--
-- TOC entry 3687 (class 2606 OID 70332)
-- Name: term term_id_term_lexical_value_lang_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_lexical_value_lang_id_thesaurus_key UNIQUE (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 3690 (class 2606 OID 70334)
-- Name: term term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_pkey PRIMARY KEY (id);


--
-- TOC entry 3700 (class 2606 OID 70336)
-- Name: thesaurus_alignement_source thesaurus_alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_alignement_source
    ADD CONSTRAINT thesaurus_alignement_source_pkey PRIMARY KEY (id_thesaurus, id_alignement_source);


--
-- TOC entry 3704 (class 2606 OID 70338)
-- Name: thesaurus_array_concept thesaurus_array_concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_array_concept
    ADD CONSTRAINT thesaurus_array_concept_pkey PRIMARY KEY (thesaurusarrayid, id_concept, id_thesaurus);


--
-- TOC entry 3702 (class 2606 OID 70340)
-- Name: thesaurus_array thesaurus_array_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_array
    ADD CONSTRAINT thesaurus_array_pkey PRIMARY KEY (facet_id, id_thesaurus, id_concept_parent);


--
-- TOC entry 3706 (class 2606 OID 70342)
-- Name: thesaurus_label thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT thesaurus_pkey PRIMARY KEY (id_thesaurus, lang, title);


--
-- TOC entry 3698 (class 2606 OID 70344)
-- Name: thesaurus thesaurus_pkey1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus
    ADD CONSTRAINT thesaurus_pkey1 PRIMARY KEY (id_thesaurus, id_ark);


--
-- TOC entry 3710 (class 2606 OID 70346)
-- Name: thesohomepage thesohomepage_idtheso_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesohomepage
    ADD CONSTRAINT thesohomepage_idtheso_lang_key UNIQUE (idtheso, lang);


--
-- TOC entry 3708 (class 2606 OID 70348)
-- Name: thesaurus_label unique_thesau_lang; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT unique_thesau_lang UNIQUE (id_thesaurus, lang);


--
-- TOC entry 3712 (class 2606 OID 70350)
-- Name: user_group_label user_group-label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_label
    ADD CONSTRAINT "user_group-label_pkey" PRIMARY KEY (id_group);


--
-- TOC entry 3718 (class 2606 OID 70708)
-- Name: user_role_group user_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_group
    ADD CONSTRAINT user_group_pkey UNIQUE (id_user, id_group);


--
-- TOC entry 3714 (class 2606 OID 70354)
-- Name: user_group_thesaurus user_group_thesaurus_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 3716 (class 2606 OID 70356)
-- Name: user_group_thesaurus user_group_thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_pkey PRIMARY KEY (id_group, id_thesaurus);


--
-- TOC entry 3722 (class 2606 OID 70358)
-- Name: users user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT user_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3720 (class 2606 OID 70360)
-- Name: user_role_only_on user_role_only_on_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_only_on
    ADD CONSTRAINT user_role_only_on_pkey PRIMARY KEY (id_user, id_role, id_theso);


--
-- TOC entry 3734 (class 2606 OID 70362)
-- Name: users_historique users_historique_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users_historique
    ADD CONSTRAINT users_historique_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3728 (class 2606 OID 70364)
-- Name: users2 users_login_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_login_key UNIQUE (login);


--
-- TOC entry 3730 (class 2606 OID 70366)
-- Name: users2 users_mail_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_mail_key UNIQUE (mail);


--
-- TOC entry 3724 (class 2606 OID 70368)
-- Name: users users_mail_key1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_mail_key1 UNIQUE (mail);


--
-- TOC entry 3732 (class 2606 OID 70370)
-- Name: users2 users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_pkey PRIMARY KEY (id_user);


--
-- TOC entry 3726 (class 2606 OID 70372)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 3590 (class 1259 OID 70373)
-- Name: concept_notation_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX concept_notation_unaccent ON public.concept USING gin (public.f_unaccent(lower((notation)::text)) public.gin_trgm_ops);


--
-- TOC entry 3683 (class 1259 OID 70374)
-- Name: index_lexical_value; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value ON public.term USING btree (lexical_value);


--
-- TOC entry 3694 (class 1259 OID 70375)
-- Name: index_lexical_value_copy; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value_copy ON public.term_historique USING btree (lexical_value);


--
-- TOC entry 3643 (class 1259 OID 70376)
-- Name: index_lexical_value_npt; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value_npt ON public.non_preferred_term USING btree (lexical_value);


--
-- TOC entry 3649 (class 1259 OID 70377)
-- Name: note_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX note_lexical_value_unaccent ON public.note USING gin (public.f_unaccent(lower((lexicalvalue)::text)) public.gin_trgm_ops);


--
-- TOC entry 3662 (class 1259 OID 70378)
-- Name: permuted_lexical_value_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX permuted_lexical_value_idx ON public.permuted USING btree (lexical_value);


--
-- TOC entry 3646 (class 1259 OID 70379)
-- Name: term_lexical_value_npt_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX term_lexical_value_npt_unaccent ON public.non_preferred_term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 3688 (class 1259 OID 70380)
-- Name: term_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX term_lexical_value_unaccent ON public.term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 3691 (class 1259 OID 70381)
-- Name: terms_values_gin; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX terms_values_gin ON public.term USING gin (lexical_value public.gin_trgm_ops);


-- Completed on 2020-07-09 12:18:31 CEST

--
-- PostgreSQL database dump complete
--

