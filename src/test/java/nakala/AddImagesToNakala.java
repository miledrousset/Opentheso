package nakala;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class AddImagesToNakala {

    /**
     * Fonctionne validé par Miled
     */
    @Test
    public void createCollection() {
        String apiKey = "01234567-89ab-cdef-0123-456789abcdef";
        String status = "private"; // private // public
        String title = "Collection Miled via API";
        String lang = "fr";
        String jsonDatas = "{\n"
                + "    \"status\": \"" + status + "\",\n"
                + "    \"metas\" : [\n"
                + "        {\n"
                + "            \"propertyUri\": \"http://nakala.fr/terms#title\",\n"
                + "            \"value\": \"" + title + "\",\n"
                + "            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\",\n"
                + "            \"lang\": \"" + lang + "\"\n"
                + "        }\n"
                + "    ]\n"
                + "}";
        try {
            URL url = new URL("https://api.nakala.fr/collections");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-API-KEY", apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonDatas.getBytes());
            outputStream.flush();

            StringBuffer response;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                response = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            System.out.println(response.toString());
            connection.disconnect();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void nakalaUploadImages() {
        String filePath = "/Users/miledrousset/Documents/Nakala/Hirada/Location/";
        String fileName = "location.csv";
        String apiKey = "60f86d89-25c7-9961-4f53-c2a5c1ab079e";
        char delimiter = ';';

        // lecture du fichier CSV
        ArrayList<NakalaMetaData> nakalaMetaDatas = getDataFromCSV(filePath + fileName, delimiter);

        String jsonResponseImage;
        String name;
        String doi;
        for (NakalaMetaData nakalaMetaData : nakalaMetaDatas) {
            // envoie des images à Nakala, dépot temporaire            
            name = "";
            for (String nameTemp : nakalaMetaData.getFileName().getNames()) {
                name = name + nameTemp;
            }
            jsonResponseImage = uploadImage(filePath + name + "." + nakalaMetaData.getFileName().getExtension(), apiKey);
            //jsonResponseImage = uploadImage(name, apiKey);
            /// {"name":"miled.jpg","sha1": "5bdb7f886b69f202a0108c55e3cae57f9787237b"},{"name":"chouette1.jpg;chouette2.jpg","sha1": "5bdb7f886b69f202a0108c55e3cae57f9787237b"}
            if (StringUtils.isEmpty(jsonResponseImage)) {
                System.out.println("Erreur: " + nakalaMetaData.getFileName().getNames().toString());
                continue;
            }
            setSha1(nakalaMetaData, jsonResponseImage);

            /// envoie des Métadonnées 
            doi = addData(nakalaMetaData, apiKey);
            nakalaMetaData.setDoi(doi);
            System.out.println(nakalaMetaData.getFileName().getNames().toString() + " __ " + nakalaMetaData.getFileName().getSha1() + " __ " + nakalaMetaData.getDoi());
        }
        /// réponse= {"code":201,"message":"Data created","payload":{"id":"10.34847\/nkl.c371ov20"}}
        //URI de l'image = https://apitest.nakala.fr/data/10.34847/nkl.2c4b00wx/a4b9288b7918ecedaab4669a9b1639c104245a05
    }

    private void setSha1(NakalaMetaData nakalaMetaData, String jsonData) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonData));
        JsonObject object = jsonReader.readObject();
        String sha1 = object.getString("sha1");
        nakalaMetaData.getFileName().setSha1(sha1);
    }

    /**
     * // curl -X POST "https://apitest.nakala.fr/datas/uploads" -H "accept:
     * application/json" -H "X-API-KEY: 01234567-89ab-cdef-0123-456789abcdef" -H
     * "Content-Type: multipart/form-data" -F "file=@Capture.png;type=image/png"
     * Fonctionne, validé par Miled
     */
    private String uploadImage(String imagePath, String apiKey) {
//            return "{\"name\":\""+ imagePath + "\",\"sha1\": \"5bdb7f886b69f202a0108c55e3cae57f9787237b"+ imagePath+ "\"}";
        try {
            String boundary = Long.toHexString(System.currentTimeMillis());
            String charset = "UTF-8";
            File file = new File(imagePath);

            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            URL url = new URL("https://api.nakala.fr/datas/uploads");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("X-API-KEY", apiKey);

            try (
                    OutputStream output = connection.getOutputStream(); PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);) {
                // Send binary file.
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append(CRLF);
                writer.append("Content-Type: application/octet-stream").append(CRLF);
                writer.append(CRLF).flush();
                Files.copy(file.toPath(), output);
                output.flush(); // Important before continuing with writer!
                writer.append(CRLF).flush();

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF).flush();
            } catch (IOException ex) {
                Logger.getLogger(AddImagesToNakala.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Get the response.
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_CREATED) {
                try (
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        //    System.out.println(line);
                        return line;
                        /// {"name":"miled.jpg","sha1": »5bdb7f886b69f202a0108c55e3cae57f9787237b"}
                    }
                }
            } else {
                System.out.println("Error: " + responseCode + " " + connection.getResponseMessage());
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(AddImagesToNakala.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(AddImagesToNakala.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AddImagesToNakala.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private String getJsonFromNakalaObject(NakalaMetaData nakalaMetaData) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        
        /// collections
        if(nakalaMetaData.getCollectionsIds() == null || nakalaMetaData.getCollectionsIds().isEmpty()) {
        } else {
            JsonArrayBuilder jobArrayCollection = Json.createArrayBuilder();
            for (String collectionsId : nakalaMetaData.getCollectionsIds()) {
                jobArrayCollection.add(collectionsId);
            }
            if(nakalaMetaData.getCollectionsIds() == null || nakalaMetaData.getCollectionsIds().isEmpty()) {

            } else {
                if(jobArrayCollection != null) {
                    jsonObjectBuilder.add("collectionsIds", jobArrayCollection.build());
                }
            }            
        }        
        
        /// files datas
        JsonArrayBuilder jobArrayFiles = Json.createArrayBuilder();
        for (String fileName : nakalaMetaData.getFileName().getNames()) {
            JsonObjectBuilder jsonObjectFile = Json.createObjectBuilder();
            jsonObjectFile.add("name", fileName + "." + nakalaMetaData.getFileName().getExtension() + "|null");
            jsonObjectFile.add("sha1", nakalaMetaData.getFileName().getSha1());
            if(!StringUtils.isEmpty(nakalaMetaData.getFileName().getEmbargoed()))
                jsonObjectFile.add("embargoed", nakalaMetaData.getFileName().getEmbargoed());
            if(!StringUtils.isEmpty(nakalaMetaData.getFileName().getDescription()))
                jsonObjectFile.add("description", nakalaMetaData.getFileName().getDescription());      
            jobArrayFiles.add(jsonObjectFile.build());
        }
        if(jobArrayFiles != null) {
            jsonObjectBuilder.add("files", jobArrayFiles.build());
        }
        
        /// Status      
        jsonObjectBuilder.add("status", nakalaMetaData.getStatus());
        
        /// Métas
        JsonArrayBuilder jobArrayMetas = Json.createArrayBuilder();        
        for (Meta meta : nakalaMetaData.getMeta()) {
            JsonObjectBuilder jsonObject = Json.createObjectBuilder();
            jsonObject.add("value", meta.getValue());
            if(!StringUtils.isEmpty(meta.getLang()))
                jsonObject.add("lang", meta.getLang());            
            jsonObject.add("typeUri", meta.getTypeUri());
            jsonObject.add("propertyUri", meta.getPropertyUri());
              
            jobArrayMetas.add(jsonObject.build());
        }
        /// cas d'un creator
        if(nakalaMetaData.getMetaCreator() != null) {
            JsonObjectBuilder jsonObject = Json.createObjectBuilder();            
            JsonObjectBuilder jsonObjectCreator = Json.createObjectBuilder();
            
            jsonObjectCreator.add("givenname", nakalaMetaData.getMetaCreator().getGivenname());
            jsonObjectCreator.add("surname", nakalaMetaData.getMetaCreator().getSurname());
            
            jsonObject.add("value", jsonObjectCreator.build());
            jsonObject.add("propertyUri", nakalaMetaData.getMetaCreator().getPropertyUri());
            
            jobArrayMetas.add(jsonObject.build());
        }
        
        if(jobArrayMetas != null) {
            jsonObjectBuilder.add("metas", jobArrayMetas.build());
        }        
        return jsonObjectBuilder.build().toString();
    }    

    /**
     *
     * String curl = "curl -X POST \"https://apitest.nakala.fr/datas\" -H
     * \"accept: application/json\" -H \"X-API-KEY:
     * 01234567-89ab-cdef-0123-456789abcdef\"" + " -H \"Content-Type:
     * application/json\" -d " + "\"{ \\\"files\\\": [ { \\\"name\\\":
     * \\\"miled.jpg|null\\\", \\\"sha1\\\":
     * \\\"5bdb7f886b69f202a0108c55e3cae57f9787237b\\\", \\\"embargoed\\\":
     * \\\"2021-03-27\\\", \\\"description\\\": \\\"pour tester\\\" } ],
     * \\\"status\\\": \\\"pending\\\", \\\"metas\\\": [ { \\\"value\\\":
     * \\\"http://purl.org/coar/resource_type/c_c513\\\", \\\"lang\\\":
     * \\\"fr\\\", \\\"typeUri\\\":
     * \\\"http://www.w3.org/2001/XMLSchema#anyURI\\\", \\\"propertyUri\\\":
     * \\\"http://nakala.fr/terms#type\\\" }, { \\\"value\\\": \\\"photo de
     * miled\\\", \\\"lang\\\": \\\"fr\\\", \\\"typeUri\\\":
     * \\\"http://www.w3.org/2001/XMLSchema#string\\\", \\\"propertyUri\\\":
     * \\\"http://nakala.fr/terms#title\\\" } ]}\"";
     *
     */
    /**
     * fonctionne, testé par Miled
     */
    private String addData(NakalaMetaData nakalaMetaData, String apiKey) {
        
        String jsonDatas = getJsonFromNakalaObject(nakalaMetaData);

        String apiUrl = "https://api.nakala.fr/datas";
        try {
            URL url = new URL(apiUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-API-KEY", apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonDatas.getBytes());
            outputStream.flush();

            StringBuffer response;
            
            BufferedReader br;
            String line;
            
            if (100 <= connection.getResponseCode() && connection.getResponseCode() <= 399) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    response = new StringBuffer();                
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    connection.disconnect();
                    return response.toString();
                   // System.out.println(response.toString());
                }
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                connection.disconnect();
                return null;
            }              
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
    /// réponse= {"code":201,"message":"Data created","payload":{"id":"10.34847\/nkl.c371ov20"}}
    //URI de l'image = https://apitest.nakala.fr/data/10.34847/nkl.2c4b00wx/a4b9288b7918ecedaab4669a9b1639c104245a05

    

    /**
     * récupère les noms des fichiers
     *
     * @param csvFile
     * @param delimiter
     * @return
     */
    private ArrayList<NakalaMetaData> getDataFromCSV(String csvFile, char delimiter) {

        ArrayList<NakalaMetaData> nakalaMetaDatas = new ArrayList<>();

        try (FileReader reader = new FileReader(csvFile); CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader()
                .setIgnoreEmptyLines(true).setIgnoreHeaderCase(true).setTrim(true).setDelimiter(delimiter).build())) {
            String multiFiles;

            for (CSVRecord csvRecord : csvParser) {
                NakalaMetaData nakalaMetaData = new NakalaMetaData();
                ArrayList<Meta> metas = new ArrayList<>();

                ///// traitement du nom de fichier et des métas liées
                multiFiles = csvRecord.get("name");
                if (StringUtils.isEmpty(multiFiles)) {
                    continue;
                }
                FileName fileName = new FileName();
                fileName.setNames(getMultiFileNames(multiFiles));
                fileName.setExtension(csvRecord.get("extension"));
                fileName.setEmbargoed(csvRecord.get("embargoed"));
                fileName.setDescription(csvRecord.get("description"));
                nakalaMetaData.setFileName(fileName);

                ///// traitement des collections
                try {
                    ArrayList<String> collectionIds = getCollectionIds(csvRecord.get("collectionsIds"));
                    if (collectionIds == null || collectionIds.isEmpty()) {
                    } else {
                        nakalaMetaData.setCollectionsIds(collectionIds);
                    }                    
                } catch (Exception e) {
                }

                //// traitement du status
                nakalaMetaData.setStatus(csvRecord.get("Status")); // published pending

                //// traitement des métasDonnées Creator
                MetaCreator metaCreator = getCreator(csvRecord.get("Creator"));
                if (metaCreator != null) {
                    metaCreator.setPropertyUri("http://nakala.fr/terms#creator");
                    nakalaMetaData.setMetaCreator(metaCreator);
                }

                //// traitement des métaDonnées
                // type
                metas.add(getMeta(csvRecord.get("Type"), "http://www.w3.org/2001/XMLSchema#anyURI", "http://nakala.fr/terms#type", null));
                // identifier
                metas.add(getMeta(csvRecord.get("identifier"), "http://www.w3.org/2001/XMLSchema#string", "http://purl.org/dc/terms/identifier", null));
                // title
                metas.add(getMeta(csvRecord.get("title"), "http://www.w3.org/2001/XMLSchema#string", "http://nakala.fr/terms#title", csvRecord.get("lang")));
                // description
                metas.add(getMeta(csvRecord.get("description"), "http://www.w3.org/2001/XMLSchema#string", "http://purl.org/dc/terms/description", csvRecord.get("lang")));
                // subject
                metas = getMultiSubjects(metas, csvRecord.get("subject"), "http://www.w3.org/2001/XMLSchema#string", "http://purl.org/dc/terms/subject", csvRecord.get("lang"));
                // created
                metas.add(getMeta(csvRecord.get("created"), "http://www.w3.org/2001/XMLSchema#string", "http://nakala.fr/terms#created", null));
                // licence
                metas.add(getMeta(csvRecord.get("licence"), "http://www.w3.org/2001/XMLSchema#string", "http://nakala.fr/terms#license", null));
                // rights
                metas.add(getMeta(csvRecord.get("rights"), "http://www.w3.org/2001/XMLSchema#string", "http://purl.org/dc/terms/rights", null));

                nakalaMetaData.setMeta(metas);

                nakalaMetaDatas.add(nakalaMetaData);
            }
            return nakalaMetaDatas;
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        return null;
    }

    private Meta getMeta(String metaType, String typeUri, String propertyUri, String lang) {
        Meta meta = new Meta();
        meta.setValue(metaType);
        meta.setTypeUri(typeUri);
        meta.setPropertyUri(propertyUri);
        meta.setLang(lang);
        return meta;
    }

    private ArrayList<Meta> getMultiSubjects(ArrayList<Meta> metas, String multiSubject, String typeUri, String propertyUri, String lang) {
        String[] multi = multiSubject.split("##");
        for (String subject : multi) {
            metas.add(getMeta(subject, typeUri, propertyUri, lang));
        }
        return metas;
    }

    private ArrayList<String> getMultiFileNames(String multiFiles) {
        ArrayList<String> fileNames = new ArrayList<>();
        String[] multiFileName = multiFiles.split("##");
        fileNames.addAll(Arrays.asList(multiFileName));
        return fileNames;
    }

    private ArrayList<String> getCollectionIds(String collectionIds) {
        if (StringUtils.isEmpty(collectionIds)) {
            return null;
        }

        ArrayList<String> collections = new ArrayList<>();
        String[] multiCollections = collectionIds.split("##");
        collections.addAll(Arrays.asList(multiCollections));
        return collections;
    }

    private MetaCreator getCreator(String creator) {
        MetaCreator metaCreator = new MetaCreator();
        String[] multi = creator.split(",");
        if (multi == null) {
            return null;
        }
        if (multi.length == 1) {
            metaCreator.setGivenname(multi[0]);
        }
        if (multi.length == 2) {
            metaCreator.setGivenname(multi[0]);
            metaCreator.setSurname(multi[1]);
        }
        return metaCreator;
    }


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     *
     * String curl = "curl -X POST \"https://apitest.nakala.fr/datas\" -H
     * \"accept: application/json\" -H \"X-API-KEY:
     * 01234567-89ab-cdef-0123-456789abcdef\"" + " -H \"Content-Type:
     * application/json\" -d " + "\"{ \\\"files\\\": [ { \\\"name\\\":
     * \\\"miled.jpg|null\\\", \\\"sha1\\\":
     * \\\"5bdb7f886b69f202a0108c55e3cae57f9787237b\\\", \\\"embargoed\\\":
     * \\\"2021-03-27\\\", \\\"description\\\": \\\"pour tester\\\" } ],
     * \\\"status\\\": \\\"pending\\\", \\\"metas\\\": [ { \\\"value\\\":
     * \\\"http://purl.org/coar/resource_type/c_c513\\\", \\\"lang\\\":
     * \\\"fr\\\", \\\"typeUri\\\":
     * \\\"http://www.w3.org/2001/XMLSchema#anyURI\\\", \\\"propertyUri\\\":
     * \\\"http://nakala.fr/terms#type\\\" }, { \\\"value\\\": \\\"photo de
     * miled\\\", \\\"lang\\\": \\\"fr\\\", \\\"typeUri\\\":
     * \\\"http://www.w3.org/2001/XMLSchema#string\\\", \\\"propertyUri\\\":
     * \\\"http://nakala.fr/terms#title\\\" } ]}\"";
     *
     */
    /// à supprimer
    @Test
    public void addMetaData() {

        /*
            curl -X POST "https://apitest.nakala.fr/datas" -H "accept: application/json" -H "X-API-KEY: 01234567-89ab-cdef-0123-456789abcdef" -H "Content-Type: application/json" -d         
         */
 /*
            {
              "files": [
                {
                  "name": "miled.jpg|null",
                  "sha1": "5bdb7f886b69f202a0108c55e3cae57f9787237b",
                  "embargoed": "2021-03-27",
                  "description": "pour tester"
                }
              ],
              "status": "pending",
              "metas": [
                {
                  "value": "http://purl.org/coar/resource_type/c_c513",
                  "lang": "fr",
                  "typeUri": "http://www.w3.org/2001/XMLSchema#anyURI",
                  "propertyUri": "http://nakala.fr/terms#type"
                },
                {
                  "value": "photo de miled",
                  "lang": "fr",
                  "typeUri": "http://www.w3.org/2001/XMLSchema#string",
                  "propertyUri": "http://nakala.fr/terms#title"
                }
              ]
            }        
        
         */
        String jsonDatas = "{"
                + "    \"collectionsIds\": ["
                + "        \"10.34847/nkl.3add3v2o\" "
                + "    ],"
                + "    \"status\": \"published\","
                + "    \"files\": ["
                + "        {"
                + "            \"name\": \"broken_surface.jpg\","
                + "            \"sha1\": \"5bdb7f886b69f202a0108c55e3cae57f9787237b\","
                + "            \"embargoed\": \"2021-08-30\""
                + "        }"
                + "    ],"
                + "    \"metas\": ["
                + "        {"
                + "            \"value\": \"http://purl.org/coar/resource_type/c_c513\","
                + "            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#anyURI\","
                + "            \"propertyUri\": \"http://nakala.fr/terms#type\""
                + "        },"
                + "        {"
                + "            \"value\": \"55555\","
                + "            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\","
                + "            \"propertyUri\": \"http://purl.org/dc/terms/identifier\""
                + "        },"
                + "        {"
                + "            \"value\": \"Martin-pêcheur\","
                + "            \"lang\": \"fr\","
                + "            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\","
                + "            \"propertyUri\": \"http://nakala.fr/terms#title\""
                + "        },"
                + "        {"
                + "            \"value\": {"
                + "                \"givenname\": \"Rousset\","
                + "                \"surname\": \"Miled\""
                + "            },"
                + "            \"propertyUri\": \"http://nakala.fr/terms#creator\""
                + "        },"
                + "        {"
                + "            \"value\": \"2000-10-21\","
                + "            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\","
                + "            \"propertyUri\": \"http://nakala.fr/terms#created\""
                + "        },"
                + "        {"
                + "            \"value\": \"CC-BY-4.0\","
                + "            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\","
                + "            \"propertyUri\": \"http://nakala.fr/terms#license\""
                + "        },"
                + "        {"
                + "            \"value\": \"Une photo de martin-pêcheur\","
                + "            \"lang\": \"fr\","
                + "            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\","
                + "            \"propertyUri\": \"http://purl.org/dc/terms/description\""
                + "        },"
                + "        {"
                + "            \"value\": \"oiseau\","
                + "            \"lang\": \"fr\","
                + "            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\","
                + "            \"propertyUri\": \"http://purl.org/dc/terms/subject\""
                + "        },"
                + "        {"
                + "            \"value\": \"martin-pêcheur\","
                + "            \"lang\": \"fr\","
                + "            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\","
                + "            \"propertyUri\": \"http://purl.org/dc/terms/subject\""
                + "        }"
                + "    ]"
                + "}";

        String apiUrl = "https://apitest.nakala.fr/datas";
        String apiKey = "01234567-89ab-cdef-0123-456789abcdef";
        try {
            URL url = new URL(apiUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("X-API-KEY", apiKey);
            connection.setRequestProperty("Content-Type", "application/json"); // replace with your request content type
            connection.setDoOutput(true);

            //    String postData = "{\"key1\":\"value1\",\"key2\":\"value2\"}"; // replace with your POST request data
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonDatas.getBytes());
            outputStream.flush();
            
            BufferedReader br = null;
            String line;
            StringBuffer response;
            
            if (100 <= connection.getResponseCode() && connection.getResponseCode() <= 399) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    response = new StringBuffer();                
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println(response.toString());
                }
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                System.out.println("Erreur : " + br.toString());
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }                
            }               

            connection.disconnect();
            /*   HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-API-KEY", apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            int status = conn.getResponseCode();
            if (status != 200) {
                return;
            }
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((output = br.readLine()) != null) {
                json += output;
            }
            br.close();
            //return getCountFromJson(json);

             */

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
