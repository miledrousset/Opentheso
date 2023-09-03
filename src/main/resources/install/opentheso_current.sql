--
-- PostgreSQL database dump
--

-- Dumped from database version 14.2
-- Dumped by pg_dump version 14.4

-- Started on 2022-10-17 13:10:22 CEST


SET role = opentheso;
SET schema 'public';

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
-- TOC entry 2 (class 3079 OID 28514)
-- Name: pg_trgm; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;


--
-- TOC entry 4389 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pg_trgm; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pg_trgm IS 'text similarity measurement and index searching based on trigrams';


--
-- TOC entry 3 (class 3079 OID 28595)
-- Name: unaccent; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;


--
-- TOC entry 4390 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION unaccent; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION unaccent IS 'text search dictionary that removes accents';


--
-- TOC entry 980 (class 1247 OID 28603)
-- Name: alignement_format; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.alignement_format AS ENUM (
    'skos',
    'json',
    'xml'
);


--
-- TOC entry 983 (class 1247 OID 28610)
-- Name: alignement_type_rqt; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.alignement_type_rqt AS ENUM (
    'SPARQL',
    'REST'
);


--
-- TOC entry 986 (class 1247 OID 28616)
-- Name: auth_method; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.auth_method AS ENUM (
    'DB',
    'LDAP',
    'FILE',
    'test'
);


--
-- TOC entry 347 (class 1255 OID 28625)
-- Name: f_unaccent(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.f_unaccent(text) RETURNS text
    LANGUAGE sql IMMUTABLE
    AS $_$
SELECT public.unaccent('public.unaccent', $1)
$_$;


--
-- TOC entry 361 (class 1255 OID 29329)
-- Name: naturalsort(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.naturalsort(text) RETURNS bytea
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$
    select string_agg(convert_to(coalesce(r[2], length(length(r[1])::text) || length(r[1])::text || r[1]), 'SQL_ASCII'),'\x00')
    from regexp_matches($1, '0*([0-9]+)|([^0-9]+)', 'g') r;
$_$;


--
-- TOC entry 372 (class 1255 OID 29380)
-- Name: opentheso_get_alignements(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 363 (class 1255 OID 29371)
-- Name: opentheso_get_alter_term(character varying, character varying, boolean); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 375 (class 1255 OID 29384)
-- Name: opentheso_get_concepts(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.opentheso_get_concepts(id_theso character varying, path character varying) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
DECLARE

	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@';

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
					|| '@' || relation_rec.relationship_role || '@' || relation_rec.relationship_id_concept || '@' ;

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
			img = img || img_rec.url || seperateur;
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

		SELECT 	uri, con.status, local_URI, con.id_concept, con.id_ark, prefLab, altLab, altLab_hiden, definition, example,
				editorialNote, changeNote, secopeNote, note, historyNote, con.notation, narrower, broader, related, exactMatch, closeMatch,
				broadMatch, relatedMatch, narrowMatch, gpsData, membre, con.created, con.modified,
				img, creator, contributor, replaces, replacedBy, facets INTO rec;

  		RETURN NEXT rec;
    END LOOP;
END;
$$;


--
-- TOC entry 374 (class 1255 OID 29382)
-- Name: opentheso_get_concepts_by_group(character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.opentheso_get_concepts_by_group(id_theso character varying, path character varying, id_group character varying) RETURNS SETOF record
    LANGUAGE plpgsql
    AS $$
DECLARE

	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@';

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
					|| '@' || relation_rec.relationship_role || '@' || relation_rec.relationship_id_concept || '@' ;

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
			img = img || img_rec.url || seperateur;
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

		SELECT 	uri, con.status, local_URI, con.id_concept, con.id_ark, prefLab, altLab, altLab_hiden, definition, example,
				editorialNote, changeNote, secopeNote, note, historyNote, con.notation, narrower, broader, related, exactMatch, closeMatch,
				broadMatch, relatedMatch, narrowMatch, gpsData, membre, con.created, con.modified, img,
				creator, contributor, replaces, replacedBy, facets INTO rec;

  		RETURN NEXT rec;
    END LOOP;
END;
$$;


--
-- TOC entry 373 (class 1255 OID 29381)
-- Name: opentheso_get_facettes(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.opentheso_get_facettes(id_theso character varying, path character varying) RETURNS SETOF record
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


--
-- TOC entry 364 (class 1255 OID 29372)
-- Name: opentheso_get_gps(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.opentheso_get_gps(id_thesorus character varying, id_con character varying) RETURNS TABLE(gps_latitude double precision, gps_longitude double precision)
    LANGUAGE plpgsql
    AS $$
begin
	return query
		SELECT latitude, longitude
		FROM gps
		WHERE id_theso = id_thesorus
		AND id_concept = id_con;

end;
$$;


--
-- TOC entry 365 (class 1255 OID 29373)
-- Name: opentheso_get_groups(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 366 (class 1255 OID 29374)
-- Name: opentheso_get_images(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 367 (class 1255 OID 29375)
-- Name: opentheso_get_note_concept(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 368 (class 1255 OID 29376)
-- Name: opentheso_get_note_term(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 369 (class 1255 OID 29377)
-- Name: opentheso_get_relations(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 370 (class 1255 OID 29378)
-- Name: opentheso_get_traductions(character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.opentheso_get_traductions(id_theso character varying, id_con character varying) RETURNS TABLE(term_id character varying, term_lexical_value character varying, term_lang character varying)
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


--
-- TOC entry 371 (class 1255 OID 29379)
-- Name: opentheso_get_uri(boolean, character varying, character varying, boolean, character varying, boolean, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 348 (class 1255 OID 28626)
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
-- TOC entry 362 (class 1255 OID 29338)
-- Name: update_table_preferences_displayusername(); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 360 (class 1255 OID 29328)
-- Name: update_table_preferences_useconcepttree(); Type: FUNCTION; Schema: public; Owner: -
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


--
-- TOC entry 211 (class 1259 OID 28627)
-- Name: alignement_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alignement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 212 (class 1259 OID 28628)
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
-- TOC entry 213 (class 1259 OID 28636)
-- Name: alignement_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alignement_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 214 (class 1259 OID 28637)
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
-- TOC entry 215 (class 1259 OID 28643)
-- Name: alignement_source__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.alignement_source__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 216 (class 1259 OID 28644)
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
-- TOC entry 217 (class 1259 OID 28652)
-- Name: alignement_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alignement_type (
    id integer NOT NULL,
    label text NOT NULL,
    isocode text NOT NULL,
    label_skos character varying
);


--
-- TOC entry 218 (class 1259 OID 28657)
-- Name: bt_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bt_type (
    id integer NOT NULL,
    relation character varying,
    description_fr character varying,
    description_en character varying
);


--
-- TOC entry 219 (class 1259 OID 28662)
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.candidat_messages_id_message_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 220 (class 1259 OID 28663)
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
-- TOC entry 221 (class 1259 OID 28669)
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
-- TOC entry 222 (class 1259 OID 28675)
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
-- TOC entry 223 (class 1259 OID 28680)
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
-- TOC entry 4391 (class 0 OID 0)
-- Dependencies: 223
-- Name: candidat_vote_id_vote_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.candidat_vote_id_vote_seq OWNED BY public.candidat_vote.id_vote;


--
-- TOC entry 224 (class 1259 OID 28681)
-- Name: compound_equivalence; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.compound_equivalence (
    id_split_nonpreferredterm text NOT NULL,
    id_preferredterm text NOT NULL
);


--
-- TOC entry 225 (class 1259 OID 28686)
-- Name: concept__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept__id_seq
    START WITH 43
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 226 (class 1259 OID 28687)
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
    id_doi character varying DEFAULT ''::character varying,
    creator integer DEFAULT '-1'::integer,
    contributor integer DEFAULT '-1'::integer,
    concept_type text DEFAULT 'concept'::text
);


--
-- TOC entry 227 (class 1259 OID 28702)
-- Name: concept_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 228 (class 1259 OID 28703)
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
-- TOC entry 229 (class 1259 OID 28712)
-- Name: concept_facet; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_facet (
    id_facet character varying NOT NULL,
    id_thesaurus text NOT NULL,
    id_concept text NOT NULL
);


--
-- TOC entry 230 (class 1259 OID 28717)
-- Name: concept_group__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 231 (class 1259 OID 28718)
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
-- TOC entry 232 (class 1259 OID 28727)
-- Name: concept_group_concept; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group_concept (
    idgroup text NOT NULL,
    idthesaurus text NOT NULL,
    idconcept text NOT NULL
);


--
-- TOC entry 233 (class 1259 OID 28732)
-- Name: concept_group_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 234 (class 1259 OID 28733)
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
-- TOC entry 235 (class 1259 OID 28740)
-- Name: concept_group_label_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group_label_id_seq
    START WITH 60
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 236 (class 1259 OID 28741)
-- Name: concept_group_label; Type: TABLE; Schema: public; Owner: -
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


--
-- TOC entry 237 (class 1259 OID 28749)
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_group_label_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 238 (class 1259 OID 28750)
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
-- TOC entry 239 (class 1259 OID 28757)
-- Name: concept_group_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_group_type (
    code text NOT NULL,
    label text NOT NULL,
    skoslabel text
);


--
-- TOC entry 240 (class 1259 OID 28762)
-- Name: concept_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.concept_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 241 (class 1259 OID 28763)
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
-- TOC entry 242 (class 1259 OID 28771)
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
-- TOC entry 243 (class 1259 OID 28777)
-- Name: concept_term_candidat; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_term_candidat (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 306 (class 1259 OID 29330)
-- Name: concept_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.concept_type (
    code text NOT NULL,
    label_fr text NOT NULL,
    label_en text
);


--
-- TOC entry 244 (class 1259 OID 28782)
-- Name: copyright; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.copyright (
    id_thesaurus character varying NOT NULL,
    copyright character varying
);


--
-- TOC entry 245 (class 1259 OID 28787)
-- Name: corpus_link; Type: TABLE; Schema: public; Owner: -
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


--
-- TOC entry 246 (class 1259 OID 28794)
-- Name: custom_concept_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custom_concept_attribute (
    "idConcept" character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 247 (class 1259 OID 28799)
-- Name: custom_term_attribute; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custom_term_attribute (
    identifier character varying NOT NULL,
    "lexicalValue" character varying,
    "customAttributeType" character varying,
    lang character varying
);


--
-- TOC entry 248 (class 1259 OID 28804)
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
-- TOC entry 305 (class 1259 OID 29319)
-- Name: external_resources; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.external_resources (
    id_concept character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    description character varying,
    id_user integer,
    external_uri character varying DEFAULT ''::character varying NOT NULL
);


--
-- TOC entry 249 (class 1259 OID 28810)
-- Name: facet_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.facet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 250 (class 1259 OID 28811)
-- Name: gps; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gps (
    id_concept character varying NOT NULL,
    id_theso character varying NOT NULL,
    latitude double precision,
    longitude double precision
);


--
-- TOC entry 251 (class 1259 OID 28816)
-- Name: gps_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gps_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 252 (class 1259 OID 28817)
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
-- TOC entry 253 (class 1259 OID 28826)
-- Name: hierarchical_relationship; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.hierarchical_relationship (
    id_concept1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    role character varying NOT NULL,
    id_concept2 character varying NOT NULL
);


--
-- TOC entry 254 (class 1259 OID 28831)
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
-- TOC entry 255 (class 1259 OID 28837)
-- Name: homepage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.homepage (
    htmlcode character varying,
    lang character varying
);


--
-- TOC entry 256 (class 1259 OID 28842)
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
-- TOC entry 257 (class 1259 OID 28848)
-- Name: info; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.info (
    version_opentheso character varying NOT NULL,
    version_bdd character varying NOT NULL,
    googleanalytics character varying
);


--
-- TOC entry 258 (class 1259 OID 28853)
-- Name: languages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.languages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 311 (class 1259 OID 29361)
-- Name: languages_iso639; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.languages_iso639 (
    iso639_1 character varying,
    iso639_2 character varying,
    english_name character varying,
    french_name character varying,
    id integer DEFAULT nextval('public.languages_id_seq'::regclass) NOT NULL,
    code_pays character varying
);


--
-- TOC entry 259 (class 1259 OID 28860)
-- Name: thesaurus_array_facet_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.thesaurus_array_facet_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 260 (class 1259 OID 28861)
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
-- TOC entry 261 (class 1259 OID 28869)
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
-- TOC entry 262 (class 1259 OID 28877)
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
-- TOC entry 263 (class 1259 OID 28884)
-- Name: note__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 264 (class 1259 OID 28885)
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
-- TOC entry 265 (class 1259 OID 28893)
-- Name: note_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.note_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 266 (class 1259 OID 28894)
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
-- TOC entry 267 (class 1259 OID 28901)
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
-- TOC entry 268 (class 1259 OID 28907)
-- Name: nt_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.nt_type (
    id integer NOT NULL,
    relation character varying,
    description_fr character varying,
    description_en character varying
);


--
-- TOC entry 269 (class 1259 OID 28912)
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
-- TOC entry 270 (class 1259 OID 28917)
-- Name: pref__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.pref__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 271 (class 1259 OID 28918)
-- Name: preferences; Type: TABLE; Schema: public; Owner: -
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
    suggestion boolean DEFAULT false
);


--
-- TOC entry 272 (class 1259 OID 28962)
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
-- TOC entry 273 (class 1259 OID 28968)
-- Name: preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.preferred_term (
    id_concept character varying NOT NULL,
    id_term character varying NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 274 (class 1259 OID 28973)
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
-- TOC entry 308 (class 1259 OID 29341)
-- Name: proposition_modification; Type: TABLE; Schema: public; Owner: -
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
    date character varying
);


--
-- TOC entry 310 (class 1259 OID 29349)
-- Name: proposition_modification_detail; Type: TABLE; Schema: public; Owner: -
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


--
-- TOC entry 309 (class 1259 OID 29348)
-- Name: proposition_modification_detail_id_seq; Type: SEQUENCE; Schema: public; Owner: -
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
-- TOC entry 307 (class 1259 OID 29340)
-- Name: proposition_modification_id_seq; Type: SEQUENCE; Schema: public; Owner: -
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
-- TOC entry 275 (class 1259 OID 28980)
-- Name: relation_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.relation_group (
    id_group1 character varying NOT NULL,
    id_thesaurus character varying NOT NULL,
    relation character varying NOT NULL,
    id_group2 character varying NOT NULL
);


--
-- TOC entry 276 (class 1259 OID 28985)
-- Name: roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.roles (
    id integer NOT NULL,
    name character varying,
    description character varying
);


--
-- TOC entry 277 (class 1259 OID 28990)
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4392 (class 0 OID 0)
-- Dependencies: 277
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.role_id_seq OWNED BY public.roles.id;


--
-- TOC entry 278 (class 1259 OID 28991)
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
-- TOC entry 279 (class 1259 OID 28997)
-- Name: split_non_preferred_term; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.split_non_preferred_term (
);


--
-- TOC entry 280 (class 1259 OID 29000)
-- Name: status; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.status (
    id_status integer NOT NULL,
    value text
);


--
-- TOC entry 281 (class 1259 OID 29005)
-- Name: status_id_status_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.status_id_status_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 0
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 282 (class 1259 OID 29006)
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
-- TOC entry 4393 (class 0 OID 0)
-- Dependencies: 282
-- Name: status_id_status_seq1; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.status_id_status_seq1 OWNED BY public.status.id_status;


--
-- TOC entry 283 (class 1259 OID 29007)
-- Name: term__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 284 (class 1259 OID 29008)
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
-- TOC entry 285 (class 1259 OID 29017)
-- Name: term_candidat__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term_candidat__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 286 (class 1259 OID 29018)
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
-- TOC entry 287 (class 1259 OID 29026)
-- Name: term_historique__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.term_historique__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 288 (class 1259 OID 29027)
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
-- TOC entry 289 (class 1259 OID 29035)
-- Name: thesaurus_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.thesaurus_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 290 (class 1259 OID 29036)
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
-- TOC entry 291 (class 1259 OID 29045)
-- Name: thesaurus_alignement_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesaurus_alignement_source (
    id_thesaurus character varying NOT NULL,
    id_alignement_source integer NOT NULL
);


--
-- TOC entry 292 (class 1259 OID 29050)
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
-- TOC entry 293 (class 1259 OID 29056)
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
-- TOC entry 294 (class 1259 OID 29063)
-- Name: thesohomepage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.thesohomepage (
    htmlcode character varying,
    lang character varying,
    idtheso character varying
);


--
-- TOC entry 295 (class 1259 OID 29068)
-- Name: user__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 296 (class 1259 OID 29069)
-- Name: user_group_label__id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_group_label__id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 297 (class 1259 OID 29070)
-- Name: user_group_label; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_group_label (
    id_group integer DEFAULT nextval('public.user_group_label__id_seq'::regclass) NOT NULL,
    label_group character varying
);


--
-- TOC entry 298 (class 1259 OID 29076)
-- Name: user_group_thesaurus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_group_thesaurus (
    id_group integer NOT NULL,
    id_thesaurus character varying NOT NULL
);


--
-- TOC entry 299 (class 1259 OID 29081)
-- Name: user_role_group; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role_group (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_group integer NOT NULL
);


--
-- TOC entry 300 (class 1259 OID 29084)
-- Name: user_role_only_on; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_role_only_on (
    id_user integer NOT NULL,
    id_role integer NOT NULL,
    id_theso character varying NOT NULL,
    id_theso_domain character varying DEFAULT 'all'::character varying NOT NULL
);


--
-- TOC entry 301 (class 1259 OID 29090)
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
-- TOC entry 302 (class 1259 OID 29100)
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
-- TOC entry 303 (class 1259 OID 29108)
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
-- TOC entry 304 (class 1259 OID 29115)
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
-- TOC entry 3831 (class 2604 OID 29120)
-- Name: candidat_vote id_vote; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_vote ALTER COLUMN id_vote SET DEFAULT nextval('public.candidat_vote_id_vote_seq'::regclass);


--
-- TOC entry 3931 (class 2604 OID 29121)
-- Name: roles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);


--
-- TOC entry 3933 (class 2604 OID 29122)
-- Name: status id_status; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status ALTER COLUMN id_status SET DEFAULT nextval('public.status_id_status_seq1'::regclass);


--
-- TOC entry 4284 (class 0 OID 28628)
-- Dependencies: 212
-- Data for Name: alignement; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4286 (class 0 OID 28637)
-- Dependencies: 214
-- Data for Name: alignement_preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4288 (class 0 OID 28644)
-- Dependencies: 216
-- Data for Name: alignement_source; Type: TABLE DATA; Schema: public; Owner: -
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


--
-- TOC entry 4289 (class 0 OID 28652)
-- Dependencies: 217
-- Data for Name: alignement_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (1, 'Equivalence exacte', '=EQ', 'exactMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (2, 'Equivalence inexacte', '~EQ', 'closeMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (3, 'Equivalence générique', 'EQB', 'broadMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (4, 'Equivalence associative', 'EQR', 'relatedMatch');
INSERT INTO public.alignement_type (id, label, isocode, label_skos) VALUES (5, 'Equivalence spécifique', 'EQS', 'narrowMatch');


--
-- TOC entry 4290 (class 0 OID 28657)
-- Dependencies: 218
-- Data for Name: bt_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (1, 'BT', 'Terme générique', 'Broader term');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (2, 'BTG', 'Terme générique (generic)', 'Broader term (generic)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (3, 'BTP', 'Terme générique (partitive)', 'Broader term (partitive)');
INSERT INTO public.bt_type (id, relation, description_fr, description_en) VALUES (4, 'BTI', 'Terme générique (instance)', 'Broader term (instance)');


--
-- TOC entry 4292 (class 0 OID 28663)
-- Dependencies: 220
-- Data for Name: candidat_messages; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4293 (class 0 OID 28669)
-- Dependencies: 221
-- Data for Name: candidat_status; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4294 (class 0 OID 28675)
-- Dependencies: 222
-- Data for Name: candidat_vote; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4296 (class 0 OID 28681)
-- Dependencies: 224
-- Data for Name: compound_equivalence; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4298 (class 0 OID 28687)
-- Dependencies: 226
-- Data for Name: concept; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('2', 'th1', '', '2022-01-12 12:55:26.18484+01', '2022-10-17 00:00:00+02', 'D', 'N1', true, 2, false, '', '', 1, 1, 'concept');
INSERT INTO public.concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id, gps, id_handle, id_doi, creator, contributor, concept_type) VALUES ('3', 'th1', '', '2022-10-17 12:21:22.250461+02', '2022-10-17 12:21:22.250461+02', 'D', '', false, 3, false, '', '', 1, -1, 'concept');


--
-- TOC entry 4300 (class 0 OID 28703)
-- Dependencies: 228
-- Data for Name: concept_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4301 (class 0 OID 28712)
-- Dependencies: 229
-- Data for Name: concept_facet; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4303 (class 0 OID 28718)
-- Dependencies: 231
-- Data for Name: concept_group; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group (idgroup, id_ark, idthesaurus, idtypecode, notation, id, numerotation, id_handle, id_doi) VALUES ('G3', '', 'th1', 'C', '', 4, NULL, '', '');


--
-- TOC entry 4304 (class 0 OID 28727)
-- Dependencies: 232
-- Data for Name: concept_group_concept; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('G3', 'th1', '2');
INSERT INTO public.concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('G3', 'th1', '3');


--
-- TOC entry 4306 (class 0 OID 28733)
-- Dependencies: 234
-- Data for Name: concept_group_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4308 (class 0 OID 28741)
-- Dependencies: 236
-- Data for Name: concept_group_label; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_label (id, lexicalvalue, created, modified, lang, idthesaurus, idgroup) VALUES (2, 'collect1', '2022-01-12 00:00:00', '2022-01-12 00:00:00', 'en', 'th1', 'G3');
INSERT INTO public.concept_group_label (id, lexicalvalue, created, modified, lang, idthesaurus, idgroup) VALUES (3, 'collection1', '2022-10-17 00:00:00', '2022-10-17 00:00:00', 'fr', 'th1', 'G3');


--
-- TOC entry 4310 (class 0 OID 28750)
-- Dependencies: 238
-- Data for Name: concept_group_label_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (2, 'collect1', '2022-01-12 12:55:11.191981', 'en', 'th1', 'G3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (3, 'collection1', '2022-10-17 11:53:17.423963', 'fr', 'th1', 'G3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (4, 'collection1', '2022-10-17 11:53:39.689419', 'fr', 'th1', 'G3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (5, 'coll1', '2022-10-17 11:54:45.113446', 'fr', 'th1', 'G3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (6, 'collection1', '2022-10-17 11:55:09.36457', 'fr', 'th1', 'G3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (7, 'collection1', '2022-10-17 11:59:43.597546', 'fr', 'th1', 'G3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (8, 'collection2', '2022-10-17 12:20:31.181533', 'fr', 'th1', 'G3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (9, 'collection1', '2022-10-17 12:20:39.798785', 'fr', 'th1', 'G3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (10, 'collection1ff', '2022-10-17 12:20:47.479691', 'fr', 'th1', 'G3', 1);
INSERT INTO public.concept_group_label_historique (id, lexicalvalue, modified, lang, idthesaurus, idgroup, id_user) VALUES (11, 'collection1', '2022-10-17 12:20:57.129624', 'fr', 'th1', 'G3', 1);


--
-- TOC entry 4311 (class 0 OID 28757)
-- Dependencies: 239
-- Data for Name: concept_group_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('MT', 'Microthesaurus', 'MicroThesaurus');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('G', 'Group', 'ConceptGroup');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('C', 'Collection', 'Collection');
INSERT INTO public.concept_group_type (code, label, skoslabel) VALUES ('T', 'Theme', 'Theme');


--
-- TOC entry 4313 (class 0 OID 28763)
-- Dependencies: 241
-- Data for Name: concept_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('2', 'th1', '', '2022-01-12 12:55:26.18484+01', 'D', 'N1', true, 'G3', 2, 1);
INSERT INTO public.concept_historique (id_concept, id_thesaurus, id_ark, modified, status, notation, top_concept, id_group, id, id_user) VALUES ('3', 'th1', '', '2022-10-17 12:21:22.250461+02', 'D', '', false, 'G3', 3, 1);


--
-- TOC entry 4314 (class 0 OID 28771)
-- Dependencies: 242
-- Data for Name: concept_replacedby; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4315 (class 0 OID 28777)
-- Dependencies: 243
-- Data for Name: concept_term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4378 (class 0 OID 29330)
-- Dependencies: 306
-- Data for Name: concept_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.concept_type (code, label_fr, label_en) VALUES ('concept', 'concept', 'concept');
INSERT INTO public.concept_type (code, label_fr, label_en) VALUES ('people', 'personne', 'people');
INSERT INTO public.concept_type (code, label_fr, label_en) VALUES ('period', 'période', 'period');
INSERT INTO public.concept_type (code, label_fr, label_en) VALUES ('place', 'lieu', 'place');


--
-- TOC entry 4316 (class 0 OID 28782)
-- Dependencies: 244
-- Data for Name: copyright; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4317 (class 0 OID 28787)
-- Dependencies: 245
-- Data for Name: corpus_link; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4318 (class 0 OID 28794)
-- Dependencies: 246
-- Data for Name: custom_concept_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4319 (class 0 OID 28799)
-- Dependencies: 247
-- Data for Name: custom_term_attribute; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4320 (class 0 OID 28804)
-- Dependencies: 248
-- Data for Name: external_images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4377 (class 0 OID 29319)
-- Dependencies: 305
-- Data for Name: external_resources; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4322 (class 0 OID 28811)
-- Dependencies: 250
-- Data for Name: gps; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4324 (class 0 OID 28817)
-- Dependencies: 252
-- Data for Name: gps_preferences; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4325 (class 0 OID 28826)
-- Dependencies: 253
-- Data for Name: hierarchical_relationship; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('2', 'th1', 'NT', '3');
INSERT INTO public.hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) VALUES ('3', 'th1', 'BT', '2');


--
-- TOC entry 4326 (class 0 OID 28831)
-- Dependencies: 254
-- Data for Name: hierarchical_relationship_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('2', 'th1', 'NT', '3', '2022-10-17 12:21:22.250461+02', 1, 'ADD');
INSERT INTO public.hierarchical_relationship_historique (id_concept1, id_thesaurus, role, id_concept2, modified, id_user, action) VALUES ('3', 'th1', 'BT', '2', '2022-10-17 12:21:22.250461+02', 1, 'ADD');


--
-- TOC entry 4327 (class 0 OID 28837)
-- Dependencies: 255
-- Data for Name: homepage; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.homepage (htmlcode, lang) VALUES ('<p>Help and tutorials : <a href="https://opentheso.hypotheses.org" rel="noopener noreferrer" target="_blank" style="color: blue;">https://opentheso.hypotheses.org</a></p><p><strong style="color: rgb(230, 0, 0);">!!!!! To get started, select a thesaurus in the upper right !!!!!</strong></p><p>Opentheso is distributed under a free French law license compatible with the license <a href="http://www.gnu.org/copyleft/gpl.html" rel="noopener noreferrer" target="_blank" style="color: blue;">GNU GPL</a></p><p>It is a multilingual thesaurus manager, developed by the Technological Platform <a href="https://www.mom.fr/plateformes-technologiques/web-semantique-et-thesauri" rel="noopener noreferrer" target="_blank" style="color: blue;">WST</a> (Semantic Web &amp; Thesauri) located at <a href="https://www.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">MOM</a></p><p>in partnership with the <a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">GDS-FRANTIQ</a></p><p>&nbsp;</p><p><span style="color: black;">Designer : Brann Etienne</span><strong style="color: black;"> (</strong><a href="http://ithaqstudio.com/" rel="noopener noreferrer" target="_blank" style="color: rgb(149, 79, 114);"><strong>ithaqstudio.com</strong></a><strong style="color: black;">) </strong></p><p>Design integrator : Miled Rousset</p><p>&nbsp;</p><p>The development of Opentheso is supported in part by the consortium <a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank" style="color: blue;">MASA </a>(Memory of Archaeologists and Archaeological Sites) of the <a href="http://www.huma-num.fr/" rel="noopener noreferrer" target="_blank" style="color: blue;">TGIR Huma-Num.</a></p><p>Project Manager : <strong>Miled Rousset</strong></p><p>Development : <strong>Miled Rousset, Firas Gabsi, Emmanuelle Perrin, Prudham Jean-Marc, Quincy Mbape Eyoke, Antonio Perez, Carole Bonfré</strong></p><p>Partnership, testing and expertise : <strong>The teams of the network </strong><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;"><strong>Frantiq</strong></a> and in particular the group <a href="https://www.frantiq.fr/frantiq/organisation/groupes-de-travail-et-projets/pactols-opentheso/" rel="noopener noreferrer" target="_blank" style="color: blue;">PACTOLS</a>.</p><p>The development was carried out with the following technologies :</p><ul><li>PostgreSQL for the database</li><li>Java for the API and business module</li><li>JSF2 and PrimeFaces for the graphic part</li></ul><p>&nbsp;</p><p><strong>Opentheso</strong> is based on the project <a href="http://ark.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Arkéo</a> of the MOM to generate ark type identifiers <a href="http://fr.wikipedia.org/wiki/Archival_Resource_Key" rel="noopener noreferrer" target="_blank" style="color: blue;">ARK</a></p><p>Partners :</p><ul><li><a href="http://www.cnrs.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">CNRS</a></li><li><a href="http://www.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">MOM</a></li><li><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Frantiq</a></li><li><a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank" style="color: blue;">MASA</a></li><li><a href="http://www.huma-num.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Huma-Num</a></li></ul>', 'en');
INSERT INTO public.homepage (htmlcode, lang) VALUES ('<p>Aide et tutoriels : <a href="https://opentheso.hypotheses.org" rel="noopener noreferrer" target="_blank" style="color: blue;">https://opentheso.hypotheses.org</a></p><p><strong style="color: rgb(230, 0, 0);">!!!!! Pour commencer, sélectionnez un thésaurus en haut à droite !!!!!</strong></p><p>Opentheso est distribué en licence libre de droit français compatible avec la licence <a href="http://www.gnu.org/copyleft/gpl.html" rel="noopener noreferrer" target="_blank" style="color: blue;">GNU GPL</a></p><p>C''est un gestionnaire de thesaurus multilingue, développé par la plateforme Technologique <a href="https://www.mom.fr/plateformes-technologiques/web-semantique-et-thesauri" rel="noopener noreferrer" target="_blank" style="color: blue;">WST</a> (Web Sémantique &amp; Thesauri) située à la <a href="https://www.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">MOM</a></p><p>en partenariat avec le <a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">GDS-FRANTIQ</a></p><p>&nbsp;</p><p><span style="color: black;">Designer : Brann Etienne</span><strong style="color: black;"> (</strong><a href="http://ithaqstudio.com/" rel="noopener noreferrer" target="_blank" style="color: rgb(149, 79, 114);"><strong>ithaqstudio.com</strong></a><strong style="color: black;">) </strong></p><p>Intégrateur du design : Miled Rousset</p><p>&nbsp;</p><p>Le développement d''Opentheso est soutenu en partie par le Consortium <a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank" style="color: blue;">MASA </a>(Mémoire des archéologues et des Sites Archéologiques) de la <a href="http://www.huma-num.fr/" rel="noopener noreferrer" target="_blank" style="color: blue;">TGIR Huma-Num.</a></p><p>Chef de Projet : <strong>Miled Rousset</strong></p><p>Développement : <strong>Miled Rousset, Firas Gabsi, Emmanuelle Perrin, Prudham Jean-Marc, Quincy Mbape Eyoke, Antonio Perez, Carole Bonfré</strong></p><p>Partenariat, test et expertise : <strong>Les équipes du réseau </strong><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;"><strong>Frantiq</strong></a> et en particulier le groupe <a href="https://www.frantiq.fr/frantiq/organisation/groupes-de-travail-et-projets/pactols-opentheso/" rel="noopener noreferrer" target="_blank" style="color: blue;">PACTOLS</a>.</p><p>Le développement a été réalisé avec les technologies suivantes :</p><ul><li>PostgreSQL pour la base des données</li><li>Java pour le module API et module métier</li><li>JSF2 et PrimeFaces pour la partie graphique</li></ul><p>&nbsp;</p><p><strong>Opentheso</strong> s''appuie sur le projet <a href="http://ark.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Arkéo</a> de la MOM pour générer des identifiants type <a href="http://fr.wikipedia.org/wiki/Archival_Resource_Key" rel="noopener noreferrer" target="_blank" style="color: blue;">ARK</a></p><p>Partenaires :</p><ul><li><a href="http://www.cnrs.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">CNRS</a></li><li><a href="http://www.mom.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">MOM</a></li><li><a href="http://www.frantiq.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Frantiq</a></li><li><a href="http://masa.hypotheses.org/" rel="noopener noreferrer" target="_blank" style="color: blue;">MASA</a></li><li><a href="http://www.huma-num.fr" rel="noopener noreferrer" target="_blank" style="color: blue;">Huma-Num</a></li></ul>', 'fr');


--
-- TOC entry 4328 (class 0 OID 28842)
-- Dependencies: 256
-- Data for Name: images; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4329 (class 0 OID 28848)
-- Dependencies: 257
-- Data for Name: info; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4383 (class 0 OID 29361)
-- Dependencies: 311
-- Data for Name: languages_iso639; Type: TABLE DATA; Schema: public; Owner: -
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


--
-- TOC entry 4332 (class 0 OID 28861)
-- Dependencies: 260
-- Data for Name: node_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4333 (class 0 OID 28869)
-- Dependencies: 261
-- Data for Name: non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4334 (class 0 OID 28877)
-- Dependencies: 262
-- Data for Name: non_preferred_term_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4336 (class 0 OID 28885)
-- Dependencies: 264
-- Data for Name: note; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4338 (class 0 OID 28894)
-- Dependencies: 266
-- Data for Name: note_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4339 (class 0 OID 28901)
-- Dependencies: 267
-- Data for Name: note_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('note', false, true, 'Note', 'Note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('historyNote', true, true, 'Note historique', 'History note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('scopeNote', false, true, 'Note d''application', 'Scope note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('example', true, false, 'Exemple', 'Example');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('editorialNote', true, false, 'Note éditoriale', 'Editorial note');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('definition', true, false, 'Définition', 'Definition');
INSERT INTO public.note_type (code, isterm, isconcept, label_fr, label_en) VALUES ('changeNote', true, false, 'Note de changement', 'Change note');


--
-- TOC entry 4340 (class 0 OID 28907)
-- Dependencies: 268
-- Data for Name: nt_type; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (1, 'NT', 'Term spécifique', 'Narrower term');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (2, 'NTG', 'Term spécifique (generic)', 'Narrower term (generic)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (3, 'NTP', 'Term spécifique (partitive)', 'Narrower term (partitive)');
INSERT INTO public.nt_type (id, relation, description_fr, description_en) VALUES (4, 'NTI', 'Term spécifique (instantial)', 'Narrower term (instantial)');


--
-- TOC entry 4341 (class 0 OID 28912)
-- Dependencies: 269
-- Data for Name: permuted; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4343 (class 0 OID 28918)
-- Dependencies: 271
-- Data for Name: preferences; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.preferences (id_pref, id_thesaurus, source_lang, identifier_type, path_image, dossier_resize, bdd_active, bdd_use_id, url_bdd, url_counter_bdd, z3950actif, collection_adresse, notice_url, url_encode, path_notice1, path_notice2, chemin_site, webservices, use_ark, server_ark, id_naan, prefix_ark, user_ark, pass_ark, use_handle, user_handle, pass_handle, path_key_handle, path_cert_handle, url_api_handle, prefix_handle, private_prefix_handle, preferredname, original_uri, original_uri_is_ark, original_uri_is_handle, uri_ark, generate_handle, auto_expand_tree, sort_by_notation, tree_cache, original_uri_is_doi, use_ark_local, naan_ark_local, prefix_ark_local, sizeid_ark_local, breadcrumb, useconcepttree, display_user_name, suggestion) VALUES (2, 'th1', 'fr', 2, '/var/www/images/', 'resize', false, false, 'http://www.mondomaine.fr/concept/##value##', 'http://mondomaine.fr/concept/##conceptId##/total', false, 'KOHA/biblios', 'http://catalogue.mondomaine.fr/cgi-bin/koha/opac-search.pl?type=opac&op=do_search&q=an=terme', 'UTF-8', '/var/www/notices/repositories.xml', '/var/www/notices/SchemaMappings.xml', 'http://localhost:8080/opentheso2/', true, false, 'http://ark.mondomaine.fr/ark:/', '66666', 'crt', 'null', 'null', false, 'null', 'null', '/certificat/key.p12', '/certificat/cacerts2', 'https://handle-server.mondomaine.fr:8001/api/handles/', '66.666.66666', 'crt', 'test', 'http://localhost:8080/opentheso2', false, false, 'https://ark.mom.fr/ark:/', false, true, false, false, false, false, '', '', 10, true, false, false, false);


--
-- TOC entry 4344 (class 0 OID 28962)
-- Dependencies: 272
-- Data for Name: preferences_sparql; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4345 (class 0 OID 28968)
-- Dependencies: 273
-- Data for Name: preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('2', '1', 'th1');
INSERT INTO public.preferred_term (id_concept, id_term, id_thesaurus) VALUES ('3', '4', 'th1');


--
-- TOC entry 4346 (class 0 OID 28973)
-- Dependencies: 274
-- Data for Name: proposition; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4380 (class 0 OID 29341)
-- Dependencies: 308
-- Data for Name: proposition_modification; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4382 (class 0 OID 29349)
-- Dependencies: 310
-- Data for Name: proposition_modification_detail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4347 (class 0 OID 28980)
-- Dependencies: 275
-- Data for Name: relation_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4348 (class 0 OID 28985)
-- Dependencies: 276
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.roles (id, name, description) VALUES (1, 'superAdmin', 'Super Administrateur pour tout gérer tout thésaurus et tout utilisateur');
INSERT INTO public.roles (id, name, description) VALUES (2, 'admin', 'administrateur pour un domaine ou parc de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (3, 'manager', 'gestionnaire de thésaurus, pas de création de thésaurus');
INSERT INTO public.roles (id, name, description) VALUES (4, 'contributor', 'traducteur, notes, candidats, images');


--
-- TOC entry 4350 (class 0 OID 28991)
-- Dependencies: 278
-- Data for Name: routine_mail; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4351 (class 0 OID 28997)
-- Dependencies: 279
-- Data for Name: split_non_preferred_term; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4352 (class 0 OID 29000)
-- Dependencies: 280
-- Data for Name: status; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.status (id_status, value) VALUES (1, 'En attente');
INSERT INTO public.status (id_status, value) VALUES (2, 'Inséré');
INSERT INTO public.status (id_status, value) VALUES (3, 'Rejeté');


--
-- TOC entry 4356 (class 0 OID 29008)
-- Dependencies: 284
-- Data for Name: term; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('1', 'top1', 'en', 'th1', '2022-01-12 12:55:26.18484+01', '2022-01-12 12:55:26.18484+01', '', 'D', 2, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('1', 'topTerm', 'fr', 'th1', '2022-10-17 11:52:54.039128+02', '2022-10-17 11:52:54.039128+02', '', '', 3, 1, 1);
INSERT INTO public.term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, id, contributor, creator) VALUES ('4', 'concept1', 'fr', 'th1', '2022-10-17 12:21:22.250461+02', '2022-10-17 12:21:22.250461+02', '', 'D', 4, 1, 1);


--
-- TOC entry 4358 (class 0 OID 29018)
-- Dependencies: 286
-- Data for Name: term_candidat; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4360 (class 0 OID 29027)
-- Dependencies: 288
-- Data for Name: term_historique; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('1', 'top1', 'en', 'th1', '2022-01-12 12:55:26.18484+01', '', 'D', 2, 1, NULL);
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('1', 'topTerm', 'fr', 'th1', '2022-10-17 11:52:54.053827+02', '', 'D', 3, 1, 'New');
INSERT INTO public.term_historique (id_term, lexical_value, lang, id_thesaurus, modified, source, status, id, id_user, action) VALUES ('4', 'concept1', 'fr', 'th1', '2022-10-17 12:21:22.250461+02', '', 'D', 4, 1, 'ADD');


--
-- TOC entry 4362 (class 0 OID 29036)
-- Dependencies: 290
-- Data for Name: thesaurus; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesaurus (id_thesaurus, id_ark, created, modified, id, private) VALUES ('th1', '', '2022-01-12 00:00:00', '2022-01-12 00:00:00', 2, false);


--
-- TOC entry 4363 (class 0 OID 29045)
-- Dependencies: 291
-- Data for Name: thesaurus_alignement_source; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4364 (class 0 OID 29050)
-- Dependencies: 292
-- Data for Name: thesaurus_array; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4365 (class 0 OID 29056)
-- Dependencies: 293
-- Data for Name: thesaurus_label; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', 'admin', '', 'admin', '2022-01-12 00:00:00', '2022-01-12 00:00:00', '', '', 'en', '', '', '', '', '', 'test', '');
INSERT INTO public.thesaurus_label (id_thesaurus, contributor, coverage, creator, created, modified, description, format, lang, publisher, relation, rights, source, subject, title, type) VALUES ('th1', 'admin', '', 'admin', '2022-10-17 00:00:00', '2022-10-17 00:00:00', '', '', 'fr', '', '', '', '', '', 'test', '');


--
-- TOC entry 4366 (class 0 OID 29063)
-- Dependencies: 294
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
-- TOC entry 4369 (class 0 OID 29070)
-- Dependencies: 297
-- Data for Name: user_group_label; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4370 (class 0 OID 29076)
-- Dependencies: 298
-- Data for Name: user_group_thesaurus; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4371 (class 0 OID 29081)
-- Dependencies: 299
-- Data for Name: user_role_group; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4372 (class 0 OID 29084)
-- Dependencies: 300
-- Data for Name: user_role_only_on; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4373 (class 0 OID 29090)
-- Dependencies: 301
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.users (id_user, username, password, active, mail, passtomodify, alertmail, issuperadmin) VALUES (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', true, 'admin@domaine.fr', false, false, true);


--
-- TOC entry 4374 (class 0 OID 29100)
-- Dependencies: 302
-- Data for Name: users2; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4375 (class 0 OID 29108)
-- Dependencies: 303
-- Data for Name: users_historique; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4376 (class 0 OID 29115)
-- Dependencies: 304
-- Data for Name: version_history; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- TOC entry 4394 (class 0 OID 0)
-- Dependencies: 211
-- Name: alignement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_id_seq', 1, false);


--
-- TOC entry 4395 (class 0 OID 0)
-- Dependencies: 213
-- Name: alignement_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_preferences_id_seq', 1, false);


--
-- TOC entry 4396 (class 0 OID 0)
-- Dependencies: 215
-- Name: alignement_source__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.alignement_source__id_seq', 195, true);


--
-- TOC entry 4397 (class 0 OID 0)
-- Dependencies: 219
-- Name: candidat_messages_id_message_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.candidat_messages_id_message_seq', 13, true);


--
-- TOC entry 4398 (class 0 OID 0)
-- Dependencies: 223
-- Name: candidat_vote_id_vote_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.candidat_vote_id_vote_seq', 19, true);


--
-- TOC entry 4399 (class 0 OID 0)
-- Dependencies: 225
-- Name: concept__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept__id_seq', 3, true);


--
-- TOC entry 4400 (class 0 OID 0)
-- Dependencies: 227
-- Name: concept_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_candidat__id_seq', 1, false);


--
-- TOC entry 4401 (class 0 OID 0)
-- Dependencies: 230
-- Name: concept_group__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group__id_seq', 4, true);


--
-- TOC entry 4402 (class 0 OID 0)
-- Dependencies: 233
-- Name: concept_group_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_historique__id_seq', 1, false);


--
-- TOC entry 4403 (class 0 OID 0)
-- Dependencies: 237
-- Name: concept_group_label_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_label_historique__id_seq', 11, true);


--
-- TOC entry 4404 (class 0 OID 0)
-- Dependencies: 235
-- Name: concept_group_label_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_group_label_id_seq', 3, true);


--
-- TOC entry 4405 (class 0 OID 0)
-- Dependencies: 240
-- Name: concept_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.concept_historique__id_seq', 3, true);


--
-- TOC entry 4406 (class 0 OID 0)
-- Dependencies: 249
-- Name: facet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.facet_id_seq', 1, false);


--
-- TOC entry 4407 (class 0 OID 0)
-- Dependencies: 251
-- Name: gps_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.gps_preferences_id_seq', 1, false);


--
-- TOC entry 4408 (class 0 OID 0)
-- Dependencies: 258
-- Name: languages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.languages_id_seq', 193, true);


--
-- TOC entry 4409 (class 0 OID 0)
-- Dependencies: 263
-- Name: note__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.note__id_seq', 1, false);


--
-- TOC entry 4410 (class 0 OID 0)
-- Dependencies: 265
-- Name: note_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.note_historique__id_seq', 1, false);


--
-- TOC entry 4411 (class 0 OID 0)
-- Dependencies: 270
-- Name: pref__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.pref__id_seq', 2, true);


--
-- TOC entry 4412 (class 0 OID 0)
-- Dependencies: 309
-- Name: proposition_modification_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.proposition_modification_detail_id_seq', 1, false);


--
-- TOC entry 4413 (class 0 OID 0)
-- Dependencies: 307
-- Name: proposition_modification_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.proposition_modification_id_seq', 1, false);


--
-- TOC entry 4414 (class 0 OID 0)
-- Dependencies: 277
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.role_id_seq', 6, true);


--
-- TOC entry 4415 (class 0 OID 0)
-- Dependencies: 281
-- Name: status_id_status_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.status_id_status_seq', 1, false);


--
-- TOC entry 4416 (class 0 OID 0)
-- Dependencies: 282
-- Name: status_id_status_seq1; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.status_id_status_seq1', 1, false);


--
-- TOC entry 4417 (class 0 OID 0)
-- Dependencies: 283
-- Name: term__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term__id_seq', 4, true);


--
-- TOC entry 4418 (class 0 OID 0)
-- Dependencies: 285
-- Name: term_candidat__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term_candidat__id_seq', 1, false);


--
-- TOC entry 4419 (class 0 OID 0)
-- Dependencies: 287
-- Name: term_historique__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.term_historique__id_seq', 4, true);


--
-- TOC entry 4420 (class 0 OID 0)
-- Dependencies: 259
-- Name: thesaurus_array_facet_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.thesaurus_array_facet_id_seq', 1, false);


--
-- TOC entry 4421 (class 0 OID 0)
-- Dependencies: 289
-- Name: thesaurus_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.thesaurus_id_seq', 2, true);


--
-- TOC entry 4422 (class 0 OID 0)
-- Dependencies: 295
-- Name: user__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user__id_seq', 2, false);


--
-- TOC entry 4423 (class 0 OID 0)
-- Dependencies: 296
-- Name: user_group_label__id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.user_group_label__id_seq', 1, false);


--
-- TOC entry 4131 (class 2606 OID 29124)
-- Name: version_history VersionHistory_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.version_history
    ADD CONSTRAINT "VersionHistory_pkey" PRIMARY KEY ("idVersionhistory");


--
-- TOC entry 3966 (class 2606 OID 29126)
-- Name: alignement alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement
    ADD CONSTRAINT alignement_internal_id_concept_internal_id_thesaurus_uri_ta_key UNIQUE (internal_id_concept, internal_id_thesaurus, uri_target);


--
-- TOC entry 3968 (class 2606 OID 29128)
-- Name: alignement alignement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement
    ADD CONSTRAINT alignement_pkey PRIMARY KEY (id);


--
-- TOC entry 3970 (class 2606 OID 29130)
-- Name: alignement_preferences alignement_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_preferences
    ADD CONSTRAINT alignement_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_concept_depart, id_alignement_source);


--
-- TOC entry 3972 (class 2606 OID 29132)
-- Name: alignement_source alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_source
    ADD CONSTRAINT alignement_source_pkey PRIMARY KEY (id);


--
-- TOC entry 3974 (class 2606 OID 29134)
-- Name: alignement_source alignement_source_source_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_source
    ADD CONSTRAINT alignement_source_source_key UNIQUE (source);


--
-- TOC entry 3976 (class 2606 OID 29136)
-- Name: alignement_type alignment_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alignement_type
    ADD CONSTRAINT alignment_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3978 (class 2606 OID 29138)
-- Name: bt_type bt_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bt_type
    ADD CONSTRAINT bt_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3980 (class 2606 OID 29140)
-- Name: bt_type bt_type_relation_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bt_type
    ADD CONSTRAINT bt_type_relation_key UNIQUE (relation);


--
-- TOC entry 3982 (class 2606 OID 29142)
-- Name: candidat_messages candidat_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_messages
    ADD CONSTRAINT candidat_messages_pkey PRIMARY KEY (id_message);


--
-- TOC entry 3984 (class 2606 OID 29144)
-- Name: candidat_status candidat_status_id_concept_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_status
    ADD CONSTRAINT candidat_status_id_concept_id_thesaurus_key UNIQUE (id_concept, id_thesaurus);


--
-- TOC entry 3986 (class 2606 OID 29146)
-- Name: candidat_vote candidat_vote_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.candidat_vote
    ADD CONSTRAINT candidat_vote_pkey PRIMARY KEY (id_vote);


--
-- TOC entry 3988 (class 2606 OID 29148)
-- Name: compound_equivalence compound_equivalence_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compound_equivalence
    ADD CONSTRAINT compound_equivalence_pkey PRIMARY KEY (id_split_nonpreferredterm, id_preferredterm);


--
-- TOC entry 3993 (class 2606 OID 29150)
-- Name: concept_candidat concept_candidat_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_candidat
    ADD CONSTRAINT concept_candidat_id_key UNIQUE (id);


--
-- TOC entry 3995 (class 2606 OID 29152)
-- Name: concept_candidat concept_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_candidat
    ADD CONSTRAINT concept_candidat_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 4013 (class 2606 OID 29154)
-- Name: concept_historique concept_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_historique
    ADD CONSTRAINT concept_copy_pkey PRIMARY KEY (id_concept, id_thesaurus, id_group, id_user, modified);


--
-- TOC entry 3997 (class 2606 OID 29156)
-- Name: concept_facet concept_facettes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_facet
    ADD CONSTRAINT concept_facettes_pkey PRIMARY KEY (id_facet, id_thesaurus, id_concept);


--
-- TOC entry 4015 (class 2606 OID 29158)
-- Name: concept_replacedby concept_fusion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_replacedby
    ADD CONSTRAINT concept_fusion_pkey PRIMARY KEY (id_concept1, id_concept2, id_thesaurus);


--
-- TOC entry 4001 (class 2606 OID 29160)
-- Name: concept_group_concept concept_group_concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_concept
    ADD CONSTRAINT concept_group_concept_pkey PRIMARY KEY (idgroup, idthesaurus, idconcept);


--
-- TOC entry 4003 (class 2606 OID 29162)
-- Name: concept_group_historique concept_group_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_historique
    ADD CONSTRAINT concept_group_copy_pkey PRIMARY KEY (idgroup, idthesaurus, modified, id_user);


--
-- TOC entry 4009 (class 2606 OID 29164)
-- Name: concept_group_label_historique concept_group_label_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_label_historique
    ADD CONSTRAINT concept_group_label_copy_pkey PRIMARY KEY (lang, idthesaurus, lexicalvalue, modified, id_user);


--
-- TOC entry 4005 (class 2606 OID 29166)
-- Name: concept_group_label concept_group_label_idgrouplabel_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_label
    ADD CONSTRAINT concept_group_label_idgrouplabel_key UNIQUE (id);


--
-- TOC entry 4007 (class 2606 OID 29315)
-- Name: concept_group_label concept_group_label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_label
    ADD CONSTRAINT concept_group_label_pkey PRIMARY KEY (lang, idthesaurus, lexicalvalue);


--
-- TOC entry 3999 (class 2606 OID 29170)
-- Name: concept_group concept_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group
    ADD CONSTRAINT concept_group_pkey PRIMARY KEY (idgroup, idthesaurus);


--
-- TOC entry 4011 (class 2606 OID 29172)
-- Name: concept_group_type concept_group_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_group_type
    ADD CONSTRAINT concept_group_type_pkey PRIMARY KEY (code, label);


--
-- TOC entry 3991 (class 2606 OID 29174)
-- Name: concept concept_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept
    ADD CONSTRAINT concept_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 4017 (class 2606 OID 29176)
-- Name: concept_term_candidat concept_term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_term_candidat
    ADD CONSTRAINT concept_term_candidat_pkey PRIMARY KEY (id_concept, id_term, id_thesaurus);


--
-- TOC entry 4135 (class 2606 OID 29336)
-- Name: concept_type concept_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.concept_type
    ADD CONSTRAINT concept_type_pkey PRIMARY KEY (code);


--
-- TOC entry 4019 (class 2606 OID 29178)
-- Name: copyright copyright_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.copyright
    ADD CONSTRAINT copyright_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 4021 (class 2606 OID 29180)
-- Name: corpus_link corpus_link_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.corpus_link
    ADD CONSTRAINT corpus_link_pkey PRIMARY KEY (id_theso, corpus_name);


--
-- TOC entry 4023 (class 2606 OID 29182)
-- Name: custom_concept_attribute custom_concept_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_concept_attribute
    ADD CONSTRAINT custom_concept_attribute_pkey PRIMARY KEY ("idConcept");


--
-- TOC entry 4025 (class 2606 OID 29184)
-- Name: custom_term_attribute custom_term_attribute_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_term_attribute
    ADD CONSTRAINT custom_term_attribute_pkey PRIMARY KEY (identifier);


--
-- TOC entry 4027 (class 2606 OID 29186)
-- Name: external_images external_images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_images
    ADD CONSTRAINT external_images_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 4133 (class 2606 OID 29326)
-- Name: external_resources external_resources_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.external_resources
    ADD CONSTRAINT external_resources_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 4029 (class 2606 OID 29188)
-- Name: gps gps_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gps
    ADD CONSTRAINT gps_pkey PRIMARY KEY (id_concept, id_theso);


--
-- TOC entry 4031 (class 2606 OID 29190)
-- Name: gps_preferences gps_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gps_preferences
    ADD CONSTRAINT gps_preferences_pkey PRIMARY KEY (id_thesaurus, id_user, id_alignement_source);


--
-- TOC entry 4035 (class 2606 OID 29192)
-- Name: hierarchical_relationship_historique hierarchical_relationship_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchical_relationship_historique
    ADD CONSTRAINT hierarchical_relationship_copy_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2, modified, id_user);


--
-- TOC entry 4033 (class 2606 OID 29194)
-- Name: hierarchical_relationship hierarchical_relationship_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.hierarchical_relationship
    ADD CONSTRAINT hierarchical_relationship_pkey PRIMARY KEY (id_concept1, id_thesaurus, role, id_concept2);


--
-- TOC entry 4037 (class 2606 OID 29196)
-- Name: homepage homepage_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.homepage
    ADD CONSTRAINT homepage_lang_key UNIQUE (lang);


--
-- TOC entry 4039 (class 2606 OID 29198)
-- Name: images images_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.images
    ADD CONSTRAINT images_pkey PRIMARY KEY (id_concept, id_thesaurus, external_uri);


--
-- TOC entry 4141 (class 2606 OID 29368)
-- Name: languages_iso639 languages_iso639_iso639_1_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_iso639_1_key UNIQUE (iso639_1);


--
-- TOC entry 4143 (class 2606 OID 29370)
-- Name: languages_iso639 languages_iso639_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.languages_iso639
    ADD CONSTRAINT languages_iso639_pkey PRIMARY KEY (id);


--
-- TOC entry 4042 (class 2606 OID 29204)
-- Name: non_preferred_term non_prefered_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.non_preferred_term
    ADD CONSTRAINT non_prefered_term_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 4045 (class 2606 OID 29206)
-- Name: non_preferred_term_historique non_preferred_term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.non_preferred_term_historique
    ADD CONSTRAINT non_preferred_term_copy_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, modified, id_user);


--
-- TOC entry 4050 (class 2606 OID 29208)
-- Name: note_historique note_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_historique
    ADD CONSTRAINT note_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 4048 (class 2606 OID 29210)
-- Name: note note_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note
    ADD CONSTRAINT note_pkey PRIMARY KEY (id);


--
-- TOC entry 4054 (class 2606 OID 29212)
-- Name: nt_type nt_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_pkey PRIMARY KEY (id);


--
-- TOC entry 4056 (class 2606 OID 29214)
-- Name: nt_type nt_type_relation_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.nt_type
    ADD CONSTRAINT nt_type_relation_key UNIQUE (relation);


--
-- TOC entry 4059 (class 2606 OID 29216)
-- Name: permuted permuted_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.permuted
    ADD CONSTRAINT permuted_pkey PRIMARY KEY (ord, id_concept, id_group, id_thesaurus, id_lang, lexical_value, ispreferredterm);


--
-- TOC entry 4052 (class 2606 OID 29218)
-- Name: note_type pk_note_type; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.note_type
    ADD CONSTRAINT pk_note_type PRIMARY KEY (code);


--
-- TOC entry 4073 (class 2606 OID 29220)
-- Name: relation_group pk_relation_group; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.relation_group
    ADD CONSTRAINT pk_relation_group PRIMARY KEY (id_group1, id_thesaurus, relation, id_group2);


--
-- TOC entry 4061 (class 2606 OID 29222)
-- Name: preferences preferences_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 4063 (class 2606 OID 29224)
-- Name: preferences preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_pkey PRIMARY KEY (id_pref);


--
-- TOC entry 4065 (class 2606 OID 29226)
-- Name: preferences preferences_preferredname_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences
    ADD CONSTRAINT preferences_preferredname_key UNIQUE (preferredname);


--
-- TOC entry 4067 (class 2606 OID 29228)
-- Name: preferences_sparql preferences_sparql_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferences_sparql
    ADD CONSTRAINT preferences_sparql_pkey PRIMARY KEY (thesaurus);


--
-- TOC entry 4069 (class 2606 OID 29230)
-- Name: preferred_term preferred_term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.preferred_term
    ADD CONSTRAINT preferred_term_pkey PRIMARY KEY (id_concept, id_thesaurus);


--
-- TOC entry 4139 (class 2606 OID 29355)
-- Name: proposition_modification_detail proposition_modification_detail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposition_modification_detail
    ADD CONSTRAINT proposition_modification_detail_pkey PRIMARY KEY (id);


--
-- TOC entry 4137 (class 2606 OID 29347)
-- Name: proposition_modification proposition_modification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposition_modification
    ADD CONSTRAINT proposition_modification_pkey PRIMARY KEY (id);


--
-- TOC entry 4071 (class 2606 OID 29232)
-- Name: proposition proposition_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.proposition
    ADD CONSTRAINT proposition_pkey PRIMARY KEY (id_concept, id_user, id_thesaurus);


--
-- TOC entry 4075 (class 2606 OID 29234)
-- Name: roles role_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- TOC entry 4077 (class 2606 OID 29236)
-- Name: routine_mail routine_mail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routine_mail
    ADD CONSTRAINT routine_mail_pkey PRIMARY KEY (id_thesaurus);


--
-- TOC entry 4079 (class 2606 OID 29238)
-- Name: status status_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.status
    ADD CONSTRAINT status_pkey PRIMARY KEY (id_status);


--
-- TOC entry 4090 (class 2606 OID 29240)
-- Name: term_candidat term_candidat_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term_candidat
    ADD CONSTRAINT term_candidat_pkey PRIMARY KEY (id_term, lexical_value, lang, id_thesaurus, contributor);


--
-- TOC entry 4093 (class 2606 OID 29242)
-- Name: term_historique term_copy_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term_historique
    ADD CONSTRAINT term_copy_pkey PRIMARY KEY (id, modified, id_user);


--
-- TOC entry 4082 (class 2606 OID 29244)
-- Name: term term_id_term_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_key UNIQUE (id_term, lang, id_thesaurus);


--
-- TOC entry 4084 (class 2606 OID 29246)
-- Name: term term_id_term_lexical_value_lang_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_id_term_lexical_value_lang_id_thesaurus_key UNIQUE (id_term, lexical_value, lang, id_thesaurus);


--
-- TOC entry 4087 (class 2606 OID 29248)
-- Name: term term_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.term
    ADD CONSTRAINT term_pkey PRIMARY KEY (id);


--
-- TOC entry 4097 (class 2606 OID 29250)
-- Name: thesaurus_alignement_source thesaurus_alignement_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_alignement_source
    ADD CONSTRAINT thesaurus_alignement_source_pkey PRIMARY KEY (id_thesaurus, id_alignement_source);


--
-- TOC entry 4099 (class 2606 OID 29252)
-- Name: thesaurus_array thesaurus_array_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_array
    ADD CONSTRAINT thesaurus_array_pkey PRIMARY KEY (id_facet, id_thesaurus, id_concept_parent);


--
-- TOC entry 4101 (class 2606 OID 29254)
-- Name: thesaurus_label thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT thesaurus_pkey PRIMARY KEY (id_thesaurus, lang, title);


--
-- TOC entry 4095 (class 2606 OID 29256)
-- Name: thesaurus thesaurus_pkey1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus
    ADD CONSTRAINT thesaurus_pkey1 PRIMARY KEY (id_thesaurus, id_ark);


--
-- TOC entry 4105 (class 2606 OID 29258)
-- Name: thesohomepage thesohomepage_idtheso_lang_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesohomepage
    ADD CONSTRAINT thesohomepage_idtheso_lang_key UNIQUE (idtheso, lang);


--
-- TOC entry 4103 (class 2606 OID 29260)
-- Name: thesaurus_label unique_thesau_lang; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.thesaurus_label
    ADD CONSTRAINT unique_thesau_lang UNIQUE (id_thesaurus, lang);


--
-- TOC entry 4107 (class 2606 OID 29262)
-- Name: user_group_label user_group-label_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_label
    ADD CONSTRAINT "user_group-label_pkey" PRIMARY KEY (id_group);


--
-- TOC entry 4113 (class 2606 OID 29264)
-- Name: user_role_group user_group_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_group
    ADD CONSTRAINT user_group_pkey UNIQUE (id_user, id_group);


--
-- TOC entry 4109 (class 2606 OID 29266)
-- Name: user_group_thesaurus user_group_thesaurus_id_thesaurus_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_id_thesaurus_key UNIQUE (id_thesaurus);


--
-- TOC entry 4111 (class 2606 OID 29268)
-- Name: user_group_thesaurus user_group_thesaurus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_group_thesaurus
    ADD CONSTRAINT user_group_thesaurus_pkey PRIMARY KEY (id_group, id_thesaurus);


--
-- TOC entry 4117 (class 2606 OID 29270)
-- Name: users user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT user_pkey PRIMARY KEY (id_user);


--
-- TOC entry 4115 (class 2606 OID 29272)
-- Name: user_role_only_on user_role_only_on_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_role_only_on
    ADD CONSTRAINT user_role_only_on_pkey PRIMARY KEY (id_user, id_role, id_theso);


--
-- TOC entry 4129 (class 2606 OID 29274)
-- Name: users_historique users_historique_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users_historique
    ADD CONSTRAINT users_historique_pkey PRIMARY KEY (id_user);


--
-- TOC entry 4123 (class 2606 OID 29276)
-- Name: users2 users_login_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_login_key UNIQUE (login);


--
-- TOC entry 4125 (class 2606 OID 29278)
-- Name: users2 users_mail_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_mail_key UNIQUE (mail);


--
-- TOC entry 4119 (class 2606 OID 29280)
-- Name: users users_mail_key1; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_mail_key1 UNIQUE (mail);


--
-- TOC entry 4127 (class 2606 OID 29282)
-- Name: users2 users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users2
    ADD CONSTRAINT users_pkey PRIMARY KEY (id_user);


--
-- TOC entry 4121 (class 2606 OID 29284)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 3989 (class 1259 OID 29285)
-- Name: concept_notation_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX concept_notation_unaccent ON public.concept USING gin (public.f_unaccent(lower((notation)::text)) public.gin_trgm_ops);


--
-- TOC entry 4080 (class 1259 OID 29286)
-- Name: index_lexical_value; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value ON public.term USING btree (lexical_value);


--
-- TOC entry 4091 (class 1259 OID 29287)
-- Name: index_lexical_value_copy; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value_copy ON public.term_historique USING btree (lexical_value);


--
-- TOC entry 4040 (class 1259 OID 29288)
-- Name: index_lexical_value_npt; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX index_lexical_value_npt ON public.non_preferred_term USING btree (lexical_value);


--
-- TOC entry 4046 (class 1259 OID 29289)
-- Name: note_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX note_lexical_value_unaccent ON public.note USING gin (public.f_unaccent(lower((lexicalvalue)::text)) public.gin_trgm_ops);


--
-- TOC entry 4057 (class 1259 OID 29290)
-- Name: permuted_lexical_value_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX permuted_lexical_value_idx ON public.permuted USING btree (lexical_value);


--
-- TOC entry 4043 (class 1259 OID 29291)
-- Name: term_lexical_value_npt_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX term_lexical_value_npt_unaccent ON public.non_preferred_term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 4085 (class 1259 OID 29292)
-- Name: term_lexical_value_unaccent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX term_lexical_value_unaccent ON public.term USING gin (public.f_unaccent(lower((lexical_value)::text)) public.gin_trgm_ops);


--
-- TOC entry 4088 (class 1259 OID 29293)
-- Name: terms_values_gin; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX terms_values_gin ON public.term USING gin (lexical_value public.gin_trgm_ops);


-- Completed on 2022-10-17 13:10:23 CEST

--
-- PostgreSQL database dump complete
--

