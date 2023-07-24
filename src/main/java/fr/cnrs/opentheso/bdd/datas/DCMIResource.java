package fr.cnrs.opentheso.bdd.datas;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
/**
 *
 * @author miledrousset
 */

@Data
public class DCMIResource {
    private String title;
    private String creator;
    private String subject;
    private String description;
    private String publisher;
    private String contributor;
    private String date;
    private String type;
    private String format;
    private String identifier;
    private String source;
    private String language;
    private String relation;
    private String coverage;
    private String rights;

    // Constructeur par défaut
    public DCMIResource() {
    }
    public static final String TITLE = "title";
    public static final String CREATOR = "creator";
    public static final String CONTRIBUTOR = "contributor";    
    public static final String SUBJECT = "subject";    

    public static final String DESCRIPTION = "description";
    public static final String PUBLISHER = "publisher";
    public static final String DATE = "date";
    public static final String TYPE = "type";
    public static final String FORMAT = "format";
    public static final String IDENTIFIER = "identifier";
    public static final String SOURCE = "source";
    public static final String LANGUAGE = "language";
    public static final String RELATION = "relation";
    public static final String COVERAGE = "coverage";
    public static final String RIGHTS = "rights";     
    
    
    public List<String> getAllResources(){
        List allResources = new ArrayList();
        allResources.add("title");
        allResources.add("creator");
        allResources.add("subject");     
        allResources.add("description");
        allResources.add("publisher");
        allResources.add("contributor"); 
        allResources.add("date");
        allResources.add("type");
        allResources.add("format"); 
        allResources.add("identifier");
        allResources.add("language");
        allResources.add("relation");      
        allResources.add("coverage");
        allResources.add("rights");         
        return allResources;
    }
    
}
