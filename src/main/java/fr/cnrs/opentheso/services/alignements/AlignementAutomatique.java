package fr.cnrs.opentheso.services.alignements;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.repositories.AlignmentHelper;
import fr.cnrs.opentheso.repositories.NoteHelper;
import fr.cnrs.opentheso.repositories.ThesaurusHelper;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.alignment.AlignementElement;
import fr.cnrs.opentheso.models.alignment.AlignementSource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


@Service
public class AlignementAutomatique {

    @Autowired
    private AlignmentHelper alignmentHelper;

    @Autowired
    private NoteHelper noteHelper;

    @Autowired
    private ThesaurusHelper thesaurusHelper;


    public List<NodeAlignment> searchAlignementsAutomatique(HikariDataSource connection, String idTheso, String idCurrentLang,
                                                            List<AlignementElement> allignementsList,
                                                            AlignementSource alignementSource, String nom, String prenom,
                                                            String alignementMode, List<NodeIdValue> idsAndValues) {

        List<NodeAlignment> allAlignementFound = new ArrayList<>();

        var thesaurusLangs = thesaurusHelper.getIsoLanguagesOfThesaurus(connection, idTheso);
        thesaurusLangs.remove(idCurrentLang);
        var allLangsTheso = thesaurusHelper.getIsoLanguagesOfThesaurus(connection, idTheso);

        var listConcepts = new HashSet<>(allignementsList);

        ExecutorService executor = Executors.newFixedThreadPool(listConcepts.size());
        List<Callable<List<NodeAlignment>>> callables = new ArrayList<>();

        if ("alignement-comparaison".equalsIgnoreCase(alignementMode)) {
            //Supprimer l'alignement déjà ajouté dans la liste des alignements proposés
            idsAndValues = idsAndValues.stream()
                    .peek(element -> {
                        var alignements = alignmentHelper.getAllAlignmentOfConcept(connection, element.getId(), idTheso)
                                .stream()
                                .filter(alignement -> StringUtils.isEmpty(alignement.getThesaurus_target())
                                        || alignement.getThesaurus_target().equalsIgnoreCase(alignementSource.getSource())
                                        || getBaseUrl(alignementSource.getRequete()).equalsIgnoreCase(getBaseUrl(alignement.getUri_target()))
                                        || (alignementSource.getSource().contains("sparql") && alignement.getThesaurus_target().contains("Wikidata")))
                                .collect(Collectors.toList());
                        element.setAlignements(alignements);
                    })
                    .filter(element -> CollectionUtils.isNotEmpty(element.getAlignements()))
                    .collect(Collectors.toList());

            for (NodeIdValue concept : idsAndValues) {
                var definitions = noteHelper.getDefinition(connection, concept.getId(), idTheso, idCurrentLang);
                var definition = "";
                if (CollectionUtils.isNotEmpty(definitions)) {
                    definition = definitions.get(0);
                }
                var alignmentSelected = concept.getAlignements().stream()
                        .filter(source -> (getBaseUrl(alignementSource.getRequete()).equalsIgnoreCase(getBaseUrl(source.getUri_target())))
                                || source.getThesaurus_target().equalsIgnoreCase(alignementSource.getSource())
                                || (alignementSource.getSource().contains("sparql") && source.getThesaurus_target().contains("Wikidata")))
                        .findFirst();
                if (alignmentSelected.isPresent()) {
                    callables.add(new SearchAllignementByConceptCallable(alignementSource, connection, allLangsTheso,
                            thesaurusLangs, idTheso, concept, idCurrentLang, nom, prenom, alignmentSelected.get(), definition,
                            alignementMode));
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

    private String getBaseUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getHost();
        } catch (MalformedURLException e) {
            return "";
        }
    }
}
