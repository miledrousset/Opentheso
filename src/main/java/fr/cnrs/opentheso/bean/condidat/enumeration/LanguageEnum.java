package fr.cnrs.opentheso.bean.condidat.enumeration;

public enum LanguageEnum {
    
    FR("Français"),
    EN("English"),
    ES("Española"),
    IT("Italiano"),
    DE("Deutsche");
    
    private String language;
    
    LanguageEnum(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
    
}
