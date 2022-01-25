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
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author miledrousset
 */
public class CsvWriteHelper {

    public CsvWriteHelper() {
    }

    public byte[] writeCsvById(HikariDataSource ds, String idTheso, String idLang, List<String> idGroups ){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (OutputStreamWriter out = new OutputStreamWriter(os, Charset.forName("UTF-8")); CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.RFC4180)) {

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
                        nodeConcept = conceptHelper.getConcept(ds, idConcept, idTheso, idLang);
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
     * @return
     */
    public byte[] writeCsvByDeprecated(HikariDataSource ds, String idTheso, String idLang, List<String> idGroups ){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try (OutputStreamWriter out = new OutputStreamWriter(os, Charset.forName("UTF-8")); CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.RFC4180)) {

                /// écriture des headers
                ArrayList<String> header = new ArrayList<>();
                header.add("deprecatedId");
                header.add("deprecatedLabel");
                header.add("replacedBy");
                header.add("replacedByLabel");

                csvFilePrinter.printRecord(header);

                ConceptHelper conceptHelper = new ConceptHelper();
                ArrayList <NodeDeprecated> nodeDeprecateds = null;
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
