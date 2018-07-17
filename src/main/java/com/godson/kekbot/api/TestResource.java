package com.godson.kekbot.api;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.Profile;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/* Root resource (exposed at "myresource" path) */
@Path("test")
public class TestResource {

    /*
    @GET
    @Produces({ "image/png", MediaType.TEXT_PLAIN })
    public Response getIt(@QueryParam("id") long id) {
        try {
            return Response.ok(Profile.getProfile(KekBot.jda.getUserById(id)).drawCard()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("you fucked up").build();
        }
    }*/

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response test(String json, @HeaderParam("Authorization") String auth) {
        JSONObject object = new JSONObject(json);
        if (!auth.equalsIgnoreCase("pineapple")) {
            return Response.status(Response.Status.FORBIDDEN).entity("either wrong fucking token or no token at all smh").build();
        } else {
            try {
                KekBot.jda.getUserById("99405418077364224").openPrivateChannel().queue(c -> c.sendMessage("holy fuck test compreete").queue());
                return Response.ok().entity("succ").build();
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("you fucked up").build();
            }
        }
    }
}
