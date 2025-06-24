package fr.cnrs.opentheso.models.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.cnrs.opentheso.models.alignment.NodeAlignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NodeIdValue implements Comparable<NodeIdValue>, Serializable {

    @EqualsAndHashCode.Include
    private String id;

    private String value;

    private boolean status;

    private String notation;

    private Integer nbrConcepts;

    private Date creationDate;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private transient List<NodeAlignment> alignements;

    @Override
    public int compareTo(NodeIdValue o) {
        return value != null ? 1 : value.compareTo(o.value);
    }

    public NodeIdValue(String id, String value) {
        this.id = id;
        this.value = value;
    }
}