package fr.cnrs.opentheso.models.alignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlignementSource {

    private String source;
    
    // pour définir le mode de filtrage ex : Opentheso, Wikidata, Gemet ....
    private String source_filter = "";
    private String requete;
    private String typeRequete;
    private String alignement_format;
    private int id;
    private String description;
    // deprecated
    private boolean isGps;

    public String getSource_filter() {
        if(StringUtils.isEmpty(source_filter)) return "";
        return source_filter;
    }
  
}
