package fr.cnrs.opentheso.bdd.helper.nodes.term;

import java.util.ArrayList;
import java.util.Date;


public class NodeTerm {
	private String idTerm;
	private String idThesaurus;
	private String idConcept;
	private Date created;
	private Date modified;

	private String source;
	private String status;
        
	private ArrayList<NodeTermTraduction> nodeTermTraduction;



	public NodeTerm(String idTerm, String idThesaurus, String idConcept,
			Date created, Date modified, String source, String status,
			ArrayList<NodeTermTraduction> nodeTermTraduction) {
		super();
		this.idTerm = idTerm;
		this.idThesaurus = idThesaurus;
		this.idConcept = idConcept;
		this.created = created;
		this.modified = modified;
		this.source = source;
		this.status = status;
		this.nodeTermTraduction = nodeTermTraduction;
	}

	public NodeTerm() {
		// TODO Auto-generated constructor stub
	}
	
	public String getIdTerm() {
		return idTerm;
	}

	public void setIdTerm(String idTerm) {
		this.idTerm = idTerm;
	}

	public String getIdThesaurus() {
		return idThesaurus;
	}

	public void setIdThesaurus(String idThesaurus) {
		this.idThesaurus = idThesaurus;
	}

	public String getIdConcept() {
		return idConcept;
	}

	public void setIdConcept(String idConcept) {
		this.idConcept = idConcept;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ArrayList<NodeTermTraduction> getNodeTermTraduction() {
		return nodeTermTraduction;
	}

	public void setNodeTermTraduction(
			ArrayList<NodeTermTraduction> nodeTermTraduction) {
		this.nodeTermTraduction = nodeTermTraduction;
	}

	
}
