/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.cnrs.opentheso.bdd.helper.nodes.thesaurus;

/**
 *
 * @author miled.rousset
 */

public class NodeThesaurusId {
	private String idThesaurus;
	

	public NodeThesaurusId() {
		super();
	}

	public NodeThesaurusId(String idThesaurus) {
		super();
		this.idThesaurus = idThesaurus;
	}
	
	
	public String getIdThesaurus() {
		return idThesaurus;
	}
	public void setIdThesaurus(String idThesaurus) {
		this.idThesaurus = idThesaurus;
	}
}