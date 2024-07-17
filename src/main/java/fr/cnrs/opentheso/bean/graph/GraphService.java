
package fr.cnrs.opentheso.bean.graph;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author miledrousset
 */

@Named
@SessionScoped
public class GraphService implements Serializable{

    @Inject
    private Connect connect;

    private final Log LOG = LogFactory.getLog(GraphService.class);

    public Map<Integer, GraphObject> getViews() {
        HikariDataSource ds = connect.getPoolConnexion();
        Map<Integer, GraphObject> graphviews = new HashMap();
        try {
            Connection con = ds.getConnection();
            ResultSet resultViews = con.prepareStatement("SELECT * FROM public.graph_view as view_data LEFT JOIN  public.graph_view_exported_concept_branch as view_concepts ON view_data.id = view_concepts.graph_view_id").executeQuery();

            while(resultViews.next()){
                int id = resultViews.getInt("id");
                GraphObject graphObject = null;
                if(!graphviews.containsKey(id)) {
                    String name = resultViews.getString("name");
                    String description = resultViews.getString("description");
                    graphviews.put(id, new GraphObject(id, name, description));
                }

                graphObject = graphviews.get(id);
                String thesaurusId = resultViews.getString("top_concept_thesaurus_id");
                String conceptId = resultViews.getString("top_concept_id");
                if(thesaurusId != null) {
                    graphObject.getExportedData().add(new ImmutablePair<>(thesaurusId, conceptId));
                }

            }

            ds.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.err.println("Error >>> " + e);
            if (!ds.isClosed()) {
                ds.close();
            }
        }
        return graphviews;
    }

    public GraphObject getView(String id){
        HikariDataSource ds = connect.getPoolConnexion();
        GraphObject view = null;
        try {
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM public.graph_view as view_data LEFT JOIN  public.graph_view_exported_concept_branch as view_concepts ON view_data.id = view_concepts.graph_view_id WHERE view_data.id = ?");
            stmt.setInt(1, Integer.parseInt(id));
            ResultSet resultView = stmt.executeQuery();

            while(resultView.next()){
                if(view == null) {
                    int dataId = resultView.getInt("id");
                    String name = resultView.getString("name");
                    String description = resultView.getString("description");
                    view = new GraphObject(dataId, name, description);
                }
                String thesaurusId = resultView.getString("top_concept_thesaurus_id");
                String conceptId = resultView.getString("top_concept_id");
                if(thesaurusId != null){
                    view.getExportedData().add(new ImmutablePair<>(thesaurusId, conceptId));
                }
            }
            ds.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.err.println("Error >>> " + e);
            if (!ds.isClosed()) {
                ds.close();
            }
        }

        return view;
    }

    public void deleteView(String id){
        HikariDataSource ds = connect.getPoolConnexion();
        boolean result = false;
        try {
            Connection con = ds.getConnection();
            PreparedStatement stmt1 = con.prepareStatement("DELETE from public.graph_view WHERE id= ?");
            stmt1.setInt(1, Integer.parseInt(id));

            PreparedStatement stmt2 = con.prepareStatement("DELETE from public.graph_view_exported_concept_branch WHERE graph_view_id= ?");
            stmt2.setInt(1, Integer.parseInt(id));

            int res1 = stmt1.executeUpdate();
            int res2 = stmt2.executeUpdate();

            ds.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.err.println("Error >>> " + e);
            if (!ds.isClosed()) {
                ds.close();
            }
        }
    }

    public boolean saveView(GraphObject view){
        HikariDataSource ds = connect.getPoolConnexion();
        boolean result = false;
        try {
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("UPDATE public.graph_view SET name = ?, description = ? WHERE id = ?");
            stmt.setString(1, view.getName());
            stmt.setString(2, view.getDescription());
            stmt.setInt(3, view.getId());
            int queryResult = stmt.executeUpdate();
            if(queryResult > 0) {
                result = true;
            }
            ds.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.err.println("Error >>> " + e);
            if (!ds.isClosed()) {
                ds.close();
            }
        }

        return result;
    }

    public int createView(GraphObject view){
        HikariDataSource ds = connect.getPoolConnexion();
        int id = -1;
        try {
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("INSERT INTO public.graph_view (name, description) VALUES (?, ?) returning id;");
            stmt.setString(1, view.getName());
            stmt.setString(2, view.getDescription());
            ResultSet result = stmt.executeQuery();
            if(result.next()) {
                id = result.getInt("id");
                System.out.println(id);
            }
            ds.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.err.println("Error >>> " + e);
            if (!ds.isClosed()) {
                ds.close();
            }
        }

        return id;
    }

    public int addDataToView(int selectedViewId, ImmutablePair<String, String> tuple){
        HikariDataSource ds = connect.getPoolConnexion();
        int id = -1;
        try {
            Connection con = ds.getConnection();
            PreparedStatement stmt = con.prepareStatement("INSERT INTO public.graph_view_exported_concept_branch (graph_view_id, top_concept_id, top_concept_thesaurus_id) VALUES (?, ?, ?) returning id;");
            stmt.setInt(1, selectedViewId);
            stmt.setString(2, tuple.right);
            stmt.setString(3, tuple.left);
            ResultSet result = stmt.executeQuery();
            if(result.next()) {
                id = result.getInt("id");
                System.out.println(id);
            }
            ds.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.err.println("Error >>> " + e);
            if (!ds.isClosed()) {
                ds.close();
            }
        }

        return id;
    }
    
    public boolean isExistDatas(int viewId, String idTheso, String idConcept){
        try(Connection conn = connect.getPoolConnexion().getConnection()){
            try(Statement stmt = conn.createStatement()){
                
                if(StringUtils.isEmpty(idConcept)){
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
                    if(resultSet.next()) {
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

    public void removeDataFromView(int selectedViewId, ImmutablePair<String, String> tuple){
        HikariDataSource ds = connect.getPoolConnexion();
        try {
            Connection con = ds.getConnection();
            PreparedStatement stmt;
            if(tuple.right == null){
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
            ds.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.err.println("Error >>> " + e);
            if (!ds.isClosed()) {
                ds.close();
            }
        }
    }
}
