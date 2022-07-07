package fr.cnrs.opentheso.bdd.helper.nodes.concept;

import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRT;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import java.text.Normalizer;

public class NodeConceptSearch implements Comparable {
    private String thesoName;    
    private String idTheso;
    private String idConcept;
    private String prefLabel;
    private boolean isDeprecated;
    private String currentLang;
    
    private ArrayList <NodeTermTraduction> nodeTermTraductions;
    private ArrayList <NodeBT> nodeBT;
    private ArrayList <NodeNT> nodeNT;
    private ArrayList <NodeRT> nodeRT;
    private ArrayList<NodeEM> nodeEM;
    private ArrayList<NodeGroup> nodeConceptGroup;    

//    private ArrayList<NodeNote> nodeNotesTerm;
//    private ArrayList<NodeNote> nodeNotesConcept;

    public void clear(){
        idConcept = null;
        prefLabel = null;
        if(nodeTermTraductions != null)
            nodeTermTraductions.clear();
        if(nodeBT != null)
            nodeBT.clear();        
        if(nodeNT != null)
            nodeNT.clear();    
        if(nodeRT != null)
            nodeRT.clear(); 
        if(nodeEM != null)
            nodeEM.clear(); 
        if(nodeConceptGroup != null)
            nodeConceptGroup.clear();
    }
    
    public NodeConceptSearch() {
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public ArrayList<NodeTermTraduction> getNodeTermTraductions() {
        return nodeTermTraductions;
    }

    public void setNodeTermTraductions(ArrayList<NodeTermTraduction> nodeTermTraductions) {
        this.nodeTermTraductions = nodeTermTraductions;
    }

    public ArrayList<NodeBT> getNodeBT() {
        return nodeBT;
    }

    public void setNodeBT(ArrayList<NodeBT> nodeBT) {
        this.nodeBT = nodeBT;
    }

    public ArrayList<NodeNT> getNodeNT() {
        return nodeNT;
    }

    public void setNodeNT(ArrayList<NodeNT> nodeNT) {
        this.nodeNT = nodeNT;
    }

    public ArrayList<NodeRT> getNodeRT() {
        return nodeRT;
    }

    public void setNodeRT(ArrayList<NodeRT> nodeRT) {
        this.nodeRT = nodeRT;
    }

    public ArrayList<NodeEM> getNodeEM() {
        return nodeEM;
    }

    public void setNodeEM(ArrayList<NodeEM> nodeEM) {
        this.nodeEM = nodeEM;
    }

    public ArrayList<NodeGroup> getNodeConceptGroup() {
        return nodeConceptGroup;
    }

    public void setNodeConceptGroup(ArrayList<NodeGroup> nodeConceptGroup) {
        this.nodeConceptGroup = nodeConceptGroup;
    }

    public boolean isIsDeprecated() {
        return isDeprecated;
    }

    public void setIsDeprecated(boolean isDeprecated) {
        this.isDeprecated = isDeprecated;
    }

    public String getIdTheso() {
        return idTheso;
    }

    public void setIdTheso(String idTheso) {
        this.idTheso = idTheso;
    }

    public String getCurrentLang() {
        return currentLang;
    }

    public void setCurrentLang(String currentLang) {
        this.currentLang = currentLang;
    }

    public String getThesoName() {
        return thesoName;
    }

    public void setThesoName(String thesoName) {
        this.thesoName = thesoName;
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
