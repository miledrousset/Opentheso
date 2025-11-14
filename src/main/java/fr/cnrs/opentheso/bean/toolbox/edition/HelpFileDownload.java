package fr.cnrs.opentheso.bean.toolbox.edition;

import java.io.InputStream;
import java.io.Serializable;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


@Getter
@Setter
@ViewScoped
@Named(value = "helpfileDownload")
public class HelpFileDownload implements Serializable{

    private String resourcePath;
    private String resourceType;

    
    public StreamedContent downloadCSVSample() {

        resourcePath = "/samples/sampleCSV_avecCollections.csv";
        var stream = this.getClass().getResourceAsStream(resourcePath);
        return DefaultStreamedContent.builder()
                .contentType("txt/CSV")
                .name("sampleCSV_avecCollections.csv")
                .stream(()->stream)
                .build();
    }
    
    public StreamedContent downloadCSVSampleWithoutCollections() {

        resourcePath = "/samples/sampleCSV_sansCollections.csv";
        InputStream stream = this.getClass().getResourceAsStream(resourcePath);
        return DefaultStreamedContent.builder()
                .contentType("txt/CSV")
                .name("sampleCSV_sansCollections.csv")
                .stream(()->stream)
                .build();
    }
}
