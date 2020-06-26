package fr.cnrs.opentheso.ws.ark;

//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;


import java.io.StringReader;
import java.util.Properties;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.primefaces.shaded.json.JSONObject;


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
    
    private JSONObject loginJson;
    
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
    
    /**
     * pour établir la connexion
     * @return
     * #MR
     */
    public boolean login() {
        return true;
        /*
        Client client= ClientBuilder.newClient();
        
        WebTarget target = client
                .target(propertiesArk.getProperty("serverHost"))
                .path("rest/login/username=" +
                        propertiesArk.getProperty("user") + 
                        "&password=" +
                        propertiesArk.getProperty("password") +
                        "&naan=" + 
                        propertiesArk.getProperty("idNaan"));     

        Response response = target.request().get(String.class);        
        //String response = target.request().get(String.class);
        
        
        String user = "demo";
        String pass = "demo2";
        String naan = "66666";
        String urlArk = "http://localhost:8082/Arkeo/";        
        
        String url = urlArk +
                        "rest/login/username=" +
                        user + 
                        "&password=" +
                        pass +
                        "&naan=" + 
                        naan;
        
        String url2 =  "rest/login/username=" +
                        user + 
                        "&password=" +
                        pass +
                        "&naan=" + 
                        naan;
        
        WebTarget target = client.target(urlArk).path(url2);
        
        String response = target.request().get(String.class);
        return response;    
        
        
/*        client = Client.create();
        WebResource webResource = client
                .resource(
                        propertiesArk.getProperty("serverHost") +
                        "rest/login/username=" +
                        propertiesArk.getProperty("user") + 
                        "&password=" +
                        propertiesArk.getProperty("password") +
                        "&naan=" + 
                        propertiesArk.getProperty("idNaan"));

        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
           /* throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());*/
/*            message = "Erreur de login";
            return false;
        }
  //      String tokenString = response.getEntity(String.class);
        loginJson = new JSONObject(response.getEntity(String.class));
//        getTokenFromString(tokenString);
        return true;*/
    }       
    
    
    
 ////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////// nouvelles fontions //////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////        
    
    
    private void getTokenFromString(String tokenString){
        token = null;
        try (JsonReader jsonReader = Json.createReader(new StringReader(tokenString))) {
            JsonObject jo = jsonReader.readObject();
            token = jo.getString("token").trim();
        } catch (Exception e) {
        }
    }    
    
    /**
     * permet d'ajouter un identifiant Ark et Handle
     * @param arkString
     * @return 
     */
    public boolean addArk2(String arkString) {
        return true;
        /*
        
        jsonArk = null;
        
        // il faut se connecter avant 
        if(loginJson == null) return false;
    //    loginJson.put("content", arkString);
        
        WebResource webResource = client
                .resource(propertiesArk.getProperty("serverHost")
                        + "rest/v1/ark/add");

        ClientResponse response = webResource.type("application/json")
                .put(ClientResponse.class, arkString);
        if (response.getStatus() == 200) {
            jsonArk = response.getEntity(String.class);
            setIdArkHandle();
            return true;
        }
        message = "Erreur lors de l'ajout d'un Ark";
        return false;*/
    }     
    
 ////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////// Fin nouvelles fontions //////////////////////////////
////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////     
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
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
     * permet de vérifier si l'identifiant Ark exsite
     * @param ark
     * @return 
     */
    public boolean isArkExist(String ark) {
        return true;
        /*
        client = Client.create();
        String idArk1 = ark.substring(ark.indexOf("/")+1);
        String naan = ark.substring(0, ark.indexOf("/"));
        WebResource webResource = client.resource(propertiesArk.getProperty("serverHost") +
                        "/rest/ark/naan=" + 
                        naan + 
                        "&id=" +
                        idArk1);
        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);
        if (response.getStatus() != 200) {
           // throw new RuntimeException("Failed : HTTP error code : "
           //         + response.getStatus());
            message = "Erreur lors de la requête pour savoir si Ark existe";
            return false;
        }
        String retour = response.getEntity(String.class);
//        System.out.println(jsonArk);
        return isExist(retour);*/
    }
    
    /**
     * permet de retourner un objet Json contenant l'identifiant Ark et Handle (serveur Ark MOM)
     * @param ark
     * @return 
     */
    public boolean getArk(String ark) {
       return true;
       /*
        client = Client.create();
        String idArk1 = ark.substring(ark.indexOf("/")+1);
        String naan = ark.substring(0, ark.indexOf("/"));
        if(idArk1 == null || naan == null) return false;
        WebResource webResource = client.resource(propertiesArk.getProperty("serverHost") +
                        "/rest/ark/naan=" + 
                        naan + 
                        "&id=" +
                        idArk1);
        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);
        if (response.getStatus() != 200) {
            message = "Erreur lors de la récupération d'un ARK";            
            return false;
        }
        jsonArk = response.getEntity(String.class);
        setForGet();
        return true;*/
    }     
    
    private boolean isExist(String jsonResponse){
        if(jsonResponse == null) return false;
        JsonReader reader = Json.createReader(new StringReader(jsonResponse));
        JsonObject jsonObject = reader.readObject();
        reader.close();

        JsonString values = jsonObject.getJsonString("description");
        if(values != null){
            if(values.getString().contains("Inexistant ARK")) return false;
            else
                if(values.getString().contains("Ark retreived")) return true;
        }
        return false; 
    }

    /**
     * permet d'ajouter un identifiant Ark et Handle
     * @param arkString
     * @return 
     */
    public boolean addArk(String arkString) {
     return true;
     /*
        jsonArk = null;
        
        // il faut se connecter avant 
        if(loginJson == null) return false;
        loginJson.put("content", arkString);

        WebResource webResource = client
                .resource(propertiesArk.getProperty("serverHost")
                        + "/rest/v1/ark/add");

        ClientResponse response = webResource.type("application/json")
                .put(ClientResponse.class, loginJson.toString());
        if (response.getStatus() == 200) {
            jsonArk = response.getEntity(String.class);
            setIdArkHandle();
            return true;
        }
        message = "Erreur lors de l'ajout d'un Ark";
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
    
    /**
     * permet de mettre à jour un abjet Ark 
     * @param arkString
     * @return 
     */
    public boolean updateArk(String arkString) {
        return true;
        /*
        jsonArk = null;
        
        // il faut se connecter avant
        if(loginJson == null) return false;

        loginJson.put("content", arkString);

        WebResource webResource = client
                .resource(propertiesArk.getProperty("serverHost") + "/rest/ark/");

        ClientResponse response = webResource.type("application/json")
                .post(ClientResponse.class, loginJson.toString());
        if (response.getStatus() != 200) {
            message = "Erreur lors de la mise à jour d'un Ark";            
            return false;
        }
        jsonArk = response.getEntity(String.class);
        return setForUpdate();*/
    }    
    
    
    
    
////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
///////////////////////// Getters an setters ///////////////////////////
////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
  
    private boolean setForUpdate(){
        if(jsonArk == null) return false;
    //    System.out.println("avant la lecture : " + jsonArk);
        JsonReader reader = Json.createReader(new StringReader(jsonArk));
        JsonObject jsonObject = reader.readObject();
        reader.close();

        if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("success")) {
            idArk = jsonObject.getJsonObject("result").getString("ark");
            idHandle = jsonObject.getJsonObject("result").getString("handle");
            Uri = jsonObject.getJsonObject("result").getString("urlTarget");
            loginJson.put("token", jsonObject.getJsonString("token"));
            return true;
        }
        message = "Erreur lors de la lecture du Json";
        return false;
    }
    
    private boolean setForGet(){
        if(jsonArk == null) return false;
    //    System.out.println("avant la lecture : " + jsonArk);
        JsonReader reader = Json.createReader(new StringReader(jsonArk));
        JsonObject jsonObject = reader.readObject();
        reader.close();

        if(jsonObject.getJsonString("status").getString().equalsIgnoreCase("ok")) {
            idArk = jsonObject.getJsonObject("result").getString("ark");
            idHandle = jsonObject.getJsonObject("result").getString("handle");
            Uri = jsonObject.getJsonObject("result").getString("urlTarget");
            loginJson.put("token", jsonObject.getJsonString("token"));
            return true;
        }
        message = "Erreur lors de la lecture du Json";
        return false;
    }    

    private boolean setIdArkHandle(){
        if(jsonArk == null) return false;
        JsonReader reader = Json.createReader(new StringReader(jsonArk));
        JsonObject jsonObject = reader.readObject();
        
       // JsonArray jsonArray = jsonObject.asJsonArray();
        reader.close();

        JsonObject jo = jsonObject.getJsonObject("result");
        
        JsonString values = jo.getJsonString("ark");
        if(values == null)
            idArk = null;
        else
            idArk = values.getString().trim();        
        
        
        values = jo.getJsonString("Handle");
        if(values == null)
            idHandle = null;
        else
            idHandle = values.getString().trim();        
        /*    for (int i = 0; i < jsonArray.size(); ++i) {
                jb = jsonArray.getJsonObject(i);
                uri = jb.getString("uri");
                title = jb.getString("prefLabel");
        */
        /*

        for (int i = 0; i < jsonArray.size(); ++i) {
            jb = jsonArray.getJsonObject(i);
            uri = jb.getString("uri");
            title = jb.getString("prefLabel");
            try {
                definition = jb.getString("altLabel");
                title = title + " (" + definition + ")";
            } catch (Exception e) {
            }        
        */
        
    /*    JsonObject jsonObject2;
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject2 = jsonArray.getJsonObject(i);
        }*/
        /*
        JsonString values = jsonObject.getJsonString("Ark");
        if(values == null)
            idArk = null;
        else
            idArk = values.getString().trim();
        
        values = jsonObject.getJsonString("Handle");
        if(values == null)
            idHandle = null;
        else
            idHandle = values.getString().trim();
        
        loginJson.put("token", jsonObject.getJsonString("token"));
*/
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

    public JSONObject getLoginJson() {
        return loginJson;
    }

    public void setLoginJson(JSONObject loginJson) {
        this.loginJson = loginJson;
    }
    
    
    
}
