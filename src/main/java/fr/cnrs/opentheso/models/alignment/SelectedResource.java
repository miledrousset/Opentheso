package fr.cnrs.opentheso.models.alignment;

import lombok.Data;


@Data
public class SelectedResource {

    private String idLang;
    private String gettedValue;
    private boolean selected = true;
    private String localValue;
    private boolean isEqual;
    
}
