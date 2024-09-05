package fr.cnrs.opentheso.client.alignement;

import java.util.ArrayList;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import jakarta.json.Json;
import jakarta.json.JsonReader;
import lombok.Data;

import javax.net.ssl.HttpsURLConnection;


@Data
public class OntomeHelper {

    private StringBuffer messages = new StringBuffer();


    public ArrayList<NodeAlignment> queryOntomeHelper(String idC, String idTheso, String lexicalValue, String query, String source) {
        
        if (query.trim().equals("") ) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }        

        if (query.trim().equals("") ) {
            return null;
        }
        
        try {
            lexicalValue = URLEncoder.encode(lexicalValue, "UTF-8");
            query = query.replaceAll("##value##", lexicalValue);
            var cons = (HttpsURLConnection) new URL(query).openConnection();
            cons.setRequestMethod("GET");
            cons.setRequestProperty("Accept", "application/json");
            if (cons.getResponseCode() != 200){
                if (cons.getResponseCode() != 202) {
                    return null;
                }
            }
            var br = new BufferedReader(new InputStreamReader((cons.getInputStream())));

            String output;
            String xmlRecord = "";
            while ((output = br.readLine()) != null) {
                xmlRecord += output;
            }
            cons.disconnect();
            br.close();
            return getValuesFromJson(xmlRecord, idC, idTheso, source);
        } catch (Exception e) {
            return null;
        }
     }
    
    private ArrayList<NodeAlignment> getValuesFromJson(String jsonValue, String idConcept, String idTheso, String source){
        JsonArray jsonArray;
        JsonObject value;   
       
        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();
        
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonValue));
            jsonArray = jsonReader.readArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                NodeAlignment na = new NodeAlignment();
                na.setInternal_id_concept(idConcept);
                na.setInternal_id_thesaurus(idTheso);
                value = jsonArray.getJsonObject(i);
                // label ou Nom
                try {
                     na.setConcept_target(value.getString("standardLabel") + " (" + value.getInt("id") + ")");
                } catch (Exception e) {
                    continue;
                }                    

                // description
                try {
                    if(value.getString("rootNamespaceLabel") != null) {
                        na.setDef_target(value.getString("rootNamespaceLabel") + " (" + value.getInt("rootNamespaceId") + ")");
                    } else {
                        na.setDef_target("");
                    }
                } catch (Exception e) {
                    continue;
                }                

                na.setThesaurus_target(source);

                // URI
                try {
                    na.setUri_target(value.getString("ontomeURI"));
                    na.setUri_target(na.getUri_target().replaceAll("http://", "https://"));
                } catch (Exception e) {
                    continue;
                }                  

                listAlignValues.add(na);                
            }            
        } catch (Exception e) {
            System.out.println(e);
        }
        return listAlignValues;
    }

    public String getMessages() {
        return messages.toString();
    }

}
