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

    private WritePdfSettings writePdfSettings;


    public WriteAlphaPDF() throws DocumentException, IOException {
        writePdfSettings = new WritePdfSettings();
    }


    public void writeAlphabetiquePDF(SKOSXmlDocument xmlDocument, ArrayList<Paragraph> paragraphs, String langue,
                                      String langue2, boolean isTrad, HashMap<String, String> idToNameHashMap,
                                      HashMap<String, ArrayList<Integer>> idToIsTrad,
                                      ArrayList<String> resourceChecked) throws BadElementException, IOException {

        ArrayList<SKOSResource> conceptList = xmlDocument.getConceptList();
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(conceptList, sortAlphabeticInLang(isTrad, langue, langue2, idToNameHashMap, idToIsTrad, resourceChecked));
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
                    if (tradList != null) {
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
                    paragraphs.add(new Paragraph("    USE: " + labelValue, writePdfSettings.textFont));
                }
            }
        }

        paragraphs.add(new Paragraph("    ID: " + writePdfSettings.getIdFromUri(concept.getUri()), writePdfSettings.textFont));

        for (SKOSRelation relation : concept.getRelationsList()) {
            String key = writePdfSettings.getIdFromUri(relation.getTargetUri());
            String targetName = idToNameHashMap.get(key);
            if (targetName == null) {
                targetName = key;
            }
            Chunk chunk = new Chunk("    " + writePdfSettings.getCodeRelation(relation.getProperty()) + ": " + targetName, writePdfSettings.relationFont);
            chunk.setLocalGoto(writePdfSettings.getIdFromUri(relation.getTargetUri()));
            paragraphs.add(new Paragraph(chunk));
        }

        for (SKOSDocumentation doc : concept.getDocumentationsList()) {

            if (!doc.getLanguage().equals(langue)
                    && !doc.getLanguage().equals(langue2)) {
                continue;
            }

            int docCount = 0;
            if (tradList != null) {
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
                paragraphs.add(new Paragraph("    " + writePdfSettings.getDocTypeName(doc.getProperty())
                        + ": " + docText, writePdfSettings.textFont));
            }
        }

        for (SKOSMatch match : concept.getMatchList()) {
            paragraphs.add(new Paragraph("    " + writePdfSettings.getMatchTypeName(match.getProperty()) + ": "
                    + match.getValue(), writePdfSettings.textFont));
        }

        if (ObjectUtils.isNotEmpty(concept.getGPSCoordinates())
                && StringUtils.isNotEmpty(concept.getGPSCoordinates().getLat())
                && StringUtils.isNotEmpty(concept.getGPSCoordinates().getLon())) {
            paragraphs.add(new Paragraph("    lat: " + concept.getGPSCoordinates().getLat(), writePdfSettings.textFont));
            paragraphs.add(new Paragraph("    lat: " + concept.getGPSCoordinates().getLon(), writePdfSettings.textFont));
        }

        if (CollectionUtils.isNotEmpty(concept.getNodeImage())) {
            for (NodeImage nodeImage : concept.getNodeImage()) {
                Image image = Image.getInstance(new URL(nodeImage.getUri()));
                image.scaleAbsolute(200f, 200f);
                paragraphs.add(new Paragraph(new Chunk(image, 0, 0, true)));
            }
        }
    }
}
