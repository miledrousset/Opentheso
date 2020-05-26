/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.skosapi;

/**
 *
 * @author Quincy
 */
public class SKOSGPSCoordinates {
    
    String lat;
    String lon;

    public SKOSGPSCoordinates() {
        
    }
    
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

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
    
    
    
    
    
}
