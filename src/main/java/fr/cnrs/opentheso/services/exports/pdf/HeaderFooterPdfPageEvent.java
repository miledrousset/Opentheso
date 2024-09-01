package fr.cnrs.opentheso.services.exports.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;


class HeaderFooterPdfPageEvent extends PdfPageEventHelper {

    private final String thesaurusName;

    public HeaderFooterPdfPageEvent(String thesaurusName) {
        this.thesaurusName = thesaurusName;
    }

    public void onStartPage(PdfWriter writer, Document document) {
        Paragraph header = new Paragraph(thesaurusName);
        header.setAlignment(Element.ALIGN_LEFT);
    }

    public void onEndPage(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Page " + document.getPageNumber()), 550, 30, 0);
    }
}
