/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.csv;

import fr.cnrs.opentheso.skosapi.SKOSCreator;
import fr.cnrs.opentheso.skosapi.SKOSDate;
import fr.cnrs.opentheso.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSLabel;
import fr.cnrs.opentheso.skosapi.SKOSMatch;
import fr.cnrs.opentheso.skosapi.SKOSNotation;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import java.io.ByteArrayOutputStream;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class WriteCSV {

    private final static String SEPERATE = ";";
    
    private ByteArrayOutputStream output;

    /**
     * export un thésaurus en format csv
     *
     * @param xmlDocument
     * @param codeLang
     * @param codeLang2
     */
    public WriteCSV(SKOSXmlDocument xmlDocument, String codeLang, String codeLang2) {

        try {

            // create a writer
            output = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

            // write header record
            StringBuilder header = new StringBuilder();
            header.append("URI").append(SEPERATE)
                    .append("rdf:type").append(SEPERATE)
                    .append("skos:prefLabel@").append(codeLang).append(SEPERATE);
            
            if (!StringUtils.isEmpty(codeLang2)) {
                header.append("skos:prefLabel@").append(codeLang2).append(SEPERATE);
            }
            
            header.append("skos:altLabel@").append(codeLang).append(SEPERATE);
            
            if (!StringUtils.isEmpty(codeLang2)) {
                header.append("skos:altLabel@").append(codeLang2).append(SEPERATE);
            }
            
            header.append("skos:hiddenLabel@").append(codeLang).append(SEPERATE)
                .append("skos:definition@").append(codeLang).append(SEPERATE);
            
            if (!StringUtils.isEmpty(codeLang2)) {
                header.append("skos:definition@").append(codeLang2).append(SEPERATE);
            }
                    
            header.append("skos:scopeNote@").append(codeLang).append(SEPERATE)
                    .append("skos:notation").append(SEPERATE)
                    .append("skos:narrower").append(SEPERATE)
                    .append("skos:broader").append(SEPERATE)
                    .append("skos:related").append(SEPERATE)
                    .append("skos:exactMatch").append(SEPERATE)
                    .append("skos:closeMatch").append(SEPERATE)
                    .append("geo:lat").append(SEPERATE)
                    .append("geo:long").append(SEPERATE)
                    .append("skos:member").append(SEPERATE)
                    .append("dct:created").append(SEPERATE)
                    .append("dct:modified").append(SEPERATE);
                    
            
            writer.write(header.toString());
            writer.newLine();

            // write all records
            xmlDocument.getConceptList().forEach(concept-> {
                try {
                    StringBuilder stringBuffer = new StringBuilder();
                    stringBuffer.append(concept.getUri()).append(SEPERATE)             //URI
                            .append("skos:Concept").append(SEPERATE)         //rdf:type
                            .append(getLabelValue(concept.getLabelsList(), codeLang, SKOSProperty.prefLabel)).append(SEPERATE);  //skos:prefLabel
                    
                    if (!StringUtils.isEmpty(codeLang2)) {
                         stringBuffer.append(getLabelValue(concept.getLabelsList(), codeLang2, SKOSProperty.prefLabel)).append(SEPERATE); //skos:prefLabel2
                    }
                    
                    stringBuffer.append(getLabelValue(concept.getLabelsList(), codeLang, SKOSProperty.altLabel)).append(SEPERATE);   //skos:altLabel
                    
                    if (!StringUtils.isEmpty(codeLang2)) {
                        stringBuffer.append(getLabelValue(concept.getLabelsList(), codeLang2, SKOSProperty.altLabel)).append(SEPERATE);  //skos:altLabel2
                    }
                    
                    stringBuffer.append(getLabelValue(concept.getLabelsList(), codeLang, SKOSProperty.hiddenLabel)).append(SEPERATE)//hiddenLabel
                            .append(getDocumentationValue(concept.getDocumentationsList(), codeLang, SKOSProperty.definition)).append(SEPERATE);//definition
                    
                    if (!StringUtils.isEmpty(codeLang2)) {
                        stringBuffer.append(getDocumentationValue(concept.getDocumentationsList(), codeLang2, SKOSProperty.definition)).append(SEPERATE);//definition2
                    }
                            
                    stringBuffer.append(getDocumentationValue(concept.getDocumentationsList(), codeLang, SKOSProperty.scopeNote)).append(SEPERATE)//scopeNote
                            .append(getNotation(concept.getNotationList())).append(SEPERATE) //notation
                            .append(getRelationGivenValue(concept.getRelationsList(), SKOSProperty.narrower)).append(SEPERATE)  //narrower
                            .append(getRelationGivenValue(concept.getRelationsList(), SKOSProperty.broader)).append(SEPERATE)   //broader
                            .append(getRelationGivenValue(concept.getRelationsList(), SKOSProperty.related)).append(SEPERATE)   //related
                            .append(getAlligementValue(concept.getMatchList(), SKOSProperty.exactMatch)).append(SEPERATE)       //exactMatch
                            .append(getAlligementValue(concept.getMatchList(), SKOSProperty.closeMatch)).append(SEPERATE)       //closeMatch
                            .append(getLatValue(concept.getGPSCoordinates())).append(SEPERATE)//geo:lat
                            .append(getLongValue(concept.getGPSCoordinates())).append(SEPERATE)//geo:long
                            .append(getMemberValue(concept.getCreatorList())).append(SEPERATE)//skos:member
                            .append(getDateValue(concept.getDateList(), SKOSProperty.created)).append(SEPERATE)//sdct:created
                            .append(getDateValue(concept.getDateList(), SKOSProperty.modified)).append(SEPERATE);//dct:modified
                    
                    writer.write(stringBuffer.toString());
                    writer.newLine();
                } catch (IOException ex) {
                    Logger.getLogger(WriteCSV.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            //close the writer
            writer.close();

        } catch (IOException ex) {
            
        }
    }
    
    private String getLatValue(SKOSGPSCoordinates coordinates) {
        if (coordinates != null) {
            return coordinates.getLat();
        } 
        return "";
    }
    
    private String getLongValue(SKOSGPSCoordinates coordinates) {
        if (coordinates != null) {
            return coordinates.getLon();
        } 
        return "";
    }
    
    private String getMemberValue(List<SKOSCreator> creators) {
        if (!CollectionUtils.isEmpty(creators)) {
            return creators.get(0).getCreator();
        } 
        return "";
    }
    
    private String getRelationGivenValue(List<SKOSRelation> relations, int propertie) {
        String value = "";
        for (SKOSRelation relation : relations) {
            if (relation.getProperty() == propertie) {
                value = relation.getTargetUri();
                break;
            } 
        }
        return value;
    }
    
    private String getAlligementValue(List<SKOSMatch> matchs, int propertie) {
        String value = "";
        for (SKOSMatch date : matchs) {
            if (date.getProperty() == propertie) {
                value = date.getValue();
                break;
            } 
        }
        return value;
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
    
    private String getLabelValue(List<SKOSLabel> labels, String lang, int propertie) {
        String value = "";
        for (SKOSLabel label : labels) {
            if (label.getProperty() == propertie && label.getLanguage().equals(lang)) {
                value = label.getLabel();
                break;
            } 
        }
        return value;
    }
    
    private String getDocumentationValue(ArrayList<SKOSDocumentation> documentations, String lang, int propertie) {
        String value = "";
        for (SKOSDocumentation document : documentations) {
            if (document.getProperty() == propertie && document.getLanguage().equals(lang)) {
                value = document.getText();
                break;
            } 
        }
        return value;
    }

    public ByteArrayOutputStream getOutput() {
        return output;
    }

}
