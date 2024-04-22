package fr.cnrs.opentheso.bean.alignment;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.core.alignment.AlignementSource;
import fr.cnrs.opentheso.core.alignment.helper.AgrovocHelper;
import fr.cnrs.opentheso.core.alignment.helper.GemetHelper;
import fr.cnrs.opentheso.core.alignment.helper.GeoNamesHelper;
import fr.cnrs.opentheso.core.alignment.helper.GettyAATHelper;
import fr.cnrs.opentheso.core.alignment.helper.IdRefHelper;
import fr.cnrs.opentheso.core.alignment.helper.OntomeHelper;
import fr.cnrs.opentheso.core.alignment.helper.OpenthesoHelper;
import fr.cnrs.opentheso.core.alignment.helper.WikidataHelper;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


public class SearchAllignementByConceptCallable implements Callable<List<NodeAlignment>> {

    private final AlignementSource alignementSource;
    private final HikariDataSource connection;
    private final List<String> allLangsTheso;
    private final List<String> thesaurusLangs;
    private final String idTheso;
    private final NodeIdValue concept;
    private final String idCurrentLang;
    private final String nom;
    private final String prenom;
    private final NodeAlignment aligementLocal;
    private final NodeNote definition;


    public SearchAllignementByConceptCallable(AlignementSource alignementSource,
                                              HikariDataSource connection, List<String> allLangsTheso,
                                              List<String> thesaurusLangs, String idTheso, NodeIdValue concept,
                                              String idCurrentLang, String nom, String prenom,
                                              NodeAlignment aligementLocal, NodeNote definition) {

        this.alignementSource = alignementSource;
        this.connection = connection;
        this.allLangsTheso = allLangsTheso;
        this.thesaurusLangs = thesaurusLangs;
        this.idTheso = idTheso;
        this.concept = concept;
        this.idCurrentLang = idCurrentLang;
        this.nom = nom;
        this.prenom = prenom;
        this.aligementLocal = aligementLocal;
        this.definition = definition;
    }

    @Override
    public List<NodeAlignment> call() throws Exception {
        List<NodeAlignment> alignmentFound = new ArrayList<>();

        List<NodeAlignment> tmp = searchAlignmentsV2(alignementSource, idTheso, concept, idCurrentLang, nom, prenom);

        if (CollectionUtils.isNotEmpty(tmp)) {
            tmp = tmp.stream()
                    .filter(element -> !element.getUri_target().equalsIgnoreCase(aligementLocal.getUri_target()))
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(tmp)) {

            ExecutorService executor = Executors.newFixedThreadPool(tmp.size());
            List<Callable<NodeAlignment>> callables = new ArrayList<>();
            int limit = 3;
            for (NodeAlignment nodeAlignment : tmp) {
                if (limit == 0) break;
                callables.add(new SearchSingleAllignementCallable(alignementSource, nodeAlignment,
                        connection, thesaurusLangs, allLangsTheso, idCurrentLang, idTheso, concept.getId()));
                limit --;
            }

            try {
                List<Future<NodeAlignment>> futures = executor.invokeAll(callables);
                for (Future<NodeAlignment> future : futures) {
                    var nodeAlignment = future.get();
                    nodeAlignment.setLabelLocal(aligementLocal.getConcept_target());
                    nodeAlignment.setUriTargetLocal(aligementLocal.getUri_target());
                    nodeAlignment.setAlignementTypeLocal(aligementLocal.getAlignementTypeLocal());
                    nodeAlignment.setDefinitionLocal(definition.getLexicalvalue());
                    nodeAlignment.setConceptOrigin(concept.getValue());
                    alignmentFound.add(nodeAlignment);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            executor.shutdown();
        }

        return alignmentFound;
    }

    private List<NodeAlignment> searchAlignmentsV2(AlignementSource alignementSource, String idTheso, NodeIdValue concept,
                                                   String idLang, String nom, String prenom) {

        switch(alignementSource.getSource_filter().toUpperCase()) {
            case "WIKIDATA_SPARQL":
                return new WikidataHelper().queryWikidata_sparql(concept.getId(), idTheso, concept.getValue().trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "WIKIDATA_REST":
                return new WikidataHelper().queryWikidata_rest(concept.getId(), idTheso, concept.getValue().trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFSUJETS":
                return new IdRefHelper().queryIdRefSubject(concept.getId(), idTheso, concept.getValue().trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFPERSONNES":
                return new IdRefHelper().queryIdRefPerson(concept.getId(), idTheso, concept.getValue().trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFAUTEURS":
                return new IdRefHelper().queryIdRefNames(concept.getId(), idTheso, nom, prenom, alignementSource.getRequete(),
                        alignementSource.getSource());
            case "IDREFLIEUX":
                return new IdRefHelper().queryIdRefLieux(concept.getId(), idTheso, concept.getValue().trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFTITREUNIFORME":
                return new IdRefHelper().queryIdRefUniformtitle(concept.getId(), idTheso, concept.getValue().trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            case "GETTY_AAT":
                return new GettyAATHelper().queryAAT(concept.getId(), idTheso, concept.getValue().trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "OPENTHESO":
                return new OpenthesoHelper().queryOpentheso(concept.getId(), idTheso, concept.getValue().trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "GEMET":
                return new GemetHelper().queryGemet(concept.getId(), idTheso, concept.getValue().trim(),
                        idLang, alignementSource.getRequete(), alignementSource.getSource());
            case "AGROVOC":
                return new AgrovocHelper().queryAgrovoc(concept.getId(), idTheso, concept.getValue().trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "GEONAMES":
                return new GeoNamesHelper().queryGeoNames(concept.getId(), idTheso, concept.getValue().trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "ONTOME":
                return new OntomeHelper().queryOntomeHelper(concept.getId(), idTheso, concept.getValue().trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            default:
                return Collections.emptyList();
        }
    }
}
