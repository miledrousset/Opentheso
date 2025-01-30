-- FUNCTION: public.opentheso_get_list_narrower_forgraph(character varying, character varying, character varying)

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
	image_rec record;

    havechildren boolean;
    prefLab VARCHAR;
	altlabel VARCHAR;
	definition text;
	local_uri text;
	image text;

BEGIN
SELECT * INTO theso_rec FROM preferences where id_thesaurus = idtheso;

FOR con IN SELECT * FROM opentheso_get_narrowers_ignorefacet(idtheso, idbt)
                             LOOP

                         -- URI
    local_uri = opentheso_get_uri(theso_rec.original_uri_is_ark, con.idark, theso_rec.original_uri, theso_rec.original_uri_is_handle,
					 con.idhandle, theso_rec.original_uri_is_doi, con.iddoi, con.idconcept2, idtheso, theso_rec.chemin_site);

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

		-- images
		image = '';
FOR image_rec IN SELECT * FROM opentheso_get_images(idtheso, con.idconcept2)
                                   LOOP
    image = image || image_rec.url || seperateur;
END LOOP;

		-- childrens
		havechildren = false;
SELECT opentheso_ishave_children(idtheso, con.idconcept2) INTO havechildren;

-- return
SELECT con.idconcept2, local_uri, con.status, prefLab, altlabel, definition, havechildren, image INTO rec;

RETURN NEXT rec;
END LOOP;
END;
$BODY$;

-- FUNCTION: public.opentheso_get_list_topterm_forgraph(character varying, character varying)

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
	image_rec record;

    havechildren boolean;
    prefLab VARCHAR;
	altlabel VARCHAR;
	definition text;
	local_uri text;
	image text;

BEGIN
SELECT * INTO theso_rec FROM preferences where id_thesaurus = idtheso;

FOR con IN SELECT * FROM opentheso_get_topterms(idtheso)
                             LOOP

                         -- URI
                         --	local_uri = theso_rec.chemin_site || '?idc=' || con.idconcept || '&idt=' || idtheso;

    local_uri = opentheso_get_uri(theso_rec.original_uri_is_ark, con.idark, theso_rec.original_uri, theso_rec.original_uri_is_handle,
					 con.idhandle, theso_rec.original_uri_is_doi, con.iddoi, con.idconcept, idtheso, theso_rec.chemin_site);

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

		-- images
		image = '';
FOR image_rec IN SELECT * FROM opentheso_get_images(idtheso, con.idconcept)
                                   LOOP
    image = image || image_rec.url || seperateur;
END LOOP;

		-- childrens
		havechildren = false;
SELECT opentheso_ishave_children(idtheso, con.idconcept) INTO havechildren;

-- return
SELECT con.idconcept, local_uri, con.status, prefLab, altlabel, definition, havechildren, image INTO rec;

RETURN NEXT rec;
END LOOP;
END;
$BODY$;





