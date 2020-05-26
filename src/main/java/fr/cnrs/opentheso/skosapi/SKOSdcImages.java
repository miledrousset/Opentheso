package fr.cnrs.opentheso.skosapi;
/**
 *
 * @author miled.rousset
 */
public class SKOSdcImages implements SKOSProperty {

    private String imageUri;

    public SKOSdcImages(String uri) throws Exception {
        this.imageUri = uri;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String toString() {
        return "<dcterms:image>" + imageUri + "</dcterms:image>\n";
    }
}
