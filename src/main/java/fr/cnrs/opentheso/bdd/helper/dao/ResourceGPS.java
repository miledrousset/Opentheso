package fr.cnrs.opentheso.bdd.helper.dao;

import lombok.Data;
/**
 *
 * @author miledrousset
 */

@Data
public class ResourceGPS {
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private int position;
}
