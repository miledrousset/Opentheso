package fr.cnrs.opentheso.models.exports.privatesdatas;

import lombok.Data;

/**
 *
 * @author antonio.perez
 */
@Data
public class LineOfData {
    
    private String colomne;
    private String value;

    public void setValue(String value) {
        if(value == null){
            this.value = "";
            return;
        }
        if(value.isEmpty()){
            this.value = "";
            return;
        }
        if("null".equals(value)){
            this.value = "";
            return;
        }
        this.value = value.trim();
    }
}
