/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package handle;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.ws.handlestandard.HandleService;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.handle.hdllib.HandleException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author miledrousset
 */
public class ManageHandle {
    
    public ManageHandle() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void getHandle() {
    
    }
    
    @Test
    public void deleteHandle() {
    
    }    

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
