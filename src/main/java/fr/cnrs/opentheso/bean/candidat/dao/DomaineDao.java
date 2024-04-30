package fr.cnrs.opentheso.bean.candidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.candidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.Connection;
import java.sql.ResultSet;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class DomaineDao extends BasicDao {
    
    public List<DomaineDto> getAllDomaines(HikariDataSource ds, String idThesaurus, String lang) {
        List<DomaineDto> domaines = new ArrayList<>();
        Connection conn;
        Statement stmt1;
        ResultSet resultSet1;

        try {
            conn = ds.getConnection();
            try {
                stmt1 = conn.createStatement();
                try {
                    String query = "SELECT idgroup, lexicalvalue FROM concept_group_label where idthesaurus = '"
                                + idThesaurus+"' AND lang = '"+lang+"'";

                    stmt1.executeQuery(query);
                    resultSet1 = stmt1.getResultSet();
                    while (resultSet1.next()) {
                        DomaineDto domaineDto = new DomaineDto();
                        domaineDto.setId(resultSet1.getString("idgroup"));
                        domaineDto.setName(resultSet1.getString("lexicalvalue"));
                        domaines.add(domaineDto);
                    }
                } finally {
                    stmt1.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            LOG.error(sqle);
        }        
        return domaines;
    }

    public void addNewDomaine(Connect connect, String idgroup, String idthesaurus, String idconcept) throws SQLException {
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        executInsertRequest(stmt,"INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES ('"
                +idgroup+"', '"+idthesaurus+"', '"+idconcept+"')");
        stmt.close();
    }
    
    public void deleteAllDomaine(Connect connect, String idThesaurus, String idConcept) throws SQLException {
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        executDeleteRequest(stmt,"DELETE FROM concept_group_concept WHERE idconcept = '"+idConcept+"'  AND idthesaurus = '"+idThesaurus+"'");
        stmt.close();
    }

    public void deleteDomaine(Connect connect, String idThesaurus, String idConcept, String idGroupe) throws SQLException {
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        executDeleteRequest(stmt,"DELETE FROM concept_group_concept " +
                "WHERE idconcept = '"+idConcept+"' AND idthesaurus = '"+idThesaurus+"' AND idgroup='"+idGroupe+"'");
        stmt.close();
    }

    public void updateDomaine(Connect connect, String oldIdgroup, String newIdgroup, String idthesaurus, String idconcept) throws SQLException {

        stmt = connect.getPoolConnexion().getConnection().createStatement();

        if (!StringUtils.isEmpty(newIdgroup)) {
            executInsertRequest(stmt,"INSERT INTO concept_group_concept(idgroup, idthesaurus, idconcept) VALUES ('"
                    +newIdgroup+"', '"+idthesaurus+"', '"+idconcept+"')");
        } else {
            executDeleteRequest(stmt,"DELETE FROM concept_group_concept WHERE idconcept = '"+idconcept+"' AND idgroup = '"
                    +oldIdgroup+"' AND idthesaurus = '"+idthesaurus+"'");
        }

        stmt.close();
    }

    public ArrayList<NodeIdValue> getDomaineCandidatByConceptAndThesaurusAndLang(HikariDataSource hikariDataSource, String idconcept,
                                                              String idThesaurus, String lang) {
        
        GroupHelper groupHelper = new GroupHelper();
        ArrayList<NodeGroup> nodeGroups = groupHelper.getListGroupOfConcept(hikariDataSource, idThesaurus, idconcept, lang);
        
        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        for (NodeGroup nodeGroup : nodeGroups) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setValue(nodeGroup.getLexicalValue());
            nodeIdValue.setId(nodeGroup.getConceptGroup().getIdgroup());
            nodeIdValues.add(nodeIdValue);
        }
        return nodeIdValues;
    }
    
}
