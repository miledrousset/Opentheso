package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.Data;

@Data
public class SearchCorpus2 {
    private boolean haveCorpus;

    public ArrayList<NodeCorpus> SearchCorpus(ArrayList<NodeCorpus> nodeCorpuses, NodeConcept nodeConcept) {
        haveCorpus = false;
        if (nodeConcept != null) {
            for (NodeCorpus nodeCorpus : nodeCorpuses) {
                // cas où on compose uniquement une URL de lien vers les notices
                if (nodeCorpus.isIsOnlyUriLink()) {
                    if (nodeCorpus.getUriLink().contains("##id##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                    }
                    if (nodeCorpus.getUriLink().contains("##value##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                    }
                } else {
                    // recherche par Id
                    /// pour le count par Id interne
                    if (nodeCorpus.getUriCount().contains("##id##")) {
                        if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                            nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                        }
                    }
                    /// pour le count par Id ark
                    if (nodeCorpus.getUriCount().contains("##arkid##")) {
                        if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                            nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##arkid##", nodeConcept.getConcept().getIdArk()));
                        }
                    }

                    /// pour la construction de l'URL avec Id interne
                    if (nodeCorpus.getUriLink().contains("##id##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                    }
                    /// pour la construction de l'URL avec Id Ark
                    if (nodeCorpus.getUriLink().contains("##arkid##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##arkid##", nodeConcept.getConcept().getIdArk()));
                    }

                    // recherche par value
                    if (nodeCorpus.getUriCount().contains("##value##")) {
                        if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                            nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                        }
                    }
                    if (nodeCorpus.getUriLink().contains("##value##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##value##", nodeConcept.getTerm().getLexical_value()));
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
        nodeCorpus.setCount(getCountOfResourcesFromHttp(nodeCorpus.getUriCount()));
        /*if (nodeCorpus.getUriCount().contains("https://")) {
            nodeCorpus.setCount(getCountOfResourcesFromHttps(nodeCorpus.getUriCount()));
        }
        if (nodeCorpus.getUriCount().contains("http://")) {
            nodeCorpus.setCount(getCountOfResourcesFromHttp(nodeCorpus.getUriCount()));
        }*/
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
            //         System.err.println(jsonText + " #### " + nodeConcept.getConcept().getIdConcept());
            int count = jsonObject.getInt("count");
            if (count > 0) {
                haveCorpus = true;
            }
            return count;
        } catch (Exception e) {
            System.err.println(e + " " + jsonText );
            return -1;
        }
    }    
    
    
/*
    private int getCountOfResourcesFromHttps(String uri) {
        String output;
        String json = "";
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        // récupération du total des notices
        try {
            URL url = new URL(uri);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
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

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((output = br.readLine()) != null) {
                json += output;
            }
            br.close();
            return getCountFromJson(json);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    */    
    
}
