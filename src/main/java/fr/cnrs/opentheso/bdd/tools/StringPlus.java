package fr.cnrs.opentheso.bdd.tools;

import java.text.Normalizer;

public class StringPlus {

public StringPlus() {

    }
    
    /**
     * Permet de supprimer les accents d'un String
     * @param s
     * @return 
     */
    public String unaccentLowerString(String s) 
    {
        if(s == null) return s;
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s.toLowerCase();
    }
    
    /**
     * Cette fonction permet de caster les cotes pour les faire passer par le
     * jdbc pour échanger avec les bases de données PostgreSQL exp : l'équipe ->
     * l''équipe
     *
     * @param ligne
     * @return String une phrase avec des cotes en plus
     */
    public String addQuotes(String ligne) {
        String ligne_prete = "";
        String ligne_temp;
        int index;

        while (true) {
            index = ligne.indexOf("'");
            if (index == -1) {
                return (ligne_prete + ligne);
            }
            ligne_temp = ligne.substring(0, index + 1);
            ligne_prete = ligne_prete + ligne_temp.concat("'");
            ligne = ligne.substring(index + 1, ligne.length());
        }
    }

    /**
     * Handle quotes and backslashes in string before database insertion.
     *
     * @param s The string to convert.
     * @return A safe string for database insertion.
     */
    public String convertString(String s) {
        if (s == null) {
            return null;
        }
        // Handle quotes
        s = s.replaceAll("\\'", "''");
        // Handle backslashes (You like the Java style...)
        s = s.replaceAll("\\\\", "\\\\\\\\");
        return s.trim();
    }

    /**
     * Fonction qui permet de normaliser les textes pour les documents XML
     *
     * @param s
     * @return
     */
    public String normalizeStringForXml(String s) {
        if (s == null) {
            return null;
        }
        // normalisation of words for XML
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("\"", " ");
        s = s.replace("\"", " ");
        s = s.replaceAll("\n", " ");
        s = s.replaceAll("\t", " ");
        return s.trim();
    }

    /**
     * Fonction qui permet de normaliser les textes pour un script SQL à
     * appliquer en Java
     *
     * @param s
     * @return
     */
    public String normalizeStringForSQL(String s) {
        if (s == null) {
            return null;
        }
        // normalisation of words for XML
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("\"", " ");
        s = s.replace("\"", " ");
        s = s.replaceAll("\n", " ");
        s = s.replaceAll("\t", " ");
//        s = s.replaceAll("'", " ");
        return s.trim();
    }

    /**
     * Fonction qui permet de normaliser les textes pour les documents XML
     *
     * @param s
     * @return
     */
    public String normalizeStringForIdentifier(String s) {
        if (s == null) {
            return null;
        }
        // normalisation of words for identifier 
        // exp : ?idg=MT_10&idt=2
        // devient : idgMT_10idt2

        s = s.replaceAll("\\?", "");
        s = s.replaceAll("=", "");
        s = s.replaceAll("&", "");

        return s;
    }

    /**
     * Fonction qui permet de normaliser les textes pour les documents XML
     *
     * @param s
     * @return
     */
    public String clearStringForSerach(String s) {
        if (s == null) {
            return null;
        }
        // normalisation of words for XML
        s = s.replaceAll("&", " ");
        s = s.replaceAll("-", " ");
        s = s.replaceAll("_", " ");
        s = s.replaceAll("/", " ");
        s = s.replaceAll("(", " ");
        s = s.replaceAll(")", " ");

        return s;
    }

    public String clearAngles(String s) {
        if (s == null) {
            return null;
        }
        boolean premiereAngle = false;
        boolean deuxiemeAngle = false;
        String aux = "";
        char character;
        for (int i = 0; i < s.length(); i++) {
            character = s.charAt(i);
            if (character == '<') {
                premiereAngle = true;
            }
            if (!premiereAngle) {
                aux += character;
                deuxiemeAngle = false;
            }
            if (character == '>') {
                deuxiemeAngle = true;
                premiereAngle = false;
            }
        }
        return aux;
    }
    
    /**
     * permet de supprimer le retour à la ligne dans les commentaires, les notes ...
     * @param text
     * @return 
     */
    public String clearNewLine(String text) {
        text = text.replace('\r', ' ');
        text = text.replace('\n', ' ');
        text = text.replace('\t', ' ');
        return text;
    }

}
