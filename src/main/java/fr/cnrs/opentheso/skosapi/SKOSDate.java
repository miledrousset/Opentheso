package fr.cnrs.opentheso.skosapi;

public class SKOSDate implements SKOSProperty{

	private String sDate;
	private int property;
	
	public SKOSDate(String date, int prop) throws Exception{
		if(50 <=prop && prop <= 52){
			this.sDate = date;
			this.property = prop;
		}
		else{
			throw new Exception("Erreur : cette propriété n'est pas valide pour la date "+date);
		}
	}
	
	public String getDate() {
		return sDate;
	}
	
	public int getProperty() {
		return property;
	}
	
	public String toString(){
		String xmlTag = new String();
		switch(property){
			case SKOSProperty.created :
				xmlTag = "<dcterms:created>"+sDate+"</dcterms:created>\n";
				break;
			case SKOSProperty.modified :
				xmlTag = "<dcterms:modified>"+sDate+"</dcterms:modified>\n";
				break;
			default:
				break;
		}
		
		return xmlTag;
	}
}
