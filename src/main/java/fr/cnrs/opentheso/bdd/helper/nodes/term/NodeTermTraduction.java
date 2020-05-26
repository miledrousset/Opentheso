package fr.cnrs.opentheso.bdd.helper.nodes.term;

public class NodeTermTraduction {

	private String lang;
	private String lexicalValue;
	
	public NodeTermTraduction() {
		super();
	}

	
	public NodeTermTraduction(String lang, String lexicalValue) {
		super();
		this.lang = lang;
		this.lexicalValue = lexicalValue;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getLexicalValue() {
		return lexicalValue;
	}

	public void setLexicalValue(String lexicalValue) {
		this.lexicalValue = lexicalValue;
	}
	
	
}
