package fr.cnrs.opentheso.ws.openapi.helper;

import fr.cnrs.opentheso.repositories.ToolsHelper;
import fr.cnrs.opentheso.utils.MD5Password;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

/**
 * Helper permettant de vérifier l'existence d'une clé API dans la table useres
 */
@Service
public class ApiKeyHelper {

    @Autowired
    private ToolsHelper toolsHelper;

    @Autowired
    private DataSource dataSource;

    /**
     * Génère une clé API d'une longueur choisie avec le header voulu
     * @param header le header de la clé API
     * @param keyLength la longueur de la clé API
     * @return la clé API
     */
    public String generateApiKey(String header, int keyLength) {
        final String timestamp = String.valueOf(System.currentTimeMillis());
        final String randomKey = header.length() + timestamp.length() < keyLength ? toolsHelper.getNewId(keyLength-header.length()-timestamp.length(), false, false) : "";
        String apiKey= header + timestamp + randomKey;
        if (apiKey.length() > keyLength) {
            apiKey = apiKey.substring(0, keyLength);
        }
        return apiKey;
    }

    /**
     * @param apiKey Clé d'API à sauvegarder
     * @param idUser id de l'utilisateur propriétaire de la clé
     * @return True si la clé a bien été sauvegardé, False sinon
     * @throws SQLException Appelé quand il y a eu une erreur dans l'exécution de la commande SQL
     */
    public boolean saveApiKey(String apiKey, int idUser) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement stmt = connection.prepareStatement("UPDATE users SET apikey = ? WHERE id_user = ?");
            stmt.setString(1, apiKey);
            stmt.setInt(2, idUser);
            int result = stmt.executeUpdate();
            return result > 0;
        } catch (SQLException sqle) {
            return false;
        }
    }

    /**
     * Renvoie une réponse adaptée si l'état de la clé n'est pas VALID
     * @param state Etat de la clé API
     * @return Response avec le message erreur correspondant
     */
    public ResponseEntity<Object> errorResponse(ApiKeyState state) {
        int code = 0;
        String msg = switch (state) {
            case EMPTY -> {
                code = Response.Status.UNAUTHORIZED.getStatusCode();
                yield "No API key given";
            }
            case DATABASE_UNAVAILABLE -> {
                code = Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
                yield "Database unavailable";
            }
            case INVALID -> {
                code = Response.Status.FORBIDDEN.getStatusCode();
                yield "API key is invalid";
            }
            case SQL_ERROR -> {
                code = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
                yield "Server internal error";
            }
            case EXPIRED -> {
                code = Response.Status.UNAUTHORIZED.getStatusCode();
                yield "API key is expired";
            }
            default -> null;
        };

        return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(msg);
    }

    /**
     * Retourne l'id de l'utilisateur propriétaire de la clé
     * @param apiKey
     * @return Optional<Integer> Id utilisateur trouvé ou null sinon
     */
    public int getIdUser(String apiKey) {

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT id_user, key_expires_at FROM users WHERE apikey =?")) {
                stmt.setString(1, MD5Password.getEncodedPassword(apiKey));
                ResultSet result = stmt.executeQuery();
                if (result.next()) {
                    return result.getInt("id_user");
                }
                return -1;
            }
        } catch (SQLException sqle) {
            return -1;
        }
    }

    /**
     * Vérifie si la clé est valide, existe et n'est pas expirée
     * @param apiKey
     * @return ApiKeyState Etat de validation de la clé
     */
    public ApiKeyState checkApiKey(String apiKey){
        if (apiKey == null || apiKey.isEmpty()) return ApiKeyState.EMPTY;

        try (Connection connection = dataSource.getConnection()) {
            try(PreparedStatement stmt = connection.prepareStatement("SELECT key_expires_at, key_never_expire  FROM users WHERE apikey =?")){
                stmt.setString(1, MD5Password.getEncodedPassword(apiKey));
                ResultSet result = stmt.executeQuery();
                if (!result.next()) {return ApiKeyState.INVALID;}
                if (!result.getBoolean("key_never_expire")) {
                    if (result.getDate("key_expires_at")==null){return ApiKeyState.INVALID;}
                    if (LocalDate.now().isAfter(result.getDate("key_expires_at").toLocalDate())){return ApiKeyState.EXPIRED;}
                }
                return ApiKeyState.VALID;

            }
        } catch (SQLException sqle) {
            return null;
        }
    }
    
}
