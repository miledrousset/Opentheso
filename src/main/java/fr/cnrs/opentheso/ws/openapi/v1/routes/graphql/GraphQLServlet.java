package fr.cnrs.opentheso.ws.openapi.v1.routes.graphql;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.dao.DaoResourceHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAutoCompletion;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;

@WebServlet(name = "GraphQLServlet", urlPatterns = {"/graphql"})
public class GraphQLServlet extends HttpServlet {
    private final GraphQL graphQL;
    private final Gson gson;
    private HikariDataSource ds; // Added data source as a class member

    public GraphQLServlet() {
        this.gson = new Gson();
        this.ds = connect(); // Initialize data source

        // Define your GraphQL schema here
        GraphQLSchema schema = buildSchema();
        this.graphQL = GraphQL.newGraphQL(schema).build();
    }

    private GraphQLSchema buildSchema() {
        // Define all the types
        GraphQLObjectType conceptLabelType = GraphQLObjectType.newObject()
                .name("ConceptLabel")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idTerm").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("id").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("label").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idLang").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("codeFlag").type(Scalars.GraphQLString))
                .build();

        GraphQLObjectType conceptRelationType = GraphQLObjectType.newObject()
                .name("ConceptRelation")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("Uri").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("label").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idConcept").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("role").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("status").type(Scalars.GraphQLString))
                .build();

        GraphQLObjectType conceptNoteType = GraphQLObjectType.newObject()
                .name("ConceptNote")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idNote").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idLang").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("label").type(Scalars.GraphQLString))
                .build();

        GraphQLObjectType resourceGPSType = GraphQLObjectType.newObject()
                .name("ResourceGPS")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("latitude").type(Scalars.GraphQLFloat))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("longitude").type(Scalars.GraphQLFloat))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("position").type(Scalars.GraphQLInt))
                .build();

        GraphQLObjectType conceptImageType = GraphQLObjectType.newObject()
                .name("ConceptImage")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("id").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("imageName").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("copyRight").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("uri").type(Scalars.GraphQLString))
                .build();

        GraphQLObjectType conceptIdLabelType = GraphQLObjectType.newObject()
                .name("ConceptIdLabel")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("uri").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("identifier").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("label").type(Scalars.GraphQLString))
                .build();

        GraphQLObjectType conceptCustomRelationType = GraphQLObjectType.newObject()
                .name("ConceptCustomRelation")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("targetConcept").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("targetLabel").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("relation").type(Scalars.GraphQLString))
                .build();

        // Define the main type
        GraphQLObjectType nodeFullConceptType = GraphQLObjectType.newObject()
                .name("NodeFullConcept")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("uri").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("resourceType").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("identifier").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("permanentId").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("notation").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("resourceStatus").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("created").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("modified").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("creatorName").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("contributorName").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("prefLabel").type(conceptLabelType))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("altLabels").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("hiddenLabels").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("prefLabelsTraduction").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("altLabelTraduction").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("hiddenLabelTraduction").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("narrowers").type(new GraphQLList(conceptRelationType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("broaders").type(new GraphQLList(conceptRelationType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("relateds").type(new GraphQLList(conceptRelationType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("notes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("definitions").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("examples").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("editorialNotes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("changeNotes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("scopeNotes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("historyNotes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("exactMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("closeMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("broadMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("relatedMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("narrowMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("gps").type(new GraphQLList(resourceGPSType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("images").type(new GraphQLList(conceptImageType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("membres").type(new GraphQLList(conceptIdLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("replacedBy").type(new GraphQLList(conceptIdLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("replaces").type(new GraphQLList(conceptIdLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("facets").type(new GraphQLList(conceptIdLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("nodeCustomRelations").type(new GraphQLList(conceptCustomRelationType)))
                .build();

        // Define a new type to encapsulate the search results and the count
        GraphQLObjectType searchResultsType = GraphQLObjectType.newObject()
                .name("SearchResults")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("count").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("results").type(new GraphQLList(nodeFullConceptType)))
                .build();

        GraphQLObjectType queryType;
        // Define the query type
        queryType = GraphQLObjectType.newObject()
                .name("Query")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("fullConcept")
                        .type(nodeFullConceptType)
                        .argument(GraphQLArgument.newArgument().name("idTheso").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idConcept").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idLang").type(Scalars.GraphQLString))
                        .dataFetcher(env -> {
                            String idTheso = env.getArgument("idTheso");
                            String idConcept = env.getArgument("idConcept");
                            String idLang = env.getArgument("idLang");
                            DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
                            return daoResourceHelper.getFullConcept(ds, idTheso, idConcept, idLang);
                        })
                )
                .field(GraphQLFieldDefinition.newFieldDefinition() // New query for searching concepts
                        .name("searchConcepts")
                        .type(searchResultsType)
                        .argument(GraphQLArgument.newArgument().name("value").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idLang").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idGroups").type(new GraphQLList(Scalars.GraphQLString)))
                        .argument(GraphQLArgument.newArgument().name("idTheso").type(Scalars.GraphQLString))
                        .dataFetcher(env -> {
                            String value = env.getArgument("value");
                            String idLang = env.getArgument("idLang");
                            List<String> idGroupsList = env.getArgument("idGroups");
                            String idTheso = env.getArgument("idTheso");
                            String[] idGroups = idGroupsList != null ? idGroupsList.toArray(new String[0]) : null;
                            SearchHelper searchHelper = new SearchHelper();
                            List<String> autoCompletions = searchHelper.searchAutoCompletionWSForWidget(ds, value, idLang, idGroups, idTheso);
                            DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
                            List<Object> fullConcepts = new ArrayList<>();
                            for (String autoCompletion : autoCompletions) {
                                fullConcepts.add(daoResourceHelper.getFullConcept(ds, idTheso, autoCompletion, idLang));
                            }
                            return Map.of("count", fullConcepts.size(), "results", fullConcepts);
                        })
                )
                .build();

        return GraphQLSchema.newSchema()
                .query(queryType)
                .build();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Read and parse the JSON request body
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        // Convert JSON to Map
        Map<String, Object> requestMap = gson.fromJson(stringBuilder.toString(), Map.class);
        String query = (String) requestMap.get("query");

        // Execute GraphQL query
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        // Convert execution result to JSON response
        String jsonResponse = gson.toJson(executionResult.toSpecification());

        // Write JSON response back to client
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse);
    }

}