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

    private String message;
    
    /**
     * vérifie la cohérence des relations avant l'action
     */
    public boolean isAddRelationNTValid(HikariDataSource ds, String idTheso, String idConcept, String idConceptToAdd) {
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
     */
    public boolean isAddRelationBTValid( HikariDataSource ds, String idTheso, 
            String idConcept, String idConceptToAdd) {
        RelationsHelper relationsHelper = new RelationsHelper();
        if(idConcept.equalsIgnoreCase(idConceptToAdd)) return false;
        
        // relations RT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationRT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            message = "Une relation associative existe déjà entre les deux concepts";            
            return false;
        }
        
        // relations BT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationNTorBT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            message = "Une relation générique ou spécifique existe déjà entre les deux concepts";              
            return false;
        }
        
        // relation entre frères est interdite 
        if(relationsHelper.isConceptHaveBrother(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            message = "Les concepts ont déjà une relation frère";            
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
    public boolean isMoveConceptToConceptValid(HikariDataSource ds, String idTheso, 
            String idConcept, String idConceptToAdd) {
        RelationsHelper relationsHelper = new RelationsHelper();
        if(idConcept.equalsIgnoreCase(idConceptToAdd)) return false;
        
        // relations RT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationRT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            message = "Une relation associative existe déjà entre les deux concepts";
            return false;
        }
        
        // relations BT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationNTorBT(ds,
                idConcept, idConceptToAdd, idTheso) == true){ 
            message = "Une relation générique ou spécifique existe déjà entre les deux concepts";            
            return false;
        }
        
      
        return true;
    }    
    
    /**
     * vérifie la cohérence des relations avant l'action
     */
    public boolean isAddRelationRTValid(HikariDataSource ds, String idTheso, String idConcept,
            String idConceptToAdd) {
        RelationsHelper relationsHelper = new RelationsHelper();
        if(relationsHelper.isConceptHaveRelationNTorBT(ds,
                idConcept, idConceptToAdd, idTheso) == true) {
            message = "Une relation générique ou spécifique existe déjà entre les deux concepts";              
            return false;
        }  
        return true;
    }     

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
