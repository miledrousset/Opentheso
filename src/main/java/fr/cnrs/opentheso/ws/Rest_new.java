/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;
import java.util.ArrayList;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 * REST Web Service
 *
 * @author miled.rousset
 */
//general path = /api
@Path("/")
public class Rest_new {

    /**
     * Creates a new instance of resources La connexion est faite à chaque
     * question
     *
     */
    public Rest_new() {
    }

    private HikariDataSource connect() {
        ConnexionRest connexionRest = new ConnexionRest();
        return connexionRest.getConnexion();
    }

    /**
     * Permet de lire les préférences d'un thésaurus pour savoir si le
     * webservices est activé ou non
     *
     * @param idTheso
     */
    private boolean getStatusOfWebservices(HikariDataSource ds, String idTheso) {
        return new PreferencesHelper().isWebservicesOn(ds, idTheso);
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * recherche par Id Ark
     * Partie pour la négociation de contenu 
     * concernant les URI de type ARK avec header 
     * curl -L --header "Accept: application/rdf+xml »
     * curl -L --header "Accept: text/turtle »
     * curl -L --header "Accept: application/json »
     * curl -L --header "Accept: application/ld+json »
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////    
/*
    //Pour tester
    @Path("/add")
    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public String testPut(String content) {
        return content;
    }     
    
     */
    /**
     * pour produire du RDF-SKOS
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromArk__(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du RDF-SKOS
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}.rdf")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromArk(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du Json
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromArk__(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Json
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}.json")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromArk(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromArk__(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}.jsonld")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromArk(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromArk__(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}.ttl")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromArk(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * fin de la recherche par idArk
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////       
/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * recherche par Id Handle
     * Partie pour la négociation de contenu 
     * concernant les URI de type Handle avec header 
     * curl -L --header "Accept: application/rdf+xml »
     * curl -L --header "Accept: text/turtle »
     * curl -L --header "Accept: application/json »
     * curl -L --header "Accept: application/ld+json »
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////    
    /**
     * pour produire du RDF-SKOS
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromHandle(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du RDF-SKOS
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}.rdf")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromHandle__(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du Json
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromHandle(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Json
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}.json")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromHandle__(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromHandle(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}.jsonld")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromHandle__(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromHandle(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}.ttl")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromHandle__(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * fin de la recherche par idHandle
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////       
/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * Recherche par Id du concept
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////  
    /**
     * pour produire du RDF-SKOS
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromIdConcept__(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du RDF-SKOS
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}.rdf")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromIdConcept(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du Json
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromIdConcept__(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Json
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}.json")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromIdConcept(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonLdFromIdConcept__(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}.jsonld")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonLdFromIdConcept(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromIdConcept__(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}.ttl")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromIdConcept(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }
/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 

    /*
     * Fin de la recherche par Id du concept
     */
///////////////////////////////////////////////////// 
////////////////////////////////////////////////////

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * Trouver la valeur d'après un ID Ark concept
     */
///////////////////////////////////////////////////// 
///////////////////////////////////////////////////// 
    /**
     * pour produire du Json
     *
     * @param idLang
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/preflabel.{idLang}/{naan}/{idArk}.json")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getPrefLabelJsonFromArk(
            @PathParam("idLang") String idLang,
            @PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null || naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null || arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (idLang == null || idLang.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.getPrefLabelFromArk(ds, naan, arkId, idLang);
        ds.close();
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * Recherche par valeurs avec négociation de contenu
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////  
    /**
     * Permet de rechercher une valeur en filtrant par theso et par langue avec
     * négociation de contenu //exp curl -L --header "Accept:
     * application/rdf+xml"
     * http://localhost:8083/opentheso/api/search?q="vase&lang=fr&theso=2" curl
     * http://localhost:8083/opentheso/api/search?q=notation:nota1&theso=1&format=json
     * curl -L --header "Accept: application/rdf+xml"
     * http://localhost:8083/opentheso/api/search?q=notation:nota1&theso=1
     *
     * @param uri RDF+XML
     * @return
     */
    @Path("/search")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response searchRdf(@Context UriInfo uri) {
        String value = null;
        String idLang = "";
        String idTheso = null;
        String format = null;
        String group = "";

        String filter = null;

        String datas;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("q")) {
                    value = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    group = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }

        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (value == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }

        // vérification du filtre pour savoir si la recherche concerne des champs spécifiques 
        if (value.contains("notation:")) {
            /// rercherche par notation
            filter = "notation:";
        }
        if (value.contains("prefLabel:")) {
            /// rercherche par notation
        }

        if (format == null) {
            format = "rdf";
        }
        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
            case "turtle":
                format = "text/turtle";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
            case "json":
                format = "application/json";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * Permet de rechercher une valeur en filtrant par theso et par langue avec
     * négociation de contenu //exp curl -L --header "Accept: application/json"
     * "http://193.48.140.131:8083/opentheso/api/search?q=or&lang=fr&theso=TH_1"
     *
     * @param uri JSON
     * @return
     */
    @Path("/search")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response searchJson(@Context UriInfo uri) {
        String value = null;
        String idLang = "";
        String idTheso = null;
        String group = "";
        String format = null;
        String filter = null;

        String datas;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("q")) {
                    value = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    group = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }

        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (value == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }

        // vérification du filtre pour savoir si la recherche concerne des champs spécifiques 
        if (value.contains("notation:")) {
            /// rercherche par notation
            filter = "notation:";
        }
        if (value.contains("prefLabel:")) {
            /// rercherche par notation
        }

        if (format == null) {
            format = "json";
        }
        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
            case "turtle":
                format = "text/turtle";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
            case "json":
                format = "application/json";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * Permet de rechercher une valeur en filtrant par theso et par langue avec
     * négociation de contenu //exp curl -L --header "Accept:
     * application/ld+json"
     * "http://193.48.140.131:8083/opentheso/api/search?q=or&lang=fr&theso=TH_1"
     *
     * @param uri JSON+Ld
     * @return
     */
    @Path("/search")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response searchJsonLd(@Context UriInfo uri) {
        String value = null;
        String idLang = "";
        String idTheso = null;
        String group = "";
        String format = null;
        String filter = null;
        String datas;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("q")) {
                    value = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    group = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }
        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (value == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        // vérification du filtre pour savoir si la recherche concerne des champs spécifiques 
        if (value.contains("notation:")) {
            /// rercherche par notation
            filter = "notation:";
        }
        if (value.contains("prefLabel:")) {
            /// rercherche par notation
        }

        if (format == null) {
            format = "jsonld";
        }
        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
            case "turtle":
                format = "text/turtle";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
            case "json":
                format = "application/json";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * Permet de rechercher une valeur en filtrant par theso et par langue avec
     * négociation de contenu //exp curl -L --header "Accept: text/turtle"
     * "http://193.48.140.131:8083/opentheso/api/search?q=or&lang=fr&theso=TH_1"
     *
     * @param uri JSON+Ld
     * @return
     */
    @Path("/search")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response searchTurtle(@Context UriInfo uri) {
        String value = null;
        String idLang = "";
        String idTheso = null;
        String group = "";
        String format = null;
        String filter = null;
        String datas;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("q")) {
                    value = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    group = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }

        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (value == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }

        // vérification du filtre pour savoir si la recherche concerne des champs spécifiques 
        if (value.contains("notation:")) {
            /// rercherche par notation
            filter = "notation:";
        }
        if (value.contains("prefLabel:")) {
            /// rercherche par notation
        }

        if (format == null) {
            format = "turtle";
        }
        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
            case "turtle":
                format = "text/turtle";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
            case "json":
                format = "application/json";
                datas = getDatas(idTheso, idLang, group, value, format, filter);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
    }

    private String getDatas(
            String idTheso, String idLang, String group,
            String value,
            String format, String filter) {
        HikariDataSource ds = connect();
        if (ds == null) {
            return null;
        }
        String datas = null;
        RestRDFHelper restRDFHelper = new RestRDFHelper();

        if (filter != null) {
            switch (filter) {
                case "notation:":
                    value = value.substring(value.indexOf(":") + 1);
                    datas = restRDFHelper.findNotation(ds, idTheso, value, format);
                    ds.close();
                    return datas;
            }
        }

        datas = restRDFHelper.findConcepts(ds,
                idTheso, idLang, group, value, format);
        ds.close();
        if (datas == null) {
            return null;
        }
        return datas;
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * fin de la recherche par valeur
     */
///////////////////////////////////////////////////// 
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * Recherche par valeurs pour autocomplétion 
     * on revoie que le prefLable et l'URI en Json
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////  
    /**
     * Permet de rechercher une valeur en filtrant par theso et par langue
     * retourne une liste des valeurs (prefLabel + Uri) pour les programmes qui
     * font de l'autocompletion exp :
     * http://193.48.140.131:8083/opentheso/api/autocomplete/or?theso=TH_1&lang=fr&group=6
     *
     * @param value
     * @param uri JSON+Ld
     * @return
     */
    @Path("/autocomplete/{value}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response searchAutocomplete(@PathParam("value") String value, @Context UriInfo uri) {
        String idLang = "";
        String idTheso = null;
        String group = "";

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    group = valeur;
                }
            }
        }
        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (value == null || value.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }

        String datas = getAutocompleteDatas(idTheso, idLang, group, value);
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Une autre forme d'appel pour l'autocomplétion adaptée aux plugins type
     * Omeka-S Permet de rechercher une valeur en filtrant par theso et par
     * langue retourne une liste des valeurs (prefLabel + Uri) pour les
     * programmes qui font de l'autocompletion exp :
     * http://193.48.140.131:8083/opentheso/api/autocomplete?theso=TH_1&value=vase&lang=fr&group=6
     *
     * @param uri JSON+Ld
     * @return
     */
    @Path("/autocomplete")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response searchAutocomplete2(@Context UriInfo uri) {
        String idLang = "";
        String value = null;
        String idTheso = null;
        String group = "";

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("value")) {
                    value = valeur;
                }
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    group = valeur;
                }
            }
        }
        if (value == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        String datas = getAutocompleteDatas(idTheso, idLang, group, value);
        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    private String getAutocompleteDatas(String idTheso,
            String idLang, String group,
            String value) {
        HikariDataSource ds = connect();
        if (ds == null) {
            return null;
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.findAutocompleteConcepts(ds,
                idTheso, idLang, group, value);
        ds.close();
        if (datas == null) {
            return null;
        }
        return datas;
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * Fin de la recherche par valeurs pour autocomplétion 
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////  
/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * Fonctions avancées pour retourner une branche complète
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////      
    /**
     * Pour retourner une branche complète à partir d'un concept en SKOS mais en
     * remontant la branche par les BT (termes génériques)
     * http://localhost:8082/opentheso2/api/expansion/concept?id=30&theso=th1&way=down
     * http://localhost:8082/opentheso2/api/expansion/concept?id=30&theso=th1&way=down&format=json
     * @param uri
     * @return
     */
    @Path("expansion/concept")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getBrancheOfConcepts(@Context UriInfo uri) {
        String idConcept = null;
        String idTheso = null;
        String way = null;
        String format = null;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("id")) {
                    idConcept = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("way")) {
                    way = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }                
            }
        }
        if (idTheso == null || idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        
        if (format == null) {
            format = "rdf";
        }
        String datas;

        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getBranchOfConcepts(idConcept, idTheso, way, format);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getBranchOfConcepts(idConcept, idTheso, way, format);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
            case "turtle":
                format = "text/turtle";
                datas = getBranchOfConcepts(idConcept, idTheso, way, format);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
            case "json":
                format = "application/json";
                datas = getBranchOfConcepts(idConcept, idTheso, way, format);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
    }

    private String getBranchOfConcepts(String idConcept,
            String idTheso, String way, String format) {
        String datas = null;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }   RestRDFHelper restRDFHelper = new RestRDFHelper();
            // sens de récupération des concepts vers le haut
            if (way.equalsIgnoreCase("top")) {
                datas = restRDFHelper.brancheOfConceptsTop(ds,
                        idConcept, idTheso, format);
            }   // sens de récupération des concepts vers le bas        
            if (way.equalsIgnoreCase("down")) {
                datas = restRDFHelper.brancheOfConceptsDown(ds,
                        idConcept, idTheso, format);
            }
        }
        if (datas == null) {
            return null;
        }
        return datas;
    }

    /**
     * Pour retourner une branche complète à partir d'un identifiant d'un groupe
     *
     * @param uri
     * @return
     */
    @Path("all/group")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getAllBrancheOfGroup(@Context UriInfo uri) {
        String idGroup = null;
        String idTheso = null;
        String format = null;
        String datas;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("id")) {
                    idGroup = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }

        if (idTheso == null || idGroup == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (format == null) {
            format = "rdf";
        }
        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getAllBrancheOfGroup__(idTheso, idGroup, format);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getAllBrancheOfGroup__(idTheso, idGroup, format);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
            case "turtle":
                format = "text/turtle";
                datas = getAllBrancheOfGroup__(idTheso, idGroup, format);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
            case "json":
                format = "application/json";
                datas = getAllBrancheOfGroup__(idTheso, idGroup, format);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
    }

    private String getAllBrancheOfGroup__(String idtheso,
            String idGroup, String format) {
        HikariDataSource ds = connect();
        String datas;
        if (ds == null) {
            return null;
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        datas = restRDFHelper.brancheOfGroup(ds, idGroup, idtheso, format);

        ds.close();
        if (datas == null) {
            return null;
        }
        return datas;
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * inforamtions sur le thésaurus
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////      
    /**
     * Pour retourner la liste des thésaurus publics ou la liste des collections
     * d'un thésaurus
     * https://pactols.frantiq.fr/opentheso/api/info/list?theso=all
     * https://pactols.frantiq.fr/opentheso/api/info/list?theso=th1&group=all
     * https://pactols.frantiq.fr/opentheso/api/info/list?theso=th1&topconcept=all
     * 
     * @param uri
     * @return
     */
    @Path("info/list")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getlistAllPublicTheso(@Context UriInfo uri) {
        String idTheso = null;
        String group = null;
        String topconcept = null;
        
        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    group = valeur;
                    if (!group.trim().equalsIgnoreCase("all")) {
                        group = null;
                    }
                }
                if (e.getKey().equalsIgnoreCase("topconcept")) {
                    topconcept = valeur;
                    if (!topconcept.trim().equalsIgnoreCase("all")) {
                        topconcept = null;
                    }
                }                
            }
        }

        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        String datas = null;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
            }
            if (group == null && topconcept == null) {
                if (!idTheso.equalsIgnoreCase("all")) {
                    return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                datas = getlistAllPublicTheso__(ds);
            } else {
                if(group != null && group.equalsIgnoreCase("all"))
                    datas = getlistAllGroupOfTheso__(ds, idTheso);
                if(topconcept != null && topconcept.equalsIgnoreCase("all"))
                    datas = getlistAllTopConceptOfTheso__(ds, idTheso);                
            }
        }

        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    private String getlistAllPublicTheso__(HikariDataSource ds) {
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        List<String> listPublicTheso = thesaurusHelper.getAllIdOfThesaurus(ds, false);

        NodeThesaurus nodeThesaurus;

        String datasJson;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idTheso : listPublicTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idTheso", idTheso);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeThesaurus = thesaurusHelper.getNodeThesaurus(ds, idTheso);
            for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {
                JsonObjectBuilder jobLang = Json.createObjectBuilder();
                jobLang.add("lang", thesaurus.getLanguage());
                jobLang.add("title", thesaurus.getTitle());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeThesaurus.getListThesaurusTraduction().isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }
            jsonArrayBuilder.add(job.build());
        }
        datasJson = jsonArrayBuilder.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }

    private String getlistAllGroupOfTheso__(HikariDataSource ds, String idTheso) {
        GroupHelper groupHelper = new GroupHelper();
        List<String> listIdGroupOfTheso = groupHelper.getListIdOfGroup(ds, idTheso);

        ArrayList<NodeGroupTraductions> nodeGroupTraductions;

        String datasJson;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idGroup : listIdGroupOfTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idGroup", idGroup);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeGroupTraductions = groupHelper.getAllGroupTraduction(ds, idGroup, idTheso);
            for (NodeGroupTraductions nodeGroupTraduction : nodeGroupTraductions) {
                JsonObjectBuilder jobLang = Json.createObjectBuilder();
                jobLang.add("lang", nodeGroupTraduction.getIdLang());
                jobLang.add("title", nodeGroupTraduction.getTitle());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeGroupTraductions.isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }
            jsonArrayBuilder.add(job.build());
        }
        datasJson = jsonArrayBuilder.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }
    
    private String getlistAllTopConceptOfTheso__(HikariDataSource ds, String idTheso) {
        ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();
        
        List<String> listIdTopConceptOfTheso = conceptHelper.getAllTopTermOfThesaurus(ds, idTheso);
       
        ArrayList<NodeTermTraduction> nodeTermTraductions;

        String datasJson;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idConcept : listIdTopConceptOfTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idConcept", idConcept);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeTermTraductions = termHelper.getAllTraductionsOfConcept(ds, idConcept, idTheso);
            for (NodeTermTraduction nodeTermTraduction : nodeTermTraductions) {
                JsonObjectBuilder jobLang = Json.createObjectBuilder();
                jobLang.add("lang", nodeTermTraduction.getLang());
                jobLang.add("title", nodeTermTraduction.getLexicalValue());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeTermTraductions.isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }
            jsonArrayBuilder.add(job.build());
        }
        datasJson = jsonArrayBuilder.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }    

    /**
     * Pour retourner la liste des langues d'un thésaurus
     * https://pactols.frantiq.fr/opentheso/api/info/list?theso=TH_1&lang=all
     *
     * @param uri
     * @return
     */
    @Path("info/listLang")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getlistLangOfTheso(@Context UriInfo uri) {
        String idTheso = null;
        String lang = null;
        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }

            }

        }
        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (idTheso.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        String datas = getlistLangOfTheso__(ds, idTheso);
        ds.close();

        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    private String getlistLangOfTheso__(HikariDataSource ds, String idTheso) {
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        ArrayList<String> listLangOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurus(ds, idTheso);

        String datasJson;

        JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

        for (String idLang : listLangOfTheso) {
            JsonObjectBuilder jobLang = Json.createObjectBuilder();
            jobLang.add("lang", idLang);
            jsonArrayBuilderLang.add(jobLang.build());
        }
        datasJson = jsonArrayBuilderLang.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }

    /**
     * Pour retourner la dernière date de modification
     *
     * @param uri
     * @return
     */
    @Path("info/lastupdate")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getInfoLastUpdate(@Context UriInfo uri) {
        String idTheso = null;
        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
            }
        }
        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        String datas = getInfoLastUpdate__(ds, idTheso);
        ds.close();

        if (datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    private String getInfoLastUpdate__(HikariDataSource ds, String idTheso) {
        ConceptHelper conceptHelper = new ConceptHelper();
        Date date = conceptHelper.getLastModifcation(ds, idTheso);
        if (date == null) {
            return messageEmptyJson();
        }

        return "{\"lastUpdate\": \"" + date.toString() + "\"}";
    }

    private String messageEmptySkos() {
        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rdf:RDF\n"
                + "	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">"
                + "</rdf:RDF>";

        return message;
    }

    private String messageEmptyJson() {

        String message = "{\n"
                + "}";

        return message;
    }

    private String messageEmptyTurtle() {
        String message = "";

        return message;
    }
}
