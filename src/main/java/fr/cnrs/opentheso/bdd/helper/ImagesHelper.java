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

public class ImagesHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Nouvelles fontions #MR//////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////      
    /**
     * Permet d'ajouter un lien vers une image externe lien de type URI
     */
    public boolean addExternalImage(HikariDataSource ds, String idConcept, String idThesausus,
            String imageName, String copyRight, String uri, int idUser) {

        boolean status = false;

        StringPlus stringPlus = new StringPlus();
        copyRight = stringPlus.convertString(copyRight);
        imageName = stringPlus.convertString(imageName);
        uri = uri.trim();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into external_images "
                        + "(id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri)"
                        + " values ('" + idConcept + "','" + idThesausus + "','" + imageName + "'"
                        + ",'" + copyRight + "', " + idUser + ",'" + uri + "')");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding external image of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * permet de mettre à jour une Uri
     *
     * @param ds
     * @param idConcept
     * @param idThesausus
     * @param oldUri
     * @param newCopyRight
     * @param newUri
     * @param idUser
     * @return
     */
    public boolean updateExternalImage(HikariDataSource ds, String idConcept, String idThesausus,
            String oldUri, String newUri, String newCopyRight, int idUser) {

        boolean status = false;

        StringPlus stringPlus = new StringPlus();
        oldUri = stringPlus.convertString(oldUri);

        newUri = stringPlus.convertString(newUri);
        newCopyRight = stringPlus.convertString(newCopyRight);
        newUri = newUri.trim();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE external_images set image_copyright = '" + newCopyRight
                        + "', external_uri = '" + newUri + "' WHERE id_concept = '" + idConcept
                        + "' and id_thesaurus = '" + idThesausus + "' and external_uri = '" + oldUri
                        + "' and id_user = " + idUser);
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating external image of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Pemret de supprimer l'URI d'une image, donc la suppression de l'accès à
     * l'image distante
     */
    public boolean deleteExternalImage(HikariDataSource ds, String idConcept, String idThesaurus, String uri) {

        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
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
     * Permet de récupérer les URI des images distantes qui sont liées au
     * concept
     */
    public ArrayList<NodeImage> getExternalImages(HikariDataSource ds, String idConcept, String idThesausus) {
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
            log.error("Error while getting image of Concept : " + idConcept, sqle);
        }
        return nodeImageList;
    }

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Fin Nouvelles fontions #MR///////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////     
    /**
     * Cette fonction permet d'ajouter une définition note à un Terme à la table
     * Term, en paramètre un objet Classe Term
     */
    public boolean addImage(HikariDataSource ds, String idConcept, String idThesausus,
            String imageName, String copyRight, int idUser) {

        boolean status = false;
        copyRight = new StringPlus().convertString(copyRight);
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into images (id_concept, id_thesaurus, image_name, image_copyright)"
                        + " values ('" + idConcept + "','" + idThesausus + "','" + imageName + "'"
                        + ",'" + copyRight + "')");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding image of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter une définition note à un Terme à la table
     * Term, en paramètre un objet Classe Term
     */
    public ArrayList<NodeImage> getImages(HikariDataSource ds, String idConcept, String idThesausus) {

        ArrayList<NodeImage> nodeImageList = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from images where id_concept = '" + idConcept + "' and id_thesaurus = '"
                        + idThesausus + "'");
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
            log.error("Error while adding image of Concept : " + idConcept, sqle);
        }
        return nodeImageList;
    }

    /**
     * Cette fonction permet de supprimer un Terme avec toutes les dépendances
     * (Prefered term dans toutes les langues) et (nonPreferedTerm dans toutes
     * les langues)
     */
    public boolean deleteImage(HikariDataSource ds, String idConcept, String idThesaurus, String imageName) {

        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from images where id_thesaurus = '" + idThesaurus + "' and id_concept  = '"
                        + idConcept + "' and image_name  = '" + imageName + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting Image of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Change l'id d'un concept dans la table images
     */
    public void setIdConceptImage(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        try ( Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE images SET id_concept = '" + newIdConcept + "' WHERE id_concept = '"
                    + idConcept + "' AND id_thesaurus = '" + idTheso + "'");
        }
    }

    /**
     * Change l'id d'un concept dans la table images
     */
    public void setIdConceptExternalImages(Connection conn, String idTheso, String idConcept, String newIdConcept)
            throws SQLException {
        try ( Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE external_images SET id_concept = '" + newIdConcept + "' WHERE id_concept = '"
                    + idConcept + "' AND id_thesaurus = '" + idTheso + "'");
        }
    }

}
