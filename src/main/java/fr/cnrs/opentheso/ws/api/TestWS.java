/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.api;

import fr.cnrs.opentheso.core.json.helper.JsonHelper;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author miledrousset
 */
@Path("/bonjour")
public class TestWS {

    /////// TEST TEST TEST /////////////
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String direBonjour() {
        return "Bonjour, tout le monde!";
    }

    /////// TEST TEST TEST /////////////       

    @Path("/testput")
    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public String addArk(String content) {
        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jo;
        try {
            jo = jsonHelper.getJsonObject(content);
            return jo.toString();
        } catch (Exception e) {
            return "Erreur";
        }

    }

}
