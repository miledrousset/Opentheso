package fr.cnrs.opentheso.client;

import lombok.Data;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;


@Data
public class CurlHelper {

    private String header1 = "Accept";
    private String header2 = "application/json";


    public String getDatasFromUriHttps(String uri) {
        return getdatasHttps(uri);
    }

    public String getDatasFromUriHttp(String uri) {
        return getdatasHttp(uri);
    }
    
    private String getdatasHttps(String uri) {

        var datas = "";
        try {
            if(!uri.contains("https:"))
                uri = uri.replace("http:", "https:");

            var conn1 = (HttpsURLConnection) new URL(uri).openConnection();
            conn1.setRequestMethod("GET");
            conn1.setRequestProperty(header1, header2);
            conn1.setUseCaches(false);
            conn1.setDoInput(true);
            conn1.setDoOutput(true);

            var in2 = conn1.getInputStream();
            var reader = new BufferedReader(new InputStreamReader(in2, "UTF-8"));
            for (String line; (line = reader.readLine()) != null;) {
                datas += line;
            }
            conn1.disconnect();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return datas;
    }
    
    private String getdatasHttp(String uri) {

        String datas = "";

        try {
            URL url = new URL(uri);
            var conn1 = (HttpURLConnection) url.openConnection();
            conn1.setRequestMethod("GET");
            conn1.setRequestProperty(header1, header2);
            conn1.setUseCaches(false);
            conn1.setDoInput(true);
            conn1.setDoOutput(true);

            var in2 = conn1.getInputStream();
            var reader = new BufferedReader(new InputStreamReader(in2, "UTF-8"));
            for (String line; (line = reader.readLine()) != null;) {
               datas += line;
            }
            conn1.disconnect();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return datas;
    }
}
