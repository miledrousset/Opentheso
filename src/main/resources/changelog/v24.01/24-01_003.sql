DROP FUNCTION IF EXISTS public.opentheso_get_idconcept_from_idterm(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_idconcept_from_idterm(
	id_theso character varying,
	id_term1 character varying)
    RETURNS TABLE(id_concept1 character varying) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT id_concept FROM preferred_term WHERE id_thesaurus = id_theso and id_term = id_term1;
end;
$BODY$;

-- 
DROP PROCEDURE IF EXISTS public.opentheso_normalize_notes();

CREATE OR REPLACE PROCEDURE public.opentheso_normalize_notes()
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	notes_rec record;
	identifier1 character varying;
BEGIN

	FOR notes_rec IN (SELECT id, id_thesaurus, id_concept, id_term from note)
    LOOP
		IF notes_rec.id_concept is NULL THEN
			identifier1 = opentheso_get_idconcept_from_idterm(notes_rec.id_thesaurus, notes_rec.id_term);
		ELSE
			identifier1 = notes_rec.id_concept;
		END IF;
		update note set identifier = identifier1 where id = notes_rec.id;
	END LOOP;
END;
$BODY$;

-- 

DROP FUNCTION IF EXISTS public.opentheso_get_notes(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_notes(
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
		WHERE 
		note.id_thesaurus = id_theso
		AND note.identifier = id_con;
end;
$BODY$;

--- 

DROP FUNCTION IF EXISTS public.opentheso_get_concepts(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_concepts(
	id_theso character varying,
	path character varying)
    RETURNS SETOF record 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

-- 


DROP FUNCTION IF EXISTS public.opentheso_get_concepts_by_group(character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_concepts_by_group(
	id_theso character varying,
	path character varying,
	id_group character varying)
    RETURNS SETOF record 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

--


DROP FUNCTION IF EXISTS public.opentheso_get_concept(character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_concept(
	idtheso character varying,
	idconcept character varying,
	idlang character varying)
    RETURNS SETOF record 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_get_definitions(character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_definitions(
	idtheso character varying,
	idconcept character varying,
	idlang character varying)
    RETURNS TABLE(definition character varying, unique_id integer) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

-- 

DROP FUNCTION IF EXISTS public.opentheso_get_narrowers_ignorefacet(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_narrowers_ignorefacet(
	id_theso character varying,
	id_bt character varying)
    RETURNS TABLE(notation character varying, status character varying, idconcept2 character varying) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_get_list_narrower_forgraph(character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_list_narrower_forgraph(
	idtheso character varying,
	idbt character varying,
	idlang character varying)
    RETURNS SETOF record 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

--

DROP PROCEDURE IF EXISTS public.opentheso_add_notes(character varying, character varying, integer, text);
CREATE OR REPLACE PROCEDURE public.opentheso_add_notes(
	IN id_concept character varying,
	IN id_thesaurus character varying,
	IN id_user integer,
	IN notes text)
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
		
		insert into note (notetypecode, id_thesaurus, lang, lexicalvalue, id_user, identifier) 
			values (array_string[2], id_thesaurus, array_string[3], array_string[1], id_user, id_concept);
		Insert into note_historique (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue, action_performed, id_user)
			values (array_string[2], id_thesaurus, id_concept, array_string[3], array_string[1], 'add', id_user);
	END LOOP;
END;
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_get_facettes(character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_facettes(
	id_theso character varying,
	path character varying)
    RETURNS SETOF record 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

-- 

DROP PROCEDURE IF EXISTS public.opentheso_add_facet(character varying, character varying, character varying, text, text);
DROP PROCEDURE IF EXISTS public.opentheso_add_facet(character varying, character varying, character varying, text, text, text);
DROP PROCEDURE IF EXISTS public.opentheso_add_facet(character varying, integer, character varying, character varying, text, text, text);
CREATE OR REPLACE PROCEDURE public.opentheso_add_facet(
	IN id_facet character varying,
	IN id_user integer,
	IN id_thesaurus character varying,
	IN id_conceotparent character varying,
	IN labels text,
	IN membres text,
	IN notes text)
LANGUAGE 'plpgsql'
AS $BODY$
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
$BODY$;