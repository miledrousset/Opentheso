package fr.cnrs.opentheso.ws.ark;

import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.datas.Qualifier;
import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.tools.FileUtilities;

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
    private ArrayList <DcElement> dcElements;    
    private ArrayList<Qualifier> qualifiers;    
    
    public NodeJson2() {
    }
    
    /**
     * Permet de retourner les valeurs déifnies sous le format Json String
     * @return 
     */
    public String getJsonString() {
        String arkString = "{"
                + "\"token\":\""+ token + "\","
                + "\"ark\":\""+ ark + "\","
                + "\"naan\":\"" + naan + "\","
                + "\"type\":\""+  type + "\","                
                + "\"urlTarget\":\""+ urlTarget + "\","
                + "\"title\":\" "+ title + "\","
                + "\"creator\":\"" + creator + "\","
                + "\"useHandle\":" + useHandle + ","                
                + "\"modificationDate\":\""+ new FileUtilities().getDate() + "\","
                + "\"dcElments\":" + getStringFromDcElements() + ","
                + "\"qualifiers\":" + getStringFromQualifiers()                
                + "}";

        return arkString;
    }
    
    private String getStringFromDcElements(){
        if(dcElements == null) return "[]";
        String dcElementString = "[";
        boolean first = true;
        for (DcElement dcElement : dcElements) {
            if(!first) {
                dcElementString += ",";
            }             
            dcElementString += "{";
            dcElementString += "\"name\":\""+ dcElement.getName() + "\",";
            dcElementString += "\"value\":\""+ dcElement.getValue()+ "\","; 
            dcElementString += "\"language\":\""+ dcElement.getLanguage()+ "\"";  
            dcElementString += "}";
            first = false;  
        }
        dcElementString += "]";
        return dcElementString;
    }
    
    private String getStringFromQualifiers(){
        if(qualifiers == null) return "[]";
        String qualifierString = "[";
        boolean first = true;
        for (Qualifier qualifier : qualifiers) {
            if(!first) {
                qualifierString += ",";
            }             
            qualifierString += "{";
            qualifierString += "\"qualifier\":\""+ qualifier.getQualifier() + "\",";
            qualifierString += "\"url_target\":\""+ qualifier.getUrlTarget() + "\""; 
            qualifierString += "}";
            first = false;  
        }
        qualifierString += "]";
        return qualifierString;
    }    
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getArk() {
        return ark;
    }

    public void setArk(String ark) {
        this.ark = ark;
    }

    public String getNaan() {
        return naan;
    }

    public void setNaan(String naan) {
        this.naan = naan;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public boolean isUseHandle() {
        return useHandle;
    }

    public void setUseHandle(boolean useHandle) {
        this.useHandle = useHandle;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public ArrayList<DcElement> getDcElements() {
        return dcElements;
    }

    public void setDcElements(ArrayList<DcElement> dcElements) {
        this.dcElements = dcElements;
    }

    public ArrayList<Qualifier> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(ArrayList<Qualifier> qualifiers) {
        this.qualifiers = qualifiers;
    }

}
