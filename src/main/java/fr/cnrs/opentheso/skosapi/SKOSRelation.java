package fr.cnrs.opentheso.skosapi;

/**
 *
 * @author Djamel Ferhod
 *
 */
public class SKOSRelation implements SKOSProperty {

    private String targetUri;
    private int property;

    public SKOSRelation(String uri, int prop) throws Exception {
        if (-1 <= prop && prop <= 22 ) {
            this.targetUri = uri;
            this.property = prop;
        } else {
            if (100 <= prop && prop <= 105 ){
                this.targetUri = uri;
                this.property = prop;                
            } else
            throw new Exception("Erreur : cette propriété n'est pas valide pour la relation avec le concept d'URI" + uri);
        }
    }

    public String getTargetUri() {
        return targetUri;
    }

    public int getProperty() {
        return property;
    }

    public String toString() {
        String propertyName = new String();
        String xmlTag;
        switch (property) {
            case SKOSProperty.related:
                propertyName = "related";
                break;
            case SKOSProperty.narrower:
                propertyName = "narrower";
                break;
            case SKOSProperty.broader:
                propertyName = "broader";
                break;
            case SKOSProperty.inScheme:
                propertyName = "inScheme";
                break;
            default:
                break;
        }
        xmlTag = "<skos:" + propertyName + " rdf:resource=\"" + targetUri + "\"/>\n";
        return xmlTag;
    }
}
