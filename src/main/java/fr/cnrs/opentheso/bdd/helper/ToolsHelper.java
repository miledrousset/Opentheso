package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.datas.HierarchicalRelationship;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.tools.FileUtilities;
//import fr.cnrs.opentheso.ws.ark.ArkClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ToolsHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    public ToolsHelper() {

    }

    /**
     * Cette fonction permet de regénerer ou générer tous les identifiants Ark
     * du thésaurus
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList de NodePermute
     */
    public boolean GenerateArkIds(HikariDataSource ds,
            String idThesaurus) {

        ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();
        ArrayList<NodeTermTraduction> nodeTermTraductionList;

        // Génération des Id Ark pour les concepts
        ArrayList<String> tabIdConcept = conceptHelper.getAllIdConceptOfThesaurus(ds, idThesaurus);

        String idArk;
//        ArkClient ark_Client = new ArkClient();
        ArrayList<DcElement> dcElementsList = new ArrayList<>();

        for (String idConcept : tabIdConcept) {

            dcElementsList.clear();
            nodeTermTraductionList = termHelper.getAllTraductionsOfConcept(ds, idConcept, idThesaurus);
            for (NodeTermTraduction nodeTermTraduction : nodeTermTraductionList) {
                DcElement dcElement = new DcElement();
                // cette fonction permet de remplir la table Permutée
                dcElement.setName("description");
                dcElement.setValue(nodeTermTraduction.getLexicalValue());
                dcElement.setLanguage(nodeTermTraduction.getLang());
                dcElementsList.add(dcElement);
            }
            // String date, String url, String title, String creator, String description, String type
    /*        idArk = ark_Client.getArkId(
                    new FileUtilities().getDate(),
                    "http://pactols.frantiq.fr/" + "?idc=" + idConcept + "&idt=" + idThesaurus,
                    idConcept,
                    "Frantiq",
                    dcElementsList,
                    "pcrt"); // pcrt : p= pactols, crt=code DCMI pour collection
            conceptHelper.updateArkIdOfConcept(ds, idConcept, idThesaurus, idArk);*/
        }

        return true;
    }

    /**
     * Fonction qui permet de replacer les orphelins qui ne les sont plus et les
     * attacher aux concepts
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
/*    public boolean orphanReplace(HikariDataSource ds,
            String idThesaurus) {

        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        GroupHelper groupHelper = new GroupHelper();
        
        // récupération de tous les Id concepts du thésaurus
        ArrayList<String> idOrphans = orphanHelper.getListOrphanId(ds, idThesaurus);

        // on controle si l'orphelin a un BT on le supprime des orphelins pour le rattacher au concept.
        for (String idOrphan : idOrphans) {
            if (relationsHelper.isConceptHaveRelationBT(ds, idOrphan, idThesaurus)) {
                if(groupHelper.isConceptHaveGroup(ds, idOrphan, idThesaurus)) {
                    try {
                        Connection conn = ds.getConnection();
                        conn.setAutoCommit(false);
                        if (!orphanHelper.deleteOrphan(conn, idOrphan, idThesaurus)) {
                            conn.rollback();
                            conn.close();
                        } else {
                            conn.commit();
                            conn.close();
                        }
                    } catch (SQLException sqle) {
                        Logger.getLogger(ToolsHelper.class.getName()).log(Level.SEVERE, null, sqle);
                    }
                }
            }
            else {
                // si l'orphelin est un TopTerm, il faut le supprimer pour le replacer dans le thésaurus
                if(conceptHelper.isTopConcept(ds, idOrphan, idThesaurus)) {
                    if(groupHelper.isConceptHaveGroup(ds, idOrphan, idThesaurus)) {
                        try {
                            Connection conn = ds.getConnection();
                            conn.setAutoCommit(false);
                            if (!orphanHelper.deleteOrphan(conn, idOrphan, idThesaurus)) {
                                conn.rollback();
                                conn.close();
                            }
                        } catch (SQLException sqle) {
                            Logger.getLogger(ToolsHelper.class.getName()).log(Level.SEVERE, null, sqle);
                        }
                    }
                }
            }
        }
        return true;
    }*/

    /**
     * Fonction qui permet de restructurer le thésaurus en ajoutant les NT et les BT qui manquent
     * elle permet aussi de sortir les termes orphelins si nécessaire
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public boolean reorganizingTheso(HikariDataSource ds,
            String idThesaurus) {

        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<String> idBT;
        ArrayList<String> idConcept1WhereIsNT;

        // récupération de tous les Id concepts du thésaurus
        ArrayList<String> tabIdConcept = conceptHelper.getAllIdConceptOfThesaurus(ds, idThesaurus);

        try {
            Connection conn = ds.getConnection();
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
     * Fonction qui permet de compléter les relations reciproques qui manquent
     * ex : 100 RT 200 / 200 RT 100
     * 100 NT 200 / 200 BT 100
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public boolean completeReciprocalRelation(HikariDataSource ds,
            String idThesaurus) {

        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<NodeRelation> nodeRelations;
        
        // récupération de tous les Id concepts du thésaurus
        ArrayList<String> tabIdConcept = conceptHelper.getAllIdConceptOfThesaurus(ds, idThesaurus);
        for (String idConcept : tabIdConcept) {
            // je récupère toutes les relations d'un concept 
            /*
            100 BT  50
            100 NT  200
            100 RT  300
            puis on produit les relations reciproques 
            50  NT  100
            200 BT  100
            300 RT  100
            */
            
            nodeRelations = relationsHelper.getLeftRelationsOfConcept(ds, idConcept, idThesaurus);
            
            for (NodeRelation nodeRelation : nodeRelations) {
                switch (nodeRelation.getRelation()) {
                    case "NT":
                        relationsHelper.addOneRelation(ds, nodeRelation.getIdConcept2(), idThesaurus, "BT", idConcept);
                        break;
                    case "BT":
                        relationsHelper.addOneRelation(ds, nodeRelation.getIdConcept2(), idThesaurus, "NT", idConcept);
                        break;                       
                    case "NTG":
                        relationsHelper.addOneRelation(ds, nodeRelation.getIdConcept2(), idThesaurus, "BTG", idConcept);
                        break;
                    case "NTP":
                        relationsHelper.addOneRelation(ds, nodeRelation.getIdConcept2(), idThesaurus, "BTP", idConcept);
                        break;
                    case "NTI":
                        relationsHelper.addOneRelation(ds, nodeRelation.getIdConcept2(), idThesaurus, "BTI", idConcept);
                        break;
                    case "BTG":
                        relationsHelper.addOneRelation(ds, nodeRelation.getIdConcept2(), idThesaurus, "NTG", idConcept);
                        break;
                    case "BTP":
                        relationsHelper.addOneRelation(ds, nodeRelation.getIdConcept2(), idThesaurus, "NTP", idConcept);
                        break;
                    case "BTI":
                        relationsHelper.addOneRelation(ds, nodeRelation.getIdConcept2(), idThesaurus, "NTI", idConcept);
                        break;
                }
            }
        }

        return true;
    }
    
    
    /**
     * Fonction qui permet de détecter les concepts qui n'ont pas de groupes
     * puis elle les replace dans le domaine (NoGroup)
     *
     * @param ds
     * @param idThesaurus
     * @param defaultLang
     * @return
     */
    public boolean detectAndReplaceNoGroup(HikariDataSource ds,
            String idThesaurus, String defaultLang) {

        ConceptHelper conceptHelper = new ConceptHelper();
        GroupHelper groupHelper = new GroupHelper();
        
        boolean first = true;

        // ici, on traite les concept qui n'ont aucun groupe 
        ArrayList<String> tabIdConcept = conceptHelper.getAllIdConceptOfThesaurusWithoutGroup(ds, idThesaurus);
        for (String idConcept : tabIdConcept) {
            if(first) {
                // création du domaine (NoGroup)
                if(!groupHelper.addGroupDefault(ds, defaultLang, idThesaurus))
                    return false;
            }
            first = false;
            String idDefaultGroup = "orphans";
            groupHelper.addConceptGroupConcept(ds, idDefaultGroup, idConcept, idThesaurus); 
        }
        return true;
    }    
        
        
    /**
     * Fonction qui permet de compléter les domaines qui manquent pour un Concept
     * Pour pallier au concepts qui n'ont pas de domaine ou groupe
     * 
     * ex : 200 fait partie du groupe G1 
     * on récupère le BT du concept 200 
     * 200 BT 100
     * on vérifie le groupe ou domaine du concept 100
     * on ajoute le group au concept 200
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public boolean completeLackGroup(HikariDataSource ds,
            String idThesaurus) {

        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        GroupHelper groupHelper = new GroupHelper();
        
        ArrayList<String> idBTs;
        ArrayList<String> idGroupsOfBT;
        ArrayList<String> idGroupsOfConcept;         
        
        // récupération de tous les Id concepts du thésaurus qui n'ont pas de groupe
        // pour essayer de le compléter en se basant au BT 
        

        // ici, on traite les concept qui n'ont aucun groupe 
        ArrayList<String> tabIdConcept = conceptHelper.getAllIdConceptOfThesaurusWithoutGroup(ds, idThesaurus);
        for (String idConcept : tabIdConcept) {
            idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
            for (String idBT : idBTs) {
                idGroupsOfBT = groupHelper.getListIdGroupOfConcept(ds, idThesaurus, idBT);
                for (String idGroup : idGroupsOfBT) {
                    groupHelper.addConceptGroupConcept(ds, idGroup, idConcept, idThesaurus); 
                }
            }
        }
        
        /// supprimer les groupes en trop 
        // on récupère les concepts qui appartiennent à plusieurs groupes
        tabIdConcept = conceptHelper.getConceptsHavingMultiGroup(ds, idThesaurus);
        for (String idConcept : tabIdConcept) {
            // on récupère les termes génériques
            idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
            if(idBTs != null) {
                // si on a moins de 2 BT, il y une erreur
                if(idBTs.size() < 2) {
                    for (String idBT : idBTs) {
                        // on récupère les groupes du terme générique
                        idGroupsOfBT = groupHelper.getListIdGroupOfConcept(ds, idThesaurus, idBT);
                        
                        // on récupère les groupes du concept
                        idGroupsOfConcept = groupHelper.getListIdGroupOfConcept(ds, idThesaurus, idConcept);
                        
                        for (String idGroupOfConcept : idGroupsOfConcept) {
                            
                            // si le groupe du concept ne se trouve pas dans les groupes du BT, alors on le supprime
                            if(!idGroupsOfBT.contains(idGroupOfConcept)) {
                                conceptHelper.deleteGroupOfConcept(ds, idConcept, idGroupOfConcept, idThesaurus, 1);
                            }
                        }
                    }
                }
            }
        }
        
        //ici on traite les concepts qui ont plusieurs BT pour vérifier s'il ne manque pas un groupe
        
        tabIdConcept = conceptHelper.getConceptsHavingMultiBT(ds, idThesaurus);        
        
        for (String idConcept : tabIdConcept) {
            // on récupère les termes génériques
            idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
            if(idBTs != null) {
                for (String idBT : idBTs) {
                    // on récupère les groupes du terme générique
                    idGroupsOfBT = groupHelper.getListIdGroupOfConcept(ds, idThesaurus, idBT);

                    // on récupère les groupes du concept
                    idGroupsOfConcept = groupHelper.getListIdGroupOfConcept(ds, idThesaurus, idConcept);

                    for (String idGroupOfBT : idGroupsOfBT) {

                        // si le groupe du concept ne se trouve pas dans les groupes du BT, alors on le supprime
                        if(!idGroupsOfConcept.contains(idGroupOfBT)) {
                            groupHelper.addConceptGroupConcept(ds, idGroupOfBT, idConcept, idThesaurus);
                        }
                    }
                }
            }
        }        
        
        //ici on traite les concepts qui ont un seul BT mais avec un groupe différent entre le BT et le concept 
        
        // traitement pour tous les groupes        
        tabIdConcept = conceptHelper.getConceptsHavingOneBT(ds, idThesaurus);
        
        /// traitement pour un groupe en particulier
        //tabIdConcept = conceptHelper.getConceptsHavingOneBTByGroup(ds, idThesaurus, "G122");

        
        for (String idConcept : tabIdConcept) {
            // on récupère les termes génériques
            idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
            if(idBTs != null) {
                for (String idBT : idBTs) {
                    // on récupère les groupes du terme générique
                    idGroupsOfBT = groupHelper.getListIdGroupOfConcept(ds, idThesaurus, idBT);

                    // on récupère les groupes du concept
                    idGroupsOfConcept = groupHelper.getListIdGroupOfConcept(ds, idThesaurus, idConcept);
                    if(idGroupsOfConcept.size() == 1) {
                        if(idGroupsOfBT.size() == 1) {
                            // si le groupe du concept n'est pas égale au groupe du BT, alors on remplace le groupe du concept par celui du BT
                            if(!idGroupsOfConcept.get(0).equals(idGroupsOfBT.get(0))) {
                                // suppression du groupe du concept
                                groupHelper.deleteRelationConceptGroupConcept(ds, idGroupsOfConcept.get(0),idConcept, idThesaurus, 1);
                                
                                // ajout du groupe de BT au concept
                                groupHelper.addConceptGroupConcept(ds, idGroupsOfBT.get(0), idConcept, idThesaurus);
                            }
                        }
                    }
                }
            }
        }         
        return true;
    }
    

    /**
     * Permet de supprimer les Groupes qui sont orphelins, cas où un concept qui
     * appartient à deux groupes mais le concept n'a qu'une branche ou relation
     * BT
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    /*   public boolean removeGroupOrphan(HikariDataSource ds,
            String idThesaurus) {
        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        GroupHelper groupHelper = new GroupHelper();
        
        ArrayList<String> idBTs;
        ArrayList<String> idGroupsOfCOncept;
        ArrayList<String> idGroupsOfBT;        
        String idGroup;

        // récupération de tous les Id concepts qui ont plusieurs groupes en même temps
        ArrayList<String> idConcepts = conceptHelper.getConceptsHavingMultiGroup(ds, idThesaurus);

        
        
        
        for (String idConcept : idConcepts) {
            // récupértation des Groupes pour ce concept
            idGroupsOfCOncept = groupHelper.getListIdGroupOfConcept(ds, idThesaurus, idConcept);            
            
            // récupértation des BT pour ce concept
            idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
            
            // comparaison pour détecter l'incohérence
            // s'il n'y a qu'un sel BT pour un concept qui appartient à 2 Groupes, il y a peut être un problème
            if(idBTs.size() < 2) {
                for (String idBT : idBTs) {
                    idGroupsOfBT = groupHelper.getListIdGroupOfConcept(ds, idThesaurus, idBT);
                    if(idGroupsOfBT.size() == 1) {

                    }
                }
            }

            

            
            
            
        }

        return false;
    } *
        
        
    /**
     * Permet de supprimer les BT à un concept qui est Top terme, 
     * c'est incohérent et ca provoque une boucle à l'infini
     * @param ds
     * @param idThesaurus
     * @return 
     */
    public boolean removeBTofTopTerm(HikariDataSource ds,
            String idThesaurus) {
        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<String> idBTs;

        // récupération de tous les Id TT du thésaurus
        ArrayList<String> tabIdTT = conceptHelper.getAllTopTermOfThesaurus(ds, idThesaurus);
        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            for (String idConcept : tabIdTT) {
                idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
                for (String idBT : idBTs) {
                    if (!idBT.isEmpty()) {
                        if (!relationsHelper.deleteRelationBT(conn, idConcept, idThesaurus, idBT, 1)) {
                            conn.rollback();
                            conn.close();
                            return false;
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
     * Permet de detecter les concepts qui n'ont pas de BT,
     * puis vérifier qu'ils ont l'info de TopTerme
     * @param ds
     * @param idThesaurus
     * @return 
     */
    public boolean reorganizingTopTerm(HikariDataSource ds,
            String idThesaurus) {
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
     * Permet de détecter les concepts TT et les organiser dans la BDD pour pouvoir les afficher 
     * un TT est un concept qui n'a pas de BT, il faut ajouter dans la table Concept l'information top_concept = true
     * @param ds
     * @param idThesaurus
     * @return 
     */
 /*   public boolean detectAndSetTopTerm(HikariDataSource ds,
            String idThesaurus) {
        ConceptHelper conceptHelper = new ConceptHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<String> idConcepts;

        // récupération de tous les Id concepts qui n'ont pas de BT
        ArrayList<String> idConcepts = conceptHelper.getAllTopTermOfThesaurus(ds, idThesaurus);
        try {
            Connection conn = ds.getConnection();
            conn.setAutoCommit(false);
            for (String idConcept : tabIdTT) {
                idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
                for (String idBT : idBTs) {
                    if (!idBT.isEmpty()) {
                        if (!relationsHelper.deleteRelationBT(conn, idConcept, idThesaurus, idBT, 1)) {
                            conn.rollback();
                            conn.close();
                            return false;
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
    }    */

    /**
     * Permet de supprimer les relations en boucle qui sont interdites (100 ->
     * BT -> 100) ou (100 -> NT -> 100)ou (100 -> RT -> 100) c'est incohérent et
     * ca provoque une boucle à l'infini
     *
     * @param ds
     * @param role
     * @param idThesaurus
     * @return
     */
    public boolean removeLoopRelations(HikariDataSource ds,
            String role,
            String idThesaurus) {

        RelationsHelper relationsHelper = new RelationsHelper();

        // récupération des relations en Loop
        ArrayList<HierarchicalRelationship> tabRelations
                = relationsHelper.getListLoopRelations(ds, role, idThesaurus);
        if (!tabRelations.isEmpty()) {
            for (HierarchicalRelationship relation : tabRelations) {
                relationsHelper.deleteThisRelation(
                        ds,
                        relation.getIdConcept1(),
                        idThesaurus,
                        role,
                        relation.getIdConcept2());
            }
        }
        return true;
    }

    public String getNewId(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder pass = new StringBuilder();
        for (int x = 0; x < length; x++) {
            int i = (int) Math.floor(Math.random() * (chars.length() - 1));
            pass.append(chars.charAt(i));
        }
        return pass.toString();
    }

    public Date getDate() {
        return new java.util.Date();
    }

}
