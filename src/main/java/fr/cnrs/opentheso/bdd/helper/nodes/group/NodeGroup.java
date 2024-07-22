package fr.cnrs.opentheso.bdd.helper.nodes.group;

import java.io.Serializable;
import java.sql.Date;
import fr.cnrs.opentheso.bdd.datas.ConceptGroup;
import java.text.Normalizer;
import org.apache.commons.lang3.StringUtils;

public class NodeGroup implements Serializable, Comparable<NodeGroup> {
    
    private static final long serialVersionUID = 1L;

    private ConceptGroup conceptGroup;
    private String lexicalValue;
    private String idLang;
    private Date created;
    private Date modified;
    private String idUser;
    private int orde;
    private String original_value;
    private boolean  ispreferredterm;
    private String notation;
    private boolean isHaveChildren = false;
    
    private boolean isSelected;    

    public NodeGroup() {
        conceptGroup = new ConceptGroup();
    }

    public ConceptGroup getConceptGroup() {
        return conceptGroup;
    }

    public void setConceptGroup(ConceptGroup conceptGroup) {
        this.conceptGroup = conceptGroup;
    }

    public String getLexicalValue() {
        return lexicalValue;
    }

    public void setLexicalValue(String lexicalValue) {
        this.lexicalValue = lexicalValue;
    }

    public String getIdLang() {
        return idLang;
    }

    public void setIdLang(String idLang) {
        this.idLang = idLang;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

/*    @Override
    public int compareTo(Object o) {
        return this.lexicalValue.compareTo(((NodeGroup)o).lexicalValue);
    }
*/
    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public int getOrde() {
        return orde;
    }

    public void setOrde(int orde) {
        this.orde = orde;
    }

    public String getOriginal_value() {
        return original_value;
    }

    public void setOriginal_value(String original_value) {
        this.original_value = original_value;
    }

    public boolean isIspreferredterm() {
        return ispreferredterm;
    }

    public void setIspreferredterm(boolean ispreferredterm) {
        this.ispreferredterm = ispreferredterm;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public boolean isIsHaveChildren() {
        return isHaveChildren;
    }

    public void setIsHaveChildren(boolean isHaveChildren) {
        this.isHaveChildren = isHaveChildren;
    }

    public boolean isIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    
    
    
    @Override
    public int compareTo(NodeGroup o) {
        if(StringUtils.isEmpty(o.getLexicalValue())) return 0;
        if(this.lexicalValue.equalsIgnoreCase(o.getLexicalValue())) return 0;        
        
/*        if (o == null || getClass() != o.getClass()) {
            throw new ClassCastException("Incompatible types");
        }        
        NodeConceptTree other = (NodeConceptTree) o;
*/
        String str1 = StringUtils.defaultIfEmpty(this.lexicalValue, "");
        String str2 = StringUtils.defaultIfEmpty(o.getLexicalValue(), "");        
        
        
        //String str1, str2;
        if(StringUtils.isEmpty(this.lexicalValue)) return 0;
        str1 = Normalizer.normalize(this.lexicalValue, Normalizer.Form.NFD);
        str1 = str1.replaceAll("[^\\p{ASCII}]", "");
        str2 = Normalizer.normalize(((NodeGroup)o).getLexicalValue(), Normalizer.Form.NFD);
        str2 = str2.replaceAll("[^\\p{ASCII}]", "");
         
      //  int retour = naturalCompare(str1, str2, true);
     //   System.out.println("str1 :" + str1 + "________" + "str2 :" + str2 + " = " + retour );
        return naturalCompare(str1, str2, true);
        //return str1.toUpperCase().compareTo(str2.toUpperCase());
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
