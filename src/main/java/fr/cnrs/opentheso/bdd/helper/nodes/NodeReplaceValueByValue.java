package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author miledrousset
 */
public class NodeReplaceValueByValue {
    private String idConcept;
    private int SKOSProperty;
    private String oldValue;
    private String newValue;
    private String idLang;
    
    public NodeReplaceValueByValue() {
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }



    public int getSKOSProperty() {
        return SKOSProperty;
    }

    public void setSKOSProperty(int SKOSProperty) {
        this.SKOSProperty = SKOSProperty;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getIdLang() {
        return idLang;
    }

    public void setIdLang(String idLang) {
        this.idLang = idLang;
    }
 
    
}
