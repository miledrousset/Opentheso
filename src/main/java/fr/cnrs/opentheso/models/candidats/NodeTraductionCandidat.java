package fr.cnrs.opentheso.models.candidats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeTraductionCandidat {

    private String idLang;
    private String title;
    private String user;
    private int useId;
       
}
