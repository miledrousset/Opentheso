CREATE OR REPLACE procedure opentheso_add_terms(
	id_term character varying,
	id_thesaurus character varying,
	id_concept character varying,
	id_user int,
	terms text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@';
	term_rec record;
	array_string   text[];
BEGIN
	--label.getLabel() + SOUS_SEPERATEUR + label.getLanguage()
	FOR term_rec IN SELECT unnest(string_to_array(terms, seperateur)) AS term_value
    LOOP
		SELECT string_to_array(term_rec.term_value, sous_seperateur) INTO array_string;
            
      	Insert into term (id_term, lexical_value, lang, id_thesaurus, created, modified, source, status, contributor) 
			values (id_term, array_string[1], array_string[2], id_thesaurus, CURRENT_DATE, CURRENT_DATE, '', '', id_user);
	END LOOP;
	
	-- Insert link term
	Insert into preferred_term (id_concept, id_term, id_thesaurus) values (id_concept, id_term, id_thesaurus);
END;
$BODY$;





CREATE OR REPLACE procedure opentheso_add_hierarchical_relations(
	id_thesaurus character varying,
	relations text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@';
	
	relations_rec record;
	array_string   text[];
BEGIN

	FOR relations_rec IN SELECT unnest(string_to_array(relations, seperateur)) AS relation_value
    LOOP
		SELECT string_to_array(relations_rec.relation_value, sous_seperateur) INTO array_string;
		
		INSERT INTO hierarchical_relationship (id_concept1, id_thesaurus, role, id_concept2) 
			VALUES (array_string[1], id_thesaurus, array_string[2], array_string[3]); 
	END LOOP;
END;
$BODY$;




CREATE OR REPLACE procedure opentheso_add_notes(
	id_concept character varying,
	id_thesaurus character varying,
	id_user int,
	notes text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@';
	
	notes_rec record;
	array_string   text[];
BEGIN

	FOR notes_rec IN SELECT unnest(string_to_array(notes, seperateur)) AS note_value
    LOOP
		SELECT string_to_array(notes_rec.note_value, sous_seperateur) INTO array_string;
		
		if (array_string[2] = 'customnote' OR array_string[2] = 'scopeNote' OR array_string[2] = 'note')  THEN
			insert into note (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue, id_user) 
				values (array_string[2], id_thesaurus, id_concept, array_string[3], array_string[1], id_user);
		END IF;
		
		if (array_string[2] = 'definition' OR array_string[2] = 'historyNote' OR array_string[2] = 'editorialNote'
		   		OR array_string[2] = 'changeNote' OR array_string[2] = 'example')  THEN
			Insert into note (notetypecode, id_thesaurus, id_term, lang, lexicalvalue, id_user) 
				values (array_string[2], id_thesaurus, array_string[4], array_string[3], array_string[1], id_user);
		END IF;	
		
		if (id_user != '-1') THEN
			Insert into note_historique (notetypecode, id_thesaurus, id_concept, lang, lexicalvalue, action_performed, id_user)
				values (array_string[2], id_thesaurus, id_concept, array_string[3], array_string[1], 'add', id_user);
		END IF;
	END LOOP;
END;
$BODY$;



CREATE OR REPLACE procedure opentheso_add_non_preferred_term(id_thesaurus character varying,
			id_user int, 
			non_pref_terms text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@';
	
	non_pref_rec record;
	array_string   text[];
BEGIN

	FOR non_pref_rec IN SELECT unnest(string_to_array(non_pref_terms, seperateur)) AS non_pref_value
    LOOP
		SELECT string_to_array(non_pref_rec.non_pref_value, sous_seperateur) INTO array_string;
		-- 'id_term@lexical_value@lang@id_thesaurus@source@status@hiden'
		Insert into non_preferred_term (id_term, lexical_value, lang, id_thesaurus, source, status, hiden)
			values (array_string[1], array_string[2], array_string[3], array_string[4], array_string[5], array_string[6], CAST(array_string[7] AS BOOLEAN));
			
		Insert into non_preferred_term_historique (id_term, lexical_value, lang, id_thesaurus, source, status, id_user, action)
			values (array_string[1], array_string[2], array_string[3], id_thesaurus, array_string[4], array_string[5], id_user, 'ADD');	
	END LOOP;
END;
$BODY$;



CREATE OR REPLACE procedure opentheso_add_external_images(
	id_thesaurus character varying,
	id_concept character varying,
	id_user int,
	images text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@';
    images_rec record;
BEGIN

    FOR images_rec IN SELECT unnest(string_to_array(images, seperateur)) AS image_value
    LOOP
		Insert into external_images (id_concept, id_thesaurus, id_user, image_name, external_uri, image_copyright) 
            values (id_concept, id_thesaurus, id_user, '', images_rec.image_value, '');	
    END LOOP;
END;
$BODY$;




CREATE OR REPLACE procedure opentheso_add_alignements(alignements text) 
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@';
	
	alignements_rec record;
	array_string   text[];
BEGIN

	FOR alignements_rec IN SELECT unnest(string_to_array(alignements, seperateur)) AS alignement_value
    LOOP
		SELECT string_to_array(alignements_rec.alignement_value, sous_seperateur) INTO array_string;
		
		Insert into alignement (author, concept_target, thesaurus_target, uri_target, alignement_id_type, internal_id_thesaurus, internal_id_concept) 
			values (CAST(array_string[1] AS int), array_string[2], array_string[3], array_string[4], CAST(array_string[5] AS int), array_string[6], array_string[7]);
	END LOOP;
END;
$BODY$;



CREATE OR REPLACE procedure opentheso_add_new_concept(
	id_thesaurus character varying,
	id_con character varying,
	id_user int,
	conceptStatus character varying,
	notationConcept character varying,
	id_ark character varying, 
	isTopConcept Boolean, 
	id_handle character varying,
	id_doi character varying,
	prefterms text,
	relation_hiarchique text,
	notes text,
	non_pref_terms text,
	alignements text,
	images text,
	idsConceptsReplaceBy text,
	altitude double precision,
	longitude double precision)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    id_new_concet character varying;
    idConceptReplaceBy character varying;
    seperateur constant varchar := '##';
    concept_Rep_rec record; 
    replaces_rec record;
BEGIN

	Insert into concept (id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id_handle, id_doi, creator, contributor) 
		values (id_con, id_thesaurus, id_ark, CURRENT_DATE, CURRENT_DATE, conceptStatus, notationConcept, isTopConcept, id_handle, id_doi, id_user, id_user);
		
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
		
       	IF (altitude > 0 AND longitude > 0) THEN
            insert into gps values(id_new_concet, id_thesaurus, altitude, longitude);
        END IF;		
    END IF;
END;
$BODY$;




CREATE OR REPLACE procedure opentheso_add_facet(
	id_facet character varying,
	id_thesaurus character varying,
	id_conceotParent character varying,
	labels text,
	membres text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
	seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@';
	
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
END;
$BODY$;;