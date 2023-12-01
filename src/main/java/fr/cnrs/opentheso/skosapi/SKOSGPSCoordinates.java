/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.skosapi;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Quincy
 */
@Data
@NoArgsConstructor
public class SKOSGPSCoordinates {
    
    String lat;
    String lon;
    
    public SKOSGPSCoordinates(String lat,String lon) {
        this.lat = lat;
        this.lon = lon;
    }
    
    public SKOSGPSCoordinates(double lat,double lon) {
        try{
            this.lat = Double.toString(lat);
            this.lon = Double.toString(lon);
        }catch(Exception e){}
    }


    @Override
    public String toString() {
        return lat + " " + lon;
    }
}
