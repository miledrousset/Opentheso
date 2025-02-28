package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import fr.cnrs.opentheso.models.nodes.NodeImage;

import fr.cnrs.opentheso.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Slf4j
@Service
public class ImagesHelper {

    @Autowired
    private DataSource dataSource;


    /**
     * Permet d'ajouter un lien vers une image externe lien de type URI
     */
    public boolean addExternalImage(String idConcept, String idThesausus,
            String imageName, String copyRight, String uri, String creator, int idUser) {
        
        copyRight = StringUtils.convertString(copyRight);
        imageName = StringUtils.convertString(imageName);
        creator = StringUtils.convertString(creator);
        uri = uri.trim();
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into external_images "
                        + "(id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri, image_creator)"
                        + " values ('" + idConcept + "','" + idThesausus + "','" + imageName + "'"
                        + ",'" + copyRight + "', " + idUser + ",'" + uri + "','" + creator + "')");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding external image of Concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * permet de mettre à jour une Uri
     */
    public boolean updateExternalImage(String idConcept, String idThesausus, int id,
            String uri, String copyRight, String name, String creator) {

        boolean status = false;
        
        uri = StringUtils.convertString(uri);

        name = StringUtils.convertString(name);
        copyRight = StringUtils.convertString(copyRight);
        creator = StringUtils.convertString(creator);
        uri = uri.trim();

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE external_images set "
                        + " image_copyright = '" + copyRight
                        + "', external_uri = '" + uri + "'"
                        + ", image_name = '" + name + "'"
                        + ", image_creator = '" + creator + "'"
                        + " WHERE id_concept = '" + idConcept + "'"
                        + " and id_thesaurus = '" + idThesausus + "'"
                        + " and id = " + id);
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating external image of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Pemret de supprimer l'URI d'une image, donc la suppression de l'accès à l'image distante
     */
    public boolean deleteExternalImage(String idConcept, String idThesaurus, String uri) {

        boolean status = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from external_images where id_thesaurus = '" + idThesaurus + "'"
                        + " and id_concept  = '" + idConcept + "' and external_uri  = '" + uri + "'");
                status = true;

            }
        } catch (SQLException sqle) {
            log.error("Error while deleting external Image of Concept : " + idConcept, sqle);
        }
        return status;
    }
    
    /**
     * Pemret de supprimer toutes les images, d'un concept
     */
    public boolean deleteAllExternalImage(String idConcept, String idThesaurus) {

        boolean status = false;
        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from external_images where id_thesaurus = '" + idThesaurus + "'"
                        + " and id_concept  = '" + idConcept + "'");
                status = true;

            }
        } catch (SQLException sqle) {
            log.error("Error while deleting external Image of Concept : " + idConcept, sqle);
        }
        return status;
    }    

    /**
     * Permet de récupérer les URI des images distantes qui sont liées au concept
    */
    public ArrayList<NodeImage> getExternalImages(String idConcept, String idThesausus) {

        ArrayList<NodeImage> nodeImageList = null;

        try ( Connection conn = dataSource.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from external_images where id_concept = '"
                        + idConcept + "' and id_thesaurus = '" + idThesausus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    nodeImageList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeImage nodeImage = new NodeImage();
                        nodeImage.setId(resultSet.getInt("id"));
                        nodeImage.setIdConcept(resultSet.getString("id_concept"));
                        nodeImage.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeImage.setImageName(resultSet.getString("image_name"));
                        nodeImage.setCopyRight(resultSet.getString("image_copyright"));
                        nodeImage.setCreator(resultSet.getString("image_creator"));                        
                        nodeImage.setUri(resultSet.getString("external_uri"));
                        nodeImageList.add(nodeImage);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting image of Concept : " + idConcept, sqle);
        }
        return nodeImageList;
    }
}
