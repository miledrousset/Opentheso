/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author miledrousset
 */
public class NodeSelectedAlignment {

    private int idAlignmnetSource;
    private String sourceLabel;
    private String sourceDescription;
    
    // pour la modification
    private boolean isSelected;

    public int getIdAlignmnetSource() {
        return idAlignmnetSource;
    }

    public void setIdAlignmnetSource(int idAlignmnetSource) {
        this.idAlignmnetSource = idAlignmnetSource;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }

    public void setSourceDescription(String sourceDescription) {
        this.sourceDescription = sourceDescription;
    }

    public boolean isIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    
    
}
