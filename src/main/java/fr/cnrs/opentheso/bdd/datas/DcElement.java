package fr.cnrs.opentheso.bdd.datas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DcElement {

    private int id;
    protected String name;
    protected String value;
    protected String language;
    private String type;

    public DcElement(String name, String value, String language, String type) {
        this.id = -1;
        this.language = language;
        this.name = name;
        this.value = value;
        this.type = type;
    }
}
