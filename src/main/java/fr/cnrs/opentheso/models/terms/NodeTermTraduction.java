package fr.cnrs.opentheso.models.terms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeTermTraduction implements Serializable {

    private String lang;
    private String nomLang;
    private String lexicalValue;
    private String codePays;

}
