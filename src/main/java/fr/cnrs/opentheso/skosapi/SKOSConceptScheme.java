package fr.cnrs.opentheso.skosapi;

import java.util.ArrayList;
import java.util.Iterator;

public class SKOSConceptScheme{
	private String uri;
	private ArrayList<SKOSTopConcept> topConceptsList;
	
	public SKOSConceptScheme(String uri){
		this.uri = uri;
		this.topConceptsList = new ArrayList<SKOSTopConcept>();
	}
	
	public String getUri() {
		return uri;
	}
	
	public ArrayList<SKOSTopConcept> getTopConceptsList() {
		return topConceptsList;
	}
	
	public void addTopConcept(String uri){
		SKOSTopConcept tC = new SKOSTopConcept(uri);
		topConceptsList.add(tC);
	}
	
	public  String openingBlock(String uri){
		String xml = new String();
		xml = "<skos:ConceptScheme rdf:about=\""+uri+"\">\n";
		return xml;
	}
	
	public String closingBlock(){
		String xml = new String();
		xml += "    </skos:ConceptScheme>\n";
		return xml;
	}
	
	public String toString(){
		String xml = new String();
		Iterator<SKOSTopConcept> it = topConceptsList.iterator();
		xml = "<skos:ConceptScheme rdf:about=\""+uri+"\">\n";
		while(it.hasNext()){
			//xml += "        <skos:hasTopConcept>\n";
			xml += "        "+it.next().toString();
			//xml += "        </skos:hasTopConcept>\n";
		}
		xml += "    </skos:ConceptScheme>\n";
		return xml;
	}
}
