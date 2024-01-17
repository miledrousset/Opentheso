DROP FUNCTION IF EXISTS public.opentheso_get_topterms(character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_topterms(
	id_theso character varying)
    RETURNS TABLE(notation character varying, status character varying, idconcept character varying) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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
$BODY$;


--

DROP FUNCTION IF EXISTS public.opentheso_get_list_topterm_forgraph(character varying, character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_list_topterm_forgraph(
	idtheso character varying,
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
$BODY$;

