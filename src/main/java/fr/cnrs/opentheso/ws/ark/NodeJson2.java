package fr.cnrs.opentheso.ws.ark;

import fr.cnrs.opentheso.models.nodes.DcElement;
import fr.cnrs.opentheso.models.concept.Qualifier;
import java.util.List;
import fr.cnrs.opentheso.utils.DateUtils;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import lombok.Data;


@Data
public final class NodeJson2 {

    private String token;
    private String ark;
    private String naan;    
    private String type; // prefix
    private String urlTarget;
    private String title;
    private String creator;
    private boolean useHandle;
    private String modificationDate;
    private List<DcElement> dcElements;
    private List<Qualifier> qualifiers;
    
    public NodeJson2() {
    }
    
    /**
     * Permet de retourner les valeurs déifnies sous le format Json String
     * @return 
     */
    public String getJsonString() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("token", token);
        job.add("ark", ark);
        job.add("naan", naan);
        job.add("type", type);
        job.add("urlTarget", urlTarget);
        job.add("title", title);
        job.add("creator", creator);
        job.add("useHandle", useHandle);
        job.add("modificationDate", new DateUtils().getDate());
        
        job.add("dcElements", getStringFromDcElements());        
        job.add("qualifiers", getStringFromQualifiers()); 

        return job.build().toString();
    }
    
    /**
     * Pour la mise à jour des URI uniquement pour Ark
     * @return 
     */
    public String getJsonUriString() {
        String arkString = "{"
                + "\"token\":\""+ token + "\","
                + "\"ark\":\""+ ark + "\","
                + "\"naan\":\"" + naan + "\","
                + "\"urlTarget\":\""+ urlTarget + "\","
                + "\"useHandle\":" + useHandle
                + "}";
        return arkString;
    }    
    
    private JsonArray getStringFromDcElements(){
       JsonArrayBuilder jsonArrayBuilderDC = Json.createArrayBuilder();
        if(dcElements == null) {
            return jsonArrayBuilderDC.build();
        }
        for (DcElement dcElement : dcElements) {
            JsonObjectBuilder jobLine = Json.createObjectBuilder();
            jobLine.add("name", dcElement.getName());
            jobLine.add("value", dcElement.getValue());
            jobLine.add("language", dcElement.getLanguage());
            jsonArrayBuilderDC.add(jobLine.build());
        }
        return jsonArrayBuilderDC.build();
    }
    
    private JsonArray getStringFromQualifiers(){
       JsonArrayBuilder jsonArrayBuilderQual = Json.createArrayBuilder();
        if(qualifiers == null) {
            return jsonArrayBuilderQual.build();
        }
        for (Qualifier qualifier : qualifiers) {
            JsonObjectBuilder jobLine = Json.createObjectBuilder();
            jobLine.add("qualifier", qualifier.getQualifier());
            jobLine.add("url_target", qualifier.getUrlTarget());
            jsonArrayBuilderQual.add(jobLine.build());
        }
        return jsonArrayBuilderQual.build();        
    }
}
