
package graph;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class Graph {
    
    public Graph() {
    }
    
    @Test
    public void getChildren() {
        String idTheso = "th5";
        String idConcept = "31"; 
        
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();

        PathHelper pathHelper = new PathHelper();

        List<String> paths = pathHelper.getGraphOfConcept(ds, idConcept, idTheso);
        for (String path : paths) {
            System.out.println(path);
        }
        
        List<List<String>> allPaths = pathHelper.getPathFromGraph(paths);
        for (List path : allPaths) {
            System.out.println(path.toString());
        }
    }
  
}

