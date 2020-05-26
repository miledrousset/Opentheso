package fr.cnrs.opentheso.bdd.datas;


public class Languages_iso639 {

    private String id_iso639_1;
    private String id_iso639_2;
    private String english_name;
    private String french_name;

    public Languages_iso639() {
    }

    public String getId_iso639_1() {
        return id_iso639_1;
    }

    public void setId_iso639_1(String id_iso639_1) {
        this.id_iso639_1 = id_iso639_1;
    }

    public String getId_iso639_2() {
        return id_iso639_2;
    }

    public void setId_iso639_2(String id_iso639_2) {
        this.id_iso639_2 = id_iso639_2;
    }

    public String getEnglish_name() {
        return english_name;
    }

    public void setEnglish_name(String english_name) {
        this.english_name = english_name;
    }

    public String getFrench_name() {
        return french_name;
    }

    public void setFrench_name(String french_name) {
        this.french_name = french_name;
    }

}
