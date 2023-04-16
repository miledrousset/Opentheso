package fr.cnrs.opentheso.core.exports.pdf.new_export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.zaxxer.hikari.HikariDataSource;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.cnrs.opentheso.core.exports.pdf.WritePdf;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;



public class WritePdfNewGen {

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


    public byte[] createPdfFile(HikariDataSource hikariDataSource, SKOSXmlDocument xmlDocument, String codeLang,
                                String codeLang2, PdfExportType pdfExportType) throws DocumentException, IOException {

        Document document = new Document();
        idToNameHashMap = new HashMap<>();

        WritePdfSettings writePdfSettings = new WritePdfSettings();
        WriteAlphaPDF writeAlphaPDF = new WriteAlphaPDF();
        WriteHierachiquePDF writeHierachiquePDF = new WriteHierachiquePDF();

        try ( ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            preparePdfFile(document, output, xmlDocument, codeLang2);

            document.open();

            new WriteConceptShemas().writeConceptSchemas(writePdfSettings, document, xmlDocument, codeLang, codeLang2);

            // Préparation des données
            if (pdfExportType == PdfExportType.ALPHABETIQUE) {
                alphabetiqueMode(writeAlphaPDF, xmlDocument, codeLang, codeLang2);
            } else {
                hiarchiqueMode(writeHierachiquePDF, hikariDataSource, xmlDocument, codeLang, codeLang2);
            }

            createPdfFile(document, codeLang2);

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

    private void hiarchiqueMode(WriteHierachiquePDF writeHierachiquePDF, HikariDataSource hikariDataSource,
                                SKOSXmlDocument xmlDocument, String codeLanguage1, String codeLanguage2) {

        writeHierachiquePDF.writeHieraPDF(xmlDocument, hikariDataSource, paragraphList,
                codeLanguage1, codeLanguage2, false, idToDocumentation,
                idToNameHashMap, idToChildId, idToMatch, idToGPS, idToImg, resourceChecked, idToIsTradDiff);

        if (StringUtils.isNotEmpty(codeLanguage2)) {
            writeHierachiquePDF.writeHieraPDF(xmlDocument, hikariDataSource, paragraphTradList,
                    codeLanguage2, codeLanguage1, true, idToDocumentation2,
                    idToNameHashMap, idToChildId, idToMatch, idToGPS, idToImg, resourceChecked, idToIsTradDiff);
        }
    }

    private void alphabetiqueMode(WriteAlphaPDF writeAlphaPDF, SKOSXmlDocument xmlDocument, String codeLanguage1,
                                  String codeLanguage2) throws BadElementException, IOException {

        writeAlphaPDF.writeAlphabetiquePDF(xmlDocument, paragraphList, codeLanguage1, codeLanguage2, false,
                idToNameHashMap, idToIsTrad, resourceChecked);

        if (StringUtils.isNotEmpty(codeLanguage2)) {
            writeAlphaPDF.writeAlphabetiquePDF(xmlDocument, paragraphTradList, codeLanguage2, codeLanguage1, true,
                    idToNameHashMap, idToIsTrad, resourceChecked);
        }
    }

    private void preparePdfFile(Document document, ByteArrayOutputStream output, SKOSXmlDocument xmlDocument,
                                String codeLang2) throws DocumentException {

        PdfWriter writer = PdfWriter.getInstance(document, output);

        if (StringUtils.isNotEmpty(codeLang2)) {
            document.setPageSize(PageSize.LETTER.rotate());
        }

        // Ajout de l'entête et pied de page
        writer.setPageEvent(new HeaderFooterPdfPageEvent(
                xmlDocument.getConceptScheme().getThesaurus().getId_thesaurus()
                        + " - " + xmlDocument.getConceptScheme().getThesaurus().getTitle()
                        + " ( " + xmlDocument.getConceptScheme().getThesaurus().getLanguage() + " )"));
    }

    private void createPdfFile(Document document, String language2) throws DocumentException {
        if (StringUtils.isBlank(language2)) {
            for (Paragraph paragraph : paragraphList) {
                document.add(paragraph);
            }
        } else {
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
    }
}
