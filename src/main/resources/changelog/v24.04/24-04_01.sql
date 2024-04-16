
-- récupération de toutes les langues d'un thésaurus

DROP FUNCTION IF EXISTS public.opentheso_get_all_lang_of_theso(character varying);
CREATE OR REPLACE FUNCTION public.opentheso_get_all_lang_of_theso(
    id_theso character varying)
  RETURNS TABLE(id_lang character varying) 
  LANGUAGE 'plpgsql'
  COST 100
  VOLATILE PARALLEL UNSAFE
  ROWS 1000

AS $BODY$
DECLARE
    lang_rec record;
BEGIN
    return query
        select distinct lang from term where id_thesaurus = id_theso;
end;
$BODY$;


-- déduplication des notes

DROP PROCEDURE IF EXISTS public.opentheso_deduplicate_notes();
CREATE OR REPLACE PROCEDURE public.opentheso_deduplicate_notes()
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    theso_rec record;
    lang_rec record;
    notes_id record;
    notes_rec record;

    notes_temp character varying; 
    notes_concat character varying; 
    notes_tab character varying[]; 
BEGIN
    -- on récupère tous les thésaurus 
    FOR theso_rec IN (SELECT id_thesaurus from thesaurus)    
    LOOP    
        -- on récupère toutes les langues d'un thésaurus
        FOR lang_rec IN SELECT * from opentheso_get_all_lang_of_theso(theso_rec.id_thesaurus)
        LOOP

            -- on récupère toutes les notes dans une langue et par concept 
            FOR notes_id IN (select identifier from note  
                            where  
                            id_thesaurus = theso_rec.id_thesaurus  
                            and  
                            lang = lang_rec.id_lang  
                            and notetypecode = 'definition'  
                            and identifier in (select id_concept from concept where id_thesaurus = theso_rec.id_thesaurus)  
                            group by identifier having count(*) > 1)
            LOOP
                -- définition
                notes_concat := '';
                notes_tab := ARRAY[]::character varying[];
                --RAISE NOTICE 'idTheso : %, idNote : %, type : %', theso_rec.id_thesaurus,  notes_id.identifier, 'definition';
                FOR notes_rec IN select * from note where identifier = notes_id.identifier
                    and notetypecode = 'definition' 
                    and id_thesaurus = theso_rec.id_thesaurus 
                    and lang = lang_rec.id_lang
                LOOP
                -- Vérifier si l'élément existe déjà dans le tableau
                    IF NOT notes_rec.lexicalvalue = ANY(notes_tab) THEN
                        notes_tab := array_append(notes_tab, notes_rec.lexicalvalue);
                    end if;
                END LOOP;

              FOREACH notes_temp IN ARRAY notes_tab LOOP
              --RAISE NOTICE '%', notes_concat;
                    if notes_concat = '' THEN
                        notes_concat := notes_temp;
                    else
                        notes_concat := notes_concat ||  '</br>' || notes_temp;
                    end if;
              END LOOP;
                -- RAISE NOTICE '%', notes_concat;

                -- suppression des lignes en double
                delete from note where 
                      note.id_thesaurus = theso_rec.id_thesaurus
                    and note.identifier = notes_id.identifier
                    and lang = lang_rec.id_lang
                    and notetypecode = 'definition';

                    -- ajout des notes concaténées
                    insert into note (id, notetypecode, id_thesaurus, id_term, id_concept, 
                        lang, lexicalvalue, created, modified, id_user, notesource, identifier)
                        values (notes_rec.id, notes_rec.notetypecode, notes_rec.id_thesaurus,
                        notes_rec.id_term, notes_rec.id_concept, 
                        notes_rec.lang, notes_concat, 
                        notes_rec.created, notes_rec.modified, 
                        notes_rec.id_user, notes_rec.notesource, notes_rec.identifier);


                -- note
                notes_concat := '';
                notes_tab := ARRAY[]::character varying[];
                --RAISE NOTICE 'idTheso : %, idNote : %, type : %', theso_rec.id_thesaurus,  notes_id.identifier, 'definition';
                FOR notes_rec IN select * from note where identifier = notes_id.identifier
                    and notetypecode = 'note' 
                    and id_thesaurus = theso_rec.id_thesaurus 
                    and lang = lang_rec.id_lang
                LOOP
                -- Vérifier si l'élément existe déjà dans le tableau
                    IF NOT notes_rec.lexicalvalue = ANY(notes_tab) THEN
                        notes_tab := array_append(notes_tab, notes_rec.lexicalvalue);
                    end if;
                END LOOP;

              FOREACH notes_temp IN ARRAY notes_tab LOOP
              --RAISE NOTICE '%', notes_concat;
                    if notes_concat = '' THEN
                        notes_concat := notes_temp;
                    else
                        notes_concat := notes_concat ||  '</br>' || notes_temp;
                    end if;
              END LOOP;
                -- RAISE NOTICE '%', notes_concat;

                -- suppression des lignes en double
                delete from note where 
                      note.id_thesaurus = theso_rec.id_thesaurus
                    and note.identifier = notes_id.identifier
                    and lang = lang_rec.id_lang
                    and notetypecode = 'note';

                    -- ajout des notes concaténées
                    insert into note (id, notetypecode, id_thesaurus, id_term, id_concept, 
                        lang, lexicalvalue, created, modified, id_user, notesource, identifier)
                        values (notes_rec.id, notes_rec.notetypecode, notes_rec.id_thesaurus,
                        notes_rec.id_term, notes_rec.id_concept, 
                        notes_rec.lang, notes_concat, 
                        notes_rec.created, notes_rec.modified, 
                        notes_rec.id_user, notes_rec.notesource, notes_rec.identifier);


                -- changeNote
                notes_concat := '';
                notes_tab := ARRAY[]::character varying[];
                --RAISE NOTICE 'idTheso : %, idNote : %, type : %', theso_rec.id_thesaurus,  notes_id.identifier, 'definition';
                FOR notes_rec IN select * from note where identifier = notes_id.identifier
                    and notetypecode = 'changeNote' 
                    and id_thesaurus = theso_rec.id_thesaurus 
                    and lang = lang_rec.id_lang
                LOOP
                -- Vérifier si l'élément existe déjà dans le tableau
                    IF NOT notes_rec.lexicalvalue = ANY(notes_tab) THEN
                        notes_tab := array_append(notes_tab, notes_rec.lexicalvalue);
                    end if;
                END LOOP;

              FOREACH notes_temp IN ARRAY notes_tab LOOP
              --RAISE NOTICE '%', notes_concat;
                    if notes_concat = '' THEN
                        notes_concat := notes_temp;
                    else
                        notes_concat := notes_concat ||  '</br>' || notes_temp;
                    end if;
              END LOOP;
                -- RAISE NOTICE '%', notes_concat;

                -- suppression des lignes en double
                delete from note where 
                      note.id_thesaurus = theso_rec.id_thesaurus
                    and note.identifier = notes_id.identifier
                    and lang = lang_rec.id_lang
                    and notetypecode = 'changeNote';

                    -- ajout des notes concaténées
                    insert into note (id, notetypecode, id_thesaurus, id_term, id_concept, 
                        lang, lexicalvalue, created, modified, id_user, notesource, identifier)
                        values (notes_rec.id, notes_rec.notetypecode, notes_rec.id_thesaurus,
                        notes_rec.id_term, notes_rec.id_concept, 
                        notes_rec.lang, notes_concat, 
                        notes_rec.created, notes_rec.modified, 
                        notes_rec.id_user, notes_rec.notesource, notes_rec.identifier);


                -- editorialNote
                notes_concat := '';
                notes_tab := ARRAY[]::character varying[];
                --RAISE NOTICE 'idTheso : %, idNote : %, type : %', theso_rec.id_thesaurus,  notes_id.identifier, 'definition';
                FOR notes_rec IN select * from note where identifier = notes_id.identifier
                    and notetypecode = 'editorialNote' 
                    and id_thesaurus = theso_rec.id_thesaurus 
                    and lang = lang_rec.id_lang
                LOOP
                -- Vérifier si l'élément existe déjà dans le tableau
                    IF NOT notes_rec.lexicalvalue = ANY(notes_tab) THEN
                        notes_tab := array_append(notes_tab, notes_rec.lexicalvalue);
                    end if;
                END LOOP;

              FOREACH notes_temp IN ARRAY notes_tab LOOP
              --RAISE NOTICE '%', notes_concat;
                    if notes_concat = '' THEN
                        notes_concat := notes_temp;
                    else
                        notes_concat := notes_concat ||  '</br>' || notes_temp;
                    end if;
              END LOOP;
                -- RAISE NOTICE '%', notes_concat;

                -- suppression des lignes en double
                delete from note where 
                      note.id_thesaurus = theso_rec.id_thesaurus
                    and note.identifier = notes_id.identifier
                    and lang = lang_rec.id_lang
                    and notetypecode = 'editorialNote';

                    -- ajout des notes concaténées
                    insert into note (id, notetypecode, id_thesaurus, id_term, id_concept, 
                        lang, lexicalvalue, created, modified, id_user, notesource, identifier)
                        values (notes_rec.id, notes_rec.notetypecode, notes_rec.id_thesaurus,
                        notes_rec.id_term, notes_rec.id_concept, 
                        notes_rec.lang, notes_concat, 
                        notes_rec.created, notes_rec.modified, 
                        notes_rec.id_user, notes_rec.notesource, notes_rec.identifier);


                -- example
                notes_concat := '';
                notes_tab := ARRAY[]::character varying[];
                --RAISE NOTICE 'idTheso : %, idNote : %, type : %', theso_rec.id_thesaurus,  notes_id.identifier, 'definition';
                FOR notes_rec IN select * from note where identifier = notes_id.identifier
                    and notetypecode = 'example' 
                    and id_thesaurus = theso_rec.id_thesaurus 
                    and lang = lang_rec.id_lang
                LOOP
                -- Vérifier si l'élément existe déjà dans le tableau
                    IF NOT notes_rec.lexicalvalue = ANY(notes_tab) THEN
                        notes_tab := array_append(notes_tab, notes_rec.lexicalvalue);
                    end if;
                END LOOP;

              FOREACH notes_temp IN ARRAY notes_tab LOOP
              --RAISE NOTICE '%', notes_concat;
                    if notes_concat = '' THEN
                        notes_concat := notes_temp;
                    else
                        notes_concat := notes_concat ||  '</br>' || notes_temp;
                    end if;
              END LOOP;
                -- RAISE NOTICE '%', notes_concat;

                -- suppression des lignes en double
                delete from note where 
                      note.id_thesaurus = theso_rec.id_thesaurus
                    and note.identifier = notes_id.identifier
                    and lang = lang_rec.id_lang
                    and notetypecode = 'example';

                    -- ajout des notes concaténées
                    insert into note (id, notetypecode, id_thesaurus, id_term, id_concept, 
                        lang, lexicalvalue, created, modified, id_user, notesource, identifier)
                        values (notes_rec.id, notes_rec.notetypecode, notes_rec.id_thesaurus,
                        notes_rec.id_term, notes_rec.id_concept, 
                        notes_rec.lang, notes_concat, 
                        notes_rec.created, notes_rec.modified, 
                        notes_rec.id_user, notes_rec.notesource, notes_rec.identifier);


                -- historyNote
                notes_concat := '';
                notes_tab := ARRAY[]::character varying[];
                --RAISE NOTICE 'idTheso : %, idNote : %, type : %', theso_rec.id_thesaurus,  notes_id.identifier, 'definition';
                FOR notes_rec IN select * from note where identifier = notes_id.identifier
                    and notetypecode = 'historyNote' 
                    and id_thesaurus = theso_rec.id_thesaurus 
                    and lang = lang_rec.id_lang
                LOOP
                -- Vérifier si l'élément existe déjà dans le tableau
                    IF NOT notes_rec.lexicalvalue = ANY(notes_tab) THEN
                        notes_tab := array_append(notes_tab, notes_rec.lexicalvalue);
                    end if;
                END LOOP;

              FOREACH notes_temp IN ARRAY notes_tab LOOP
              --RAISE NOTICE '%', notes_concat;
                    if notes_concat = '' THEN
                        notes_concat := notes_temp;
                    else
                        notes_concat := notes_concat ||  '</br>' || notes_temp;
                    end if;
              END LOOP;
                -- RAISE NOTICE '%', notes_concat;

                -- suppression des lignes en double
                delete from note where 
                      note.id_thesaurus = theso_rec.id_thesaurus
                    and note.identifier = notes_id.identifier
                    and lang = lang_rec.id_lang
                    and notetypecode = 'historyNote';

                    -- ajout des notes concaténées
                    insert into note (id, notetypecode, id_thesaurus, id_term, id_concept, 
                        lang, lexicalvalue, created, modified, id_user, notesource, identifier)
                        values (notes_rec.id, notes_rec.notetypecode, notes_rec.id_thesaurus,
                        notes_rec.id_term, notes_rec.id_concept, 
                        notes_rec.lang, notes_concat, 
                        notes_rec.created, notes_rec.modified, 
                        notes_rec.id_user, notes_rec.notesource, notes_rec.identifier);


                -- scopeNote
                notes_concat := '';
                notes_tab := ARRAY[]::character varying[];
                --RAISE NOTICE 'idTheso : %, idNote : %, type : %', theso_rec.id_thesaurus,  notes_id.identifier, 'definition';
                FOR notes_rec IN select * from note where identifier = notes_id.identifier
                    and notetypecode = 'scopeNote' 
                    and id_thesaurus = theso_rec.id_thesaurus 
                    and lang = lang_rec.id_lang
                LOOP
                -- Vérifier si l'élément existe déjà dans le tableau
                    IF NOT notes_rec.lexicalvalue = ANY(notes_tab) THEN
                        notes_tab := array_append(notes_tab, notes_rec.lexicalvalue);
                    end if;
                END LOOP;

              FOREACH notes_temp IN ARRAY notes_tab LOOP
              --RAISE NOTICE '%', notes_concat;
                    if notes_concat = '' THEN
                        notes_concat := notes_temp;
                    else
                        notes_concat := notes_concat ||  '</br>' || notes_temp;
                    end if;
              END LOOP;
                -- RAISE NOTICE '%', notes_concat;

                -- suppression des lignes en double
                delete from note where 
                      note.id_thesaurus = theso_rec.id_thesaurus
                    and note.identifier = notes_id.identifier
                    and lang = lang_rec.id_lang
                    and notetypecode = 'scopeNote';

                    -- ajout des notes concaténées
                    insert into note (id, notetypecode, id_thesaurus, id_term, id_concept, 
                        lang, lexicalvalue, created, modified, id_user, notesource, identifier)
                        values (notes_rec.id, notes_rec.notetypecode, notes_rec.id_thesaurus,
                        notes_rec.id_term, notes_rec.id_concept, 
                        notes_rec.lang, notes_concat, 
                        notes_rec.created, notes_rec.modified, 
                        notes_rec.id_user, notes_rec.notesource, notes_rec.identifier);


            END LOOP;
        END LOOP;
    END LOOP;
END;
$BODY$;
