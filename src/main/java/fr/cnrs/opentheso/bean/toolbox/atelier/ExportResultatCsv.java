package fr.cnrs.opentheso.bean.toolbox.atelier;

import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ExportResultatCsv {
    
    private final String seperate = ";";
    
    private BufferedWriter writer;
    private ByteArrayOutputStream output;
    
    public void createResultatFileRapport(ArrayList<ConceptResultNode> datas) {

        try {
            output = new ByteArrayOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(output));

            writer.write("ID Origine;PrefLabel Origine;ID Concept;PrefLabel;AltLabel;Term Générique;Définition;URI Ark;");
            writer.newLine();

            datas.forEach(collection -> {
                try {
                    StringBuilder ligne = new StringBuilder()
                            .append(collection.getIdOrigine()).append(seperate)
                            .append(collection.getPrefLabelOrigine()).append(seperate)
                            .append(collection.getIdConcept()).append(seperate)
                            .append(collection.getPrefLabelConcept()).append(seperate)
                            .append(collection.getAltLabelConcept()).append(seperate)
                            .append(collection.getTermGenerique()).append(seperate)
                            .append(collection.getDefinition()).append(seperate)
                            .append(collection.getUriArk()).append(seperate);
                    writer.write(ligne.toString());
                    writer.newLine();
                } catch (IOException ex) {
                    Logger.getLogger(ExportResultatCsv.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            writer.close();

        } catch (IOException ex) {}
    }

    public ByteArrayOutputStream getOutput() {
        return output;
    }
    
}
