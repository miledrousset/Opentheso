package fr.cnrs.opentheso.models.skosapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoafImage {

    private String uri;
    private String identifier;
    private String imageName;
    private String creator;
    private String copyRight;

}
