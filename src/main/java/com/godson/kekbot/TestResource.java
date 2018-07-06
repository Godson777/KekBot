package com.godson.kekbot;

import com.godson.kekbot.profile.Profile;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/* Root resource (exposed at "myresource" path) */
@Path("test")
public class TestResource {

    @GET
    @Produces({ MediaType.TEXT_PLAIN, "image/png" })
    public Response getIt(@QueryParam("id") long id) {
        try {
            return Response.ok(Profile.getProfile(KekBot.jda.getUserById(id)).drawCard()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("you fucked up").build();
        }
    }
}
