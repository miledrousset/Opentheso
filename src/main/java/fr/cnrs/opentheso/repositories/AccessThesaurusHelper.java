package fr.cnrs.opentheso.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import fr.cnrs.opentheso.models.thesaurus.Thesaurus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;


@Slf4j
@Service
public class AccessThesaurusHelper {

    @Autowired
    private DataSource dataSource;
    
    /**
     * permet de mettre à jour la visibilité du thésaurus en publique ou privé 
     *
     * @param idTheso
     * @param isPrivate
     * @return 
     * #MR
     */
    public boolean updateVisibility(String idTheso, boolean isPrivate) {
        
        Statement stmt = null;
        boolean status = false;
        try {
            Connection conn = dataSource.getConnection();
            try {
                stmt = conn.createStatement();
                String query = "UPDATE thesaurus SET private = " +  isPrivate + " WHERE id_thesaurus='" + idTheso + "'";
                stmt.executeUpdate(query);
                status = true;
            } finally {
                if (stmt != null) stmt.close();
                conn.close();
            }
        } catch (SQLException ex) {
            log.error("error while updating visibility of thesaurus = " +idTheso, ex);
        }
        return status;        
    }

    /**
     * Fonction getAThesaurus
     * #JM
     * permet de récupérer un thésaurus d'après son identifiant
     * @param idTheso
     * @param idLang
     * @return 
     */
    public Thesaurus getAThesaurus(String idTheso,String idLang){
        
        Thesaurus th =new Thesaurus();
        
        try (Connection conn = dataSource.getConnection()) {
            
            try (PreparedStatement stmt = conn.prepareStatement("SELECT *  FROM thesaurus INNER JOIN thesaurus_label ON thesaurus.id_thesaurus=thesaurus_label.id_thesaurus WHERE thesaurus.id_thesaurus=? AND lang=?")) {
                
                stmt.setString(1,idTheso);
                stmt.setString(2,idLang);

                try (ResultSet rs=stmt.executeQuery()) {
                    if(rs.next()){
                        th.setContributor(rs.getString("contributor"));
                        th.setCoverage(rs.getString("coverage"));
                        th.setCreated(rs.getDate("created"));
                        th.setCreator(rs.getString("creator"));
                        th.setDescription(rs.getString("description"));
                        th.setFormat(rs.getString("format"));
                        th.setId_ark(rs.getString("id_ark"));
                        th.setId_thesaurus(rs.getString("id_thesaurus"));
                        th.setLanguage(rs.getString("lang"));
                        th.setModified(rs.getDate("modified"));
                        th.setPublisher(rs.getString("publisher"));
                        th.setRelation(rs.getString("relation"));
                        th.setRights(rs.getString("rights"));
                        th.setSource(rs.getString("source"));
                        th.setSubject(rs.getString("subject"));
                        th.setTitle(rs.getString("title"));
                        th.setType(rs.getString("type"));
                        th.setPrivateTheso(rs.getBoolean("private"));

                    } else{
                        log.error("la requete n'a pas trouver de thesaurus associé à l'idenitifiant");
                    }
                }
            }
        }
        catch(SQLException e){
            log.error("error while retrieving a thesaurus from the bdd "+th.toString(),e);
        }
        
        return th;
    }
  
}
