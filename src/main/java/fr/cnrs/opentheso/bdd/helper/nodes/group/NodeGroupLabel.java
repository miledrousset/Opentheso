package fr.cnrs.opentheso.bdd.helper.nodes.group;

import java.util.ArrayList;
import java.util.Date;

public class NodeGroupLabel {

    private String idGroup;
    private String idArk;
    private String idHandle;
    private String idDoi;
    private String notation;
    private String idThesaurus;
    private Date created;
    private Date modified;
    
    private ArrayList<NodeGroupTraductions> nodeGroupTraductionses = new ArrayList<>();

    public NodeGroupLabel() {
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getIdArk() {
        return idArk;
    }

    public void setIdArk(String idArk) {
        this.idArk = idArk;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public String getIdHandle() {
        return idHandle;
    }

    public void setIdHandle(String idHandle) {
        this.idHandle = idHandle;
    }

    public ArrayList<NodeGroupTraductions> getNodeGroupTraductionses() {
        return nodeGroupTraductionses;
    }

    public void setNodeGroupTraductionses(ArrayList<NodeGroupTraductions> nodeGroupTraductionses) {
        this.nodeGroupTraductionses = nodeGroupTraductionses;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public String getIdDoi() {
        return idDoi;
    }

    public void setIdDoi(String idDoi) {
        this.idDoi = idDoi;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

           

}
