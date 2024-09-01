package fr.cnrs.opentheso.models.skosapi;

import lombok.Data;

/**
 *
 * @author Djamel Ferhod
 *
 */
@Data
public class SKOSRelation implements SKOSProperty {

    private String localIdentifier;
    private String targetUri;
    private int property;

    public SKOSRelation(String identifier, String uri, int prop) throws Exception {
        if (-1 <= prop && prop <= 22 ) {
            this.localIdentifier = identifier;
            this.targetUri = uri;
            this.property = prop;
        } else {
            if (100 <= prop && prop <= 105 ){
                this.localIdentifier = identifier;                
                this.targetUri = uri;
                this.property = prop;                
            } else
            throw new Exception("Erreur : cette propriété n'est pas valide pour la relation avec le concept d'URI" + uri);
        }
    }

    public String toString() {
        String propertyName = new String();
        String xmlTag;
        switch (property) {
            case SKOSProperty.RELATED:
                propertyName = "related";
                break;
            case SKOSProperty.NARROWER:
                propertyName = "narrower";
                break;
            case SKOSProperty.BROADER:
                propertyName = "broader";
                break;
            case SKOSProperty.INSCHEME:
                propertyName = "inScheme";
                break;
            default:
                break;
        }
        xmlTag = "<skos:" + propertyName + " rdf:resource=\"" + targetUri + "\"/>\n";
        return xmlTag;
    }
}
