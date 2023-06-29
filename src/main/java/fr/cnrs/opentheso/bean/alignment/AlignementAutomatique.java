package fr.cnrs.opentheso.bean.alignment;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ExternalImagesHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.core.alignment.AlignementSource;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.alignment.helper.AgrovocHelper;
import fr.cnrs.opentheso.core.alignment.helper.GemetHelper;
import fr.cnrs.opentheso.core.alignment.helper.GeoNamesHelper;
import fr.cnrs.opentheso.core.alignment.helper.GettyAATHelper;
import fr.cnrs.opentheso.core.alignment.helper.IdRefHelper;
import fr.cnrs.opentheso.core.alignment.helper.OntomeHelper;
import fr.cnrs.opentheso.core.alignment.helper.OpenthesoHelper;
import fr.cnrs.opentheso.core.alignment.helper.WikidataHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlignementAutomatique {


    public List<NodeAlignment> searchAlignementsAutomatique(HikariDataSource connection, String idTheso, String idCurrentLang,
                                                            String idConcept, ArrayList<AlignementElement> allignementsList,
                                                            List<AlignementSource> alignementSources, String nom, String prenom) {

        List<NodeAlignment> allAlignementFound = new ArrayList<>();

        List<String> thesaurusLangs = new ThesaurusHelper().getIsoLanguagesOfThesaurus(connection, idTheso);
        thesaurusLangs.remove(idCurrentLang);
        List<String> allLangsTheso = new ThesaurusHelper().getIsoLanguagesOfThesaurus(connection, idTheso);

        Set<AlignementElement> listConcepts = new HashSet<>(allignementsList);

        for (AlignementElement alignementElement : listConcepts) {

            List<NodeAlignment> alignmentFound = new ArrayList<>();
            for (AlignementSource alignementSource : alignementSources) {

                List<NodeAlignment> tmp = searchAlignmentsV2(alignementSource, idTheso, alignementElement.getIdConceptOrig(),
                        alignementElement.getLabelConceptOrig(), idCurrentLang, nom, prenom);

                if (CollectionUtils.isNotEmpty(tmp)) {
                    for (NodeAlignment nodeAlignment : tmp) {
                        switch(alignementSource.getSource_filter().toUpperCase()) {
                            case "WIKIDATA_SPARQL":
                            case "WIKIDATA_REST":
                                loadWikiDatas(connection, nodeAlignment, thesaurusLangs, allLangsTheso, idTheso, idConcept);
                                break;
                            case "IDREFSUJETS":
                            case "IDREFPERSONNES":
                            case "IDREFAUTEURS":
                            case "IDREFLIEUX":
                            case "IDREFTITREUNIFORME":
                            case "GETTY_AAT":
                            case "OPENTHESO":
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
                    }

                    alignmentFound.addAll(tmp);
                }
            }

            if (CollectionUtils.isNotEmpty(alignmentFound)) {
                allAlignementFound.addAll(alignmentFound);
            }
        }

        return allAlignementFound;
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

        GemetHelper gemetHelper = new GemetHelper();
        gemetHelper.setOptions(nodeAlignment, List.of("langues", "notes", "images"), thesaurusLangs, allLangTheso);

        if (!"Pactols".equalsIgnoreCase(alignementSource.getSource())) {
            if (CollectionUtils.isNotEmpty(gemetHelper.getResourceTraductions())) {
                nodeAlignment.setSelectedTraductionsList(
                        searchTraductions(gemetHelper.getResourceTraductions(), connection, idConcept, idTheso));
            }
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
                        idLang, alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFPERSONNES":
                return new IdRefHelper().queryIdRefPerson(idConcept, idTheso, lexicalValue.trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFAUTEURS":
                return new IdRefHelper().queryIdRefNames(idConcept, idTheso, nom, prenom, idLang, alignementSource.getRequete(),
                        alignementSource.getSource());
            case "IDREFLIEUX":
                return new IdRefHelper().queryIdRefLieux(idConcept, idTheso, lexicalValue.trim(), idLang,
                        alignementSource.getRequete(), alignementSource.getSource());
            case "IDREFTITREUNIFORME":
                return new IdRefHelper().queryIdRefUniformtitle(idConcept, idTheso, lexicalValue.trim(),
                        idLang, alignementSource.getRequete(), alignementSource.getSource());
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

        String idTerm = new TermHelper().getIdTermOfConcept(connection, idConcept, idTheso);
        ArrayList<NodeNote> terms = new NoteHelper().getListNotesTermAllLang(connection, idTerm, idTheso);

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
