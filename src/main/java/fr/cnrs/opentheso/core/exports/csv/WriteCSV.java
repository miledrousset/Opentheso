package fr.cnrs.opentheso.core.exports.csv;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.skosapi.SKOSDate;
import fr.cnrs.opentheso.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSLabel;
import fr.cnrs.opentheso.skosapi.SKOSMatch;
import fr.cnrs.opentheso.skosapi.SKOSNotation;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;

import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class WriteCSV {

    private final String delim_multi_datas = "##";

    private char seperate;
    private BufferedWriter writer;

    /**
     * export un thésaurus en format csv
     *
     * @param xmlDocument
     * @param selectedLanguages
     * @param seperate
     */
    
    
    public byte[] importCsv(SKOSXmlDocument xmlDocument, List<NodeLangTheso> selectedLanguages, char seperate) {
        if (selectedLanguages == null || selectedLanguages.isEmpty()) {
            return null;
        }
        try ( ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            this.writer = new BufferedWriter(new OutputStreamWriter(output));
            this.seperate = seperate;

            // write header record
            //URI rdf:type
            StringBuilder header = new StringBuilder();
            header.append("URI").append(seperate)
                    .append("rdf:type").append(seperate);

            header.append("localURI").append(seperate);

            header.append("identifier").append(seperate)
                    .append("arkId").append(seperate);

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

            //skos:editorialNote
            langs.forEach((lang) -> {
                header.append("skos:editorialNote@").append(lang).append(seperate);
            });            

            header.append("skos:notation").append(seperate)
                    .append("skos:narrower").append(seperate)
                    .append("narrowerId").append(seperate)
                    .append("skos:broader").append(seperate)
                    .append("broaderId").append(seperate)
                    .append("skos:related").append(seperate)
                    .append("relatedId").append(seperate)
                    .append("skos:exactMatch").append(seperate)
                    .append("skos:closeMatch").append(seperate)
                    .append("geo:lat").append(seperate)
                    .append("geo:long").append(seperate)
                    .append("geo:gps").append(seperate)
                    .append("skos:member").append(seperate)
                    .append("dct:created").append(seperate)
                    .append("dct:modified");

            writer.write(header.toString());
            writer.newLine();

            xmlDocument.getGroupList().forEach(groupe -> {
                try {
                    writeResource(groupe, "skos:Collection", langs);
                } catch (IOException e) {
                    System.err.println(e.toString());
                }
            });

            // write all concepts
            xmlDocument.getConceptList().forEach(concept -> {
                try {
                    writeResource(concept, "skos:Concept", langs);
                } catch (IOException e) {
                    System.err.println(e.toString());
                }
            });

            writer.close();

            return output.toByteArray();

        } catch (IOException ex) {
            return null;
        }
    }

    private void writeResource(SKOSResource skosResource, String type, List<String> langs) throws IOException {
        StringBuilder stringBuffer = new StringBuilder();

        //URI + rdf:type        
        stringBuffer.append(skosResource.getUri()).append(seperate) //URI
                .append(type).append(seperate);//rdf:type

        //localURI
        stringBuffer.append(skosResource.getLocalUri()).append(seperate);

        // identifier and arkId
        if (skosResource.getSdc() != null && skosResource.getSdc().getIdentifier() != null) {
            stringBuffer.append(skosResource.getSdc().getIdentifier());
        }
        stringBuffer.append(seperate);
        if (skosResource.getArkId() != null && !skosResource.getArkId().isEmpty()) {
            stringBuffer.append(skosResource.getArkId());
        }
        stringBuffer.append(seperate);

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
        //skos:editorialNote
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.editorialNote);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            stringBuffer.append(def).append(seperate);//editorialNote
        }        
        
        stringBuffer
                .append(getNotation(skosResource.getNotationList())).append(seperate) //notation

                .append(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.narrower)).append(seperate) //narrower
                .append(getRelationGivenValueId(skosResource.getRelationsList(), SKOSProperty.narrower)).append(seperate) //narrowerId

                .append(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.broader)).append(seperate) //broader
                .append(getRelationGivenValueId(skosResource.getRelationsList(), SKOSProperty.broader)).append(seperate) //broaderId      

                .append(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.related)).append(seperate) //related      
                .append(getRelationGivenValueId(skosResource.getRelationsList(), SKOSProperty.related)).append(seperate) //relatedId                

                .append(getAlligementValue(skosResource.getMatchList(), SKOSProperty.exactMatch)).append(seperate) //exactMatch
                .append(getAlligementValue(skosResource.getMatchList(), SKOSProperty.closeMatch)).append(seperate) //closeMatch
                .append(getLatValue(skosResource.getGpsCoordinates())).append(seperate)//geo:lat
                .append(getLongValue(skosResource.getGpsCoordinates())).append(seperate)//geo:long
                .append(getGps(skosResource.getGpsCoordinates())).append(seperate)//gps
                .append(getMemberValue(skosResource.getRelationsList())).append(seperate)//skos:member
                .append(getDateValue(skosResource.getDateList(), SKOSProperty.created)).append(seperate)//sdct:created
                .append(getDateValue(skosResource.getDateList(), SKOSProperty.modified));//dct:modified
        writer.write(stringBuffer.toString());
        writer.newLine();
    }

    private String getLatValue(List<SKOSGPSCoordinates> coordinates) {
        if (CollectionUtils.isNotEmpty(coordinates) && coordinates.size() == 1) {
            if (coordinates.get(0).getLat() == null) {
                return "";
            }
            return coordinates.get(0).getLat();
        }
        /*
        if (CollectionUtils.isNotEmpty(coordinates)) {
            return coordinates.stream()
                    .map(SKOSGPSCoordinates::getLat)
                    .collect(Collectors.joining(delim_multi_datas));
        }*/
        return "";
    }

    private String getLongValue(List<SKOSGPSCoordinates> coordinates) {
        if (CollectionUtils.isNotEmpty(coordinates) && coordinates.size() == 1) {
            if (coordinates.get(0).getLon() == null) {
                return "";
            }
            return coordinates.get(0).getLon();
        }
        /*
        if (CollectionUtils.isNotEmpty(coordinates)) {
            return coordinates.stream()
                    .map(SKOSGPSCoordinates::getLat)
                    .collect(Collectors.joining(delim_multi_datas));
        }*/
        return "";
    }

    private String getGps(List<SKOSGPSCoordinates> coordinates) {
        if (CollectionUtils.isNotEmpty(coordinates) && coordinates.size() > 1) {
            StringBuilder result = new StringBuilder();
            for (SKOSGPSCoordinates gps : coordinates) {
                result.append(gps.toString()).append(" ");
            }

            if (result.length() > 0) {
                result.deleteCharAt(result.length() - 1);
            }

            return result.toString();
        } else {
            return "";
        }
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

    private String getRelationGivenValueId(List<SKOSRelation> relations, int propertie) {
        return relations.stream()
                .filter(relation -> relation.getProperty() == propertie)
                .map(relation -> relation.getLocalIdentifier())
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

    public byte[] importTreeCsv(String[][] tab, char seperate) {
        try ( ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            this.writer = new BufferedWriter(new OutputStreamWriter(output));
            this.seperate = seperate;

            for (int ii = 0; ii < tab.length; ii++) {
                StringBuilder stringBuffer = new StringBuilder();  
                for (int jj = 0; jj < tab[ii].length; jj++) {
                    if (StringUtils.isNotEmpty(tab[ii][jj])) {
                        stringBuffer.append(tab[ii][jj]).append(seperate);
                    } else {
                        stringBuffer.append("").append(seperate);
                    }
                }
                writer.write(stringBuffer.toString());
                writer.newLine();
            }
            
            writer.close();
            return output.toByteArray();
        } catch (IOException ex) {
            return null;
        }
    }

}
