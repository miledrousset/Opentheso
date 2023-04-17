package fr.cnrs.opentheso.core.exports.pdf.new_export;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Image;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Paragraph;

import com.zaxxer.hikari.HikariDataSource;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.cnrs.opentheso.skosapi.SKOSResource.sortForHiera;


public class WriteHierachiquePDF {

    private String uri;

    private HashMap<String, String> idToNameHashMap;
    private HashMap<String, List<String>> idToChildId;
    private HashMap<String, ArrayList<String>> idToMatch;
    private HashMap<String, ArrayList<NodeImage>> idToImg;
    private HashMap<String, ArrayList<Integer>> idToIsTradDiff;
    private HashMap<String, ArrayList<String>> idToDoc;
    private HashMap<String, String> idToGPS;

    private ArrayList<String> resourceChecked;
    private List<SKOSResource> conceptList;

    private WritePdfSettings writePdfSettings;



    public WriteHierachiquePDF(WritePdfSettings writePdfSettings, SKOSXmlDocument xmlDocument) {

        this.writePdfSettings = writePdfSettings;

        idToNameHashMap = new HashMap<>();
        idToChildId = new HashMap<>();
        idToMatch = new HashMap<>();
        idToDoc = new HashMap<>();
        idToImg = new HashMap<>();
        idToGPS = new HashMap<>();
        idToIsTradDiff = new HashMap<>();
        resourceChecked = new ArrayList<>();
        uri = xmlDocument.getConceptScheme().getUri();
        conceptList = xmlDocument.getConceptList();
    }

    public void writeHieraPDF(HikariDataSource hikariDataSource, ArrayList<Paragraph> paragraphs,
                              ArrayList<Paragraph> paragraphTradList, String codeLanguage1, String codeLanguage2) {

        traitement(hikariDataSource, paragraphs, codeLanguage1, codeLanguage2, false);

        if (StringUtils.isNotEmpty(codeLanguage2)) {
            traitement(hikariDataSource, paragraphTradList, codeLanguage2, codeLanguage1, true);
        }
    }

    private void traitement(HikariDataSource hikariDataSource, ArrayList<Paragraph> paragraphs, String langue,
                            String langue2, boolean isTrad) {

        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(conceptList, sortForHiera(hikariDataSource, isTrad, langue, langue2, idToNameHashMap,
                idToChildId, idToDoc, idToMatch, idToGPS, idToImg, resourceChecked, idToIsTradDiff));

        for (SKOSResource concept : conceptList) {
            boolean isAtRoot = true;
            String conceptID = writePdfSettings.getIdFromUri(concept.getUri());
            Iterator i = idToChildId.keySet().iterator();
            while (i.hasNext()) {
                ArrayList<String> valeur = (ArrayList<String>) idToChildId.get((String) i.next());
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
                anchor.setReference(uri + "&idc=" + conceptID);
                paragraph.add(anchor);
                paragraphs.add(paragraph);

                String indentation = "";
                writeHieraTermInfo(conceptID, indentation, paragraphs);
                writeHieraTermRecursif(conceptID, indentation, paragraphs);
            }
        }
    }


    private void writeHieraTermRecursif(String id, String indentation, ArrayList<Paragraph> paragraphs) {

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
            anchor.setReference(uri + "&idc=" + idFils);
            paragraph.add(anchor);
            paragraphs.add(paragraph);

            writeHieraTermInfo(idFils, indentation, paragraphs);
            writeHieraTermRecursif(idFils, indentation, paragraphs);
        }
    }

    private void writeHieraTermInfo(String key, String indentation, List<Paragraph> paragraphs) {

        String space = getSpace(indentation);
        addDocuments(paragraphs, key, space);
        addMatchs(paragraphs, key, space);
        addGpsCoordiantes(paragraphs, key, space);
        addImages(paragraphs, key, indentation);
    }

    private String getSpace(String indentation) {
        String space = "";
        for (int i = 0; i < indentation.length(); i++) {
            space += " ";
        }
        return space;
    }

    private void addDocuments(List<Paragraph> paragraphs, String key, String space) {

        int docCount = 0;
        if (CollectionUtils.isNotEmpty(idToIsTradDiff.get(key))) {
            docCount = (int) idToIsTradDiff.get(key).stream().filter(traduction -> traduction == SKOSProperty.note).count();
        }

        AtomicInteger docWrite = new AtomicInteger();
        if (CollectionUtils.isNotEmpty(idToDoc.get(key))) {
            idToDoc.get(key).stream().forEach(document  -> {
                paragraphs.add(new Paragraph(space + document, writePdfSettings.hieraInfoFont));
                docWrite.getAndIncrement();
            });
        }

        if (docWrite.get() < docCount) {
            for (int i = 0; i < docCount; i++) {
                paragraphs.add(new Paragraph(space + "-", writePdfSettings.hieraInfoFont));
            }
        }
    }

    private void addMatchs(List<Paragraph> paragraphs, String key, String space) {

        if (CollectionUtils.isNotEmpty(idToMatch.get(key))) {
            idToMatch.get(key).stream().forEach(match -> paragraphs.add(
                    new Paragraph(space + match, writePdfSettings.hieraInfoFont)));
        }
    }

    private void addGpsCoordiantes(List<Paragraph> paragraphs, String key, String space) {

        if (StringUtils.isNotEmpty(idToGPS.get(key))) {
            paragraphs.add(new Paragraph(space + idToGPS.get(key), writePdfSettings.hieraInfoFont));
        }
    }

    private void addImages(List<Paragraph> paragraphs, String key, String indentation) {

        if (CollectionUtils.isNotEmpty(idToImg.get(key))) {
            idToImg.get(key).stream()
                    .forEach(imageElement -> {
                        try {
                            Image image = Image.getInstance(new URL(imageElement.getUri()));
                            image.scaleAbsolute(writePdfSettings.resiseImage(image));
                            paragraphs.add(new Paragraph(new Chunk(image, indentation.length() * (2.9f), 20, true)));
                        } catch (BadElementException | IOException ex) {}
                    });
        }
    }
}
