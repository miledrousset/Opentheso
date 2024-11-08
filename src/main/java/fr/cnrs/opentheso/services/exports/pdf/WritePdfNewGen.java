package fr.cnrs.opentheso.services.exports.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;
import fr.cnrs.opentheso.models.exports.UriHelper;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.ArrayList;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;


public class WritePdfNewGen {

    private final ArrayList<Paragraph> paragraphList = new ArrayList<>();
    private final ArrayList<Paragraph> paragraphTradList = new ArrayList<>();


    public byte[] createPdfFile(SKOSXmlDocument xmlDocument, String codeLanguage1,
                                String codeLanguage2, PdfExportType pdfExportType, UriHelper uriHelper) throws DocumentException, IOException {

        Document document = new Document();

        WritePdfSettings writePdfSettings = new WritePdfSettings();

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            preparePdfFile(document, output, xmlDocument, codeLanguage2);

            document.open();

            new WriteConceptShemas().writeConceptSchemas(writePdfSettings, document, xmlDocument, codeLanguage1, codeLanguage2);

            // Préparation des données
            if (pdfExportType == PdfExportType.ALPHABETIQUE) {
                WriteAlphaPDF writeAlphaPDF = new WriteAlphaPDF(writePdfSettings, xmlDocument, uriHelper);
                writeAlphaPDF.writeAlphabetiquePDF(paragraphList, paragraphTradList, codeLanguage1, codeLanguage2);
            } else {
                WriteHierachiquePDF writeHierachiquePDF = new WriteHierachiquePDF(writePdfSettings, xmlDocument, uriHelper);
                writeHierachiquePDF.writeHierachiquePDF(paragraphList, paragraphTradList,
                        codeLanguage1, codeLanguage2);
            }

            createPdfFile(document, codeLanguage2);

            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            if (ObjectUtils.isNotEmpty(document)) {
                document.close();
            }
            return new byte[0];
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
                        + " - " + xmlDocument.getConceptScheme().getThesaurus().getTitle()));
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

                PdfPCell cell1 = new PdfPCell();
                cell1.addElement(paragraphList.get(i));
                cell1.setBorderWidth(Rectangle.NO_BORDER);

                PdfPCell cell2 = new PdfPCell();
                cell2.addElement(paragraphTradList.get(i));
                cell2.setBorder(Rectangle.NO_BORDER);

                table.addCell(cell1);
                table.addCell(cell2);
            }
            document.add(table);
        }
    }
}
