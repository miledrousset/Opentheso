/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.pdf;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSLabel;
import fr.cnrs.opentheso.skosapi.SKOSMatch;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import static fr.cnrs.opentheso.skosapi.SKOSResource.sortAlphabeticInLang;
import static fr.cnrs.opentheso.skosapi.SKOSResource.sortForHiera;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Quincy
 */
public class WritePdf {

    private SKOSXmlDocument xmlDocument;
    Document document;

    ArrayList<Paragraph> paragraphList = new ArrayList<>();
    ArrayList<Paragraph> paragraphTradList = new ArrayList<>();

    ByteArrayOutputStream output;
    HashMap<String, String> idToNameHashMap;
    HashMap<String, ArrayList<String>> idToChildId = new HashMap<>();
    HashMap<String, ArrayList<String>> idToDocumentation = new HashMap<>();
    HashMap<String, ArrayList<String>> idToDocumentation2 = new HashMap<>();
    HashMap<String, ArrayList<String>> idToMatch = new HashMap<>();
    HashMap<String, String> idToGPS = new HashMap<>();

    HashMap<String, ArrayList<Integer>> idToIsTrad = new HashMap<>();
    HashMap<String, ArrayList<Integer>> idToIsTradDiff = new HashMap<>();
    ArrayList<String> resourceChecked = new ArrayList<>();

    String codeLang;
    String codeLang2;

    BaseFont bf;
    String FONT = "fonts/FreeSans.ttf";
    Font titleFont;
    Font subTitleFont;
    Font termFont;
    Font textFont;
    Font relationFont;
    Font hieraInfoFont;

    /**
     * export un thésaurus en format pdf
     *
     * @param xmlDocument
     * @param codeLang
     * @param codeLang2
     * @param type 1 pour alphabetique / 2 pour hierarchique
     */
    public WritePdf(SKOSXmlDocument xmlDocument, String codeLang, String codeLang2, int type) {

        try {
            bf = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(WritePdf.class.getName()).log(Level.SEVERE, null, ex);
        }

        titleFont = new Font(bf, 20, Font.BOLD);
        subTitleFont = new Font(bf, 16);
        termFont = new Font(bf, 12, Font.BOLD);
        textFont = new Font(bf, 10);
        relationFont = new Font(bf, 10, Font.ITALIC);
        hieraInfoFont = new Font(bf, 10, Font.ITALIC);

        this.codeLang = codeLang;
        this.codeLang2 = codeLang2;
        this.idToNameHashMap = new HashMap<>();
        this.xmlDocument = xmlDocument;
        document = new Document();
        if (codeLang2 != null && !codeLang2.equals("")) {
            document.setPageSize(PageSize.LETTER.rotate());
        }
        try {
            output = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, output);

        } catch (DocumentException ex) {
            Logger.getLogger(WritePdf.class.getName()).log(Level.SEVERE, null, ex);
        }
        document.open();
        writeConceptSheme();
        if (type == 1) {
            writeAlphabetiquePDF(paragraphList, codeLang, codeLang2, false);
        } else if (type == 0) {
            writeHieraPDF(paragraphList, codeLang, codeLang2, false, idToDocumentation);
        }
        try {

            if (codeLang2 != null && codeLang2.equals("")) {
                for (Paragraph paragraph : paragraphList) {

                    document.add(paragraph);

                }

            } else {
                if (type == 1) {
                    writeAlphabetiquePDF(paragraphTradList, codeLang2, codeLang, true);
                } else if (type == 0) {
                    writeHieraPDF(paragraphTradList, codeLang2, codeLang, true, idToDocumentation2);
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

                document.add(table);

            }

        } catch (DocumentException ex) {
            Logger.getLogger(WritePdf.class.getName()).log(Level.SEVERE, null, ex);
        }

        document.close();
    }

    /**
     * ecri un thésaurus en PDF par ordre hiérarchique
     */
    private void writeHieraPDF(ArrayList<Paragraph> paragraphs, String langue, String langue2, boolean isTrad, HashMap<String, ArrayList<String>> idToDoc) {

        ArrayList<SKOSResource> conceptList = xmlDocument.getConceptList();
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(conceptList, sortForHiera(isTrad, langue, langue2, idToNameHashMap, idToChildId, idToDoc, idToMatch, idToGPS, resourceChecked, idToIsTradDiff));

        for (SKOSResource concept : conceptList) {

            boolean isAtRoot = true;

            String conceptID = getIdFromUri(concept.getUri());

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
                if (name == null) {
                    name = "";
                }
                paragraphs.add(new Paragraph(name + " (" + conceptID + ")", termFont));
                String indentation = "";
                writeHieraTermInfo(conceptID, indentation, paragraphs, idToDoc);
                writeHieraTermRecursif(conceptID, indentation, paragraphs, idToDoc);

            }

        }

    }

    /**
     * fonction recursive qui sert a ecrire tout les fils des term
     *
     * @param id
     * @param indentation
     * @param paragraphs
     * @param idToDoc
     */
    private void writeHieraTermRecursif(String id, String indentation, ArrayList<Paragraph> paragraphs, HashMap<String, ArrayList<String>> idToDoc) {

        indentation += ".......";

        ArrayList<String> childList = idToChildId.get(id);
        if (childList == null) {
            return;
        }

        for (String idFils : childList) {
            String name = idToNameHashMap.get(idFils);
            if (name == null) {
                name = "";
            }
            paragraphs.add(new Paragraph(indentation + name + " (" + idFils + ")", textFont));
            writeHieraTermInfo(idFils, indentation, paragraphs, idToDoc);
            writeHieraTermRecursif(idFils, indentation, paragraphs, idToDoc);

        }

    }

    /**
     * ecri les données d'un term pour le format hiérarchique
     *
     * @param key
     * @param indenatation
     * @param paragraphs
     * @param idToDoc
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
                paragraphs.add(new Paragraph(space + doc, hieraInfoFont));
                docWrite++;
            }

        }
        if (docWrite < docCount) {
            for (int i = 0; i < docCount; i++) {
                paragraphs.add(new Paragraph(space + "-", hieraInfoFont));
            }
        }

        //match
        ArrayList<String> matchList = idToMatch.get(key);
        if (matchList != null) {
            for (String match : matchList) {
                paragraphs.add(new Paragraph(space + match, hieraInfoFont));
            }
        }

        String gps = idToGPS.get(key);
        if (gps != null) {
            paragraphs.add(new Paragraph(space + gps, hieraInfoFont));
        }

    }

    /**
     * ecri un thésaurus en PDF par ordre alphabetique
     */
    private void writeAlphabetiquePDF(ArrayList<Paragraph> paragraphs, String langue, String langue2, boolean isTrad) {

        ArrayList<SKOSResource> conceptList = xmlDocument.getConceptList();
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(conceptList, sortAlphabeticInLang(isTrad, langue, langue2, idToNameHashMap, idToIsTrad, resourceChecked));
        for (SKOSResource concept : conceptList) {
            writeTerm(concept, paragraphs, langue, langue2);
        }
    }

    /**
     * ecri les information du ConceptSheme dans le PDF
     */
    private void writeConceptSheme() {

        PdfPTable table = new PdfPTable(2);
        PdfPCell cell1 = new PdfPCell();
        PdfPCell cell2 = new PdfPCell();

        try {

            SKOSResource thesaurus = xmlDocument.getConceptScheme();

            for (SKOSLabel label : thesaurus.getLabelsList()) {
                if (label.getLanguage().equals(codeLang)) {
                    String labelValue = label.getLabel();
                    if (label.getProperty() == SKOSProperty.prefLabel) {
                        cell1.addElement(new Paragraph(labelValue + " (" + codeLang + ")", titleFont));

                    }
                }

            }
            cell1.setBorderWidth(Rectangle.NO_BORDER);
            table.addCell(cell1);

            if (codeLang2 != null && !codeLang2.equals("")) {
                for (SKOSLabel label : thesaurus.getLabelsList()) {
                    if (label.getLanguage().equals(codeLang2)) {
                        String labelValue = label.getLabel();
                        if (label.getProperty() == SKOSProperty.prefLabel) {
                            cell2.addElement(new Paragraph(labelValue + " (" + codeLang2 + ")", titleFont));

                        }
                    }

                }
            }
            cell2.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell2);

            document.add(new Paragraph(thesaurus.getUri(), subTitleFont));
            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

        } catch (DocumentException ex) {
            Logger.getLogger(WritePdf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * ajoute un paragraphe qui decri le term dans le document PDF
     *
     * @param concept
     * @param paragraphs
     */
    private void writeTerm(SKOSResource concept, ArrayList<Paragraph> paragraphs, String langue, String langue2) {

        String id = getIdFromUri(concept.getUri());

        int altLabelCount = 0;

        ArrayList<Integer> tradList = idToIsTrad.get(id);
        if (tradList != null) {
            for (int lab : tradList) {
                if (lab == SKOSProperty.altLabel) {
                    altLabelCount++;
                }
            }
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

                    Chunk chunk = new Chunk(labelValue, termFont);
                    chunk.setLocalDestination(id);
                    paragraphs.add(new Paragraph(chunk));

                } else if (label.getProperty() == SKOSProperty.altLabel && !altIsTrad) {
                    paragraphs.add(new Paragraph("    USE: " + labelValue, textFont));
                }
            }

        }

        paragraphs.add(new Paragraph("    ID: " + id, textFont));

        for (SKOSRelation relation : concept.getRelationsList()) {

            int prop = relation.getProperty();
            String codeRelation;

            switch (prop) {
                case SKOSProperty.broader:
                    codeRelation = "BT";
                    break;
                case SKOSProperty.narrower:
                    codeRelation = "NT";
                    break;
                case SKOSProperty.related:
                    codeRelation = "RT";
                    break;
                case SKOSProperty.relatedHasPart:
                    codeRelation = "RHP";
                    break;
                case SKOSProperty.relatedPartOf:
                    codeRelation = "RPO";
                    break;
                case SKOSProperty.narrowerGeneric:
                    codeRelation = "NTG";
                    break;
                case SKOSProperty.narrowerInstantial:
                    codeRelation = "NTI";
                    break;
                case SKOSProperty.narrowerPartitive:
                    codeRelation = "NTP";
                    break;
                case SKOSProperty.broaderGeneric:
                    codeRelation = "BTG";
                    break;
                case SKOSProperty.broaderInstantial:
                    codeRelation = "BTI";
                    break;
                case SKOSProperty.broaderPartitive:
                    codeRelation = "BTP";
                    break;

                default:
                    continue;

            }
            String key = getIdFromUri(relation.getTargetUri());
            String targetName = idToNameHashMap.get(key);
            if (targetName == null) {
                targetName = key;
            }
            Chunk chunk = new Chunk("    " + codeRelation + ": " + targetName, relationFont);

            chunk.setLocalGoto(getIdFromUri(relation.getTargetUri()));
            paragraphs.add(new Paragraph(chunk));
        }

        for (SKOSDocumentation doc : concept.getDocumentationsList()) {

            if (!doc.getLanguage().equals(langue) && !doc.getLanguage().equals(langue2)) {
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
            int docWrite = 0;

            int prop = doc.getProperty();
            String docTypeName;
            switch (prop) {
                case SKOSProperty.definition:
                    docTypeName = "definition";
                    break;
                case SKOSProperty.scopeNote:
                    docTypeName = "scopeNote";
                    break;
                case SKOSProperty.example:
                    docTypeName = "example";
                    break;
                case SKOSProperty.historyNote:
                    docTypeName = "historyNote";
                    break;
                case SKOSProperty.editorialNote:
                    docTypeName = "editorialNote";
                    break;
                case SKOSProperty.changeNote:
                    docTypeName = "changeNote";
                    break;
                case SKOSProperty.note:
                    docTypeName = "note";
                    break;
                default:
                    docTypeName = "note";
                    break;
            }

            String docText = "";
            boolean docIsTrad = false;
            if (doc.getLanguage().equals(langue)) {
                docText = doc.getText();
            } else {

                if (tradList != null && tradList.contains(SKOSProperty.note)) {

                    if (docCount > docWrite) {
                        docIsTrad = true;
                    }
                    docWrite++;
                }

            }
            if (!docIsTrad) {
                paragraphs.add(new Paragraph("    " + docTypeName + ": " + docText, textFont));
            }

        }

        for (SKOSMatch match : concept.getMatchList()) {
            int prop = match.getProperty();
            String matchTypeName = null;
            switch (prop) {
                case SKOSProperty.exactMatch:
                    matchTypeName = "exactMatch";
                    break;
                case SKOSProperty.closeMatch:
                    matchTypeName = "closeMatch";
                    break;
                case SKOSProperty.broadMatch:
                    matchTypeName = "broadMatch";
                    break;
                case SKOSProperty.relatedMatch:
                    matchTypeName = "relatedMatch";
                    break;
                case SKOSProperty.narrowMatch:
                    matchTypeName = "narrowMatch";
                    break;
            }
            paragraphs.add(new Paragraph("    " + matchTypeName + ": " + match.getValue(), textFont));
        }

        SKOSGPSCoordinates gps = concept.getGPSCoordinates();
        String lat = gps.getLat();
        String lon = gps.getLon();

        if (lat != null && lon != null) {

            paragraphs.add(new Paragraph("    lat: " + lat, textFont));
            paragraphs.add(new Paragraph("    lat: " + lon, textFont));

        }

    }

    /**
     *
     * @return un ByteArrayOutputStream pour télécharcher le pdf
     */
    public ByteArrayOutputStream getOutput() {
        return output;
    }

    public static String getIdFromUri(String uri) {
        if (uri.contains("idg=")) {
            if (uri.contains("&")) {
                uri = uri.substring(uri.indexOf("idg=") + 4, uri.indexOf("&"));
            } else {
                uri = uri.substring(uri.indexOf("idg=") + 4, uri.length());
            }
        } else {
            if (uri.contains("idc=")) {
                if (uri.contains("&")) {
                    uri = uri.substring(uri.indexOf("idc=") + 4, uri.indexOf("&"));
                } else {
                    uri = uri.substring(uri.indexOf("idc=") + 4, uri.length());
                }
            } else {
                if (uri.contains("#")) {
                    uri = uri.substring(uri.indexOf("#") + 1, uri.length());
                } else {
                    uri = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
                }
            }
        }

        StringPlus stringPlus = new StringPlus();
        uri = stringPlus.normalizeStringForIdentifier(uri);
        return uri;
    }
}
