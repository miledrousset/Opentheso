package fr.cnrs.opentheso.bdd.helper.dao;
import lombok.Data;
/**
 *
 * @author miledrousset
 */

@Data
public class ConceptImage {
    private int id;
    private String imageName;
    private String copyRight;
    private String creator;
    private String uri;
}
