package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.models.relations.HierarchicalRelationship;
import fr.cnrs.opentheso.models.relations.NodeRelation;
import fr.cnrs.opentheso.utils.NoIdCheckDigit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;


@Slf4j
@Service
public class ToolsHelper {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RelationsHelper relationsHelper;


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

    /**
     * Permet de supprimer les relations en boucle qui sont interdites (100 ->
     * BT -> 100) ou (100 -> NT -> 100)ou (100 -> RT -> 100) c'est incohérent et
     * ca provoque une boucle à l'infini
     * @param role
     * @param idThesaurus
     * @return
     */
    public boolean removeSameRelations(String role, String idThesaurus) {

        // récupération des relations en Loop
        ArrayList<HierarchicalRelationship> tabRelations = relationsHelper.getListLoopRelations(role, idThesaurus);
        if (!tabRelations.isEmpty()) {
            for (HierarchicalRelationship relation : tabRelations) {
                relationsHelper.deleteThisRelation(relation.getIdConcept1(), idThesaurus,
                        role, relation.getIdConcept2());
            }
        }
        return true;
    }

    /**
     * Fonction qui permet de restructurer le thésaurus en ajoutant les NT et les BT qui manquent
     * elle permet aussi de sortir les termes orphelins si nécessaire
     */
    public boolean reorganizingTheso(String idThesaurus) {

        ArrayList<String> idBT;
        ArrayList<String> idConcept1WhereIsNT;

        // récupération de tous les Id concepts du thésaurus
        ArrayList<String> tabIdConcept = getAllIdConceptOfThesaurus(idThesaurus);

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            for (String idConcept : tabIdConcept) {
                idBT = relationsHelper.getListIdBT(idConcept, idThesaurus);
                idConcept1WhereIsNT = relationsHelper.getListIdWhichHaveNt(idConcept, idThesaurus);
                if (idBT.isEmpty() && idConcept1WhereIsNT.isEmpty()) {
                    if (!isTopConcept(idConcept, idThesaurus)) {
                        // le concept est orphelin
                        setTopConcept(idConcept, idThesaurus);
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

    private ArrayList<String> getAllIdConceptOfThesaurus(String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and concept.status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Permet de supprimer le status TopTerm pour les concepts qui ont une relation BT
     * @param idThesaurus
     * @return
     */
    public boolean removeTopTermForConceptWithBT(String idThesaurus) {

        // récupération de tous les Id TT du thésaurus
        ArrayList<String> tabIdTT = getAllTopTermOfThesaurus(idThesaurus);
        for (String idConcept : tabIdTT) {
            if(relationsHelper.isConceptHaveRelationBT(idConcept, idThesaurus)){
                setNotTopConcept(idConcept, idThesaurus);
            }
        }
        return true;
    }

    public boolean setNotTopConcept(String idConcept, String idThesaurus) {

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set top_concept = false WHERE id_concept ='" + idConcept
                        + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
        return false;
    }

    public ArrayList<String> getAllTopTermOfThesaurus(String idThesaurus) {

        ArrayList<String> listIdOfTopConcept = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and top_concept = true and status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdOfTopConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All Ids of TopConcept : " + idThesaurus, sqle);
        }
        return listIdOfTopConcept;
    }

    /**
     * Permet de detecter les concepts qui n'ont pas de BT,
     * puis vérifier qu'ils ont l'info de TopTerme
     * @param idThesaurus
     * @return
     */
    public boolean reorganizingTopTerm(String idThesaurus) {

        // récupération des TopTerms en tenant compte des éventuelles erreurs donc par déduction
        ArrayList<String> listIds = relationsHelper.getListIdOfTopTermForRepair(idThesaurus);

        for (String idConcept : listIds) {
            setTopConcept(idConcept, idThesaurus);
        }
        return true;
    }

    private boolean setTopConcept(String idConcept, String idThesaurus) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set top_concept = true WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
        return false;
    }

    private boolean isTopConcept(String idConcept, String idThesaurus) {
        boolean existe = false;
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select top_concept from concept where id_concept = '" + idConcept
                        + "' and id_thesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getBoolean("top_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Asking if TopConcept : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * permet de supprimer la relation en boucle de type (a -> BT -> b et b -> BT -> a )
     */
    public boolean removeLoopRelation(String idTheso, String idConcept) {

        NodeRelation nodeRelation = relationsHelper.getLoopRelation(idTheso, idConcept);
        if(nodeRelation!= null){
            relationsHelper.deleteThisRelation(nodeRelation.getIdConcept2(), idTheso, "BT", nodeRelation.getIdConcept1());
            relationsHelper.deleteThisRelation(nodeRelation.getIdConcept1(), idTheso, "NT", nodeRelation.getIdConcept2());
        }
        return true;
    }

}
