package fr.cnrs.opentheso.models.terms;

import lombok.Data;
import java.text.Normalizer;


@Data
public class NodeRT implements Comparable {

    private String title;
    private String idConcept;
    private String status;
    private String role;
    
    @Override
    public int compareTo(Object o) {
        if(title == null) {
            title = "";
        }
        if(((NodeRT)o).title == null) {
            ((NodeRT)o).title = "";
        }
        String str1, str2;
        str1 = Normalizer.normalize(this.title, Normalizer.Form.NFD);
        str1 = str1.replaceAll("[^\\p{ASCII}]", "");
        str2 = Normalizer.normalize(((NodeRT)o).title, Normalizer.Form.NFD);
        str2 = str2.replaceAll("[^\\p{ASCII}]", "");
        return str1.toUpperCase().compareTo(str2.toUpperCase());
    }
}
