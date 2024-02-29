package fr.cnrs.opentheso.skosapi;

/**
 *
 * @author Miled Rousset
 *
 */
public class FoafImage {

    private String uri;
    private String identifier;
    private String imageName;
    private String creator;
    private String copyRight;

    /**
     *
     */
    public FoafImage() {

    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }



}
