package fr.cnrs.opentheso.models.exports.privatesdatas.tables;

import java.util.ArrayList;
import fr.cnrs.opentheso.models.exports.privatesdatas.LineOfData;
import lombok.Data;


@Data
public class Table {
    
    private String name;
    private ArrayList<LineOfData> lineOfDatas;
    
}
