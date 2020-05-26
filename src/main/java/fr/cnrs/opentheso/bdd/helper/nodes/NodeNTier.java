
package fr.cnrs.opentheso.bdd.helper.nodes;

import java.text.Normalizer;



public class NodeNTier implements Comparable {
    private String title;
    private String idConcept;
    private String status;
    private String dateCre;
    
    public NodeNTier() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    @Override
    public int compareTo(Object o) {
        if(title == null) {
            title = "";
        }
        if(((NodeNTier)o).title == null) {
            ((NodeNTier)o).title = "";
        }
        String str1, str2;
        str1 = Normalizer.normalize(this.title, Normalizer.Form.NFD);
        str1 = str1.replaceAll("[^\\p{ASCII}]", "");
        str2 = Normalizer.normalize(((NodeNTier)o).title, Normalizer.Form.NFD);
        str2 = str2.replaceAll("[^\\p{ASCII}]", "");
        return str1.toUpperCase().compareTo(str2.toUpperCase());
        
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateCre() {
        return dateCre;
    }

    public void setDateCre(String dateCre) {
        this.dateCre = dateCre;
    }
    
    
}
