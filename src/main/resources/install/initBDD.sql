-- * Author:  miled.rousset
-- * Created: 15 avr. 2016
-- * Modified : 02/08/2018
-- */


-- # !!!!!!! Attention !!!!!!! opération irréversible
-- # Opération à appliquer pour l'installation et la préparation de la BDD  

-- # Suppression de tous les thésaurus et des données
delete from alignement;
delete from concept;
delete from concept_candidat;
delete from concept_fusion;
delete from concept_group;
delete from concept_group_concept;
delete from concept_group_historique;
delete from concept_group_label;
delete from concept_group_label_historique;
delete from concept_historique;
delete from concept_orphan;
delete from concept_term_candidat;
delete from gps;
delete from hierarchical_relationship;
delete from hierarchical_relationship_historique;
delete from images;
delete from external_images;
delete from non_preferred_term;
delete from non_preferred_term_historique;
delete from note;
delete from node_label;
delete from note_historique;
delete from permuted;
delete from preferences;
delete from preferred_term;
delete from proposition;
delete from term;
delete from term_candidat;
delete from term_historique;
delete from thesaurus;
delete from thesaurus_array;
delete from thesaurus_label;
--delete from user_role;
delete from users;
delete from users_historique;
delete from thesaurus_array_concept;
delete from relation_group;
delete from alignement_preferences;
delete from gps_preferences;
delete from copyright;
delete from info;
delete from thesaurus_alignement_source;
delete from preferences_sparql;
delete from user_role_only_on;
delete from user_role_group;
delete from user_group_thesaurus;
delete from user_group_label;
delete from routine_mail;




-- # initialisation des séquences 
ALTER SEQUENCE alignement_id_seq RESTART WITH 1;
ALTER SEQUENCE concept__id_seq RESTART WITH 1;
ALTER SEQUENCE concept_candidat__id_seq RESTART WITH 1;
ALTER SEQUENCE concept_group__id_seq RESTART WITH 1;
ALTER SEQUENCE concept_group_historique__id_seq RESTART WITH 1;
ALTER SEQUENCE concept_group_label_id_seq RESTART WITH 1;
ALTER SEQUENCE concept_group_label_historique__id_seq RESTART WITH 1;
ALTER SEQUENCE concept_historique__id_seq RESTART WITH 1;
ALTER SEQUENCE facet_id_seq RESTART WITH 1;
ALTER SEQUENCE note__id_seq RESTART WITH 1;
ALTER SEQUENCE note_historique__id_seq RESTART WITH 1;
ALTER SEQUENCE pref__id_seq RESTART WITH 1;
ALTER SEQUENCE term__id_seq RESTART WITH 1;
ALTER SEQUENCE term_candidat__id_seq RESTART WITH 1;
ALTER SEQUENCE term_historique__id_seq RESTART WITH 1;
ALTER SEQUENCE thesaurus_id_seq RESTART WITH 1;
ALTER SEQUENCE user__id_seq RESTART WITH 2;
ALTER SEQUENCE user_group_label__id_seq RESTART WITH 1;
ALTER SEQUENCE alignement_preferences_id_seq RESTART WITH 1;
ALTER SEQUENCE gps_preferences_id_seq RESTART WITH 1;



-- # créarion de l'admin pour la première connexion
INSERT INTO users (id_user, username, password, active, mail, passtomodify,alertmail, issuperadmin) VALUES
 (1, 'admin', '21232f297a57a5a743894a0e4a801fc3', true, 'admin@domaine.fr', false, false, true);

