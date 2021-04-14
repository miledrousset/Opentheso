/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws;

import java.util.ArrayList;

/**
 *
 * @author miledrousset
 */
/**
 * Class pour regrouper les datas pour un noeud
 */
public class NodeDatas {

    private String name;
    private String type;
    private String url;
    private String definition;
    private ArrayList<String> synonym;

    private ArrayList<NodeDatas> childrens;

    public NodeDatas() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public ArrayList<String> getSynonym() {
        return synonym;
    }

    public void setSynonym(ArrayList<String> synonym) {
        this.synonym = synonym;
    }

    public ArrayList<NodeDatas> getChildrens() {
        return childrens;
    }

    public void setChildren(ArrayList<NodeDatas> childrens) {
        this.childrens = childrens;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
