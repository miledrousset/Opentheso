package fr.cnrs.opentheso.bean.alignment;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
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


public class SearchAllignementByConceptCallable implements Callable<List<NodeAlignment>> {

    private AlignementElement alignementElement;
    private AlignementSource alignementSource;
    private HikariDataSource connection;
    private List<String> allLangsTheso;
    private List<String> thesaurusLangs;
    private String idTheso;
    private String idConcept;
    private String idCurrentLang;
    private String nom;
    private String prenom;


    public SearchAllignementByConceptCallable(AlignementElement alignementElement, AlignementSource alignementSource,
                                              HikariDataSource connection, List<String> allLangsTheso,
                                              List<String> thesaurusLangs, String idTheso, String idConcept,
                                              String idCurrentLang, String nom, String prenom) {

        this.alignementElement = alignementElement;
        this.alignementSource = alignementSource;
        this.connection = connection;
        this.allLangsTheso = allLangsTheso;
        this.thesaurusLangs = thesaurusLangs;
        this.idTheso = idTheso;
        this.idConcept = idConcept;
        this.idCurrentLang = idCurrentLang;
        this.nom = nom;
        this.prenom = prenom;

    }

    @Override
    public List<NodeAlignment> call() throws Exception {
        List<NodeAlignment> alignmentFound = new ArrayList<>();

        List<NodeAlignment> tmp = searchAlignmentsV2(alignementSource, idTheso, alignementElement.getIdConceptOrig(),
                alignementElement.getLabelConceptOrig(), idCurrentLang, nom, prenom);

        if (CollectionUtils.isNotEmpty(tmp)) {

            ExecutorService executor = Executors.newFixedThreadPool(tmp.size());
            List<Callable<NodeAlignment>> callables = new ArrayList<>();
            for (NodeAlignment nodeAlignment : tmp) {
                callables.add(new SearchSingleAllignementCallable(alignementSource, nodeAlignment,
                        connection, thesaurusLangs, allLangsTheso, idCurrentLang, idTheso, idConcept));
            }

            try {
                List<Future<NodeAlignment>> futures = executor.invokeAll(callables);
                for (Future<NodeAlignment> future : futures) {
                    alignmentFound.add(future.get());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            executor.shutdown();
        }

        return alignmentFound;
    }

    private List<NodeAlignment> searchAlignmentsV2(AlignementSource alignementSource, String idTheso, String idConcept,
                                                   String lexicalValue, String idLang, String nom, String prenom) {

        switch(alignementSource.getSource_filter().toUpperCase()) {
            case "WIKIDATA_SPARQL":
                return new WikidataHelper().queryWikidata_sparql(idConcept, idTheso, lexicalValue.trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "WIKIDATA_REST":
                return new WikidataHelper().queryWikidata_rest(idConcept, idTheso, lexicalValue.trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFSUJETS":
                return new IdRefHelper().queryIdRefSubject(idConcept, idTheso, lexicalValue.trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFPERSONNES":
                return new IdRefHelper().queryIdRefPerson(idConcept, idTheso, lexicalValue.trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFAUTEURS":
                return new IdRefHelper().queryIdRefNames(idConcept, idTheso, nom, prenom, alignementSource.getRequete(),
                        alignementSource.getSource());
            case "IDREFLIEUX":
                return new IdRefHelper().queryIdRefLieux(idConcept, idTheso, lexicalValue.trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFTITREUNIFORME":
                return new IdRefHelper().queryIdRefUniformtitle(idConcept, idTheso, lexicalValue.trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            case "GETTY_AAT":
                return new GettyAATHelper().queryAAT(idConcept, idTheso, lexicalValue.trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "OPENTHESO":
                return new OpenthesoHelper().queryOpentheso(idConcept, idTheso, lexicalValue.trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "GEMET":
                return new GemetHelper().queryGemet(idConcept, idTheso, lexicalValue.trim(),
                        idLang, alignementSource.getRequete(), alignementSource.getSource());
            case "AGROVOC":
                return new AgrovocHelper().queryAgrovoc(idConcept, idTheso, lexicalValue.trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "GEONAMES":
                return new GeoNamesHelper().queryGeoNames(idConcept, idTheso, lexicalValue.trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "ONTOME":
                return new OntomeHelper().queryOntomeHelper(idConcept, idTheso, lexicalValue.trim(),
                        alignementSource.getRequete(), alignementSource.getSource());
            default:
                return Collections.emptyList();
        }
    }
}
