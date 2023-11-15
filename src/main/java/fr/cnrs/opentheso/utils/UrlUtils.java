package fr.cnrs.opentheso.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class UrlUtils {

    public static boolean isAPIAvailable(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Limitez le temps d'attente à 2 secondes
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("L'API est disponible et répond sous 5 seconde.");
                return true;
            } else {
                System.out.println("L'API a renvoyé une réponse non valide : " + responseCode);
                return false;
            }
        } catch (Exception e) {
            System.out.println("L'API n'est pas disponible : " + e.getMessage());
            return false;
        }
    }
}
