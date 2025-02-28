-- FUNCTION: public.opentheso_get_uri(boolean, character varying, character varying, boolean, character varying, boolean, character varying, character varying, character varying, character varying)

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

	path := (CASE
            WHEN RIGHT(path, 1) = '/' THEN path  -- Si path se termine déjà par '/', on ne change rien
            ELSE path || '/'  -- Sinon, on ajoute un '/'
         END);

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
