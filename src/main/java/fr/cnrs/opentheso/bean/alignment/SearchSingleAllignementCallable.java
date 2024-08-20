package fr.cnrs.opentheso.bean.alignment;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ExternalImagesHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.core.alignment.AlignementSource;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.alignment.helper.AgrovocHelper;
import fr.cnrs.opentheso.core.alignment.helper.GemetHelper;
import fr.cnrs.opentheso.core.alignment.helper.GeoNamesHelper;
import fr.cnrs.opentheso.core.alignment.helper.WikidataHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public class SearchSingleAllignementCallable implements Callable<NodeAlignment> {

    private AlignementSource alignementSource;
    private HikariDataSource connection;
    private NodeAlignment nodeAlignment;
    private List<String> thesaurusLangs;
    private List<String> allLangsTheso;
    private String idCurrentLang;
    private String idTheso;
    private String idConcept;


    public SearchSingleAllignementCallable(AlignementSource alignementSource,
                                           NodeAlignment nodeAlignment,
                                           HikariDataSource connection,
                                           List<String> thesaurusLangs,
                                           List<String> allLangsTheso,
                                           String idCurrentLang,
                                           String idTheso, String idConcept) {

        this.alignementSource = alignementSource;
        this.connection = connection;
        this.nodeAlignment = nodeAlignment;
        this.allLangsTheso = allLangsTheso;
        this.idCurrentLang = idCurrentLang;
        this.idTheso = idTheso;
        this.idConcept = idConcept;
        this.thesaurusLangs = thesaurusLangs;
    }

    @Override
    public NodeAlignment call() {
        switch(alignementSource.getSource_filter().toUpperCase()) {
            case "WIKIDATA_SPARQL":
            case "WIKIDATA_REST":
                loadWikiDatas(connection, nodeAlignment, thesaurusLangs, allLangsTheso, idTheso, idConcept);
                break;
            case "GETTY_AAT":
            case "GEMET":
                loadGemeDatas(connection, alignementSource, nodeAlignment, thesaurusLangs, allLangsTheso, idTheso, idConcept);
                break;
            case "AGROVOC":
                loadAgrovocDatas(connection, nodeAlignment, thesaurusLangs, idTheso, idConcept, idCurrentLang);
                break;
            case "GEONAMES" :
                loadGeoNameDatas(connection, nodeAlignment, thesaurusLangs, idTheso, idConcept);
                break;
        }

        nodeAlignment.setAlignementLocalValide(true);

        return nodeAlignment;
    }

    public boolean isURLAvailable(String urlString) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHead httpHead = new HttpHead(urlString);
            try (CloseableHttpResponse response = httpClient.execute(httpHead)) {
                int statusCode = response.getStatusLine().getStatusCode();
                return (statusCode == 200);
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void loadGeoNameDatas(HikariDataSource connection, NodeAlignment nodeAlignment, List<String> thesaurusLangs,
                                  String idTheso, String idConcept) {

        GeoNamesHelper geoNamesHelper = new GeoNamesHelper();
        geoNamesHelper.setOptions(nodeAlignment, List.of("langues", "notes", "images"), thesaurusLangs);

        if (CollectionUtils.isNotEmpty(geoNamesHelper.getResourceTraductions())) {
            nodeAlignment.setSelectedTraductionsList(searchTraductions(geoNamesHelper.getResourceTraductions(),
                    connection, idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(geoNamesHelper.getResourceDefinitions())) {
            nodeAlignment.setSelectedDefinitionsList(searchDefinitions(geoNamesHelper.getResourceDefinitions(),
                    connection, idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(geoNamesHelper.getResourceImages())) {
            nodeAlignment.setSelectedImagesList(searchImages(geoNamesHelper.getResourceImages(), connection, idConcept, idTheso));
        }
    }

    private void loadAgrovocDatas(HikariDataSource connection, NodeAlignment nodeAlignment, List<String> thesaurusLangs,
                                  String idTheso, String idConcept, String idLang) {

        AgrovocHelper agrovocHelper = new AgrovocHelper();
        agrovocHelper.setOptions(nodeAlignment, List.of("langues", "notes", "images"), thesaurusLangs, idLang);

        if (CollectionUtils.isNotEmpty(agrovocHelper.getResourceTraductions())) {
            nodeAlignment.setSelectedTraductionsList(searchTraductions(agrovocHelper.getResourceTraductions(),
                    connection, idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(agrovocHelper.getResourceDefinitions())) {
            nodeAlignment.setSelectedDefinitionsList(searchDefinitions(agrovocHelper.getResourceDefinitions(),
                    connection, idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(agrovocHelper.getResourceImages())) {
            nodeAlignment.setSelectedImagesList(searchImages(agrovocHelper.getResourceImages(), connection, idConcept, idTheso));
        }
    }

    private void loadGemeDatas(HikariDataSource connection, AlignementSource alignementSource, NodeAlignment nodeAlignment,
                               List<String> thesaurusLangs, List<String> allLangTheso, String idTheso, String idConcept) {

        List<String> sourceToDisableTraduction = List.of("IdRefPersonnes", "Pactols");
        List<String> options;
        if (sourceToDisableTraduction.contains(alignementSource.getSource())) {
            options = List.of("notes", "images");
        } else {
            options = List.of("langues", "notes", "images");
        }

        GemetHelper gemetHelper = new GemetHelper();
        gemetHelper.setOptions(nodeAlignment, options, thesaurusLangs, allLangTheso);

        if (CollectionUtils.isNotEmpty(gemetHelper.getResourceTraductions())) {
            nodeAlignment.setSelectedTraductionsList(
                    searchTraductions(gemetHelper.getResourceTraductions(), connection, idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(gemetHelper.getResourceDefinitions())) {
            nodeAlignment.setSelectedDefinitionsList(searchDefinitions(gemetHelper.getResourceDefinitions(),
                    connection, idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(gemetHelper.getResourceImages())) {
            nodeAlignment.setSelectedImagesList(searchImages(gemetHelper.getResourceImages(), connection, idConcept, idTheso));
        }
    }

    private void loadWikiDatas(HikariDataSource connection, NodeAlignment nodeAlignment, List<String> thesaurusLangs,
                               List<String> allLangTheso, String idTheso, String idConcept) {

        WikidataHelper wikidataHelper = new WikidataHelper();
        wikidataHelper.setOptionsFromWikidata(nodeAlignment, List.of("langues", "notes", "images"), thesaurusLangs, allLangTheso);

        if (CollectionUtils.isNotEmpty(wikidataHelper.getResourceWikidataTraductions())) {
            nodeAlignment.setSelectedTraductionsList(searchTraductions(wikidataHelper.getResourceWikidataTraductions(),
                    connection, idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(wikidataHelper.getResourceWikidataDefinitions())) {
            nodeAlignment.setSelectedDefinitionsList(searchDefinitions(wikidataHelper.getResourceWikidataDefinitions(),
                    connection, idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(wikidataHelper.getResourceWikidataImages())) {
            nodeAlignment.setSelectedImagesList(searchImages(wikidataHelper.getResourceWikidataImages(),
                    connection, idConcept, idTheso));
        }
    }



    private List<SelectedResource> searchTraductions(List<SelectedResource> traductionsoOfAlignmentTemp,
                                                     HikariDataSource connection, String idConcept, String idTheso) {

        List<SelectedResource> selectedTraductionsList = new ArrayList<>();
        List<NodeTermTraduction> termTraductions = new TermHelper().getAllTraductionsOfConcept(connection, idConcept, idTheso);

        for (SelectedResource selectedResource : traductionsoOfAlignmentTemp) {
            boolean added = false;
            for (NodeTermTraduction nodeTermTraduction : termTraductions) {
                if (selectedResource.getIdLang().equalsIgnoreCase(nodeTermTraduction.getLang())) {
                    if (!selectedResource.getGettedValue().trim().equalsIgnoreCase(nodeTermTraduction.getLexicalValue().trim())) {
                        selectedResource.setLocalValue(nodeTermTraduction.getLexicalValue());
                        selectedTraductionsList.add(selectedResource);
                        added = true;
                        break;
                    } else {
                        added = true;
                        break;
                    }
                }
            }
            if (!added) {
                selectedTraductionsList.add(selectedResource);
            }
        }
        return selectedTraductionsList;
    }

    private List<SelectedResource> searchDefinitions(List<SelectedResource> definitionOfAlignmentTemp,
                                                     HikariDataSource connection, String idConcept, String idTheso) {

        List<SelectedResource> selectedDefinitionsList = new ArrayList<>();

        var terms = new NoteHelper().getListNotesAllLang(connection, idConcept, idTheso);

        for (SelectedResource selectedResource : definitionOfAlignmentTemp) {
            boolean added = false;
            for (NodeNote nodeNote : terms) {
                if (!selectedResource.getGettedValue().trim().equalsIgnoreCase(nodeNote.getLexicalvalue().trim())) {
                    selectedResource.setLocalValue(nodeNote.getLexicalvalue());
                    selectedDefinitionsList.add(selectedResource);
                    added = true;
                    break;
                } else {
                    added = true;
                    break;
                }
            }
            // si on a déjà ajouté la traduction, on l'ignore, sinon, on l'ajoute
            if (!added) {
                selectedDefinitionsList.add(selectedResource);
            }
        }

        return selectedDefinitionsList;
    }

    private List<SelectedResource> searchImages(List<SelectedResource> imagesOfAlignmentTemp,
                                                HikariDataSource connection, String idConcept, String idTheso) {

        List<NodeImage> images = new ExternalImagesHelper().getExternalImages(connection, idConcept, idTheso);
        List<SelectedResource> selectedImages = new ArrayList<>();

        for (SelectedResource selectedResource : imagesOfAlignmentTemp) {
            boolean added = false;
            for (NodeImage nodeImage : images) {
                if (!selectedResource.getGettedValue().trim().equalsIgnoreCase(nodeImage.getUri().trim())) {
                    selectedResource.setLocalValue(nodeImage.getUri());
                    selectedImages.add(selectedResource);
                    added = true;
                    break;
                } else {
                    added = true;
                    break;
                }
            }
            // si on a déjà ajouté la traduction, on l'ignore, sinon, on l'ajoute
            if (!added) {
                selectedImages.add(selectedResource);
            }
        }

        return selectedImages;
    }
}
