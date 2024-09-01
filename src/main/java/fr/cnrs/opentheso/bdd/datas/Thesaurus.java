package fr.cnrs.opentheso.bdd.datas;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
public class Thesaurus {

    private String id_thesaurus;
    private String id_ark;
    private String contributor = "";
    private String coverage = "";
    private String creator = "";
    private Date created;
    private Date modified;
    private String description = "";
    private String format = "";
    private String language;
    private String publisher = "";
    private String relation = "";
    private String rights = "";
    private String source = "";
    private String subject = "";
    private String title = "";
    private String type = "";
    private boolean privateTheso;
 
    private List <DcElement> dcElement;

    /**
     *
     */
    public Thesaurus() {
        dcElement = new ArrayList<>();
    }
    
    /**
     * @param contributor
     * @param coverage
     * @param creator
     * @param description
     * @param format
     * @param id_langue
     * @param publisher
     * @param relation
     * @param rights
     * @param source
     * @param subject
     * @param title
     * @param type
     */
    public Thesaurus(String contributor, String coverage, String creator, String description, String format, String id_langue, String publisher, String relation, String rights, String source, String subject, String title, String type) {
        this.contributor = contributor;
        this.coverage = coverage;
        this.creator = creator;
        this.description = description;
        this.format = format;
        this.language = id_langue;
        this.publisher = publisher;
        this.relation = relation;
        this.rights = rights;
        this.source = source;
        this.subject = subject;
        this.title = title;
        this.type = type;
    }

    public void addDcElement(DcElement dcElement) {
        this.dcElement.add(dcElement);
    }
}
