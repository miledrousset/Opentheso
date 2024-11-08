package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.models.alignment.AlignementSource;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import fr.cnrs.opentheso.models.alignment.SelectedResource;
import fr.cnrs.opentheso.client.alignement.AgrovocHelper;
import fr.cnrs.opentheso.client.alignement.GemetHelper;
import fr.cnrs.opentheso.client.alignement.GeoNamesHelper;
import fr.cnrs.opentheso.client.alignement.WikidataHelper;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.notes.NodeNote;
import fr.cnrs.opentheso.models.terms.NodeTermTraduction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


@Slf4j
@AllArgsConstructor
public class SearchSingleAllignementCallable implements Callable<NodeAlignment> {

    private AlignementSource alignementSource;
    private NodeAlignment nodeAlignment;
    private DataSource connection;
    private List<String> thesaurusLangs;
    private List<String> allLangsTheso;
    private String idCurrentLang;
    private String idTheso;
    private String idConcept;


    @Override
    public NodeAlignment call() {
        switch(alignementSource.getSource_filter().toUpperCase()) {
            case "WIKIDATA_SPARQL":
            case "WIKIDATA_REST":
                loadWikiDatas(nodeAlignment, thesaurusLangs, allLangsTheso, idTheso, idConcept);
                break;
            case "GETTY_AAT":
            case "GEMET":
                loadGemeDatas(alignementSource, nodeAlignment, thesaurusLangs, allLangsTheso, idTheso, idConcept);
                break;
            case "AGROVOC":
                loadAgrovocDatas(nodeAlignment, thesaurusLangs, idTheso, idConcept, idCurrentLang);
                break;
            case "GEONAMES" :
                loadGeoNameDatas(nodeAlignment, thesaurusLangs, idTheso, idConcept);
                break;
        }

        nodeAlignment.setAlignementLocalValide(true);

        return nodeAlignment;
    }

    private void loadGeoNameDatas(NodeAlignment nodeAlignment, List<String> thesaurusLangs, String idTheso, String idConcept) {

        GeoNamesHelper geoNamesHelper = new GeoNamesHelper();
        geoNamesHelper.setOptions(nodeAlignment, List.of("langues", "notes", "images"), thesaurusLangs);

        if (CollectionUtils.isNotEmpty(geoNamesHelper.getResourceTraductions())) {
            nodeAlignment.setSelectedTraductionsList(searchTraductions(geoNamesHelper.getResourceTraductions(), idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(geoNamesHelper.getResourceDefinitions())) {
            nodeAlignment.setSelectedDefinitionsList(searchDefinitions(geoNamesHelper.getResourceDefinitions(), idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(geoNamesHelper.getResourceImages())) {
            nodeAlignment.setSelectedImagesList(searchImages(geoNamesHelper.getResourceImages(), idConcept, idTheso));
        }
    }

    private void loadAgrovocDatas(NodeAlignment nodeAlignment, List<String> thesaurusLangs, String idTheso, String idConcept, String idLang) {

        AgrovocHelper agrovocHelper = new AgrovocHelper();
        agrovocHelper.setOptions(nodeAlignment, List.of("langues", "notes", "images"), thesaurusLangs, idLang);

        if (CollectionUtils.isNotEmpty(agrovocHelper.getResourceTraductions())) {
            nodeAlignment.setSelectedTraductionsList(searchTraductions(agrovocHelper.getResourceTraductions(), idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(agrovocHelper.getResourceDefinitions())) {
            nodeAlignment.setSelectedDefinitionsList(searchDefinitions(agrovocHelper.getResourceDefinitions(), idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(agrovocHelper.getResourceImages())) {
            nodeAlignment.setSelectedImagesList(searchImages(agrovocHelper.getResourceImages(), idConcept, idTheso));
        }
    }

    private void loadGemeDatas(AlignementSource alignementSource, NodeAlignment nodeAlignment,
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
            nodeAlignment.setSelectedTraductionsList(searchTraductions(gemetHelper.getResourceTraductions(), idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(gemetHelper.getResourceDefinitions())) {
            nodeAlignment.setSelectedDefinitionsList(searchDefinitions(gemetHelper.getResourceDefinitions(), idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(gemetHelper.getResourceImages())) {
            nodeAlignment.setSelectedImagesList(searchImages(gemetHelper.getResourceImages(), idConcept, idTheso));
        }
    }

    private void loadWikiDatas(NodeAlignment nodeAlignment, List<String> thesaurusLangs, List<String> allLangTheso, String idTheso, String idConcept) {

        WikidataHelper wikidataHelper = new WikidataHelper();
        wikidataHelper.setOptionsFromWikidata(nodeAlignment, List.of("langues", "notes", "images"), thesaurusLangs, allLangTheso);

        if (CollectionUtils.isNotEmpty(wikidataHelper.getResourceWikidataTraductions())) {
            nodeAlignment.setSelectedTraductionsList(searchTraductions(wikidataHelper.getResourceWikidataTraductions(), idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(wikidataHelper.getResourceWikidataDefinitions())) {
            nodeAlignment.setSelectedDefinitionsList(searchDefinitions(wikidataHelper.getResourceWikidataDefinitions(), idConcept, idTheso));
        }

        if (CollectionUtils.isNotEmpty(wikidataHelper.getResourceWikidataImages())) {
            nodeAlignment.setSelectedImagesList(searchImages(wikidataHelper.getResourceWikidataImages(), idConcept, idTheso));
        }
    }



    private List<SelectedResource> searchTraductions(List<SelectedResource> traductionsoOfAlignmentTemp,
                                                     String idConcept, String idTheso) {

        List<SelectedResource> selectedTraductionsList = new ArrayList<>();
        List<NodeTermTraduction> termTraductions = getAllTraductionsOfConcept(idConcept, idTheso);

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

    private ArrayList<NodeTermTraduction> getAllTraductionsOfConcept(String idConcept, String idThesaurus) {

        ArrayList<NodeTermTraduction> nodeTraductionsList = new ArrayList<>();

        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT term.id_term, term.lexical_value, term.lang FROM"
                    + " term, preferred_term WHERE term.id_term = preferred_term.id_term"
                    + " and term.id_thesaurus = preferred_term.id_thesaurus"
                    + " and preferred_term.id_concept = '" + idConcept + "'"
                    + " and term.id_thesaurus = '" + idThesaurus + "' order by term.lexical_value");
            try (ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    NodeTermTraduction nodeTraductions = new NodeTermTraduction();
                    nodeTraductions.setLang(resultSet.getString("lang"));
                    nodeTraductions.setLexicalValue(resultSet.getString("lexical_value"));
                    nodeTraductionsList.add(nodeTraductions);
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Traductions of Concept  : " + idConcept, sqle);
        }
        return nodeTraductionsList;
    }

    private List<SelectedResource> searchDefinitions(List<SelectedResource> definitionOfAlignmentTemp, String idConcept, String idTheso) {

        List<SelectedResource> selectedDefinitionsList = new ArrayList<>();

        var terms = getListNotesAllLang(idConcept, idTheso);

        for (SelectedResource selectedResource : definitionOfAlignmentTemp) {
            boolean added = false;
            for (NodeNote nodeNote : terms) {
                if (!selectedResource.getGettedValue().trim().equalsIgnoreCase(nodeNote.getLexicalValue().trim())) {
                    selectedResource.setLocalValue(nodeNote.getLexicalValue());
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

    private ArrayList<NodeNote> getListNotesAllLang(String identifier, String idThesaurus) {

        ArrayList<NodeNote> nodeNotes = new ArrayList<>();

        try (Connection conn = connection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT note.id, note.notetypecode, note.lexicalvalue, note.created, note.modified, note.lang, note.notesource"
                    + " FROM note WHERE note.identifier = '" + identifier + "' AND note.id_thesaurus = '" + idThesaurus + "'");
            try (ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    NodeNote nodeNote = new NodeNote();
                    nodeNote.setIdTerm(identifier);
                    nodeNote.setIdNote(resultSet.getInt("id"));
                    nodeNote.setLang(resultSet.getString("lang"));
                    nodeNote.setLexicalValue(resultSet.getString("lexicalvalue"));
                    nodeNote.setModified(resultSet.getDate("modified"));
                    nodeNote.setCreated(resultSet.getDate("created"));
                    nodeNote.setNoteTypeCode(resultSet.getString("notetypecode"));
                    nodeNote.setNoteSource(resultSet.getString("notesource"));
                    nodeNotes.add(nodeNote);
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All Notes of Term : " + identifier, sqle);
        }
        return nodeNotes;
    }

    private List<SelectedResource> searchImages(List<SelectedResource> imagesOfAlignmentTemp, String idConcept, String idTheso) {

        List<NodeImage> images = getExternalImages(idConcept, idTheso);
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

    private ArrayList<NodeImage> getExternalImages(String idConcept, String idThesausus) {

        ArrayList<NodeImage> nodeImageList = null;

        try (var conn = connection.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeQuery("select * from external_images where id_concept = '" + idConcept + "' and id_thesaurus = '" + idThesausus + "'");
            try (ResultSet resultSet = stmt.getResultSet()) {
                nodeImageList = new ArrayList<>();
                while (resultSet.next()) {
                    NodeImage nodeImage = new NodeImage();
                    nodeImage.setIdConcept(resultSet.getString("id_concept"));
                    nodeImage.setIdThesaurus(resultSet.getString("id_thesaurus"));
                    nodeImage.setImageName(resultSet.getString("image_name"));
                    nodeImage.setCopyRight(resultSet.getString("image_copyright"));
                    nodeImage.setUri(resultSet.getString("external_uri"));
                    nodeImageList.add(nodeImage);
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting image of Concept : " + idConcept, sqle);
        }
        return nodeImageList;
    }
}
