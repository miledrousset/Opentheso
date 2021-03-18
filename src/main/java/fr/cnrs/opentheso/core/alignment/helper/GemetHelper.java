/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.net.ssl.HttpsURLConnection;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.json.helper.JsonHelper;


/**
 *
 * @author miled.rousset
 */
public class GemetHelper {

    private StringBuffer messages;
    // private ArrayList<NodeAlignment> listAlignValues;

    // les informations récupérées de Wikidata
    private ArrayList<SelectedResource> resourceTraductions;
    private ArrayList<SelectedResource> resourceDefinitions;
    private ArrayList<SelectedResource> resourceImages;

    public GemetHelper() {
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
    public ArrayList<NodeAlignment> queryGemet(String idC, String idTheso,
            String value, String lang,
            String query, String source) {
        ArrayList<NodeAlignment> listeAlign;

        if (query.trim().equals("")) {
            return null;
        }
        if (value.trim().equals("")) {
            return null;
        }

        try {
            value = URLEncoder.encode(value, "UTF-8");
            query = query.replace("##lang##", lang);
            query = query.replace("##value##", value);
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

    private ArrayList<NodeAlignment> getValues(
            String value,
            String jsonDatas,
            String idC, String idTheso, String source) {
        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();

        JsonArray jsonArray;
        JsonObject jb;
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonDatas))) {
            String title;
            String uri;
            
            jsonArray = jsonReader.readArray();

            for (int i = 0; i < jsonArray.size(); ++i) {
                jb = jsonArray.getJsonObject(i);
                title = jb.getJsonObject("preferredLabel").getString("string");
                uri = jb.getString("uri");
                
                uri = uri.replaceAll("http://", "https://");

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
     * @param thesaurusUsedLanguage
     */
    public void setOptions(
            NodeAlignment selectedNodeAlignment,
            List<String> selectedOptions,
            ArrayList<String> thesaurusUsedLanguageWithoutCurrentLang,
            ArrayList<String> thesaurusUsedLanguage) {
        if (selectedNodeAlignment == null) {
            return;
        }

        // uri traductions
        // https://www.eionet.europa.eu/gemet/getAllTranslationsForConcept?concept_uri=http://www.eionet.europa.eu/gemet/concept/7769&property_uri=http://www.w3.org/2004/02/skos/core%23prefLabel
        String uriLang = "https://www.eionet.europa.eu/gemet/getAllTranslationsForConcept?concept_uri="
                + selectedNodeAlignment.getUri_target().replaceAll("https://", "http://")
                + "&property_uri=http://www.w3.org/2004/02/skos/core%23prefLabel";
        // uri défintions
        // https://www.eionet.europa.eu/gemet/getAllTranslationsForConcept?concept_uri=http://www.eionet.europa.eu/gemet/concept/7769&property_uri=http://www.w3.org/2004/02/skos/core%23definition
        String uriDefinition = "https://www.eionet.europa.eu/gemet/getAllTranslationsForConcept?concept_uri="
                + selectedNodeAlignment.getUri_target().replaceAll("https://", "http://")
                + "&property_uri=http://www.w3.org/2004/02/skos/core%23definition";
        // pas de négociation de contenu, donc récupération via requete Get 

        CurlHelper curlHelper = new CurlHelper();
        curlHelper.setHeader1("Accept");
        curlHelper.setHeader2("application/json");

        //   String uri = selectedNodeAlignment.getUri_target();//."https://www.wikidata.org/entity/Q178401";//"https://www.wikidata.org/entity/Q178401";//Q7748";Q324926
        String datasLang = curlHelper.getDatasFromUriHttps(uriLang);
        String datasDefinition = curlHelper.getDatasFromUriHttps(uriDefinition);
        String datas = "";

        for (String selectedOption : selectedOptions) {
            switch (selectedOption) {
                case "langues":
                    resourceTraductions = getTraductions(datasLang, thesaurusUsedLanguageWithoutCurrentLang);
                    break;
                case "notes":
                    resourceDefinitions = getDescriptions(datasDefinition, thesaurusUsedLanguage);
                    break;
                case "images":
                    resourceImages = getImages(datas);
                    break;
            }
        }
    }

    /**
     * récupération des traductions
     *
     * @param jsonDatas
     * @param entity
     * @param languages
     * @return
     */
    private ArrayList<SelectedResource> getTraductions(
            String jsonDatas,
            ArrayList<String> languages) {
        ArrayList<SelectedResource> traductions = new ArrayList<>();

        String lang;
        String value;        
        
        // lecture du fichier Json des langues
        JsonArray dataArray;
        JsonObject dataObject;
        try (JsonReader reader = Json.createReader(new StringReader(jsonDatas))) {
            dataArray = reader.readArray();
            for (int i = 0; i < dataArray.size(); ++i) {
                dataObject = dataArray.getJsonObject(i);

                lang = dataObject.getString("language");
                value = dataObject.getString("string");

                if(lang == null || value == null || lang.isEmpty() || value.isEmpty())  continue;

                if(languages.contains(lang)) {
                    SelectedResource selectedResource = new SelectedResource();
                    selectedResource.setIdLang(lang);
                    selectedResource.setGettedValue(value);
                    traductions.add(selectedResource);
                }
            }
        }
        return traductions;
    }

    /**
     * permet de récupérer les descriptions de wikidata
     *
     * @param jsonDatas
     * @param entity
     * @param languages
     * @return
     */
    private ArrayList<SelectedResource> getDescriptions(
            String jsonDatas,
            ArrayList<String> languages) {
        ArrayList<SelectedResource> descriptions = new ArrayList<>();
        String lang;
        String value;        
        
        // lecture du fichier Json des langues
        JsonArray dataArray;
        JsonObject dataObject;
        try (JsonReader reader = Json.createReader(new StringReader(jsonDatas))) {
            dataArray = reader.readArray();
            for (int i = 0; i < dataArray.size(); ++i) {
                dataObject = dataArray.getJsonObject(i);
                
                SelectedResource selectedResource = new SelectedResource();
                lang = dataObject.getString("language");
                value = dataObject.getString("string");

                if(lang == null || value == null || lang.isEmpty() || value.isEmpty())  continue;

                if(languages.contains(lang)) {
                    selectedResource.setIdLang(lang);
                    selectedResource.setGettedValue(value);
                    descriptions.add(selectedResource);
                }
            }
        }
        return descriptions;
    }

    /**
     * permet de récupérer les images de Wikidata
     *
     * @param jsonDatas
     * @param entity
     * @return
     */
    private ArrayList<SelectedResource> getImages(String jsonDatas) {
        // pour construire l'URL de Wikimedia, il faut ajouter 
        // http://commons.wikimedia.org/wiki/Special:FilePath/
        // puis le nom de l'image

        String fixedUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/";

        JsonHelper jsonHelper = new JsonHelper();
  //      JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);

        //    JsonObject test = jsonObject.getJsonObject("entities");
        JsonObject jsonObject1;

        JsonObject jsonObject2;
        JsonValue jsonValue;

        ArrayList<SelectedResource> imagesUrls = new ArrayList<>();
/*
        try {
            jsonObject1 = jsonObject.getJsonObject("entities").getJsonObject(entity).getJsonObject("claims");//.getJsonObject("P18");
        } catch (Exception e) {
            //System.err.println(e.toString());
            return null;
        }

        try {
            JsonArray jsonArray = jsonObject1.getJsonArray("P18");
            for (int i = 0; i < jsonArray.size(); i++) {
                SelectedResource selectedResource = new SelectedResource();
                jsonObject2 = jsonArray.getJsonObject(i);
                jsonValue = jsonObject2.getJsonObject("mainsnak").getJsonObject("datavalue").get("value");
                selectedResource.setGettedValue(fixedUrl + jsonValue.toString().replace("\"", ""));
                imagesUrls.add(selectedResource);
            }

        } catch (Exception e) {
        }*/
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
