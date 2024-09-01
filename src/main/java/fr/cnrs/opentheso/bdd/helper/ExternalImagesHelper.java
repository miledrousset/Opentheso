package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExternalImagesHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Nouvelles fontions #MR//////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////      
    /**
     * Permet d'ajouter un lien vers une image externe lien de type URI
    * 
    * @param ds
    * @param idConcept
    * @param idThesausus
    * @param imageName
    * @param copyRight
    * @param uri
     * @param creator
    * @param idUser
    * @return 
    */ 
    public boolean addExternalImage(HikariDataSource ds, String idConcept, String idThesausus,
            String imageName, String copyRight, String uri, String creator, int idUser) {

        boolean status = false;

        copyRight = fr.cnrs.opentheso.utils.StringUtils.convertString(copyRight);
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into external_images (id_concept, id_thesaurus, image_name, "
                        + "image_copyright, external_uri, image_creator) values ('"
                        + idConcept + "','" + idThesausus + "','" + imageName + "','"
                        + copyRight + "','" + uri + "','" + creator + "')");
                status = true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Image of concept : " + idConcept, sqle);
                //System.out.println(sqle.toString());
                return false;
            } else {
                return true;
            }
        }
        return status;
    }

    /**
     * Pemret de supprimer l'URI d'une image, donc la suppression de l'image
     * distante
     */
    public boolean deleteExternalImage(HikariDataSource ds, String idConcept, String idThesaurus, String uri) {

        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from external_images where id_thesaurus = '" + idThesaurus
                        + "' and id_concept  = '" + idConcept + "' and external_uri  = '" + uri + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting external Image of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Permet de récupérer les URI des images distantes qui sont liées au
     * concept
     */
    public ArrayList<NodeImage> getExternalImages(HikariDataSource ds,
            String idConcept, String idThesausus) {

        ArrayList<NodeImage> nodeImageList = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from external_images where id_concept = '"
                        + idConcept + "' and id_thesaurus = '" + idThesausus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    nodeImageList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeImage nodeImage = new NodeImage();
                        nodeImage.setIdConcept(resultSet.getString("id_concept"));
                        nodeImage.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeImage.setImageName(resultSet.getString("image_name"));
                        nodeImage.setCopyRight(resultSet.getString("image_copyright"));
                        nodeImage.setUri(resultSet.getString("external_uri"));
                        nodeImageList.add(nodeImage);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting image of Concept : " + idConcept, sqle);
        }
        return nodeImageList;
    }

    /**
     * Change l'id d'un concept dans la table images suite à un changement
     * d'identifiant pour un concept pour ne pas perdre le lien
     */
    public void setIdConceptExternalImage(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        try ( Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE external_images SET id_concept = '" + newIdConcept
                    + "' WHERE id_concept = '" + idConcept + "' AND id_thesaurus = '" + idTheso + "'");
        }
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Fin Nouvelles fontions #MR///////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////     
}
