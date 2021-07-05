package virtuoso;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import org.junit.Ignore;
import org.junit.Test;
import fr.cnrs.opentheso.virtuoso.SynchroSparql;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class SynchroSparqlTest {

    @Test
    public void virtuosoTest() {

        SynchroSparql synchroSparql = new SynchroSparql();

        NodeLangTheso nodeLangTheso = new NodeLangTheso();
        nodeLangTheso.setCode("fr");
        nodeLangTheso.setValue("fr");
        synchroSparql.setListe_lang(List.of(nodeLangTheso));

        synchroSparql.setListe_group(new ArrayList<>());

        synchroSparql.run();

    }

}
