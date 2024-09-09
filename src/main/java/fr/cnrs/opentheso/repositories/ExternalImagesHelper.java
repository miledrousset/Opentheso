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
public class ExternalImagesHelper {

    /**
     * Permet d'ajouter un lien vers une image externe lien de type URI
    */ 
    public boolean addExternalImage(HikariDataSource ds, String idConcept, String idThesausus,
            String imageName, String copyRight, String uri, String creator) {

        copyRight = fr.cnrs.opentheso.utils.StringUtils.convertString(copyRight);
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into external_images (id_concept, id_thesaurus, image_name, "
                        + "image_copyright, external_uri, image_creator) values ('"
                        + idConcept + "','" + idThesausus + "','" + imageName + "','"
                        + copyRight + "','" + uri + "','" + creator + "')");
                return true;
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
}
