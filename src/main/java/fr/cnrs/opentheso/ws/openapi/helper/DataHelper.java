/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.ConnexionRest;
import fr.cnrs.opentheso.ws.RestRDFHelper;

/**
 *
 * @author julie
 */
public class DataHelper {
    
    public static HikariDataSource connect() {
        ConnexionRest connexionRest = new ConnexionRest();
        return connexionRest.getConnexion();
    }    
    
    public static String getDatas(
            String idTheso, String idLang,
            String [] groups,
            String value,
            String format, String filter,
            String match) {
        format = HeaderHelper.removeCharset(format);
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            if (filter != null && filter.equalsIgnoreCase("notation:")) {
                datas = restRDFHelper.findNotation(ds, idTheso, value, format);
                ds.close();
                return datas;
            }
            datas = restRDFHelper.findConcepts(ds,
                    idTheso, idLang, groups, value, format, match);
        }
        if (datas == null) {
            return null;
        }
        return datas;
    }
    
    public static String getDatasFromArk(
            String idTheso,
            String idLang,
            String idArk,
            boolean showLabels) {

        HikariDataSource ds = null;
        String datas;
        try {
            ds = connect();
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            if(idLang == null || idLang.isEmpty()) {
                datas = restRDFHelper.exportConcept(ds,
                        idArk, "application/json");
            } else
                datas = restRDFHelper.exportConceptFromArkWithLang(ds,
                        idArk, idTheso, idLang, showLabels, "application/json");
            ds.close();
            return datas;
        } catch (Exception e) {
            if (ds != null) {
                ds.close();
            }
        }
        return null;
    }
       
   public static String getAutocompleteDatas(String idTheso,
                                        String idLang, String[] groups,
                                        String value, boolean withNotes) {
       String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }   RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.findAutocompleteConcepts(ds,
                    idTheso, idLang, groups, value, withNotes);
        }

        return datas;
   }
    
}
