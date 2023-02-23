/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package nakala;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class AddImagesToNakala {
    
    public AddImagesToNakala() {
    }

    @Test
    public void hello() {
        ArrayList<Map<String, String>> datas = readFileCsv("/Users/miledrousset/Documents/Nakala/dataset.csv", ';');
        for (Map<String, String> record : datas) {
            for (Map.Entry<String, String> entry : record.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                System.out.println(key + " = " + value);
            }            
        }        
        
        String jsonDatas = "{\n" +
"    \"status\": \"pending\",\n" +
"    \"files\": [\n" +
"        {\n" +
"            \"name\": \"kingfisher.jpg\",\n" +
"            \"sha1\": \"f4ca1456fa44f99a11be0b58cacd11fe86162e2e\",\n" +
"            \"embargoed\": \"2021-08-30\"\n" +
"        }\n" +
"    ],\n" +
"    \"metas\": [\n" +
"        {\n" +
"            \"value\": \"http://purl.org/coar/resource_type/c_c513\",\n" +
"            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#anyURI\",\n" +
"            \"propertyUri\": \"http://nakala.fr/terms#type\"\n" +
"        },\n" +
"        {\n" +
"            \"value\": \"Martin-pêcheur\",\n" +
"            \"lang\": \"fr\",\n" +
"            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\",\n" +
"            \"propertyUri\": \"http://nakala.fr/terms#title\"\n" +
"        },\n" +
"        {\n" +
"            \"value\": {\n" +
"                \"givenname\": \"Jean\",\n" +
"                \"surname\": \"Dupont\"\n" +
"            },\n" +
"            \"propertyUri\": \"http://nakala.fr/terms#creator\"\n" +
"        },\n" +
"        {\n" +
"            \"value\": \"2000-10-21\",\n" +
"            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\",\n" +
"            \"propertyUri\": \"http://nakala.fr/terms#created\"\n" +
"        },\n" +
"        {\n" +
"            \"value\": \"CC-BY-4.0\",\n" +
"            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\",\n" +
"            \"propertyUri\": \"http://nakala.fr/terms#license\"\n" +
"        },\n" +
"        {\n" +
"            \"value\": \"Une photo de martin-pêcheur\",\n" +
"            \"lang\": \"fr\",\n" +
"            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\",\n" +
"            \"propertyUri\": \"http://purl.org/dc/terms/description\"\n" +
"        },\n" +
"        {\n" +
"            \"value\": \"oiseau\",\n" +
"            \"lang\": \"fr\",\n" +
"            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\",\n" +
"            \"propertyUri\": \"http://purl.org/dc/terms/subject\"\n" +
"        },\n" +
"        {\n" +
"            \"value\": \"martin-pêcheur\",\n" +
"            \"lang\": \"fr\",\n" +
"            \"typeUri\": \"http://www.w3.org/2001/XMLSchema#string\",\n" +
"            \"propertyUri\": \"http://purl.org/dc/terms/subject\"\n" +
"        }\n" +
"    ],\n" +
"    \"rights\": [\n" +
"        {\n" +
"            \"id\": \"de0f2a9b-a198-48a4-8074-db5120187a16\",\n" +
"            \"role\": \"ROLE_READER\"\n" +
"        }\n" +
"    ]\n" +
"}";
        
        
        String apiUrl = "https://apitest.nakala.fr";
        String apiKey = "aae99aba-476e-4ff2-2886-0aaf1bfa6fd2";
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
    
    private ArrayList<Map<String, String>> readFileCsv(String csvFile, char delimiter){
        ArrayList<Map<String, String>> datas = new ArrayList<>();

        try (FileReader reader = new FileReader(csvFile);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder().setHeader()
                    .setIgnoreEmptyLines(true).setIgnoreHeaderCase(true).setTrim(true).setDelimiter(delimiter).build())) {
            for (CSVRecord csvRecord : csvParser) {
                Map<String, String> record = new HashMap<>();
                record.put("file", csvRecord.get("file"));
                record.put("status", csvRecord.get("status"));
                record.put("type", csvRecord.get("type"));
                datas.add(record);
            } 
            return datas;
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        return null;
    }

}
