/*
* permet de valider les actions à apporter sur le thésaurus
*/
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;

/**
 *
 * @author miled.rousset
 */
public class ValidateActionHelper {

    public ValidateActionHelper() {
    }
    
    /**
     * vérifie la cohérence des relations avant l'action
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idConceptToAdd
     * @return 
     */
    public boolean isAddRelationNTValid(
            HikariDataSource ds,
            String idTheso, 
            String idConcept,
            String idConceptToAdd) {
        RelationsHelper relationsHelper = new RelationsHelper();
        if(idConcept.equalsIgnoreCase(idConceptToAdd)) return false;
        
        // relations RT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationRT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            return false;
        }
        
        // relations BT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationNTorBT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            return false;
        }
        
        // relation entre frères est interdite 
        if(relationsHelper.isConceptHaveBrother(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            return false;
        }        
        return true;
    }
    
    /**
     * vérifie la cohérence des relations avant l'action
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idConceptToAdd
     * @return 
     */
    public boolean isAddRelationBTValid(
            HikariDataSource ds,
            String idTheso, 
            String idConcept,
            String idConceptToAdd) {
        RelationsHelper relationsHelper = new RelationsHelper();
        if(idConcept.equalsIgnoreCase(idConceptToAdd)) return false;
        
        // relations RT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationRT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            return false;
        }
        
        // relations BT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationNTorBT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            return false;
        }
        
        // relation entre frères est interdite 
        if(relationsHelper.isConceptHaveBrother(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            return false;
        }        
        return true;
    }    
    
    /**
     * vérifie la cohérence des relations avant l'action
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idConceptToAdd
     * @return 
     */
    public boolean isMoveConceptToConceptValid(
            HikariDataSource ds,
            String idTheso, 
            String idConcept,
            String idConceptToAdd) {
        RelationsHelper relationsHelper = new RelationsHelper();
        if(idConcept.equalsIgnoreCase(idConceptToAdd)) return false;
        
        // relations RT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationRT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            return false;
        }
        
        // relations BT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationNTorBT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            return false;
        }
        
      
        return true;
    }    
    
    /**
     * vérifie la cohérence des relations avant l'action
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idConceptToAdd
     * @return 
     */
    public boolean isAddRelationRTValid(
            HikariDataSource ds,
            String idTheso,
            String idConcept,
            String idConceptToAdd) {
        RelationsHelper relationsHelper = new RelationsHelper();
        if(relationsHelper.isConceptHaveRelationNTorBT(ds,
                idConcept, idConceptToAdd, idTheso) == true) 
            return false;
        
        // relation entre frères est interdite 
/*        if(relationsHelper.isConceptHaveBrother(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            return false;
        }*/         
        return true;
    }     
    
}
