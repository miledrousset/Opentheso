--
-- PostgreSQL database dump
--

-- Dumped from database version 14.2
-- Dumped by pg_dump version 16.0

-- Started on 2024-02-14 11:31:14 CET

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
-- TOC entry 6 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO postgres;

--
-- TOC entry 2 (class 3079 OID 104861)
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- TOC entry 4458 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- TOC entry 3 (class 3079 OID 104942)
-- Name: unaccent; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;


--
-- TOC entry 4459 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION unaccent; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION unaccent IS 'text search dictionary that removes accents';


--
-- TOC entry 1020 (class 1247 OID 104950)
-- Name: alignement_format; Type: TYPE; Schema: public; Owner: opentheso
--

CREATE TYPE public.alignement_format AS ENUM (
    'skos',
    'json',
    'xml'
);


ALTER TYPE public.alignement_format OWNER TO opentheso;

--
-- TOC entry 1023 (class 1247 OID 104958)
-- Name: alignement_type_rqt; Type: TYPE; Schema: public; Owner: opentheso
--

CREATE TYPE public.alignement_type_rqt AS ENUM (
    'SPARQL',
    'REST'
);


ALTER TYPE public.alignement_type_rqt OWNER TO opentheso;

--
-- TOC entry 1026 (class 1247 OID 104964)
-- Name: auth_method; Type: TYPE; Schema: public; Owner: opentheso
--

CREATE TYPE public.auth_method AS ENUM (
    'DB',
    'LDAP',
    'FILE',
    'test'
);


ALTER TYPE public.auth_method OWNER TO opentheso;

--
-- TOC entry 356 (class 1255 OID 104973)
-- Name: f_unaccent(text); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.f_unaccent(text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
SELECT public.unaccent('public.unaccent', $1)
$_$;


ALTER FUNCTION public.f_unaccent(text) OWNER TO opentheso;

--
-- TOC entry 357 (class 1255 OID 104974)
-- Name: naturalsort(text); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.naturalsort(text) RETURNS bytea
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
    select string_agg(convert_to(coalesce(r[2], length(length(r[1])::text) || length(r[1])::text || r[1]), 'SQL_ASCII'),'\x00')
    from regexp_matches($1, '0*([0-9]+)|([^0-9]+)', 'g') r;
$_$;


ALTER FUNCTION public.naturalsort(text) OWNER TO opentheso;

--
-- TOC entry 365 (class 1255 OID 104975)
-- Name: opentheso_add_alignements(text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_alignements(IN alignements text)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER PROCEDURE public.opentheso_add_alignements(IN alignements text) OWNER TO opentheso;

--
-- TOC entry 366 (class 1255 OID 104976)
-- Name: opentheso_add_concept_dcterms(character varying, character varying, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_concept_dcterms(IN id_concept character varying, IN id_thesaurus character varying, IN dcterms text)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER PROCEDURE public.opentheso_add_concept_dcterms(IN id_concept character varying, IN id_thesaurus character varying, IN dcterms text) OWNER TO opentheso;

--
-- TOC entry 375 (class 1255 OID 104977)
-- Name: opentheso_add_custom_relations(character varying, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_custom_relations(IN id_thesaurus character varying, IN relations text)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER PROCEDURE public.opentheso_add_custom_relations(IN id_thesaurus character varying, IN relations text) OWNER TO opentheso;

--
-- TOC entry 376 (class 1255 OID 104978)
-- Name: opentheso_add_external_images(character varying, character varying, integer, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_external_images(IN id_thesaurus character varying, IN id_concept character varying, IN id_user integer, IN images text)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER PROCEDURE public.opentheso_add_external_images(IN id_thesaurus character varying, IN id_concept character varying, IN id_user integer, IN images text) OWNER TO opentheso;

--
-- TOC entry 377 (class 1255 OID 104979)
-- Name: opentheso_add_facet(character varying, integer, character varying, character varying, text, text, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_facet(IN id_facet character varying, IN id_user integer, IN id_thesaurus character varying, IN id_conceotparent character varying, IN labels text, IN membres text, IN notes text)
    LANGUAGE plpgsql
    AS $$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	
	label_rec record;
	membres_rec record;
	array_string   text[];
	isFirst boolean;
BEGIN
	isFirst = false;
	FOR label_rec IN SELECT unnest(string_to_array(labels, seperateur)) AS label_value
    LOOP
		SELECT string_to_array(label_rec.label_value, sous_seperateur) INTO array_string;
		
		if (isFirst = false) then
			isFirst = true;
			INSERT INTO node_label(id_facet, id_thesaurus, lexical_value, created, modified, lang) 
				VALUES (id_facet, id_thesaurus, array_string[1], CURRENT_DATE, CURRENT_DATE, array_string[2]);
							
			INSERT INTO thesaurus_array(id_thesaurus, id_concept_parent, id_facet) VALUES (id_thesaurus, id_conceotParent, id_facet);
		ELSE
			Insert into node_label (id_facet, id_thesaurus, lexical_value, lang) values (id_facet, id_thesaurus, array_string[1], array_string[2]);	
		END IF;
	END LOOP;
	
	FOR membres_rec IN SELECT unnest(string_to_array(membres, seperateur)) AS membre_value
    LOOP		
		INSERT INTO concept_facet(id_facet, id_thesaurus, id_concept) VALUES (id_facet, id_thesaurus, membres_rec.membre_value);
	END LOOP;
	IF (notes IS NOT NULL AND notes != 'null') THEN
		-- 'value@typeCode@lang@id_term'
		CALL opentheso_add_notes(id_facet, id_thesaurus, id_user, notes);
	END IF;	
END;
$$;


ALTER PROCEDURE public.opentheso_add_facet(IN id_facet character varying, IN id_user integer, IN id_thesaurus character varying, IN id_conceotparent character varying, IN labels text, IN membres text, IN notes text) OWNER TO opentheso;

--
-- TOC entry 378 (class 1255 OID 104980)
-- Name: opentheso_add_gps(character varying, character varying, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_gps(IN id_concept character varying, IN id_thesaurus character varying, IN gpslist text)
    LANGUAGE plpgsql
    AS $$
DECLARE
    seperateur constant varchar := '##';
    sous_seperateur constant varchar := '@@';

    pos numeric;
    gps_rec record;
    array_string   text[];
BEGIN
    pos = 1;
    FOR gps_rec IN SELECT unnest(string_to_array(gpsList, seperateur)) AS gps_value
        LOOP
            SELECT string_to_array(gps_rec.gps_value, sous_seperateur) INTO array_string;
            IF array_string[1] IS NOT NULL THEN
                insert into gps(position, id_concept, id_theso, latitude, longitude)
                    values (pos, id_concept, id_thesaurus, CAST (array_string[1] AS double precision), CAST (array_string[2] AS double precision));
                pos = pos + 1;
            END IF;
        END LOOP;
END;
$$;


ALTER PROCEDURE public.opentheso_add_gps(IN id_concept character varying, IN id_thesaurus character varying, IN gpslist text) OWNER TO opentheso;

--
-- TOC entry 358 (class 1255 OID 104981)
-- Name: opentheso_add_hierarchical_relations(character varying, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_hierarchical_relations(IN id_thesaurus character varying, IN relations text)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER PROCEDURE public.opentheso_add_hierarchical_relations(IN id_thesaurus character varying, IN relations text) OWNER TO opentheso;

--
-- TOC entry 379 (class 1255 OID 104982)
-- Name: opentheso_add_new_concept(character varying, character varying, integer, character varying, text, character varying, character varying, boolean, character varying, character varying, text, text, text, text, text, text, text, text, boolean, text, date, date, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_new_concept(IN id_thesaurus character varying, IN id_con character varying, IN id_user integer, IN conceptstatus character varying, IN concepttype text, IN notationconcept character varying, IN id_ark character varying, IN istopconcept boolean, IN id_handle character varying, IN id_doi character varying, IN prefterms text, IN relation_hiarchique text, IN custom_relation text, IN notes text, IN non_pref_terms text, IN alignements text, IN images text, IN idsconceptsreplaceby text, IN isgpspresent boolean, IN gps text, IN created date, IN modified date, IN concept_dcterms text)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER PROCEDURE public.opentheso_add_new_concept(IN id_thesaurus character varying, IN id_con character varying, IN id_user integer, IN conceptstatus character varying, IN concepttype text, IN notationconcept character varying, IN id_ark character varying, IN istopconcept boolean, IN id_handle character varying, IN id_doi character varying, IN prefterms text, IN relation_hiarchique text, IN custom_relation text, IN notes text, IN non_pref_terms text, IN alignements text, IN images text, IN idsconceptsreplaceby text, IN isgpspresent boolean, IN gps text, IN created date, IN modified date, IN concept_dcterms text) OWNER TO opentheso;

--
-- TOC entry 380 (class 1255 OID 104983)
-- Name: opentheso_add_non_preferred_term(character varying, integer, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_non_preferred_term(IN id_thesaurus character varying, IN id_user integer, IN non_pref_terms text)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER PROCEDURE public.opentheso_add_non_preferred_term(IN id_thesaurus character varying, IN id_user integer, IN non_pref_terms text) OWNER TO opentheso;

--
-- TOC entry 381 (class 1255 OID 104984)
-- Name: opentheso_add_notes(character varying, character varying, integer, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_notes(IN id_concept character varying, IN id_thesaurus character varying, IN id_user integer, IN notes text)
    LANGUAGE plpgsql
    AS $$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	
	notes_rec record;
	array_string   text[];
BEGIN

	FOR notes_rec IN SELECT unnest(string_to_array(notes, seperateur)) AS note_value
    LOOP
		SELECT string_to_array(notes_rec.note_value, sous_seperateur) INTO array_string;
		
		insert into note (notetypecode, id_thesaurus, lang, lexicalvalue, id_user, identifier) 
			values (array_string[2], id_thesaurus, array_string[3], array_string[1], id_user, id_concept);
		Insert into note_historique (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue, action_performed, id_user)
			values (array_string[2], id_thesaurus, id_concept, array_string[3], array_string[1], 'add', id_user);
	END LOOP;
END;
$$;


ALTER PROCEDURE public.opentheso_add_notes(IN id_concept character varying, IN id_thesaurus character varying, IN id_user integer, IN notes text) OWNER TO opentheso;

--
-- TOC entry 382 (class 1255 OID 104985)
-- Name: opentheso_add_terms(character varying, character varying, character varying, integer, text); Type: PROCEDURE; Schema: public; Owner: opentheso
--

CREATE PROCEDURE public.opentheso_add_terms(IN id_term character varying, IN id_thesaurus character varying, IN id_concept character varying, IN id_user integer, IN terms text)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER PROCEDURE public.opentheso_add_terms(IN id_term character varying, IN id_thesaurus character varying, IN id_concept character varying, IN id_user integer, IN terms text) OWNER TO opentheso;

--
-- TOC entry 383 (class 1255 OID 104986)
-- Name: opentheso_get_alignements(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_alignements(id_theso character varying, id_con character varying) RETURNS TABLE(alig_uri_target character varying, alig_id_type integer)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT uri_target, alignement_id_type
		FROM alignement
		where internal_id_concept = id_con
		and internal_id_thesaurus = id_theso;
end;
$$;


ALTER FUNCTION public.opentheso_get_alignements(id_theso character varying, id_con character varying) OWNER TO opentheso;

--
-- TOC entry 384 (class 1255 OID 104987)
-- Name: opentheso_get_all_preflabel(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_all_preflabel(id_theso character varying, id_con character varying) RETURNS TABLE(term_id character varying, term_lexical_value character varying, term_lang character varying)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.opentheso_get_all_preflabel(id_theso character varying, id_con character varying) OWNER TO opentheso;

--
-- TOC entry 385 (class 1255 OID 104988)
-- Name: opentheso_get_alter_term(character varying, character varying, boolean); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_alter_term(id_theso character varying, id_con character varying, ishiden boolean) RETURNS TABLE(alter_term_lexical_value character varying, alter_term_source character varying, alter_term_status character varying, alter_term_lang character varying)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.opentheso_get_alter_term(id_theso character varying, id_con character varying, ishiden boolean) OWNER TO opentheso;

--
-- TOC entry 386 (class 1255 OID 104989)
-- Name: opentheso_get_altlabel(character varying, character varying, character varying, boolean); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_altlabel(idtheso character varying, idconcept character varying, idlang character varying, ishiden boolean) RETURNS TABLE(idterm character varying, altlabel character varying, unique_id integer)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT non_preferred_term.id_term, non_preferred_term.lexical_value, non_preferred_term.id
		FROM non_preferred_term, preferred_term
		WHERE preferred_term.id_term = non_preferred_term.id_term
		AND preferred_term.id_thesaurus = non_preferred_term.id_thesaurus
		AND preferred_term.id_concept = idconcept
		AND non_preferred_term.id_thesaurus = idtheso
		AND non_preferred_term.lang = idlang
		AND non_preferred_term.hiden = isHiden;
end;
$$;


ALTER FUNCTION public.opentheso_get_altlabel(idtheso character varying, idconcept character varying, idlang character varying, ishiden boolean) OWNER TO opentheso;

--
-- TOC entry 387 (class 1255 OID 104990)
-- Name: opentheso_get_altlabel_traductions(character varying, character varying, character varying, boolean); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_altlabel_traductions(idtheso character varying, idconcept character varying, idlang character varying, ishiden boolean) RETURNS TABLE(altlabel_id character varying, altlabel character varying, idlang_alt character varying, unique_id integer)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT non_preferred_term.id_term, non_preferred_term.lexical_value, non_preferred_term.lang, non_preferred_term.id
		FROM non_preferred_term, preferred_term
		WHERE preferred_term.id_term = non_preferred_term.id_term
		AND preferred_term.id_thesaurus = non_preferred_term.id_thesaurus
		AND preferred_term.id_concept = idconcept
		AND non_preferred_term.id_thesaurus = idtheso
		AND non_preferred_term.lang != idlang
		AND non_preferred_term.hiden = isHiden;
end;
$$;


ALTER FUNCTION public.opentheso_get_altlabel_traductions(idtheso character varying, idconcept character varying, idlang character varying, ishiden boolean) OWNER TO opentheso;

--
-- TOC entry 388 (class 1255 OID 104991)
-- Name: opentheso_get_concept(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_concept(idtheso character varying, idconcept character varying, idlang character varying) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
DECLARE

    seperateur constant varchar := '##';
    sous_seperateur constant varchar := '@@';

    rec record;
    con record;
    theso_rec record;
    traduction_rec record;
    altlab_rec record;
    altlab_hiden_rec record;
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
    contributor_rec record;
    preflab_selected_rec record;
    altlab_selected_rec record;
    altlab_hiden_selected_rec record;

    tmp text;
    uri text;
    local_URI text;

    prefLab_selected VARCHAR;
    altLab_selected VARCHAR;
    altLab_hiden_selected VARCHAR;

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
    tmpLabel text;
BEGIN
		SELECT * INTO con FROM concept WHERE id_thesaurus = idtheso AND id_concept = idconcept AND status != 'CA';
		SELECT * INTO theso_rec FROM preferences where id_thesaurus = idtheso;

		-- URI
		uri = opentheso_get_uri(theso_rec.original_uri_is_ark, con.id_ark, theso_rec.original_uri, theso_rec.original_uri_is_handle,
					 con.id_handle, theso_rec.original_uri_is_doi, con.id_doi, idconcept, idtheso, theso_rec.chemin_site);

		-- LocalUri
		local_URI = theso_rec.chemin_site || '?idc=' || idconcept || '&idt=' || idtheso;

		-- prefLab_selected
		preflab_selected = '';
		SELECT * INTO preflab_selected_rec FROM opentheso_get_conceptlabel(idtheso, idconcept, idlang);
		preflab_selected = preflab_selected_rec.libelle || sous_seperateur || preflab_selected_rec.unique_id;
		
		-- altLab_selected	
		altlab_selected = '';
		FOR altlab_selected_rec IN SELECT * FROM opentheso_get_altlabel(idtheso, idconcept, idlang, false)
		LOOP
			altlab_selected = altlab_selected || altLab_selected_rec.altlabel || sous_seperateur || altLab_selected_rec.unique_id || seperateur;
		END LOOP;

		-- altLab_hiden_selected
		altlab_hiden_selected = '';
		FOR altlab_selected_rec IN SELECT * FROM opentheso_get_altLabel(idtheso, idconcept, idlang, true)
		LOOP
			altlab_hiden_selected = altlab_hiden_selected || altlab_selected_rec.altlabel || sous_seperateur || altlab_selected_rec.unique_id || seperateur;
		END LOOP;

		-- PrefLab
		preflab = '';
		FOR traduction_rec IN SELECT * FROM opentheso_get_preflabel_traductions(idtheso, idconcept, idlang)
		LOOP
			preflab = preflab || traduction_rec.term_id || sous_seperateur || traduction_rec.term_lexical_value || sous_seperateur || traduction_rec.term_lang || sous_seperateur || traduction_rec.unique_id || seperateur;
		END LOOP;

		-- altLab
		altLab = '';
		FOR altlab_rec IN SELECT * FROM opentheso_get_altLabel_traductions(idtheso, idconcept, idlang, false)
		LOOP
			altlab = altlab || altlab_rec.altlabel_id || sous_seperateur || altlab_rec.altlabel || sous_seperateur || altlab_rec.idlang_alt || sous_seperateur || altlab_rec.unique_id || seperateur;
		END LOOP;

		-- altLab hiden
		altlab_hiden = '';
		FOR altlab_hiden_rec IN SELECT * FROM opentheso_get_altLabel_traductions(idtheso, idconcept, idlang, true)
		LOOP
			altlab_hiden = altlab_hiden || altlab_hiden_rec.altlabel_id || sous_seperateur || altlab_hiden_rec.altlabel || sous_seperateur || altlab_hiden_rec.idlang_alt || sous_seperateur || altlab_hiden_rec.unique_id || seperateur;
		END LOOP;

		-- Notes
		note = '';
		example = '';
		changeNote = '';
		secopeNote = '';
		definition = '';
		historyNote = '';
		editorialNote = '';
		FOR note_concept_rec IN SELECT * FROM opentheso_get_notes(idtheso, idconcept)
		LOOP
			IF (note_concept_rec.note_notetypecode = 'note') THEN
				note = note || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'example') THEN
				example = example || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			END IF;
		END LOOP;

		-- Relations
		narrower = '';
		broader = '';
		related = '';
		FOR relation_rec IN SELECT * FROM opentheso_get_relations(idtheso, idconcept)
		LOOP
			tmpLabel = '';
			select libelle INTO tmpLabel from opentheso_get_conceptlabel(idtheso, relation_rec.relationship_id_concept, idLang);
			tmp = opentheso_get_uri(theso_rec.original_uri_is_ark, relation_rec.relationship_id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, relation_rec.relationship_id_handle, theso_rec.original_uri_is_doi,
					relation_rec.relationship_id_doi, relation_rec.relationship_id_concept, idtheso, theso_rec.chemin_site)
					|| sous_seperateur || relation_rec.relationship_role || sous_seperateur || relation_rec.relationship_id_concept || sous_seperateur || tmpLabel ;
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
		FOR alignement_rec IN SELECT * FROM opentheso_get_alignements(idtheso, idconcept)
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
		FOR geo_rec IN SELECT * FROM opentheso_get_gps(idtheso, idconcept)
		LOOP
			gpsData = gpsData || geo_rec.gps_latitude || sous_seperateur || geo_rec.gps_longitude || sous_seperateur || geo_rec.pos || seperateur;
		END LOOP;

		-- membre
		membre = '';
		FOR group_rec IN SELECT * FROM opentheso_get_groups(idtheso, idconcept)
		LOOP
			tmpLabel = '';
			select libelle INTO tmpLabel from opentheso_get_grouplabel(idtheso, group_rec.group_id, idLang);
			
			IF (theso_rec.original_uri_is_ark = true AND group_rec.group_id_ark IS NOT NULL  AND group_rec.group_id_ark != '') THEN
				membre = membre || theso_rec.original_uri || '/' || group_rec.group_id_ark || sous_seperateur || group_rec.group_id || sous_seperateur || tmpLabel || seperateur;
			ELSIF (theso_rec.original_uri_is_ark = true AND (group_rec.group_id_ark IS NULL OR group_rec.group_id_ark = '')) THEN
				membre = membre || theso_rec.chemin_site || '?idg=' || group_rec.group_id || '&idt=' || idtheso || sous_seperateur || group_rec.group_id || sous_seperateur || tmpLabel || seperateur;
			ELSIF (group_rec.group_id_handle IS NOT NULL AND group_rec.group_id_handle != '') THEN
				membre = membre || 'https://hdl.handle.net/' || group_rec.group_id_handle || sous_seperateur || group_rec.group_id || sous_seperateur || tmpLabel || seperateur;
			ELSIF (theso_rec.original_uri IS NOT NULL AND theso_rec.original_uri != '') THEN
				membre = membre || theso_rec.original_uri || '/?idg=' || group_rec.group_id || '&idt=' || idtheso || sous_seperateur || group_rec.group_id || sous_seperateur || tmpLabel || seperateur;
			ELSE
				membre = membre || theso_rec.chemin_site || '?idc=' || group_rec.group_id || '&idt=' || idtheso || sous_seperateur || group_rec.group_id || sous_seperateur || tmpLabel || seperateur;
			END IF;
		END LOOP;

		-- Images
		img = '';
		FOR img_rec IN SELECT * FROM opentheso_get_images(idtheso, idconcept)
		LOOP
			img = img || img_rec.name || sous_seperateur || img_rec.copyright || sous_seperateur || img_rec.url || seperateur;
		END LOOP;

        -- Agent DcTerms
        creator = '';
        contributor = '';
		SELECT value INTO creator FROM concept_dcterms WHERE id_concept = con.id_concept and id_thesaurus = con.id_thesaurus and name = 'creator';

		FOR contributor_rec IN SELECT value
				FROM concept_dcterms
				WHERE id_concept = con.id_concept
				AND id_thesaurus = con.id_thesaurus
				AND name = 'contributor'
		LOOP
                    IF (contributor != '') THEN
                        contributor = contributor || seperateur;
                    END IF;
                    contributor = contributor || contributor_rec.value;
		END LOOP;

		replaces = '';
		FOR replace_rec IN SELECT id_concept1, id_ark, id_handle, id_doi
			FROM concept_replacedby, concept
			WHERE concept.id_concept = concept_replacedby.id_concept1
					 AND concept.id_thesaurus = concept_replacedby.id_thesaurus
					 AND concept_replacedby.id_concept2 = idconcept
					 AND concept_replacedby.id_thesaurus = idtheso
		LOOP
			tmpLabel = '';
			select libelle INTO tmpLabel from opentheso_get_conceptlabel(idtheso, replace_rec.id_concept1, idLang);	
			replaces = replaces || opentheso_get_uri(theso_rec.original_uri_is_ark, replace_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replace_rec.id_handle, theso_rec.original_uri_is_doi,
					replace_rec.id_doi, replace_rec.id_concept1, idtheso, theso_rec.chemin_site) || sous_seperateur || replace_rec.id_concept1 || sous_seperateur || tmpLabel || seperateur;
		END LOOP;

		replacedBy = '';
		FOR replacedBy_rec IN SELECT id_concept2, id_ark, id_handle, id_doi
			FROM concept_replacedby, concept
			WHERE concept.id_concept = concept_replacedby.id_concept2
			AND concept.id_thesaurus = concept_replacedby.id_thesaurus
			AND concept_replacedby.id_concept1 = idconcept
			AND concept_replacedby.id_thesaurus = idtheso
		LOOP
			tmpLabel = '';
			select libelle INTO tmpLabel from opentheso_get_conceptlabel(idtheso, replacedBy_rec.id_concept2, idLang);			
			replacedBy = replacedBy || opentheso_get_uri(theso_rec.original_uri_is_ark, replacedBy_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replacedBy_rec.id_handle, theso_rec.original_uri_is_doi,
					replacedBy_rec.id_doi, replacedBy_rec.id_concept2, idtheso, theso_rec.chemin_site) || sous_seperateur || replacedBy_rec.id_concept2 || sous_seperateur || tmpLabel || seperateur;
		END LOOP;

		facets = '';
		FOR facet_rec IN SELECT thesaurus_array.id_facet
			FROM thesaurus_array
			WHERE thesaurus_array.id_thesaurus = idtheso
			AND thesaurus_array.id_concept_parent = idconcept
		LOOP
			tmpLabel = '';
			select libelle INTO tmpLabel from opentheso_get_labelfacet(idtheso, facet_rec.id_facet, idLang);		
			facets = facets || facet_rec.id_facet || sous_seperateur || tmpLabel || seperateur;
		END LOOP;

		externalResources = '';
		FOR externalResource_rec IN SELECT external_resources.external_uri
			FROM external_resources
			WHERE external_resources.id_thesaurus = idtheso
			AND external_resources.id_concept = idconcept
		LOOP
			externalResources = externalResources || externalResource_rec.external_uri || seperateur;
		END LOOP;

		return query 
			SELECT 	uri, con.status, local_URI, idconcept, con.id_ark, prefLab_selected, altLab_selected, altLab_hiden_selected, prefLab, altLab, altLab_hiden, definition, example,
			  editorialNote, changeNote, secopeNote, note, historyNote, con.notation, narrower, broader, related, exactMatch, closeMatch,
			  broadMatch, relatedMatch, narrowMatch, gpsData, membre, con.created, con.modified,
			  img, creator, contributor, replaces, replacedBy, facets, externalResources;

END;
$$;


ALTER FUNCTION public.opentheso_get_concept(idtheso character varying, idconcept character varying, idlang character varying) OWNER TO opentheso;

--
-- TOC entry 389 (class 1255 OID 104993)
-- Name: opentheso_get_conceptlabel(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_conceptlabel(id_theso character varying, id_con character varying, id_lang character varying) RETURNS TABLE(idterm character varying, libelle character varying, unique_id integer)
    LANGUAGE plpgsql
    AS $$
begin
       return query
	   		select term.id_term, term.lexical_value, term.id from term, preferred_term where
                        preferred_term.id_term = term.id_term AND
                        preferred_term.id_thesaurus = term.id_thesaurus
                        and term.id_thesaurus = id_theso
                        and preferred_term.id_concept = id_con
                        and term.lang = id_lang;
end;
$$;


ALTER FUNCTION public.opentheso_get_conceptlabel(id_theso character varying, id_con character varying, id_lang character varying) OWNER TO opentheso;

--
-- TOC entry 390 (class 1255 OID 104994)
-- Name: opentheso_get_concepts(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_concepts(id_theso character varying, path character varying) RETURNS SETOF record
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
	contributor_rec record;

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
        FOR traduction_rec IN SELECT * FROM opentheso_get_all_preflabel(id_theso, con.id_concept)
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
        FOR note_concept_rec IN SELECT * FROM opentheso_get_notes(id_theso, con.id_concept)
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
		
        -- Agent DcTerms
        creator = '';
        contributor = '';
		SELECT value INTO creator FROM concept_dcterms WHERE id_concept = con.id_concept and id_thesaurus = con.id_thesaurus and name = 'creator';

		FOR contributor_rec IN SELECT value
				FROM concept_dcterms
				WHERE id_concept = con.id_concept
				AND id_thesaurus = con.id_thesaurus
				AND name = 'contributor'
		LOOP
                    IF (contributor != '') THEN
                        contributor = contributor || seperateur;
                    END IF;
                    contributor = contributor || contributor_rec.value;
		END LOOP;

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


ALTER FUNCTION public.opentheso_get_concepts(id_theso character varying, path character varying) OWNER TO opentheso;

--
-- TOC entry 391 (class 1255 OID 104996)
-- Name: opentheso_get_concepts_by_group(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_concepts_by_group(id_theso character varying, path character varying, id_group character varying) RETURNS SETOF record
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
	contributor_rec record;

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
        FOR traduction_rec IN SELECT * FROM opentheso_get_all_preflabel(id_theso, con.id_concept)
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
        FOR note_concept_rec IN SELECT * FROM opentheso_get_notes(id_theso, con.id_concept)
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
            ELSIF (group_rec.group_id_handle IS NOT NULL) THEN
                        membre = membre || 'https://hdl.handle.net/' || group_rec.group_id_handle || seperateur;
            ELSIF (theso_rec.original_uri IS NOT NULL) THEN
                        membre = membre || theso_rec.original_uri || '/?idg=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
            ELSE
                        membre = membre || path || '/?idg=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
            END IF;
        END LOOP;

		-- Images
		img = '';
        FOR img_rec IN SELECT * FROM opentheso_get_images(id_theso, con.id_concept)
        LOOP
            img = img || img_rec.name || sous_seperateur || img_rec.copyright || sous_seperateur || img_rec.url || seperateur;
        END LOOP;

		-- Agent DcTerms
		creator = '';
		contributor = '';
		SELECT value INTO creator FROM concept_dcterms WHERE id_concept = con.id_concept and id_thesaurus = con.id_thesaurus and name = 'creator';

		FOR contributor_rec IN SELECT value
				FROM concept_dcterms
				WHERE id_concept = con.id_concept
				AND id_thesaurus = con.id_thesaurus
				AND name = 'contributor'
		LOOP
                    IF (contributor != '') THEN
                        contributor = contributor || seperateur;
                    END IF;
                    contributor = contributor || contributor_rec.value;
		END LOOP;

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


ALTER FUNCTION public.opentheso_get_concepts_by_group(id_theso character varying, path character varying, id_group character varying) OWNER TO opentheso;

--
-- TOC entry 392 (class 1255 OID 104998)
-- Name: opentheso_get_definitions(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_definitions(idtheso character varying, idconcept character varying, idlang character varying) RETURNS TABLE(definition character varying, unique_id integer)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT note.lexicalvalue, note.id
		FROM note
		WHERE 
		note.notetypecode = 'definition'
		AND note.identifier = idconcept
		AND note.id_thesaurus = idtheso
		AND note.lang = idlang;
end;
$$;


ALTER FUNCTION public.opentheso_get_definitions(idtheso character varying, idconcept character varying, idlang character varying) OWNER TO opentheso;

--
-- TOC entry 393 (class 1255 OID 104999)
-- Name: opentheso_get_facets_of_concept(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_facets_of_concept(id_theso character varying, id_con character varying, id_lang character varying) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
DECLARE
	rec record;
	facet_rec record;

	libelle character varying;
	have_members boolean;

BEGIN
	FOR facet_rec IN 
		SELECT thesaurus_array.id_facet FROM thesaurus_array 
			WHERE thesaurus_array.id_thesaurus = id_theso
			AND thesaurus_array.id_concept_parent = id_con
    LOOP
		libelle = '';
		SELECT opentheso_get_labelfacet(id_theso, facet_rec.id_facet, id_lang) INTO libelle; 
		
		SELECT opentheso_isfacet_hasmembers(id_theso, facet_rec.id_facet) INTO have_members;
		
		SELECT facet_rec.id_facet, libelle, have_members INTO rec;

  		RETURN NEXT rec;
    END LOOP;
END;
$$;


ALTER FUNCTION public.opentheso_get_facets_of_concept(id_theso character varying, id_con character varying, id_lang character varying) OWNER TO opentheso;

--
-- TOC entry 394 (class 1255 OID 105000)
-- Name: opentheso_get_facettes(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_facettes(id_theso character varying, path character varying) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
DECLARE
    seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';
	rec record;
	facet_rec record;
	theso_rec record;
	concept_rec record;
	note_concept_rec record;	

	uri_membre VARCHAR;
	id_ark VARCHAR;
	id_handle VARCHAR;
	uri_value VARCHAR;
	
	definition text;
	secopeNote text;
	note text;
	historyNote text;
	example text;
	changeNote text;
	editorialNote text;	
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


		-- Notes
		note = '';
		example = '';
		changeNote = '';
		secopeNote = '';
		definition = '';
		historyNote = '';
		editorialNote = '';
        FOR note_concept_rec IN SELECT * FROM opentheso_get_notes(id_theso, facet_rec.id_facet)
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

		SELECT facet_rec.id_facet, facet_rec.lexical_value, facet_rec.created, facet_rec.modified, facet_rec.lang,
				facet_rec.id_concept_parent, uri_value, definition, example, editorialNote, changeNote, secopeNote, note, historyNote INTO rec;
  		RETURN NEXT rec;
    END LOOP;
END;
$$;


ALTER FUNCTION public.opentheso_get_facettes(id_theso character varying, path character varying) OWNER TO opentheso;

--
-- TOC entry 395 (class 1255 OID 105001)
-- Name: opentheso_get_gps(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_gps(id_thesorus character varying, id_con character varying) RETURNS TABLE(gps_latitude double precision, gps_longitude double precision, pos integer)
    LANGUAGE plpgsql
    AS $$
begin
    return query
        SELECT latitude, longitude, gps.position
        FROM gps
        WHERE id_theso = id_thesorus
          AND id_concept = id_con;

end;
$$;


ALTER FUNCTION public.opentheso_get_gps(id_thesorus character varying, id_con character varying) OWNER TO opentheso;

--
-- TOC entry 396 (class 1255 OID 105002)
-- Name: opentheso_get_grouplabel(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_grouplabel(id_theso character varying, id_group character varying, id_lang character varying) RETURNS TABLE(libelle text)
    LANGUAGE plpgsql
    AS $$
begin
       return query
	   		select lexicalvalue from concept_group_label where
                        idthesaurus = id_theso
                        and LOWER(idgroup) = LOWER(id_group)
                        and lang = id_lang;
end;
$$;


ALTER FUNCTION public.opentheso_get_grouplabel(id_theso character varying, id_group character varying, id_lang character varying) OWNER TO opentheso;

--
-- TOC entry 397 (class 1255 OID 105003)
-- Name: opentheso_get_groups(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_groups(id_theso character varying, id_con character varying) RETURNS TABLE(group_id text, group_id_ark text, group_id_handle character varying, group_id_doi character varying)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT concept_group.idgroup, concept_group.id_ark, concept_group.id_handle, concept_group.id_doi
		FROM concept_group_concept, concept_group
		WHERE concept_group.idgroup = concept_group_concept.idgroup
		AND concept_group.idthesaurus = concept_group_concept.idthesaurus
		AND concept_group_concept.idthesaurus = id_theso
		AND concept_group_concept.idconcept = id_con;

end;
$$;


ALTER FUNCTION public.opentheso_get_groups(id_theso character varying, id_con character varying) OWNER TO opentheso;

--
-- TOC entry 398 (class 1255 OID 105004)
-- Name: opentheso_get_idconcept_from_idterm(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_idconcept_from_idterm(id_theso character varying, id_term1 character varying) RETURNS TABLE(id_concept1 character varying)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT id_concept FROM preferred_term WHERE id_thesaurus = id_theso and id_term = id_term1;
end;
$$;


ALTER FUNCTION public.opentheso_get_idconcept_from_idterm(id_theso character varying, id_term1 character varying) OWNER TO opentheso;

--
-- TOC entry 399 (class 1255 OID 105005)
-- Name: opentheso_get_images(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_images(id_theso character varying, id_con character varying) RETURNS TABLE(name character varying, copyright character varying, url character varying)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT image_name, image_copyright, external_uri
		FROM external_images
		WHERE id_thesaurus = id_theso
		AND id_concept = id_con;

end;
$$;


ALTER FUNCTION public.opentheso_get_images(id_theso character varying, id_con character varying) OWNER TO opentheso;

--
-- TOC entry 359 (class 1255 OID 105006)
-- Name: opentheso_get_labelfacet(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_labelfacet(id_theso character varying, idfacet character varying, id_lang character varying) RETURNS TABLE(libelle character varying)
    LANGUAGE plpgsql
    AS $$

begin
       return query
	   		select lexical_value from node_label where
                        id_facet = idfacet
                        and id_thesaurus = id_theso
                        and lang = id_lang;

end;
$$;


ALTER FUNCTION public.opentheso_get_labelfacet(id_theso character varying, idfacet character varying, id_lang character varying) OWNER TO opentheso;

--
-- TOC entry 400 (class 1255 OID 105007)
-- Name: opentheso_get_list_narrower_forgraph(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_list_narrower_forgraph(idtheso character varying, idbt character varying, idlang character varying) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
DECLARE
    seperateur constant varchar := '##';
    sous_seperateur constant varchar := '@@';

    theso_rec record;
    rec record;
	con record;
	havechildren_rec record;
	preflab_rec record;
	altlab_rec record;	
	definition_rec record;
	
    havechildren boolean;
    prefLab VARCHAR;
	altlabel VARCHAR;
	definition text;
	local_uri text;

BEGIN
	SELECT * INTO theso_rec FROM preferences where id_thesaurus = idtheso;
	
    FOR con IN SELECT * FROM opentheso_get_narrowers_ignorefacet(idtheso, idbt)
    LOOP

		-- URI
		local_uri = theso_rec.chemin_site || '?idc=' || con.idconcept2 || '&idt=' || idtheso;
					 
		-- prefLab
		preflab = '';
		SELECT * INTO preflab_rec FROM opentheso_get_conceptlabel(idtheso, con.idconcept2, idlang);
		preflab = preflab_rec.libelle;
		
		-- altLab
		altlabel = '';
		FOR altlab_rec IN SELECT * FROM opentheso_get_altlabel(idtheso, con.idconcept2, idlang, false)
		LOOP
			altlabel = altlabel || altlab_rec.altlabel || seperateur;
		END LOOP;	
		
		-- definition
		definition = '';
		FOR definition_rec IN SELECT * FROM opentheso_get_definitions(idtheso, con.idconcept2, idlang)
		LOOP
			definition = definition || definition_rec.definition || seperateur;
		END LOOP;			
		
		-- childrens
		havechildren = false;
		SELECT opentheso_ishave_children(idtheso, con.idconcept2) INTO havechildren;
		
		-- return
        SELECT con.idconcept2, local_uri, con.status, prefLab, altlabel, definition, havechildren INTO rec;

        RETURN NEXT rec;
	END LOOP;
END;
$$;


ALTER FUNCTION public.opentheso_get_list_narrower_forgraph(idtheso character varying, idbt character varying, idlang character varying) OWNER TO opentheso;

--
-- TOC entry 401 (class 1255 OID 105008)
-- Name: opentheso_get_list_narrower_fortree(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_list_narrower_fortree(idtheso character varying, idbt character varying, idlang character varying) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
DECLARE
    seperateur constant varchar := '##';
    sous_seperateur constant varchar := '@@';

    rec record;
	con record;
	havechildren_rec record;
	preflab_rec record;
	
    havechildren boolean;
    prefLab VARCHAR;

BEGIN

    FOR con IN SELECT * FROM opentheso_get_narrowers(idtheso, idbt)
    LOOP
		-- prefLab
		preflab = '';
		SELECT * INTO preflab_rec FROM opentheso_get_conceptlabel(idtheso, con.idconcept2, idlang);
		preflab = preflab_rec.libelle;
		

		-- altLab hiden
		havechildren = false;
		SELECT opentheso_ishave_children(idtheso, con.idconcept2) INTO havechildren;
		
		
        SELECT con.idconcept2, con.notation, con.status, prefLab, havechildren INTO rec;

        RETURN NEXT rec;
	END LOOP;
END;
$$;


ALTER FUNCTION public.opentheso_get_list_narrower_fortree(idtheso character varying, idbt character varying, idlang character varying) OWNER TO opentheso;

--
-- TOC entry 402 (class 1255 OID 105009)
-- Name: opentheso_get_list_topterm_forgraph(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_list_topterm_forgraph(idtheso character varying, idlang character varying) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
DECLARE
    seperateur constant varchar := '##';
    sous_seperateur constant varchar := '@@';

    theso_rec record;
    rec record;
	con record;
	havechildren_rec record;
	preflab_rec record;
	altlab_rec record;	
	definition_rec record;
	
    havechildren boolean;
    prefLab VARCHAR;
	altlabel VARCHAR;
	definition text;
	local_uri text;

BEGIN
	SELECT * INTO theso_rec FROM preferences where id_thesaurus = idtheso;
	
    FOR con IN SELECT * FROM opentheso_get_topterms(idtheso)
    LOOP

		-- URI
		local_uri = theso_rec.chemin_site || '?idc=' || con.idconcept || '&idt=' || idtheso;
					 
		-- prefLab
		preflab = '';
		SELECT * INTO preflab_rec FROM opentheso_get_conceptlabel(idtheso, con.idconcept, idlang);
		preflab = preflab_rec.libelle;
		
		-- altLab
		altlabel = '';
		FOR altlab_rec IN SELECT * FROM opentheso_get_altlabel(idtheso, con.idconcept, idlang, false)
		LOOP
			altlabel = altlabel || altlab_rec.altlabel || seperateur;
		END LOOP;	
		
		-- definition
		definition = '';
		FOR definition_rec IN SELECT * FROM opentheso_get_definitions(idtheso, con.idconcept, idlang)
		LOOP
			definition = definition || definition_rec.definition || seperateur;
		END LOOP;			
		
		-- childrens
		havechildren = false;
		SELECT opentheso_ishave_children(idtheso, con.idconcept) INTO havechildren;
		
		-- return
        SELECT con.idconcept, local_uri, con.status, prefLab, altlabel, definition, havechildren INTO rec;

        RETURN NEXT rec;
	END LOOP;
END;
$$;


ALTER FUNCTION public.opentheso_get_list_topterm_forgraph(idtheso character varying, idlang character varying) OWNER TO opentheso;

--
-- TOC entry 403 (class 1255 OID 105010)
-- Name: opentheso_get_narrowers(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_narrowers(id_theso character varying, id_bt character varying) RETURNS TABLE(notation character varying, status character varying, idconcept2 character varying)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		select concept.notation, concept.status, hierarchical_relationship.id_concept2
		from hierarchical_relationship, concept 
		where concept.id_thesaurus = hierarchical_relationship.id_thesaurus 
		and concept.id_concept = hierarchical_relationship.id_concept2 
		and hierarchical_relationship.id_thesaurus = id_theso 
		and id_concept1 = id_bt 
		and role LIKE 'NT%' 
		and concept.status != 'CA' 
		and id_concept2 
		not in (
			select id_concept from concept_facet, thesaurus_array 
			where concept_facet.id_facet = thesaurus_array.id_facet 
			and concept_facet.id_thesaurus = thesaurus_array.id_thesaurus 
			and thesaurus_array.id_concept_parent = id_bt 
			and thesaurus_array.id_thesaurus = id_theso)
		ORDER BY concept.notation ASC;
end;
$$;


ALTER FUNCTION public.opentheso_get_narrowers(id_theso character varying, id_bt character varying) OWNER TO opentheso;

--
-- TOC entry 404 (class 1255 OID 105011)
-- Name: opentheso_get_narrowers_ignorefacet(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_narrowers_ignorefacet(id_theso character varying, id_bt character varying) RETURNS TABLE(notation character varying, status character varying, idconcept2 character varying)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		select concept.notation, concept.status, hierarchical_relationship.id_concept2
		from hierarchical_relationship, concept 
		where concept.id_thesaurus = hierarchical_relationship.id_thesaurus 
		and concept.id_concept = hierarchical_relationship.id_concept2 
		and hierarchical_relationship.id_thesaurus = id_theso 
		and id_concept1 = id_bt 
		and role LIKE 'NT%' 
		and concept.status != 'CA' 
		ORDER BY concept.notation ASC;
end;
$$;


ALTER FUNCTION public.opentheso_get_narrowers_ignorefacet(id_theso character varying, id_bt character varying) OWNER TO opentheso;

--
-- TOC entry 405 (class 1255 OID 105012)
-- Name: opentheso_get_note_concept(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_note_concept(id_theso character varying, id_con character varying) RETURNS TABLE(note_id integer, note_notetypecode text, note_lexicalvalue character varying, note_created timestamp without time zone, note_modified timestamp without time zone, note_lang character varying)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT note.id, note.notetypecode, note.lexicalvalue, note.created, note.modified, note.lang
		FROM note, note_type
		WHERE note.notetypecode = note_type.code
		AND note_type.isconcept = true
		AND note.id_thesaurus = id_theso
		AND note.id_concept = id_con;

end;
$$;


ALTER FUNCTION public.opentheso_get_note_concept(id_theso character varying, id_con character varying) OWNER TO opentheso;

--
-- TOC entry 406 (class 1255 OID 105013)
-- Name: opentheso_get_note_term(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_note_term(id_theso character varying, id_con character varying) RETURNS TABLE(note_id integer, note_notetypecode text, note_lexicalvalue character varying, note_created timestamp without time zone, note_modified timestamp without time zone, note_lang character varying)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.opentheso_get_note_term(id_theso character varying, id_con character varying) OWNER TO opentheso;

--
-- TOC entry 360 (class 1255 OID 105014)
-- Name: opentheso_get_notes(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_notes(id_theso character varying, id_con character varying) RETURNS TABLE(note_id integer, note_notetypecode text, note_lexicalvalue character varying, note_created timestamp without time zone, note_modified timestamp without time zone, note_lang character varying)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT note.id, note.notetypecode, note.lexicalvalue, note.created, note.modified, note.lang
		FROM note
		WHERE 
		note.id_thesaurus = id_theso
		AND note.identifier = id_con;
end;
$$;


ALTER FUNCTION public.opentheso_get_notes(id_theso character varying, id_con character varying) OWNER TO opentheso;

--
-- TOC entry 361 (class 1255 OID 105015)
-- Name: opentheso_get_preflabel_traductions(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_preflabel_traductions(id_theso character varying, id_con character varying, id_lang character varying) RETURNS TABLE(term_id character varying, term_lexical_value character varying, term_lang character varying, unique_id integer)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT term.id_term, term.lexical_value, term.lang, term.id
		FROM term, preferred_term
		WHERE term.id_term = preferred_term.id_term
		AND term.id_thesaurus = preferred_term.id_thesaurus
		AND term.id_thesaurus = id_theso
		AND preferred_term.id_concept = id_con
		AND term.lang != id_lang
		ORDER BY term.lexical_value;

end;
$$;


ALTER FUNCTION public.opentheso_get_preflabel_traductions(id_theso character varying, id_con character varying, id_lang character varying) OWNER TO opentheso;

--
-- TOC entry 407 (class 1255 OID 105016)
-- Name: opentheso_get_relations(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_relations(id_theso character varying, id_con character varying) RETURNS TABLE(relationship_id_concept character varying, relationship_role character varying, relationship_id_ark character varying, relationship_id_handle character varying, relationship_id_doi character varying)
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.opentheso_get_relations(id_theso character varying, id_con character varying) OWNER TO opentheso;

--
-- TOC entry 408 (class 1255 OID 105017)
-- Name: opentheso_get_topterms(character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_topterms(id_theso character varying) RETURNS TABLE(notation character varying, status character varying, idconcept character varying)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		select concept.notation, concept.status, concept.id_concept
		from concept 
		where 
		concept.id_thesaurus = id_theso
		and concept.top_concept = true
		and concept.status != 'CA'
		ORDER BY concept.id_concept ASC;
end;
$$;


ALTER FUNCTION public.opentheso_get_topterms(id_theso character varying) OWNER TO opentheso;

--
-- TOC entry 409 (class 1255 OID 105018)
-- Name: opentheso_get_uri(boolean, character varying, character varying, boolean, character varying, boolean, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_get_uri(original_uri_is_ark boolean, id_ark character varying, original_uri character varying, original_uri_is_handle boolean, id_handle character varying, original_uri_is_doi boolean, id_doi character varying, id_concept character varying, id_theso character varying, path character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.opentheso_get_uri(original_uri_is_ark boolean, id_ark character varying, original_uri character varying, original_uri_is_handle boolean, id_handle character varying, original_uri_is_doi boolean, id_doi character varying, id_concept character varying, id_theso character varying, path character varying) OWNER TO opentheso;

--
-- TOC entry 410 (class 1255 OID 105019)
-- Name: opentheso_isconcept_havefacet(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_isconcept_havefacet(id_theso character varying, id_co character varying) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    rec record;
    havefacet boolean;
BEGIN
	select count(id_facet) INTO rec from thesaurus_array
	where
	id_thesaurus = id_theso
	and id_concept_parent = id_co;

	IF rec.count = 0 THEN 
		havefacet := false;
	ELSE
		havefacet := true;
	END IF;
	RETURN havefacet;
END;
$$;


ALTER FUNCTION public.opentheso_isconcept_havefacet(id_theso character varying, id_co character varying) OWNER TO opentheso;

--
-- TOC entry 411 (class 1255 OID 105020)
-- Name: opentheso_isfacet_hasmembers(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_isfacet_hasmembers(idtheso character varying, idfacet character varying) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    rec record;
    have_members boolean;
BEGIN
    -- Vérifier la première condition
    SELECT count(id_concept) INTO rec
		from concept_facet 
		where concept_facet.id_thesaurus = idtheso
		and concept_facet.id_facet = idfacet;
	IF rec.count = 0 THEN 
		have_members := false;
    ELSE
        have_members := true;
    END IF;
    RETURN have_members;
END;
$$;


ALTER FUNCTION public.opentheso_isfacet_hasmembers(idtheso character varying, idfacet character varying) OWNER TO opentheso;

--
-- TOC entry 412 (class 1255 OID 105021)
-- Name: opentheso_ishave_children(character varying, character varying); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.opentheso_ishave_children(id_theso character varying, id_co character varying) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE
    rec1 record;
    rec2 record;
    havechildren boolean;
BEGIN
    -- Vérifier la première condition
    SELECT count(id_concept2) INTO rec1
    FROM hierarchical_relationship, concept 
    WHERE
        hierarchical_relationship.id_concept2 = concept.id_concept
        AND hierarchical_relationship.id_thesaurus = concept.id_thesaurus
        AND hierarchical_relationship.id_thesaurus = id_theso
        AND id_concept1 = id_co
        AND role LIKE 'NT%' 
        AND concept.status != 'CA';

    IF rec1.count = 0 THEN 
        -- Vérifier la deuxième condition
        SELECT count(id_facet) INTO rec2
        FROM thesaurus_array
        WHERE
            id_concept_parent = id_co
            AND id_thesaurus = id_theso;

		IF rec2.count = 0 THEN 
            havechildren := false;
        ELSE
            havechildren := true;
        END IF;
        RETURN havechildren;
    ELSE
        havechildren := true;
        RETURN havechildren;
    END IF;
END;
$$;


ALTER FUNCTION public.opentheso_ishave_children(id_theso character varying, id_co character varying) OWNER TO opentheso;

--
-- TOC entry 413 (class 1255 OID 105022)
-- Name: unaccent_string(text); Type: FUNCTION; Schema: public; Owner: opentheso
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


ALTER FUNCTION public.unaccent_string(text) OWNER TO opentheso;

--
-- TOC entry 414 (class 1255 OID 105023)
-- Name: update_table_preferences_displayusername(); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.update_table_preferences_displayusername() RETURNS void
    LANGUAGE plpgsql
    AS $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='display_user_name') THEN
        execute 'ALTER TABLE preferences ADD COLUMN display_user_name boolean DEFAULT false;';
END IF;
end
$$;


ALTER FUNCTION public.update_table_preferences_displayusername() OWNER TO opentheso;

--
-- TOC entry 415 (class 1255 OID 105024)
-- Name: update_table_preferences_useconcepttree(); Type: FUNCTION; Schema: public; Owner: opentheso
--

CREATE FUNCTION public.update_table_preferences_useconcepttree() RETURNS void
    LANGUAGE plpgsql
    AS $$
begin
    IF NOT EXISTS(SELECT *  FROM information_schema.columns where table_name='preferences' AND column_name='useconcepttree') THEN
        execute 'ALTER TABLE preferences ADD COLUMN useconcepttree boolean DEFAULT false;';
    END IF;
end
$$;


ALTER FUNCTION public.update_table_preferences_useconcepttree() OWNER TO opentheso;

--
-- TOC entry 211 (class 1259 OID 105025)
-- Name: alignement_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.alignement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alignement_id_seq OWNER TO opentheso;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 212 (class 1259 OID 105026)
-- Name: alignement; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.alignement OWNER TO opentheso;

--
-- TOC entry 213 (class 1259 OID 105034)
-- Name: alignement_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.alignement_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alignement_preferences_id_seq OWNER TO opentheso;

--
-- TOC entry 214 (class 1259 OID 105035)
-- Name: alignement_preferences; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.alignement_preferences (
    id integer DEFAULT nextval('public.alignement_preferences_id_seq'::regclass) NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_user integer NOT NULL,
    id_concept_depart character varying NOT NULL,
    id_concept_tratees character varying,
    id_alignement_source integer NOT NULL
);


ALTER TABLE public.alignement_preferences OWNER TO opentheso;

--
-- TOC entry 215 (class 1259 OID 105041)
-- Name: alignement_source__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.alignement_source__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alignement_source__id_seq OWNER TO opentheso;

--
-- TOC entry 216 (class 1259 OID 105042)
-- Name: alignement_source; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.alignement_source OWNER TO opentheso;

--
-- TOC entry 217 (class 1259 OID 105050)
-- Name: alignement_type; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.alignement_type (
    id integer NOT NULL,
    label text NOT NULL,
    isocode text NOT NULL,
    label_skos character varying
);


ALTER TABLE public.alignement_type OWNER TO opentheso;

--
-- TOC entry 218 (class 1259 OID 105055)
-- Name: bt_type; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.bt_type (
    id integer NOT NULL,
    relation character varying,
    description_fr character varying,
    description_en character varying
);


ALTER TABLE public.bt_type OWNER TO opentheso;

--
-- TOC entry 219 (class 1259 OID 105060)
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.candidat_messages_id_message_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidat_messages_id_message_seq OWNER TO opentheso;

--
-- TOC entry 220 (class 1259 OID 105061)
-- Name: candidat_messages; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.candidat_messages (
    id_message integer DEFAULT nextval('public.candidat_messages_id_message_seq'::regclass) NOT NULL,
    value text NOT NULL,
    id_user integer,
    id_concept integer,
    id_thesaurus character varying,
    date text
);


ALTER TABLE public.candidat_messages OWNER TO opentheso;

--
-- TOC entry 221 (class 1259 OID 105067)
-- Name: candidat_status; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.candidat_status OWNER TO opentheso;

--
-- TOC entry 222 (class 1259 OID 105073)
-- Name: candidat_vote; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.candidat_vote (
    id_vote integer NOT NULL,
    id_user integer,
    id_concept character varying,
    id_thesaurus character varying,
    type_vote character varying(30),
    id_note character varying(30)
);


ALTER TABLE public.candidat_vote OWNER TO opentheso;

--
-- TOC entry 223 (class 1259 OID 105078)
-- Name: candidat_vote_id_vote_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.candidat_vote_id_vote_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.candidat_vote_id_vote_seq OWNER TO opentheso;

--
-- TOC entry 4460 (class 0 OID 0)
-- Dependencies: 223
-- Name: candidat_vote_id_vote_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: opentheso
--

ALTER SEQUENCE public.candidat_vote_id_vote_seq OWNED BY public.candidat_vote.id_vote;


--
-- TOC entry 224 (class 1259 OID 105079)
-- Name: compound_equivalence; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.compound_equivalence (
    id_split_nonpreferredterm text NOT NULL,
    id_preferredterm text NOT NULL
);


ALTER TABLE public.compound_equivalence OWNER TO opentheso;

--
-- TOC entry 225 (class 1259 OID 105084)
-- Name: concept__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.concept__id_seq
    START WITH 43
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.concept__id_seq OWNER TO opentheso;

--
-- TOC entry 226 (class 1259 OID 105085)
-- Name: concept; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.concept (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    id_ark character varying DEFAULT ''::character varying,
    created timestamp with time zone,
    modified timestamp with time zone,
    status character varying,
    notation character varying DEFAULT ''::character varying,
    top_concept boolean,
    id integer DEFAULT nextval('public.concept__id_seq'::regclass),
    gps boolean DEFAULT false,
    id_handle character varying DEFAULT ''::character varying,
    id_doi character varying DEFAULT ''::character varying,
    creator integer DEFAULT '-1'::integer,
    contributor integer DEFAULT '-1'::integer,
    concept_type text DEFAULT 'concept'::text
);


ALTER TABLE public.concept OWNER TO opentheso;

--
-- TOC entry 227 (class 1259 OID 105099)
-- Name: concept_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.concept_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.concept_candidat__id_seq OWNER TO opentheso;

--
-- TOC entry 228 (class 1259 OID 105100)
-- Name: concept_candidat; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.concept_candidat OWNER TO opentheso;

--
-- TOC entry 229 (class 1259 OID 105109)
-- Name: concept_dcterms; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.concept_dcterms (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    name character varying NOT NULL,
    value character varying NOT NULL,
    language character varying,
    data_type character varying
);


ALTER TABLE public.concept_dcterms OWNER TO opentheso;

--
-- TOC entry 230 (class 1259 OID 105114)
-- Name: concept_facet; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.concept_facet (
    id_facet character varying NOT NULL,
    id_thesaurus text NOT NULL,
    id_concept text NOT NULL
);


ALTER TABLE public.concept_facet OWNER TO opentheso;

--
-- TOC entry 231 (class 1259 OID 105119)
-- Name: concept_group__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.concept_group__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.concept_group__id_seq OWNER TO opentheso;

--
-- TOC entry 232 (class 1259 OID 105120)
-- Name: concept_group; Type: TABLE; Schema: public; Owner: opentheso
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
    id_doi character varying DEFAULT ''::character varying,
    created timestamp without time zone,
    modified timestamp without time zone
);


ALTER TABLE public.concept_group OWNER TO opentheso;

--
-- TOC entry 233 (class 1259 OID 105129)
-- Name: concept_group_concept; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.concept_group_concept (
    idgroup text NOT NULL,
    idthesaurus text NOT NULL,
    idconcept text NOT NULL
);


ALTER TABLE public.concept_group_concept OWNER TO opentheso;

--
-- TOC entry 234 (class 1259 OID 105134)
-- Name: concept_group_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.concept_group_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.concept_group_historique__id_seq OWNER TO opentheso;

--
-- TOC entry 235 (class 1259 OID 105135)
-- Name: concept_group_historique; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.concept_group_historique OWNER TO opentheso;

--
-- TOC entry 236 (class 1259 OID 105142)
-- Name: concept_group_label_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.concept_group_label_id_seq
    START WITH 60
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.concept_group_label_id_seq OWNER TO opentheso;

--
-- TOC entry 237 (class 1259 OID 105143)
-- Name: concept_group_label; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.concept_group_label (
    id integer DEFAULT nextval('public.concept_group_label_id_seq'::regclass) NOT NULL,
    lexicalvalue text NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    lang character varying NOT NULL,
    idthesaurus text NOT NULL,
    idgroup text NOT NULL
);


ALTER TABLE public.concept_group_label OWNER TO opentheso;

--
-- TOC entry 238 (class 1259 OID 105151)
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.concept_group_label_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.concept_group_label_historique__id_seq OWNER TO opentheso;

--
-- TOC entry 239 (class 1259 OID 105152)
-- Name: concept_group_label_historique; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.concept_group_label_historique OWNER TO opentheso;

--
-- TOC entry 240 (class 1259 OID 105159)
-- Name: concept_group_type; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.concept_group_type (
    code text NOT NULL,
    label text NOT NULL,
    skoslabel text
);


ALTER TABLE public.concept_group_type OWNER TO opentheso;

--
-- TOC entry 241 (class 1259 OID 105164)
-- Name: concept_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.concept_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.concept_historique__id_seq OWNER TO opentheso;

--
-- TOC entry 242 (class 1259 OID 105165)
-- Name: concept_historique; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.concept_historique OWNER TO opentheso;

--
-- TOC entry 243 (class 1259 OID 105173)
-- Name: concept_replacedby; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.concept_replacedby (
    id_concept1 character varying NOT NULL,
    id_concept2 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    modified timestamp with time zone DEFAULT now() NOT NULL,
    id_user integer NOT NULL
);


ALTER TABLE public.concept_replacedby OWNER TO opentheso;

--
-- TOC entry 244 (class 1259 OID 105179)
-- Name: concept_term_candidat; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.concept_term_candidat (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


ALTER TABLE public.concept_term_candidat OWNER TO opentheso;

--
-- TOC entry 245 (class 1259 OID 105184)
-- Name: concept_type; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.concept_type (
    code text NOT NULL,
    label_fr text NOT NULL,
    label_en text,
    reciprocal boolean DEFAULT false,
    id_theso character varying DEFAULT 'all'::character varying NOT NULL
);


ALTER TABLE public.concept_type OWNER TO opentheso;

--
-- TOC entry 246 (class 1259 OID 105191)
-- Name: copyright; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.copyright (
    id_thesaurus character varying NOT NULL,
    copyright character varying
);


ALTER TABLE public.copyright OWNER TO opentheso;

--
-- TOC entry 247 (class 1259 OID 105196)
-- Name: corpus_link; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.corpus_link (
    id_theso character varying NOT NULL,
    corpus_name character varying NOT NULL,
    uri_count character varying,
    uri_link character varying NOT NULL,
    active boolean DEFAULT false,
    only_uri_link boolean DEFAULT false,
    sort integer
);


ALTER TABLE public.corpus_link OWNER TO opentheso;

--
-- TOC entry 248 (class 1259 OID 105203)
-- Name: custom_concept_attribute; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.custom_concept_attribute (
    "idConcept" character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


ALTER TABLE public.custom_concept_attribute OWNER TO opentheso;

--
-- TOC entry 249 (class 1259 OID 105208)
-- Name: custom_term_attribute; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.custom_term_attribute (
    identifier character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


ALTER TABLE public.custom_term_attribute OWNER TO opentheso;

--
-- TOC entry 250 (class 1259 OID 105213)
-- Name: databasechangelog; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.databasechangelog (
    id character varying(255) NOT NULL,
    author character varying(255) NOT NULL,
    filename character varying(255) NOT NULL,
    dateexecuted timestamp without time zone NOT NULL,
    orderexecuted integer NOT NULL,
    exectype character varying(10) NOT NULL,
    md5sum character varying(35),
    description character varying(255),
    comments character varying(255),
    tag character varying(255),
    liquibase character varying(20),
    contexts character varying(255),
    labels character varying(255),
    deployment_id character varying(10)
);


ALTER TABLE public.databasechangelog OWNER TO opentheso;

--
-- TOC entry 251 (class 1259 OID 105218)
-- Name: databasechangeloglock; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.databasechangeloglock (
    id integer NOT NULL,
    locked boolean NOT NULL,
    lockgranted timestamp without time zone,
    lockedby character varying(255)
);


ALTER TABLE public.databasechangeloglock OWNER TO opentheso;

--
-- TOC entry 252 (class 1259 OID 105221)
-- Name: external_images; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.external_images (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    image_name character varying NOT NULL,
    image_copyright character varying NOT NULL,
    id_user integer,
    external_uri character varying DEFAULT ''::character varying NOT NULL,
    id integer NOT NULL
);


ALTER TABLE public.external_images OWNER TO opentheso;

--
-- TOC entry 253 (class 1259 OID 105227)
-- Name: external_images_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

ALTER TABLE public.external_images ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.external_images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 254 (class 1259 OID 105228)
-- Name: external_resources; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.external_resources (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    description character varying,
    id_user integer,
    external_uri character varying DEFAULT ''::character varying NOT NULL
);


ALTER TABLE public.external_resources OWNER TO opentheso;

--
-- TOC entry 255 (class 1259 OID 105234)
-- Name: facet_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.facet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.facet_id_seq OWNER TO opentheso;

--
-- TOC entry 256 (class 1259 OID 105235)
-- Name: gps; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.gps (
    id_concept character varying NOT NULL,
    id_theso character varying NOT NULL,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    id integer NOT NULL,
    "position" integer
);


ALTER TABLE public.gps OWNER TO opentheso;

--
-- TOC entry 257 (class 1259 OID 105240)
-- Name: gps_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

ALTER TABLE public.gps ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.gps_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 258 (class 1259 OID 105241)
-- Name: gps_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.gps_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.gps_preferences_id_seq OWNER TO opentheso;

--
-- TOC entry 259 (class 1259 OID 105242)
-- Name: hierarchical_relationship; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.hierarchical_relationship (
    id_concept1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    role character varying NOT NULL,
    id_concept2 character varying NOT NULL
);


ALTER TABLE public.hierarchical_relationship OWNER TO opentheso;

--
-- TOC entry 260 (class 1259 OID 105247)
-- Name: hierarchical_relationship_historique; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.hierarchical_relationship_historique OWNER TO opentheso;

--
-- TOC entry 261 (class 1259 OID 105253)
-- Name: homepage; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.homepage (
    htmlcode character varying,
    lang character varying
);


ALTER TABLE public.homepage OWNER TO opentheso;

--
-- TOC entry 262 (class 1259 OID 105258)
-- Name: info; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.info (
    version_opentheso character varying NOT NULL,
    version_bdd character varying NOT NULL,
    googleanalytics character varying
);


ALTER TABLE public.info OWNER TO opentheso;

--
-- TOC entry 263 (class 1259 OID 105263)
-- Name: languages_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.languages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.languages_id_seq OWNER TO opentheso;

--
-- TOC entry 264 (class 1259 OID 105264)
-- Name: languages_iso639; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.languages_iso639 (
    iso639_1 character varying,
    iso639_2 character varying,
    english_name character varying,
    french_name character varying,
    id integer DEFAULT nextval('public.languages_id_seq'::regclass) NOT NULL,
    code_pays character varying
);


ALTER TABLE public.languages_iso639 OWNER TO opentheso;

--
-- TOC entry 265 (class 1259 OID 105270)
-- Name: thesaurus_array_facet_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.thesaurus_array_facet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.thesaurus_array_facet_id_seq OWNER TO opentheso;

--
-- TOC entry 266 (class 1259 OID 105271)
-- Name: node_label; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.node_label OWNER TO opentheso;

--
-- TOC entry 267 (class 1259 OID 105279)
-- Name: non_preferred_term; Type: TABLE; Schema: public; Owner: opentheso
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
    hiden boolean DEFAULT false NOT NULL,
    id integer NOT NULL
);


ALTER TABLE public.non_preferred_term OWNER TO opentheso;

--
-- TOC entry 268 (class 1259 OID 105287)
-- Name: non_preferred_term_historique; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.non_preferred_term_historique OWNER TO opentheso;

--
-- TOC entry 269 (class 1259 OID 105294)
-- Name: non_preferred_term_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

ALTER TABLE public.non_preferred_term ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.non_preferred_term_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 270 (class 1259 OID 105295)
-- Name: note__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.note__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.note__id_seq OWNER TO opentheso;

--
-- TOC entry 271 (class 1259 OID 105296)
-- Name: note; Type: TABLE; Schema: public; Owner: opentheso
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
    id_user integer,
    notesource character varying,
    identifier character varying
);


ALTER TABLE public.note OWNER TO opentheso;

--
-- TOC entry 272 (class 1259 OID 105304)
-- Name: note_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.note_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.note_historique__id_seq OWNER TO opentheso;

--
-- TOC entry 273 (class 1259 OID 105305)
-- Name: note_historique; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.note_historique OWNER TO opentheso;

--
-- TOC entry 274 (class 1259 OID 105312)
-- Name: note_type; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.note_type (
    code text NOT NULL,
    isterm boolean NOT NULL,
    isconcept boolean NOT NULL,
    label_fr character varying,
    label_en character varying,
    CONSTRAINT chk_not_false_values CHECK ((NOT ((isterm = false) AND (isconcept = false))))
);


ALTER TABLE public.note_type OWNER TO opentheso;

--
-- TOC entry 275 (class 1259 OID 105318)
-- Name: nt_type; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.nt_type (
    id integer NOT NULL,
    relation character varying,
    description_fr character varying,
    description_en character varying
);


ALTER TABLE public.nt_type OWNER TO opentheso;

--
-- TOC entry 276 (class 1259 OID 105323)
-- Name: permuted; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.permuted OWNER TO opentheso;

--
-- TOC entry 277 (class 1259 OID 105328)
-- Name: pref__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.pref__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.pref__id_seq OWNER TO opentheso;

--
-- TOC entry 278 (class 1259 OID 105329)
-- Name: preferences; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.preferences (
    id_pref integer DEFAULT nextval('public.pref__id_seq'::regclass) NOT NULL,
    id_thesaurus character varying NOT NULL,
    source_lang character varying DEFAULT 'fr'::character varying,
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
    generate_handle boolean DEFAULT false,
    auto_expand_tree boolean DEFAULT true,
    sort_by_notation boolean DEFAULT false,
    tree_cache boolean DEFAULT false,
    original_uri_is_doi boolean DEFAULT false,
    use_ark_local boolean DEFAULT false,
    naan_ark_local character varying DEFAULT ''::character varying,
    prefix_ark_local character varying DEFAULT ''::character varying,
    sizeid_ark_local integer DEFAULT 10,
    breadcrumb boolean DEFAULT true,
    useconcepttree boolean DEFAULT false,
    display_user_name boolean DEFAULT false,
    suggestion boolean DEFAULT false,
    use_custom_relation boolean DEFAULT false,
    uppercase_for_ark boolean DEFAULT false
);


ALTER TABLE public.preferences OWNER TO opentheso;

--
-- TOC entry 279 (class 1259 OID 105379)
-- Name: preferences_sparql; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.preferences_sparql OWNER TO opentheso;

--
-- TOC entry 280 (class 1259 OID 105385)
-- Name: preferred_term; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.preferred_term (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


ALTER TABLE public.preferred_term OWNER TO opentheso;

--
-- TOC entry 281 (class 1259 OID 105390)
-- Name: project_description; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.project_description (
    id integer NOT NULL,
    id_group character varying(256),
    lang character varying(256),
    description text
);


ALTER TABLE public.project_description OWNER TO opentheso;

--
-- TOC entry 282 (class 1259 OID 105395)
-- Name: project_description_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

ALTER TABLE public.project_description ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.project_description_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 283 (class 1259 OID 105396)
-- Name: proposition; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.proposition OWNER TO opentheso;

--
-- TOC entry 284 (class 1259 OID 105403)
-- Name: proposition_modification; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.proposition_modification (
    id integer NOT NULL,
    id_concept character varying NOT NULL,
    id_theso character varying NOT NULL,
    status character varying NOT NULL,
    nom character varying NOT NULL,
    email character varying NOT NULL,
    commentaire character varying,
    approuve_par character varying,
    approuve_date timestamp with time zone,
    lang character varying,
    date character varying,
    admin_comment character varying
);


ALTER TABLE public.proposition_modification OWNER TO opentheso;

--
-- TOC entry 285 (class 1259 OID 105408)
-- Name: proposition_modification_detail; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.proposition_modification_detail (
    id integer NOT NULL,
    id_proposition integer NOT NULL,
    categorie character varying NOT NULL,
    value character varying NOT NULL,
    action character varying,
    lang character varying,
    old_value character varying,
    hiden boolean,
    status character varying,
    id_term character varying
);


ALTER TABLE public.proposition_modification_detail OWNER TO opentheso;

--
-- TOC entry 286 (class 1259 OID 105413)
-- Name: proposition_modification_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

ALTER TABLE public.proposition_modification_detail ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.proposition_modification_detail_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 287 (class 1259 OID 105414)
-- Name: proposition_modification_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

ALTER TABLE public.proposition_modification ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.proposition_modification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 288 (class 1259 OID 105415)
-- Name: relation_group; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.relation_group (
    id_group1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    relation character varying NOT NULL,
    id_group2 character varying NOT NULL
);


ALTER TABLE public.relation_group OWNER TO opentheso;

--
-- TOC entry 289 (class 1259 OID 105420)
-- Name: releases; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.releases (
    id integer NOT NULL,
    version character varying(256),
    url text,
    date date,
    description text
);


ALTER TABLE public.releases OWNER TO opentheso;

--
-- TOC entry 290 (class 1259 OID 105425)
-- Name: releases_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

ALTER TABLE public.releases ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.releases_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 291 (class 1259 OID 105426)
-- Name: roles; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying,
    description character varying
);


ALTER TABLE public.roles OWNER TO opentheso;

--
-- TOC entry 292 (class 1259 OID 105431)
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.role_id_seq OWNER TO opentheso;

--
-- TOC entry 4461 (class 0 OID 0)
-- Dependencies: 292
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: opentheso
--

ALTER SEQUENCE public.role_id_seq OWNED BY public.roles.id;


--
-- TOC entry 293 (class 1259 OID 105432)
-- Name: routine_mail; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.routine_mail (
    id_thesaurus character varying NOT NULL,
    alert_cdt boolean DEFAULT true,
    debut_env_cdt_propos date NOT NULL,
    debut_env_cdt_valid date NOT NULL,
    period_env_cdt_propos integer NOT NULL,
    period_env_cdt_valid integer NOT NULL
);


ALTER TABLE public.routine_mail OWNER TO opentheso;

--
-- TOC entry 294 (class 1259 OID 105438)
-- Name: split_non_preferred_term; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.split_non_preferred_term (
);


ALTER TABLE public.split_non_preferred_term OWNER TO opentheso;

--
-- TOC entry 295 (class 1259 OID 105441)
-- Name: status; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.status (
    id_status integer NOT NULL,
    value text
);


ALTER TABLE public.status OWNER TO opentheso;

--
-- TOC entry 296 (class 1259 OID 105446)
-- Name: status_id_status_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.status_id_status_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.status_id_status_seq OWNER TO opentheso;

--
-- TOC entry 297 (class 1259 OID 105447)
-- Name: status_id_status_seq1; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.status_id_status_seq1
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.status_id_status_seq1 OWNER TO opentheso;

--
-- TOC entry 4462 (class 0 OID 0)
-- Dependencies: 297
-- Name: status_id_status_seq1; Type: SEQUENCE OWNED BY; Schema: public; Owner: opentheso
--

ALTER SEQUENCE public.status_id_status_seq1 OWNED BY public.status.id_status;


--
-- TOC entry 298 (class 1259 OID 105448)
-- Name: term__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.term__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.term__id_seq OWNER TO opentheso;

--
-- TOC entry 299 (class 1259 OID 105449)
-- Name: term; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.term OWNER TO opentheso;

--
-- TOC entry 300 (class 1259 OID 105458)
-- Name: term_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.term_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.term_candidat__id_seq OWNER TO opentheso;

--
-- TOC entry 301 (class 1259 OID 105459)
-- Name: term_candidat; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.term_candidat OWNER TO opentheso;

--
-- TOC entry 302 (class 1259 OID 105467)
-- Name: term_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.term_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.term_historique__id_seq OWNER TO opentheso;

--
-- TOC entry 303 (class 1259 OID 105468)
-- Name: term_historique; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.term_historique OWNER TO opentheso;

--
-- TOC entry 304 (class 1259 OID 105476)
-- Name: thesaurus_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.thesaurus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.thesaurus_id_seq OWNER TO opentheso;

--
-- TOC entry 305 (class 1259 OID 105477)
-- Name: thesaurus; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.thesaurus (
    id_thesaurus character varying NOT NULL,
    id_ark character varying NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    id integer DEFAULT nextval('public.thesaurus_id_seq'::regclass) NOT NULL,
    private boolean DEFAULT false
);


ALTER TABLE public.thesaurus OWNER TO opentheso;

--
-- TOC entry 306 (class 1259 OID 105486)
-- Name: thesaurus_alignement_source; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.thesaurus_alignement_source (
    id_thesaurus character varying NOT NULL,
    id_alignement_source integer NOT NULL
);


ALTER TABLE public.thesaurus_alignement_source OWNER TO opentheso;

--
-- TOC entry 307 (class 1259 OID 105491)
-- Name: thesaurus_array; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.thesaurus_array (
    id_thesaurus character varying NOT NULL,
    id_concept_parent character varying NOT NULL,
    ordered boolean DEFAULT false NOT NULL,
    notation character varying,
    id_facet character varying NOT NULL,
    created timestamp with time zone,
    modified timestamp with time zone,
    contributor integer DEFAULT '-1'::integer
);


ALTER TABLE public.thesaurus_array OWNER TO opentheso;

--
-- TOC entry 308 (class 1259 OID 105498)
-- Name: thesaurus_dcterms; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.thesaurus_dcterms (
    id_thesaurus character varying NOT NULL,
    name character varying NOT NULL,
    value character varying NOT NULL,
    language character varying,
    id bigint NOT NULL,
    data_type character varying
);


ALTER TABLE public.thesaurus_dcterms OWNER TO opentheso;

--
-- TOC entry 309 (class 1259 OID 105503)
-- Name: thesaurus_dcterms_id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

ALTER TABLE public.thesaurus_dcterms ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.thesaurus_dcterms_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- TOC entry 310 (class 1259 OID 105504)
-- Name: thesaurus_label; Type: TABLE; Schema: public; Owner: opentheso
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


ALTER TABLE public.thesaurus_label OWNER TO opentheso;

--
-- TOC entry 311 (class 1259 OID 105511)
-- Name: thesohomepage; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.thesohomepage (
    htmlcode character varying,
    lang character varying,
    idtheso character varying
);


ALTER TABLE public.thesohomepage OWNER TO opentheso;

--
-- TOC entry 312 (class 1259 OID 105516)
-- Name: user__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.user__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user__id_seq OWNER TO opentheso;

--
-- TOC entry 313 (class 1259 OID 105517)
-- Name: user_group_label__id_seq; Type: SEQUENCE; Schema: public; Owner: opentheso
--

CREATE SEQUENCE public.user_group_label__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_group_label__id_seq OWNER TO opentheso;

--
-- TOC entry 314 (class 1259 OID 105518)
-- Name: user_group_label; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.user_group_label (
    id_group integer DEFAULT nextval('public.user_group_label__id_seq'::regclass) NOT NULL,
    label_group character varying
);


ALTER TABLE public.user_group_label OWNER TO opentheso;

--
-- TOC entry 315 (class 1259 OID 105524)
-- Name: user_group_thesaurus; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.user_group_thesaurus (
    id_group integer NOT NULL,
    id_thesaurus character varying NOT NULL
);


ALTER TABLE public.user_group_thesaurus OWNER TO opentheso;

--
-- TOC entry 316 (class 1259 OID 105529)
-- Name: user_role_group; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.user_role_group (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_group integer NOT NULL
);


ALTER TABLE public.user_role_group OWNER TO opentheso;

--
-- TOC entry 317 (class 1259 OID 105532)
-- Name: user_role_only_on; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.user_role_only_on (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_theso character varying NOT NULL,
    id_group integer
);


ALTER TABLE public.user_role_only_on OWNER TO opentheso;

--
-- TOC entry 318 (class 1259 OID 105537)
-- Name: users; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.users (
    id_user integer DEFAULT nextval('public.user__id_seq'::regclass) NOT NULL,
    username character varying NOT NULL,
    password character varying NOT NULL,
    active boolean DEFAULT true NOT NULL,
    mail character varying NOT NULL,
    passtomodify boolean DEFAULT false,
    alertmail boolean DEFAULT false,
    issuperadmin boolean DEFAULT false,
    apikey character varying
);


ALTER TABLE public.users OWNER TO opentheso;

--
-- TOC entry 319 (class 1259 OID 105547)
-- Name: users_historique; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.users_historique (
    id_user integer NOT NULL,
    username character varying,
    created timestamp(6) with time zone DEFAULT now() NOT NULL,
    modified timestamp(6) with time zone DEFAULT now() NOT NULL,
    delete timestamp(6) with time zone
);


ALTER TABLE public.users_historique OWNER TO opentheso;

--
-- TOC entry 320 (class 1259 OID 105554)
-- Name: version_history; Type: TABLE; Schema: public; Owner: opentheso
--

CREATE TABLE public.version_history (
    "idVersionhistory" character varying NOT NULL,
    "idThesaurus" character varying NOT NULL,
    date date,
    "versionNote" character varying,
    "currentVersion" boolean,
    "thisVersion" boolean NOT NULL
);


ALTER TABLE public.version_history OWNER TO opentheso;

--
-- TOC entry 3880 (class 2604 OID 105559)
-- Name: candidat_vote id_vote; Type: DEFAULT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.candidat_vote ALTER COLUMN id_vote SET DEFAULT nextval('public.candidat_vote_id_vote_seq'::regclass);


--
-- TOC entry 3978 (class 2604 OID 105560)
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);


--
-- TOC entry 3980 (class 2604 OID 105561)
-- Name: status id_status; Type: DEFAULT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.status ALTER COLUMN id_status SET DEFAULT nextval('public.status_id_status_seq1'::regclass);


--
-- TOC entry 4343 (class 0 OID 105026)
-- Dependencies: 212
-- Data for Name: alignement; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.alignement (id, created, modified, author, concept_target, thesaurus_target, uri_target, alignement_id_type, internal_id_thesaurus, internal_id_concept, id_alignement_source) VALUES (1, '2024-02-14 10:43:43.821831', '2024-02-14 10:43:43.821831', 1, 'Lyon', 'GeoNames', 'https://www.geonames.org/2996944', 1, 'th2', '4', 190);
INSERT INTO public.alignement (id, created, modified, author, concept_target, thesaurus_target, uri_target, alignement_id_type, internal_id_thesaurus, internal_id_concept, id_alignement_source) VALUES (2, '2024-02-14 10:43:55.893498', '2024-02-14 10:43:55.893498', 1, 'Lyon', 'Wikidata_sparql', 'http://www.wikidata.org/entity/Q456', 1, 'th2', '4', 194);
INSERT INTO public.alignement (id, created, modified, author, concept_target, thesaurus_target, uri_target, alignement_id_type, internal_id_thesaurus, internal_id_concept, id_alignement_source) VALUES (3, '2024-02-14 10:44:12.518078', '2024-02-14 10:44:12.518078', 1, 'amphore', 'Wikidata_sparql', 'http://www.wikidata.org/entity/Q178401', 1, 'th2', '7', 194);


--
-- TOC entry 4345 (class 0 OID 105035)
-- Dependencies: 214
-- Data for Name: alignement_preferences; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4347 (class 0 OID 105042)
-- Dependencies: 216
-- Data for Name: alignement_source; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefSujets', 'https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:(##value##)%20AND%20recordtype_z:r', 'REST', 'json', 184, 1, 'alignement avec les Sujets de IdRef ABES Rameaux', false, 'IdRefSujets');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefAuteurs', 'https://www.idref.fr/Sru/Solr?wt=json&q=nom_t:(##nom##)%20AND%20prenom_t:(##prenom##)%20AND%20recordtype_z:a&fl=ppn_z,affcourt_z,prenom_s,nom_s&start=0&rows=30&version=2.2', 'REST', 'json', 185, 1, 'alignement avec les Auteurs de IdRef ABES', false, 'IdRefAuteurs');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('local_culture', 'http://localhost:8082/opentheso2/api/search?q=##value##&lang=##lang##&theso=th2', 'REST', 'skos', 39, 1, 'Opentheso', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('local_sarah', 'http://localhost:8082/opentheso2/api/search?q=##value##&lang=##lang##&theso=th1', 'REST', 'skos', 41, 1, 'Opentheso', false, 'Opentheso');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefPersonnes', 'https://www.idref.fr/Sru/Solr?wt=json&q=persname_t:(##value##)&fl=ppn_z,affcourt_z,prenom_s,nom_s&start=0&rows=30&version=2.2', 'REST', 'json', 186, 1, 'alignement avec les Noms de personnes de IdRef ABES', false, 'IdRefPersonnes');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('IdRefTitreUniforme', 'https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=uniformtitle_t:(##value##)%20AND%20recordtype_z:f', 'REST', 'json', 187, 1, 'alignement avec les titres uniformes de IdRef ABES', false, 'IdRefTitreUniforme');
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
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Wikidata_sparql', 'SELECT ?item ?itemLabel ?itemDescription WHERE {
                            ?item rdfs:label "##value##"@##lang##.
                            SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],##lang##". }
                }', 'SPARQL', 'json', 194, 1, 'alignement avec le vocabulaire Wikidata SPARQL', false, 'Wikidata_sparql');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Wikidata_rest', 'https://www.wikidata.org/w/api.php?action=wbsearchentities&language=##lang##&search=##value##&format=json&limit=10', 'REST', 'json', 195, 1, 'alignement avec le vocabulaire Wikidata REST', false, 'Wikidata_rest');
INSERT INTO public.alignement_source (source, requete, type_rqt, alignement_format, id, id_user, description, gps, source_filter) VALUES ('Ontome', 'https://ontome.net/api/classes-type-descendants/label/##value##/json', 'REST', 'json', 196, 1, 'OntoME is a LARHRA application, developed and maintained by the Digital history research team', false, 'Ontome');


--
-- TOC entry 4348 (class 0 OID 105050)
-- Dependencies: 217
-- Data for Name: alignement_type; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (1, 'Equivalence exacte', '=EQ', 'exactMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (2, 'Equivalence inexacte', '~EQ', 'closeMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (3, 'Equivalence générique', 'EQB', 'broadMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (4, 'Equivalence associative', 'EQR', 'relatedMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (5, 'Equivalence spécifique', 'EQS', 'narrowMatch');


--
-- TOC entry 4349 (class 0 OID 105055)
-- Dependencies: 218
-- Data for Name: bt_type; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (1, 'BT', 'Terme générique', 'Broader term');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (2, 'BTG', 'Terme générique (generic)', 'Broader term (generic)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (3, 'BTP', 'Terme générique (partitive)', 'Broader term (partitive)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (4, 'BTI', 'Terme générique (instance)', 'Broader term (instance)');


--
-- TOC entry 4351 (class 0 OID 105061)
-- Dependencies: 220
-- Data for Name: candidat_messages; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4352 (class 0 OID 105067)
-- Dependencies: 221
-- Data for Name: candidat_status; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4353 (class 0 OID 105073)
-- Dependencies: 222
-- Data for Name: candidat_vote; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4355 (class 0 OID 105079)
-- Dependencies: 224
-- Data for Name: compound_equivalence; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4357 (class 0 OID 105085)
-- Dependencies: 226
-- Data for Name: concept; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('3', 'th2', '', '2024-02-14 00:00:00+01', NULL, 'D', '', false, 3, false, '', '', 1, -1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('5', 'th2', '', '2024-02-14 00:00:00+01', NULL, 'D', '', false, 5, false, '', '', 1, -1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('6', 'th2', '', '2024-02-14 00:00:00+01', NULL, 'D', '', false, 6, false, '', '', 1, -1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('4', 'th2', '', '2024-02-14 00:00:00+01', '2024-02-14 00:00:00+01', 'D', '', false, 4, true, '', '', 1, 1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('7', 'th2', '', '2024-02-14 00:00:00+01', '2024-02-14 00:00:00+01', 'D', '', false, 7, false, '', '', 1, 1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('8', 'th2', '', '2024-02-14 00:00:00+01', NULL, 'D', '', false, 8, false, '', '', 1, -1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('1', 'th2', '', '2024-02-14 00:00:00+01', '2024-02-14 00:00:00+01', 'D', '', true, 1, false, '', '', 1, 1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('2', 'th2', '', '2024-02-14 00:00:00+01', '2024-02-14 00:00:00+01', 'D', '', true, 2, false, '', '', 1, 1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('9', 'th2', '', '2024-02-14 00:00:00+01', '2024-02-14 00:00:00+01', 'DEP', '', false, 9, false, '', '', 1, 1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('10', 'th2', '', '2024-02-14 00:00:00+01', NULL, 'D', '', false, 10, false, '', '', 1, -1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('11', 'th2', '', '2024-02-14 00:00:00+01', NULL, 'D', '', false, 11, false, '', '', 1, -1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('12', 'th2', '', '2024-02-14 00:00:00+01', NULL, 'D', '', false, 12, false, '', '', 1, -1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('13', 'th2', '', '2024-02-14 00:00:00+01', NULL, 'D', '', false, 13, false, '', '', 1, -1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('14', 'th2', '', '2024-02-14 00:00:00+01', NULL, 'D', '', false, 14, false, '', '', 1, -1, 'concept');


--
-- TOC entry 4359 (class 0 OID 105100)
-- Dependencies: 228
-- Data for Name: concept_candidat; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4360 (class 0 OID 105109)
-- Dependencies: 229
-- Data for Name: concept_dcterms; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('3', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('4', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('5', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('6', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('7', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('4', 'th2', 'contributor', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('7', 'th2', 'contributor', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('8', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('1', 'th2', 'contributor', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('2', 'th2', 'contributor', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('9', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('9', 'th2', 'contributor', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('10', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('11', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('12', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('13', 'th2', 'creator', 'admin', NULL, NULL);
INSERT INTO public.concept_dcterms (id_concept, id_thesaurus, name, value, language, data_type) VALUES ('14', 'th2', 'creator', 'admin', NULL, NULL);


--
-- TOC entry 4361 (class 0 OID 105114)
-- Dependencies: 230
-- Data for Name: concept_facet; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_facet (id_facet, id_thesaurus, id_concept) VALUES ('F1', 'th2', '11');
INSERT INTO public.concept_facet (id_facet, id_thesaurus, id_concept) VALUES ('F1', 'th2', '12');
INSERT INTO public.concept_facet (id_facet, id_thesaurus, id_concept) VALUES ('F3', 'th2', '13');
INSERT INTO public.concept_facet (id_facet, id_thesaurus, id_concept) VALUES ('F3', 'th2', '14');


--
-- TOC entry 4363 (class 0 OID 105120)
-- Dependencies: 232
-- Data for Name: concept_group; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_group (idgroup, id_ark, idthesaurus, idtypecode, notation, id, numerotation, id_handle, id_doi, created, modified) VALUES ('g2', '', 'th2', 'C', '', 1, NULL, '', '', '2024-02-14 00:00:00', NULL);
INSERT INTO public.concept_group (idgroup, id_ark, idthesaurus, idtypecode, notation, id, numerotation, id_handle, id_doi, created, modified) VALUES ('g3', '', 'th2', 'C', '', 2, NULL, '', '', '2024-02-14 00:00:00', NULL);
INSERT INTO public.concept_group (idgroup, id_ark, idthesaurus, idtypecode, notation, id, numerotation, id_handle, id_doi, created, modified) VALUES ('g4', '', 'th2', 'C', '', 3, NULL, '', '', '2024-02-14 00:00:00', '2024-02-14 00:00:00');


--
-- TOC entry 4364 (class 0 OID 105129)
-- Dependencies: 233
-- Data for Name: concept_group_concept; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g2', 'th2', '1');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g2', 'th2', '3');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g2', 'th2', '4');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g2', 'th2', '5');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g2', 'th2', '8');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g3', 'th2', '2');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g3', 'th2', '6');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g3', 'th2', '7');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g3', 'th2', '9');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g3', 'th2', '10');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g3', 'th2', '11');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g3', 'th2', '12');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g3', 'th2', '13');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('g3', 'th2', '14');


--
-- TOC entry 4366 (class 0 OID 105135)
-- Dependencies: 235
-- Data for Name: concept_group_historique; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4368 (class 0 OID 105143)
-- Dependencies: 237
-- Data for Name: concept_group_label; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_group_label (id, lexicalvalue, created, modified, lang, idthesaurus, idgroup) VALUES (1, 'Lieux', '2024-02-14 00:00:00', '2024-02-14 00:00:00', 'fr', 'th2', 'g2');
INSERT INTO public.concept_group_label (id, lexicalvalue, created, modified, lang, idthesaurus, idgroup) VALUES (2, 'Sujets', '2024-02-14 00:00:00', '2024-02-14 00:00:00', 'fr', 'th2', 'g3');
INSERT INTO public.concept_group_label (id, lexicalvalue, created, modified, lang, idthesaurus, idgroup) VALUES (3, 'Lieux France', '2024-02-14 00:00:00', '2024-02-14 00:00:00', 'fr', 'th2', 'g4');


--
-- TOC entry 4370 (class 0 OID 105152)
-- Dependencies: 239
-- Data for Name: concept_group_label_historique; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (1, 'Lieux', '2024-02-14 10:52:30.27442', 'fr', 'th2', 'g2', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (2, 'Sujets', '2024-02-14 10:52:41.906582', 'fr', 'th2', 'g3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (3, 'Lieux France', '2024-02-14 10:53:48.503919', 'fr', 'th2', 'g4', 1);


--
-- TOC entry 4371 (class 0 OID 105159)
-- Dependencies: 240
-- Data for Name: concept_group_type; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('MT', 'Microthesaurus', 'MicroThesaurus');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('G', 'Group', 'ConceptGroup');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('C', 'Collection', 'Collection');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('T', 'Theme', 'Theme');


--
-- TOC entry 4373 (class 0 OID 105165)
-- Dependencies: 242
-- Data for Name: concept_historique; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('1', 'th2', '', '2024-02-14 10:42:03.620622+01', 'D', '', true, '', 1, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('2', 'th2', '', '2024-02-14 10:42:15.20879+01', 'D', '', true, '', 2, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('3', 'th2', '', '2024-02-14 10:42:23.836192+01', 'D', '', false, '', 3, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('4', 'th2', '', '2024-02-14 10:42:27.498476+01', 'D', '', false, '', 4, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('5', 'th2', '', '2024-02-14 10:42:37.167732+01', 'D', '', false, '', 5, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('6', 'th2', '', '2024-02-14 10:43:05.393293+01', 'D', '', false, '', 6, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('7', 'th2', '', '2024-02-14 10:43:09.866156+01', 'D', '', false, '', 7, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('8', 'th2', '', '2024-02-14 10:52:02.037354+01', 'D', '', false, '', 8, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('9', 'th2', '', '2024-02-14 11:17:48.431011+01', 'D', '', false, 'g3', 9, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('9', 'th2', '', '2024-02-14 11:17:56.009109+01', 'DEP', '', false, '', 10, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('10', 'th2', '', '2024-02-14 11:19:04.124065+01', 'D', '', false, 'g3', 11, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('11', 'th2', '', '2024-02-14 11:19:17.329258+01', 'D', '', false, 'g3', 12, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('12', 'th2', '', '2024-02-14 11:19:22.187408+01', 'D', '', false, 'g3', 13, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('13', 'th2', '', '2024-02-14 11:19:27.163533+01', 'D', '', false, 'g3', 14, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('14', 'th2', '', '2024-02-14 11:19:32.708942+01', 'D', '', false, 'g3', 15, 1);


--
-- TOC entry 4374 (class 0 OID 105173)
-- Dependencies: 243
-- Data for Name: concept_replacedby; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_replacedby (id_concept1, id_concept2, id_thesaurus, modified, id_user) VALUES ('9', '6', 'th2', '2024-02-14 11:18:34.367492+01', 1);


--
-- TOC entry 4375 (class 0 OID 105179)
-- Dependencies: 244
-- Data for Name: concept_term_candidat; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4376 (class 0 OID 105184)
-- Dependencies: 245
-- Data for Name: concept_type; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.concept_type (code, label_fr, label_en, reciprocal, id_theso) VALUES ('concept', 'concept', 'concept', false, 'all');
INSERT INTO public.concept_type (code, label_fr, label_en, reciprocal, id_theso) VALUES ('people', 'personne', 'people', false, 'all');
INSERT INTO public.concept_type (code, label_fr, label_en, reciprocal, id_theso) VALUES ('period', 'période', 'period', false, 'all');
INSERT INTO public.concept_type (code, label_fr, label_en, reciprocal, id_theso) VALUES ('place', 'lieu', 'place', false, 'all');
INSERT INTO public.concept_type (code, label_fr, label_en, reciprocal, id_theso) VALUES ('qualifier', 'qualificatif', 'qualifier', false, 'all');
INSERT INTO public.concept_type (code, label_fr, label_en, reciprocal, id_theso) VALUES ('attribute', 'attribut', 'attribute', false, 'all');
INSERT INTO public.concept_type (code, label_fr, label_en, reciprocal, id_theso) VALUES ('attitude', 'attitude', 'attitude', false, 'all');


--
-- TOC entry 4377 (class 0 OID 105191)
-- Dependencies: 246
-- Data for Name: copyright; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4378 (class 0 OID 105196)
-- Dependencies: 247
-- Data for Name: corpus_link; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4379 (class 0 OID 105203)
-- Dependencies: 248
-- Data for Name: custom_concept_attribute; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4380 (class 0 OID 105208)
-- Dependencies: 249
-- Data for Name: custom_term_attribute; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4381 (class 0 OID 105213)
-- Dependencies: 250
-- Data for Name: databasechangelog; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('1', 'firas', 'changelog/v23.05.1/001-create-release-table.xml', '2023-12-20 11:35:57.826411', 1, 'EXECUTED', '9:2c7952fc7d3358ffc1b8a5d31da52488', 'createTable tableName=releases', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('dropPrimaryKey', 'Miled', 'changelog/v23.07.2/001-drop-primaryKey-concept_type.xml', '2023-12-20 11:35:57.901611', 2, 'EXECUTED', '9:43f2a8d7940e2d24d5761af1077f036a', 'dropPrimaryKey tableName=concept_type; addPrimaryKey constraintName=concept_type_theso, tableName=concept_type', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('script0003', 'Miled', 'changelog/db.changelog.xml', '2023-12-20 11:35:57.954649', 3, 'EXECUTED', '9:df2ea984ed7f5026e3d57e1c6cd0a4f3', 'sqlFile path=../install/liquibaseupdate/script0003.sql', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('executeScriptOriginal5', 'Miled', 'changelog/db.changelog.xml', '2023-12-20 11:35:58.192072', 4, 'EXECUTED', '9:8245dcb574f32ac7b77809fb1b28bb8f', 'sqlFile path=../install/maj_bdd_current2.sql', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('dropPrimaryKeyConceptDcterms', 'Miled', 'changelog/v23.07.3/001-update_concept_dcterms.xml', '2023-12-20 11:35:58.199914', 5, 'EXECUTED', '9:f017ddd5503a7e802538381074c7561a', 'dropPrimaryKey tableName=concept_dcterms; addPrimaryKey constraintName=concept_dcterms_pkey, tableName=concept_dcterms; dropNotNullConstraint columnName=language, tableName=concept_dcterms', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('dropPrimaryKeyThesaurusDcterms', 'Miled', 'changelog/v23.07.3/001-update_thesaurus_dcterms.xml', '2023-12-20 11:35:58.212816', 6, 'EXECUTED', '9:7e0800e30acd19a639b77c9a65b218ad', 'dropPrimaryKey tableName=thesaurus_dcterms; addColumn tableName=thesaurus_dcterms; addUniqueConstraint constraintName=thesaurus_dcterms_uniquekey, tableName=thesaurus_dcterms; dropNotNullConstraint columnName=language, tableName=thesaurus_dcterms', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('addColumnDataTypeDCterms', 'Miled', 'changelog/v23.07.3/002-update_dcterms.xml', '2023-12-20 11:35:58.216017', 7, 'EXECUTED', '9:6cc799f2f5a5bbcafa8d777d802884b9', 'addColumn tableName=concept_dcterms; addColumn tableName=thesaurus_dcterms', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('002', 'Gabsi', 'changelog/v23.07.3/002-add-projet-description-table.xml', '2023-12-20 11:35:58.22913', 8, 'EXECUTED', '9:35e57a55daa984f1dd1b0c2a4206fff5', 'createTable tableName=project_description', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('003', 'Gabsi', 'changelog/v23.07.3/003-update-primary-key-gps-table.xml', '2023-12-20 11:35:58.242039', 9, 'EXECUTED', '9:83cf54fb5bb52c4414ff3525265146dd', 'dropPrimaryKey tableName=gps; addColumn tableName=gps', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('v23.09.001', 'Miled', 'changelog/v23.09/001-delete-table-pref-gps.xml', '2023-12-20 11:35:58.245377', 10, 'EXECUTED', '9:b2ea753440dd1867abc153ee221bb1ec', 'dropTable tableName=gps_preferences', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('v23.09.002', 'Miled', 'changelog/v23.09/002-add-apikey-user.xml', '2023-12-20 11:35:58.247615', 11, 'EXECUTED', '9:f913daf076a1f1fe7d1d4e3e828d6012', 'addColumn tableName=users', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('v23.09.002', 'Firas', 'changelog/v23.09/003-add-position-gps-table.xml', '2023-12-20 11:35:58.249648', 12, 'EXECUTED', '9:350854c716a667ee07392946b53c6752', 'addColumn tableName=gps', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('v23.09.004', 'Firas', 'changelog/v23.09/004-update-primary-key-gps-table.xml', '2023-12-20 11:35:58.255251', 13, 'EXECUTED', '9:c2478b51fc3e799eb78ddea63e7021df', 'dropPrimaryKey tableName=gps; addPrimaryKey tableName=gps; modifyDataType columnName=latitude, tableName=gps; modifyDataType columnName=longitude, tableName=gps', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('executeScriptOriginal7', 'Fgabsi', 'changelog/db.changelog.xml', '2023-12-20 11:35:58.286162', 14, 'EXECUTED', '9:7dcae8ec0bba2fa918de56ca56900b25', 'sqlFile path=../changelog/v23.09/maj_functions.sql', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('v23.10.001', 'Miled', 'changelog/v23.10/001-table-roleonly.xml', '2023-12-20 11:35:58.290512', 15, 'EXECUTED', '9:960970be064e30817fce67645ec29982', 'dropColumn columnName=id_theso_domain, tableName=user_role_only_on; addColumn tableName=user_role_only_on', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('v23.10.001', 'Firas', 'changelog/v23.10/002-update-primary-key-ceoncept_group_label-table.xml', '2023-12-20 11:35:58.30236', 16, 'EXECUTED', '9:6f247ca3ae72eb602bc9c47b95093731', 'dropPrimaryKey tableName=concept_group_label; addPrimaryKey tableName=concept_group_label', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('v23.12.001', 'Miled', 'changelog/v23.12/23-12_001.xml', '2023-12-20 11:35:58.308984', 17, 'EXECUTED', '9:8c1ac6970114ad3a53259213dea36d94', 'insert tableName=users', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('23.12.002', 'Miled', 'changelog/db.changelog.xml', '2023-12-20 11:35:58.318785', 18, 'EXECUTED', '9:c35f133ff4394e94a63c4beb9026bb6b', 'sqlFile path=../changelog/v23.12/23-12_002.sql', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('23.12.003', 'Miled', 'changelog/db.changelog.xml', '2023-12-20 11:35:58.36601', 19, 'EXECUTED', '9:69c092e6b0cc2e87ef6b22e0e8f05ed4', 'sqlFile path=../changelog/v23.12/23-12_003.sql', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('23.12.004', 'Miled', 'changelog/v23.12/23-12_004.xml', '2023-12-20 11:35:58.399191', 20, 'EXECUTED', '9:f52898331a75b9dbca798919fe6cdb23', 'dropPrimaryKey tableName=external_images; addColumn tableName=external_images; addUniqueConstraint constraintName=external_images_unique, tableName=external_images', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('23.12.005', 'Miled', 'changelog/v23.12/23-12_004.xml', '2023-12-20 11:35:58.46468', 21, 'EXECUTED', '9:031f4ab2b8366da722c7b0e41b6328db', 'dropPrimaryKey tableName=non_preferred_term; addColumn tableName=non_preferred_term; addUniqueConstraint constraintName=non_prefered_term_unique, tableName=non_preferred_term', '', NULL, '4.23.2', NULL, NULL, '3068557742');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('23.12.005', 'Miled', 'changelog/db.changelog.xml', '2024-01-04 11:34:32.831885', 22, 'EXECUTED', '9:247e3fa48d2303dc148c3780f97079f4', 'sqlFile path=../changelog/v23.12/23-12_005.sql', '', NULL, '4.23.2', NULL, NULL, '4364472780');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('24.01.001', 'Miled', 'changelog/db.changelog.xml', '2024-01-17 11:55:13.24191', 23, 'EXECUTED', '9:7812bf0fbe1952eb2bcad50102fa7510', 'sqlFile path=../changelog/v24.01/24-01_001.sql', '', NULL, '4.23.2', NULL, NULL, '5488913198');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('v24.01.002', 'Miled', 'changelog/v24.01/24-01_002.xml', '2024-01-17 11:55:13.248149', 24, 'EXECUTED', '9:8ef6635b09a4b849e2a78069c7263ec2', 'addColumn tableName=note', '', NULL, '4.23.2', NULL, NULL, '5488913198');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('v24.01.004', 'Miled', 'changelog/v24.01/24-01_004.xml', '2024-01-23 16:25:35.537079', 26, 'EXECUTED', '9:50f2ca94b176f29fcbdf0db4fbe1258f', 'addColumn tableName=thesaurus_array; addColumn tableName=thesaurus_array; addColumn tableName=thesaurus_array', '', NULL, '4.23.2', NULL, NULL, '6023535419');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('24.01.003', 'Miled', 'changelog/db.changelog.xml', '2024-01-24 15:37:29.863114', 27, 'EXECUTED', '9:fc0013c469b3a0e039d3c8b5871df727', 'sqlFile path=../changelog/v24.01/24-01_003.sql', '', NULL, '4.23.2', NULL, NULL, '6107049733');
INSERT INTO public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) VALUES ('24.01.007', 'Miled', 'changelog/db.changelog.xml', '2024-02-13 14:43:32.498438', 28, 'EXECUTED', '9:b69460e49e65d1e2e59cd7759a1fec92', 'sqlFile path=../changelog/v24.01/24-01_007.sql', '', NULL, '4.23.2', NULL, NULL, '7831812119');


--
-- TOC entry 4382 (class 0 OID 105218)
-- Dependencies: 251
-- Data for Name: databasechangeloglock; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.databasechangeloglock (id, locked, lockgranted, lockedby) VALUES (1, false, NULL, NULL);


--
-- TOC entry 4383 (class 0 OID 105221)
-- Dependencies: 252
-- Data for Name: external_images; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri, id) VALUES ('4', 'th2', '', 'Wikidata_sparql', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/01. Panorama de Lyon pris depuis le toit de la Basilique de Fourvière.jpg', 15);
INSERT INTO public.external_images (id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri, id) VALUES ('7', 'th2', '', 'Wikidata_sparql', NULL, 'https://commons.wikimedia.org/wiki/Special:FilePath/Amphorae stacking.jpg', 16);


--
-- TOC entry 4385 (class 0 OID 105228)
-- Dependencies: 254
-- Data for Name: external_resources; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4387 (class 0 OID 105235)
-- Dependencies: 256
-- Data for Name: gps; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.gps (id_concept, id_theso, latitude, longitude, id, "position") VALUES ('4', 'th2', 45.74846, 4.84671, 60, NULL);


--
-- TOC entry 4390 (class 0 OID 105242)
-- Dependencies: 259
-- Data for Name: hierarchical_relationship; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('1', 'th2', 'NT', '3');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('3', 'th2', 'BT', '1');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('1', 'th2', 'NT', '4');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('4', 'th2', 'BT', '1');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('4', 'th2', 'NT', '5');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('5', 'th2', 'BT', '4');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('2', 'th2', 'NT', '6');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('6', 'th2', 'BT', '2');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('2', 'th2', 'NT', '7');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('7', 'th2', 'BT', '2');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('1', 'th2', 'NT', '8');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('8', 'th2', 'BT', '1');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('2', 'th2', 'NT', '9');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('9', 'th2', 'BT', '2');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('2', 'th2', 'NT', '10');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('10', 'th2', 'BT', '2');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('10', 'th2', 'NT', '11');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('11', 'th2', 'BT', '10');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('10', 'th2', 'NT', '12');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('12', 'th2', 'BT', '10');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('10', 'th2', 'NT', '13');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('13', 'th2', 'BT', '10');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('10', 'th2', 'NT', '14');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('14', 'th2', 'BT', '10');


--
-- TOC entry 4391 (class 0 OID 105247)
-- Dependencies: 260
-- Data for Name: hierarchical_relationship_historique; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('1', 'th2', 'NT', '3', '2024-02-14 10:42:23.836192+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('3', 'th2', 'BT', '1', '2024-02-14 10:42:23.836192+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('1', 'th2', 'NT', '4', '2024-02-14 10:42:27.498476+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('4', 'th2', 'BT', '1', '2024-02-14 10:42:27.498476+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('4', 'th2', 'NT', '5', '2024-02-14 10:42:37.167732+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('5', 'th2', 'BT', '4', '2024-02-14 10:42:37.167732+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('2', 'th2', 'NT', '6', '2024-02-14 10:43:05.393293+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('6', 'th2', 'BT', '2', '2024-02-14 10:43:05.393293+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('2', 'th2', 'NT', '7', '2024-02-14 10:43:09.866156+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('7', 'th2', 'BT', '2', '2024-02-14 10:43:09.866156+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('1', 'th2', 'NT', '8', '2024-02-14 10:52:02.037354+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('8', 'th2', 'BT', '1', '2024-02-14 10:52:02.037354+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('2', 'th2', 'NT', '9', '2024-02-14 11:17:48.431011+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('9', 'th2', 'BT', '2', '2024-02-14 11:17:48.431011+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('2', 'th2', 'NT', '10', '2024-02-14 11:19:04.124065+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('10', 'th2', 'BT', '2', '2024-02-14 11:19:04.124065+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('10', 'th2', 'NT', '11', '2024-02-14 11:19:17.329258+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('11', 'th2', 'BT', '10', '2024-02-14 11:19:17.329258+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('10', 'th2', 'NT', '12', '2024-02-14 11:19:22.187408+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('12', 'th2', 'BT', '10', '2024-02-14 11:19:22.187408+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('10', 'th2', 'NT', '13', '2024-02-14 11:19:27.163533+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('13', 'th2', 'BT', '10', '2024-02-14 11:19:27.163533+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('10', 'th2', 'NT', '14', '2024-02-14 11:19:32.708942+01', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('14', 'th2', 'BT', '10', '2024-02-14 11:19:32.708942+01', 1, 'ADD');


--
-- TOC entry 4392 (class 0 OID 105253)
-- Dependencies: 261
-- Data for Name: homepage; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.homepage (htmlcode, lang) VALUES ('<p><br></p><p><span class="ql-size-large">Aide et tutoriels : </span><a href="https://opentheso.hypotheses.org" rel="noopener noreferrer" target="_blank" class="ql-size-large">https://opentheso.hypotheses.org</a></p><p>Opentheso est distribué en licence libre de droit français compatible avec la licence <a href="http://www.gnu.org/copyleft/gpl.html" rel="noopener noreferrer" target="_blank">GNU GPL</a></p><p>C''est un gestionnaire de thesaurus multilingue, développé par la plateforme Technologique <a href="https://www.mom.fr/plateformes-technologiques/web-semantique-et-thesauri" rel="noopener noreferrer" target="_blank">WST</a> (Web Sémantique &amp; Thesauri) située à la <a href="https://www.mom.fr" rel="noopener noreferrer" target="_blank">MOM</a></p><p>en partenariat avec le <a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank">GDS-FRANTIQ</a>, le consortium <a href="https://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank">MASA</a> et la TGIR <a href="https://www.huma-num.fr/" rel="noopener noreferrer" target="_blank">Huma-Num</a></p><p><br></p>', 'fr');


--
-- TOC entry 4393 (class 0 OID 105258)
-- Dependencies: 262
-- Data for Name: info; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4395 (class 0 OID 105264)
-- Dependencies: 264
-- Data for Name: languages_iso639; Type: TABLE DATA; Schema: public; Owner: opentheso
--

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


--
-- TOC entry 4397 (class 0 OID 105271)
-- Dependencies: 266
-- Data for Name: node_label; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.node_label (id_thesaurus, lexical_value, created, modified, lang, id, id_facet) VALUES ('th2', 'lait par origine', '2024-02-14 11:19:49.351915+01', '2024-02-14 11:19:49.351915+01', 'fr', 2, 'F1');
INSERT INTO public.node_label (id_thesaurus, lexical_value, created, modified, lang, id, id_facet) VALUES ('th2', 'lait par matière', '2024-02-14 11:20:12.057607+01', '2024-02-14 11:20:12.057607+01', 'fr', 4, 'F3');


--
-- TOC entry 4398 (class 0 OID 105279)
-- Dependencies: 267
-- Data for Name: non_preferred_term; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4399 (class 0 OID 105287)
-- Dependencies: 268
-- Data for Name: non_preferred_term_historique; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4402 (class 0 OID 105296)
-- Dependencies: 271
-- Data for Name: note; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user, notesource, identifier) VALUES (1, 'definition', 'th2', NULL, NULL, 'en', 'commune in the metropolis of Lyon, France', '2024-02-14 10:43:55.974728', '2024-02-14 10:43:55.974728', 1, 'Wikidata_sparql', '4');
INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user, notesource, identifier) VALUES (2, 'definition', 'th2', NULL, NULL, 'fr', 'ville et commune française siège de la métropole de Lyon (chef-lieu de la région Auvergne-Rhône-Alpes)', '2024-02-14 10:43:55.978834', '2024-02-14 10:43:55.978834', 1, 'Wikidata_sparql', '4');
INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user, notesource, identifier) VALUES (3, 'definition', 'th2', NULL, NULL, 'en', 'type of storage container', '2024-02-14 10:44:12.520617', '2024-02-14 10:44:12.520617', 1, 'Wikidata_sparql', '7');
INSERT INTO public.note (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, created, modified, id_user, notesource, identifier) VALUES (4, 'definition', 'th2', NULL, NULL, 'fr', 'récipient de stockage pourvu de deux anses', '2024-02-14 10:44:12.521882', '2024-02-14 10:44:12.521882', 1, 'Wikidata_sparql', '7');


--
-- TOC entry 4404 (class 0 OID 105305)
-- Dependencies: 273
-- Data for Name: note_historique; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (1, 'definition', 'th2', NULL, '4', 'en', 'commune in the metropolis of Lyon, France', '2024-02-14 10:43:55.977417', 1, 'add');
INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (2, 'definition', 'th2', NULL, '4', 'fr', 'ville et commune française siège de la métropole de Lyon (chef-lieu de la région Auvergne-Rhône-Alpes)', '2024-02-14 10:43:55.981188', 1, 'add');
INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (3, 'definition', 'th2', NULL, '7', 'en', 'type of storage container', '2024-02-14 10:44:12.52098', 1, 'add');
INSERT INTO public.note_historique (id, notetypecode, id_thesaurus, id_term, id_concept, lang, lexicalvalue, modified, id_user, action_performed) VALUES (4, 'definition', 'th2', NULL, '7', 'fr', 'récipient de stockage pourvu de deux anses', '2024-02-14 10:44:12.522515', 1, 'add');


--
-- TOC entry 4405 (class 0 OID 105312)
-- Dependencies: 274
-- Data for Name: note_type; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('note', false, true, 'Note', 'Note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('historyNote', true, true, 'Note historique', 'History note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('scopeNote', false, true, 'Note d''application', 'Scope note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('example', true, false, 'Exemple', 'Example');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('editorialNote', true, false, 'Note éditoriale', 'Editorial note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('definition', true, false, 'Définition', 'Definition');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('changeNote', true, false, 'Note de changement', 'Change note');


--
-- TOC entry 4406 (class 0 OID 105318)
-- Dependencies: 275
-- Data for Name: nt_type; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (1, 'NT', 'Term spécifique', 'Narrower term');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (2, 'NTG', 'Term spécifique (generic)', 'Narrower term (generic)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (3, 'NTP', 'Term spécifique (partitive)', 'Narrower term (partitive)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (4, 'NTI', 'Term spécifique (instantial)', 'Narrower term (instantial)');


--
-- TOC entry 4407 (class 0 OID 105323)
-- Dependencies: 276
-- Data for Name: permuted; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4409 (class 0 OID 105329)
-- Dependencies: 278
-- Data for Name: preferences; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.preferences (id_pref, id_thesaurus, source_lang, identifier_type, path_image, dossier_resize, bdd_active, bdd_use_id, url_bdd, url_counter_bdd, z3950actif, collection_adresse, notice_url, url_encode, path_notice1, path_notice2, chemin_site, webservices, use_ark, server_ark, id_naan, prefix_ark, user_ark, pass_ark, use_handle, user_handle, pass_handle, path_key_handle, path_cert_handle, url_api_handle, prefix_handle, private_prefix_handle, preferredname, original_uri, original_uri_is_ark, original_uri_is_handle, uri_ark, generate_handle, auto_expand_tree, sort_by_notation, tree_cache, original_uri_is_doi, use_ark_local, naan_ark_local, prefix_ark_local, sizeid_ark_local, breadcrumb, useconcepttree, display_user_name, suggestion, use_custom_relation, uppercase_for_ark) VALUES (1, 'th2', 'fr', 2, '/var/www/images/', 'resize', false, false, 'http://www.mondomaine.fr/concept/##value##', 'http://mondomaine.fr/concept/##conceptId##/total', false, 'KOHA/biblios', 'http://catalogue.mondomaine.fr/cgi-bin/koha/opac-search.pl?type=opac&op=do_search&q=an=terme', 'UTF-8', '/var/www/notices/repositories.xml', '/var/www/notices/SchemaMappings.xml', 'http://mondomaine.fr/', true, false, 'http://ark.mondomaine.fr/ark:/', '66666', 'crt', NULL, NULL, false, NULL, NULL, '/certificat/key.p12', '/certificat/cacerts2', 'https://handle-server.mondomaine.fr:8001/api/handles/', '66.666.66666', 'crt', 'th2', NULL, false, false, 'https://ark.mom.fr/ark:/', false, true, false, false, false, false, '', '', 10, true, false, false, false, false, false);


--
-- TOC entry 4410 (class 0 OID 105379)
-- Dependencies: 279
-- Data for Name: preferences_sparql; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4411 (class 0 OID 105385)
-- Dependencies: 280
-- Data for Name: preferred_term; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('1', '1', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('2', '2', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('3', '3', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('4', '4', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('5', '5', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('6', '6', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('7', '7', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('8', '10', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('9', '11', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('10', '12', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('11', '13', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('12', '14', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('13', '15', 'th2');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('14', '16', 'th2');


--
-- TOC entry 4412 (class 0 OID 105390)
-- Dependencies: 281
-- Data for Name: project_description; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4414 (class 0 OID 105396)
-- Dependencies: 283
-- Data for Name: proposition; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4415 (class 0 OID 105403)
-- Dependencies: 284
-- Data for Name: proposition_modification; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4416 (class 0 OID 105408)
-- Dependencies: 285
-- Data for Name: proposition_modification_detail; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4419 (class 0 OID 105415)
-- Dependencies: 288
-- Data for Name: relation_group; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.relation_group (id_group1, id_thesaurus, relation, id_group2) VALUES ('g2', 'th2', 'sub', 'g4');


--
-- TOC entry 4420 (class 0 OID 105420)
-- Dependencies: 289
-- Data for Name: releases; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.releases (id, version, url, date, description) VALUES (1, 'v23.12.00', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.12.00', '2023-12-01', 'Pour une mise à jour : 
- il faut supprimer l''ancienne version (opentheso.war) dans le dossier tomcat (/var/lib/tomcat9/webapps)
- mettre le dernier war qui se trouve [ici](https://github.com/miledrousset/Opentheso2/tags) à la place de l''ancien dans le dossier tomcat (/var/lib/tomcat9/webapps)
- renommer le war en opentheso.war
- attendre que le WAR se déploie
- modifier les fichiers de conf (hikari.properties, hibernate.cfg.xml, preferences.properties)
- relancer Postgresql et Tomcat

Les prérequis :
Java version 11 minimum est nécessaire, postgresql 15,
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2

!!!IMPORTANT!!! à partir cette version, il y a 2 fichiers à paramétrer pour la base de données (hikari.properties) et (hibernate.cfg.xml).
A partir de cette version, il n''est plus nécessaire de faire les mises à jour de la BDD manuellement, ca se fait automatiquement Via Liquibase

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install)
l''aide se trouve sur https://opentheso.hypotheses.org/

**English:**
To upgrade : 
- delete the old version (opentheso.war) in the tomcat folder (/var/lib/tomcat9/webapps)
- put the latest war [here](https://github.com/miledrousset/Opentheso2/tags) in place of the old one in the tomcat folder (/var/lib/tomcat9/webapps)
- rename the war to opentheso.war
- wait for the WAR to be deployed
- modify the conf files (hikari.properties, hibernate.cfg.xml, preferences.properties)
- restart Postgresql and Tomcat

Translated with DeepL.com (free version)

Prerequisites :
Java version 11 minimum is required, postgresql 15,
Tomcat 9 (for the Tomcat 10 version, follow the home page) https://github.com/miledrousset/Opentheso2

!!!IMPORTANT!!! from this version, there are 2 files to configure for the database (hikari.properties) and (hibernate.cfg.xml).
From this version onwards, it is no longer necessary to update the database manually, it is done automatically via Liquibase.

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in (/classes/install).
Help is available at https://opentheso.hypotheses.org/
');
INSERT INTO public.releases (id, version, url, date, description) VALUES (2, 'v23.10.01', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.10.01', '2023-10-30', 'Les prérequis :
Java version 11 minimum est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.

!!!IMPORTANT!!! à partir cette version, il y a 2 fichiers à paramétrer pour la base de données (hikari.properties) et (hibernate.cfg.xml).
A partir de cette version, il n''est plus nécessaire de faire les mises à jour de la BDD manuellement, ca se fait automatiquement Via Liquibase

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install)
l''aide se trouve sur https://opentheso.hypotheses.org/

Prerequisites :
Java version 11 minimum is required
Tomcat 9 (for Tomcat 10 version, follow the home page) https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from version 10.

!!!IMPORTANT!!! starting with this version, there are 2 files to configure for the database (hikari.properties) and (hibernate.cfg.xml).
From this version onwards, it is no longer necessary to update the DB manually, it is done automatically via Liquibase.

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in (/classes/install).
Help is available at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (3, 'v23.09.01', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.09.01', '2023-09-11', 'Les prérequis :
Java version 11 minimum est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.

!!!IMPORTANT!!! à partir cette version, il y a 2 fichiers à paramétrer pour la base de données (hikari.properties) et (hibernate.cfg.xml).
A partir de cette version, il n''est plus nécessaire de faire les mises à jour de la BDD manuellement, ca se fait automatiquement Via Liquibase

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install)
l''aide se trouve sur https://opentheso.hypotheses.org/


Prerequisites :
Java version 11 minimum is required
Tomcat 9 (for Tomcat 10 version, follow the home page) https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from version 10.

!!!IMPORTANT!!! starting with this version, there are 2 files to configure for the database (hikari.properties) and (hibernate.cfg.xml).
From this version onwards, it is no longer necessary to update the DB manually, it is done automatically via Liquibase.

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in (/classes/install).
Help is available at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (4, 'v23.07.03', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.07.03', '2023-07-28', 'Note de version
- Correction de l''import de candidats en SKOS
- intégration des rebonds dans le même document PDF au format alphabétique
- correction d''une erreur lors du téléchargement d''un concept en SKOS
- correction mineur dans l''import CSV
- ajout des méta-données Dublin-Core pour les auteurs des concepts
- finalisation de l''intégration de l''OPEN API
- ajout de l''identifiant Ark pour le thésaurus
- amélioration de l''OpenApi
- ajout de la possibilité de cliquer sur l''URL de l''image pour aller vers la source
- déplacement d''un candidat vers un autre thésaurus
- Intégration des DcTerms pour les contributeurs et créateurs pour chaque modification :
   fonctionne en (import, export, toutes modifications dans le thésuaurus)
- ajout des CDMI pour le thésaurus
- intégration de Liquibase et Hibernate
- possibilité de filtrer les thésaurus par projet
- traduction en allemand

Les prérequis :
Java version 11 minimum est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.

**!!!IMPORTANT!!! à partir cette version, il y a 2 fichiers à paramétrer pour la base de données (hikari.properties) et (hibernate.cfg.xml).**
A partir de cette version, il n''est plus nécessaire de faire les mises à jour de la BDD manuellement, ca se fait automatiquement Via Liquibase

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install)
l''aide se trouve sur https://opentheso.hypotheses.org/

Release note
- Correction of the import of candidates in SKOS
- integration of bounces in the same PDF document in alphabetical format
- correction of an error when downloading a concept in SKOS
- minor correction to CSV import
- addition of Dublin-Core metadata for concept authors
- finalised integration of the OPEN API
- addition of the Ark identifier for the thesaurus
- improved OpenApi
- addition of the ability to click on the image URL to go to the source
- move a candidate to another thesaurus
- integration of DcTerms for contributors and creators for each modification:
   works in (import, export, all modifications in the thesaurus)
- addition of CDMIs for the thesaurus
- Liquibase and Hibernate integration
- ability to filter thesauri by project
- German translation

Prerequisites :
Java version 11 minimum is required
Tomcat 9 (for Tomcat 10 version, follow the home page) https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from version 10.

**!!!IMPORTANT!!! starting with this version, there are 2 files to configure for the database (hikari.properties) and (hibernate.cfg.xml).**
From this version onwards, it is no longer necessary to update the DB manually, it is done automatically via Liquibase.

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in (/classes/install).
Help is available at https://opentheso.hypotheses.org/
');
INSERT INTO public.releases (id, version, url, date, description) VALUES (19, 'v22.04.1', 'https://github.com/miledrousset/Opentheso2/releases/tag/v22.04.1', '2023-12-05', 'Note de version
- Intégration d''une nouvelle méthode de Gestion des permissions
- possibilité de gérer les permissions par thésaurus 
- Accélération de l''export des thésaurus x30
- export des concepts dépréciés amélioré 

Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (ne marche pas avec Tomcat 10)
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-22.04/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English
Release note
- Integration of a new permission management method
- possibility of managing permissions by thesaurus
- Acceleration of thesaurus export x30
- export of deprecated concepts improved

Prerequisites :
Java version 11 is required
Tomcat 9 (doesn''t work with Tomcat 10)
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-22.04/WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (5, 'v23.06.06', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.06.06', '2023-07-10', 'Note de version
- Correction de l''import de candidats en SKOS
- intégration des rebonds dans le même document PDF au foramt alphabétique
- correction d''une erreur lors du téléchargement d''un concept en SKOS
- correction mineur dans l''import CSV
- ajout des méta-données Dublin-Core pour les auteurs des concepts
- finalisation de l''intégration de l''OPEN API

Release note
- Correction of the import of SKOS candidates
- integration of bounces in the same PDF document in alphabetical order
- correction of an error when downloading a concept in SKOS
- minor correction to CSV import
- addition of Dublin-Core metadata for concept authors
- finalisation of OPEN API integration

Les prérequis :
Java version 11 minimum est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.
Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-.../WEB-INF⁩/classes⁩/install)
Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org/

Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.
For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-..../WEB-INF/classes/install)
For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.
help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (6, 'v23.06.2', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.06.2', '2023-06-09', 'Note de version
- Correction de l''import de candidats en SKOS
- intégration des rebonds dans le même document PDF au foramt alphabétique
- correction d''une erreur lors du téléchargement d''un concept en SKOS
- correction mineur dans l''import CSV
- ajout des méta-données Dublin-Core pour les auteurs des concepts
- finalisation de l''intégration de l''OPEN API

Release note
- Correction of the import of candidates in SKOS
- integration of bounces in the same PDF document in alphabetical format
- fixed an error when downloading a concept in SKOS
- minor fix in CSV import
- added Dublin-Core metadata for concept authors
- finalization of the integration of the OPEN API

Les prérequis :
Java version 11 minimum est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.
Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-.../WEB-INF⁩/classes⁩/install)
Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org/

Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.
For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-..../WEB-INF/classes/install)
For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.
help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (7, 'v23.06.0', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.06.0', '2023-06-02', 'Notes de version :
- Correction d''un bug pour l''import CSV quand il n''y a pas d''Id pour l''URI
- ajout de la précision de la langue source pour l''imprt SKOS
- Ajout d''une action pour comparer une liste de concepts au thésaurus (avec des
    options pour préciser le type de recherche à effectuer)
- Affichage des titres des images et des copyriht avec un lien cliquable
- correction de l''affichage erronné des candidats dans la vue concept (relation RT)
-correction d''un bug pour l''import des dates pour les concepts
- correction de l''import des dates pour les collections
- correction de la boite de dialogue de création d''utilisateur
- amélioration de l''affichage du fil d''ariane
- correction du fil d''ariane pour la poly-hiérarchie imbriquée
- amélioration de l''export PDF
- Passage de l''API REST vers la norme OpenAPI 

Les prérequis :
Java version 11 minimum est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.
Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-.../WEB-INF⁩/classes⁩/install)
Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org/

Release notes :
- fixed a bug in CSV import when there is no Id for the URI
- addition of source language precision for SKOS import
- Added an action for comparing a list of concepts with the thesaurus (with options for
    options to specify the type of search to be performed)
- Display of image and copyriht titles with a clickable link
- corrected incorrect display of candidates in the concept view (RT relationship)
-fixed a bug in importing dates for concepts
- fixed import of dates for collections
- correction of the user creation dialogue box
- improved display of breadcrumb trail
- fixed breadcrumb trail for nested poly-hierarchy
- improved PDF export
- Transition of the REST API to the OpenAPI standard 

Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.
For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-..../WEB-INF/classes/install)
For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.
help can be found at https://opentheso.hypotheses.org/
');
INSERT INTO public.releases (id, version, url, date, description) VALUES (8, 'v23.04.08', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.04.08', '2023-04-20', 'Notes de version :
_Copier/coller d''un thésaurus à un autre
    - correction de la procédure.
correction de la dialogue de la gestion des projets
correction de l''export PDF pour mieux afficher les images
masquer l''affichage d''un bloc vide qui s''affichait en mode non connecté 
Activer la commande de génération d''identifiants ARK en local
Correction de l''import CSV pour mieux gérer les identifiants Majuscule Minuscule 
**Intégration de la gestion de type de concept (Qualificatif, Period, Place ...)**
**changment du coeur d''import RDF en utilisant la dernière version RDF4J et en ajoutant des nouvelles classes**
correction de l''export pour les URLs vides_

Les prérequis :
Java version 11 minimum est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-.../WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English
**Release notes :**
_Copy/paste from one thesaurus to another
    - correction of the procedure.
correction of the project management dialogue
correction of the PDF export to better display images
hide the display of an empty block which was displayed in offline mode 
Activate the command to generate ARK identifiers locally
Fix CSV import to better manage upper and lower case identifiers 
**Integration of concept type management (Qualificative, Period, Place ...)**.
**change of the RDF import core using the latest RDF4J version and adding new classes
correction of the export for empty URLs_

Translated with www.DeepL.com/Translator (free version)
Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-..../WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (18, 'v22.07', 'https://github.com/miledrousset/Opentheso2/releases/tag/v22.07', '2022-07-06', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (ne marche pas avec Tomcat 10)
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-22.04/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English

Prerequisites :
Java version 11 is required
Tomcat 9 (doesn''t work with Tomcat 10)
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-22.04/WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (9, 'v23.04.06', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.04.06', '2023-04-06', '**Notes de version :** 
_Copier/coller d''un thésaurus à un autre
    - correction de la procédure.
correction de la dialogue de la gestion des projets
correction de l''export PDF pour mieux afficher les images
masquer l''affichage d''un bloc vide qui s''affichait en mode non connecté_ 


Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-.../WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English

Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-..../WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (10, 'v23.03.03', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.03.03', '2023-03-23', '_**Alignement:
    - correction module d''alignement auto (cas de l''ajout d''un alignement manuel)
    - correction module d''alignement auto (intitulé des colonnes non conformes)
Export CSV :
    - correction de l''import de fichier structuré (hiérarchie par tabulation)
Dialogue Traduction : 
    - correction des intitulés 
    - traductions des dialogues
Note:
    - distinguer la note à modifier en affichant la langue utilisée
Couper/coller manuel : 
    - Controle renforcer pour éviter les boucles à l''infini
Navigation: 
    - detection en temps réel des branches qui bouclent à l''infini
    - proposition de correction de ces boucles
Projets :
    - déscativation pour Admin de la possibilité d''ajouter des projets (ajout abusif et incontrôlé) 
    - correction du module Candidat (insertion des candidats)
import CSV
    - correction d''une petite erreur qui empêchait l''import**_



Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-.../WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English

Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-..../WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (11, 'v23.03.01', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.03.01', '2023-03-14', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-.../WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English

Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-..../WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (12, 'v23.02.05', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.02.05', '2023-02-23', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-.../WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English

Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-..../WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (26, 'v21.05.0', 'https://github.com/miledrousset/Opentheso2/releases/tag/v21.05.0', '2021-05-05', 'Optimisation de la mémoire
nouveau Graphe basé sur D3JS
Java version 11 minimum est nécessaire
Tomcat 9 (ne marche pas avec la 10)
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"

Pour une mise à jour à partir des versions antérieures, il faut appliquer le script SQL "maj_bdd_current.sql" inclus dans le WAR (opentheso-20.10/WEB-INF⁩/classes⁩/install)
puis "maj_bdd_current2.sql"

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install).

L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org
');
INSERT INTO public.releases (id, version, url, date, description) VALUES (13, 'v23.01.01', 'https://github.com/miledrousset/Opentheso2/releases/tag/v23.01.01', '2023-01-18', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-23.01.01/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English

Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-23.01.01/WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (14, 'v22.11', 'https://github.com/miledrousset/Opentheso2/releases/tag/v22.11', '2022-11-15', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2 
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-22.04/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English

Prerequisites :
Java version 11 is required
Tomcat 9 (for the version of Tomcat 10, follow the doc at https://github.com/miledrousset/Opentheso2
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-22.04/WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (15, 'v22.10.1', 'https://github.com/miledrousset/Opentheso2/releases/tag/v22.10.1', '2022-10-17', 'Correction de quelques bugs');
INSERT INTO public.releases (id, version, url, date, description) VALUES (16, 'v22.10', 'https://github.com/miledrousset/Opentheso2/releases/tag/v22.10', '2022-10-06', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9, pour la version Tomcat 10, il faut suivre la doc [ici](https://github.com/miledrousset/Opentheso2)
Postgresql (la version la plus récente).

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-22.09/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English

Prerequisites :
Java version 11 is required
Tomcat 9 for the version of Tomcat 10, please follow [this doc](https://github.com/miledrousset/Opentheso2)
Postgresql (the most recent version).

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-22.04/WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (17, 'v22.09', 'https://github.com/miledrousset/Opentheso2/releases/tag/v22.09', '2022-09-14', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9, pour la version Tomcat 10, il faut suivre la doc [ici](https://github.com/miledrousset/Opentheso2)
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-22.09/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), **il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql")**.
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English

Prerequisites :
Java version 11 is required
Tomcat 9 for the version of Tomcat 10, please follow [this doc](https://github.com/miledrousset/Opentheso2)
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-22.04/WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (27, 'v21.02.1', 'https://github.com/miledrousset/Opentheso2/releases/tag/v21.02.1', '2021-02-23', 'Correction de bugs

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"

Pour une mise à jour à partir des versions antérieures, il faut appliquer le script SQL "maj_bdd_current.sql" inclus dans le WAR (opentheso-20.10/WEB-INF⁩/classes⁩/install)
puis "maj_bdd_current2.sql"

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install).

L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org
');
INSERT INTO public.releases (id, version, url, date, description) VALUES (20, 'v22.04', 'https://github.com/miledrousset/Opentheso2/releases/tag/v22.04', '2022-04-15', '**La première version basée sur un nouveau template.**

Les prérequis :
**Java version 11** est nécessaire
**Tomcat 9** (ne marche pas avec Tomcat 10)
**Postgresql** (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-22.04/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

English
The first version based on a new template
Prerequisites :
Java version 11 is required
Tomcat 9 (doesn''t work with Tomcat 10)
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-22.04/WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (21, 'v22.03', 'https://github.com/miledrousset/Opentheso2/releases/tag/v22.03', '2022-03-04', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (ne marche pas avec Tomcat 10)
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-22.01.0/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org/

**English**
Prerequisites :
Java version 11 is required
Tomcat 9 (doesn''t work with Tomcat 10)
Postgresql (the most recent version) from 10.

For an update between the new Opentheso2 versions, you must apply the script "maj_bdd_current2.sql"
included in the WAR (opentheso-22.01.0/WEB-INF/classes/install)

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in ( /classes/install), you must always apply the update script ("maj_bdd_current2.sql").
The old installation doc remains valid.

help can be found at https://opentheso.hypotheses.org/ ');
INSERT INTO public.releases (id, version, url, date, description) VALUES (22, 'v22.01', 'https://github.com/miledrousset/Opentheso2/releases/tag/v22.01', '2022-01-19', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (ne marche pas avec Tomcat 10)
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-22.01.0/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org');
INSERT INTO public.releases (id, version, url, date, description) VALUES (23, 'v21.11.0', 'https://github.com/miledrousset/Opentheso2/releases/tag/v21.11.0', '2021-10-28', 'Les prérequis :
Java version 11 est nécessaire
Tomcat 9 (ne marche pas avec Tomcat 10)
Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"
inclus dans le WAR (opentheso-21.11.0/WEB-INF⁩/classes⁩/install)

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").
L''ancienne doc d''installation reste valide.

l''aide se trouve sur https://opentheso.hypotheses.org');
INSERT INTO public.releases (id, version, url, date, description) VALUES (24, 'v21.09.0', 'https://github.com/miledrousset/Opentheso2/releases/tag/v21.09.0', '2021-09-15', 'Les prérequis :
    Java version 11 est nécessaire
    Tomcat 9 (ne marche pas avec la 10)
    Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"

Pour une mise à jour à partir des versions antérieures (passage de Opentheso à Opentheso2), il faut appliquer le script SQL "maj_bdd_current.sql" inclus dans le WAR (opentheso-21.07.0/WEB-INF⁩/classes⁩/install)
puis "maj_bdd_current2.sql"

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").

L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org
');
INSERT INTO public.releases (id, version, url, date, description) VALUES (25, 'v21.07.0', 'https://github.com/miledrousset/Opentheso2/releases/tag/v21.07.0', '2021-07-09', '- Problème majeur de mémoire corrigé, ce problème provenait d''une bibliothèque non compatible avec les autres modules.

Les prérequis :  
- Java version 11 minimum est nécessaire
- Tomcat 9 (ne marche pas avec la 10)
- Postgresql (la version la plus récente) à partir de la 10.

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"

Pour une mise à jour à partir des versions antérieures (passage de Opentheso à Opentheso2), il faut appliquer le script SQL "maj_bdd_current.sql" inclus dans le WAR (opentheso-21.07.0/WEB-INF⁩/classes⁩/install)
puis "maj_bdd_current2.sql"

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install), il faut toujours appliquer le script de mise à jour ("maj_bdd_current2.sql").

L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org
');
INSERT INTO public.releases (id, version, url, date, description) VALUES (28, '21.02', 'https://github.com/miledrousset/Opentheso2/releases/tag/21.02', '2021-02-03', '- Intégration de la gestion des Facettes
- Traductions de l''interface en anglais : phase 1 (50% des traductions)

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"

Pour une mise à jour à partir des versions antérieures, il faut appliquer le script SQL "maj_bdd_current.sql" inclus dans le WAR (opentheso-20.10/WEB-INF⁩/classes⁩/install)
puis "maj_bdd_current2.sql"

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install).

L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org');
INSERT INTO public.releases (id, version, url, date, description) VALUES (29, 'v20.12', 'https://github.com/miledrousset/Opentheso2/releases/tag/v20.12', '2020-12-02', '- Intégration de la gestion des concepts dépréciés 
- gestion des notes avec URL

Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"

Pour une mise à jour à partir des versions antérieures, il faut appliquer le script SQL "maj_bdd_current.sql" inclus dans le WAR (opentheso-20.10/WEB-INF⁩/classes⁩/install)
puis "maj_bdd_current2.sql"

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (opentheso-20.10/WEB-INF⁩/classes⁩/install).

L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org/
');
INSERT INTO public.releases (id, version, url, date, description) VALUES (30, 'v20.11.1', 'https://github.com/miledrousset/Opentheso2/releases/tag/v20.11.1', '2020-11-23', '!!! correction d''un bug important qui prenait beaucoup de ressources mémoire !!!
Pour une mise à jour entre les nouvelles version Opentheso2, il faut appliquer le script "maj_bdd_current2.sql"

Pour une mise à jour à partir des versions antérieures, il faut appliquer le script SQL "maj_bdd_current.sql" inclus dans le WAR (opentheso-20.10/WEB-INF⁩/classes⁩/install)
puis "maj_bdd_current2.sql"

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (opentheso-20.10/WEB-INF⁩/classes⁩/install).

L''ancienne doc d''installation reste valide.
l''aide se trouve sur https://opentheso.hypotheses.org/
');
INSERT INTO public.releases (id, version, url, date, description) VALUES (31, 'v24.02.01', 'https://github.com/miledrousset/Opentheso2/releases/tag/v24.02.01', '2024-02-05', 'Note de version v24.02
- Export/import csv avec prise en charge des images + multi-GPS + resourcesExternes
  + Facettes + concepts dépréciés
 
- Ajout des commandes Plpgsql pour récupérer les concepts (gain de temps x100)
- Restructuration du fil d''Ariane (prise en charge des boucles type graphe)
- Restructuration des notes : intégration des notes pour les collections et les facettes (intégration dans l''export et l''import)
- Export: ajout de la possibilité d''exporter les collections d''un thésaurus sous
    forme de thésaurus indépendant, 1 thésaurus par collection, le résultat est retourner dans un fichier zippé. (formats pris en charge : SKOS et CSV)
    
Pour une mise à jour :
    il faut supprimer l''ancienne version (opentheso.war) dans le dossier tomcat (/var/lib/tomcat9/webapps)
    mettre le dernier war qui se trouve [ici](https://github.com/miledrousset/Opentheso2/tags) à la place de l''ancien dans le dossier tomcat (/var/lib/tomcat9/webapps)
    renommer le war en opentheso.war
    attendre que le WAR se déploie
    modifier les fichiers de conf (hikari.properties, hibernate.cfg.xml, preferences.properties)
    relancer Postgresql et Tomcat

Les prérequis :
Java version 11 minimum est nécessaire, postgresql 15,
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2

!!!IMPORTANT!!! à partir cette version, il y a 2 fichiers à paramétrer pour la base de données (hikari.properties) et (hibernate.cfg.xml).
A partir de cette version, il n''est plus nécessaire de faire les mises à jour de la BDD manuellement, ca se fait automatiquement Via Liquibase

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install)
l''aide se trouve sur https://opentheso.hypotheses.org/

**English**
Release note v24.02
- Export/import csv with image support + multi-GPS + resourcesExternes
  + Facets + deprecated concepts
 
- Addition of Plpgsql commands to retrieve concepts (time saving x100)
- Restructured breadcrumb trail (support for graph-type loops)
- Restructuring of notes: integration of notes for collections and facets (integration into export and import)
- Export: added the ability to export thesaurus collections as independent
    as an independent thesaurus, 1 thesaurus per collection, the result is returned in a zipped file (formats supported: SKOS and CSV)
    
To update :
    delete the old version (opentheso.war) from the tomcat folder (/var/lib/tomcat9/webapps)
    put the latest war [here](https://github.com/miledrousset/Opentheso2/tags) in place of the old one in the tomcat folder (/var/lib/tomcat9/webapps)
    rename the war to opentheso.war
    wait for the WAR to be deployed
    modify the conf files (hikari.properties, hibernate.cfg.xml, preferences.properties)
    restart Postgresql and Tomcat

Prerequisites:
Java version 11 minimum is required, postgresql 15,
Tomcat 9 (for the version of Tomcat 10, follow the documentation on the home page) https://github.com/miledrousset/Opentheso2

!!!IMPORTANT!!! from this version, there are 2 files to set up for the database (hikari.properties) and (hibernate.cfg.xml).
From this version onwards, it is no longer necessary to update the database manually, it is done automatically via Liquibase.

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in (/classes/install).
Help is available at https://opentheso.hypotheses.org/');
INSERT INTO public.releases (id, version, url, date, description) VALUES (32, 'v24.02.03', 'https://github.com/miledrousset/Opentheso2/releases/tag/v24.02.03', '2024-02-14', 'Note de version v24.02.03

- Correction d''un bug pour la gestions des coordonnées GPS

Pour une mise à jour :
il faut supprimer l''ancienne version (opentheso.war) dans le dossier tomcat (/var/lib/tomcat9/webapps)
mettre le dernier war qui se trouve [ici](https://github.com/miledrousset/Opentheso2/tags) à la place de l''ancien dans le dossier tomcat (/var/lib/tomcat9/webapps)
renommer le war en opentheso.war
attendre que le WAR se déploie
modifier les fichiers de conf (hikari.properties, hibernate.cfg.xml, preferences.properties)
relancer Postgresql et Tomcat

Les prérequis :
Java version 11 minimum est nécessaire, postgresql 15,
Tomcat 9 (pour la version de Tomcat 10, suivre la doc à l''accueil) https://github.com/miledrousset/Opentheso2

!!!IMPORTANT!!! à partir cette version, il y a 2 fichiers à paramétrer pour la base de données (hikari.properties) et (hibernate.cfg.xml).
A partir de cette version, il n''est plus nécessaire de faire les mises à jour de la BDD manuellement, ca se fait automatiquement Via Liquibase

Pour une nouvelle installation, il faut suivre la documentation dans le wiki (https://github.com/miledrousset/opentheso/wiki/Installation), puis choisir le script de création de la base de données (opentheso_current.sql) inclut dans (/classes⁩/install)
l''aide se trouve sur https://opentheso.hypotheses.org/

English
Release note v24.02.03
- Fixed a bug in the management of GPS coordinates

To update :
delete the old version (opentheso.war) from the tomcat folder (/var/lib/tomcat9/webapps)
put the latest war [here](https://github.com/miledrousset/Opentheso2/tags) in place of the old one in the tomcat folder (/var/lib/tomcat9/webapps)
rename the war to opentheso.war
wait for the WAR to be deployed
modify the conf files (hikari.properties, hibernate.cfg.xml, preferences.properties)
restart Postgresql and Tomcat

Prerequisites:
Java version 11 minimum is required, postgresql 15,
Tomcat 9 (for the version of Tomcat 10, follow the documentation on the home page) https://github.com/miledrousset/Opentheso2

!!!IMPORTANT!!! from this version, there are 2 files to set up for the database (hikari.properties) and (hibernate.cfg.xml).
From this version onwards, it is no longer necessary to update the database manually, it is done automatically via Liquibase.

For a new installation, follow the documentation in the wiki (https://github.com/miledrousset/opentheso/wiki/Installation), then choose the database creation script (opentheso_current.sql) included in (/classes/install).
Help is available at https://opentheso.hypotheses.org/');


--
-- TOC entry 4422 (class 0 OID 105426)
-- Dependencies: 291
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.roles (id, name, description) VALUES (1, 'superAdmin', 'Super Administrateur pour tout gérer tout thésaurus et tout utilisateur');
INSERT INTO public.roles (id, name, description) VALUES (2, 'admin', 'administrateur pour un domaine ou parc de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (3, 'manager', 'gestionnaire de thésaurus, pas de création de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (4, 'contributor', 'traducteur, notes, candidats, images');


--
-- TOC entry 4424 (class 0 OID 105432)
-- Dependencies: 293
-- Data for Name: routine_mail; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4425 (class 0 OID 105438)
-- Dependencies: 294
-- Data for Name: split_non_preferred_term; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4426 (class 0 OID 105441)
-- Dependencies: 295
-- Data for Name: status; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.status (id_status, value) VALUES (1, 'En attente');
INSERT INTO public.status (id_status, value) VALUES (2, 'Inséré');
INSERT INTO public.status (id_status, value) VALUES (3, 'Rejeté');


--
-- TOC entry 4430 (class 0 OID 105449)
-- Dependencies: 299
-- Data for Name: term; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('1', 'Europe', 'fr', 'th2', '2024-02-14 10:42:03.620622+01', '2024-02-14 10:42:03.620622+01', '', 'D', 1, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('2', 'Objets', 'fr', 'th2', '2024-02-14 10:42:15.20879+01', '2024-02-14 10:42:15.20879+01', '', 'D', 2, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('3', 'Paris', 'fr', 'th2', '2024-02-14 10:42:23.836192+01', '2024-02-14 10:42:23.836192+01', '', 'D', 3, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('4', 'Lyon', 'fr', 'th2', '2024-02-14 10:42:27.498476+01', '2024-02-14 10:42:27.498476+01', '', 'D', 4, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('5', 'vaise', 'fr', 'th2', '2024-02-14 10:42:37.167732+01', '2024-02-14 10:42:37.167732+01', '', 'D', 5, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('6', 'vase', 'fr', 'th2', '2024-02-14 10:43:05.393293+01', '2024-02-14 10:43:05.393293+01', '', 'D', 6, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('7', 'amphore', 'fr', 'th2', '2024-02-14 10:43:09.866156+01', '2024-02-14 10:43:09.866156+01', '', 'D', 7, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('4', 'Lyon', 'en', 'th2', '2024-02-14 10:43:55.896132+01', '2024-02-14 10:43:55.896132+01', '', '', 8, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('7', 'amphora', 'en', 'th2', '2024-02-14 10:44:12.519672+01', '2024-02-14 10:44:12.519672+01', '', '', 9, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('10', 'Grenoble', 'fr', 'th2', '2024-02-14 10:52:02.037354+01', '2024-02-14 10:52:02.037354+01', '', 'D', 10, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('11', 'récipient', 'fr', 'th2', '2024-02-14 11:17:48.431011+01', '2024-02-14 11:17:48.431011+01', '', 'D', 11, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('12', 'lait', 'fr', 'th2', '2024-02-14 11:19:04.124065+01', '2024-02-14 11:19:04.124065+01', '', 'D', 12, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('13', 'lait de chèvre', 'fr', 'th2', '2024-02-14 11:19:17.329258+01', '2024-02-14 11:19:17.329258+01', '', 'D', 13, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('14', 'lait de vache', 'fr', 'th2', '2024-02-14 11:19:22.187408+01', '2024-02-14 11:19:22.187408+01', '', 'D', 14, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('15', 'lait entier', 'fr', 'th2', '2024-02-14 11:19:27.163533+01', '2024-02-14 11:19:27.163533+01', '', 'D', 15, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('16', 'lait demi-écrémé', 'fr', 'th2', '2024-02-14 11:19:32.708942+01', '2024-02-14 11:19:32.708942+01', '', 'D', 16, 1, 1);


--
-- TOC entry 4432 (class 0 OID 105459)
-- Dependencies: 301
-- Data for Name: term_candidat; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4434 (class 0 OID 105468)
-- Dependencies: 303
-- Data for Name: term_historique; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('1', 'Europe', 'fr', 'th2', '2024-02-14 10:42:03.620622+01', '', 'D', 1, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('2', 'Objets', 'fr', 'th2', '2024-02-14 10:42:15.20879+01', '', 'D', 2, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('3', 'Paris', 'fr', 'th2', '2024-02-14 10:42:23.836192+01', '', 'D', 3, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('4', 'Lyon', 'fr', 'th2', '2024-02-14 10:42:27.498476+01', '', 'D', 4, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('5', 'vaise', 'fr', 'th2', '2024-02-14 10:42:37.167732+01', '', 'D', 5, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('6', 'vase', 'fr', 'th2', '2024-02-14 10:43:05.393293+01', '', 'D', 6, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('7', 'amphore', 'fr', 'th2', '2024-02-14 10:43:09.866156+01', '', 'D', 7, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('4', 'Lyon', 'en', 'th2', '2024-02-14 10:43:55.900684+01', '', 'D', 8, 1, 'New');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('7', 'amphora', 'en', 'th2', '2024-02-14 10:44:12.520045+01', '', 'D', 9, 1, 'New');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('10', 'Grenoble', 'fr', 'th2', '2024-02-14 10:52:02.037354+01', '', 'D', 10, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('11', 'récipient', 'fr', 'th2', '2024-02-14 11:17:48.431011+01', '', 'D', 11, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('12', 'lait', 'fr', 'th2', '2024-02-14 11:19:04.124065+01', '', 'D', 12, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('13', 'lait de chèvre', 'fr', 'th2', '2024-02-14 11:19:17.329258+01', '', 'D', 13, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('14', 'lait de vache', 'fr', 'th2', '2024-02-14 11:19:22.187408+01', '', 'D', 14, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('15', 'lait entier', 'fr', 'th2', '2024-02-14 11:19:27.163533+01', '', 'D', 15, 1, 'ADD');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('16', 'lait demi-écrémé', 'fr', 'th2', '2024-02-14 11:19:32.708942+01', '', 'D', 16, 1, 'ADD');


--
-- TOC entry 4436 (class 0 OID 105477)
-- Dependencies: 305
-- Data for Name: thesaurus; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.thesaurus (id_thesaurus, id_ark, created, modified, id, private) VALUES ('th2', '', '2024-02-14 00:00:00', '2024-02-14 00:00:00', 1, false);


--
-- TOC entry 4437 (class 0 OID 105486)
-- Dependencies: 306
-- Data for Name: thesaurus_alignement_source; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.thesaurus_alignement_source (id_thesaurus, id_alignement_source) VALUES ('th2', 190);
INSERT INTO public.thesaurus_alignement_source (id_thesaurus, id_alignement_source) VALUES ('th2', 194);


--
-- TOC entry 4438 (class 0 OID 105491)
-- Dependencies: 307
-- Data for Name: thesaurus_array; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.thesaurus_array (id_thesaurus, id_concept_parent, ordered, notation, id_facet, created, modified, contributor) VALUES ('th2', '10', false, NULL, 'F1', NULL, NULL, -1);
INSERT INTO public.thesaurus_array (id_thesaurus, id_concept_parent, ordered, notation, id_facet, created, modified, contributor) VALUES ('th2', '10', false, NULL, 'F3', NULL, NULL, -1);


--
-- TOC entry 4439 (class 0 OID 105498)
-- Dependencies: 308
-- Data for Name: thesaurus_dcterms; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4441 (class 0 OID 105504)
-- Dependencies: 310
-- Data for Name: thesaurus_label; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th2', 'admin', '', 'admin', '2024-02-14 00:00:00', '2024-02-14 00:00:00', '', '', 'fr', '', '', '', '', '', 'Theso_test', '');
INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th2', 'admin', '', 'admin', '2024-02-14 00:00:00', '2024-02-14 00:00:00', '', '', 'en', '', '', '', '', '', 'Theso_test', '');


--
-- TOC entry 4442 (class 0 OID 105511)
-- Dependencies: 311
-- Data for Name: thesohomepage; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.thesohomepage (htmlcode, lang, idtheso) VALUES ('<p>Page à modifier pour décrire le thésaurus</p>', 'fr', 'th2');


--
-- TOC entry 4445 (class 0 OID 105518)
-- Dependencies: 314
-- Data for Name: user_group_label; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4446 (class 0 OID 105524)
-- Dependencies: 315
-- Data for Name: user_group_thesaurus; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4447 (class 0 OID 105529)
-- Dependencies: 316
-- Data for Name: user_role_group; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4448 (class 0 OID 105532)
-- Dependencies: 317
-- Data for Name: user_role_only_on; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4449 (class 0 OID 105537)
-- Dependencies: 318
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: opentheso
--

INSERT INTO public.users (id_user, username, password, active, mail, passtomodify, alertmail, issuperadmin, apikey) VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', true, 'admin@domaine.fr', false, false, true, NULL);


--
-- TOC entry 4450 (class 0 OID 105547)
-- Dependencies: 319
-- Data for Name: users_historique; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4451 (class 0 OID 105554)
-- Dependencies: 320
-- Data for Name: version_history; Type: TABLE DATA; Schema: public; Owner: opentheso
--



--
-- TOC entry 4463 (class 0 OID 0)
-- Dependencies: 211
-- Name: alignement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.alignement_id_seq', 3, true);


--
-- TOC entry 4464 (class 0 OID 0)
-- Dependencies: 213
-- Name: alignement_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.alignement_preferences_id_seq', 1, false);


--
-- TOC entry 4465 (class 0 OID 0)
-- Dependencies: 215
-- Name: alignement_source__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.alignement_source__id_seq', 196, true);


--
-- TOC entry 4466 (class 0 OID 0)
-- Dependencies: 219
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.candidat_messages_id_message_seq', 25, true);


--
-- TOC entry 4467 (class 0 OID 0)
-- Dependencies: 223
-- Name: candidat_vote_id_vote_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.candidat_vote_id_vote_seq', 19, true);


--
-- TOC entry 4468 (class 0 OID 0)
-- Dependencies: 225
-- Name: concept__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.concept__id_seq', 14, true);


--
-- TOC entry 4469 (class 0 OID 0)
-- Dependencies: 227
-- Name: concept_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.concept_candidat__id_seq', 1, false);


--
-- TOC entry 4470 (class 0 OID 0)
-- Dependencies: 231
-- Name: concept_group__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.concept_group__id_seq', 3, true);


--
-- TOC entry 4471 (class 0 OID 0)
-- Dependencies: 234
-- Name: concept_group_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.concept_group_historique__id_seq', 1, false);


--
-- TOC entry 4472 (class 0 OID 0)
-- Dependencies: 238
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.concept_group_label_historique__id_seq', 3, true);


--
-- TOC entry 4473 (class 0 OID 0)
-- Dependencies: 236
-- Name: concept_group_label_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.concept_group_label_id_seq', 3, true);


--
-- TOC entry 4474 (class 0 OID 0)
-- Dependencies: 241
-- Name: concept_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.concept_historique__id_seq', 15, true);


--
-- TOC entry 4475 (class 0 OID 0)
-- Dependencies: 253
-- Name: external_images_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.external_images_id_seq', 16, true);


--
-- TOC entry 4476 (class 0 OID 0)
-- Dependencies: 255
-- Name: facet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.facet_id_seq', 1, false);


--
-- TOC entry 4477 (class 0 OID 0)
-- Dependencies: 257
-- Name: gps_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.gps_id_seq', 60, true);


--
-- TOC entry 4478 (class 0 OID 0)
-- Dependencies: 258
-- Name: gps_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.gps_preferences_id_seq', 1, false);


--
-- TOC entry 4479 (class 0 OID 0)
-- Dependencies: 263
-- Name: languages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.languages_id_seq', 193, true);


--
-- TOC entry 4480 (class 0 OID 0)
-- Dependencies: 269
-- Name: non_preferred_term_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.non_preferred_term_id_seq', 34865, true);


--
-- TOC entry 4481 (class 0 OID 0)
-- Dependencies: 270
-- Name: note__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.note__id_seq', 4, true);


--
-- TOC entry 4482 (class 0 OID 0)
-- Dependencies: 272
-- Name: note_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.note_historique__id_seq', 4, true);


--
-- TOC entry 4483 (class 0 OID 0)
-- Dependencies: 277
-- Name: pref__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.pref__id_seq', 1, true);


--
-- TOC entry 4484 (class 0 OID 0)
-- Dependencies: 282
-- Name: project_description_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.project_description_id_seq', 1, false);


--
-- TOC entry 4485 (class 0 OID 0)
-- Dependencies: 286
-- Name: proposition_modification_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.proposition_modification_detail_id_seq', 3, true);


--
-- TOC entry 4486 (class 0 OID 0)
-- Dependencies: 287
-- Name: proposition_modification_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.proposition_modification_id_seq', 2, true);


--
-- TOC entry 4487 (class 0 OID 0)
-- Dependencies: 290
-- Name: releases_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.releases_id_seq', 32, true);


--
-- TOC entry 4488 (class 0 OID 0)
-- Dependencies: 292
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.role_id_seq', 6, true);


--
-- TOC entry 4489 (class 0 OID 0)
-- Dependencies: 296
-- Name: status_id_status_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.status_id_status_seq', 1, false);


--
-- TOC entry 4490 (class 0 OID 0)
-- Dependencies: 297
-- Name: status_id_status_seq1; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.status_id_status_seq1', 1, false);


--
-- TOC entry 4491 (class 0 OID 0)
-- Dependencies: 298
-- Name: term__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.term__id_seq', 16, true);


--
-- TOC entry 4492 (class 0 OID 0)
-- Dependencies: 300
-- Name: term_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.term_candidat__id_seq', 1, false);


--
-- TOC entry 4493 (class 0 OID 0)
-- Dependencies: 302
-- Name: term_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.term_historique__id_seq', 16, true);


--
-- TOC entry 4494 (class 0 OID 0)
-- Dependencies: 265
-- Name: thesaurus_array_facet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.thesaurus_array_facet_id_seq', 4, true);


--
-- TOC entry 4495 (class 0 OID 0)
-- Dependencies: 309
-- Name: thesaurus_dcterms_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.thesaurus_dcterms_id_seq', 146, true);


--
-- TOC entry 4496 (class 0 OID 0)
-- Dependencies: 304
-- Name: thesaurus_id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.thesaurus_id_seq', 1, true);


--
-- TOC entry 4497 (class 0 OID 0)
-- Dependencies: 312
-- Name: user__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.user__id_seq', 2, false);


--
-- TOC entry 4498 (class 0 OID 0)
-- Dependencies: 313
-- Name: user_group_label__id_seq; Type: SEQUENCE SET; Schema: public; Owner: opentheso
--

SELECT pg_catalog.setval('public.user_group_label__id_seq', 1, false);


--
-- TOC entry 4202 (class 2606 OID 105564)
-- Name: version_history VersionHistory_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.version_history
    ADD CONSTRAINT "VersionHistory_pkey" PRIMARY KEY ("idVersionhistory");


--
-- TOC entry 4009 (class 2606 OID 105566)
-- Name: alignement alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.alignement
    ADD CONSTRAINT alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key UNIQUE (internal_id_concept, internal_id_thesaurus, uri_target);


--
-- TOC entry 4011 (class 2606 OID 105568)
-- Name: alignement alignement_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.alignement
    ADD CONSTRAINT alignement_pkey PRIMARY KEY (id);


--
-- TOC entry 4013 (class 2606 OID 105570)
-- Name: alignement_preferences alignement_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.alignement_preferences
    ADD CONSTRAINT alignement_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_concept_depart, id_alignement_source);


--
-- TOC entry 4015 (class 2606 OID 105572)
-- Name: alignement_source alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.alignement_source
    ADD CONSTRAINT alignement_source_pkey PRIMARY KEY (id);


--
-- TOC entry 4017 (class 2606 OID 105574)
-- Name: alignement_source alignement_source_source_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.alignement_source
    ADD CONSTRAINT alignement_source_source_key UNIQUE (source);


--
-- TOC entry 4019 (class 2606 OID 105576)
-- Name: alignement_type alignment_type_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.alignement_type
    ADD CONSTRAINT alignment_type_pkey PRIMARY KEY (id);


--
-- TOC entry 4021 (class 2606 OID 105578)
-- Name: bt_type bt_type_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.bt_type
    ADD CONSTRAINT bt_type_pkey PRIMARY KEY (id);


--
-- TOC entry 4023 (class 2606 OID 105580)
-- Name: bt_type bt_type_relation_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.bt_type
    ADD CONSTRAINT bt_type_relation_key UNIQUE (relation);


--
-- TOC entry 4025 (class 2606 OID 105582)
-- Name: candidat_messages candidat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.candidat_messages
    ADD CONSTRAINT candidat_messages_pkey PRIMARY KEY (id_message);


--
-- TOC entry 4027 (class 2606 OID 105584)
-- Name: candidat_status candidat_status_id_concept_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.candidat_status
    ADD CONSTRAINT candidat_status_id_concept_id_thesaurus_key UNIQUE (id_concept, id_thesaurus);


--
-- TOC entry 4029 (class 2606 OID 105586)
-- Name: candidat_vote candidat_vote_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.candidat_vote
    ADD CONSTRAINT candidat_vote_pkey PRIMARY KEY (id_vote);


--
-- TOC entry 4031 (class 2606 OID 105588)
-- Name: compound_equivalence compound_equivalence_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.compound_equivalence
    ADD CONSTRAINT compound_equivalence_pkey PRIMARY KEY (id_split_nonpreferredterm, id_preferredterm);


--
-- TOC entry 4037 (class 2606 OID 105590)
-- Name: concept_candidat concept_candidat_id_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_candidat
    ADD CONSTRAINT concept_candidat_id_key UNIQUE (id);


--
-- TOC entry 4039 (class 2606 OID 105592)
-- Name: concept_candidat concept_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_candidat
    ADD CONSTRAINT concept_candidat_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 4061 (class 2606 OID 105594)
-- Name: concept_historique concept_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_historique
    ADD CONSTRAINT concept_copy_pkey PRIMARY KEY (id_concept, id_thesaurus, id_group, id_user, modified);


--
-- TOC entry 4041 (class 2606 OID 105596)
-- Name: concept_dcterms concept_dcterms_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_dcterms
    ADD CONSTRAINT concept_dcterms_pkey PRIMARY KEY (id_concept, id_thesaurus, name, value);


--
-- TOC entry 4043 (class 2606 OID 105598)
-- Name: concept_facet concept_facettes_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_facet
    ADD CONSTRAINT concept_facettes_pkey PRIMARY KEY (id_facet, id_thesaurus, id_concept);


--
-- TOC entry 4063 (class 2606 OID 105600)
-- Name: concept_replacedby concept_fusion_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_replacedby
    ADD CONSTRAINT concept_fusion_pkey PRIMARY KEY (id_concept1, id_concept2, id_thesaurus);


--
-- TOC entry 4048 (class 2606 OID 105602)
-- Name: concept_group_concept concept_group_concept_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_group_concept
    ADD CONSTRAINT concept_group_concept_pkey PRIMARY KEY (idgroup, idthesaurus, idconcept);


--
-- TOC entry 4050 (class 2606 OID 105604)
-- Name: concept_group_historique concept_group_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_group_historique
    ADD CONSTRAINT concept_group_copy_pkey PRIMARY KEY (idgroup, idthesaurus, modified, id_user);


--
-- TOC entry 4057 (class 2606 OID 105606)
-- Name: concept_group_label_historique concept_group_label_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_group_label_historique
    ADD CONSTRAINT concept_group_label_copy_pkey PRIMARY KEY (lang, idthesaurus, lexicalvalue, modified, id_user);


--
-- TOC entry 4052 (class 2606 OID 105608)
-- Name: concept_group_label concept_group_label_idgrouplabel_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_group_label
    ADD CONSTRAINT concept_group_label_idgrouplabel_key UNIQUE (id);


--
-- TOC entry 4055 (class 2606 OID 105610)
-- Name: concept_group_label concept_group_label_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_group_label
    ADD CONSTRAINT concept_group_label_pkey PRIMARY KEY (id);


--
-- TOC entry 4046 (class 2606 OID 105612)
-- Name: concept_group concept_group_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_group
    ADD CONSTRAINT concept_group_pkey PRIMARY KEY (idgroup, idthesaurus);


--
-- TOC entry 4059 (class 2606 OID 105614)
-- Name: concept_group_type concept_group_type_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_group_type
    ADD CONSTRAINT concept_group_type_pkey PRIMARY KEY (code, label);


--
-- TOC entry 4035 (class 2606 OID 105616)
-- Name: concept concept_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept
    ADD CONSTRAINT concept_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 4065 (class 2606 OID 105618)
-- Name: concept_term_candidat concept_term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_term_candidat
    ADD CONSTRAINT concept_term_candidat_pkey PRIMARY KEY (id_concept, id_term, id_thesaurus);


--
-- TOC entry 4067 (class 2606 OID 105620)
-- Name: concept_type concept_type_theso; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.concept_type
    ADD CONSTRAINT concept_type_theso PRIMARY KEY (code, id_theso);


--
-- TOC entry 4069 (class 2606 OID 105622)
-- Name: copyright copyright_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.copyright
    ADD CONSTRAINT copyright_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 4071 (class 2606 OID 105624)
-- Name: corpus_link corpus_link_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.corpus_link
    ADD CONSTRAINT corpus_link_pkey PRIMARY KEY (id_theso, corpus_name);


--
-- TOC entry 4073 (class 2606 OID 105626)
-- Name: custom_concept_attribute custom_concept_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.custom_concept_attribute
    ADD CONSTRAINT custom_concept_attribute_pkey PRIMARY KEY ("idConcept");


--
-- TOC entry 4075 (class 2606 OID 105628)
-- Name: custom_term_attribute custom_term_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.custom_term_attribute
    ADD CONSTRAINT custom_term_attribute_pkey PRIMARY KEY (identifier);


--
-- TOC entry 4077 (class 2606 OID 105630)
-- Name: databasechangeloglock databasechangeloglock_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.databasechangeloglock
    ADD CONSTRAINT databasechangeloglock_pkey PRIMARY KEY (id);


--
-- TOC entry 4079 (class 2606 OID 105632)
-- Name: external_images external_images_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.external_images
    ADD CONSTRAINT external_images_pkey PRIMARY KEY (id);


--
-- TOC entry 4081 (class 2606 OID 105634)
-- Name: external_images external_images_unique; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.external_images
    ADD CONSTRAINT external_images_unique UNIQUE (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 4083 (class 2606 OID 105636)
-- Name: external_resources external_resources_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.external_resources
    ADD CONSTRAINT external_resources_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 4085 (class 2606 OID 105638)
-- Name: gps gps_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.gps
    ADD CONSTRAINT gps_pkey PRIMARY KEY (id);


--
-- TOC entry 4089 (class 2606 OID 105640)
-- Name: hierarchical_relationship_historique hierarchical_relationship_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.hierarchical_relationship_historique
    ADD CONSTRAINT hierarchical_relationship_copy_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2, modified, id_user);


--
-- TOC entry 4087 (class 2606 OID 105642)
-- Name: hierarchical_relationship hierarchical_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.hierarchical_relationship
    ADD CONSTRAINT hierarchical_relationship_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2);


--
-- TOC entry 4091 (class 2606 OID 105644)
-- Name: homepage homepage_lang_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.homepage
    ADD CONSTRAINT homepage_lang_key UNIQUE (lang);


--
-- TOC entry 4093 (class 2606 OID 105646)
-- Name: languages_iso639 languages_iso639_iso639_1_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_iso639_1_key UNIQUE (iso639_1);


--
-- TOC entry 4095 (class 2606 OID 105648)
-- Name: languages_iso639 languages_iso639_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_pkey PRIMARY KEY (id);


--
-- TOC entry 4098 (class 2606 OID 105650)
-- Name: non_preferred_term non_prefered_term_unique; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.non_preferred_term
    ADD CONSTRAINT non_prefered_term_unique UNIQUE (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 4103 (class 2606 OID 105652)
-- Name: non_preferred_term_historique non_preferred_term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.non_preferred_term_historique
    ADD CONSTRAINT non_preferred_term_copy_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, modified, id_user);


--
-- TOC entry 4100 (class 2606 OID 105654)
-- Name: non_preferred_term non_preferred_term_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.non_preferred_term
    ADD CONSTRAINT non_preferred_term_pkey PRIMARY KEY (id);


--
-- TOC entry 4114 (class 2606 OID 105656)
-- Name: note_historique note_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.note_historique
    ADD CONSTRAINT note_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 4112 (class 2606 OID 105658)
-- Name: note note_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_pkey PRIMARY KEY (id);


--
-- TOC entry 4118 (class 2606 OID 105660)
-- Name: nt_type nt_type_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_pkey PRIMARY KEY (id);


--
-- TOC entry 4120 (class 2606 OID 105662)
-- Name: nt_type nt_type_relation_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_relation_key UNIQUE (relation);


--
-- TOC entry 4123 (class 2606 OID 105664)
-- Name: permuted permuted_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.permuted
    ADD CONSTRAINT permuted_pkey PRIMARY KEY (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm);


--
-- TOC entry 4116 (class 2606 OID 105666)
-- Name: note_type pk_note_type; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.note_type
    ADD CONSTRAINT pk_note_type PRIMARY KEY (code);


--
-- TOC entry 4144 (class 2606 OID 105668)
-- Name: relation_group pk_relation_group; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.relation_group
    ADD CONSTRAINT pk_relation_group PRIMARY KEY (id_group1, id_thesaurus, relation, id_group2);


--
-- TOC entry 4125 (class 2606 OID 105670)
-- Name: preferences preferences_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 4127 (class 2606 OID 105672)
-- Name: preferences preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_pkey PRIMARY KEY (id_pref);


--
-- TOC entry 4129 (class 2606 OID 105674)
-- Name: preferences preferences_preferredname_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_preferredname_key UNIQUE (preferredname);


--
-- TOC entry 4131 (class 2606 OID 105676)
-- Name: preferences_sparql preferences_sparql_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.preferences_sparql
    ADD CONSTRAINT preferences_sparql_pkey PRIMARY KEY (thesaurus);


--
-- TOC entry 4134 (class 2606 OID 105678)
-- Name: preferred_term preferred_term_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.preferred_term
    ADD CONSTRAINT preferred_term_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 4136 (class 2606 OID 105680)
-- Name: project_description project_description_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.project_description
    ADD CONSTRAINT project_description_pkey PRIMARY KEY (id);


--
-- TOC entry 4142 (class 2606 OID 105682)
-- Name: proposition_modification_detail proposition_modification_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.proposition_modification_detail
    ADD CONSTRAINT proposition_modification_detail_pkey PRIMARY KEY (id);


--
-- TOC entry 4140 (class 2606 OID 105684)
-- Name: proposition_modification proposition_modification_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.proposition_modification
    ADD CONSTRAINT proposition_modification_pkey PRIMARY KEY (id);


--
-- TOC entry 4138 (class 2606 OID 105686)
-- Name: proposition proposition_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.proposition
    ADD CONSTRAINT proposition_pkey PRIMARY KEY (id_concept, id_user, id_thesaurus);


--
-- TOC entry 4146 (class 2606 OID 105688)
-- Name: releases releases_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.releases
    ADD CONSTRAINT releases_pkey PRIMARY KEY (id);


--
-- TOC entry 4148 (class 2606 OID 105690)
-- Name: roles role_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- TOC entry 4150 (class 2606 OID 105692)
-- Name: routine_mail routine_mail_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.routine_mail
    ADD CONSTRAINT routine_mail_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 4152 (class 2606 OID 105694)
-- Name: status status_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_pkey PRIMARY KEY (id_status);


--
-- TOC entry 4163 (class 2606 OID 105696)
-- Name: term_candidat term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.term_candidat
    ADD CONSTRAINT term_candidat_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, contributor);


--
-- TOC entry 4166 (class 2606 OID 105698)
-- Name: term_historique term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.term_historique
    ADD CONSTRAINT term_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 4155 (class 2606 OID 105700)
-- Name: term term_id_term_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_key UNIQUE (id_term, lang, id_thesaurus);


--
-- TOC entry 4157 (class 2606 OID 105702)
-- Name: term term_id_term_lexical_value_lang_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_lexical_value_lang_id_thesaurus_key UNIQUE (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 4160 (class 2606 OID 105704)
-- Name: term term_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_pkey PRIMARY KEY (id);


--
-- TOC entry 4170 (class 2606 OID 105706)
-- Name: thesaurus_alignement_source thesaurus_alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.thesaurus_alignement_source
    ADD CONSTRAINT thesaurus_alignement_source_pkey PRIMARY KEY (id_thesaurus, id_alignement_source);


--
-- TOC entry 4172 (class 2606 OID 105708)
-- Name: thesaurus_array thesaurus_array_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.thesaurus_array
    ADD CONSTRAINT thesaurus_array_pkey PRIMARY KEY (id_facet, id_thesaurus, id_concept_parent);


--
-- TOC entry 4174 (class 2606 OID 105710)
-- Name: thesaurus_dcterms thesaurus_dcterms_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.thesaurus_dcterms
    ADD CONSTRAINT thesaurus_dcterms_pkey PRIMARY KEY (id);


--
-- TOC entry 4176 (class 2606 OID 105712)
-- Name: thesaurus_dcterms thesaurus_dcterms_uniquekey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.thesaurus_dcterms
    ADD CONSTRAINT thesaurus_dcterms_uniquekey UNIQUE (id_thesaurus, name, value);


--
-- TOC entry 4178 (class 2606 OID 105714)
-- Name: thesaurus_label thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT thesaurus_pkey PRIMARY KEY (id_thesaurus, lang, title);


--
-- TOC entry 4168 (class 2606 OID 105716)
-- Name: thesaurus thesaurus_pkey1; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.thesaurus
    ADD CONSTRAINT thesaurus_pkey1 PRIMARY KEY (id_thesaurus, id_ark);


--
-- TOC entry 4182 (class 2606 OID 105718)
-- Name: thesohomepage thesohomepage_idtheso_lang_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.thesohomepage
    ADD CONSTRAINT thesohomepage_idtheso_lang_key UNIQUE (idtheso, lang);


--
-- TOC entry 4180 (class 2606 OID 105720)
-- Name: thesaurus_label unique_thesau_lang; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT unique_thesau_lang UNIQUE (id_thesaurus, lang);


--
-- TOC entry 4184 (class 2606 OID 105722)
-- Name: user_group_label user_group-label_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.user_group_label
    ADD CONSTRAINT "user_group-label_pkey" PRIMARY KEY (id_group);


--
-- TOC entry 4190 (class 2606 OID 105724)
-- Name: user_role_group user_group_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.user_role_group
    ADD CONSTRAINT user_group_pkey UNIQUE (id_user, id_group);


--
-- TOC entry 4186 (class 2606 OID 105726)
-- Name: user_group_thesaurus user_group_thesaurus_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 4188 (class 2606 OID 105728)
-- Name: user_group_thesaurus user_group_thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_pkey PRIMARY KEY (id_group, id_thesaurus);


--
-- TOC entry 4194 (class 2606 OID 105730)
-- Name: users user_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT user_pkey PRIMARY KEY (id_user);


--
-- TOC entry 4192 (class 2606 OID 105732)
-- Name: user_role_only_on user_role_only_on_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.user_role_only_on
    ADD CONSTRAINT user_role_only_on_pkey PRIMARY KEY (id_user, id_role, id_theso);


--
-- TOC entry 4200 (class 2606 OID 105734)
-- Name: users_historique users_historique_pkey; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.users_historique
    ADD CONSTRAINT users_historique_pkey PRIMARY KEY (id_user);


--
-- TOC entry 4196 (class 2606 OID 105736)
-- Name: users users_mail_key1; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_mail_key1 UNIQUE (mail);


--
-- TOC entry 4198 (class 2606 OID 105738)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: opentheso
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 4044 (class 1259 OID 105739)
-- Name: concept_group_id_ark_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX concept_group_id_ark_idx ON public.concept_group USING btree (id_ark);


--
-- TOC entry 4053 (class 1259 OID 105740)
-- Name: concept_group_label_lexicalvalue_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX concept_group_label_lexicalvalue_idx ON public.concept_group_label USING btree (lexicalvalue);


--
-- TOC entry 4032 (class 1259 OID 105741)
-- Name: concept_id_ark_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX concept_id_ark_idx ON public.concept USING btree (id_ark);


--
-- TOC entry 4033 (class 1259 OID 105742)
-- Name: concept_notation_unaccent; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX concept_notation_unaccent ON public.concept USING gin (public.f_unaccent(lower((notation)::text)) public.gin_trgm_ops);


--
-- TOC entry 4153 (class 1259 OID 105743)
-- Name: index_lexical_value; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX index_lexical_value ON public.term USING btree (lexical_value);


--
-- TOC entry 4164 (class 1259 OID 105744)
-- Name: index_lexical_value_copy; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX index_lexical_value_copy ON public.term_historique USING btree (lexical_value);


--
-- TOC entry 4096 (class 1259 OID 105745)
-- Name: index_lexical_value_npt; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX index_lexical_value_npt ON public.non_preferred_term USING btree (lexical_value);


--
-- TOC entry 4104 (class 1259 OID 105746)
-- Name: note_id_concept_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX note_id_concept_idx ON public.note USING btree (id_concept) WITH (deduplicate_items='true');


--
-- TOC entry 4105 (class 1259 OID 105747)
-- Name: note_id_term_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX note_id_term_idx ON public.note USING btree (id_term) WITH (deduplicate_items='true');


--
-- TOC entry 4106 (class 1259 OID 105748)
-- Name: note_id_thesaurus_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX note_id_thesaurus_idx ON public.note USING btree (id_thesaurus) WITH (deduplicate_items='true');


--
-- TOC entry 4107 (class 1259 OID 105749)
-- Name: note_identifier_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX note_identifier_idx ON public.note USING btree (identifier);


--
-- TOC entry 4108 (class 1259 OID 105750)
-- Name: note_lang_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX note_lang_idx ON public.note USING btree (lang) WITH (deduplicate_items='true');


--
-- TOC entry 4109 (class 1259 OID 105751)
-- Name: note_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX note_lexical_value_unaccent ON public.note USING gin (public.f_unaccent(lower((lexicalvalue)::text)) public.gin_trgm_ops);


--
-- TOC entry 4110 (class 1259 OID 105752)
-- Name: note_notetypecode_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX note_notetypecode_idx ON public.note USING btree (notetypecode bpchar_pattern_ops) WITH (deduplicate_items='true');


--
-- TOC entry 4121 (class 1259 OID 105753)
-- Name: permuted_lexical_value_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX permuted_lexical_value_idx ON public.permuted USING btree (lexical_value);


--
-- TOC entry 4132 (class 1259 OID 105754)
-- Name: preferred_term_id_term_idx; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX preferred_term_id_term_idx ON public.preferred_term USING btree (id_term);


--
-- TOC entry 4101 (class 1259 OID 105755)
-- Name: term_lexical_value_npt_unaccent; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX term_lexical_value_npt_unaccent ON public.non_preferred_term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 4158 (class 1259 OID 105756)
-- Name: term_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX term_lexical_value_unaccent ON public.term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 4161 (class 1259 OID 105757)
-- Name: terms_values_gin; Type: INDEX; Schema: public; Owner: opentheso
--

CREATE INDEX terms_values_gin ON public.term USING gin (lexical_value public.gin_trgm_ops);


--
-- TOC entry 4457 (class 0 OID 0)
-- Dependencies: 6
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2024-02-14 11:31:14 CET

--
-- PostgreSQL database dump complete
--

