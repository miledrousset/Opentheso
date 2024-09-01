/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.models.exports.pdf;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
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

import fr.cnrs.opentheso.models.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.models.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.models.skosapi.SKOSLabel;
import fr.cnrs.opentheso.models.skosapi.SKOSMatch;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;
import fr.cnrs.opentheso.models.skosapi.SKOSRelation;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;
import java.net.URL;

import fr.cnrs.opentheso.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

import static fr.cnrs.opentheso.models.skosapi.SKOSResource.sortAlphabeticInLang;
import static fr.cnrs.opentheso.models.skosapi.SKOSResource.sortForHiera;
import java.util.List;


/**
 *
 * @author Quincy
 */
public class WritePdf {

    private SKOSXmlDocument xmlDocument;
    Document document;

    ArrayList<Paragraph> paragraphList = new ArrayList<>();
    ArrayList<Paragraph> paragraphTradList = new ArrayList<>();

    HashMap<String, String> idToNameHashMap;
    HashMap<String, List<String>> idToChildId = new HashMap<>();
    HashMap<String, ArrayList<String>> idToDocumentation = new HashMap<>();
    HashMap<String, ArrayList<String>> idToDocumentation2 = new HashMap<>();
    HashMap<String, ArrayList<String>> idToMatch = new HashMap<>();
    HashMap<String, ArrayList<NodeImage>> idToImg = new HashMap<>();

    HashMap<String, List<String>> idToGPS = new HashMap<>();

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

    public byte[] createPdfFile(HikariDataSource hikariDataSource, SKOSXmlDocument xmlDocument, 
            String codeLang, String codeLang2, int type) {
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

        try ( ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, output);

            document.open();
            writeConceptSheme();

            if (type == 1) {
                writeAlphabetiquePDF(paragraphList, codeLang, codeLang2, false);
            } else if (type == 0) {
                writeHieraPDF(hikariDataSource, paragraphList, codeLang, codeLang2, false, idToDocumentation);
            }

            if (codeLang2 != null && codeLang2.equals("")) {
                for (Paragraph paragraph : paragraphList) {
                    document.add(paragraph);
                }
            } else {
                if (type == 1) {
                    writeAlphabetiquePDF(paragraphTradList, codeLang2, codeLang, true);
                } else if (type == 0) {
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
                document.add(table);
            }

            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            if (document != null) {
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

                Paragraph paragraph = new Paragraph();
                Anchor anchor = new Anchor(name + " (" + conceptID + ")", termFont);
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
     *
     * @param id
     * @param indentation
     * @param paragraphs
     * @param idToDoc
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
            Anchor anchor = new Anchor(indentation + name + " (" + idFils + ")", textFont);
            anchor.setReference(xmlDocument.getConceptScheme().getUri() + "&idc=" + idFils);
            paragraph.add(anchor);
            paragraphs.add(paragraph);

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
                if (lab == SKOSProperty.NOTE) {
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

        if (CollectionUtils.isNotEmpty(idToGPS.get(key))) {
            for (String element : idToGPS.get(key)) {
                paragraphs.add(new Paragraph(space + element, hieraInfoFont));
            }
        }
        
        if (CollectionUtils.isNotEmpty(idToImg.get(key))) {
            paragraphs.add(new Paragraph(Chunk.NEWLINE));
            for (NodeImage nodeImage : idToImg.get(key)) {
                try {
                    Image image = Image.getInstance(new URL(nodeImage.getUri()));
                    image.scaleAbsolute(resiseImage(image));
                    paragraphs.add(new Paragraph(new Chunk(image, indenatation.length()*(2.9f), 20, true)));
                } catch (BadElementException | IOException ex) {}
            }
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
                    if (label.getProperty() == SKOSProperty.PREF_LABEL) {

                        Paragraph paragraph = new Paragraph();
                        Anchor anchor = new Anchor(labelValue + " (" + codeLang + ")", titleFont);
                        anchor.setReference(xmlDocument.getConceptScheme().getUri());
                        paragraph.add(anchor);
                        cell1.addElement(paragraph);
                    }
                }

            }
            cell1.setBorderWidth(Rectangle.NO_BORDER);
            table.addCell(cell1);

            if (codeLang2 != null && !codeLang2.equals("")) {
                for (SKOSLabel label : thesaurus.getLabelsList()) {
                    if (label.getLanguage().equals(codeLang2)) {
                        String labelValue = label.getLabel();
                        if (label.getProperty() == SKOSProperty.PREF_LABEL) {

                            Paragraph paragraph = new Paragraph();
                            Anchor anchor = new Anchor(labelValue + " (" + codeLang2 + ")", titleFont);
                            anchor.setReference(xmlDocument.getConceptScheme().getUri());
                            paragraph.add(anchor);
                            cell2.addElement(paragraph);
                        }
                    }

                }
            }
            cell2.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell2);

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
                if (lab == SKOSProperty.ALT_LABEL) {
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
                        if (tradList.contains(SKOSProperty.PREF_LABEL) && label.getProperty() == SKOSProperty.PREF_LABEL) {
                            prefIsTrad = true;
                        }
                        if (tradList.contains(SKOSProperty.ALT_LABEL) && label.getProperty() == SKOSProperty.ALT_LABEL) {

                            if (altLabelCount > altLabelWrite) {
                                altIsTrad = true;
                            }
                            altLabelWrite++;
                        }
                    }

                    labelValue = "-";

                }

                if (label.getProperty() == SKOSProperty.PREF_LABEL && !prefIsTrad) {
                    
                    Paragraph paragraph = new Paragraph();
                    Anchor anchor = new Anchor(labelValue, termFont);
                    anchor.setReference(xmlDocument.getConceptScheme().getUri() + "&idc=" + id);
                    paragraph.add(anchor);
                    paragraphs.add(paragraph);

                } else if (label.getProperty() == SKOSProperty.ALT_LABEL && !altIsTrad) {
                    paragraphs.add(new Paragraph("    USE: " + labelValue, textFont));
                }
            }

        }

        paragraphs.add(new Paragraph("    ID: " + id, textFont));

        for (SKOSRelation relation : concept.getRelationsList()) {

            int prop = relation.getProperty();
            String codeRelation;

            switch (prop) {
                case SKOSProperty.BROADER:
                    codeRelation = "BT";
                    break;
                case SKOSProperty.NARROWER:
                    codeRelation = "NT";
                    break;
                case SKOSProperty.RELATED:
                    codeRelation = "RT";
                    break;
                case SKOSProperty.RELATED_HAS_PART:
                    codeRelation = "RHP";
                    break;
                case SKOSProperty.RELATED_PART_OF:
                    codeRelation = "RPO";
                    break;
                case SKOSProperty.NARROWER_GENERIC:
                    codeRelation = "NTG";
                    break;
                case SKOSProperty.NARROWER_INSTANTIAL:
                    codeRelation = "NTI";
                    break;
                case SKOSProperty.NARROWER_PARTITIVE:
                    codeRelation = "NTP";
                    break;
                case SKOSProperty.BROADER_GENERIC:
                    codeRelation = "BTG";
                    break;
                case SKOSProperty.BROADER_INSTANTIAL:
                    codeRelation = "BTI";
                    break;
                case SKOSProperty.BROADER_PARTITIVE:
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
                    if (lab == SKOSProperty.NOTE) {
                        docCount++;
                    }
                }
            }
            int docWrite = 0;

            int prop = doc.getProperty();
            String docTypeName;
            switch (prop) {
                case SKOSProperty.DEFINITION:
                    docTypeName = "definition";
                    break;
                case SKOSProperty.SCOPE_NOTE:
                    docTypeName = "scopeNote";
                    break;
                case SKOSProperty.EXAMPLE:
                    docTypeName = "example";
                    break;
                case SKOSProperty.HISTORY_NOTE:
                    docTypeName = "historyNote";
                    break;
                case SKOSProperty.EDITORIAL_NOTE:
                    docTypeName = "editorialNote";
                    break;
                case SKOSProperty.CHANGE_NOTE:
                    docTypeName = "changeNote";
                    break;
                case SKOSProperty.NOTE:
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

                if (tradList != null && tradList.contains(SKOSProperty.NOTE)) {

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
                case SKOSProperty.EXACT_MATCH:
                    matchTypeName = "exactMatch";
                    break;
                case SKOSProperty.CLOSE_MATCH:
                    matchTypeName = "closeMatch";
                    break;
                case SKOSProperty.BROAD_MATCH:
                    matchTypeName = "broadMatch";
                    break;
                case SKOSProperty.RELATED_MATCH:
                    matchTypeName = "relatedMatch";
                    break;
                case SKOSProperty.NARROWER_MATCH:
                    matchTypeName = "narrowMatch";
                    break;
            }
            paragraphs.add(new Paragraph("    " + matchTypeName + ": " + match.getValue(), textFont));
        }

        if (CollectionUtils.isNotEmpty(concept.getGpsCoordinates())) {
            for (SKOSGPSCoordinates element : concept.getGpsCoordinates()) {
                paragraphs.add(new Paragraph("    lat: " + element.getLat(), textFont));
                paragraphs.add(new Paragraph("    long: " + element.getLon(), textFont));
            }
        }
        
        if (CollectionUtils.isNotEmpty(concept.getNodeImage())) {
            paragraphs.add(new Paragraph(Chunk.NEWLINE));
            for(NodeImage nodeImage : concept.getNodeImage()) {
                try {
                    Image image = Image.getInstance(new URL(nodeImage.getUri()));
                    image.scaleAbsolute(resiseImage(image));                
                    paragraphs.add(new Paragraph(new Chunk(image, 11, 20, true)));
                } catch (BadElementException | IOException ex) {}
            }
        }
    }
    
    private Rectangle resiseImage(Image image){
        float width = image.getWidth();
        float height = image.getHeight();
        float rate;
        // Vérification si l'image est horizontale ou verticale
        if (width > height) {
            //L'image est horizontale.
            rate = getRate(width);
        } else {
            //L'image est verticale
            rate = getRate(height);
        }   
        return new Rectangle(width/rate, height/rate);          
    }
    // pour définir la taille souhaitée, 
    // la valeur size/200 est pour obtenir une image de (200x200)
    private float getRate(float size){
        return size/250;
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

        return StringUtils.normalizeStringForIdentifier(uri);
    }
}
