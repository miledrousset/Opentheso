/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 *
 * @author julie
 */
public class ApiKeyHelper {
    
    private static Connection connection = null;
    
    public ApiKeyHelper() {
        if (connection == null) {
            try {
                connection = DataHelper.connect().getConnection();
            } catch (SQLException ex) {
                Logger.getLogger(ApiKeyHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Vérifie dans la base de donnée si la clé API fournie existe
     * @param apiKey Clé api
     * @return une ApiKeyState indiquant l'état de la vérification de la clé API
     **/
    public ApiKeyState checkApiKeyExistance(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) return ApiKeyState.EMPTY;
        if (connection == null) return ApiKeyState.DATABASE_UNAVAILABLE;
        
        try {
            boolean keyExist = apiKeyExistInDatabase(apiKey);
            if (keyExist) {
                return ApiKeyState.VALID;
            } else {
                return ApiKeyState.INVALID;
            }
        } catch (SQLException e) {
            Logger.getLogger(ApiKeyHelper.class.getName()).log(Level.SEVERE, null, e);
            return ApiKeyState.SQL_ERROR;
        }
    }
    
    public Response errorResponse(ApiKeyState state) {
        Status code = null;
        String msg = null;
        switch (state) {
            case EMPTY:
                code = Response.Status.FORBIDDEN;
                msg = "No API key given";
                break;
            case DATABASE_UNAVAILABLE:
                code = Response.Status.SERVICE_UNAVAILABLE;
                msg = "Database unavailable";
                break;
            case INVALID:
                code = Response.Status.FORBIDDEN;
                msg = "API key is invalid";
                break;
            case SQL_ERROR:
                code = Response.Status.INTERNAL_SERVER_ERROR;
                msg = "Server internal error";
                break;
        }
        
        return ResponseHelper.errorResponse(code, msg, CustomMediaType.APPLICATION_JSON_UTF_8);
    }
    
    private boolean apiKeyExistInDatabase(String apiKey) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) AS NB_LINES FROM users WHERE apikey = ?")) {
           stmt.setString(1, apiKey);
           ResultSet result = stmt.executeQuery();
           if (result.next()) {
               int rowCount = result.getInt("NB_LINES");
               if (rowCount == 1) {
                   return true;
               }  
           } 
        }     
        return false;
    }
    
    
    
}
