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
     *
     * Permet d'ajouter un lien vers une image externe lien de type URI
     *
     * @param ds
     * @param idConcept
     * @param idThesausus
     * @param imageName
     * @param copyRight
     * @param uri
     * @param idUser
     * @param creator
     * @return 
     */
    public boolean addExternalImage(HikariDataSource ds, String idConcept, String idThesausus,
            String imageName, String copyRight, String uri,String creator, int idUser) {

        boolean status = false;

        StringPlus stringPlus = new StringPlus();
        copyRight = stringPlus.convertString(copyRight);
        imageName = stringPlus.convertString(imageName);
        uri = uri.trim();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into external_images "
                        + "(id_concept, id_thesaurus, image_name, image_copyright, id_user, external_uri, image_creator)"
                        + " values ('" + idConcept + "','" + idThesausus + "','" + imageName + "'"
                        + ",'" + copyRight + "', " + idUser + ",'" + uri + "','" + creator + "')");
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
     * @param id
     * @param uri
     * @param copyRight
     * @param name
     * @param creator
     * @param idUser
     * @return
     */
    public boolean updateExternalImage(HikariDataSource ds, String idConcept, String idThesausus,
            int id, 
            String uri, String copyRight, String name, String creator, int idUser) {

        boolean status = false;

        StringPlus stringPlus = new StringPlus();
        uri = stringPlus.convertString(uri);

        name = stringPlus.convertString(name);
        copyRight = stringPlus.convertString(copyRight);
        creator = stringPlus.convertString(creator);        
        uri = uri.trim();

        try ( Connection conn = ds.getConnection()) {
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
     * Pemret de supprimer l'URI d'une image, donc la suppression de l'accès à
     * l'image distante
     * 
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param uri
     * @return 
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
     * Pemret de supprimer toutes les images, d'un concept
     * 
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return 
     */
    public boolean deleteAllExternalImage(HikariDataSource ds, String idConcept, String idThesaurus) {

        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
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
     * Permet de récupérer les URI des images distantes qui sont liées au
     * concept
    * 
    * @param ds
    * @param idConcept
    * @param idThesausus
    * @return 
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

    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Fin Nouvelles fontions #MR///////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////     
   


}
