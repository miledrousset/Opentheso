package fr.cnrs.opentheso.bdd.datas;

public class SourceLanguage {

	private int id_thesaurus;
	private String id_langue;
	
	
	public SourceLanguage(int idThesaurus, String idLangue) {
		id_thesaurus = idThesaurus;
		id_langue = idLangue;
	}


	public SourceLanguage() {
	}


	public int getId_thesaurus() {
		return id_thesaurus;
	}


	public void setId_thesaurus(int idThesaurus) {
		id_thesaurus = idThesaurus;
	}


	public String getId_langue() {
		return id_langue;
	}


	public void setId_langue(String idLangue) {
		id_langue = idLangue;
	}
	
	
	
}
