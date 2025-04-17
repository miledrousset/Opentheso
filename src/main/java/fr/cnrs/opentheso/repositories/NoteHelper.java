package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.utils.StringUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Slf4j
@Service
public class NoteHelper {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private LanguageHelper languageHelper;
       
    /**
     * Nouvelle méthode qui va avec la restructuration des notes
     * Cette fonction permet d'ajouter une Note, la note peut être pour :
     * un concept
     * un term
     * un groupe
     * une facette
     */
    public boolean addNote(String identifier, String idLang, String idThesaurus, String note, String noteTypeCode,
                           String noteSource, int idUser) {

        boolean status = false;
         
        note = StringUtils.clearValue(note);
        note = StringEscapeUtils.unescapeXml(note);
        note = StringUtils.convertString(note);
        noteSource = fr.cnrs.opentheso.utils.StringUtils.convertString(noteSource);

        idLang = languageHelper.normalizeIdLang(idLang);
        
        if(isNoteExistInThatLang(identifier, idThesaurus, idLang, noteTypeCode)) {
            try (Connection conn = dataSource.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("update note set lexicalvalue = '" + note +  "'" 
                            + ", notesource = '" + noteSource + "'"
                            + " where id_thesaurus = '" + idThesaurus + "'"
                            + " and notetypecode = '" + noteTypeCode + "'"
                            + " and identifier = '" + identifier + "'"
                            + " and lang = '" + idLang + "'");
                    status = true;
                }
            } catch (SQLException sqle) {
                System.out.println("ERREUR : " + sqle);
                log.error("Error while adding Note : " + identifier, sqle);
            }
        } else {
            try (Connection conn = dataSource.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("Insert into note (notetypecode, id_thesaurus, lang, lexicalvalue, id_user, notesource, identifier)"
                            + " values ('" + noteTypeCode + "','" + idThesaurus + "','" + idLang + "','"
                            + note + "'," + idUser
                            + ",'" + noteSource + "','" + identifier + "'" 
                            + ")");
                    status = true;
                }
            } catch (SQLException sqle) {
                System.out.println("ERREUR : " + sqle);
                log.error("Error while adding Note : " + identifier, sqle);
            }
        }
        if (idUser != -1) {
            addConceptNoteHistorique(identifier, idLang, idThesaurus, note, noteTypeCode, "add", idUser);
        }
        return status;
    }   
    
    /**
     * Cette fonction permet de mettre à jour une note
     */
    public boolean updateNote(int idNote, String idConcept, String idLang, String idThesaurus,
                              String note, String noteSource, String noteTypeCode, int idUser) {
        
        idLang = languageHelper.normalizeIdLang(idLang);

        boolean status = false;
        note = fr.cnrs.opentheso.utils.StringUtils.convertString(note);
        noteSource = fr.cnrs.opentheso.utils.StringUtils.convertString(noteSource);

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE note set lexicalvalue = '" + note + "',"
                        + " notesource = '" + noteSource + "',"
                        + " modified = current_date WHERE id = " + idNote + " AND id_thesaurus = '" + idThesaurus + "'");
                addConceptNoteHistorique(idConcept, idLang, idThesaurus, note, noteTypeCode, "update", idUser);
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating Note of Concept : " + idConcept, sqle);
        }
        return status;
    }     
    
    /**
     * Cette fonction permet de savoir si la Note existe ou non
     */
    public boolean isNoteExist(String identifier, String idThesaurus, String idLang, String note, String noteTypeCode) {

        idLang = languageHelper.normalizeIdLang(idLang);        
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id from note"
                            + " where identifier = '" + identifier + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and lexicalvalue = '" + fr.cnrs.opentheso.utils.StringUtils.convertString(note) + "'"
                            + " and notetypecode = '" + noteTypeCode + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if Note exist : " + identifier, sqle);
        }
        return existe;
    }    
    
    /**
     * Cette fonction permet de savoir si la Note existe ou non
     *
     * @param identifier
     * @param idThesaurus
     * @param idLang
     * @param noteTypeCode
     * @return boolean
     */
    public boolean isNoteExistInThatLang(String identifier, String idThesaurus, String idLang, String noteTypeCode) {

        idLang = languageHelper.normalizeIdLang(idLang);
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id from note"
                            + " where identifier = '" + identifier + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and notetypecode = '" + noteTypeCode + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        resultSet.next();
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if Note exist : " + identifier, sqle);
        }
        return existe;
    }       
    
    /**
     * Cette fonction permet de récupérer la source de la  Note 
     *
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param noteType
     * @return 
     * #MR
     */
    public String getSourceOfNote(String idConcept, String idThesaurus, String idLang, String noteType) {

        idLang = languageHelper.normalizeIdLang(idLang);
        
        String source = null;
        try (Connection conn = dataSource.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select notesource from note" +
                            " where " +
                            " note.notetypecode = '" + noteType + "' " +
                            " and" +
                            " note.id_thesaurus = '" + idThesaurus + "'" +
                            " and" +
                            " note.lang = '" + idLang + "'" +
                            " and" +
                            " note.identifier = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next())
                        source = resultSet.getString("notesource");
                } 
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting definition of concept : " + idConcept, sqle);
        }
        return source;
    }    
    
    /**
     * Cette fonction permet de récupérer une Note 
     * Une seule fonction pour récupérer tout type de note
     * le retour se fait sous forme de texte
     *
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param noteType
     * @return 
     * #MR
     */
    public String getLabelOfNote(
            String idConcept, String idThesaurus, String idLang, String noteType) {

        idLang = languageHelper.normalizeIdLang(idLang);
        
        String label = null;
        try (Connection conn = dataSource.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lexicalvalue from note" +
                            " where " +
                            " note.notetypecode = '" + noteType + "' " +
                            " and" +
                            " note.id_thesaurus = '" + idThesaurus + "'" +
                            " and" +
                            " note.lang = '" + idLang + "'" +
                            " and" +
                            " note.identifier = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next())
                        label = resultSet.getString("lexicalvalue");
                } 
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting definition of concept : " + idConcept, sqle);
        }
        return label;
    }
    
    /**
     * Cette fonction permet de récupérer une Note 
     * Une seule fonction pour récupérer tout type de note
     * le retour se fait sous forme de texte
     *
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param noteType
     * @return 
     * #MR
     */
    public NodeNote getNodeNote(
            String idConcept, String idThesaurus, String idLang, String noteType) {
        idLang = languageHelper.normalizeIdLang(idLang);
        NodeNote nodeNote = null;
        try (Connection conn = dataSource.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT note.id, note.notetypecode," +
                            " note.lexicalvalue, note.created," +
                            " note.modified, note.notesource FROM note" +
                            " WHERE " +
                            " note.identifier = '" + idConcept + "'" +
                            " and note.lang ='" + idLang + "'" +
                            " and note.notetypecode ='" + noteType + "'" +
                            " and note.id_thesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()) { 
                    if (resultSet.next()) {
                        nodeNote = new NodeNote();
                        nodeNote.setIdConcept(idConcept);
                        nodeNote.setIdNote(resultSet.getInt("id"));
                        nodeNote.setLang(idLang);
                        nodeNote.setLexicalValue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNoteTypeCode(resultSet.getString("notetypecode"));
                        nodeNote.setNoteSource(resultSet.getString("notesource"));
                        nodeNote.setIdentifier(idConcept);
                    }
                } 
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting definition of concept : " + idConcept, sqle);
        }
        return nodeNote;
    }      
    
    
    /**
     * Cette fonction permet de retourner la liste des notes 
     * (tout types :  CustomNote, ScopeNote, HistoryNote ....)
     *
     * @param identifier
     * @param idThesaurus
     * @param idLang
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<NodeNote> getListNotes(String identifier, String idThesaurus, String idLang) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT note.id, note.notetypecode," +
                            " note.lexicalvalue, note.created," +
                            " note.modified, note.notesource FROM note" +
                            " WHERE " +
                            " note.identifier = '" + identifier + "'" +
                            " and note.lang ='" + idLang + "'" +
                            " and note.id_thesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()) { 
                    while (resultSet.next()) {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setIdConcept(identifier);
                        nodeNote.setIdNote(resultSet.getInt("id"));
                        nodeNote.setLang(idLang);
                        nodeNote.setLexicalValue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNoteTypeCode(resultSet.getString("notetypecode"));
                        nodeNote.setNoteSource(resultSet.getString("notesource"));
                        nodeNote.setIdentifier(identifier);
                        nodeNotes.add(nodeNote);
                    }
                } 
            } 
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Notes of Concept : " + identifier, sqle);
        }
        return nodeNotes;
    }
    
    /**
     * Cette focntion permet de retourner la liste des notes (tout type
     * HistoryNote, Definition, EditotrialNote ....) dans toutes les langues
     *
     * @param idThesaurus
     * @param identifier
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<NodeNote> getListNotesAllLang(String identifier, String idThesaurus) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT note.id, note.notetypecode, note.lexicalvalue, note.created, note.modified, note.lang, note.notesource"
                        + " FROM note"
                        + " WHERE "
                        + " note.identifier = '" + identifier + "'"
                        + " AND note.id_thesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setIdTerm(identifier);
                        nodeNote.setIdNote(resultSet.getInt("id"));
                        nodeNote.setLang(resultSet.getString("lang"));
                        nodeNote.setLexicalValue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNoteTypeCode(resultSet.getString("notetypecode"));
                        nodeNote.setNoteSource(resultSet.getString("notesource"));
                        nodeNote.setIdentifier(identifier);
                        nodeNotes.add(nodeNote);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All Notes of Term : " + identifier, sqle);
        }
        return nodeNotes;
    }    
    
    /**
     * Cette fonction permet de supprimer toutes les notes par identifier
     * traductions et types
     *
     * @param identifier
     * @param idThesaurus
     * @return boolean
     */
    public boolean deleteNotes(String identifier, String idThesaurus) {
        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from note"
                    + " where identifier = '" + identifier + "'"
                    + " and id_thesaurus = '" + idThesaurus + "'");
            return true;
        } catch (SQLException sqle) {
            log.error("Error while deleting all notes : " + identifier, sqle);
        }
        return false;
    }
    
    /**
     *
     * Cette fonction permet de supprimer une note suivant l'id de la note
     * juste une ligne
     *
     * @param idNote
     * @param identifier
     * @param idThesaurus
     * @param idLang
     * @param noteTypeCode
     * @param oldNote
     * @param idUser
     * @return boolean
     */
    public boolean deleteThisNote(int idNote, String identifier, String idLang, String idThesaurus, String noteTypeCode,
            String oldNote, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from note"
                            + " where id = '" + idNote + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeUpdate(query);
                    addConceptNoteHistorique(identifier, idLang, idThesaurus, oldNote, noteTypeCode, "delete", idUser);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting note of term : " + identifier, sqle);
        }
        return status;
    }    
    
    /**
     * Cette fonction permet de supprimer toutes les notes par langue
     *
     * @param identifier
     * @param idThesaurus
     * @param idLang
     * @param notetypecode
     * @return boolean
     */
    public boolean deleteNoteByLang(
            String identifier, String idThesaurus, String idLang, String notetypecode) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from note"
                        + " where identifier = '" + identifier + "'"
                        + " and id_thesaurus = '" + idThesaurus + "'"
                        + " and lang = '" + idLang + "'"
                        + " and notetypecode = '" + notetypecode + "'"
                );
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting all notes of Concept : " + identifier, sqle);
        }
        return false;
    }


    /**
     * Cette fonction permet d'ajouter l'historique de l'ajout d'une Note Ã  un
     * concept insert dans la table Note_hisorique
     *
     * @param idConcept
     * @param idLang
     * @param idThesausus
     * @param note
     * @param noteTypeCode
     * @param actionPerformed
     * @param idUser
     * @return
     */
    public boolean addConceptNoteHistorique(String idConcept, String idLang, String idThesausus,
            String note, String noteTypeCode, String actionPerformed, int idUser) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into note_historique "
                        + "(notetypecode, id_thesaurus, id_concept, lang, lexicalvalue, "
                        + "action_performed, id_user)"
                        + " values ("
                        + "'" + noteTypeCode + "'"
                        + ",'" + idThesausus + "'"
                        + ",'" + idConcept + "'"
                        + ",'" + idLang + "'"
                        + ",'" + fr.cnrs.opentheso.utils.StringUtils.convertString(note) + "'"
                        + ",'" + actionPerformed + "'"
                        + ",'" + idUser + "')");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Note historique of concept : " + idConcept, sqle);
            return false;
        }
    }

    public void deleteVoteByNoteId(int idNote, String idThesaurus, String idConcept) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM candidat_vote WHERE id_concept = '" + idConcept
                        + "' AND id_thesaurus = '" + idThesaurus + "' AND id_note = '" + idNote + "'");
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting candidat_vote of id_note : " + idNote, sqle);
        }
    }

    public ArrayList<NoteType> getNotesType() {
        ArrayList<NoteType> noteTypes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT code, isterm, isconcept,"
                            + " label_fr, label_en"
                            + " FROM note_type";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NoteType noteType = new NoteType();
                        noteType.setCodeNote(resultSet.getString("code"));
                        noteType.setTerm(resultSet.getBoolean("isterm"));
                        noteType.setConcept(resultSet.getBoolean("isconcept"));
                        noteType.setLabelFr(resultSet.getString("label_fr"));
                        noteType.setLabelEn(resultSet.getString("label_en"));
                        noteTypes.add(noteType);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Notes types ", sqle);
        }
        return noteTypes;
    }


    @Data
    public class NoteType {

        private String codeNote;
        private boolean term;
        private boolean concept;
        private String labelFr;
        private String labelEn;

    }

    /**
     * Cette fonction permet de retourner la définition d'un terme
     *
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<String> getDefinition(String idConcept, String idThesaurus, String idLang) {

        if("en-GB".equalsIgnoreCase(idLang))
            idLang = "en";
        
        ArrayList<String> listDefinitions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lexicalvalue from note" +
                            " where note.notetypecode = 'definition' " +
                            " and note.id_thesaurus = '" + idThesaurus + "'" +
                            " and note.lang = '" + idLang + "'" +
                            " and note.identifier = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listDefinitions.add(resultSet.getString("lexicalvalue"));
                    }
                } 
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting definition of concept : " + idConcept, sqle);
        }
        return listDefinitions;
    }

    /**
     * Cette fonction permet de retourner la note d'application d'un concept
     *
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<String> getScopeNote(String idConcept, String idThesaurus, String idLang) {

        ArrayList<String> listDefinitions = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lexicalvalue from note"
                            + " where"
                            + " note.notetypecode = 'scopeNote'"
                            + " and"
                            + " note.id_thesaurus = '" + idThesaurus + "'"
                            + " and "
                            + " note.lang = '" + idLang + "'"
                            + " and"
                            + " note.identifier = '" + idConcept + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        listDefinitions.add(resultSet.getString("lexicalvalue"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting definition of concept : " + idConcept, sqle);
        }
        return listDefinitions;
    }

    public NodeNote getNoteByIdNote(int idNote) {

        NodeNote nodeNote = null;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT note.id, note.lang, note.notetypecode, note.lexicalvalue, note.created, "
                            + "note.modified FROM note WHERE  note.id = " + idNote;

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        nodeNote = new NodeNote();
                        nodeNote.setIdNote(resultSet.getInt("id"));
                        nodeNote.setLexicalValue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNoteTypeCode(resultSet.getString("notetypecode"));
                        nodeNote.setLang(resultSet.getString("lang"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
        }
        return nodeNote;
    }

    public NodeNote getNoteByValue(String noteValue) {

        NodeNote nodeNote = null;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id, lang, notetypecode, lexicalvalue, created, modified "
                            + "FROM note WHERE lexicalvalue = '" + noteValue + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        nodeNote = new NodeNote();
                        nodeNote.setIdNote(resultSet.getInt("id"));
                        nodeNote.setLexicalValue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNoteTypeCode(resultSet.getString("notetypecode"));
                        nodeNote.setLang(resultSet.getString("lang"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
        }
        return nodeNote;
    }

    /**
     * permet de retourner l'Id de la note par value et lang et idTheso et NoteType
     * @param value
     * @param noteTypeCode
     * @param idLang
     * @param idTheso
     * @return 
     */
    public int getNoteByValueAndThesaurus(String value, String noteTypeCode, String idLang, String idTheso) {
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        int idNote = -1;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id FROM note "
                        + " WHERE lexicalvalue = '" + value + "'"
                        + " AND id_thesaurus = '" + idTheso + "'"
                        + " and notetypecode = '" + noteTypeCode + "'"
                        + " and lang = '" + idLang + "'"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idNote = resultSet.getInt("id");
                    }
                }
            }
        } catch (SQLException sqle) {
        }
        return idNote;
    }



    private int getNbrNoteByGroupTypeConcept(String idGroup, String idThesaurus, String idLang) {

        int count = 0;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {

                // ajouté par Miled pour test et optimisation 
                stmt.executeQuery("SELECT count (note.id) " +
                        " FROM note, concept_group_concept " +
                        " WHERE " +
                        " concept_group_concept.idthesaurus = note.id_thesaurus " +
                        " AND" +
                        " concept_group_concept.idconcept = note.identifier " +
                        " AND " +
                        " note.id_thesaurus = '" + idThesaurus + "' AND" +
                        " note.lang = '" + idLang + "' AND" +
                        " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Count of Note in Group : " + idGroup, sqle);
        }
        return count;
    }

    private int getNbrNoteDesConceptsSansGroup(String idThesaurus, String idLang) {

        int count = 0;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT count(note.id) FROM concept, note WHERE concept.id_concept = note.id_concept "
                        + "AND concept.id_thesaurus = note.id_thesaurus AND concept.id_thesaurus = '" + idThesaurus + "' "
                        + "AND note.lang = '" + idLang + "' AND concept.id_concept NOT IN (SELECT idconcept FROM concept_group_concept "
                        + "WHERE idthesaurus = '" + idThesaurus + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Count of Note in without Group", sqle);
        }
        return count;
    }

    private int getNbrNoteDesTermsSansGroup(String idThesaurus, String idLang) {

        int count = 0;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT count(note.id) FROM preferred_term, note WHERE preferred_term.id_term = note.id_term "
                        + "AND preferred_term.id_thesaurus = note.id_thesaurus AND preferred_term.id_thesaurus = '" + idThesaurus + "' "
                        + "AND note.lang = '" + idLang + "' AND preferred_term.id_concept NOT IN "
                        + "(SELECT idconcept FROM concept_group_concept WHERE idthesaurus = '" + idThesaurus + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Count of Note in without Group", sqle);
        }
        return count;
    }

    /**
     * permet de retourner le nombre de notes (type concept et terme) pour les
     * concepts qui n'ont pas de collection
     *
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public int getNbrNoteSansGroup(String idThesaurus, String idLang) {
        int nbrNoteConcepts = getNbrNoteDesConceptsSansGroup(idThesaurus, idLang);
        int nbrNoteTerms = getNbrNoteDesTermsSansGroup(idThesaurus, idLang);
        return nbrNoteConcepts + nbrNoteTerms;
    }

    /**
     * permet de retourner le nombre de notes (type concept et terme) pour les
     * concepts qui appartiennent à ce groupe
     */
    public int getNbrNoteByGroup(String idGroup, String idThesaurus, String idLang) {
        int nbrNoteConcepts = getNbrNoteByGroupTypeConcept(idGroup, idThesaurus, idLang);
        return nbrNoteConcepts; //+ nbrNoteTerms;
    }

}
