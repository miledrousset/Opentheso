package fr.cnrs.opentheso.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class UrlUtils {

    public static boolean isAPIAvailable(String apiUrl) {
        return true;
    /*    try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Limitez le temps d'attente à 2 secondes
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            
            connection.getResponseCode();
            //int responseCode = connection.getResponseCode();

        //    if (responseCode == HttpURLConnection.HTTP_HTTP_OK) {
          //      System.out.println("L'API est disponible et répond sous 2 seconde.");
                return true;
        //    } else {
            //    System.out.println("L'API a renvoyé une réponse non valide : " + responseCode);
        //        return false;
        //    }
        } catch (Exception e) {
          //  System.out.println("L'API n'est pas disponible : " + e.getMessage());
            return false;
        }*/
    }
}
