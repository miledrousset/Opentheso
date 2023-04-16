package fr.cnrs.opentheso.core.exports.pdf.new_export;

import com.itextpdf.text.*;
import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

import static fr.cnrs.opentheso.skosapi.SKOSResource.sortForHiera;


public class WriteHierachiquePDF {

    private WritePdfSettings writePdfSettings;


    public WriteHierachiquePDF() throws DocumentException, IOException {
        writePdfSettings = new WritePdfSettings();
    }

    public void writeHieraPDF(SKOSXmlDocument xmlDocument,
                              HikariDataSource hikariDataSource,
                              ArrayList<Paragraph> paragraphs,
                              String langue,
                              String langue2,
                              boolean isTrad,
                              HashMap<String, ArrayList<String>> idToDoc,
                              HashMap<String, String> idToNameHashMap,
                              HashMap<String, List<String>> idToChildId,
                              HashMap<String, ArrayList<String>> idToMatch,
                              HashMap<String, String> idToGPS,
                              HashMap<String, ArrayList<NodeImage>> idToImg,
                              ArrayList<String> resourceChecked,
                              HashMap<String, ArrayList<Integer>> idToIsTradDiff) {

        ArrayList<SKOSResource> conceptList = xmlDocument.getConceptList();
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(conceptList, sortForHiera(hikariDataSource, isTrad, langue, langue2, idToNameHashMap,
                idToChildId, idToDoc, idToMatch, idToGPS, idToImg, resourceChecked, idToIsTradDiff));

        for (SKOSResource concept : conceptList) {
            boolean isAtRoot = true;
            String conceptID = writePdfSettings.getIdFromUri(concept.getUri());
            Iterator i = idToChildId.keySet().iterator();
            ArrayList<String> valeur;
            while (i.hasNext()) {
                String clef = (String) i.next();
                valeur = (ArrayList<String>) idToChildId.get(clef);

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

                Anchor anchor = new Anchor(name + " (" + conceptID + ")", writePdfSettings.termFont);
                anchor.setReference(xmlDocument.getConceptScheme().getUri() + "&idc=" + conceptID);
                paragraphs.add(new Paragraph(anchor));

                String indentation = "";
                writeHieraTermInfo(conceptID, indentation, paragraphs, idToDoc, idToIsTradDiff,
                        idToMatch, idToGPS, idToImg);
                writeHieraTermRecursif(xmlDocument, conceptID, indentation, paragraphs, idToDoc,
                        idToIsTradDiff, idToMatch, idToGPS, idToImg, idToChildId, idToNameHashMap);
            }
        }
    }

    /**
     * fonction recursive qui sert a ecrire tout les fils des term
     */
    private void writeHieraTermRecursif(SKOSXmlDocument xmlDocument, String id,
                                        String indentation, ArrayList<Paragraph> paragraphs,
                                        HashMap<String, ArrayList<String>> idToDoc,
                                        HashMap<String, ArrayList<Integer>> idToIsTradDiff,
                                        HashMap<String, ArrayList<String>> idToMatch,
                                        HashMap<String, String> idToGPS,
                                        HashMap<String, ArrayList<NodeImage>> idToImg,
                                        HashMap<String, List<String>> idToChildId,
                                        HashMap<String, String> idToNameHashMap) {

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
            anchor.setReference(xmlDocument.getConceptScheme().getUri() + "&idc=" + idFils);
            paragraph.add(anchor);
            paragraphs.add(paragraph);

            writeHieraTermInfo(idFils, indentation, paragraphs, idToDoc, idToIsTradDiff, idToMatch, idToGPS, idToImg);
            writeHieraTermRecursif(xmlDocument, idFils, indentation, paragraphs, idToDoc, idToIsTradDiff,
                    idToMatch, idToGPS, idToImg, idToChildId, idToNameHashMap);
        }
    }

    /**
     * ecri les données d'un term pour le format hiérarchique
     */
    private void writeHieraTermInfo(String key,
                                    String indenatation,
                                    ArrayList<Paragraph> paragraphs,
                                    HashMap<String, ArrayList<String>> idToDoc,
                                    HashMap<String, ArrayList<Integer>> idToIsTradDiff,
                                    HashMap<String, ArrayList<String>> idToMatch,
                                    HashMap<String, String> idToGPS,
                                    HashMap<String, ArrayList<NodeImage>> idToImg) {

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
                paragraphs.add(new Paragraph(space + doc, writePdfSettings.hieraInfoFont));
                docWrite++;
            }

        }
        if (docWrite < docCount) {
            for (int i = 0; i < docCount; i++) {
                paragraphs.add(new Paragraph(space + "-", writePdfSettings.hieraInfoFont));
            }
        }

        //match
        if (CollectionUtils.isNotEmpty(idToMatch.get(key))) {
            for (String match : idToMatch.get(key)) {
                paragraphs.add(new Paragraph(space + match, writePdfSettings.hieraInfoFont));
            }
        }

        if (StringUtils.isNotEmpty(idToGPS.get(key))) {
            paragraphs.add(new Paragraph(space + idToGPS.get(key), writePdfSettings.hieraInfoFont));
        }

        if (CollectionUtils.isNotEmpty(idToImg.get(key))) {
            for (NodeImage nodeImage : idToImg.get(key)) {
                try {
                    Image image = Image.getInstance(new URL(nodeImage.getUri()));
                    image.scaleAbsolute(writePdfSettings.resiseImage(image));
                    paragraphs.add(new Paragraph(new Chunk(image, indenatation.length()*(2.9f), 20, true)));
                } catch (BadElementException | IOException ex) {}
            }
        }
    }
}
