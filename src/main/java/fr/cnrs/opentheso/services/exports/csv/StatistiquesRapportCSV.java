package fr.cnrs.opentheso.services.exports.csv;

import fr.cnrs.opentheso.bean.toolbox.statistique.ConceptStatisticData;
import fr.cnrs.opentheso.bean.toolbox.statistique.GenericStatistiqueData;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.function.Function;

@Data
@Slf4j
public class StatistiquesRapportCSV {

    private final String separator = ";";
    private BufferedWriter writer;
    private ByteArrayOutputStream output;


    public void createGenericStatistiquesRapport(List<GenericStatistiqueData> datas) {
        writeCSV(
                datas,
                "Collection;Concepts;Synonymes;Termes non traduits;Notes;Align Wikidata;Total align",
                data -> String.join(separator,
                        data.getCollection(),
                        String.valueOf(data.getConceptsNbr()),
                        String.valueOf(data.getSynonymesNbr()),
                        String.valueOf(data.getTermesNonTraduitsNbr()),
                        String.valueOf(data.getNotesNbr()),
                        String.valueOf(data.getWikidataAlignNbr()),
                        String.valueOf(data.getTotalAlignment()))
        );
    }

    public void createConceptsStatistiquesRapport(List<ConceptStatisticData> datas) {
        writeCSV(
                datas,
                "Id;Label;Type;Date de création;Date de modification;Utilisateur;",
                data -> String.join(separator,
                        data.getIdConcept(),
                        data.getLabel(),
                        data.getType(),
                        data.getDateCreation(),
                        data.getDateModification(),
                        data.getUtilisateur()) + separator // pour respecter le `;` final
        );
    }

    private <T> void writeCSV(List<T> datas, String header, Function<T, String> lineMapper) {
        try {
            output = new ByteArrayOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(output));

            writer.write(header);
            writer.newLine();

            for (T item : datas) {
                try {
                    writer.write(lineMapper.apply(item));
                    writer.newLine();
                } catch (IOException ex) {
                    log.error("Erreur lors de l'écriture d'une ligne : {}", ex.getMessage());
                }
            }

            writer.close();
        } catch (IOException ex) {
            log.error("Erreur pendant la génération du fichier des statistiques : {}", ex.getMessage());
        }
    }
}