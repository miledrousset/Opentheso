package fr.cnrs.opentheso.models.propositions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PropositionDao implements Serializable {
    
    private Integer id;
    
    private String idTheso;
    
    private String thesoName;
    
    private String idConcept;
    
    private String lang;
    
    private String nomConcept;
    
    private String nom;
    
    private String email;
    
    private String commentaire;
    
    private String status;
    
    private String datePublication;
    
    private String dateUpdate;
    
    private String userAction;
    
    private String codeDrapeau;
    
    private String adminComment;
    
    public boolean isEnvoyer() {
        return PropositionStatusEnum.ENVOYER.name().equals(status);
    }
    
    public boolean isLu() {
        return PropositionStatusEnum.LU.name().equals(status);
    }
    
    public boolean isApprouver() {
        return PropositionStatusEnum.APPROUVER.name().equals(status);
    }
    
    public boolean isRefuser() {
        return PropositionStatusEnum.REFUSER.name().equals(status);
    }
    
}
