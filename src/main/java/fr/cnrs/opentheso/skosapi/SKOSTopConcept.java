package fr.cnrs.opentheso.skosapi;

public class SKOSTopConcept {
	private String uri;
	
	public SKOSTopConcept(String uri){
		this.uri = uri;
	}
	
	public String getTopConcept() {
		return uri;
	}
	
	public String toString(){
		String xmlTag = new String();
		xmlTag = "        <skos:hasTopConcept>\n";
		xmlTag += "            <skos:Concept rdf:about=\""+uri+"\"/>\n";
		xmlTag +="        </skos:hasTopConcept>\n";
		return xmlTag;
	}
	
}