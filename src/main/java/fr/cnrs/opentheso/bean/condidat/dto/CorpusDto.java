package fr.cnrs.opentheso.bean.condidat.dto;

import java.io.Serializable;


public class CorpusDto implements Serializable {
    
    private String value;
    
    private String url;

    public CorpusDto() {
        value = "";
        url = "";
    }

    public CorpusDto(String value, String url) {
        this.value = value;
        this.url = url;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
