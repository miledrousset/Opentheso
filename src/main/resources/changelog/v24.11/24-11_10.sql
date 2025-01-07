DO $$
        BEGIN
        -- Vérifie si la colonne "position" existe dans la table "gps"
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'gps' AND column_name = 'position') THEN
        -- Ajoute la colonne "position" si elle n'existe pas
        ALTER TABLE public.gps ADD COLUMN "position" integer;
        END IF;
        END $$;