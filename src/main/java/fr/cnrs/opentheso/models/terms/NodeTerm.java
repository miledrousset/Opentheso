package fr.cnrs.opentheso.models.terms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NodeTerm {

	private String idTerm;
	private String idThesaurus;
	private String idConcept;
	private Date created;
	private Date modified;
	private String source;
	private String status;
	private List<NodeTermTraduction> nodeTermTraduction;

}
