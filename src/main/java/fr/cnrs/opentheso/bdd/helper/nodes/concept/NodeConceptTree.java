package fr.cnrs.opentheso.bdd.helper.nodes.concept;

import java.text.Normalizer;


/**
 * Cette Classe permet de gérer les noeuds de Concept dans l'arbre.
 * 
 * @author miled.rousset
 */
  

public class NodeConceptTree implements Comparable {

	private String title;
	private String idConcept;
        private String notation = "";
        private String idThesaurus;
        private String idLang;
        private String statusConcept;
	private boolean haveChildren = false;
        private boolean isGroup =false;
        private boolean isSubGroup = false;
        private boolean isTopTerm =false;
        private boolean isTerm =false;
        private boolean isDeprecated = false;

    public boolean isIsTopTerm() {
        return isTopTerm;
    }

    public void setIsTopTerm(boolean isTopTerm) {
        this.isTopTerm = isTopTerm;
    }

    public boolean isIsTerm() {
        return isTerm;
    }

    public void setIsTerm(boolean isTerm) {
        this.isTerm = isTerm;
    }
        

        
    public NodeConceptTree() {
        this.title = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public String getIdLang() {
        return idLang;
    }

    public void setIdLang(String idLang) {
        this.idLang = idLang;
    }

    public boolean isHaveChildren() {
        return haveChildren;
    }

    public void setHaveChildren(boolean haveChildren) {
        this.haveChildren = haveChildren;
    }

    public boolean isIsDeprecated() {
        return isDeprecated;
    }

    public void setIsDeprecated(boolean isDeprecated) {
        this.isDeprecated = isDeprecated;
    }

    @Override
    public int compareTo(Object o) {
        String str1, str2;
        str1 = Normalizer.normalize(this.title, Normalizer.Form.NFD);
        str1 = str1.replaceAll("[^\\p{ASCII}]", "");
        str2 = Normalizer.normalize(((NodeConceptTree)o).title, Normalizer.Form.NFD);
        str2 = str2.replaceAll("[^\\p{ASCII}]", "");
        return naturalCompare(str1, str2, true);
        //return str1.toUpperCase().compareTo(str2.toUpperCase());
    }
    
    private int naturalCompare(String a, String b, boolean ignoreCase) {
        if (ignoreCase) {
            a = a.toLowerCase();
            b = b.toLowerCase();
        }
        int aLength = a.length();
        int bLength = b.length();
        int minSize = Math.min(aLength, bLength);
        char aChar, bChar;
        boolean aNumber, bNumber;
        boolean asNumeric = false;
        int lastNumericCompare = 0;
        for (int i = 0; i < minSize; i++) {
            aChar = a.charAt(i);
            bChar = b.charAt(i);
            aNumber = aChar >= '0' && aChar <= '9';
            bNumber = bChar >= '0' && bChar <= '9';
            if (asNumeric)
                if (aNumber && bNumber) {
                    if (lastNumericCompare == 0)
                        lastNumericCompare = aChar - bChar;
                } else if (aNumber)
                    return 1;
                else if (bNumber)
                    return -1;
                else if (lastNumericCompare == 0) {
                    if (aChar != bChar)
                        return aChar - bChar;
                    asNumeric = false;
                } else
                    return lastNumericCompare;
            else if (aNumber && bNumber) {
                asNumeric = true;
                if (lastNumericCompare == 0)
                    lastNumericCompare = aChar - bChar;
            } else if (aChar != bChar)
                return aChar - bChar;
        }
        if (asNumeric)
            if (aLength > bLength && a.charAt(bLength) >= '0' && a.charAt(bLength) <= '9') // as number
                return 1;  // a has bigger size, thus b is smaller
            else if (bLength > aLength && b.charAt(aLength) >= '0' && b.charAt(aLength) <= '9') // as number
                return -1;  // b has bigger size, thus a is smaller
            else if (lastNumericCompare == 0)
              return aLength - bLength;
            else
                return lastNumericCompare;
        else
            return aLength - bLength;
    }    
    

    public String getStatusConcept() {
        return statusConcept;
    }

    public void setStatusConcept(String statusConcept) {
        this.statusConcept = statusConcept;
    }

    public boolean isIsGroup() {
        return isGroup;
    }

    public boolean isIsSubGroup() {
        return isSubGroup;
    }

    public void setIsSubGroup(boolean isSubGroup) {
        this.isSubGroup = isSubGroup;
    }

    public void setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    

}
