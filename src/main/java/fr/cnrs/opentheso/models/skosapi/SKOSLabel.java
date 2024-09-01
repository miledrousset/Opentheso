package fr.cnrs.opentheso.models.skosapi;

import lombok.Data;

/**
 * 
 * @author Djamel Ferhod
 *
 */
@Data
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
	
	public String toString(){
		String propertyName = new String();
		String xmlTag;
		switch(property){
			case PREF_LABEL:
				propertyName = "prefLabel";
				break;
			case ALT_LABEL:
				propertyName = "altLabel";
				break;
			case HIDDEN_LABEL:
				propertyName = "hiddenLabel";
				break;
			case INSCHEME:
				propertyName = "inScheme";
				break;
			default:
				break;
		}
		xmlTag = "<skos:"+propertyName+" xml:lang=\""+language+"\">"+label+"</skos:"+propertyName+">\n";
		return xmlTag;
	}
}
