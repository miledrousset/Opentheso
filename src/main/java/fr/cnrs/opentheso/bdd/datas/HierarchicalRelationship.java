package fr.cnrs.opentheso.bdd.datas;

public class HierarchicalRelationship {
	private String idConcept1;
	private String idConcept2;
	private String idThesaurus;
	private String role;
	public HierarchicalRelationship(String idConcept1, String idConcept2,
			String idThesaurus, String role) {
		super();
		this.idConcept1 = idConcept1;
		this.idConcept2 = idConcept2;
		this.idThesaurus = idThesaurus;
		this.role = role;
	}
	public HierarchicalRelationship() {
		// TODO Auto-generated constructor stub
	}
	public String getIdConcept1() {
		return idConcept1;
	}
	public void setIdConcept1(String idConcept1) {
		this.idConcept1 = idConcept1;
	}
	public String getIdConcept2() {
		return idConcept2;
	}
	public void setIdConcept2(String idConcept2) {
		this.idConcept2 = idConcept2;
	}
	public String getIdThesaurus() {
		return idThesaurus;
	}
	public void setIdThesaurus(String idThesaurus) {
		this.idThesaurus = idThesaurus;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	
	

}
