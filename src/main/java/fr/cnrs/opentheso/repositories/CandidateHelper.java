package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.repositories.candidats.RelationDao;
import fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost.Candidate;
import fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost.Element;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 *
 * @author miled.rousset
 */
@Slf4j
@Service
public class CandidateHelper {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private RelationDao relationDao;


    /**
     * permet de réactiver un candidat s'il a été rejeté
     */
    public boolean reactivateRejectedCandidat (String idTheso, String idCandidat){
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("update candidat_status set id_status = 1" +
                        " WHERE id_concept = '" + idCandidat + "' and id_thesaurus = '" + idTheso + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " + idTheso, sqle);
        }
        return false;        
    }


    //Sauvegarde un candidat en base à partir d'un JSON
    public boolean saveCandidat (Candidate candidate, int userId) {

        try (Connection conn = dataSource.getConnection()){
            conn.setAutoCommit(false);

            var idTerm = conceptHelper.getNumericConceptId(conn);
            var idConcept = conceptHelper.getNumericConceptId(conn);
            if (CollectionUtils.isNotEmpty(candidate.getTerme())) {
                for (Element term : candidate.getTerme()) {
                    insertTerm(conn, candidate.getThesoId(), userId, term.getLang(), idTerm, term.getValue());
                }
            }

            insertConcept(conn, candidate.getThesoId(), userId, idConcept);

            insertCollection(conn, candidate.getCollectionId(), candidate.getThesoId(), idConcept);

            if (CollectionUtils.isNotEmpty(candidate.getDefinition())) {
                for (Element definition : candidate.getDefinition()) {
                    insertNoteInCandidat(conn, "definition", candidate.getThesoId(), idTerm, definition.getLang(),
                            definition.getValue(), userId, candidate.getSource(), idConcept);
                }
            }

            if (CollectionUtils.isNotEmpty(candidate.getNote())) {
                for (Element note : candidate.getNote()) {
                    insertNoteInCandidat(conn, "note", candidate.getThesoId(), idTerm, note.getLang(),
                            note.getValue(), userId, candidate.getSource(), idConcept);
                }
            }

            if (StringUtils.isNotEmpty(candidate.getComment())) {
                insertComment(conn, candidate.getComment(), userId, idConcept, candidate.getThesoId());
            }

            if (CollectionUtils.isNotEmpty(candidate.getSynonymes())) {
                for (Element synonyme : candidate.getSynonymes()) {
                    insertSynonymes(conn, synonyme.getValue(), synonyme.getLang(), candidate.getThesoId(), idTerm);
                }
            }

            if (StringUtils.isNotEmpty(candidate.getConceptGenericId())) {
                relationDao.addRelationBT(idConcept, candidate.getConceptGenericId(), candidate.getThesoId());
            }

            insertPreferredTerm(conn, candidate.getThesoId(), idTerm, idConcept);

            insertCandidat(conn, idConcept, candidate.getThesoId(), userId);

            conn.commit();
            return true;
        }catch (SQLException e) {
            return false;
        }
    }


    private void insertComment(Connection conn, String message, int idUser, String idConcept, String idTheso) throws SQLException {

        var termStmt = conn.prepareStatement("INSERT INTO candidat_messages(value, id_user, date, id_concept, id_thesaurus) VALUES (?, ?, ?, ?, ?)");
        termStmt.setString(1, message);
        termStmt.setInt(2, idUser);
        termStmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
        termStmt.setInt(4, Integer.parseInt(idConcept));
        termStmt.setString(5, idTheso);

        termStmt.executeUpdate();
    }

    private void insertSynonymes(Connection conn, String value, String lang, String thesoId, String termId) throws SQLException {

        var termStmt = conn.prepareStatement("INSERT INTO non_preferred_term(lexical_value, lang, id_thesaurus, hiden, id_term) VALUES (?, ?, ?, false, ?)");
        termStmt.setString(1, value);
        termStmt.setString(2, lang);
        termStmt.setString(3, thesoId);
        termStmt.setString(4, termId);

        termStmt.executeUpdate();
    }

    private void insertCollection(Connection conn, String collectionId, String thesoId, String conceptId) throws SQLException {

        var termStmt = conn.prepareStatement("INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES (?, ?, ?)");
        termStmt.setString(1, collectionId);
        termStmt.setString(2, thesoId);
        termStmt.setString(3, conceptId);

        termStmt.executeUpdate();
    }

    private void insertTerm(Connection conn, String thesoId, int userId, String lang, String idTerm, String title) throws SQLException {

        var termStmt = conn.prepareStatement("INSERT INTO term (id_term, lexical_value, lang, id_thesaurus, created, status, source, creator) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
        termStmt.setString(1, idTerm);
        termStmt.setString(2, title);
        termStmt.setString(3, lang);
        termStmt.setString(4, thesoId);
        termStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
        termStmt.setString(6, "D");
        termStmt.setString(7, "candidat");
        termStmt.setInt(8, userId);

        termStmt.executeUpdate();
    }

    private void insertConcept(Connection conn, String thesoId, int userId, String idConcept) throws SQLException {

        var conceptStmt = conn.prepareStatement("INSERT INTO concept (id_concept, id_thesaurus, created, status, concept_type, creator, top_concept) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");
        conceptStmt.setString(1, idConcept);
        conceptStmt.setString(2, thesoId);
        conceptStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        conceptStmt.setString(4, "CA");
        conceptStmt.setString(5, "concept");
        conceptStmt.setInt(6, userId);
        conceptStmt.setBoolean(7, false);

        conceptStmt.executeUpdate();
    }

    private void insertPreferredTerm(Connection conn, String thesoId, String idTerm, String idConcept) throws SQLException {

        var preferredTermStmt = conn.prepareStatement("INSERT INTO preferred_term (id_concept, id_term, id_thesaurus) VALUES (?, ?, ?)");
        preferredTermStmt.setString(1, idConcept);
        preferredTermStmt.setString(2, idTerm);
        preferredTermStmt.setString(3, thesoId);

        preferredTermStmt.executeUpdate();
    }

    private void insertNoteInCandidat(Connection conn, String type, String thesoId, String idTerm, String lang, String description,
                                      int userId, String source, String idConcept) throws SQLException {

        var insertNoteSQL = "INSERT INTO note (notetypecode, id_thesaurus, id_term, lang, lexicalvalue, created, modified, id_user, notesource, identifier) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        var noteStmt = conn.prepareStatement(insertNoteSQL);
        noteStmt.setString(1, type);
        noteStmt.setString(2, thesoId);
        noteStmt.setString(3, idTerm);
        noteStmt.setString(4, lang);
        noteStmt.setString(5, description);
        noteStmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
        noteStmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
        noteStmt.setInt(8, userId);
        noteStmt.setString(9, source);
        noteStmt.setString(10, idConcept);

        noteStmt.executeUpdate();
    }

    private void insertCandidat(Connection conn, String idConcept, String thesoId, int userId) throws SQLException {

        var candidatStatusStmt = conn.prepareStatement("INSERT INTO candidat_status(id_concept, id_status, date, id_user, id_thesaurus) VALUES (?, ?, ?, ?, ?)");
        candidatStatusStmt.setString(1, idConcept);
        candidatStatusStmt.setInt(2, 1);
        candidatStatusStmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
        candidatStatusStmt.setInt(4, userId);
        candidatStatusStmt.setString(5, thesoId);

        candidatStatusStmt.executeUpdate();
    }
}
