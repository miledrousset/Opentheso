package fr.cnrs.opentheso.utils;


import java.net.URI;
import java.net.URISyntaxException;

public class ToolsHelper {

    public static String getNewId(int length, boolean isUpperCase, boolean isUseNoidCheck) {
        String chars = "0123456789bcdfghjklmnpqrstvwxz";
        StringBuilder pass = new StringBuilder();
        String idArk;
        for (int x = 0; x < length; x++) {
            int i = (int) Math.floor(Math.random() * (chars.length() - 1));
            pass.append(chars.charAt(i));
        }
        idArk = pass.toString();
        if(isUseNoidCheck) {
            NoIdCheckDigit noIdCheckDigit = new NoIdCheckDigit();
            String checkCode = noIdCheckDigit.getControlCharacter(idArk);
            idArk = idArk + "-" + checkCode;             
        }
        
        if(isUpperCase)
            return idArk.toUpperCase();
        else
            return idArk;
    }

    public boolean isValidURI(String uriStr) {
        try {
            URI uri = new URI(uriStr);
            return uri.getScheme() != null && (uri.getHost() != null || uri.getSchemeSpecificPart() != null);
        } catch (URISyntaxException e) {
            return false;
        }
    }

}
