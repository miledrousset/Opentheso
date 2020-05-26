/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;

import java.util.ArrayList;

/**
 *
 * @author miledrousset
 */
public class Path {
    private ArrayList<String> path;

    public Path() {
        path = new ArrayList<>();
    }

    public ArrayList<String> getPath() {
        return path;
    }

    public void setPath(ArrayList<String> path) {
        this.path = path;
    }
}
