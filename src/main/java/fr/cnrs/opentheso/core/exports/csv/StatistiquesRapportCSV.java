package fr.cnrs.opentheso.core.exports.csv;

import fr.cnrs.opentheso.bean.toolbox.statistique.ConceptStatisticData;
import fr.cnrs.opentheso.bean.toolbox.statistique.GenericStatistiqueData;
import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class StatistiquesRapportCSV {

    private final String seperate = ";";
    
    private BufferedWriter writer;
    private ByteArrayOutputStream output;

    
    public void createGenericStatitistiquesRapport(List<GenericStatistiqueData> datas) {

        try {
            // create a writer
            output = new ByteArrayOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(output));

            // write header record
            writer.write("Collection;Concepts;Sysnonymes;Termes non traduits;Notes;");
            writer.newLine();

            // write all concepts
            datas.forEach(collection -> {
                try {
                    StringBuilder ligne = new StringBuilder()
                            .append(collection.getCollection()).append(seperate)
                            .append(collection.getConceptsNbr()).append(seperate)
                            .append(collection.getSynonymesNbr()).append(seperate)
                            .append(collection.getTermesNonTraduitsNbr()).append(seperate)
                            .append(collection.getNotesNbr()).append(seperate);
                    writer.write(ligne.toString());
                    writer.newLine();
                } catch (IOException ex) {
                    Logger.getLogger(StatistiquesRapportCSV.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            //close the writer
            writer.close();

        } catch (IOException ex) {
             System.out.println(">> " + ex.getMessage());

        }
    }
    
    public void createConceptsStatitistiquesRapport(List<ConceptStatisticData> datas) {

        try {
            // create a writer
            output = new ByteArrayOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(output));

            // write header record
            writer.write("Id;Label;Type;Date de création;Date de modification;Utilisateur;");
            writer.newLine();

            // write all concepts
            datas.forEach(concept -> {
                try {
                    StringBuilder ligne = new StringBuilder()
                            .append(concept.getIdConcept()).append(seperate)
                            .append(concept.getLabel()).append(seperate)
                            .append(concept.getType()).append(seperate)
                            .append(concept.getDateCreation()).append(seperate)
                            .append(concept.getDateModification()).append(seperate)
                            .append(concept.getUtilisateur()).append(seperate);
                    writer.write(ligne.toString());
                    writer.newLine();
                } catch (IOException ex) {
                    Logger.getLogger(StatistiquesRapportCSV.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

            //close the writer
            writer.close();

        } catch (IOException ex) {
             System.out.println(">> " + ex.getMessage());

        }
    }

    public ByteArrayOutputStream getOutput() {
        return output;
    }

}
