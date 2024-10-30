ALTER TABLE candidat_messages
    ALTER COLUMN id_concept TYPE VARCHAR(255) USING id_concept::VARCHAR;
