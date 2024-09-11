package fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    private List<Element> terme;

    private String thesoId;

    private String collectionId;

    private List<Element> definition;

    private List<Element> note;

    private String source;

    private List<Element> synonymes;

    private String comment;
}
