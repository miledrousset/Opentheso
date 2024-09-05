package fr.cnrs.opentheso.models.terms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Terme_nd {

	private int id_terme;
	private String id_langue;
	private int id_thesaurus;
	private String nd;
	private int id_mt;

}
