package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidat_vote")
public class CandidatVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vote", nullable = false)
    private Integer idVote;

    @ManyToOne
    @JoinColumn(name = "id_user", referencedColumnName = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_concept", referencedColumnName = "id_concept", nullable = false)
    private Concept concept;

    @ManyToOne
    @JoinColumn(name = "id_thesaurus", referencedColumnName = "id_thesaurus", nullable = false)
    private Thesaurus thesaurus;

    @Column(name = "type_vote", length = 30)
    private String typeVote;

    @Column(name = "id_note", length = 30)
    private String idNote;
}

