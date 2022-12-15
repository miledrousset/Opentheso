/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.csv;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeDeprecated;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author miledrousset
 */
public class CsvWriteHelper {
    private final String delim_multi_datas = "##";
    public CsvWriteHelper() {
    }

    /**
     * Export en CSV avec tous les champs 
     * @param xmlDocument
     * @param selectedLanguages
     * @param delimiter
     * @return 
     */
    public byte[] writeCsv(SKOSXmlDocument xmlDocument, List<NodeLangTheso> selectedLanguages, char delimiter) {
        if (selectedLanguages == null || selectedLanguages.isEmpty()) {
            return null;
        }        
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (OutputStreamWriter out = new OutputStreamWriter(os, Charset.forName("UTF-8"));
                    CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.RFC4180.builder().setDelimiter(delimiter).build())) {  
                
                // write Headers
                ArrayList<String> header = new ArrayList<>();
                header.add("URI");
                header.add("rdf:type");
                header.add("localURI");
                header.add("identifier");
                header.add("arkId");

                List<String> langs = selectedLanguages.stream().map(lang -> lang.getCode()).collect(Collectors.toList());
                //skos:prefLabel
                langs.forEach((lang) -> {
                     header.add("skos:prefLabel@"+lang);
                });

                //skos:altLabel
                langs.forEach((lang) -> {
                    header.add("skos:altLabel@"+lang);
                });

                //skos:hiddenLabel
                langs.forEach((lang) -> {
                    header.add("skos:hiddenLabel@"+lang);
                });

                //skos:definition
                langs.forEach((lang) -> {
                    header.add("skos:definition@"+lang);                    
                });

                //skos:scopeNote
                langs.forEach((lang) -> {
                    header.add("skos:scopeNote@"+lang);                    
                });

                //skos:note
                langs.forEach((lang) -> {
                    header.add("skos:note@"+lang);                    
                });

                //skos:historyNote
                langs.forEach((lang) -> {
                    header.add("skos:historyNote@"+lang);                       
                });

                //skos:editorialNote
                langs.forEach((lang) -> {
                    header.add("skos:editorialNote@"+lang);                     
                });
                
                header.add("skos:notation");
                header.add("skos:narrower");
                header.add("narrowerId");
                header.add("skos:broader");
                header.add("broaderId");
                header.add("skos:related");
                header.add("relatedId");
                header.add("skos:exactMatch");
                header.add("skos:closeMatch");
                header.add("geo:lat");
                header.add("geo:long");
                header.add("skos:member");
                header.add("dct:created");
                header.add("dct:modified");                
                csvFilePrinter.printRecord(header);                
               
                
                ArrayList<Object> record = new ArrayList<>();                
                // write concepts and collections
                xmlDocument.getGroupList().forEach(groupe -> {
                    try {
                        writeResource(record, csvFilePrinter, groupe, "skos:Collection", langs);
                    } catch (IOException e) {
                        System.err.println(e.toString());
                    }
                });                
                // write all concepts
                xmlDocument.getConceptList().forEach(concept -> {
                    try {
                        writeResource(record, csvFilePrinter, concept, "skos:Concept", langs);
                    } catch (IOException e) {
                        System.err.println(e.toString());
                    }
                });                
            }
            return os.toByteArray();
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }            
    }
    
    private void writeResource(ArrayList<Object> record, CSVPrinter csvFilePrinter,
            SKOSResource skosResource, String type, List<String> langs) throws IOException {

        //URI + rdf:type
        record.add(skosResource.getUri());
        record.add(type);        

        //localURI
        record.add(skosResource.getLocalUri()); 

        // identifier and arkId
        if (skosResource.getSdc() != null && skosResource.getSdc().getIdentifier() != null) {
            record.add(skosResource.getSdc().getIdentifier());             
        } else
            record.add(""); 
        
        if (skosResource.getArkId() != null && !skosResource.getArkId().isEmpty()) {
            record.add(skosResource.getArkId());
        } else
            record.add("");
        
        //skos:prefLabel
        for (String lang : langs) {
            record.add(getPrefLabelValue(skosResource.getLabelsList(), lang, SKOSProperty.prefLabel));            
        }

        //skos:altLabel
        for (String lang : langs) {
            record.add(getAltLabelValue(skosResource.getLabelsList(), lang, SKOSProperty.altLabel));
        }

        //skos:hiddenLabel
        for (String lang : langs) {
            record.add(getAltLabelValue(skosResource.getLabelsList(), lang, SKOSProperty.hiddenLabel));            
        }

        //skos:definition
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.definition);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            record.add(def);            
        }

        //skos:scopeNote
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.scopeNote);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            record.add(def);  
        }

        //skos:note
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.note);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            record.add(def);  
        }

        //skos:historyNote
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.historyNote);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            record.add(def);  
        }
        //skos:editorialNote
        for (String lang : langs) {
            String def = getDocumentationValue(skosResource.getDocumentationsList(), lang, SKOSProperty.editorialNote);
            def = def.replaceAll("amp;", "");
            def = def.replaceAll(";", ",");
            record.add(def);  
        }        
        
        // notation
        record.add(getNotation(skosResource.getNotationList())); 
        
        //narrower 
        record.add(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.narrower));  
        //narrowerId
        record.add(getRelationGivenValueId(skosResource.getRelationsList(), SKOSProperty.narrower));          
        //broader
        record.add(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.broader));  
        //broaderId 
        record.add(getRelationGivenValueId(skosResource.getRelationsList(), SKOSProperty.broader));  
        //related
        record.add(getRelationGivenValue(skosResource.getRelationsList(), SKOSProperty.related)); 
        //relatedId 
        record.add(getRelationGivenValueId(skosResource.getRelationsList(), SKOSProperty.related));
        
        //exactMatch
        record.add(getAlligementValue(skosResource.getMatchList(), SKOSProperty.exactMatch));
        //closeMatch
        record.add(getAlligementValue(skosResource.getMatchList(), SKOSProperty.closeMatch));
        
        //geo:lat
        record.add(getLatValue(skosResource.getGPSCoordinates()));  
        //geo:long
        record.add(getLongValue(skosResource.getGPSCoordinates()));  
        //skos:member
        record.add(getMemberValue(skosResource.getRelationsList()));
        //sdct:created
        record.add(getDateValue(skosResource.getDateList(), SKOSProperty.created));
        //dct:modified
        record.add(getDateValue(skosResource.getDateList(), SKOSProperty.modified));

        csvFilePrinter.printRecord(record);
        record.clear();        
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
    private String getLatValue(SKOSGPSCoordinates coordinates) {
        if (coordinates != null) {
            if (coordinates.getLat() == null) {
                return "";
            }
            return coordinates.getLat();
        }
        return "";
    }

    private String getLongValue(SKOSGPSCoordinates coordinates) {
        if (coordinates != null) {
            if (coordinates.getLon() == null) {
                return "";
            }
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
    
    
    /**
     * Export des données limitées en CSV
     * @param ds
     * @param idTheso
     * @param idLang
     * @param idGroups
     * @param delimiter
     * @return 
     */
    public byte[] writeCsvById(HikariDataSource ds, String idTheso, String idLang, List<String> idGroups, char delimiter){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (OutputStreamWriter out = new OutputStreamWriter(os, Charset.forName("UTF-8"));
                    CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.RFC4180.builder().setDelimiter(delimiter).build())) {

                /// écriture des headers
                ArrayList<String> header = new ArrayList<>();
                header.add("conceptId");
                header.add("arkId");
                header.add("handleId");
                header.add("prefLabel");
                header.add("altLabel");
                header.add("definition");
                header.add("alignment");
                csvFilePrinter.printRecord(header);

                ConceptHelper conceptHelper = new ConceptHelper();
                ArrayList <String> idConcepts = null;
                if(idGroups == null || idGroups.isEmpty())
                    idConcepts = conceptHelper.getAllIdConceptOfThesaurus(ds, idTheso);
                else {
                    if(idConcepts == null)
                        idConcepts = new ArrayList<>();
                    ArrayList <String> idConceptsTemp;
                    for (String idGroup : idGroups) {
                        idConceptsTemp = conceptHelper.getAllIdConceptOfThesaurusByGroup(ds, idTheso, idGroup);
                        if(idConceptsTemp != null)
                            idConcepts.addAll(idConceptsTemp);
                    }
                }

                if(idConcepts == null) return null;

                NodeConcept nodeConcept;
                /// écritures des données
                ArrayList<Object> record = new ArrayList<>();

                String repeatedValue = "";
                boolean first = true;
                for (String idConcept : idConcepts) {
                    try {
                        nodeConcept = conceptHelper.getConcept(ds, idConcept, idTheso, idLang, -1, -1);
                        record.add(nodeConcept.getConcept().getIdConcept());
                        record.add(nodeConcept.getConcept().getIdArk());
                        record.add(nodeConcept.getConcept().getIdHandle());
                        record.add(nodeConcept.getTerm().getLexical_value());
                        for (NodeEM nodeEM : nodeConcept.getNodeEM()) {
                            if(first) {
                                repeatedValue = nodeEM.getLexical_value();
                                first = false;
                            } else {
                                repeatedValue = repeatedValue + delim_multi_datas + nodeEM.getLexical_value();
                            }
                        }
                        first = true;
                        record.add(repeatedValue);
                        repeatedValue = "";
                        for (NodeNote nodeNote : nodeConcept.getNodeNotesTerm()) {
                            if("definition".equalsIgnoreCase(nodeNote.getNotetypecode()))
                                if(first) {
                                    repeatedValue = nodeNote.getLexicalvalue();
                                    first = false;
                                } else {
                                    repeatedValue = repeatedValue + delim_multi_datas + nodeNote.getLexicalvalue();
                                }
                        }
                        record.add(repeatedValue);
                        repeatedValue = "";
                        first = true;
                        for (NodeAlignment nodeAlignment : nodeConcept.getNodeAlignments()) {
                            if(first) {
                                repeatedValue = nodeAlignment.getAlignmentLabelType() +  ":" + nodeAlignment.getUri_target();
                                first = false;
                            } else {
                                repeatedValue = repeatedValue + delim_multi_datas + nodeAlignment.getAlignmentLabelType() +  ":" + nodeAlignment.getUri_target();
                            }
                        }
                        record.add(repeatedValue);
                        repeatedValue = "";
                        first = true;

                        csvFilePrinter.printRecord(record);
                        record.clear();
                    } catch (IOException e){
                        System.err.println(e.toString());
                    }
                }


            }
            return os.toByteArray();
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }
    }
    
    /**
     * permet d'écrire un tableau CSV comme un graphe
     * 
     * @param ds
     * @param idTheso
     * @param idLang
     * @param idGroups
     * @param delimiter
     * @return 
     */
    public byte[] writeCsvGragh(HikariDataSource ds, String idTheso, String idLang, List<String> idGroups, char delimiter){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (OutputStreamWriter out = new OutputStreamWriter(os, Charset.forName("UTF-8")); 
                    CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.RFC4180.builder().setDelimiter(delimiter).build())) {

                ConceptHelper conceptHelper = new ConceptHelper();
                ArrayList <String> idConcepts = null;
                if(idGroups == null || idGroups.isEmpty())
                    idConcepts = conceptHelper.getAllIdConceptOfThesaurus(ds, idTheso);
                else {
                    if(idConcepts == null)
                        idConcepts = new ArrayList<>();
                    ArrayList <String> idConceptsTemp;
                    for (String idGroup : idGroups) {
                        idConceptsTemp = conceptHelper.getAllIdConceptOfThesaurusByGroup(ds, idTheso, idGroup);
                        if(idConceptsTemp != null)
                            idConcepts.addAll(idConceptsTemp);
                    }
                }

                if(idConcepts == null) return null;

                NodeConcept nodeConcept;
                /// écritures des données
                ArrayList<Object> record = new ArrayList<>();

                String repeatedValue = "";
                boolean first = true;
                for (String idConcept : idConcepts) {
                    try {
                        nodeConcept = conceptHelper.getConcept(ds, idConcept, idTheso, idLang, -1, -1);
                        record.add(nodeConcept.getConcept().getIdConcept());
                        record.add(nodeConcept.getConcept().getIdArk());
                        record.add(nodeConcept.getConcept().getIdHandle());
                        record.add(nodeConcept.getTerm().getLexical_value());
                        for (NodeEM nodeEM : nodeConcept.getNodeEM()) {
                            if(first) {
                                repeatedValue = nodeEM.getLexical_value();
                                first = false;
                            } else {
                                repeatedValue = repeatedValue + delim_multi_datas + nodeEM.getLexical_value();
                            }
                        }
                        first = true;
                        record.add(repeatedValue);
                        repeatedValue = "";
                        for (NodeNote nodeNote : nodeConcept.getNodeNotesTerm()) {
                            if("definition".equalsIgnoreCase(nodeNote.getNotetypecode()))
                                if(first) {
                                    repeatedValue = nodeNote.getLexicalvalue();
                                    first = false;
                                } else {
                                    repeatedValue = repeatedValue + delim_multi_datas + nodeNote.getLexicalvalue();
                                }
                        }
                        record.add(repeatedValue);
                        repeatedValue = "";
                        first = true;
                        for (NodeAlignment nodeAlignment : nodeConcept.getNodeAlignments()) {
                            if(first) {
                                repeatedValue = nodeAlignment.getAlignmentLabelType() +  ":" + nodeAlignment.getUri_target();
                                first = false;
                            } else {
                                repeatedValue = repeatedValue + delim_multi_datas + nodeAlignment.getAlignmentLabelType() +  ":" + nodeAlignment.getUri_target();
                            }
                        }
                        record.add(repeatedValue);
                        repeatedValue = "";
                        first = true;

                        csvFilePrinter.printRecord(record);
                        record.clear();
                    } catch (IOException e){
                        System.err.println(e.toString());
                    }
                }


            }
            return os.toByteArray();
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }
    }    

    /**
     * permet d'exporter les concepts dépréciés
     * @param ds
     * @param idTheso
     * @param idLang
     * @param idGroups
     * @param delimiter
     * @return
     */
    public byte[] writeCsvByDeprecated(HikariDataSource ds, String idTheso, String idLang, List<String> idGroups, char delimiter ){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (OutputStreamWriter out = new OutputStreamWriter(os, Charset.forName("UTF-8"));
                    CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.RFC4180.builder().setDelimiter(delimiter).build())) {

                /// écriture des headers
                ArrayList<String> header = new ArrayList<>();
                header.add("deprecatedId");
                header.add("deprecatedLabel");
                header.add("replacedBy");
                header.add("replacedByLabel");

                csvFilePrinter.printRecord(header);

                ConceptHelper conceptHelper = new ConceptHelper();
                ArrayList <NodeDeprecated> nodeDeprecateds;
                //    if(idGroups == null || idGroups.isEmpty())
                nodeDeprecateds = conceptHelper.getAllDeprecatedConceptOfThesaurus(ds, idTheso, idLang);
              /*  else {
                    if(idConcepts == null)
                        idConcepts = new ArrayList<>();
                    ArrayList <String> idConceptsTemp;
                    for (String idGroup : idGroups) {
                        idConceptsTemp = conceptHelper.getAllDeprecatedConceptOfThesaurusByGroup(ds, idTheso, idGroup);
                        if(idConceptsTemp != null)
                            idConcepts.addAll(idConceptsTemp);
                    }
                }*/

                if(nodeDeprecateds == null) return null;

                /// écritures des données
                ArrayList<Object> record = new ArrayList<>();

                for (NodeDeprecated nodeDeprecated : nodeDeprecateds) {
                    try {
                        record.add(nodeDeprecated.getDeprecatedId());
                        record.add(nodeDeprecated.getDeprecatedLabel());
                        record.add(nodeDeprecated.getReplacedById());
                        record.add(nodeDeprecated.getReplacedByLabel());
                        csvFilePrinter.printRecord(record);
                        record.clear();
                    } catch (IOException e){
                        System.err.println(e.toString());
                    }
                }
            }
            return os.toByteArray();
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }        
    }
}
