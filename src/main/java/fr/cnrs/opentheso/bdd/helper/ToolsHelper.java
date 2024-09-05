package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.models.relations.HierarchicalRelationship;
import fr.cnrs.opentheso.models.relations.NodeRelation;
import fr.cnrs.opentheso.utils.NoIdCheckDigit;


public class ToolsHelper {

    /**
     * Fonction qui permet de restructurer le thésaurus en ajoutant les NT et les BT qui manquent
     * elle permet aussi de sortir les termes orphelins si nécessaire
     */
    public boolean reorganizingTheso(HikariDataSource ds, String idThesaurus) {

        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<String> idBT;
        ArrayList<String> idConcept1WhereIsNT;

        // récupération de tous les Id concepts du thésaurus
        ArrayList<String> tabIdConcept = conceptHelper.getAllIdConceptOfThesaurus(ds, idThesaurus);

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            for (String idConcept : tabIdConcept) {
                idBT = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
                idConcept1WhereIsNT = relationsHelper.getListIdWhichHaveNt(ds, idConcept, idThesaurus);
                if (idBT.isEmpty() && idConcept1WhereIsNT.isEmpty()) {
                    if (!conceptHelper.isTopConcept(ds, idConcept, idThesaurus)) {
                        // le concept est orphelin
                        conceptHelper.setTopConcept(ds, idConcept, idThesaurus);
                    }
                } else {
                    if (!(idBT.containsAll(idConcept1WhereIsNT))) {
                        //alors il manque des BT
                        ArrayList<String> BTmiss = new ArrayList<>(idConcept1WhereIsNT);
                        BTmiss.removeAll(idBT);
                        //on ajoute la différence
                        for (String miss : BTmiss) {
                            if (!relationsHelper.insertHierarchicalRelation(conn, idConcept, idThesaurus, "BT", miss)) {
                                conn.rollback();
                                conn.close();
                                return false;
                            }
                        }
                    }
                    if (!(idConcept1WhereIsNT.containsAll(idBT))) {
                        //il manque des NT pour certain idBT
                        ArrayList<String> NTmiss = new ArrayList<>(idBT);
                        NTmiss.removeAll(idConcept1WhereIsNT);
                        //on jaoute la différence
                        for (String miss : NTmiss) {
                            if (!relationsHelper.insertHierarchicalRelation(conn, miss, idThesaurus, "NT", idConcept)) {
                                conn.rollback();
                                conn.close();
                                return false;
                            }
                        }
                    }
                }
            }
            conn.commit();
            conn.close();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(ToolsHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
          
    /**
     * Permet de supprimer le status TopTerm pour les concepts qui ont une relation BT
     * @param ds
     * @param idThesaurus
     * @return 
     */
    public boolean removeTopTermForConceptWithBT(HikariDataSource ds, String idThesaurus) {
        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();

        // récupération de tous les Id TT du thésaurus
        ArrayList<String> tabIdTT = conceptHelper.getAllTopTermOfThesaurus(ds, idThesaurus);
        for (String idConcept : tabIdTT) {
            if(relationsHelper.isConceptHaveRelationBT(ds, idConcept, idThesaurus)){
                conceptHelper.setNotTopConcept(ds, idConcept, idThesaurus);
            }
        }
        return true;
    }
            
    /**
     * Permet de detecter les concepts qui n'ont pas de BT,
     * puis vérifier qu'ils ont l'info de TopTerme
     * @param ds
     * @param idThesaurus
     * @return 
     */
    public boolean reorganizingTopTerm(HikariDataSource ds, String idThesaurus) {
        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();

        // récupération des TopTerms en tenant compte des éventuelles erreurs donc par déduction
        ArrayList<String> listIds = relationsHelper.getListIdOfTopTermForRepair(ds, idThesaurus);
        
        for (String idConcept : listIds) {
            conceptHelper.setTopConcept(ds, idConcept, idThesaurus);
        }
        return true;
    }    

    /**
     * permet de supprimer la relation en boucle de type (a -> BT -> b et b -> BT -> a ) 
     * @param ds
     * @param idTheso
     * @param idConcept
     * @return 
     */
    public boolean removeLoopRelation(HikariDataSource ds, String idTheso, String idConcept) {
        RelationsHelper relationsHelper = new RelationsHelper();
        NodeRelation nodeRelation = relationsHelper.getLoopRelation(ds, idTheso, idConcept);
        if(nodeRelation!= null){
            relationsHelper.deleteThisRelation(ds, nodeRelation.getIdConcept2(), idTheso, "BT", nodeRelation.getIdConcept1());
            relationsHelper.deleteThisRelation(ds, nodeRelation.getIdConcept1(), idTheso, "NT", nodeRelation.getIdConcept2());
        }
        return true;   
    }        
    
    /**
     * Permet de supprimer les relations en boucle qui sont interdites (100 ->
     * BT -> 100) ou (100 -> NT -> 100)ou (100 -> RT -> 100) c'est incohérent et
     * ca provoque une boucle à l'infini
     * @param ds
     * @param role
     * @param idThesaurus
     * @return 
     */
    public boolean removeSameRelations(HikariDataSource ds, String role, String idThesaurus) {

        RelationsHelper relationsHelper = new RelationsHelper();

        // récupération des relations en Loop
        ArrayList<HierarchicalRelationship> tabRelations
                = relationsHelper.getListLoopRelations(ds, role, idThesaurus);
        if (!tabRelations.isEmpty()) {
            for (HierarchicalRelationship relation : tabRelations) {
                relationsHelper.deleteThisRelation(ds, relation.getIdConcept1(), idThesaurus,
                        role, relation.getIdConcept2());
            }
        }
        return true;
    }

    public String getNewId(int length, boolean isUpperCase, boolean isUseNoidCheck) {
        String chars = "0123456789bcdfghjklmnpqrstvwxz";
        StringBuilder pass = new StringBuilder();
        String idArk;
        for (int x = 0; x < length; x++) {
            int i = (int) Math.floor(Math.random() * (chars.length() - 1));
            pass.append(chars.charAt(i));
        }
        idArk = pass.toString();
        if(isUseNoidCheck) {
            NoIdCheckDigit noIdCheckDigit = new NoIdCheckDigit();
            String checkCode = noIdCheckDigit.getControlCharacter(idArk);
            idArk = idArk + "-" + checkCode;             
        }
        
        if(isUpperCase)
            return idArk.toUpperCase();
        else
            return idArk;
    }

    public Date getDate() {
        return new java.util.Date();
    }

}
