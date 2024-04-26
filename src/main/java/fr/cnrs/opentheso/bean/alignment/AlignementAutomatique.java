package fr.cnrs.opentheso.bean.alignment;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.core.alignment.AlignementSource;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


public class AlignementAutomatique {


    public List<NodeAlignment> searchAlignementsAutomatique(HikariDataSource connection, String idTheso, String idCurrentLang,
                                                            List<AlignementElement> allignementsList,
                                                            AlignementSource alignementSource, String nom, String prenom,
                                                            String alignementMode, List<NodeIdValue> idsAndValues) {

        List<NodeAlignment> allAlignementFound = new ArrayList<>();

        var thesaurusLangs = new ThesaurusHelper().getIsoLanguagesOfThesaurus(connection, idTheso);
        thesaurusLangs.remove(idCurrentLang);
        var allLangsTheso = new ThesaurusHelper().getIsoLanguagesOfThesaurus(connection, idTheso);

        var listConcepts = new HashSet<>(allignementsList);

        ExecutorService executor = Executors.newFixedThreadPool(listConcepts.size());
        List<Callable<List<NodeAlignment>>> callables = new ArrayList<>();

        if ("V2".equalsIgnoreCase(alignementMode)) {
            //Supprimer l'alignement déjà ajouté dans la liste des alignements proposés
            idsAndValues = idsAndValues.stream()
                    .peek(element -> {
                        var alignements = new AlignmentHelper().getAllAlignmentOfConcept(connection, element.getId(), idTheso)
                                .stream()
                                .filter(alignement -> alignement.getThesaurus_target().equalsIgnoreCase(alignementSource.getSource()))
                                .collect(Collectors.toList());
                        element.setAlignements(alignements);
                    })
                    .filter(element -> CollectionUtils.isNotEmpty(element.getAlignements()))
                    .collect(Collectors.toList());

            for (NodeIdValue concept : idsAndValues) {
                var definition = new NoteHelper().getDefinitionByLangAndSource(connection, concept.getId(), idTheso,
                        idCurrentLang, alignementSource.getSource());
                for (NodeAlignment alignment : concept.getAlignements()) {
                    callables.add(new SearchAllignementByConceptCallable(alignementSource, connection, allLangsTheso, thesaurusLangs, idTheso,
                            concept, idCurrentLang, nom, prenom, alignment, definition, alignementMode));
                }
            }
        } else {
            for (AlignementElement alignementElement : listConcepts) {
                var concept = idsAndValues.stream().filter(element -> element.getId().equals(alignementElement.getIdConceptOrig())).findFirst().get();
                callables.add(new SearchAllignementByConceptCallable(alignementSource, connection, allLangsTheso, thesaurusLangs, idTheso,
                        concept, idCurrentLang, nom, prenom, null, null, alignementMode));
            }
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
