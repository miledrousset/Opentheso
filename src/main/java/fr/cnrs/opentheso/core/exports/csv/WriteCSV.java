/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.csv;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.skosapi.SKOSDate;
import fr.cnrs.opentheso.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSLabel;
import fr.cnrs.opentheso.skosapi.SKOSMatch;
import fr.cnrs.opentheso.skosapi.SKOSNotation;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import java.io.ByteArrayOutputStream;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;


public class WriteCSV {

    private final String delim_multi_datas = "##";
    
    private char seperate;
    private BufferedWriter writer;
    private ByteArrayOutputStream output;

    /**
     * export un thésaurus en format csv
     *
     * @param xmlDocument
     * @param selectedLanguages
     * @param seperate
     */
    public WriteCSV(SKOSXmlDocument xmlDocument, List<NodeLangTheso> selectedLanguages, char seperate) {
        if(selectedLanguages == null || selectedLanguages.isEmpty()) {
            return;
        }
        try {
            this.seperate = seperate;
            // create a writer
            output = new ByteArrayOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(output));

            // write header record
            //URI rdf:type
            StringBuilder header = new StringBuilder();
            header.append("URI").append(seperate)
                    .append("rdf:type").append(seperate);

            List<String> langs = selectedLanguages.stream().map(lang -> lang.getCode()).collect(Collectors.toList());

            //skos:prefLabel
            langs.forEach((lang) -> {
                header.append("skos:prefLabel@").append(lang).append(seperate);
            });

            //skos:altLabel
            langs.forEach((lang) -> {
                header.append("skos:altLabel@").append(lang).append(seperate);
            });
            
            //skos:hiddenLabel
            langs.forEach((lang) -> {
                header.append("skos:hiddenLabel@").append(lang).append(seperate);
            });

            //skos:definition
            langs.forEach((lang) -> {
                header.append("skos:definition@").append(lang).append(seperate);
            });
            
            //skos:scopeNote
            langs.forEach((lang) -> {
                header.append("skos:scopeNote@").append(lang).append(seperate);
            });
            
            //skos:note
            langs.forEach((lang) -> {
                header.append("skos:note@").append(lang).append(seperate);
            });     
            
            //skos:historyNote
            langs.forEach((lang) -> {
                header.append("skos:historyNote@").append(lang).append(seperate);
            });  
            
            
            header.append("skos:notation").append(seperate)
                    .append("skos:narrower").append(seperate)
                    .append("skos:broader").append(seperate)
                    .append("skos:related").append(seperate)
                    .append("skos:exactMatch").append(seperate)
                    .append("skos:closeMatch").append(seperate)
                    .append("geo:lat").append(seperate)
                    .append("geo:long").append(seperate)
                    .append("skos:member").append(seperate)
                    .append("dct:created").append(seperate)
                    .append("dct:modified").append(seperate);

            writer.write(header.toString());
            writer.newLine();

            xmlDocument.getGroupList().forEach(groupe -> {
                try {
                    writeResource(groupe, "skos:Collection", langs);
                } catch (IOException e){ 
                    System.err.println(e.toString());
                }
            });

            // write all concepts
            xmlDocument.getConceptList().forEach(concept -> {
                try {
                    writeResource(concept, "skos:Concept", langs);
                } catch (IOException e){
                    System.err.println(e.toString());                    
                }
            });

            //close the writer
            writer.close();

        } catch (IOException ex) {

        }
    }

    private void writeResource(SKOSResource skosResource, String type, List<String> langs) throws IOException {
        StringBuilder stringBuffer = new StringBuilder();
        
        //URI rdf:type        
        stringBuffer.append(skosResource.getUri()).append(seperate) //URI
                .append(type).append(seperate) ;//rdf:type
        
        //skos:prefLabel
        for (String lang : langs) {
            stringBuffer.append(getPrefLabelValue(skosResource.getLabelsList(), lang, SKOSProperty.prefLabel)).append(seperate); //skos:prefLabel
        }
        
        //skos:altLabel
        for (String lang : langs) {
            stringBuffer.append(getAltLabelValue(skosResource.getLabelsList(), lang, SKOSProperty.altLabel)).append(seperate);  //skos:altLabel
        }
        
        //skos:hiddenLabel
        for (String lang : langs) {
            stringBuffer.append(getAltLabelValue(skosResource.getLabelsList(), lang, SKOSProperty.hiddenLabel)).append(seperate);//hiddenLabel
        }        

        //skos:definition
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.definition);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            stringBuffer.append(def).append(seperate);//definition
        }
        
        //skos:scopeNote
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.scopeNote);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            stringBuffer.append(def).append(seperate);//scopeNote
        }        
        
        //skos:note
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.note);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            stringBuffer.append(def).append(seperate);//note
        }
        
        //skos:historyNote
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.historyNote);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            stringBuffer.append(def).append(seperate);//historyNote
        }        
        
        //        skos:notation
        //        skos:narrower
        //        skos:broader
        //        skos:related
        //        skos:exactMatch
        //        skos:closeMatch
        //        geo:lat
        //        geo:long
        //        skos:member
        //        dct:created
        //        dct:modified
        stringBuffer
                .append(getNotation(skosResource.getNotationList())).append(seperate) //notation
                .append(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.narrower)).append(seperate) //narrower
                .append(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.broader)).append(seperate) //broader
                .append(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.related)).append(seperate) //related
                .append(getAlligementValue(skosResource.getMatchList(), SKOSProperty.exactMatch)).append(seperate) //exactMatch
                .append(getAlligementValue(skosResource.getMatchList(), SKOSProperty.closeMatch)).append(seperate) //closeMatch
                .append(getLatValue(skosResource.getGPSCoordinates())).append(seperate)//geo:lat
                .append(getLongValue(skosResource.getGPSCoordinates())).append(seperate)//geo:long
                .append(getMemberValue(skosResource.getRelationsList())).append(seperate)//skos:member
                .append(getDateValue(skosResource.getDateList(), SKOSProperty.created)).append(seperate)//sdct:created
                .append(getDateValue(skosResource.getDateList(), SKOSProperty.modified)).append(seperate);//dct:modified
        writer.write(stringBuffer.toString());
        writer.newLine();
    }

    private String getLatValue(SKOSGPSCoordinates coordinates) {
        if (coordinates != null) {
            if(coordinates.getLat() == null) return "";
            return coordinates.getLat();
        }
        return "";
    }

    private String getLongValue(SKOSGPSCoordinates coordinates) {
        if (coordinates != null) {
            if(coordinates.getLon() == null) return "";
            return coordinates.getLon();
        }
        return "";
    }

    private String getMemberValue(ArrayList<SKOSRelation> sKOSRelations) {
        
        return sKOSRelations.stream()
                .filter(sKOSRelation -> sKOSRelation.getProperty() == SKOSProperty.memberOf)
                .map(sKOSRelation -> sKOSRelation.getTargetUri())
                .collect(Collectors.joining(delim_multi_datas));
    }

    private String getRelationGivenValue(List<SKOSRelation> relations, int propertie) {
        
        return relations.stream()
                .filter(relation -> relation.getProperty() == propertie)
                .map(relation -> relation.getTargetUri())
                .collect(Collectors.joining(delim_multi_datas));
    }

    private String getAlligementValue(List<SKOSMatch> matchs, int propertie) {
        return matchs.stream()
                .filter(alignment -> alignment.getProperty() == propertie)
                .map(alignment -> alignment.getValue())
                .collect(Collectors.joining(delim_multi_datas));
    }

    public String getNotation(List<SKOSNotation> notations) {
        if (!CollectionUtils.isEmpty(notations)) {
            return notations.get(0).getNotation();
        }
        return "";
    }

    private String getDateValue(List<SKOSDate> dates, int propertie) {
        String value = "";
        for (SKOSDate date : dates) {
            if (date.getProperty() == propertie) {
                value = date.getDate();
                break;
            }
        }
        return value;
    }

    private String getPrefLabelValue(List<SKOSLabel> labels, String lang, int propertie) {
        String value = "";
        for (SKOSLabel label : labels) {
            if (label.getProperty() == propertie && label.getLanguage().equals(lang)) {
                value = label.getLabel();
                break;
            }
        }
        return value;
    }    
    
    private String getAltLabelValue(List<SKOSLabel> labels, String lang, int propertie) {
        
        return labels.stream()
                .filter(label -> label.getProperty() == propertie && label.getLanguage().equals(lang))
                .map(label -> label.getLabel())
                .collect(Collectors.joining(delim_multi_datas));
    }

    private String getDocumentationValue(ArrayList<SKOSDocumentation> documentations, String lang, int propertie) {
        
        return documentations.stream()
                .filter(document -> document.getProperty() == propertie && document.getLanguage().equals(lang))
                .map(document -> document.getText())
                .collect(Collectors.joining(delim_multi_datas));
    }

    public ByteArrayOutputStream getOutput() {
        return output;
    }

}
