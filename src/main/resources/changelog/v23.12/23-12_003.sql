DROP TABLE IF EXISTS public.images; 


DROP PROCEDURE IF EXISTS public.opentheso_add_new_concept(character varying, character varying, integer, character varying, text, character varying, character varying, boolean, character varying, character varying, text, text, text, text, text, text, text, text, boolean, double precision, double precision, date, date, text);

--

DROP FUNCTION IF EXISTS public.opentheso_get_conceptlabel(character varying, character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_conceptlabel(
	id_theso character varying,
	id_con character varying,
	id_lang character varying)
    RETURNS TABLE(idterm character varying, libelle character varying, unique_id integer) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
       return query
	   		select term.id_term, term.lexical_value, term.id from term, preferred_term where
                        preferred_term.id_term = term.id_term AND
                        preferred_term.id_thesaurus = term.id_thesaurus
                        and term.id_thesaurus = id_theso
                        and preferred_term.id_concept = id_con
                        and term.lang = id_lang;
end;
$BODY$;

DROP FUNCTION IF EXISTS public.opentheso_get_grouplabel(character varying, character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_grouplabel(
	id_theso character varying,
	id_group character varying,
	id_lang character varying)
    RETURNS TABLE(libelle text) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
       return query
	   		select lexicalvalue from concept_group_label where
                        idthesaurus = id_theso
                        and LOWER(idgroup) = LOWER(id_group)
                        and lang = id_lang;
end;
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_get_gps(character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_gps(
	id_thesorus character varying,
	id_con character varying)
    RETURNS TABLE(gps_latitude double precision, gps_longitude double precision, pos int) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
    return query
        SELECT latitude, longitude, gps.position
        FROM gps
        WHERE id_theso = id_thesorus
          AND id_concept = id_con;

end;
$BODY$;

DROP FUNCTION IF EXISTS public.opentheso_get_traductions(character varying, character varying);
DROP FUNCTION IF EXISTS public.opentheso_get_preflabel_traductions(character varying, character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_preflabel_traductions(
	id_theso character varying,
	id_con character varying,
	id_lang character varying)
    RETURNS TABLE(term_id character varying, term_lexical_value character varying, term_lang character varying, unique_id int) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_get_all_preflabel(character varying, character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_all_preflabel(
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

--

DROP FUNCTION IF EXISTS public.opentheso_get_altlabel(character varying, character varying, character varying, boolean);
CREATE OR REPLACE FUNCTION public.opentheso_get_altlabel(
	idtheso character varying,
	idconcept character varying,
	idlang character varying,
	ishiden boolean)
    RETURNS TABLE(idterm character varying, altlabel character varying, unique_id integer) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_get_altlabel_traductions(character varying, character varying, character varying, boolean);
CREATE OR REPLACE FUNCTION public.opentheso_get_altlabel_traductions(
	idtheso character varying,
	idconcept character varying,
	idlang character varying,
	ishiden boolean)
	RETURNS TABLE(altlabel_id character varying, altlabel character varying, idlang_alt character varying, unique_id int) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

--

--

DROP FUNCTION IF EXISTS opentheso_ishave_children(character varying,character varying);
CREATE OR REPLACE FUNCTION public.opentheso_ishave_children(
    id_theso character varying,
    id_co character varying)
    RETURNS boolean
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE

AS $BODY$
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
$BODY$;

--

DROP FUNCTION IF EXISTS opentheso_isconcept_havefacet(character varying,character varying);
CREATE OR REPLACE FUNCTION public.opentheso_isconcept_havefacet(
    id_theso character varying,
    id_co character varying)
    RETURNS boolean
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE

AS $BODY$
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
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_isfacet_hasmembers(character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_isfacet_hasmembers(
	idtheso character varying,
	idfacet character varying)
    RETURNS boolean
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
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
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_get_labelfacet(character varying, character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_labelfacet(
	id_theso character varying,
	idfacet character varying,
	id_lang character varying)
    RETURNS TABLE(libelle character varying) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$

begin
       return query
	   		select lexical_value from node_label where
                        id_facet = idfacet
                        and id_thesaurus = id_theso
                        and lang = id_lang;

end;
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_get_narrowers(character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_narrowers(
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
		and id_concept2 
		not in (
			select id_concept from concept_facet, thesaurus_array 
			where concept_facet.id_facet = thesaurus_array.id_facet 
			and concept_facet.id_thesaurus = thesaurus_array.id_thesaurus 
			and thesaurus_array.id_concept_parent = id_bt 
			and thesaurus_array.id_thesaurus = id_theso)
		ORDER BY concept.notation ASC;
end;
$BODY$;

--

DROP FUNCTION IF EXISTS public.opentheso_get_facets_of_concept(character varying, character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_facets_of_concept(
	id_theso character varying,
	id_con character varying,
	id_lang character varying)
    RETURNS SETOF record 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;

--
DROP FUNCTION IF EXISTS public.opentheso_get_list_narrower_fortree(character varying, character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_list_narrower_fortree(
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
		FOR note_concept_rec IN SELECT * FROM opentheso_get_note_concept(idtheso, idconcept)
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

		FOR note_term_rec IN SELECT * FROM opentheso_get_note_term(idtheso, idconcept)
		LOOP
			IF (note_term_rec.note_notetypecode = 'note') THEN
				note = note || note_term_rec.note_id || sous_seperateur || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_term_rec.note_id || sous_seperateur || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_term_rec.note_id || sous_seperateur || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_term_rec.note_id || sous_seperateur || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'example') THEN
				example = example || note_term_rec.id || sous_seperateur || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_term_rec.note_id || sous_seperateur || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_term_rec.note_id || sous_seperateur || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
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



