package fr.cnrs.opentheso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryValue {

    //// pour l'historique des relations
    private String id_concept1;
    private String role;
    private String id_concept2;

    //// pour les notes
    private String noteType;
    private String value;
    private String lang;
    private String action;
    private Date date;
    private String user;
}
