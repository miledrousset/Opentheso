package fr.cnrs.opentheso.bean.proposition.helper;

import fr.cnrs.opentheso.bean.proposition.dao.PropositionDao;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropositionHelper {

    private final Log log = LogFactory.getLog(PropositionHelper.class);

    public List<PropositionDao> getAllProposition(HikariDataSource ds) {

        List<PropositionDao> propositions = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT pro.*, term.lexical_value " +
                                "FROM proposition_modification pro LEFT JOIN preferred_term pre ON pro.id_concept = pre.id_concept AND pro.id_theso = pre.id_thesaurus " +
                                "LEFT JOIN term ON pre.id_term = term.id_term AND pro.lang = term.lang AND pro.id_theso = term.id_thesaurus " +
                                "ORDER BY pro.id DESC");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        propositions.add(toPropositionDao(resultSet));
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println("Erreur : " + sqle.getMessage());
        }

        return propositions;
    }

    public List<PropositionDao> getAllPropositionByStatus(HikariDataSource ds, String status) {

        List<PropositionDao> propositions = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT pro.*, term.lexical_value " +
                                "FROM proposition_modification pro LEFT JOIN preferred_term pre ON pro.id_concept = pre.id_concept AND pro.id_theso = pre.id_thesaurus " +
                                "LEFT JOIN term ON pre.id_term = term.id_term AND pro.lang = term.lang AND pro.id_theso = term.id_thesaurus " +
                                "WHERE pro.status = '" + status + "' ORDER BY pro.id DESC");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        propositions.add(toPropositionDao(resultSet));
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println("Erreur : " + sqle.getMessage());
        }

        return propositions;
    }

    public List<PropositionDao> getOldPropositionByStatus(HikariDataSource ds) {

        List<PropositionDao> propositions = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT pro.*, term.lexical_value " +
                                "FROM proposition_modification pro LEFT JOIN preferred_term pre ON pro.id_concept = pre.id_concept AND pro.id_theso = pre.id_thesaurus " +
                                "LEFT JOIN term ON pre.id_term = term.id_term AND pro.lang = term.lang AND pro.id_theso = term.id_thesaurus " +
                                "WHERE pro.status NOT IN ('LU', 'ENVOYER') ORDER BY pro.id DESC");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        propositions.add(toPropositionDao(resultSet));
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println("Erreur : " + sqle.getMessage());
        }

        return propositions;
    }

    public PropositionDao searchPropositionByEmailAndConceptAndLang(HikariDataSource ds, String email, 
            String conceptID, String lang) {

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT DISTINCT pro.*, term.lexical_value "
                        + "FROM proposition_modification pro, preferred_term pre, term "
                        + "WHERE pro.id_concept = pre.id_concept "
                        + "AND pre.id_term = term.id_term "
                        + "AND pro.email = '" + email + "' "
                        + "AND pro.id_concept = '" + conceptID + "' "
                        + "AND pro.lang = '" + lang + "'" 
                        + "AND pro.status IN ('ENVOYER', 'LU')");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        return toPropositionDao(resultSet);
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println("Erreur : " + sqle.getMessage());
        }

        return null;
    }

    public int searchNbrPorpositoinByStatus(HikariDataSource ds, String status) {

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select count(*) AS nbr from proposition_modification WHERE status = '" + status + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        return resultSet.getInt("nbr");
                    } else {
                        return 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println("Erreur : " + sqle.getMessage());
            return 0;
        }
    }

    private PropositionDao toPropositionDao(ResultSet resultSet) throws SQLException {
        PropositionDao proposition = new PropositionDao();
        proposition.setId(resultSet.getInt("id"));
        proposition.setIdConcept(resultSet.getString("id_concept"));
        proposition.setLang(resultSet.getString("lang"));
        proposition.setIdTheso(resultSet.getString("id_theso"));
        proposition.setStatus(resultSet.getString("status"));
        proposition.setDatePublication(resultSet.getString("date"));
        proposition.setNom(resultSet.getString("nom"));
        proposition.setEmail(resultSet.getString("email"));
        proposition.setCommentaire(resultSet.getString("commentaire"));
        proposition.setUserAction(resultSet.getString("approuve_par"));
        proposition.setDateUpdate(resultSet.getString("approuve_date"));
        proposition.setNomConcept(resultSet.getString("lexical_value"));
        return proposition;
    }

    public int createNewProposition(HikariDataSource ds, PropositionDao proposition) {
        try {
            PreparedStatement ps = ds.getConnection().prepareStatement("Insert into proposition_modification "
                    + "(id_concept, id_theso, lang, status, date, nom, email, commentaire) values ('"
                    + proposition.getIdConcept() + "','" + proposition.getIdTheso()
                    + "','" + proposition.getLang() + "', '" + proposition.getStatus() + "'" + ",'"
                    + proposition.getDatePublication() + "', '" + proposition.getNom() + "', '"
                    + proposition.getEmail() + "', '" + proposition.getCommentaire() + "')",
                    Statement.RETURN_GENERATED_KEYS);

            ps.execute();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            log.error("Erreur lors de la creation d'une nouvelle proposition : " + ex.getMessage());
            return -1;
        }
    }

    public boolean updateStatusProposition(HikariDataSource ds, String status, String approuvePar, String approuveDate, int propositionId) {
        boolean updateStatus = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("UPDATE proposition_modification SET status = '" + status + "', approuve_par = '" 
                        + approuvePar + "', approuve_date = '" + approuveDate + "' WHERE id = " + propositionId);
                updateStatus = true;
            }
        } catch (SQLException sqle) {
            log.error("Erreur lors de la mise à jour du status de la proposition : " + propositionId);
        }
        return updateStatus;
    }

    public boolean setLuStatusProposition(HikariDataSource ds, int propositionId) {
        boolean updateStatus = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("UPDATE proposition_modification SET status = 'LU' WHERE id = " + propositionId);
                updateStatus = true;
            }
        } catch (SQLException sqle) {
            log.error("Erreur lors de la mise à jour du status de la proposition : " + propositionId);
        }
        return updateStatus;
    }

    public boolean supprimerProposition(HikariDataSource ds, int propositionID) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM proposition_modification WHERE id = " + propositionID);
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error lords de la suppression de la proposition : " + propositionID);
        }
        return status;
    }

}
