package fr.cnrs.opentheso.client.alignement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.models.alignment.SelectedResource;
import lombok.Data;


@Data
public class GettyAATHelper {

    private StringBuffer messages = new StringBuffer();
    // les informations récupérées de Wikidata
    private ArrayList<SelectedResource> resourceAATTraductions;
    private ArrayList<SelectedResource> resourceAATDefinitions;
    private ArrayList<SelectedResource> resourceAATImages;

    // Alignement du thésaurus vers la source Wikidata en Sparql et en retour du Json
    public ArrayList<NodeAlignment> queryAAT(String idC, String idTheso, String lexicalValue, String query, String source) {

        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }

        ArrayList<NodeAlignment> listeAlign;

        try {
            lexicalValue = URLEncoder.encode(lexicalValue, "UTF-8");
            query = query.replace("##value##", lexicalValue);
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            if (conn.getResponseCode() != 200) {
                messages.append(conn.getResponseMessage());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String xmlRecord = "";
            while ((output = br.readLine()) != null) {
                xmlRecord += output;
            }
            conn.disconnect();

            listeAlign = getValues(xmlRecord, idC, idTheso, source);
            br.close();
        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;
        }
        return listeAlign;
    }

    private ArrayList<NodeAlignment> getValues(String xmlDatas,
            String idC, String idTheso, String source) {

        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();

        String uri = "http://vocab.getty.edu/page/aat/";

        try {
            String localName = "";
            String text;
            String originalText;
            String id;
            //    try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader r = factory.createXMLStreamReader(new StringReader(xmlDatas));
            NodeAlignment nodeAlignment = new NodeAlignment();
            while (r.hasNext()) {
                int event = r.next();
                if (event == r.START_ELEMENT) {
                    if (r.hasName()) {
                        localName = r.getLocalName();
                    }
                }
                if (event == r.CHARACTERS) {
                    // le term dans la langue source du Getty
                    if (localName.equalsIgnoreCase("preferred_term")) {
                        originalText = new String(r.getTextCharacters(), r.getTextStart(), r.getTextLength());
                        if (!originalText.trim().isEmpty()) {
                            nodeAlignment.setDef_target(originalText);
                        }
                    }
                    // uri du concept
                    if (localName.equalsIgnoreCase("subject_id")) {
                        id = new String(r.getTextCharacters(), r.getTextStart(), r.getTextLength());
                        if (!id.trim().isEmpty()) {
                            nodeAlignment.setUri_target(uri + id);
                        }
                    }
                    // le texte recherché avec la langue en cours 
                    if (localName.equalsIgnoreCase("term")) {
                        text = new String(r.getTextCharacters(), r.getTextStart(), r.getTextLength());
                        if (!text.trim().isEmpty()) {
                            nodeAlignment.setConcept_target(text);
                        }
                    }
                }
                if (event == r.END_ELEMENT) {
                    if (r.hasName()) {
                        localName = r.getLocalName();
                    }
                    if (localName.equalsIgnoreCase("subject")) {
                        nodeAlignment.setInternal_id_concept(idC);
                        nodeAlignment.setInternal_id_thesaurus(idTheso);
                        nodeAlignment.setThesaurus_target(source);
                        listAlignValues.add(nodeAlignment);
                        nodeAlignment = new NodeAlignment();
                    }
                }
            }

        } catch (XMLStreamException ex) {
            messages.append(ex.toString());
        }
        return listAlignValues;
    }

    public String getMessages() {
        return messages.toString();
    }
}
