package fr.cnrs.opentheso.models.alignment;

import lombok.Data;

import java.util.List;


@Data
public class ResultatAlignement {

    private String title;
    private String url;
    private List<String> broarder;
    private List<String> narrowers;
    private String related;
    private String note;
    private String hierarchy;
    private List<String> terms;

}
