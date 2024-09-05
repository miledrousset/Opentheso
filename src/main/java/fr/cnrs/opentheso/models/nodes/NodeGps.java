/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.models.nodes;

import lombok.Data;
/**
 *
 * @author miled.rousset
 */

@Data
public class NodeGps {

    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private int position;
    
}
