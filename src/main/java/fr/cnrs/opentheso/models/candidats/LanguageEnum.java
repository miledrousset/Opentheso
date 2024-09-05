package fr.cnrs.opentheso.models.candidats;

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

    static LanguageEnum valueOfLanguage(String label) throws IllegalArgumentException {
        try {
            return valueOf(label.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Ici je prefere mon propre message.
            throw new IllegalArgumentException("La valeur " + label + " n'est pas disponible.");
        }
    }

    public static LanguageEnum fromString(String text) {
        for (LanguageEnum b : LanguageEnum.values()) {
            if (b.language.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }

}
