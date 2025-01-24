package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.models.languages.Languages_iso639;
import fr.cnrs.opentheso.models.thesaurus.Thesaurus;
import fr.cnrs.opentheso.models.nodes.NodeIdValue;
import fr.cnrs.opentheso.models.thesaurus.NodeLangTheso;
import fr.cnrs.opentheso.models.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.utils.StringUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Slf4j
@Data
@Service
public class ThesaurusHelper {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ToolsHelper toolsHelper;

    @Autowired
    private PreferencesHelper preferencesHelper;

    private String identifierType = "2";

    /**
     * Cette fonction permet de récupérer l'identifiant du thésaurus d'après
     * l'idArk
    */
    public String getIdThesaurusFromArkId(String arkId) {
        String idThesaurus = null;
      //  arkId = StringUtils.replaceOnce(arkId, "-", "");
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_thesaurus from thesaurus WHERE REPLACE(thesaurus.id_ark, '-', '') = REPLACE('" + arkId + "', '-', '')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("id_thesaurus");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idThesaurus by idArk : " + arkId, sqle);
        }
        return idThesaurus;
    }  
    
    /**
     * Retourne la liste de tous les thésaurus dans la langue source de chaque thésaurus
     * avec l'option de récupération de thésaurus privés ou pas.
     * @param withPrivateTheso
     * @return 
     */
    public ArrayList<NodeIdValue> getAllTheso(boolean withPrivateTheso) {

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        List<String> tabIdThesaurus = getAllIdOfThesaurus(withPrivateTheso);
        String idLang;
        for (String idTheso : tabIdThesaurus) {
            idLang = preferencesHelper.getWorkLanguageOfTheso(idTheso);
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso);
            nodeIdValue.setValue(getTitleOfThesaurus(idTheso, idLang));
            nodeIdValues.add(nodeIdValue);
        }
        return nodeIdValues;
    }

    /**
     * permet de savoir si le thésaurus est public ou privé
     * @param idTheso
     * @return 
     */
    public boolean isThesoPrivate(String idTheso) {
        boolean status = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select private from thesaurus where id_thesaurus = '" + idTheso + "'");
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
     *
     * @return String Id du thésaurus rajouté
     */
    public String addThesaurusRollBack() {

        String idThesaurus = null;
        String idArk = "";

        try (var conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            if (identifierType.equalsIgnoreCase("1")) { // identifiants types alphanumérique
                idThesaurus = toolsHelper.getNewId(10, false, false);
                while (isThesaurusExiste(conn, idThesaurus)) {
                    idThesaurus = toolsHelper.getNewId(10, false, false);
                }
            } else {
              //  stmt.executeQuery("select max(id) from thesaurus");
                stmt.execute("SELECT last_value FROM thesaurus_id_seq");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    int idNumeriqueThesaurus = resultSet.getInt(1);
                    idThesaurus = "th" + ++idNumeriqueThesaurus;
                    while (isThesaurusExiste(conn, idThesaurus)) {
                        idThesaurus = "th" + ++idNumeriqueThesaurus;
                    }
                }
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
     * @param thesaurus
     * @return 
     */
    public boolean addThesaurusTraductionRollBack(Thesaurus thesaurus) {

        thesaurus = addQuotes(thesaurus);
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into thesaurus_label (id_thesaurus, contributor, coverage,"
                    + " creator, created, modified, description, format,lang, publisher, relation,"
                    + " rights, source, subject, title, type) values ('" + thesaurus.getId_thesaurus() + "', '"
                    + thesaurus.getContributor() + "', '" + thesaurus.getCoverage() + "','" + thesaurus.getCreator()
                    + "',current_date, current_date, '" + thesaurus.getDescription() + "','" + thesaurus.getFormat()
                    + "','" + thesaurus.getLanguage().trim() + "','" + thesaurus.getPublisher() + "','"
                    + thesaurus.getRelation() + "','" + thesaurus.getRights() + "','" + thesaurus.getSource()
                    + "','" + thesaurus.getSubject() + "','" + thesaurus.getTitle() + "','" + thesaurus.getType() + "')");
            return true;
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Traduction Thesaurus : " + thesaurus.getTitle(), sqle);
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Cette focntion permet de nettoyer un thésaurus des espaces et des null
     * @param idTheso
     * @return 
     */
    public boolean cleaningTheso(String idTheso) {
        boolean status = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from term where id_term = ''"
                        + " and id_thesaurus = '" + idTheso + "'");
                stmt.executeUpdate("delete from concept_group_label where idgroup = ''"
                        + " and idthesaurus = '" + idTheso + "'");
                stmt.executeUpdate("UPDATE concept_group SET notation = '' WHERE notation ilike 'null'");
                stmt.executeUpdate("UPDATE concept_group SET idtypecode = 'MT' WHERE idtypecode ilike 'null'");
                stmt.executeUpdate("UPDATE concept SET notation = '' WHERE notation ilike 'null'");   
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while reorganizing theso : " + idTheso, sqle);
        }        
        return status;
    }

    /**
     * Permet de rajouter une traduction à un Thésaurus existant suivant un l'id
     * du thésaurus et la langue retourne yes or No si l'opération a réussie ou
     * non
     * @param thesaurus
     * @return 
     */
    public boolean addThesaurusTraduction(Thesaurus thesaurus) {

        thesaurus = addQuotes(thesaurus);

        try ( Connection conn = dataSource.getConnection()) {
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
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding Traduction Thesaurus : " + thesaurus.getTitle(), sqle);
            return false;
        }
    }

    /**
     * Permet de retourner un thésaurus par identifiant et par langue / ou null
     * si rien cette fonction ne retourne pas les détails et les traductions
     * @param idThesaurus
     * @param idLang
     * @return 
     */
    public Thesaurus getThisThesaurus(String idThesaurus, String idLang) {
        idLang = idLang.trim();
        Thesaurus thesaurus = null;
        try ( Connection conn = dataSource.getConnection()) {
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
                            thesaurus.setId_ark(resultSet.getString("id_ark"));
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
     * @param idThesaurus
     * @param idLang
     * @return 
     */
    public String getTitleOfThesaurus(String idThesaurus, String idLang) {
        String title = "";
        try ( Connection conn = dataSource.getConnection()) {
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
     * @param idThesaurus
     * @return 
     */
    public NodeThesaurus getNodeThesaurus(String idThesaurus) {

        ArrayList<Languages_iso639> listLangTheso = getLanguagesOfThesaurus(idThesaurus);
        NodeThesaurus nodeThesaurus = new NodeThesaurus();
        ArrayList<Thesaurus> thesaurusTraductionsList = new ArrayList<>();

        if(!isThesaurusExiste(idThesaurus)){
            return null;
        }
        for (int i = 0; i < listLangTheso.size(); i++) {
            Thesaurus thesaurus = getThisThesaurus(idThesaurus, listLangTheso.get(i).getId_iso639_1());
            if (thesaurus != null) {
                thesaurusTraductionsList.add(thesaurus);
            }
        }
        nodeThesaurus.setIdThesaurus(idThesaurus);
        nodeThesaurus.setIdArk(getIdArkOfThesaurus(idThesaurus));
        nodeThesaurus.setListThesaurusTraduction(thesaurusTraductionsList);
        return nodeThesaurus;
    }

    /**
     * Retourne la liste des Ids des thésaurus existants
     * @param withPrivateTheso
     * @return 
     */
    public List<String> getAllIdOfThesaurus(boolean withPrivateTheso) {

        List<String> tabIdThesaurus = new ArrayList();
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                String query;
                if (withPrivateTheso) {
                    query = "select id_thesaurus from thesaurus order by id desc";
                } else { // uniquement pour les SuperAdmin
                    query = "select id_thesaurus from thesaurus where thesaurus.private != true order by id desc";
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
     * Cette fonction permet de récupérer l'identifiant Ark sinon renvoie une chaine vide
     * @param idThesaurus
     * @return 
     */
    public String getIdArkOfThesaurus(String idThesaurus) {
        String ark = "";
        try ( Connection conn = dataSource.getConnection()) {
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
     * Cette fonction permet de récupérer l'identifiant Ark sinon renvoie une chaine vide
     * @param idThesaurus
     * @return
     */
    public boolean updateIdArkOfThesaurus(String idThesaurus, String idArk) {
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("update thesaurus set id_ark = '" + idArk + "' where id_thesaurus = '" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idArk of Thesaurus : " + idThesaurus, sqle);
        }
        return false;
    }

    /**
     * Retourne la liste des traductions d'un thesaurus sous forme de ArrayList
     * avec le code iso de la langue
     * @param idThesaurus
     * @return 
     */
    public List<String> getIsoLanguagesOfThesaurus(String idThesaurus) {

        ArrayList<String> idLang = new ArrayList<>();
        try ( Connection conn = dataSource.getConnection()) {
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
     * Retourne la liste des traductions d'un thesaurus sous forme de ArrayList d'Objet Languages_iso639
     * @param idThesaurus
     * @return 
     */
    public ArrayList<Languages_iso639> getLanguagesOfThesaurus(String idThesaurus) {

        ArrayList<Languages_iso639> lang = null;
        try ( Connection conn = dataSource.getConnection()) {
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
    * @param idThesaurus
     * @param idLang
    * @return 
    */
    public ArrayList<NodeLangTheso> getAllUsedLanguagesOfThesaurusNode(String idThesaurus, String idLang) {

        if(idLang == null || idLang.isEmpty())
            idLang = "fr";
        ArrayList<NodeLangTheso> nodeLangs = new ArrayList<>();

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                    stmt.executeQuery("SELECT code_pays, iso639_1, french_name, english_name, title "
                            + " FROM thesaurus_label, languages_iso639 "
                            + " WHERE thesaurus_label.lang = languages_iso639.iso639_1 "
                            + " AND thesaurus_label.id_thesaurus = '" + idThesaurus + "'"
                            + " order by languages_iso639.french_name");

                try ( ResultSet resultSet = stmt.getResultSet()) {
                    int i = 0;
                    while (resultSet.next()) {
                        NodeLangTheso nodeLang = new NodeLangTheso();
                        nodeLang.setId("" + i);
                        nodeLang.setCodeFlag(resultSet.getString("code_pays"));
                        nodeLang.setCode(resultSet.getString("iso639_1"));
                        if(idLang.equalsIgnoreCase("fr"))
                            nodeLang.setValue(resultSet.getString("french_name"));
                        else
                            nodeLang.setValue(resultSet.getString("english_name"));
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
     *
     * @param idThesaurus
     * @return 
     */
    public ArrayList<String> getAllUsedLanguagesOfThesaurus(String idThesaurus) {

        ArrayList<String> tabIdLang = new ArrayList<>();
        try ( Connection conn = dataSource.getConnection()) {
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
     * @param idThesaurus
     * @param idLang
     * @return 
     */
    public boolean isLanguageExistOfThesaurus(String idThesaurus, String idLang) {

        boolean existe = false;
        try ( Connection conn = dataSource.getConnection()) {
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
     * @param idThesaurus
     * @return 
     */
    public boolean isThesaurusExiste(String idThesaurus) {
        boolean existe = false;
        try ( Connection conn = dataSource.getConnection()) {
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
     * @param conn
     * @param idThesaurus
     * @return 
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

        thesaurus.setContributor(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getContributor()));
        thesaurus.setCoverage(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getCoverage()));
        thesaurus.setCreator(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getCreator()));
        thesaurus.setDescription(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getDescription()));
        thesaurus.setFormat(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getFormat()));
        thesaurus.setPublisher(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getPublisher()));
        thesaurus.setRelation(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getRelation()));
        thesaurus.setRights(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getRights()));
        thesaurus.setSource(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getSource()));
        thesaurus.setSubject(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getSubject()));
        thesaurus.setTitle(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getTitle()));
        thesaurus.setType(fr.cnrs.opentheso.utils.StringUtils.convertString(thesaurus.getType()));

        return thesaurus;
    }

    /**
     * Permet de mettre à jour un thésaurus suivant un identifiant et une langue donnés
     * @param thesaurus
     * @return 
     */
    public boolean UpdateThesaurus(Thesaurus thesaurus) {

        boolean status = false;

        thesaurus = addQuotes(thesaurus);

        /**
         * On met à jour tous les chmamps saufs l'idThesaurus, la date de
         * creation en utilisant et la langue
         */
        try ( Connection conn = dataSource.getConnection()) {
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
     * @param idThesaurus
     * @return 
     */
    public boolean deleteThesaurus(String idThesaurus) {

        idThesaurus = StringUtils.convertString(idThesaurus);
        boolean state = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from thesaurus where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from thesaurus_label where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from thesaurus_array where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from node_label where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from concept where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from concept_historique where id_thesaurus = '" + idThesaurus + "';"
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
                        + "delete from concept_facet where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from external_resources where id_thesaurus = '" + idThesaurus + "';"
                        + "delete from external_images where id_thesaurus = '" + idThesaurus + "';"          
                        + "delete from corpus_link where id_theso = '" + idThesaurus + "';"   
                        
                        // métadonnées Concepts + thésaurus 
                        + "delete from concept_dcterms where id_thesaurus = '" + idThesaurus + "';" 
                        + "delete from thesaurus_dcterms where id_thesaurus = '" + idThesaurus + "';"   
        
                        + "delete from preferences where id_thesaurus = '" + idThesaurus + "';");
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
     * @param idThesaurus
     * @param id_lang
     * @return 
     */
    public boolean deleteThesaurusTraduction(String idThesaurus, String id_lang) {

        idThesaurus = StringUtils.convertString(idThesaurus);
        boolean state = false;

        try ( Connection conn = dataSource.getConnection()) {
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
