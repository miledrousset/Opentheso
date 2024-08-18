package fr.cnrs.opentheso.ws.ark;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import javax.net.ssl.HttpsURLConnection;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
//import org.primefaces.shaded.json.JSONObject;


public final class ArkClientRest {
    private Properties propertiesArk;  
//    private Client client;
    
    private String idArk;
    private String idHandle;
    private String Uri;
    
    
    // prefix MOM
    private String prefixHandle = "20.500.11859";
    
    private String urlHandle = "http://193.48.137.68:8000/api/handles/";
    private String jsonArk;
    
    private JsonObject loginJson;
    
    private String token;
    
    private String message;
    
    
    public ArkClientRest() {
    }
   
    /**
     * defition des propriétés du serveur Ark
     * @param propertiesArk 
     * #MR
     */
    public void setPropertiesArk(Properties propertiesArk) {
        this.propertiesArk = propertiesArk;
    }    
    
    
////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////// nouvelles fontions //////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////       
    
    /**
     * pour établir la connexion
     * @return
     * #MR
     */
    public boolean login() {
        Client client= ClientBuilder.newClient();
        WebTarget webTarget = client
                .target(propertiesArk.getProperty("serverHost"))
                .path("rest/login/username=" +
                        propertiesArk.getProperty("user") + 
                        "&password=" +
                        propertiesArk.getProperty("password") +
                        "&naan=" + 
                        propertiesArk.getProperty("idNaan"));     

        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);        
        Response response = invocationBuilder.get();
        String tokenString = null;
        try {
            if (response.getStatus() != 200) {
                message = "Erreur de login: " + response.getStatus();
                return false;
            }
            tokenString = response.readEntity(String.class);
        } finally {
            response.close();
            client.close();
        }          
        getTokenFromString(tokenString);
        return token != null;
    }       
    
    private void getTokenFromString(String tokenString){
        token = null;
        try (JsonReader jsonReader = Json.createReader(new StringReader(tokenString))) {
            loginJson = jsonReader.readObject();
            token = loginJson.getString("token").trim();
        } catch (Exception e) {
        }
    }
    
    /**
     * permet de retourner un objet Json contenant l'identifiant Ark et Handle (serveur Ark MOM)
     * @param ark
     * @return 
     */
    public boolean getArk(String ark) {
        Client client= ClientBuilder.newClient();  
        
        String idArk1 = ark.substring(ark.indexOf("/")+1);
        String naan = ark.substring(0, ark.indexOf("/"));
        if(idArk1 == null || naan == null) return false;
        
        WebTarget webTarget = client
                .target(propertiesArk.getProperty("serverHost"))
                .path("/rest/v1/ark/naan=" +
                        naan + 
                        "&id=" +
                        idArk1);          

        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);        
        Response response = invocationBuilder.get();
        try {
            if (response.getStatus() != 200) {
                 message = "Erreur lors de la récupération d'un ARK : " + response.getStatus();
                 return false;
            }
            jsonArk = response.readEntity(String.class);
        } finally {
            response.close();
            client.close();
        }
        setForGet();
        return true;
    }     
    
    /**
     * permet d'ajouter un lot d'identifiants Ark
     * le retour de résultat est sous forme de tableau json 
     * @param arkString
     * @return 
     */
    public String addBatchArk(String arkString) {
        jsonArk = null;
       
        // il faut vérifier la connexion avant 
        if(loginJson == null) {
            message = "Erreur de connexion";
            return null;
        }

        Client client= ClientBuilder.newClient();
        try (Response response = client
            .target(propertiesArk.getProperty("serverHost"))
            .path("rest/v1/ark/batchAdd")
            .request(MediaType.APPLICATION_JSON)
            .header("Content-type", "application/json")
            .put(Entity.json(arkString))){
            
            if (response.getStatus() != 200) {
                message =  "Erreur lors de l'ajout d'un Ark" + response.getStatus();
                response.close();
                client.close();
                return null;
            }
            jsonArk = response.readEntity(String.class);            
            
        } catch (Exception e) {
            message =  "Exception lors de l'ajout d'un Ark";
            return null;            
        }
        return jsonArk;
    }    
    /////// la même méthode au dessus mais avec httpUrlConnection ///////////
    public String addBatchArk2(String arkString) {
        String apiUrl = propertiesArk.getProperty("serverHost") + "rest/v1/ark/batchAdd";
        System.out.println("/////////////////// avant envoie au serveur Ark /////////////////////");
        try {
            URL url = new URL(apiUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(3600000);
            
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(arkString.getBytes());
            outputStream.flush();

            StringBuffer response;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                response = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

          //  System.out.println(response.toString());
            connection.disconnect();
            System.out.println("/////////////////// retour du serveur Ark /////////////////////");            
            return response.toString();

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return null;
    }     
    
    
    /**
     * permet d'ajouter un identifiant Ark et Handle
     * @param arkString
     * @return 
     */
    public boolean addArk(String arkString) {
        jsonArk = null;
       
        // il faut vérifier la connexion avant 
        if(loginJson == null) return false;

        Client client= ClientBuilder.newClient();
        try (Response response = client
            .target(propertiesArk.getProperty("serverHost"))
            .path("rest/v1/ark/add")
            .request(MediaType.APPLICATION_JSON)
            .header("Content-type", "application/json")
            .put(Entity.json(arkString))){
            
            if (response.getStatus() != 200) {
                message =  "Erreur lors de l'ajout d'un Ark" + response.getStatus();
                response.close();
                client.close();
                return false;
            }
            jsonArk = response.readEntity(String.class);            
            
        } catch (Exception e) {
            message =  "Exception lors de l'ajout d'un Ark";
            return false;            
        }
        return setIdArkHandle();
    }
    
    /**
     * permet de vérifier si l'identifiant Ark exsite
     * @param ark
     * @return 
     */
    public boolean isArkExist(String ark) {
        Client client= ClientBuilder.newClient(); 
        
        String idArk1 = ark.substring(ark.indexOf("/")+1);
        String naan = ark.substring(0, ark.indexOf("/"));
        WebTarget webTarget = client
                .target(propertiesArk.getProperty("serverHost"))
                .path("/rest/v1/ark/exist=" + 
                        naan + 
                        "&id=" +
                        idArk1); 
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);        
        Response response = invocationBuilder.get();
        String output = null;
        try {
            if (response.getStatus() != 200) {
                message = "Erreur " + response.getStatus();
                return false;
            }
            output = response.readEntity(String.class);
        } finally {
            response.close();
            client.close();
        }           
        return isExist(output);        
    }    
    private boolean isExist(String jsonResponse){
        if(jsonResponse == null) return false;
        JsonObject jsonObject;
        try (JsonReader reader = Json.createReader(new StringReader(jsonResponse))) {
            jsonObject = reader.readObject();
            JsonString values = jsonObject.getJsonString("description");
            if(values != null){
                if(values.getString().contains("Inexistant ARK")) return false;
                if(values.getString().contains("Ark exist")) return true;
            }            
        }
        message = "Erreur de format";
        return false; 
    }    
    
    /**
     * permet de mettre à jour un abjet Ark 
     * @param arkString
     * @return 
     */
    public boolean updateArk(String arkString) {
        jsonArk = null;
        
        // il faut se connecter avant
        if(loginJson == null) return false;
        
        Client client= ClientBuilder.newClient();
        try (Response response = client
            .target(propertiesArk.getProperty("serverHost"))
            .path("rest/v1/ark/update")
            .request(MediaType.APPLICATION_JSON)
            .header("Content-type", "application/json")
            .put(Entity.json(arkString))){
            
            if (response.getStatus() != 200) {
                message = "Erreur lors de la mise à jour d'un Ark : " + response.getStatus();
                response.close();
                client.close();
                return false;
            }
            jsonArk = response.readEntity(String.class);           
        } catch (Exception e) {
            message =  "Exception lors de la mise à jour de Ark";
            return false;            
        } 
        return setForUpdate();
    }      
    
    /**
     * permet de mettre à jour un abjet Ark 
     * @param arkString
     * @return 
     */
    public boolean updateUriArk(String arkString) {
        jsonArk = null;
        
        // il faut se connecter avant
        if(loginJson == null) return false;
        
        Client client= ClientBuilder.newClient();
        try (Response response = client
            .target(propertiesArk.getProperty("serverHost"))
            .path("rest/v1/ark/uriupdate")
            .request(MediaType.APPLICATION_JSON)
            .header("Content-type", "application/json")
            .put(Entity.json(arkString))){
            
            if (response.getStatus() != 200) {
                message = "Erreur lors de la mise à jour d'un Ark : " + response.getStatus();
                response.close();
                client.close();
                return false;
            }
            jsonArk = response.readEntity(String.class);           
        } catch (Exception e) {
            message =  "Exception lors de la mise à jour de Ark";
            return false;            
        } 
        return setForUriUpdate();
    }      
    
 ////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////// Fin nouvelles fontions //////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////     
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /** Ne marche pas
     * permet de vérifier si l'identifiant handle exsite
     * @param idHandle
     * @return 
     */
    public boolean isHandleExist(String idHandle) {
        return true;

    //    String prefixe = "20.500.11859"; // prefixe MOM
    //    String idHandle = "66666.crt0eTJm32hksG";

   /*     HandleClient handleClient = new HandleClient();
        return handleClient.isHandleExist(
                    "https://hdl.handle.net/",
                    prefixHandle + "/" + idHandle);
       // System.err.println("reponse " + test);
     */  
       

      //  String urlHandle = "http://193.48.137.68:8000/api/handles/";
    //    String prefix = "20.500.11859";

    //    String internalId = "66666.crt2hbt7fWNBn";
    //    Client client = Client.create();
/*
        String output;
        client = Client.create();        
        WebResource webResource = client
                .resource(urlHandle + prefixHandle + "/" + idHandle);
        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);
        output = response.getEntity(String.class);
        JSONObject json = new JSONObject(output);
        if (json.getInt("responseCode") == 100) {
            return false;
            //System.err.println("n'existe pas");
        }
        if (json.getInt("responseCode") == 1) {
            return true;
            //System.err.println("existe");
        }
        return false;*/
    }    
    
    
    

    
    
    



    
    /**
     * permet d'ajouter un identifiant Ark et Handle
     * @param arkString
     * @return 
     */
    public boolean deleteHandle(String arkString) {
        return true;
        /*
        jsonArk = null;
        
        // il faut se connecter avant 
        if(loginJson == null) return false;
        loginJson.put("content", arkString);
        WebResource webResource = client
                .resource(propertiesArk.getProperty("serverHost")
                        + "/rest/ark/deletehandle");
        ClientResponse response = webResource.type("application/json")
                .put(ClientResponse.class, loginJson.toString());
        if (response.getStatus() == 200) {
            jsonArk = response.getEntity(String.class);
            //setIdArkHandle();
            return true;
        }
        message = "Erreur lors de de la suppression de l'id Handle";
        return false;*/
    }     
    
  
    
    
    
    
////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
///////////////////////// Getters an setters ///////////////////////////
////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
  
    private boolean setForUriUpdate(){
        if(jsonArk == null) return false;
        JsonReader reader;
        try {
            reader = Json.createReader(new StringReader(jsonArk));
            JsonObject jsonObject = reader.readObject();
            reader.close();  
            if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("ok")) {
                token = jsonObject.getJsonString("token").getString();
                return true;
            }
            if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("ERROR")) {
                message = jsonObject.getJsonString("description").getString();
                token = jsonObject.getJsonString("token").getString();
                return false;
            }        
        } catch (Exception e) {
        }
        message = "Erreur lors de la lecture du Json";
        return false;           
    }    
    
    private boolean setForUpdate(){
        if(jsonArk == null) return false;
        JsonReader reader;
        try {
            reader = Json.createReader(new StringReader(jsonArk));
            JsonObject jsonObject = reader.readObject();
            reader.close();  
            if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("ok")) {
                idArk = jsonObject.getJsonObject("result").getString("ark");
                idHandle = jsonObject.getJsonObject("result").getString("handle");
                Uri = jsonObject.getJsonObject("result").getString("urlTarget");
                token = jsonObject.getJsonString("token").getString();
                return true;
            }
            if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("ERROR")) {
                message = jsonObject.getJsonString("description").getString();
                token = jsonObject.getJsonString("token").getString();
                return false;
            }        
        } catch (Exception e) {
        }
        message = "Erreur lors de la lecture du Json";
        return false;           
    }
    
    private boolean setForGet(){
        if(jsonArk == null) return false;
        JsonReader reader;
        try {
            reader = Json.createReader(new StringReader(jsonArk));
            JsonObject jsonObject = reader.readObject();
            reader.close();

            if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("ok")) {
                idArk = jsonObject.getJsonObject("result").getString("ark");
                idHandle = jsonObject.getJsonObject("result").getString("handle");
                Uri = jsonObject.getJsonObject("result").getString("urlTarget");
                token = jsonObject.getJsonString("token").getString();
                return true;
            }
            if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("ERROR")) {
                message = jsonObject.getJsonString("description").getString();
                token = jsonObject.getJsonString("token").getString();
                return false;
            }               
        } catch (Exception e) {
        }
        message = "Erreur lors de la lecture du Json";
        return false;
    }    

    private boolean setIdArkHandle(){
        
        if(jsonArk == null) return false;
        JsonReader reader;
        try {
            reader = Json.createReader(new StringReader(jsonArk));
            JsonObject jsonObject = reader.readObject();

           // JsonArray jsonArray = jsonObject.asJsonArray();
            reader.close();
            if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("ok")) {
                idArk = jsonObject.getJsonObject("result").getString("ark");
                idHandle = jsonObject.getJsonObject("result").getString("handle");
            //    Uri = jsonObject.getJsonObject("result").getString("urlTarget");
                token = jsonObject.getJsonString("token").getString();
                return true;
//                
//                JsonObject jo = jsonObject.getJsonObject("result");
//                JsonString values = jo.getJsonString("ark");
//                if(values == null)
//                    idArk = null;
//                else
//                    idArk = values.getString().trim();        
//                values = jo.getJsonString("handle");
//                if(values == null)
//                    idHandle = null;
//                else
//                    idHandle = values.getString().trim();        
//                token = jsonObject.getString("token");
//                return true;
                
            }            
            if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("ERROR")) {
                message = jsonObject.getJsonString("description").getString();
                if(message.contains("URL already Exist")) {
                    idArk = jsonObject.getJsonString("ark").getString();
                    token = jsonObject.getJsonString("token").getString();
                    return true;
                }
                
                token = jsonObject.getJsonString("token").getString();
                return false;
            }  
            
//            JsonObject jo = jsonObject.getJsonObject("result");
//
//            JsonString values = jo.getJsonString("ark");
//            if(values == null)
//                idArk = null;
//            else
//                idArk = values.getString().trim();        
//
//
//            values = jo.getJsonString("handle");
//            if(values == null)
//                idHandle = null;
//            else
//                idHandle = values.getString().trim();        
//            token = jsonObject.getString("token");
        } catch (Exception e) {
            message = e.toString();
            return false;
        }
        return true;        
    }

    public String getIdHandle() {
        return idHandle;
    }
    
    public String getIdArk() {
        return idArk;
    }

    public String getUri() {
        return Uri;
    }

    public String getMessage() {
        return message;
    }

    public JsonObject getLoginJson() {
        return loginJson;
    }

    public void setLoginJson(JsonObject loginJson) {
        this.loginJson = loginJson;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
}
