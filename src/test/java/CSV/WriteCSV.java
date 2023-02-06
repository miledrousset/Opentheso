/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSV;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class WriteCSV {

    public WriteCSV() {
    }

    @Test
    public void writeFile() {
        String csvFile = "/Users/miledrousset/Downloads/erreursarkfrantiq/resultat.csv";

        ArrayList<String> header = new ArrayList<>();
        header.add("faux");
        header.add("juste");
        try {
            CSVPrinter csvFilePrinter = new CSVPrinter(new FileWriter(csvFile), CSVFormat.DEFAULT);
            csvFilePrinter.printRecord(header);

            for (int i = 0; i <= 10; i++) {
                ArrayList<Object> record = new ArrayList<>();
                record.add(i);
                record.add("ark " + i);
                csvFilePrinter.printRecord(record);
            }
            csvFilePrinter.close();
            System.out.println("Generated vertex file: " + csvFile);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Test
    public void compareArkKohaToArkFrantiq() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();

        String csvFile = "/Users/miledrousset/Downloads/erreursarkfrantiq/resultat.csv";

        ArrayList<String> header = new ArrayList<>();
        header.add("faux");
        header.add("bon");
        header.add("supprimé");   
        header.add("old value");   
        header.add("old Id");         
        
        CSVPrinter csvFilePrinter;
        try {
            csvFilePrinter = new CSVPrinter(new FileWriter(csvFile), CSVFormat.DEFAULT);
            csvFilePrinter.printRecord(header);


        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }        
        
        
        try ( Connection conn = ds.getConnection()) {
            CSVParser cSVParser = readFileCsv();
            if (cSVParser == null) {
                return;
            }

            String value;
            ArrayList<String> ids;
            for (CSVRecord record : cSVParser) {
                NodeIdValue nodeIdValue = new NodeIdValue();
                // setId, si l'identifiant n'est pas renseigné, on récupère un NULL 
                try {
                    value = record.get("ark_id");
                    if (value == null || value.isEmpty()) {
                        continue;
                    }
                    nodeIdValue.setId(value);
                } catch (Exception e) {
                    continue;
                }
                try {
                    value = record.get("id");
                    if (value == null || value.isEmpty()) {
                        continue;
                    }
                    nodeIdValue.setValue(value);
                } catch (Exception e) {
                    continue;
                }
                ids = getArksFromArkeo(conn, nodeIdValue.getValue());
                ArrayList<Object> recordCsv = new ArrayList<>();
                if(!ids.isEmpty()) {
                    if(ids.size() == 1) {
                        recordCsv.add(ids.get(0));
                        recordCsv.add(ids.get(0));
                        // colonne bon
                        // colonne bon
                    } else {
                        for (String id : ids) {
                            if(id.equalsIgnoreCase(nodeIdValue.getId())){
                                recordCsv.add(id);
                            } else {
                                recordCsv.add(id);
                            }
                        }
                    }
                    if(!isConceptExist(conn, nodeIdValue.getValue())){
                        recordCsv.add("deleted");
                        recordCsv.add(record.get("vv"));
                        recordCsv.add(nodeIdValue.getValue());
                    } else {
                        recordCsv.add("");
                        recordCsv.add("");
                    }
                    csvFilePrinter.printRecord(recordCsv);
                }
           //     System.out.println("Generated vertex file: " + csvFile);                
           //     System.out.println(nodeIdValue.getId() + "   " + nodeIdValue.getValue());
            }
            csvFilePrinter.close();

        } catch (SQLException ex) {
            Logger.getLogger(WriteCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WriteCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ArrayList<String> getArksFromArkeo(Connection conn, String idConcept) {
        ArrayList<String> arks = new ArrayList<>();
        try ( Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select ark_id from test"
                    + " where "
                    + " url ilike '%idc=" + idConcept + "&%'");
            try ( ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    arks.add(resultSet.getString("ark_id"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(WriteCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
        return arks;
    }
    
    private boolean isConceptExist(Connection conn, String idConcept) {
        boolean exist = false;
        try ( Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_concept from test2"
                    + " where "
                    + " id_concept =  '" + idConcept + "'");
            try ( ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()) {
                    exist = resultSet.getRow() != 0;
                }
            }            
        } catch (SQLException ex) {
            Logger.getLogger(WriteCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
        return exist;
    }    

    private CSVParser readFileCsv() {
        char delimiter = ',';
        FileReader filereader;
        try {
            filereader = new FileReader("/Users/miledrousset/Downloads/erreursarkfrantiq/dead.csv");
            CSVFormat cSVFormat = CSVFormat.DEFAULT.builder().setHeader().setDelimiter(delimiter)
                    .setIgnoreEmptyLines(true).setIgnoreHeaderCase(true).setTrim(true).build();

            CSVParser cSVParser = cSVFormat.parse(filereader);
            return cSVParser;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WriteCSV.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WriteCSV.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
