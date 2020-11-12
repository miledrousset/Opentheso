package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAutoCompletion;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePermute;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTab2Levels;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearch;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SearchHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    public SearchHelper() {

    }

//////////////////////////////////////////////////////////    
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////
/// new function #MR
/// optimisation de la recherche en passant par index GIN     
//////////////////////////////////////////////////////////    
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////    
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////
/// new function For Opentheso2 #MR //////////////////////     
//////////////////////////////////////////////////////////    
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////    

    
    
    /**
     * Permet de chercher les terms avec précision pour limiter le bruit
     * avec filtre par langue et ou par groupe
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idGroup
     * @param idTheso
     * @return
     */
    public ArrayList<NodeAutoCompletion> searchAutoCompletionWS(HikariDataSource ds,
            String value, String idLang, String idGroup, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeAutoCompletion> nodeAutoCompletions = new ArrayList<>();
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);
        
        String multiValuesPT = "";
        String multiValuesNPT = "";        
        String values[] = value.trim().split(" ");

        // filter by lang, c'est très important 
        if(idLang != null && !idLang.isEmpty()) {
            multiValuesPT += " and term.lang = '" + idLang + "'";
            multiValuesNPT += " and non_preferred_term.lang = '" + idLang + "'"; 
        }
        
        // filter by group, c'est très important 
        if(idGroup != null && !idGroup.isEmpty()) {
            multiValuesPT += " and concept_group_concept.idgroup = '" + idGroup + "'";
            multiValuesNPT += " and concept_group_concept.idgroup = '" + idGroup + "'"; 
        }        
        
        
        for (String value1 : values) {
            multiValuesPT += 
                    " and ("
                    + " f_unaccent(lower(term.lexical_value)) like '" + value1 + "%'"
                    + " or"
                    + " f_unaccent(lower(term.lexical_value)) like '% " + value1 + "%'"
                    + " or"
                    + " f_unaccent(lower(term.lexical_value)) like '%''" + value1 + "%'"                    
                    + ")";
            multiValuesNPT += 
                    " and ("
                    + " f_unaccent(lower(non_preferred_term.lexical_value)) like '" + value1 + "%'"
                    + " or"
                    + " f_unaccent(lower(non_preferred_term.lexical_value)) like '% " + value1 + "%'"
                    + " or"
                    + " f_unaccent(lower(non_preferred_term.lexical_value)) like '%''" + value1 + "%'"                     
                    + ")";            
        }



        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if(idGroup != null && !idGroup.isEmpty()) {
                        query = "select term.lexical_value, concept.id_concept, concept.id_ark, concept.id_handle "
                                + " from concept, concept_group_concept, preferred_term, term " 
                                + " where"
                                + " concept.id_concept = concept_group_concept.idconcept" 
                                + " and" 
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus" 
                                + " and" 
                                + " concept.id_concept = preferred_term.id_concept" 
                                + " and" 
                                + " concept.id_thesaurus = preferred_term.id_thesaurus " 
                                + " and" 
                                + " preferred_term.id_term = term.id_term" 
                                + " and" 
                                + " preferred_term.id_thesaurus = term.id_thesaurus " 
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'" 
                                + multiValuesPT
                                + " limit 100";
                    } else {
                        query = "select term.lexical_value,"
                                + " concept.id_concept, concept.id_ark, concept.id_handle"
                                + " from term, preferred_term, concept where"
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and preferred_term.id_term = term.id_term"
                                + " and"
                                + " preferred_term.id_thesaurus = term.id_thesaurus"
                                + " and"
                                + " term.id_thesaurus = '" + idTheso + "'"
                                + multiValuesPT
                                + " limit 100";
                    }
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setIdArk(resultSet.getString("id_ark"));
                        nodeAutoCompletion.setIdHandle(resultSet.getString("id_handle"));                        
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setIsAltLabel(false);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeAutoCompletions.add(0, nodeAutoCompletion);
                        } else {
                            nodeAutoCompletions.add(nodeAutoCompletion);
                        }                        
                    }

                    /**
                     * recherche de Synonymes
                     */
                    if(idGroup != null && !idGroup.isEmpty()) {
                         query = "select non_preferred_term.lexical_value, concept.id_concept, concept.id_ark, concept.id_handle " 
                                + " from concept, concept_group_concept, preferred_term, non_preferred_term" 
                                + " where" 
                                + " concept.id_concept = concept_group_concept.idconcept" 
                                + " and"
                                + " concept.id_thesaurus = concept_group_concept.idthesaurus" 
                                + " and" 
                                + " concept.id_concept = preferred_term.id_concept" 
                                + " and" 
                                + " concept.id_thesaurus = preferred_term.id_thesaurus" 
                                + " and" 
                                + " preferred_term.id_term = non_preferred_term.id_term" 
                                + " and" 
                                + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus" 
                                
                                + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"                              
                                + multiValuesNPT
                                + " limit 100";                                  
                    } else {
                        query = "select non_preferred_term.lexical_value," 
                                + " concept.id_concept, concept.id_ark, concept.id_handle" 
                                + " from non_preferred_term, preferred_term, concept" 
                                + " where" 
                                +" preferred_term.id_term = non_preferred_term.id_term " 
                                + " and preferred_term.id_thesaurus = non_preferred_term.id_thesaurus" 
                                + " and preferred_term.id_concept = concept.id_concept" 
                                + " AND preferred_term.id_thesaurus = concept.id_thesaurus" 
                                + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"  
                                + multiValuesNPT
                                + " limit 100";                        
                    }


                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setIdArk(resultSet.getString("id_ark"));
                        nodeAutoCompletion.setIdHandle(resultSet.getString("id_handle"));                        
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setIsAltLabel(true);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeAutoCompletions.add(0, nodeAutoCompletion);
                        } else {
                            nodeAutoCompletions.add(nodeAutoCompletion);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeAutoCompletions;
    }    
    
    
    /** 
     * Cette fonction permet de faire une recherche par valeur sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) en utilisant la méthode PostgreSQL Trigram Index, le
     * résultat est proche d'une recherche avec ElasticSearch
     *
     * Elle retourne les identifiants avec filtre des doublons
     * 
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> searchFullTextId(HikariDataSource ds,
            String value, String idLang, String idThesaurus) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<String> listIds = new ArrayList<>();
        if(value == null) return listIds;        
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);


        String preparedValuePT = " and f_unaccent(lower(term.lexical_value)) like '%" + value + "%'";
        String preparedValueNPT = " and f_unaccent(lower(non_preferred_term.lexical_value)) like '%" + value + "%'";

        String query;
        String lang;
        String langSynonyme;

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {

                    query = "SELECT preferred_term.id_concept, term.lexical_value "
                            + " FROM term, preferred_term, concept WHERE "
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + preparedValuePT
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + " and concept.status != 'CA'"
                            + " order by term.lexical_value limit 150";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                  /*      NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setIsAltLabel(false);*/
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            if(!listIds.contains(resultSet.getString("id_concept")))
                                listIds.add(0, resultSet.getString("id_concept"));
                        } else {
                            if(!listIds.contains(resultSet.getString("id_concept")))
                                listIds.add(resultSet.getString("id_concept"));
                        }
                    }
                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT preferred_term.id_concept,"
                            + " non_preferred_term.lexical_value as npt,"
                            + " term.lexical_value as pt"
                            + " FROM"
                            + " non_preferred_term, term, preferred_term, concept WHERE"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + "  preferred_term.id_term = term.id_term AND"
                            + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                            + "   preferred_term.id_term = non_preferred_term.id_term AND"
                            + "   term.lang = non_preferred_term.lang AND"
                            + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                            + preparedValueNPT
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            + " and concept.status != 'CA'"
                            + " order by non_preferred_term.lexical_value limit 100";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                     /*   NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setAltLabel(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setIsAltLabel(true);*/
                        if (value.trim().equalsIgnoreCase(resultSet.getString("npt"))) {
                            if(!listIds.contains(resultSet.getString("id_concept")))
                                listIds.add(0, resultSet.getString("id_concept"));                            
                            
                            
                        } else {
                            if(!listIds.contains(resultSet.getString("id_concept")))
                                listIds.add(resultSet.getString("id_concept"));
                        }
                           /* if (nodeSearchMinis.get(0).getPrefLabel().equalsIgnoreCase(value.trim())) {
                                nodeSearchMinis.add(1, nodeSearchMini);
                            } else {
                                nodeSearchMinis.add(0, nodeSearchMini);
                            }
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }*/
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listIds;
    }    
    
    /** En cours d'optimisation (non utilisée encore)
     * Cette fonction permet de faire une recherche par valeur sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) en utilisant la méthode PostgreSQL Trigram Index, le
     * résultat est proche d'une recherche avec ElasticSearch
     *
     * Elle retourne la liste des termes + identifiants
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param limit
     * @return
     */
    public ArrayList<NodeSearchMini> searchFullText(HikariDataSource ds,
            String value, String idLang, String idThesaurus, int limit) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeSearchMini> nodeSearchMinis = null;
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String preparedValuePT = " and f_unaccent(lower(term.lexical_value)) like '%" + value + "%'";
        String preparedValueNPT = " and f_unaccent(lower(non_preferred_term.lexical_value)) like '%" + value + "%'";

        String query;
        String lang;
        String langSynonyme;

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    nodeSearchMinis = new ArrayList<>();
                    query = "SELECT preferred_term.id_concept, term.lexical_value "
                            + " FROM term, preferred_term WHERE "
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + preparedValuePT
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + " order by term.lexical_value limit " + limit;

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setIsAltLabel(false);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT preferred_term.id_concept,"
                            + " non_preferred_term.lexical_value as npt,"
                            + " term.lexical_value as pt"
                            + " FROM"
                            + " non_preferred_term, term, preferred_term WHERE"
                            + "  preferred_term.id_term = term.id_term AND"
                            + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                            + "   preferred_term.id_term = non_preferred_term.id_term AND"
                            + "   term.lang = non_preferred_term.lang AND"
                            + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                            + preparedValueNPT
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            + " order by non_preferred_term.lexical_value limit " + limit;

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setAltLabel(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setIsAltLabel(true);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("npt")) && !nodeSearchMinis.isEmpty()) {
                            if (nodeSearchMinis.get(0).getPrefLabel().equalsIgnoreCase(value.trim())) {
                                nodeSearchMinis.add(1, nodeSearchMini);
                            } else {
                                nodeSearchMinis.add(0, nodeSearchMini);
                            }
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchMinis;
    }





    /**
     * Permet de chercher les terms exacts avec des règles précises pour trouver
     * par exemple : or, Ur, d'Ur ...
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idTheso
     * @return
     */
    public ArrayList<NodeSearchMini> searchExactMatch(HikariDataSource ds,
            String value, String idLang, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "select preferred_term.id_concept, term.lexical_value, term.id_term from term, preferred_term, concept where"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_term = term.id_term"
                            + " and"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and"
                            + " term.id_thesaurus = '" + idTheso + "'"
                            + " and term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"
                            + " and ("
                            + "	f_unaccent(lower(lexical_value)) like '" + value + "'"
                            + "	or"
                            + "	f_unaccent(lower(lexical_value)) like '" + value + " %'"
                            + "	or"
                            + "	f_unaccent(lower(lexical_value)) like '% " + value + "'"
                            + "	or"
                            
                            
                            // pour les tirets pour trouver victor exp: saint-victor
                            + "	f_unaccent(lower(lexical_value)) like '" + value + "-%'"
                            + "	or"
                            + "	f_unaccent(lower(lexical_value)) like '%-" + value + "-%'"
                            + "	or"                            
                            + "	f_unaccent(lower(lexical_value)) like '%-" + value + "'"  
                            + "	or"
                            
                            // pour les sous_tirets pour trouver victor exp: saint_victor
                            + "	f_unaccent(lower(lexical_value)) like '" + value + "\\_%'"
                            + "	or"
                            + "	f_unaccent(lower(lexical_value)) like '%\\_" + value + "\\_%'"
                            + "	or"                            
                            + "	f_unaccent(lower(lexical_value)) like '%\\_" + value + "'" 
                            + "	or"
                            
                            

                            + "	f_unaccent(lower(lexical_value)) like '%''" + value + "'"
                            + "	)"
                            + " order by lexical_value";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setIsAltLabel(false);

                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }

                    /**
                     * recherche de Synonymes
                     */
                    query = "select preferred_term.id_concept, term.id_term,"
                            + " non_preferred_term.lexical_value as npt, term.lexical_value as pt"
                            + " from non_preferred_term, term, preferred_term, concept where"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_term = term.id_term"
                            + " and"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_term = non_preferred_term.id_term"
                            + " and"
                            + " term.lang = non_preferred_term.lang"
                            + " and"
                            + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                            + " and"
                            + " term.id_thesaurus = '" + idTheso + "'"
                            + " and term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"                            
                            + " and ("
                            + "	f_unaccent(lower(non_preferred_term.lexical_value)) like '" + value + "'"
                            + "	or"
                            + "	f_unaccent(lower(non_preferred_term.lexical_value)) like '" + value + " %'"
                            + "	or"
                            + "	f_unaccent(lower(non_preferred_term.lexical_value)) like '% " + value + "'"
                            + "	or"
                            + "	f_unaccent(lower(non_preferred_term.lexical_value)) like '" + value + "-%'"
                            + "	or"
                            + "	f_unaccent(lower(non_preferred_term.lexical_value)) like '%-" + value + "'"
                            + "	or"
                            + "	f_unaccent(lower(non_preferred_term.lexical_value)) like '%''" + value + "'"
                            + "	)"
                            + "order by non_preferred_term.lexical_value";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setAltLabel(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setIsAltLabel(true);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("npt"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchMinis;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes exactes en
     * ignorant les accents et la casse)
     *
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idTheso
     * @return
     */
    public ArrayList<NodeSearchMini> searchExactTermForAutocompletion(HikariDataSource ds,
            String value, String idLang, String idTheso) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String query;
        String lang;
        String langSynonyme;
        String multivaluesTerm = "";
        String multivaluesSynonyme = "";
        multivaluesTerm
                += " and f_unaccent(lower(term.lexical_value)) like"
                + " '" + value + "'";
        multivaluesSynonyme
                += " and f_unaccent(lower(non_preferred_term.lexical_value)) like"
                + " '" + value + "'";

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT preferred_term.id_concept, term.lexical_value, "
                            + " preferred_term.id_term"
                            + " FROM term, preferred_term WHERE "
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + multivaluesTerm
                            + " and term.id_thesaurus = '" + idTheso + "'"
                            + lang
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setIsAltLabel(false);
                        nodeSearchMinis.add(nodeSearchMini);
                    }

                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT preferred_term.id_concept, term.id_term, "
                            + " non_preferred_term.lexical_value as npt,"
                            + " term.lexical_value as pt"
                            + " FROM"
                            + " non_preferred_term, term, preferred_term WHERE"
                            + "  preferred_term.id_term = term.id_term AND"
                            + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                            + "   preferred_term.id_term = non_preferred_term.id_term AND"
                            + "   term.lang = non_preferred_term.lang AND"
                            + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                            + multivaluesSynonyme
                            + " and non_preferred_term.id_thesaurus = '" + idTheso + "'"
                            + langSynonyme
                            + " order by non_preferred_term.lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setAltLabel(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setIsAltLabel(true);
                        nodeSearchMinis.add(nodeSearchMini);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchMinis;
    }
    
 

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * mais en ajoutant uniquement les mots commencant par.Ca sert à afficher
     * les index par ordre alphabétique
     *
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param permuted
     * @param synonym
     * @return
     */
    public ArrayList<NodeIdValue> searchTermForIndex(HikariDataSource ds,
            String value, String idLang, String idThesaurus,
            boolean permuted, boolean synonym) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String query;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    // recherche avec les synonymes
                    if (synonym) {
                        query = "SELECT preferred_term.id_concept, non_preferred_term.lexical_value"
                                + " FROM non_preferred_term, preferred_term, concept WHERE "
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and"
                                + " preferred_term.id_term = non_preferred_term.id_term AND"
                                + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                + " and"
                                + " f_unaccent(lower(non_preferred_term.lexical_value)) like '" + value + "%'"
                                + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                                + " and non_preferred_term.lang ='" + idLang + "'"
                                + " and concept.status != 'CA'"
                                + " order by lexical_value ASC LIMIT 200";

                        resultSet = stmt.executeQuery(query);
                        while (resultSet.next()) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(resultSet.getString("id_concept"));
                            nodeIdValue.setValue(resultSet.getString("lexical_value"));
                            nodeIdValues.add(nodeIdValue);
                        }
                        if (permuted) {
                            query = "SELECT preferred_term.id_concept, non_preferred_term.lexical_value"
                                    + " FROM non_preferred_term, preferred_term, concept WHERE "
                                    + " concept.id_concept = preferred_term.id_concept"
                                    + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                    + " and"
                                    + " preferred_term.id_term = non_preferred_term.id_term AND"
                                    + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                                    + " and"
                                    + " f_unaccent(lower(non_preferred_term.lexical_value)) like '% " + value + "%'"
                                    + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                                    + " and non_preferred_term.lang ='" + idLang + "'"
                                    + " and concept.status != 'CA'"
                                    + " order by lexical_value ASC LIMIT 200";

                            resultSet = stmt.executeQuery(query);
                            while (resultSet.next()) {
                                NodeIdValue nodeIdValue = new NodeIdValue();
                                nodeIdValue.setId(resultSet.getString("id_concept"));
                                nodeIdValue.setValue(resultSet.getString("lexical_value"));
                                nodeIdValues.add(nodeIdValue);
                            }
                        }
                    } else {
                        // sans les synonymes
                        query = "SELECT preferred_term.id_concept, term.lexical_value"
                                + " FROM term, preferred_term, concept WHERE "
                                + " concept.id_concept = preferred_term.id_concept"
                                + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                + " and"
                                + " preferred_term.id_term = term.id_term AND"
                                + " preferred_term.id_thesaurus = term.id_thesaurus"
                                + " and"
                                + " f_unaccent(lower(term.lexical_value)) like '" + value + "%'"
                                + " and term.id_thesaurus = '" + idThesaurus + "'"
                                + " and term.lang ='" + idLang + "'"
                                + " and concept.status != 'CA'"
                                + " order by lexical_value ASC LIMIT 200";

                        resultSet = stmt.executeQuery(query);
                        while (resultSet.next()) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(resultSet.getString("id_concept"));
                            nodeIdValue.setValue(resultSet.getString("lexical_value"));
                            nodeIdValues.add(nodeIdValue);
                        }
                        if (permuted) {
                            query = "SELECT preferred_term.id_concept, term.lexical_value"
                                    + " FROM term, preferred_term, concept WHERE "
                                    + " concept.id_concept = preferred_term.id_concept"
                                    + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                                    + " and"
                                    + " preferred_term.id_term = term.id_term AND"
                                    + " preferred_term.id_thesaurus = term.id_thesaurus"
                                    + " and"
                                    + " f_unaccent(lower(term.lexical_value)) like '% " + value + "%'"
                                    + " and term.id_thesaurus = '" + idThesaurus + "'"
                                    + " and term.lang ='" + idLang + "'"
                                    + " and concept.status != 'CA'"
                                    + " order by lexical_value ASC LIMIT 200";

                            resultSet = stmt.executeQuery(query);
                            while (resultSet.next()) {
                                NodeIdValue nodeIdValue = new NodeIdValue();
                                nodeIdValue.setId(resultSet.getString("id_concept"));
                                nodeIdValue.setValue(resultSet.getString("lexical_value"));
                                nodeIdValues.add(nodeIdValue);
                            }
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdValues;
    }

    /**
     * Cette fonction permet de faire une recherche par valeur sur les notes de
     * type terme et concept (la recherche porte sur les termes contenus dans
     * une chaine) en utilisant la méthode PostgreSQL Trigram Index, le résultat
     * est proche d'une recherche avec ElasticSearch
     *
     * Elle retourne la liste des valeurs + identifiants
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return #MR
     */
    public ArrayList<NodeSearchMini> searchNotesElastic(HikariDataSource ds,
            String value, String idLang, String idThesaurus) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String query;
        String lang;

        // préparation de la requête en fonction du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
        } else {
            lang = " and note.lang ='" + idLang + "'";
        }
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    //recherche des notes de type concepts
                    query = "SELECT preferred_term.id_concept, note.lexicalvalue "
                            + " FROM note, preferred_term WHERE "
                            + " preferred_term.id_concept = note.id_concept AND"
                            + " preferred_term.id_thesaurus = note.id_thesaurus"
                            + " and f_unaccent(lower(note.lexicalvalue)) % '" + value + "'"
                            + " and note.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + " order by note.lexicalvalue <-> '" + value + "'";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexicalvalue"));
                        nodeSearchMini.setIsAltLabel(false);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexicalvalue"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                    //recherche des notes de type termes
                    query = "SELECT preferred_term.id_concept, note.lexicalvalue "
                            + " FROM note, preferred_term WHERE "
                            + " preferred_term.id_term = note.id_term AND"
                            + " preferred_term.id_thesaurus = note.id_thesaurus"
                            + " and f_unaccent(lower(note.lexicalvalue)) % '" + value + "'"
                            + " and note.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + " order by note.lexicalvalue <-> '" + value + "'";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexicalvalue"));
                        nodeSearchMini.setIsAltLabel(false);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexicalvalue"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchMinis;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les notes et
     * retourner les Ids des concepts
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return #MR
     */
    public ArrayList<String> searchIdConceptFromNotes(HikariDataSource ds,
            String value, String idLang, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();
        ArrayList<String> tabIdConcepts = new ArrayList<>();
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String multiValues = "";
        String values[] = value.trim().split(" ");
        for (String value1 : values) {
            multiValues += " and (f_unaccent(lower(note.lexicalvalue)) like '%" + value1 + "%')";
        }

        String query;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    // notes de type terme
                    query = "SELECT distinct preferred_term.id_concept"
                            + " FROM note, preferred_term, concept WHERE"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_term = note.id_term"
                            + " and"
                            + " preferred_term.id_thesaurus = note.id_thesaurus"
                            + multiValues
                            + //" and" +
                            //" f_unaccent(lower(note.lexicalvalue)) like '%" + value + "%'" +
                            " and note.id_thesaurus = '" + idThesaurus + "'"
                            + " and note.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        tabIdConcepts.add(resultSet.getString("id_concept"));
                    }
                    // notes des concepts
                    query = "SELECT distinct preferred_term.id_concept"
                            + " FROM note, preferred_term, concept WHERE"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " preferred_term.id_concept = note.id_concept"
                            + " and"
                            + " preferred_term.id_thesaurus = note.id_thesaurus"
                            + //" and" +
                            //" f_unaccent(lower(note.lexicalvalue)) % '" + value + "'" +
                            multiValues
                            + " and note.id_thesaurus = '" + idThesaurus + "'"
                            + " and note.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        tabIdConcepts.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tabIdConcepts;
    }

    /**
     * Cette fonction permet de faire une recherche sur les identifiants
     * (idConcept, idHandle, IdArk)
     *
     * @param ds
     * @param value
     * @param idTheso
     * @return #MR
     */
    public ArrayList<String> searchForIds(HikariDataSource ds,
            String value, String idTheso) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> idConcepts = null;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT concept.id_concept"
                            + " FROM concept WHERE "
                            + " concept.id_thesaurus = '" + idTheso + "'"
                            + " and status != 'CA'"
                            + " and (concept.id_concept = '" + value + "'"
                            + " or concept.id_ark = '" + value + "'"
                            + " or concept.id_handle = '" + value + "'"
                            + " or concept.notation = '" + value + "')";

                    resultSet = stmt.executeQuery(query);
                    idConcepts = new ArrayList<>();
                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idConcepts;
    }

    /**
     * Cette fonction permet de faire une recherche par valeur sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) en utilisant la méthode PostgreSQL Trigram Index, le
     * résultat est proche d'une recherche avec ElasticSearch
     *
     * Elle retourne la liste des termes + identifiants
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return #MR
     */
    public ArrayList<NodeSearchMini> searchFullTextElastic(HikariDataSource ds,
            String value, String idLang, String idThesaurus) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeSearchMini> nodeSearchMinis = null;
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String preparedValuePT = " and f_unaccent(lower(term.lexical_value)) % '" + value + "'";
        String preparedValueNPT = " and f_unaccent(lower(non_preferred_term.lexical_value)) % '" + value + "'";

        String query;
        String lang;
        String langSynonyme;

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    nodeSearchMinis = new ArrayList<>();
                    query = "SELECT preferred_term.id_concept, term.lexical_value, term.id_term "
                            + " FROM term, preferred_term, concept "
                            + " WHERE "
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + preparedValuePT
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + " and concept.status != 'CA'"
                            + " order by term.lexical_value <-> '" + value + "'";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setIsAltLabel(false);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeSearchMinis.add(0, nodeSearchMini);
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }
                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT preferred_term.id_concept, term.id_term, "
                            + " non_preferred_term.lexical_value as npt,"
                            + " term.lexical_value as pt"
                            + " FROM"
                            + " non_preferred_term, term, preferred_term, concept"
                            + " WHERE"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " and concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + "  preferred_term.id_term = term.id_term AND"
                            + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                            + "   preferred_term.id_term = non_preferred_term.id_term AND"
                            + "   term.lang = non_preferred_term.lang AND"
                            + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                            + preparedValueNPT
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            + " and concept.status != 'CA'"
                            + " order by non_preferred_term.lexical_value <-> '" + value + "'";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();
                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setIdTerm(resultSet.getString("id_term"));
                        nodeSearchMini.setAltLabel(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setIsAltLabel(true);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("npt"))) {
                            if (nodeSearchMinis.get(0).getPrefLabel().equalsIgnoreCase(value.trim())) {
                                nodeSearchMinis.add(1, nodeSearchMini);
                            } else {
                                nodeSearchMinis.add(0, nodeSearchMini);
                            }
                        } else {
                            nodeSearchMinis.add(nodeSearchMini);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchMinis;
    }

    /**
     * Cette fonction permet de récupérer une liste de termes pour
     * l'autocomplétion pour créer des relations entre les concepts
     *
     * @param ds
     * @param value
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public List<NodeSearchMini> searchAutoCompletionForRelation(
            HikariDataSource ds,
            String value,
            String idLang,
            String idTheso) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<NodeSearchMini> nodeSearchMinis = new ArrayList<>();
        StringPlus stringPlus = new StringPlus();

        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + " term.lexical_value,"
                            + " preferred_term.id_concept"
                            + " FROM"
                            + " concept, preferred_term, term"
                            + " WHERE"
                            + " preferred_term.id_concept = concept.id_concept "
                            + " AND  preferred_term.id_thesaurus = concept.id_thesaurus "
                            + " AND  term.id_term = preferred_term.id_term "
                            + " AND  term.id_thesaurus = preferred_term.id_thesaurus "
                            + " AND  concept.status != 'hidden' "
                            + " AND term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"
                            + " AND term.id_thesaurus = '" + idTheso + "'"
                            + " AND f_unaccent(lower(term.lexical_value)) LIKE '%" + value + "%' order by term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();

                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setIsAltLabel(false);
                        nodeSearchMinis.add(nodeSearchMini);
                    }
                    query = "SELECT "
                            + "  non_preferred_term.lexical_value as npt,"
                            + "  term.lexical_value as pt,"
                            + "  preferred_term.id_concept"
                            + " FROM "
                            + "  concept, "
                            + "  preferred_term, "
                            + "  non_preferred_term, "
                            + "  term "
                            + " WHERE "
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  non_preferred_term.id_term = preferred_term.id_term AND"
                            + "  non_preferred_term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.id_term = preferred_term.id_term AND"
                            + "  term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.lang = non_preferred_term.lang AND"
                            + "  concept.status != 'hidden' AND"
                            + "  non_preferred_term.id_thesaurus = '" + idTheso + "' AND"
                            + "  non_preferred_term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"
                            + " AND"
                            + " f_unaccent(lower(non_preferred_term.lexical_value)) LIKE '%" + value + "%'"
                            + " order by non_preferred_term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();

                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setAltLabel(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setIsAltLabel(true);
                        nodeSearchMinis.add(nodeSearchMini);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of autocompletion of Text : " + value, sqle);
        }

        return nodeSearchMinis;
    }
    
    /**
     * Cette fonction permet de récupérer une liste de termes pour
     * l'autocomplétion pour créer des relations entre les concepts
     *
     * @param ds
     * @param value
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public ArrayList<NodeIdValue> searchAutoCompletionForRelationIdValue(
            HikariDataSource ds,
            String value,
            String idLang,
            String idTheso) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        StringPlus stringPlus = new StringPlus();

        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT "
                            + " term.lexical_value,"
                            + " preferred_term.id_concept"
                            + " FROM"
                            + " concept, preferred_term, term"
                            + " WHERE"
                            + " preferred_term.id_concept = concept.id_concept "
                            + " AND  preferred_term.id_thesaurus = concept.id_thesaurus "
                            + " AND  term.id_term = preferred_term.id_term "
                            + " AND  term.id_thesaurus = preferred_term.id_thesaurus "
                            + " AND  concept.status != 'hidden' "
                            + " AND term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"
                            + " AND term.id_thesaurus = '" + idTheso + "'"
                            + " AND f_unaccent(lower(term.lexical_value)) LIKE '%" + value + "%' order by term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();

                        nodeIdValue.setId(resultSet.getString("id_concept"));
                        nodeIdValue.setValue(resultSet.getString("lexical_value"));
                        nodeIdValues.add(nodeIdValue);
                    }
                    query = "SELECT "
                            + "  non_preferred_term.lexical_value as npt,"
                            + "  term.lexical_value as pt,"
                            + "  preferred_term.id_concept"
                            + " FROM "
                            + "  concept, "
                            + "  preferred_term, "
                            + "  non_preferred_term, "
                            + "  term "
                            + " WHERE "
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  non_preferred_term.id_term = preferred_term.id_term AND"
                            + "  non_preferred_term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.id_term = preferred_term.id_term AND"
                            + "  term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + "  term.lang = non_preferred_term.lang AND"
                            + "  concept.status != 'hidden' AND"
                            + "  non_preferred_term.id_thesaurus = '" + idTheso + "' AND"
                            + "  non_preferred_term.lang = '" + idLang + "'"
                            + " and concept.status != 'CA'"
                            + " AND"
                            + " f_unaccent(lower(non_preferred_term.lexical_value)) LIKE '%" + value + "%'"
                            + " order by non_preferred_term.lexical_value <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();

                        nodeIdValue.setId(resultSet.getString("id_concept"));
                        nodeIdValue.setValue(resultSet.getString("npt") + " ->" + resultSet.getString("pt"));
                        nodeIdValues.add(nodeIdValue);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List of autocompletion of Text : " + value, sqle);
        }

        return nodeIdValues;
    }    

//////////////////////////////////////////////////////////    
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////
/// Fin new function For Opentheso2 #MR //////////////////////     
//////////////////////////////////////////////////////////    
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////    


    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes exactes)
     *
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<NodeAutoCompletion> searchExactTermNewForAutocompletion(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeAutoCompletion> nodeAutoCompletions = null;
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String query;
        String lang;
        String langSynonyme;
        String group;
        String multivaluesTerm = "";
        String multivaluesSynonyme = "";
        multivaluesTerm
                += " and f_unaccent(lower(term.lexical_value)) like"
                + " '" + value + "'";
        multivaluesSynonyme
                += " and f_unaccent(lower(non_preferred_term.lexical_value)) like"
                + " '" + value + "'";

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }

        // cas du choix d'un group
        if (idGroup.isEmpty()) {
            group = "";
        } else {
            group = " and idgroup = '" + idGroup + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT preferred_term.id_concept, term.lexical_value, "
                            + " concept.id_concept, concept.id_ark, concept.id_handle"
                            + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                            + "concept_group_concept.idthesaurus  = term.id_thesaurus AND "
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + multivaluesTerm
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);
                    nodeAutoCompletions = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setIdArk(resultSet.getString("id_ark"));
                        nodeAutoCompletion.setIdHandle(resultSet.getString("id_handle"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setIsAltLabel(false);
                        nodeAutoCompletions.add(nodeAutoCompletion);
                    }

                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT preferred_term.id_concept, non_preferred_term.lexical_value, "
                            + " concept.id_concept, concept.id_ark, concept.id_handle"
                            + " FROM non_preferred_term, preferred_term,concept_group_concept, concept WHERE "
                            + " concept.id_concept = concept_group_concept.idconcept AND"
                            + "  concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + "  preferred_term.id_term = non_preferred_term.id_term AND"
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus "
                            + multivaluesSynonyme
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setIdArk(resultSet.getString("id_ark"));
                        nodeAutoCompletion.setIdHandle(resultSet.getString("id_handle"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setIsAltLabel(true);
                        nodeAutoCompletions.add(nodeAutoCompletion);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeAutoCompletions;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) exp : la recherche de "ceramiqu four" trouve la chaine
     * (four à céramique)
     *
     * Elle retourne la liste des termes + identifiants
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<NodeAutoCompletion> searchTermNewForAutocompletion(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeAutoCompletion> nodeAutoCompletions = null;
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String preparedValuePT = " and f_unaccent(lower(term.lexical_value)) % '" + value + "'";
        String preparedValueNPT = " and f_unaccent(lower(non_preferred_term.lexical_value)) % '" + value + "'";
//        String values [] = value.trim().split(" ");

        String preRequestPT;
        String preRequestNPT;

        String query;
        String lang;
        String langSynonyme;
//        String group;

        /*        String multivaluesTerm = "";
        String multivaluesSynonyme = "";
        for (String value1 : values) {
                multivaluesTerm += 
                        " and ( (f_unaccent(lower(term.lexical_value)) like" +
                        " '" + value1 + "%')" +
                        " or (f_unaccent(lower(term.lexical_value)) like" +
                        " '% " + value1 + "%') )" ;
                multivaluesSynonyme += 
                        " and ( (f_unaccent(lower(non_preferred_term.lexical_value)) like" +
                        " '" + value1 + "%')" + 
                        " or (f_unaccent(lower(non_preferred_term.lexical_value)) like" +
                        " '% " + value1 + "%') )" ;                        
            }  
         */
        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }

        // cas du choix d'un group
        if (idGroup.isEmpty()) {
            //    group = "";
            preRequestPT = "SELECT\n"
                    + "  preferred_term.id_concept, \n"
                    + "  term.lexical_value,\n"
                    + "  concept.id_handle, \n"
                    + "  concept.id_ark\n"
                    + "  FROM term, preferred_term, concept\n"
                    + "   where\n"
                    + "  \n"
                    + "  preferred_term.id_term = term.id_term AND\n"
                    + "  preferred_term.id_thesaurus = term.id_thesaurus AND\n"
                    + "  concept.id_concept = preferred_term.id_concept AND\n"
                    + "  concept.id_thesaurus = preferred_term.id_thesaurus";

            preRequestNPT = "SELECT preferred_term.id_concept,"
                    + " non_preferred_term.lexical_value as npt,"
                    + " term.lexical_value as pt,"
                    + " concept.id_ark, concept.id_handle FROM"
                    + " non_preferred_term, term, preferred_term, concept WHERE"
                    + "  preferred_term.id_term = term.id_term AND"
                    + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                    + "   preferred_term.id_term = non_preferred_term.id_term AND"
                    + "   term.lang = non_preferred_term.lang AND"
                    + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND"
                    + "   preferred_term.id_thesaurus = concept.id_thesaurus AND"
                    + "   preferred_term.id_concept = concept.id_concept";
        } else {
            preRequestPT = "SELECT\n"
                    + "  preferred_term.id_concept, \n"
                    + "  term.lexical_value,\n"
                    + "  concept.id_handle, \n"
                    + "  concept.id_ark\n"
                    + "  FROM term, preferred_term, concept\n"
                    + "   where\n"
                    + "  \n"
                    + "  preferred_term.id_term = term.id_term AND\n"
                    + "  preferred_term.id_thesaurus = term.id_thesaurus AND\n"
                    + "  concept.id_concept = preferred_term.id_concept AND\n"
                    + "  concept.id_thesaurus = preferred_term.id_thesaurus"
                    + " and idgroup = '" + idGroup + "'";

            preRequestNPT = "SELECT preferred_term.id_concept,"
                    + " non_preferred_term.lexical_value as npt,"
                    + " term.lexical_value as pt,"
                    + " concept.id_concept, concept.id_ark, concept.id_handle FROM"
                    + " non_preferred_term, term, preferred_term,concept_group_concept, concept WHERE"
                    + " concept.id_concept = concept_group_concept.idconcept AND"
                    + "  preferred_term.id_term = term.id_term AND"
                    + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                    + "  concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                    + "   preferred_term.id_term = non_preferred_term.id_term AND"
                    + "   term.lang = non_preferred_term.lang AND"
                    + "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND"
                    + "   preferred_term.id_thesaurus = concept.id_thesaurus AND"
                    + "   preferred_term.id_concept = concept.id_concept"
                    + " and idgroup = '" + idGroup + "'";

            //group = " and idgroup = '" + idGroup + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    nodeAutoCompletions = new ArrayList<>();
                    query
                            = /*"SELECT preferred_term.id_concept, term.lexical_value, "
                            + " concept.id_concept, concept.id_ark, concept.id_handle"
                            + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                            + "concept_group_concept.idthesaurus  = term.id_thesaurus AND "
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"*/ preRequestPT
                            + preparedValuePT
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            //    + group
                            + " order by term.lexical_value <-> '" + value + "'";

                    // deprecated by Miled, remplacée par une fonction (search full text)
                    /*
                    query = "SELECT preferred_term.id_concept, term.lexical_value, "
                            + " concept.id_concept, concept.id_ark, concept.id_handle"
                            + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                            + "concept_group_concept.idthesaurus  = term.id_thesaurus AND "
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + multivaluesTerm
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + group
                            + " order by lexical_value ASC LIMIT 200";
                     */
                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setIdArk(resultSet.getString("id_ark"));
                        nodeAutoCompletion.setIdHandle(resultSet.getString("id_handle"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setIsAltLabel(false);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("lexical_value"))) {
                            nodeAutoCompletions.add(0, nodeAutoCompletion);
                        } else {
                            nodeAutoCompletions.add(nodeAutoCompletion);
                        }
                    }

                    /**
                     * recherche de Synonymes
                     */
                    query = preRequestNPT
                            /*"SELECT preferred_term.id_concept," +
                            " non_preferred_term.lexical_value as npt," +
                            " term.lexical_value as pt," +
                            " concept.id_concept, concept.id_ark, concept.id_handle FROM"
                            + " non_preferred_term, term, preferred_term,concept_group_concept, concept WHERE" +
                            " concept.id_concept = concept_group_concept.idconcept AND" +
                            "  preferred_term.id_term = term.id_term AND" +
                            "  preferred_term.id_thesaurus = term.id_thesaurus AND" +
                            "  concept.id_thesaurus = concept_group_concept.idthesaurus AND" +
                            "   preferred_term.id_term = non_preferred_term.id_term AND" +
                            "   term.lang = non_preferred_term.lang AND" +
                            "   preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND" +
                            "   preferred_term.id_thesaurus = concept.id_thesaurus AND" +
                            "   preferred_term.id_concept = concept.id_concept"*/
                            + preparedValueNPT
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            //    + group
                            + " order by non_preferred_term.lexical_value <-> '" + value + "'";

                    /// deprécated by Miled, remplacé par la requête ci-dessus qui gère le lien entre les altLabels et les prefLabels 
                    /* query = "SELECT preferred_term.id_concept, non_preferred_term.lexical_value, "
                        + " concept.id_concept, concept.id_ark, concept.id_handle"
                        + " FROM non_preferred_term, preferred_term,concept_group_concept, concept WHERE "
                            
                        
                        + " concept.id_concept = concept_group_concept.idconcept AND" +
                        "  concept.id_thesaurus = concept_group_concept.idthesaurus AND" +
                        "  preferred_term.id_term = non_preferred_term.id_term AND" +
                        "  preferred_term.id_concept = concept.id_concept AND" +
                        "  preferred_term.id_thesaurus = concept.id_thesaurus AND" +
                        "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus "
                            
                        + multivaluesSynonyme
                        + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                        + langSynonyme
                        + group
                        + " order by lexical_value ASC LIMIT 200"; */
                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setIdArk(resultSet.getString("id_ark"));
                        nodeAutoCompletion.setIdHandle(resultSet.getString("id_handle"));
                        nodeAutoCompletion.setAltLabel(resultSet.getString("npt"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("pt"));
                        nodeAutoCompletion.setIsAltLabel(true);
                        if (value.trim().equalsIgnoreCase(resultSet.getString("npt"))) {
                            if (nodeAutoCompletions.get(0).getPrefLabel().equalsIgnoreCase(value.trim())) {
                                nodeAutoCompletions.add(1, nodeAutoCompletion);
                            } else {
                                nodeAutoCompletions.add(0, nodeAutoCompletion);
                            }
                        } else {
                            nodeAutoCompletions.add(nodeAutoCompletion);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeAutoCompletions;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) exp : la recherche de "ceramiqu four" trouve la chaine
     * (four à céramique)
     *
     * Elle retourne la liste des identiants des concepts
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<String> searchExactTermNew(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<String> idConcepts = null;
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String query;
        String lang;
        String langSynonyme;
        String group;
        String multivaluesTerm = "";
        String multivaluesSynonyme = "";
        multivaluesTerm
                += " and f_unaccent(lower(term.lexical_value)) like"
                + " '" + value + "'";
        multivaluesSynonyme
                += " and f_unaccent(lower(non_preferred_term.lexical_value)) like"
                + " '" + value + "'";

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }

        // cas du choix d'un group
        if (idGroup.isEmpty()) {
            group = "";
        } else {
            group = " and idgroup = '" + idGroup + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    if(group.isEmpty()) {
                        query = "SELECT preferred_term.id_concept"
                                + " FROM term, preferred_term, concept WHERE "
                                + " concept.id_concept = preferred_term.id_concept AND"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                                + " preferred_term.id_term = term.id_term AND"
                                + " preferred_term.id_thesaurus = term.id_thesaurus"
                                + multivaluesTerm
                                + " and term.id_thesaurus = '" + idThesaurus + "'"
                                + lang
                                + " order by lexical_value ASC LIMIT 200";                        
                    } else {
                        query = "SELECT preferred_term.id_concept"
                                + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                                + "concept_group_concept.idthesaurus  = term.id_thesaurus AND "
                                + "concept_group_concept.idconcept = preferred_term.id_concept AND"
                                + " concept.id_concept = preferred_term.id_concept AND"
                                + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                                + " preferred_term.id_term = term.id_term AND"
                                + " preferred_term.id_thesaurus = term.id_thesaurus"
                                + multivaluesTerm
                                + " and term.id_thesaurus = '" + idThesaurus + "'"
                                + lang
                                + group
                                + " order by lexical_value ASC LIMIT 200";
                    }


                    resultSet = stmt.executeQuery(query);
                    idConcepts = new ArrayList<>();
                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("id_concept"));
                    }

                    /**
                     * recherche de Synonymes
                     */
                    if(group.isEmpty()) {
                        query = "SELECT preferred_term.id_concept"
                                + " FROM non_preferred_term, preferred_term, concept WHERE "
                                + "  preferred_term.id_term = non_preferred_term.id_term AND"
                                + "  preferred_term.id_concept = concept.id_concept AND"
                                + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                                + "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus "
                                + multivaluesSynonyme
                                + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                                + langSynonyme
                                + group
                                + " order by lexical_value ASC LIMIT 200";                        
                    } else {
                        query = "SELECT preferred_term.id_concept"
                                + " FROM non_preferred_term, preferred_term,concept_group_concept, concept WHERE "
                                + " concept.id_concept = concept_group_concept.idconcept AND"
                                + "  concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                                + "  preferred_term.id_term = non_preferred_term.id_term AND"
                                + "  preferred_term.id_concept = concept.id_concept AND"
                                + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                                + "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus "
                                + multivaluesSynonyme
                                + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                                + langSynonyme
                                + group
                                + " order by lexical_value ASC LIMIT 200";                        
                    }                 
                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("id_concept"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idConcepts;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) exp : la recherche de "ceramiqu four" trouve la chaine
     * (four à céramique)
     *
     * Elle retourne la liste des identiants des concepts
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<String> searchTermNew(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<String> idConcepts = null;
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String values[] = value.trim().split(" ");

        String query;
        String lang;
        String langSynonyme;
        String group;
        String multivaluesTerm = "";
        String multivaluesSynonyme = "";
        for (String value1 : values) {
            multivaluesTerm
                    += " and ( (f_unaccent(lower(term.lexical_value)) like"
                    + " '" + value1 + "%')"
                    + " or (f_unaccent(lower(term.lexical_value)) like"
                    + " '% " + value1 + "%') )";
            multivaluesSynonyme
                    += " and ( (f_unaccent(lower(non_preferred_term.lexical_value)) like"
                    + " '" + value1 + "%')"
                    + " or (f_unaccent(lower(non_preferred_term.lexical_value)) like"
                    + " '% " + value1 + "%') )";
        }

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
        }

        // cas du choix d'un group
        if (idGroup.isEmpty()) {
            group = "";
        } else {
            group = " and idgroup = '" + idGroup + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT preferred_term.id_concept"
                            + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                            + "concept_group_concept.idthesaurus  = term.id_thesaurus AND "
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + multivaluesTerm
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);
                    idConcepts = new ArrayList<>();
                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("id_concept"));
                    }

                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT preferred_term.id_concept"
                            + " FROM non_preferred_term, preferred_term,concept_group_concept, concept WHERE "
                            + " concept.id_concept = concept_group_concept.idconcept AND"
                            + "  concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + "  preferred_term.id_term = non_preferred_term.id_term AND"
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus "
                            + multivaluesSynonyme
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        idConcepts.add(resultSet.getString("id_concept"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idConcepts;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) exp : la recherche de "ceramiqu four" trouve la chaine
     * (four à céramique)
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @param startByOrContain // 1=contient 2=commence par
     * @param withNote
     * @return
     */
    public ArrayList<NodeSearch> searchTermNew(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup,
            int startByOrContain, boolean withNote) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        StringPlus stringPlus = new StringPlus();

        ArrayList<NodeSearch> nodeSearchList = null;
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);

        String values[] = value.trim().split(" ");

        String query;
        String lang;
        String langSynonyme;
        String langNote;
        String group;
        String multivaluesTerm = "";
        String multivaluesSynonyme = "";
        String multivaluesNote = "";
        String notation = "";

        for (String value1 : values) {
            multivaluesTerm
                    += " and f_unaccent(lower(term.lexical_value)) like"
                    + " '%" + value1 + "%'";
            multivaluesSynonyme
                    += " and f_unaccent(lower(non_preferred_term.lexical_value)) like"
                    + " '%" + value1 + "%'";
            multivaluesNote
                    += " and f_unaccent(lower(note.lexicalvalue)) like"
                    + " '%" + value1 + "%'";
            notation
                    = " and f_unaccent(lower(concept.notation)) like "
                    + " '%" + value + "%'";
        }

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
            langNote = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
            langNote = " and note.lang ='" + idLang + "'";
        }

        // cas du choix d'un group
        if (idGroup.isEmpty()) {
            group = "";
        } else {
            group = " and idgroup = '" + idGroup + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT term.lexical_value, preferred_term.id_concept,"
                            + " preferred_term.id_term, term.lang, term.id_thesaurus,"
                            + " idgroup, concept.top_concept"
                            + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                            + "concept_group_concept.idthesaurus  = term.id_thesaurus AND "
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + multivaluesTerm
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(resultSet.getString("lang"));
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexical_value().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }

                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT non_preferred_term.id_term, non_preferred_term.lang,"
                            + " non_preferred_term.lexical_value, "
                            + " idgroup, preferred_term.id_concept,"
                            + " concept.top_concept"
                            + " FROM non_preferred_term, preferred_term,concept_group_concept, concept WHERE "
                            + "  concept.id_concept = concept_group_concept.idconcept AND"
                            + "  concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + "  preferred_term.id_term = non_preferred_term.id_term AND"
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus "
                            + multivaluesSynonyme
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(resultSet.getString("lang"));
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(false);

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexical_value().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }

                    /**
                     * recherche aussi dans les notes
                     */
                    if (withNote) {
                        query = "SELECT \n"
                                + "  note.lang, \n"
                                + "  note.lexicalvalue, \n"
                                + " note.id_term,"
                                + "  concept.top_concept, \n"
                                + "  concept.id_concept, \n"
                                + "  concept_group_concept.idgroup\n"
                                + " FROM \n"
                                + "  public.note, \n"
                                + "  public.preferred_term, \n"
                                + "  public.concept, \n"
                                + "  public.concept_group_concept\n"
                                + " WHERE \n"
                                + "  preferred_term.id_term = note.id_term AND\n"
                                + "  preferred_term.id_thesaurus = note.id_thesaurus AND\n"
                                + "  concept.id_concept = preferred_term.id_concept AND\n"
                                + "  concept.id_thesaurus = preferred_term.id_thesaurus AND\n"
                                + "  concept_group_concept.idconcept = concept.id_concept AND\n"
                                + "  concept_group_concept.idthesaurus = concept.id_thesaurus AND\n"
                                + "  note.id_thesaurus = '" + idThesaurus + "' "
                                + multivaluesNote
                                + langNote
                                + group
                                + " ORDER BY\n"
                                + "  note.lexicalvalue ASC;";

                        resultSet = stmt.executeQuery(query);

                        while (resultSet.next()) {
                            NodeSearch nodeSearch = new NodeSearch();
                            nodeSearch.setLexical_value(resultSet.getString("lexicalvalue"));
                            nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                            nodeSearch.setIdTerm(resultSet.getString("id_term"));
                            nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                            nodeSearch.setIdLang(resultSet.getString("lang"));
                            nodeSearch.setIdThesaurus(idThesaurus);
                            nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                            nodeSearch.setPreferredLabel(true);

                            nodeSearchList.add(nodeSearch);
                        }
                    }
                    /**
                     * recherche aussi dans les notations
                     */
                    if (withNote) {
                        query = "SELECT concept.id_concept, concept.id_thesaurus,"
                                + " concept.top_concept, idgroup,"
                                + " concept.notation "
                                + " FROM concept JOIN concept_group_concept ON concept.id_concept = concept_group_concept.idconcept "
                                + "AND concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " WHERE"
                                + " concept.id_thesaurus = '" + idThesaurus + "'"
                                + notation
                                + group
                                + " order by notation ASC LIMIT 200";

                        resultSet = stmt.executeQuery(query);

                        while (resultSet.next()) {
                            NodeSearch nodeSearch = new NodeSearch();
                            nodeSearch.setLexical_value(resultSet.getString("notation"));
                            nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                            //                     nodeSearch.setIdTerm(resultSet.getString("id_term"));
                            nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                            nodeSearch.setIdLang(idLang);
                            nodeSearch.setIdThesaurus(idThesaurus);
                            nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                            nodeSearch.setPreferredLabel(true);

                            nodeSearchList.add(nodeSearch);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchList;
    }

    /**
     * Cette fonction permet de récupérer une liste de termes pour
     * l'autocomplétion avec les synonymes c'est la liste simple pour index
     * rapide
     *
     * @param ds
     * @param idThesaurus
     * @param text
     * @param idLang
     * @return Objet class Concept
     */
    public List<NodeAutoCompletion> getAutoCompletionIndex(HikariDataSource ds,
            String idThesaurus, String idLang, String text) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        List<NodeAutoCompletion> nodeAutoCompletionList = new ArrayList<>();
        StringPlus stringPlus = new StringPlus();

        text = stringPlus.convertString(text);
        text = stringPlus.unaccentLowerString(text);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT \n"
                            + "  preferred_term.id_concept, \n"
                            + "  term.lexical_value\n"
                            + " FROM \n"
                            + "  public.preferred_term, \n"
                            + "  public.term\n"
                            + " WHERE \n"
                            + "  preferred_term.id_term = term.id_term AND\n"
                            + "  preferred_term.id_thesaurus = term.id_thesaurus AND\n"
                            + "  term.lang = '" + idLang + "' AND \n"
                            + "  term.id_thesaurus = '" + idThesaurus + "' AND \n"
                            + "  f_unaccent(lower(term.lexical_value)) LIKE '%" + text + "%' limit 20";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();

                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setIsAltLabel(false);
                        nodeAutoCompletionList.add(nodeAutoCompletion);
                    }

                    query = "SELECT \n"
                            + "  preferred_term.id_concept, \n"
                            + "  non_preferred_term.lexical_value\n"
                            + " FROM \n"
                            + "  public.preferred_term, \n"
                            + "  public.non_preferred_term\n"
                            + " WHERE \n"
                            + "  preferred_term.id_term = non_preferred_term.id_term AND\n"
                            + "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND\n"
                            + "  non_preferred_term.lang = '" + idLang + "' AND \n"
                            + "  non_preferred_term.id_thesaurus = '" + idThesaurus + "' AND \n"
                            + "  f_unaccent(lower(non_preferred_term.lexical_value)) LIKE '%" + text + "%' limit 20";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeAutoCompletion nodeAutoCompletion = new NodeAutoCompletion();
                        nodeAutoCompletion.setIdConcept(resultSet.getString("id_concept"));
                        nodeAutoCompletion.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeAutoCompletion.setIsAltLabel(true);
                        nodeAutoCompletionList.add(nodeAutoCompletion);
                    }
                } finally {
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

//////////////////////////////////////////////////////////    
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////
/// end new function #MR
//////////////////////////////////////////////////////////    
//////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////
    /**
     * cette fonction permet de retourner la liste des concepts qui sont
     * duppliqués C'est à dire : à la fois un concept et un synonyme
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<NodeSearchError> getduplicateTermSynonym(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearchError> listTerms = new ArrayList<>();
        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT term.lexical_value AS pref,"
                            + " non_preferred_term.lexical_value AS alt,"
                            + " preferred_term.id_concept"
                            + " FROM concept_group_concept,"
                            + " preferred_term, term, non_preferred_term"
                            + " WHERE"
                            + " preferred_term.id_thesaurus = term.id_thesaurus AND"
                            + " preferred_term.id_concept = non_preferred_term.id_term AND"
                            + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND"
                            + " preferred_term.id_concept = concept_group_concept.idconcept AND"
                            + " preferred_term.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + " term.lang = non_preferred_term.lang AND"
                            + " term.id_thesaurus = '" + idThesaurus + "' AND"
                            + " term.lang = '" + idLang + "' AND"
                            + " concept_group_concept.idgroup = '" + idGroup + "' AND"
                            + " term.lexical_value ILIKE non_preferred_term.lexical_value;";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        NodeSearchError nodeSearchError = new NodeSearchError();
                        nodeSearchError.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchError.setPrefLabel(resultSet.getString("pref"));
                        nodeSearchError.setAltLabel(resultSet.getString("alt"));
                        listTerms.add(nodeSearchError);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listTerms;
    }

    /**
     * Cette fonction permet de faire une recherche par valeur sur les termes
     * Préférés (la recherche est stricte, pas de troncature mais on ignore les
     * majuscules et les accents)
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<String> simpleSearchPreferredTerm(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> listTerms = new ArrayList<>();
        value = new StringPlus().convertString(value);
        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT term.lexical_value"
                            + " FROM term, concept_group_concept,"
                            + " preferred_term"
                            + " WHERE "
                            + " term.id_term = preferred_term.id_term AND"
                            + " term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_concept = concept_group_concept.idconcept AND"
                            + " preferred_term.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + " term.id_thesaurus = '" + idThesaurus + "' AND"
                            + " term.lang = '" + idLang + "' AND"
                            + " concept_group_concept.idgroup = '" + idGroup + "'"
                            + " and unaccent_string(term.lexical_value) ilike"
                            + " unaccent_string('" + value + "')";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        listTerms.add(resultSet.getString("lexical_value"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listTerms;
    }

    /**
     * Cette fonction permet de faire une recherche par valeur sur les synonymes
     * (la recherche est stricte, pas de troncature mais on ignore les
     * majuscules et les accents)
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public HashMap simpleSearchNonPreferredTerm(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {

        HashMap<String, String> hmap = new HashMap<>();
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        value = new StringPlus().convertString(value);
        String query;
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT   term.lexical_value AS pref, non_preferred_term.lexical_value AS alt"
                            + " FROM term, non_preferred_term, concept_group_concept,"
                            + " preferred_term"
                            + " WHERE "
                            + " term.id_term = non_preferred_term.id_term AND"
                            + " term.lang = non_preferred_term.lang AND"
                            + " term.id_thesaurus = non_preferred_term.id_thesaurus AND"
                            + " preferred_term.id_concept = concept_group_concept.idconcept AND"
                            + " preferred_term.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + " non_preferred_term.id_term = preferred_term.id_term AND"
                            + " non_preferred_term.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " non_preferred_term.id_thesaurus = '" + idThesaurus + "' AND"
                            + " non_preferred_term.lang = '" + idLang + "' AND"
                            + " concept_group_concept.idgroup = '" + idGroup + "'"
                            + " and unaccent_string(non_preferred_term.lexical_value) ilike"
                            + " unaccent_string('" + value + "')";

                    resultSet = stmt.executeQuery(query);
                    while (resultSet.next()) {
                        hmap.put(resultSet.getString("pref"), resultSet.getString("alt"));
                        //     listTerms.add(resultSet.getString("lexical_value"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hmap;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * Préférés et les synonymes (la recherche porte sur les termes contenus
     * dans une chaine) exp : la recherche de "ceramiqu four" trouve la chaine
     * (four à céramique)
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @param startByOrContain // 1=contient 2=commence par
     * @param withNote
     * @return
     */
    public ArrayList<NodeSearch> searchTerm(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup,
            int startByOrContain, boolean withNote) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = null;
        value = new StringPlus().convertString(value);
        String values[] = value.trim().split(" ");
        String query;
        String lang;
        String langSynonyme;
        String langNote;
        String group;
        String multivaluesTerm = "";
        String multivaluesSynonyme = "";
        String multivaluesNote = "";
        String notation = "";

        // préparation de la valeur à rechercher 
        if (startByOrContain == 1) { // contient
            for (String value1 : values) {
                multivaluesTerm
                        += " and unaccent_string(term.lexical_value) ilike"
                        + " unaccent_string('%" + value1 + "%')";
                multivaluesSynonyme
                        += " and unaccent_string(non_preferred_term.lexical_value) ilike"
                        + " unaccent_string('%" + value1 + "%')";
                multivaluesNote
                        += " and unaccent_string(note.lexicalvalue) ilike"
                        + " unaccent_string('%" + value1 + "%')";
                notation
                        = " and unaccent_string(concept.notation) ilike "
                        + "unaccent_string('%" + value + "%')";
            }
        }
        if (startByOrContain == 2) { // commence par

            multivaluesTerm = " and (unaccent_string(term.lexical_value) ilike"
                    + " unaccent_string('" + value + "%')"
                    + " OR unaccent_string(term.lexical_value) ilike"
                    + " unaccent_string('% " + value + "%'))";

            multivaluesSynonyme
                    = " and (unaccent_string(non_preferred_term.lexical_value) ilike"
                    + " unaccent_string('" + value + "%')"
                    + " OR unaccent_string(non_preferred_term.lexical_value) ilike"
                    + " unaccent_string('% " + value + "%'))";

            multivaluesNote
                    = " and (unaccent_string(note.lexicalvalue) ilike"
                    + " unaccent_string('" + value + "%')"
                    + " OR unaccent_string(note.lexicalvalue) ilike"
                    + " unaccent_string('% " + value + "%'))";
            notation
                    = " and unaccent_string(concept.notation) ilike "
                    + "unaccent_string('%" + value + "%')";

        }

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
            langSynonyme = "";
            langNote = "";
        } else {
            lang = " and term.lang ='" + idLang + "'";
            langSynonyme = " and non_preferred_term.lang ='" + idLang + "'";
            langNote = " and note.lang ='" + idLang + "'";
        }

        // cas du choix d'un group
        if (idGroup.isEmpty()) {
            group = "";
        } else {
            group = " and idgroup = '" + idGroup + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT term.lexical_value, preferred_term.id_concept,"
                            + " preferred_term.id_term, term.lang, term.id_thesaurus,"
                            + " idgroup, concept.top_concept"
                            + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                            + "concept_group_concept.idthesaurus  = term.id_thesaurus AND "
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + multivaluesTerm
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + lang
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(resultSet.getString("lang"));
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexical_value().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }

                    /**
                     * recherche de Synonymes
                     */
                    query = "SELECT non_preferred_term.id_term, non_preferred_term.lang,"
                            + " non_preferred_term.lexical_value, "
                            + " idgroup, preferred_term.id_concept,"
                            + " concept.top_concept"
                            + " FROM non_preferred_term, preferred_term,concept_group_concept, concept WHERE "
                            + "  concept.id_concept = concept_group_concept.idconcept AND"
                            + "  concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + "  preferred_term.id_term = non_preferred_term.id_term AND"
                            + "  preferred_term.id_concept = concept.id_concept AND"
                            + "  preferred_term.id_thesaurus = concept.id_thesaurus AND"
                            + "  preferred_term.id_thesaurus = non_preferred_term.id_thesaurus "
                            + multivaluesSynonyme
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + langSynonyme
                            + group
                            + " order by lexical_value ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(resultSet.getString("lang"));
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(false);

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexical_value().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }

                    /**
                     * recherche aussi dans les notes
                     */
                    if (withNote) {
                        query = "SELECT "
                                + " concept.id_concept, concept.id_thesaurus,"
                                + " concept.top_concept, concept_group_concept.idgroup,"
                                + " note.lang, note.lexicalvalue,"
                                + " note.id_term "
                                + " FROM term, preferred_term, note, concept,concept_group_concept"
                                + " WHERE"
                                + "  term.id_thesaurus = note.id_thesaurus AND"
                                + "  term.lang = note.lang AND"
                                + "  (term.id_term = note.id_term OR preferred_term.id_concept = note.id_concept) AND"
                                + "  preferred_term.id_term = term.id_term AND"
                                + "  preferred_term.id_thesaurus = term.id_thesaurus AND"
                                + "  concept_group_concept.idthesaurus = concept.id_thesaurus AND"
                                + "  concept_group_concept.idconcept = concept.id_concept AND"
                                + "  concept.id_concept = preferred_term.id_concept AND"
                                + "  concept.id_thesaurus = preferred_term.id_thesaurus AND"
                                + " note.id_thesaurus = '" + idThesaurus + "'"
                                + multivaluesNote
                                + langNote
                                + group
                                + " order by lexicalvalue ASC LIMIT 200";

                        resultSet = stmt.executeQuery(query);

                        while (resultSet.next()) {
                            NodeSearch nodeSearch = new NodeSearch();
                            nodeSearch.setLexical_value(resultSet.getString("lexicalvalue"));
                            nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                            nodeSearch.setIdTerm(resultSet.getString("id_term"));
                            nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                            nodeSearch.setIdLang(resultSet.getString("lang"));
                            nodeSearch.setIdThesaurus(idThesaurus);
                            nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                            nodeSearch.setPreferredLabel(true);

                            nodeSearchList.add(nodeSearch);
                        }
                    }
                    /**
                     * recherche aussi dans les notations
                     */
                    if (withNote) {
                        query = "SELECT concept.id_concept, concept.id_thesaurus,"
                                + " concept.top_concept, idgroup,"
                                + " concept.notation "
                                + " FROM concept JOIN concept_group_concept ON concept.id_concept = concept_group_concept.idconcept "
                                + "AND concept.id_thesaurus = concept_group_concept.idthesaurus"
                                + " WHERE"
                                + " concept.id_thesaurus = '" + idThesaurus + "'"
                                + notation
                                + group
                                + " order by notation ASC LIMIT 200";

                        resultSet = stmt.executeQuery(query);

                        while (resultSet.next()) {
                            NodeSearch nodeSearch = new NodeSearch();
                            nodeSearch.setLexical_value(resultSet.getString("notation"));
                            nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                            //                     nodeSearch.setIdTerm(resultSet.getString("id_term"));
                            nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                            nodeSearch.setIdLang(idLang);
                            nodeSearch.setIdThesaurus(idThesaurus);
                            nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                            nodeSearch.setPreferredLabel(true);

                            nodeSearchList.add(nodeSearch);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les notes
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @param startByOrContain //1=contient 2=commence par
     * @return
     */
    public ArrayList<NodeSearch> searchNote(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup,
            int startByOrContain) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = new ArrayList<>();
        value = new StringPlus().convertString(value);

        String values[] = value.trim().split(" ");
        String query;
        String langNote;
        String group;
        String multivaluesNote = "";

        // préparation de la valeur à rechercher 
        if (startByOrContain == 1) { // contient
            for (String value1 : values) {
                multivaluesNote
                        += " and unaccent_string(note.lexicalvalue) ilike"
                        + " unaccent_string('%" + value1 + "%')";
            }
        }
        if (startByOrContain == 2) { // commence par
            multivaluesNote
                    += " and (unaccent_string(note.lexicalvalue) ilike"
                    + " unaccent_string('" + value + "%')"
                    + " OR unaccent_string(note.lexicalvalue) ilike"
                    + " unaccent_string('% " + value + "%'))";
        }

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            langNote = "";
        } else {
            langNote = " and note.lang ='" + idLang + "'";
        }

        // cas du choix d'un group
        if (idGroup.isEmpty()) {
            group = "";
        } else {
            group = " and idgroup = '" + idGroup + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    // notes des terms
                    query = "SELECT concept.id_concept, concept.id_thesaurus,"
                            + " concept.top_concept, idgroup,"
                            + " note.lang, note.lexicalvalue,"
                            + " note.id_term "
                            + " FROM preferred_term, note, concept,concept_group_concept"
                            + " WHERE "
                            + "concept_group_concept.idthesaurus = concept.id_thesaurus AND "
                            + "concept.id_concept = concept_group_concept.idconcept AND "
                            + " preferred_term.id_term = note.id_term"
                            + " AND"
                            + " preferred_term.id_thesaurus = note.id_thesaurus"
                            + " AND"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " AND"
                            + " note.id_thesaurus = '" + idThesaurus + "'"
                            + multivaluesNote
                            + langNote
                            + group
                            + " order by lexicalvalue ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexicalvalue"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(resultSet.getString("lang"));
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        nodeSearchList.add(nodeSearch);
                    }
                    // notes des concepts
                    query = "SELECT concept.id_concept, concept.id_thesaurus,"
                            + " concept.top_concept, idgroup,"
                            + " note.lang, note.lexicalvalue,"
                            + " preferred_term.id_term "
                            + " FROM preferred_term, note, concept, concept_group_concept"
                            + " WHERE "
                            + "concept_group_concept.idconcept = concept.id_concept AND "
                            + "concept_group_concept.idthesaurus = concept.id_thesaurus AND "
                            + " preferred_term.id_concept = note.id_concept"
                            + " AND"
                            + " preferred_term.id_thesaurus = note.id_thesaurus"
                            + " AND"
                            + " concept.id_concept = preferred_term.id_concept"
                            + " AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " AND"
                            + " note.id_thesaurus = '" + idThesaurus + "'"
                            + multivaluesNote
                            + langNote
                            + group
                            + " order by lexicalvalue ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);

                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexicalvalue"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(resultSet.getString("lang"));
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        nodeSearchList.add(nodeSearch);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par notation des Concepts
     * uniquement
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<NodeSearch> searchNotation(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = null;
        value = new StringPlus().convertString(value);
        String query;
        String lang;
        String group;
        String notation
                = " and unaccent_string(concept.notation) ilike "
                + "unaccent_string('%" + value + "%')";

        // préparation de la requête en focntion du choix (toutes les langues ou langue donnée) 
        if (idLang.isEmpty()) {
            lang = "";
        }

        // cas du choix d'un group
        if (idGroup.isEmpty()) {
            group = "";
        } else {
            group = " and idgroup = '" + idGroup + "'";
        }

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "SELECT concept.id_concept, concept.id_thesaurus,"
                            + " concept.top_concept, idgroup,"
                            + " concept.notation "
                            + " FROM concept "
                            + "JOIN concept_group_concept ON concept.id_concept = concept_group_concept.idconcept "
                            + "AND concept.id_thesaurus = concept_group_concept.idthesaurus "
                            + " WHERE"
                            + " concept.id_thesaurus = '" + idThesaurus + "'"
                            + notation
                            + group
                            + " order by notation ASC LIMIT 200";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("notation"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        //                     nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(idLang);
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        nodeSearchList.add(nodeSearch);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par notation comme c'est une
     * valeur unique, on peut cherche sur tout le thésaurus sans filter par
     * groupe
     *
     * @param ds
     * @param value
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> searchNotationId(HikariDataSource ds,
            String value, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> ListIdConcept = null;
        value = new StringPlus().convertString(value);
        String query;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    query = "select id_concept from concept where notation ilike '" + value + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'";

                    resultSet = stmt.executeQuery(query);
                    ListIdConcept = new ArrayList();
                    while (resultSet.next()) {
                        ListIdConcept.add(resultSet.getString("id_concept"));
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ListIdConcept;
    }

    /**
     * Cette fonction permet de faire une recherche par value sur les termes
     * préférés
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return
     */
    public ArrayList<NodeSearch> searchPreferredTerm(HikariDataSource ds,
            String value, String idLang, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = null;

        value = new StringPlus().convertString(value);
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.lexical_value, preferred_term.id_concept,"
                            + " preferred_term.id_term, term.lang, term.id_thesaurus,"
                            + " idgroup, concept.top_concept"
                            + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                            + "concept_group_concept.idthesaurus = term.id_thesaurus AND"
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and"
                            + " unaccent_string(term.lexical_value) ilike"
                            + " unaccent_string('" + value + "%')"
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + " and term.lang = '" + idLang + "'"
                            + " order by lexical_value ASC LIMIT 100";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(idLang);
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexical_value().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par idConcept ici on indique
     * la langue
     *
     * @param ds
     * @param id
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public ArrayList<NodeSearch> searchIdConcept(HikariDataSource ds,
            String id, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = null;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.lexical_value,"
                            + " preferred_term.id_term, term.lang, term.id_thesaurus,"
                            + " idgroup, concept.top_concept"
                            + " FROM term, preferred_term, concept, concept_group_concept WHERE "
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND "
                            + "concept_group_concept.idthesaurus = term.id_thesaurus AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and concept.id_concept = '" + id + "'"
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + " order by lexical_value ASC LIMIT 100";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(id);
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(idLang);
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        nodeSearchList.add(nodeSearch);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par idConcept sans tenir
     * compte de la langue on retourne un tableau du Concept dans toutes les
     * langues disponibles
     *
     * @param ds
     * @param id
     * @param idThesaurus
     * @return
     */
    public ArrayList<NodeSearch> searchIdConcept(HikariDataSource ds,
            String id, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = null;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.lexical_value,"
                            + " preferred_term.id_term, term.lang, term.id_thesaurus,"
                            + " idgroup, concept.top_concept"
                            + " FROM term, preferred_term, concept, concept_group_concept WHERE "
                            + "concept_group_concept.idconcept = preferred_term.id_term AND "
                            + "concept_group_concept.idthesaurus = term.id_thesaurus AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and concept.id_concept = '" + id + "'"
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + " order by lexical_value ASC";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(id);
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(resultSet.getString("lang"));
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        nodeSearchList.add(nodeSearch);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par idTerme
     *
     * @param ds
     * @param id
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public ArrayList<NodeSearch> searchIdTerm(HikariDataSource ds,
            String id, String idThesaurus, String idLang) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = null;

        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.lexical_value, preferred_term.id_concept,"
                            + " term.lang, term.id_thesaurus,"
                            + " idgroup, concept.top_concept"
                            + " FROM term, preferred_term, concept,concept_group_concept WHERE "
                            + " concept_group_concept.idthesaurus = term.id_thesaurus AND "
                            + "concept_group_concept.idconcept =preferred_term.id_concept AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and term.id_term = '" + id + "'"
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + " and term.lang = '" + idLang + "'"
                            + " order by lexical_value ASC LIMIT 100";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(id);
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(idLang);
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(true);

                        nodeSearchList.add(nodeSearch);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par value en filtran par
     * Group pour les termes préférés uniquement
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<NodeSearch> searchPreferredTerm(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = null;
        value = new StringPlus().convertString(value);
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT term.lexical_value, preferred_term.id_concept,"
                            + " preferred_term.id_term, term.lang, term.id_thesaurus,"
                            + " idgroup, concept.top_concept"
                            + " FROM term, preferred_term, concept, concept_group_concept WHERE "
                            + "concept_group_concept.idthesaurus = term.id_thesaurus AND "
                            + "concept_group_concept.idconcept = preferred_term.id_concept AND "
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus"
                            + " and"
                            + " unaccent_string(term.lexical_value) ilike"
                            + " unaccent_string('" + value + "%')"
                            + " and term.id_thesaurus = '" + idThesaurus + "'"
                            + " and term.lang = '" + idLang + "'"
                            + " and idgroup = '" + idGroup + "'"
                            + " order by lexical_value ASC LIMIT 100";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(idLang);
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexical_value().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par value en filtran par
     * Group
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return
     */
    public ArrayList<NodeSearch> searchNonPreferedTerm(HikariDataSource ds,
            String value, String idLang, String idThesaurus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = null;
        value = new StringPlus().convertString(value);
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT non_preferred_term.id_term, "
                            + " non_preferred_term.lexical_value, "
                            + " idgroup, preferred_term.id_concept,"
                            + " concept.top_concept"
                            + " FROM non_preferred_term, preferred_term,"
                            + " concept,concept_group_concept WHERE "
                            + " concept_group_concept.idthesaurus = non_preferred_term.id_thesaurus AND"
                            + " concept_group_concept.idconcept = preferred_term.id_concept AND"
                            + " preferred_term.id_term = non_preferred_term.id_term AND"
                            + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " unaccent_string(non_preferred_term.lexical_value) ilike"
                            + " unaccent_string('" + value + "%')"
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + " and non_preferred_term.lang = '" + idLang + "'"
                            + " order by lexical_value ASC LIMIT 100";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(idLang);
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(false);

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexical_value().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nodeSearchList;
    }

    /**
     * Cette fonction permet de faire une recherche par value en filtran par
     * Group
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @param idGroup
     * @return
     */
    public ArrayList<NodeSearch> searchNonPreferedTerm(HikariDataSource ds,
            String value, String idLang, String idThesaurus, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeSearch> nodeSearchList = null;
        value = new StringPlus().convertString(value);
        try {
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT non_preferred_term.id_term, "
                            + " non_preferred_term.lexical_value, "
                            + " idgroup, preferred_term.id_concept,"
                            + " concept.top_concept"
                            + " FROM non_preferred_term, preferred_term,"
                            + " concept,concept_group_concept WHERE "
                            + " concept_group_concept.idthesaurus = non_preferred_term.id_thesaurus  AND"
                            + " concept_group_concept.idconcept = non_preferred_term.id_term AND"
                            + " preferred_term.id_term = non_preferred_term.id_term AND"
                            + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus AND"
                            + " concept.id_concept = preferred_term.id_concept AND"
                            + " concept.id_thesaurus = preferred_term.id_thesaurus"
                            + " and"
                            + " unaccent_string(non_preferred_term.lexical_value) ilike"
                            + " unaccent_string('" + value + "%')"
                            + " and non_preferred_term.id_thesaurus = '" + idThesaurus + "'"
                            + " and non_preferred_term.lang = '" + idLang + "'"
                            + " and idgroup = '" + idGroup + "'"
                            + " order by lexical_value ASC LIMIT 100";

                    resultSet = stmt.executeQuery(query);
                    nodeSearchList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeSearch nodeSearch = new NodeSearch();
                        nodeSearch.setLexical_value(resultSet.getString("lexical_value"));
                        nodeSearch.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearch.setIdTerm(resultSet.getString("id_term"));
                        nodeSearch.setIdGroup(resultSet.getString("idgroup"));
                        nodeSearch.setIdLang(idLang);
                        nodeSearch.setIdThesaurus(idThesaurus);
                        nodeSearch.setTopConcept(resultSet.getBoolean("top_concept"));
                        nodeSearch.setPreferredLabel(false);

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodeSearch.getLexical_value().trim())) {
                            nodeSearchList.add(0, nodeSearch);
                        } else {
                            nodeSearchList.add(nodeSearch);
                        }
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }

        } catch (SQLException ex) {
            Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return nodeSearchList;
    }

    /**
     * FONCTION CAROLE Cette fonction permet de faire une recherche permutée par
     * value
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return
     */
    /*public ArrayList<NodePermute> searchTermPermute(HikariDataSource ds, 
     String value, String idLang, String idThesaurus) {

     Connection conn;
     Statement stmt;
     ResultSet resultSet;
     ArrayList <NodePermute> nodeSearchList = new ArrayList<>();
        
     ArrayList<Entry<String, String>> listId = searchIdPermute(ds, value, idLang, idThesaurus);
        
     try {
     conn = ds.getConnection();
     stmt = conn.createStatement();
     for(Entry<String, String> e : listId) {
     String query = "SELECT * FROM permute WHERE"
     + " and id_concept = '" + e.getKey() + "'";
            
     resultSet = stmt.executeQuery(query);
                
     resultSet.next();
     NodePermute np = new NodePermute();
     np.setIdConcept(e.getKey());
     np.setIdGroup(resultSet.getString("id_group"));
     np.setIdLang(idLang);
     np.setIndexOfValue(Integer.parseInt(e.getValue()));
     if(Integer.parseInt(resultSet.getString("ord")) < np.getIndexOfValue()) {
     np.setFirstColumn(resultSet.getString("lexical_value"));
     } else {
     np.setSearchedValue(resultSet.getString("lexical_value"));
     }
                
     while (resultSet.next()) {
     if(Integer.parseInt(resultSet.getString("ord")) < np.getIndexOfValue()) {
     np.setFirstColumn(np.getFirstColumn() + resultSet.getString("lexical_value"));
     } else if(Integer.parseInt(resultSet.getString("ord")) == np.getIndexOfValue()) {
     np.setSearchedValue(resultSet.getString("lexical_value"));
     } else {
     np.setLastColumn(np.getLastColumn() + resultSet.getString("lexical_value"));
     }
     }
     nodeSearchList.add(np);
     }
            
     } catch (SQLException ex) {
     Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
     }
        
     return nodeSearchList;
     }*/
    /**
     * FONCTION CAROLE Cette fonction permet récupérer la liste des identifiants
     * pour une recherche permutée avec leur numéro (ordre)
     *
     * @param ds
     * @param value
     * @param idLang
     * @param idThesaurus
     * @return
     */
    /*public ArrayList<Entry<String, String>> searchIdPermute(HikariDataSource ds, 
     String value, String idLang, String idThesaurus) {

     Connection conn;
     Statement stmt;
     ResultSet resultSet;
     ArrayList <Entry<String, String>> idList = null;
        
     try {
     conn = ds.getConnection();
     stmt = conn.createStatement();
     String query = "SELECT id_concept, ord FROM permuted WHERE"
     + " unaccent_string(term.lexical_value) ilike"
     + " unaccent_string('" + value + "%')"
     + " and id_thesaurus = '" + idThesaurus + "'"
     + " and id_lang = '" + idLang + "'"
     + " order by lexical_value ASC LIMIT 100";
            
     resultSet = stmt.executeQuery(query);
     idList = new ArrayList<>();
     Map temp = new HashMap();
     while (resultSet.next()) {
     temp.put(resultSet.getString("id_concept"), resultSet.getString("ord"));
     }
     idList.addAll(temp.entrySet());
            
     } catch (SQLException ex) {
     Logger.getLogger(SearchHelper.class.getName()).log(Level.SEVERE, null, ex);
     }
        
     return idList;
     }*/
 /*
     * Recherche Permutée
     */
    /**
     * Cette fonction permet de récupérer une liste des concepts sous forme
     * permutée : la première collonne est la première valeur non recherchée ou
     * vide, la deuxième colonne est la valeur recherchée et la troisième
     * colonne contient le reste du concept
     *
     * Retourne les synonymes aussi
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param value
     * @return ArrayList de NodePermute
     */
    public ArrayList<NodePermute> getListPermute(HikariDataSource ds,
            String idThesaurus, String idLang, String value) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodePermute> nodePermuteList = new ArrayList<>();
        value = new StringPlus().convertString(value);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT ord, id_concept, "
                            + " id_group, id_lang,"
                            + " lexical_value, ispreferredterm, original_value"
                            + " FROM permuted WHERE"
                            + " id_lang = '" + idLang + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and unaccent_string(lexical_value) ilike"
                            + " unaccent_string('" + value + "%')"
                            + " order by lexical_value ASC LIMIT 200";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();

                    while (resultSet.next()) {
                        NodePermute nodePermute = new NodePermute();
                        nodePermute.setIdThesaurus(idThesaurus);
                        nodePermute.setIdConcept(resultSet.getString("id_concept"));
                        nodePermute.setIdLang(resultSet.getString("id_lang"));
                        nodePermute.setIdGroup(resultSet.getString("id_group"));
                        nodePermute.setSearchedValue(resultSet.getString("lexical_value"));
                        nodePermute.setIndexOfValue(resultSet.getInt("ord"));
                        nodePermute.setIsPreferredTerm(resultSet.getBoolean("ispreferredterm"));

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(resultSet.getString("original_value").trim())) {
                            nodePermuteList.add(0, nodePermute);
                        } else {
                            nodePermuteList.add(nodePermute);
                        }
                    }
                    if (!nodePermuteList.isEmpty()) {
                        nodePermuteList = addColumnsToPermute(ds, idThesaurus, nodePermuteList);
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Permute of Value : " + value, sqle);
        }

        return nodePermuteList;
    }

    /**
     * Cette fonction permet de récupérer une liste des concepts sous forme
     * permutée par domaine
     *
     * renvoie les synonymes aussi
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param value
     * @param idGroup
     * @return ArrayList de NodePermute
     */
    public ArrayList<NodePermute> getListPermute(HikariDataSource ds,
            String idThesaurus, String idLang, String value, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodePermute> nodePermuteList = null;
        value = new StringPlus().convertString(value);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT ord, id_concept, "
                            + " id_lang, lexical_value, ispreferredterm, original_value"
                            + " FROM permuted WHERE"
                            + " id_lang = '" + idLang + "'"
                            + " and id_group = '" + idGroup + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and unaccent_string(lexical_value) ilike"
                            + " unaccent_string('" + value + "%')"
                            + " order by lexical_value ASC LIMIT 200";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    nodePermuteList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodePermute nodePermute = new NodePermute();
                        nodePermute.setIdThesaurus(idThesaurus);
                        nodePermute.setIdConcept(resultSet.getString("id_concept"));
                        nodePermute.setIdLang(resultSet.getString("id_lang"));
                        nodePermute.setIdGroup(idGroup);
                        nodePermute.setSearchedValue(resultSet.getString("lexical_value"));
                        nodePermute.setIndexOfValue(resultSet.getInt("ord"));
                        nodePermute.setIsPreferredTerm(resultSet.getBoolean("ispreferredterm"));

                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(resultSet.getString("original_value").trim())) {
                            nodePermuteList.add(0, nodePermute);
                        } else {
                            nodePermuteList.add(nodePermute);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Permute of Value by Group : " + value, sqle);
        }
        if (nodePermuteList != null) {
            nodePermuteList = addColumnsToPermute(ds, idThesaurus, nodePermuteList);
        }

        return nodePermuteList;
    }

    /**
     * Cette fonction permet de récupérer une liste des concepts sous forme
     * permutée par domaine
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param value
     * @param idGroup
     * @return ArrayList de NodePermute
     */
    public ArrayList<NodePermute> getListPermuteNonPreferredTerm(HikariDataSource ds,
            String idThesaurus, String idLang, String value, String idGroup) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodePermute> nodePermuteList = null;
        value = new StringPlus().convertString(value);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT DISTINCT ord, id_concept, "
                            + " id_lang, lexical_value"
                            + " FROM permuted WHERE"
                            + " id_lang = '" + idLang + "'"
                            + " and id_group = '" + idGroup + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and ispreferredterm = " + false
                            + " and unaccent_string(lexical_value) ilike"
                            + " unaccent_string('" + value + "%')"
                            + " order by lexical_value ASC LIMIT 200";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    nodePermuteList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodePermute nodePermute = new NodePermute();
                        nodePermute.setIdThesaurus(idThesaurus);
                        nodePermute.setIdConcept(resultSet.getString("id_concept"));
                        nodePermute.setIdLang(resultSet.getString("id_lang"));
                        nodePermute.setIdGroup(idGroup);
                        nodePermute.setSearchedValue(resultSet.getString("lexical_value"));
                        nodePermute.setIndexOfValue(resultSet.getInt("ord"));
                        nodePermute.setIsPreferredTerm(false);
                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodePermute.getSearchedValue().trim())) {
                            nodePermuteList.add(0, nodePermute);
                        } else {
                            nodePermuteList.add(nodePermute);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Permute of Value : " + value, sqle);
        }
        if (nodePermuteList != null) {
            nodePermuteList = addColumnsToPermute(ds, idThesaurus, nodePermuteList);
        }

        return nodePermuteList;
    }

    /**
     * Cette fonction permet de récupérer une liste des concepts Synonymes sous
     * forme permutée
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param value
     * @return ArrayList de NodePermute
     */
    public ArrayList<NodePermute> getListPermuteNonPreferredTerm(HikariDataSource ds,
            String idThesaurus, String idLang, String value) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodePermute> nodePermuteList = null;
        value = new StringPlus().convertString(value);

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT DISTINCT ord, id_concept, "
                            + " id_lang, lexical_value, id_group"
                            + " FROM permuted WHERE"
                            + " id_lang = '" + idLang + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and ispreferredterm = " + false
                            + " and unaccent_string(lexical_value) ilike"
                            + " unaccent_string('" + value + "%')"
                            + " order by lexical_value ASC LIMIT 200";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    nodePermuteList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodePermute nodePermute = new NodePermute();
                        nodePermute.setIdThesaurus(idThesaurus);
                        nodePermute.setIdConcept(resultSet.getString("id_concept"));
                        nodePermute.setIdLang(resultSet.getString("id_lang"));
                        nodePermute.setSearchedValue(resultSet.getString("lexical_value"));
                        nodePermute.setIndexOfValue(resultSet.getInt("ord"));
                        nodePermute.setIdGroup(resultSet.getString("id_group"));
                        nodePermute.setIsPreferredTerm(false);
                        //cas où le terme recherché est égal au terme retrouvé, on le place en premier
                        if (value.trim().equalsIgnoreCase(nodePermute.getSearchedValue().trim())) {
                            nodePermuteList.add(0, nodePermute);
                        } else {
                            nodePermuteList.add(nodePermute);
                        }
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting List Permute of Value : " + value, sqle);
        }
        if (nodePermuteList != null) {
            nodePermuteList = addColumnsToPermute(ds, idThesaurus, nodePermuteList);
        }
        return nodePermuteList;
    }

    /**
     * Cette fonction permet de récupérer la valeur d'origine du concept
     * réorganisé dans le bon ordre pour construire un tableau de 3 colonnes : 1
     * - le premier est le contenu avant la valeur recherchée 2 - le deuxième
     * contient le mot recherché 3 - le troisième contient le reste de la valeur
     * exp : saint clair du rhone si on cherche (clair) 1 = saint 2 = clair 3 =
     * du rhone
     *
     * @param ds
     * @param idThesaurus
     * @param idConcept
     * @param idLang
     * @param isPreferredTerm
     * @return ArrayList de String (les valeurs dans l'ordre)
     */
    public ArrayList<String> getThisConceptPermute(HikariDataSource ds,
            String idThesaurus, String idConcept, String idLang, boolean isPreferredTerm) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<String> tabValues = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "SELECT ord, lexical_value"
                            + " FROM permuted WHERE"
                            + " id_lang = '" + idLang + "'"
                            + " and id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept = '" + idConcept + "'"
                            + " and ispreferredterm = " + isPreferredTerm
                            + " order by ord ASC";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    tabValues = new ArrayList<>();
                    while (resultSet.next()) {
                        tabValues.add(resultSet.getString("lexical_value"));
                    }

                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting the value Permute of Concept : " + idConcept, sqle);
        }
        return tabValues;
    }

    /**
     * Cette fonction permet de compléter la colomne de gauche et la colonne de
     * droite avec les valeurs d'un concept Exp : Rhone Alpes Auvergne = colonne
     * de gauche = Rhone mot recherché = Alpes colonne de droite = Auvergne
     *
     * @param ds
     * @param idThesaurus
     * @param nodePermuteList
     * @return ArrayList de NodePermute
     */
    public ArrayList<NodePermute> addColumnsToPermute(HikariDataSource ds,
            String idThesaurus, ArrayList<NodePermute> nodePermuteList) {

        ArrayList<String> tabValues;
        String firstColumn = "";
        String lastColumn = "";

        for (NodePermute nodePermute : nodePermuteList) {
            tabValues = getThisConceptPermute(ds, idThesaurus,
                    nodePermute.getIdConcept(),
                    nodePermute.getIdLang(),
                    nodePermute.isIsPreferredTerm());
            if (tabValues != null) {
                for (int i = 0; i < nodePermute.getIndexOfValue() - 1; i++) {
                    firstColumn = firstColumn + " " + tabValues.get(i);
                }
                for (int i = nodePermute.getIndexOfValue(); i < tabValues.size(); i++) {
                    lastColumn = lastColumn + " " + tabValues.get(i);
                }
                nodePermute.setFirstColumn(firstColumn.trim());
                nodePermute.setLastColumn(lastColumn.trim());

            }
            firstColumn = "";
            lastColumn = "";

        }

        return nodePermuteList;
    }

    /**
     * Cette fonction permet de générer la table Permuted d'après les tables
     * PreferredTerm et NonPreferredTerm
     *
     * @param ds
     * @param idThesaurus
     * @return ArrayList de NodePermute
     */
    public boolean generatePermutedTable(HikariDataSource ds,
            String idThesaurus) {

        ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();
        ArrayList<NodeTermTraduction> nodeTermTraductionList;
        ArrayList<NodeEM> nodeEMList;

        // suppression des données de la table Permuted pour un thésaurus
        termHelper.deletePermutedTable(ds, idThesaurus);

        // Génération des Termes Préférés (PreferredTerm)
        ArrayList<String> tabIdConcept = conceptHelper.getAllIdConceptOfThesaurus(ds, idThesaurus);

        for (String idConcept : tabIdConcept) {
            nodeTermTraductionList = termHelper.getAllTraductionsOfConcept(ds, idConcept, idThesaurus);
            for (NodeTermTraduction nodeTermTraduction : nodeTermTraductionList) {
                // cette fonction permet de remplir la table Permutée
                termHelper.splitConceptForPermute(ds, idConcept,
                        new ConceptHelper().getGroupIdOfConcept(ds, idConcept, idThesaurus),
                        idThesaurus,
                        nodeTermTraduction.getLang(),
                        nodeTermTraduction.getLexicalValue());
            }
        }

        // Génération des Termes Synonymes (NonPreferredTerm)
        ArrayList<NodeTab2Levels> tabIdNonPreferredTerm = termHelper.getAllIdOfNonPreferredTerms(ds, idThesaurus);

        for (NodeTab2Levels nodeTab2Levels : tabIdNonPreferredTerm) {
            nodeEMList = termHelper.getAllNonPreferredTerms(ds, nodeTab2Levels.getIdConcept(), idThesaurus);
            for (NodeEM nodeEM : nodeEMList) {
                // cette fonction permet de remplir la table Permutée
                termHelper.splitConceptForNonPermuted(ds,
                        nodeTab2Levels.getIdConcept(),
                        new ConceptHelper().getGroupIdOfConcept(ds, nodeTab2Levels.getIdConcept(), idThesaurus),
                        idThesaurus,
                        nodeEM.getLang(),
                        nodeEM.getLexical_value());
            }
        }

        return true;
    }
}
