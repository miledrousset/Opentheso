package fr.cnrs.opentheso.models.concept;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Image {

	private int idTerme;
	private int idThesaurus;
	private String copyright;
	private int id_image;

}
