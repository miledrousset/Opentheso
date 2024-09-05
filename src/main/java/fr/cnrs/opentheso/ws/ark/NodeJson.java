package fr.cnrs.opentheso.ws.ark;

import fr.cnrs.opentheso.models.nodes.DcElement;
import java.util.List;
import fr.cnrs.opentheso.utils.DateUtils;
import lombok.Data;


@Data
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
    private List<DcElement> dcElements;
    private int userArkId = 1;
    private String owner= null;
    private List<String> qualifiers;
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
    
    /**
     * Permet de retourner les valeurs déifnies sous le format Json String
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

}
