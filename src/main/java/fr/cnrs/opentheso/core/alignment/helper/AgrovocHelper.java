/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.imports.rdf4j.nouvelle.ReadRDF4JNewGen;
import fr.cnrs.opentheso.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.skosapi.SKOSLabel;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import org.eclipse.rdf4j.rio.RDFFormat;

import javax.net.ssl.HttpsURLConnection;


/**
 *
 * @author miled.rousset
 */
public class AgrovocHelper {

    private StringBuffer messages;
    // private ArrayList<NodeAlignment> listAlignValues;

    // les informations récupérées de Wikidata
    private ArrayList<SelectedResource> resourceTraductions;
    private ArrayList<SelectedResource> resourceDefinitions;
    private ArrayList<SelectedResource> resourceImages;

    public AgrovocHelper() {
        messages = new StringBuffer();
    }

    /**
     * Alignement du thésaurus vers la source Wikidata en Sparql et en retour du
     * Json
     *
     * @param idC
     * @param idTheso
     * @param value
     * @param lang
     * @param query
     * @param source
     * @return
     */
    public ArrayList<NodeAlignment> queryAgrovoc(String idC, String idTheso,
            String value, String lang,
            String query, String source) {
        ArrayList<NodeAlignment> listeAlign;

        if (query.trim().equals("")) {
            return null;
        }
        if (value.trim().equals("")) {
            return null;
        }

        // préparation de la valeur à rechercher 
        String newValue = "";
        String values[] = value.split(" ");
        for (String value1 : values) {
            if(newValue.isEmpty()) {
                newValue = value1 + "*";
            } else {
                newValue = newValue + value1 + "*";
            }
        }
        
        try {
            value = URLEncoder.encode(value, "UTF-8");
            query = query.replace("##lang##", lang);
            query = query.replace("##value##", newValue);
            query = query.replace("http://", "https://");
            URL url = new URL(query);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                messages.append("Failed : HTTP error code : ");
                messages.append(conn.getResponseCode());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String records = "";
            while ((output = br.readLine()) != null) {
                records += output;
            }
            byte[] bytes = records.getBytes();
            records = new String(bytes, Charset.forName("UTF-8"));
            conn.disconnect();
            listeAlign = getValues(value, records, idC, idTheso, source);
        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;
        }
        return listeAlign;
    }

    private ArrayList<NodeAlignment> getValues(
            String value,
            String jsonDatas,
            String idC, String idTheso, String source) {
        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();

        JsonArray jsonArray;
        JsonObject jsonObject;
        
        JsonObject jb;
        
        String title;
        String definition;
        String uri;
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonDatas))) {

            jsonObject = jsonReader.readObject();

            jsonArray = jsonObject.getJsonArray("results");//.getString("string");
            for (int i = 0; i < jsonArray.size(); ++i) {
                jb = jsonArray.getJsonObject(i);
                uri = jb.getString("uri");
                title = jb.getString("prefLabel");
                try {
                    definition = jb.getString("altLabel");
                    title = title + " (" + definition + ")";
                } catch (Exception e) {
                }

//                uri = uri.replaceAll("http://", "https://");

                NodeAlignment na = new NodeAlignment();
                //si le titre est équivalent, on le place en premier
                if (value.trim().equalsIgnoreCase(title.trim())) {
                    na.setConcept_target(title);
                    na.setDef_target("");
                    na.setInternal_id_concept(idC);
                    na.setInternal_id_thesaurus(idTheso);
                    na.setThesaurus_target(source);
                    na.setUri_target(uri);
                    listAlignValues.add(0, na);
                } else {
                    na.setConcept_target(title);
                    na.setDef_target("");
                    na.setInternal_id_concept(idC);
                    na.setInternal_id_thesaurus(idTheso);
                    na.setThesaurus_target(source);
                    na.setUri_target(uri);
                    listAlignValues.add(na);
                }
            }
        } catch (Exception e) {
            messages.append(e.toString());
            return null;
        }
        return listAlignValues;
    }

    /**
     * Cette fonction permet de récupérer les options de Wikidata Images,
     * alignements, traductions....ource
     *
     * @param selectedNodeAlignment
     * @param selectedOptions
     * @param thesaurusUsedLanguageWithoutCurrentLang
     */
    public void setOptions(NodeAlignment selectedNodeAlignment, List<String> selectedOptions,
                           List<String> thesaurusUsedLanguageWithoutCurrentLang, String currentLang) {

        if (selectedNodeAlignment == null) {
            return;
        }

        // uri traductions
        // après la récupération, on peut afficher le contenue à la demande.
        // http://aims.fao.org/aos/agrovoc/c_6077.rdf


        // https://www.eionet.europa.eu/gemet/getAllTranslationsForConcept?concept_uri=http://www.eionet.europa.eu/gemet/concept/7769&property_uri=http://www.w3.org/2004/02/skos/core%23prefLabel
        String uri = selectedNodeAlignment.getUri_target().trim()+ ".rdf";

        CurlHelper curlHelper = new CurlHelper();
        curlHelper.setHeader1("Accept");
        curlHelper.setHeader2("application/rdf+xml");

        String datas = curlHelper.getDatasFromUriHttp(uri);

        for (String selectedOption : selectedOptions) {
            switch (selectedOption) {
                case "langues":
                    resourceTraductions = getTraductions(datas, thesaurusUsedLanguageWithoutCurrentLang, currentLang);
                    break;
            /*    case "notes":
                    resourceDefinitions = resourceDefinitions;//getDescriptions(datas, thesaurusUsedLanguage);
                    break;*/
                case "images":
                    resourceImages = getImages(datas);
                    break;
            }
        }
    }

    /**
     * récupération des traductions
     *
     * @param xmlDatas
     * @param languages
     * @return
     */
    private ArrayList<SelectedResource> getTraductions(String xmlDatas, List<String> languages, String currentLang) {

        ArrayList<SelectedResource> traductions = new ArrayList<>();
        ArrayList<SelectedResource> descriptions = new ArrayList<>();        

        String lang;
        String value;

        try {
            InputStream inputStream = new ByteArrayInputStream(xmlDatas.getBytes("UTF-8"));
            SKOSXmlDocument sxd = new ReadRDF4JNewGen().readRdfFlux(inputStream, RDFFormat.RDFXML, currentLang);

            for (SKOSResource resource : sxd.getConceptList()) {
                for(SKOSLabel label : resource.getLabelsList()) {
                    switch (label.getProperty()) {
                        case SKOSProperty.prefLabel:
                            lang = label.getLanguage();
                            value = label.getLabel();

                            if(lang == null || value == null || lang.isEmpty() || value.isEmpty())  continue;

                            if(languages.contains(lang)) {
                                SelectedResource selectedResource = new SelectedResource();
                                selectedResource.setIdLang(lang);
                                selectedResource.setGettedValue(value);
                                traductions.add(selectedResource);
                            }
                            break;
                        default:
                            break;
                    }
                }
                for(SKOSDocumentation sd : resource.getDocumentationsList()) {
                    if(sd.getProperty() == SKOSProperty.definition) {
                        value = sd.getText();
                        lang = sd.getLanguage();
                        if(lang == null || value == null || lang.isEmpty() || value.isEmpty())  continue;

                        if(languages.contains(lang)) {
                            SelectedResource selectedResource = new SelectedResource();
                            selectedResource.setIdLang(lang);
                            selectedResource.setGettedValue(value);
                            descriptions.add(selectedResource);
                        }
                    }
                }                
            }

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AgrovocHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {            
            Logger.getLogger(AgrovocHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        resourceDefinitions = descriptions;
        return traductions;
    }


    /**
     * permet de récupérer les images de Wikidata
     *
     * @param jsonDatas
     * @return
     */
    private ArrayList<SelectedResource> getImages(String jsonDatas) {
        ArrayList<SelectedResource> imagesUrls = new ArrayList<>();
        return imagesUrls;
    }

    public String getMessages() {
        return messages.toString();
    }

    public ArrayList<SelectedResource> getResourceTraductions() {
        return resourceTraductions;
    }

    public ArrayList<SelectedResource> getResourceDefinitions() {
        return resourceDefinitions;
    }

    public ArrayList<SelectedResource> getResourceImages() {
        return resourceImages;
    }

}
