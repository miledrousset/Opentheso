/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper.nodes;

/**
 *
 * @author miled.rousset
 * 
 * Pour un utilisateur donnée, cette classe permet de regrouper un role par groupe 
 * et un noeud de thésaurus par groupe
 */
public class NodeUserRoleGroup { 
    private int idRole;
    private String roleName;
    private int idGroup;
    private String groupName;
    
    private boolean isAdmin = false;
    private boolean isManager = false;
    private boolean isContributor = false;
    
    public NodeUserRoleGroup() {
    }

    public int getIdRole() {
        return idRole;
    }

    public void setIdRole(int idRole) {
        this.idRole = idRole;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(int idGroup) {
        this.idGroup = idGroup;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isIsManager() {
        return isManager;
    }

    public void setIsManager(boolean isManager) {
        this.isManager = isManager;
    }

    public boolean isIsContributor() {
        return isContributor;
    }

    public void setIsContributor(boolean isContributor) {
        this.isContributor = isContributor;
    }

}
