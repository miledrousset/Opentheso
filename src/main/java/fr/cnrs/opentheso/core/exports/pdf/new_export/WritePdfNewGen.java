package fr.cnrs.opentheso.core.exports.pdf.new_export;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnrs.opentheso.core.exports.pdf.WritePdf;
import fr.cnrs.opentheso.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.skosapi.SKOSLabel;
import fr.cnrs.opentheso.skosapi.SKOSMatch;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import java.net.URL;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import static fr.cnrs.opentheso.skosapi.SKOSResource.sortAlphabeticInLang;
import static fr.cnrs.opentheso.skosapi.SKOSResource.sortForHiera;
import java.util.List;


public class WritePdfNewGen {

    private WritePdfSettings writePdfSettings;
    private SKOSXmlDocument xmlDocument;
    private Document document;

    private ArrayList<Paragraph> paragraphList = new ArrayList<>();
    private ArrayList<Paragraph> paragraphTradList = new ArrayList<>();

    private HashMap<String, String> idToNameHashMap;
    private HashMap<String, List<String>> idToChildId = new HashMap<>();
    private HashMap<String, ArrayList<String>> idToDocumentation = new HashMap<>();
    private HashMap<String, ArrayList<String>> idToDocumentation2 = new HashMap<>();
    private HashMap<String, ArrayList<String>> idToMatch = new HashMap<>();
    private HashMap<String, ArrayList<NodeImage>> idToImg = new HashMap<>();
    private HashMap<String, String> idToGPS = new HashMap<>();

    private HashMap<String, ArrayList<Integer>> idToIsTrad = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> idToIsTradDiff = new HashMap<>();
    private ArrayList<String> resourceChecked = new ArrayList<>();


    public byte[] createPdfFile(HikariDataSource hikariDataSource, SKOSXmlDocument xmlDocument,
                                String codeLang, String codeLang2, PdfExportType pdfExportType) throws DocumentException, IOException {

        this.xmlDocument = xmlDocument;

        document = new Document();
        idToNameHashMap = new HashMap<>();
        writePdfSettings = new WritePdfSettings();

        if (StringUtils.isNotEmpty(codeLang2)) {
            document.setPageSize(PageSize.LETTER.rotate());
        }

        try ( ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            PdfWriter writer = PdfWriter.getInstance(document, output);

            // PAraetrage de l'entête / pied de page
            writer.setPageEvent(new HeaderFooterPdfPageEvent(
                    xmlDocument.getConceptScheme().getThesaurus().getId_thesaurus()
                            + " - " + xmlDocument.getConceptScheme().getThesaurus().getTitle()
                            + " ( " + xmlDocument.getConceptScheme().getThesaurus().getLanguage() + " )"));
            document.open();

            new WriteConceptShemas().writeConceptShemas(writePdfSettings, document, xmlDocument, codeLang, codeLang2);

            if (pdfExportType == PdfExportType.ALPHABETIQUE) {
                writeAlphabetiquePDF(paragraphList, codeLang, codeLang2, false);
            } else {
                writeHieraPDF(hikariDataSource, paragraphList, codeLang, codeLang2, false, idToDocumentation);
            }

            if (StringUtils.isBlank(codeLang2)) {
                for (Paragraph paragraph : paragraphList) {
                    document.add(paragraph);
                }
            } else {
                if (pdfExportType == PdfExportType.ALPHABETIQUE) {
                    writeAlphabetiquePDF(paragraphTradList, codeLang2, codeLang, true);
                } else {
                    writeHieraPDF(hikariDataSource, paragraphTradList, codeLang2, codeLang, true, idToDocumentation2);
                }

                PdfPTable table = new PdfPTable(2);

                int listSize = Integer.min(paragraphList.size(), paragraphTradList.size());
                for (int i = 0; i < listSize; i++) {
                    Paragraph paragraph = paragraphList.get(i);
                    Paragraph paragraphTrad = paragraphTradList.get(i);

                    PdfPCell cell1 = new PdfPCell();
                    cell1.addElement(paragraph);
                    cell1.setBorderWidth(Rectangle.NO_BORDER);

                    PdfPCell cell2 = new PdfPCell();
                    cell2.addElement(paragraphTrad);
                    cell2.setBorder(Rectangle.NO_BORDER);

                    table.addCell(cell1);
                    table.addCell(cell2);
                }
            }
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            if (ObjectUtils.isNotEmpty(document)) {
                document.close();
            }
            Logger.getLogger(WritePdf.class.getName()).log(Level.SEVERE, null, ex);
            return new byte[0];
        }
    }

    /**
     * ecri un thésaurus en PDF par ordre hiérarchique
     */
    private void writeHieraPDF(HikariDataSource hikariDataSource, ArrayList<Paragraph> paragraphs,
                               String langue, String langue2, boolean isTrad, HashMap<String, ArrayList<String>> idToDoc) {

        ArrayList<SKOSResource> conceptList = xmlDocument.getConceptList();
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(conceptList, sortForHiera(hikariDataSource, isTrad, langue, langue2, idToNameHashMap,
                idToChildId, idToDoc, idToMatch, idToGPS, idToImg, resourceChecked, idToIsTradDiff));

        for (SKOSResource concept : conceptList) {

            boolean isAtRoot = true;

            String conceptID = writePdfSettings.getIdFromUri(concept.getUri());

            Iterator i = idToChildId.keySet().iterator();
            String clef;
            ArrayList<String> valeur;
            while (i.hasNext()) {
                clef = (String) i.next();
                valeur = (ArrayList<String>) idToChildId.get(clef);

                for (String id : valeur) {
                    if (id.equals(conceptID)) {
                        isAtRoot = false;
                    }
                }
            }

            if (isAtRoot) {
                String name = idToNameHashMap.get(conceptID);
                if (StringUtils.isEmpty(name)) {
                    name = "";
                }

                Paragraph paragraph = new Paragraph();
                Anchor anchor = new Anchor(name + " (" + conceptID + ")", writePdfSettings.termFont);
                anchor.setReference(xmlDocument.getConceptScheme().getUri() + "&idc=" + conceptID);
                paragraph.add(anchor);
                paragraphs.add(paragraph);

                String indentation = "";
                writeHieraTermInfo(conceptID, indentation, paragraphs, idToDoc);
                writeHieraTermRecursif(conceptID, indentation, paragraphs, idToDoc);
            }
        }
    }

    /**
     * fonction recursive qui sert a ecrire tout les fils des term
     */
    private void writeHieraTermRecursif(String id, String indentation, ArrayList<Paragraph> paragraphs, HashMap<String, ArrayList<String>> idToDoc) {

        indentation += ".......";

        List<String> childList = idToChildId.get(id);
        if (childList == null) {
            return;
        }

        for (String idFils : childList) {
            String name = idToNameHashMap.get(idFils);
            if (name == null) {
                name = "";
            }

            Paragraph paragraph = new Paragraph();
            Anchor anchor = new Anchor(indentation + name + " (" + idFils + ")", writePdfSettings.textFont);
            anchor.setReference(xmlDocument.getConceptScheme().getUri() + "&idc=" + idFils);
            paragraph.add(anchor);
            paragraphs.add(paragraph);

            writeHieraTermInfo(idFils, indentation, paragraphs, idToDoc);
            writeHieraTermRecursif(idFils, indentation, paragraphs, idToDoc);
        }
    }

    /**
     * ecri les données d'un term pour le format hiérarchique
     */
    private void writeHieraTermInfo(String key, String indenatation, ArrayList<Paragraph> paragraphs, HashMap<String, ArrayList<String>> idToDoc) {

        ArrayList<Integer> tradList = idToIsTradDiff.get(key);

        String space = "";
        for (int i = 0; i < indenatation.length(); i++) {
            space += " ";
        }

        //doc
        ArrayList<String> docList = idToDoc.get(key);
        int docCount = 0;

        if (tradList != null) {
            for (int lab : tradList) {
                if (lab == SKOSProperty.note) {
                    docCount++;
                }
            }
        }
        int docWrite = 0;

        if (docList != null) {
            for (String doc : docList) {
                paragraphs.add(new Paragraph(space + doc, writePdfSettings.hieraInfoFont));
                docWrite++;
            }

        }
        if (docWrite < docCount) {
            for (int i = 0; i < docCount; i++) {
                paragraphs.add(new Paragraph(space + "-", writePdfSettings.hieraInfoFont));
            }
        }

        //match
        if (CollectionUtils.isNotEmpty(idToMatch.get(key))) {
            for (String match : idToMatch.get(key)) {
                paragraphs.add(new Paragraph(space + match, writePdfSettings.hieraInfoFont));
            }
        }

        if (StringUtils.isNotEmpty(idToGPS.get(key))) {
            paragraphs.add(new Paragraph(space + idToGPS.get(key), writePdfSettings.hieraInfoFont));
        }

        if (CollectionUtils.isNotEmpty(idToImg.get(key))) {
            for (NodeImage nodeImage : idToImg.get(key)) {
                try {
                    Image image = Image.getInstance(new URL(nodeImage.getUri()));
                    image.scaleAbsolute(writePdfSettings.resiseImage(image));
                    paragraphs.add(new Paragraph(new Chunk(image, indenatation.length()*(2.9f), 20, true)));
                } catch (BadElementException | IOException ex) {}
            }
        }
    }

    /**
     * ecri un thésaurus en PDF par ordre alphabetique
     */
    private void writeAlphabetiquePDF(ArrayList<Paragraph> paragraphs, String langue, String langue2, boolean isTrad) throws BadElementException, IOException {

        ArrayList<SKOSResource> conceptList = xmlDocument.getConceptList();
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(conceptList, sortAlphabeticInLang(isTrad, langue, langue2, idToNameHashMap, idToIsTrad, resourceChecked));
        for (SKOSResource concept : conceptList) {
            writeTerm(concept, paragraphs, langue, langue2);
        }
    }

    /**
     * ajoute un paragraphe qui decri le term dans le document PDF
     */
    private void writeTerm(SKOSResource concept, ArrayList<Paragraph> paragraphs,
            String langue, String langue2) throws BadElementException, IOException {

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
