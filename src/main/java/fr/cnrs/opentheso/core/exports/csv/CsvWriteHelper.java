/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.csv;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 *
 * @author miledrousset
 */
public class CsvWriteHelper {

    public CsvWriteHelper() {
    }
    
    public byte[] writeCsvByIdTheso(HikariDataSource ds, String idTheso, String idLang){
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputStreamWriter out =new OutputStreamWriter(os, Charset.forName("UTF-8"));
            
            CSVPrinter csvFilePrinter = new CSVPrinter(out, CSVFormat.RFC4180);

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
            ArrayList <String> idConcepts = conceptHelper.getAllIdConceptOfThesaurus(ds, idTheso);
            
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
                        if(!first) {
                            repeatedValue = repeatedValue + "##";
                            first = false;
                        } else 
                            repeatedValue = nodeEM.getLexical_value();
                    }
                    first = true;
                    record.add(repeatedValue);
                    repeatedValue = "";
                    for (NodeNote nodeNote : nodeConcept.getNodeNotesTerm()) {
                        if("definition".equalsIgnoreCase(nodeNote.getNotetypecode()))
                            if(!first) {
                                repeatedValue = repeatedValue + "##";
                                first = false;
                            } else                             
                                repeatedValue = nodeNote.getLexicalvalue();
                    }
                    record.add(repeatedValue);
                    repeatedValue = "";
                    first = true;
                    for (NodeAlignment nodeAlignment : nodeConcept.getNodeAlignments()) {
                        if(!first) {
                            repeatedValue = repeatedValue + "##";
                            first = false;
                        } else                          
                           repeatedValue = nodeAlignment.getAlignmentLabelType() +  ":" + nodeAlignment.getUri_target();
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

            
            csvFilePrinter.close();
            out.close();
            return os.toByteArray();
        } catch (IOException e) {
            System.out.println(e.toString());
            return null;
        }        
    }
}
