package virtuoso;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.virtuoso.SparqlStruct;
import org.junit.Test;
import fr.cnrs.opentheso.virtuoso.SynchroSparql;

import java.util.ArrayList;
import java.util.List;

public class SynchroSparqlTest {

    @Test
    public void runVirtuosoTest() {

        SynchroSparql synchroSparql = new SynchroSparql();

        SparqlStruct sparqlStruct = new SparqlStruct();
        sparqlStruct.setAdresseServeur("http://localhost");
        sparqlStruct.setNom_d_utilisateur("dba");
        sparqlStruct.setMot_de_passe("dba");
        sparqlStruct.setThesaurus("th1");
        sparqlStruct.setGraph("Firas_TEST");

        synchroSparql.setSparqlStruct(sparqlStruct);

        NodeLangTheso nodeLangTheso = new NodeLangTheso();
        nodeLangTheso.setCode("fr");
        nodeLangTheso.setValue("fr");
        synchroSparql.setListe_lang(List.of(nodeLangTheso));

        synchroSparql.setListe_group(new ArrayList<>());

        synchroSparql.run();

    }

}
