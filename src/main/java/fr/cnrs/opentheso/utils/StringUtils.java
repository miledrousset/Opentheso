package fr.cnrs.opentheso.utils;


public class StringUtils {
    
    
    public static String formatTitle(String title) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(title)) {
            return title;
        }
        
        if (title.length() == 0) {
            return title.toUpperCase();
        }
        
        return title.substring(0, 1).toUpperCase() + title.substring(1);
    }
    
}
