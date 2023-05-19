/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.helper;

/**
 *
 * @author julie
 */
public enum ApiKeyState {
    
    /**
     * Aucune clé API n'a été fournie
     */
    EMPTY,
    
    /**
     * La clé fournie est invalide
     */
    INVALID,
    
    /**
     * Erreur lors de l'exécution du SQL
     */
    SQL_ERROR,
    
    /**
     * Erreur lors de la connexion à la base de donnée
     */
    DATABASE_UNAVAILABLE,
    
    /**
     * La clé API est valide
     */
    VALID    
    
}
