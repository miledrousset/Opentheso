/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author miled.rousset
 */
public class NodeImage {

    private String idConcept;
    private String idThesaurus;
    private String imageName;
    private String copyRight;
    private String uri;
    
    private String oldUri; // pour la modification d'une Uri
    
    public NodeImage() {
    }

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getCopyRight() {
        return copyRight;
    }

    public void setCopyRight(String copyRight) {
        this.copyRight = copyRight;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getOldUri() {
        return oldUri;
    }

    public void setOldUri(String oldUri) {
        this.oldUri = oldUri;
    }
    
}
