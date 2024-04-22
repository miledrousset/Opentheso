package fr.cnrs.opentheso.bdd.helper.nodes;

import lombok.Data;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.List;

/**
 *
 * @author miled.rousset
 */
@Data
public class NodeIdValue implements Comparable, Serializable{

    private String id;
    private String value;
    private boolean Status;
    private String notation;
    private Integer nbrConcepts;
    private List<NodeAlignment> alignements;

    @Override
    public int compareTo(Object o) {
        try {
            String str1, str2;
            str1 = Normalizer.normalize(this.value, Normalizer.Form.NFD);
            str1 = str1.replaceAll("[^\\p{ASCII}]", "");
            str2 = Normalizer.normalize(((NodeIdValue)o).value, Normalizer.Form.NFD);
            str2 = str2.replaceAll("[^\\p{ASCII}]", "");
            return naturalCompare(str1, str2, true);
            //return str1.toUpperCase().compareTo(str2.toUpperCase());
        } catch (Exception e) {
            return 1;
        }
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
}
