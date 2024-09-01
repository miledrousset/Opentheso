package fr.cnrs.opentheso.bdd.datas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Qualifier {

    private String qualifier;
    private String urlTarget;

}
