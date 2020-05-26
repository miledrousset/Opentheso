/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author Quincy
 */
public class NodeHieraRelation {

    private NodeUri uri;
    private String role;

    public NodeHieraRelation() {
        uri = new NodeUri();
        role = new String();
    }

    public NodeUri getUri() {
        return uri;
    }

    public void setUri(NodeUri uri) {
        this.uri = uri;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
