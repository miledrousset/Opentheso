package fr.cnrs.opentheso.client.github;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class GitHubClient {

    public static final String RELEASES_API_URL = " https://api.github.com/repos/miledrousset/Opentheso2/releases";
    public static final String TAGS_API_URL = " https://api.github.com/repos/miledrousset/Opentheso2/tags?sort=created&direction=desc";


    public static String getResponse(String apiURL) throws IOException {
        HttpURLConnection connection = createRequest(apiURL);
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                connection.disconnect();
                return response.toString();
            }
        } else {
            System.out.println("La requête a échoué avec le HTTP code : " + responseCode);
            connection.disconnect();
            return null;
        }
    }

    private static HttpURLConnection createRequest(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        return connection;
    }
}
