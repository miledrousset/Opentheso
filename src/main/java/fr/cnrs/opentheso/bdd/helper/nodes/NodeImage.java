/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.cnrs.opentheso.bdd.helper.nodes;
import lombok.Data;
/**
 *
 * @author miled.rousset
 */
@Data
public class NodeImage {

    private int id;
    private String idConcept;
    private String idThesaurus;
    private String imageName;
    private String creator;
    private String copyRight;
    private String uri;
    
    private String oldUri; // pour la modification d'une Uri
    
    public NodeImage() {
    }
    
}
