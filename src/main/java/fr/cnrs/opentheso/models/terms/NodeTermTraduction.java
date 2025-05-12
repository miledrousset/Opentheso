package fr.cnrs.opentheso.models.terms;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@NoArgsConstructor
public class NodeTermTraduction implements Serializable {

    private String lang;
    private String nomLang;
    private String lexicalValue;
    private String codePays;


    public NodeTermTraduction(String lexicalValue, String lang) {
        this.lexicalValue = lexicalValue;
        this.lang = lang;
    }

    public NodeTermTraduction(String idLang, String lexicalValue, String codePays, String nomLang) {
        this.lang = idLang;
        this.lexicalValue = lexicalValue;
        this.codePays = codePays;
        this.nomLang = nomLang;
    }

}
