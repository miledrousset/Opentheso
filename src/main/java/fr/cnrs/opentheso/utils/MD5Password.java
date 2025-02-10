package fr.cnrs.opentheso.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Password {
    /*
     * Encode la chaine passée en paramètre avec l'algorithme MD5
     *
     * @param key : la chaine à encoder
     *
     * @return la valeur (string) hexadécimale sur 32 bits
     */

    public static String getEncodedPassword(String key) {
        try {
            var uniqueKey = key.getBytes();
            var hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
            var hashString = new StringBuilder();
            for (int i = 0; i < hash.length; ++i) {
                var hex = Integer.toHexString(hash[i]);
                if (hex.length() == 1) {
                    hashString.append('0');
                    hashString.append(hex.charAt(hex.length() - 1));
                } else {
                    hashString.append(hex.substring(hex.length() - 2));
                }
            }
            return hashString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new Error("no MD5 support in this VM : " + e.toString());
        }
    }
}
