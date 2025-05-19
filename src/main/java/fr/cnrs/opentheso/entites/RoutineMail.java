package fr.cnrs.opentheso.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "routine_mail")
public class RoutineMail {

    @Id
    private String idThesaurus;

    private boolean alertCdt;

    @Temporal(TemporalType.DATE)
    private Date debutEnvCdtPropos;

    @Temporal(TemporalType.DATE)
    private Date debutEnvCdtValid;

    private Integer periodEnvCdtPropos;

    private Integer periodEnvCdtValid;

}
