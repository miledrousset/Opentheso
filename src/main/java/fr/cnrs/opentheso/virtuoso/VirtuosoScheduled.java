package fr.cnrs.opentheso.virtuoso;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@WebListener
public class VirtuosoScheduled implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
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

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(synchroSparql, 0, 1, TimeUnit.DAYS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        scheduler.shutdownNow();
    }

}