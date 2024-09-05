package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Data
@Service
public class ValidateActionHelper {

    @Autowired
    private RelationsHelper relationsHelper;

    private String message;


    /**
     * Vérifie la cohérence des relations avant l'action
     */
    public boolean isAddRelationNTValid(HikariDataSource ds, String idTheso, String idConcept, String idConceptToAdd) {

        if(idConcept.equalsIgnoreCase(idConceptToAdd)) return false;
        
        // relations RT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationRT(ds, idConcept, idConceptToAdd, idTheso)){
            return false;
        }
        
        // relations BT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationNTorBT(ds, idConcept, idConceptToAdd, idTheso)){
            return false;
        }
        
        // relation entre frères est interdite 
        if(relationsHelper.isConceptHaveBrother(ds, idConcept, idConceptToAdd, idTheso)){
            return false;
        }        
        return true;
    }
    
    /**
     * vérifie la cohérence des relations avant l'action
     */
    public boolean isAddRelationBTValid( HikariDataSource ds, String idTheso, String idConcept, String idConceptToAdd) {

        if(idConcept.equalsIgnoreCase(idConceptToAdd)) return false;
        
        // relations RT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationRT(ds, idConcept, idConceptToAdd, idTheso)){
            message = "Une relation associative existe déjà entre les deux concepts";            
            return false;
        }
        
        // relations BT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationNTorBT(ds, idConcept, idConceptToAdd, idTheso)){
            message = "Une relation générique ou spécifique existe déjà entre les deux concepts";              
            return false;
        }
        
        // relation entre frères est interdite 
        if(relationsHelper.isConceptHaveBrother(ds, idConcept, idConceptToAdd, idTheso)){
            message = "Les concepts ont déjà une relation frère";            
            return false;
        }        
        return true;
    }    
    
    /**
     * Vérifie la cohérence des relations avant l'action
     */
    public boolean isMoveConceptToConceptValid(HikariDataSource ds, String idTheso, String idConcept, String idConceptToAdd) {

        if(idConcept.equalsIgnoreCase(idConceptToAdd)) return false;
        
        // relations RT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationRT(ds, idConcept, idConceptToAdd, idTheso)){
            message = "Une relation associative existe déjà entre les deux concepts";
            return false;
        }
        
        // relations BT et NT en même temps interdites
        if(relationsHelper.isConceptHaveRelationNTorBT(ds, idConcept, idConceptToAdd, idTheso)){
            message = "Une relation générique ou spécifique existe déjà entre les deux concepts";            
            return false;
        }
        
      
        return true;
    }    
    
    /**
     * Vérifie la cohérence des relations avant l'action
     */
    public boolean isAddRelationRTValid(HikariDataSource ds, String idTheso, String idConcept, String idConceptToAdd) {

        if(relationsHelper.isConceptHaveRelationNTorBT(ds, idConcept, idConceptToAdd, idTheso)) {
            message = "Une relation générique ou spécifique existe déjà entre les deux concepts";              
            return false;
        }  
        return true;
    }
    
}
