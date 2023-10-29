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
        String la = null;
        String lo=null;        
        try{
            la = Double.toString(lat);
            lo = Double.toString(lon);
        }catch(Exception e){
            return;
        }
        
        
        this.lat = la;
        this.lon = lo;
    }


    @Override
    public String toString() {
        return String.format("(%f, %f)", lat, lon);
    }
}
