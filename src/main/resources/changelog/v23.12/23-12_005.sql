
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
		FROM note, preferred_term
		WHERE preferred_term.id_term = note.id_term
		AND preferred_term.id_thesaurus = note.id_thesaurus
		
		AND note.notetypecode = 'definition'
		AND preferred_term.id_concept = idconcept
		AND preferred_term.id_thesaurus = idtheso
		AND note.lang = idlang;
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
	
    FOR con IN SELECT * FROM opentheso_get_narrowers(idtheso, idbt)
    LOOP

		-- URI
		local_uri = theso_rec.chemin_site || '?idc=' || con.idconcept2 || '&idt=' || idtheso;
					 
		-- prefLab
		preflab = '';
		SELECT * INTO preflab_rec FROM opentheso_get_conceptlabel(idtheso, con.idconcept2, idlang);
		preflab = preflab_rec.libelle;
		
		-- altLab
		altlabel = '';
		FOR altlab_rec IN SELECT * FROM opentheso_get_altlabel(idtheso, idbt, idlang, false)
		LOOP
			altlabel = altlabel || altlab_rec.altlabel || seperateur;
		END LOOP;	
		
		-- definition
		definition = '';
		FOR definition_rec IN SELECT * FROM opentheso_get_definitions(idtheso, idbt, idlang)
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


