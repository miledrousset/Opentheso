/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.cnrs.opentheso.bdd.helper.nodes;

import fr.cnrs.opentheso.bdd.datas.DcElement;
import java.util.ArrayList;


/**
 *
 * @author miled.rousset
 */
public class NodeMetaData {
    private String title;
    private String source;
    private String creator;
    private ArrayList <DcElement> dcElementsList;

    public NodeMetaData() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public ArrayList<DcElement> getDcElementsList() {
        return dcElementsList;
    }

    public void setDcElementsList(ArrayList<DcElement> dcElementsList) {
        this.dcElementsList = dcElementsList;
    }
   
}
