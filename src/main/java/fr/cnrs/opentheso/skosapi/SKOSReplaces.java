package fr.cnrs.opentheso.skosapi;

/**
 * 
 * @author Miled Rousset
 *
 */
public class SKOSReplaces implements SKOSProperty{
	private String targetUri;
	private int property;
	
	public SKOSReplaces(String uri, int prop) throws Exception{
		if(95 <=prop && prop <= 96){
			this.targetUri = uri;
			this.property = prop;
		}
		else{
			throw new Exception("Erreur : cette propriété n'est pas valide pour la relation avec le concept d'URI"+uri);
		}
	}
	
	public String getTargetUri() {
		return targetUri;
	}
	
	public int getProperty() {
		return property;
	}
	
	public String toString(){
		String xmlTag = new String();
		switch(property){
			case SKOSProperty.isReplacedBy :
				xmlTag = "<dcterms:isReplacedBy>rdf:resource=\"" + targetUri + "\"</dcterms:isReplacedBy>\n";
				break;
			case SKOSProperty.replaces :
				xmlTag = "<dcterms:replaces>rdf:resource=\"" + targetUri + "\"</dcterms:replaces>\n";
				break;
			default:
				break;
		}
		return xmlTag;
	}
}
