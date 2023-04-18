package fr.cnrs.opentheso.core.exports.pdf.new_export;

import com.itextpdf.text.Image;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Paragraph;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSLabel;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.skosapi.SKOSMatch;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


import static fr.cnrs.opentheso.skosapi.SKOSResource.sortAlphabeticInLang;

public class WriteAlphaPDF {

    private final static String TAB_NIVEAU = "    ";
    private final static String ID = TAB_NIVEAU + "ID: ";
    private final static String USE = TAB_NIVEAU + "USE: ";
    private final static String LATITUDE = TAB_NIVEAU + "lat: ";
    private final static String LONGITUDE = TAB_NIVEAU + "lon: ";

    private String uri;
    private WritePdfSettings writePdfSettings;

    private HashMap<String, String> labels;
    private HashMap<String, ArrayList<Integer>> traductions;

    private ArrayList<SKOSResource> concepts;
    private ArrayList<String> resourceChecked;


    public WriteAlphaPDF(WritePdfSettings writePdfSettings, SKOSXmlDocument xmlDocument) {

        this.writePdfSettings = writePdfSettings;

        uri = xmlDocument.getConceptScheme().getUri();
        concepts = xmlDocument.getConceptList();

        resourceChecked = new ArrayList<>();
        labels = new HashMap<>();
        traductions = new HashMap<>();
    }

    public void writeAlphabetiquePDF(ArrayList<Paragraph> paragraphs, ArrayList<Paragraph> paragraphTradList,
                                     String codeLanguage1, String codeLanguage2) {

        traitement(paragraphs, false, codeLanguage1, codeLanguage2);

        if (StringUtils.isNotEmpty(codeLanguage2)) {
            traitement(paragraphTradList, true, codeLanguage2, codeLanguage1);
        }
    }

    private void traitement(ArrayList<Paragraph> paragraphs, boolean isTrad, String codeLanguage1, String codeLanguage2) {

        // Trier les concepts selon leurs labels
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(concepts, sortAlphabeticInLang(isTrad, codeLanguage1, codeLanguage2, labels, traductions, resourceChecked));

        // Construire la liste des concepts sous forme d'une suite des paragraphs
        for (SKOSResource concept : concepts) {
            writeTerm(concept, paragraphs, codeLanguage1, codeLanguage2);
        }
    }

    private void writeTerm(SKOSResource concept, ArrayList<Paragraph> paragraphs, String codeLanguage1, String codeLanguage2) {

        String idFromUri = writePdfSettings.getIdFromUri(concept.getUri());

        addLabels(paragraphs, concept.getLabelsList(), codeLanguage1, codeLanguage2, idFromUri);
        paragraphs.add(new Paragraph(ID + idFromUri, writePdfSettings.textFont));
        addRelations(paragraphs, concept.getRelationsList());
        addDocuments(paragraphs, concept.getDocumentationsList(), traductions.get(idFromUri), codeLanguage1, codeLanguage2);
        addMatchs(paragraphs, concept.getMatchList());
        addGpsCoordiantes(paragraphs, concept.getGPSCoordinates());
        addImages(paragraphs, concept.getNodeImage());

    }

    private void addLabels(List<Paragraph> paragraphs, ArrayList<SKOSLabel> labels, String codeLanguage1,
                                String codeLanguage2, String idFromUri) {

        int altLabelCount = 0;
        if (CollectionUtils.isNotEmpty(traductions.get(idFromUri))) {
            altLabelCount = (int) traductions.get(idFromUri).stream().filter(trad -> trad == SKOSProperty.altLabel).count();
        }

        int altLabelWrite = 0;
        for (SKOSLabel label : labels) {
            if (label.getLanguage().equals(codeLanguage1) || label.getLanguage().equals(codeLanguage2)) {
                String labelValue;
                boolean prefIsTrad = false;
                boolean altIsTrad = false;

                if (label.getLanguage().equals(codeLanguage1)) {
                    labelValue = label.getLabel();
                } else {
                    if (CollectionUtils.isNotEmpty(traductions.get(idFromUri))) {
                        if (traductions.get(idFromUri).contains(SKOSProperty.prefLabel) && label.getProperty() == SKOSProperty.prefLabel) {
                            prefIsTrad = true;
                        }
                        if (traductions.get(idFromUri).contains(SKOSProperty.altLabel) && label.getProperty() == SKOSProperty.altLabel) {
                            if (altLabelCount > altLabelWrite) {
                                altIsTrad = true;
                            }
                            altLabelWrite++;
                        }
                    }
                    labelValue = "-";
                }

                if (label.getProperty() == SKOSProperty.prefLabel && !prefIsTrad) {
                    Paragraph paragraph = new Paragraph();
                    Anchor anchor = new Anchor(labelValue, writePdfSettings.termFont);
                    anchor.setReference(uri + "&idc=" + idFromUri);
                    paragraph.add(anchor);
                    paragraphs.add(paragraph);
                } else if (label.getProperty() == SKOSProperty.altLabel && !altIsTrad) {
                    paragraphs.add(new Paragraph(USE + labelValue, writePdfSettings.textFont));
                }
            }
        }
    }

    private void addRelations(List<Paragraph> paragraphs, List<SKOSRelation> relations) {

        if (CollectionUtils.isNotEmpty(relations)) {
            relations.stream()
                    .forEach(relation -> {
                        String targetName = labels.get(writePdfSettings.getIdFromUri(relation.getTargetUri()));
                        if (ObjectUtils.isEmpty(targetName)) {
                            targetName = writePdfSettings.getIdFromUri(relation.getTargetUri());
                        }
                        if (StringUtils.isNotEmpty(writePdfSettings.getCodeRelation(relation.getProperty()))) {
                            Chunk chunk = new Chunk(TAB_NIVEAU + writePdfSettings.getCodeRelation(relation.getProperty())
                                    + ": " + targetName, writePdfSettings.relationFont);
                            chunk.setLocalGoto(writePdfSettings.getIdFromUri(relation.getTargetUri()));
                            paragraphs.add(new Paragraph(chunk));
                        }
                    });
        }
    }

    private void addDocuments(List<Paragraph> paragraphs, List<SKOSDocumentation> documentations, List<Integer> tradList,
                              String codeLanguage1, String codeLanguage2) {

        if (CollectionUtils.isNotEmpty(documentations)) {
            documentations.stream()
                    .filter(document -> (document.getLanguage().equals(codeLanguage1) || document.getLanguage().equals(codeLanguage2)))
                    .forEach(document -> {
                        int docCount = 0;
                        if (CollectionUtils.isNotEmpty(tradList)) {
                            docCount = (int) tradList.stream().filter(traduction -> traduction == SKOSProperty.note).count();
                        }

                        String docText = "";
                        boolean docIsTrad = false;
                        if (document.getLanguage().equals(codeLanguage1)) {
                            docText = document.getText();
                        } else {
                            if (CollectionUtils.isNotEmpty(tradList) && tradList.contains(SKOSProperty.note)) {
                                docIsTrad = (docCount > 0);
                            }
                        }

                        if (!docIsTrad) {
                            paragraphs.add(new Paragraph(TAB_NIVEAU + writePdfSettings.getDocTypeName(document.getProperty())
                                    + ": " + docText, writePdfSettings.textFont));
                        }
                    });
        }
    }

    private void addMatchs(List<Paragraph> paragraphs, List<SKOSMatch> matchs){

        if (CollectionUtils.isNotEmpty(matchs)) {
            matchs.stream().forEach(match ->
                    paragraphs.add(new Paragraph(TAB_NIVEAU + writePdfSettings.getMatchTypeName(match.getProperty())
                    + ": " + match.getValue(), writePdfSettings.textFont))
            );
        }
    }

    private void addGpsCoordiantes(List<Paragraph> paragraphs, SKOSGPSCoordinates skosgpsCoordinates) {

        if (ObjectUtils.isNotEmpty(skosgpsCoordinates)
                && StringUtils.isNotEmpty(skosgpsCoordinates.getLat())
                && StringUtils.isNotEmpty(skosgpsCoordinates.getLon())) {

            paragraphs.add(new Paragraph(LATITUDE + skosgpsCoordinates.getLat(), writePdfSettings.textFont));
            paragraphs.add(new Paragraph(LONGITUDE + skosgpsCoordinates.getLon(), writePdfSettings.textFont));
        }
    }

    private void addImages(List<Paragraph> paragraphs, List<NodeImage> images) {

        if (CollectionUtils.isNotEmpty(images)) {
            images.stream().forEach(element -> {
                try {
                    Image image = Image.getInstance(new URL(element.getUri()));
                    image.scaleAbsolute(writePdfSettings.resiseImage(image));
                    paragraphs.add(new Paragraph(new Chunk(image, 0, 0, true)));
                } catch(Exception ex) {}
            });
        }
    }
}
