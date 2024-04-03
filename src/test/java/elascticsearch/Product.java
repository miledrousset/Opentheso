
package elascticsearch;


/**
 *
 * @author miledrousset
 */


public class Product {
    private String id;
    private String prefLabel;
    private String altLabel;
    private String lang;

    public Product(String id, String prefLabel, String altLabel, String lang) {
        this.id = id;
        this.prefLabel = prefLabel;
        this.altLabel = altLabel;
        this.lang = lang;
    }

    public Product() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(String altLabel) {
        this.altLabel = altLabel;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }


    
}
