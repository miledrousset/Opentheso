package fr.cnrs.opentheso.models.concept;

import java.util.ArrayList;
import java.util.List;
import fr.cnrs.opentheso.models.terms.NodeBT;
import fr.cnrs.opentheso.models.terms.NodeEM;
import fr.cnrs.opentheso.models.terms.NodeNT;
import fr.cnrs.opentheso.models.terms.NodeRT;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.Normalizer;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeConceptSearch implements Comparable {

    private String thesoName;    
    private String idTheso;
    private String idConcept;
    private String prefLabel;
    private String currentLang;
    private boolean isDeprecated;
    
    private List<NodeTermTraduction> nodeTermTraductions;
    private List<NodeBT> nodeBT;
    private List<NodeNT> nodeNT;
    private List<NodeRT> nodeRT;
    private List<NodeEM> nodeEM;
    private List<NodeGroup> nodeConceptGroup;
    private List<String> dupplicateWithIds;

    public void clear(){
        idConcept = null;
        prefLabel = null;

        nodeTermTraductions = new ArrayList<>();
        nodeBT = new ArrayList<>();
        nodeNT = new ArrayList<>();
        nodeRT = new ArrayList<>();
        nodeEM = new ArrayList<>();
        nodeConceptGroup = new ArrayList<>();
        dupplicateWithIds = new ArrayList<>();
    }

    @Override
    public int compareTo(Object o) {
        String str1, str2;
        str1 = Normalizer.normalize(this.prefLabel, Normalizer.Form.NFD);
        str1 = str1.replaceAll("[^\\p{ASCII}]", "");
        str2 = Normalizer.normalize(((NodeConceptSearch)o).prefLabel, Normalizer.Form.NFD);
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
    
}
