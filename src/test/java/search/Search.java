package search;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import java.time.LocalTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/**
 *
 * @author miledrousset
 */
public class Search {

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
