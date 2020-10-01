/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package candidate;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.CandidateHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeCandidateOld;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeTraductionCandidat;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
@Ignore
public class getOldCandidateTest {
    
    public getOldCandidateTest() {
    }


    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void exportOldCandidatesToSkos() {
        String idTheso = "TH_1";
        ArrayList<NodeCandidateOld> nodeCandidateOlds = getCandidates(idTheso);
        
    }
     
    private ArrayList<NodeCandidateOld> getCandidates(String idTheso){
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();        

        
        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList<NodeCandidateOld> nodeCandidateOlds = new ArrayList<>();

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select concept_candidat.id_concept, concept_candidat.status"
                            + " from concept_candidat"
                            + " where"
                            + " concept_candidat.id_thesaurus = '" + idTheso +"'"
                            + " and concept_candidat.status = 'a'";
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    while (resultSet.next()) {
                        NodeCandidateOld nodeCandidateOld = new NodeCandidateOld();
                        nodeCandidateOld.setIdCandidate(resultSet.getString("id_concept"));
                        nodeCandidateOld.setStatus(resultSet.getString("status"));
                        nodeCandidateOlds.add(nodeCandidateOld);
                    }
                    
                    for (NodeCandidateOld nodeCandidateOld : nodeCandidateOlds) {
                        ArrayList<NodeTraductionCandidat> nodeTraductionCandidats = new ArrayList<>();                        
                        query = "select term_candidat.lexical_value, term_candidat.lang"
                                + " from concept_term_candidat, term_candidat"
                                + " where"
                                + " concept_term_candidat.id_term = term_candidat.id_term"
                                + " and"
                                + " concept_term_candidat.id_thesaurus = term_candidat.id_thesaurus"
                                + " and"
                                + " term_candidat.id_thesaurus = '" + idTheso + "'"
                                + " and"
                                + " concept_term_candidat.id_concept = '" + nodeCandidateOld.getIdCandidate() + "'";
                        stmt.executeQuery(query);
                        resultSet = stmt.getResultSet();
                        while (resultSet.next()) {
                            NodeTraductionCandidat nodeTraductionCandidat  = new NodeTraductionCandidat();
                            nodeTraductionCandidat.setIdLang(resultSet.getString("lang"));
                            nodeTraductionCandidat.setTitle(resultSet.getString("lexical_value"));
                            nodeTraductionCandidats.add(nodeTraductionCandidat);
                        }
                        nodeCandidateOld.setNodeTraductions(nodeTraductionCandidats);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {

        }
        return nodeCandidateOlds;
    }
    
    
    @Test
    public void exportCandidatsToCSV() {
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource conn = connexionTest.getConnexionPool();

        String idTheso = "TH_1";
 //       String idLang = "fr";

        ArrayList<String> tabIdCandidats;
        boolean passed = false;
        StringBuilder file = new StringBuilder();
        
        CandidateHelper candidateHelper = new CandidateHelper();
        tabIdCandidats = candidateHelper.getAllCandidatId(conn, idTheso);
        
        if(tabIdCandidats != null){
            if(!tabIdCandidats.isEmpty()) {
                file.append("id_candidat");
                file.append("\t");
                file.append("titre");
                file.append("\t");
                file.append("langue");
                file.append("\t");
                file.append("notes contributeurs");
                file.append("\t");
                file.append("status");
                file.append("\t");
                file.append("message de l'administrateur");
                file.append("\t");
                file.append("id_concept");
                file.append("\t");                
                file.append("date création");
                file.append("\t");
                file.append("date modification");
                file.append("\n");
                for (String tabIdCandidat : tabIdCandidats) {
                    
                    file.append(tabIdCandidat);
                    file.append("\t");
                    file.append(" ");
                    file.append("\t");
                    file.append(" ");
                    file.append("\t");
                    file.append(" ");
                    file.append("\t");
                    file.append(" ");
                    file.append("\t");
                    file.append(" ");
                    file.append("\t");
                    file.append(" ");
                    file.append("\t");
                    file.append(" ");
                    file.append("\t");
                    file.append(" ");
                    file.append("\n");                    
                }
            }
        }
        System.out.println(file.toString());
        conn.close();
    }    
    
}
