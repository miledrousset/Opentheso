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
        byte[] uniqueKey = key.getBytes();
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
        } catch (NoSuchAlgorithmException e) {
            throw new Error("no MD5 support in this VM : " + e.toString());
        }
        StringBuilder hashString = new StringBuilder();
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }
        }
        return hashString.toString();
    }
    /*
     * Test une chaine et une valeur encodée (chaine hexadécimale)
     *
     * @param clearTextTestPassword : la chaine non codée à tester
     * @param encodedActualPassword : la valeur hexa MD5 de référence
     *
     * @return true si vérifié false sinon
     */

    public static boolean testPassword(String clearTextTestPassword, String encodedActualPassword) throws NoSuchAlgorithmException {
        String encodedTestPassword = MD5Password.getEncodedPassword(clearTextTestPassword);
        return (encodedTestPassword.equals(encodedActualPassword));
    }
}
