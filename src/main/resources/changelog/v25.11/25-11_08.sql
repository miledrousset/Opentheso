-- pour optimiser la recherche
ALTER TABLE term
    ADD COLUMN lexical_value_norm text GENERATED ALWAYS AS (f_unaccent(lower(lexical_value))) STORED;

CREATE INDEX idx_term_lexical_value_norm_trgm
    ON term USING gin (lexical_value_norm gin_trgm_ops);

ALTER TABLE non_preferred_term
    ADD COLUMN lexical_value_norm text
        GENERATED ALWAYS AS (f_unaccent(lower(lexical_value))) STORED;

CREATE INDEX idx_non_preferred_term_lexical_value_norm_trgm
    ON non_preferred_term USING gin (lexical_value_norm gin_trgm_ops);