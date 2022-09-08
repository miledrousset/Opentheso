-- FUNCTION: public.opentheso_get_alter_term(character varying, character varying, boolean)

-- DROP FUNCTION public.opentheso_get_alter_term(character varying, character varying, boolean);

CREATE OR REPLACE FUNCTION public.opentheso_get_alter_term(
	id_theso character varying,
	id_con character varying,
	ishiden boolean)
    RETURNS TABLE(alter_term_lexical_value character varying, alter_term_source character varying, alter_term_status character varying, alter_term_lang character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;


-- FUNCTION: public.opentheso_get_gps(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_gps(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_gps(
	id_thesorus character varying,
	id_con character varying)
    RETURNS TABLE(gps_latitude double precision, gps_longitude double precision)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT latitude, longitude
		FROM gps
		WHERE id_theso = id_thesorus
		AND id_concept = id_con;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_groups(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_groups(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_groups(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(group_id text, group_id_ark text, group_id_handle character varying, group_id_doi character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT concept_group.idgroup, concept_group.id_ark, concept_group.id_handle, concept_group.id_doi
		FROM concept_group_concept, concept_group
		WHERE concept_group.idgroup = concept_group_concept.idgroup
		AND concept_group.idthesaurus = concept_group_concept.idthesaurus
		AND concept_group_concept.idthesaurus = id_theso
		AND concept_group_concept.idconcept = id_con;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_images(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_images(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_images(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(name character varying, copyright character varying, url character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT image_name, image_copyright, external_uri
		FROM external_images
		WHERE id_thesaurus = id_theso
		AND id_concept = id_con;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_note_concept(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_note_concept(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_note_concept(
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
		FROM note, note_type
		WHERE note.notetypecode = note_type.code
		AND note_type.isconcept = true
		AND note.id_thesaurus = id_theso
		AND note.id_concept = id_con;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_note_term(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_note_term(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_note_term(
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
		FROM note, note_type
		WHERE note.notetypecode = note_type.code
		AND note_type.isterm = true
		AND note.id_term IN (SELECT id_term
							 FROM preferred_term
							 WHERE id_thesaurus = id_theso
							 AND id_concept = id_con);

end;
$BODY$;


-- FUNCTION: public.opentheso_get_relations(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_relations(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_relations(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(relationship_id_concept character varying, relationship_role character varying, relationship_id_ark character varying, relationship_id_handle character varying, relationship_id_doi character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT id_concept2, role, id_ark, id_handle, id_doi
		FROM hierarchical_relationship as hr
				LEFT JOIN concept AS con ON id_concept = id_concept2 AND hr.id_thesaurus = con.id_thesaurus
		WHERE hr.id_thesaurus = id_theso
		AND id_concept1 = id_con;

end;
$BODY$;


-- FUNCTION: public.opentheso_get_traductions(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_traductions(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_traductions(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(term_id character varying, term_lexical_value character varying, term_lang character varying)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;


-- FUNCTION: public.opentheso_get_uri(boolean, character varying, character varying, boolean, character varying, boolean, character varying, character varying, character varying, character varying)

-- DROP FUNCTION public.opentheso_get_uri(boolean, character varying, character varying, boolean, character varying, boolean, character varying, character varying, character varying, character varying);

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
DECLARE
	uri varchar;
BEGIN
	IF (original_uri_is_ark = true) THEN
		IF (id_ark IS NOT NULL) THEN
			uri = original_uri || '/' || id_ark;
		END IF;
	ELSIF (original_uri_is_handle = true) THEN
		IF (id_handle IS NOT NULL) THEN
		  	uri = 'https://hdl.handle.net/' || id_handle;
		END IF;
	ELSIF (original_uri_is_doi = true) THEN
		IF (id_doi IS NOT NULL) THEN
		  	uri = 'https://doi.org/' || id_doi;
		END IF;
	ELSIF (original_uri IS NOT NULL) THEN
		uri = original_uri || '/?idc=' || id_concept || '&idt=' || id_theso;
	ELSE
		uri = path || '/?idc=' || id_concept || '&idt=' || id_theso;
	end if;

	return uri;
end;
$BODY$;



-- FUNCTION: public.opentheso_get_alignements(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_alignements(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_alignements(
	id_theso character varying,
	id_con character varying)
    RETURNS TABLE(alig_uri_target character varying, alig_id_type integer)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
	return query
		SELECT uri_target, alignement_id_type
		FROM concept con LEFT JOIN alignement ali ON ali.internal_id_concept = con.id_concept
		WHERE id_thesaurus = id_theso
		AND con.id_concept = id_con;
end;
$BODY$;


-- FUNCTION: public.opentheso_get_concepts(character varying, character varying, character varying)

-- DROP FUNCTION public.opentheso_get_concepts(character varying, character varying, character varying);

CREATE OR REPLACE FUNCTION opentheso_get_concepts (id_theso VARCHAR, path VARCHAR, id_lang VARCHAR) 
	RETURNS SETOF RECORD 
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
	status_candidat VARCHAR;
	date_candidat VARCHAR;
	message_candidat VARCHAR;
	vote_candidat text;
	messages_candidat text;
	creator text;
	contributor text;
BEGIN

	SELECT * INTO theso_rec FROM preferences where id_thesaurus = id_theso;
				 
	
	FOR con IN SELECT * FROM concept WHERE id_thesaurus = id_theso
    LOOP		
		-- URI
		uri = opentheso_get_uri(theso_rec.original_uri_is_ark, con.id_ark, theso_rec.original_uri, theso_rec.original_uri_is_handle,
					 con.id_handle, theso_rec.original_uri_is_doi, con.id_doi, con.id_concept, id_theso, path);
		
		-- LocalUri
		local_URI = path || '/?idc=' || con.id_concept || '&idt=' || id_theso;
		prefLab = '';
		
		
		vote_candidat = '';
		messages_candidat = '';
		status_candidat = '';
		date_candidat = '';
		message_candidat = '';
		
		IF (con.status = 'CA') THEN
			-- Recherche des votes
			FOR vote_record IN SELECT id_user, type_vote, id_note 
						FROM candidat_vote WHERE id_concept::varchar(255) = con.id_concept AND id_thesaurus = id_theso
			LOOP
				vote_candidat = vote_candidat || vote_record.id_user || sous_seperateur || vote_record.type_vote || sous_seperateur || vote_record.id_note || seperateur; 
			END LOOP;
			
			-- Recherche des messages
			FOR message_record IN SELECT users.id_user, cand.value, cand.date FROM candidat_messages cand, users 
						WHERE id_concept::varchar(255) = con.id_concept AND id_thesaurus = id_theso
			LOOP
				messages_candidat = messages_candidat || message_record.value || sous_seperateur || message_record.id_user || sous_seperateur || message_record.date || seperateur;
			END LOOP;
			
			FOR cadidat_record IN SELECT * FROM candidat_status WHERE id_concept::varchar(255) = con.id_concept
			LOOP
				status_candidat = cadidat_record.id_status;
				date_candidat = cadidat_record.date;
				message_candidat = cadidat_record.message;
			END LOOP;
		END IF;
		
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
		SELECT * INTO geo_rec FROM opentheso_get_gps(id_theso, con.id_concept);
		
		-- membre
		membre = '';
		FOR group_rec IN SELECT * FROM opentheso_get_groups(id_theso, con.id_concept)
		LOOP
			IF (group_rec.group_id_ark IS NOT NULL) THEN
				membre = membre || theso_rec.original_uri || '/' || group_rec.group_id_ark || seperateur;
			ELSIF (group_rec.group_id_handle IS NOT NULL) THEN
				membre = membre || 'https://hdl.handle.net/' || group_rec.group_id_handle || seperateur;
			ELSIF (theso_rec.original_uri IS NOT NULL) THEN
				membre = membre || theso_rec.original_uri || '/?idg=' || group_rec.group_idgroup || '&idt=' || id_theso || seperateur;
			ELSE
				membre = membre || path || '/?idc=' || group_rec.group_idgroup || '&idt=' || id_theso || seperateur;
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
		
		
		SELECT 	uri, con.status, local_URI, con.id_concept, con.id_ark, prefLab, altLab, altLab_hiden, definition, example, 
				editorialNote, changeNote, secopeNote, note, historyNote, con.notation, narrower, broader, related, exactMatch, closeMatch, 
				broadMatch, relatedMatch, narrowMatch, geo_rec.gps_latitude, geo_rec.gps_longitude, 
				membre, con.created, con.modified, img, status_candidat, date_candidat, message_candidat, vote_candidat, messages_candidat, creator, contributor INTO rec;
		
  		RETURN NEXT rec;
    END LOOP;
END;
$$