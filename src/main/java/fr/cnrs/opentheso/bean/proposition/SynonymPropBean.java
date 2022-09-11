package fr.cnrs.opentheso.bean.proposition;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;


public class SynonymPropBean extends NodeEM {
    
    private boolean toAdd;
    private boolean toRemove;
    private boolean toUpdate;
    private String idTerm;
    

    public boolean isToAdd() {
        return toAdd;
    }

    public void setToAdd(boolean toAdd) {
        this.toAdd = toAdd;
    }

    public boolean isToRemove() {
        return toRemove;
    }

    public void setToRemove(boolean toRemove) {
        this.toRemove = toRemove;
    }

    public boolean isToUpdate() {
        return toUpdate;
    }

    public void setToUpdate(boolean toUpdate) {
        this.toUpdate = toUpdate;
    }

    public String getIdTerm() {
        return idTerm;
    }

    public void setIdTerm(String idTerm) {
        this.idTerm = idTerm;
    }
    
}
