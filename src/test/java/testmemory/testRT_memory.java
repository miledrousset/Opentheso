/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testmemory;


import connexion.ConnexionTest;
import fr.cnrs.opentheso.models.search.NodeSearchMini;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.utils.StringUtils;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

/**
 *
 * @author miledrousset
 */
public class testRT_memory {

    public testRT_memory() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void searchValue() {
        ConnexionTest connexionTest = new ConnexionTest();
        DataSource ds = connexionTest.getConnexionPool();

        String value = "vase";
        String idLang = "fr";
        String idTheso = "TH_1";


        List<NodeSearchMini> liste ;
        for (int i = 0; i < 10000; i++) {
            liste  = new ArrayList<>();
            liste = searchValue__(ds, value, idLang, idTheso, liste);
            liste.clear();
            liste = null;
        }
        String o = "";
    }

    private List<NodeSearchMini> searchValue__(DataSource ds, String value, String idLang, String idTheso, List<NodeSearchMini> liste) {
        return  searchAutoCompletionForRelation(
                ds,
                value,
                idLang,
                idTheso, liste);
    }

    
    /**
     * Cette fonction permet de récupérer une liste de termes pour
     * l'autocomplétion pour créer des relations entre les concepts
     *
     * @param value
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public List<NodeSearchMini> searchAutoCompletionForRelation(
            DataSource dataSource,
            String value,
            String idLang,
            String idTheso,
            List<NodeSearchMini> nodeSearchMinis) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;

        value = StringUtils.convertString(value);
        value = StringUtils.unaccentLowerString(value);

        try {
            // Get connection from pool
            conn = dataSource.getConnection();
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
                            + " AND f_unaccent(lower(term.lexical_value)) LIKE '%" + value + "%' order by term.lexicalValue <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();

                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("lexical_value"));
                        nodeSearchMini.setAltLabel(false);
                        nodeSearchMinis.add(nodeSearchMini);
                    }
                    resultSet.close();
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
                            + " order by non_preferred_term.lexicalValue <-> '" + value + "' limit 20";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeSearchMini nodeSearchMini = new NodeSearchMini();

                        nodeSearchMini.setIdConcept(resultSet.getString("id_concept"));
                        nodeSearchMini.setAltLabelValue(resultSet.getString("npt"));
                        nodeSearchMini.setPrefLabel(resultSet.getString("pt"));
                        nodeSearchMini.setAltLabel(true);
                        nodeSearchMinis.add(nodeSearchMini);
                    }
                     resultSet.close();
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            
        }

        return nodeSearchMinis;
    }    
    
}
