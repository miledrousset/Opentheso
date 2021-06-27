package virtuoso;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import org.junit.Test;
import fr.cnrs.opentheso.virtuoso.SynchroSparql;

import java.util.ArrayList;
import java.util.List;


public class SynchroSparqlTest {

    @Test
    public void runVirtuosoTest() {

        SynchroSparql synchroSparql = new SynchroSparql();

        NodeLangTheso nodeLangTheso = new NodeLangTheso();
        nodeLangTheso.setCode("fr");
        nodeLangTheso.setValue("fr");
        synchroSparql.setListe_lang(List.of(nodeLangTheso));

        synchroSparql.setListe_group(new ArrayList<>());

        synchroSparql.run();

    }

}
