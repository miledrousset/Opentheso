package fr.cnrs.opentheso.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;


@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "thesaurus_label", uniqueConstraints = {@UniqueConstraint(columnNames = {"idThesaurus", "lang"})})
@IdClass(ThesaurusLabelId.class)
public class ThesaurusLabel {

    @Id
    @Column(name = "id_thesaurus", nullable = false)
    private String idThesaurus;

    @Id
    @Column(name = "lang", nullable = false)
    private String lang;

    @Id
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "contributor")
    private String contributor;

    @Column(name = "coverage")
    private String coverage;

    @Column(name = "creator")
    private String creator;

    @Column(name = "created", nullable = false)
    private LocalDateTime created = LocalDateTime.now();

    @Column(name = "modified", nullable = false)
    private LocalDateTime modified = LocalDateTime.now();

    @Column(name = "description")
    private String description;

    @Column(name = "format")
    private String format;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "relation")
    private String relation;

    @Column(name = "rights")
    private String rights;

    @Column(name = "source")
    private String source;

    @Column(name = "subject")
    private String subject;

    @Column(name = "type")
    private String type;
}
