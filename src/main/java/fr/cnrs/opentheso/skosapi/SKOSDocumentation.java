package fr.cnrs.opentheso.skosapi;

public class SKOSDocumentation implements SKOSProperty{
	private String text;
	private String language;
	private int property;
	
	public SKOSDocumentation(String text, String lang, int prop) throws Exception{
		if(30 <=prop && prop <= 36){
			this.text = text;
			this.language = lang;
			this.property = prop;
		}
		else{
			throw new Exception("Erreur : cette propriété n'est pas valide pour la documentation " + text);
		}
	}
	
	public String getText() {
		return text;
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
			case SKOSProperty.DEFINITION :
				propertyName = "definition";
				break;
			case SKOSProperty.SCOPE_NOTE :
				propertyName = "scopeNote";
				break;
			case SKOSProperty.EXAMPLE :
				propertyName = "example";
				break;
			case SKOSProperty.HISTORY_NOTE :
				propertyName = "historyNote";
				break;
			case SKOSProperty.EDITORIAL_NOTE :
				propertyName = "editorialNote";
				break;
			case SKOSProperty.CHANGE_NOTE :
				propertyName = "changeNote";
				break;
			default:
				break;
		}
		xmlTag = "<skos:"+propertyName+" xml:lang=\""+language+"\">"+text+"</skos:"+propertyName+">\n";
		return xmlTag;
	}
}
