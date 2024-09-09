package fr.cnrs.opentheso.repositories;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import fr.cnrs.opentheso.models.nodes.NodeImage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class ExternalResourcesHelper {


    /**
     * Permet d'ajouter un lien vers une ressource externe lien de type URI
     */
    public boolean addExternalResource(HikariDataSource ds, String idConcept, String idThesausus, String description, String uri) {

        description = fr.cnrs.opentheso.utils.StringUtils.convertString(description);
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into external_resources (id_concept, id_thesaurus, description, "
                        + " external_uri) values ('"
                        + idConcept + "','" + idThesausus + "','" + description 
                        + "','" + uri + "')");
                return true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding resource of concept : " + idConcept, sqle);
            }
            return false;
        }
    }

    /**
     * Pemret de supprimer l'URI d'une image, donc la suppression de l'image
     * distante
     */
    public boolean deleteExternalResource(HikariDataSource ds, String idConcept, String idThesaurus, String uri) {
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from external_resources where id_thesaurus = '" + idThesaurus
                        + "' and id_concept  = '" + idConcept + "' and external_uri  = '" + uri + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while deleting external resource of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Permet de récupérer les URI des resources distantes qui sont liées au
     * concept
     */
    public ArrayList<NodeImage> getExternalResources(HikariDataSource ds, String idConcept, String idThesausus) {

        ArrayList<NodeImage> nodeImageList = null;

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from external_resources where id_concept = '"
                        + idConcept + "' and id_thesaurus = '" + idThesausus + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    nodeImageList = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeImage nodeImage = new NodeImage();
                        nodeImage.setIdConcept(resultSet.getString("id_concept"));
                        nodeImage.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeImage.setImageName(resultSet.getString("description"));
                        nodeImage.setUri(resultSet.getString("external_uri"));
                        nodeImageList.add(nodeImage);
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting resources of Concept : " + idConcept, sqle);
        }
        return nodeImageList;
    }
    
    /**
     * Change l'URI de la ressource externe liée au concept
     */
    public boolean setExternalResourceUri(HikariDataSource ds,
            String idTheso, String idConcept,
            String oldUri, String newUri, String description, int idUser){
        boolean status = false;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("update external_resources SET external_uri = '" + newUri + "'"
                        + ", id_user = " + idUser
                        + ", description = '" + description + "'"
                        + " WHERE id_concept = '" + idConcept + "'"
                        + " AND id_thesaurus = '" + idTheso + "'"
                        + " AND external_uri = '" + oldUri + "'"
                        );
                status = true;
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating external resource of Concept : " + idConcept, sqle);
        }
        return status;        
    }
}
