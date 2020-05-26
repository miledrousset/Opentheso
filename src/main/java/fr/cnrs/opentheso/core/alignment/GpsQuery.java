/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author antonio.perez
 */
public class GpsQuery {

    private ArrayList<NodeAlignment> listeAlign;

    public ArrayList<NodeAlignment> queryGps2(String idC, String idTheso,
            String lexicalValue, String lang,
            String requete) {

        ArrayList<NodeLangTheso> nodeLangs;

        
        
        try {
            lexicalValue = URLEncoder.encode(lexicalValue, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(GpsQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        lexicalValue = lexicalValue.replaceAll(" ", "%20");
        
        
        requete = requete.replace("##lang##", lang);
        requete = requete.replace("##value##", lexicalValue);
        try {
            //URL url = new URL("https://" + lang + ".wikipedia.org/w/api.php?action=query&list=search&srwhat=text&format=xml&srsearch=" + lexicalValue + "&srnamespace=0");


        //    requete = URLEncoder.encode(requete, "UTF-8");
            URL url = new URL(requete);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String xmlRecord = "";
            while ((output = br.readLine()) != null) {
                xmlRecord += output;
            }
            byte[] bytes = xmlRecord.getBytes();

            xmlRecord = new String(bytes, Charset.forName("UTF-8"));

            conn.disconnect();

            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xmlRecord));

                org.w3c.dom.Document doc = db.parse(is);
                NodeList nodes = doc.getElementsByTagName("geoname");
                    listeAlign = new ArrayList<>();
                // iterate the employees
                for (int i = 0; i < nodes.getLength(); i++) {

                    nodeLangs = new ArrayList<>();
                    org.w3c.dom.Element element = (org.w3c.dom.Element) nodes.item(i);
                    NodeAlignment nodeAlignment = new NodeAlignment();
                    NodeList nodeList = element.getElementsByTagName("name");
                    for (int j = 0; j < nodeList.getLength(); j++) {
                        nodeAlignment.setName(nodeList.item(j).getTextContent());
                    }
                    nodeList = element.getElementsByTagName("lat");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            nodeAlignment.setLat(Double.parseDouble(nodeList.item(j).getTextContent()));
                        }
                    }
                    nodeList = element.getElementsByTagName("lng");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            nodeAlignment.setLng(Double.parseDouble(nodeList.item(j).getTextContent()));
                        }
                    }
                    nodeList = element.getElementsByTagName("geonameId");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            nodeAlignment.setIdUrl("http://www.geonames.org/" + (nodeList.item(j).getTextContent()));
                        }
                    }
                    nodeList = element.getElementsByTagName("countryName");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            nodeAlignment.setCountryName(nodeList.item(j).getTextContent());
                        }
                    }
                    nodeList = element.getElementsByTagName("toponymName");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            nodeAlignment.setToponymName(nodeList.item(j).getTextContent());
                        }
                    }
                    nodeList = element.getElementsByTagName("adminName1");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            nodeAlignment.setAdminName1(nodeList.item(j).getTextContent());
                        }
                    }
                    nodeList = element.getElementsByTagName("adminName2");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            nodeAlignment.setAdminName2(nodeList.item(j).getTextContent());
                        }
                    }                    
                    nodeList = element.getElementsByTagName("alternateName");
                    if (nodeList != null && nodeList.getLength() > 0) {
                        for (int j = 0; j < nodeList.getLength(); j++) {
                            NodeLangTheso nodeLang = new NodeLangTheso();

                            //nodeAlignment.setToponymName(p.item(j).getTextContent());
                            ArrayList<String> langueFound = new ArrayList<>();
                            org.w3c.dom.Element el = (org.w3c.dom.Element) nodeList.item(j);
                            if (el.hasAttribute("lang")) {
                                nodeLang.setCode(el.getAttribute("lang"));
                                nodeLang.setValue(nodeList.item(j).getTextContent());
                            }
                            nodeLangs.add(nodeLang);
                        }
                       nodeAlignment.setAlltraductions(nodeLangs);
                    }
                    listeAlign.add(nodeAlignment);
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
            }

        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return listeAlign;
    }

    public ArrayList<NodeAlignment> queryGps(String idC, String idTheso,
            String lexicalValue, String lang,
            String requete) throws ParserConfigurationException, SAXException {
        listeAlign = new ArrayList<>();

        lexicalValue = lexicalValue.replaceAll(" ", "%20");
        requete = requete.replace("##lang##", lang);
        requete = requete.replace("##value##", lexicalValue);
        try {

            URL url = new URL(requete);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String xmlRecord = "test \n";
            while ((output = br.readLine()) != null) {
                xmlRecord += output;
                xmlRecord += "\n";
            }
            byte[] bytes = xmlRecord.getBytes();
            //xmlRecord = new String(bytes, Charset.forName("UTF-8"));

            conn.disconnect();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xmlRecord));
            org.w3c.dom.Document doc = db.parse(is);

            listeAlign = getlisteAlign(xmlRecord);
            //NodeList nodes = doc.getElementsByTagName("geoname");
            /*try {


                // iterate the employees
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element element = (Element) nodes.item(i);

                    NodeList p = element.getElementsByTagName("toponymName");
                    if (p != null && p.getLength() > 0) {
                        for (int j = 0; j < p.getLength(); j++) {
                            String title = "", snippet = "";
                            Element el = (org.w3c.dom.Element) p.item(j);
                            if (el.hasChildNodes()) {
                                NodeList name = element.getElementsByTagName("name");
                                title = el.getAttribute("title");
                            }
                            if (el.hasAttribute("snippet")) {
                                snippet = el.getAttribute("snippet") + "...";
                            }
                            NodeAlignment na = new NodeAlignment();
                            //si le titre est équivalent, on le place en premier
                            if (lexicalValue.trim().equalsIgnoreCase(title.trim())) {
                                na.setConcept_target(title);
                                na.setDef_target(snippet);
                                na.setInternal_id_concept(idC);
                                na.setInternal_id_thesaurus(idTheso);
                                na.setThesaurus_target("Wikipedia");
                                na.setUri_target("https://" + lang + ".wikipedia.org/wiki/" + title.replaceAll(" ", "_"));
                                listeAlign.add(0, na);
                            } else {
                                na.setConcept_target(title);
                                na.setDef_target(snippet);
                                na.setInternal_id_concept(idC);
                                na.setInternal_id_thesaurus(idTheso);
                                na.setThesaurus_target("Wikipedia");
                                na.setUri_target("https://" + lang + ".wikipedia.org/wiki/" + title.replaceAll(" ", "_"));
                                listeAlign.add(na);
                            }

                        }
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
            }*/

        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return listeAlign;
    }

    private ArrayList<NodeAlignment> getlisteAlign(String xmlrecord) {
        ArrayList<NodeAlignment> listeAlign1 = new ArrayList<>();

//Se crea un SAXBuilder para poder parsear el archivo
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(xmlrecord);
        try {
            //Se crea el documento a traves del archivo
            Document document = (Document) builder.build(xmlFile);

            //Se obtiene la raiz 'tables'
            Element rootNode = document.getRootElement();

            //Se obtiene la lista de hijos de la raiz 'tables'
            List list = rootNode.getChildren("geoname");

            //Se recorre la lista de hijos de 'tables'
            for (int i = 0; i < list.size(); i++) {
                //Se obtiene el elemento 'tabla'
                Element tabla = (Element) list.get(i);

                //Se obtiene la lista de hijos del tag 'tabla'
                List lista_campos = tabla.getChildren();

                //Se recorre la lista de campos
                for (int j = 0; j < lista_campos.size(); j++) {
                    //Se obtiene el elemento 'campo'
                    Element campo = (Element) lista_campos.get(j);

                    //Se obtienen los valores que estan entre los tags '&lt;campo&gt;&lt;/campo&gt;'
                    //Se obtiene el valor que esta entre los tags '&lt;nombre&gt;&lt;/nombre&gt;'
                    String nombre = campo.getChildTextTrim("name");

                    //Se obtiene el valor que esta entre los tags '&lt;tipo&gt;&lt;/tipo&gt;'
                    String tname = campo.getChildTextTrim("toponymName");

                    //Se obtiene el valor que esta entre los tags '&lt;valor&gt;&lt;/valor&gt;'
                    String lat = campo.getChildTextTrim("lat");
                    String lng = campo.getChildTextTrim("lng");

                    System.out.println("\t" + nombre + "\t\t" + tname + "\t\t" + lat + "\t\t" + lng);
                }
            }
        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException jdomex) {
            System.out.println(jdomex.getMessage());
        }

        return listeAlign1;
    }
}
