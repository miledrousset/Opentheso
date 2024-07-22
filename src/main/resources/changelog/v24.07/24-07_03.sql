
-- Mise à jour de la fonction de récupération des coordonnées GPS

DROP FUNCTION IF EXISTS public.opentheso_get_gps(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_gps(
	id_thesorus character varying,
	id_con character varying)
    RETURNS TABLE(gps_latitude double precision, gps_longitude double precision, pos integer) 
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