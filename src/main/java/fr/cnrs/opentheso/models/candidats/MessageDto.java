package fr.cnrs.opentheso.models.candidats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto implements Serializable {

    private int idUser;
    private String nom;
    private String msg;
    private String date;
    private boolean mine;
    
    public String getMessagePossition() {
        return mine ? "right" : "left";
    }
    
    public String getMessageColor() {
        return mine ? "#C8EAD6" : "#FAFAFA";
    }
}
