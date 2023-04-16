package fr.cnrs.opentheso.core.exports.pdf.new_export;

import com.itextpdf.text.*;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.skosapi.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


import static fr.cnrs.opentheso.skosapi.SKOSResource.sortAlphabeticInLang;

public class WriteAlphaPDF {

    private final static String TAB_NIVEAU = "    ";
    private final static String ID = TAB_NIVEAU + "ID: ";
    private final static String USE = TAB_NIVEAU + "USE: ";
    private final static String LATITUDE = TAB_NIVEAU + "lat: ";
    private final static String LONGITUDE = TAB_NIVEAU + "lon: ";

    private WritePdfSettings writePdfSettings;


    public WriteAlphaPDF(WritePdfSettings writePdfSettings) {
        this.writePdfSettings = writePdfSettings;
    }


    public void writeAlphabetiquePDF(SKOSXmlDocument xmlDocument, ArrayList<Paragraph> paragraphs, String langue,
                                      String langue2, boolean isTrad, HashMap<String, String> idToNameHashMap,
                                      HashMap<String, ArrayList<Integer>> idToIsTrad,
                                      ArrayList<String> resourceChecked) throws BadElementException, IOException {

        ArrayList<SKOSResource> conceptList = xmlDocument.getConceptList();

        // Trier les concepts selon leurs labels
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(conceptList, sortAlphabeticInLang(isTrad, langue, langue2, idToNameHashMap, idToIsTrad, resourceChecked));

        // Construire la liste des concepts sous forme d'une suite des paragraphs
        for (SKOSResource concept : conceptList) {
            writeTerm(xmlDocument, concept, paragraphs, langue, langue2, idToIsTrad, idToNameHashMap);
        }
    }

    private void writeTerm(SKOSXmlDocument xmlDocument, SKOSResource concept, ArrayList<Paragraph> paragraphs,
                           String langue, String langue2, HashMap<String, ArrayList<Integer>> idToIsTrad,
                           HashMap<String, String> idToNameHashMap) throws BadElementException, IOException {

        int altLabelCount = 0;
        ArrayList<Integer> tradList = idToIsTrad.get(writePdfSettings.getIdFromUri(concept.getUri()));
        if (CollectionUtils.isNotEmpty(tradList)) {
            altLabelCount = (int) tradList.stream().filter(trad -> trad == SKOSProperty.altLabel).count();
        }

        int altLabelWrite = 0;
        for (SKOSLabel label : concept.getLabelsList()) {
            if (label.getLanguage().equals(langue) || label.getLanguage().equals(langue2)) {
                String labelValue;
                boolean prefIsTrad = false;
                boolean altIsTrad = false;

                if (label.getLanguage().equals(langue)) {
                    labelValue = label.getLabel();
                } else {
                    if (CollectionUtils.isNotEmpty(tradList)) {
                        if (tradList.contains(SKOSProperty.prefLabel) && label.getProperty() == SKOSProperty.prefLabel) {
                            prefIsTrad = true;
                        }
                        if (tradList.contains(SKOSProperty.altLabel) && label.getProperty() == SKOSProperty.altLabel) {
                            if (altLabelCount > altLabelWrite) {
                                altIsTrad = true;
                            }
                            altLabelWrite++;
                        }
                    }
                    labelValue = "-";
                }

                if (label.getProperty() == SKOSProperty.prefLabel && !prefIsTrad) {
                    Anchor anchor = new Anchor(labelValue, writePdfSettings.termFont);
                    anchor.setReference(xmlDocument.getConceptScheme().getUri() + "&idc=" + writePdfSettings.getIdFromUri(concept.getUri()));
                    paragraphs.add(new Paragraph(anchor));
                } else if (label.getProperty() == SKOSProperty.altLabel && !altIsTrad) {
                    paragraphs.add(new Paragraph(USE + labelValue, writePdfSettings.textFont));
                }
            }
        }

        paragraphs.add(new Paragraph(ID + writePdfSettings.getIdFromUri(concept.getUri()), writePdfSettings.textFont));

        for (SKOSRelation relation : concept.getRelationsList()) {
            String targetName = idToNameHashMap.get(writePdfSettings.getIdFromUri(relation.getTargetUri()));
            if (ObjectUtils.isEmpty(writePdfSettings.getIdFromUri(relation.getTargetUri()))) {
                targetName = writePdfSettings.getIdFromUri(relation.getTargetUri());
            }
            Chunk chunk = new Chunk(TAB_NIVEAU + writePdfSettings.getCodeRelation(relation.getProperty()) + ": " + targetName, writePdfSettings.relationFont);
            chunk.setLocalGoto(writePdfSettings.getIdFromUri(relation.getTargetUri()));
            paragraphs.add(new Paragraph(chunk));
        }

        for (SKOSDocumentation doc : concept.getDocumentationsList()) {

            if (!doc.getLanguage().equals(langue) && !doc.getLanguage().equals(langue2)) {
                continue;
            }

            int docCount = 0;
            if (CollectionUtils.isNotEmpty(tradList)) {
                for (int lab : tradList) {
                    if (lab == SKOSProperty.note) {
                        docCount++;
                    }
                }
            }

            String docText = "";
            boolean docIsTrad = false;
            if (doc.getLanguage().equals(langue)) {
                docText = doc.getText();
            } else {
                if (tradList != null && tradList.contains(SKOSProperty.note)) {
                    if (docCount > 0) {
                        docIsTrad = true;
                    }
                }
            }
            if (!docIsTrad) {
                paragraphs.add(new Paragraph(TAB_NIVEAU + writePdfSettings.getDocTypeName(doc.getProperty())
                        + ": " + docText, writePdfSettings.textFont));
            }
        }

        for (SKOSMatch match : concept.getMatchList()) {
            paragraphs.add(new Paragraph(TAB_NIVEAU + writePdfSettings.getMatchTypeName(match.getProperty())
                    + ": " + match.getValue(), writePdfSettings.textFont));
        }

        if (ObjectUtils.isNotEmpty(concept.getGPSCoordinates())
                && StringUtils.isNotEmpty(concept.getGPSCoordinates().getLat())
                && StringUtils.isNotEmpty(concept.getGPSCoordinates().getLon())) {

            paragraphs.add(new Paragraph(LATITUDE + concept.getGPSCoordinates().getLat(), writePdfSettings.textFont));
            paragraphs.add(new Paragraph(LONGITUDE + concept.getGPSCoordinates().getLon(), writePdfSettings.textFont));
        }

        if (CollectionUtils.isNotEmpty(concept.getNodeImage())) {
            for (NodeImage nodeImage : concept.getNodeImage()) {
                Image image = Image.getInstance(new URL(nodeImage.getUri()));
                image.scaleAbsolute(writePdfSettings.resiseImage(image));
                paragraphs.add(new Paragraph(new Chunk(image, 0, 0, true)));
            }
        }
    }
}
