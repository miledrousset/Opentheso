/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;

/**
 *
 * @author julie
 */
public class DataHelper {
    
    public static String getDatas(HikariDataSource ds,
                                  String idTheso, String idLang,
                                  String [] groups,
                                  String value,
                                  String format, String filter,
                                  String match) {

        format = HeaderHelper.removeCharset(format);
        if (filter != null && filter.equalsIgnoreCase("notation:")) {
            return new RestRDFHelper().findNotation(ds, idTheso, value, format);
        }
        return new RestRDFHelper().findConcepts(ds, idTheso, idLang, groups, value, format, match);
    }
    
}
