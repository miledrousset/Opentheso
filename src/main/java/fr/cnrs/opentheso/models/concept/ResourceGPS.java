package fr.cnrs.opentheso.models.concept;

import lombok.Data;


@Data
public class ResourceGPS {

    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private int position;
}
