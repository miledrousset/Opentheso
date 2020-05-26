
package fr.cnrs.opentheso.core.cron;

//import java.util.Date;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;



/**
 *
 * @author miled.rousset
 */
public class TimerExportThesaurus implements Runnable {

    private HikariDataSource ds;
    private Properties prefs;
    public TimerExportThesaurus(HikariDataSource ds1,
            Properties prefs) {
        this.ds = ds1;
        this.prefs = prefs;
    }

    
    /**
     * Cette fonction est l'action du cron qui sera lancé suivant le 
     * paramètre du cron
     */
    public void run() {
    //    System.out.println("Current system time: " + new Date());
    //    System.out.println("Another minute ticked away...");
//        export();
    }
    
    public void export(){
  /*      ExportFromBDD_Frantiq exportFromBDD_Frantiq = new ExportFromBDD_Frantiq();
        exportFromBDD_Frantiq.setServerAdress(prefs.getProperty("serverAdress"));
        exportFromBDD_Frantiq.setServerArk(prefs.getProperty("serverArk"));
        if("true".equals(prefs.getProperty("arkActive"))) {
            exportFromBDD_Frantiq.setArkActive(true);
        } 
        else
            exportFromBDD_Frantiq.setArkActive(false);

        StringBuffer stringBuffer = exportFromBDD_Frantiq.exportThesaurus(ds, "1");
        
        //System.out.println(stringBuffer);*/
    }

}