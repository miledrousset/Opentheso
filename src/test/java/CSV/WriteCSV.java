/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CSV;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author miledrousset
 */
public class WriteCSV {
    
    public WriteCSV() {
    }

    @Test
    public void writeFile() {
        String filename = "test";
        if (!filename.toLowerCase().endsWith(".csv")) {
            filename = filename + ".csv";
        }
        String csvFile = "/Users/miledrousset/Desktop" + "/" + filename + ".csv";
        
        ArrayList<String> header = new ArrayList<String>();
        header.add("id");
        header.add("Ark");
        try {
            CSVPrinter csvFilePrinter = new CSVPrinter(new FileWriter(csvFile), CSVFormat.DEFAULT);
            csvFilePrinter.printRecord(header);
            
            for (int i = 0; i<=10; i++){
                ArrayList<Object> record = new ArrayList<Object>();
                record.add(i);
                record.add("ark " + i);
                csvFilePrinter.printRecord(record);
            }
            csvFilePrinter.close();
            System.out.println("Generated vertex file: "+ csvFile);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }
    

}
