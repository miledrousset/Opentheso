package fr.cnrs.opentheso.bean.toolbox.statistique;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class GenericStatistiqueData {
    
    private String idCollection;
    private String collection;
    private int conceptsNbr;
    private int synonymesNbr;
    private int termesNonTraduitsNbr;
    private int notesNbr;
    private int wikidataAlignNbr;
    private int totalAlignment;

}
