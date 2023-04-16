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

    private String uri;
    private ArrayList<SKOSResource> conceptList;
    private WritePdfSettings writePdfSettings;

    private HashMap<String, String> idToNameHashMap;
    private HashMap<String, ArrayList<Integer>> idToIsTrad;
    private ArrayList<String> resourceChecked;


    public WriteAlphaPDF(WritePdfSettings writePdfSettings, SKOSXmlDocument xmlDocument) {

        this.writePdfSettings = writePdfSettings;

        uri = xmlDocument.getConceptScheme().getUri();
        conceptList = xmlDocument.getConceptList();

        resourceChecked = new ArrayList<>();
        idToNameHashMap = new HashMap<>();
        idToIsTrad = new HashMap<>();
    }


    public void writeAlphabetiquePDF(ArrayList<Paragraph> paragraphs, ArrayList<Paragraph> paragraphTradList,
                                     String codeLanguage1, String codeLanguage2) throws BadElementException, IOException {

        traitement(paragraphs, false, codeLanguage1, codeLanguage2);

        if (StringUtils.isNotEmpty(codeLanguage2)) {
            traitement(paragraphTradList, true, codeLanguage2, codeLanguage1);
        }
    }

    private void traitement(ArrayList<Paragraph> paragraphs, boolean isTrad, String codeLanguage1, String codeLanguage2) throws BadElementException, IOException {

        // Trier les concepts selon leurs labels
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(conceptList, sortAlphabeticInLang(isTrad, codeLanguage1, codeLanguage2, idToNameHashMap, idToIsTrad, resourceChecked));

        // Construire la liste des concepts sous forme d'une suite des paragraphs
        for (SKOSResource concept : conceptList) {
            writeTerm(concept, paragraphs, codeLanguage1, codeLanguage2);
        }
    }

    private void writeTerm(SKOSResource concept, ArrayList<Paragraph> paragraphs, String codeLanguage1, String codeLanguage2) throws BadElementException, IOException {

        int altLabelCount = 0;
        ArrayList<Integer> tradList = idToIsTrad.get(writePdfSettings.getIdFromUri(concept.getUri()));
        if (CollectionUtils.isNotEmpty(tradList)) {
            altLabelCount = (int) tradList.stream().filter(trad -> trad == SKOSProperty.altLabel).count();
        }

        int altLabelWrite = 0;
        for (SKOSLabel label : concept.getLabelsList()) {
            if (label.getLanguage().equals(codeLanguage1) || label.getLanguage().equals(codeLanguage2)) {
                String labelValue;
                boolean prefIsTrad = false;
                boolean altIsTrad = false;

                if (label.getLanguage().equals(codeLanguage1)) {
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
                    anchor.setReference(uri + "&idc=" + writePdfSettings.getIdFromUri(concept.getUri()));
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

            if (!doc.getLanguage().equals(codeLanguage1) && !doc.getLanguage().equals(codeLanguage2)) {
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
            if (doc.getLanguage().equals(codeLanguage1)) {
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
