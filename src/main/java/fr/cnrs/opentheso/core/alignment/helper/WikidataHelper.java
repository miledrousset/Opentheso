/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment.helper;

import com.bordercloud.sparql.Endpoint;
import com.bordercloud.sparql.EndpointException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.json.helper.JsonHelper;

/**
 *
 * @author miled.rousset
 */
public class WikidataHelper {

    private StringBuffer messages;
   // private ArrayList<NodeAlignment> listAlignValues;
    
    // les informations récupérées de Wikidata
    private ArrayList<SelectedResource> resourceWikidataTraductions;
    private ArrayList<SelectedResource> resourceWikidataDefinitions; 
    private ArrayList<SelectedResource> resourceWikidataImages; 
    
    
    public WikidataHelper() {
        messages = new StringBuffer();
    }
    
    /**
     * Alignement du thésaurus vers la source Wikidata en Sparql et en retour du Json
     * @param idC
     * @param idTheso
     * @param lexicalValue
     * @param lang
     * @param requete
     * @param source
     * @return 
     */
    public ArrayList<NodeAlignment> queryWikidata(String idC, String idTheso,
            String lexicalValue, String lang, 
            String requete, String source) {
            ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();
        try {
            Endpoint sp = new Endpoint("https://query.wikidata.org/sparql", false);

    /*       String querySelect = "SELECT ?item ?itemLabel ?itemDescription WHERE {" +
                                    "  ?item rdfs:label \"fibula\"@en." +
                                    "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"[AUTO_LANGUAGE],en\". }" +
                                    "}";*/

            requete = requete.replaceAll("##value##", lexicalValue);
            requete = requete.replaceAll("##lang##", lang);
            HashMap<String, HashMap> rs = sp.query(requete);

            if(rs == null) return null;


            ArrayList<HashMap<String, Object>> rows_queryWikidata = (ArrayList) rs.get("result").get("rows");
            for (HashMap<String, Object> hashMap : rows_queryWikidata) {
                NodeAlignment na = new NodeAlignment();
                na.setInternal_id_concept(idC);
                na.setInternal_id_thesaurus(idTheso);
                
                // label ou Nom
                if(hashMap.get("itemLabel") != null)
                    na.setConcept_target(hashMap.get("itemLabel").toString());
                else
                    continue;
                
                // description
                if(hashMap.get("itemDescription") != null)
                    na.setDef_target(hashMap.get("itemDescription").toString());
                else 
                    na.setDef_target("");
                
                na.setThesaurus_target(source);
                                
                // URI
                if(hashMap.get("item") != null)
                    na.setUri_target(hashMap.get("item").toString());
                else 
                    continue;
                
                listAlignValues.add(na);
            }
        } catch (EndpointException eex) {
            messages.append(eex.toString());
            return null;
        }
        catch (Exception e) {
            messages.append("pas de connexion internet !! " );
            return null;
        }
        return listAlignValues;
    }       
    
    
    /**
     * Cette fonction permet de récupérer les options de Wikidata 
     * Images, alignements, traductions....ource
     * @param selectedNodeAlignment
     * @param selectedOptions
     * @param thesaurusUsedLanguageWithoutCurrentLang
     * @param thesaurusUsedLanguage
     */
    public void setOptionsFromWikidata(
            NodeAlignment selectedNodeAlignment,
            List<String> selectedOptions,
            ArrayList<String> thesaurusUsedLanguageWithoutCurrentLang,
            ArrayList<String> thesaurusUsedLanguage) {
        if (selectedNodeAlignment == null) {
            return;
        }
        CurlHelper curlHelper = new CurlHelper();
        curlHelper.setHeader1("Accept");
        curlHelper.setHeader2("application/json");
        
        String uri = selectedNodeAlignment.getUri_target();//."https://www.wikidata.org/entity/Q178401";//"https://www.wikidata.org/entity/Q178401";//Q7748";Q324926
        String datas = curlHelper.getDatasFromUriHttps(uri);
        String entity = uri.substring(uri.lastIndexOf("/") + 1);

        
        for (String selectedOption : selectedOptions) {
            switch (selectedOption) {
                case "langues":
                    resourceWikidataTraductions = getTraductions(datas, entity, thesaurusUsedLanguageWithoutCurrentLang);
                    break;
                case "notes":
                    resourceWikidataDefinitions = getDescriptions(datas, entity, thesaurusUsedLanguage);
                    break;
                case "images":
                    resourceWikidataImages = getImages(datas, entity);
                    break;                    
            }
        }
    }
    
    /**
     * récupération des traductions
     * @param jsonDatas
     * @param entity
     * @param languages
     * @return 
     */
    private ArrayList<SelectedResource> getTraductions(
            String jsonDatas, String entity,
            ArrayList<String> languages) {
        ArrayList<SelectedResource> traductions = new ArrayList<>();
        
        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);

        //    JsonObject test = jsonObject.getJsonObject("entities");
        JsonObject jsonObject1;
        JsonValue jsonValue;
        
        String lang;
        String value;

        try {
            jsonObject1 = jsonObject.getJsonObject("entities").getJsonObject(entity).getJsonObject("labels");
        } catch (Exception e) {
            //System.err.println(e.toString());
            return null;
        }
        for (String language : languages) {
            try {
                SelectedResource selectedResource = new SelectedResource();
                jsonValue = jsonObject1.getJsonObject(language).get("language");
                lang = jsonValue.toString().replace("\"", "");
                selectedResource.setIdLang(lang);
                jsonValue = jsonObject1.getJsonObject(language).get("value");
                value = jsonValue.toString().replace("\"", "");
                selectedResource.setGettedValue(value);
                traductions.add(selectedResource);
            } catch (Exception e) {
            }
        }
        return traductions;
    }
    
    /**
     * permet de récupérer les descriptions de wikidata
     * @param jsonDatas
     * @param entity
     * @param languages
     * @return 
     */
    private ArrayList<SelectedResource> getDescriptions(
            String jsonDatas, String entity,
            ArrayList<String> languages) {
        ArrayList<SelectedResource> descriptions = new ArrayList<>();
        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);

        //    JsonObject test = jsonObject.getJsonObject("entities");
        JsonObject jsonObject1;
        JsonValue jsonValue;

        String lang;
        String value;
        
        try {
            jsonObject1 = jsonObject.getJsonObject("entities").getJsonObject(entity).getJsonObject("descriptions");
        } catch (Exception e) {
            //System.err.println(e.toString());
            return null;
        }
        
        for (String language : languages) {
            try {
                SelectedResource selectedResource = new SelectedResource();
                jsonValue = jsonObject1.getJsonObject(language).get("language");
                lang = jsonValue.toString().replace("\"", "");
                selectedResource.setIdLang(lang);
                jsonValue = jsonObject1.getJsonObject(language).get("value");
                value = jsonValue.toString().replace("\"", "");
                selectedResource.setGettedValue(value);
                descriptions.add(selectedResource);
            } catch (Exception e) {
            }
        }
        return descriptions;
    }
    
    /**
     * permet de récupérer les images de Wikidata
     * @param jsonDatas
     * @param entity 
     * @return  
     */
    private ArrayList<SelectedResource> getImages(String jsonDatas, String entity) {
        // pour construire l'URL de Wikimedia, il faut ajouter 
        // http://commons.wikimedia.org/wiki/Special:FilePath/
        // puis le nom de l'image

        String fixedUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/";
        
        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);

        //    JsonObject test = jsonObject.getJsonObject("entities");
        JsonObject jsonObject1;

        JsonObject jsonObject2;
        JsonValue jsonValue;
        
        ArrayList<SelectedResource> imagesUrls = new ArrayList<>();

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
        }
        return imagesUrls;
    }

    public String getMessages() {
        return messages.toString();
    }

    public ArrayList<SelectedResource> getResourceWikidataTraductions() {
        return resourceWikidataTraductions;
    }

    public ArrayList<SelectedResource> getResourceWikidataDefinitions() {
        return resourceWikidataDefinitions;
    }

    public ArrayList<SelectedResource> getResourceWikidataImages() {
        return resourceWikidataImages;
    }


    
    
}
