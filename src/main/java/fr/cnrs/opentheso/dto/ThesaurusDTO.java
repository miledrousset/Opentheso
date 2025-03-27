package fr.cnrs.opentheso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThesaurusDTO {
    private String idThesaurus;
    private String idArk;
    private Date created;
    private Date modified;
    private Boolean privateTheso;

    private String title;
    private String lang;
    private String contributor;
    private String coverage;
    private String creator;
    private String description;
    private String format;
    private String publisher;
    private String relation;
    private String rights;
    private String source;
    private String subject;
    private String type;
}
