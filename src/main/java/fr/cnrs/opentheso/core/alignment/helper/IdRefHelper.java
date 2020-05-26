/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.json.helper.JsonHelper;

/**
 *
 * @author miled.rousset
 */
public class IdRefHelper {

    private StringBuffer messages;
    
    // les informations récupérées de IdRef
    private ArrayList<SelectedResource> resourceIdRefTraductions;
    private ArrayList<SelectedResource> resourceIdRefDefinitions; 
    private ArrayList<SelectedResource> resourceIdRefImages; 
    
    
    public IdRefHelper() {
        messages = new StringBuffer();
    }
  
    /**
     * Alignement du thésaurus vers la source IdRef Titre Uniforme en REST et en retour du Json
     * @param idC
     * @param idTheso
     * @param lexicalValue
     * @param lang
     * @param query
     * @param source
     * @return 
     */
    public ArrayList<NodeAlignment> queryIdRefUniformtitle(String idC, String idTheso,
            String lexicalValue, String lang, 
            String query, String source) {

        //https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=uniformtitle_t:(la%20belle)%20AND%20recordtype_z:f

        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }
        
        String [] splitValues = lexicalValue.split(" ");
        
        String value = "";
        
        for (String splitValue : splitValues) {
            if(value.isEmpty())
                value = splitValue;
            else
                value = value + " AND " + splitValue;
        }
    //    value = value.replaceAll(" ", "%20");        

        ArrayList<NodeAlignment> listeAlign;
        // construction de la requête de type (webservices Opentheso)

        try {
            value = URLEncoder.encode(value,"UTF-8");            
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

            conn.disconnect();
            listeAlign = getValuesSubject(records, idC, idTheso, source);

        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }                
            
    /**
     * Alignement du thésaurus vers la source IdRef Sujets en REST et en retour du Json
     * @param idC
     * @param idTheso
     * @param lexicalValue
     * @param lang
     * @param query
     * @param source
     * @return 
     */
    public ArrayList<NodeAlignment> queryIdRefLieux(String idC, String idTheso,
            String lexicalValue, String lang, 
            String query, String source) {

        // https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=geogname_t:
        // (saint%20clair%20du%20rhone)%20AND%20recordtype_z:c

        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }
        
        String [] splitValues = lexicalValue.split(" ");
        
        String value = "";
        
        for (String splitValue : splitValues) {
            if(value.isEmpty())
                value = splitValue;
            else
                value = value + " AND " + splitValue;
        }
    //    value = value.replaceAll(" ", "%20");        

        ArrayList<NodeAlignment> listeAlign;
        // construction de la requête de type (webservices Opentheso)

        try {
            value = URLEncoder.encode(value,"UTF-8");            
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

            conn.disconnect();
            listeAlign = getValuesSubject(records, idC, idTheso, source);

        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }     
    
    /**
     * Alignement du thésaurus vers la source IdRef Sujets en REST et en retour du Json
     * @param idC
     * @param idTheso
     * @param lexicalValue
     * @param lang
     * @param query
     * @param source
     * @return 
     */
    public ArrayList<NodeAlignment> queryIdRefSubject(String idC, String idTheso,
            String lexicalValue, String lang, 
            String query, String source) {

        //https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:amphore*%20AND%20recordtype_z:r
//        String query = "https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:##value##%20AND%20recordtype_z:r";


        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }
        
        String [] splitValues = lexicalValue.split(" ");
        
        String value = "";
        
        for (String splitValue : splitValues) {
            if(value.isEmpty())
                value = splitValue;
            else
                value = value + " AND " + splitValue;
        }
    //    value = value.replaceAll(" ", "%20");        

        ArrayList<NodeAlignment> listeAlign;
        // construction de la requête de type (webservices Opentheso)

        try {
            value = URLEncoder.encode(value,"UTF-8");            
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

            conn.disconnect();
            listeAlign = getValuesSubject(records, idC, idTheso, source);

        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }   
    
    
    private ArrayList<NodeAlignment> getValuesSubject(String jsonDatas, 
                    String idC, String idTheso, String source) {
        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);

        //    JsonObject test = jsonObject.getJsonObject("entities");
        JsonObject jsonObject1;
        JsonArray jsonArray;
        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();
        

        String uri = "https://www.idref.fr/";
        
        //String valeur = "";
        //String idRef = "";
        //JsonValue jsonValue;

        try {
            if(jsonObject == null) return null;
            if(jsonObject.getJsonObject("response") == null) return null;
            jsonArray = jsonObject.getJsonObject("response").getJsonArray("docs");
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
      
        for (int i = 0; i < jsonArray.size(); i++) {
            NodeAlignment na = new NodeAlignment();
            na.setInternal_id_concept(idC);
            na.setInternal_id_thesaurus(idTheso);
            
            jsonObject1 = jsonArray.getJsonObject(i); //jsonObject1.getValueType()
           
            // label ou Nom
            try {
                jsonObject1.getString("affcourt_z");
                na.setConcept_target(((JsonObject) jsonObject1).getString("affcourt_z"));
            } catch (Exception e) {
                continue;
            }            
            
            // description
            na.setDef_target("");
            na.setThesaurus_target(source);

            // URI
            try {
                jsonObject1.getString("ppn_z");
                na.setUri_target(uri + ((JsonObject) jsonObject1).getString("ppn_z"));
            } catch (Exception e) {
                continue;
            }             
            na.setUri_target(uri + ((JsonObject) jsonObject1).getString("ppn_z"));

            listAlignValues.add(na);
            
        /*    valeur = ((JsonObject) jsonObject1).getString("affcourt_z");
            idRef = ((JsonObject) jsonObject1).getString("ppn_z");

            System.out.println(valeur);
            System.out.println(idRef);
            System.out.println("url = " + uri+idRef); */
        }
        return listAlignValues;
    }
    
    /**
     * Alignement du thésaurus vers la source IdRef Personnes en REST et en retour du Json
     * @param idC
     * @param idTheso
     * @param lexicalValue
     * @param lang
     * @param query
     * @param source
     * @return 
     */
    public ArrayList<NodeAlignment> queryIdRefPerson(String idC, String idTheso,
            String lexicalValue, String lang, 
            String query, String source) {

        //https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:amphore*%20AND%20recordtype_z:r
//        String query = "https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:##value##%20AND%20recordtype_z:r";


        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }
        
        String [] splitValues = lexicalValue.split(" ");
        
        String value = "";
        
        for (String splitValue : splitValues) {
            if(value.isEmpty())
                value = splitValue;
            else
                value = value + " AND " + splitValue;
        }
    //    value = value.replaceAll(" ", "%20");        

        ArrayList<NodeAlignment> listeAlign;
        // construction de la requête de type (webservices Opentheso)

        try {
            value = URLEncoder.encode(value,"UTF-8");            
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

            conn.disconnect();
            listeAlign = getValuesNames(records, idC, idTheso, source);

        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }       
    
    /**
     * Alignement du thésaurus vers la source IdRef Names en REST et en retour du Json
     * @param idC
     * @param idTheso
     * @param nom
     * @param lang
     * @param prenom
     * @param query
     * @param source
     * @return 
     */
    public ArrayList<NodeAlignment> queryIdRefNames(String idC, String idTheso,
            String nom, String prenom, String lang, 
            String query, String source) {

        //https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:amphore*%20AND%20recordtype_z:r
//        String query = "https://www.idref.fr/Sru/Solr?wt=json&version=2.2&start=&rows=100&indent=on&fl=id,ppn_z,affcourt_z&q=subjectheading_t:##value##%20AND%20recordtype_z:r";


    //    nom = "marie";//"Jeanne d'Arc";
    //    prenom = "jean-bernard";

        nom = nom.trim();
        prenom = prenom.trim();
        if (query.trim().equals("")) {
            return null;
        }
        if (nom.isEmpty()) {
            query = query.replace("nom_t:(##nom##)%20AND%20", "");
        }
        if (prenom.isEmpty()) {
            query = query.replace("%20AND%20prenom_t:(##prenom##)", "");
        } 
        
        /// il faut ici séparer les valeurs des noms et prenoms 
        // pour ajouter des AND entre les valeurs multiples
        
        if(!nom.isEmpty()) {
            nom = clearName(nom);
        }
        if(!prenom.isEmpty()) {
            prenom = clearName(prenom);
        }        

        ArrayList<NodeAlignment> listeAlign;
        // construction de la requête de type (webservices Opentheso)

        try {
            nom = URLEncoder.encode(nom,"UTF-8");
            prenom = URLEncoder.encode(prenom,"UTF-8");

            query = query.replace("##nom##", nom);
            query = query.replace("##prenom##", prenom);
            
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

            conn.disconnect();
            listeAlign = getValuesNames(records, idC, idTheso, source);

        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }
    
    private String clearName(String nom){
        nom = nom.replaceAll("\\[|\\]" , "");        
        
        nom = nom.replaceAll(";", " AND ");            
        nom = nom.replaceAll(" ", " AND ");
        nom = nom.replaceAll("_", " AND ");
        nom = nom.replaceAll("-", " AND ");
        
       
        return nom;
    }
    
    
    private ArrayList<NodeAlignment> getValuesNames(String jsonDatas, 
                    String idC, String idTheso, String source) {
        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);

        //    JsonObject test = jsonObject.getJsonObject("entities");
        JsonObject jsonObject1;
        JsonArray jsonArray;
        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();
        

        String uri = "https://www.idref.fr/";
        
        //String valeur = "";
        //String idRef = "";
        //JsonValue jsonValue;
        String nom = "";
        String prenom = "";
        JsonArray jsonArrayNames;

        try {
            if(jsonObject == null) return null;
            if(jsonObject.getJsonObject("response") == null) return null;
            jsonArray = jsonObject.getJsonObject("response").getJsonArray("docs");
        } catch (Exception e) {
            System.err.println(e.toString());
            return null;
        }
      
        for (int i = 0; i < jsonArray.size(); i++) {
            NodeAlignment na = new NodeAlignment();
            na.setInternal_id_concept(idC);
            na.setInternal_id_thesaurus(idTheso);
            
            jsonObject1 = jsonArray.getJsonObject(i); //jsonObject1.getValueType()
           
            // label ou Nom
            try {
                jsonObject1.getString("affcourt_z");
                na.setConcept_target(((JsonObject) jsonObject1).getString("affcourt_z"));
            } catch (Exception e) {
                continue;
            }
            

            na.setThesaurus_target(source);

            // URI
            try {
                jsonObject1.getString("ppn_z");
                na.setUri_target(uri + ((JsonObject) jsonObject1).getString("ppn_z"));
            } catch (Exception e) {
                continue;
            }            


            // récupération des noms et prénoms
            jsonArrayNames = jsonObject1.getJsonArray("nom_s");
            if(jsonArrayNames != null) {
                for (int j = 0; j < jsonArrayNames.size(); j++) {
                    if(j == 0)
                        nom = jsonArrayNames.getString(j);
                    else
                        nom = nom + "; " + jsonArrayNames.getString(j);
                }
            }

            jsonArrayNames = jsonObject1.getJsonArray("prenom_s");
            if(jsonArrayNames != null) {
                for (int j = 0; j < jsonArrayNames.size(); j++) {
                    if(j == 0)
                        prenom = jsonArrayNames.getString(j);
                    else
                        prenom = prenom+ "; " + jsonArrayNames.getString(j);
                }
            }            
            
            // description
            na.setDef_target("Noms=" + nom + "/ Prenoms= " + prenom);
            
            listAlignValues.add(na);
            
        /*    valeur = ((JsonObject) jsonObject1).getString("affcourt_z");
            idRef = ((JsonObject) jsonObject1).getString("ppn_z");

            System.out.println(valeur);
            System.out.println(idRef);
            System.out.println("url = " + uri+idRef); */
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
/*    public void setOptionsFromIdRef(
            NodeAlignment selectedNodeAlignment,
            List<String> selectedOptions,
            ArrayList<String> thesaurusUsedLanguageWithoutCurrentLang,
            ArrayList<String> thesaurusUsedLanguage) {
        if (selectedNodeAlignment == null) {
            listAlignValues = null;
            return;
        }
        CurlHelper curlHelper = new CurlHelper();
        curlHelper.setHeader1("Accept");
        curlHelper.setHeader2("application/json");
        
        String uri = selectedNodeAlignment.getUri_target();//."https://www.wikidata.org/entity/Q178401";//"https://www.wikidata.org/entity/Q178401";//Q7748";Q324926
        String datas = curlHelper.getDatasFromUri(uri);
        String entity = uri.substring(uri.lastIndexOf("/") + 1);

        
        for (String selectedOption : selectedOptions) {
            switch (selectedOption) {
                case "langues":
                    resourceIdRefTraductions = getTraductions(datas, entity, thesaurusUsedLanguageWithoutCurrentLang);
                //    getTraductionsOfConcept(idTheso, idConcept);
                //    setObjectTraductions(resourceWikidataTemp);
                    break;
                case "notes":
                    resourceIdRefDefinitions = getDescriptions(datas, entity, thesaurusUsedLanguage);
                //    getDefinitionsOfConcept(idTheso, idConcept);
                //    setObjectDefinitions(resourceWikidataTemp);
                    break;
                case "images":
                    resourceIdRefImages = getImages(datas, entity);
                //    getExternalImagesOfConcept(idTheso, idConcept);
                //    setObjectImages(resourceWikidataTemp);
                    break;                    
            }
        }
    }*/
    


    public String getMessages() {
        return messages.toString();
    }

    public ArrayList<SelectedResource> getResourceIdRefTraductions() {
        return resourceIdRefTraductions;
    }

    public ArrayList<SelectedResource> getResourceIdRefDefinitions() {
        return resourceIdRefDefinitions;
    }

    public ArrayList<SelectedResource> getResourceIdRefImages() {
        return resourceIdRefImages;
    }


    
    
}
