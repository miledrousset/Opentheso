package fr.cnrs.opentheso.entites;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "proposition_modification_detail")
public class PropositionModificationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer idProposition;
    private String categorie;
    private String value;
    private String action;
    private String lang;
    private String oldValue;
    private boolean hiden;
    private String status;
    private String idTerm;
}
