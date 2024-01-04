package fr.cnrs.opentheso.core.exports.pdf.new_export;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Image;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Paragraph;

import com.zaxxer.hikari.HikariDataSource;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.core.exports.UriHelper;
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
import java.util.stream.Collectors;

import static fr.cnrs.opentheso.skosapi.SKOSResource.sortForHiera;


public class WriteHierachiquePDF {

    private String uri;

    private List<SKOSResource> concepts;

    private WritePdfSettings writePdfSettings;

    private HashMap<String, String> labels;
    private HashMap<String, List<String>> gps;
    private HashMap<String, List<String>> idToChildId;
    private HashMap<String, ArrayList<String>> notes;
    private HashMap<String, ArrayList<String>> notesTraduction;
    private HashMap<String, ArrayList<Integer>> notesDiff;
    private HashMap<String, ArrayList<String>> matchs;
    private HashMap<String, ArrayList<NodeImage>> images;
    private ArrayList<String> resourceChecked;

    private UriHelper uriHelper;

    public WriteHierachiquePDF(WritePdfSettings writePdfSettings, SKOSXmlDocument xmlDocument, UriHelper uriHelper) {

        this.writePdfSettings = writePdfSettings;

        labels = new HashMap<>();
        idToChildId = new HashMap<>();
        notes = new HashMap<>();
        notesTraduction = new HashMap<>();
        matchs = new HashMap<>();
        images = new HashMap<>();
        gps = new HashMap<>();
        notesDiff = new HashMap<>();
        resourceChecked = new ArrayList<>();

        uri = xmlDocument.getConceptScheme().getUri();

        concepts = xmlDocument.getConceptList();
        this.uriHelper = uriHelper;
    }

    public void writeHierachiquePDF(HikariDataSource hikariDataSource, ArrayList<Paragraph> paragraphs,
                              ArrayList<Paragraph> paragraphTradList, String codeLanguage1, String codeLanguage2) {

        traitement(hikariDataSource, paragraphs, codeLanguage1, codeLanguage2, false, notes);

        if (StringUtils.isNotEmpty(codeLanguage2)) {
            traitement(hikariDataSource, paragraphTradList, codeLanguage2, codeLanguage1, true, notesTraduction);
        }
    }

    private void traitement(HikariDataSource hikariDataSource, ArrayList<Paragraph> paragraphs, String codeLanguage1,
                            String codeLanguage2, boolean isTrad, HashMap<String, ArrayList<String>> idToDoc) {

        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(concepts, sortForHiera(hikariDataSource, isTrad, codeLanguage1, codeLanguage2, labels,
                idToChildId, idToDoc, matchs, gps, images, resourceChecked, notesDiff));

        for (SKOSResource concept : concepts) {

            boolean isAtRoot = true;
            String conceptID = concept.getIdentifier();//uriHelper.getUriForConcept(concept.getIdentifier(), concept.getArkId(), concept.getArkId());//writePdfSettings.getIdFromUri(concept.getUri());
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
                String name = labels.get(conceptID);
                if (name == null) {
                    name = "";
                }

                Paragraph paragraph = new Paragraph();
                Anchor anchor = new Anchor(name + " (" + conceptID + ")", writePdfSettings.termFont);
                anchor.setReference(uriHelper.getUriForConcept(concept.getIdentifier(), concept.getArkId(), concept.getArkId()));  //uri + "&idc=" + conceptID);
                paragraph.add(anchor);
                paragraphs.add(paragraph);

                String indentation = "";
                addConceptDetails(conceptID, indentation, paragraphs, idToDoc);
                addConcept(conceptID, indentation, paragraphs, idToDoc);
            }
        }
    }


    private void addConcept(String id, String indentation, ArrayList<Paragraph> paragraphs, HashMap<String, ArrayList<String>> idToDoc) {

        indentation += ".......";

        List<String> childList = idToChildId.get(id);
        if (childList == null) {
            return;
        }
        String idArk;
        for (String idFils : childList) {
            String name = labels.get(idFils);
            if (name == null) {
                name = "";
            }

            Paragraph paragraph = new Paragraph();
            Anchor anchor = new Anchor(indentation + name + " (" + idFils + ")", writePdfSettings.textFont);
            idArk = uriHelper.getIdArk(idFils);
            anchor.setReference(uriHelper.getUriForConcept(idFils, idArk, idArk));//uri + "&idc=" + idFils);//uriHelper.getUriForConcept(concept.getIdentifier(), concept.getArkId(), concept.getArkId())//uri + "&idc=" + idFils);
            paragraph.add(anchor);
            paragraphs.add(paragraph);

            addConceptDetails(idFils, indentation, paragraphs, idToDoc);
            addConcept(idFils, indentation, paragraphs, idToDoc);
        }
    }

    private void addConceptDetails(String key, String indentation, ArrayList<Paragraph> paragraphs, HashMap<String,
            ArrayList<String>> idToDoc) {

        String space = getSpace(indentation);
        addNotes(paragraphs, space, idToDoc.get(key), notesDiff.get(key));
        addMatchs(paragraphs, matchs.get(key), space);
        addGpsCoordiantes(paragraphs, gps.get(key), space);
        addImages(paragraphs, images.get(key), indentation);
    }

    private String getSpace(String indentation) {
        String space = "";
        for (int i = 0; i < indentation.length(); i++) {
            space += " ";
        }
        return space;
    }

    private void addNotes(List<Paragraph> paragraphs, String space, ArrayList<String> idToDoc, ArrayList<Integer> idTradDiff) {

        int docCount = 0;
        if (CollectionUtils.isNotEmpty(idTradDiff)) {
            docCount = (int) idTradDiff.stream().filter(traduction -> traduction == SKOSProperty.NOTE).count();
        }

        AtomicInteger docWrite = new AtomicInteger();
        if (CollectionUtils.isNotEmpty(idToDoc)) {
            idToDoc.stream().forEach(document  -> {
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

    private void addMatchs(List<Paragraph> paragraphs, ArrayList<String> matchs, String space) {

        if (CollectionUtils.isNotEmpty(matchs)) {
            matchs.stream().forEach(match -> paragraphs.add(new Paragraph(space + match, writePdfSettings.hieraInfoFont)));
        }
    }

    private void addGpsCoordiantes(List<Paragraph> paragraphs, List<String> gps, String space) {
        if (CollectionUtils.isNotEmpty(gps)) {
            paragraphs.add(new Paragraph(space + "GPS : (" + gps.stream().collect(Collectors.joining(", ")) + ")", writePdfSettings.hieraInfoFont));
        }
    }

    private void addImages(List<Paragraph> paragraphs, ArrayList<NodeImage> images, String indentation) {

        if (CollectionUtils.isNotEmpty(images)) {
            paragraphs.add(new Paragraph(Chunk.NEWLINE));
            images.stream()
                    .forEach(imageElement -> {
                        try {
                            Image image = Image.getInstance(new URL(imageElement.getUri()));
                            image.scaleAbsolute(writePdfSettings.resiseImage(image));
                            paragraphs.add(new Paragraph(new Chunk(image, indentation.length() * (2.9f), 0, true)));
                        } catch (BadElementException | IOException ex) {}
                    });
        }
    }
   
}
