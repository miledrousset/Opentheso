package fr.cnrs.opentheso.core.exports.pdf.new_export;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.skosapi.SKOSProperty;

import java.io.IOException;

public class WritePdfSettings {

    public Font titleFont;
    public Font termFont;
    public Font textFont;
    public Font relationFont;
    public Font hieraInfoFont;

    public WritePdfSettings() throws DocumentException, IOException {

        BaseFont bf = BaseFont.createFont("fonts/FreeSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        this.titleFont = new Font(bf, 20, Font.BOLD);
        this.termFont = new Font(bf, 12, Font.BOLD);
        this.textFont = new Font(bf, 10);
        this.relationFont = new Font(bf, 10, Font.ITALIC);
        this.hieraInfoFont = new Font(bf, 10, Font.ITALIC);
    }

    public String getCodeRelation(int codeRelation) {
        switch (codeRelation) {
            case SKOSProperty.broader:
                return "BT";
            case SKOSProperty.narrower:
                return "NT";
            case SKOSProperty.related:
                return "RT";
            case SKOSProperty.relatedHasPart:
                return "RHP";
            case SKOSProperty.relatedPartOf:
                return "RPO";
            case SKOSProperty.narrowerGeneric:
                return "NTG";
            case SKOSProperty.narrowerInstantial:
                return "NTI";
            case SKOSProperty.narrowerPartitive:
                return "NTP";
            case SKOSProperty.broaderGeneric:
                return "BTG";
            case SKOSProperty.broaderInstantial:
                return "BTI";
            case SKOSProperty.broaderPartitive:
                return "BTP";
            default:
                return "";
        }
    }

    public String getDocTypeName(int property) {
        switch (property) {
            case SKOSProperty.definition:
                return "definition";
            case SKOSProperty.scopeNote:
                return "scopeNote";
            case SKOSProperty.example:
                return "example";
            case SKOSProperty.historyNote:
                return "historyNote";
            case SKOSProperty.editorialNote:
                return "editorialNote";
            case SKOSProperty.changeNote:
                return "changeNote";
            case SKOSProperty.note:
                return "note";
            default:
                return "";
        }
    }

    public String getMatchTypeName(int property) {
        switch (property) {
            case SKOSProperty.exactMatch:
                return "exactMatch";
            case SKOSProperty.closeMatch:
                return "closeMatch";
            case SKOSProperty.broadMatch:
                return "broadMatch";
            case SKOSProperty.relatedMatch:
                return "relatedMatch";
            case SKOSProperty.narrowMatch:
                return "narrowMatch";
            default:
                return "";
        }
    }

    public String getIdFromUri(String uri) {
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

        return new StringPlus().normalizeStringForIdentifier(uri);
    }
    
    
    
    
    
    

    public Rectangle resiseImage(Image image){
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
}
