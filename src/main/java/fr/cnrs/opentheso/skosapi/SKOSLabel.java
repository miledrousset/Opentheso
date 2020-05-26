package fr.cnrs.opentheso.skosapi;

/**
 * 
 * @author Djamel Ferhod
 *
 */
public class SKOSLabel implements SKOSProperty{
	private String label;
	private String language;
	private int property;
	
	public SKOSLabel(String lab, String lang, int prop) throws Exception{
		if(40 <=prop && prop <= 42){
			this.label = lab;
			this.language = lang;
			this.property = prop;
		}
		else{
			throw new Exception("Erreur : cette propriété n'est pas valide pour le label"+lab);
		}
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public int getProperty() {
		return property;
	}
	
	public String toString(){
		String propertyName = new String();
		String xmlTag;
		switch(property){
			case SKOSProperty.prefLabel :
				propertyName = "prefLabel";
				break;
			case SKOSProperty.altLabel :
				propertyName = "altLabel";
				break;
			case SKOSProperty.hiddenLabel :
				propertyName = "hiddenLabel";
				break;
			case SKOSProperty.inScheme :
				propertyName = "inScheme";
				break;
			default:
				break;
		}
		xmlTag = "<skos:"+propertyName+" xml:lang=\""+language+"\">"+label+"</skos:"+propertyName+">\n";
		return xmlTag;
	}
}
