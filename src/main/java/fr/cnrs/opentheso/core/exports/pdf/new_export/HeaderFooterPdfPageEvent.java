package fr.cnrs.opentheso.core.exports.pdf.new_export;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;


class HeaderFooterPdfPageEvent extends PdfPageEventHelper {

    private String thesaurusName;

    public HeaderFooterPdfPageEvent(String thesaurusName) {
        this.thesaurusName = thesaurusName;
    }

    public void onStartPage(PdfWriter writer, Document document) {

        Paragraph header = new Paragraph(thesaurusName);
        header.setAlignment(Element.ALIGN_LEFT);
        float headerX = document.left() + 50;
        float headerY = document.top() + 10;

        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, header, headerX, headerY, 0);
    }

    public void onEndPage(PdfWriter writer, Document document) {
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("https://opentheso2.mom.fr"), 110, 30, 0);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("Page " + document.getPageNumber()), 550, 30, 0);
    }
}
