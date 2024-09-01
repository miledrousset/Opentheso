package fr.cnrs.opentheso.services.exports.csv;

import fr.cnrs.opentheso.bean.toolbox.statistique.ConceptStatisticData;
import fr.cnrs.opentheso.bean.toolbox.statistique.GenericStatistiqueData;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;


@Data
@Slf4j
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
            writer.write("Collection;Concepts;Sysnonymes;Termes non traduits;Notes;Align Wikidata;Total align");
            writer.newLine();

            // write all concepts
            datas.forEach(collection -> {
                try {
                    writer.write(collection.getCollection() + seperate +
                            collection.getConceptsNbr() + seperate +
                            collection.getSynonymesNbr() + seperate +
                            collection.getTermesNonTraduitsNbr() + seperate +
                            collection.getNotesNbr() + seperate +
                            collection.getWikidataAlignNbr() + seperate +
                            collection.getTotalAlignment());
                    writer.newLine();
                } catch (IOException ex) {
                    log.error(ex.getMessage());
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
                    log.error(ex.getMessage());
                }
            });

            //close the writer
            writer.close();

        } catch (IOException ex) {
             System.out.println(">> " + ex.getMessage());

        }
    }

}
