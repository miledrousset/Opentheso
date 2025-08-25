package fr.cnrs.opentheso.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.Date;


@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "thesaurus")
@EntityListeners(AuditingEntityListener.class)
public class Thesaurus {

    @Column(name = "id", nullable = false)
    private Integer id;

    @Id
    @Column(name = "id_thesaurus", nullable = false)
    private String idThesaurus;

    @Column(name = "id_ark", nullable = false)
    private String idArk;

    @CreatedDate
    @Column(name = "created", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @LastModifiedDate
    @Column(name = "modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    @Column(name = "private", nullable = false)
    private Boolean isPrivate = false;

}
