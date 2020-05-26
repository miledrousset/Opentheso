package fr.cnrs.opentheso.bdd.datas;

public class Terme_nd {

	private int id_terme;
	private String id_langue;
	private int id_thesaurus;
	private String nd;
	private int id_mt;
	
	
	public Terme_nd() {
		
	}

	public Terme_nd(int idTerme, String idLangue, int idThesaurus, String nd,
			int idMt) {
		super();
		id_terme = idTerme;
		id_langue = idLangue;
		id_thesaurus = idThesaurus;
		this.nd = nd;
		id_mt = idMt;
	}


	public int getId_terme() {
		return id_terme;
	}


	public void setId_terme(int idTerme) {
		id_terme = idTerme;
	}


	public String getId_langue() {
		return id_langue;
	}


	public void setId_langue(String idLangue) {
		id_langue = idLangue;
	}


	public int getId_thesaurus() {
		return id_thesaurus;
	}


	public void setId_thesaurus(int idThesaurus) {
		id_thesaurus = idThesaurus;
	}


	public String getNd() {
		return nd;
	}


	public void setNd(String nd) {
		this.nd = nd;
	}


	public int getId_mt() {
		return id_mt;
	}


	public void setId_mt(int idMt) {
		id_mt = idMt;
	}
	
	
}
