package fr.cnrs.opentheso.skosapi;
/**
 *
 * @author miled.rousset
 */
public class SKOSdc implements SKOSProperty {

    private String sIdentifier;
    private int property;

    public SKOSdc(String identifier, int prop) throws Exception {
        if (prop == 55) {
            this.sIdentifier = identifier;
            this.property = prop;
        } else {
            throw new Exception("Erreur : cette propriété n'est pas valide pour l'identifiant DC " + identifier);
        }
    }

    public String getIdentifier() {
        return sIdentifier;
    }

    public int getProperty() {
        return property;
    }

    public String toString() {
        String xmlTag = new String();
        switch (property) {
            case SKOSProperty.identifier:
                xmlTag = "<dcterms:identifier>" + identifier + "</dcterms:identifier>\n";
                break;
            default:
                break;
        }

        return xmlTag;
    }
}
