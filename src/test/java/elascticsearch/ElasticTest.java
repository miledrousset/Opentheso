/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package elascticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
*/
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;


/**
 *
 * @author miledrousset
 */
public class ElasticTest {
    
    public ElasticTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void Index() {
        
        // URL and API key
        String serverUrl = "http://localhost:9200";
        //String apiKey = "VnVhQ2ZHY0JDZGJrU...";

        // Create the low-level client
        RestClient restClient = RestClient
            .builder(HttpHost.create(serverUrl))
//            .setDefaultHeaders(new Header[]{
  //              new BasicHeader("Authorization", "ApiKey " + apiKey)
//            })
            .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
            restClient, new JacksonJsonpMapper());

        // And create the API client
        ElasticsearchClient esClient = new ElasticsearchClient(transport);        
        
        try {
           esClient.indices().delete(c -> c
                    .index("products2")
            );            
           esClient.indices().create(c -> c
                    .index("products2")
            );
            
            Product product = new Product("3001", "amphore", "vase", "fr");

            IndexResponse response = esClient.index(i -> i
                .index("products2")
                .id(product.getId())
                .document(product)
            );
            Product product2 = new Product("3002", "amphora", "vessel", "en");
            response = esClient.index(i -> i
                .index("products2")
                .id(product2.getId())
                .document(product2)
            );            

            System.out.println("Indexed with version " + response.version());

            
            
            /*        try (RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder("127.0.0.1:9200"))) {
            
            // Créez un document JSON à indexer
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("prefLabel", "amphore");
            jsonMap.put("altLabel", "vase");

            // Préparez la requête d'indexation
            IndexRequest request = new IndexRequest("concept1")
            .id("300") // facultatif : spécifiez un ID personnalisé
            .source(jsonMap);
            
            // Envoyez la requête d'indexation
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);

            // Traitez la réponse...
            System.out.println("Indexation réussie. ID du document : " + response.getId());
            
            } catch (IOException e) {
            e.printStackTrace();
            }*/
        } catch (IOException ex) {
            Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ElasticsearchException ex) {
            Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    @Test
    public void Find() {
        try {
            String serverUrl = "http://localhost:9200";
            //String apiKey = "VnVhQ2ZHY0JDZGJrU...";
            
            // Create the low-level client
            RestClient restClient = RestClient
                    .builder(HttpHost.create(serverUrl))
//            .setDefaultHeaders(new Header[]{
                    //              new BasicHeader("Authorization", "ApiKey " + apiKey)
//            })
                    .build();
            
            // Create the transport with a Jackson mapper
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());
            
            // And create the API client
            ElasticsearchClient esClient = new ElasticsearchClient(transport);
            
            String searchText = "amphore";
            //GetResponse<Product> response;
            SearchResponse<Product> response = esClient.search(s -> s
                    .index("products2")
                    .query(q -> q
                            .match(t -> t
                                    .field("prefLabel")
                                    .query(searchText)
                            )
                    ),
                    Product.class
            );
            

            String toString = response.toString();
            
            GetResponse<Product> response2;
            try {
                response2 = esClient.get(g -> g
                        .index("products2")
                        .id("300"),
                        Product.class
                );
                if (response2.found()) {
                    Product product = response2.source();
                    if(product != null)
                        System.out.println("Product name " + product.getPrefLabel() + " " + product.getAltLabel() + " " + product.getId());
                } else {
                    System.out.println("Product not found");
                }
                
            } catch (IOException ex) {
                Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ElasticsearchException ex) {
                Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ElasticsearchException ex) {
            Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
        }


    }    
    
    
    /**
     * Cette fonction marche bien pour faire la recherche 
     */
    @Test
    public void searchExact(){
        try {
            String serverUrl = "http://localhost:9200";
            RestClient restClient = RestClient
                    .builder(HttpHost.create(serverUrl))
                    .build();
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());
            
            ElasticsearchClient esClient = new ElasticsearchClient(transport);
            
            String searchText = "vase";
            
            SearchResponse<Product> response = esClient.search(s -> s
                    .index("products2")
                    .query(q -> q
                            .match(t -> t
                                    .field("altLabel")
                                    .query(searchText)
                            )
                    ),
                    Product.class
            );
            
            TotalHits total = response.hits().total();
            boolean isExactResult = total.relation() == TotalHitsRelation.Eq;
            
            if (isExactResult) {
                System.out.println("There are " + total.value() + " results");
            } else {
                System.out.println("There are more than " + total.value() + " results");
            }
            
            List<Hit<Product>> hits = response.hits().hits();
            for (Hit<Product> hit: hits) {
                Product product = hit.source();
                System.out.println("Found product " + product.getPrefLabel() + ", score " + hit.score());
            }
        } catch (IOException ex) {
            Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ElasticsearchException ex) {
            Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
    
    /**
     * Cette fonction marche bien pour faire la recherche 
     */
    @Test
    public void searchContain(){
        try {
            String serverUrl = "http://localhost:9200";
            RestClient restClient = RestClient
                    .builder(HttpHost.create(serverUrl))
                    .build();
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());
            
            ElasticsearchClient esClient = new ElasticsearchClient(transport);
            
            String searchText = "amp*";//"vas*";
           
            /*
            SearchResponse<Product> response = esClient.search(s -> s
                    .index("products")
                    .query(q -> q
                            .queryString(t -> t
                                    .fields("altLabel", "prefLabel")
                                    .query(searchText)
                            )
                    ),
                    Product.class
            );
            */
            
            SearchResponse<Product> response = esClient.search(s -> s
                    .index("products2")
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m
                                            .queryString(t -> t
                                                    .fields("altLabel", "prefLabel")
                                                    .query(searchText)
                                            )
                                    )
//                                    .must(m -> m
//                                            .term(t -> t
//                                                    .field("lang")
//                                                    .value("fr")
//                                            )
//                                    )
                            )
                    ),
                    Product.class
            );                   
            
            /*
            SearchResponse<Product> response = esClient.search(s -> s
                    .index("products2")
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m
                                            .queryString(t -> t
                                                    .fields("altLabel", "prefLabel")
                                                    .query(searchText)
                                            )
                                    )
                                    .must(m -> m
                                            .term(t -> t
                                                    .field("lang")
                                                    .value("fr")
                                            )
                                    )
                            )
                    ),
                    Product.class
            );       */     
            
            
            TotalHits total = response.hits().total();
            boolean isExactResult = total.relation() == TotalHitsRelation.Eq;
            
            if (isExactResult) {
                System.out.println("There are " + total.value() + " results");
            } else {
                System.out.println("There are more than " + total.value() + " results");
            }
            
            List<Hit<Product>> hits = response.hits().hits();
            for (Hit<Product> hit: hits) {
                Product product = hit.source();
                System.out.println("Found product " + product.getPrefLabel() + ", score " + hit.score());
            }
        } catch (IOException ex) {
            Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ElasticsearchException ex) {
            Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }    
    
    
    @Test 
    public void deleteIndex(){
        try {
            String serverUrl = "http://localhost:9200";
            RestClient restClient = RestClient
                    .builder(HttpHost.create(serverUrl))
                    .build();
            ElasticsearchTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper());
            
            ElasticsearchClient esClient = new ElasticsearchClient(transport);            
            
            
       //     esClient.delete(request)lete("products", "300");
       
       
            // suppression d'une référence / un concept
            DeleteRequest request = DeleteRequest.of(d -> d.index("products2").id("301"));
            DeleteResponse response = esClient.delete(request);
            System.out.println("  " + response.toString());
            
            request = DeleteRequest.of(d -> d.index("products2").id("301"));
            response = esClient.delete(request);
            System.out.println("  " + response.toString());           
            
            request = DeleteRequest.of(d -> d.index("products2"));
            response = esClient.delete(request);
            System.out.println("  " + response.toString());               


            /// suppression d'un index complet / un thésaurus complet
           esClient.indices().delete(c -> c
                    .index("products2")
            );  

            
            
            
            
        } catch (ElasticsearchException exception) {

        } catch (IOException ex) {
            Logger.getLogger(ElasticTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
