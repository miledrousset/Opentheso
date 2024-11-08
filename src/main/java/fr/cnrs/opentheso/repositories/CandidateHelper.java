package fr.cnrs.opentheso.repositories;

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
import java.sql.Timestamp;

/**
 *
 * @author miled.rousset
 */
@Slf4j
@Service
public class CandidateHelper {

    @Autowired
    private ConceptHelper conceptHelper;

    @Autowired
    private DataSource dataSource;


    /**
     * permet de réactiver un candidat s'il a été rejeté
     */
    public boolean reactivateRejectedCandidat (String idTheso, String idCandidat){
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("update candidat_status set id_status = 1" +
                    " WHERE id_concept = '" + idCandidat + "' and id_thesaurus = '" + idTheso + "'");
            return true;
        } catch (SQLException sqle) {
            log.error("Error while getting List Group or Domain of thesaurus : " + idTheso, sqle);
            return false;
        }
    }


    //Sauvegarde un candidat en base à partir d'un JSON
    public boolean saveCandidat (Candidate candidate, int userId) {

        try (Connection conn = dataSource.getConnection()){
            conn.setAutoCommit(false);

            var idTerm = conceptHelper.getNumericConceptId(conn);
            var idConcept = conceptHelper.getNumericConceptId(conn);
            if (CollectionUtils.isNotEmpty(candidate.getTerme())) {
                for (Element term : candidate.getTerme()) {
                    insertTerm(candidate.getThesoId(), userId, term.getLang(), idTerm, term.getValue());
                }
            }

            insertConcept(candidate.getThesoId(), userId, idConcept);

            insertCollection(candidate.getCollectionId(), candidate.getThesoId(), idConcept);

            if (CollectionUtils.isNotEmpty(candidate.getDefinition())) {
                for (Element definition : candidate.getDefinition()) {
                    insertNoteInCandidat("definition", candidate.getThesoId(), idTerm, definition.getLang(),
                            definition.getValue(), userId, candidate.getSource(), idConcept);
                }
            }

            if (CollectionUtils.isNotEmpty(candidate.getNote())) {
                for (Element note : candidate.getNote()) {
                    insertNoteInCandidat("note", candidate.getThesoId(), idTerm, note.getLang(),
                            note.getValue(), userId, candidate.getSource(), idConcept);
                }
            }

            if (StringUtils.isNotEmpty(candidate.getComment())) {
                insertComment(candidate.getComment(), userId, idConcept, candidate.getThesoId());
            }

            if (CollectionUtils.isNotEmpty(candidate.getSynonymes())) {
                for (Element synonyme : candidate.getSynonymes()) {
                    insertSynonymes(synonyme.getValue(), synonyme.getLang(), candidate.getThesoId(), idTerm);
                }
            }

            insertPreferredTerm(candidate.getThesoId(), idTerm, idConcept);

            insertCandidat(idConcept, candidate.getThesoId(), userId);

            conn.commit();
        }catch (SQLException e) {

        }
        return true;
    }


    private void insertComment(String message, int idUser, String idConcept, String idTheso) throws SQLException {

        var sql = "INSERT INTO candidat_messages(value, id_user, date, id_concept, id_thesaurus) VALUES (?, ?, ?, ?, ?)";
        try(var connexion = dataSource.getConnection();
            var statement = connexion.prepareStatement(sql)) {
            statement.setString(1, message);
            statement.setInt(2, idUser);
            statement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            statement.setInt(4, Integer.parseInt(idConcept));
            statement.setString(5, idTheso);
            statement.executeUpdate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void insertSynonymes(String value, String lang, String thesoId, String termId) throws SQLException {

        var sql = "INSERT INTO non_preferred_term(lexical_value, lang, id_thesaurus, hiden, id_term) VALUES (?, ?, ?, false, ?)";
        try(var connexion = dataSource.getConnection();
            var statement = connexion.prepareStatement(sql)) {
            statement.setString(1, value);
            statement.setString(2, lang);
            statement.setString(3, thesoId);
            statement.setString(4, termId);
            statement.executeUpdate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

    }

    private void insertCollection(String collectionId, String thesoId, String conceptId) throws SQLException {

        var sql = "INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES (?, ?, ?)";
        try (var connexion = dataSource.getConnection(); var stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, collectionId);
            stmt.setString(2, thesoId);
            stmt.setString(3, conceptId);

            stmt.executeUpdate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void insertTerm(String thesoId, int userId, String lang, String idTerm, String title) throws SQLException {

        var sql = "INSERT INTO term (id_term, lexical_value, lang, id_thesaurus, created, status, source, creator) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (var connexion = dataSource.getConnection(); var stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, idTerm);
            stmt.setString(2, title);
            stmt.setString(3, lang);
            stmt.setString(4, thesoId);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setString(6, "D");
            stmt.setString(7, "candidat");
            stmt.setInt(8, userId);

            stmt.executeUpdate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void insertConcept(String thesoId, int userId, String idConcept) throws SQLException {
        var sql = "INSERT INTO concept (id_concept, id_thesaurus, created, status, concept_type, creator, top_concept) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (var connexion = dataSource.getConnection(); var stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, idConcept);
            stmt.setString(2, thesoId);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setString(4, "CA");
            stmt.setString(5, "concept");
            stmt.setInt(6, userId);
            stmt.setBoolean(7, false);

            stmt.executeUpdate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void insertPreferredTerm(String thesoId, String idTerm, String idConcept) throws SQLException {

        var sql = "INSERT INTO preferred_term (id_concept, id_term, id_thesaurus) VALUES (?, ?, ?)";
        try (var connexion = dataSource.getConnection(); var stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, idConcept);
            stmt.setString(2, idTerm);
            stmt.setString(3, thesoId);
            stmt.executeUpdate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void insertNoteInCandidat(String type, String thesoId, String idTerm, String lang, String description,
                                      int userId, String source, String idConcept) throws SQLException {

        var sql = "INSERT INTO note (notetypecode, id_thesaurus, id_term, lang, lexicalvalue, created, modified, id_user, notesource, identifier) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (var connexion = dataSource.getConnection(); var stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.setString(2, thesoId);
            stmt.setString(3, idTerm);
            stmt.setString(4, lang);
            stmt.setString(5, description);
            stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(8, userId);
            stmt.setString(9, source);
            stmt.setString(10, idConcept);

            stmt.executeUpdate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    private void insertCandidat(String idConcept, String thesoId, int userId) throws SQLException {

        var sql = "INSERT INTO candidat_status(id_concept, id_status, date, id_user, id_thesaurus) VALUES (?, ?, ?, ?, ?)";
        try (var connexion = dataSource.getConnection(); var stmt = connexion.prepareStatement(sql)) {
            stmt.setString(1, idConcept);
            stmt.setInt(2, 1);
            stmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            stmt.setInt(4, userId);
            stmt.setString(5, thesoId);

            stmt.executeUpdate();
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }
}
