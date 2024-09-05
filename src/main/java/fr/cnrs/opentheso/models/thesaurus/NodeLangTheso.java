package fr.cnrs.opentheso.models.thesaurus;

import lombok.Data;


@Data
public class NodeLangTheso {

    private String id;
    private String code;
    private String value;
    private String labelTheso;
    private String codeFlag;


    public String getValue() {
        if(value == null || value.isEmpty()) {
            return value;
        }

        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

}
