package fr.cnrs.opentheso.ws.ark;

import fr.cnrs.opentheso.bdd.datas.DcElement;
import java.util.ArrayList;
import fr.cnrs.opentheso.utils.DateUtils;
/*
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
*/

public final class NodeJson {

    private String urlTarget;
    private String title;
    private String creator;
    private String handle_prefix;
    private String handle =""; //idHandle
    private boolean handle_stored = false;
    private String date;
    private String type = "Service";
    private String language = "fr";
    private boolean linkup = true;
    private String ark =""; // idArk
    private String name = "";
    private String qualifier= "";
    private String modificationDate;
    private boolean saved = false;
    private String naan;
    private boolean redirect =true;
    private ArrayList <DcElement> dcElements;
    private int userArkId = 1;
    private String owner= null;
    private ArrayList<String> qualifiers;
    private String format= "";
    private String identifier= "";
    private String description= "";
    private String source= "";
    private String subject= "";
    private String rights= "";
    private String publisher= "";
    private String relation= "";
    private String coverage= "";
    private String contributor= "";       
    
    public NodeJson() {
    }
    
    /**
     * Permet de retourner les valeurs déifnies sous le format Json String
     * @return 
     */
    public String getJsonString() {
        String arkString = "{\"urlTarget\":\""+ urlTarget + "\","
                + "\"title\":\" "+ title + "\","
                
                + "\"creator\":\"" + creator + "\","
                + "\"handle_prefix\":\"" + handle_prefix + "\","
                + "\"handle\":\""+ handle +"\","
                + "\"handle_stored\":"+ handle_stored +","
                + "\"date\":\"" + new DateUtils().getDate() +"\","
                + "\"type\":\""+  type + "\","
                + "\"language\":\""+ language + "\","
                + "\"linkup\":" + linkup + ","
                + "\"ark\":\""+ ark + "\","
                + "\"name\":\""+ name + "\","
                + "\"qualifier\":\""+ qualifier + "\","
                + "\"modificationDate\":\""+ new DateUtils().getDate() +"\","
                + "\"saved\":" + saved +","
                + "\"naan\":\"" + naan + "\","
                + "\"redirect\":" + true + ","
                + "\"dcElements\":[],"
                + "\"userArkId\":" + userArkId + ","
                + "\"owner\":" + owner + ","
                + "\"qualifiers\":[],"
                + "\"format\":\""+ format + "\","
                + "\"identifier\":\""+ identifier + "\","
                + "\"description\":\""+ description + "\","
                + "\"source\":\""+ source + "\","
                + "\"subject\":\""+ subject + "\","
                + "\"rights\":\""+ rights + "\","
                + "\"publisher\":\""+ publisher + "\","
                + "\"relation\":\""+ relation + "\","
                + "\"coverage\":\""+ coverage + "\","
                + "\"contributor\":\""+ contributor + "\"}";
        return arkString;
    }
    
    
    
/*    public String getJsonData(String urlResource) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
    
        builder.add("index", "1");
        builder.add("type", "URL");

        // pour le tableau 
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("format", "string");
        job.add("value", urlResource);
        jsonArrayBuilder.add(job.build());
        
        builder.add("data", jsonArrayBuilder.build().toString());
        
        builder.add("ttl", "86400");
        builder.add("permissions", "1110");
        return builder.build().toString();
    }*/

    public String getUrlTarget() {
        return urlTarget;
    }

    public void setUrlTarget(String urlTarget) {
        this.urlTarget = urlTarget;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getHandle_prefix() {
        return handle_prefix;
    }

    public void setHandle_prefix(String handle_prefix) {
        this.handle_prefix = handle_prefix;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public boolean isHandle_stored() {
        return handle_stored;
    }

    public void setHandle_stored(boolean handle_stored) {
        this.handle_stored = handle_stored;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isLinkup() {
        return linkup;
    }

    public void setLinkup(boolean linkup) {
        this.linkup = linkup;
    }

    public String getArk() {
        return ark;
    }

    public void setArk(String ark) {
        this.ark = ark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public String getNaan() {
        return naan;
    }

    public void setNaan(String naan) {
        this.naan = naan;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public ArrayList<DcElement> getDcElements() {
        return dcElements;
    }

    public void setDcElements(ArrayList<DcElement> dcElements) {
        this.dcElements = dcElements;
    }

    public int getUserArkId() {
        return userArkId;
    }

    public void setUserArkId(int userArkId) {
        this.userArkId = userArkId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<String> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(ArrayList<String> qualifiers) {
        this.qualifiers = qualifiers;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
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

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getContributor() {
        return contributor;
    }

    public void setContributor(String contributor) {
        this.contributor = contributor;
    }

}
