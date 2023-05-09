/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;


/**
 *
 * @author miled.rousset
 */
public class NodeAlignmentSmall {
    private String uri_target;
    private int alignement_id_type;
    private String source;    
    
    public NodeAlignmentSmall() {

    }

    public String getUri_target() {
        return uri_target;
    }

    public void setUri_target(String uri_target) {
        this.uri_target = uri_target;
    }

    public int getAlignement_id_type() {
        return alignement_id_type;
    }

    public void setAlignement_id_type(int alignement_id_type) {
        this.alignement_id_type = alignement_id_type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
