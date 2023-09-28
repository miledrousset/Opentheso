package fr.cnrs.opentheso.bean.alignment;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.AlignementSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class AlignementAutomatique {


    public List<NodeAlignment> searchAlignementsAutomatique(HikariDataSource connection, String idTheso, String idCurrentLang,
                                                            String idConcept, List<AlignementElement> allignementsList,
                                                            AlignementSource alignementSource, String nom, String prenom) {

        List<NodeAlignment> allAlignementFound = new ArrayList<>();

        var thesaurusLangs = new ThesaurusHelper().getIsoLanguagesOfThesaurus(connection, idTheso);
        thesaurusLangs.remove(idCurrentLang);
        var allLangsTheso = new ThesaurusHelper().getIsoLanguagesOfThesaurus(connection, idTheso);

        var listConcepts = new HashSet<>(allignementsList);

        ExecutorService executor = Executors.newFixedThreadPool(listConcepts.size());
        List<Callable<List<NodeAlignment>>> callables = new ArrayList<>();
        for (AlignementElement alignementElement : listConcepts) {
            callables.add(new SearchAllignementByConceptCallable(alignementElement,
                    alignementSource, connection, allLangsTheso, thesaurusLangs, idTheso,
                    idConcept, idCurrentLang, nom, prenom));
        }

        try {
            List<Future<List<NodeAlignment>>> futures = executor.invokeAll(callables);
            for (Future<List<NodeAlignment>> future : futures) {
                allAlignementFound.addAll(future.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        executor.shutdown();

        return allAlignementFound;
    }

}
