
package fr.cnrs.opentheso.bean.graph;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author miledrousset
 */

@Named
@SessionScoped
public class GraphService implements Serializable{

    @Inject
    private Connect connect;

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return graphviews;
    }

    public GraphObject getView(String id){
        HikariDataSource ds = connect.getPoolConnexion();
        GraphObject view = null;
        try {
            Connection con = ds.getConnection();
            ResultSet resultView = con.prepareStatement("SELECT * FROM public.graph_view as view_data LEFT JOIN  public.graph_view_exported_concept_branch as view_concepts ON view_data.id = view_concepts.graph_view_id WHERE view_data.id ="+id).executeQuery();

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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return view;
    }
}
