-- Mise à jour de la fonction de récupération du concept

-- FUNCTION: public.opentheso_get_concept(character varying, character varying, character varying)

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

	permaLinkId VARCHAR;

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

		IF(theso_rec.original_uri_is_ark) THEN permaLinkId = con.id_ark;
		END IF;
		IF(theso_rec.original_uri_is_handle) THEN permaLinkId = con.id_handle;
		END IF;		
		
			
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

		-- PrefLabTraduction
		preflab = '';
		FOR traduction_rec IN SELECT * FROM opentheso_get_preflabel_traductions(idtheso, idconcept, idlang)
		LOOP
			preflab = preflab || traduction_rec.term_id || sous_seperateur || traduction_rec.term_lexical_value || sous_seperateur || traduction_rec.term_lang || sous_seperateur || traduction_rec.unique_id || sous_seperateur || traduction_rec.flag_code || seperateur;
		END LOOP;

		-- altLabTraduction
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
				note = note || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || sous_seperateur || note_concept_rec.note_source || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || sous_seperateur || note_concept_rec.note_source || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || sous_seperateur || note_concept_rec.note_source || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || sous_seperateur || note_concept_rec.note_source || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'example') THEN
				example = example || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || sous_seperateur || note_concept_rec.note_source || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || sous_seperateur || note_concept_rec.note_source || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_concept_rec.note_id || sous_seperateur || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || sous_seperateur || note_concept_rec.note_source || seperateur;
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
			img = img || img_rec.name || sous_seperateur || img_rec.copyright || sous_seperateur || img_rec.url || sous_seperateur || img_rec.creator || seperateur;
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
			SELECT 	uri, con.status, local_URI, idconcept, permaLinkId, prefLab_selected, altLab_selected, altLab_hiden_selected, prefLab, altLab, altLab_hiden, definition, example,
			  editorialNote, changeNote, secopeNote, note, historyNote, con.notation, narrower, broader, related, exactMatch, closeMatch,
			  broadMatch, relatedMatch, narrowMatch, gpsData, membre, con.created, con.modified,
			  img, creator, contributor, replaces, replacedBy, facets, externalResources, con.concept_type;

END;
$BODY$;



-- Mise à jour de la focntion de récupération des langues

DROP FUNCTION IF EXISTS public.opentheso_get_preflabel_traductions(character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_preflabel_traductions(
	id_theso character varying,
	id_con character varying,
	id_lang character varying)
    RETURNS TABLE(term_id character varying, term_lexical_value character varying, term_lang character varying, unique_id integer, flag_code character varying) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT term.id_term, term.lexical_value, term.lang, term.id, languages_iso639.code_pays
		FROM term, preferred_term, languages_iso639
		WHERE term.id_term = preferred_term.id_term
		AND term.id_thesaurus = preferred_term.id_thesaurus
		AND term.lang = languages_iso639.iso639_1
		
		AND term.id_thesaurus = id_theso
		AND preferred_term.id_concept = id_con
		AND term.lang != id_lang
		ORDER BY term.lexical_value;

end;
$BODY$;


-- mise à jour de la fonction des Uris

DROP FUNCTION IF EXISTS public.opentheso_get_uri(boolean, character varying, character varying, boolean, character varying, boolean, character varying, character varying, character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_uri(
	original_uri_is_ark boolean,
	id_ark character varying,
	original_uri character varying,
	original_uri_is_handle boolean,
	id_handle character varying,
	original_uri_is_doi boolean,
	id_doi character varying,
	id_concept character varying,
	id_theso character varying,
	path character varying)
    RETURNS character varying
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
BEGIN
	IF (original_uri_is_ark = true AND id_ark IS NOT NULL AND id_ark != '') THEN
		return original_uri || '/' || id_ark;
	ELSIF (original_uri_is_ark = true AND (id_ark IS NULL or id_ark = '')) THEN
		return path || '?idc=' || id_concept || '&idt=' || id_theso;	
		
	ELSIF (original_uri_is_handle = true AND id_handle IS NOT NULL AND id_handle != '') THEN
		return 'https://hdl.handle.net/' || id_handle;
	ELSIF (original_uri_is_handle = true AND (id_handle IS NULL or id_handle = '')) THEN
		return path || '?idc=' || id_concept || '&idt=' || id_theso;			
		
	ELSIF (original_uri_is_doi = true AND id_doi IS NOT NULL AND id_doi != '') THEN
		return 'https://doi.org/' || id_doi;
	ELSIF (original_uri IS NOT NULL AND original_uri != '') THEN
		return original_uri || '/?idc=' || id_concept || '&idt=' || id_theso;
	ELSE
		return path || '?idc=' || id_concept || '&idt=' || id_theso;
	end if;
end;
$BODY$;

-- mise à jour de la focntion pour les notes
DROP FUNCTION IF EXISTS public.opentheso_get_notes(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_notes(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(note_id integer, note_notetypecode text, note_lexicalvalue character varying, note_created timestamp without time zone,
	note_modified timestamp without time zone, note_lang character varying, note_source character varying) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT note.id, note.notetypecode, note.lexicalvalue, note.created, note.modified, note.lang, note.notesource
		FROM note
		WHERE 
		note.id_thesaurus = id_theso
		AND note.identifier = id_con;
end;
$BODY$;