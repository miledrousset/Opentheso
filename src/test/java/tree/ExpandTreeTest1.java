/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tree;

import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class ExpandTreeTest1 {
    

    
    public ExpandTreeTest1() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    // ne marche pas en instantiant un Bean ....
    @Test
    public void expandToConcept() {
        String idConcept = "31561";
        String idTheso = "th37";
        String idLang = "fr";
        Tree tree = new Tree();
        
        tree.expandTreeToPath(
            idConcept,
            idTheso,
            idLang);
        String test = "";
    }
}
