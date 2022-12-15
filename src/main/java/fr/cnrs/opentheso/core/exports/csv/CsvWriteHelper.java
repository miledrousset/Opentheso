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
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author miledrousset
 */
public class CsvWriteHelper {

    public CsvWriteHelper() {
    }

    /**
     * Export en CSV avec tous les champs 
     * @param xmlDocument
     * @param selectedLanguages
     * @param seperate
     * @return 
     */
  /*  public byte[] writeCsv(SKOSXmlDocument xmlDocument, List<NodeLangTheso> selectedLanguages, char seperate) {
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
    }*/
    
    

    
    
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
                                repeatedValue = repeatedValue + "##" + nodeEM.getLexical_value();
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
                                    repeatedValue = repeatedValue + "##" + nodeNote.getLexicalvalue();
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
                                repeatedValue = repeatedValue + "##" + nodeAlignment.getAlignmentLabelType() +  ":" + nodeAlignment.getUri_target();
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
                                repeatedValue = repeatedValue + "##" + nodeEM.getLexical_value();
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
                                    repeatedValue = repeatedValue + "##" + nodeNote.getLexicalvalue();
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
                                repeatedValue = repeatedValue + "##" + nodeAlignment.getAlignmentLabelType() +  ":" + nodeAlignment.getUri_target();
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
