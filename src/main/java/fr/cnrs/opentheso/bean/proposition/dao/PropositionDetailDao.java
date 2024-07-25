package fr.cnrs.opentheso.bean.proposition.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropositionDetailDao implements Serializable {
    
    private Integer id;
    private Integer idProposition;
    private String categorie;
    private String value;
    private String oldValue;
    private String action;
    private String lang;
    private String idTerm;
    private String status;
    private boolean hiden;
    
}
