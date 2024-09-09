/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package handle;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.repositories.PreferencesHelper;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.ws.handlestandard.HandleService;
import java.io.UnsupportedEncodingException;
import net.handle.hdllib.HandleException;
import org.junit.jupiter.api.Test;

/**
 *
 * @author miledrousset
 */
public class ManageHandle {

    @Test
    public void testCreateHandle() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();        

        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(ds, "th24");        
        
        
        HandleService hs = HandleService.getInstance();
        hs.applyNodePreference(nodePreference);
        hs.connectHandle();
        try {
            if(!hs.createHandle("testtest3", "http://www.mouad223.fr/")){
                System.out.println(hs.getResponseMsg());
            }
            System.out.println(hs.getResponseMsg());
            //hs.updateHandleUrl("mom16", "www.mom.fr");
            //hs.deleteHandle("mom16");
        } catch (UnsupportedEncodingException | HandleException ex) {
            System.out.println("handle.ManageHandle.testCreateHandle()" + ex.getMessage());
        }
    }    
    
    @Test
    public void testDeleteHandle() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();        

        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(ds, "th24");        
        
        
        HandleService hs = HandleService.getInstance();
        hs.applyNodePreference(nodePreference);
        hs.connectHandle();

        try {
            hs.deleteHandle("20.500.11859/7clp91p0npwt0cb3sw4whtg7j");
        } catch (HandleException ex) {
            System.out.println(ex.toString());
        }

    }    
}
