package fr.cnrs.opentheso.bdd.datas;

import java.sql.Date;
import java.util.ArrayList;

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
 
    private ArrayList <DcElement> dcElement;

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

    public String getId_thesaurus() {
        return id_thesaurus;
    }

    public void setId_thesaurus(String id_thesaurus) {
        this.id_thesaurus = id_thesaurus;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId_ark() {
        return id_ark;
    }

    public void setId_ark(String id_ark) {
        this.id_ark = id_ark;
    }

    public ArrayList<DcElement> getDcElement() {
        return dcElement;
    }

    public void setDcElement(ArrayList<DcElement> dcElement) {
        this.dcElement = dcElement;
    }
    
    public void addDcElement(DcElement dcElement) {
        this.dcElement.add(dcElement);
    }
    /**
     * #JM
     * @return 
     */
    public boolean isPrivateTheso() {
        return privateTheso;
    }
    /**
     * #JM
     * @param privateTheso 
     */
    public void setPrivateTheso(boolean privateTheso) {
        this.privateTheso = privateTheso;
    }
    


}
