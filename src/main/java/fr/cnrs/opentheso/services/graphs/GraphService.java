package fr.cnrs.opentheso.services.graphs;

import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.models.graphs.GraphObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.context.SessionScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;

/**
 *
 * @author miledrousset
 */
@Slf4j
@Named
@SessionScoped
public class GraphService implements Serializable {

    @Autowired
    private DataSource dataSource;

    @Autowired @Lazy
    private CurrentUser currentUser;


    /**
     * permet de récupérer les informations complètes 
     * pour toutes les vues d'un utilisateur
     * @return 
     */
    public Map<Integer, GraphObject> getViews() {
        if (currentUser == null || currentUser.getNodeUser().getIdUser() == -1) {
            return null;
        }
        Map<Integer, GraphObject> graphviews = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from graph_view"
                        + " where "
                        + " graph_view.id_user = " + currentUser.getNodeUser().getIdUser());
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString("name");
                        String description = resultSet.getString("description");
                        int id = resultSet.getInt("id");
                        graphviews.put(id, new GraphObject(id, name, description));

                        GraphObject graphObject = graphviews.get(id);
                        setObject(graphObject);
                    }
                }
            }
        } catch (SQLException e) {
        }
        return graphviews;
    }
    private void setObject(GraphObject graphObject) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from graph_view_exported_concept_branch"
                        + " where "
                        + " graph_view_id = " + graphObject.getId());
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        String thesaurusId = resultSet.getString("top_concept_thesaurus_id");
                        String conceptId = resultSet.getString("top_concept_id");
                        if (thesaurusId != null) {
                            graphObject.getExportedData().add(new ImmutablePair<>(thesaurusId, conceptId));
                        }
                    }
                }
            }
        } catch (SQLException e) {

        }
    }    
    
    

    public GraphObject getView(String id) {
        GraphObject view = null;
        try (var con = dataSource.getConnection()){
            try (PreparedStatement stmt = con.prepareStatement("SELECT * FROM public.graph_view as view_data LEFT JOIN  public.graph_view_exported_concept_branch as view_concepts ON view_data.id = view_concepts.graph_view_id WHERE view_data.id = ?")) {
                stmt.setInt(1, Integer.parseInt(id));
                ResultSet resultView = stmt.executeQuery();

                while (resultView.next()) {
                    if (view == null) {
                        int dataId = resultView.getInt("id");
                        String name = resultView.getString("name");
                        String description = resultView.getString("description");
                        view = new GraphObject(dataId, name, description);
                    }
                    String thesaurusId = resultView.getString("top_concept_thesaurus_id");
                    String conceptId = resultView.getString("top_concept_id");
                    if (thesaurusId != null) {
                        view.getExportedData().add(new ImmutablePair<>(thesaurusId, conceptId));
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e.toString());
        }

        return view;
    }

    public void deleteView(String id) {
        try (var con = dataSource.getConnection()){
            PreparedStatement stmt1 = con.prepareStatement("DELETE from public.graph_view WHERE id= ?");
            stmt1.setInt(1, Integer.parseInt(id));

            PreparedStatement stmt2 = con.prepareStatement("DELETE from public.graph_view_exported_concept_branch WHERE graph_view_id= ?");
            stmt2.setInt(1, Integer.parseInt(id));

            stmt1.executeUpdate();
            stmt2.executeUpdate();
            stmt2.close();
            stmt1.close();
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }

    public boolean saveView(GraphObject view) {
        boolean result = false;
        try (Connection con = dataSource.getConnection()){
            PreparedStatement stmt = con.prepareStatement("UPDATE public.graph_view SET name = ?, description = ? WHERE id = ?");
            stmt.setString(1, view.getName());
            stmt.setString(2, view.getDescription());
            stmt.setInt(3, view.getId());
            int queryResult = stmt.executeUpdate();
            if (queryResult > 0) {
                result = true;
            }
            stmt.close();
        } catch (SQLException e) {
            log.error(e.toString());
        }

        return result;
    }

    public int createView(GraphObject view) {
        int id = -1;
        try (var con = dataSource.getConnection()){
            PreparedStatement stmt = con.prepareStatement("INSERT INTO public.graph_view (name, description, id_user) VALUES (?, ?, ?) returning id;");
            stmt.setString(1, view.getName());
            stmt.setString(2, view.getDescription());
            stmt.setInt(3, currentUser.getNodeUser().getIdUser());
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                id = result.getInt("id");
              
            }
            stmt.close();
        } catch (SQLException e) {
            log.error(e.toString());
            System.err.println("Error >>> " + e);
        }

        return id;
    }

    /**
     * Permet d'ajouter les informations d'un thésaurus ou branche à la BDD
     * @param selectedViewId
     * @param tuple
     * @return 
     */
    public int addDataToView(int selectedViewId, ImmutablePair<String, String> tuple) {
        int id = -1;
        String concept = tuple.right == null ? null : "'" + tuple.right + "'";
        try (Connection conn = dataSource.getConnection()){
            try(Statement stmt = conn.createStatement()){
                stmt.executeQuery("INSERT INTO graph_view_exported_concept_branch (graph_view_id, top_concept_id, top_concept_thesaurus_id) "
                        + " VALUES (" 
                        + selectedViewId
                        + "," + concept 
                        + ",'" + tuple.left + "')"
                        + " returning id;");
                try(ResultSet resultSet = stmt.getResultSet()){
                    if(resultSet.next()){
                        id = resultSet.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e.toString());
            System.err.println("Error >>> " + e);
        }
        return id;
    }

    /**
     * vérifie si la combinaison de vue existe ou pas
     * @param viewId
     * @param idTheso
     * @param idConcept
     * @return 
     */
    public boolean isExistDatas(int viewId, String idTheso, String idConcept) {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {

                if (StringUtils.isEmpty(idConcept)) {
                    stmt.executeQuery("select id from graph_view_exported_concept_branch"
                            + " where "
                            + " graph_view_id = " + viewId
                            + " and"
                            + " top_concept_id ISNULL"
                            + " and top_concept_thesaurus_id = '" + idTheso + "'"
                    );
                } else {
                    stmt.executeQuery("select id from graph_view_exported_concept_branch"
                            + " where "
                            + " graph_view_id = " + viewId
                            + " and"
                            + " top_concept_id = '" + idConcept + "'"
                            + " and top_concept_thesaurus_id = '" + idTheso + "'"
                    );
                }
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        resultSet.getInt("id");
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(GraphService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public void removeDataFromView(int selectedViewId, ImmutablePair<String, String> tuple) {

        try (var con = dataSource.getConnection();){
            PreparedStatement stmt;
            if (tuple.right == null) {
                stmt = con.prepareStatement("DELETE FROM public.graph_view_exported_concept_branch WHERE graph_view_id = ? AND top_concept_thesaurus_id = ?");
                stmt.setInt(1, selectedViewId);
                stmt.setString(2, tuple.left);
            } else {
                stmt = con.prepareStatement("DELETE FROM public.graph_view_exported_concept_branch WHERE graph_view_id = ? AND top_concept_id = ? AND top_concept_thesaurus_id = ?");
                stmt.setInt(1, selectedViewId);
                stmt.setString(2, tuple.right);
                stmt.setString(3, tuple.left);
            }
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            log.error(e.toString());
        }
    }
}
