/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.helper;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Julien LINGET
 */
public class HeaderHelper {
    
    /**
     * Retire l'option ;charset= de la chaine de caractère contentType
     * @param contentType Type de donnée passé en header HTTP avec la précision du charset
     * @return Le type de donnée en minuscule sans la précision du charset
     */
    public static String removeCharset(String contentType) {
        if(StringUtils.isEmpty(contentType)) return null;
        
        contentType = contentType.toLowerCase();
        int indexCharset = contentType.indexOf(";charset=");
        if (indexCharset == -1) {
            return contentType;
        }
        return contentType.substring(0, indexCharset);
    }
    
}
