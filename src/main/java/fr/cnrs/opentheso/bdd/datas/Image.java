package fr.cnrs.opentheso.bdd.datas;

public class Image {
	private int idTerme;
	private int idThesaurus;
	private String copyright;
	private int id_image;
	

	/**
	 */
	public Image() {
	}
	
	
	public Image(int idTerme, int idThesaurus, String copyright, int idImage) {
		super();
		this.idTerme = idTerme;
		this.idThesaurus = idThesaurus;
		this.copyright = copyright;
		id_image = idImage;
	}


	public int getIdTerme() {
		return idTerme;
	}

	public void setIdTerme(int idTerme) {
		this.idTerme = idTerme;
	}

	public int getIdThesaurus() {
		return idThesaurus;
	}

	public void setIdThesaurus(int idThesaurus) {
		this.idThesaurus = idThesaurus;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public int getId_image() {
		return id_image;
	}

	public void setId_image(int idImage) {
		id_image = idImage;
	}

}
