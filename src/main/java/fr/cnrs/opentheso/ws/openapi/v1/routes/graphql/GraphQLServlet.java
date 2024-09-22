package fr.cnrs.opentheso.ws.openapi.v1.routes.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;

import javax.annotation.PostConstruct;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.repositories.SearchHelper;
import fr.cnrs.opentheso.repositories.DaoResourceHelper;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;



@RestController
@RequestMapping("/openapi/v1/graphql")
public class GraphQLServlet {

    @Autowired
    private SearchHelper searchHelper;

    @Autowired
    private DaoResourceHelper daoResourceHelper;

    @Autowired
    private Connect connect;

    private GraphQL graphQL;
    private final Gson gson = new Gson();

    @PostConstruct
    public void init() {
        GraphQLSchema schema = buildSchema();
        this.graphQL = GraphQL.newGraphQL(schema).build();
    }

    private GraphQLSchema buildSchema() {
        // Configuration des schémas comme dans l'original
        // Type ConceptLabel
        GraphQLObjectType conceptLabelType = GraphQLObjectType.newObject()
                .name("ConceptLabel")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idTerm").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("id").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("label").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idLang").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("codeFlag").type(Scalars.GraphQLString))
                .build();

        // Création des autres types GraphQL similaires
        // ...

        // Déclaration du type Query pour les requêtes
        GraphQLObjectType queryType = GraphQLObjectType.newObject()
                .name("Query")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("fullConcept")
                        .type(conceptLabelType)
                        .argument(GraphQLArgument.newArgument().name("idTheso").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idConcept").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idLang").type(Scalars.GraphQLString))
                        .dataFetcher(env -> {
                            String idTheso = env.getArgument("idTheso");
                            String idConcept = env.getArgument("idConcept");
                            String idLang = env.getArgument("idLang");
                            return daoResourceHelper.getFullConcept(connect.getPoolConnexion(), idTheso, idConcept, idLang, -1, -1);
                        })
                )
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("searchConcepts")
                        .type(new GraphQLList(conceptLabelType))
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
                            List<String> autoCompletions = searchHelper.searchAutoCompletionWSForWidget(connect.getPoolConnexion(), value, idLang, idGroups, idTheso);
                            List<Object> fullConcepts = new ArrayList<>();
                            for (String autoCompletion : autoCompletions) {
                                fullConcepts.add(daoResourceHelper.getFullConcept(connect.getPoolConnexion(), idTheso, autoCompletion, idLang, -1, -1));
                            }
                            return fullConcepts;
                        })
                )
                .build();

        return GraphQLSchema.newSchema()
                .query(queryType)
                .build();
    }

    @PostMapping
    public String executeGraphQLQuery(@RequestBody Map<String, Object> requestMap) {
        String query = (String) requestMap.get("query");

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);
        return gson.toJson(executionResult.toSpecification());
    }
}
