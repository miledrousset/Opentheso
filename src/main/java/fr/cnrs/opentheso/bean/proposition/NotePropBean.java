package fr.cnrs.opentheso.bean.proposition;

import fr.cnrs.opentheso.models.notes.NodeNote;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotePropBean extends NodeNote {
    
    private String oldValue;
    private boolean toAdd;
    private boolean toRemove;
    private boolean toUpdate;
    
}
