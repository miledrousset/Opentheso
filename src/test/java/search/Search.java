/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package search;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import java.time.LocalTime;
import java.util.ArrayList;
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
public class Search {
    
    public Search() {
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
    public void exact() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();        
        SearchHelper searchHelper = new SearchHelper();
        ArrayList<NodeSearchMini> nodeSearchMinis;

        System.out.println("Avant  : " + LocalTime.now());
        for(int i=0; i<1000; i++){
            nodeSearchMinis = searchHelper.searchExactMatch(ds, "amphore", "fr", "TH_1");
            //nodeSearchMinis = searchHelper.searchFullTextElastic(ds, "amphore", "fr", "TH_1");
        }
        System.out.println("Après  : " + LocalTime.now());
    }
}
