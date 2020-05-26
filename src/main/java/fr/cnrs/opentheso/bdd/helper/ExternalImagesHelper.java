package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class ExternalImagesHelper {
    private final Log log = LogFactory.getLog(ThesaurusHelper.class);
    public ExternalImagesHelper() {
        
    }
    
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////// Nouvelles fontions #MR//////////////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////      
    
    /**
     * Permet d'ajouter un lien vers une image externe 
     * lien de type URI
     *
     * @param ds
     * @param idConcept
     * @param imageName
     * @param idThesausus
     * @param copyRight
     * @param uri
     * @param idUser
     * @return
     */
    public boolean addExternalImage(HikariDataSource ds,
            String idConcept, String idThesausus,
            String imageName, String copyRight,
            String uri, int idUser) {


        Connection conn;
        Statement stmt;
        boolean status = false;

        copyRight = new StringPlus().convertString(copyRight);
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "Insert into external_images "
                            + "(id_concept, id_thesaurus, image_name, image_copyright, external_uri)"
                            + " values ("
                            + "'" + idConcept + "'"
                            + ",'" + idThesausus + "'"
                            + ",'" + imageName + "'"
                            + ",'" + copyRight + "'"
                            + ",'" + uri + "')";
                    stmt.executeUpdate(query);
                    status = true;
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Image of concept : " + idConcept, sqle);
                //System.out.println(sqle.toString());
                return false;
            } else
                return true;
        }
        return status;
    }      
    
    /**
     * Pemret de supprimer l'URI d'une image, donc la suppression de l'image distante
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param uri
     * @return
     */
    public boolean deleteExternalImage(HikariDataSource ds,
            String idConcept, String idThesaurus,
            String uri) {
        
        Connection conn;
        Statement stmt;
        boolean status = false;
        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "delete from external_images where"
                            + " id_thesaurus = '" + idThesaurus + "'"
                            + " and id_concept  = '" + idConcept + "'"
                            + " and external_uri  = '" + uri + "'";
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
            log.error("Error while deleting external Image of Concept : " + idConcept, sqle);
        }
        return status;
    }      
    
    
    /**
     * Permet de récupérer les URI des images distantes qui sont liées au concept
     *
     * @param ds
     * @param idConcept
     * @param idThesausus
     * @return
     */
    public ArrayList <NodeImage> getExternalImages(HikariDataSource ds,
            String idConcept, String idThesausus) {

        Connection conn;
        Statement stmt;
        ResultSet resultSet;
        ArrayList <NodeImage> nodeImageList = null;

        try {
            // Get connection from pool
            conn = ds.getConnection();
            try {
                stmt = conn.createStatement();
                try {
                    String query = "select * from external_images where"
                            + " id_concept = '" + idConcept + "'"
                            + " and id_thesaurus = '" + idThesausus + "'";
                    
                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    nodeImageList = new ArrayList <>();
                    while (resultSet.next()) {
                        NodeImage nodeImage = new NodeImage();
                        nodeImage.setIdConcept(resultSet.getString("id_concept"));
                        nodeImage.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeImage.setImageName(resultSet.getString("image_name"));
                        nodeImage.setCopyRight(resultSet.getString("image_copyright"));
                        nodeImage.setUri(resultSet.getString("external_uri"));                        
                        nodeImageList.add(nodeImage);
                    }
                } finally {
                    stmt.close();
                }
            } finally {
                conn.close();
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting image of Concept : " + idConcept, sqle);
        }
        return nodeImageList;
    }
    
    
    /**
     * Change l'id d'un concept dans la table images
     * suite à un changement d'identifiant pour un concept pour ne pas perdre le lien
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @param newIdConcept
     * @throws SQLException
     */
    public void setIdConceptExternalImage(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        Statement stmt;
        stmt = conn.createStatement();
        try {
            String query = "UPDATE external_images"
                    + " SET id_concept = '" + newIdConcept + "'"
                    + " WHERE id_concept = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
        } finally {
            stmt.close();
        }
    }    
    
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////// Fin Nouvelles fontions #MR///////////////////////
 ////////////////////////////////////////////////////////////////////
 ////////////////////////////////////////////////////////////////////     
    
    
}
