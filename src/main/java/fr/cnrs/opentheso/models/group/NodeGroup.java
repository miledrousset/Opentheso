package fr.cnrs.opentheso.models.group;

import java.io.Serializable;
import java.sql.Date;
import java.text.Normalizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeGroup implements Serializable, Comparable<NodeGroup> {

    private ConceptGroup conceptGroup = new ConceptGroup();
    private String lexicalValue;
    private String idLang;
    private Date created;
    private Date modified;
    private String idUser;
    private int orde;
    private String original_value;
    private boolean ispreferredterm;
    private String notation;
    private boolean isHaveChildren;
    private boolean isSelected;
    private boolean groupPrivate;
    
    
    @Override
    public int compareTo(NodeGroup o) {
        if(StringUtils.isEmpty(o.getLexicalValue())) return 0;
        if(this.lexicalValue.equalsIgnoreCase(o.getLexicalValue())) return 0;

        //String str1, str2;
        if(StringUtils.isEmpty(this.lexicalValue)) return 0;
        var str1 = Normalizer.normalize(this.lexicalValue, Normalizer.Form.NFD);
        str1 = str1.replaceAll("[^\\p{ASCII}]", "");

        var str2 = Normalizer.normalize(o.getLexicalValue(), Normalizer.Form.NFD);
        str2 = str2.replaceAll("[^\\p{ASCII}]", "");

        return naturalCompare(str1, str2, true);
    }

    public int naturalCompare(String a, String b, boolean ignoreCase) {
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
