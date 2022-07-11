package fr.cnrs.opentheso.bean.proposition;

import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;


public class NotePropBean extends NodeNote {
    
    private String oldValue;
    
    private boolean toAdd;
    private boolean toRemove;
    private boolean toUpdate;
    

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

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
}
