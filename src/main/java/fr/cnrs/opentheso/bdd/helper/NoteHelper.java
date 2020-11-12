/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class NoteHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    public NoteHelper() {
    }

    //// restructuration de la classe NoteHelper le 29/10/2018 //////    
    
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////// Nouvelles fontions #MR//////////////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////  
    
    
    /**
     * Cette fonction permet d'ajouter l'historique de l'ajout d'une Note Ã  un
     * term insert dans la table Note_hisorique
     *
     * @param ds
     * @param idTerme
     * @param idLang
     * @param idThesausus
     * @param note
     * @param noteTypeCode
     * @param actionPerformed
     * @param idUser
     * @return
     */
    public boolean addTermNoteHistorique(HikariDataSource ds,
            String idTerme, String idLang, String idThesausus,
            String note, String noteTypeCode,
            String actionPerformed,
            int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        note = new StringPlus().convertString(note);

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into note_historique "
                            + "(notetypecode, id_thesaurus, id_term, lang, lexicalvalue, "
                            + "action_performed, id_user)"
                            + " values ("
                            + "'" + noteTypeCode + "'"
                            + ",'" + idThesausus + "'"
                            + ",'" + idTerme + "'"
                            + ",'" + idLang + "'"
                            + ",'" + note + "'"
                            + ",'" + actionPerformed + "'"
                            + ",'" + idUser + "')";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Note historique of term : " + idTerme, sqle);
        }
        return status;
    }    
    
    /**
     * Cette fonction permet d'ajouter l'historique de l'ajout d'une Note Ã  un
     * concept insert dans la table Note_hisorique
     *
     * @param ds
     * @param idConcept
     * @param idLang
     * @param idThesausus
     * @param note
     * @param noteTypeCode
     * @param actionPerformed
     * @param idUser
     * @return
     */
    public boolean addConceptNoteHistorique(HikariDataSource ds,
            String idConcept, String idLang, String idThesausus,
            String note, String noteTypeCode, String actionPerformed,
            int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        note = new StringPlus().convertString(note);

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into note_historique "
                            + "(notetypecode, id_thesaurus, id_concept, lang, lexicalvalue, "
                            + "action_performed, id_user)"
                            + " values ("
                            + "'" + noteTypeCode + "'"
                            + ",'" + idThesausus + "'"
                            + ",'" + idConcept + "'"
                            + ",'" + idLang + "'"
                            + ",'" + note + "'"
                            + ",'" + actionPerformed + "'"
                            + ",'" + idUser + "')";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Note historique of concept : " + idConcept, sqle);
        }
        return status;
    } 
    
    /**
     * Cette fonction permet de mettre à  jour une note de Concept
     *
     * @param ds
     * @param idNote
     * @param idConcept
     * @param idLang
     * @param idThesaurus
     * @param note
     * @param noteTypeCode
     * @param idUser
     * @return
     */
    public boolean updateConceptNote(HikariDataSource ds,
            int idNote,
            String idConcept, String idLang,
            String idThesaurus, String note, String noteTypeCode,
            int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        note = new StringPlus().convertString(note);
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE note set"
                            + " lexicalvalue = '" + note + "',"
                            + " modified = current_date"
                            + " WHERE id = " + idNote
                            + " AND id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeUpdate(query);
                    addConceptNoteHistorique(ds, idConcept, idLang, idThesaurus, note, noteTypeCode, "update", idUser);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating Note of Concept : " + idConcept, sqle);
        }

        return status;
    }

    /**
     * Cette fonction permet de mettre Ã  jour une note de Concept
     *
     * @param ds
     * @param idNote
     * @param idTerm
     * @param idLang
     * @param idThesausus
     * @param note
     * @param noteTypeCode
     * @param idUser
     * @return
     */
    public boolean updateTermNote(HikariDataSource ds,
            int idNote, 
            String idTerm, String idLang,
            String idThesausus, String note, String noteTypeCode,
            int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        note = new StringPlus().convertString(note);
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE note set"
                            + " lexicalvalue = '" + note + "',"
                            + " modified = current_date"
                            + " WHERE id =" + idNote 
                            + " AND id_thesaurus = '" + idThesausus + "'";
                    stmt.executeUpdate(query);
                    addTermNoteHistorique(ds, idTerm, idLang, idThesausus, note, noteTypeCode, "update", idUser);                    
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating Note of Term : " + idTerm, sqle);
        }

        return status;
    }    
    
   /**
    *   
     * Cette fonction permet de supprimer une note suivant l'id de la note
     * 
     * @param ds
     * @param idNote
     * @param idTerm
     * @param idThesaurus
     * @param idLang
     * @param noteTypeCode
     * @param oldNote
     * @param idUser
     * @return boolean
     */
    public boolean deleteThisNoteOfTerm(HikariDataSource ds,
            int idNote,
            String idTerm,
            String idLang,
            String idThesaurus,
            String noteTypeCode,
            String oldNote, int idUser) {
        Connection conn;
        Statement stmt;
        boolean status = false;
        
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {   
                    String query = "delete from note"
                            + " where id = '" + idNote + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeUpdate(query);
                    addTermNoteHistorique(ds, idTerm, idLang, idThesaurus, oldNote,
                            noteTypeCode, "delete", idUser);
                    status = true;
                    
                    } finally {
                        stmt.close();
                    }
                } finally {
                    conn.close();
                }
            } catch (SQLException sqle) {
                // Log exception
                log.error("Error while deleting note of term : " + idTerm, sqle);
            }
        return status;
    }


    public void deleteVoteByNoteId(HikariDataSource ds, int idNote, String idThesaurus, String idConcept) {
        Connection conn;
        Statement stmt;
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM candidat_vote WHERE id_concept = '"+idConcept
                    +"' AND id_thesaurus = '"+idThesaurus+"' AND id_note = '"+idNote+"'");
            stmt.close();
            conn.close();
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting candidat_vote of id_note : " + idNote, sqle);
        }
    }
    
   /**
    *  déprécié
     * Cette fonction permet de supprimer une note suivant l'id de la note
     * 
     * @param ds
     * @param idNote
     * @param idConcept
     * @param idLang
     * @param noteTypeCode
     * @param idThesaurus
     * @param oldNote
     * @param idUser
     * @return boolean
     */
    public boolean deletethisNoteOfConcept(HikariDataSource ds,
            int idNote,
            String idConcept,
            String idLang,
            String idThesaurus,
            String noteTypeCode,
            String oldNote, int idUser) {
        
        Connection conn;
        Statement stmt;
        boolean status = false;
        
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {   
                    String query = "delete from note"
                            + " where id = '" + idNote + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeUpdate(query);
                    addConceptNoteHistorique(ds, idConcept, idLang, idThesaurus, oldNote, noteTypeCode, "delete", idUser);
                    status = true;
                    } finally {
                        stmt.close();
                    }
                } finally {
                    conn.close();
                }
            } catch (SQLException sqle) {
                // Log exception
                log.error("Error while deleting note of concept : " + idConcept, sqle);
            }
        return status;
    }
    
    
    public ArrayList<NoteType> getNotesType(HikariDataSource ds){
        ArrayList<NoteType> noteTypes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
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
                        noteType.setIsterm(resultSet.getBoolean("isterm"));
                        noteType.setIsconcept(resultSet.getBoolean("isconcept"));
                        noteType.setLabel_fr(resultSet.getString("label_fr"));
                        noteType.setLabel_en(resultSet.getString("label_en"));
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
    
    
    public class NoteType {
        private String CodeNote;
        private boolean isterm;
        private boolean isconcept;
        private String label_fr;
        private String label_en;
        
        public NoteType() {
        }

        public String getCodeNote() {
            return CodeNote;
        }

        public void setCodeNote(String CodeNote) {
            this.CodeNote = CodeNote;
        }

        public boolean isIsterm() {
            return isterm;
        }

        public void setIsterm(boolean isterm) {
            this.isterm = isterm;
        }

        public boolean isIsconcept() {
            return isconcept;
        }

        public void setIsconcept(boolean isconcept) {
            this.isconcept = isconcept;
        }

        public String getLabel_fr() {
            return label_fr;
        }

        public void setLabel_fr(String label_fr) {
            this.label_fr = label_fr;
        }

        public String getLabel_en() {
            return label_en;
        }

        public void setLabel_en(String label_en) {
            this.label_en = label_en;
        }
        
        
    }    
    
    
    
    
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////// Fin nouvelles fontions #MR///////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////       
    
    
    

    
    /**
     * Cette focntion permet de retourner la liste des notes pour un concept
     * (type CustomNote, ScopeNote, HistoryNote)
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<NodeNote> getListNotesConcept(HikariDataSource ds, String idConcept,
            String idThesaurus, String idLang) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT note.id, note.notetypecode,"
                            + " note.lexicalvalue, note.created,"
                            + " note.modified FROM note, note_type"
                            + " WHERE note.notetypecode = note_type.code"
                            + " and note_type.isconcept = true"
                            + " and note.id_concept = '" + idConcept + "'"
                            + " and note.lang ='" + idLang + "'"
                            + " and note.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setId_concept(idConcept);
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLang(idLang);
                        nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                        nodeNotes.add(nodeNote);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Notes of Concept : " + idConcept, sqle);
        }
        return nodeNotes;
    }

    /**
     * Cette focntion permet de retourner la liste des notes pour un concept
     * (type CustomNote, ScopeNote, HistoryNote) avec toutes les langues
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<NodeNote> getListNotesConceptAllLang(HikariDataSource ds, String idConcept,
            String idThesaurus) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT note.id, note.notetypecode,"
                            + " note.lexicalvalue, note.created,"
                            + " note.modified, note.lang FROM note, note_type"
                            + " WHERE note.notetypecode = note_type.code"
                            + " and note_type.isconcept = true"
                            + " and note.id_concept = '" + idConcept + "'"
                            + " and note.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setId_concept(idConcept);
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLang(resultSet.getString("lang"));
                        nodeNote.setLexicalvalue(
                            stringPlus.normalizeStringForXml(
                                resultSet.getString("lexicalvalue"))
                            );
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                        nodeNotes.add(nodeNote);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Notes of Concept : " + idConcept, sqle);
        }
        return nodeNotes;
    }

    /**
     * Cette focntion permet de retourner la liste des notes pour un Term(type
     * HistoryNote, Definition, EditotrialNote)
     *
     * @param ds
     * @param idThesaurus
     * @param idTerm
     * @param idLang
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<NodeNote> getListNotesTerm(HikariDataSource ds,
            String idTerm, String idThesaurus, String idLang) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT note.id, note.notetypecode,"
                            + " note.lexicalvalue, note.created,"
                            + " note.modified FROM note, note_type"
                            + " WHERE note.notetypecode = note_type.code"
                            + " and note_type.isterm = true"
                            + " and note.id_term = '" + idTerm + "'"
                            + " and note.lang ='" + idLang + "'"
                            + " and note.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setId_term(idTerm);
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLang(idLang);
                        nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                        nodeNotes.add(nodeNote);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Notes of Term : " + idTerm, sqle);
        }
        return nodeNotes;
    }

    /**
     * Cette focntion permet de retourner la liste des notes pour un Term(type
     * HistoryNote, Definition, EditotrialNote) dans toutes les langues
     *
     * @param ds
     * @param idThesaurus
     * @param idTerm
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<NodeNote> getListNotesTermAllLang(HikariDataSource ds,
            String idTerm, String idThesaurus) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT note.id, note.notetypecode,"
                            + " note.lexicalvalue, note.created,"
                            + " note.modified, note.lang FROM note, note_type"
                            + " WHERE note.notetypecode = note_type.code"
                            + " and note_type.isterm = true"
                            + " and note.id_term = '" + idTerm + "'"
                            + " and note.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setId_term(idTerm);
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLang(resultSet.getString("lang"));
                        nodeNote.setLexicalvalue(
                            stringPlus.normalizeStringForXml(
                                resultSet.getString("lexicalvalue"))
                            );
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                        nodeNotes.add(nodeNote);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Notes of Term : " + idTerm, sqle);
        }
        return nodeNotes;
    }

    /**
     * Cette fonction permet d'ajouter une Note Ã  un concept instert dans la
     * table Note
     *
     * @param ds
     * @param idConcept
     * @param idLang
     * @param idThesaurus
     * @param note
     * @param noteTypeCode
     * @param idUser
     * @return
     */
    public boolean addConceptNote(HikariDataSource ds,
            String idConcept, String idLang, String idThesaurus,
            String note, String noteTypeCode, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        note = new StringPlus().convertString(note);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into note "
                            + "(notetypecode, id_thesaurus, id_concept, lang, lexicalvalue, id_user)"
                            + " values ("
                            + "'" + noteTypeCode + "'"
                            + ",'" + idThesaurus + "'"
                            + ",'" + idConcept + "'"
                            + ",'" + idLang + "'"
                            + ",'" + note + "'," + idUser + ")";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding Note of Concept : " + idConcept, sqle);
        }
        addConceptNoteHistorique(ds, idConcept, idLang, idThesaurus, note, noteTypeCode, "add", idUser);
        return status;
    }



    /**
     * Cette fonction permet d'ajouter une Note Ã  un concept instert dans la
     * table Note
     *
     * @param ds
     * @param idConcept
     * @param idLang
     * @param idThesausus
     * @param note
     * @param noteTypeCode
     * @param idUser
     * @return
     */
    private boolean addConceptNoteRollback(HikariDataSource ds,
            String idConcept, String idLang, String idThesausus,
            String note, String noteTypeCode, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        note = new StringPlus().convertString(note);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into note "
                            + "(notetypecode, id_thesaurus, id_concept, lang, lexicalvalue)"
                            + " values ("
                            + "'" + noteTypeCode + "'"
                            + ",'" + idThesausus + "'"
                            + ",'" + idConcept + "'"
                            + ",'" + idLang + "'"
                            + ",'" + note + "')";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding Note of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette focntion permet de retourner la liste de l'historique des notes
     * pour un concept (type CustomNote, ScopeNote, HistoryNote)
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idTerm
     * @param idLang
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<NodeNote> getNoteHistoriqueAll(HikariDataSource ds, String idConcept,
            String idThesaurus, String idTerm, String idLang) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id, notetypecode, lexicalvalue, modified, id_user FROM note_historique, users"
                            + " WHERE id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and (id_concept = '" + idConcept + "' OR id_term = '" + idTerm + "' )"
                            + " and note_historique.id_user=users.id_user"
                            + " order by modified DESC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setId_concept(idConcept);
                        nodeNote.setId_term(idTerm);
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLang(idLang);
                        nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                        nodeNote.setIdUser(resultSet.getInt("id_user"));
                        nodeNotes.add(nodeNote);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting all historique Notes of Concept : " + idConcept, sqle);
        }
        return nodeNotes;
    }

    /**
     * Cette focntion permet de retourner la liste de l'historique des notes
     * pour un concept (type CustomNote, ScopeNote, HistoryNote) à une date
     * précise
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idTerm
     * @param idLang
     * @param date
     * @return ArrayList des notes sous forme de Class NodeNote
     */
    public ArrayList<NodeNote> getNoteHistoriqueFromDate(HikariDataSource ds, String idConcept,
            String idThesaurus, String idTerm, String idLang, Date date) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id, notetypecode, lexicalvalue, modified, id_user FROM note_historique, users"
                            + " WHERE id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and (id_concept = '" + idConcept + "' OR id_term = '" + idTerm + "' )"
                            + " and note_historique.id_user=users.id_user"
                            + " and modified <= '" + date.toString()
                            + "' order by modified DESC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        boolean exist = false;
                        for (NodeNote nn : nodeNotes) {
                            if (nn.getNotetypecode().equals(resultSet.getString("notetypecode"))) {
                                if (nn.getModified().before(resultSet.getDate("modified"))) {
                                    NodeNote nodeNote = new NodeNote();
                                    nodeNote.setId_concept(idConcept);
                                    nodeNote.setId_term(idTerm);
                                    nodeNote.setId_note(resultSet.getInt("id"));
                                    nodeNote.setLang(idLang);
                                    nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                                    nodeNote.setModified(resultSet.getDate("modified"));
                                    nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                                    nodeNote.setIdUser(resultSet.getInt("id_user"));
                                    nodeNotes.add(nodeNote);
                                }
                                exist = true;
                            }
                        }
                        if (!exist) {
                            NodeNote nodeNote = new NodeNote();
                            nodeNote.setId_concept(idConcept);
                            nodeNote.setId_term(idTerm);
                            nodeNote.setId_note(resultSet.getInt("id"));
                            nodeNote.setLang(idLang);
                            nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                            nodeNote.setModified(resultSet.getDate("modified"));
                            nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                            nodeNote.setIdUser(resultSet.getInt("id_user"));
                            nodeNotes.add(nodeNote);
                        }

                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting date historique Notes of Concept : " + idConcept, sqle);
        }
        return nodeNotes;
    }

    /**
     * Cette fonction permet d'ajouter une Note Ã  un Term instert dans la table
     * Note
     *
     * @param ds
     * @param idTerm
     * @param idLang
     * @param idThesaurus
     * @param note
     * @param noteTypeCode
     * @param idUser
     * @return
     */
    public boolean addTermNote(HikariDataSource ds,
            String idTerm, String idLang, String idThesaurus,
            String note, String noteTypeCode, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        note = new StringPlus().convertString(note);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into note "
                            + "(notetypecode, id_thesaurus, id_term, lang, lexicalvalue, id_user)"
                            + " values ("
                            + "'" + noteTypeCode + "'"
                            + ",'" + idThesaurus + "'"
                            + ",'" + idTerm + "'"
                            + ",'" + idLang + "'"
                            + ",'" + note + "'," + idUser +")";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Note of Term : " + idTerm, sqle);
                //System.out.println(sqle.toString());
                return false;
            } else
                return true;
            // Log exception
            
        }
        addTermNoteHistorique(ds, idTerm, idLang, idThesaurus, note, noteTypeCode, "add", idUser);
        return status;
    }



    /**
     * Cette fonction permet de savoir si la Note d'un Concept existe ou non
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param noteTypeCode
     * @return boolean
     */
    public boolean isNoteExistOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang,
            String noteTypeCode) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id from note"
                            + " where id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and noteTypeCode = '" + noteTypeCode + "'";
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
            log.error("Error while asking if Note of Concept exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si la Note d'un Concept existe ou non
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @param idLang
     * @param noteTypeCode
     * @return boolean
     */
    public boolean isNoteExistOfTerm(HikariDataSource ds,
            String idTerm, String idThesaurus, String idLang,
            String noteTypeCode) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id from note"
                            + " where id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and lang ='" + idLang + "'"
                            + " and noteTypeCode = '" + noteTypeCode + "'";
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
            log.error("Error while asking if Note of Term exist : " + idTerm, sqle);
        }
        return existe;
    }

    
    /**
     * Cette fonction permet de supprimer toutes les notes d'un Concept
     * 
     * @param conn
     * @param idConcept
     * @param idThesaurus
     * @return boolean
     */
    public boolean deleteNotesOfConcept(Connection conn,
            String idConcept, String idThesaurus) {

        Statement stmt;
        try {
            // Get connection from pool
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from note"
                            + " where id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeUpdate(query);
                    return true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting all notes of Concept : " + idConcept, sqle);
        }
        return false;
    }
    
    /**
     * Cette fonction permet de supprimer toutes les notes d'un terme
     * 
     * @param conn
     * @param idTerm
     * @param idThesaurus
     * @return boolean
     */
    public boolean deleteNotesOfTerm(Connection conn,
            String idTerm, String idThesaurus) {

        Statement stmt;
        try {
            // Get connection from pool
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from note"
                            + " where id_term = '" + idTerm + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";
                    stmt.executeUpdate(query);
                    return true;
                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting all notes of Term : " + idTerm, sqle);
        }
        return false;
    }  
    

    /**
     * pour pouvoir obtener une list des Notes a partir du idTerm
     * sans conter avec le language
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @return 
     */
    public ArrayList<NodeNote> getListNotesTerm2(HikariDataSource ds,
            String idTerm, String idThesaurus) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT note.id, note.lang, note.notetypecode,"
                            + " note.lexicalvalue, note.created,"
                            + " note.modified FROM note, note_type"
                            + " WHERE note.notetypecode = note_type.code"
                            + " and note_type.isterm = true"
                            + " and note.id_term = '" + idTerm + "'"
                            + " and note.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setId_term(idTerm);
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                        nodeNote.setLang(resultSet.getString("lang"));
                        nodeNotes.add(nodeNote);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Notes of Term : " + idTerm, sqle);
        }
        return nodeNotes;
    }
      /**
     * pour pouvoir obtener une list des Notes a partir du idConcept
     * sans conter avec le language
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return 
     */
    public ArrayList<NodeNote> getListNotesConcept2(HikariDataSource ds, String idConcept,
            String idThesaurus) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT note.id, note.lang, note.notetypecode,"
                            + " note.lexicalvalue, note.created,"
                            + " note.modified FROM note, note_type"
                            + " WHERE note.notetypecode = note_type.code"
                            + " and note_type.isconcept = true"
                            + " and note.id_concept = '" + idConcept + "'"
                            + " and note.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeNote nodeNote = new NodeNote();
                        nodeNote.setId_concept(idConcept);
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
                        nodeNote.setLang(resultSet.getString("lang"));
                        nodeNotes.add(nodeNote);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Notes of Concept : " + idConcept, sqle);
        }
        return nodeNotes;
    }

    public NodeNote getNoteByIdNote(HikariDataSource ds, int idNote) {

        NodeNote nodeNote = null;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT note.id, note.lang, note.notetypecode, note.lexicalvalue, note.created, " +
                            "note.modified FROM note WHERE  note.id = " + idNote;

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        nodeNote = new NodeNote();
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
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

    public NodeNote getNoteByValue(HikariDataSource ds, String noteValue) {

        NodeNote nodeNote = null;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT id, lang, notetypecode, lexicalvalue, created, modified " +
                            "FROM note WHERE lexicalvalue = '"+noteValue+"'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        nodeNote = new NodeNote();
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
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

    public NodeNote getNoteByValueAndThesaurus(HikariDataSource ds, String value, String idThes) {

        NodeNote nodeNote = null;
        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    stmt.executeQuery("SELECT id, lang, notetypecode, lexicalvalue, created, modified FROM note " +
                            "WHERE lexicalvalue = '"+value+"' AND id_thesaurus = '"+idThes+"'");
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        nodeNote = new NodeNote();
                        nodeNote.setId_note(resultSet.getInt("id"));
                        nodeNote.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        nodeNote.setModified(resultSet.getDate("modified"));
                        nodeNote.setCreated(resultSet.getDate("created"));
                        nodeNote.setNotetypecode(resultSet.getString("notetypecode"));
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
     * Cette fonction permet de retourner le nombre de notes pour un concept
     * (toutes les notes (type terme et type concept)
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public int getCountOfNotes(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        int count = 0;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT count(preferred_term.id_concept) FROM " +
                        "  preferred_term, note" +
                        " WHERE " +
                        "  preferred_term.id_thesaurus = note.id_thesaurus AND" +
                        "  (preferred_term.id_term = note.id_term or" +
                        "  preferred_term.id_concept = note.id_concept)" +
                        "  AND" +
                        "  preferred_term.id_thesaurus = '" + idThesaurus + "'" +
                        " AND preferred_term.id_concept='" + idConcept + "'" +
                        " AND note.lang = '" + idLang + "'";    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                   if(resultSet.next()) {
                       count = resultSet.getInt(1);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Count of Traductions of Concept  : " + idConcept, sqle);
        }
        return count;
    }    
    
     /**
     * Change l'id d'un concept dans la table note
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws SQLException
     */
    public void setIdConceptNote(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE note"
                    + " SET id_concept = '" + newIdConcept + "'"
                    + " WHERE id_concept = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
        } finally {
            stmt.close();
        }
    }
    
    /**
     * Change l'id d'un concept dans la table note_historique
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws SQLException
     */
    public void setIdConceptNoteHisto(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE note_historique"
                    + " SET id_concept = '" + newIdConcept + "'"
                    + " WHERE id_concept = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
        } finally {
            stmt.close();
        }
    }
    
    
    
    private int getNbrNoteByGroupTypeConcept(HikariDataSource ds, String idGroup, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        int count = 0;
 
        try {
            // Get connection from pool
            conn = ds.getConnection();
            stmt = conn.createStatement();
            
            // ajouté par Miled pour test et optimisation 
            stmt.executeQuery("SELECT count (note.id)" 
                + " FROM preferred_term, note, concept"
                + " WHERE preferred_term.id_concept = note.id_concept "
                + " AND preferred_term.id_thesaurus = note.id_thesaurus" 
                + " AND concept.id_thesaurus = preferred_term.id_thesaurus" 
                + " AND concept.id_concept = preferred_term.id_concept"
                + " AND preferred_term.id_thesaurus = '"+ idThesaurus+ "'"
                + " AND note.lang = '" +idLang + "' "
                + " AND preferred_term.id_concept "
                + " IN (SELECT idconcept FROM concept_group_concept WHERE idthesaurus = '" + idThesaurus +"'"
                + " and concept_group_concept.idgroup='" + idGroup +"')");             
            /*stmt.executeQuery("SELECT count(preferred_term.id_concept) " +
                              "FROM preferred_term, note " +
                              "WHERE preferred_term.id_thesaurus = note.id_thesaurus " +
                              "AND (preferred_term.id_term = note.id_term or preferred_term.id_concept = note.id_concept) " +
                              "AND preferred_term.id_thesaurus = '"+idThesaurus+"' " +
                              "AND preferred_term.id_concept IN (SELECT concept.id_concept " +
                                             "FROM concept, concept_group_concept " +
                                             "WHERE concept.id_concept = concept_group_concept.idconcept " +
                                             "AND concept.id_thesaurus = concept_group_concept.idthesaurus " +
                                             "AND concept.id_thesaurus = '"+idThesaurus+"' " +
                                             "AND concept_group_concept.idgroup = '"+idGroup+"') " +
                              "AND note.lang = '"+idLang+"'");*/
            ResultSet resultSet = stmt.getResultSet();
            if(resultSet.next()) {
                count = resultSet.getInt(1);
            }
            stmt.close();
            conn.close();
        } catch (SQLException sqle) {
            log.error("Error while getting Count of Note in Group : " + idGroup, sqle);
        }
        return count;
    }
    
    private int getNbrNoteByGroupTypeTerm(HikariDataSource ds, String idGroup, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        int count = 0;
 
        try {
            // Get connection from pool
            conn = ds.getConnection();
            stmt = conn.createStatement();
            
            // ajouté par Miled pour test et optimisation 
            stmt.executeQuery("SELECT count (note.id)" 
                + " FROM preferred_term, note, concept"
                + " WHERE preferred_term.id_term = note.id_term "
                + " AND preferred_term.id_thesaurus = note.id_thesaurus" 
                + " AND concept.id_thesaurus = preferred_term.id_thesaurus" 
                + " AND concept.id_concept = preferred_term.id_concept"
                + " AND preferred_term.id_thesaurus = '"+ idThesaurus+ "'"
                + " AND note.lang = '" +idLang + "' "
                + " AND preferred_term.id_concept "
                + " IN (SELECT idconcept FROM concept_group_concept WHERE idthesaurus = '" + idThesaurus +"'"
                + " and concept_group_concept.idgroup='" + idGroup +"')");             
            /*stmt.executeQuery("SELECT count(preferred_term.id_concept) " +
                              "FROM preferred_term, note " +
                              "WHERE preferred_term.id_thesaurus = note.id_thesaurus " +
                              "AND (preferred_term.id_term = note.id_term or preferred_term.id_concept = note.id_concept) " +
                              "AND preferred_term.id_thesaurus = '"+idThesaurus+"' " +
                              "AND preferred_term.id_concept IN (SELECT concept.id_concept " +
                                             "FROM concept, concept_group_concept " +
                                             "WHERE concept.id_concept = concept_group_concept.idconcept " +
                                             "AND concept.id_thesaurus = concept_group_concept.idthesaurus " +
                                             "AND concept.id_thesaurus = '"+idThesaurus+"' " +
                                             "AND concept_group_concept.idgroup = '"+idGroup+"') " +
                              "AND note.lang = '"+idLang+"'");*/
            ResultSet resultSet = stmt.getResultSet();
            if(resultSet.next()) {
                count = resultSet.getInt(1);
            }
            stmt.close();
            conn.close();
        } catch (SQLException sqle) {
            log.error("Error while getting Count of Note in Group : " + idGroup, sqle);
        }
        return count;
    }    

    private int getNbrNoteDesConceptsSansGroup(HikariDataSource ds, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        int count = 0;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT count(note.id) FROM concept, note WHERE concept.id_concept = note.id_concept " +
                    "AND concept.id_thesaurus = note.id_thesaurus AND concept.id_thesaurus = '"+idThesaurus+"' " +
                    "AND note.lang = '"+idLang+"' AND concept.id_concept NOT IN (SELECT idconcept FROM concept_group_concept " +
                    "WHERE idthesaurus = '"+idThesaurus+"')");
            ResultSet resultSet = stmt.getResultSet();
            if(resultSet.next()) {
                count = resultSet.getInt(1);
            }
            stmt.close();
            conn.close();
        } catch (SQLException sqle) {
            log.error("Error while getting Count of Note in without Group", sqle);
        }
        return count;
    }

    private int getNbrNoteDesTermsSansGroup(HikariDataSource ds, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        int count = 0;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT count(note.id) FROM preferred_term, note WHERE preferred_term.id_term = note.id_term " +
                    "AND preferred_term.id_thesaurus = note.id_thesaurus AND preferred_term.id_thesaurus = '"+idThesaurus+"' " +
                    "AND note.lang = '"+idLang+"' AND preferred_term.id_concept NOT IN " +
                    "(SELECT idconcept FROM concept_group_concept WHERE idthesaurus = '"+idThesaurus+"')");
            ResultSet resultSet = stmt.getResultSet();
            if(resultSet.next()) {
                count = resultSet.getInt(1);
            }
            stmt.close();
            conn.close();
        } catch (SQLException sqle) {
            log.error("Error while getting Count of Note in without Group", sqle);
        }
        return count;
    }

    /**
     * permet de retourner le nombre de notes (type concept et terme) pour les concepts qui n'ont pas de collection
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @return 
     */
    public int getNbrNoteSansGroup(HikariDataSource ds, String idThesaurus, String idLang) {
        int nbrNoteConcepts = getNbrNoteDesConceptsSansGroup(ds, idThesaurus, idLang);
        int nbrNoteTerms = getNbrNoteDesTermsSansGroup(ds, idThesaurus, idLang);
        return nbrNoteConcepts + nbrNoteTerms;
    }
    
    /**
     * permet de retourner le nombre de notes (type concept et terme) pour les concepts qui appartiennent à ce groupe
     * @param ds
     * @param idThesaurus
     * @param idGroup
     * @param idLang
     * @return 
     */
    public int getNbrNoteByGroup(HikariDataSource ds, String idGroup, String idThesaurus,  String idLang) {
        int nbrNoteConcepts = getNbrNoteByGroupTypeConcept(ds, idGroup, idThesaurus, idLang);
        int nbrNoteTerms = getNbrNoteByGroupTypeTerm(ds,idGroup, idThesaurus, idLang);
        return nbrNoteConcepts + nbrNoteTerms;
    }    

}

