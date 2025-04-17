package search;

import connexion.ConnexionTest;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import java.time.LocalTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

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
        DataSource ds = connexionTest.getConnexionPool();
        SearchHelper searchHelper = new SearchHelper();
        ArrayList<NodeSearchMini> nodeSearchMinis;

        System.out.println("Avant  : " + LocalTime.now());
        for(int i=0; i<1000; i++){
            nodeSearchMinis = searchHelper.searchExactMatch("amphore", "fr", "TH_1", false);
            //nodeSearchMinis = searchHelper.searchFullTextElastic("amphore", "fr", "TH_1");
        }
        System.out.println("Après  : " + LocalTime.now());
    }
}
