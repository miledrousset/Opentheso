package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.model.SelectItem;
import fr.cnrs.opentheso.models.group.ConceptGroup;
import fr.cnrs.opentheso.models.group.ConceptGroupLabel;
import fr.cnrs.opentheso.models.concept.NodeAutoCompletion;
import fr.cnrs.opentheso.models.group.NodeGroupType;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.concept.NodeMetaData;
import fr.cnrs.opentheso.models.nodes.NodePreference;
import fr.cnrs.opentheso.models.concept.NodeUri;
import fr.cnrs.opentheso.models.group.NodeGroup;
import fr.cnrs.opentheso.models.group.NodeGroupLabel;
import fr.cnrs.opentheso.models.group.NodeGroupTraductions;
import fr.cnrs.opentheso.models.candidats.DomaineDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.ws.ark.ArkHelper2;
import fr.cnrs.opentheso.ws.handle.HandleHelper;
import java.util.Collections;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Data
@Service
public class GroupHelper {

    @Autowired
    private ToolsHelper toolsHelper;

    @Autowired
    private HandleHelper handleHelper;

    private NodePreference nodePreference;
    private String message;
    
    /**
     * permet de savoir si le group à déplacer est vers un descendant, c'est interdit
     */
    public boolean isMoveToDescending(HikariDataSource ds, String idGroup, String toIdGroup, String idTheso){

        var idGroupDescending = getAllGroupDescending(ds, idGroup, idTheso);
        return idGroupDescending.contains(toIdGroup);
    }
    
    /**
     * Récupération des IdGroup fils par récurcivité
     */
    public List<String> getAllGroupDescending(HikariDataSource ds, String idGroup, String idTheso) {

        return getDescendingId_(ds, idGroup, idTheso, new ArrayList<>());
    }
    
    private List<String> getDescendingId_(HikariDataSource ds, String idGroup, String idTheso, List<String> allIdGroup) {
        
        allIdGroup.add(idGroup);

        var listIds = getListGroupChildIdOfGroup(ds, idGroup, idTheso);

        for (String idGroupFils : listIds) {
            getDescendingId_(ds, idGroupFils, idTheso, allIdGroup);
        }
        return allIdGroup;
    }    
    
    /**
     * Permet de retirer le groupe du parent
     */
    public boolean removeGroupFromGroup(HikariDataSource ds, String idGroup, String idParent, String idTheso){

        boolean status = false;
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from relation_group where"
                            + " LOWER(id_group2)= '" + idGroup.toLowerCase() + "'"
                            + " and "
                            + " LOWER(id_group1)='" + idParent.toLowerCase() + "'"
                            + " AND id_thesaurus='" + idTheso + "'");

                    status = true;
                    updateModifiedDate(ds, idGroup, idTheso);
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating groupType : " + idGroup, sqle);
        }
        return status;        
    }

    /**
     * permet de mettre à jour le Type de groupe
     */
    public boolean updateTypeGroup(HikariDataSource ds, String type, String idTheso, String idGroup) {

        Connection conn;
        Statement stmt;
        boolean status = false;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept_group SET idtypecode='" + type + "' WHERE LOWER(idgroup)='" + idGroup.toLowerCase() + "'"
                            + " AND idthesaurus='" + idTheso + "'";

                    stmt.executeUpdate(query);
                    status = true;
                    updateModifiedDate(ds, idGroup, idTheso);
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating groupType : " + idGroup, sqle);
        }
        return status;
    }

    /**
     * Permet de renommer un Domaine
     */
    public boolean renameGroup(
            HikariDataSource ds,
            String label,
            String idLang,
            String idGroup,
            String idTheso,
            int idUser) {
        boolean status = false;
        label = fr.cnrs.opentheso.utils.StringUtils.convertString(label);

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept_group_label "
                            + "set lexicalvalue='" + label + "',"
                            + " modified = current_date"
                            + " WHERE lang ='" + idLang + "'"
                            + " AND idthesaurus='" + idTheso + "'"
                            + " AND LOWER(idgroup) ='" + idGroup.toLowerCase() + "'");
                status = true;
                addGroupTraductionHistoriqueRollBack(conn, idGroup, idTheso, idLang, label, idUser);
                updateModifiedDate(ds, idGroup, idTheso);
            }
        } catch (SQLException sqle) {
            Logger.getLogger(GroupHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }
        return status;
    }
    
    /**
     * mettre à jour la date de modification du groupe/collection
     * @param ds
     * @param idGroup
     * @param idTheso 
     */
    private void updateCreatedDate(
            HikariDataSource ds,
            String idGroup,
            String idTheso) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept_group "
                            + "set created = current_date"
                            + " WHERE idthesaurus='" + idTheso + "'"
                            + " AND LOWER(idgroup) ='" + idGroup.toLowerCase() + "'");
            }
        } catch (SQLException sqle) {
            Logger.getLogger(GroupHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }
    } 
    
    /**
     * mettre à jour la date de modification du groupe/collection
     * @param ds
     * @param idGroup
     * @param idTheso 
     */
    public void updateModifiedDate(
            HikariDataSource ds,
            String idGroup,
            String idTheso) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept_group "
                            + "set modified = current_date"
                            + " WHERE idthesaurus='" + idTheso + "'"
                            + " AND LOWER(idgroup) ='" + idGroup.toLowerCase() + "'");
            }
        } catch (SQLException sqle) {
            Logger.getLogger(GroupHelper.class.getName()).log(Level.SEVERE, null, sqle);
        }
    }    

    /**
     * Permet de retourner le type de groupe en utilisant le code
     *
     * @param ds
     * @param codeType
     * @return
     */
    public NodeGroupType getGroupType(HikariDataSource ds, String codeType) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        NodeGroupType nodeGroupType = null;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Select code, label, skoslabel from"
                            + " concept_group_type where"
                            + " code = '" + codeType + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        nodeGroupType = new NodeGroupType();
                        nodeGroupType.setCode(resultSet.getString("code"));
                        nodeGroupType.setLabel(resultSet.getString("label"));
                        nodeGroupType.setSkosLabel(resultSet.getString("skoslabel"));
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Type of Group : " + sqle);
        }
        return nodeGroupType;
    }

    /**
     * permet de supprimer les concepts qui ont une relation vers une collection supprimée
     */
    public boolean deleteConceptsHavingRelationShipWithDeletedGroup(HikariDataSource ds, String idTheso) {
        boolean status = false;
        try (Connection  conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select idgroup " +
                        " from concept_group_concept " +
                        " where " +
                        " idthesaurus = '" + idTheso + "' " +
                        " and " +
                        " idgroup not in" +
                        " (select idgroup from concept_group where idthesaurus = '" + idTheso + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        // le group est supprimé, alors on supprime toutes les relations avec les concepts
                        removeAllConceptsFromThisGroup(ds, resultSet.getString("idgroup"), idTheso);
                    }
                    status = true;
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting fack relations groups of theso : " + idTheso, sqle);
        }
        return status;
    }

    /**
     * permet de supprimer les concepts de la table collection
     * qui ont une relation vers un concept supprimé
     */
    public boolean deleteConceptsHavingRelationShipWithDeletedConcept(HikariDataSource ds, String idTheso) {
        boolean status = false;
        try (Connection  conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select idconcept, idgroup " +
                        " from concept_group_concept " +
                        " where " +
                        " idthesaurus = '" + idTheso + "' " +
                        " and " +
                        " idconcept not in" +
                        " (select id_concept from concept where id_thesaurus = '" + idTheso + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        removeConceptFromThisGroup(ds, resultSet.getString("idconcept"), resultSet.getString("idgroup"), idTheso);
                    }
                    status = true;
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting fack relations groups of theso : " + idTheso, sqle);
        }
        return status;
    }

    public boolean addIdArkGroup(HikariDataSource ds,
            String idTheso,
            String idGroup,
            String labelGroup) {
        if (nodePreference != null) {
            if (nodePreference.isUseArkLocal()) {
                generateArkIdLocal(ds, idTheso, idGroup);
                return true;
            }            
            
            ArkHelper2 arkHelper2 = new ArkHelper2(nodePreference);
            if (!arkHelper2.login()) {
                message = "Erreur de connexion !!";
                return false;
            }            
            
            Connection conn;
            try {
                // Get connection from pool
                conn = ds.getConnection();
                conn.setAutoCommit(false);
                if (nodePreference.isUseArk()) {
                    NodeMetaData nodeMetaData = new NodeMetaData();
                    nodeMetaData.setCreator("");
                    nodeMetaData.setTitle(labelGroup);
                    nodeMetaData.setDcElementsList(new ArrayList<>());
                    
                    String privateUri = "?idg=" + idGroup.toLowerCase() + "&idt=" + idTheso;
                    if (!arkHelper2.addArk(privateUri, nodeMetaData)) {
                        message = arkHelper2.getMessage();
                        message = arkHelper2.getMessage() + "  idGroup = " + idGroup.toLowerCase();
                        conn.rollback();
                        conn.close();
                        return false;
                    }
                    if (!updateArkIdOfGroup(conn, idGroup, idTheso, arkHelper2.getIdArk())) {
                        conn.rollback();
                        conn.close();                        
                        return false;
                    }
                    if (nodePreference.isGenerateHandle()) {
                        if (!updateHandleIdOfGroup(conn, idGroup, idTheso, arkHelper2.getIdHandle())) {
                            conn.rollback();
                            conn.close();                             
                            return false;
                        }
                    }
                }
                conn.commit();
                conn.close();
                updateModifiedDate(ds, idGroup, idTheso);
                return true;
            } catch (SQLException sqle) {
                // Log exception
                log.error("Error while adding ArkId for IdGroup : " + idGroup, sqle);
            }
        }
        return false;
    }
    
    /**
     * Cette fonction permet de générer les idArk en local
     */
    public boolean generateArkIdLocal(HikariDataSource ds, String idTheso, String idGroup) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArkLocal()) {
            return false;
        }

        String idArk = getIdArkOfGroup(ds, idGroup, idTheso);
        
        if(StringUtils.isEmpty(idArk)) {
            idArk = toolsHelper.getNewId(nodePreference.getSizeIdArkLocal(), nodePreference.isUppercase_for_ark(), true);
            idArk = nodePreference.getNaanArkLocal() + "/" + nodePreference.getPrefixArkLocal() + idArk;
        }
        return updateArkIdOfGroup(ds, idGroup, idTheso, idArk);
    }

    /**
     * Cette fonction permet d'ajouter un group (MT, domaine etc..) avec le
     * libellé elle retourne l'identifiant du nouveau groupe ou null
     *
     * @param ds
     * @param nodeConceptGroup
     * @param idUser
     * @return
     */
    public String addGroup(HikariDataSource ds,
            NodeGroup nodeConceptGroup,
            int idUser) {

        String idGroup = null;
        Connection conn;
        String idArk = "";
        String idHandle = "";
        int idSequenceConcept = -1;

        if (nodeConceptGroup.getConceptGroup().getNotation() == null) {
            nodeConceptGroup.getConceptGroup().setNotation("");
        }
        try {
            // Get connection from pool
            conn = ds.getConnection();
            conn.setAutoCommit(false);

            // récupération d'un nouvel id pour group
            idGroup = getNewIdGroup(conn);
            if (idGroup == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            try {
                idSequenceConcept = Integer.parseInt(idGroup);
                idGroup = "g" + idGroup;
            } catch (Exception e) {
                return null;
            }
            if (idSequenceConcept == -1) {
                return null;
            } else {
                // Ajout des informations dans la table de ConceptGroup
                if (!insertGroup(conn, idSequenceConcept, idGroup, idArk,
                        nodeConceptGroup.getConceptGroup().getIdthesaurus(),
                        nodeConceptGroup.getConceptGroup().getIdtypecode(),
                        nodeConceptGroup.getConceptGroup().getNotation(),
                        idHandle)) {
                    conn.rollback();
                    conn.close();
                    return null;
                }
            }

            // ajout de la traduction 
            if (!addGroupTraductionRollBack(conn, idGroup,
                    nodeConceptGroup.getConceptGroup().getIdthesaurus(),
                    nodeConceptGroup.getIdLang(),
                    nodeConceptGroup.getLexicalValue())) {
                conn.rollback();
                conn.close();
                return null;
            }

            // ajout dans l'historique 
            if (!addGroupTraductionHistoriqueRollBack(conn,
                    idGroup,
                    nodeConceptGroup.getConceptGroup().getIdthesaurus(),
                    nodeConceptGroup.getIdLang(),
                    nodeConceptGroup.getLexicalValue(),
                    idUser)) {
                conn.rollback();
                conn.close();
                return null;
            }
            if (nodePreference != null) {
                if (nodePreference.isUseArk()) {
                    NodeMetaData nodeMetaData = new NodeMetaData();
                    nodeMetaData.setCreator("");
                    nodeMetaData.setTitle(nodeConceptGroup.getLexicalValue());
                    nodeMetaData.setDcElementsList(new ArrayList<>());

                    if (!addIdArk__(conn,
                            idGroup,
                            nodeConceptGroup.getConceptGroup().getIdthesaurus(),
                            nodeMetaData)) {
                        conn.rollback();
                        conn.close();
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échouée");
                        return null;
                    }
                }
               
                // création de l'identifiant Handle
                if (nodePreference.isUseHandle()) {
                    if (!addIdHandle(conn,
                            idGroup,
                            nodeConceptGroup.getConceptGroup().getIdthesaurus())) {
                        conn.rollback();
                        conn.close();
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Handle a échouée");
                        return null;
                    }
                }
            }
            conn.commit();
            conn.close();
            updateCreatedDate(ds, idGroup, nodeConceptGroup.getConceptGroup().getIdthesaurus());
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding ConceptGroup : " + idGroup, sqle);
        }
        return idGroup;
    }

    /**
     * Cette fonction permet d'ajouter un libellé pour un group (MT, domaine
     * etc..)
     *
     * @param conn
     * @param idGroup
     * @param idLang
     * @param value
     * @param idTheso
     * @return
     */
    private boolean addGroupTraductionRollBack(Connection conn,
            String idGroup, String idTheso,
            String idLang, String value) {
        Statement stmt;
        boolean status = false;
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        try {
            stmt = conn.createStatement();
            try {

                String query = "Insert into concept_group_label "
                        + "(lexicalvalue, created, modified,lang, idthesaurus, idgroup)"
                        + " values ("
                        + "'" + value + "'"
                        + ",current_date"
                        + ",current_date"
                        + ",'" + idLang + "'"
                        + ",'" + idTheso + "'"
                        + ",'" + idGroup.toLowerCase() + "'" + ")";

                stmt.executeUpdate(query);
                status = true;
            } finally {
                stmt.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding traduction of Group : " + idGroup, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter un libellé pour un group (MT, domaine
     * etc..)
     *
     * @param ds
     * @param idGroup
     * @param idLang
     * @param value
     * @param idTheso
     * @return
     */
    public boolean addGroupTraduction(HikariDataSource ds,
            String idGroup, String idTheso,
            String idLang, String value) {
        boolean status = false;
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("Insert into concept_group_label "
                        + "(lexicalvalue, created, modified,lang, idthesaurus, idgroup)"
                        + "values ("
                        + "'" + value + "'"
                        + ",current_date"
                        + ",current_date"
                        + ",'" + idLang + "'"
                        + ",'" + idTheso + "'"
                        + ",'" + idGroup.toLowerCase() + "'" + ")");
                status = true;  
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding traduction of Group : " + idGroup, sqle);
        }
        return status;
    }    
    
    /**
     * Cette fonction permet d'ajouter une ligne historisque du chagement dans
     * les traductions du groupe
     *
     * @param conn
     * @param idGroup
     * @param idThesaurus
     * @param value
     * @param idUser
     * @param idLang
     * @return
     */
    public boolean addGroupTraductionHistoriqueRollBack(Connection conn,
            String idGroup, String idThesaurus,
            String idLang, String value,
            int idUser) {

        Statement stmt;
        boolean status = false;
        value = fr.cnrs.opentheso.utils.StringUtils.convertString(value);
        try {
            stmt = conn.createStatement();
            try {

                String query = "Insert into concept_group_label_historique "
                        + "(lexicalvalue,lang, idthesaurus, idgroup, id_user)"
                        + "values ("
                        + "'" + value + "'"
                        + ",'" + idLang + "'"
                        + ",'" + idThesaurus + "'"
                        + ",'" + idGroup.toLowerCase() + "'"
                        + ",'" + idUser + "'" + ")";

                stmt.executeUpdate(query);
                status = true;

            } finally {
                stmt.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding traduction to ConceptGroupLabel : " + idGroup, sqle);
            } else {
                status = true;
            }

        }
        return status;
    }

    /**
     * Permet d'insérer un Group dans la BDD
     *
     * @param conn
     * @param idGroup
     * @param idArk
     * @param idTheso
     * @param idTypeCode
     * @param notation
     * @return
     */
    private boolean insertGroup(Connection conn,
            int idSequenceGroup,
            String idGroup, String idArk,
            String idTheso, String idTypeCode,
            String notation, String idHandle) {
        Statement stmt;
        boolean status = false;
        try {
            stmt = conn.createStatement();
            try {
                String query = "Insert into concept_group "
                        + "(idgroup,id_ark,idthesaurus,idtypecode,notation,id, id_handle)"
                        + " values("
                        + "'" + idGroup.toLowerCase() + "'"
                        + ",'" + idArk + "'"
                        + ",'" + idTheso + "'"
                        + ",'" + idTypeCode + "'"
                        + ",'" + notation + "'"
                        + "," + idSequenceGroup
                        + ",'" + idHandle + "'"
                        + ")";
                stmt.executeUpdate(query);
                status = true;
            } finally {
                stmt.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding group : " + idGroup, sqle);
        }
        return status;
    }

    /**
     *
     * @param conn
     * @param idThesaurus
     * @param nodeMetaData
     * @return
     */
    private boolean addIdArk__(Connection conn,
            String idGroup,
            String idThesaurus,
            NodeMetaData nodeMetaData) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }
        ArkHelper2 arkHelper2 = new ArkHelper2(nodePreference);
        if (!arkHelper2.login()) {
            message = "Erreur de connexion !!";
            return false;
        }

        String privateUri = "?idg=" + idGroup.toLowerCase() + "&idt=" + idThesaurus;
        if (!arkHelper2.addArk(privateUri, nodeMetaData)) {
            message = arkHelper2.getMessage();
            return false;
        }

        String idArk = arkHelper2.getIdArk();
        String idHandle = arkHelper2.getIdHandle();
        if (idHandle == null) {
            idHandle = "";
        }
        if (!updateArkIdOfGroup(conn, idGroup, idThesaurus, idArk)) {
            return false;
        }
        if (nodePreference.isGenerateHandle()) {
            return updateHandleIdOfGroup(conn, idGroup.toLowerCase(), idThesaurus, idHandle);
        } else
            return true;
    }

    private boolean addIdHandle(Connection conn,
            String idGroup,
            String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        String privateUri = "?idg=" + idGroup.toLowerCase() + "&idt=" + idThesaurus;

        String idHandle = handleHelper.addIdHandle(privateUri, nodePreference);
        if (idHandle == null) {
            message = handleHelper.getMessage();
            return false;
        }
        return updateHandleIdOfGroup(conn, idGroup,
                idThesaurus, idHandle);
    }

    
    /**
     * Permet de retourner un Id numérique et unique pour le Concept
     */
    private String getNewIdGroup(Connection conn) {
        String idGroup = getNewIdGroup__(conn);
        while (isIdGroupExiste(conn, "d" + idGroup)) {
            idGroup = getNewIdGroup__(conn);
        }
        return idGroup;
    }

    /**
     * Permet de retourner un Id numérique et unique pour le Concept
     */
    private String getNewIdGroup__(Connection conn) {
        String idGroup = null;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select nextval('concept_group__id_seq') from concept_group__id_seq");
            try (ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()) {
                    int idNumerique = resultSet.getInt(1);
                    idGroup = "" + (idNumerique);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idGroup;
    }

    /**
     * Cette fonction permet de savoir si l'ID du group existe ou non
     *
     * @param ds
     * @param idGroup
     * @param idTheso
     * @return boolean
     */
    public boolean isIdGroupExiste(HikariDataSource ds,
            String idGroup, String idTheso) {

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idgroup from concept_group where "
                            + " LOWER(idgroup) = '" + idGroup.toLowerCase() + "'" 
                            + " and idthesaurus = '" + idTheso + "'");
                try(ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        if(resultSet.getRow() != 0) return true;
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if id exist : " + idGroup, sqle);
        }
        return false;
    }    
    
    /**
     * Cette fonction permet de savoir si l'ID du group existe ou non
     *
     * @param conn
     * @param idGroup
     * @return boolean
     */
    public boolean isIdGroupExiste(Connection conn,
            String idGroup) {

        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group where "
                            + "LOWER(idgroup) = '" + idGroup.toLowerCase() + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if id exist : " + idGroup, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si la notation esxiste dans le thésaurus
     * (pour les groupes)
     *
     * @param ds
     * @param notation
     * @param idTheso
     * @return boolean
     */
    public boolean isNotationExist(HikariDataSource ds,
            String notation, String idTheso) {

        Statement stmt;
        ResultSet resultSet = null;
        Connection conn;
        boolean existe = false;
        if(notation == null || notation.isEmpty()) return false;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group where "
                            + " notation = '" + notation + "'"
                            + " and idthesaurus = '" + idTheso + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if notation existe : " + notation, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de mettre à jour l'identifiant Ark d'un group
     * existant
     *
     * @param conn
     * @param idGroup
     * @param idTheso
     * @param idArk
     * @return
     */
    public boolean updateArkIdOfGroup(Connection conn, String idGroup,
            String idTheso, String idArk) {

        Statement stmt;
        boolean status = false;
        try {

            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept_group "
                            + "set id_ark='" + idArk + "'"
                            + " WHERE LOWER(idgroup) ='" + idGroup.toLowerCase() + "'"
                            + " AND idthesaurus='" + idTheso + "'";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //      conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating or adding ArkId of Group : " + idGroup, sqle);
        }
        return status;
    }
    
    /**
     * Cette fonction permet de mettre à jour l'identifiant Ark d'un group
     * existant
     *
     * @param ds
     * @param idGroup
     * @param idTheso
     * @param idArk
     * @return
     */
    public boolean updateArkIdOfGroup(HikariDataSource ds, String idGroup,
            String idTheso, String idArk) {

        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            try(Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept_group "
                            + "set id_ark='" + idArk + "'"
                            + " WHERE LOWER(idgroup) ='" + idGroup.toLowerCase() + "'"
                            + " AND idthesaurus='" + idTheso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating or adding ArkId of Group : " + idGroup, sqle);
        }
        return status;
    }    

    /**
     * Cette fonction permet de mettre à jour l'identifiant Handle d'un group
     * existant
     *
     * @param conn
     * @param idGroup
     * @param idTheso
     * @param idHandle
     * @return
     */
    public boolean updateHandleIdOfGroup(Connection conn, String idGroup,
            String idTheso, String idHandle) {

        Statement stmt;
        boolean status = false;
        try {

            try {
                stmt = conn.createStatement();
                try {
                    String query = "UPDATE concept_group "
                            + "set id_handle='" + idHandle + "'"
                            + " WHERE LOWER(idgroup) ='" + idGroup.toLowerCase() + "'"
                            + " AND idthesaurus='" + idTheso + "'";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //      conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating or adding HandleId of Group : " + idGroup, sqle);
        }
        return status;
    }

    /**
     * permet de supprimer une relation entre le concept et le groupe
     * suppression de l'appartenance du concept à ce groupe
     *
     * @param ds
     * @param idGroup
     * @param idConcept
     * @param idThesaurus
     * @param idUser
     * @return
     */
    public boolean deleteRelationConceptGroupConcept(HikariDataSource ds,
            String idGroup, String idConcept, String idThesaurus, int idUser) {
        boolean status = false;
        Connection conn;
        Statement stmt;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_group_concept where "
                            + "LOWER(idgroup) = '" + idGroup.toLowerCase() + "'"
                            + " and idthesaurus = '" + idThesaurus + "'"
                            + " and idconcept = '" + idConcept + "'";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting relation groupe-concept of Concept : " + idConcept + " and group: " + idGroup, sqle);
        }
        return status;
    }

    /**
     * permet de supprimer l'appartenance des concepts à ce groupe/ collection
     * cas d'une suppression de collection
     *
     * @param ds
     * @param idGroup
     * @param idTheso
     * @return #MR
     */
    public boolean removeAllConceptsFromThisGroup(HikariDataSource ds, String idGroup, String idTheso) {
        boolean status = false;
        Connection conn;
        Statement stmt;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_group_concept where "
                            + "LOWER(idgroup) = '" + idGroup.toLowerCase() + "'"
                            + " and idthesaurus = '" + idTheso + "'";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting all relations of groupe-concept of group : " + idGroup, sqle);
        }
        return status;
    }
    
    /**
     * permet de supprimer l'appartenance des concepts à ce groupe/ collection
     *
     * @param ds
     * @param idConcept
     * @param idGroup
     * @param idTheso
     * @return #MR
     */
    public boolean removeConceptFromThisGroup(HikariDataSource ds, String idConcept, String idGroup, String idTheso) {
        boolean status = false;
        Connection conn;
        Statement stmt;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_group_concept where "
                            + " LOWER(idgroup) = '" + idGroup.toLowerCase() + "'"
                            + " and idconcept = '" + idConcept + "'"
                            + " and idthesaurus = '" + idTheso + "'";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting concept from group-concept : " + idConcept, sqle);
        }
        return status;
    }
    

    /**
     * permet de supprimer l'appartenance des concepts avec des relations vide vers une collection
     *
     * @param ds
     * @param idTheso
     * @return #MR
     */
    public boolean deleteConceptsWithEmptyRelation(HikariDataSource ds, String idTheso) {
        boolean status = false;
        
        try (Connection  conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("delete FROM concept_group_concept" +
                                " WHERE idthesaurus = '" + idTheso + "' and idgroup = ''");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting fack relations groups of theso : " + idTheso, sqle);
        }
        return status;
    }
    
    /**
     * Cette fonction permet de supprimer un groupe et ses traductions
     * (subGroup)
     *
     * @param conn
     * @param idGroup
     * @param idThesaurus
     * @param idUser
     * @return #MR
     */
    public boolean deleteConceptGroupRollBack(Connection conn, String idGroup, String idThesaurus, int idUser) {

        Statement stmt;
        boolean status = false;
        try {
            stmt = conn.createStatement();
            try {
                // Suppression des traductions
                String query = "delete from concept_group_label where"
                        + " idthesaurus = '" + idThesaurus + "'"
                        + " and LOWER(idgroup)  = '" + idGroup.toLowerCase() + "'";
                stmt.executeUpdate(query);

                // Suppression du groupe
                query = "delete from concept_group where"
                        + " idthesaurus = '" + idThesaurus + "'"
                        + " and LOWER(idgroup)  = '" + idGroup.toLowerCase() + "'";
                stmt.executeUpdate(query);

                // Suppression du sous groupe
                query = "delete from relation_group where"
                        + " id_thesaurus = '" + idThesaurus + "'"
                        + " and LOWER(id_group2)  = '" + idGroup.toLowerCase() + "'";
                stmt.executeUpdate(query);
                status = true;

            } finally {
                stmt.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting group : " + idGroup, sqle);
        }
        return status;
    }

    /**
     * permet de retourner la liste des domaines de premier niveau (MT, G, C, T)
     *
     * @param ds
     * @param idTheso
     * @param idLang #MR
     * @param isSortByNotation
     * @return
     */
    public ArrayList<NodeGroup> getListRootConceptGroup(HikariDataSource ds, String idTheso, String idLang, boolean isSortByNotation) {

        ArrayList<NodeGroup> nodeConceptGroupList = new ArrayList<>();
        ArrayList<String> tabIdConceptGroup = getListIdOfRootGroup(ds, idTheso);

        for (String idGroup : tabIdConceptGroup) {
            NodeGroup nodeConceptGroup = getThisConceptGroup(ds, idGroup, idTheso, idLang);
            if (nodeConceptGroup == null) {
                return null;
            }
            if (isHaveSubGroup(ds, idTheso, idGroup)) {
                nodeConceptGroup.setHaveChildren(true);
            }
            if (isGroupHaveConcepts(ds, idGroup, idTheso)) {
                nodeConceptGroup.setHaveChildren(true);
            }

            if (StringUtils.isEmpty(nodeConceptGroup.getLexicalValue())) {
                nodeConceptGroup.setLexicalValue("__" + idGroup);
            }

            nodeConceptGroupList.add(nodeConceptGroup);
        }
        if (!isSortByNotation) {
            Collections.sort(nodeConceptGroupList);
        }
        return nodeConceptGroupList;

    }

    /**
     * permet de trouver la liste des Id des Groupes de premier niveau
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> getListIdOfRootGroup(HikariDataSource ds,
            String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList tabIdConceptGroup = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup, notation from concept_group where idthesaurus = '"
                            + idThesaurus
                            + "' and  idgroup NOT IN ( SELECT id_group2 FROM relation_group WHERE relation = 'sub' and id_thesaurus = '" + idThesaurus + "')"
                            + " order by notation ASC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    tabIdConceptGroup = new ArrayList();
                    while (resultSet.next()) {
                        tabIdConceptGroup.add(resultSet.getString("idgroup"));
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Id or Groups of thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConceptGroup;
    }

    /**
     * Permet de retourner un NodeConceptGroup par identifiant, par thésaurus et
     * par langue / ou null si rien cette fonction ne retourne pas les détails
     * et les traductions
     *
     * @param ds le pool de connexion
     * @param idConceptGroup
     * @param idThesaurus
     * @param idLang
     * @return Objet Class NodeConceptGroup
     */
    public NodeGroup getThisConceptGroup(HikariDataSource ds,
            String idConceptGroup, String idThesaurus, String idLang) {

        NodeGroup nodeConceptGroup = null;
        ConceptGroup conceptGroup = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * from concept_group where "
                            + " LOWER(idgroup) = '" + idConceptGroup.toLowerCase() + "'"
                            + " and idthesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()){
                    if (resultSet.next()) {
                        if (resultSet.getRow() != 0) {
                            conceptGroup = new ConceptGroup();
                            conceptGroup.setIdgroup(idConceptGroup);
                            conceptGroup.setIdthesaurus(idThesaurus);
                            conceptGroup.setIdARk(resultSet.getString("id_ark"));
                            conceptGroup.setIdHandle(resultSet.getString("id_handle"));
                            conceptGroup.setIdtypecode(resultSet.getString("idtypecode"));
                            conceptGroup.setNotation(resultSet.getString("notation"));
                            conceptGroup.setCreated(resultSet.getDate("created"));
                            conceptGroup.setModified(resultSet.getDate("modified"));
                        }
                    }
                }
                if (conceptGroup != null) {
                    stmt.executeQuery("SELECT * FROM concept_group_label WHERE"
                            + " LOWER(idgroup) = '" + conceptGroup.getIdgroup().toLowerCase() + "'"
                            + " AND idthesaurus = '" + idThesaurus + "'"
                            + " AND lang = '" + idLang + "'");
                    try (ResultSet resultSet = stmt.getResultSet()){
                        nodeConceptGroup = new NodeGroup();
                        if (resultSet.next()) {                    
                            nodeConceptGroup.setLexicalValue(resultSet.getString("lexicalvalue"));
                            nodeConceptGroup.setIdLang(idLang);
                            nodeConceptGroup.setCreated(resultSet.getDate("created"));
                            nodeConceptGroup.setModified(resultSet.getDate("modified"));
                            
                        } else {
                            nodeConceptGroup.setLexicalValue("");
                            nodeConceptGroup.setIdLang(idLang);
                        }
                        nodeConceptGroup.setConceptGroup(conceptGroup);
                    }
                } 
            } 
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding element : " + idThesaurus, sqle);
        }
        return nodeConceptGroup;
    }
    
    /**
     * Permet de retourner un NodeConceptGroup par identifiant, par thésaurus et
     * par langue / ou null si rien cette fonction ne retourne pas les détails
     * et les traductions
     *
     * @param ds le pool de connexion
     * @param idGroup
     * @param idThesaurus
     * @return Objet Class NodeConceptGroup
     */
    public NodeUri getThisGroupIds(HikariDataSource ds, String idGroup, String idThesaurus) {

        NodeUri nodeUri = null;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idgroup, id_ark, id_handle, id_doi from concept_group"
                        + " where LOWER(idgroup) = '" + idGroup.toLowerCase() + "'"
                        + " and idthesaurus = '" + idThesaurus + "'"
                );
                
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        nodeUri = new NodeUri();
                        nodeUri.setIdConcept(resultSet.getString("idgroup"));
                        nodeUri.setIdArk(resultSet.getString("id_ark"));
                        nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        nodeUri.setIdDoi(resultSet.getString("id_doi"));
                    }
                    return nodeUri;
                }
            } 
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idsOfGroup : " + idGroup, sqle);
        }
        return null;
    }    
    

    /**
     * Permet de retourner la liste des sous_groupes d'un Group (type G/C/MT/T)
     * si le group n'est pas traduit, on récupère l'ID à la place
     *
     * @param ds
     * @param idConceptGroup
     * @param idTheso
     * @param idLang
     * @param isSortByNotation
     * @return #MR
     */
    public ArrayList<NodeGroup> getListChildsOfGroup(HikariDataSource ds,
            String idConceptGroup, String idTheso, String idLang, boolean isSortByNotation) {

        ArrayList<String> lisIdGroups = getListGroupChildIdOfGroup(ds, idConceptGroup, idTheso);
        if (lisIdGroups == null) {
            return null;
        }
        if (lisIdGroups.isEmpty()) {
            return null;
        }

        ArrayList<NodeGroup> nodeGroups = new ArrayList<>();

        for (String idGroup : lisIdGroups) {
            NodeGroup nodeConceptGroup;
            nodeConceptGroup = getThisConceptGroup(ds, idGroup, idTheso, idLang);
            if (nodeConceptGroup == null) {
                return null;
            }
            if (isHaveSubGroup(ds, idTheso, idGroup)) {
                nodeConceptGroup.setHaveChildren(true);
            }
            if (isGroupHaveConcepts(ds, idGroup, idTheso)) {
                nodeConceptGroup.setHaveChildren(true);
            }

            if (nodeConceptGroup.getLexicalValue().isEmpty()) {
                nodeConceptGroup.setLexicalValue("__" + idGroup);
            }
            nodeGroups.add(nodeConceptGroup);
        }
        if (!isSortByNotation) {
            Collections.sort(nodeGroups);
        }        
        
        return nodeGroups;
    }

    /**
     * Retourne la liste des identifiants des groupes fils
     *
     * @param ds
     * @param idGRoup
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> getListGroupChildIdOfGroup(HikariDataSource ds, String idGRoup, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<String> idGroupParentt = new ArrayList<>();
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + "  concept_group.notation, "
                            + "  concept_group.idgroup"
                            + " FROM "
                            + "  relation_group, "
                            + "  concept_group"
                            + " WHERE "
                            + "  concept_group.idthesaurus = relation_group.id_thesaurus AND"
                            + "  LOWER(concept_group.idgroup) = LOWER(relation_group.id_group2) AND"
                            + "  concept_group.idthesaurus = '" + idThesaurus + "' AND "
                            + "  LOWER(relation_group.id_group1) = '" + idGRoup.toLowerCase() + "' AND "
                            + "  relation_group.relation = 'sub'"
                            + "  order by notation ASC;";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroupParentt.add(resultSet.getString("idgroup"));
                        }
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Id of group of Concept : " + idGRoup, sqle);
        }
        return idGroupParentt;
    }
    
    
    /**
     * Permet de retourner la liste des sous Groupes pour un Groupe et un thésaurus
     * donné
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @param idGroupParent
     * @return Objet Class ArrayList NodeUri #MR
     */
    public ArrayList<NodeUri> getListGroupChildOfGroup(HikariDataSource ds, String idThesaurus, String idGroupParent) {

        ArrayList<NodeUri> nodeUris = new ArrayList<>();

        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()) {

                stmt.executeQuery(
                        "SELECT" +
                        " relation_group.id_group2, concept_group.id_ark, concept_group.id_handle, concept_group.id_doi" +
                        " FROM relation_group, concept_group " +
                        " WHERE" +
                        " concept_group.idthesaurus = relation_group.id_thesaurus " +
                        " AND" +
                        " LOWER(concept_group.idgroup) = LOWER(relation_group.id_group2) " +
                        " AND" +
                        " concept_group.idthesaurus = '" + idThesaurus + "'" +
                        " AND" +
                        " LOWER(relation_group.id_group1) = '" + idGroupParent.toLowerCase() + "'" +
                        " AND" +
                        " relation_group.relation = 'sub'" +
                        " order by id_group2 ASC;"
                );

                try (ResultSet resultSet = stmt.getResultSet()) {

                    while (resultSet.next()) {
                        NodeUri nodeUri = new NodeUri();
                        nodeUri.setIdConcept(resultSet.getString("id_group2"));
                        if (resultSet.getString("id_ark") == null) {
                            nodeUri.setIdArk("");
                        } else {
                            nodeUri.setIdArk(resultSet.getString("id_ark"));
                        }
                        if (resultSet.getString("id_handle") == null) {
                            nodeUri.setIdHandle("");
                        } else {
                            nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        }
                        if (resultSet.getString("id_doi") == null) {
                            nodeUri.setIdDoi("");
                        } else {
                            nodeUri.setIdDoi(resultSet.getString("id_doi"));
                        }
                        nodeUris.add(nodeUri);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List or SubGroups : " + idGroupParent, sqle);
        }
        return nodeUris;
    }

    /**
     * permet d'ajouter un sous groupe
     */
    public boolean addSubGroup(HikariDataSource ds, String fatherNodeID, String childNodeID, String idThesaurus) {

        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("Insert into relation_group (id_group1, id_thesaurus, relation, id_group2) values ('"
                        + fatherNodeID.toLowerCase() + "','" + idThesaurus + "','sub','" + childNodeID.toLowerCase() + "')");
                return true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding relation : " + sqle);
            }
            return false;
        }
    }

    /**
     * permet d'ajouter un concept à un groupe ou collection
     *
     * @param ds
     * @param groupID
     * @param conceptID
     * @param idThesaurus
     * @return
     */
    public boolean addConceptGroupConcept(HikariDataSource ds, String groupID, String conceptID, String idThesaurus) {

        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("Insert into concept_group_concept (idgroup, idthesaurus, idconcept) values ('"
                        + groupID.toLowerCase() + "','" + idThesaurus + "','" + conceptID + "')");
                return true;
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                return true;
            } else {
                log.error("Error while addConceptGroupConcept : " + sqle);
                return false;
            }
        }
    }

    /**
     * Ajout d'un groupe à un concept avec Rollback
     *
     * @param conn
     * @param groupID
     * @param conceptID
     * @param idThesaurus
     * @return
     */
    public boolean addConceptGroupConcept(Connection conn,
            String groupID, String conceptID, String idThesaurus) {

        Statement stmt;
        boolean status = false;
        try {

            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into concept_group_concept "
                            + "(idgroup, idthesaurus, idconcept)"
                            + "values ("
                            + "'" + groupID.toLowerCase() + "'"
                            + ",'" + idThesaurus + "'"
                            + ",'" + conceptID + "'"
                            + ")";

                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                //     conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while addConceptGroupConcept : " + sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter un group (MT, domaine etc..) avec le
     * libellé dans le cas d'un import avec idGroup existant
     *
     * @param ds
     * @param idThesaurus
     * @param typeCode
     * @param idArk
     * @param idGroup
     * @param notation
     * @param urlSite
     * @param isArkActive
     * @param created
     * @param modified
     * @param idUser
     * @return
     */
    public boolean insertGroup(HikariDataSource ds, String idGroup, String idThesaurus, String idArk, String typeCode,
            String notation, String urlSite, boolean isArkActive, Date created, Date modified, int idUser) {

        if (idArk == null) {
            idArk = "";
        }
        String createdTemp;
        String modifiedTemp;
        if (created == null) {
            createdTemp = null;
        } else {
            createdTemp = "'" + created + "'";
        }
        if (modified == null) {
            modifiedTemp = null;
        } else {
            modifiedTemp = "'" + modified + "'";
        }        
        
        String idHandle = "";
        /*
         * récupération de l'identifiant Ark pour le ConceptGroup de type : ark:/66666/srvq9a5Ll41sk
         * Controler si l'identifiant du Group existe
         */
        // à faire
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("Insert into concept_group(idgroup, id_ark, idthesaurus, idtypecode, notation, id_handle, created, modified) values ('" 
                        + idGroup.toLowerCase() + "','" + idArk + "','"
                        + idThesaurus + "','" + typeCode + "','" + notation + "','" + idHandle + "'," + createdTemp + "," + modifiedTemp + ")");
                return true;
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
                return true;
            } else {
                log.error("Error while adding ConceptGroup : " + idGroup, sqle);
                return false;
            }
        }
    }

    /**
     * Cette fonction permet de retourner les traductions d'un domaine sans
     * celle qui est en cours
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public ArrayList<NodeGroupTraductions> getGroupTraduction(HikariDataSource ds,
            String idGroup, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeGroupTraductions> nodeGroupTraductionsList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select lang, lexicalvalue from concept_group_label "
                            + "where LOWER(idgroup) = '" + idGroup.toLowerCase() + "'"
                            + " and idthesaurus = '" + idThesaurus + "'"
                            + " and lang != '" + idLang + "'";

                    resultSet = stmt.executeQuery(query);
                    if (resultSet != null) {
                        nodeGroupTraductionsList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeGroupTraductions nodeGroupTraductions = new NodeGroupTraductions();
                            // cas du Group non traduit
                            nodeGroupTraductions.setIdLang(resultSet.getString("lang"));
                            nodeGroupTraductions.setTitle(resultSet.getString("lexicalvalue"));
                            nodeGroupTraductionsList.add(nodeGroupTraductions);
                        }
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting traduction of Group : " + idGroup, sqle);
        }
        return nodeGroupTraductionsList;
    }

    /**
     * Cette fonction permet de retourner les traductions d'un domaine
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return
     */
    public ArrayList<NodeGroupTraductions> getAllGroupTraduction(HikariDataSource ds,
            String idGroup, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeGroupTraductions> nodeGroupTraductionsList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {

                    String query = "select lang, lexicalvalue, created, modified from concept_group_label "
                            + "where LOWER(idgroup) = '" + idGroup.toLowerCase() + "'"
                            + " and idthesaurus = '" + idThesaurus + "'";

                    resultSet = stmt.executeQuery(query);
                    if (resultSet != null) {
                        nodeGroupTraductionsList = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeGroupTraductions nodeGroupTraductions = new NodeGroupTraductions();
                            // cas du Group non traduit
                            nodeGroupTraductions.setIdLang(resultSet.getString("lang"));
                            nodeGroupTraductions.setTitle(resultSet.getString("lexicalvalue"));
                            nodeGroupTraductions.setCreated(resultSet.getDate("created"));
                            nodeGroupTraductions.setModified(resultSet.getDate("modified"));
                            nodeGroupTraductionsList.add(nodeGroupTraductions);
                        }
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All traduction of Group : " + idGroup, sqle);
        }
        return nodeGroupTraductionsList;
    }

    /**
     * Cette fonction permet de rajouter une traduction de domaine
     *
     * @param ds
     * @param conceptGroupLabel
     * @param idUser
     * @return
     */
    public boolean addGroupTraduction(HikariDataSource ds, ConceptGroupLabel conceptGroupLabel, int idUser) {

        conceptGroupLabel.setLexicalValue(fr.cnrs.opentheso.utils.StringUtils.convertString(conceptGroupLabel.getLexicalValue()));
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into concept_group_label (lexicalvalue, created, modified,lang, idthesaurus, idgroup)"
                        + "values ('" + conceptGroupLabel.getLexicalValue() + "',current_date,current_date,'"
                        + conceptGroupLabel.getLang() + "','" + conceptGroupLabel.getIdthesaurus() + "','"
                        + conceptGroupLabel.getIdgroup().toLowerCase() + "'" + ")");
                addGroupTraductionHistorique(ds, conceptGroupLabel, idUser);

                return true;
            }
        } catch (SQLException sqle) {
            if (sqle.getSQLState().equalsIgnoreCase("23505")) {
               return true;
            } else {
                log.error("Error while adding traduction to ConceptGroupLabel : " + conceptGroupLabel.getIdgroup(), sqle);
                return false;
            }
        }
    }

    /**
     * Cette fonction permet de rajouter une traduction de domaine en historique
     *
     * @param ds
     * @param conceptGroupLabel
     * @param idUser
     * @return
     */
    public boolean addGroupTraductionHistorique(HikariDataSource ds,
            ConceptGroupLabel conceptGroupLabel, int idUser) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        conceptGroupLabel.setLexicalValue(
                fr.cnrs.opentheso.utils.StringUtils.convertString(conceptGroupLabel.getLexicalValue()));
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {

                    String query = "Insert into concept_group_label_historique "
                            + "(lexicalvalue,lang, idthesaurus, idgroup, id_user)"
                            + "values ("
                            + "'" + conceptGroupLabel.getLexicalValue() + "'"
                            + ",'" + conceptGroupLabel.getLang() + "'"
                            + ",'" + conceptGroupLabel.getIdthesaurus() + "'"
                            + ",'" + conceptGroupLabel.getIdgroup().toLowerCase() + "'"
                            + ",'" + idUser + "'" + ")";

                    stmt.executeUpdate(query);
                    status = true;

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding traduction to ConceptGroupLabel : " + conceptGroupLabel.getIdgroup(), sqle);
        }
        return status;
    }

    /**
     * retourne l'Id du parent du groupe
     * @param ds
     * @param idGRoup
     * @param idThesaurus
     * @return 
     */
    public String getIdFather(HikariDataSource ds, String idGRoup, String idThesaurus) {

        String idFather = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_group1 from relation_group where id_thesaurus = '"
                            + idThesaurus + "'"
                            + " and LOWER(id_group2) = '" + idGRoup.toLowerCase() + "'"
                            + " and relation='sub'");
                
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idFather = resultSet.getString("id_group1");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while  getFatherOf: " + idThesaurus, sqle);
        }

        return idFather;
    }

    /**
     * Permet de retourner un NodeGroupLable des Labels d'un Group / idThesaurus
     * Ce qui représente un domaine d'un thésaurus avec toutes les traductions
     *
     * @param ds le pool de connexion
     * @param idConceptGroup
     * @param idThesaurus
     * @return Objet ArrayLis ConceptGroupLabel
     */
    public NodeGroupLabel getNodeGroupLabel(HikariDataSource ds, String idConceptGroup, String idThesaurus) {

        NodeGroupLabel nodeGroupLabel = new NodeGroupLabel();
        nodeGroupLabel.setIdGroup(idConceptGroup);
        nodeGroupLabel.setIdThesaurus(idThesaurus);
        nodeGroupLabel.setIdArk(getIdArkOfGroup(ds, idConceptGroup, idThesaurus));
        nodeGroupLabel.setIdHandle(getIdHandleOfGroup(ds, idConceptGroup, idThesaurus));
        nodeGroupLabel.setNotation(getNotationOfGroup(ds, idConceptGroup, idThesaurus));
        nodeGroupLabel.setCreated(getCreatedDate(ds, idConceptGroup, idThesaurus));
        nodeGroupLabel.setModified(getModifiedDate(ds, idConceptGroup, idThesaurus));
        nodeGroupLabel.setNodeGroupTraductionses(getAllGroupTraduction(ds, idConceptGroup, idThesaurus));

        return nodeGroupLabel;
    }

    /**
     * Permet de retourner le lexical Value of Group
     *
     * @param ds le pool de connexion
     * @param idConceptGroup
     * @param idThesaurus
     * @param idLang
     * @return Objet Class NodeConceptGroup
     */
    public String getLexicalValueOfGroup(HikariDataSource ds,
            String idConceptGroup, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String lexicalValue = "";

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT lexicalvalue FROM concept_group_label"
                            + " WHERE idthesaurus = '" + idThesaurus + "'"
                            + " and LOWER(idgroup) = '" + idConceptGroup.toLowerCase() + "'"
                            + " and lang  = '" + idLang + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        lexicalValue = resultSet.getString("lexicalvalue");
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting lexical value of Group : " + idConceptGroup, sqle);
        }
        return lexicalValue;
    }

    /**
     * Cette fonction permet de supprimer une traduciton à un groupe
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @param idLang
     * @return #MR
     */
    public boolean deleteGroupTraduction(HikariDataSource ds,
            String idGroup, String idThesaurus,
            String idLang) {

        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from concept_group_label where"
                            + " idthesaurus = '" + idThesaurus + "'"
                            + " and LOWER(idgroup) = '" + idGroup.toLowerCase() + "'"
                            + " and lang = '" + idLang + "'";
                    stmt.executeUpdate(query);
                    status = true;
                    updateModifiedDate(ds, idGroup, idThesaurus);                    
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting traduction of Group : " + idGroup, sqle);
        }
        return status;
    }

    /**
     * Permet de retourner une ArrayList de NodeConceptGroup par langue et par
     * thésaurus / ou null si rien cette fonction ne retourne pas les détails et
     * les traductions Si le Domaine n'est pas traduit dans la langue en cours,
     * on récupère l'identifiant pour l'afficher à la place
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @param idLang
     * @return Objet Class ArrayList NodeConceptGroup
     */
    public ArrayList<NodeGroup> getListConceptGroup(HikariDataSource ds, String idThesaurus, String idLang) {

        ArrayList<NodeGroup> nodeConceptGroupList;
        ArrayList<String> tabIdConceptGroup = getListIdOfGroup(ds, idThesaurus);

        nodeConceptGroupList = new ArrayList<>();
        for (String tabIdGroup1 : tabIdConceptGroup) {
            NodeGroup nodeConceptGroup;
            nodeConceptGroup = getThisConceptGroup(ds, tabIdGroup1, idThesaurus, idLang);
            if (nodeConceptGroup == null) {
                continue;
            }
            nodeConceptGroupList.add(nodeConceptGroup);
        }

        return nodeConceptGroupList;
    }

    /**
     * Cette fonction permet de récupérer la liste des domaines pour
     * l'autocomplétion
     *
     * @param ds
     * @param idThesaurus
     * @param text
     * @param idLang
     * @return Objet class Concept
     */
    public List<NodeAutoCompletion> getAutoCompletionGroup(HikariDataSource ds,
            String idThesaurus, String idLang, String text) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        List<NodeAutoCompletion> nodeAutoCompletionList = null;
        text = fr.cnrs.opentheso.utils.StringUtils.convertString(text);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT concept_group_label.idgroup,"
                            + " concept_group_label.lexicalvalue FROM concept_group_label"
                            + " WHERE "
                            + " concept_group_label.idthesaurus = '" + idThesaurus + "'"
                            + " AND concept_group_label.lang = '" + idLang + "'"
                            + " AND unaccent_string(concept_group_label.lexicalvalue) ILIKE unaccent_string('%" + text + "%')"
                            + " ORDER BY concept_group_label.lexicalvalue ASC LIMIT 40";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    nodeAutoCompletionList = new ArrayList<>();
                    while (resultSet.next()) {
                        if (resultSet.getRow() != 0) {
                            NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                            nodeAutoCompletion.setIdConcept("");
                            nodeAutoCompletion.setPrefLabel("");
                            nodeAutoCompletion.setGroupLexicalValue(resultSet.getString("lexicalvalue"));
                            nodeAutoCompletion.setIdGroup(resultSet.getString("idgroup"));
                            nodeAutoCompletionList.add(nodeAutoCompletion);
                        }
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of autocompletion of Text : " + text, sqle);
        }

        return nodeAutoCompletionList;
    }

    /**
     * Cette fonction permet de récupérer la liste des domaines pour
     * l'autocomplétion
     *
     * @param ds
     * @param idThesaurus
     * @param text
     * @param idLang
     * @return Objet class Concept
     */
    public ArrayList<NodeIdValue> searchGroup(HikariDataSource ds,
            String idThesaurus, String idLang, String text) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        text = fr.cnrs.opentheso.utils.StringUtils.convertString(text);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT concept_group_label.idgroup,"
                            + " concept_group_label.lexicalvalue FROM concept_group_label"
                            + " WHERE "
                            + " concept_group_label.idthesaurus = '" + idThesaurus + "'"
                            + " AND concept_group_label.lang = '" + idLang + "'"
                            + " AND unaccent_string(concept_group_label.lexicalvalue) ILIKE unaccent_string('%" + text + "%')"
                            + " ORDER BY concept_group_label.lexicalvalue ASC LIMIT 40";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        if (resultSet.getRow() != 0) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setValue(resultSet.getString("lexicalvalue"));
                            nodeIdValue.setId(resultSet.getString("idgroup"));
                            nodeIdValues.add(nodeIdValue);
                        }
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of autocompletion of collection : " + text, sqle);
        }

        return nodeIdValues;
    }

    /**
     * Permet de retourner une ArrayList de String (idGroup) par thésaurus / ou
     * null si rien
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @return Objet Class ArrayList NodeConceptGroup
     */
    public ArrayList<String> getListIdOfGroup(HikariDataSource ds, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList tabIdConceptGroup = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group where idthesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    tabIdConceptGroup = new ArrayList();
                    while (resultSet.next()) {
                        tabIdConceptGroup.add(resultSet.getString("idgroup"));
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Id or Groups of thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConceptGroup;
    }

    /**
     * Permet de retourner la liste Id des Groupes pour un Concept et un
     * thésaurus donné
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @param idConcept
     * @return Objet Class ArrayList NodeConceptGroup
     */
    public ArrayList<String> getListIdGroupOfConcept(HikariDataSource ds, String idThesaurus, String idConcept) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList tabIdConceptGroup = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group_concept where idthesaurus = '" + idThesaurus + "'"
                            + " and idconcept = '" + idConcept + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        tabIdConceptGroup = new ArrayList<>();
                        while (resultSet.next()) {
                            tabIdConceptGroup.add(resultSet.getString("idgroup"));
                        }
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Id or Groups of Concept : " + idConcept, sqle);
        }
        return tabIdConceptGroup;
    }

    /**
     * Permet de retourner la liste des Groupes pour un Concept et un thésaurus
     * donné
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @param idConcept
     * @return Objet Class ArrayList NodeGroup #MR
     */
    public ArrayList<NodeUri> getListGroupOfConceptArk(HikariDataSource ds, String idThesaurus, String idConcept) {

        ArrayList<NodeUri> nodeUris = new ArrayList<>();

        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()) {

                stmt.executeQuery("SELECT concept_group.idgroup, concept_group.id_ark, concept_group.id_handle, concept_group.id_doi "
                        + " FROM concept_group_concept, concept_group"
                        + " WHERE LOWER(concept_group.idgroup) = LOWER(concept_group_concept.idgroup)"
                        + " AND concept_group.idthesaurus = concept_group_concept.idthesaurus"
                        + " AND concept_group_concept.idconcept = '" + idConcept
                        + "' AND concept_group_concept.idthesaurus = '" + idThesaurus + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {

                    while (resultSet.next()) {
                        NodeUri nodeUri = new NodeUri();
                        nodeUri.setIdConcept(resultSet.getString("idgroup"));
                        if (resultSet.getString("id_ark") == null) {
                            nodeUri.setIdArk("");
                        } else {
                            nodeUri.setIdArk(resultSet.getString("id_ark"));
                        }
                        if (resultSet.getString("id_handle") == null) {
                            nodeUri.setIdHandle("");
                        } else {
                            nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        }
                        if (resultSet.getString("id_doi") == null) {
                            nodeUri.setIdDoi("");
                        } else {
                            nodeUri.setIdDoi(resultSet.getString("id_doi"));
                        }
                        nodeUris.add(nodeUri);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Id or Groups of Concept : " + idConcept, sqle);
        }
        return nodeUris;
    }

    /**
     * Permet de retourner la liste des Groupes pour un Concept et un thésaurus
     * donné
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @param idConcept
     * @param idLang
     * @return Objet Class ArrayList NodeGroup
     */
    public ArrayList<NodeGroup> getListGroupOfConcept(HikariDataSource ds, String idThesaurus, String idConcept, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        ArrayList<NodeGroup> nodeGroups = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    /*String query = "select id_group from concept where id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'";*/

                    String query = "select idgroup from concept_group_concept where idthesaurus = '" + idThesaurus + "'"
                            + " and idconcept = '" + idConcept + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeGroup nodeGroup = getThisConceptGroup(ds,
                                resultSet.getString("idgroup"),
                                idThesaurus, idLang);
                        if (nodeGroup != null) {
                            nodeGroups.add(nodeGroup);
                        }
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Id or Groups of Concept : " + idConcept, sqle);
        }
        return nodeGroups;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant Ark sinon renvoie un une
     * chaine vide
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return Objet class Concept
     */
    public String getIdArkOfGroup(HikariDataSource ds, String idGroup, String idThesaurus) {

        String ark = "";
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select id_ark from concept_group where idthesaurus = '" + idThesaurus
                        + "' and LOWER(idgroup) = '" + idGroup.toLowerCase() + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        ark = resultSet.getString("id_ark");
                    }
                }

            }
        } catch (SQLException sqle) {
            log.error("Error while getting idArk of Group : " + idGroup, sqle);
        }
        return ark;
    }
    /**
     * Cette fonction permet de récupérer la date de modification
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return Objet class Concept
     */
    private Date getModifiedDate(HikariDataSource ds, String idGroup, String idThesaurus) {

        Date date = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select modified from concept_group where idthesaurus = '" + idThesaurus
                        + "' and LOWER(idgroup) = '" + idGroup.toLowerCase() + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        date = resultSet.getDate("modified");
                    }
                }

            }
        } catch (SQLException sqle) {
            log.error("Error while getting modified date of Group : " + idGroup, sqle);
        }
        return date;
    }    
    /**
     * Cette fonction permet de récupérer la date de modification
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return Objet class Concept
     */
    private Date getCreatedDate(HikariDataSource ds, String idGroup, String idThesaurus) {

        Date date = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select created from concept_group where idthesaurus = '" + idThesaurus
                        + "' and LOWER(idgroup) = '" + idGroup.toLowerCase() + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        date = resultSet.getDate("created");
                    }
                }

            }
        } catch (SQLException sqle) {
            log.error("Error while getting created date of Group : " + idGroup, sqle);
        }
        return date;
    }     

    /**
     * Cette fonction permet de récupérer l'identifiant Ark sinon renvoie un une
     * chaine vide
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return Objet class Concept
     */
    public String getIdHandleOfGroup(HikariDataSource ds, String idGroup, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String ark = "";
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select id_handle from concept_group where"
                            + " idthesaurus = '" + idThesaurus + "'"
                            + " and LOWER(idgroup) = '" + idGroup.toLowerCase() + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        ark = resultSet.getString("id_handle");
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting id_handle of Group : " + idGroup, sqle);
        }
        return ark;
    }

    /**
     * Cette fonction permet de récupérer la notation d'un groupe
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return Objet class Concept
     */
    public String getNotationOfGroup(HikariDataSource ds, String idGroup, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String notation = "";
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select notation from concept_group where"
                            + " idthesaurus = '" + idThesaurus + "'"
                            + " and LOWER(idgroup) = '" + idGroup.toLowerCase() + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        notation = resultSet.getString("notation");
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting notation of Group : " + idGroup, sqle);
        }
        return notation;
    }

    /**
     * Cette fonction permet de récupérer la notation d'un groupe
     *
     * @param ds
     * @param notation
     * @param idGroup
     * @param idThesaurus
     * @return Objet class Concept
     */
    public boolean setNotationOfGroup(HikariDataSource ds, String notation, String idGroup, String idThesaurus) {

        Connection conn;
        Statement stmt;
        if (notation == null) {
            return false;
        }
        boolean status = false;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "update concept_group set notation = '" + notation + "'"
                            + " where idthesaurus = '" + idThesaurus + "'"
                            + " and LOWER(idgroup) = '" + idGroup.toLowerCase() + "'";
                    stmt.executeUpdate(query);
                    status = true;
                    updateModifiedDate(ds, idGroup, idThesaurus);  
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating notation of Group : " + idGroup, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant Ark sinon renvoie un une
     * chaine vide
     *
     * @param ds
     * @param idArk
     * @return Objet class Concept
     */
    public String getIdGroupFromArkId(HikariDataSource ds, String idArk, String idTheso) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String idGroup = "";
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group where"
                            + " id_ark = '" + idArk + "' and idthesaurus = '" + idTheso + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        idGroup = resultSet.getString("idgroup");
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idGroup from idArk of Group : " + idArk, sqle);
        }
        return idGroup;
    }
    /**
     * Cette fonction permet de récupérer l'identifiant Ark sinon renvoie un une
     * chaine vide
     *
     * @param ds
     * @param idHandle
     * @return Objet class Concept
     */
    public String getIdGroupFromHandleId(HikariDataSource ds, String idHandle) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String idGroup = "";
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group where"
                            + " id_handle = '" + idHandle + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        idGroup = resultSet.getString("idgroup");
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idGroup from idArk of Group : " + idHandle, sqle);
        }
        return idGroup;
    }    

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après
     * l'idArk
     *
     * @param ds
     * @param arkId
     * @return IdConcept
     */
    public String getIdThesaurusFromArkId(HikariDataSource ds, String arkId) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        String idThesaurus = null;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idthesaurus from concept_group where"
                           + " REPLACE(concept.id_ark, '-', '') = REPLACE('" + arkId + "', '-', '')";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("idthesaurus");
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting idThesaurus by idArk  : " + arkId, sqle);
        }
        return idThesaurus;
    }

    /**
     * Cette fonction permet de savoir si le Groupe a des concepts
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return
     */
    public boolean isGroupHaveConcepts(HikariDataSource ds, String idGroup, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean haveConcept = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT idconcept FROM concept_group_concept"
                            + " WHERE idthesaurus='" + idThesaurus + "'"
                            + " AND LOWER(idgroup)='" + idGroup.toLowerCase() + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    haveConcept = resultSet.next();
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while testing if Group have Concepts : " + idGroup, sqle);
        }
        return haveConcept;
    }

    /**
     * Cette fonction permet de savoir si le Domaine existe dans cette langue
     *
     * @param ds
     * @param title
     * @param idThesaurus
     * @param idLang
     * @return boolean
     */
    public boolean isDomainExist(HikariDataSource ds, String title, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;
        title = fr.cnrs.opentheso.utils.StringUtils.convertString(title);
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group_label where "
                            + "unaccent_string(lexicalvalue) ilike "
                            + "unaccent_string('" + title
                            + "')  and lang = '" + idLang
                            + "' and idthesaurus = '" + idThesaurus
                            + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if Title of Term exist : " + title, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Domaine existe dans cette langue
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @param idLang
     * @return boolean
     */
    public boolean isHaveTraduction(HikariDataSource ds, String idGroup, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean existe = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select idgroup from concept_group_label where "
                            + " LOWER(idgroup) ='" + idGroup.toLowerCase() + "'"
                            + " and lang = '" + idLang + "'"
                            + " and idthesaurus = '" + idThesaurus + "'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if traduction exist of Group : " + idGroup, sqle);
        }
        return existe;
    }

    /**
     * Fonction qui permet de retourner la liste des Type de Groupe
     *
     * @param ds
     * @return
     */
    public List<SelectItem> getAllGroupType(HikariDataSource ds) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        List<SelectItem> nodeGroupTypes = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Select code, label, skoslabel from concept_group_type order by label";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        SelectItem nodeGroupType = new SelectItem();
                        nodeGroupType.setValue(resultSet.getString("code"));
                        nodeGroupType.setLabel(resultSet.getString("label"));
                        nodeGroupTypes.add(nodeGroupType);
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting list of Group Type : " + sqle);
        }
        return nodeGroupTypes;
    }

    /**
     * Cette fonction permet de savoir si un group ou sous_group a des
     * sous_group ou non suivant l'id du Group ou sous_group et l'id du
     * thésaurus #MR.
     *
     * @param ds
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public boolean isHaveSubGroup(HikariDataSource ds, String idThesaurus, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet = null;
        boolean subGroup = false;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select count(*)  from relation_group"
                            + " where id_thesaurus ='" + idThesaurus + "'"
                            + " and LOWER(id_group1) ='" + idGroup.toLowerCase() + "'"
                            + " and relation ='sub'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) != 0) {
                            subGroup = true;
                        }
                    }

                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while testing if haveSubGroup of Group : " + idGroup, sqle);
        }
        return subGroup;
    }
    
    /**
     * Change l'id d'un group dans la table concept_group
     *
     * @param conn
     * @param idTheso
     * @param idGroup
     * @param newIdGroup
     * @throws java.sql.SQLException
     */
    public void setIdGroup(Connection conn, String idTheso, String idGroup, String newIdGroup) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE concept_group"
                    + " SET idgroup = '" + newIdGroup.toLowerCase() + "'"
                    + " WHERE LOWER(idgroup) = '" + idGroup.toLowerCase() + "'"
                    + " AND idthesaurus = '" + idTheso + "'";
            stmt.execute(query);

        } finally {
            stmt.close();
        }
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<DomaineDto> getAllGroupsByThesaurusAndLang(Connect connect, String idThesaurus, String lang) {
        ArrayList<DomaineDto> domaines = new ArrayList<>();
        try ( Statement stmt = connect.getPoolConnexion().getConnection().createStatement()) {
            stmt.executeQuery("SELECT idgroup, lexicalvalue FROM concept_group_label where idthesaurus = '"
                    + idThesaurus + "' AND lang = '" + lang.toLowerCase() + "'");
            try ( ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    DomaineDto domaineDto = new DomaineDto();
                    domaineDto.setId(resultSet.getString("idgroup"));
                    domaineDto.setName(resultSet.getString("lexicalvalue"));
                    domaines.add(domaineDto);
                }
            }
        } catch (SQLException e) {
            log.error(e.toString());
        }
        return domaines;
    }

}
