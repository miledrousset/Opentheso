/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.models.alignment.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author miled.rousset
 */
public class CurlHelper {

    private String header1 = "Accept";
    private String header2 = "application/json";
    
    public CurlHelper() {
    }
    
    public String getDatasFromUriHttps(String uri) {
        return getdatasHttps(uri);
    }
    
    public String getDatasFromUriHttp(String uri) {
        return getdatasHttp(uri);
    }    
    
    private String getdatasHttps(String uri) {
        String datas = "";
        HttpsURLConnection conn1;
        InputStream in2;
        try {
            if(!uri.contains("https:"))
                uri = uri.replace("http:", "https:");
            URL url = new URL(uri);
            conn1 = (HttpsURLConnection) url.openConnection();
            conn1.setRequestMethod("GET");
            conn1.setRequestProperty(header1, header2);
            conn1.setUseCaches(false);
            conn1.setDoInput(true);
            conn1.setDoOutput(true);
            in2 = conn1.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in2, "UTF-8"));
            for (String line; (line = reader.readLine()) != null;) {
                datas += line;
            }
            conn1.disconnect();

        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex);
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return datas;
    }
    
    private String getdatasHttp(String uri) {
        String datas = "";
        HttpURLConnection conn1;
        InputStream in2;
        try {
            URL url = new URL(uri);
            conn1 = (HttpURLConnection) url.openConnection();
            conn1.setRequestMethod("GET");
            conn1.setRequestProperty(header1, header2);
            conn1.setUseCaches(false);
            conn1.setDoInput(true);
            conn1.setDoOutput(true);
            in2 = conn1.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in2, "UTF-8"));
            for (String line; (line = reader.readLine()) != null;) {
               datas += line;
            }
            conn1.disconnect();

        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex);
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return datas;
    }    

    public void setHeader1(String header1) {
        this.header1 = header1;
    }
    
    public void setHeader2(String header2) {
        this.header2 = header2;
    }    
}
