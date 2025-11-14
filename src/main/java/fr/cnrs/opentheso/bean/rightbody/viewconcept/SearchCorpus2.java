package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import fr.cnrs.opentheso.models.concept.NodeFullConcept;
import fr.cnrs.opentheso.models.nodes.NodeCorpus;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCorpus2 {
    private boolean haveCorpus;

    public List<NodeCorpus> SearchCorpus(List<NodeCorpus> nodeCorpuses, NodeFullConcept nodeFullConcept) {
        haveCorpus = false;
        if (nodeFullConcept != null) {
            for (NodeCorpus nodeCorpus : nodeCorpuses) {
                // cas où on compose uniquement une URL de lien vers les notices
                if (nodeCorpus.isOnlyUriLink()) {
                    if (nodeCorpus.getUriLink().contains("##id##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##id##", nodeFullConcept.getIdentifier()));
                    }
                    if (nodeCorpus.getUriLink().contains("##value##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##value##", nodeFullConcept.getPrefLabel().getLabel()));
                    }
                    haveCorpus = true;
                } else {
                    // recherche par Id
                    /// pour le count par Id interne
                    if (nodeCorpus.getUriCount().contains("##id##")) {
                        if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                            nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##id##", nodeFullConcept.getIdentifier()));
                        }
                    }
                    /// pour le count par Id ark
                    if (nodeCorpus.getUriCount().contains("##arkid##")) {
                        if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                            nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##arkid##", nodeFullConcept.getPermanentId()));
                        }
                    }

                    /// pour la construction de l'URL avec Id interne
                    if (nodeCorpus.getUriLink().contains("##id##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##id##", nodeFullConcept.getIdentifier()));
                    }
                    /// pour la construction de l'URL avec Id Ark
                    if (nodeCorpus.getUriLink().contains("##arkid##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##arkid##", nodeFullConcept.getPermanentId()));
                    }

                    // recherche par value
                    if (nodeCorpus.getUriCount().contains("##value##")) {
                        if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                            try {
                                nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##value##",
                                        URLEncoder.encode(nodeFullConcept.getPrefLabel().getLabel(), StandardCharsets.UTF_8.toString())));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (nodeCorpus.getUriLink().contains("##value##")) {
                        try {
                            nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##value##",
                                    URLEncoder.encode(nodeFullConcept.getPrefLabel().getLabel(), StandardCharsets.UTF_8.toString())));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    setCorpusCount(nodeCorpus);
                }
            }
        }
        return nodeCorpuses;
    }

    private void setCorpusCount(NodeCorpus nodeCorpus) {
        if (nodeCorpus == null) {
            return;
        }
        if(nodeCorpus.isOmekaS()){
            nodeCorpus.setCount(getCountOfResourcesFromOmekaS(nodeCorpus.getUriCount()));
        } else {
            nodeCorpus.setCount(getCountOfResourcesFromHttp(nodeCorpus.getUriCount()));
        }
    }

    private int getCountOfResourcesFromHttp(String uri) {
        String output;
        String json = "";

        // récupération du total des notices
        try {
            URL url = new URL(uri);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int status = conn.getResponseCode();
            if (status != 200) {
                return -1;
            }
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                while ((output = br.readLine()) != null) {
                    json += output;
                }
            }
            return getCountFromJson(json);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        }
        return -1;
    }

    private int getCountFromJson(String jsonText) {
        if (jsonText == null) {
            return -1;
        }
        JsonObject jsonObject;
        try {
            JsonReader reader = Json.createReader(new StringReader(jsonText));
            jsonObject = reader.readObject();
            int count = -1;
            try {
                count = jsonObject.getInt("count");
            } catch (Exception e) {
            }
            ///  récupération du total de HAL SHS
            if(count == -1) {
                try {
                    count = jsonObject.getJsonObject("response").getInt("numFound");
                } catch (Exception e) {
                }
            }
            if (count > 0) {
                haveCorpus = true;
            }
            return count;
        } catch (Exception e) {
            System.err.println(e + " " + jsonText );
            return -1;
        }
    }    

    //// code pour OmekaS
    private int getCountOfResourcesFromOmekaS(String uri) {
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lire les headers de la réponse
            // Convertir les headers en minuscule
            Map<String, List<String>> headers = connection.getHeaderFields();
            Map<String, List<String>> lowerCaseHeaders = headers.entrySet().stream()
                    .collect(Collectors.toMap(entry -> entry.getKey() != null ? entry.getKey().toLowerCase() : "", Map.Entry::getValue));
            List<String> values = lowerCaseHeaders.get("omeka-s-total-results");

            connection.disconnect();

            if (values != null && !values.isEmpty()) {
                int val = Integer.parseInt(values.get(0));
                if(val > 0) haveCorpus = true;
                return val; // Convertir la valeur en entier
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        }
        return -1;
    }

}
