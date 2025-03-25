package fr.cnrs.opentheso.services.exports.pdf;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Image;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Paragraph;

import fr.cnrs.opentheso.models.nodes.NodeImage;
import fr.cnrs.opentheso.models.exports.UriHelper;
import fr.cnrs.opentheso.models.skosapi.SKOSProperty;
import fr.cnrs.opentheso.models.skosapi.SKOSResource;
import fr.cnrs.opentheso.models.skosapi.SKOSXmlDocument;
import fr.cnrs.opentheso.repositories.TermHelper;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static fr.cnrs.opentheso.models.skosapi.SKOSResource.sortForHiera;


@Service
public class WriteHierachiquePDF {

    @Autowired
    private UriHelper uriHelper;

    @Autowired
    private TermHelper termHelper;

    private boolean isToogleExportImage;

    public void writeHierachiquePDF(List<Paragraph> paragraphs, List<Paragraph> paragraphTradList, String codeLanguage1,
                                    String codeLanguage2, WritePdfSettings writePdfSettings, SKOSXmlDocument xmlDocument, boolean isToogleExportImage) {
        this.isToogleExportImage = isToogleExportImage;
        HashMap<String, String> labels = new HashMap<>();
        HashMap<String, List<String>> idToChildId = new HashMap<>();
        HashMap<String, ArrayList<String>> notes = new HashMap<>();
        HashMap<String, ArrayList<String>> notesTraduction = new HashMap<>();
        HashMap<String, ArrayList<String>> matchs = new HashMap<>();
        HashMap<String, ArrayList<NodeImage>> images = new HashMap<>();
        HashMap<String, List<String>> gps = new HashMap<>();
        HashMap<String, ArrayList<Integer>> notesDiff = new HashMap<>();
        ArrayList<String> resourceChecked = new ArrayList<>();

        List<SKOSResource> concepts = xmlDocument.getConceptList();

        traitement(paragraphs, codeLanguage1, codeLanguage2, false, notes, concepts, labels, idToChildId,
                writePdfSettings, gps, matchs, images, notesDiff, resourceChecked);

        if (StringUtils.isNotEmpty(codeLanguage2)) {
            traitement(paragraphTradList, codeLanguage2, codeLanguage1, true, notesTraduction, concepts, labels,
                    idToChildId, writePdfSettings, gps, matchs, images, notesDiff, resourceChecked);
        }
    }

    private void traitement(List<Paragraph> paragraphs, String codeLanguage1, String codeLanguage2, boolean isTrad,
                            HashMap<String, ArrayList<String>> idToDoc, List<SKOSResource> concepts,
                            HashMap<String, String> labels, HashMap<String, List<String>> idToChildId,
                            WritePdfSettings writePdfSettings, HashMap<String, List<String>> gps,
                            HashMap<String, ArrayList<String>> matchs, HashMap<String, ArrayList<NodeImage>> images,
                            HashMap<String, ArrayList<Integer>> notesDiff, ArrayList<String> resourceChecked) {

        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(concepts, sortForHiera(isTrad, codeLanguage1, codeLanguage2, labels,
                idToChildId, idToDoc, matchs, gps, images, resourceChecked, notesDiff, termHelper));

        for (SKOSResource concept : concepts) {

            boolean isAtRoot = true;
            String conceptID = concept.getIdentifier();
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
                addConceptDetails(conceptID, indentation, paragraphs, idToDoc, writePdfSettings, gps, images, matchs, notesDiff);
                addConcept(conceptID, indentation, paragraphs, idToDoc, labels, idToChildId, writePdfSettings, gps, images, matchs, notesDiff);
            }
        }
    }


    private void addConcept(String id, String indentation, List<Paragraph> paragraphs, HashMap<String, ArrayList<String>> idToDoc,
                            HashMap<String, String> labels, HashMap<String, List<String>> idToChildId,
                            WritePdfSettings writePdfSettings, HashMap<String, List<String>> gps,
                            HashMap<String, ArrayList<NodeImage>> images, HashMap<String, ArrayList<String>> matchs,
                            HashMap<String, ArrayList<Integer>> notesDiff) {

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
            anchor.setReference(uriHelper.getUriForConcept(idFils, idArk, idArk));
            paragraph.add(anchor);
            paragraphs.add(paragraph);

            addConceptDetails(idFils, indentation, paragraphs, idToDoc, writePdfSettings, gps, images, matchs, notesDiff);
            addConcept(idFils, indentation, paragraphs, idToDoc, labels, idToChildId, writePdfSettings, gps, images, matchs, notesDiff);
        }
    }

    private void addConceptDetails(String key, String indentation, List<Paragraph> paragraphs, HashMap<String,
            ArrayList<String>> idToDoc, WritePdfSettings writePdfSettings, HashMap<String, List<String>> gps,
            HashMap<String, ArrayList<NodeImage>> images, HashMap<String, ArrayList<String>> matchs,
            HashMap<String, ArrayList<Integer>> notesDiff) {

        String space = getSpace(indentation);
        addNotes(paragraphs, space, idToDoc.get(key), notesDiff.get(key), writePdfSettings);
        addMatchs(paragraphs, matchs.get(key), space, writePdfSettings);
        addGpsCoordiantes(paragraphs, gps.get(key), space, writePdfSettings);
        if(isToogleExportImage) {
            addImages(paragraphs, images.get(key), indentation, writePdfSettings);
        }
    }

    private String getSpace(String indentation) {
        String space = "";
        for (int i = 0; i < indentation.length(); i++) {
            space += " ";
        }
        return space;
    }

    private void addNotes(List<Paragraph> paragraphs, String space, ArrayList<String> idToDoc, ArrayList<Integer> idTradDiff, WritePdfSettings writePdfSettings) {

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

    private void addMatchs(List<Paragraph> paragraphs, ArrayList<String> matchs, String space, WritePdfSettings writePdfSettings) {

        if (CollectionUtils.isNotEmpty(matchs)) {
            matchs.stream().forEach(match -> paragraphs.add(new Paragraph(space + match, writePdfSettings.hieraInfoFont)));
        }
    }

    private void addGpsCoordiantes(List<Paragraph> paragraphs, List<String> gps, String space, WritePdfSettings writePdfSettings) {
        if (CollectionUtils.isNotEmpty(gps)) {
            paragraphs.add(new Paragraph(space + "GPS : (" + gps.stream().collect(Collectors.joining(", ")) + ")", writePdfSettings.hieraInfoFont));
        }
    }

    private void addImages(List<Paragraph> paragraphs, List<NodeImage> images, String indentation, WritePdfSettings writePdfSettings) {
        if (CollectionUtils.isNotEmpty(images)) {
            paragraphs.add(new Paragraph(Chunk.NEWLINE));
            for (NodeImage imageElement : images) {
                if (imageElement.getUri() != null && !imageElement.getUri().isEmpty()) {
                    try {
                        // Téléchargement de l'image
                        URL imageUrl = new URL(imageElement.getUri());
                        Image image = Image.getInstance(imageUrl);

                        // Redimensionnement
                        float scaleFactor = writePdfSettings.resiseImage(image);
                        if (scaleFactor > 0) {
                            image.scaleAbsolute(image.getWidth() / scaleFactor, image.getHeight() / scaleFactor);
                            // Ajout de l'image au paragraphe avec indentation
                            paragraphs.add(new Paragraph(new Chunk(image, indentation.length() * (2.9f), 0, true)));
                        } else {
                            paragraphs.add(new Paragraph("Erreur de redimensionnement de l'image : " + imageElement.getUri()));
                        }
                    } catch (BadElementException | IOException ex) {
                        paragraphs.add(new Paragraph("Erreur de téléchargement de l'image (image vide) : " + imageElement.getUri()));
                    }
                } else {
                    paragraphs.add(new Paragraph("Image invalide : " + imageElement.getUri()));
                }
            }
        }
    }

    /*
    private void addImages(List<Paragraph> paragraphs, ArrayList<NodeImage> images, String indentation, WritePdfSettings writePdfSettings) {

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
    }*/
   
}
