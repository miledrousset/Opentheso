/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.datas.Languages_iso639;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class ThesaurusHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    private String identifierType = "2";

    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }

    /**
     * Retourne la liste de tous les thésaurus dans la langue sélectionnée
     */
    public ArrayList<NodeIdValue> getAllTheso(HikariDataSource ds, String idLang) {

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        boolean withPrivateTheso = true;
        List<String> tabIdThesaurus = getAllIdOfThesaurus(ds, withPrivateTheso);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                for (String idTheso : tabIdThesaurus) {
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(idTheso);
                    nodeIdValue.setValue("");
                    stmt.executeQuery("select title from thesaurus_label where id_thesaurus = '"
                            + idTheso + "'" + " and lang = '" + idLang + "'");
                    try ( ResultSet resultSet = stmt.getResultSet()) {
                        if (resultSet.next()) {
                            nodeIdValue.setValue(resultSet.getString("title"));
                        }
                        nodeIdValues.add(nodeIdValue);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting all thesaurus : ", sqle);
        }
        return nodeIdValues;
    }

    /**
     * permet de savoir si le thésaurus est public ou privé
     */
    public boolean isThesoPrivate(HikariDataSource ds, String idTheso) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select private from thesaurus where id_thesaurus = '"
                        + idTheso + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        status = resultSet.getBoolean("private");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if theso is private : " + idTheso, sqle);
        }
        return status;
    }

    /**
     * Permet de créer un nouveau Thésaurus. Retourne l'identifiant du thésaurus
     * ou null
     */
    public String addThesaurus(HikariDataSource ds, Thesaurus thesaurus,
            String urlSite, boolean isArkActive) {

        String idThesaurus = null;//"TH";//"ark:/66666/srvq9a5Ll41sk";

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select max(id) from thesaurus");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    int idNumeriqueThesaurus = resultSet.getInt(1);
                    idThesaurus = "" + ++idNumeriqueThesaurus;

                    //récupération du code Ark via WebServices
                    String idArk = "";
                    if (isArkActive) {
                        /*    ArrayList<DcElement> dcElementsList = new ArrayList<>();
                        ArkClient ark_Client = new ArkClient();
                        idArk = ark_Client.getArkId(
                                new FileUtilities().getDate(),
                                urlSite + "?idt=" + idThesaurus,
                                "", "", dcElementsList, "pcrt"); // pcrt : p= pactols, crt=code DCMI pour collection*/
                    }
                    stmt.executeUpdate("Insert into thesaurus (id_thesaurus, id_ark, created, modified)"
                            + " values ('" + idThesaurus + "','" + idArk + "'"
                            + "," + "current_date, current_date)");
                    thesaurus.setId_thesaurus(idThesaurus);
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Thesaurus : " + idThesaurus, sqle);
            idThesaurus = null;
        }
        return idThesaurus;
    }

    /**
     * Permet de créer un nouveau Thésaurus. Retourne l'identifiant du thésaurus
     * ou null
     *
     * @param conn
     * @param urlSite
     * @param isArkActive
     * @return String Id du thésaurus rajouté
     */
    public String addThesaurusRollBack(Connection conn, String urlSite, boolean isArkActive) {

        String idThesaurus = null;
        String idArk = "";

        try ( Statement stmt = conn.createStatement()) {
            if (identifierType.equalsIgnoreCase("1")) { // identifiants types alphanumérique
                ToolsHelper toolsHelper = new ToolsHelper();
                idThesaurus = toolsHelper.getNewId(10);
                while (isThesaurusExiste(conn, idThesaurus)) {
                    idThesaurus = toolsHelper.getNewId(10);
                }
            } else {
                stmt.executeQuery("select max(id) from thesaurus");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    int idNumeriqueThesaurus = resultSet.getInt(1);
                    idThesaurus = "th" + ++idNumeriqueThesaurus;
                    while (isThesaurusExiste(conn, idThesaurus)) {
                        idThesaurus = "th" + ++idNumeriqueThesaurus;
                    }
                }
            }
            /**
             * récupération du code Ark via WebServices
             *
             */
            if (isArkActive) {
                /*    ArrayList<DcElement> dcElementsList = new ArrayList<>();
                        ArkClient ark_Client = new ArkClient();
                        idArk = ark_Client.getArkId(
                                new FileUtilities().getDate(),
                                urlSite + "?idt=" + idThesaurus,
                                "", "", dcElementsList, "pcrt"); // pcrt : p= pactols, crt=code DCMI pour collection
                 */
            }
            stmt.executeUpdate("Insert into thesaurus (id_thesaurus, id_ark, created, modified)"
                    + " values ('" + idThesaurus + "', '" + idArk + "'," + "current_date, current_date)");
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while adding Thesaurus : " + idThesaurus, sqle);
            idThesaurus = null;
        }
        return idThesaurus;
    }

    /**
     * Permet de rajouter une traduction à un Thésaurus existant suivant un l'id
     * du thésaurus et la langue retourne yes or No si l'opération a réussie ou
     * non
     */
    public boolean addThesaurusTraductionRollBack(Connection conn, Thesaurus thesaurus) {

        boolean status = false;
        thesaurus = addQuotes(thesaurus);
        try ( Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into thesaurus_label ("
                    + " id_thesaurus,"
                    + " contributor, coverage,"
                    + " creator, created, modified, description,"
                    + " format,lang, publisher, relation,"
                    + " rights, source, subject, title,"
                    + " type)"
                    + " values ("
                    + "'" + thesaurus.getId_thesaurus() + "'"
                    + ",'" + thesaurus.getContributor() + "'"
                    + ",'" + thesaurus.getCoverage() + "'"
                    + ",'" + thesaurus.getCreator() + "'"
                    + ",current_date"
                    + ",current_date"
                    + ",'" + thesaurus.getDescription() + "'"
                    + ",'" + thesaurus.getFormat() + "'"
                    + ",'" + thesaurus.getLanguage().trim() + "'"
                    + ",'" + thesaurus.getPublisher() + "'"
                    + ",'" + thesaurus.getRelation() + "'"
                    + ",'" + thesaurus.getRights() + "'"
                    + ",'" + thesaurus.getSource() + "'"
                    + ",'" + thesaurus.getSubject() + "'"
                    + ",'" + thesaurus.getTitle() + "'"
                    + ",'" + thesaurus.getType() + "')");
            status = true;
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Traduction Thesaurus : " + thesaurus.getTitle(), sqle);
                return false;
            } else {
                status = true;
            }
        }
        return status;
    }

    /**
     * Cette focntion permet de nettoyer un thésaurus
     */
    public boolean cleaningTheso(Connection conn, String idTheso) {

        boolean status = false;

        try ( Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from term where id_term = ''"
                    + " and id_thesaurus = '" + idTheso + "'");

            stmt.executeUpdate("delete from concept_group_label where idgroup = ''"
                    + " and idthesaurus = '" + idTheso + "'");

            stmt.executeUpdate("UPDATE concept_group SET notation = '' WHERE notation ilike 'null'");

            stmt.executeUpdate("UPDATE concept_group SET idtypecode = 'MT' WHERE idtypecode ilike 'null'");

            stmt.executeUpdate("UPDATE concept SET notation = '' WHERE notation ilike 'null'");

            status = true;
        } catch (SQLException sqle) {
            log.error("Error while reorganizing theso : " + idTheso, sqle);
        }
        return status;
    }

    /**
     * Permet de rajouter une traduction à un Thésaurus existant suivant un l'id
     * du thésaurus et la langue retourne yes or No si l'opération a réussie ou
     * non
     */
    public boolean addThesaurusTraduction(HikariDataSource ds, Thesaurus thesaurus) {

        boolean status = false;
        thesaurus = addQuotes(thesaurus);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into thesaurus_label (id_thesaurus,"
                        + " contributor, coverage,"
                        + " creator, created, modified, description,"
                        + " format,lang, publisher, relation,"
                        + " rights, source, subject, title,"
                        + " type)"
                        + " values ("
                        + "'" + thesaurus.getId_thesaurus() + "'"
                        + ",'" + thesaurus.getContributor() + "'"
                        + ",'" + thesaurus.getCoverage() + "'"
                        + ",'" + thesaurus.getCreator() + "'"
                        + ",current_date"
                        + ",current_date"
                        + ",'" + thesaurus.getDescription() + "'"
                        + ",'" + thesaurus.getFormat() + "'"
                        + ",'" + thesaurus.getLanguage().trim() + "'"
                        + ",'" + thesaurus.getPublisher() + "'"
                        + ",'" + thesaurus.getRelation() + "'"
                        + ",'" + thesaurus.getRights() + "'"
                        + ",'" + thesaurus.getSource() + "'"
                        + ",'" + thesaurus.getSubject() + "'"
                        + ",'" + thesaurus.getTitle() + "'"
                        + ",'" + thesaurus.getType() + "')");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Traduction Thesaurus : " + thesaurus.getTitle(), sqle);
        }
        return status;
    }

    /**
     * Permet de retourner un thésaurus par identifiant et par langue / ou null
     * si rien cette fonction ne retourne pas les détails et les traductions
     */
    public Thesaurus getThisThesaurus(HikariDataSource ds, String idThesaurus, String idLang) {
        idLang = idLang.trim();
        Thesaurus thesaurus = null;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {

                stmt.executeQuery("select * from thesaurus, thesaurus_label where"
                        + " thesaurus.id_thesaurus = thesaurus_label.id_thesaurus"
                        + " and thesaurus_label.id_thesaurus = '" + idThesaurus + "'"
                        + " and thesaurus_label.lang = '" + idLang + "'");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if (resultSet.getString("lang") != null) {
                            thesaurus = new Thesaurus();
                            thesaurus.setId_thesaurus(idThesaurus);
                            thesaurus.setContributor(resultSet.getString("contributor"));
                            thesaurus.setCoverage(resultSet.getString("coverage"));
                            thesaurus.setCreator(resultSet.getString("creator"));
                            thesaurus.setCreated(resultSet.getDate("created"));
                            thesaurus.setModified(resultSet.getDate("modified"));
                            thesaurus.setDescription(resultSet.getString("description"));
                            thesaurus.setFormat(resultSet.getString("format"));
                            thesaurus.setLanguage(resultSet.getString("lang"));
                            thesaurus.setPublisher(resultSet.getString("publisher"));
                            thesaurus.setRelation(resultSet.getString("relation"));
                            thesaurus.setRights(resultSet.getString("rights"));
                            thesaurus.setSource(resultSet.getString("source"));
                            thesaurus.setSubject(resultSet.getString("subject"));
                            thesaurus.setTitle(resultSet.getString("title"));
                            thesaurus.setType(resultSet.getString("type"));
                            thesaurus.setPrivateTheso(resultSet.getBoolean("private"));
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting This Thesaurus : " + idThesaurus, sqle);
        }
        return thesaurus;
    }

    /**
     * Permet de retourner le titre du thésaurus par identifiant et par langue
     */
    public String getTitleOfThesaurus(HikariDataSource ds, String idThesaurus, String idLang) {
        String title = null;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select title from thesaurus_label where id_thesaurus = '"
                        + idThesaurus + "' and lang = '" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        title = resultSet.getString("title");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting title of Thesaurus : " + idThesaurus, sqle);
        }
        return title;
    }

    /**
     * Permet de retourner un thésaurus par identifiant sous forme de
     * NodeThesaurus avec les traductions
     */
    public NodeThesaurus getNodeThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<Languages_iso639> listLangTheso = getLanguagesOfThesaurus(ds, idThesaurus);
        NodeThesaurus nodeThesaurus = new NodeThesaurus();
        ArrayList<Thesaurus> thesaurusTraductionsList = new ArrayList<>();

        for (int i = 0; i < listLangTheso.size(); i++) {
            Thesaurus thesaurus = getThisThesaurus(ds, idThesaurus, listLangTheso.get(i).getId_iso639_1());
            if (thesaurus != null) {
                thesaurusTraductionsList.add(thesaurus);
            }
        }
        nodeThesaurus.setIdThesaurus(idThesaurus);
        nodeThesaurus.setListThesaurusTraduction(thesaurusTraductionsList);
        return nodeThesaurus;
    }

    /**
     * Retourne la liste des langues sous forme de MAP (nom + id) si le
     * thesaurus n'existe pas dans la langue demandée, on récupère seulement son
     * id
     */
    public Map getListThesaurus(HikariDataSource ds, String idLang) {

        ResultSet resultSet = null;
        Map map = new HashMap();
        ArrayList tabIdThesaurus = new ArrayList();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select DISTINCT id_thesaurus from thesaurus");
                resultSet = stmt.getResultSet();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        tabIdThesaurus.add(resultSet.getString("id_thesaurus"));
                    }
                    for (Object tabIdThesauru : tabIdThesaurus) {
                        stmt.executeQuery("select title from thesaurus_label where"
                                + " id_thesaurus = '" + tabIdThesauru + "'" + " and lang = '" + idLang + "'");
                        resultSet = stmt.getResultSet();
                        if (resultSet != null) {
                            resultSet.next();
                            if (resultSet.getRow() == 0) {
                                map.put("(" + tabIdThesauru + ")", tabIdThesauru);
                            } else {
                                map.put(resultSet.getString("title") + "(" + tabIdThesauru + ")", tabIdThesauru);
                            }

                        }
                    }
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        } catch (SQLException sqle) {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (Exception ex) {
            }
            log.error("Error while getting Map of thesaurus : " + map.toString(), sqle);
        }
        return map;
    }

    /**
     * Retourne la liste des Ids des thésaurus existants
     */
    public List<String> getAllIdOfThesaurus(HikariDataSource ds, boolean withPrivateTheso) {

        List<String> tabIdThesaurus = new ArrayList();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "";
                if (withPrivateTheso) {
                    query = "select id_thesaurus from thesaurus order by id_thesaurus";
                } else { // uniquement pour les SuperAdmin
                    query = "select id_thesaurus from thesaurus where thesaurus.private != true order by id_thesaurus";
                }
                try ( ResultSet resultSet = stmt.executeQuery(query)) {
                    while (resultSet.next()) {
                        tabIdThesaurus.add(resultSet.getString("id_thesaurus"));
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All ids of thesaurus : ", sqle);
        }
        return tabIdThesaurus;
    }

    /**
     * retourne la liste des thésaurus d'un utilisateur
     */
    public Map getListThesaurusOfUser(HikariDataSource ds, int idUser, String idLang) {
        Map map = new HashMap();
        ArrayList tabIdThesaurus = new ArrayList();
        ResultSet resultSet = null;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT DISTINCT user_role.id_thesaurus FROM user_role, thesaurus WHERE "
                        + "thesaurus.id_thesaurus = user_role.id_thesaurus and id_user = " + idUser);
                resultSet = stmt.getResultSet();
                if (resultSet != null) {
                    while (resultSet.next()) {
                        if (!resultSet.getString("id_thesaurus").isEmpty()) {
                            tabIdThesaurus.add(resultSet.getString("id_thesaurus"));
                        }
                    }
                    for (Object tabIdThesauru : tabIdThesaurus) {
                        stmt.executeQuery("select title from thesaurus_label where"
                                + " id_thesaurus = '" + tabIdThesauru + "'" + " and lang = '" + idLang + "'");
                        resultSet = stmt.getResultSet();
                        if (resultSet != null) {
                            resultSet.next();
                            if (resultSet.getRow() == 0) {
                                map.put("(" + tabIdThesauru + ")", tabIdThesauru);
                            } else {
                                map.put(resultSet.getString("title") + "(" + tabIdThesauru + ")", tabIdThesauru);
                            }
                        }
                    }
                }
                if (resultSet != null) {
                    resultSet.close();
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Map of thesaurus : " + map.toString(), sqle);
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (Exception ex) {
            }
        }
        return map;
    }

    public Map getListThesaurusOfAllTheso(HikariDataSource ds, String idLang) {

        Map map = new HashMap();
        boolean withPrivateTheso = true;
        List tabIdThesaurus = getAllIdOfThesaurus(ds, withPrivateTheso);

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                for (Object tabIdThesauru : tabIdThesaurus) {
                    stmt.executeQuery("select title from thesaurus_label where"
                            + " id_thesaurus = '" + tabIdThesauru + "'" + " and lang = '" + idLang + "'");
                    try ( ResultSet resultSet = stmt.getResultSet()) {
                        if (resultSet != null) {
                            resultSet.next();
                            if (resultSet.getRow() == 0) {
                                map.put("(" + tabIdThesauru + ")", tabIdThesauru);
                            } else {
                                map.put(resultSet.getString("title") + "(" + tabIdThesauru + ")", tabIdThesauru);
                            }
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Map of thesaurus : " + map.toString(), sqle);
        }
        return map;
    }

    /**
     * Retourne la liste des traductions d'un thesaurus sous forme de MAP (lang
     * + title)
     */
    public Map getMapTraduction(HikariDataSource ds, String idThesaurus) {

        Map map = new HashMap();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lang, title from thesaurus_label where id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        map.put(
                                resultSet.getString("lang"),
                                resultSet.getString("title")
                        );
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Map of thesaurus : " + map.toString(), sqle);
        }
        return map;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant Ark sinon renvoie un une
     * chaine vide
     */
    public String getIdArkOfThesaurus(HikariDataSource ds, String idThesaurus) {
        String ark = "";
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_ark from thesaurus where id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        ark = resultSet.getString("id_ark");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idArk of Thesaurus : " + idThesaurus, sqle);
        }
        return ark;
    }

    /**
     * Retourne la liste des traductions d'un thesaurus sous forme de ArrayList
     * avec le code iso de la langue
     */
    public ArrayList<String> getIsoLanguagesOfThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> idLang = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT languages_iso639.iso639_1 FROM thesaurus_label,"
                        + " languages_iso639 WHERE thesaurus_label.lang = languages_iso639.iso639_1 AND"
                        + " thesaurus_label.id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        idLang.add(resultSet.getString("iso639_1").trim());
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Language of thesaurus : " + idThesaurus, sqle);
        }
        return idLang;
    }

    /**
     * Retourne la liste des traductions d'un thesaurus sous forme de ArrayList
     * d'Objet Languages_iso639
     */
    public ArrayList<Languages_iso639> getLanguagesOfThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<Languages_iso639> lang = null;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT DISTINCT languages_iso639.iso639_1, "
                        + " languages_iso639.iso639_2, "
                        + " languages_iso639.english_name, "
                        + " languages_iso639.french_name, "
                        + " thesaurus_label.lang"
                        + " FROM "
                        + " thesaurus_label,"
                        + " languages_iso639"
                        + " WHERE"
                        + " thesaurus_label.lang = languages_iso639.iso639_1 AND"
                        + " thesaurus_label.lang = languages_iso639.iso639_1;");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        lang = new ArrayList<>();
                        while (resultSet.next()) {
                            Languages_iso639 languages_iso639 = new Languages_iso639();
                            languages_iso639.setId_iso639_1(resultSet.getString("iso639_1"));
                            languages_iso639.setId_iso639_2(resultSet.getString("iso639_2"));
                            languages_iso639.setFrench_name(resultSet.getString("french_name"));
                            languages_iso639.setFrench_name(resultSet.getString("english_name"));

                            lang.add(languages_iso639);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Language of thesaurus : " + idThesaurus, sqle);
        }
        return lang;
    }

    /**
     * Cette fonction permet de retourner toutes les langues utilisées par les
     * Concepts d'un thésaurus (sous forme de NodeLang, un objet complet)
     */
    public ArrayList<NodeLangTheso> getAllUsedLanguagesOfThesaurusNode(HikariDataSource ds,
            String idThesaurus) {

        ArrayList<NodeLangTheso> nodeLangs = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT thesaurus_label.lang, languages_iso639.french_name,"
                        + " thesaurus_label.title"
                        + " FROM thesaurus_label, languages_iso639"
                        + " WHERE thesaurus_label.lang = languages_iso639.iso639_1"
                        + " and thesaurus_label.id_thesaurus = '" + idThesaurus + "'"
                        + " order by languages_iso639.french_name");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    int i = 0;
                    while (resultSet.next()) {
                        NodeLangTheso nodeLang = new NodeLangTheso();
                        nodeLang.setId("" + i);
                        nodeLang.setCode(resultSet.getString("lang"));
                        nodeLang.setValue(resultSet.getString("french_name"));
                        nodeLang.setLabelTheso(resultSet.getString("title"));
                        nodeLangs.add(nodeLang);
                        i++;
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Used languages of Concepts of thesaurus  : " + idThesaurus, sqle);
        }
        return nodeLangs;
    }

    /**
     * Cette fonction permet de retourner toutes les langues utilisées par les
     * Concepts d'un thésaurus !!! seulement les code iso des langues sert
     * essentiellement à l'import
     */
    public ArrayList<String> getAllUsedLanguagesOfThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> tabIdLang = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select distinct lang from term where id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdLang.add(resultSet.getString("lang"));
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting All Used languages of Concepts of thesaurus  : " + idThesaurus, sqle);
        }
        return tabIdLang;
    }

    /**
     * Cette fonction permet de savoir si le terme existe ou non
     */
    public boolean isLanguageExistOfThesaurus(HikariDataSource ds, String idThesaurus, String idLang) {

        boolean existe = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_thesaurus from thesaurus_label where id_thesaurus ='"
                        + idThesaurus + "' and lang = '" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    if (resultSet.getRow() == 0) {
                        existe = false;
                    } else {
                        existe = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if Language exist of Thesaurus : " + idThesaurus, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le thesaurus existe ou non
     */
    public boolean isThesaurusExiste(HikariDataSource ds, String idThesaurus) {
        boolean existe = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_thesaurus from thesaurus where id_thesaurus = '" + idThesaurus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if thesaurus exist : " + idThesaurus, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le thesaurus existe ou non
     */
    public boolean isThesaurusExiste(Connection conn, String idThesaurus) {
        boolean existe = false;

        try ( Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_thesaurus from thesaurus where id_thesaurus = '"
                    + idThesaurus + "'");
            try ( ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()) {
                    existe = resultSet.getRow() != 0;
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while asking if thesaurus exist : " + idThesaurus, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet d'ajouter des cotes pour passer des données en JDBC
     *
     * @return
     */
    private Thesaurus addQuotes(Thesaurus thesaurus) {

        thesaurus.setContributor(new StringPlus().convertString(thesaurus.getContributor()));
        thesaurus.setCoverage(new StringPlus().convertString(thesaurus.getCoverage()));
        thesaurus.setCreator(new StringPlus().convertString(thesaurus.getCreator()));
        thesaurus.setDescription(new StringPlus().convertString(thesaurus.getDescription()));
        thesaurus.setFormat(new StringPlus().convertString(thesaurus.getFormat()));
        thesaurus.setPublisher(new StringPlus().convertString(thesaurus.getPublisher()));
        thesaurus.setRelation(new StringPlus().convertString(thesaurus.getRelation()));
        thesaurus.setRights(new StringPlus().convertString(thesaurus.getRights()));
        thesaurus.setSource(new StringPlus().convertString(thesaurus.getSource()));
        thesaurus.setSubject(new StringPlus().convertString(thesaurus.getSubject()));
        thesaurus.setTitle(new StringPlus().convertString(thesaurus.getTitle()));
        thesaurus.setType(new StringPlus().convertString(thesaurus.getType()));

        return thesaurus;
    }

    /**
     * Permet de mettre à jour un thésaurus suivant un identifiant et une langue
     * donnés
     */
    public boolean UpdateThesaurus(HikariDataSource ds, Thesaurus thesaurus) {

        boolean status = false;

        thesaurus = addQuotes(thesaurus);

        /**
         * On met à jour tous les chmamps saufs l'idThesaurus, la date de
         * creation en utilisant et la langue
         */
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE thesaurus_label "
                        + "set contributor='" + thesaurus.getContributor() + "',"
                        + " coverage='" + thesaurus.getCoverage() + "',"
                        + " creator='" + thesaurus.getCreator() + "',"
                        + " modified = current_date,"
                        + " description='" + thesaurus.getDescription() + "',"
                        + " format='" + thesaurus.getFormat() + "',"
                        + " publisher='" + thesaurus.getPublisher() + "',"
                        + " relation='" + thesaurus.getRelation() + "',"
                        + " rights='" + thesaurus.getRights() + "',"
                        + " source='" + thesaurus.getSource() + "',"
                        + " subject='" + thesaurus.getSubject() + "',"
                        + " title='" + thesaurus.getTitle() + "',"
                        + " type='" + thesaurus.getType() + "'"
                        + " WHERE lang='" + thesaurus.getLanguage() + "'"
                        + " AND id_thesaurus='" + thesaurus.getId_thesaurus() + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating thesausurs : " + thesaurus.getTitle() + " lang = " + thesaurus.getLanguage(), sqle);
        }
        return status;

    }

    /**
     * Permet de supprimer un thésaurus
     */
    public boolean deleteThesaurus(HikariDataSource ds, String idThesaurus) {
        StringPlus text = new StringPlus();
        idThesaurus = text.convertString(idThesaurus);
        boolean state = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query = "delete from thesaurus where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from thesaurus_label where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from thesaurus_array where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from node_label where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from concept where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from concept_historique where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from images where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from preferred_term where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from non_preferred_term where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from non_preferred_term_historique where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from term where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from term_historique where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from concept_group where idthesaurus = '" + idThesaurus + "';"
                        + "delete from concept_group_historique where idthesaurus = '" + idThesaurus + "';"
                        + "delete from concept_group_label where idthesaurus = '" + idThesaurus + "';"
                        + "delete from concept_group_label_historique where idthesaurus = '" + idThesaurus + "';"
                        + "delete from note where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from note_historique where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from permuted where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from hierarchical_relationship where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from hierarchical_relationship_historique where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from concept_candidat where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from concept_term_candidat where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from term_candidat where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from alignement where internal_id_thesaurus = '" + idThesaurus + "';"
                        + "delete from proposition where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from concept_replacedby where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from gps where id_theso = '" + idThesaurus + "';"
                        + "delete from thesaurus_alignement_source where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from concept_group_concept where idthesaurus = '" + idThesaurus + "';"
                        + "delete from relation_group where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from preferences where id_thesaurus = '" + idThesaurus + "';";
                stmt.executeUpdate(query);
                state = true;

            }
        } catch (SQLException ex) {
            Logger.getLogger(ThesaurusHelper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return state;
    }

    /**
     * Permet de supprimer une traduction d'un thésaurus
     */
    public boolean deleteThesaurusTraduction(HikariDataSource ds, String idThesaurus, String id_lang) {
        StringPlus text = new StringPlus();
        idThesaurus = text.convertString(idThesaurus);
        boolean state = false;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from thesaurus_label where id_thesaurus = '"
                        + idThesaurus + "' and lang = '" + id_lang + "'");
                state = true;

            }
        } catch (SQLException ex) {
            Logger.getLogger(ThesaurusHelper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return state;
    }

}
