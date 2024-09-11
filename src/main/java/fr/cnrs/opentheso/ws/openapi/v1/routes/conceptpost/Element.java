package fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Element {
    private String value;
    private String lang;
}
