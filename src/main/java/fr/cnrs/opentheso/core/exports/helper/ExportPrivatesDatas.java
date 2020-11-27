/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.exports.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Concept_Candidat;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Concept_Group_Historique;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Concept_Group_Label_Historique;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Concept_Historique;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Concept_Term_Candidat;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Hierarchical_Relationship_Historique;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Images;
import fr.cnrs.opentheso.core.exports.privatesdatas.LineOfData;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Non_Preferred_Term;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Note_Historique;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Preferences;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Proposition;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Role;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Term_Candidat;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Term_Historique;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.User_Role;
import fr.cnrs.opentheso.core.exports.privatesdatas.tables.Table;
import fr.cnrs.opentheso.core.exports.privatesdatas.TablesColumn;

/**
 *
 * @author antonio.perez
 */
/**
 * Cette fonction permet de récupérer toutes les données de la table tableName
 *
 * @author antonio.perez
 */
public class ExportPrivatesDatas {

    private final ArrayList<String> columnOfTablesToIgnore;
    private final ArrayList<String> tablesToIgnore;

    public ExportPrivatesDatas() {
        columnOfTablesToIgnore = new ArrayList<>();
        columnOfTablesToIgnore.add("id");
        columnOfTablesToIgnore.add("id_alignement");
        columnOfTablesToIgnore.add("id");
        columnOfTablesToIgnore.add("id");
        columnOfTablesToIgnore.add("id_pref");
        columnOfTablesToIgnore.add("identifier");

        tablesToIgnore = new ArrayList<>();
        tablesToIgnore.add("languages_iso639");
        tablesToIgnore.add("alignement_type");
        tablesToIgnore.add("concept_group_type");
        tablesToIgnore.add("users2");
        tablesToIgnore.add("note_type");
        tablesToIgnore.add("roles");
    }

    /**
     * nous ignorons le données qui sont dans columnOfTablesToIgnore
     *
     * @param value
     * @return
     */
    public boolean isToIgnore(String value) {
        for (String colomne : columnOfTablesToIgnore) {
            if (value.equalsIgnoreCase(colomne)) {
                return true;
            }
        }
        return false;
    }

    /**
     * on ignore les tables qui sont dans tablesToIgnore
     *
     * @param value
     * @return
     */
    public boolean isTableToIgnore(String value) {
        for (String table : tablesToIgnore) {
            if (value.equalsIgnoreCase(table)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Table> getDatasOfTable(HikariDataSource ds, String tableName) {
        ArrayList<Table> tableList = new ArrayList<>();
        if (!isTableToIgnore(tableName)) {
            ArrayList<TablesColumn> tablesColumns = new ArrayList<>();
            Connection conn;
            Statement stmt;
            ResultSet resultSet;
            String colomneNameTemp;
            try {
                conn = ds.getConnection();
                try {
                    stmt = conn.createStatement();
                    try {
                        // récupération des noms des colonnes de la table
                        //et sont type de données
                        String query = "SELECT COLUMN_NAME,"
                                + " data_type FROM INFORMATION_SCHEMA.COLUMNS"
                                + " where TABLE_NAME='" + tableName + "'";
                        resultSet = stmt.executeQuery(query);
                        while (resultSet.next()) {
                            colomneNameTemp = resultSet.getString("COLUMN_NAME");
                            if (!isToIgnore(colomneNameTemp)) {
                                TablesColumn tablesColumn = new TablesColumn();
                                tablesColumn.setColumnName(colomneNameTemp);
                                tablesColumn.setColumnType(resultSet.getString("data_type"));
                                tablesColumns.add(tablesColumn);
                            }
                        }
                        // récupération des données de la table
                        query = "SELECT * FROM " + tableName;
                        resultSet = stmt.executeQuery(query);

                        while (resultSet.next()) {
                            Table table = new Table();
                            ArrayList<LineOfData> lineOfDatas = new ArrayList<>();
                            for (TablesColumn tablesColumn : tablesColumns) {
                                LineOfData lineOfData = new LineOfData();
                                lineOfData.setColomne(tablesColumn.getColumnName());
                                if (tablesColumn.getColumnType().equalsIgnoreCase("integer")) {
                                    lineOfData.setValue("" + resultSet.getInt(tablesColumn.getColumnName()));
                                }
                                if (tablesColumn.getColumnType().equalsIgnoreCase("character varying")) {
                                    lineOfData.setValue(resultSet.getString(tablesColumn.getColumnName()));
                                }
                                if (tablesColumn.getColumnType().equalsIgnoreCase("character")) {
                                    lineOfData.setValue(resultSet.getString(tablesColumn.getColumnName()));
                                }
                                if (tablesColumn.getColumnType().equalsIgnoreCase("text")) {
                                    lineOfData.setValue(resultSet.getString(tablesColumn.getColumnName()));
                                }
                                if (tablesColumn.getColumnType().equalsIgnoreCase("timestamp with time zone")) {
                                    lineOfData.setValue("" + resultSet.getDate(tablesColumn.getColumnName()));
                                }
                                if (tablesColumn.getColumnType().equalsIgnoreCase("timestamp without time zone")) {
                                    lineOfData.setValue("" + resultSet.getDate(tablesColumn.getColumnName()));
                                }
                                if (tablesColumn.getColumnType().equalsIgnoreCase("boolean")) {
                                    lineOfData.setValue("" + resultSet.getBoolean(tablesColumn.getColumnName()));
                                }
                                if (tablesColumn.getColumnType().equalsIgnoreCase("USER-DEFINED")) {
                                    lineOfData.setValue(resultSet.getString(tablesColumn.getColumnName()));
                                }
                                lineOfDatas.add(lineOfData);
                            }

                            table.setLineOfDatas(lineOfDatas);
                            tableList.add(table);
                        }
                    } finally {
                        stmt.close();
                    }
                } finally {
                    conn.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return tableList;
    }

    /**
     * on recupere toutes le tables de la BDD
     *
     * @param ds
     * @return
     */
    public ArrayList<String> showAllTables(HikariDataSource ds) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tables = new ArrayList<>();

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * from Information_Schema.Tables where table_type= 'BASE TABLE' and table_schema ='public'";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        tables.add(resultSet.getString("table_name"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tables;
    }

    /**
     * On recupere seulement les tables privates
     *
     * @param ds
     * @return
     */
    public ArrayList<String> showPrivateTables(HikariDataSource ds) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tablesprivate = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * from Information_Schema.Tables where table_type= 'BASE TABLE' and table_schema ='public'";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        if (resultSet.getString("table_name").equalsIgnoreCase("users")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("roles")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("user_role")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("concept_term_candidat")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("proposition")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("concept_candidat")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("term_candidat")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("concept_replacedby")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("images")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("preferences")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("concept_group_historique")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("concept_group_label_historique")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("concept_historique")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("hierarchical_relationship_historique")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("non_preferred_term")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("note_historique")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                        if (resultSet.getString("table_name").equalsIgnoreCase("term_historique")) {
                            tablesprivate.add(resultSet.getString("table_name"));
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tablesprivate;
    }

    /**
     * fonctions pour récupérer toutes données par table pour une utilisation
     * future
     */
    /**
     * Cette fonction permet de récupérer toutes les données de la table R
     *
     * @param ds
     * @return
     */
    public ArrayList<Role> getRoles(HikariDataSource ds) {
        ArrayList<Role> listRoles = new ArrayList<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM roles ";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Role role1 = new Role();

                        /*         role1.setId(resultSet.getInt("id"));
                        role1.setName(resultSet.getString("name"));
                        role1.setDescription(resultSet.getString("description"));
                         */
                        listRoles.add(role1);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listRoles;
    }

    public ArrayList<User_Role> getUser_Roles(HikariDataSource ds) {
        ArrayList<User_Role> list_User_Role = new ArrayList<>();
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM user_role";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        User_Role user_Role1 = new User_Role();

                        user_Role1.setId_user(resultSet.getInt("id_user"));
                        user_Role1.setId_role(resultSet.getInt("id_role"));
                        user_Role1.setId_thesaurus(resultSet.getString("id_thesaurus"));
                        user_Role1.setId_group(resultSet.getString("id_group"));

                        list_User_Role.add(user_Role1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list_User_Role;
    }

    public ArrayList<Concept_Term_Candidat> getConceptTermCandidat(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Concept_Term_Candidat> listConceptTermCandidat = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM concept_term_candidat";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Concept_Term_Candidat concetpTC = new Concept_Term_Candidat();

                        concetpTC.setId_concept(resultSet.getString("id_concept"));
                        concetpTC.setId_term(resultSet.getString("id_term"));
                        concetpTC.setId_thesaurus(resultSet.getString("id_thesaurus"));

                        listConceptTermCandidat.add(concetpTC);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listConceptTermCandidat;
    }

    public ArrayList<Proposition> getProposition(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Proposition> listProposition = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM proposition ";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Proposition proposition1 = new Proposition();

                        proposition1.setId_concept(resultSet.getString("id_concept"));
                        proposition1.setId_user(resultSet.getInt("id_user"));
                        proposition1.setId_thesaurus(resultSet.getString("id_thesaurus"));
                        proposition1.setNote(resultSet.getString("note"));
                        proposition1.setCreated(resultSet.getDate("created"));
                        proposition1.setModified(resultSet.getDate("modified"));
                        proposition1.setConcept_parent(resultSet.getString("concept_parent"));
                        proposition1.setId_group(resultSet.getString("id_group"));

                        listProposition.add(proposition1);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listProposition;
    }

    public ArrayList<Concept_Candidat> getconceptCandidat(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Concept_Candidat> listConceptCandidat = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM concept_candidat";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Concept_Candidat conceptCandidat1 = new Concept_Candidat();
                        /*     
                        conceptCandidat1.setId_concept(resultSet.getString("id_concept"));
                        conceptCandidat1.setId_thesaururs(resultSet.getString("id_thesaurus"));
                        conceptCandidat1.setCreated(resultSet.getDate("created"));
                        conceptCandidat1.setModified(resultSet.getDate("modified"));
                        conceptCandidat1.setStatus(resultSet.getString("status"));
                        conceptCandidat1.setId(resultSet.getInt("id"));
                        conceptCandidat1.setAdmin_message(resultSet.getString("admin_message"));
                        conceptCandidat1.setAdmin_id(resultSet.getInt("admin_id"));
                         */
                        listConceptCandidat.add(conceptCandidat1);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listConceptCandidat;
    }

    public ArrayList<Term_Candidat> getTermeCandidat(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Term_Candidat> listTermeCandidat = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM term_candidat";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Term_Candidat termeCandidat1 = new Term_Candidat();

                        termeCandidat1.setId_term(resultSet.getString("id_term"));
                        termeCandidat1.setLexical_value(resultSet.getString("lexical_value"));
                        termeCandidat1.setLang(resultSet.getString("lang"));
                        termeCandidat1.setId_thesaurus(resultSet.getString("id_thesaurus"));
                        termeCandidat1.setCreated(resultSet.getDate("created"));
                        termeCandidat1.setModified(resultSet.getDate("modified"));
                        termeCandidat1.setContributor(resultSet.getInt("contributor"));
                        termeCandidat1.setId(resultSet.getInt("id"));

                        listTermeCandidat.add(termeCandidat1);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listTermeCandidat;
    }



    public ArrayList<Images> getImages(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Images> listImages = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM images";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Images images1 = new Images();

                        images1.setId_concept(resultSet.getString("id_concept"));
                        images1.setId_thesaururs(resultSet.getString("id_thesaurus"));
                        images1.setImage_name(resultSet.getString("image_name"));
                        images1.setImage_copyright(resultSet.getString("image_copyright"));
                        images1.setId_user(resultSet.getInt("id_user"));

                        listImages.add(images1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listImages;
    }

    public ArrayList<Concept_Group_Historique> getConceptGroupHist(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Concept_Group_Historique> listConceptGH = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM concept_group_historique";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Concept_Group_Historique conceptGroup_H1 = new Concept_Group_Historique();

                        conceptGroup_H1.setIdgroup(resultSet.getString("idgroup"));
                        conceptGroup_H1.setId_ark(resultSet.getString("id_ark"));
                        conceptGroup_H1.setIdthesaurus(resultSet.getString("idthesaurus"));
                        conceptGroup_H1.setIdtypecode(resultSet.getString("idtypecode"));
                        conceptGroup_H1.setIdparentgroup(resultSet.getString("idparentgroup"));
                        conceptGroup_H1.setNotation(resultSet.getString("notation"));
                        conceptGroup_H1.setIdconcept(resultSet.getString("idconcept"));
                        conceptGroup_H1.setId(resultSet.getInt("id"));
                        conceptGroup_H1.setModified(resultSet.getDate("modified"));
                        conceptGroup_H1.setId_user(resultSet.getInt("id_user"));

                        listConceptGH.add(conceptGroup_H1);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listConceptGH;
    }

    public ArrayList<Concept_Group_Label_Historique> getconceptGroupLabelH(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Concept_Group_Label_Historique> listConceptGLH = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM concept_group_label_historique";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Concept_Group_Label_Historique conceptGLH1 = new Concept_Group_Label_Historique();

                        conceptGLH1.setIdgrouplabel(resultSet.getInt("id"));
                        conceptGLH1.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        conceptGLH1.setModified(resultSet.getDate("modified"));
                        conceptGLH1.setLang(resultSet.getString("lang"));
                        conceptGLH1.setIdthesaurus(resultSet.getString("idthesaurus"));
                        conceptGLH1.setIdgroup(resultSet.getString("idgroup"));
                        conceptGLH1.setId_user(resultSet.getInt("id_user"));

                        listConceptGLH.add(conceptGLH1);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listConceptGLH;
    }

    public ArrayList<Concept_Historique> getConceptHistorique(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Concept_Historique> listConcetpHistorique = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM concept_historique";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Concept_Historique conceptHistorique1 = new Concept_Historique();

                        conceptHistorique1.setId_concept(resultSet.getString("id_concept"));
                        conceptHistorique1.setId_thesaurus(resultSet.getString("id_thesaurus"));
                        conceptHistorique1.setId_ark(resultSet.getString("id_ark"));
                        conceptHistorique1.setModified(resultSet.getDate("modified"));
                        conceptHistorique1.setStatus(resultSet.getString("status"));
                        conceptHistorique1.setNotation(resultSet.getString("notation"));
                        conceptHistorique1.setTop_concept(resultSet.getBoolean("top_concept"));
                        conceptHistorique1.setId(resultSet.getInt("id"));
                        conceptHistorique1.setId_user(resultSet.getInt("id_user"));

                        listConcetpHistorique.add(conceptHistorique1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listConcetpHistorique;
    }

    public ArrayList<Hierarchical_Relationship_Historique> getHierarchicalRelationshipH(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Hierarchical_Relationship_Historique> listHRelationShipH = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM hierarchical_relationship_historique";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Hierarchical_Relationship_Historique HRelationShipHistorique1 = new Hierarchical_Relationship_Historique();

                        HRelationShipHistorique1.setId_concept1(resultSet.getString("id_concept1"));
                        HRelationShipHistorique1.setId_thesaurus(resultSet.getString("id_thesaurus"));
                        HRelationShipHistorique1.setRole(resultSet.getString("role"));
                        HRelationShipHistorique1.setId_concept2(resultSet.getString("id_concept2"));
                        HRelationShipHistorique1.setModified(resultSet.getDate("modified"));
                        HRelationShipHistorique1.setId_user(resultSet.getInt("id_user"));
                        HRelationShipHistorique1.setAction(resultSet.getString("action"));

                        listHRelationShipH.add(HRelationShipHistorique1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listHRelationShipH;
    }

    public ArrayList<Non_Preferred_Term> getNonPreferredTerm(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Non_Preferred_Term> listNonPTerm = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM non_preferred_term";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Non_Preferred_Term nonPreferredTerm1 = new Non_Preferred_Term();

                        nonPreferredTerm1.setId_term(resultSet.getString("id_term"));
                        nonPreferredTerm1.setLexical_value(resultSet.getString("lexical_value"));
                        nonPreferredTerm1.setLang(resultSet.getString("lang"));
                        nonPreferredTerm1.setId_thesaurus(resultSet.getString("id_thesaurus"));
                        nonPreferredTerm1.setCreated(resultSet.getDate("created"));
                        nonPreferredTerm1.setModified(resultSet.getDate("modified"));
                        nonPreferredTerm1.setSource(resultSet.getString("source"));
                        nonPreferredTerm1.setStatus(resultSet.getString("status"));
                        nonPreferredTerm1.setHiden(resultSet.getBoolean("hiden"));

                        listNonPTerm.add(nonPreferredTerm1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listNonPTerm;
    }

    public ArrayList<Note_Historique> getNoteHistorique(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Note_Historique> listNoteHistorique = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM note_historique";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Note_Historique noteHistorique1 = new Note_Historique();

                        noteHistorique1.setId_note(resultSet.getInt("id"));
                        noteHistorique1.setNotetypecode(resultSet.getString("notetypecode"));
                        noteHistorique1.setId_thesaurus(resultSet.getString("id_thesaurus"));
                        noteHistorique1.setId_term(resultSet.getString("id_term"));
                        noteHistorique1.setId_concept(resultSet.getString("id_concept"));
                        noteHistorique1.setLang(resultSet.getString("lang"));
                        noteHistorique1.setLexicalvalue(resultSet.getString("lexicalvalue"));
                        noteHistorique1.setModified(resultSet.getDate("modified"));
                        noteHistorique1.setId_user(resultSet.getInt("id_user"));

                        listNoteHistorique.add(noteHistorique1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listNoteHistorique;
    }

    public ArrayList<Term_Historique> getTermHistorique(HikariDataSource ds) {
        ResultSet resultSet;
        Connection conn;
        Statement stmt;
        ArrayList<Term_Historique> listTermHistorique = new ArrayList<>();
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT * FROM term_historique";
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        Term_Historique termHistorique1 = new Term_Historique();

                        termHistorique1.setId_term(resultSet.getString("id_term"));
                        termHistorique1.setLexical_value(resultSet.getString("lexical_value"));
                        termHistorique1.setLang(resultSet.getString("lang"));
                        termHistorique1.setId_thesaurus(resultSet.getString("id_thesaurus"));
                        termHistorique1.setModified(resultSet.getDate("modified"));
                        termHistorique1.setSource(resultSet.getString("source"));
                        termHistorique1.setStatus(resultSet.getString("status"));
                        termHistorique1.setId(resultSet.getInt("id"));
                        termHistorique1.setId_user(resultSet.getInt("id_user"));

                        listTermHistorique.add(termHistorique1);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Table.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listTermHistorique;
    }

}
