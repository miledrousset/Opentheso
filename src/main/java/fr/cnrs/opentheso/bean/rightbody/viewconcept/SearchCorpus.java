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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SearchCorpus implements Callable<NodeCorpus> {

    private boolean haveCorpus;
    private NodeCorpus nodeCorpus;
    private NodeConcept nodeConcept;

    public SearchCorpus(NodeCorpus nodeCorpus, NodeConcept nodeConcept) {
        this.nodeCorpus = nodeCorpus;
        this.nodeConcept = nodeConcept;
        haveCorpus = false;
    }

    @Override
    public NodeCorpus call() throws Exception {
        // cas où on compose uniquement une URL de lien vers les notices
        if (nodeCorpus.isIsOnlyUriLink()) {
            if (nodeCorpus.getUriLink().contains("##id##")) {
                nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                haveCorpus = true;
            }
            if (nodeCorpus.getUriLink().contains("##value##")) {
                nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                haveCorpus = true;
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

        return nodeCorpus;
    }

    private void setCorpusCount(NodeCorpus nodeCorpus) {
        if (nodeConcept != null) {
            if (nodeCorpus == null) {
                return;
            }
            nodeCorpus.setCount(getCountOfResourcesFromHttp(nodeCorpus.getUriCount()));
        }
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
            System.err.println(e + " " + jsonText + " " + nodeConcept.getConcept().getIdConcept());
            return -1;
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
