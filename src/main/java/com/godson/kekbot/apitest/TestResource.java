package com.godson.kekbot.apitest;

import com.godson.kekbot.KekBot;
import com.godson.kekbot.profile.Profile;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/* Root resource (exposed at "myresource" path) */
@Path("test")
public class TestResource {

    @GET
    @Produces({ "image/png", MediaType.TEXT_PLAIN })
    public Response getIt(@QueryParam("id") long id) {
        try {
            return Response.ok(Profile.getProfile(KekBot.jda.getUserById(id)).drawCard()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("you fucked up").build();
        }
    }

    /**
     * leftover code from doing discordbots.org integration, will reuse later for daily bonuses.
     */
    /*@POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response test(String json, @HeaderParam("Authorization") String auth) {
        //JSONObject object = new JSONObject(json);
        if (!auth.equalsIgnoreCase("pineapple")) {
            return Response.status(Response.Status.FORBIDDEN).entity("either wrong fucking token or no token at all smh").build();
        } else {
            try {
                KekBot.jda.getUserById("99405418077364224").openPrivateChannel().queue(c -> c.sendMessage("holy fuck test compreete" + "\n\ndata recieved: \n" + json).queue());
                System.out.println("API TEST: succ");
                return Response.ok().entity("succ").build();
            } catch (Exception e) {
                System.out.println("API TEST: user fucked up somehow");
                System.out.println("API TEST: data provided: \n" + json);
                return Response.status(Response.Status.BAD_REQUEST).entity("you fucked up").build();
            }
        }
    }*/
}
